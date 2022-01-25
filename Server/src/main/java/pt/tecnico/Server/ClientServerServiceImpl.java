package pt.tecnico.Server;

import pt.tecnico.Common.CryptographyImpl;
import pt.tecnico.Server.ServerController.ChildServerInfo;
import pt.tecnico.grpc.ClientServer;
import pt.tecnico.grpc.ClientToServerServiceGrpc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;

import io.grpc.stub.StreamObserver;

public class ClientServerServiceImpl extends ClientToServerServiceGrpc.ClientToServerServiceImplBase {
	public static String filePath = "";
	//TODO: Don't have these in plain text
	public static String keyPaths = "";
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

		PublicKey targetPubKey = serverController.caServer.requestPublicKeyOf(request.getUserName(), true, false);
		byte[] requestDecryptedBytes = DecryptRequest(request, targetPubKey);

		ClientServer.RegisterRequest decryptRequest = null;
		try {
			decryptRequest = ClientServer.RegisterRequest.parseFrom(requestDecryptedBytes);
		} catch (InvalidProtocolBufferException e) {
			ClientServer.RegisterResponse res = ClientServer.RegisterResponse.newBuilder()
				.setAck("ERROR").build();

			System.out.println("Error in parse");



			responseObserver.onNext(EncryptResponse(res, targetPubKey));
			responseObserver.onCompleted();
		} 

		ClientServer.RegisterResponse res;
		//TODO: Remove the dictionary + add DB query
		if(userDictionary.containsKey(decryptRequest.getUserName())) {
			res = ClientServer.RegisterResponse.newBuilder()
				.setAck("ERROR").build();
		}
		else {
			userDictionary.put(decryptRequest.getUserName(), decryptRequest.getCipheredPassword());
			res = ClientServer.RegisterResponse.newBuilder()
			.setAck("OK").build();

			//TODO: Password needs to be encrypted
			serverController.db.addUserDatabase(serverController.conn , decryptRequest.getUserName(), decryptRequest.getCipheredPassword());
		}

		//TODO: Add the encryption
		// ClientServer.EncryptedMessageResponse encryptedRes = ClientServer.EncryptedMessageResponse.newBuilder()
		// 	.setMessageResponseBytes(res.toByteString()).build();

