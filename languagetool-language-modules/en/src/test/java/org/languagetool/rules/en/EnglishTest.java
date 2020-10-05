/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.LanguageSpecificTest;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.io.IOException;
import java.util.Arrays;

public class EnglishTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    // NOTE: this text needs to be kept in sync with WelcomeController.php's getDefaultDemoTexts():
    String s = "LanguageTool offers spell and grammar checking. Just paste your text here and click the 'Check Text' button. Click the colored phrases for details on potential errors. or use this text too see an few of of the problems that LanguageTool can detecd. What do you thinks of grammar checkers? Please not that they are not perfect. Style issues get a blue marker: It's 5 P.M. in the afternoon. The weather was nice on Thursday, 27 June 2017.";
    Language lang = Languages.getLanguageForShortCode("en-US");
    testDemoText(lang, s,
      Arrays.asList("UPPERCASE_SENTENCE_START",
                    "TOO_TO",
                    "EN_A_VS_AN",
                    "ENGLISH_WORD_REPEAT_RULE",
                    "MORFOLOGIK_RULE_EN_US",
                    "DO_VBZ",
                    "PLEASE_NOT_THAT",
                    "PM_IN_THE_EVENING",
                    "DATE_WEEKDAY")
    );
    runTests(lang, null, "ÆæāýÅåøšùçıčćö");
  }

  @Test
  public void testMessages() {
    Language lang = Languages.getLanguageForShortCode("en-US");
    JLanguageTool lt = new JLanguageTool(lang);
    for (Rule rule : lt.getAllRules()) {
      if (rule instanceof AbstractPatternRule) {
        AbstractPatternRule patternRule = (AbstractPatternRule) rule;
        String message = patternRule.getMessage();
        String origWord = null;
        String suggWord = null;
        if (message.contains("full stop")) {
          origWord = "full stop";
          suggWord = "period";
        }
        if (message.contains("pound sign")) {
          origWord = "pound sign";
          suggWord = "(ambiguous in en-US, see https://en.wikipedia.org/wiki/Pound_sign)";
        }
        if (message.contains("colour")) {
          origWord = "colour";
          suggWord = "color";
        }
        if (message.contains("flavour")) {
          origWord = "flavour";
          suggWord = "flavor";
        }
        if (message.contains("theatre")) {
          origWord = "theatre";
          suggWord = "theater";
        }
        if (message.contains("centre")) {
          origWord = "centre";
          suggWord = "center";
        }
        if (message.matches(".*(kilo|centi|milli)?(me|li)tres?.*")) {
          origWord = "metre";
          suggWord = "meter";
        }
        if (origWord != null) {
          System.err.println("WARNING: Instead of '" + origWord + "' (en-GB) consider using " +
                  "words that work in all locales (or '" + suggWord + "' (en-US) if you can't find those) for rule " +
                  patternRule.getFullId() + ", message: '" + message + "'");
        }
      }
    }
  }

}
