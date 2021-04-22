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

package org.languagetool.rules.pl;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Polish;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class PolishUnpairedBracketsRuleTest {

  @Test
  public void testRulePolish() throws IOException {
    Polish language = new Polish();
    PolishUnpairedBracketsRule rule = new PolishUnpairedBracketsRule(TestTools.getEnglishMessages(), language);
    JLanguageTool lt = new JLanguageTool(language);

    assertEquals(0, getMatches("(To jest zdanie do testowania).", rule, lt));
    assertEquals(0, getMatches("Piosenka ta trafiła na wiele list \"Best of...\", włączając w to te, które zostały utworzone przez magazyn Rolling Stone.", rule, lt));
    assertEquals(0, getMatches("A \"B\" C.", rule, lt));
    assertEquals(0, getMatches("\"A\" B \"C\".", rule, lt));

    assertEquals(1, getMatches("W tym zdaniu jest niesparowany „cudzysłów.", rule, lt));
  }

  private int getMatches(String input, PolishUnpairedBracketsRule rule, JLanguageTool lt) throws IOException {
    return rule.match(Collections.singletonList(lt.getAnalyzedSentence(input))).length;
  }

}
