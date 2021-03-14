/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Andriy Rysin
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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class MixedAlphabetsRuleTest {

  @Test
  public void testRule() throws IOException {
    final MixedAlphabetsRule rule = new MixedAlphabetsRule(TestTools.getMessages("uk"));
    final JLanguageTool langTool = new JLanguageTool(new Ukrainian());

    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("сміття")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("not mixed")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("123454")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("x = a якщо")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("x − a та y − b")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("записати x та y через параметр t")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("ЛЮДИНИ І НАЦІЇ")).length);
    
    //incorrect sentences:

    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("смiття"));  //latin i
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні й латинські літери в одному слові", matches[0].getMessage());
    assertEquals(Arrays.asList("сміття"), matches[0].getSuggestedReplacements());

    matches = rule.match(langTool.getAnalyzedSentence("mіхed"));  // cyrillic i and x
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні літери замість латинських", matches[0].getMessage());
    assertEquals(Arrays.asList("mixed"), matches[0].getSuggestedReplacements());

    matches = rule.match(langTool.getAnalyzedSentence("горíти"));  // umlaut instead of accented і
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні й латинські літери в одному слові", matches[0].getMessage());
    assertEquals(Arrays.asList("горі́ти"), matches[0].getSuggestedReplacements());

    matches = rule.match(langTool.getAnalyzedSentence("двоáктний")); // lating á 
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні й латинські літери в одному слові", matches[0].getMessage());

    matches = rule.match(langTool.getAnalyzedSentence("Чорного i Азовського"));  // latin i
    assertEquals(1, matches.length);
    assertEquals("Вжито латинську «i» замість кириличної", matches[0].getMessage());
    assertEquals(Arrays.asList("і"), matches[0].getSuggestedReplacements());

    matches = rule.match(langTool.getAnalyzedSentence("A нема"));  // latin A
    assertEquals(1, matches.length);
    assertEquals("Вжито латинську «A» замість кириличної", matches[0].getMessage());
    assertEquals(Arrays.asList("А"), matches[0].getSuggestedReplacements());

    matches = rule.match(langTool.getAnalyzedSentence("Петро І")); // cyrillic І
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличну літеру замість латинської", matches[0].getMessage());
    assertEquals("I", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(langTool.getAnalyzedSentence("у І ст.")); // cyrillic І
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличну літеру замість латинської", matches[0].getMessage());
    assertEquals("I", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(langTool.getAnalyzedSentence("XІ")); // cyrillic І and latin X
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні літери замість латинських", matches[0].getMessage());
    assertEquals(Arrays.asList("XI"), matches[0].getSuggestedReplacements());

    matches = rule.match(langTool.getAnalyzedSentence("ХI")); // cyrillic X and latin I
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні літери замість латинських", matches[0].getMessage());
    assertEquals(Arrays.asList("XI"), matches[0].getSuggestedReplacements());

    matches = rule.match(langTool.getAnalyzedSentence("VIIІ-го")); // latin VII and cyrillic І
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні літери замість латинських. Також: до римських цифр букви не дописуються.", matches[0].getMessage());
    assertEquals(Arrays.asList("VIII"), matches[0].getSuggestedReplacements());

    matches = rule.match(langTool.getAnalyzedSentence("ІІІ-го")); // cyrillic І
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні літери замість латинських на позначення римської цифри. Також: до римських цифр букви не дописуються.", matches[0].getMessage());
    assertEquals(Arrays.asList("III"), matches[0].getSuggestedReplacements());

    matches = rule.match(langTool.getAnalyzedSentence("ХІ")); // cyrillic both X and I used for latin number
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні літери замість латинських на позначення римської цифри", matches[0].getMessage());
    assertEquals(Arrays.asList("XI"), matches[0].getSuggestedReplacements());

    matches = rule.match(langTool.getAnalyzedSentence("СOVID-19")); // cyrillic С
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні літери замість латинських", matches[0].getMessage());
    assertEquals(Arrays.asList("COVID-19"), matches[0].getSuggestedReplacements());

    matches = rule.match(langTool.getAnalyzedSentence("австрo-турецької")); // cyrillic both X and I used for latin number
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні й латинські літери в одному слові", matches[0].getMessage());
    assertEquals(Arrays.asList("австро-турецької"), matches[0].getSuggestedReplacements());
    
    
    matches = rule.match(langTool.getAnalyzedSentence("Щеплення від гепатиту В.")); // cyrillic B
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличну літеру замість латинської", matches[0].getMessage());
    assertEquals("B", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(langTool.getAnalyzedSentence("група А")); // cyrillic А
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличну літеру замість латинської", matches[0].getMessage());
    assertEquals("A", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(langTool.getAnalyzedSentence("На 0,6°С.")); // cyrillic С
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличну літеру замість латинської", matches[0].getMessage());
    assertEquals("0,6°C", matches[0].getSuggestedReplacements().get(0));



    
  }

}
