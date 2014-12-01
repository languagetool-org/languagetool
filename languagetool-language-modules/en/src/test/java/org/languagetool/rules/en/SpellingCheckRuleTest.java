/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.en;

import junit.framework.TestCase;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

public class SpellingCheckRuleTest extends TestCase {

  public void testIgnoreSuggestionsWithMorfologik() throws IOException {
    final JLanguageTool langTool = new JLanguageTool(new AmericanEnglish());

    final List<RuleMatch> matches = langTool.check("This is anArtificialTestWordForLanguageTool.");
    assertEquals(0, matches.size());   // no error, as this word is in ignore.txt

    final List<RuleMatch> matches2 = langTool.check("This is a real typoh.");
    assertEquals(1, matches2.size());
    assertEquals("MORFOLOGIK_RULE_EN_US", matches2.get(0).getRule().getId());
  }

}
