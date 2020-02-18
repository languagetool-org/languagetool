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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.PatternRuleLoader;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.RegexPatternRule;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

  @Test
  public void testMessageCoherency() {
    Language lang = Languages.getLanguageForShortCode("de-DE");
    JLanguageTool lt = new JLanguageTool(lang);
    for (Rule rule : lt.getAllRules()) {
      if (rule instanceof AbstractPatternRule) {
        AbstractPatternRule patternRule = (AbstractPatternRule) rule;
        String message = patternRule.getMessage();
        String origWord = null;
        String suggWord = null;
        if (message.contains("Kommata")) {
          origWord = "Kommata";
          suggWord = "Kommas";
        }
        if (message.contains("Substantiv")) {
          origWord = "Substantiv";
          suggWord = "Nomen";
        }
        if (message.toLowerCase().contains("substantiviert")) {
          origWord = "substantiviert";
          suggWord = "nominalisiert";
        }
        if (message.toLowerCase().contains("substantivisch")) {
          origWord = "substantivisch";
          suggWord = "nominalisiert";
        }
        if (message.contains("Genetiv")) {
          origWord = "Genetiv";
          suggWord = "Genitiv";
        }
        if (message.contains("Partizip 1")) {
          origWord = "Partizip 1";
          suggWord = "Partizip I";
        }
        if (message.contains("Partizip 2")) {
          origWord = "Partizip 2";
          suggWord = "Partizip II";
        }
        if (message.contains(" fordert ")) {
          origWord = "fordert";
          suggWord = "erfordert";
        }
        if (origWord != null) {
          System.err.println("WARNING: Aus Gründen der Einheitlichkeit bitte '" + suggWord + "' nutzen statt '" + origWord + "' in der Regel " + patternRule.getFullId() + ", message: '" + message + "'");
        }
      }
    }
  }

  // test that patterns with 'ß' also contain that pattern with 'ss' so the rule can match for de-CH users
  @Test
  public void testSwissSpellingVariants() throws IOException {
    Language lang = Languages.getLanguageForShortCode("de-DE");
    String dirBase = JLanguageTool.getDataBroker().getRulesDir() + "/" + lang.getShortCode() + "/";
    List<String> ignoreIds = Arrays.asList("SCHARFES_SZ", "APOSTROPH_S", "EMPFOHLENE_ZUSAMMENSCHREIBUNG");
    List<String> warnings = new ArrayList<>();
    for (String ruleFileName : lang.getRuleFileNames()) {
      int i = 0;
      InputStream is = this.getClass().getResourceAsStream(ruleFileName);
      List<AbstractPatternRule> rules = new PatternRuleLoader().getRules(is, dirBase + "/" + ruleFileName);
      for (AbstractPatternRule rule : rules) {
        if (ignoreIds.contains(rule.getId())) {
          continue;
        }
        for (DisambiguationPatternRule antiPattern : rule.getAntiPatterns()) {
          for (PatternToken patternToken : antiPattern.getPatternTokens()) {
            String pattern = patternToken.getString();
            if (lacksSwitzerlandSpelling(pattern)) {
              warnings.add(rule.getFullId() + ": " + pattern + " [antipattern]");
              i++;
            }
          }
        }
        if (rule instanceof RegexPatternRule) {
          String pattern = ((RegexPatternRule) rule).getPattern().toString();
          if (lacksSwitzerlandSpelling(pattern)) {
            warnings.add(rule.getFullId() + ": " + pattern);
            i++;
          }
        } else {
          List<PatternToken> patternTokens = rule.getPatternTokens();
          if (patternTokens != null) {
            for (PatternToken patternToken : patternTokens) {
              String pattern = patternToken.getString();
              if (lacksSwitzerlandSpelling(pattern)) {
                warnings.add(rule.getFullId() + ": " + pattern);
                i++;
              }
            }
          }
        }
      }
      if (warnings.size() > 0) {
        System.err.println("*** WARNING: " + ruleFileName + ": " + i + " <token>s with 'ß' but not 'ss' - these will not match for users of German (Switzerland):");
        for (String warning : warnings) {
          System.err.println("  " + warning);
        }
      }
    }
  }

  private boolean lacksSwitzerlandSpelling(String pattern) {
    return pattern != null && pattern.contains("ß") 
      && !pattern.contains("(ß|ss)") 
      && !containsSwitzerlandSpelling(pattern) 
      && !allInBrackets('ß', pattern);
  }

  // only works for e.g.: foo|baß|bla
  private boolean containsSwitzerlandSpelling(String pattern) {
    String[] parts = pattern.split("\\|");
    for (String part : parts) {
      if (part.contains("ß")) {
        if (!cleanSyntax(pattern).contains(cleanSyntax(part).replace("ß", "ss"))) {
          return false;
        }
      }
    }
    return true;
  }

  private String cleanSyntax(String part) {
    return part.replaceAll("[()]", "");
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
