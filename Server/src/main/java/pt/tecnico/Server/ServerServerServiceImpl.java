package pt.tecnico.Server;

import java.io.File;

import io.grpc.stub.StreamObserver;
import pt.tecnico.grpc.ServerServer;
import pt.tecnico.grpc.ServerToServerServiceGrpc;

public class ServerServerServiceImpl extends ServerToServerServiceGrpc.ServerToServerServiceImplBase {
    public static String filePath = "";

    @Override
    public void sendFile(ServerServer.SendFileRequest request, StreamObserver<ServerServer.SendFileResponse> response) {

    }

    @Override
    public void retrieveFile(ServerServer.RetrieveFileRequest request, StreamObserver<ServerServer.RetrieveFileResponse> response) {
        
    }

    //-------

	public void SetupStoragePath() {
		filePath = System.getProperty("user.home") + "/Documents/SIRS_Test/"; //"RansomwareResistantRemoteDocuments/Server/Files/" + args[2];
		File dir = new File(filePath);
		if(!dir.exists()) {
			dir.mkdir();
		}
	}
}
