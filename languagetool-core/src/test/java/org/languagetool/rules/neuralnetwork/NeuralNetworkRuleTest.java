package org.languagetool.rules.neuralnetwork;

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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NeuralNetworkRuleTest {

  @Test
  public void testNeuralNetworkRule() throws IOException {
    InputStream confusionSetStream = new ByteArrayInputStream("foo|lorem;bar|ipsum;0.8;#blabla".getBytes(StandardCharsets.UTF_8.name()));
    ScoredConfusionSet confusionSet = ScoredConfusionSetLoader.loadConfusionSet(confusionSetStream).get(0);
    Language language = TestTools.getDemoLanguage();
    JLanguageTool lt = new JLanguageTool(language);
    NeuralNetworkRule neuralNetworkRule = new NeuralNetworkRule(TestTools.getEnglishMessages(), language, confusionSet, context -> new double[]{-0.9, 1.9});

    assertThat(neuralNetworkRule.getId(), is("XX_FOO_VS_BAR_NEURALNETWORK"));
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("We go to the foo tomorrow.");
    RuleMatch[] ruleMatches = neuralNetworkRule.match(analyzedSentence);
    assertThat(ruleMatches.length, is(1));
    assertThat(ruleMatches[0].getMessage(), is("Our neural network thinks that 'foo' (lorem) might be the correct word here, not 'bar' (ipsum). Please check. [-0,90, 1,90]"));
    assertThat(ruleMatches[0].getSuggestedReplacements().get(0), is("bar"));
  }

}