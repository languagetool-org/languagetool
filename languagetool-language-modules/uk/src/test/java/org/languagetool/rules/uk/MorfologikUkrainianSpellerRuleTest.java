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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;

public class MorfologikUkrainianSpellerRuleTest {

  @Test
  public void testMorfologikSpeller() throws IOException {
    MorfologikUkrainianSpellerRule rule = new MorfologikUkrainianSpellerRule (TestTools.getMessages("uk"), new Ukrainian(), 
            null, Collections.emptyList());

    JLanguageTool langTool = new JLanguageTool(new Ukrainian());

    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("До вас прийде заввідділу!")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence(",")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("123454")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("До нас приїде The Beatles!")).length);

    // soft hyphen
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("піс\u00ADні")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("піс\u00ADні піс\u00ADні")).length);

    // non-breaking hyphen
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("ось\u2011ось")).length);

    // frequent infix notation
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("-ськ-")).length);

    // accent
    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("Іва́н Петро́вич Котляре́вський"));
    assertEquals(0, matches.length);

    matches = rule.match(langTool.getAnalyzedSentence("1998 ро́ку"));
    assertEquals(0, matches.length);

    //incorrect sentences:

    matches = rule.match(langTool.getAnalyzedSentence("атакуючий"));
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getFromPos());
    assertEquals("атакуючий".length(), matches[0].getToPos());

    matches = rule.match(langTool.getAnalyzedSentence("шклянка"));

    assertEquals(1, matches.length);
    assertEquals("склянка", matches[0].getSuggestedReplacements().get(0));

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("а")).length);

    // mix alphabets
    matches = rule.match(langTool.getAnalyzedSentence("прийдешнiй"));   // latin 'i'

    assertEquals(1, matches.length);
    assertEquals("прийдешній", matches[0].getSuggestedReplacements().get(0));

    // кличний для неістот
    matches = rule.match(langTool.getAnalyzedSentence("душе"));

    assertEquals(0, matches.length);

    // розмовний інфінітив
    matches = rule.match(langTool.getAnalyzedSentence("писать"));

    assertEquals(1, matches.length);
    
    // compounding
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Жакет був синьо-жовтого кольору")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Він багато сидів на інтернет-форумах")).length);

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Він багато сидів на інтермет-форумах")).length);

    
    // dynamic tagging
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("екс-креветка")).length);

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("банд-формування.")).length);

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("учбово-виховного")).length);

    // abbreviations

    RuleMatch[] match = rule.match(langTool.getAnalyzedSentence("Читання віршів Т.Г.Шевченко і Г.Тютюнника"));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(match));

    match = rule.match(langTool.getAnalyzedSentence("Читання віршів Т. Г. Шевченко і Г. Тютюнника"));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(match));

    match = rule.match(langTool.getAnalyzedSentence("Англі́йська мова (англ. English language, English) належить до германської групи"));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(match));

    match = rule.match(langTool.getAnalyzedSentence("Англі́йська мова (англ English language, English) належить до германської групи"));
    assertEquals(1, match.length);

  
    match = rule.match(langTool.getAnalyzedSentence("100 тис. гривень"));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(match));

    match = rule.match(langTool.getAnalyzedSentence("100 кв. м"));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(match));

    match = rule.match(langTool.getAnalyzedSentence("100 км²"));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(match));

    match = rule.match(langTool.getAnalyzedSentence("100 кв м"));
    assertEquals(1, Arrays.asList(match).size());

    match = rule.match(langTool.getAnalyzedSentence("2 раза"));
    assertEquals(1, Arrays.asList(match).size());

    match = rule.match(langTool.getAnalyzedSentence("півтора раза"));
    assertEquals(0, match.length);

    match = rule.match(langTool.getAnalyzedSentence("УКРА"));
    assertEquals(1, Arrays.asList(match).size());

    String sent = "Іва\u0301н Петро\u0301ввич";
    match = rule.match(langTool.getAnalyzedSentence(sent));
    assertEquals(1, Arrays.asList(match).size());
    assertEquals(sent.indexOf("Петро"), match[0].getFromPos());
    assertEquals(sent.length(), match[0].getToPos());

    sent = "ґалаґа́нівська";
    match = rule.match(langTool.getAnalyzedSentence(sent));
    assertEquals(1, Arrays.asList(match).size());
    assertEquals(0, match[0].getFromPos());
    assertEquals(sent.length(), match[0].getToPos());
  }

  @Test
  public void testProhibitedSuggestions() throws IOException {
    MorfologikUkrainianSpellerRule rule = new MorfologikUkrainianSpellerRule (TestTools.getMessages("uk"), new Ukrainian(), 
            null, Collections.emptyList());
    JLanguageTool langTool = new JLanguageTool(new Ukrainian());
    
    RuleMatch[] match = rule.match(langTool.getAnalyzedSentence("онлайннавчання"));
    assertEquals(1, match.length);

//    assertEquals(Arrays.asList("онлайн-навчання"), match[0].getSuggestedReplacements());
    
    match = rule.match(langTool.getAnalyzedSentence("авіабегемот"));
    assertEquals(1, match.length);

    assertTrue("Should be empty: " + match[0].getSuggestedReplacements().toString(), match[0].getSuggestedReplacements().isEmpty());

    match = rule.match(langTool.getAnalyzedSentence("вело-маршрут"));
    assertEquals(1, match.length);

    assertEquals(Arrays.asList("веломаршрут"), match[0].getSuggestedReplacements());

    match = rule.match(langTool.getAnalyzedSentence("відео-маршрут"));
    assertEquals(1, match.length);

    assertEquals(new ArrayList<String>(), match[0].getSuggestedReplacements());

    match = rule.match(langTool.getAnalyzedSentence("вело-бегемот"));
    assertEquals(1, match.length);

    assertTrue("Unexpected suggestions: " + match[0].getSuggestedReplacements().toString(), match[0].getSuggestedReplacements().isEmpty());

    match = rule.match(langTool.getAnalyzedSentence("радіо- та відеоспостереження"));
    assertEquals(0, match.length);

    match = rule.match(langTool.getAnalyzedSentence("радіо- засоби"));
    assertEquals(1, match.length);

  }  
  
}
