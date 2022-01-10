package pt.tecnico.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.42.1)",
    comments = "Source: server_server.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class ServerToServerServiceGrpc {

  private ServerToServerServiceGrpc() {}

  public static final String SERVICE_NAME = "pt.tecnico.grpc.ServerToServerService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<pt.tecnico.grpc.ServerServer.TestRequest,
      pt.tecnico.grpc.ServerServer.TestResponse> getTestMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "test",
      requestType = pt.tecnico.grpc.ServerServer.TestRequest.class,
      responseType = pt.tecnico.grpc.ServerServer.TestResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.tecnico.grpc.ServerServer.TestRequest,
      pt.tecnico.grpc.ServerServer.TestResponse> getTestMethod() {
    io.grpc.MethodDescriptor<pt.tecnico.grpc.ServerServer.TestRequest, pt.tecnico.grpc.ServerServer.TestResponse> getTestMethod;
    if ((getTestMethod = ServerToServerServiceGrpc.getTestMethod) == null) {
      synchronized (ServerToServerServiceGrpc.class) {
        if ((getTestMethod = ServerToServerServiceGrpc.getTestMethod) == null) {
          ServerToServerServiceGrpc.getTestMethod = getTestMethod =
              io.grpc.MethodDescriptor.<pt.tecnico.grpc.ServerServer.TestRequest, pt.tecnico.grpc.ServerServer.TestResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "test"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.tecnico.grpc.ServerServer.TestRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.tecnico.grpc.ServerServer.TestResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ServerToServerServiceMethodDescriptorSupplier("test"))
              .build();
        }
      }
    }
    return getTestMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.tecnico.grpc.ServerServer.SendFileRequest,
      pt.tecnico.grpc.ServerServer.SendFileResponse> getSendFileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "sendFile",
      requestType = pt.tecnico.grpc.ServerServer.SendFileRequest.class,
      responseType = pt.tecnico.grpc.ServerServer.SendFileResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.tecnico.grpc.ServerServer.SendFileRequest,
      pt.tecnico.grpc.ServerServer.SendFileResponse> getSendFileMethod() {
    io.grpc.MethodDescriptor<pt.tecnico.grpc.ServerServer.SendFileRequest, pt.tecnico.grpc.ServerServer.SendFileResponse> getSendFileMethod;
    if ((getSendFileMethod = ServerToServerServiceGrpc.getSendFileMethod) == null) {
      synchronized (ServerToServerServiceGrpc.class) {
        if ((getSendFileMethod = ServerToServerServiceGrpc.getSendFileMethod) == null) {
          ServerToServerServiceGrpc.getSendFileMethod = getSendFileMethod =
              io.grpc.MethodDescriptor.<pt.tecnico.grpc.ServerServer.SendFileRequest, pt.tecnico.grpc.ServerServer.SendFileResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "sendFile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.tecnico.grpc.ServerServer.SendFileRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.tecnico.grpc.ServerServer.SendFileResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ServerToServerServiceMethodDescriptorSupplier("sendFile"))
              .build();
        }
      }
    }
    return getSendFileMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.tecnico.grpc.ServerServer.RetrieveFileRequest,
      pt.tecnico.grpc.ServerServer.RetrieveFileResponse> getRetrieveFileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "retrieveFile",
      requestType = pt.tecnico.grpc.ServerServer.RetrieveFileRequest.class,
      responseType = pt.tecnico.grpc.ServerServer.RetrieveFileResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.tecnico.grpc.ServerServer.RetrieveFileRequest,
      pt.tecnico.grpc.ServerServer.RetrieveFileResponse> getRetrieveFileMethod() {
    io.grpc.MethodDescriptor<pt.tecnico.grpc.ServerServer.RetrieveFileRequest, pt.tecnico.grpc.ServerServer.RetrieveFileResponse> getRetrieveFileMethod;
    if ((getRetrieveFileMethod = ServerToServerServiceGrpc.getRetrieveFileMethod) == null) {
      synchronized (ServerToServerServiceGrpc.class) {
        if ((getRetrieveFileMethod = ServerToServerServiceGrpc.getRetrieveFileMethod) == null) {
          ServerToServerServiceGrpc.getRetrieveFileMethod = getRetrieveFileMethod =
              io.grpc.MethodDescriptor.<pt.tecnico.grpc.ServerServer.RetrieveFileRequest, pt.tecnico.grpc.ServerServer.RetrieveFileResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "retrieveFile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.tecnico.grpc.ServerServer.RetrieveFileRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.tecnico.grpc.ServerServer.RetrieveFileResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ServerToServerServiceMethodDescriptorSupplier("retrieveFile"))
              .build();
        }
      }
    }
    return getRetrieveFileMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ServerToServerServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ServerToServerServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ServerToServerServiceStub>() {
        @java.lang.Override
        public ServerToServerServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ServerToServerServiceStub(channel, callOptions);
        }
      };
    return ServerToServerServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ServerToServerServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ServerToServerServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ServerToServerServiceBlockingStub>() {
        @java.lang.Override
        public ServerToServerServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ServerToServerServiceBlockingStub(channel, callOptions);
        }
      };
    return ServerToServerServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ServerToServerServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ServerToServerServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ServerToServerServiceFutureStub>() {
        @java.lang.Override
        public ServerToServerServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ServerToServerServiceFutureStub(channel, callOptions);
        }
      };
    return ServerToServerServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class ServerToServerServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Define a RPC operation
     * </pre>
     */
    public void test(pt.tecnico.grpc.ServerServer.TestRequest request,
        io.grpc.stub.StreamObserver<pt.tecnico.grpc.ServerServer.TestResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getTestMethod(), responseObserver);
    }

    /**
     */
    public void sendFile(pt.tecnico.grpc.ServerServer.SendFileRequest request,
        io.grpc.stub.StreamObserver<pt.tecnico.grpc.ServerServer.SendFileResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSendFileMethod(), responseObserver);
    }

    /**
     */
    public void retrieveFile(pt.tecnico.grpc.ServerServer.RetrieveFileRequest request,
        io.grpc.stub.StreamObserver<pt.tecnico.grpc.ServerServer.RetrieveFileResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRetrieveFileMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getTestMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.tecnico.grpc.ServerServer.TestRequest,
                pt.tecnico.grpc.ServerServer.TestResponse>(
                  this, METHODID_TEST)))
          .addMethod(
            getSendFileMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.tecnico.grpc.ServerServer.SendFileRequest,
                pt.tecnico.grpc.ServerServer.SendFileResponse>(
                  this, METHODID_SEND_FILE)))
          .addMethod(
            getRetrieveFileMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.tecnico.grpc.ServerServer.RetrieveFileRequest,
                pt.tecnico.grpc.ServerServer.RetrieveFileResponse>(
                  this, METHODID_RETRIEVE_FILE)))
          .build();
    }
  }

  /**
   */
  public static final class ServerToServerServiceStub extends io.grpc.stub.AbstractAsyncStub<ServerToServerServiceStub> {
    private ServerToServerServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ServerToServerServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ServerToServerServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Define a RPC operation
     * </pre>
     */
    public void test(pt.tecnico.grpc.ServerServer.TestRequest request,
        io.grpc.stub.StreamObserver<pt.tecnico.grpc.ServerServer.TestResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getTestMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void sendFile(pt.tecnico.grpc.ServerServer.SendFileRequest request,
        io.grpc.stub.StreamObserver<pt.tecnico.grpc.ServerServer.SendFileResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSendFileMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void retrieveFile(pt.tecnico.grpc.ServerServer.RetrieveFileRequest request,
        io.grpc.stub.StreamObserver<pt.tecnico.grpc.ServerServer.RetrieveFileResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRetrieveFileMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class ServerToServerServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<ServerToServerServiceBlockingStub> {
    private ServerToServerServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ServerToServerServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ServerToServerServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Define a RPC operation
     * </pre>
     */
    public pt.tecnico.grpc.ServerServer.TestResponse test(pt.tecnico.grpc.ServerServer.TestRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getTestMethod(), getCallOptions(), request);
    }

    /**
     */
    public pt.tecnico.grpc.ServerServer.SendFileResponse sendFile(pt.tecnico.grpc.ServerServer.SendFileRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSendFileMethod(), getCallOptions(), request);
    }

    /**
     */
    public pt.tecnico.grpc.ServerServer.RetrieveFileResponse retrieveFile(pt.tecnico.grpc.ServerServer.RetrieveFileRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRetrieveFileMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class ServerToServerServiceFutureStub extends io.grpc.stub.AbstractFutureStub<ServerToServerServiceFutureStub> {
    private ServerToServerServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ServerToServerServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ServerToServerServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Define a RPC operation
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.tecnico.grpc.ServerServer.TestResponse> test(
        pt.tecnico.grpc.ServerServer.TestRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getTestMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.tecnico.grpc.ServerServer.SendFileResponse> sendFile(
        pt.tecnico.grpc.ServerServer.SendFileRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSendFileMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.tecnico.grpc.ServerServer.RetrieveFileResponse> retrieveFile(
        pt.tecnico.grpc.ServerServer.RetrieveFileRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRetrieveFileMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_TEST = 0;
  private static final int METHODID_SEND_FILE = 1;
  private static final int METHODID_RETRIEVE_FILE = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ServerToServerServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ServerToServerServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_TEST:
          serviceImpl.test((pt.tecnico.grpc.ServerServer.TestRequest) request,
              (io.grpc.stub.StreamObserver<pt.tecnico.grpc.ServerServer.TestResponse>) responseObserver);
          break;
        case METHODID_SEND_FILE:
          serviceImpl.sendFile((pt.tecnico.grpc.ServerServer.SendFileRequest) request,
              (io.grpc.stub.StreamObserver<pt.tecnico.grpc.ServerServer.SendFileResponse>) responseObserver);
          break;
        case METHODID_RETRIEVE_FILE:
          serviceImpl.retrieveFile((pt.tecnico.grpc.ServerServer.RetrieveFileRequest) request,
              (io.grpc.stub.StreamObserver<pt.tecnico.grpc.ServerServer.RetrieveFileResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class ServerToServerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ServerToServerServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return pt.tecnico.grpc.ServerServer.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ServerToServerService");
    }
  }

  private static final class ServerToServerServiceFileDescriptorSupplier
      extends ServerToServerServiceBaseDescriptorSupplier {
    ServerToServerServiceFileDescriptorSupplier() {}
  }

  private static final class ServerToServerServiceMethodDescriptorSupplier
      extends ServerToServerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ServerToServerServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ServerToServerServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ServerToServerServiceFileDescriptorSupplier())
              .addMethod(getTestMethod())
              .addMethod(getSendFileMethod())
              .addMethod(getRetrieveFileMethod())
              .build();
        }
      }
    }
    return result;
  }
}
