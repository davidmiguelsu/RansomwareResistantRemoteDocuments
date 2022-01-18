package pt.tecnico.Client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
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

    ClientCommandImpl() {
        // stub = serverStub;
        dirPath = System.getProperty("user.home") + "/Downloads/";
        keyStorePath = System.getProperty("user.home") + "/Documents/";
        keyPath = System.getProperty("user.home") + "/Documents/SIRS_Stuff/Repo" + "/RansomwareResistantRemoteDocuments/CAServer/";
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
        // return true;
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

            case "help":
            case "h":
                System.out.println(" \n The commands available are: \n Create File - write arg / w arg \n Read File - download arg / d arg \n List Files - list / ls \n Remove file - remove arg / rm arg \n Close the session -  exit \n \n");   
                break;            
            case "exit":
                CryptographyImpl.UpdateKeyStore(ks, pwdArray, keyStorePath + "standard.jceks");
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

            CryptographyImpl.UpdateKeyStore(ks, pwdArray, keyStorePath + "standard.jceks");

            byte[] encryptedFile = CryptographyImpl.encryptAES(fileName, fis.readAllBytes(), key);
            // byte[] encryptedFile = CryptographyImpl.encryptAES(fileName, fis.readAllBytes(), 
            //         CryptographyImpl.readAESKey(System.getProperty("user.home") + "/SIRS_KEYS/" + fileName + ".key"));


            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            byte[] hashBytes = digest.digest(fis.readAllBytes());

            // String sha3Hex = CryptographyImpl.bytesToHex(hashBytes);


            ClientServer.WriteFileRequest request = ClientServer.WriteFileRequest.newBuilder()
                                                        .setFileName(fileName)
                                                        .setFile(ByteString.copyFrom(encryptedFile))
                                                        .setHash(ByteString.copyFrom(hashBytes))
                                                        .build();
            
            ClientServer.EncryptedMessageRequest encryptedReq = EncryptMessage(request);

                                     

            ClientServer.EncryptedMessageResponse res = stub.writeFile(encryptedReq);
            try {
                byte[] responseBytes = DecryptResponse(res);
                ClientServer.WriteFileResponse response = ClientServer.WriteFileResponse.parseFrom(responseBytes);

                System.out.println(response.getAck());
            } catch (InvalidProtocolBufferException ipbe) {
                //TODO: handle exception
            }
            fis.close();
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

            ClientServer.ReadFileRequest request =  ClientServer.ReadFileRequest.newBuilder()
                                                        .setFileName(fileName)
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

            // System.out.println(response.getFile().toStringUtf8());

            
            Key key = ks.getKey(fileName + "_key", pwdArray);

            byte[] decryptedFile = CryptographyImpl.decryptAES(fileName, response.getFile().toByteArray(), key);
            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            byte[] hashBytes = digest.digest(decryptedFile);
            // String sha3Hex = CryptographyImpl.bytesToHex(hashBytes);
            
            // byte[] responseHash = Base64.getDecoder().decode(response.getHash());

            //TODO: Readd this later!
            // if(!hashBytes.equals(response.getHash().toByteArray())) {
            //     System.out.println("ERROR - Read - Hash of the downloaded file differs from the hash received! File may be compromised.");
            //     return;
            // }
            
            
            
            FileOutputStream writer = new FileOutputStream(dirPath + fileName);
            writer.write(decryptedFile);
			// writer.write(CryptographyImpl.decryptAES(fileName, response.getFile().toByteArray(), 
            //     CryptographyImpl.readAESKey(System.getProperty("user.home") + "/SIRS_KEYS/" + fileName + ".key")));

			writer.close();
	


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
            ClientServer.ListFileResponse response = ClientServer.ListFileResponse.parseFrom(res.getMessageResponseBytes());
    
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
            .build();
            
        ClientServer.EncryptedMessageRequest encryptedReq = EncryptMessage(request);

        ClientServer.EncryptedMessageResponse res = stub.deleteFile(encryptedReq);
        try {
            byte[] responseBytes = DecryptResponse(res);
            ClientServer.DeleteFileResponse response = ClientServer.DeleteFileResponse.parseFrom(responseBytes);
    
            System.out.println(response.getAck());
        } catch (InvalidProtocolBufferException ipbe) {
            //TODO: handle exception
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
            }
            else {
                System.out.println("ERROR - Register - Username already exists");
            }
        } catch (InvalidProtocolBufferException ipbe) {
            //TODO: handle exception
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
            }
            else {
                System.out.println("ERROR - Login - Authentication failed");
            }
        } catch (InvalidProtocolBufferException ipbe) {
            //TODO: handle exception
        }
    }

    public void logout() {
        username = null;
        System.out.println("Successful logout");
    }

    //------------
    public String connectToServer(String host, String port, String serverPath) {
        zooHost = host;
        zooPort = port;
        path = serverPath;

        System.out.println("Contacting ZooKeeper at " + host + ":" + port + "...");
        ZKNaming zkNaming = new ZKNaming(host, port);
		System.out.println("Looking up " + serverPath + "...");
		// final String target = zkNaming.lookup(path).getURI();
		// Collection<ZKRecord> records = zkNaming.listRecords(path);

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
            // Random rand = new Random();
            // int chosenServerIndex = rand.nextInt(numberOfServers);
            String target = recordList.get(0).getURI();     //Always connects to the first server (First server == Master server)

            channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            ClientToServerServiceGrpc.ClientToServerServiceBlockingStub newStub = ClientToServerServiceGrpc.newBlockingStub(channel);
            stub = newStub;
            System.out.println("Located server at " + target);

            //TODO: Change this
            ks = CryptographyImpl.InitializeKeyStore(pwdArray, keyStorePath + "standard.jceks");
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
            byte[] encryptedKey = CryptographyImpl.encryptRSA(tempKey.getEncoded(), CryptographyImpl.readPublicKey(keyPath + "LeadServerKeys/leadServer_public.der"));
            
            ClientServer.EncryptedMessageRequest encryptedReq = ClientServer.EncryptedMessageRequest.newBuilder()
                                                    .setMessageRequestBytes(ByteString.copyFrom(encryptedData))
                                                    .setEncryptionKey(ByteString.copyFrom(encryptedKey))
                                                    .build();
            
            return encryptedReq;
            
        } catch (Exception e) {
            //TODO: handle exception
            return null;
        }
    }

    byte[] DecryptResponse(ClientServer.EncryptedMessageResponse response) {

		byte[] decryptedTempKeyBytes = CryptographyImpl.decryptRSA(response.getEncryptionKey().toByteArray(), 
		CryptographyImpl.readPrivateKey(keyPath + "ClientKeys/client_private.der"));
		Key decryptTempKey = new SecretKeySpec(decryptedTempKeyBytes, 0, 16, "AES");

		//TODO: Check IV later
		byte[] requestDecryptedBytes = CryptographyImpl.decryptAES("", response.getMessageResponseBytes().toByteArray(), decryptTempKey);
		return requestDecryptedBytes;
	}

    public void ShutdownChannel() {
        channel.shutdownNow();
    }
    public void setStub(ClientToServerServiceGrpc.ClientToServerServiceBlockingStub newStub) {
        stub = newStub;
    }
}