package org.languagetool.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.cli.*;
import org.languagetool.*;
import org.languagetool.rules.GRPCUtils;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.ml.MLServerProto.AnalyzeResponse;
import org.languagetool.rules.ml.MLServerProto.ProcessResponse;
import org.languagetool.rules.ml.MLServerProto.ProcessingOptions;
import org.languagetool.rules.ml.ProcessingServerGrpc.ProcessingServerImplBase;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Experimental
public class GRPCServer extends ProcessingServerImplBase
{
  private PipelinePool pool;
  private UserConfig userConfig;
  private GlobalConfig globalConfig;

  public GRPCServer() {
    userConfig = new UserConfig();
    globalConfig = new GlobalConfig();
  }

  public void initPool(HTTPServerConfig serverConfig) {
    pool = new PipelinePool(serverConfig, null, false);
  }

  private PipelineSettings buildSettings(ProcessingOptions options) {
    Language lang = Languages.getLanguageForShortCode(options.getLanguage());
    JLanguageTool.Level level = GRPCUtils.fromGRPC(options.getLevel());
    List<String> enabled = options.getEnabledRulesList().stream().collect(Collectors.toList());
    List<String> disabled = options.getDisabledRulesList().stream().collect(Collectors.toList());
    TextChecker.QueryParams params = new TextChecker.QueryParams(Collections.emptyList(), enabled, disabled,
      Collections.emptyList(), Collections.emptyList(), options.getEnabledOnly(), true, false,
      false, options.getPremium(), options.getTempOff(), JLanguageTool.Mode.ALL, level, null);
    return new PipelineSettings(lang, null, params, globalConfig, userConfig);
  }

  /**
   */
  public void analyze(org.languagetool.rules.ml.MLServerProto.AnalyzeRequest request,
      io.grpc.stub.StreamObserver<org.languagetool.rules.ml.MLServerProto.AnalyzeResponse> responseObserver) {
    if (pool == null) {
      responseObserver.onError(new IllegalStateException("Pool not initialized"));
      return;
    }
    try {
      log.info("Handling analyze request");
       PipelineSettings settings = buildSettings(request.getOptions());
       Pipeline pipeline = pool.getPipeline(settings);
       List<AnalyzedSentence> sentences = pipeline.analyzeText(request.getText());
      AnalyzeResponse response = AnalyzeResponse.newBuilder()
         .addAllSentences(sentences.stream().map(GRPCUtils::toGRPC).collect(Collectors.toList()))
        .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.warn("Analyze request failed", e);
      responseObserver.onError(e);
    }
  }

  /**
   */
  public void process(org.languagetool.rules.ml.MLServerProto.ProcessRequest request,
      io.grpc.stub.StreamObserver<org.languagetool.rules.ml.MLServerProto.ProcessResponse> responseObserver) {
    if (pool == null) {
      responseObserver.onError(new IllegalStateException("Pool not initialized"));
      return;
    }
    try {
      log.info("Handling process request: {}", request);
      PipelineSettings settings = buildSettings(request.getOptions());
      Pipeline pipeline = pool.getPipeline(settings);
      // List<AnalyzedSentence> sentences = request.getSentencesList().stream()
      //   .map(GRPCUtils::fromGRPC).collect(Collectors.toList());

      // TODO: rawMatches should have all results, regardless of mode/level/tempOff/...
      List<RuleMatch> rawMatches = new ArrayList<>();
      String text = request.getSentencesList().stream().map(s -> s.getText()).collect(Collectors.joining());
      List<RuleMatch> matches = pipeline.check(text, m -> rawMatches.add(m));
      // TODO use checkInternal again
      // CheckResults results = pipeline.checkAnalyzedSentences(sentences, m -> rawMatches.add(m));

      ProcessResponse response = ProcessResponse.newBuilder()
        .addAllRawMatches(rawMatches.stream().map(GRPCUtils::toGRPC).collect(Collectors.toList()))
        // .addAllMatches(results.getRuleMatches().stream().map(GRPCUtils::toGRPC).collect(Collectors.toList()))
        .addAllMatches(matches.stream().map(GRPCUtils::toGRPC).collect(Collectors.toList()))
        .build();
      responseObserver.onNext(response);
      log.info("Sending response: {}", response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.warn("Process request failed", e);
      responseObserver.onError(e);
    }
  }

  private Options getCommandLineOptions() {
    Options options = new Options();
    options.addOption(Option.builder().longOpt("config").hasArg().required().build());
    options.addOption(Option.builder().longOpt("port").hasArg().build());
    options.addOption(Option.builder().longOpt("cert").hasArg().build());
    options.addOption(Option.builder().longOpt("key").hasArg().build());
    return options;
  }

  private CommandLine parseCommandLine(String[] args) throws ParseException {
    CommandLineParser parser = new DefaultParser();
    return parser.parse(getCommandLineOptions(), args);
  }

  public static void main(String[] args) throws Exception {
    GRPCServer instance = new GRPCServer();

    int port = 8080;
    File cert = null, key = null;
    HTTPServerConfig config;

    try {
      CommandLine cli = instance.parseCommandLine(args);
      String certPath = cli.getOptionValue("cert");
      String keyPath = cli.getOptionValue("key");
      if (certPath != null && keyPath != null) {
        cert = new File(certPath);
        key = new File(keyPath);
      }
      if (cli.hasOption("port")) {
        port = Integer.parseInt(cli.getOptionValue("port"));
      }
      if (cli.hasOption("config")) {
        config = new HTTPServerConfig(new String[] { "--config", cli.getOptionValue("config")});
      } else {
        config = new HTTPServerConfig();
      }
    } catch (Exception e) {
      HelpFormatter help = new HelpFormatter();
      help.printHelp("GRPCServer", instance.getCommandLineOptions());
      System.exit(1);
      return;
    }

    instance.initPool(config);

    Executor executor = Executors.newCachedThreadPool();
    ServerBuilder builder = ServerBuilder.forPort(port)
      .addService(instance)
      .executor(executor);
    if (cert != null && key != null) {
      builder = builder.useTransportSecurity(cert, key);
    }
    Server server = builder.build();
    server.start();
    System.out.println("port=" + server.getPort());
    server.awaitTermination();
  }
}