		responseObserver.onNext(EncryptResponse(res, targetPubKey));
		responseObserver.onCompleted();
	}

	@Override
	public void login(ClientServer.EncryptedMessageRequest request, StreamObserver<ClientServer.EncryptedMessageResponse> responseObserver) {

		// byte[] requestDecryptedBytes = CryptographyImpl.decryptRSA(request.getMessageRequestBytes().toByteArray(), 
		// 	CryptographyImpl.readPrivateKey("/home/fenix/Documents/SIRS_Stuff/Repo/RansomwareResistantRemoteDocuments/CAServer/LeadServerKeys/leadServer_private.der"));
		PublicKey targetPubKey = serverController.caServer.requestPublicKeyOf(request.getUserName(), true, false);


		byte[] requestDecryptedBytes = DecryptRequest(request, targetPubKey);

		ClientServer.LoginRequest decryptRequest = null;
		try {
			decryptRequest = ClientServer.LoginRequest.parseFrom(requestDecryptedBytes);
		} catch (InvalidProtocolBufferException e) {
			//TODO: handle exception
		} 
				

		ClientServer.LoginResponse res;

		
		// if(!userDictionary.containsKey(decryptRequest.getUserName())) {
		// 	res = ClientServer.LoginResponse.newBuilder()
		// 		.setAck("ERROR").build();
		// 		System.out.println("Doesn't contain key");
		// }	
			//userDictionary.get(decryptRequest.getUserName()).equals(decryptRequest.getCipheredPassword())
		
			//query to retrieve password for authentication purposes
		String tempPW = serverController.db.getUserPWbyUsername(serverController.conn, decryptRequest.getUserName());

		if(decryptRequest.getCipheredPassword().equals(tempPW)) {
			res = ClientServer.LoginResponse.newBuilder()
			.setAck("OK").build();				
		}
		else {
			res = ClientServer.LoginResponse.newBuilder()
			.setAck("ERROR").build();
		}
		

		// ClientServer.EncryptedMessageResponse encryptedRes = ClientServer.EncryptedMessageResponse.newBuilder()
		// 	.setMessageResponseBytes(res.toByteString()).build();

		responseObserver.onNext(EncryptResponse(res, targetPubKey));
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
		
		PublicKey targetPubKey = serverController.caServer.requestPublicKeyOf(request.getUserName(), true, false);

		byte[] requestDecryptedBytes = DecryptRequest(request, targetPubKey);

		ClientServer.WriteFileRequest decryptRequest = null;
		try {
			decryptRequest = ClientServer.WriteFileRequest.parseFrom(requestDecryptedBytes);
		} catch (InvalidProtocolBufferException e) {
			//TODO: handle exception
		} 

		if (serverController.isLeader && serverController.db.doesFileExist(serverController.conn, decryptRequest.getFileName())){
			System.out.println("File with that name already exists, consider changing the name!!");	

			ClientServer.WriteFileResponse response = ClientServer.WriteFileResponse.newBuilder()
				.setAck("ERROR - Confirmed write of file " + decryptRequest.getFileName())
				.build();
			responseObserver.onNext(EncryptResponse(response, targetPubKey));
	
			responseObserver.onCompleted();
			return;
		}

		File file = new File(filePath + decryptRequest.getFileName());
		try {
			FileOutputStream writer = new FileOutputStream(file, false);
			writer.write(decryptRequest.getFile().toByteArray());

			
			if(serverController.isLeader) {
				int tempNameID = serverController.db.getUserIDbyUsername(serverController.conn , decryptRequest.getUsername());
				serverController.db.addFileDatabase(serverController.conn , decryptRequest.getFileName(), decryptRequest.getHash().toByteArray());
				
				int tempFileID = serverController.db.getFileIDbyFileName(serverController.conn , decryptRequest.getFileName());
				serverController.db.addUserFileDatabase(serverController.conn, tempNameID, tempFileID);
			}
			//TODO: Temp hash file -> TO BE MOVED TO DB
			// File hashFile = new File(filePath + decryptRequest.getFileName() + ".hash");
			// FileOutputStream hashWriter = new FileOutputStream(hashFile, false);
			// hashWriter.write(decryptRequest.getHash().toByteArray());
			// hashWriter.close();

			writer.close();
			
			System.out.println("Sucessfull write of file " + decryptRequest.getFileName());
			
			if(serverController.isLeader) {
				List<ClientServer.WriteFileResponse> responseList = new ArrayList<>();

				HashMap<String, String> responseMap = new HashMap<>();
				int i = 2;

				for (ChildServerInfo info : serverController.childServerList) {
					PublicKey targetServerPublicKey = serverController.caServer.requestPublicKeyOf(String.valueOf(i), true, false);
											// ClientServer.WriteFileRequest recreatedRequest = ClientServer.WriteFileRequest.newBuilder()
					// 													.setFileName(decryptRequest.getFileName())
					// 													.setFile(ByteString.copyFrom(decry))
					// 													.setHash(ByteString.copyFrom(hashBytes))
					// 													.setUsername(username)
					// 													.build();


					PublicKey targetPublicKey = serverController.caServer.requestPublicKeyOf(String.valueOf(i), true, false);

					ClientServer.EncryptedMessageResponse childRes = info.stub.writeFile(EncryptRequest(decryptRequest, targetServerPublicKey));
					i++;
					try {
						ClientServer.WriteFileResponse decryptedChildRes = ClientServer.WriteFileResponse.parseFrom(DecryptResponse(childRes, targetServerPublicKey));
						responseList.add(decryptedChildRes);
					} catch (InvalidProtocolBufferException e) {
						System.out.println("ERROR - Failed to decrypt response from server" + i + " -- "+ e.getMessage());
						// responseObserver.onNext(ClientServer.EncryptedMessageResponse.getDefaultInstance());
						
						// responseObserver.onCompleted();
						// return;
					} 
					//TODO: CHeck if response is an OK or ERROR
				}
				int resOK = 0;
				int resNOK = 0;
				for (ClientServer.WriteFileResponse res : responseList) {
					if(res.getAck().startsWith("OK")) {
						resOK++;
					}
					else
						resNOK++;
				}

				if(resOK < serverController.childServerList.size() / 2 + 1) {
					ClientServer.WriteFileResponse response = ClientServer.WriteFileResponse.newBuilder()
						.setAck("ERROR - Failed to write correctly to sufficient servers" + decryptRequest.getFileName())
						.build();

						responseObserver.onNext(EncryptResponse(response, targetPubKey));
						responseObserver.onCompleted();
						return;
				}

			}

			
			ClientServer.WriteFileResponse response = ClientServer.WriteFileResponse.newBuilder()
			.setAck("OK - Confirmed write of file " + decryptRequest.getFileName())
			.build();

			// ClientServer.EncryptedMessageResponse encryptedRes = ClientServer.EncryptedMessageResponse.newBuilder()
			// .setMessageResponseBytes(response.toByteString()).build();
	
			// Use responseObserver to send a single response back
			responseObserver.onNext(EncryptResponse(response, targetPubKey));
	
			// When you are done, you must call onCompleted
			responseObserver.onCompleted();
		} catch (Exception e) {
			//TODO: handle exception
			ClientServer.WriteFileResponse response = ClientServer.WriteFileResponse.newBuilder()
			.setAck("ERROR - Failed to write file " + decryptRequest.getFileName())
			.build();
	
			// ClientServer.EncryptedMessageResponse encryptedRes = ClientServer.EncryptedMessageResponse.newBuilder()
			// .setMessageResponseBytes(response.toByteString()).build();

			// Use responseObserver to send a single response back
			responseObserver.onNext(EncryptResponse(response, targetPubKey));
	
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
		
		PublicKey targetPubKey = serverController.caServer.requestPublicKeyOf(request.getUserName(), true, false);

		byte[] requestDecryptedBytes = DecryptRequest(request, targetPubKey);

		ClientServer.ReadFileRequest decryptRequest = null;
		try {
			decryptRequest = ClientServer.ReadFileRequest.parseFrom(requestDecryptedBytes);
		} catch (InvalidProtocolBufferException e) {
			System.out.println("ERROR - Failed to decrypt request" + e.getMessage());
			responseObserver.onNext(ClientServer.EncryptedMessageResponse.getDefaultInstance());

			responseObserver.onCompleted();
			return;
		} 
		

		String fileName = decryptRequest.getFileName();
		String userName = decryptRequest.getUsername();

		if(serverController.isLeader) {
			int fileID = serverController.db.getFileIDbyFileName(serverController.conn, fileName);
			int userID = serverController.db.getUserIDbyUsername(serverController.conn, userName);
			//verificar se existe entry em user_files com file_id e user_id
					
			//TODO: esta verificaçao should be enough -> if true = tem read perms otherwise não tem e é suposto printar que u user n tem perms para ler.
			if(!serverController.db.doesUserHaveReadPerms(serverController.conn, userID, fileID)){
				ClientServer.ReadFileResponse response =  ClientServer.ReadFileResponse.getDefaultInstance();
				
				responseObserver.onNext(EncryptResponse(response, targetPubKey));
	
				responseObserver.onCompleted();
	
				return;
			}
		}

		File file = new File(filePath + fileName);
		try {
			FileInputStream fis = new FileInputStream(file);
			

			//TODO: Temp hash file -> TO BE MOVED TO DB
			// File hashFile = new File(filePath + fileName + ".hash");
			// FileInputStream hashFIS = new FileInputStream(hashFile);

			// byte[] allBytes = hashFIS.readAllBytes();

			ClientServer.ReadFileResponse response = ClientServer.ReadFileResponse.newBuilder()
														.setFile(ByteString.copyFrom(fis.readAllBytes()))
														.setHash(ByteString.copyFrom("".getBytes()))
														.build();

			// ClientServer.EncryptedMessageResponse encryptedRes = ClientServer.EncryptedMessageResponse.newBuilder()
			// 											.setMessageResponseBytes(response.toByteString()).build();
			ClientServer.EncryptedMessageResponse encryptedRes = EncryptResponse(response, targetPubKey);	
			fis.close();
			// hashFIS.close();
			List<ClientServer.ReadFileResponse> responseList = new ArrayList<>();

			responseList.add(response);

			if(serverController.isLeader) {
				int i = 2;
				HashMap<ByteBuffer, MessageOccurence> messageDigests = new HashMap<>();

				for (ChildServerInfo info : serverController.childServerList) {
					
					PublicKey targetServerPublicKey = serverController.caServer.requestPublicKeyOf(String.valueOf(i), true, false);
					
					ClientServer.EncryptedMessageResponse childRes = info.stub.readFile(EncryptRequest(decryptRequest, targetServerPublicKey));
					byte[] decryptedChildResBytes = DecryptResponse(childRes, targetServerPublicKey);

					ClientServer.ReadFileResponse decryptedChildRes = null;
					try {
						decryptedChildRes = ClientServer.ReadFileResponse.parseFrom(decryptedChildResBytes);
						responseList.add(decryptedChildRes);
					} catch (InvalidProtocolBufferException e) {
						System.out.println("ERROR - Failed to decrypt response from server" + i + " -- "+ e.getMessage());
						// responseObserver.onNext(ClientServer.EncryptedMessageResponse.getDefaultInstance());
						
						// responseObserver.onCompleted();
						// return;
					} 
					i++;

				}
				
				for (ClientServer.ReadFileResponse res : responseList) {
					byte[] digest = CryptographyImpl.GenerateSHA3Digest(res.toByteArray());

					if(messageDigests.containsKey(ByteBuffer.wrap(digest))){
						messageDigests.get(ByteBuffer.wrap(digest)).AddElement(res);
					}
					else {
						messageDigests.put(ByteBuffer.wrap(digest), new MessageOccurence(res));
					}
				}

				for (var entry : messageDigests.entrySet()) {
					if(entry.getValue().GetLength() >= (serverController.childServerList.size() / 2) + 1) {
						encryptedRes = EncryptResponse(entry.getValue().GetElement(), targetPubKey);
					}
				}
			}
			

			responseObserver.onNext(encryptedRes);

			responseObserver.onCompleted();
		} catch (Exception e) {
			//TODO: handle exception
			System.out.println("ERROR - Failed to send requested file: " + e.getMessage());
			responseObserver.onNext(ClientServer.EncryptedMessageResponse.getDefaultInstance());

			responseObserver.onCompleted();
		}
	}


	// public void listFiles(ClientServer.ListFileRequest request, StreamObserver<ClientServer.ListFileResponse> responseObserver) {
	@Override
	public void listFiles(ClientServer.EncryptedMessageRequest request, StreamObserver<ClientServer.EncryptedMessageResponse> responseObserver) {
		
		PublicKey targetPubKey = serverController.caServer.requestPublicKeyOf(request.getUserName(), true, false);

		byte[] requestDecryptedBytes = DecryptRequest(request, targetPubKey);

		// byte[] requestDecryptedBytes = CryptographyImpl.decryptRSA(request.getMessageRequestBytes().toByteArray(), 
		// 	CryptographyImpl.readPrivateKey("/home/fenix/Documents/SIRS_Stuff/Repo/RansomwareResistantRemoteDocuments/CAServer/LeadServerKeys/leadServer_private.der"));

			try {
				ClientServer.ListFileRequest decryptRequest = ClientServer.ListFileRequest.parseFrom(requestDecryptedBytes);
				
				int tempID = serverController.db.getUserIDbyUsername(serverController.conn, decryptRequest.getUserName());
				
				//TODO: falta ver como dar print nisto.
				List<String> listaFiles = (serverController.db.getListFile(serverController.conn, tempID));
				List<String> fileList = Arrays.asList(new File(filePath).list());
				
				ClientServer.ListFileResponse response = ClientServer.ListFileResponse.newBuilder()
				.addAllFileName(fileList)
				.build();
				
				// ClientServer.EncryptedMessageResponse encryptedRes = ClientServer.EncryptedMessageResponse.newBuilder()
				// .setMessageResponseBytes(response.toByteString()).build();
				responseObserver.onNext(EncryptResponse(response, targetPubKey));
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
		
		PublicKey targetPubKey = serverController.caServer.requestPublicKeyOf(request.getUserName(), true, false);

		byte[] requestDecryptedBytes = DecryptRequest(request, targetPubKey);
		ClientServer.DeleteFileRequest decryptRequest = null;
		try {
			decryptRequest = ClientServer.DeleteFileRequest.parseFrom(requestDecryptedBytes);	
		} catch (InvalidProtocolBufferException e) {
			//TODO: handle exception
		}

		if(serverController.isLeader) {
			int tempUserID = serverController.db.getUserIDbyUsername(serverController.conn, decryptRequest.getUsername());
			int tempFileID = serverController.db.getFileIDbyFileName(serverController.conn , decryptRequest.getFileName());	
			serverController.db.deleteFileUserDatabase(serverController.conn, tempUserID, tempFileID);
			serverController.db.deleteFileDatabase(serverController.conn, tempFileID);
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

		ClientServer.EncryptedMessageResponse encryptedRes = EncryptResponse(response, targetPubKey);	

		// Use responseObserver to send a single response back
		responseObserver.onNext(encryptedRes);

		// When you are done, you must call onCompleted
		responseObserver.onCompleted();

	}

	@Override
	public void givePermission(ClientServer.EncryptedMessageRequest request, StreamObserver<ClientServer.EncryptedMessageResponse> responseObserver) {
		if(serverController.isLeader) {
			PublicKey targetPubKey = serverController.caServer.requestPublicKeyOf(request.getUserName(), true, false);

			byte[] requestDecryptedBytes = DecryptRequest(request, targetPubKey);
			ClientServer.GivePermissionsRequest decryptRequest = null;
			try {
				decryptRequest = ClientServer.GivePermissionsRequest.parseFrom(requestDecryptedBytes);	
			} catch (InvalidProtocolBufferException e) {
				//TODO: handle exception
			}

			if(serverController.isLeader) {
				int tempUserID = serverController.db.getUserIDbyUsername(serverController.conn, decryptRequest.getUserName());
				int tempFileID = serverController.db.getFileIDbyFileName(serverController.conn, decryptRequest.getFileName());
	
	
	
				//TODO: verificar este if for sure aint 100% done
				if (decryptRequest.getPermission().equals("all")) {	
					serverController.db.giveAllPermission(serverController.conn, tempUserID, tempFileID);
				} 
				else{
					serverController.db.giveReadPermission(serverController.conn, tempUserID, tempFileID);
				}
			}



			File file = new File(filePath + decryptRequest.getFileName() + "_" + decryptRequest.getUserName());

			try {
				FileOutputStream writer = new FileOutputStream(file, false);
				writer.write(decryptRequest.getKey().toByteArray());
			} catch (FileNotFoundException fnfe) {
				//TODO: handle exception, but shouldn't occur?
			} catch (IOException ioe) {
				System.out.println("ERROR - Failed ");
				ClientServer.GivePermissionsResponse response = ClientServer.GivePermissionsResponse.newBuilder()
				.setAck("ERROR -Failed write of key for user: " + decryptRequest.getUserName())
				.build();
				responseObserver.onNext(EncryptResponse(response, targetPubKey));
				responseObserver.onCompleted();
				return;
			}

			ClientServer.GivePermissionsResponse response = ClientServer.GivePermissionsResponse.newBuilder()
				.setAck("Confirmed write of key for user: " + decryptRequest.getUserName())
				.build();
			
			responseObserver.onNext(EncryptResponse(response, targetPubKey));
			responseObserver.onCompleted();

		}
	}
	@Override
	public void updatePermissions(ClientServer.EncryptedMessageRequest request, StreamObserver<ClientServer.EncryptedMessageResponse> responseObserver) {
		if(serverController.isLeader) {
			PublicKey targetPubKey = serverController.caServer.requestPublicKeyOf(request.getUserName(), true, false);
			byte[] requestDecryptedBytes = DecryptRequest(request, targetPubKey);
			ClientServer.UpdatePermissionsRequest decryptRequest = null;
			try {
				decryptRequest = ClientServer.UpdatePermissionsRequest.parseFrom(requestDecryptedBytes);	
			} catch (InvalidProtocolBufferException e) {
				//TODO: handle exception
			}

			//For each key the user has access to

			ClientServer.UpdatePermissionsResponse response = ClientServer.UpdatePermissionsResponse.getDefaultInstance();
			// .newBuilder()
			// .setAck("Confirmed write of key for user: " + decryptRequest.getUserName())
			// .build();
		
		responseObserver.onNext(EncryptResponse(response, targetPubKey));
		responseObserver.onCompleted();
		}
	}
	//-------

	byte[] DecryptRequest(ClientServer.EncryptedMessageRequest request, PublicKey targetPubKey) {

		PrivateKey privKey = (PrivateKey) serverController.caServer.FetchPrivateKey();
		// PrivateKey privKey = CryptographyImpl.readPrivateKey(keyPaths + "LeadServerKeys/leadServer_private.der");
		byte[] decryptedTimestamp = CryptographyImpl.decryptRSA(request.getTimestamp().toByteArray(), privKey);

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

		byte[] decryptedTempKeyBytes = CryptographyImpl.decryptRSA(request.getEncryptionKey().toByteArray(), privKey);
		// byte[] decryptedTempKeyBytes = CryptographyImpl.decryptRSA(request.getEncryptionKey().toByteArray(), 
		//     CryptographyImpl.readPublicKey(keyPaths + "ClientKeys/client_public.der"));
		// CryptographyImpl.readPrivateKey("/home/fenix/Documents/SIRS_Stuff/Repo/RansomwareResistantRemoteDocuments/CAServer/LeadServerKeys/leadServer_private.der"));
		Key decryptTempKey = new SecretKeySpec(decryptedTempKeyBytes, 0, 16, "AES");

		//TODO: Check IV later
		byte[] requestDecryptedBytes = CryptographyImpl.decryptAES("", request.getMessageRequestBytes().toByteArray(), decryptTempKey);

		// PublicKey pubKey = serverController.caServer.requestPublicKeyOf(request.getUserName(), true, false);

		if(!CryptographyImpl.verifyDigitalSignature(requestDecryptedBytes, request.getDigitalSignature().toByteArray(), targetPubKey)) {
            System.out.println("ERROR - Received message doesn't match with the digital signature!");
            return null;
        }

		return requestDecryptedBytes;
	}

	byte[] DecryptResponse(ClientServer.EncryptedMessageResponse response, PublicKey targetPubKey) {
		PrivateKey privKey = (PrivateKey) serverController.caServer.FetchPrivateKey();

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
		
        // if(!CryptographyImpl.verifyDigitalSignature(responseDecryptedBytes, response.getDigitalSignature().toByteArray(), CryptographyImpl.readPublicKey(keyPath + "LeadServerKeys/leadServer_public.der"))) {
        if(!CryptographyImpl.verifyDigitalSignature(responseDecryptedBytes, response.getDigitalSignature().toByteArray(), targetPubKey)) {
            System.out.println("ERROR - Received message doesn't match with the digital signature!");
            return null;
        }


        return responseDecryptedBytes;
	}

    public void SetServerMain(ServerController main) {
        serverController = main;
    }

	ClientServer.EncryptedMessageResponse EncryptResponse(GeneratedMessageV3 response, PublicKey targetPubKey) {
		//TODO: Check IV input
		try {
			PrivateKey privKey = (PrivateKey) serverController.caServer.FetchPrivateKey();

			Key tempKey = CryptographyImpl.generateAESKey();
			byte[] encryptedData = CryptographyImpl.encryptAES("", response.toByteArray(), tempKey);
			byte[] encryptedKey = CryptographyImpl.encryptRSA(tempKey.getEncoded(), targetPubKey);
            byte[] digitalSignature = CryptographyImpl.generateDigitalSignature(response.toByteArray(), privKey);

			Timestamp timestamp = Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build();
			byte[] encryptedTimestamp = CryptographyImpl.encryptRSA(timestamp.toByteArray(), targetPubKey);
			// byte[] encryptedKey = CryptographyImpl.encryptRSA(noSignatureEncryptedKey, CryptographyImpl.readPrivateKey(keyPaths + "LeadServerKeys/leadServer_private.der"));

			// byte[] encryptedKey = CryptographyImpl.encryptRSA(tempKey.getEncoded(), CryptographyImpl.readPublicKey("/home/fenix/Documents/SIRS_Stuff/Repo/RansomwareResistantRemoteDocuments/CAServer/ClientKeys/client_public.der"));
			
			ClientServer.EncryptedMessageResponse encryptedRes = ClientServer.EncryptedMessageResponse.newBuilder()
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

	ClientServer.EncryptedMessageRequest EncryptRequest(GeneratedMessageV3 request, PublicKey targetPublicKey) {
        //TODO: Check IV input
        try {
			PrivateKey privKey = (PrivateKey) serverController.caServer.FetchPrivateKey();

            Key tempKey = CryptographyImpl.generateAESKey();
            byte[] encryptedData = CryptographyImpl.encryptAES("", request.toByteArray(), tempKey);

            byte[] encryptedKey = CryptographyImpl.encryptRSA(tempKey.getEncoded(), targetPublicKey);
            // byte[] encryptedKey = CryptographyImpl.encryptRSA(tempKey.getEncoded(), CryptographyImpl.readPublicKey(keyPath + "LeadServerKeys/leadServer_public.der"));
        
            byte[] digitalSignature = CryptographyImpl.generateDigitalSignature(request.toByteArray(), privKey);
            // byte[] digitalSignature = CryptographyImpl.generateDigitalSignature(request.toByteArray(), CryptographyImpl.readPrivateKey(keyPath + "ClientKeys/client_private.der"));
            
            Timestamp timestamp = Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build();

            byte[] encryptedTimestamp = CryptographyImpl.encryptRSA(timestamp.toByteArray(), targetPublicKey);
			// byte[] encryptedTimestamp = CryptographyImpl.encryptRSA(timestamp.toByteArray(), CryptographyImpl.readPublicKey(keyPath + "LeadServerKeys/leadServer_public.der"));

            // byte[] encryptedKey = CryptographyImpl.encryptRSA(noSignatureEncryptedKey, CryptographyImpl.readPrivateKey(keyPath + "ClientKeys/client_private.der"));
            
            ClientServer.EncryptedMessageRequest encryptedReq = ClientServer.EncryptedMessageRequest.newBuilder()
                                                    .setMessageRequestBytes(ByteString.copyFrom(encryptedData))
                                                    .setEncryptionKey(ByteString.copyFrom(encryptedKey))
                                                    .setDigitalSignature(ByteString.copyFrom(digitalSignature))
                                                    .setTimestamp(ByteString.copyFrom(encryptedTimestamp))
                                                    .setUserName("LeadServer")
                                                    .build();
            
            return encryptedReq;
            
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
		File dir1 = new File(System.getProperty("user.home") + "/Documents/SIRS_Test/");
		keyPaths = System.getProperty("user.home") + "/Documents" + "/RansomwareResistantRemoteDocuments/CAServer/";
		
		System.out.println(dir1.exists());
		if(!dir1.exists()){
			System.out.println("folder created");
			dir1.mkdir();
		}
		if(!dir.exists()) {
			dir.mkdir();
		}
	}

	//Helper class for checking whether or not 
	public class MessageOccurence {
		List<ClientServer.ReadFileResponse> resList = new ArrayList<>();

		public MessageOccurence(ClientServer.ReadFileResponse res) {
			AddElement(res);
		}

		public void AddElement(ClientServer.ReadFileResponse res) {
			resList.add(res);
		}

		public ClientServer.ReadFileResponse GetElement() {
			return resList.get(0);
		}

		public int GetLength() {
			return resList.size();
		}
	}

}


