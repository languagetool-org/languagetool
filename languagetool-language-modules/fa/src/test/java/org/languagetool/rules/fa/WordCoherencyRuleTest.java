/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Ebrahim Byagowi <ebrahim@gnu.org>
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
package org.languagetool.rules.fa;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Persian;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRuleTest;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class WordCoherencyRuleTest extends PatternRuleTest {

  private JLanguageTool langTool;
  private Rule rule;

  @Before
  public void setUp() throws Exception {
    langTool = new JLanguageTool(new Persian());
    rule = new WordCoherencyRule(TestTools.getMessages("fa"));
  }

  @Test
  public void testRules() throws IOException {
    RuleMatch[] matches;

    matches = rule.match(langTool.getAnalyzedSentence("این یک اتاق است."));
    assertEquals(0, matches.length);

    matches = rule.match(langTool.getAnalyzedSentence("این یک اطاق است."));
    assertEquals(1, matches.length);
  }

}
