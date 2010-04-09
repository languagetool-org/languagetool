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

package de.danielnaber.languagetool.rules.pl;

import java.io.IOException;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.rules.RuleMatch;
import junit.framework.TestCase;

public class PolishUnpairedBracketsRuleTest extends TestCase {

  public void testRulePolish() throws IOException {
    PolishUnpairedBracketsRule rule = new PolishUnpairedBracketsRule(TestTools
        .getEnglishMessages(), Language.POLISH);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.POLISH);
    // correct sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("(To jest zdanie do testowania)."));
    assertEquals(0, matches.length);
    // correct sentences:
    matches = rule
        .match(langTool
            .getAnalyzedSentence("Piosenka ta trafiła na wiele list \"Best of...\", włączając w to te, które zostały utworzone przez magazyn Rolling Stone."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("A \"B\" C."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("\"A\" B \"C\"."));
    assertEquals(0, matches.length);
    // incorrect sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("W tym zdaniu jest niesparowany „cudzysłów."));
    assertEquals(1, matches.length);
  }
  
}
