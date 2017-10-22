package org.languagetool.dev.bigdata;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.languagetool.dev.bigdata.NeuralNetworkRuleEvaluator.confusionSetConfig;

public class NeuralNetworkRuleEvaluatorTest {
  @Test
  public void testConfusionSetConfig() throws Exception {
    Map<Double, NeuralNetworkRuleEvaluator.EvalResult> evaluationResults = new HashMap<>();
    evaluationResults.put(0.5, new NeuralNetworkRuleEvaluator.EvalResult("summary 1", .7f, .8f));
    evaluationResults.put(1.0, new NeuralNetworkRuleEvaluator.EvalResult("summary 2", .99f, .7f));
    evaluationResults.put(1.5, new NeuralNetworkRuleEvaluator.EvalResult("summary 3", .998f, .5f));

    assertThat(confusionSetConfig(evaluationResults, .9f), is("summary 2"));
    assertThat(confusionSetConfig(evaluationResults, .99f), is("summary 2"));
    assertThat(confusionSetConfig(evaluationResults, .999f), is("###"));
  }

}