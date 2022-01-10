package pt.tecnico.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Defining a Service, a Service can have multiple RPC operations
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.42.1)",
    comments = "Source: client_server.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class ClientToServerServiceGrpc {

  private ClientToServerServiceGrpc() {}

  public static final String SERVICE_NAME = "pt.tecnico.grpc.ClientToServerService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<pt.tecnico.grpc.ClientServer.HelloRequest,
      pt.tecnico.grpc.ClientServer.HelloResponse> getGreetingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "greeting",
      requestType = pt.tecnico.grpc.ClientServer.HelloRequest.class,
      responseType = pt.tecnico.grpc.ClientServer.HelloResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.tecnico.grpc.ClientServer.HelloRequest,
      pt.tecnico.grpc.ClientServer.HelloResponse> getGreetingMethod() {
    io.grpc.MethodDescriptor<pt.tecnico.grpc.ClientServer.HelloRequest, pt.tecnico.grpc.ClientServer.HelloResponse> getGreetingMethod;
    if ((getGreetingMethod = ClientToServerServiceGrpc.getGreetingMethod) == null) {
      synchronized (ClientToServerServiceGrpc.class) {
        if ((getGreetingMethod = ClientToServerServiceGrpc.getGreetingMethod) == null) {
          ClientToServerServiceGrpc.getGreetingMethod = getGreetingMethod =
              io.grpc.MethodDescriptor.<pt.tecnico.grpc.ClientServer.HelloRequest, pt.tecnico.grpc.ClientServer.HelloResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "greeting"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.tecnico.grpc.ClientServer.HelloRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.tecnico.grpc.ClientServer.HelloResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ClientToServerServiceMethodDescriptorSupplier("greeting"))
              .build();
        }
      }
    }
    return getGreetingMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.tecnico.grpc.ClientServer.WriteFileRequest,
      pt.tecnico.grpc.ClientServer.WriteFileResponse> getWriteFileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "writeFile",
      requestType = pt.tecnico.grpc.ClientServer.WriteFileRequest.class,
      responseType = pt.tecnico.grpc.ClientServer.WriteFileResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.tecnico.grpc.ClientServer.WriteFileRequest,
      pt.tecnico.grpc.ClientServer.WriteFileResponse> getWriteFileMethod() {
    io.grpc.MethodDescriptor<pt.tecnico.grpc.ClientServer.WriteFileRequest, pt.tecnico.grpc.ClientServer.WriteFileResponse> getWriteFileMethod;
    if ((getWriteFileMethod = ClientToServerServiceGrpc.getWriteFileMethod) == null) {
      synchronized (ClientToServerServiceGrpc.class) {
        if ((getWriteFileMethod = ClientToServerServiceGrpc.getWriteFileMethod) == null) {
          ClientToServerServiceGrpc.getWriteFileMethod = getWriteFileMethod =
              io.grpc.MethodDescriptor.<pt.tecnico.grpc.ClientServer.WriteFileRequest, pt.tecnico.grpc.ClientServer.WriteFileResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "writeFile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.tecnico.grpc.ClientServer.WriteFileRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.tecnico.grpc.ClientServer.WriteFileResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ClientToServerServiceMethodDescriptorSupplier("writeFile"))
              .build();
        }
      }
    }
    return getWriteFileMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.tecnico.grpc.ClientServer.ReadFileRequest,
      pt.tecnico.grpc.ClientServer.ReadFileResponse> getReadFileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "readFile",
      requestType = pt.tecnico.grpc.ClientServer.ReadFileRequest.class,
      responseType = pt.tecnico.grpc.ClientServer.ReadFileResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.tecnico.grpc.ClientServer.ReadFileRequest,
      pt.tecnico.grpc.ClientServer.ReadFileResponse> getReadFileMethod() {
    io.grpc.MethodDescriptor<pt.tecnico.grpc.ClientServer.ReadFileRequest, pt.tecnico.grpc.ClientServer.ReadFileResponse> getReadFileMethod;
    if ((getReadFileMethod = ClientToServerServiceGrpc.getReadFileMethod) == null) {
      synchronized (ClientToServerServiceGrpc.class) {
        if ((getReadFileMethod = ClientToServerServiceGrpc.getReadFileMethod) == null) {
          ClientToServerServiceGrpc.getReadFileMethod = getReadFileMethod =
              io.grpc.MethodDescriptor.<pt.tecnico.grpc.ClientServer.ReadFileRequest, pt.tecnico.grpc.ClientServer.ReadFileResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "readFile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.tecnico.grpc.ClientServer.ReadFileRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.tecnico.grpc.ClientServer.ReadFileResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ClientToServerServiceMethodDescriptorSupplier("readFile"))
              .build();
        }
      }
    }
    return getReadFileMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.tecnico.grpc.ClientServer.DeleteFileRequest,
      pt.tecnico.grpc.ClientServer.DeleteFileResponse> getDeleteFileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "deleteFile",
      requestType = pt.tecnico.grpc.ClientServer.DeleteFileRequest.class,
      responseType = pt.tecnico.grpc.ClientServer.DeleteFileResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.tecnico.grpc.ClientServer.DeleteFileRequest,
      pt.tecnico.grpc.ClientServer.DeleteFileResponse> getDeleteFileMethod() {
    io.grpc.MethodDescriptor<pt.tecnico.grpc.ClientServer.DeleteFileRequest, pt.tecnico.grpc.ClientServer.DeleteFileResponse> getDeleteFileMethod;
    if ((getDeleteFileMethod = ClientToServerServiceGrpc.getDeleteFileMethod) == null) {
      synchronized (ClientToServerServiceGrpc.class) {
        if ((getDeleteFileMethod = ClientToServerServiceGrpc.getDeleteFileMethod) == null) {
          ClientToServerServiceGrpc.getDeleteFileMethod = getDeleteFileMethod =
              io.grpc.MethodDescriptor.<pt.tecnico.grpc.ClientServer.DeleteFileRequest, pt.tecnico.grpc.ClientServer.DeleteFileResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "deleteFile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.tecnico.grpc.ClientServer.DeleteFileRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.tecnico.grpc.ClientServer.DeleteFileResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ClientToServerServiceMethodDescriptorSupplier("deleteFile"))
              .build();
        }
      }
    }
    return getDeleteFileMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.tecnico.grpc.ClientServer.ListFileRequest,
      pt.tecnico.grpc.ClientServer.ListFileResponse> getListFilesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "listFiles",
      requestType = pt.tecnico.grpc.ClientServer.ListFileRequest.class,
      responseType = pt.tecnico.grpc.ClientServer.ListFileResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.tecnico.grpc.ClientServer.ListFileRequest,
      pt.tecnico.grpc.ClientServer.ListFileResponse> getListFilesMethod() {
    io.grpc.MethodDescriptor<pt.tecnico.grpc.ClientServer.ListFileRequest, pt.tecnico.grpc.ClientServer.ListFileResponse> getListFilesMethod;
    if ((getListFilesMethod = ClientToServerServiceGrpc.getListFilesMethod) == null) {
      synchronized (ClientToServerServiceGrpc.class) {
        if ((getListFilesMethod = ClientToServerServiceGrpc.getListFilesMethod) == null) {
          ClientToServerServiceGrpc.getListFilesMethod = getListFilesMethod =
              io.grpc.MethodDescriptor.<pt.tecnico.grpc.ClientServer.ListFileRequest, pt.tecnico.grpc.ClientServer.ListFileResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "listFiles"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.tecnico.grpc.ClientServer.ListFileRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.tecnico.grpc.ClientServer.ListFileResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ClientToServerServiceMethodDescriptorSupplier("listFiles"))
              .build();
        }
      }
    }
    return getListFilesMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ClientToServerServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ClientToServerServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ClientToServerServiceStub>() {
        @java.lang.Override
        public ClientToServerServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ClientToServerServiceStub(channel, callOptions);
        }
      };
    return ClientToServerServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ClientToServerServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ClientToServerServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ClientToServerServiceBlockingStub>() {
        @java.lang.Override
        public ClientToServerServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ClientToServerServiceBlockingStub(channel, callOptions);
        }
      };
    return ClientToServerServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ClientToServerServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ClientToServerServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ClientToServerServiceFutureStub>() {
        @java.lang.Override
        public ClientToServerServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ClientToServerServiceFutureStub(channel, callOptions);
        }
      };
    return ClientToServerServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Defining a Service, a Service can have multiple RPC operations
   * </pre>
   */
  public static abstract class ClientToServerServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Define a RPC operation
     * </pre>
     */
    public void greeting(pt.tecnico.grpc.ClientServer.HelloRequest request,
        io.grpc.stub.StreamObserver<pt.tecnico.grpc.ClientServer.HelloResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGreetingMethod(), responseObserver);
    }

    /**
     */
    public void writeFile(pt.tecnico.grpc.ClientServer.WriteFileRequest request,
        io.grpc.stub.StreamObserver<pt.tecnico.grpc.ClientServer.WriteFileResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getWriteFileMethod(), responseObserver);
    }

    /**
     */
    public void readFile(pt.tecnico.grpc.ClientServer.ReadFileRequest request,
        io.grpc.stub.StreamObserver<pt.tecnico.grpc.ClientServer.ReadFileResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getReadFileMethod(), responseObserver);
    }

    /**
     */
    public void deleteFile(pt.tecnico.grpc.ClientServer.DeleteFileRequest request,
        io.grpc.stub.StreamObserver<pt.tecnico.grpc.ClientServer.DeleteFileResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteFileMethod(), responseObserver);
    }

    /**
     */
    public void listFiles(pt.tecnico.grpc.ClientServer.ListFileRequest request,
        io.grpc.stub.StreamObserver<pt.tecnico.grpc.ClientServer.ListFileResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListFilesMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGreetingMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.tecnico.grpc.ClientServer.HelloRequest,
                pt.tecnico.grpc.ClientServer.HelloResponse>(
                  this, METHODID_GREETING)))
          .addMethod(
            getWriteFileMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.tecnico.grpc.ClientServer.WriteFileRequest,
                pt.tecnico.grpc.ClientServer.WriteFileResponse>(
                  this, METHODID_WRITE_FILE)))
          .addMethod(
            getReadFileMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.tecnico.grpc.ClientServer.ReadFileRequest,
                pt.tecnico.grpc.ClientServer.ReadFileResponse>(
                  this, METHODID_READ_FILE)))
          .addMethod(
            getDeleteFileMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.tecnico.grpc.ClientServer.DeleteFileRequest,
                pt.tecnico.grpc.ClientServer.DeleteFileResponse>(
                  this, METHODID_DELETE_FILE)))
          .addMethod(
            getListFilesMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.tecnico.grpc.ClientServer.ListFileRequest,
                pt.tecnico.grpc.ClientServer.ListFileResponse>(
                  this, METHODID_LIST_FILES)))
          .build();
    }
  }

  /**
   * <pre>
   * Defining a Service, a Service can have multiple RPC operations
   * </pre>
   */
  public static final class ClientToServerServiceStub extends io.grpc.stub.AbstractAsyncStub<ClientToServerServiceStub> {
    private ClientToServerServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClientToServerServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ClientToServerServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Define a RPC operation
     * </pre>
     */
    public void greeting(pt.tecnico.grpc.ClientServer.HelloRequest request,
        io.grpc.stub.StreamObserver<pt.tecnico.grpc.ClientServer.HelloResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGreetingMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void writeFile(pt.tecnico.grpc.ClientServer.WriteFileRequest request,
        io.grpc.stub.StreamObserver<pt.tecnico.grpc.ClientServer.WriteFileResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getWriteFileMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void readFile(pt.tecnico.grpc.ClientServer.ReadFileRequest request,
        io.grpc.stub.StreamObserver<pt.tecnico.grpc.ClientServer.ReadFileResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getReadFileMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteFile(pt.tecnico.grpc.ClientServer.DeleteFileRequest request,
        io.grpc.stub.StreamObserver<pt.tecnico.grpc.ClientServer.DeleteFileResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteFileMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listFiles(pt.tecnico.grpc.ClientServer.ListFileRequest request,
        io.grpc.stub.StreamObserver<pt.tecnico.grpc.ClientServer.ListFileResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListFilesMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * Defining a Service, a Service can have multiple RPC operations
   * </pre>
   */
  public static final class ClientToServerServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<ClientToServerServiceBlockingStub> {
    private ClientToServerServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClientToServerServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ClientToServerServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Define a RPC operation
     * </pre>
     */
    public pt.tecnico.grpc.ClientServer.HelloResponse greeting(pt.tecnico.grpc.ClientServer.HelloRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGreetingMethod(), getCallOptions(), request);
    }

    /**
     */
    public pt.tecnico.grpc.ClientServer.WriteFileResponse writeFile(pt.tecnico.grpc.ClientServer.WriteFileRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getWriteFileMethod(), getCallOptions(), request);
    }

    /**
     */
    public pt.tecnico.grpc.ClientServer.ReadFileResponse readFile(pt.tecnico.grpc.ClientServer.ReadFileRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getReadFileMethod(), getCallOptions(), request);
    }

    /**
     */
    public pt.tecnico.grpc.ClientServer.DeleteFileResponse deleteFile(pt.tecnico.grpc.ClientServer.DeleteFileRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteFileMethod(), getCallOptions(), request);
    }

    /**
     */
    public pt.tecnico.grpc.ClientServer.ListFileResponse listFiles(pt.tecnico.grpc.ClientServer.ListFileRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListFilesMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * Defining a Service, a Service can have multiple RPC operations
   * </pre>
   */
  public static final class ClientToServerServiceFutureStub extends io.grpc.stub.AbstractFutureStub<ClientToServerServiceFutureStub> {
    private ClientToServerServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClientToServerServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ClientToServerServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Define a RPC operation
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.tecnico.grpc.ClientServer.HelloResponse> greeting(
        pt.tecnico.grpc.ClientServer.HelloRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGreetingMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.tecnico.grpc.ClientServer.WriteFileResponse> writeFile(
        pt.tecnico.grpc.ClientServer.WriteFileRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getWriteFileMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.tecnico.grpc.ClientServer.ReadFileResponse> readFile(
        pt.tecnico.grpc.ClientServer.ReadFileRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getReadFileMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.tecnico.grpc.ClientServer.DeleteFileResponse> deleteFile(
        pt.tecnico.grpc.ClientServer.DeleteFileRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteFileMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.tecnico.grpc.ClientServer.ListFileResponse> listFiles(
        pt.tecnico.grpc.ClientServer.ListFileRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListFilesMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GREETING = 0;
  private static final int METHODID_WRITE_FILE = 1;
  private static final int METHODID_READ_FILE = 2;
  private static final int METHODID_DELETE_FILE = 3;
  private static final int METHODID_LIST_FILES = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ClientToServerServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ClientToServerServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GREETING:
          serviceImpl.greeting((pt.tecnico.grpc.ClientServer.HelloRequest) request,
              (io.grpc.stub.StreamObserver<pt.tecnico.grpc.ClientServer.HelloResponse>) responseObserver);
          break;
        case METHODID_WRITE_FILE:
          serviceImpl.writeFile((pt.tecnico.grpc.ClientServer.WriteFileRequest) request,
              (io.grpc.stub.StreamObserver<pt.tecnico.grpc.ClientServer.WriteFileResponse>) responseObserver);
          break;
        case METHODID_READ_FILE:
          serviceImpl.readFile((pt.tecnico.grpc.ClientServer.ReadFileRequest) request,
              (io.grpc.stub.StreamObserver<pt.tecnico.grpc.ClientServer.ReadFileResponse>) responseObserver);
          break;
        case METHODID_DELETE_FILE:
          serviceImpl.deleteFile((pt.tecnico.grpc.ClientServer.DeleteFileRequest) request,
              (io.grpc.stub.StreamObserver<pt.tecnico.grpc.ClientServer.DeleteFileResponse>) responseObserver);
          break;
        case METHODID_LIST_FILES:
          serviceImpl.listFiles((pt.tecnico.grpc.ClientServer.ListFileRequest) request,
              (io.grpc.stub.StreamObserver<pt.tecnico.grpc.ClientServer.ListFileResponse>) responseObserver);
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

  private static abstract class ClientToServerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ClientToServerServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return pt.tecnico.grpc.ClientServer.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ClientToServerService");
    }
  }

  private static final class ClientToServerServiceFileDescriptorSupplier
      extends ClientToServerServiceBaseDescriptorSupplier {
    ClientToServerServiceFileDescriptorSupplier() {}
  }

  private static final class ClientToServerServiceMethodDescriptorSupplier
      extends ClientToServerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ClientToServerServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (ClientToServerServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ClientToServerServiceFileDescriptorSupplier())
              .addMethod(getGreetingMethod())
              .addMethod(getWriteFileMethod())
              .addMethod(getReadFileMethod())
              .addMethod(getDeleteFileMethod())
              .addMethod(getListFilesMethod())
              .build();
        }
      }
    }
    return result;
  }
}
