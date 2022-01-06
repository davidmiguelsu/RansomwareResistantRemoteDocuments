package pt.tecnico.Client;

import java.io.File;
import java.io.FileInputStream;

import com.google.protobuf.ByteString;

import io.grpc.StatusRuntimeException;
import pt.tecnico.grpc.ClientServer;
import pt.tecnico.grpc.ClientToServerServiceGrpc;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class ClientCommandImpl {
    ClientToServerServiceGrpc.ClientToServerServiceBlockingStub stub = null;

    ClientCommandImpl(ClientToServerServiceGrpc.ClientToServerServiceBlockingStub serverStub) {
        stub = serverStub;
    }  
    
    public boolean ExecuteCommand(String input) throws ZKNamingException{
        String[] args = input.split("[ ]+");
        try{
            switch (args[0]){
                case "write":
                case "w":            
                    writeFile(args);
                    break;
                
                case "read":
                case "r":
                    readFile(args);
                    break;

                case "list":
                case "ls":
                    listFiles();
                    break;
                default:
                    System.out.println("ERROR - Invalid Command");
                    break;
            }
        }
        catch (StatusRuntimeException sre) {
            // if(!ConnectToServer(zooKeeperHost, zooKeeperPort, serverPath))
            //     throw new ZKNamingException();
            // else
            //     ExecuteCommand(input);      //Repeat the parsing if it managed to reconnect
        }
        return true;
    }

    void writeFile(String[] args) {
        if(args.length < 1 || args.length > 3) {
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
            //TODO: handle exception
        }
		
    }
    
    void readFile(String[] args) {
        if(args.length > 2 || args.length < 1) {
            System.out.println("ERRO - Formato read");
            return;
        }

        String fileName = args[1];

        try{

            ClientServer.ReadFileRequest request =  ClientServer.ReadFileRequest.newBuilder()
                                                        .setFileName(fileName)
                                                        .build();
            
            ClientServer.ReadFileResponse response = 1;



        } catch (Exception e) {
            //TODO: handle exception
        }
    
    }
    

    void listFiles(){
        
    }
}


