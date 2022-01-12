package org.languagetool.rules;

import static org.languagetool.rules.GRPCRule.Connection.getManagedChannel;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.net.ssl.SSLException;

import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.ml.MLServerProto.Match;
import org.languagetool.rules.ml.MLServerProto.MatchList;
import org.languagetool.rules.ml.MLServerProto.MatchResponse;
import org.languagetool.rules.ml.MLServerProto.PostProcessingRequest;
import org.languagetool.rules.ml.PostProcessingServerGrpc;
import org.languagetool.rules.ml.PostProcessingServerGrpc.PostProcessingServerBlockingStub;
import org.languagetool.tools.CircuitBreakers;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GRPCRuleMatchFilter implements RuleMatchFilter {

  private static final Duration slowDurationThreshold = Duration.ofMillis(400);
  private static final int slowRateThreshold = 80;
  private static final int failureRateThreshold = 50;
  private static final Duration waitDurationOpen = Duration.ofSeconds(60);

  private static final CircuitBreaker circuitBreaker;
  private static ManagedChannel channel;
  private static PostProcessingServerBlockingStub stub;

  static {
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
      .slowCallDurationThreshold(slowDurationThreshold)
      .slowCallRateThreshold(slowRateThreshold)
      .failureRateThreshold(failureRateThreshold)
      .waitDurationInOpenState(waitDurationOpen)
      .build();
    circuitBreaker = CircuitBreakers.registry().circuitBreaker(GRPCRuleMatchFilter.class.getName(), config);
  }

  public GRPCRuleMatchFilter(Language language) {
  }

  public static void configure(File remoteRuleConfig) {
    // TODO: own config object / values; configure circuit breaker via options;
    // make a static load method that loads and caches based on remote rule configuration, language-dependent
    if (channel != null) {
      return;
    }
    List<RemoteRuleConfig> configs = null;
    try {
      configs = RemoteRuleConfig.load(remoteRuleConfig);
      // TODO: make this language-dependent or send language as part of request
      RemoteRuleConfig serviceConfiguration = RemoteRuleConfig.getRelevantConfig("AI_RESORTING", configs);
      if (serviceConfiguration != null) {
        String host = serviceConfiguration.getUrl();
        int port = serviceConfiguration.getPort();
        boolean ssl = Boolean.parseBoolean(serviceConfiguration.getOptions().getOrDefault("secure", "false"));
        String key = serviceConfiguration.getOptions().get("clientKey");
        String cert = serviceConfiguration.getOptions().get("clientCertificate");
        String ca = serviceConfiguration.getOptions().get("rootCertificate");
        channel = getManagedChannel(host, port, ssl, key, cert, ca);
        stub = PostProcessingServerGrpc.newBlockingStub(channel);
      }
    } catch (ExecutionException | SSLException e) {
      log.error("Couldn't configure GRPCRuleMatchFilter", e);
    }
  }

  private Match convertMatch(RuleMatch m) {
    return Match.newBuilder()
      .setOffset(m.getFromPos())
      .setLength(m.getToPos() - m.getFromPos())
      .setId(m.getRule().getId())
      // TODO: subId for pattern rules, etc.; for now empty
      // TODO: use suggestedReplacements field
      .addAllSuggestions(m.getSuggestedReplacements())
      .setRuleDescription(m.getRule().getDescription())
      .setMatchDescription(m.getMessage())
      .setMatchShortDescription(m.getShortMessage())
      .setUrl((m.getUrl() != null ? m.getUrl().toString() : ""))
      .setAutoCorrect(m.isAutoCorrect())
      .build();
  }

  private RuleMatch convertMatch(Match m, AnalyzedSentence s) {
    // TODO: use our own rule subclass?
    Rule rule = new GRPCRule.GRPCSubRule(m.getId(), m.getSubId(), m.getRuleDescription());
    RuleMatch r = new RuleMatch(rule, s, m.getOffset(), m.getOffset() + m.getLength(), m.getMatchDescription(), m.getMatchShortDescription());

    r.setSuggestedReplacements(m.getSuggestionsList());
    r.setAutoCorrect(m.getAutoCorrect());

    if (!m.getUrl().isEmpty()) {
      try {
        r.setUrl(new URL(m.getUrl()));
      } catch (MalformedURLException e) {
        log.warn("Got invalid URL from GRPC match filter {}: {}", this, e);
      }
    }
    return r;
  }

  @Override
  public List<RuleMatch> filter(List<RuleMatch> ruleMatches, AnnotatedText text) {
    if (channel == null) {
      return ruleMatches;
    }
    System.out.println("RESORTING IN: " + ruleMatches.stream().map(RuleMatch::getSuggestedReplacements).collect(Collectors.toList()));

    // TODO: transform or take as argument (List<AnalyzedSentence>, List<List<RuleMatch>>)
    // for now, allow duplicate sentences; each rule match is transformed into one sentence and a list with just that one rule match
    List<String> sentences = ruleMatches.stream().map(RuleMatch::getSentence).map(AnalyzedSentence::getText).collect(Collectors.toList());
    List<MatchList> matches = ruleMatches.stream().map(m -> MatchList.newBuilder().addMatches(convertMatch(m)).build()).collect(Collectors.toList());

    PostProcessingRequest req = PostProcessingRequest.newBuilder().addAllSentences(sentences).addAllMatches(matches).build();
    // TODO: circuitBreaker
    MatchResponse response = stub.process(req);
    List<RuleMatch> result = new ArrayList<>(response.getSentenceMatchesCount());
    for (int i = 0; i < response.getSentenceMatchesCount(); i++) {
      result.add(convertMatch(response.getSentenceMatches(i).getMatches(0), ruleMatches.get(i).getSentence()));
    }
    System.out.println("RESORTING OUT: " + result.stream().map(RuleMatch::getSuggestedReplacements).collect(Collectors.toList()));
    return result;
  }
}
