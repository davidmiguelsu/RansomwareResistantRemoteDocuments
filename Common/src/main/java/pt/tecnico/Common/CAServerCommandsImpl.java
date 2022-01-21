package pt.tecnico.Common;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyFactory;
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
            //TODO: handle exception
        }

        channel = ManagedChannelBuilder.forTarget(uri).usePlaintext().build();
        stub = CAServerServiceGrpc.newBlockingStub(channel);

        caPublicKey = CryptographyImpl.readPublicKey("src/test/resources/CAserver_public.der");
        ks = keyStore;
    }

    public void requestKeyPair(String username) {
        CaServer.GenerateKeyPairRequest req = CaServer.GenerateKeyPairRequest.newBuilder().setUserName(username).build();

        CaServer.EncryptedCAMessageRequest request = EncryptMessage(req, false);
        CaServer.EncryptedCAMessageResponse res = stub.generateKeyPair(request);


        CaServer.GenerateKeyPairResponse response = null;
        try {
            byte[] responseBytes = DecryptResponse(res);
            response = CaServer.GenerateKeyPairResponse.parseFrom(responseBytes);
        

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(response.getCertificate().toByteArray());
            X509Certificate cert = (X509Certificate)certFactory.generateCertificate(in);
            Certificate[] certChain = new X509Certificate[2];

            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privKeyDecoded = kf.generatePrivate(new PKCS8EncodedKeySpec(response.getPrivateKey().toByteArray()));

            if(response.getAck().equals("OK")) {
                ks.setCertificateEntry(username + "_certificate", cert);
                ks.setKeyEntry(username + "_private_key", privKeyDecoded, keyStorePassword.toCharArray(), certChain);
            }
            System.out.println("Key pair registered for user " + username);

        } catch (InvalidProtocolBufferException ipbe) {
            System.out.println("ERROR - Failed to parse response");
        } catch (CertificateException ce) {
            System.out.println("ERROR - Certificate-related error");
        } catch (NoSuchAlgorithmException nsae) {
            System.out.println("ERROR - Failed to parse response");
        } catch (InvalidKeySpecException ike) {
            System.out.println("ERROR - Invalid key");
        } catch (KeyStoreException kse) {
            System.out.println("ERROR - KeyStore-related error");
        }
    }

    public void SetUser(String name, String password) {
        username = name;
        keyStorePassword = password;
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
            byte[] digitalSignature = null;
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
            //TODO: handle exception
            return null;
        }
    }

    byte[] DecryptResponse(CaServer.EncryptedCAMessageResponse response) {
        Key privKey = null;
        try {
            privKey = ks.getKey("ca_private_key", keyStorePassword.toCharArray());
        } catch (Exception e) {
            //TODO: handle exception
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
        // byte[] decryptedTempKeyBytes = CryptographyImpl.decryptRSA(partiallyDecryptedTempKeyBytes, 
		//     CryptographyImpl.readPublicKey(keyPath + "LeadServerKeys/leadServer_public.der"));
		Key decryptTempKey = new SecretKeySpec(decryptedTempKeyBytes, 0, 16, "AES");
        
		//TODO: Check IV later
		byte[] responseDecryptedBytes = CryptographyImpl.decryptAES("", response.getMessageResponseBytes().toByteArray(), decryptTempKey);
		
        if(!CryptographyImpl.verifyDigitalSignature(responseDecryptedBytes, response.getDigitalSignature().toByteArray(), caPublicKey)) {
            System.out.println("ERROR - Received message doesn't match with the digital signature!");
            return null;
        }


        return responseDecryptedBytes;
	}
}
