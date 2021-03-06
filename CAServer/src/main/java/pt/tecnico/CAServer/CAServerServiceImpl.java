package pt.tecnico.CAServer;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.spec.SecretKeySpec;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;

import io.grpc.stub.StreamObserver;
import pt.tecnico.Common.CryptographyImpl;
import pt.tecnico.grpc.CAServerServiceGrpc;
import pt.tecnico.grpc.CaServer;

public class CAServerServiceImpl extends CAServerServiceGrpc.CAServerServiceImplBase{
    String keyStorePath = System.getProperty("user.home") + "/Documents/CAServer/";
    char[] pwdArray = null;
    KeyStore ks = null;
    Key caPrivateKey = null;
    Key caPublicKey = null;

    X509Certificate cert = null;
    private static final String CERTIFICATE_DN = "CN=cn, O=o, L=L, ST=il, C= c";

    @Override
    public void generateKeyPair(CaServer.EncryptedCAMessageRequest request, StreamObserver<CaServer.EncryptedCAMessageResponse> responseObserver) {
        byte[] requestDecryptedBytes = DecryptRequest(request);

        
        CaServer.GenerateKeyPairRequest decryptedRequest = null;
        try {
            decryptedRequest = CaServer.GenerateKeyPairRequest.parseFrom(requestDecryptedBytes);
        } catch (InvalidProtocolBufferException ipbe) {
            System.out.println("Failed to parse the request");
            // responseObserver.onNext(EncryptResponse(CaServer.EncryptedCAMessageResponse.getDefaultInstance()));
            responseObserver.onNext(CaServer.EncryptedCAMessageResponse.getDefaultInstance());
            responseObserver.onCompleted();
            return;
        }
        
        try {
            //TODO: Check if this makes sense to do? Maybe this is only worth doing when username == "ca"
            if(ks.containsAlias(decryptedRequest.getUserName() + "_private_key") || 
            ks.containsAlias(decryptedRequest.getUserName() + "_certificate")) {
                System.out.println("Attempt to recreate already existing keys");
                responseObserver.onNext(EncryptResponse(CaServer.EncryptedCAMessageResponse.getDefaultInstance(), decryptedRequest.getUserName()));
                responseObserver.onCompleted();
                return;
            }
        } catch (KeyStoreException e) {
            System.out.println("Error when checking existing keys");
            responseObserver.onNext(EncryptResponse(CaServer.EncryptedCAMessageResponse.getDefaultInstance(), decryptedRequest.getUserName()));
            responseObserver.onCompleted();
            return;
        }

        KeyPair keyPair = CryptographyImpl.generateRSAKeyPair();
        X509Certificate clientCert = CryptographyImpl.generateSelfSignedCertificate(keyPair, CERTIFICATE_DN, decryptedRequest.getUserName());

        try {
            X509Certificate[] certificateChain = new X509Certificate[2];
            certificateChain[0] = clientCert;
            certificateChain[1] = cert;
            ks.setKeyEntry(decryptedRequest.getUserName() + "_private_key", caPrivateKey, pwdArray, certificateChain);
            ks.setCertificateEntry(decryptedRequest.getUserName() + "_certificate", clientCert);

            CryptographyImpl.UpdateKeyStore(ks, pwdArray, keyStorePath + "keystore.jceks");

            CaServer.GenerateKeyPairResponse.Builder resBuilder = CaServer.GenerateKeyPairResponse.newBuilder()
                                                        .setAck("OK")
                                                        .setCertificate(ByteString.copyFrom(clientCert.getEncoded()))
                                                        .setPrivateKey(ByteString.copyFrom(keyPair.getPrivate().getEncoded()));

            for (X509Certificate x509Certificate : certificateChain) {
                resBuilder.addCertificateChain(ByteString.copyFrom(x509Certificate.getEncoded()));
            }
    
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey pubKeyDecoded = kf.generatePublic(new X509EncodedKeySpec(decryptedRequest.getTempPublicKey().toByteArray()));

            responseObserver.onNext(EncryptResponse(resBuilder.build(), decryptedRequest.getUserName(), pubKeyDecoded));
            responseObserver.onCompleted();
        } catch (Exception e) {
            System.out.println("Error generating keys: " + e.getLocalizedMessage());
            responseObserver.onNext(EncryptResponse(CaServer.EncryptedCAMessageResponse.getDefaultInstance(), decryptedRequest.getUserName()));
            responseObserver.onCompleted();
        } 
    }

