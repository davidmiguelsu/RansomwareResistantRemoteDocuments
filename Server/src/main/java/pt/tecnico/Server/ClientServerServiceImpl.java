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
import java.util.Base64;
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
	public static String keyPaths = "";
    private ServerController serverController;

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
		int id = serverController.db.getUserIDbyUsername(serverController.conn, decryptRequest.getUserName());

		if(id > 0) {
			res = ClientServer.RegisterResponse.newBuilder()
				.setAck("ERROR").build();
		}
		else {
			res = ClientServer.RegisterResponse.newBuilder()
			.setAck("OK").build();

			serverController.db.addUserDatabase(serverController.conn , decryptRequest.getUserName(), decryptRequest.getCipheredPassword());
		}

		responseObserver.onNext(EncryptResponse(res, targetPubKey));
		responseObserver.onCompleted();
	}

	@Override
	public void login(ClientServer.EncryptedMessageRequest request, StreamObserver<ClientServer.EncryptedMessageResponse> responseObserver) {
		PublicKey targetPubKey = serverController.caServer.requestPublicKeyOf(request.getUserName(), true, false);


		byte[] requestDecryptedBytes = DecryptRequest(request, targetPubKey);

		ClientServer.LoginRequest decryptRequest = null;
		try {
			decryptRequest = ClientServer.LoginRequest.parseFrom(requestDecryptedBytes);
		} catch (InvalidProtocolBufferException ipbe) {
			System.out.println("Failed to decrypt request: " + ipbe.getMessage());
			responseObserver.onNext(EncryptResponse(ClientServer.LoginResponse.getDefaultInstance(), targetPubKey));
			responseObserver.onCompleted();
			return;		
		} 
				

		ClientServer.LoginResponse res;

		//query to retrieve password for authentication purposes
		String tempPW = serverController.db.getUserPWbyUsername(serverController.conn, decryptRequest.getUserName());
		byte[] decodedPassword = Base64.getDecoder().decode(tempPW);

		String passwordSalt = serverController.db.getUserSaltbyUsername(serverController.conn, decryptRequest.getUserName());
		byte[] decodedSalt = Base64.getDecoder().decode(passwordSalt);

		ByteBuffer newSaltedPassword = ByteBuffer.wrap(CryptographyImpl.GenerateSaltedSHA3Digest(decryptRequest.getCipheredPassword().getBytes(), decodedSalt));

		if (newSaltedPassword.compareTo(ByteBuffer.wrap(decodedPassword)) == 0) {
			res = ClientServer.LoginResponse.newBuilder()
			.setAck("OK").build();				
		}
		else {
			res = ClientServer.LoginResponse.newBuilder()
			.setAck("ERROR").build();
		}

		responseObserver.onNext(EncryptResponse(res, targetPubKey));
		responseObserver.onCompleted();
	}

	@Override
	public void writeFile(ClientServer.EncryptedMessageRequest request, StreamObserver<ClientServer.EncryptedMessageResponse> responseObserver) {
		PublicKey targetPubKey = serverController.caServer.requestPublicKeyOf(request.getUserName(), true, false);

		byte[] requestDecryptedBytes = DecryptRequest(request, targetPubKey);

		ClientServer.WriteFileRequest decryptRequest = null;
		try {
			decryptRequest = ClientServer.WriteFileRequest.parseFrom(requestDecryptedBytes);
		} catch (InvalidProtocolBufferException ipbe) {
			System.out.println("Failed to decrypt request: " + ipbe.getMessage());
			responseObserver.onNext(EncryptResponse(ClientServer.WriteFileResponse.getDefaultInstance(), targetPubKey));
			responseObserver.onCompleted();
			return;		
		} 

		if(serverController.isLeader) {
			int tempNameID = serverController.db.getUserIDbyUsername(serverController.conn , decryptRequest.getUsername());
			
			int tempFileID = serverController.db.getFileIDbyFileName(serverController.conn , decryptRequest.getFileName());
			
			if ((serverController.db.doesFileExist(serverController.conn, decryptRequest.getFileName())) && 
				!serverController.db.doesUserHaveWritePerms(serverController.conn, tempNameID, tempFileID) ){
				System.out.println("File with that name already exists, consider changing the name!!");	
	
				ClientServer.WriteFileResponse response = ClientServer.WriteFileResponse.newBuilder()
					.setAck("ERROR - Failed write of file " + decryptRequest.getFileName())
					.build();
				responseObserver.onNext(EncryptResponse(response, targetPubKey));
		
				responseObserver.onCompleted();
				return;
			}

				List<ClientServer.WriteFileResponse> responseList = new ArrayList<>();

				int i = 2;

				for (ChildServerInfo info : serverController.childServerList) {
					PublicKey targetServerPublicKey = serverController.caServer.requestPublicKeyOf(String.valueOf(i), true, false);

					PublicKey targetPublicKey = serverController.caServer.requestPublicKeyOf(String.valueOf(i), true, false);

					ClientServer.EncryptedMessageResponse childRes = info.stub.writeFile(EncryptRequest(decryptRequest, targetServerPublicKey));
					i++;
					try {
						ClientServer.WriteFileResponse decryptedChildRes = ClientServer.WriteFileResponse.parseFrom(DecryptResponse(childRes, targetServerPublicKey));
						responseList.add(decryptedChildRes);
					} catch (InvalidProtocolBufferException e) {
						System.out.println("ERROR - Failed to decrypt response from server" + i + " -- " + e.getMessage());

					} 
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
				else {			
					System.out.println("Sucessfull write of file " + decryptRequest.getFileName());
			
					
					ClientServer.WriteFileResponse response = ClientServer.WriteFileResponse.newBuilder()
						.setAck("OK - Confirmed write of file " + decryptRequest.getFileName())
						.build();
					responseObserver.onNext(EncryptResponse(response, targetPubKey));
					responseObserver.onCompleted();

					if(!serverController.db.doesFileExist(serverController.conn, decryptRequest.getFileName())) {
						serverController.db.addFileDatabase(serverController.conn , decryptRequest.getFileName(), decryptRequest.getHash().toByteArray());
						tempFileID = serverController.db.getFileIDbyFileName(serverController.conn , decryptRequest.getFileName());
						serverController.db.addUserFileDatabase(serverController.conn, tempNameID, tempFileID);
					}
					else {
						tempFileID = serverController.db.getFileIDbyFileName(serverController.conn , decryptRequest.getFileName());
						serverController.db.updateFileDatabase(serverController.conn, tempFileID, decryptRequest.getHash().toByteArray());
					}

					return;
				}

		}

		File file = new File(filePath + decryptRequest.getFileName());
		try {
			FileOutputStream writer = new FileOutputStream(file, false);
			writer.write(decryptRequest.getFile().toByteArray());
			writer.close();
			
			System.out.println("Sucessfull write of file " + decryptRequest.getFileName());
			
			ClientServer.WriteFileResponse response = ClientServer.WriteFileResponse.newBuilder()
			.setAck("OK - Confirmed write of file " + decryptRequest.getFileName())
			.build();

			// Use responseObserver to send a single response back
			responseObserver.onNext(EncryptResponse(response, targetPubKey));
	
			// When you are done, you must call onCompleted
			responseObserver.onCompleted();
		} catch (Exception e) {
			System.out.println("Failed to write file " + decryptRequest.getFileName() + ". " + e.getMessage());
			ClientServer.WriteFileResponse response = ClientServer.WriteFileResponse.newBuilder()
				.setAck("ERROR - Failed to write file " + decryptRequest.getFileName())
				.build();
	
			responseObserver.onNext(EncryptResponse(response, targetPubKey));
			responseObserver.onCompleted();
		}
	}

	@Override
	public void readFile(ClientServer.EncryptedMessageRequest request, StreamObserver<ClientServer.EncryptedMessageResponse> responseObserver) {
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
					
			//TODO: esta verifica??ao should be enough -> if true = tem read perms otherwise n??o tem e ?? suposto printar que u user n tem perms para ler.
			if(!serverController.db.doesUserHaveReadPerms(serverController.conn, userID, fileID)){
				ClientServer.ReadFileResponse response =  ClientServer.ReadFileResponse.getDefaultInstance();
				
				responseObserver.onNext(EncryptResponse(response, targetPubKey));
	
				responseObserver.onCompleted();
	
				return;
			}

			List<ClientServer.ReadFileResponse> responseList = new ArrayList<>();
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

			ClientServer.EncryptedMessageResponse encryptedRes = EncryptResponse(ClientServer.ReadFileResponse.getDefaultInstance(), targetPubKey);
			for (var entry : messageDigests.entrySet()) {
				if(entry.getValue().GetLength() >= (serverController.childServerList.size() / 2) + 1) {
					encryptedRes = EncryptResponse(entry.getValue().GetElement(), targetPubKey);


					byte[] hashString = serverController.db.getFileHashbyFilename(serverController.conn, fileName);

					ClientServer.ReadFileResponse resWithHash = ClientServer.ReadFileResponse.newBuilder()
																	.setFile(entry.getValue().GetElement().getFile())
																	.setHash(ByteString.copyFrom(hashString))
																	.build();

					encryptedRes = EncryptResponse(resWithHash, targetPubKey);
					break; 
				}
			}


			responseObserver.onNext(encryptedRes);

			responseObserver.onCompleted();
			return;
		}

		File file = new File(filePath + fileName);
		try {
			FileInputStream fis = new FileInputStream(file);

			ClientServer.ReadFileResponse response = ClientServer.ReadFileResponse.newBuilder()
														.setFile(ByteString.copyFrom(fis.readAllBytes()))
														.setHash(ByteString.copyFrom("".getBytes()))
														.build();

			ClientServer.EncryptedMessageResponse encryptedRes = EncryptResponse(response, targetPubKey);	
			fis.close();
			
			responseObserver.onNext(encryptedRes);

			responseObserver.onCompleted();
		} catch (Exception e) {
			System.out.println("ERROR - Failed to send requested file: " + e.getMessage());
			responseObserver.onNext(ClientServer.EncryptedMessageResponse.getDefaultInstance());

			responseObserver.onCompleted();
		}
	}

	@Override
	public void listFiles(ClientServer.EncryptedMessageRequest request, StreamObserver<ClientServer.EncryptedMessageResponse> responseObserver) {
		
		PublicKey targetPubKey = serverController.caServer.requestPublicKeyOf(request.getUserName(), true, false);

		byte[] requestDecryptedBytes = DecryptRequest(request, targetPubKey);

			try {
				ClientServer.ListFileRequest decryptRequest = ClientServer.ListFileRequest.parseFrom(requestDecryptedBytes);
				
				int tempID = serverController.db.getUserIDbyUsername(serverController.conn, decryptRequest.getUserName());
				
				List<String> listaFiles = (serverController.db.getListFile(serverController.conn, tempID));
				
				ClientServer.ListFileResponse response = ClientServer.ListFileResponse.newBuilder()
				.addAllFileName(listaFiles)
				.build();

				responseObserver.onNext(EncryptResponse(response, targetPubKey));
				responseObserver.onCompleted();
			} catch (Exception e) {
				System.out.println("Failed to list files: " + e.getMessage());
				responseObserver.onNext(EncryptResponse(ClientServer.ListFileResponse.getDefaultInstance(), targetPubKey));
				responseObserver.onCompleted();
				return;
			}
	}

	@Override
	public void deleteFile(ClientServer.EncryptedMessageRequest request, StreamObserver<ClientServer.EncryptedMessageResponse> responseObserver) {

		PublicKey targetPubKey = serverController.caServer.requestPublicKeyOf(request.getUserName(), true, false);

		byte[] requestDecryptedBytes = DecryptRequest(request, targetPubKey);
		ClientServer.DeleteFileRequest decryptRequest = null;
		try {
			decryptRequest = ClientServer.DeleteFileRequest.parseFrom(requestDecryptedBytes);	
		} catch (InvalidProtocolBufferException ipbe) {
			System.out.println("Failed to decrypt request: " + ipbe.getMessage());
			responseObserver.onNext(EncryptResponse(ClientServer.DeleteFileResponse.getDefaultInstance(), targetPubKey));
			responseObserver.onCompleted();
			return;
		}

		if(serverController.isLeader) {
			int tempUserID = serverController.db.getUserIDbyUsername(serverController.conn, decryptRequest.getUsername());
			int tempFileID = serverController.db.getFileIDbyFileName(serverController.conn , decryptRequest.getFileName());	

			if (!serverController.db.checkIsUserOwner(serverController.conn, tempUserID, tempFileID)){
				ClientServer.WriteFileResponse response = ClientServer.WriteFileResponse.newBuilder()
					.setAck("ERROR - File " + decryptRequest.getFileName() + " either doesn't exist or you don't own it")
					.build();

				responseObserver.onNext(EncryptResponse(response, targetPubKey));
				responseObserver.onCompleted();
				return;
			}

			List<ClientServer.DeleteFileResponse> responseList = new ArrayList<>();
			int i = 2;

			for (ChildServerInfo info : serverController.childServerList) {
				
				PublicKey targetServerPublicKey = serverController.caServer.requestPublicKeyOf(String.valueOf(i), true, false);
				
				ClientServer.EncryptedMessageResponse childRes = info.stub.deleteFile(EncryptRequest(decryptRequest, targetServerPublicKey));
				byte[] decryptedChildResBytes = DecryptResponse(childRes, targetServerPublicKey);

				ClientServer.DeleteFileResponse decryptedChildRes = null;
				try {
					decryptedChildRes = ClientServer.DeleteFileResponse.parseFrom(decryptedChildResBytes);
					responseList.add(decryptedChildRes);
				} catch (InvalidProtocolBufferException e) {
					System.out.println("ERROR - Failed to decrypt response from server" + i + " -- "+ e.getMessage());
				} 
				i++;
			}

			int resOK = 0;
			int resNOK = 0;
			for (ClientServer.DeleteFileResponse res : responseList) {
				if(res.getAck().startsWith("OK")) {
					resOK++;
				}
				else
					resNOK++;
			}

			if(resOK < serverController.childServerList.size() / 2 + 1) {
				ClientServer.WriteFileResponse response = ClientServer.WriteFileResponse.newBuilder()
					.setAck("ERROR - Failed to delete correctly from sufficient servers" + decryptRequest.getFileName())
					.build();

					responseObserver.onNext(EncryptResponse(response, targetPubKey));
					responseObserver.onCompleted();
					return;
			}
			else {			
				System.out.println("Sucessfull delete of file " + decryptRequest.getFileName());
		
				
				ClientServer.WriteFileResponse response = ClientServer.WriteFileResponse.newBuilder()
					.setAck("OK - Confirmed delete of file " + decryptRequest.getFileName())
					.build();
				responseObserver.onNext(EncryptResponse(response, targetPubKey));
				responseObserver.onCompleted();

				serverController.db.deleteFileUserDatabase(serverController.conn, tempUserID, tempFileID);
				serverController.db.deleteFileDatabase(serverController.conn, tempFileID);
					
				return;
			}
		}

		String filename = decryptRequest.getFileName();
		File toDelete = new File(filePath + filename); 

		String ackStr = "---";

		if (toDelete.exists()){
			
			if (toDelete.delete()) { 
				ackStr = "OK - Deleted the file: " + filename;
			} else {
				ackStr = "ERROR - Failed to delete the file.";
			} 
  
		} else {
			ackStr = "ERROR - File does not exist.";
		}

		

		System.out.println(ackStr);

		ClientServer.DeleteFileResponse response = ClientServer.DeleteFileResponse.newBuilder()
			.setAck(ackStr)
			.build();


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
			} catch (InvalidProtocolBufferException ipbe) {
				System.out.println("Failed to decrypt request: " + ipbe.getMessage());
				responseObserver.onNext(EncryptResponse(ClientServer.GivePermissionsResponse.getDefaultInstance(), targetPubKey));
				responseObserver.onCompleted();
				return;			}

			int tempUserID = serverController.db.getUserIDbyUsername(serverController.conn, decryptRequest.getUserName());
			int tempTargetUserID = serverController.db.getUserIDbyUsername(serverController.conn, decryptRequest.getTargetUserName());
			int tempFileID = serverController.db.getFileIDbyFileName(serverController.conn, decryptRequest.getFileName());

			if(!serverController.db.checkIsUserOwner(serverController.conn, tempUserID, tempFileID)) {
				ClientServer.GivePermissionsResponse response = ClientServer.GivePermissionsResponse.newBuilder()
					.setAck("ERROR - You don't own the selected file")
					.build();
				responseObserver.onNext(EncryptResponse(response, targetPubKey));
				responseObserver.onCompleted();
				return;
			}

			String perm = decryptRequest.getPermission();
			if(!perm.equals("all") && !perm.equals("read")) {
				ClientServer.GivePermissionsResponse response = ClientServer.GivePermissionsResponse.newBuilder()
					.setAck("ERROR - Wrong permission input")
					.build();
				responseObserver.onNext(EncryptResponse(response, targetPubKey));
				responseObserver.onCompleted();
				return;
			}

			if (perm.equals("all")) {	
				serverController.db.giveAllPermission(serverController.conn, tempTargetUserID, tempFileID);
			} 
			else{
				serverController.db.giveReadPermission(serverController.conn, tempTargetUserID, tempFileID);
			}




			File file = new File(filePath + decryptRequest.getFileName() + "_" + decryptRequest.getTargetUserName());

			try {
				FileOutputStream writer = new FileOutputStream(file, false);
				writer.write(decryptRequest.getKey().toByteArray());
				writer.close();
			} catch (FileNotFoundException fnfe) {
				System.out.println("ERROR - Failed to write file, as it wasn't found? " + fnfe.getLocalizedMessage());

				ClientServer.GivePermissionsResponse response = ClientServer.GivePermissionsResponse.newBuilder()
					.setAck("ERROR -Failed write of key for user: " + decryptRequest.getUserName())
					.build();
				responseObserver.onNext(EncryptResponse(response, targetPubKey));
				responseObserver.onCompleted();
				return;
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
			} catch (InvalidProtocolBufferException ipbe) {
				System.out.println("Failed to decrypt request: " + ipbe.getMessage());
				responseObserver.onNext(EncryptResponse(ClientServer.UpdatePermissionsResponse.getDefaultInstance(), targetPubKey));
				responseObserver.onCompleted();
				return;
			}

			//For each key the user has access to
			List<String> fileList = Arrays.asList(new File(filePath).list());

			List<File> keysToSend = new ArrayList<>();
			for (String file : fileList) {
				if(file.endsWith(request.getUserName())) {
					keysToSend.add(new File(filePath + file));
				}
			}

			ClientServer.UpdatePermissionsResponse.Builder permsBuilder = ClientServer.UpdatePermissionsResponse.newBuilder();
			try {
				for (File file : keysToSend) {
					permsBuilder.addFileName(file.getName().substring(0, file.getName().indexOf("_" + request.getUserName())));
					FileInputStream fis = new FileInputStream(file);
					permsBuilder.addKeys(ByteString.copyFrom(fis.readAllBytes()));
					fis.close();
				}
			} catch (Exception e) {
				ClientServer.UpdatePermissionsResponse response = ClientServer.UpdatePermissionsResponse.getDefaultInstance();
				responseObserver.onNext(EncryptResponse(response, targetPubKey));
				responseObserver.onCompleted();
				return;
			}

		ClientServer.UpdatePermissionsResponse permsRes = permsBuilder.build();
		responseObserver.onNext(EncryptResponse(permsRes, targetPubKey));
		responseObserver.onCompleted();
		}
	}

	@Override
	public void giveKeysToPermittedUsers(ClientServer.EncryptedMessageRequest request, StreamObserver<ClientServer.EncryptedMessageResponse> responseObserver) {
		if(serverController.isLeader) {
			PublicKey targetPubKey = serverController.caServer.requestPublicKeyOf(request.getUserName(), true, false);

			byte[] requestDecryptedBytes = DecryptRequest(request, targetPubKey);
			ClientServer.GiveKeysToPermittedUsersRequest decryptRequest = null;
			try {
				decryptRequest = ClientServer.GiveKeysToPermittedUsersRequest.parseFrom(requestDecryptedBytes);	
			} catch (InvalidProtocolBufferException ipbe) {
				System.out.println("Failed to decrypt request: " + ipbe.getMessage());
				responseObserver.onNext(EncryptResponse(ClientServer.GiveKeysToPermittedUsersResponse.getDefaultInstance(), targetPubKey));
				responseObserver.onCompleted();
				return;			
			}

			int tempUserID = serverController.db.getUserIDbyUsername(serverController.conn, decryptRequest.getUserName());
			int tempFileID = serverController.db.getFileIDbyFileName(serverController.conn, decryptRequest.getFileName());

			List<Integer> userIDList = serverController.db.getUsersWithAccessToFile(serverController.conn, tempFileID);
			if(userIDList == null) {
				ClientServer.GiveKeysToPermittedUsersResponse response = ClientServer.GiveKeysToPermittedUsersResponse.newBuilder()
					.addAck("ERROR - Failed to fetch users")
					.build();
				responseObserver.onNext(EncryptResponse(response, targetPubKey));
				responseObserver.onCompleted();
				return;
			}

			PrivateKey privKey = (PrivateKey) serverController.caServer.FetchPrivateKey();
			byte[] decryptedSecretKey = CryptographyImpl.decryptRSA(decryptRequest.getKey().toByteArray(), privKey);

			ClientServer.GiveKeysToPermittedUsersResponse.Builder responseBuilder = ClientServer.GiveKeysToPermittedUsersResponse.newBuilder();
			for (Integer userID : userIDList) {
				String targetName = serverController.db.getUsernamebyID(serverController.conn, userID);
				if(targetName == null) {
					System.out.println("ERROR - Failed to find username for ID " + userID);
	
					responseBuilder.addAck("ERROR - Failed to find username for a user");
					continue;
				}

				PublicKey targetKey = serverController.caServer.requestPublicKeyOf(targetName, true, false);

				byte[] encryptedKeyForTarget = CryptographyImpl.encryptRSA(decryptedSecretKey, targetKey);
				File file = new File(filePath + decryptRequest.getFileName() + "_" + targetName);
				try {
					FileOutputStream writer = new FileOutputStream(file, false);
					writer.write(encryptedKeyForTarget);
					writer.close();
				} catch (FileNotFoundException fnfe) {
					System.out.println("ERROR - Failed to write file, as it wasn't found? " + fnfe.getLocalizedMessage());
	
					responseBuilder.addAck("ERROR - Failed write of key for user: " + targetName);
				} catch (IOException ioe) {
					System.out.println("ERROR - Failed due to IO: " + ioe.getMessage());
					responseBuilder.addAck("ERROR - Failed write of key for user: " + targetName);
				}

			}

			responseObserver.onNext(EncryptResponse(responseBuilder.build(), targetPubKey));
			responseObserver.onCompleted();
		}
	}
	//-------

	byte[] DecryptRequest(ClientServer.EncryptedMessageRequest request, PublicKey targetPubKey) {

		PrivateKey privKey = (PrivateKey) serverController.caServer.FetchPrivateKey();
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
		Key decryptTempKey = new SecretKeySpec(decryptedTempKeyBytes, 0, 16, "AES");

		//TODO: Check IV later
		byte[] requestDecryptedBytes = CryptographyImpl.decryptAES("", request.getMessageRequestBytes().toByteArray(), decryptTempKey);


		if(!CryptographyImpl.verifyDigitalSignature(requestDecryptedBytes, request.getDigitalSignature().toByteArray(), targetPubKey)) {
            System.out.println("ERROR - Received message doesn't match with the digital signature!");
            return null;
        }

		return requestDecryptedBytes;
	}

	byte[] DecryptResponse(ClientServer.EncryptedMessageResponse response, PublicKey targetPubKey) {
		PrivateKey privKey = (PrivateKey) serverController.caServer.FetchPrivateKey();

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
			
			ClientServer.EncryptedMessageResponse encryptedRes = ClientServer.EncryptedMessageResponse.newBuilder()
													.setMessageResponseBytes(ByteString.copyFrom(encryptedData))
													.setEncryptionKey(ByteString.copyFrom(encryptedKey))
													.setDigitalSignature(ByteString.copyFrom(digitalSignature))
													.setTimestamp(ByteString.copyFrom(encryptedTimestamp))
													.build();
			
			return encryptedRes;
			
		} catch (Exception e) {
            System.out.println("Failed to encrypt response: " + e.getMessage());
            return ClientServer.EncryptedMessageResponse.getDefaultInstance();
		}
	}

	ClientServer.EncryptedMessageRequest EncryptRequest(GeneratedMessageV3 request, PublicKey targetPublicKey) {
        //TODO: Check IV input
        try {
			PrivateKey privKey = (PrivateKey) serverController.caServer.FetchPrivateKey();

            Key tempKey = CryptographyImpl.generateAESKey();
            byte[] encryptedData = CryptographyImpl.encryptAES("", request.toByteArray(), tempKey);

            byte[] encryptedKey = CryptographyImpl.encryptRSA(tempKey.getEncoded(), targetPublicKey);
        
            byte[] digitalSignature = CryptographyImpl.generateDigitalSignature(request.toByteArray(), privKey);
            
            Timestamp timestamp = Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build();

            byte[] encryptedTimestamp = CryptographyImpl.encryptRSA(timestamp.toByteArray(), targetPublicKey);
            
            ClientServer.EncryptedMessageRequest encryptedReq = ClientServer.EncryptedMessageRequest.newBuilder()
                                                    .setMessageRequestBytes(ByteString.copyFrom(encryptedData))
                                                    .setEncryptionKey(ByteString.copyFrom(encryptedKey))
                                                    .setDigitalSignature(ByteString.copyFrom(digitalSignature))
                                                    .setTimestamp(ByteString.copyFrom(encryptedTimestamp))
                                                    .setUserName("LeadServer")
                                                    .build();
            
            return encryptedReq;
            
        } catch (Exception e) {
            System.out.println("Failed to encrypt request: " + e.getMessage());
            return ClientServer.EncryptedMessageRequest.getDefaultInstance();
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


