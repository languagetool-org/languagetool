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
package org.languagetool.rules.en;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.UserConfig;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.PatternRuleTest;

import java.io.IOException;
import java.util.*;

public class EnglishPatternRuleTest extends PatternRuleTest {

  @Test
  public void testRules() throws IOException {
    runGrammarRulesFromXmlTest();
  }
  
  @Test
  public void testL2Languages() throws IOException {
    validatePatternFile(Arrays.asList("en/grammar-l2-de.xml"));
    validatePatternFile(Arrays.asList("en/grammar-l2-fr.xml"));
    runTestForLanguage(new L2GermanRulesOnlyEnglish());
    runTestForLanguage(new L2FrenchRulesOnlyEnglish());
  }

  // used to cause an ArrayIndexOutOfBoundsException in MatchState.setToken()
  @Test
  public void testBug() throws Exception {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("en"));
    lt.check("Alexander between 369 and 358 BC\n\nAlexander");
  }
  
  private static class L2GermanRulesOnlyEnglish extends AmericanEnglish {
    @Override
    public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) {
      return new ArrayList<>();
    }
    @Override
    public List<String> getRuleFileNames() {
      return Arrays.asList("/org/languagetool/rules/en/grammar-l2-de.xml");
    }
  }

  private static class L2FrenchRulesOnlyEnglish extends AmericanEnglish {
    @Override
    public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) {
      return new ArrayList<>();
    }
    @Override
    public List<String> getRuleFileNames() {
      return Arrays.asList("/org/languagetool/rules/en/grammar-l2-fr.xml");
    }
  }

}
