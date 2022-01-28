package pt.tecnico.Client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.Common.CAServerCommandsImpl;
import pt.tecnico.Common.CryptographyImpl;
import pt.tecnico.grpc.ClientServer;
import pt.tecnico.grpc.ClientToServerServiceGrpc;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class ClientCommandImpl {
    String username = null;

    String zooHost = "";
    String zooPort = "";
    String path = "";

    ManagedChannel channel = null;
    ClientToServerServiceGrpc.ClientToServerServiceBlockingStub stub = null;
    String dirPath = "";
    String keyStorePath = "";
    String keyPath = "";


    KeyStore ks;
    char[] pwdArray = "pwd".toCharArray();

    CAServerCommandsImpl caServer = null;
    Key leadServerPublicKey = null;

    ClientCommandImpl() {
        // stub = serverStub;
        dirPath = System.getProperty("user.home") + "/Downloads/";
        keyStorePath = System.getProperty("user.home") + "/Documents/SIRS_KeyStores/";
        keyPath = System.getProperty("user.home") + "/Documents" + "/RansomwareResistantRemoteDocuments/CAServer/";

    }  
    
    public boolean ExecuteCommand(String input) throws ZKNamingException{
        String[] args = input.split("[ ]+");
        try{
            return handleCommand(args);
        }
        catch (StatusRuntimeException sre) {
            String result = connectToServer(zooHost, zooPort, path);
            if(result == "ERROR_LIST_RECORDS" || result == "ERROR_NO_SERVER_AVAILABLE"){
                System.out.println("No servers are currently active");
                return false;
            }
            else{
                return handleCommand(args);      //Repeat the parsing if it managed to reconnect
            }
        }
    }

    boolean handleCommand(String[] args) {
        switch (args[0]){
            case "write":
            case "w":            
                writeFile(args);
                break;
            
            case "download":
            case "d":
                readFile(args);
                break;

            case "remove":
            case "rm":
                deleteFile(args);
                break;

            case "list":
            case "ls":
                listFiles();
                break;

            case "register":
            case "reg":
                register(args);
                break;

            case "login":
            case "log":
                login(args);
                break;

            case "logout":
                logout();
                break;

            case "permission":
            case "perm":
                givePermissions(args);                  
                break;
  
            case "help":
            case "h":
                System.out.println(" \n \n \n The commands available are: \n Register - register user passwd // reg user passwd \n Login - login user passwd // log user passwd \n Logout - logout \n Create File - write filePath // w filePath \n Read File - download fileName // d fileName \n List Files - list // ls" +
                "\n Remove file - remove fileName // rm fileName \n Share a file  - permission fileName userName read/all // perm fileName userName read/all \n Close the session -  exit \n \n");   
                break; 
                
            case "exit":
                CryptographyImpl.UpdateKeyStore(ks, pwdArray, keyStorePath + "standard_" + username + ".jceks");
                caServer.Shutdown();
                return false;
            default:
                System.out.println("ERROR - Invalid Command");
                break;
        }
        return true;
    }

    void writeFile(String[] args) {
        if(username == null) {
            System.out.println("ERROR - Please login in first");
            return;
        }

        if(args.length == 1 || args.length > 3) {
            System.out.println("ERROR - Write format - You need to use this format ->  \" write arg / w arg \"  ");
            return;
        }

        String fileName = "";
        File file = null;
        if(args.length == 2) {
            file = new File(args[1]);
            fileName = file.getName();
        }
        else if(args.length == 3) {
            file = new File(args[2]);
            fileName = args[1];
        }
        try {
            FileInputStream fis = new FileInputStream(file);

            // CryptographyImpl.generateAESKey(System.getProperty("user.home") + "/SIRS_KEYS/" + fileName + ".key");

            SecretKey key = CryptographyImpl.generateAESKey();
            KeyStore.SecretKeyEntry secret = new KeyStore.SecretKeyEntry(key);
            KeyStore.ProtectionParameter password = new KeyStore.PasswordProtection(pwdArray);
            ks.setEntry(fileName + "_key", secret, password);

            CryptographyImpl.UpdateKeyStore(ks, pwdArray, keyStorePath + "standard_" + username + ".jceks");

            byte[] encryptedFile = CryptographyImpl.encryptAES(fileName, fis.readAllBytes(), key);
            fis.close();

            fis = new FileInputStream(file);
            byte[] hashBytes = CryptographyImpl.GenerateSHA3Digest(fis.readAllBytes());

            fis.close();

            // String sha3Hex = CryptographyImpl.bytesToHex(hashBytes);


            ClientServer.WriteFileRequest request = ClientServer.WriteFileRequest.newBuilder()
                                                        .setFileName(fileName)
                                                        .setFile(ByteString.copyFrom(encryptedFile))
                                                        .setHash(ByteString.copyFrom(hashBytes))
                                                        .setUsername(username)
                                                        .build();
            
            ClientServer.EncryptedMessageRequest encryptedReq = EncryptMessage(request);

                                     

            ClientServer.EncryptedMessageResponse res = stub.writeFile(encryptedReq);
            try {
                byte[] responseBytes = DecryptResponse(res);
                ClientServer.WriteFileResponse response = ClientServer.WriteFileResponse.parseFrom(responseBytes);


                System.out.println(response.getAck());
                if(response.getAck().startsWith("OK")) {
                    giveKeysToPermittedUsers(fileName, key);
                }
                
            } catch (InvalidProtocolBufferException ipbe) {
                System.out.println("ERROR - Write - Failed to parse server response.");
                System.out.println(ipbe.getMessage());
            }
        } catch (Exception e) {
             System.out.println("ERROR - Write - (File not found) | Dont forget you need to use this format ->  \" write arg / w arg \"  \n ");
             System.out.println(e.getMessage());
        }
		
    }
    
    void readFile(String[] args) {
        if(username == null) {
            System.out.println("ERROR - Please login in first");
            return;
        }

        if(args.length < 1 || args.length > 2) {
            System.out.println("ERROR - Read format - You need to use this format -> \" download arg / d arg \"  \n ");
            return;
        }

        String fileName = args[1];

        try{
            updatePendingKeys();
            
            ClientServer.ReadFileRequest request =  ClientServer.ReadFileRequest.newBuilder()
                                                        .setFileName(fileName)
                                                        .setUsername(username)
                                                        .build();
            
            ClientServer.EncryptedMessageRequest encryptedReq = EncryptMessage(request);

            ClientServer.EncryptedMessageResponse res = stub.readFile(encryptedReq);
            ClientServer.ReadFileResponse response = null;
            try {
                byte[] responseBytes = DecryptResponse(res);
                response = ClientServer.ReadFileResponse.parseFrom(responseBytes);
            } catch (InvalidProtocolBufferException ipbe) {
                System.out.println("ERROR - Read - Failed to decrypt response");
                return;
            }

            
            Key key = ks.getKey(fileName + "_key", pwdArray);

            byte[] decryptedFile = CryptographyImpl.decryptAES(fileName, response.getFile().toByteArray(), key);
            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            byte[] hashBytes = digest.digest(decryptedFile);

            if(ByteBuffer.wrap(hashBytes).compareTo(ByteBuffer.wrap(response.getHash().toByteArray())) != 0) {
                System.out.println("ERROR - Read - Hash of the downloaded file differs from the hash received! File may be compromised.");
                return;
            }
            
            
            
            FileOutputStream writer = new FileOutputStream(dirPath + fileName);
            writer.write(decryptedFile);

			writer.close();
            System.out.println("Download complete, file stored in the Download folder");



        } catch (Exception e) {
            System.out.println("ERROR - Read - (File not found) | Dont forget you need to use this format -> \" download arg / d arg \" \n");
            System.out.println(e.getMessage());
        }
    
    }
    

    void listFiles(){
        if(username == null) {
            System.out.println("ERROR - Please login in first");
            return;
        }

        ClientServer.ListFileRequest request = ClientServer.ListFileRequest.newBuilder().setUserName(username).build();

        ClientServer.EncryptedMessageRequest encryptedReq = EncryptMessage(request);

        ClientServer.EncryptedMessageResponse res = stub.listFiles(encryptedReq);

        try {
            byte[] responseBytes = DecryptResponse(res);
            ClientServer.ListFileResponse response = ClientServer.ListFileResponse.parseFrom(responseBytes);
    
    
            for (String fileName : response.getFileNameList()) {
                System.out.println(fileName);
            }
            
        } catch (InvalidProtocolBufferException ipbe) {
            System.out.println("ERROR - List Files - Response obtained is invalid!");
        }
    }

    void deleteFile(String[] args){
        if(username == null) {
            System.out.println("ERROR - Please login in first");
            return;
        }

        if(args.length != 2) {
            System.out.println("ERROR - Delete - You need to use this format -> \" delete arg / del arg \"  \n");
            return;
        }

        ClientServer.DeleteFileRequest request = ClientServer.DeleteFileRequest.newBuilder()
            .setFileName(args[1])
            .setUsername(username)
            .build();
            
        ClientServer.EncryptedMessageRequest encryptedReq = EncryptMessage(request);

        ClientServer.EncryptedMessageResponse res = stub.deleteFile(encryptedReq);
        try {
            byte[] responseBytes = DecryptResponse(res);
            ClientServer.DeleteFileResponse response = ClientServer.DeleteFileResponse.parseFrom(responseBytes);
    
            System.out.println(response.getAck());
        } catch (InvalidProtocolBufferException ipbe) {
            System.out.println("ERROR - Remove - Parsing response message error");
        }
    }

    void register(String[] args) {
        if (username != null) {
            System.err.println("ERROR - Register - Log out from the current user before a new user register.");
            return;
        }

        if(args.length != 3) {
            System.out.println("ERROR - Register - You need to use this format -> \" register username password \"  \n");
            return;
        } 

        username = args[1];
        pwdArray = args[2].toCharArray();


        ks = CryptographyImpl.InitializeKeyStore(pwdArray, keyStorePath + "standard_" + username + ".jceks");

        caServer.SetUser(args[1], args[2]);
        caServer.SetKeyStore(ks);
        caServer.requestKeyPair();

        ClientServer.RegisterRequest request = ClientServer.RegisterRequest.newBuilder()
            .setUserName(args[1])
            .setCipheredPassword(args[2])
            .build();

        ClientServer.EncryptedMessageRequest encryptedReq = EncryptMessage(request);

        ClientServer.EncryptedMessageResponse res = stub.register(encryptedReq);
        try {
            byte[] responseBytes = DecryptResponse(res);
            ClientServer.RegisterResponse response = ClientServer.RegisterResponse.parseFrom(responseBytes);
    
            if(!response.getAck().equals("ERROR")) {
                username = args[1];

                CryptographyImpl.CreateNewKeyStore(ks, pwdArray, keyStorePath + "standard_" + username + ".jceks");
            }
            else {
                System.out.println("ERROR - Register - Username already exists");
                username = "";
                pwdArray = "pwd".toCharArray();
                caServer.SetUser("", "");
                ks = null;
                caServer.SetKeyStore(null);
            }
        } catch (InvalidProtocolBufferException ipbe) {
            System.out.println("ERROR - Register - Failed to register");
            username = "";
            pwdArray = "pwd".toCharArray();
            caServer.SetUser("", "");
            ks = null;
            caServer.SetKeyStore(null);
        }

    }

    void login(String[] args) {
        if (username != null) {
            System.err.println("ERROR - Login - Log out from the current user before a new user register.");
            return;
        }

        if(args.length != 3) {
            System.out.println("ERROR - Login - You need to use this format -> \" login username password \"  \n");
            return;
        } 

        username = args[1];
        pwdArray = args[2].toCharArray();

        ks = CryptographyImpl.InitializeKeyStore(pwdArray, keyStorePath + "standard_" + username + ".jceks");
        if(ks == null) {
            System.err.println("ERROR - Login - Keystore failed to initialize");
            username = "";
            pwdArray = "pwd".toCharArray();
            return;
        }

        caServer.SetUser(args[1], args[2]);
        caServer.SetKeyStore(ks);

        ClientServer.LoginRequest request = ClientServer.LoginRequest.newBuilder()
            .setUserName(args[1])
            .setCipheredPassword(args[2])
            .build();

        ClientServer.EncryptedMessageRequest encryptedReq = EncryptMessage(request);

        ClientServer.EncryptedMessageResponse res = stub.login(encryptedReq);
        try {
            byte[] responseBytes = DecryptResponse(res);
            ClientServer.LoginResponse response = ClientServer.LoginResponse.parseFrom(responseBytes);
    
            if(!response.getAck().equals("ERROR")) {
                username = args[1];
                pwdArray = args[2].toCharArray();

                ks = CryptographyImpl.InitializeKeyStore(pwdArray, keyStorePath + "standard_" + username + ".jceks");

                updatePendingKeys();
            }
            else {
                System.out.println("ERROR - Login - Authentication failed");
                username = "";
                pwdArray = "pwd".toCharArray();
                caServer.SetUser("", "");
                ks = null;
                caServer.SetKeyStore(null);
            }
        } catch (InvalidProtocolBufferException ipbe) {
            System.out.println("ERROR - Login - Authentication failed");
            username = "";
            pwdArray = "pwd".toCharArray();
            caServer.SetUser("", "");
            ks = null;
            caServer.SetKeyStore(null);
        }
    }

    public void givePermissions(String[] args) {
        if (username == null) {
            System.err.println("ERROR - Permissions - Log out from the current user before a new user register.");
            return;
        }

        if(args.length != 4) {
            System.err.println("ERROR - Permissions - You need to use the format -> \" permission/perm fileName userName read/all");
            return;
        }
        String filename = args[1];
        String targetUsername = args[2];
        String perm = args[3];

        if(!perm.equals("read") && !perm.equals("all")) {
            System.err.println("ERROR - Permissions - Invalid permission type");
            return;
        }
        if(targetUsername.equals(username)) {
            System.err.println("ERROR - Permissions - You can't send permissions to yourself");
            return;
        }
        
        
        Key key = null;
        try {
            if(!ks.containsAlias(filename + "_key")) {
                System.err.println("ERROR - Permissions - Invalid file name");
                return;
            }
            key = ks.getKey(filename + "_key", pwdArray);
        } catch (Exception e) {
            System.err.println("ERROR - Permissions - Something occured fetching the key. You need to use the format -> \" permission/perm fileName userName read/all");
            return;
        }

        PublicKey targetPubKey = caServer.requestPublicKeyOf(targetUsername, true, false);
        byte[] encryptedKey = CryptographyImpl.encryptRSA(key.getEncoded(), targetPubKey);
        ClientServer.GivePermissionsRequest request = ClientServer.GivePermissionsRequest.newBuilder()
                                                        .setFileName(filename)
                                                        .setUserName(username)
                                                        .setTargetUserName(targetUsername)
                                                        .setKey(ByteString.copyFrom(encryptedKey))
                                                        .setPermission(perm)
                                                        .build();

        ClientServer.EncryptedMessageRequest encryptedReq = EncryptMessage(request);
        ClientServer.EncryptedMessageResponse res = stub.givePermission(encryptedReq);

        ClientServer.GivePermissionsResponse response = null;
        try {
            byte[] responseBytes = DecryptResponse(res);
            response = ClientServer.GivePermissionsResponse.parseFrom(responseBytes);

            System.out.println(response.getAck());
        } catch (InvalidProtocolBufferException ipbe) {
            System.out.println("ERROR - Read - Failed to decrypt response");
            return;
        }
    }

    void giveKeysToPermittedUsers(String filename, Key secretKey) {
        if (username == null) {
            System.err.println("ERROR - Please login first");
            return;
        }
 
        byte[] encryptedKey = CryptographyImpl.encryptRSA(secretKey.getEncoded(), leadServerPublicKey);
        ClientServer.GiveKeysToPermittedUsersRequest request = ClientServer.GiveKeysToPermittedUsersRequest.newBuilder()
                                                        .setFileName(filename)
                                                        .setUserName(username)
                                                        .setKey(ByteString.copyFrom(encryptedKey))
                                                        .build();

        ClientServer.EncryptedMessageRequest encryptedReq = EncryptMessage(request);
        ClientServer.EncryptedMessageResponse res = stub.giveKeysToPermittedUsers(encryptedReq);

        ClientServer.GiveKeysToPermittedUsersResponse response = null;
        try {
            byte[] responseBytes = DecryptResponse(res);
            response = ClientServer.GiveKeysToPermittedUsersResponse.parseFrom(responseBytes);

            for (String ack : response.getAckList()) {
                System.out.println(ack);
            }
        } catch (InvalidProtocolBufferException ipbe) {
            System.out.println("ERROR - Read - Failed to decrypt response");
            return;
        }
    }

    public void logout() {
        username = null;
        ks = null;
        System.out.println("Successful logout");
    }

    void updatePendingKeys() {

        ClientServer.UpdatePermissionsRequest request = ClientServer.UpdatePermissionsRequest.newBuilder()
            .setUserName(username)
            .build();

        ClientServer.EncryptedMessageRequest encryptedReq = EncryptMessage(request);

        ClientServer.EncryptedMessageResponse res = stub.updatePermissions(encryptedReq);

        ClientServer.UpdatePermissionsResponse response = null;
        try {
            byte[] responseBytes = DecryptResponse(res);
            response = ClientServer.UpdatePermissionsResponse.parseFrom(responseBytes);
        } catch (InvalidProtocolBufferException ipbe) {
            System.out.println("Failed to decrypt response: " + ipbe.getMessage());
            return;
        }

        int i = 0;
        try {
            for(String fileName : response.getFileNameList()){
                PrivateKey privKey = (PrivateKey) caServer.FetchPrivateKey();
                byte[] decryptedSecret = CryptographyImpl.decryptRSA(response.getKeys(i).toByteArray(), privKey);

                SecretKey newKey = new SecretKeySpec(decryptedSecret, 0, 16, "AES");
    
                KeyStore.SecretKeyEntry secret = new KeyStore.SecretKeyEntry(newKey);
                KeyStore.ProtectionParameter password = new KeyStore.PasswordProtection(pwdArray);
                ks.setEntry(fileName + "_key", secret, password);
                i++;
            }
        } catch (KeyStoreException kse) {
            System.out.println("Failed to store new keys: " + kse.getMessage());
            return;
        }
        CryptographyImpl.UpdateKeyStore(ks, pwdArray, keyStorePath + "standard_" + username + ".jceks");
    }

    //------------
    public String connectToServer(String host, String port, String serverPath) {
        zooHost = host;
        zooPort = port;
        path = serverPath;

        System.out.println("Contacting ZooKeeper at " + host + ":" + port + "...");
        ZKNaming zkNaming = new ZKNaming(host, port);
		System.out.println("Looking up " + serverPath + "...");

        try {
            if(channel != null) {
                ShutdownChannel();
            }
            
            Collection<ZKRecord> records = zkNaming.listRecords(serverPath);
            int numberOfServers = records.size();
            
            if(numberOfServers == 0) {
                System.err.println("ConnectToServer: No servers available");
                return "ERROR_NO_SERVER_AVAILABLE";
            }

            ArrayList<ZKRecord> recordList = new ArrayList<>(records);

            String target = recordList.get(0).getURI();     //Always connects to the first server (First server == Master server)

            channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            ClientToServerServiceGrpc.ClientToServerServiceBlockingStub newStub = ClientToServerServiceGrpc.newBlockingStub(channel);
            stub = newStub;
            System.out.println("Located server at " + target);

            caServer = new CAServerCommandsImpl(zkNaming, null);
            leadServerPublicKey = caServer.requestPublicKeyOf("LeadServer", false, true);

            return "OK";
        } catch (ZKNamingException zkne) {
            System.err.println("ConnectToServer: Failed to lookup records" + zkne.getStackTrace());
            return "ERROR_LIST_RECORDS";
        }
    }

    ClientServer.EncryptedMessageRequest EncryptMessage(GeneratedMessageV3 request) {
        //TODO: Check IV input
        try {
            Key tempKey = CryptographyImpl.generateAESKey();
            byte[] encryptedData = CryptographyImpl.encryptAES("", request.toByteArray(), tempKey);

            byte[] encryptedKey = CryptographyImpl.encryptRSA(tempKey.getEncoded(), leadServerPublicKey);
        
            byte[] digitalSignature = CryptographyImpl.generateDigitalSignature(request.toByteArray(), (PrivateKey) ks.getKey(username + "_private_key", pwdArray));
            
            Timestamp timestamp = Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build();

            byte[] encryptedTimestamp = CryptographyImpl.encryptRSA(timestamp.toByteArray(), leadServerPublicKey);
            
            ClientServer.EncryptedMessageRequest encryptedReq = ClientServer.EncryptedMessageRequest.newBuilder()
                                                    .setMessageRequestBytes(ByteString.copyFrom(encryptedData))
                                                    .setEncryptionKey(ByteString.copyFrom(encryptedKey))
                                                    .setDigitalSignature(ByteString.copyFrom(digitalSignature))
                                                    .setTimestamp(ByteString.copyFrom(encryptedTimestamp))
                                                    .setUserName(username)
                                                    .build();
            
            return encryptedReq;
            
        } catch (Exception e) {
            System.out.println("Failed to encrypt request: " + e.getMessage());
            return ClientServer.EncryptedMessageRequest.getDefaultInstance();
        }
    }

    byte[] DecryptResponse(ClientServer.EncryptedMessageResponse response) {
        PrivateKey privKey = null;
        try {
            privKey = (PrivateKey) ks.getKey(username + "_private_key", pwdArray);
        } catch (Exception e) {
            System.out.println("Error fetching key");
            return null;
        }
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
		
        if(!CryptographyImpl.verifyDigitalSignature(responseDecryptedBytes, response.getDigitalSignature().toByteArray(), (PublicKey) leadServerPublicKey)) {
            System.out.println("ERROR - Received message doesn't match with the digital signature!");
            return null;
        }


        return responseDecryptedBytes;
	}

    public void ShutdownChannel() {
        channel.shutdownNow();
    }
    public void setStub(ClientToServerServiceGrpc.ClientToServerServiceBlockingStub newStub) {
        stub = newStub;
    }
}