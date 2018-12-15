/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;

public class SimilarNameRuleTest {

  @Test
  public void testRule() throws IOException {
    SimilarNameRule rule = new SimilarNameRule(TestTools.getEnglishMessages());
    JLanguageTool lt = new JLanguageTool(new GermanyGerman());
    assertErrors("Hier steht Angela Müller. Im nächsten Satz dann Miller.", 1, rule, lt);
    assertErrors("Hier steht Angela Müller. Im nächsten Satz dann Müllers Ehemann.", 0, rule, lt);
    assertErrors("Hier steht Angela Müller. Dann Mulla, nicht ähnlich genug.", 0, rule, lt);
    assertErrors("Ein Mikrocontroller, bei Mikrocontrollern", 0, rule, lt);
    assertErrors("Hier steht das Rad Deiner Freundin. Und Deinem Hund geht es gut?", 0, rule, lt);
  }

  private void assertErrors(String input, int expectedMatches, SimilarNameRule rule, JLanguageTool lt) throws IOException {
    AnalyzedSentence sentence = lt.getAnalyzedSentence(input);
    RuleMatch[] matches = rule.match(Collections.singletonList(sentence));
    assertThat(matches.length, is(expectedMatches));
    if (expectedMatches == 1) {
      assertThat(matches[0].getRule().getId(), is("DE_SIMILAR_NAMES"));
    }
  }

}
