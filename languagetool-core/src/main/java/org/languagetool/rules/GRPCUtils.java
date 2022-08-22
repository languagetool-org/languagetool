package org.languagetool.rules;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.rules.ml.MLServerProto;
import org.languagetool.rules.ml.MLServerProto.Match;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class GRPCUtils
{

  private static class RuleData extends Rule {
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
      throw new UnsupportedOperationException(
          "Not implemented; internal class used for returning match" + " information from remote endpoint");
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


  public static MLServerProto.AnalyzedToken toGRPC(AnalyzedToken token) {
      MLServerProto.AnalyzedToken.Builder t = MLServerProto.AnalyzedToken.newBuilder();
      if (token.getLemma() != null) {
        t.setLemma(token.getLemma());
      }
      if (token.getPOSTag() != null) {
        t.setPosTag(token.getPOSTag());
      }
      t.setToken(token.getToken());
      return t.build();
  }

  public static MLServerProto.AnalyzedTokenReadings toGRPC(AnalyzedTokenReadings readings) {
    return MLServerProto.AnalyzedTokenReadings.newBuilder()
      .addAllChunkTags(readings.getChunkTags().stream().map(ChunkTag::getChunkTag).collect(Collectors.toList()))
      .addAllReadings(readings.getReadings().stream().map(GRPCUtils::toGRPC).collect(Collectors.toList()))
      .build();

  }

  public static MLServerProto.AnalyzedSentence toGRPC(AnalyzedSentence sentence) {
    return MLServerProto.AnalyzedSentence.newBuilder()
      .setText(sentence.getText())
      .addAllTokens(Arrays.stream(sentence.getTokens()).map(GRPCUtils::toGRPC).collect(Collectors.toList()))
      .build();
  }

  public static AnalyzedTokenReadings fromGRPC(MLServerProto.AnalyzedTokenReadings tokenReadings) {
    return new AnalyzedTokenReadings(tokenReadings.getReadingsList().stream()
                                     .map(GRPCUtils::fromGRPC).collect(Collectors.toList()),
                                     tokenReadings.getStartPos());
  }

  public static AnalyzedToken fromGRPC(MLServerProto.AnalyzedToken token) {
    return new AnalyzedToken(token.getToken(), token.getPosTag(), token.getLemma());
  }

  public static AnalyzedSentence fromGRPC(MLServerProto.AnalyzedSentence sentence) {
    return new AnalyzedSentence(sentence.getTokensList().stream()
                                .map(GRPCUtils::fromGRPC).toArray(AnalyzedTokenReadings[]::new));
  }

  public static String getUrl(RuleMatch m) {
    // URL can be attached to Rule or RuleMatch (or both); in Protobuf, only to RuleMatch
    // prefer URL from RuleMatch, default to empty string
    if (m.getUrl() != null) {
      return m.getUrl().toString();
    }
    if (m.getRule().getUrl() != null) {
      return m.getRule().getUrl().toString();
    }
    return "";
  }


  public static Match toGRPC(RuleMatch m) {
    // could add better handling for conversion errors with enums
    return Match.newBuilder()
      .setOffset(m.getFromPos())
      .setLength(m.getToPos() - m.getFromPos())
      .setId(m.getSpecificRuleId())
      .setSubId(nullAsEmpty(m.getRule().getSubId()))
      .addAllSuggestedReplacements(m.getSuggestedReplacementObjects().stream()
                                   .map(GRPCUtils::toGRPC).collect(Collectors.toList()))
      .setRuleDescription(nullAsEmpty(m.getRule().getDescription()))
      .setMatchDescription(nullAsEmpty(m.getMessage()))
      .setMatchShortDescription(nullAsEmpty(m.getShortMessage()))
      .setUrl(getUrl(m))
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

  public static RuleMatch fromGRPC(Match m, AnalyzedSentence s) {
    Rule rule = new RuleData(m);
    RuleMatch r = new RuleMatch(rule, s, m.getOffset(), m.getOffset() + m.getLength(), m.getMatchDescription(), m.getMatchShortDescription());

    r.setSuggestedReplacementObjects(m.getSuggestedReplacementsList().stream()
                                     .map(GRPCUtils::fromGRPC).collect(Collectors.toList()));
    r.setAutoCorrect(m.getAutoCorrect());
    r.setType(RuleMatch.Type.valueOf(m.getType().name()));

    if (!m.getUrl().isEmpty()) {
      try {
        r.setUrl(new URL(m.getUrl()));
      } catch (MalformedURLException e) {
        log.warn("Got invalid URL from GRPC match filter: {}", e);
      }
    }
    return r;
  }

  public static MLServerProto.SuggestedReplacement toGRPC(SuggestedReplacement s) {
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

  public static SuggestedReplacement fromGRPC(MLServerProto.SuggestedReplacement s) {
    SuggestedReplacement sb = new SuggestedReplacement(s.getReplacement(),
      emptyAsNull(s.getDescription()), emptyAsNull(s.getSuffix()));
    sb.setType(SuggestedReplacement.SuggestionType.valueOf(s.getType().name()));
    if (s.getConfidence() != 0f) {
      sb.setConfidence(s.getConfidence());
    }
    return sb;
  }

  public static JLanguageTool.Level fromGRPC(MLServerProto.ProcessingOptions.Level l) {
    if (l.equals(MLServerProto.ProcessingOptions.Level.defaultLevel)) {
      return JLanguageTool.Level.DEFAULT;
    }
    return JLanguageTool.Level.valueOf(l.name().toUpperCase());
  }

  public static MLServerProto.ProcessingOptions.Level toGRPC(JLanguageTool.Level level) {
    if (level.equals(JLanguageTool.Level.DEFAULT)) {
      return MLServerProto.ProcessingOptions.Level.defaultLevel;
    }
    return MLServerProto.ProcessingOptions.Level.valueOf(level.name().toLowerCase());
  }

}
