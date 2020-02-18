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

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.spelling.CachingWordListLoader;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.util.*;
import java.util.regex.Pattern;

import static junit.framework.TestCase.fail;

public class WordListValidatorTest {

  private static final Pattern VALID_CHARS = Pattern.compile(
          "[ 0-9a-zA-ZöäüÖÄÜßëçèéáàóòÈÉÁÀÓÒãñíîş&" +
          "Œ€ūαΑβΒγɣΓδΔεΕζΖηΗθΘιΙκΚλΛμΜνΝξΞοΟπΠρΡσΣτΤυΥφΦχΧψΨωΩάΆέΈίΊήΉύΎϊϋΰΐœţłń" +
          "ŚśōżúïÎôêâû" +
          "Ææ" +  // English
          "ÍÚÑ" + // for Spanish
          "õș" +   // for Portuguese
          "ā" + // for Persian
          "·" +   // for Catalan
          "_" +   // for German (syntax: prefix_verb)
          "'’" +
          "ýùźăŽČĆÅıøğåšĝÇİŞŠčžć±ą+-" +   // for Dutch (inhabitants) proper names mostly
          "./-]+" + 
          "|[khmcdµ]?m[²³]|°[CFR]|CO₂-?.*|mc²"
  );

  // Words that are valid but with special characters so that we don't want to
  // allow them in general:
  private static final Set<String> VALID_WORDS = new HashSet<>(Arrays.asList(
          "Będzin",
          "Bhagavad-gītā",
          "Brāhmaṇa",
          "Forlì-Cesena",
          "Hárbarðsljóð",
          "Hassānīya",
          "Hyndluljóð",
          "Kazanlǎk",
          "Kesäranta",
          "Kŭrdzhali",
          "Malko Tŭrnovo",
          "Rígsþula",
          "Savitṛ",
          "Vafþrúðnismál",
          "Völundarkviða",
          "Kṛṣṇa",
          "art.º",
          "Klaipėda",
          "Mondelēz",
          "chef-d’œuvre",
          "chefs-d’œuvre",
          "Brač",
          "Qur’an",
          "Djuveč",
          "Djuvečreis",
          "Hidschāb/S",
          "Dvořák/S",
          "Erdoğan/S",
          "Ångström",
          "ångström",
          "ångströms",
          "'Ndrangheta",
          "Hồ Chí Minh",
          "McDonald's",
          "Bahrām",
          "Kęstutis",
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

  public void testWordListValidity(Language lang) {
    if (lang.getShortCode().equals("ru")) {
      return;   // skipping, Cyrillic chars not part of the validation yet
    }
    Set<String> checked = new HashSet<>();
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

  private void validateWords(List<String> words, String spellingFileName) {
    List<String> failures = new ArrayList<>();
    for (String word : words) {
      if (VALID_WORDS.contains(word) || VALID_WORDS.contains(word.trim())) {
        // okay
      } else if (!VALID_CHARS.matcher(word).matches()) {
        failures.add("Word '" + word + "' from " + spellingFileName + " doesn't match regex: " + VALID_CHARS +
                " - please fix the word or add the character to the language's " + WordListValidatorTest.class.getName() + " if it's valid");
      }
    }
    if (failures.size() > 0) {
      fail(String.join("\n\n", failures));
    }
  }

}
