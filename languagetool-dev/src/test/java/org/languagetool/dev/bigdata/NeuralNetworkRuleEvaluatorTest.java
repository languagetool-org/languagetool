/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Markus Brenneis
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
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