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
    MixedAlphabetsRule rule = new MixedAlphabetsRule(TestTools.getMessages("uk"));
    JLanguageTool lt = new JLanguageTool(new Ukrainian());

    // correct sentences:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("сміття")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("not mixed")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("123454")).length);

    assertEquals(0, rule.match(lt.getAnalyzedSentence("x = a якщо")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("x − a та y − b")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("записати x та y через параметр t")).length);

    assertEquals(0, rule.match(lt.getAnalyzedSentence("ЛЮДИНИ І НАЦІЇ")).length);
    
    //incorrect sentences:

    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("смiття"));  //latin i
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні й латинські літери в одному слові", matches[0].getMessage());
    assertEquals(Arrays.asList("сміття"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("mіхed"));  // cyrillic i and x
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні літери замість латинських", matches[0].getMessage());
    assertEquals(Arrays.asList("mixed"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("горíти"));  // umlaut instead of accented і
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні й латинські літери в одному слові", matches[0].getMessage());
    assertEquals(Arrays.asList("горі́ти"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("двоáктний")); // lating á 
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні й латинські літери в одному слові", matches[0].getMessage());

    matches = rule.match(lt.getAnalyzedSentence("Чорного i Азовського"));  // latin i
    assertEquals(1, matches.length);
    assertEquals("Вжито латинську «i» замість кириличної", matches[0].getMessage());
    assertEquals(Arrays.asList("і"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("A нема"));  // latin A
    assertEquals(1, matches.length);
    assertEquals("Вжито латинську «A» замість кириличної", matches[0].getMessage());
    assertEquals(Arrays.asList("А"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("Петро І")); // cyrillic І
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличну літеру замість латинської", matches[0].getMessage());
    assertEquals("I", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("сибірську колекцію Петра І.\n\n Всім")); // cyrillic І
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличну літеру замість латинської", matches[0].getMessage());
    assertEquals("I.", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("Миколая І.")); // cyrillic І
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличну літеру замість латинської", matches[0].getMessage());
    assertEquals("I.", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("У І кварталі")); // cyrillic І
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличну літеру замість латинської", matches[0].getMessage());
    assertEquals("I", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("ЗА І ПРОТИ")); // cyrillic І
    assertEquals(0, matches.length);

    // ambiguous without semantics:
    // російський хемік Александр І. Опарін (1894–1980)
    
    matches = rule.match(lt.getAnalyzedSentence("Ленін В. І."));
    assertEquals(0, matches.length);

    matches = rule.match(lt.getAnalyzedSentence("Тому І.    Вишенський радить "));
    assertEquals(0, matches.length);

    matches = rule.match(lt.getAnalyzedSentence("у І ст.")); // cyrillic І
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличну літеру замість латинської", matches[0].getMessage());
    assertEquals("I", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("XІ")); // cyrillic І and latin X
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні літери замість латинських", matches[0].getMessage());
    assertEquals(Arrays.asList("XI"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("ХI")); // cyrillic X and latin I
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні літери замість латинських", matches[0].getMessage());
    assertEquals(Arrays.asList("XI"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("VIIІ-го")); // latin VII and cyrillic І
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні літери замість латинських. Також: до римських цифр букви не дописуються.", matches[0].getMessage());
    assertEquals(Arrays.asList("VIII"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("ІІІ-го")); // cyrillic І
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні літери замість латинських на позначення римської цифри. Також: до римських цифр букви не дописуються.", matches[0].getMessage());
    assertEquals(Arrays.asList("III"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("ХІ")); // cyrillic both X and I used for latin number
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні літери замість латинських на позначення римської цифри", matches[0].getMessage());
    assertEquals(Arrays.asList("XI"), matches[0].getSuggestedReplacements());

    // we split tokens for latin numbers with \u2013 inside now
//    matches = rule.match(lt.getAnalyzedSentence("ХI\u2013XX")); // cyrillic X and latin I
//    assertEquals(1, matches.length);
//    assertEquals("Вжито кириличні літери замість латинських", matches[0].getMessage());
//    assertEquals(Arrays.asList("ХІ–ХХ", "XI–XX"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("СOVID-19")); // cyrillic С
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні літери замість латинських", matches[0].getMessage());
    assertEquals(Arrays.asList("COVID-19"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("австрo-турецької")); // cyrillic both X and I used for latin number
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличні й латинські літери в одному слові", matches[0].getMessage());
    assertEquals(Arrays.asList("австро-турецької"), matches[0].getSuggestedReplacements());
    
    
    matches = rule.match(lt.getAnalyzedSentence("Щеплення від гепатиту В.")); // cyrillic B
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличну літеру замість латинської", matches[0].getMessage());
    assertEquals("B", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("група А")); // cyrillic А
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличну літеру замість латинської", matches[0].getMessage());
    assertEquals("A", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("На 0,6°С.")); // cyrillic С
    assertEquals(1, matches.length);
    assertEquals("Вжито кириличну літеру замість латинської", matches[0].getMessage());
    assertEquals("C", matches[0].getSuggestedReplacements().get(0));
  }

  @Test
  public void testCombiningChars() throws IOException {
    MixedAlphabetsRule rule = new MixedAlphabetsRule(TestTools.getMessages("uk"));
    JLanguageTool lt = new JLanguageTool(new Ukrainian());

    // й and ї are done via combining characters: и + U+0306, ї + U+308
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("Білоруський - українці"));
    assertEquals(2, matches.length);
    assertEquals("Білоруський", matches[0].getSuggestedReplacements().get(0));
    assertEquals("українці", matches[1].getSuggestedReplacements().get(0));
  }
  
}