    @Override
    public void requestPublicKey(CaServer.EncryptedCAMessageRequest request, StreamObserver<CaServer.EncryptedCAMessageResponse> responseObserver) {
        byte[] requestDecryptedBytes = DecryptRequest(request);

        CaServer.PublicKeyRequest decryptedRequest = null;
        try {
            decryptedRequest = CaServer.PublicKeyRequest.parseFrom(requestDecryptedBytes);
        } catch (InvalidProtocolBufferException ipbe) {
            System.out.println("Failed to parse the request");
            responseObserver.onNext(CaServer.EncryptedCAMessageResponse.getDefaultInstance());
            responseObserver.onCompleted();
            return;
        }
        
        if(!decryptedRequest.getUserName().isEmpty() && !VerifyRequest(requestDecryptedBytes, request.getDigitalSignature().toByteArray(), decryptedRequest.getUserName())) {
            System.out.println("Digital signature missmatch");
            CaServer.PublicKeyResponse res = CaServer.PublicKeyResponse.newBuilder().setAck("ERROR").build();
            responseObserver.onNext(EncryptResponse(res, decryptedRequest.getUserName()));
            responseObserver.onCompleted();
            return;
        }

        try {
            if(ks.containsAlias(decryptedRequest.getTarget() + "_certificate")) {
                Key key = ks.getCertificate(decryptedRequest.getTarget() + "_certificate").getPublicKey();

                CaServer.PublicKeyResponse res = CaServer.PublicKeyResponse.newBuilder()
                                                    .setAck("OK")
                                                    .setPublicKey(ByteString.copyFrom(key.getEncoded()))
                                                    .build();


                if(decryptedRequest.getTempPublicKey().size() != 0) {
                    KeyFactory kf = KeyFactory.getInstance("RSA");
                    PublicKey pubKeyDecoded = kf.generatePublic(new X509EncodedKeySpec(decryptedRequest.getTempPublicKey().toByteArray()));

                    responseObserver.onNext(EncryptResponse(res, decryptedRequest.getUserName(), pubKeyDecoded));
                    responseObserver.onCompleted();
                }
                else {
                    responseObserver.onNext(EncryptResponse(res, decryptedRequest.getUserName()));
                    responseObserver.onCompleted();
                }

                return;
            }
            else {
                System.out.println("No Keys for target: " + decryptedRequest.getTarget());
                responseObserver.onNext(CaServer.EncryptedCAMessageResponse.getDefaultInstance());
                responseObserver.onCompleted();
            }
        } 
        catch (KeyStoreException e) {
            System.out.println("ERROR - Error when checking existing keys");

            responseObserver.onNext(CaServer.EncryptedCAMessageResponse.getDefaultInstance());
            responseObserver.onCompleted();
            return;
        }
        catch (NoSuchAlgorithmException nsae) {
            System.out.println("ERROR - Failed to instance the KeyFactory");
            responseObserver.onNext(CaServer.EncryptedCAMessageResponse.getDefaultInstance());
            responseObserver.onCompleted();
            return;
        }
        catch (InvalidKeySpecException ikse) {
            System.out.println("ERROR - Invalid key");
            responseObserver.onNext(CaServer.EncryptedCAMessageResponse.getDefaultInstance());
            responseObserver.onCompleted();
            return;
        }
    }


    //----------------


    public void SetupSystem(char[] password) {
        pwdArray = password;
        File dir = new File(keyStorePath);
        if(!dir.exists()) {
            dir.mkdirs();
        }

        boolean isKeyStoreNew = !(new File(keyStorePath + "keystore.jceks").exists());
        ks = CryptographyImpl.InitializeKeyStore(pwdArray, keyStorePath + "keystore.jceks");

        if(!isKeyStoreNew) {
            try {
                cert = (X509Certificate) ks.getCertificate("ca_certificate");
                caPrivateKey = ks.getKey("ca_private_key", pwdArray);
                caPublicKey = cert.getPublicKey();


            } catch (Exception e) {
                System.out.println("Failed to get private key: " + e.getMessage());
                System.exit(0);
            }
        }
        else {
            try {
                Path privKeyFile = Paths.get("CAKeys", "CAserver_private.der");
                System.out.println(privKeyFile.toAbsolutePath().toString());
                
                byte[] privKeyBytes = Files.readAllBytes(privKeyFile);

                PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(privKeyBytes);

                KeyFactory keyFactory = KeyFactory.getInstance("RSA");

                caPrivateKey = keyFactory.generatePrivate(privKeySpec);

                CertificateFactory fac = CertificateFactory.getInstance("X509");
                FileInputStream is = new FileInputStream(Paths.get("CAKeys", "CAserver.crt").toAbsolutePath().toString());
                cert = (X509Certificate) fac.generateCertificate(is);
                
                X509Certificate[] certificateChain = new X509Certificate[1];
                certificateChain[0] = cert;

                ks.setKeyEntry("ca_private_key", caPrivateKey, pwdArray, certificateChain);
                ks.setCertificateEntry("ca_certificate", cert);

                caPublicKey = cert.getPublicKey();

                CryptographyImpl.UpdateKeyStore(ks, pwdArray, keyStorePath + "keystore.jceks");
            } catch (Exception e) {
                System.out.println("Exception: " + e.getLocalizedMessage());
            }
        }
    }

