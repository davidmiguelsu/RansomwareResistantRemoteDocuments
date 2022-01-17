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

import com.google.protobuf.ByteString;

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



    KeyStore ks;
    char[] pwdArray;

    ClientCommandImpl() {
        // stub = serverStub;
        dirPath = System.getProperty("user.home") + "/Downloads/";
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

            case "delete":
            case "del":
                deleteFile(args);
                break;

            case "list":
            case "ls":
                listFiles();
                break;

            case "register":
                register(args);
                break;

            case "login":
                login(args);
                break;

            case "logout":
                logout();
                break;

            case "help":
            case "h":
                System.out.println(" \n The commands available are: \n Create File - write arg / w arg \n Read File - download arg / d arg \n List Files - list / ls \n Delete file - delete arg / d arg \n Close the session -  exit \n \n");   
                break;            
            case "exit":
                UpdateKeyStore();
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

            UpdateKeyStore();

            byte[] encryptedFile = CryptographyImpl.encryptFileAES(fileName, fis.readAllBytes(), key);
            // byte[] encryptedFile = CryptographyImpl.encryptFileAES(fileName, fis.readAllBytes(), 
            //         CryptographyImpl.readAESKey(System.getProperty("user.home") + "/SIRS_KEYS/" + fileName + ".key"));


            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            byte[] hashBytes = digest.digest(fis.readAllBytes());

            // String sha3Hex = CryptographyImpl.bytesToHex(hashBytes);


            ClientServer.WriteFileRequest request = ClientServer.WriteFileRequest.newBuilder()
                                                        .setFileName(fileName)
                                                        .setFile(ByteString.copyFrom(encryptedFile))
                                                        .setHash(ByteString.copyFrom(hashBytes))
                                                        .build();
            
            ClientServer.WriteFileResponse response = stub.writeFile(request);
            System.out.println(response.getAck());
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
            
            ClientServer.ReadFileResponse response = stub.readFile(request);
            // System.out.println(response.getFile().toStringUtf8());

            
            Key key = ks.getKey(fileName + "_key", pwdArray);

            byte[] decryptedFile = CryptographyImpl.decryptFileAES(fileName, response.getFile().toByteArray(), key);
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
			// writer.write(CryptographyImpl.decryptFileAES(fileName, response.getFile().toByteArray(), 
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

        ClientServer.ListFileRequest request = ClientServer.ListFileRequest.newBuilder().build();

        ClientServer.ListFileResponse response = stub.listFiles(request);
        for (String fileName : response.getFileNameList()) {
            System.out.println(fileName);
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
            
        ClientServer.DeleteFileResponse response = stub.deleteFile(request);
        System.out.println(response.getAck());
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
        
        ClientServer.RegisterResponse response = stub.register(request);

        if(!response.getAck().equals("ERROR")) {
            username = args[1];
        }
        else {
            System.out.println("ERROR - Register - Username already exists");
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

        ClientServer.LoginResponse response = stub.login(request);

        if(!response.getAck().equals("ERROR")) {
            username = args[1];
        }
        else {
            System.out.println("ERROR - Login - Authentication failed");
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

            InitializeKeyStore();
            return "OK";
        } catch (ZKNamingException zkne) {
            System.err.println("ConnectToServer: Failed to lookup records" + zkne.getStackTrace());
            return "ERROR_LIST_RECORDS";
        }
    }


    void InitializeKeyStore() {
        try {
            ks = KeyStore.getInstance("JCEKS");

            pwdArray = "pwd".toCharArray();      //TODO: Change password depending on user login?
            File jks = new File("/home/fenix/Documents/SIRS_Stuff/Repo/RansomwareResistantRemoteDocuments/Client/standard.jceks");
            if(jks.exists()) {
                System.out.println("Keystore exists, will load");
                ks = KeyStore.getInstance("JCEKS");
                ks.load(new FileInputStream(jks), pwdArray);
            }
            else {
                System.out.println("Keystore doesn't exist, will create");
                ks.load(null, pwdArray);

                FileOutputStream fos = new FileOutputStream(jks);
                ks.store(fos, pwdArray);
            }


        } catch (KeyStoreException kse) {
            //TODO: handle exception
        } catch (IOException ioe) {

        } catch (NoSuchAlgorithmException nsae) {

        } catch (CertificateException ce) {

        }
    }

    void UpdateKeyStore() {
        File jks = new File("/home/fenix/Documents/SIRS_Stuff/Repo/RansomwareResistantRemoteDocuments/Client/standard.jceks");
        try {
            if(jks.exists()) {
                FileOutputStream fos = new FileOutputStream(jks);
                ks.store(fos, pwdArray);
            }
            else {
                System.out.println("ERROR - KeyStore file not found");
            }
        } catch (KeyStoreException kse) {
            System.out.println("ERROR - KeyStore exception: " + kse.getMessage());
        } catch (FileNotFoundException e) {
            System.out.println("ERROR - KeyStore file not found");
        } catch (NoSuchAlgorithmException nsae) {
            
        } catch (CertificateException ce) {
            
        } catch (IOException ioe) {

        }
    }

    public void ShutdownChannel() {
        channel.shutdownNow();
    }
    public void setStub(ClientToServerServiceGrpc.ClientToServerServiceBlockingStub newStub) {
        stub = newStub;
    }
}


