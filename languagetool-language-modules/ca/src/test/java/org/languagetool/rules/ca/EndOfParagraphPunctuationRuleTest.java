/* LanguageTool, a natural language style checker
 * Copyright (C) 2024 Jaume Ortolà
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
package org.languagetool.rules.ca;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class EndOfParagraphPunctuationRuleTest {

  private JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("ca"));
  private TextLevelRule rule = new EndOfParagraphPunctuationRule(TestTools.getMessages("ca"));

  @Test
  public void testRule() throws IOException {
    RuleMatch[] matches = rule.match(lt.analyzeText("Això és un paràgraf amb una frase només"));
    assertEquals(0, matches.length);

    matches = rule.match(lt.analyzeText("Això és un paràgraf amb una frase només,"));
    assertEquals(0, matches.length);

    matches = rule.match(lt.analyzeText("Això és un paràgraf amb una frase només. Això és la segona frase"));
    assertEquals(1, matches.length);
    assertEquals("frase.", matches[0].getSuggestedReplacements().get(0));
    assertEquals(59, matches[0].getFromPos());

    matches = rule.match(lt.analyzeText("Això és un paràgraf amb una frase només. Això és la segona frase,"));
    assertEquals(1, matches.length);
    assertEquals(".", matches[0].getSuggestedReplacements().get(0));
    assertEquals(64, matches[0].getFromPos());

    matches = rule.match(lt.analyzeText("Això és un paràgraf amb una frase només.\\n"
    +"Això és una única frase en un paràgraf"));
    assertEquals(0, matches.length);

  }

}
