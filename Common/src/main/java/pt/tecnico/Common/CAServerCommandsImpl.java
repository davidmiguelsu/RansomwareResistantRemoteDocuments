package pt.tecnico.Common;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
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

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.grpc.CAServerServiceGrpc;
import pt.tecnico.grpc.CaServer;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class CAServerCommandsImpl {
    KeyStore ks = null;
    String username = "";
    String keyStorePassword = "";

    ManagedChannel channel = null;
    CAServerServiceGrpc.CAServerServiceBlockingStub stub = null;
    // Since every machine needs to have access to the CA, this is stored in Common/resources
    PublicKey caPublicKey = null;

    public CAServerCommandsImpl(ZKNaming zkNaming, KeyStore keyStore) {
        String uri = "";
        try {
            uri = zkNaming.lookup("/grpc/CAServer").getURI();
        } catch (ZKNamingException zkne) {
            System.out.println("Failed to locate CAServer. Is it active? " + zkne.getMessage());
            System.exit(0);
        }

        channel = ManagedChannelBuilder.forTarget(uri).usePlaintext().build();
        stub = CAServerServiceGrpc.newBlockingStub(channel);

        // Path caPublicKeyPath = Paths.get("/home/fenix/Documents/TempFolder/RansomwareResistantRemoteDocuments/Common/src/main/resources/CAserver_public.der");
        Path caPublicKeyPath = Paths.get("..", "Common", "src", "main", "resources", "CAserver_public.der");
        caPublicKey = CryptographyImpl.readPublicKey(caPublicKeyPath.toAbsolutePath().toString());
        ks = keyStore;
    }

    public boolean requestKeyPair() {
        KeyPair tempKeyPair = CryptographyImpl.generateRSAKeyPair();

        CaServer.GenerateKeyPairRequest req = CaServer.GenerateKeyPairRequest.newBuilder()
                                                .setUserName(username)
                                                .setTempPublicKey(ByteString.copyFrom(tempKeyPair.getPublic().getEncoded()))
                                                .build();

        CaServer.EncryptedCAMessageRequest request = EncryptMessage(req, false);
        CaServer.EncryptedCAMessageResponse res = stub.generateKeyPair(request);


        CaServer.GenerateKeyPairResponse response = null;
        try {
            byte[] responseBytes = DecryptResponse(res, tempKeyPair);
            response = CaServer.GenerateKeyPairResponse.parseFrom(responseBytes);
        
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(response.getCertificate().toByteArray());
            X509Certificate cert = (X509Certificate)certFactory.generateCertificate(in);
            Certificate[] certChain = new X509Certificate[2];

            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privKeyDecoded = kf.generatePrivate(new PKCS8EncodedKeySpec(response.getPrivateKey().toByteArray()));

            int i = 0;
            for (ByteString certificateBytes : response.getCertificateChainList()) {
                in = new ByteArrayInputStream(certificateBytes.toByteArray());

                certChain[i++] = (X509Certificate)certFactory.generateCertificate(in);
            }

            if(response.getAck().equals("OK")) {
                ks.setCertificateEntry(username + "_certificate", cert);
                ks.setKeyEntry(username + "_private_key", privKeyDecoded, keyStorePassword.toCharArray(), certChain);
                System.out.println("Key pair registered for user " + username);
                return true;
            }
            else {
                System.out.println("Failed to register keypair for user" + username);
                return false;
            }
        } catch (InvalidProtocolBufferException ipbe) {
            System.out.println("ERROR - Failed to parse response");
            return false;
        } catch (CertificateException ce) {
            System.out.println("ERROR - Certificate-related error");
            return false;
        } catch (NoSuchAlgorithmException nsae) {
            System.out.println("ERROR - Failed to parse response");
            return false;
        } catch (InvalidKeySpecException ike) {
            System.out.println("ERROR - Invalid key");
            return false;
        } catch (KeyStoreException kse) {
            System.out.println("ERROR - KeyStore-related error");
            return false;
        }
    }

    public PublicKey requestPublicKeyOf(String target, boolean withDigitalSignature, boolean withTempKeys) {
        KeyPair tempKeyPair = CryptographyImpl.generateRSAKeyPair();

        CaServer.PublicKeyRequest.Builder reqBuilder = CaServer.PublicKeyRequest.newBuilder()
                                            .setUserName(username)
                                            .setTarget(target);

        if(withTempKeys) {
            reqBuilder.setTempPublicKey(ByteString.copyFrom(tempKeyPair.getPublic().getEncoded()));
        }

        CaServer.EncryptedCAMessageRequest request = EncryptMessage(reqBuilder.build(), withDigitalSignature);
        CaServer.EncryptedCAMessageResponse res = stub.requestPublicKey(request);
        // CaServer.PublicKeyResponse res = stub.requestPublicKey(req);

        CaServer.PublicKeyResponse response = null;
        try {
            byte[] responseBytes = null;
            if(withTempKeys) {
                responseBytes = DecryptResponse(res, tempKeyPair);
            }
            else {
                responseBytes = DecryptResponse(res, null);

            }
            response = CaServer.PublicKeyResponse.parseFrom(responseBytes);
            
            if(response.getAck().equals("OK")) {
                KeyFactory kf = KeyFactory.getInstance("RSA");
                return kf.generatePublic(new X509EncodedKeySpec(response.getPublicKey().toByteArray()));
            }
            else {
                System.out.println("ERROR - Failed to retrieve public key");
                return null;
            }
        } catch (InvalidProtocolBufferException ipbe) {
            System.out.println("ERROR - Failed to parse response");
            return null;
        } catch (NoSuchAlgorithmException nsae) {
            System.out.println("ERROR - Failed to parse response");
            return null;
        } catch (InvalidKeySpecException ike) {
            System.out.println("ERROR - Invalid key");
            return null;
        } 
    }

    public void SetUser(String name, String password) {
        username = name;
        keyStorePassword = password;
    }

    public void SetKeyStore(KeyStore keyStore) {
        ks = keyStore;
    }


    CaServer.EncryptedCAMessageRequest EncryptMessage(GeneratedMessageV3 request, boolean withDigitalSignature) {
        //TODO: Check IV input
        try {
            Key tempKey = CryptographyImpl.generateAESKey();
            byte[] encryptedData = CryptographyImpl.encryptAES("", request.toByteArray(), tempKey);
            byte[] encryptedKey = CryptographyImpl.encryptRSA(tempKey.getEncoded(), caPublicKey);

            
            Timestamp timestamp = Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build();
			byte[] encryptedTimestamp = CryptographyImpl.encryptRSA(timestamp.toByteArray(), caPublicKey);
            
            // byte[] encryptedKey = CryptographyImpl.encryptRSA(noSignatureEncryptedKey, CryptographyImpl.readPrivateKey(keyPath + "ClientKeys/client_private.der"));
            byte[] digitalSignature = {};
            if(withDigitalSignature) {
                Key privKey = ks.getKey(username + "_private_key", keyStorePassword.toCharArray());
                digitalSignature = CryptographyImpl.generateDigitalSignature(request.toByteArray(), (PrivateKey) privKey);
            }
            CaServer.EncryptedCAMessageRequest encryptedReq = CaServer.EncryptedCAMessageRequest.newBuilder()
                                                    .setMessageRequestBytes(ByteString.copyFrom(encryptedData))
                                                    .setEncryptionKey(ByteString.copyFrom(encryptedKey))
                                                    .setDigitalSignature(ByteString.copyFrom(digitalSignature))
                                                    .setTimestamp(ByteString.copyFrom(encryptedTimestamp))
                                                    .build();
            
            return encryptedReq;
            
        } catch (Exception e) {
            System.out.println("Failed to encrypt request: " + e.getMessage());
            return CaServer.EncryptedCAMessageRequest.getDefaultInstance();
        }
    }

    byte[] DecryptResponse(CaServer.EncryptedCAMessageResponse response, KeyPair tempKeys) {
        Key privKey = null;
        try {
            if(tempKeys != null) {
                privKey = tempKeys.getPrivate();
            }
            else {
                privKey = ks.getKey(username + "_private_key", keyStorePassword.toCharArray());
            }
        } catch (Exception e) {
            System.out.println("Failed to get private key: " + e.getMessage());
        }

        // PrivateKey privKey = CryptographyImpl.readPrivateKey(keyPath + "ClientKeys/client_private.der");
        byte[] decryptedTimestamp = CryptographyImpl.decryptRSA(response.getTimestamp().toByteArray(), privKey);

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

        byte[] decryptedTempKeyBytes = CryptographyImpl.decryptRSA(response.getEncryptionKey().toByteArray(), privKey);
		Key decryptTempKey = new SecretKeySpec(decryptedTempKeyBytes, 0, 16, "AES");
        
		//TODO: Check IV later
		byte[] responseDecryptedBytes = CryptographyImpl.decryptAES("", response.getMessageResponseBytes().toByteArray(), decryptTempKey);
		
        if(!CryptographyImpl.verifyDigitalSignature(responseDecryptedBytes, response.getDigitalSignature().toByteArray(), caPublicKey)) {
            System.out.println("ERROR - Received message doesn't match with the digital signature!");
            return null;
        }


        return responseDecryptedBytes;
	}

    public Key FetchPrivateKey() {
        try {
            return ks.getKey(username + "_private_key", keyStorePassword.toCharArray());
        } catch (Exception e) {
            System.out.println("ERROR - Unable to fetch private key");
            return null;
        }
    }

    public void Shutdown() {
        channel.shutdownNow();
    }
}
