package org.languagetool.languagemodel.bert.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.42.1)",
    comments = "Source: bert-lm.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class BertLmGrpc {

  private BertLmGrpc() {}

  public static final String SERVICE_NAME = "bert.BertLm";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.languagetool.languagemodel.bert.grpc.BertLmProto.ScoreRequest,
      org.languagetool.languagemodel.bert.grpc.BertLmProto.BertLmResponse> getScoreMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Score",
      requestType = org.languagetool.languagemodel.bert.grpc.BertLmProto.ScoreRequest.class,
      responseType = org.languagetool.languagemodel.bert.grpc.BertLmProto.BertLmResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.languagetool.languagemodel.bert.grpc.BertLmProto.ScoreRequest,
      org.languagetool.languagemodel.bert.grpc.BertLmProto.BertLmResponse> getScoreMethod() {
    io.grpc.MethodDescriptor<org.languagetool.languagemodel.bert.grpc.BertLmProto.ScoreRequest, org.languagetool.languagemodel.bert.grpc.BertLmProto.BertLmResponse> getScoreMethod;
    if ((getScoreMethod = BertLmGrpc.getScoreMethod) == null) {
      synchronized (BertLmGrpc.class) {
        if ((getScoreMethod = BertLmGrpc.getScoreMethod) == null) {
          BertLmGrpc.getScoreMethod = getScoreMethod =
              io.grpc.MethodDescriptor.<org.languagetool.languagemodel.bert.grpc.BertLmProto.ScoreRequest, org.languagetool.languagemodel.bert.grpc.BertLmProto.BertLmResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Score"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.languagetool.languagemodel.bert.grpc.BertLmProto.ScoreRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.languagetool.languagemodel.bert.grpc.BertLmProto.BertLmResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BertLmMethodDescriptorSupplier("Score"))
              .build();
        }
      }
    }
    return getScoreMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchScoreRequest,
      org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchBertLmResponse> getBatchScoreMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BatchScore",
      requestType = org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchScoreRequest.class,
      responseType = org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchBertLmResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchScoreRequest,
      org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchBertLmResponse> getBatchScoreMethod() {
    io.grpc.MethodDescriptor<org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchScoreRequest, org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchBertLmResponse> getBatchScoreMethod;
    if ((getBatchScoreMethod = BertLmGrpc.getBatchScoreMethod) == null) {
      synchronized (BertLmGrpc.class) {
        if ((getBatchScoreMethod = BertLmGrpc.getBatchScoreMethod) == null) {
          BertLmGrpc.getBatchScoreMethod = getBatchScoreMethod =
              io.grpc.MethodDescriptor.<org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchScoreRequest, org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchBertLmResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BatchScore"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchScoreRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchBertLmResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BertLmMethodDescriptorSupplier("BatchScore"))
              .build();
        }
      }
    }
    return getBatchScoreMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static BertLmStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BertLmStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BertLmStub>() {
        @java.lang.Override
        public BertLmStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BertLmStub(channel, callOptions);
        }
      };
    return BertLmStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static BertLmBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BertLmBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BertLmBlockingStub>() {
        @java.lang.Override
        public BertLmBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BertLmBlockingStub(channel, callOptions);
        }
      };
    return BertLmBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static BertLmFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BertLmFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BertLmFutureStub>() {
        @java.lang.Override
        public BertLmFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BertLmFutureStub(channel, callOptions);
        }
      };
    return BertLmFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class BertLmImplBase implements io.grpc.BindableService {

    /**
     */
    public void score(org.languagetool.languagemodel.bert.grpc.BertLmProto.ScoreRequest request,
        io.grpc.stub.StreamObserver<org.languagetool.languagemodel.bert.grpc.BertLmProto.BertLmResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getScoreMethod(), responseObserver);
    }

    /**
     */
    public void batchScore(org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchScoreRequest request,
        io.grpc.stub.StreamObserver<org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchBertLmResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getBatchScoreMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getScoreMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                org.languagetool.languagemodel.bert.grpc.BertLmProto.ScoreRequest,
                org.languagetool.languagemodel.bert.grpc.BertLmProto.BertLmResponse>(
                  this, METHODID_SCORE)))
          .addMethod(
            getBatchScoreMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchScoreRequest,
                org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchBertLmResponse>(
                  this, METHODID_BATCH_SCORE)))
          .build();
    }
  }

  /**
   */
  public static final class BertLmStub extends io.grpc.stub.AbstractAsyncStub<BertLmStub> {
    private BertLmStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BertLmStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BertLmStub(channel, callOptions);
    }

    /**
     */
    public void score(org.languagetool.languagemodel.bert.grpc.BertLmProto.ScoreRequest request,
        io.grpc.stub.StreamObserver<org.languagetool.languagemodel.bert.grpc.BertLmProto.BertLmResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getScoreMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void batchScore(org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchScoreRequest request,
        io.grpc.stub.StreamObserver<org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchBertLmResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getBatchScoreMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class BertLmBlockingStub extends io.grpc.stub.AbstractBlockingStub<BertLmBlockingStub> {
    private BertLmBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BertLmBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BertLmBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.languagetool.languagemodel.bert.grpc.BertLmProto.BertLmResponse score(org.languagetool.languagemodel.bert.grpc.BertLmProto.ScoreRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getScoreMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchBertLmResponse batchScore(org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchScoreRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getBatchScoreMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class BertLmFutureStub extends io.grpc.stub.AbstractFutureStub<BertLmFutureStub> {
    private BertLmFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BertLmFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BertLmFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.languagetool.languagemodel.bert.grpc.BertLmProto.BertLmResponse> score(
        org.languagetool.languagemodel.bert.grpc.BertLmProto.ScoreRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getScoreMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchBertLmResponse> batchScore(
        org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchScoreRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getBatchScoreMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SCORE = 0;
  private static final int METHODID_BATCH_SCORE = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final BertLmImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(BertLmImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SCORE:
          serviceImpl.score((org.languagetool.languagemodel.bert.grpc.BertLmProto.ScoreRequest) request,
              (io.grpc.stub.StreamObserver<org.languagetool.languagemodel.bert.grpc.BertLmProto.BertLmResponse>) responseObserver);
          break;
        case METHODID_BATCH_SCORE:
          serviceImpl.batchScore((org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchScoreRequest) request,
              (io.grpc.stub.StreamObserver<org.languagetool.languagemodel.bert.grpc.BertLmProto.BatchBertLmResponse>) responseObserver);
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

  private static abstract class BertLmBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    BertLmBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.languagetool.languagemodel.bert.grpc.BertLmProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("BertLm");
    }
  }

  private static final class BertLmFileDescriptorSupplier
      extends BertLmBaseDescriptorSupplier {
    BertLmFileDescriptorSupplier() {}
  }

  private static final class BertLmMethodDescriptorSupplier
      extends BertLmBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    BertLmMethodDescriptorSupplier(String methodName) {
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
      synchronized (BertLmGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new BertLmFileDescriptorSupplier())
              .addMethod(getScoreMethod())
              .addMethod(getBatchScoreMethod())
              .build();
        }
      }
    }
    return result;
  }
}
