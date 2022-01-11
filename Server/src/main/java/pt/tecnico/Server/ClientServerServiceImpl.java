package pt.tecnico.Server;

import pt.tecnico.grpc.ClientServer;
import pt.tecnico.grpc.ClientToServerServiceGrpc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

import com.google.protobuf.ByteString;

import io.grpc.stub.StreamObserver;

public class ClientServerServiceImpl extends ClientToServerServiceGrpc.ClientToServerServiceImplBase {
	public static String filePath = "";
    private ServerController serverMain;


	@Override
	public void greeting(ClientServer.HelloRequest request, StreamObserver<ClientServer.HelloResponse> responseObserver) {

		// HelloRequest has auto-generated toString method that shows its contents
		System.out.println(request);

		// You must use a builder to construct a new Protobuffer object
		ClientServer.HelloResponse response = ClientServer.HelloResponse.newBuilder()
				.setGreeting("Hello " + request.getName()).build();

		// Use responseObserver to send a single response back
		responseObserver.onNext(response);

		// When you are done, you must call onCompleted
		responseObserver.onCompleted();
	}

	@Override
	public void writeFile(ClientServer.WriteFileRequest request, StreamObserver<ClientServer.WriteFileResponse> responseObserver) {

		File file = new File(filePath + request.getFileName());
		try {
			FileOutputStream writer = new FileOutputStream(file, false);
			writer.write(request.getFile().toByteArray());

			writer.close();
			
			System.out.println("Sucessfull write of file " + request.getFileName());

			ClientServer.WriteFileResponse response = ClientServer.WriteFileResponse.newBuilder()
			.setAck("Confirmed write of file " + request.getFileName())
			.build();
	
			// Use responseObserver to send a single response back
			responseObserver.onNext(response);
	
			// When you are done, you must call onCompleted
			responseObserver.onCompleted();
		} catch (Exception e) {
			//TODO: handle exception
			ClientServer.WriteFileResponse response = ClientServer.WriteFileResponse.newBuilder()
			.setAck("Failed to write file " + request.getFileName())
			.build();
	
			// Use responseObserver to send a single response back
			responseObserver.onNext(response);
	
			// When you are done, you must call onCompleted
			responseObserver.onCompleted();

			System.out.println(e.getStackTrace().toString());
		}
		

	}

	@Override
	public void readFile(ClientServer.ReadFileRequest request, StreamObserver<ClientServer.ReadFileResponse> responseObserver) {
		String name = request.getFileName();

		File file = new File(filePath + name);
		try {
			FileInputStream fis = new FileInputStream(file);

            ClientServer.ReadFileResponse response = ClientServer.ReadFileResponse.newBuilder()
                                                        .setFile(ByteString.copyFrom(fis.readAllBytes()))
                                                        .build();
			fis.close();

			responseObserver.onNext(response);

			responseObserver.onCompleted();
		} catch (Exception e) {
			//TODO: handle exception
		}

	}


	@Override
	public void listFiles(ClientServer.ListFileRequest request, StreamObserver<ClientServer.ListFileResponse> responseObserver) {
		List<String> fileList = Arrays.asList(new File(filePath).list());

		ClientServer.ListFileResponse response = ClientServer.ListFileResponse.newBuilder()
													.addAllFileName(fileList)
													.build();

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void deleteFile(ClientServer.DeleteFileRequest request, StreamObserver<ClientServer.DeleteFileResponse> responseObserver) {


		String filename = request.getFileName();
		File toDelete = new File(filePath + filename); 

		String ackStr = "---";

		if (toDelete.exists()){
			
			if (toDelete.delete()) { 
				ackStr = "Deleted the file: " + filename;
			} else {
				ackStr = "Failed to delete the file.";
			} 
  
		} else {
			ackStr = "File does not exist.";
		}

		

		System.out.println(ackStr);

		ClientServer.DeleteFileResponse response = ClientServer.DeleteFileResponse.newBuilder()
			.setAck(ackStr)
			.build();
	
			// Use responseObserver to send a single response back
			responseObserver.onNext(response);
	
			// When you are done, you must call onCompleted
			responseObserver.onCompleted();

	}

	

	//-------

    public void SetServerMain(ServerController main) {
        serverMain = main;
    }

	public void SetupStoragePath() {
		filePath = System.getProperty("user.home") + "/Documents/SIRS_Test/"; //"RansomwareResistantRemoteDocuments/Server/Files/" + args[2];
		File dir = new File(filePath);
		if(!dir.exists()) {
			dir.mkdir();
		}
	}




}


