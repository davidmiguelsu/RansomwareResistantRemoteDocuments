package pt.tecnico.Server;

import pt.tecnico.Common.CryptographyImpl;
import pt.tecnico.Server.ServerController.ChildServerInfo;
import pt.tecnico.grpc.ClientServer;
import pt.tecnico.grpc.ClientToServerServiceGrpc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;

import io.grpc.stub.StreamObserver;

public class ClientServerServiceImpl extends ClientToServerServiceGrpc.ClientToServerServiceImplBase {
	public static String filePath = "";
    private ServerController serverController;


	//TODO: Temp dict while we don't have a DB for users
	private Map<String, String> userDictionary = new HashMap<>();


	@Override
	public void greeting(ClientServer.HelloRequest request, StreamObserver<ClientServer.HelloResponse> responseObserver) {
		//TODO: Working under assumption that the leader server is the only one that can receive pings atm

		System.out.println("Received ping");

		serverController.addChildServerToList(request.getName());
	
		ClientServer.HelloResponse response = ClientServer.HelloResponse.getDefaultInstance();

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}


	@Override
	public void register(ClientServer.EncryptedMessageRequest request, StreamObserver<ClientServer.EncryptedMessageResponse> responseObserver) {
		// byte[] requestDecryptedBytes = CryptographyImpl.decryptRSA(request.getMessageRequestBytes().toByteArray(), 
		// 	CryptographyImpl.readPrivateKey("/home/fenix/Documents/SIRS_Stuff/Repo/RansomwareResistantRemoteDocuments/CAServer/LeadServerKeys/leadServer_private.der"));

		byte[] requestDecryptedBytes = DecryptRequest(request);

		ClientServer.RegisterRequest decryptRequest = null;
		try {
			decryptRequest = ClientServer.RegisterRequest.parseFrom(requestDecryptedBytes);
		} catch (InvalidProtocolBufferException e) {
			//TODO: handle exception
		} 

		ClientServer.RegisterResponse res;
		if(userDictionary.containsKey(decryptRequest.getUserName())) {
			res = ClientServer.RegisterResponse.newBuilder()
				.setAck("ERROR").build();
		}
		else {
			userDictionary.put(decryptRequest.getUserName(), decryptRequest.getCipheredPassword());
			res = ClientServer.RegisterResponse.newBuilder()
			.setAck("OK").build();
		}

		//TODO: Add the encryption
		// ClientServer.EncryptedMessageResponse encryptedRes = ClientServer.EncryptedMessageResponse.newBuilder()
		// 	.setMessageResponseBytes(res.toByteString()).build();

		responseObserver.onNext(EncryptResponse(res));
		responseObserver.onCompleted();
	}

	@Override
	public void login(ClientServer.EncryptedMessageRequest request, StreamObserver<ClientServer.EncryptedMessageResponse> responseObserver) {

		// byte[] requestDecryptedBytes = CryptographyImpl.decryptRSA(request.getMessageRequestBytes().toByteArray(), 
		// 	CryptographyImpl.readPrivateKey("/home/fenix/Documents/SIRS_Stuff/Repo/RansomwareResistantRemoteDocuments/CAServer/LeadServerKeys/leadServer_private.der"));
		byte[] requestDecryptedBytes = DecryptRequest(request);

		ClientServer.LoginRequest decryptRequest = null;
		try {
			decryptRequest = ClientServer.LoginRequest.parseFrom(requestDecryptedBytes);
		} catch (InvalidProtocolBufferException e) {
			//TODO: handle exception
		} 
				

		ClientServer.LoginResponse res;
		if(!userDictionary.containsKey(decryptRequest.getUserName())) {
			res = ClientServer.LoginResponse.newBuilder()
				.setAck("ERROR").build();
				System.out.println("Doesn't contain key");
		}
		else {
			if(userDictionary.get(decryptRequest.getUserName()).equals(decryptRequest.getCipheredPassword())) {
				res = ClientServer.LoginResponse.newBuilder()
				.setAck("OK").build();				
			}
			else {
				res = ClientServer.LoginResponse.newBuilder()
				.setAck("ERROR").build();
			}
		}

		// ClientServer.EncryptedMessageResponse encryptedRes = ClientServer.EncryptedMessageResponse.newBuilder()
		// 	.setMessageResponseBytes(res.toByteString()).build();

		responseObserver.onNext(EncryptResponse(res));
		responseObserver.onCompleted();
	}

