package org.languagetool.rules;

import static org.languagetool.rules.GRPCRule.Connection.getManagedChannel;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import javax.net.ssl.SSLException;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.Premium;
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


  class RuleData extends Rule {
    private final Match m;
    private final String sourceFile;

    RuleData(Match m) {
      this.m = m;
      this.sourceFile = m.getRule().getSourceFile();
      // keep default values from Rule baseclass
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
      return emptyAsNull(sourceFile);
    }

    @Override
    public String getId() {
      return m.getId();
    }

    @Override
    public String getSubId() {
     return emptyAsNull(m.getSubId());
    }

    @Override
    public String getDescription() {
      return m.getRuleDescription();
    }

    @Override
    public int estimateContextForSureMatch() {
      // 0 is okay as default value
      return m.getContextForSureMatch();
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
      throw new UnsupportedOperationException("Not implemented; internal class used for returning match" +
        " information from remote endpoint");
    }
  }

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
    // could add better handling for conversion errors with enums
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
        .setIsPremium(Premium.get().isPremiumRule(m.getRule()))
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
      matches.add(MatchList.newBuilder().addAllMatches(
        sentenceMatches.stream().map(this::convertMatch).collect(Collectors.toList())).build());
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
          RuleMatch match = convertMatch(matchList.getMatches(j), sentence);
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
