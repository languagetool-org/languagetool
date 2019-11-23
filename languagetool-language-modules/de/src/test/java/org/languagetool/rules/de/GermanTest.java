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
package org.languagetool.rules.de;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.Languages;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.PatternRuleLoader;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.RegexPatternRule;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class GermanTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    // NOTE: this text needs to be kept in sync with WelcomeController.php's getDefaultDemoTexts():
    String s = "Fügen Sie hier Ihren Text ein. Klicken Sie nach der Prüfung auf die farbig unterlegten Textstellen. oder nutzen Sie diesen Text als Beispiel für ein Paar Fehler , die LanguageTool erkennen kann: Ihm wurde Angst und bange. Mögliche stilistische Probleme werden blau hervorgehoben: Das ist besser wie vor drei Jahren. Eine Rechtschreibprüfun findet findet übrigens auch statt. Donnerstag, den 23.01.2019 war gutes Wetter. Die Beispiel endet hier.";
    Language lang = Languages.getLanguageForShortCode("de-DE");
    testDemoText(lang, s,
      Arrays.asList("UPPERCASE_SENTENCE_START", "EIN_PAAR", "COMMA_PARENTHESIS_WHITESPACE", "ANGST_UND_BANGE", "KOMP_WIE", "GERMAN_SPELLER_RULE", "SAGT_RUFT", "DATUM_WOCHENTAG", "DE_AGREEMENT")
    );
    runTests(lang);
  }
  
  // test that patterns with 'ß' also contain that pattern with 'ss' so the rule can match for de-CH users
  @Test
  @Ignore("too many warnings yet - activate once the conversion has (mostly) been finished")
  public void testSwissSpellingVariants() throws IOException {
    Language lang = Languages.getLanguageForShortCode("de-DE");
    String dirBase = JLanguageTool.getDataBroker().getRulesDir() + "/" + lang.getShortCode() + "/";
    for (String ruleFileName : lang.getRuleFileNames()) {
      int i = 0;
      InputStream is = this.getClass().getResourceAsStream(ruleFileName);
      List<AbstractPatternRule> rules = new PatternRuleLoader().getRules(is, dirBase + "/" + ruleFileName);
      for (AbstractPatternRule rule : rules) {
        for (DisambiguationPatternRule antiPattern : rule.getAntiPatterns()) {
          for (PatternToken patternToken : antiPattern.getPatternTokens()) {
            String pattern = patternToken.getString();
            if (lacksSwitzerlandSpelling(pattern)) {
              System.out.println(rule.getFullId() + ": " + pattern + " [antipattern]");
              i++;
            }
          }
        }
        if (rule instanceof RegexPatternRule) {
          String pattern = ((RegexPatternRule) rule).getPattern().toString();
          if (lacksSwitzerlandSpelling(pattern)) {
            System.out.println(rule.getFullId() + ": " + pattern);
            i++;
          }
        } else {
          List<PatternToken> patternTokens = rule.getPatternTokens();
          if (patternTokens != null) {
            for (PatternToken patternToken : patternTokens) {
              String pattern = patternToken.getString();
              if (lacksSwitzerlandSpelling(pattern)) {
                System.out.println(rule.getFullId() + ": " + pattern);
                i++;
              }
            }
          }
        }
      }
      System.out.println("*** " + ruleFileName + ": " + i + " <token>s with 'ß' but not 'ss' - these will not match for users of German (Switzerland)");
    }
  }

  private boolean lacksSwitzerlandSpelling(String pattern) {
    return pattern != null && pattern.contains("ß") && !containsSwitzerlandSpelling(pattern) && !allInBrackets('ß', pattern);
  }

  // only works for e.g.: foo|baß|bla
  private boolean containsSwitzerlandSpelling(String pattern) {
    String[] parts = pattern.split("\\|");
    for (String part : parts) {
      if (part.contains("ß")) {
        if (!pattern.contains(part.replace("ß", "ss"))) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean allInBrackets(char searchChar, String pattern) {
    boolean inBrackets = false;
    for (int i = 0; i < pattern.length(); i++) {
      char c = pattern.charAt(i);
      if (c == '[') {
        inBrackets = true;
      } else if (c == ']') {
        inBrackets = false;
      } else if (c == searchChar && !inBrackets) {
        return false;
      }
    }
    return true;
  }

}
