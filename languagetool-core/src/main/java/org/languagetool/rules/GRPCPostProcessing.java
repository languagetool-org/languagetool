package org.languagetool.rules;

import static org.languagetool.rules.GRPCRule.Connection.getManagedChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.rules.ml.MLServerProto.MatchList;
import org.languagetool.rules.ml.MLServerProto.MatchResponse;
import org.languagetool.rules.ml.MLServerProto.PostProcessingRequest;
import org.languagetool.rules.ml.PostProcessingServerGrpc;
import org.languagetool.rules.ml.PostProcessingServerGrpc.PostProcessingServerBlockingStub;
import org.languagetool.tools.CircuitBreakers;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GRPCPostProcessing {
  public static final String CONFIG_TYPE = "grpc-post";

  private final CircuitBreaker circuitBreaker;
  private ManagedChannel channel;
  private PostProcessingServerBlockingStub stub;

  private RemoteRuleConfig config;

  // instances by rule ID in RemoteRuleConfig
  private static ConcurrentMap<String, GRPCPostProcessing> instances = new ConcurrentHashMap<>();
  // configured rule IDs by language
  // multiple language variants can share an instance
  // there can be multiple IDs per language
  private static ConcurrentMap<Language, Set<String>> configIDs = new ConcurrentHashMap<>();



  protected GRPCPostProcessing(RemoteRuleConfig config) throws Exception {
    this.config = config;
    CircuitBreakerConfig circuitBreakerConfig = RemoteRule.getCircuitBreakerConfig(config, config.getRuleId());
    circuitBreaker = CircuitBreakers.registry().circuitBreaker(
      "grpc-postprocessing-" + config.getRuleId(), circuitBreakerConfig);

    String host = config.getUrl();
    int port = config.getPort();
    boolean ssl = Boolean.parseBoolean(config.getOptions().getOrDefault("secure", "false"));
    String key = config.getOptions().get("clientKey");
    String cert = config.getOptions().get("clientCertificate");
    String ca = config.getOptions().get("rootCertificate");
    channel = getManagedChannel(host, port, ssl, key, cert, ca);
    stub = PostProcessingServerGrpc.newBlockingStub(channel);
  }

  @NotNull
  public static List<GRPCPostProcessing> get(Language lang) {
    return configIDs
      .getOrDefault(lang, Collections.emptySet())
      .stream().map(instances::get)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  public static void configure(Language lang, List<RemoteRuleConfig> configs) {
    configs.stream().filter(RemoteRuleConfig.isRelevantConfig(CONFIG_TYPE, lang)).forEach(config -> {
      String key = config.getRuleId();
      configIDs.computeIfAbsent(lang, k -> new HashSet<>()).add(key);
      instances.computeIfAbsent(key, k -> {
        try {
          return new GRPCPostProcessing(config);
        } catch (Exception e) {
          log.warn(String.format("Couldn't initialize GRPCPostProcessing instance" +
            " for language '%s' and configuration '%s'", lang, config), e);
          return null;
        }
      });
    });
  }

  private PostProcessingRequest buildRequest(List<AnalyzedSentence> sentences, List<RuleMatch> ruleMatches,
                                             List<Integer> offset, Long textSessionId, boolean inputLogging) {
    // don't modify passed rule matches list, used as fallback
    ruleMatches = ruleMatches.stream().map(r -> new RuleMatch(r)).collect(Collectors.toList());
    List<MatchList> matches = new ArrayList<>();
    // convert offsets so that they are relative to each sentence instead of the text
    for (int i = 0; i < sentences.size(); i++) {
      AnalyzedSentence sentence = sentences.get(i);
      if (i == 0) {
        offset.add(0);
      } else {
        offset.add(offset.get(i-1) + sentences.get(i-1).getText().length());
      }
      List<RuleMatch> sentenceMatches = new ArrayList<>();
      Iterator<RuleMatch> iter = ruleMatches.iterator();
      while (iter.hasNext()) {
        RuleMatch m = iter.next();
        // NOTE: we can't compare sentences directly, as parts of sentences like AnalyzedTokenReadings
        // seem to be mutated during the matching process and rule matches will thus refer to sentences that are
        // not equal to the initially analyzed sentences we receive in the first argument
        // e.g. by AnalyzedTokenReadings.immunize()
        // example sentence: The breakdown suggest that the transportation sector register a large fall in prices.
        // so just use sentence.getText()
        if (sentence.getText().equals(m.getSentence().getText())) {
          iter.remove();
          m.setOffsetPosition(m.getFromPos() - offset.get(i), m.getToPos() - offset.get(i));
          sentenceMatches.add(m);
        }
      }
      matches.add(MatchList.newBuilder()
          .addAllMatches(sentenceMatches.stream().map(GRPCUtils::toGRPC).collect(Collectors.toList())).build());
    }
    List<String> sentenceText = sentences.stream().map(AnalyzedSentence::getText).collect(Collectors.toList());

    PostProcessingRequest.Builder req = PostProcessingRequest.newBuilder()
      .addAllSentences(sentenceText).addAllMatches(matches);
    if (textSessionId != null) {
      req.addAllTextSessionID(Collections.nCopies(sentenceText.size(), textSessionId));
    }
    req.setInputLogging(inputLogging);
    return req.build();
  }

  public List<RuleMatch> filter(List<AnalyzedSentence> sentences, List<RuleMatch> ruleMatches, Long textSessionID, boolean inputLogging) {
    if (channel == null) {
      return ruleMatches;
    }

    int chars = sentences.stream().map(s -> s.getText().length()).reduce(0, Integer::sum);
    List<RuleMatch> result;
    long start = System.nanoTime();
    try {
      if (circuitBreaker != null) {
        result = RemoteRuleMetrics.inCircuitBreaker(System.nanoTime(), circuitBreaker,
          config.ruleId, chars, () -> runPostprocessing(sentences, ruleMatches, textSessionID, inputLogging, chars));
      } else {
        result = runPostprocessing(sentences, ruleMatches, textSessionID, inputLogging, chars);
      }
    } catch (Exception e) {
      log.warn("gRPC postprocessing failed", e);
      return ruleMatches;
    }
    if (result == null) {
      return ruleMatches;
    } else {
      long delta = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
      log.info("gRPC postprocessing chars={} sentences={} matches={} time={}ms",
               chars, sentences.size(), ruleMatches.size(), delta);
      RemoteRuleMetrics.wait(config.getRuleId(), delta);
      RemoteRuleMetrics.request(config.getRuleId(), start, chars, RemoteRuleMetrics.RequestResult.SUCCESS);
      return result;
    }
  }

  protected MatchResponse sendRequest(PostProcessingRequest req, long timeout) throws Exception {
    return stub.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS)
      .process(req);
  }

  protected List<RuleMatch> runPostprocessing(List<AnalyzedSentence> sentences, List<RuleMatch> ruleMatches,
                                            Long textSessionID, boolean inputLogging, int chars) throws Exception {
    List<Integer> offset = new ArrayList<>();
    long timeout = RemoteRule.getTimeout(config, chars);
    try {
      PostProcessingRequest req = buildRequest(sentences, ruleMatches, offset, textSessionID, inputLogging);
      MatchResponse response = sendRequest(req, timeout);
      List<RuleMatch> result = new ArrayList<>(response.getSentenceMatchesCount());
      for (int i = 0; i < response.getSentenceMatchesCount(); i++) {
        MatchList matchList = response.getSentenceMatches(i);
        AnalyzedSentence sentence = sentences.get(i);
        int offsetShift = offset.get(i);
        for (int j = 0; j  < matchList.getMatchesCount(); j++) {
          RuleMatch match = GRPCUtils.fromGRPC(matchList.getMatches(j), sentence);
          match.setOffsetPosition(match.getFromPos() + offsetShift, match.getToPos() + offsetShift);
          result.add(match);
        }
      }
      return result;
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.DEADLINE_EXCEEDED.getCode()) {
        throw new TimeoutException("gRPC postprocessing timed out: " + e.getMessage());
      } else {
        throw e;
      }
    }
  }
}
