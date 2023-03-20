package org.languagetool.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.50.2)",
    comments = "Source: synthesizer.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class SynthesizeServerGrpc {

  private SynthesizeServerGrpc() {}

  public static final String SERVICE_NAME = "lt_ml_server.SynthesizeServer";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.languagetool.grpc.Synthesizer.SynthesizeRequest,
      org.languagetool.grpc.Synthesizer.SynthesizeResponse> getSynthesizeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Synthesize",
      requestType = org.languagetool.grpc.Synthesizer.SynthesizeRequest.class,
      responseType = org.languagetool.grpc.Synthesizer.SynthesizeResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.languagetool.grpc.Synthesizer.SynthesizeRequest,
      org.languagetool.grpc.Synthesizer.SynthesizeResponse> getSynthesizeMethod() {
    io.grpc.MethodDescriptor<org.languagetool.grpc.Synthesizer.SynthesizeRequest, org.languagetool.grpc.Synthesizer.SynthesizeResponse> getSynthesizeMethod;
    if ((getSynthesizeMethod = SynthesizeServerGrpc.getSynthesizeMethod) == null) {
      synchronized (SynthesizeServerGrpc.class) {
        if ((getSynthesizeMethod = SynthesizeServerGrpc.getSynthesizeMethod) == null) {
          SynthesizeServerGrpc.getSynthesizeMethod = getSynthesizeMethod =
              io.grpc.MethodDescriptor.<org.languagetool.grpc.Synthesizer.SynthesizeRequest, org.languagetool.grpc.Synthesizer.SynthesizeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Synthesize"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.languagetool.grpc.Synthesizer.SynthesizeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.languagetool.grpc.Synthesizer.SynthesizeResponse.getDefaultInstance()))
              .setSchemaDescriptor(new SynthesizeServerMethodDescriptorSupplier("Synthesize"))
              .build();
        }
      }
    }
    return getSynthesizeMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static SynthesizeServerStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SynthesizeServerStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SynthesizeServerStub>() {
        @java.lang.Override
        public SynthesizeServerStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SynthesizeServerStub(channel, callOptions);
        }
      };
    return SynthesizeServerStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static SynthesizeServerBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SynthesizeServerBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SynthesizeServerBlockingStub>() {
        @java.lang.Override
        public SynthesizeServerBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SynthesizeServerBlockingStub(channel, callOptions);
        }
      };
    return SynthesizeServerBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static SynthesizeServerFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SynthesizeServerFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SynthesizeServerFutureStub>() {
        @java.lang.Override
        public SynthesizeServerFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SynthesizeServerFutureStub(channel, callOptions);
        }
      };
    return SynthesizeServerFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class SynthesizeServerImplBase implements io.grpc.BindableService {

    /**
     */
    public void synthesize(org.languagetool.grpc.Synthesizer.SynthesizeRequest request,
        io.grpc.stub.StreamObserver<org.languagetool.grpc.Synthesizer.SynthesizeResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSynthesizeMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getSynthesizeMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                org.languagetool.grpc.Synthesizer.SynthesizeRequest,
                org.languagetool.grpc.Synthesizer.SynthesizeResponse>(
                  this, METHODID_SYNTHESIZE)))
          .build();
    }
  }

  /**
   */
  public static final class SynthesizeServerStub extends io.grpc.stub.AbstractAsyncStub<SynthesizeServerStub> {
    private SynthesizeServerStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SynthesizeServerStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SynthesizeServerStub(channel, callOptions);
    }

    /**
     */
    public void synthesize(org.languagetool.grpc.Synthesizer.SynthesizeRequest request,
        io.grpc.stub.StreamObserver<org.languagetool.grpc.Synthesizer.SynthesizeResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSynthesizeMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class SynthesizeServerBlockingStub extends io.grpc.stub.AbstractBlockingStub<SynthesizeServerBlockingStub> {
    private SynthesizeServerBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SynthesizeServerBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SynthesizeServerBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.languagetool.grpc.Synthesizer.SynthesizeResponse synthesize(org.languagetool.grpc.Synthesizer.SynthesizeRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSynthesizeMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class SynthesizeServerFutureStub extends io.grpc.stub.AbstractFutureStub<SynthesizeServerFutureStub> {
    private SynthesizeServerFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SynthesizeServerFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SynthesizeServerFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.languagetool.grpc.Synthesizer.SynthesizeResponse> synthesize(
        org.languagetool.grpc.Synthesizer.SynthesizeRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSynthesizeMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SYNTHESIZE = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final SynthesizeServerImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(SynthesizeServerImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SYNTHESIZE:
          serviceImpl.synthesize((org.languagetool.grpc.Synthesizer.SynthesizeRequest) request,
              (io.grpc.stub.StreamObserver<org.languagetool.grpc.Synthesizer.SynthesizeResponse>) responseObserver);
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

  private static abstract class SynthesizeServerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    SynthesizeServerBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.languagetool.grpc.Synthesizer.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("SynthesizeServer");
    }
  }

  private static final class SynthesizeServerFileDescriptorSupplier
      extends SynthesizeServerBaseDescriptorSupplier {
    SynthesizeServerFileDescriptorSupplier() {}
  }

  private static final class SynthesizeServerMethodDescriptorSupplier
      extends SynthesizeServerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    SynthesizeServerMethodDescriptorSupplier(String methodName) {
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
      synchronized (SynthesizeServerGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new SynthesizeServerFileDescriptorSupplier())
              .addMethod(getSynthesizeMethod())
              .build();
        }
      }
    }
    return result;
  }
}
