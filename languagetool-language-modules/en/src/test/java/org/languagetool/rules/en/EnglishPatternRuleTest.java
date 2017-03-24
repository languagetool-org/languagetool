/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.English;
import org.languagetool.rules.patterns.PatternRuleTest;

import java.io.IOException;

public class EnglishPatternRuleTest extends PatternRuleTest {

  @Test
  public void testRules() throws IOException {
    runGrammarRulesFromXmlTest();
  }

  // used to cause an ArrayIndexOutOfBoundsException in MatchState.setToken()
  @Test
  public void testBug() throws Exception {
    JLanguageTool langTool = new JLanguageTool(new English());
    langTool.check("Alexander between 369 and 358 BC\n\nAlexander");
  }

}
