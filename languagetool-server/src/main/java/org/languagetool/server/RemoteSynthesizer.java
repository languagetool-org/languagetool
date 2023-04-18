package org.languagetool.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.grpc.SynthesizeServerGrpc;
import org.languagetool.rules.GRPCUtils;
import org.languagetool.synthesis.Synthesizer;

@Slf4j
public class RemoteSynthesizer extends SynthesizeServerGrpc.SynthesizeServerImplBase {

  protected List<String> synthesize(String languageCode, String lemma, String postag, boolean postagRegexp)
      throws IOException {
    Language lang = Languages.getLanguageForShortCode(languageCode);
    Synthesizer synth = lang.getSynthesizer();
    AnalyzedToken at = new AnalyzedToken(lemma, postag, lemma);
    String[] synthesizedForms = synth.synthesize(at, postag, postagRegexp);
    // removing duplicates. TODO: de-duplicate in the original synthesizer (?)
    return removeDuplicates(synthesizedForms);
  }

  protected List<String> synthesize(String languageCode, AnalyzedTokenReadings atrs, boolean postagRegexp,
      String postagSelect, String postagReplace, String lemmaReplace) throws IOException {
    if (!postagRegexp) {
      return synthesize(languageCode, lemmaReplace, postagReplace, false);
    }
    AnalyzedToken atr = atrs.readingWithTagRegex(postagSelect);
    if (atr == null) {
      log.error("AnalyzedToken is null. Language: " + languageCode + ", Token:" + atrs + ", postagSelect: " + postagSelect);
      return null;
    }
    if (lemmaReplace != null & !lemmaReplace.isEmpty()) {
      atr = new AnalyzedToken(atr.getToken(), atr.getPOSTag(), lemmaReplace);
    }
    String postagReplaceFinal = null;
    try {
      Pattern p = Pattern.compile(postagSelect);
      Matcher m = p.matcher(atr.getPOSTag());
      postagReplaceFinal = m.replaceAll(postagReplace);
    } catch (IndexOutOfBoundsException | PatternSyntaxException e) {
      log.error("Exception: error in regular expressions. Language: " + languageCode + ", Token:" + atrs, e);
      return null;
    }
    Language lang = Languages.getLanguageForShortCode(languageCode);
    Synthesizer synth = lang.getSynthesizer();
    String[] synthesizedForms = synth.synthesize(atr, postagReplaceFinal, true);
    return removeDuplicates(synthesizedForms);
  }

  private List<String> removeDuplicates(String[] forms) {
    List<String> results = new ArrayList<>();
    for (String s : forms) {
      if (!results.contains(s)) {
        results.add(s);
      }
    }
    return results;
  }

  @Override
  public void synthesize(org.languagetool.grpc.Synthesizer.SynthesizeRequest request, StreamObserver<org.languagetool.grpc.Synthesizer.SynthesizeResponse> responseObserver) {

    try {
      List<org.languagetool.grpc.Synthesizer.SynthesizeResponseItem> items = new ArrayList<>();

      for (org.languagetool.grpc.Synthesizer.SynthesizeRequestItem item : request.getItemsList()) {
        AnalyzedTokenReadings tokens = null;
        if (item.getPostagRegexp()) {
          tokens = GRPCUtils.fromGRPC(item.getTokens());
        }
        List<String> forms = synthesize(request.getLanguageCode(), tokens, item.getPostagRegexp(), item.getPostag(), item.getPostagReplace(), item.getLemma());
        items.add(org.languagetool.grpc.Synthesizer.SynthesizeResponseItem.newBuilder()
          .addAllForms(forms).build());
      }
      responseObserver.onNext(org.languagetool.grpc.Synthesizer.SynthesizeResponse.newBuilder().addAllItems(items).build());
      responseObserver.onCompleted();
    } catch(Exception e) {
      log.warn("Synthesize request failed", e);
      responseObserver.onError(e);
    }

  }
  private Options getCommandLineOptions() {
    Options options = new Options();
    options.addOption(Option.builder().longOpt("port").hasArg().build());
    options.addOption(Option.builder().longOpt("cert").hasArg().build());
    options.addOption(Option.builder().longOpt("key").hasArg().build());
    return options;
  }

  private CommandLine parseCommandLine(String[] args) throws ParseException {
    CommandLineParser parser = new DefaultParser();
    return parser.parse(getCommandLineOptions(), args);
  }


  public static void main(String[] args) throws IOException, InterruptedException {
    int port = 8080;
    File cert = null, key = null;
    RemoteSynthesizer instance = new RemoteSynthesizer();

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
    } catch (Exception e) {
      HelpFormatter help = new HelpFormatter();
      help.printHelp("RemoteSynthesizer", instance.getCommandLineOptions());
      System.exit(1);
      return;
    }

    Executor executor = Executors.newCachedThreadPool();
    ServerBuilder builder = ServerBuilder.forPort(port)
      .addService(instance)
      .executor(executor);
    if (cert != null && key != null) {
      builder = builder.useTransportSecurity(cert, key);
    }
    Server server = builder.build();
    server.start();
    log.info("Server listening on port {}", port);
    server.awaitTermination();
  }
}
