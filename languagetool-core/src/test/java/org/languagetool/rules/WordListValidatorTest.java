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

  private static final String VALID_CHARS =
          "[ 0-9a-zA-ZöäüÖÄÜßëçèéáàóòÈÉÁÀÓÒÍãñíîŞş&*_:\\\\" +
          "___INSERT___" +
          "Œ€ūαΑβΒγɣΓδΔεΕζΖηΗθΘιΙκΚλΛμΜνΝξΞοΟπΠρΡσΣτΤυΥφΦχΧψΨωΩάΆέΈίΊήΉύΎϊϋΰΐœţłń" +
          "ŚśōżúïÎôêâû" +
          "'’" +
          "./-]+" +
          "|[khmcdµ]?m[²³]|°[CFR]|C?O₂-?.*|mc²";

  // Words that are valid but with special characters so that we don't want to
  // allow them in general:
  private static final Set<String> VALID_WORDS = new HashSet<>(Arrays.asList(
          "Varaždin/S",
          "Będzin",
          "Aydın",
          "Poreč",
          "Čeferin",
          "Čeferin/S",
          "Perišić",
          "Perišić/S",
          "Modrić",
          "Modrić/S",
          "Miłosz",
          "Arnautović/S",
          "Bhagavad-gītā",
          "Sønderjylland/S",
          "Utøya/S",
          "Božena/S",
          "Brăila/S",
          "Timișoara/S",
          "Tromsø/S",
          "Solidarność",
          "Salihamidžić/S",
          "Darʿā",  // de
          "veni, vidi, vici", // en
          "Food+Tech Connect", // en
          "comme ci, comme ça", // en
          "Robinson + Yu",
          "herõon", // en
          "herõons", // en
          "Võro",  // en
          "Łukasz",
          "Čakavian",
          "Erdoğan",
          "Rădulescu",
          "Štokavian",
          "Veliko Tărnovo",
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
          "Forlì",
          "Qur’an",
          "Djuveč",
          "Djuvečreis",
          "Hidschāb/S",
          "Dvořák/S",
          "Paul Erdős",
          "Erdoğan/S",
          "Sørensen/S",
          "Sørensen",
          "Søren/S",
          "Søren",
          "Radosław",
          "Radosław/S",
          "Jarosław",
          "Jarosław/S",
          "Władysław/S",
          "Şahin/S",
          "Uğur/S",
          "Jørgensen/S",
          "Jørgensen",
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
          "İlkay",
          "Gündoğan",
          "Tuğrul",
          "Ñuñoa",
          "Ibišević",
          "Fríður",
          "Łódź",
          "Ørsted",
          "Mirotić",
          "Subotić",
          "Pÿur",
          "Subašić",
          "celebrytę", // for PL
          "antybiotykoterapię", // for PL
          "elektromobilność", // for PL
          "kryptowalutę", // for PL
          "fotowoltaikę", // for PL
          "insulinooporność", // for PL
          "infografikę", // for PL
          "dtª",  // for PT
          "dtº",  // for PT
          "ª",  // for PT
          "º",  // for PT
          // Greek letters / Mathematics and physics variables
          "Α", "Β", "Γ", "Δ", "Ε", "Ζ", "Η", "Θ", "Ι", "Κ", "Λ", "Μ", "Ν", "Ξ", "Ο", "Π", "Ρ", "Σ", "Τ", "Υ", "Φ", "Χ", "Ψ", "Ω", 
          "α", "β", "γ", "δ", "ε", "ζ", "η", "θ", "ι", "κ", "λ", "μ", "ν", "ξ", "ο", "π", "ρ", "σ", "τ", "υ", "φ", "χ", "ψ", "ω"          
  ));

  private final String additionalValidationChars;

  public WordListValidatorTest() {
    this("");
  }

  public WordListValidatorTest(String additionalValidationChars) {
    this.additionalValidationChars = additionalValidationChars;
  }

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
    String validChars = VALID_CHARS.replace("___INSERT___", additionalValidationChars);
    Pattern validPattern = Pattern.compile(validChars);
    for (String word : words) {
      if (VALID_WORDS.contains(word) || VALID_WORDS.contains(word.trim())) {
        // okay
      } else if (!validPattern.matcher(word).matches()) {
        failures.add("Word '" + word + "' from " + spellingFileName + " doesn't match regex: " + validChars +
                " - please fix the word or add the character to the language's " + WordListValidatorTest.class.getName() + " if it's valid");
      }
    }
    if (failures.size() > 0) {
      fail(String.join("\n\n", failures));
    }
  }

}
