/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.spelling.CachingWordListLoader;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static junit.framework.TestCase.fail;

public class WordListValidatorTest {

  private static final Pattern VALID_CHARS = Pattern.compile(
          "[0-9a-zA-ZöäüÖÄÜßëçèéáàóòÈÉÁÀÓÒãñíîş&" +
          "Œ€ūαΑβΒγɣΓδΔεΕζΖηΗθΘιΙκΚλΛμΜνΝξΞοΟπΠρΡσΣτΤυΥφΦχΧψΨωΩάΆέΈίΊήΉύΎϊϋΰΐœţłń" +
          "ŚśōżúïÎôêâû" +
          "õ" +   // for Portuguese
          "·" +   // for Catalan
          "'ýùźăŽČĆÅıøğåšĝÇİŞŠčžć±ą+-" +   // for Dutch (inhabitants) proper names mostly
          "./-]+" + 
          "|[khmcdµ]?m[²³]|°[CFR]|CO₂-?.*|mc²"
  );

  // Words that are valid but with special characters so that we don't want to
  // allow them in general:
  private static final Set<String> VALID_WORDS = new HashSet<>(Arrays.asList(
          "Mondelēz",
          "chef-d’œuvre",
          "chefs-d’œuvre",
          "Brač",
          "Djuveč",
          "Djuvečreis",
          "Hidschāb/S",
          "Dvořák/S",
          "Erdoğan/S",
          "Ångström",
          "ångström",
          "ångströms",
          "'Ndrangheta",
          "McDonald's",
          "µm",
          "µg",
          "µl",
          "CD&V",
          "C&A",
          "P&O",
          "S&P",
          "ČSSR",
          "V&D",
          // Greek letters / Mathematics and physics variables
          "Α", "Β", "Γ", "Δ", "Ε", "Ζ", "Η", "Θ", "Ι", "Κ", "Λ", "Μ", "Ν", "Ξ", "Ο", "Π", "Ρ", "Σ", "Τ", "Υ", "Φ", "Χ", "Ψ", "Ω", 
          "α", "β", "γ", "δ", "ε", "ζ", "η", "θ", "ι", "κ", "λ", "μ", "ν", "ξ", "ο", "π", "ρ", "σ", "τ", "υ", "φ", "χ", "ψ", "ω"          
  ));

  @Test
  public void testWordListValidity() throws IOException {
    Set<String> checked = new HashSet<>();
    for (Language lang : Languages.get()) {
      if (lang.getShortCode().equals("ru")) {
        // skipping, Cyrillic chars not part of the validation yet
        continue;
      }
      JLanguageTool lt = new JLanguageTool(lang);
      List<Rule> rules = lt.getAllActiveRules();
      for (Rule rule : rules) {
        if (rule instanceof SpellingCheckRule) {
          SpellingCheckRule sRule = (SpellingCheckRule) rule;
          String file = sRule.getSpellingFileName();
          if (JLanguageTool.getDataBroker().resourceExists(file) && !checked.contains(file)) {
            System.out.println("Checking " + file);
            CachingWordListLoader loader = new CachingWordListLoader();
            List<String> words = loader.loadWords(file);
            validateWords(words, file);
            checked.add(file);
          }
        }
      }
    }
  }

  private void validateWords(List<String> words, String spellingFileName) {
    for (String word : words) {
      if (VALID_WORDS.contains(word)) {
        // okay
      } else if (word.contains(" ")) {
        // since version 3.8 multi-word entries are allowed 'spelling.txt' (= getSpellingFileName()) -- ignore them
      } else if (!VALID_CHARS.matcher(word).matches()) {
        fail("Word '" + word + "' from " + spellingFileName + " doesn't match regex: " + VALID_CHARS +
             " - please fix the word or add the character to " + WordListValidatorTest.class.getName() + " if it's valid");
      }
    }
  }

}
