package pt.tecnico.Client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import com.google.protobuf.ByteString;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.grpc.ClientServer;
import pt.tecnico.grpc.ClientToServerServiceGrpc;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class ClientCommandImpl {
    String zooHost = "";
    String zooPort = "";
    String path = "";

    ManagedChannel channel = null;
    ClientToServerServiceGrpc.ClientToServerServiceBlockingStub stub = null;
    String dirPath = "";

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
            case "exit":
                return false;
            default:
                System.out.println("ERROR - Invalid Command");
                break;
        }
        return true;
    }

    void writeFile(String[] args) {
        if(args.length == 1 || args.length > 3) {
            System.out.println("ERRO - Formato write");
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

            ClientServer.WriteFileRequest request = ClientServer.WriteFileRequest.newBuilder()
                                                        .setFileName(fileName)
                                                        .setFile(ByteString.copyFrom(fis.readAllBytes()))
                                                        .setHash("1")
                                                        .build();
            
            ClientServer.WriteFileResponse response = stub.writeFile(request);
            System.out.println(response.getAck());
            fis.close();
        } catch (Exception e) {
             System.out.println("ERRO - Formato write (File not found)");
        }
		
    }
    
    void readFile(String[] args) {
        if(args.length < 1 || args.length > 2) {
            System.out.println("ERRO - Formato read");
            return;
        }

        String fileName = args[1];

        try{

            ClientServer.ReadFileRequest request =  ClientServer.ReadFileRequest.newBuilder()
                                                        .setFileName(fileName)
                                                        .build();
            
            ClientServer.ReadFileResponse response = stub.readFile(request);
            // System.out.println(response.getFile().toStringUtf8());
            FileOutputStream writer = new FileOutputStream(dirPath + fileName);
			writer.write(response.getFile().toByteArray());

			writer.close();
	


        } catch (Exception e) {
            System.out.println("ERRO - Formato write (File not found)");
        }
    
    }
    

    void listFiles(){
        ClientServer.ListFileRequest request = ClientServer.ListFileRequest.newBuilder().build();

        ClientServer.ListFileResponse response = stub.listFiles(request);
        for (String fileName : response.getFileNameList()) {
            System.out.println(fileName);
        }
    }

    void deleteFile(String[] args){

        if(args.length != 2) {
            System.out.println("ERRO - Formato delete");
            return;
        }

        ClientServer.DeleteFileRequest request = ClientServer.DeleteFileRequest.newBuilder()
            .setFileName(args[1])
            .build();
            
        ClientServer.DeleteFileResponse response = stub.deleteFile(request);
        System.out.println(response.getAck());
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
            Random rand = new Random();
            int chosenServerIndex = rand.nextInt(numberOfServers);
            String target = recordList.get(chosenServerIndex).getURI();

            channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            ClientToServerServiceGrpc.ClientToServerServiceBlockingStub newStub = ClientToServerServiceGrpc.newBlockingStub(channel);
            stub = newStub;
            System.out.println("Located server at " + target);
            return "OK";
        } catch (ZKNamingException zkne) {
            System.err.println("ConnectToServer: Failed to lookup records" + zkne.getStackTrace());
            return "ERROR_LIST_RECORDS";
        }
    }

    public void ShutdownChannel() {
        channel.shutdownNow();
    }
    public void setStub(ClientToServerServiceGrpc.ClientToServerServiceBlockingStub newStub) {
        stub = newStub;
    }
}


