/* LanguageTool, a natural language style checker 
 * Copyright (C) 2010 Daniel Naber (http://www.languagetool.org)
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

package de.danielnaber.languagetool.rules.ru;

import java.io.IOException;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.rules.RuleMatch;
import junit.framework.TestCase;

public class RussianUnpairedBracketsRuleTest extends TestCase {

  public void testRulePolish() throws IOException {
    RussianUnpairedBracketsRule rule = new RussianUnpairedBracketsRule(TestTools
        .getEnglishMessages(), Language.RUSSIAN);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.RUSSIAN);
    // correct sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("(О жене и детях не беспокойся, я беру их на свои руки)."));
    assertEquals(0, matches.length);
    // correct sentences:
    matches = rule
        .match(langTool
            .getAnalyzedSentence("Позже выходит другая «южная поэма» «Бахчисарайский фонтан» (1824)."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("А \"б\" Д."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("а), б), Д)..., ДД), аа) и 1а)"));
    assertEquals(0, matches.length);
    // incorrect sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("В таком ключе был начат в мае 1823 в Кишинёве роман в стихах «Евгений Онегин."));
    assertEquals(1, matches.length);
  }
  
}
