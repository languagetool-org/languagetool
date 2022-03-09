/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski
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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;

public class MorfologikUkrainianSpellerRuleTest {
  private JLanguageTool lt;
  private MorfologikUkrainianSpellerRule rule;
  
  @Before
  public void init() throws IOException {
    rule = new MorfologikUkrainianSpellerRule (TestTools.getMessages("uk"), new Ukrainian(), null, Collections.emptyList());
    lt = new JLanguageTool(new Ukrainian());
  }

  @Test
  public void testMorfologikSpeller() throws IOException {
    // correct sentences:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("До вас прийде заввідділу!")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence(",")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("123454")).length);

    assertEquals(0, rule.match(lt.getAnalyzedSentence("До нас приїде The Beatles!")).length);

    // soft hyphen
    assertEquals(0, rule.match(lt.getAnalyzedSentence("піс\u00ADні")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("піс\u00ADні піс\u00ADні")).length);

    // non-breaking hyphen
    assertEquals(0, rule.match(lt.getAnalyzedSentence("ось\u2011ось")).length);

    // frequent infix notation
    assertEquals(0, rule.match(lt.getAnalyzedSentence("-ськ-")).length);

    assertEquals(1, rule.match(lt.getAnalyzedSentence("Халгін-Гол")).length);

    // accent
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("Іва́н Петро́вич Котляре́вський"));
    assertEquals(0, matches.length);

    matches = rule.match(lt.getAnalyzedSentence("1998 ро́ку"));
    assertEquals(0, matches.length);

    //incorrect sentences:

//    matches = rule.match(langTool.getAnalyzedSentence("атакуючий"));
//    // check match positions:
//    assertEquals(1, matches.length);
//    assertEquals(0, matches[0].getFromPos());
//    assertEquals("атакуючий".length(), matches[0].getToPos());

//    matches = rule.match(langTool.getAnalyzedSentence("шклянка"));
//
//    assertEquals(1, matches.length);
//    assertEquals("склянка", matches[0].getSuggestedReplacements().get(0));

    assertEquals(0, rule.match(lt.getAnalyzedSentence("а")).length);

    // mix alphabets
    matches = rule.match(lt.getAnalyzedSentence("прийдешнiй"));   // latin 'i'

    assertEquals(1, matches.length);
    assertEquals("прийдешній", matches[0].getSuggestedReplacements().get(0));

    // кличний для неістот
    matches = rule.match(lt.getAnalyzedSentence("душе"));

    assertEquals(0, matches.length);

    // розмовний інфінітив
//    matches = rule.match(langTool.getAnalyzedSentence("треба писать"));
//    assertEquals(0, matches.length);

    // abbreviations

    RuleMatch[] match = rule.match(lt.getAnalyzedSentence("Читання віршів Т.Г.Шевченко і Г.Тютюнника"));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(match));

    match = rule.match(lt.getAnalyzedSentence("Читання віршів Т. Г. Шевченко і Г. Тютюнника"));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(match));

    match = rule.match(lt.getAnalyzedSentence("Англі́йська мова (англ. English language, English) належить до германської групи"));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(match));

    match = rule.match(lt.getAnalyzedSentence("Англі́йська мова (англ English language, English) належить до германської групи"));
    assertEquals(1, match.length);

  
    match = rule.match(lt.getAnalyzedSentence("100 тис. гривень"));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(match));

    match = rule.match(lt.getAnalyzedSentence("100 кв. м"));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(match));

    match = rule.match(lt.getAnalyzedSentence("100 км²"));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(match));

    match = rule.match(lt.getAnalyzedSentence("100 кв м"));
    assertEquals(1, Arrays.asList(match).size());

//    match = rule.match(langTool.getAnalyzedSentence("2 раза"));
//    assertEquals(1, Arrays.asList(match).size());

    match = rule.match(lt.getAnalyzedSentence("півтора раза"));
    assertEquals(0, match.length);

    match = rule.match(lt.getAnalyzedSentence("УКРА"));
    assertEquals(1, Arrays.asList(match).size());

    String sent = "Іва\u0301н Петро\u0301ввич";
    match = rule.match(lt.getAnalyzedSentence(sent));
    assertEquals(1, Arrays.asList(match).size());
    assertEquals(sent.indexOf("Петро"), match[0].getFromPos());
    assertEquals(sent.length(), match[0].getToPos());

    sent = "голага́нівська";
    match = rule.match(lt.getAnalyzedSentence(sent));
    assertEquals(1, Arrays.asList(match).size());
    assertEquals(0, match[0].getFromPos());
    assertEquals(sent.length(), match[0].getToPos());