	@Override
	public void writeFile(ClientServer.EncryptedMessageRequest request, StreamObserver<ClientServer.EncryptedMessageResponse> responseObserver) {

		// byte[] decryptedTempKeyBytes = CryptographyImpl.decryptRSA(request.getEncryptionKey().toByteArray(), 
		// 	CryptographyImpl.readPrivateKey("/home/fenix/Documents/SIRS_Stuff/Repo/RansomwareResistantRemoteDocuments/CAServer/LeadServerKeys/leadServer_private.der"));
		// Key decryptTempKey = new SecretKeySpec(decryptedTempKeyBytes, 0, 16, "AES");

		// //TODO: Check IV later
		// byte[] requestDecryptedBytes = CryptographyImpl.decryptAES("", request.getMessageRequestBytes().toByteArray(), decryptTempKey);
		// byte[] requestDecryptedBytes = CryptographyImpl.decryptRSA(request.getMessageRequestBytes().toByteArray(), 
		// 	CryptographyImpl.readPrivateKey("/home/fenix/Documents/SIRS_Stuff/Repo/RansomwareResistantRemoteDocuments/CAServer/LeadServerKeys/leadServer_private.der"));
		byte[] requestDecryptedBytes = DecryptRequest(request);

		ClientServer.WriteFileRequest decryptRequest = null;
		try {
			decryptRequest = ClientServer.WriteFileRequest.parseFrom(requestDecryptedBytes);
		} catch (InvalidProtocolBufferException e) {
			//TODO: handle exception
		} 

		File file = new File(filePath + decryptRequest.getFileName());
		try {
				
			FileOutputStream writer = new FileOutputStream(file, false);
			writer.write(decryptRequest.getFile().toByteArray());

			//TODO: Temp hash file -> TO BE MOVED TO DB
			File hashFile = new File(filePath + decryptRequest.getFileName() + ".hash");
			FileOutputStream hashWriter = new FileOutputStream(hashFile, false);
			hashWriter.write(decryptRequest.getHash().toByteArray());
			hashWriter.close();

			writer.close();
			
			System.out.println("Sucessfull write of file " + decryptRequest.getFileName());

			if(serverController.isLeader) {
				for (ChildServerInfo info : serverController.childServerList) {
					
					ClientServer.EncryptedMessageResponse childRes = info.stub.writeFile(request);

					//TODO: CHeck if response is an OK or ERROR
				}
			}

			
			ClientServer.WriteFileResponse response = ClientServer.WriteFileResponse.newBuilder()
			.setAck("Confirmed write of file " + decryptRequest.getFileName())
			.build();

			// ClientServer.EncryptedMessageResponse encryptedRes = ClientServer.EncryptedMessageResponse.newBuilder()
			// .setMessageResponseBytes(response.toByteString()).build();
	
			// Use responseObserver to send a single response back
			responseObserver.onNext(EncryptResponse(response));
	
			// When you are done, you must call onCompleted
			responseObserver.onCompleted();
		} catch (Exception e) {
			//TODO: handle exception
			ClientServer.WriteFileResponse response = ClientServer.WriteFileResponse.newBuilder()
			.setAck("Failed to write file " + decryptRequest.getFileName())
			.build();
	
			// ClientServer.EncryptedMessageResponse encryptedRes = ClientServer.EncryptedMessageResponse.newBuilder()
			// .setMessageResponseBytes(response.toByteString()).build();

			// Use responseObserver to send a single response back
			responseObserver.onNext(EncryptResponse(response));
	
			// When you are done, you must call onCompleted
			responseObserver.onCompleted();

			System.out.println(e.getStackTrace().toString());
		}
	}

