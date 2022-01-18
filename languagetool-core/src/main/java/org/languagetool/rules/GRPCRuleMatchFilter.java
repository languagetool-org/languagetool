package org.languagetool.rules;

import static org.languagetool.rules.GRPCRule.Connection.getManagedChannel;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.net.ssl.SSLException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.Tag;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.ml.MLServerProto;
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

  class RuleData extends Rule {
    private final Match m;
    private final String sourceFile;

    RuleData(Match m) {
      this.m = m;
      this.sourceFile = m.getRule().getSourceFile();
      if (!m.getRule().getIssueType().isEmpty()) {
        setLocQualityIssueType(ITSIssueType.valueOf(m.getRule().getIssueType()));
      }
      if (m.getRule().getTempOff()) {
        setDefaultTempOff();
      }
      if (m.getRule().hasCategory()) {
        Category c = new Category(new CategoryId(m.getRule().getCategory().getId()),
          m.getRule().getCategory().getName());
        setCategory(c);
      }
      setPremium(m.getRule().getIsPremium());
      setTags(m.getRule().getTagsList().stream().map(t -> Tag.valueOf(t.name())).collect(Collectors.toList()));
    }

    @Nullable
    @Override
    public String getSourceFile() {
      if (sourceFile == null || sourceFile.isEmpty()) {
        return null;
      }
      return sourceFile;
    }

    @Override
    public String getId() {
      return m.getId();
    }

    @Override
    public String getSubId() {
     return m.getSubId();
    }

    @Override
    public String getDescription() {
      return m.getRuleDescription();
    }

    @Override
    public int estimateContextForSureMatch() {
      return m.getContextForSureMatch();
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
      throw new UnsupportedOperationException("Not implemented; internal class used for returning match" +
        " information from remote endpoint");
    }
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

  @NotNull
  private static String nullAsEmpty(@Nullable String s) {
    return s != null ? s : "";
  }

  @Nullable
  private static String emptyAsNull(String s) {
    if (s != null && s.isEmpty()) {
      return null;
    }
    return s;
  }

  private MLServerProto.SuggestedReplacement convertSuggestedReplacement(SuggestedReplacement s) {
    MLServerProto.SuggestedReplacement.Builder sb = MLServerProto.SuggestedReplacement.newBuilder()
      .setReplacement(s.getReplacement())
      .setDescription(nullAsEmpty(s.getShortDescription()))
      .setSuffix(nullAsEmpty(s.getSuffix()))
      .setType(MLServerProto.SuggestedReplacement.SuggestionType.valueOf(s.getType().name()));
    if (s.getConfidence() != null) {
      sb.setConfidence(s.getConfidence());
    }
    return sb.build();
  }

  private SuggestedReplacement convertSuggestedReplacement(MLServerProto.SuggestedReplacement s) {
    SuggestedReplacement sb = new SuggestedReplacement(s.getReplacement(),
      emptyAsNull(s.getDescription()), emptyAsNull(s.getSuffix()));
    sb.setType(SuggestedReplacement.SuggestionType.valueOf(s.getType().name()));
    if (s.getConfidence() != 0f) {
      sb.setConfidence(s.getConfidence());
    }
    return sb;
  }

  private Match convertMatch(RuleMatch m) {
    // TODO: handling for conversion errors with enums
    return Match.newBuilder()
      .setOffset(m.getFromPos())
      .setLength(m.getToPos() - m.getFromPos())
      .setId(m.getSpecificRuleId())
      .setSubId(nullAsEmpty(m.getRule().getSubId()))
      .addAllSuggestedReplacements(m.getSuggestedReplacementObjects().stream()
        .map(this::convertSuggestedReplacement).collect(Collectors.toList()))
      .setRuleDescription(nullAsEmpty(m.getRule().getDescription()))
      .setMatchDescription(nullAsEmpty(m.getMessage()))
      .setMatchShortDescription(nullAsEmpty(m.getShortMessage()))
      .setUrl((m.getUrl() != null ? m.getUrl().toString() : ""))
      .setAutoCorrect(m.isAutoCorrect())
      .setType(Match.MatchType.valueOf(m.getType().name()))
      .setContextForSureMatch(m.getRule().estimateContextForSureMatch())
      .setRule(MLServerProto.Rule.newBuilder()
        .setSourceFile(nullAsEmpty(m.getRule().getSourceFile()))
        .setIssueType(m.getRule().getLocQualityIssueType().name())
        .setTempOff(m.getRule().isDefaultTempOff())
        .setCategory(MLServerProto.RuleCategory.newBuilder()
          .setId(m.getRule().getCategory().getId().toString())
          .setName(m.getRule().getCategory().getName())
          .build())
        .setIsPremium(m.getRule().isPremium())
        .addAllTags(m.getRule().getTags().stream()
          .map(t -> MLServerProto.Rule.Tag.valueOf(t.name()))
          .collect(Collectors.toList()))
        .build()
      ).build();
  }

  private RuleMatch convertMatch(Match m, AnalyzedSentence s) {
    Rule rule = new RuleData(m);
    RuleMatch r = new RuleMatch(rule, s, m.getOffset(), m.getOffset() + m.getLength(), m.getMatchDescription(), m.getMatchShortDescription());

    r.setSuggestedReplacementObjects(m.getSuggestedReplacementsList().stream()
      .map(this::convertSuggestedReplacement).collect(Collectors.toList()));
    r.setAutoCorrect(m.getAutoCorrect());
    r.setType(RuleMatch.Type.valueOf(m.getType().name()));

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
    System.out.println("Sending " + req);
    // TODO: circuitBreaker
    MatchResponse response = stub.process(req);
    System.out.println("Received " + response);
    List<RuleMatch> result = new ArrayList<>(response.getSentenceMatchesCount());
    for (int i = 0; i < response.getSentenceMatchesCount(); i++) {
      result.add(convertMatch(response.getSentenceMatches(i).getMatches(0), ruleMatches.get(i).getSentence()));
    }
    System.out.println("RESORTING OUT: " + result.stream().map(RuleMatch::getSuggestedReplacements).collect(Collectors.toList()));
    return result;
  }
}
