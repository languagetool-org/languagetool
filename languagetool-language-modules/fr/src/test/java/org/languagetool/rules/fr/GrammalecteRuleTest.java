/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.fr;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.GlobalConfig;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

public class GrammalecteRuleTest {

  @Test
  @Ignore("only works with Grammalecte running")
  public void testMatch() throws IOException {
    GlobalConfig cfg = new GlobalConfig();
    cfg.setGrammalecteServer("http://localhost:8080/gc_text/fr");
    GrammalecteRule rule = new GrammalecteRule(JLanguageTool.getMessageBundle(), cfg);
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("fr"));

    String text = "Elles sont aveugle.";
    String expectedMessage = "Accord avec le sujet “Elles” : “aveugle” devrait être au féminin pluriel.";

    AnalyzedSentence sentence = lt.getAnalyzedSentence(text);
    RuleMatch[] matches = rule.match(sentence);

    assertThat(matches.length, is(1));
    assertThat(matches[0].getMessage(), startsWith(expectedMessage));
  }

}