	@Override
	public void readFile(ClientServer.EncryptedMessageRequest request, StreamObserver<ClientServer.EncryptedMessageResponse> responseObserver) {
		// byte[] decryptedTempKeyBytes = CryptographyImpl.decryptRSA(request.getEncryptionKey().toByteArray(), 
		// CryptographyImpl.readPrivateKey("/home/fenix/Documents/SIRS_Stuff/Repo/RansomwareResistantRemoteDocuments/CAServer/LeadServerKeys/leadServer_private.der"));
		// Key decryptTempKey = new SecretKeySpec(decryptedTempKeyBytes, 0, 16, "AES");

		// //TODO: Check IV later
		// byte[] requestDecryptedBytes = CryptographyImpl.decryptAES("", request.getMessageRequestBytes().toByteArray(), decryptTempKey);
		byte[] requestDecryptedBytes = DecryptRequest(request);

		ClientServer.ReadFileRequest decryptRequest = null;
		try {
			decryptRequest = ClientServer.ReadFileRequest.parseFrom(requestDecryptedBytes);
		} catch (InvalidProtocolBufferException e) {
			//TODO: handle exception
		} 
		

		String name = decryptRequest.getFileName();

		File file = new File(filePath + name);
		try {
			FileInputStream fis = new FileInputStream(file);
			

			//TODO: Temp hash file -> TO BE MOVED TO DB
			File hashFile = new File(filePath + name + ".hash");
			FileInputStream hashFIS = new FileInputStream(hashFile);

			byte[] allBytes = hashFIS.readAllBytes();

			ClientServer.ReadFileResponse response = ClientServer.ReadFileResponse.newBuilder()
														.setFile(ByteString.copyFrom(fis.readAllBytes()))
														.setHash(ByteString.copyFrom(hashFIS.readAllBytes()))
														.build();

			// ClientServer.EncryptedMessageResponse encryptedRes = ClientServer.EncryptedMessageResponse.newBuilder()
			// 											.setMessageResponseBytes(response.toByteString()).build();
			ClientServer.EncryptedMessageResponse encryptedRes = EncryptResponse(response);	
			fis.close();
			hashFIS.close();
			List<ClientServer.EncryptedMessageResponse> responseList = new ArrayList<>();

			responseList.add(encryptedRes);

			if(serverController.isLeader) {
				for (ChildServerInfo info : serverController.childServerList) {
					responseList.add(info.stub.readFile(request));
					
					//TODO: CHeck if response is an OK or ERROR & check if file matches
				}
				
				// for (ClientServer.ReadFileResponse res : responseList) {
					
				// }
			}
			

			responseObserver.onNext(encryptedRes);

			responseObserver.onCompleted();
		} catch (Exception e) {
			//TODO: handle exception
		}
	}


	// public void listFiles(ClientServer.ListFileRequest request, StreamObserver<ClientServer.ListFileResponse> responseObserver) {
	@Override
	public void listFiles(ClientServer.EncryptedMessageRequest request, StreamObserver<ClientServer.EncryptedMessageResponse> responseObserver) {
		byte[] requestDecryptedBytes = DecryptRequest(request);

		// byte[] requestDecryptedBytes = CryptographyImpl.decryptRSA(request.getMessageRequestBytes().toByteArray(), 
		// 	CryptographyImpl.readPrivateKey("/home/fenix/Documents/SIRS_Stuff/Repo/RansomwareResistantRemoteDocuments/CAServer/LeadServerKeys/leadServer_private.der"));

			try {
				ClientServer.ListFileRequest decryptRequest = ClientServer.ListFileRequest.parseFrom(requestDecryptedBytes);
				
				List<String> fileList = Arrays.asList(new File(filePath).list());
				
				ClientServer.ListFileResponse response = ClientServer.ListFileResponse.newBuilder()
				.addAllFileName(fileList)
				.build();
				
				ClientServer.EncryptedMessageResponse encryptedRes = ClientServer.EncryptedMessageResponse.newBuilder()
				.setMessageResponseBytes(response.toByteString()).build();
				responseObserver.onNext(encryptedRes);
				responseObserver.onCompleted();
			} catch (Exception e) {
				//TODO: handle exception
			}
	}

