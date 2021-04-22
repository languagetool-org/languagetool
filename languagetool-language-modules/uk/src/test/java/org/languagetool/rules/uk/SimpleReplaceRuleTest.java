/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;


public class SimpleReplaceRuleTest {
  private final JLanguageTool lt = new JLanguageTool(new Ukrainian());
  private MorfologikUkrainianSpellerRule morfologikSpellerRule;
  private SimpleReplaceRule rule;

  @Before
  public void setup() throws IOException {
    morfologikSpellerRule = new MorfologikUkrainianSpellerRule (TestTools.getMessages("uk"), new Ukrainian(), 
        null, Collections.emptyList());

    rule = new SimpleReplaceRule(TestTools.getEnglishMessages(), morfologikSpellerRule);
  }
  
  @Test
  public void testRule() throws IOException {

    RuleMatch[] matches;

    // correct sentences:
    matches = rule.match(lt.getAnalyzedSentence("Ці рядки повинні збігатися."));
    assertEquals(0, matches.length);

    // incorrect sentences:
    matches = rule.match(lt.getAnalyzedSentence("Ці рядки повинні співпадати"));
    assertEquals(1, matches.length);
    assertEquals(2, matches[0].getSuggestedReplacements().size());
    assertEquals(Arrays.asList("збігатися", "сходитися"), matches[0].getSuggestedReplacements());
    assertFalse(matches[0].getMessage().contains("просторічна форма"));

    matches = rule.match(lt.getAnalyzedSentence("Нападаючий"));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("Нападник", "Нападальний", "Нападний"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("Нападаючого"));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("Нападник", "Нападальний", "Нападний"), matches[0].getSuggestedReplacements());

    // test enforce list
    // главком - дуже рідко зустрічається, як загальна назва
//    matches = rule.match(langTool.getAnalyzedSentence("главком"));
//    assertEquals(1, matches.length);
//    assertEquals(Arrays.asList("головком"), matches[0].getSuggestedReplacements());

    // test ignoreTagged
    matches = rule.match(lt.getAnalyzedSentence("щедрота"));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("щедрість", "гойність", "щедриня"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("щедроти"));
    assertEquals(0, matches.length);

    matches = rule.match(lt.getAnalyzedSentence("200 чоловік."));
    assertEquals(0, matches.length);

    matches = rule.match(lt.getAnalyzedSentence("Конрадом II і Генріхом III"));
    assertEquals(0, matches.length);
    
    //TODO: should not react at all
    matches = rule.match(lt.getAnalyzedSentence("мікро-району"));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("мікрорайону"), matches[0].getSuggestedReplacements());

  }

  @Test
  public void testRulePartOfMultiword() throws IOException {
    SimpleReplaceRule rule = new SimpleReplaceRule(TestTools.getEnglishMessages(), morfologikSpellerRule);

    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("на думку проводжаючих"));
        assertEquals(1, matches.length);
  }

  @Test
  public void testSubstandards() throws IOException {
    SimpleReplaceRule rule = new SimpleReplaceRule(TestTools.getEnglishMessages(), morfologikSpellerRule);

    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("А шо такого?"));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("що"), matches[0].getSuggestedReplacements());
    assertEquals("Це розмовна просторічна форма", matches[0].getMessage());
  }

  @Test
  public void testMisspellings() throws IOException {
    SimpleReplaceRule rule = new SimpleReplaceRule(TestTools.getEnglishMessages(), morfologikSpellerRule);

    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("ганделик"));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("генделик"), matches[0].getSuggestedReplacements());
//    assertEquals(Categories.MISC.toString(), matches[0].getRule().getCategory().getId());
//    assertEquals("Це розмовна просторічна форма", matches[0].getMessage());
  }

  @Test
  public void testRuleByTag() throws IOException {
    SimpleReplaceRule rule = new SimpleReplaceRule(TestTools.getEnglishMessages(), morfologikSpellerRule);

    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("спороутворюючого"));
    assertEquals(1, matches.length);

    matches = rule.match(lt.getAnalyzedSentence("примкнувшим"));
    assertEquals(1, matches.length);
  }

}
