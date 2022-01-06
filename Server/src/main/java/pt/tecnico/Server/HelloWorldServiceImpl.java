package pt.tecnico.Server;

/* these imported classes are generated by the hello-world-server contract */
import pt.tecnico.grpc.ClientServer;
import pt.tecnico.grpc.HelloWorldServiceGrpc;

import io.grpc.stub.StreamObserver;

public class HelloWorldServiceImpl extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {

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

}
