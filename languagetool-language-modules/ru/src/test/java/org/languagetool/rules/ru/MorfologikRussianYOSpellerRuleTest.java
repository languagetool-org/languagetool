/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class MorfologikRussianYOSpellerRuleTest {

  @Test
  public void testMorfologikSpeller() throws IOException {
    MorfologikRussianYOSpellerRule rule =
      new MorfologikRussianYOSpellerRule(TestTools.getMessages("ru"), new Russian(), null, Collections.emptyList());

    JLanguageTool lt = new JLanguageTool(new Russian());

    // correct word
    assertEquals(0, rule.match(lt.getAnalyzedSentence("русский")).length);

    // correct word
    assertEquals(0, rule.match(lt.getAnalyzedSentence("ёжик")).length);

    // incorrect word 
    assertEquals(1, rule.match(lt.getAnalyzedSentence("ежик")).length);

    // incorrect word with hyphen
    assertEquals(1, rule.match(lt.getAnalyzedSentence("юго-зпдный")).length);

    // correct word with hyphen
    assertEquals(0, rule.match(lt.getAnalyzedSentence("северо-восточный")).length);


    // correct word with hyphen
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Ростов-на-Дону")).length);

    // incorrect word with hyphen
    //   assertEquals(1, rule.match(langTool.getAnalyzedSentence("Ростов-на-дону")).length);  //mistake in dict

  }
}
