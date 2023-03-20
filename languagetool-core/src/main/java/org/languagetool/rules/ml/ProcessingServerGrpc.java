package org.languagetool.rules.ml;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.50.2)",
    comments = "Source: ml_server.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class ProcessingServerGrpc {

  private ProcessingServerGrpc() {}

  public static final String SERVICE_NAME = "lt_ml_server.ProcessingServer";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.languagetool.rules.ml.MLServerProto.AnalyzeRequest,
      org.languagetool.rules.ml.MLServerProto.AnalyzeResponse> getAnalyzeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Analyze",
      requestType = org.languagetool.rules.ml.MLServerProto.AnalyzeRequest.class,
      responseType = org.languagetool.rules.ml.MLServerProto.AnalyzeResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.languagetool.rules.ml.MLServerProto.AnalyzeRequest,
      org.languagetool.rules.ml.MLServerProto.AnalyzeResponse> getAnalyzeMethod() {
    io.grpc.MethodDescriptor<org.languagetool.rules.ml.MLServerProto.AnalyzeRequest, org.languagetool.rules.ml.MLServerProto.AnalyzeResponse> getAnalyzeMethod;
    if ((getAnalyzeMethod = ProcessingServerGrpc.getAnalyzeMethod) == null) {
      synchronized (ProcessingServerGrpc.class) {
        if ((getAnalyzeMethod = ProcessingServerGrpc.getAnalyzeMethod) == null) {
          ProcessingServerGrpc.getAnalyzeMethod = getAnalyzeMethod =
              io.grpc.MethodDescriptor.<org.languagetool.rules.ml.MLServerProto.AnalyzeRequest, org.languagetool.rules.ml.MLServerProto.AnalyzeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Analyze"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.languagetool.rules.ml.MLServerProto.AnalyzeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.languagetool.rules.ml.MLServerProto.AnalyzeResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ProcessingServerMethodDescriptorSupplier("Analyze"))
              .build();
        }
      }
    }
    return getAnalyzeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.languagetool.rules.ml.MLServerProto.ProcessRequest,
      org.languagetool.rules.ml.MLServerProto.ProcessResponse> getProcessMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Process",
      requestType = org.languagetool.rules.ml.MLServerProto.ProcessRequest.class,
      responseType = org.languagetool.rules.ml.MLServerProto.ProcessResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.languagetool.rules.ml.MLServerProto.ProcessRequest,
      org.languagetool.rules.ml.MLServerProto.ProcessResponse> getProcessMethod() {
    io.grpc.MethodDescriptor<org.languagetool.rules.ml.MLServerProto.ProcessRequest, org.languagetool.rules.ml.MLServerProto.ProcessResponse> getProcessMethod;
    if ((getProcessMethod = ProcessingServerGrpc.getProcessMethod) == null) {
      synchronized (ProcessingServerGrpc.class) {
        if ((getProcessMethod = ProcessingServerGrpc.getProcessMethod) == null) {
          ProcessingServerGrpc.getProcessMethod = getProcessMethod =
              io.grpc.MethodDescriptor.<org.languagetool.rules.ml.MLServerProto.ProcessRequest, org.languagetool.rules.ml.MLServerProto.ProcessResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Process"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.languagetool.rules.ml.MLServerProto.ProcessRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.languagetool.rules.ml.MLServerProto.ProcessResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ProcessingServerMethodDescriptorSupplier("Process"))
              .build();
        }
      }
    }
    return getProcessMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ProcessingServerStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ProcessingServerStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ProcessingServerStub>() {
        @java.lang.Override
        public ProcessingServerStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ProcessingServerStub(channel, callOptions);
        }
      };
    return ProcessingServerStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ProcessingServerBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ProcessingServerBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ProcessingServerBlockingStub>() {
        @java.lang.Override
        public ProcessingServerBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ProcessingServerBlockingStub(channel, callOptions);
        }
      };
    return ProcessingServerBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ProcessingServerFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ProcessingServerFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ProcessingServerFutureStub>() {
        @java.lang.Override
        public ProcessingServerFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ProcessingServerFutureStub(channel, callOptions);
        }
      };
    return ProcessingServerFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class ProcessingServerImplBase implements io.grpc.BindableService {

    /**
     */
    public void analyze(org.languagetool.rules.ml.MLServerProto.AnalyzeRequest request,
        io.grpc.stub.StreamObserver<org.languagetool.rules.ml.MLServerProto.AnalyzeResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAnalyzeMethod(), responseObserver);
    }

    /**
     */
    public void process(org.languagetool.rules.ml.MLServerProto.ProcessRequest request,
        io.grpc.stub.StreamObserver<org.languagetool.rules.ml.MLServerProto.ProcessResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getProcessMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getAnalyzeMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                org.languagetool.rules.ml.MLServerProto.AnalyzeRequest,
                org.languagetool.rules.ml.MLServerProto.AnalyzeResponse>(
                  this, METHODID_ANALYZE)))
          .addMethod(
            getProcessMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                org.languagetool.rules.ml.MLServerProto.ProcessRequest,
                org.languagetool.rules.ml.MLServerProto.ProcessResponse>(
                  this, METHODID_PROCESS)))
          .build();
    }
  }

  /**
   */
  public static final class ProcessingServerStub extends io.grpc.stub.AbstractAsyncStub<ProcessingServerStub> {
    private ProcessingServerStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ProcessingServerStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ProcessingServerStub(channel, callOptions);
    }

    /**
     */
    public void analyze(org.languagetool.rules.ml.MLServerProto.AnalyzeRequest request,
        io.grpc.stub.StreamObserver<org.languagetool.rules.ml.MLServerProto.AnalyzeResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAnalyzeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void process(org.languagetool.rules.ml.MLServerProto.ProcessRequest request,
        io.grpc.stub.StreamObserver<org.languagetool.rules.ml.MLServerProto.ProcessResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getProcessMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class ProcessingServerBlockingStub extends io.grpc.stub.AbstractBlockingStub<ProcessingServerBlockingStub> {
    private ProcessingServerBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ProcessingServerBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ProcessingServerBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.languagetool.rules.ml.MLServerProto.AnalyzeResponse analyze(org.languagetool.rules.ml.MLServerProto.AnalyzeRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAnalyzeMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.languagetool.rules.ml.MLServerProto.ProcessResponse process(org.languagetool.rules.ml.MLServerProto.ProcessRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getProcessMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class ProcessingServerFutureStub extends io.grpc.stub.AbstractFutureStub<ProcessingServerFutureStub> {
    private ProcessingServerFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ProcessingServerFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ProcessingServerFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.languagetool.rules.ml.MLServerProto.AnalyzeResponse> analyze(
        org.languagetool.rules.ml.MLServerProto.AnalyzeRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAnalyzeMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.languagetool.rules.ml.MLServerProto.ProcessResponse> process(
        org.languagetool.rules.ml.MLServerProto.ProcessRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getProcessMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_ANALYZE = 0;
  private static final int METHODID_PROCESS = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ProcessingServerImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ProcessingServerImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_ANALYZE:
          serviceImpl.analyze((org.languagetool.rules.ml.MLServerProto.AnalyzeRequest) request,
              (io.grpc.stub.StreamObserver<org.languagetool.rules.ml.MLServerProto.AnalyzeResponse>) responseObserver);
          break;
        case METHODID_PROCESS:
          serviceImpl.process((org.languagetool.rules.ml.MLServerProto.ProcessRequest) request,
              (io.grpc.stub.StreamObserver<org.languagetool.rules.ml.MLServerProto.ProcessResponse>) responseObserver);
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

  private static abstract class ProcessingServerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ProcessingServerBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.languagetool.rules.ml.MLServerProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ProcessingServer");
    }
  }

  private static final class ProcessingServerFileDescriptorSupplier
      extends ProcessingServerBaseDescriptorSupplier {
    ProcessingServerFileDescriptorSupplier() {}
  }

  private static final class ProcessingServerMethodDescriptorSupplier
      extends ProcessingServerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ProcessingServerMethodDescriptorSupplier(String methodName) {
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
      synchronized (ProcessingServerGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ProcessingServerFileDescriptorSupplier())
              .addMethod(getAnalyzeMethod())
              .addMethod(getProcessMethod())
              .build();
        }
      }
    }
    return result;
  }
}
