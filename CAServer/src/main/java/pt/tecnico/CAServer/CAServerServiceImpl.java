package pt.tecnico.CAServer;

import java.io.File;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

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
            //TODO: handle exception
        }
        
        if(!VerifyRequest(requestDecryptedBytes, request.getDigitalSignature().toByteArray(), decryptedRequest.getUserName())) {
            System.out.println("Digital signature missmatch");
            CaServer.GenerateKeyPairResponse res = CaServer.GenerateKeyPairResponse.newBuilder().setAck("ERROR").build();
            responseObserver.onNext(EncryptResponse(res, decryptedRequest.getUserName()));
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
                //TODO: handle exception
            }
        } catch (KeyStoreException e) {
            System.out.println("Error when checking existing keys");
            responseObserver.onNext(EncryptResponse(CaServer.EncryptedCAMessageResponse.getDefaultInstance(), decryptedRequest.getUserName()));
            responseObserver.onCompleted();
            return;
        }

        //TODO: Under correct circustances, these keys would be tied to a certificate?
        KeyPair keyPair = CryptographyImpl.generateRSAKeyPair();
        X509Certificate clientCert = CryptographyImpl.generateSelfSignedCertificate(keyPair, CERTIFICATE_DN, decryptedRequest.getUserName());

        try {
            X509Certificate[] certificateChain = new X509Certificate[2];
            certificateChain[0] = clientCert;
            certificateChain[1] = cert;
            ks.setKeyEntry(decryptedRequest.getUserName() + "_private_key", caPrivateKey, pwdArray, certificateChain);
            ks.setCertificateEntry(decryptedRequest.getUserName() + "_certificate", clientCert);

            CryptographyImpl.UpdateKeyStore(ks, pwdArray, keyStorePath);

            CaServer.GenerateKeyPairResponse.Builder resBuilder = CaServer.GenerateKeyPairResponse.newBuilder()
                                                        .setAck("OK")
                                                        .setCertificate(ByteString.copyFrom(clientCert.getEncoded()))
                                                        .setPrivateKey(ByteString.copyFrom(keyPair.getPrivate().getEncoded()));

            for (X509Certificate x509Certificate : certificateChain) {
                resBuilder.addCertificateChain(ByteString.copyFrom(x509Certificate.getEncoded()));
            }
    
            responseObserver.onNext(EncryptResponse(resBuilder.build(), decryptedRequest.getUserName()));
            responseObserver.onCompleted();
        } catch (CertificateEncodingException cee) {
            //TODO: handle exception
        } catch (KeyStoreException kse) {
            //TODO: handle exception
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
            // responseObserver.onNext(EncryptResponse(CaServer.EncryptedCAMessageResponse.getDefaultInstance()));
            responseObserver.onNext(CaServer.EncryptedCAMessageResponse.getDefaultInstance());
            responseObserver.onCompleted();
            return;
            //TODO: handle exception
        }
        
        if(!VerifyRequest(requestDecryptedBytes, request.getDigitalSignature().toByteArray(), decryptedRequest.getUserName())) {
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
                
                responseObserver.onNext(EncryptResponse(res, decryptedRequest.getUserName()));
                responseObserver.onCompleted();
                return;
                //TODO: handle exception
            }
        } 
        catch (KeyStoreException e) {
            System.out.println("Error when checking existing keys");
            responseObserver.onNext(EncryptResponse(CaServer.EncryptedCAMessageResponse.getDefaultInstance(), decryptedRequest.getUserName()));
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
                //TODO: handle exception
            }
        }
        else {
            KeyPair caKeyPair = CryptographyImpl.generateRSAKeyPair();
            caPrivateKey = caKeyPair.getPrivate();
            caPublicKey = caKeyPair.getPublic();
            cert = CryptographyImpl.generateSelfSignedCertificate(caKeyPair, CERTIFICATE_DN, "ca_certificate");

            X509Certificate[] certificateChain = new X509Certificate[1];
            certificateChain[0] = cert;

            try {
                ks.setKeyEntry("ca_private_key", caPrivateKey, pwdArray, certificateChain);
                ks.setCertificateEntry("ca_certificate", cert);

                CryptographyImpl.UpdateKeyStore(ks, pwdArray, keyStorePath);
            } catch (KeyStoreException kse) {
                //TODO: handle exception
            }
        }
    }

    byte[] DecryptRequest(CaServer.EncryptedCAMessageRequest request) {

        // PrivateKey privKey = CryptographyImpl.readPrivateKey(keyPaths + "LeadServerKeys/leadServer_private.der");
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
        // byte[] decryptedTempKeyBytes = CryptographyImpl.decryptRSA(request.getEncryptionKey().toByteArray(), 
        //     CryptographyImpl.readPublicKey(keyPaths + "ClientKeys/client_public.der"));
        // CryptographyImpl.readPrivateKey("/home/fenix/Documents/SIRS_Stuff/Repo/RansomwareResistantRemoteDocuments/CAServer/LeadServerKeys/leadServer_private.der"));
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

            Key targetPublicKey = ks.getCertificate(targetName + "_certificate").getPublicKey();
			byte[] encryptedKey = CryptographyImpl.encryptRSA(tempKey.getEncoded(), targetPublicKey);

            byte[] digitalSignature = CryptographyImpl.generateDigitalSignature(response.toByteArray(), (PrivateKey) caPrivateKey);

			Timestamp timestamp = Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build();
			byte[] encryptedTimestamp = CryptographyImpl.encryptRSA(timestamp.toByteArray(), targetPublicKey);
			// byte[] encryptedKey = CryptographyImpl.encryptRSA(noSignatureEncryptedKey, CryptographyImpl.readPrivateKey(keyPaths + "LeadServerKeys/leadServer_private.der"));

			// byte[] encryptedKey = CryptographyImpl.encryptRSA(tempKey.getEncoded(), CryptographyImpl.readPublicKey("/home/fenix/Documents/SIRS_Stuff/Repo/RansomwareResistantRemoteDocuments/CAServer/ClientKeys/client_public.der"));
			
			CaServer.EncryptedCAMessageResponse encryptedRes = CaServer.EncryptedCAMessageResponse.newBuilder()
													.setMessageResponseBytes(ByteString.copyFrom(encryptedData))
													.setEncryptionKey(ByteString.copyFrom(encryptedKey))
													.setDigitalSignature(ByteString.copyFrom(digitalSignature))
													.setTimestamp(ByteString.copyFrom(encryptedTimestamp))
													.build();
			
			return encryptedRes;
			
		} catch (Exception e) {
			//TODO: handle exception
			return null;
		}
	}


}
