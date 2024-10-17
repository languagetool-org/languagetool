package org.languagetool.rules.ml;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.68.0)",
    comments = "Source: ml_server.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class MLServerGrpc {

  private MLServerGrpc() {}

  public static final java.lang.String SERVICE_NAME = "lt_ml_server.MLServer";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.languagetool.rules.ml.MLServerProto.MatchRequest,
      org.languagetool.rules.ml.MLServerProto.MatchResponse> getMatchMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Match",
      requestType = org.languagetool.rules.ml.MLServerProto.MatchRequest.class,
      responseType = org.languagetool.rules.ml.MLServerProto.MatchResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.languagetool.rules.ml.MLServerProto.MatchRequest,
      org.languagetool.rules.ml.MLServerProto.MatchResponse> getMatchMethod() {
    io.grpc.MethodDescriptor<org.languagetool.rules.ml.MLServerProto.MatchRequest, org.languagetool.rules.ml.MLServerProto.MatchResponse> getMatchMethod;
    if ((getMatchMethod = MLServerGrpc.getMatchMethod) == null) {
      synchronized (MLServerGrpc.class) {
        if ((getMatchMethod = MLServerGrpc.getMatchMethod) == null) {
          MLServerGrpc.getMatchMethod = getMatchMethod =
              io.grpc.MethodDescriptor.<org.languagetool.rules.ml.MLServerProto.MatchRequest, org.languagetool.rules.ml.MLServerProto.MatchResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Match"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.languagetool.rules.ml.MLServerProto.MatchRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.languagetool.rules.ml.MLServerProto.MatchResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MLServerMethodDescriptorSupplier("Match"))
              .build();
        }
      }
    }
    return getMatchMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.languagetool.rules.ml.MLServerProto.AnalyzedMatchRequest,
      org.languagetool.rules.ml.MLServerProto.MatchResponse> getMatchAnalyzedMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "MatchAnalyzed",
      requestType = org.languagetool.rules.ml.MLServerProto.AnalyzedMatchRequest.class,
      responseType = org.languagetool.rules.ml.MLServerProto.MatchResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.languagetool.rules.ml.MLServerProto.AnalyzedMatchRequest,
      org.languagetool.rules.ml.MLServerProto.MatchResponse> getMatchAnalyzedMethod() {
    io.grpc.MethodDescriptor<org.languagetool.rules.ml.MLServerProto.AnalyzedMatchRequest, org.languagetool.rules.ml.MLServerProto.MatchResponse> getMatchAnalyzedMethod;
    if ((getMatchAnalyzedMethod = MLServerGrpc.getMatchAnalyzedMethod) == null) {
      synchronized (MLServerGrpc.class) {
        if ((getMatchAnalyzedMethod = MLServerGrpc.getMatchAnalyzedMethod) == null) {
          MLServerGrpc.getMatchAnalyzedMethod = getMatchAnalyzedMethod =
              io.grpc.MethodDescriptor.<org.languagetool.rules.ml.MLServerProto.AnalyzedMatchRequest, org.languagetool.rules.ml.MLServerProto.MatchResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "MatchAnalyzed"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.languagetool.rules.ml.MLServerProto.AnalyzedMatchRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.languagetool.rules.ml.MLServerProto.MatchResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MLServerMethodDescriptorSupplier("MatchAnalyzed"))
              .build();
        }
      }
    }
    return getMatchAnalyzedMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static MLServerStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MLServerStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MLServerStub>() {
        @java.lang.Override
        public MLServerStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MLServerStub(channel, callOptions);
        }
      };
    return MLServerStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static MLServerBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MLServerBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MLServerBlockingStub>() {
        @java.lang.Override
        public MLServerBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MLServerBlockingStub(channel, callOptions);
        }
      };
    return MLServerBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static MLServerFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MLServerFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MLServerFutureStub>() {
        @java.lang.Override
        public MLServerFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MLServerFutureStub(channel, callOptions);
        }
      };
    return MLServerFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void match(org.languagetool.rules.ml.MLServerProto.MatchRequest request,
        io.grpc.stub.StreamObserver<org.languagetool.rules.ml.MLServerProto.MatchResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getMatchMethod(), responseObserver);
    }

    /**
     */
    default void matchAnalyzed(org.languagetool.rules.ml.MLServerProto.AnalyzedMatchRequest request,
        io.grpc.stub.StreamObserver<org.languagetool.rules.ml.MLServerProto.MatchResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getMatchAnalyzedMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service MLServer.
   */
  public static abstract class MLServerImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return MLServerGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service MLServer.
   */
  public static final class MLServerStub
      extends io.grpc.stub.AbstractAsyncStub<MLServerStub> {
    private MLServerStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MLServerStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MLServerStub(channel, callOptions);
    }

    /**
     */
    public void match(org.languagetool.rules.ml.MLServerProto.MatchRequest request,
        io.grpc.stub.StreamObserver<org.languagetool.rules.ml.MLServerProto.MatchResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getMatchMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void matchAnalyzed(org.languagetool.rules.ml.MLServerProto.AnalyzedMatchRequest request,
        io.grpc.stub.StreamObserver<org.languagetool.rules.ml.MLServerProto.MatchResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getMatchAnalyzedMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service MLServer.
   */
  public static final class MLServerBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<MLServerBlockingStub> {
    private MLServerBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MLServerBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MLServerBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.languagetool.rules.ml.MLServerProto.MatchResponse match(org.languagetool.rules.ml.MLServerProto.MatchRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getMatchMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.languagetool.rules.ml.MLServerProto.MatchResponse matchAnalyzed(org.languagetool.rules.ml.MLServerProto.AnalyzedMatchRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getMatchAnalyzedMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service MLServer.
   */
  public static final class MLServerFutureStub
      extends io.grpc.stub.AbstractFutureStub<MLServerFutureStub> {
    private MLServerFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MLServerFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MLServerFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.languagetool.rules.ml.MLServerProto.MatchResponse> match(
        org.languagetool.rules.ml.MLServerProto.MatchRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getMatchMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.languagetool.rules.ml.MLServerProto.MatchResponse> matchAnalyzed(
        org.languagetool.rules.ml.MLServerProto.AnalyzedMatchRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getMatchAnalyzedMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_MATCH = 0;
  private static final int METHODID_MATCH_ANALYZED = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_MATCH:
          serviceImpl.match((org.languagetool.rules.ml.MLServerProto.MatchRequest) request,
              (io.grpc.stub.StreamObserver<org.languagetool.rules.ml.MLServerProto.MatchResponse>) responseObserver);
          break;
        case METHODID_MATCH_ANALYZED:
          serviceImpl.matchAnalyzed((org.languagetool.rules.ml.MLServerProto.AnalyzedMatchRequest) request,
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

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getMatchMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.languagetool.rules.ml.MLServerProto.MatchRequest,
              org.languagetool.rules.ml.MLServerProto.MatchResponse>(
                service, METHODID_MATCH)))
        .addMethod(
          getMatchAnalyzedMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.languagetool.rules.ml.MLServerProto.AnalyzedMatchRequest,
              org.languagetool.rules.ml.MLServerProto.MatchResponse>(
                service, METHODID_MATCH_ANALYZED)))
        .build();
  }

  private static abstract class MLServerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    MLServerBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.languagetool.rules.ml.MLServerProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("MLServer");
    }
  }

  private static final class MLServerFileDescriptorSupplier
      extends MLServerBaseDescriptorSupplier {
    MLServerFileDescriptorSupplier() {}
  }

  private static final class MLServerMethodDescriptorSupplier
      extends MLServerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    MLServerMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (MLServerGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new MLServerFileDescriptorSupplier())
              .addMethod(getMatchMethod())
              .addMethod(getMatchAnalyzedMethod())
              .build();
        }
      }
    }
    return result;
  }
}
