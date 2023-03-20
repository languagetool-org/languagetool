package org.languagetool.rules.ml;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * for e.g. resorting suggestions
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.50.2)",
    comments = "Source: ml_server.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class PostProcessingServerGrpc {

  private PostProcessingServerGrpc() {}

  public static final String SERVICE_NAME = "lt_ml_server.PostProcessingServer";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.languagetool.rules.ml.MLServerProto.PostProcessingRequest,
      org.languagetool.rules.ml.MLServerProto.MatchResponse> getProcessMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Process",
      requestType = org.languagetool.rules.ml.MLServerProto.PostProcessingRequest.class,
      responseType = org.languagetool.rules.ml.MLServerProto.MatchResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.languagetool.rules.ml.MLServerProto.PostProcessingRequest,
      org.languagetool.rules.ml.MLServerProto.MatchResponse> getProcessMethod() {
    io.grpc.MethodDescriptor<org.languagetool.rules.ml.MLServerProto.PostProcessingRequest, org.languagetool.rules.ml.MLServerProto.MatchResponse> getProcessMethod;
    if ((getProcessMethod = PostProcessingServerGrpc.getProcessMethod) == null) {
      synchronized (PostProcessingServerGrpc.class) {
        if ((getProcessMethod = PostProcessingServerGrpc.getProcessMethod) == null) {
          PostProcessingServerGrpc.getProcessMethod = getProcessMethod =
              io.grpc.MethodDescriptor.<org.languagetool.rules.ml.MLServerProto.PostProcessingRequest, org.languagetool.rules.ml.MLServerProto.MatchResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Process"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.languagetool.rules.ml.MLServerProto.PostProcessingRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.languagetool.rules.ml.MLServerProto.MatchResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PostProcessingServerMethodDescriptorSupplier("Process"))
              .build();
        }
      }
    }
    return getProcessMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static PostProcessingServerStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PostProcessingServerStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PostProcessingServerStub>() {
        @java.lang.Override
        public PostProcessingServerStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PostProcessingServerStub(channel, callOptions);
        }
      };
    return PostProcessingServerStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static PostProcessingServerBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PostProcessingServerBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PostProcessingServerBlockingStub>() {
        @java.lang.Override
        public PostProcessingServerBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PostProcessingServerBlockingStub(channel, callOptions);
        }
      };
    return PostProcessingServerBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static PostProcessingServerFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PostProcessingServerFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PostProcessingServerFutureStub>() {
        @java.lang.Override
        public PostProcessingServerFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PostProcessingServerFutureStub(channel, callOptions);
        }
      };
    return PostProcessingServerFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * for e.g. resorting suggestions
   * </pre>
   */
  public static abstract class PostProcessingServerImplBase implements io.grpc.BindableService {

    /**
     */
    public void process(org.languagetool.rules.ml.MLServerProto.PostProcessingRequest request,
        io.grpc.stub.StreamObserver<org.languagetool.rules.ml.MLServerProto.MatchResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getProcessMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getProcessMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                org.languagetool.rules.ml.MLServerProto.PostProcessingRequest,
                org.languagetool.rules.ml.MLServerProto.MatchResponse>(
                  this, METHODID_PROCESS)))
          .build();
    }
  }

  /**
   * <pre>
   * for e.g. resorting suggestions
   * </pre>
   */
  public static final class PostProcessingServerStub extends io.grpc.stub.AbstractAsyncStub<PostProcessingServerStub> {
    private PostProcessingServerStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PostProcessingServerStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PostProcessingServerStub(channel, callOptions);
    }

    /**
     */
    public void process(org.languagetool.rules.ml.MLServerProto.PostProcessingRequest request,
        io.grpc.stub.StreamObserver<org.languagetool.rules.ml.MLServerProto.MatchResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getProcessMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * for e.g. resorting suggestions
   * </pre>
   */
  public static final class PostProcessingServerBlockingStub extends io.grpc.stub.AbstractBlockingStub<PostProcessingServerBlockingStub> {
    private PostProcessingServerBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PostProcessingServerBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PostProcessingServerBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.languagetool.rules.ml.MLServerProto.MatchResponse process(org.languagetool.rules.ml.MLServerProto.PostProcessingRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getProcessMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * for e.g. resorting suggestions
   * </pre>
   */
  public static final class PostProcessingServerFutureStub extends io.grpc.stub.AbstractFutureStub<PostProcessingServerFutureStub> {
    private PostProcessingServerFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PostProcessingServerFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PostProcessingServerFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.languagetool.rules.ml.MLServerProto.MatchResponse> process(
        org.languagetool.rules.ml.MLServerProto.PostProcessingRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getProcessMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PROCESS = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final PostProcessingServerImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(PostProcessingServerImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PROCESS:
          serviceImpl.process((org.languagetool.rules.ml.MLServerProto.PostProcessingRequest) request,
              (io.grpc.stub.StreamObserver<org.languagetool.rules.ml.MLServerProto.MatchResponse>) responseObserver);
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

  private static abstract class PostProcessingServerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    PostProcessingServerBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.languagetool.rules.ml.MLServerProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("PostProcessingServer");
    }
  }

  private static final class PostProcessingServerFileDescriptorSupplier
      extends PostProcessingServerBaseDescriptorSupplier {
    PostProcessingServerFileDescriptorSupplier() {}
  }

  private static final class PostProcessingServerMethodDescriptorSupplier
      extends PostProcessingServerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    PostProcessingServerMethodDescriptorSupplier(String methodName) {
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
      synchronized (PostProcessingServerGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new PostProcessingServerFileDescriptorSupplier())
              .addMethod(getProcessMethod())
              .build();
        }
      }
    }
    return result;
  }
}
