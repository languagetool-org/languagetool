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
package org.languagetool.rules.uk;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;

import static org.junit.Assert.assertEquals;

public class UkrainianWordRepeatRuleTest {
  
  private JLanguageTool lt;
  private UkrainianWordRepeatRule rule;

  @Before
  public void setUp() throws IOException {
    lt = new JLanguageTool(new Ukrainian());
    rule = new UkrainianWordRepeatRule(TestTools.getMessages("uk"), lt.getLanguage());
  }
  
  @Test
  public void testRule() throws IOException {
    assertEmptyMatch("без повного розрахунку");
    assertEmptyMatch("без бугіма бугіма");
    assertEmptyMatch("без 100 100");
    assertEmptyMatch("1.30 3.20 3.20");
    assertEmptyMatch("ще в В.Кандинського");
    assertEmptyMatch("Від добра добра не шукають.");
    assertEmptyMatch("Що, що, а кіно в Україні...");
    assertEmptyMatch("Відповідно до ст. ст. 3, 7, 18.");
    assertEmptyMatch("Не можу сказати ні так, ні ні.");

    assertEquals(1, rule.match(lt.getAnalyzedSentence("без без повного розрахунку")).length);
    RuleMatch[] match = rule.match(lt.getAnalyzedSentence("Верховної Ради І і ІІ скликань"));
    assertEquals(1, match.length);
    assertEquals(2, match[0].getSuggestedReplacements().size());
  }

  private void assertEmptyMatch(String text) throws IOException {
    assertEquals(text, Collections.<RuleMatch>emptyList(), Arrays.asList(rule.match(lt.getAnalyzedSentence(text))));
  }

}
