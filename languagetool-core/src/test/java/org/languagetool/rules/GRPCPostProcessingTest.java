package org.languagetool.rules;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Tag;
import org.languagetool.language.Demo;
import org.languagetool.rules.ml.MLServerProto;

public class GRPCPostProcessingTest {

  static class GRPCPostProcessingMock extends GRPCPostProcessing {
    static RemoteRuleConfig config = new RemoteRuleConfig();
    static {
      config.url = "localhost";
      config.port = 1234;
      config.ruleId = "TEST";
    }

    GRPCPostProcessingMock() throws Exception {
      super(config);
    }

    @Override
    protected MLServerProto.MatchResponse sendRequest(MLServerProto.PostProcessingRequest req, long timeout) throws TimeoutException {
      throw new TimeoutException("Testing timeouts");
    }
  }

  @Test
  public void testRuleMatchModification() throws Exception {
    GRPCPostProcessing instance = new GRPCPostProcessingMock();
    JLanguageTool lt = new JLanguageTool(new Demo());
    List<AnalyzedSentence> sentenceList = lt.analyzeText("This is a test. This is another sentence.");
    List<RuleMatch> matches = Arrays.asList(
      new RuleMatch(new FakeRule(), sentenceList.get(0), 0, 1, "first match"),
      new RuleMatch(new FakeRule(), sentenceList.get(1), sentenceList.get(0).getText().length()+3, sentenceList.get(0).getText().length() + 5, "second match")
    );
    List<RuleMatch> original = Arrays.asList(
      new RuleMatch(new FakeRule(), sentenceList.get(0), 0, 1, "first match"),
      new RuleMatch(new FakeRule(), sentenceList.get(1), sentenceList.get(0).getText().length()+3, sentenceList.get(0).getText().length() + 5, "second match")
    );
    List<RuleMatch> transformed = instance.filter(sentenceList, matches, 0L, false);
    assertEquals("matches are equal after postprocessing", original, transformed);
  }

  @Test
  public void testTagEnums() {
    Tag[] javaValues = Tag.values();
    MLServerProto.Rule.Tag[] protoValues = MLServerProto.Rule.Tag.values();

    assertEquals("Tags in Java and Protobuf are equal",
      Arrays.stream(javaValues).map(t -> t.name()).sorted().collect(Collectors.toList()),
      Arrays.stream(protoValues).map(t -> t.name()).filter(s -> !s.equals("UNRECOGNIZED")).sorted().collect(Collectors.toList()));
  }

  @Test
  public void testMatchTypeEnums() {
    RuleMatch.Type[] javaValues = RuleMatch.Type.values();
    MLServerProto.Match.MatchType[] protoValues = MLServerProto.Match.MatchType.values();

    assertEquals("Match types in Java and Protobuf are equal",
      Arrays.stream(javaValues).map(t -> t.name()).sorted().collect(Collectors.toList()),
      Arrays.stream(protoValues).map(t -> t.name()).filter(s -> !s.equals("UNRECOGNIZED")).sorted().collect(Collectors.toList()));
  }

  @Test
  public void testSuggestionTypeEnums() {
    SuggestedReplacement.SuggestionType[] javaValues = SuggestedReplacement.SuggestionType.values();
    MLServerProto.SuggestedReplacement.SuggestionType[] protoValues = MLServerProto.SuggestedReplacement.SuggestionType.values();

    assertEquals("Suggestion types in Java and Protobuf are equal",
      Arrays.stream(javaValues).map(t -> t.name()).sorted().collect(Collectors.toList()),
      Arrays.stream(protoValues).map(t -> t.name()).filter(s -> !s.equals("UNRECOGNIZED")).sorted().collect(Collectors.toList()));
  }
}
