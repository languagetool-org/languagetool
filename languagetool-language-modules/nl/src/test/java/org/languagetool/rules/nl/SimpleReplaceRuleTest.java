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

package org.languagetool.rules.nl;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Dutch;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SimpleReplaceRuleTest {

  private SimpleReplaceRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws Exception {
    rule = new SimpleReplaceRule(TestTools.getMessages("nl"));
    lt = new JLanguageTool(new Dutch());
  }

  @Test
  public void testRule() throws IOException {
    // correct sentences:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("all right")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("De Kudde eigenschappen")).length);  // no match b/c case-sensitivity
    // edit by R. Baars 19-11-2022 to make routine case sensitive
    // incorrect sentences:
    checkSimpleReplaceRule("klaa", "klaar");

    //checkSimpleReplaceRule("Kudde eigenschappen.", "Kudde-eigenschappen");
    checkSimpleReplaceRule("een BTW nummer", "btw-nummer");
    checkSimpleReplaceRule("kleurweergave eigenschappen.", "kleurweergave-eigenschappen");
    checkSimpleReplaceRule("De kleurweergave eigenschappen.", "kleurweergave-eigenschappen");
  }
  private void checkSimpleReplaceRule(String sentence, String suggestion) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals("Invalid matches.length while checking sentence: " + sentence, 1, matches.length);
    assertEquals("Invalid replacement count wile checking sentence: "
        + sentence, 1, matches[0].getSuggestedReplacements().size());
    assertEquals("Invalid suggested replacement while checking sentence: "
        + sentence, suggestion, matches[0].getSuggestedReplacements().get(0));
  }
}
