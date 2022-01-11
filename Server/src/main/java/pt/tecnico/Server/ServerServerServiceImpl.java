package pt.tecnico.Server;

import java.io.File;

import io.grpc.stub.StreamObserver;
import pt.tecnico.grpc.ServerServer;
import pt.tecnico.grpc.ServerToServerServiceGrpc;

public class ServerServerServiceImpl extends ServerToServerServiceGrpc.ServerToServerServiceImplBase {
    public static String filePath = "";
    private static ServerController serverMain;

    @Override
    public void ping(ServerServer.PingRequest request, StreamObserver<ServerServer.PingResponse> responseObserver) {
        //TODO: Working under assumption that the leader server is the only one that can receive pings atm

        System.out.println("Received ping");

        serverMain.addChildServerToList(request.getName());
        
		ServerServer.PingResponse response = ServerServer.PingResponse.getDefaultInstance();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void sendFile(ServerServer.SendFileRequest request, StreamObserver<ServerServer.SendFileResponse> responseObserver) {

    }

    @Override
    public void retrieveFile(ServerServer.RetrieveFileRequest request, StreamObserver<ServerServer.RetrieveFileResponse> responseObserver) {
        
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
