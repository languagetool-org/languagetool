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
import org.languagetool.language.English;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EnglishTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    // NOTE: this text needs to be kept in sync with config.ts -> DEMO_TEXTS:
    String s = "Write or paste your text here too have it checked continuously. Errors will be underlined in different colours: we will mark seplling errors with red underilnes. Furthermore grammar error's are highlighted in yellow. LanguageTool also marks style issues in a reliable manner by underlining them in blue. did you know that you can sea synonyms by double clicking a word? Its a impressively versatile tool, e.g. if youd like to tell a colleague from over sea's about what happened at 5 PM in the afternoon on Monday, 27 May 2007.";
    Language lang = Languages.getLanguageForShortCode("en-US");
    testDemoText(lang, s,
      Arrays.asList("TOO_TO", "MORFOLOGIK_RULE_EN_US", "MORFOLOGIK_RULE_EN_US", "MORFOLOGIK_RULE_EN_US", "SENT_START_CONJUNCTIVE_LINKING_ADVERB_COMMA", "APOS_ARE", "IN_A_X_MANNER", "UPPERCASE_SENTENCE_START", "DOUBLE_HYPHEN", "IT_IS", "EN_A_VS_AN", "EN_CONTRACTION_SPELLING", "OVER_SEAS", "PM_IN_THE_EVENING", "DATE_WEEKDAY")
    );
    runTests(lang, null, "ÆæāýÅåøšùçıčćö");
  }

  @Test
  public void testRepeatedPatternRules() throws IOException {
    Language lang = new English();
    JLanguageTool lt = new JLanguageTool(lang);

    List<RuleMatch> matches = lt.check("Thank you for all your help. Here is another sentence. Thank you so much for all the fish.");
    assertEquals("Matches when close together", 1, matches.size());

    matches = lt.check("Thank you for all your help. This is filler. Here are more words. There needs to be a certain distance between sentences so that this doesn't match again. How long does it need to be? The default setting is 350 characters. That's quite a lot. Here are some more words to fill up this text. Here are some more words to fill up this text. Now here are some more words to fill up this text. Thank you so much for all the fish.");
    assertEquals("No matches when further apart", 0, matches.size());
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
