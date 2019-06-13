/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.rules.uk;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;


public class SimpleReplaceSpelling2019RuleTest {

  @Test
  public void testRule() throws IOException {
    SimpleReplaceSpelling1992Rule rule = new SimpleReplaceSpelling1992Rule(TestTools.getEnglishMessages());

    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Ukrainian.DEFAULT_VARIANT);

    // correct sentences:
    matches = rule.match(langTool.getAnalyzedSentence("Це — новий проєкт для фоє."));
    assertEquals(0, matches.length);

    matches = rule.match(langTool.getAnalyzedSentence("Це — новий проект для фойє."));
    assertEquals(2, matches.length);
//    assertEquals(Arrays.asList("проєкт"), matches[0].getSuggestedReplacements());
//    assertEquals(Arrays.asList("фоє"), matches[1].getSuggestedReplacements());

    matches = rule.match(langTool.getAnalyzedSentence("Топменеджер."));
    assertEquals(0, matches.length);

    matches = rule.match(langTool.getAnalyzedSentence("Топ-менеджер."));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("Топменеджер"), matches[0].getSuggestedReplacements());
  }
}
