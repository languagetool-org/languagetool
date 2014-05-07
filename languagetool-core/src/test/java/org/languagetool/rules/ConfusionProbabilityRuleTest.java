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

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConfusionProbabilityRuleTest {
  
  @Test
  public void testRule() throws IOException, ClassNotFoundException {
    ConfusionProbabilityRule rule = new ConfusionProbabilityRule(TestTools.getEnglishMessages());
    ConfusionProbabilityRule.ConfusionSet confusionSet = new ConfusionProbabilityRule.ConfusionSet("portrait", "portray");
    AnalyzedTokenReadings[] tokens = {
            reading("A"),
            reading("portray"),
            reading("of"),
            reading("me")
    };
    String alternative = rule.getBetterAlternativeOrNull(tokens, 1, confusionSet);
    assertThat(alternative, is("portrait"));
  }

  private AnalyzedTokenReadings reading(String token) {
    return new AnalyzedTokenReadings(new AnalyzedToken(token, null, null), 0);
  }
}