	@Override
	public void deleteFile(ClientServer.EncryptedMessageRequest request, StreamObserver<ClientServer.EncryptedMessageResponse> responseObserver) {

		// byte[] decryptedTempKeyBytes = CryptographyImpl.decryptRSA(request.getEncryptionKey().toByteArray(), 
		// CryptographyImpl.readPrivateKey("/home/fenix/Documents/SIRS_Stuff/Repo/RansomwareResistantRemoteDocuments/CAServer/LeadServerKeys/leadServer_private.der"));
		// Key decryptTempKey = new SecretKeySpec(decryptedTempKeyBytes, 0, 16, "AES");

		// //TODO: Check IV later
		// byte[] requestDecryptedBytes = CryptographyImpl.decryptAES("", request.getMessageRequestBytes().toByteArray(), decryptTempKey);
		byte[] requestDecryptedBytes = DecryptRequest(request);
		ClientServer.DeleteFileRequest decryptRequest = null;
		try {
			decryptRequest = ClientServer.DeleteFileRequest.parseFrom(requestDecryptedBytes);	
		} catch (InvalidProtocolBufferException e) {
			//TODO: handle exception
		}


		String filename = decryptRequest.getFileName();
		File toDelete = new File(filePath + filename); 

		String ackStr = "---";

		if (toDelete.exists()){
			
			if (toDelete.delete()) { 
				ackStr = "Deleted the file: " + filename;
			} else {
				ackStr = "Failed to delete the file.";
			} 
  
			if(serverController.isLeader) {
				for (ChildServerInfo info : serverController.childServerList) {
					info.stub.deleteFile(request);
					
					//TODO: CHeck if response is an OK or ERROR & check if file matches
				}
				
				// for (ClientServer.ReadFileResponse res : responseList) {
					
				// }
			}
		} else {
			ackStr = "File does not exist.";
		}

		

		System.out.println(ackStr);

		ClientServer.DeleteFileResponse response = ClientServer.DeleteFileResponse.newBuilder()
			.setAck(ackStr)
			.build();

		// ClientServer.EncryptedMessageResponse encryptedRes = ClientServer.EncryptedMessageResponse.newBuilder()
		// .setMessageResponseBytes(response.toByteString()).build();

		ClientServer.EncryptedMessageResponse encryptedRes = EncryptResponse(response);	

		// Use responseObserver to send a single response back
		responseObserver.onNext(encryptedRes);

		// When you are done, you must call onCompleted
		responseObserver.onCompleted();

	}

	@Override
	public void givePermission(ClientServer.EncryptedMessageRequest request, StreamObserver<ClientServer.EncryptedMessageResponse> responseObserver) {
		if(serverController.isLeader) {
			
		}
	}

	//-------

	byte[] DecryptRequest(ClientServer.EncryptedMessageRequest request) {

		byte[] decryptedTempKeyBytes = CryptographyImpl.decryptRSA(request.getEncryptionKey().toByteArray(), 
		CryptographyImpl.readPrivateKey("/home/fenix/Documents/SIRS_Stuff/Repo/RansomwareResistantRemoteDocuments/CAServer/LeadServerKeys/leadServer_private.der"));
		Key decryptTempKey = new SecretKeySpec(decryptedTempKeyBytes, 0, 16, "AES");

		//TODO: Check IV later
		byte[] requestDecryptedBytes = CryptographyImpl.decryptAES("", request.getMessageRequestBytes().toByteArray(), decryptTempKey);
		return requestDecryptedBytes;
	}

    public void SetServerMain(ServerController main) {
        serverController = main;
    }

	ClientServer.EncryptedMessageResponse EncryptResponse(GeneratedMessageV3 response) {
		//TODO: Check IV input
		try {
			Key tempKey = CryptographyImpl.generateAESKey();
			byte[] encryptedData = CryptographyImpl.encryptAES("", response.toByteArray(), tempKey);
			byte[] encryptedKey = CryptographyImpl.encryptRSA(tempKey.getEncoded(), CryptographyImpl.readPublicKey("/home/fenix/Documents/SIRS_Stuff/Repo/RansomwareResistantRemoteDocuments/CAServer/ClientKeys/client_public.der"));
			
			ClientServer.EncryptedMessageResponse encryptedRes = ClientServer.EncryptedMessageResponse.newBuilder()
													.setMessageResponseBytes(ByteString.copyFrom(encryptedData))
													.setEncryptionKey(ByteString.copyFrom(encryptedKey))
													.build();
			
			return encryptedRes;
			
		} catch (Exception e) {
			//TODO: handle exception
			return null;
		}
	}

	public void SetupStoragePath() {
		filePath = System.getProperty("user.home") + "/Documents/SIRS_Test/"; //"RansomwareResistantRemoteDocuments/Server/Files/" + args[2];
		File dir = new File(filePath);
		if(!dir.exists()) {
			dir.mkdir();
		}
	}


	//TODO: Delete when moving testing to VMs
	public void SetupStoragePath(String path) {
		filePath = System.getProperty("user.home") + "/Documents/SIRS_Test/" + path.charAt(path.length() - 1) + "/"; //"RansomwareResistantRemoteDocuments/Server/Files/" + args[2];
		File dir = new File(filePath);
		if(!dir.exists()) {
			dir.mkdir();
		}
	}


}


