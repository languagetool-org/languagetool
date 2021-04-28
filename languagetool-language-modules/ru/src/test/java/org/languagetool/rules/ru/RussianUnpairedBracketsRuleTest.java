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

package org.languagetool.rules.ru;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Russian;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class RussianUnpairedBracketsRuleTest {

  @Test
  public void testRuleRussian() throws IOException {
    RussianUnpairedBracketsRule rule = new RussianUnpairedBracketsRule(TestTools
        .getEnglishMessages(), new Russian());
    RuleMatch[] matches;
    JLanguageTool lt = new JLanguageTool(new Russian());
    // correct sentences:
    matches = rule.match(Collections.singletonList(lt
        .getAnalyzedSentence("(О жене и детях не беспокойся, я беру их на свои руки).")));
    assertEquals(0, matches.length);
    // correct sentences:
    matches = rule
        .match(Collections.singletonList(lt
            .getAnalyzedSentence("Позже выходит другая «южная поэма» «Бахчисарайский фонтан» (1824).")));
    assertEquals(0, matches.length);
    matches = rule.match(Collections.singletonList(lt.getAnalyzedSentence("А \"б\" Д.")));
    assertEquals(0, matches.length);
    matches = rule.match(Collections.singletonList(lt.getAnalyzedSentence("а), б), Д)..., ДД), аа) и 1а)")));
    assertEquals(0, matches.length);
    // incorrect sentences:
    matches = rule.match(Collections.singletonList(lt
        .getAnalyzedSentence("В таком ключе был начат в мае 1823 в Кишинёве роман в стихах 'Евгений Онегин.")));
    assertEquals(1, matches.length);
  }
  
}