    byte[] DecryptRequest(CaServer.EncryptedCAMessageRequest request) {

        byte[] decryptedTimestamp = CryptographyImpl.decryptRSA(request.getTimestamp().toByteArray(), caPrivateKey);

        Timestamp timestamp = null;
        try {
            timestamp = Timestamp.parseFrom(decryptedTimestamp);

            System.out.println("Received message with timestamp: " + timestamp.getSeconds() + "; Current time: " + System.currentTimeMillis() / 1000);
            if(System.currentTimeMillis() / 1000 + 300 < timestamp.getSeconds()
            || System.currentTimeMillis() / 1000 - 300 > timestamp.getSeconds()) {
                System.out.println("ERROR - Message received too out of expected times");
                return null;
            }
        }
        catch (InvalidProtocolBufferException ipbe) {
            System.out.println("ERROR - Failed to parse timestamp");
            return null;
        }

        byte[] decryptedTempKeyBytes = CryptographyImpl.decryptRSA(request.getEncryptionKey().toByteArray(), caPrivateKey);

        Key decryptTempKey = new SecretKeySpec(decryptedTempKeyBytes, 0, 16, "AES");

        //TODO: Check IV later
        byte[] requestDecryptedBytes = CryptographyImpl.decryptAES("", request.getMessageRequestBytes().toByteArray(), decryptTempKey);


        return requestDecryptedBytes;
    }

    boolean VerifyRequest(byte[] requestDecryptedBytes, byte[] digitalSignature, String targetName) {
        try {
            Key targetPublicKey = ks.getCertificate(targetName + "_certificate").getPublicKey();
    
            return CryptographyImpl.verifyDigitalSignature(requestDecryptedBytes, digitalSignature, (PublicKey) targetPublicKey);
        } catch (KeyStoreException e) {
            System.out.println("Error fetching public key of a target");
            return false;
        }
    }

    CaServer.EncryptedCAMessageResponse EncryptResponse(GeneratedMessageV3 response, String targetName) {
		//TODO: Check IV input
		try {
			Key tempKey = CryptographyImpl.generateAESKey();
			byte[] encryptedData = CryptographyImpl.encryptAES("", response.toByteArray(), tempKey);


            System.out.println("Will use public key of" + targetName);
            Key targetPublicKey = ks.getCertificate(targetName + "_certificate").getPublicKey();
			byte[] encryptedKey = CryptographyImpl.encryptRSA(tempKey.getEncoded(), targetPublicKey);

            byte[] digitalSignature = CryptographyImpl.generateDigitalSignature(response.toByteArray(), (PrivateKey) caPrivateKey);

			Timestamp timestamp = Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build();
			byte[] encryptedTimestamp = CryptographyImpl.encryptRSA(timestamp.toByteArray(), targetPublicKey);

			CaServer.EncryptedCAMessageResponse encryptedRes = CaServer.EncryptedCAMessageResponse.newBuilder()
													.setMessageResponseBytes(ByteString.copyFrom(encryptedData))
													.setEncryptionKey(ByteString.copyFrom(encryptedKey))
													.setDigitalSignature(ByteString.copyFrom(digitalSignature))
													.setTimestamp(ByteString.copyFrom(encryptedTimestamp))
													.build();
			
			return encryptedRes;
			
		} catch (Exception e) {
            System.out.println("Failed to encrypt response: " + e.getMessage());
            return CaServer.EncryptedCAMessageResponse.getDefaultInstance();
		}
	}

    CaServer.EncryptedCAMessageResponse EncryptResponse(GeneratedMessageV3 response, String targetName, Key tempPubKey) {
		//TODO: Check IV input
		try {
			Key tempKey = CryptographyImpl.generateAESKey();
			byte[] encryptedData = CryptographyImpl.encryptAES("", response.toByteArray(), tempKey);

			byte[] encryptedKey = CryptographyImpl.encryptRSA(tempKey.getEncoded(), tempPubKey);

            byte[] digitalSignature = CryptographyImpl.generateDigitalSignature(response.toByteArray(), (PrivateKey) caPrivateKey);

			Timestamp timestamp = Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build();
			byte[] encryptedTimestamp = CryptographyImpl.encryptRSA(timestamp.toByteArray(), tempPubKey);

			CaServer.EncryptedCAMessageResponse encryptedRes = CaServer.EncryptedCAMessageResponse.newBuilder()
													.setMessageResponseBytes(ByteString.copyFrom(encryptedData))
													.setEncryptionKey(ByteString.copyFrom(encryptedKey))
													.setDigitalSignature(ByteString.copyFrom(digitalSignature))
													.setTimestamp(ByteString.copyFrom(encryptedTimestamp))
													.build();
			
			return encryptedRes;
			
		} catch (Exception e) {
            System.out.println("Failed to encrypt response (Temp Public Key encryption): " + e.getMessage());
            return CaServer.EncryptedCAMessageResponse.getDefaultInstance();
		}
	}


}
