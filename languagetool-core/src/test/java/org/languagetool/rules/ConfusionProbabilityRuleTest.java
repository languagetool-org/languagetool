/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.TestTools;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.MorfologikLanguageModel;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConfusionProbabilityRuleTest {
  
  @Test
  public void testRule() throws IOException, ClassNotFoundException {
    File languageModelFile = new File("src/test/resources/org/languagetool/languagemodel/frequency.dict");
    LanguageModel languageModel = new MorfologikLanguageModel(languageModelFile);
    ConfusionProbabilityRule rule = new ConfusionProbabilityRule(TestTools.getEnglishMessages(), languageModel) {
      @Override public String getDescription() { return null; }
    };
    ConfusionProbabilityRule.ConfusionSet confusionSet = new ConfusionProbabilityRule.ConfusionSet("a", "an");
    AnalyzedTokenReadings[] tokens = {
            reading("is"),
            reading("an"),
            reading("café"),
            reading("in"),
            reading("Berlin")
    };
    String alternative = rule.getBetterAlternativeOrNull(tokens, 1, confusionSet);
    assertThat(alternative, is("a"));
  }

  private AnalyzedTokenReadings reading(String token) {
    return new AnalyzedTokenReadings(new AnalyzedToken(token, null, null), 0);
  }
}
