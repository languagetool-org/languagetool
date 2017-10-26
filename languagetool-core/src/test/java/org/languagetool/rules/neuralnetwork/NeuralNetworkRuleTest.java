package org.languagetool.rules.neuralnetwork;

import org.junit.BeforeClass;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.ScoredConfusionSet;
import org.languagetool.rules.ScoredConfusionSetLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NeuralNetworkRuleTest {

  private static List<ScoredConfusionSet> confusionSets;
  private static Language language;
  private static JLanguageTool lt;

  @BeforeClass
  public static void setUp() throws IOException {
    InputStream confusionSetStream = new ByteArrayInputStream("foo|lorem;bar|ipsum;0.8#blabla\nfizz;buzz;0.8".getBytes(StandardCharsets.UTF_8.name()));
    confusionSets = ScoredConfusionSetLoader.loadConfusionSet(confusionSetStream);

    language = TestTools.getDemoLanguage();
    lt = new JLanguageTool(language);
  }

  @Test
  public void testNeuralNetworkRule() throws IOException {
    NeuralNetworkRule neuralNetworkRule = new NeuralNetworkRule(TestTools.getEnglishMessages(), language, confusionSets.get(0), context -> new double[]{-0.9, 1.9});

    assertThat(neuralNetworkRule.getId(), is("XX_foo_VS_bar_NEURALNETWORK"));

    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("We go to the foo tomorrow.");
    RuleMatch[] ruleMatches = neuralNetworkRule.match(analyzedSentence);
    assertThat(ruleMatches.length, is(1));
    assertThat(ruleMatches[0].getMessage(), is("Our neural network thinks that 'bar' (ipsum) might be the correct word here, not 'foo' (lorem). Please check. [-0.90, 1.90]"));
    assertThat(ruleMatches[0].getSuggestedReplacements().get(0), is("bar"));
  }

  @Test
  public void testSuggestionTextRule() throws IOException {
    NeuralNetworkRule neuralNetworkRule = new NeuralNetworkRule(TestTools.getEnglishMessages(), language, confusionSets.get(0), context -> new double[]{0.9, -1.9});

    assertThat(neuralNetworkRule.getId(), is("XX_foo_VS_bar_NEURALNETWORK"));

    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("We go to the bar tomorrow.");
    RuleMatch[] ruleMatches = neuralNetworkRule.match(analyzedSentence);
    assertThat(ruleMatches.length, is(1));
    assertThat(ruleMatches[0].getMessage(), is("Our neural network thinks that 'foo' (lorem) might be the correct word here, not 'bar' (ipsum). Please check. [0.90, -1.90]"));
    assertThat(ruleMatches[0].getSuggestedReplacements().get(0), is("foo"));
  }

  @Test
  public void testSuggestionTextWithoutDescription() throws IOException {
    NeuralNetworkRule neuralNetworkRule = new NeuralNetworkRule(TestTools.getEnglishMessages(), language, confusionSets.get(1), context -> new double[]{0.9, -1.9});

    assertThat(neuralNetworkRule.getId(), is("XX_fizz_VS_buzz_NEURALNETWORK"));

    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("We go to the buzz tomorrow.");
    RuleMatch[] ruleMatches = neuralNetworkRule.match(analyzedSentence);
    assertThat(ruleMatches.length, is(1));
    assertThat(ruleMatches[0].getMessage(), is("Our neural network thinks that 'fizz' might be the correct word here, not 'buzz'. Please check. [0.90, -1.90]"));
    assertThat(ruleMatches[0].getSuggestedReplacements().get(0), is("fizz"));
  }

}