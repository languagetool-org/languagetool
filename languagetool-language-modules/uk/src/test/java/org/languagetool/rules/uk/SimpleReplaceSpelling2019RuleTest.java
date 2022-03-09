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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian1992;
import org.languagetool.rules.RuleMatch;


public class SimpleReplaceSpelling2019RuleTest {

  @Test
  public void testRule() throws IOException {
    SimpleReplaceSpelling2019Rule rule = new SimpleReplaceSpelling2019Rule(TestTools.getEnglishMessages());

    RuleMatch[] matches;
    JLanguageTool lt = new JLanguageTool(new Ukrainian1992());

    // correct sentences:
    matches = rule.match(lt.getAnalyzedSentence("Це — новий проект для фойє."));
    assertEquals(0, matches.length);

    matches = rule.match(lt.getAnalyzedSentence("Це — новий проєкт для фоє."));
    assertEquals(2, matches.length);
//    assertEquals(Arrays.asList("проєкт"), matches[0].getSuggestedReplacements());
//    assertEquals(Arrays.asList("фоє"), matches[1].getSuggestedReplacements());
  }
}