//    sent = "ґалаґа́нівська";
//    match = rule.match(langTool.getAnalyzedSentence(sent));
//    assertEquals(1, Arrays.asList(match).size());
//    assertEquals(0, match[0].getFromPos());
//    assertEquals(sent.length(), match[0].getToPos());
  }

  @Test
  public void testSuggestionOrder() throws IOException {
    RuleMatch[] match = rule.match(lt.getAnalyzedSentence("захворіває"));
    assertEquals(1, Arrays.asList(match).size());
    assertEquals(Arrays.asList("захворів", "захворіла", "захворіє", "захворівши", "захворював"), match[0].getSuggestedReplacements());
  }

  @Test
  public void testCompounds() throws IOException {
    
    // compounding
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Жакет був синьо-жовтого кольору")).length);

    assertEquals(0, rule.match(lt.getAnalyzedSentence("Він багато сидів на інтернет-форумах")).length);

    assertEquals(1, rule.match(lt.getAnalyzedSentence("Він багато сидів на інтермет-форумах")).length);

    
    // dynamic tagging
    assertEquals(0, rule.match(lt.getAnalyzedSentence("екс-креветка")).length);

    assertEquals(1, rule.match(lt.getAnalyzedSentence("банд-формування.")).length);

//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("учбово-виховного")).length);

    assertEquals(0, rule.match(lt.getAnalyzedSentence("екоблогер")).length);
  }
  
  
  @Test
  public void testDashedSuggestions() throws IOException {
    MorfologikUkrainianSpellerRule rule = new MorfologikUkrainianSpellerRule (TestTools.getMessages("uk"), new Ukrainian(), 
            null, Collections.emptyList());
    JLanguageTool lt = new JLanguageTool(new Ukrainian());
    
    RuleMatch[] match = rule.match(lt.getAnalyzedSentence("блоксистема"));
    assertEquals(1, match.length);
    assertEquals(Arrays.asList("блок система", "блок-система"), match[0].getSuggestedReplacements());

    match = rule.match(lt.getAnalyzedSentence("шоу-мен"));

    // TODO: commented out because it fails
    //assertEquals(1, match.length);
    //assertEquals(Arrays.asList("шоумен"), match[0].getSuggestedReplacements());
  }
  
  
  @Test
  public void testProhibitedSuggestions() throws IOException {
    
    RuleMatch[] match = rule.match(lt.getAnalyzedSentence("онлайннавчання"));
    assertEquals(1, match.length);

//    assertEquals(Arrays.asList("онлайн-навчання"), match[0].getSuggestedReplacements());
    
    match = rule.match(lt.getAnalyzedSentence("авіабегемат"));
    assertEquals(1, match.length);

    assertTrue("Should be empty: " + match[0].getSuggestedReplacements(), match[0].getSuggestedReplacements().isEmpty());

    // caught by SLOVA_BEZ_DEFISU
//    match = rule.match(langTool.getAnalyzedSentence("вело-маршрут"));
//    assertEquals(1, match.length);
//
//    assertEquals(Arrays.asList("веломаршрут"), match[0].getSuggestedReplacements());
//
//    match = rule.match(langTool.getAnalyzedSentence("відео-маршрут"));
//    assertEquals(1, match.length);
//
//    assertEquals(new ArrayList<String>(), match[0].getSuggestedReplacements());
//
//    match = rule.match(langTool.getAnalyzedSentence("вело-бегемот"));
//    assertEquals(1, match.length);

    assertTrue("Unexpected suggestions: " + match[0].getSuggestedReplacements(), match[0].getSuggestedReplacements().isEmpty());

    match = rule.match(lt.getAnalyzedSentence("радіо- та відеоспостереження"));
    assertEquals(0, match.length);

    match = rule.match(lt.getAnalyzedSentence("радіо- засоби"));
    assertEquals(1, match.length);

  }  
  
}
