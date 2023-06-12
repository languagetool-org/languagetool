/* LanguageTool, a natural language style checker 
 * Copyright (C) 2023 Jaume Ortolà
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
package org.languagetool.rules.ca;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.AnalyzedToken;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tools.StringTools;

/*
 * Suggestions for rule OBLIDARSE: se m'ha oblidat -> me n'he oblidat, m'he oblidat de
 */

public class OblidarseSugestionsFilter extends RuleFilter {

  Pattern pApostropheNeeded = Pattern.compile("h?[aeiouàèéíòóú].*", Pattern.CASE_INSENSITIVE);

  static private CatalanSynthesizer synth = CatalanSynthesizer.INSTANCE;

  private static Map<String, String> addReflexiveVowel = new HashMap<>();
  static {
    addReflexiveVowel.put("1S", "m'");
    addReflexiveVowel.put("2S", "t'");
    addReflexiveVowel.put("3S", "s'");
    addReflexiveVowel.put("1P", "ens ");
    addReflexiveVowel.put("2P", "us ");
    addReflexiveVowel.put("3P", "s'");
  }

  private static Map<String, String> addReflexiveConsonant = new HashMap<>();
  static {
    addReflexiveConsonant.put("1S", "em ");
    addReflexiveConsonant.put("2S", "et ");
    addReflexiveConsonant.put("3S", "es ");
    addReflexiveConsonant.put("1P", "ens ");
    addReflexiveConsonant.put("2P", "us ");
    addReflexiveConsonant.put("3P", "es ");
  }

  private static Map<String, String> addReflexiveEnVowel = new HashMap<>();
  static {
    addReflexiveEnVowel.put("1S", "me n'");
    addReflexiveEnVowel.put("2S", "te n'");
    addReflexiveEnVowel.put("3S", "se n'");
    addReflexiveEnVowel.put("1P", "ens n'");
    addReflexiveEnVowel.put("2P", "us n'");
    addReflexiveEnVowel.put("3P", "se n'");
  }

  private static Map<String, String> addReflexiveEnConsonant = new HashMap<>();
  static {
    addReflexiveEnConsonant.put("1S", "me'n ");
    addReflexiveEnConsonant.put("2S", "te'n ");
    addReflexiveEnConsonant.put("3S", "se'n ");
    addReflexiveEnConsonant.put("1P", "ens en ");
    addReflexiveEnConsonant.put("2P", "us en ");
    addReflexiveEnConsonant.put("3P", "se'n ");
  }

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {
    int posWord = 0;
    AnalyzedTokenReadings[] tokens = match.getSentence().getTokensWithoutWhitespace();
    while (posWord < tokens.length
        && (tokens[posWord].getStartPos() < match.getFromPos() || tokens[posWord].isSentenceStart())) {
      posWord++;
    }
    String pronomPostag = tokens[posWord + 1].readingWithTagRegex("P.*").getPOSTag();
    String pronomGenderNumber = pronomPostag.substring(2, 3) + pronomPostag.substring(4, 5);
    boolean isThereAuxiliar = !tokens[posWord + 2].hasAnyLemma("oblidar", "descuidar");
    String verbPostag = tokens[posWord + 2].readingWithTagRegex("V.*").getPOSTag();
    String lemma = tokens[posWord + 2].readingWithTagRegex("V.*").getLemma();
    AnalyzedToken at = new AnalyzedToken("", "", lemma);
    String[] synthForms = synth.synthesize(at,
        verbPostag.substring(0, 4) + pronomGenderNumber + verbPostag.substring(6, 8));
    String newVerb = "";
    if (synthForms.length == 0) {
      return null;
    }
    newVerb = synthForms[0];
    if (isThereAuxiliar) {
      newVerb = newVerb + " " + tokens[posWord + 3].getToken();
    }
    boolean verbVowel = pApostropheNeeded.matcher(newVerb).matches();
    String wordAfter = "";
    int wordAfterIndex = (isThereAuxiliar ? posWord + 4 : posWord + 3);
    if (wordAfterIndex < tokens.length) {
      AnalyzedToken wordAfterReading = tokens[wordAfterIndex].readingWithTagRegex("D.*|V.N.*|PI.*");
      if (wordAfterReading != null) {
        wordAfter = wordAfterReading.getToken();
      }
      List<String> exceptionsList = Arrays.asList("com", "de", "d'");
      if (exceptionsList.contains(tokens[wordAfterIndex].getToken().toLowerCase())) {
        wordAfter = tokens[wordAfterIndex].getToken();
      }
    }
    Map<String, String> transform;
    if (wordAfter.isEmpty() && !wordAfter.equalsIgnoreCase("de") && !wordAfter.equalsIgnoreCase("d'")) {
      transform = (verbVowel ? addReflexiveEnVowel : addReflexiveEnConsonant);
    } else {
      transform = (verbVowel ? addReflexiveVowel : addReflexiveConsonant);
    }
    StringBuilder suggBld = new StringBuilder();
    suggBld.append(transform.get(pronomGenderNumber));
    suggBld.append(newVerb);
    boolean wordAfterApostrophe = false;
    if (!wordAfter.isEmpty() && !wordAfter.equalsIgnoreCase("de") && !wordAfter.equalsIgnoreCase("d'")) {
      wordAfterApostrophe = pApostropheNeeded.matcher(wordAfter).matches();
      suggBld.append(wordAfterApostrophe ? " d'" : " de");
    }
    String replacement = StringTools.preserveCase(suggBld.toString(), tokens[posWord].getToken());
    if (replacement.isEmpty()) {
      return null;
    }
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), tokens[posWord].getStartPos(),
        tokens[wordAfterIndex - 1].getEndPos() + (wordAfterApostrophe ? 1 : 0), match.getMessage(),
        match.getShortMessage());
    ruleMatch.setType(match.getType());
    ruleMatch.setSuggestedReplacement(replacement);
    return ruleMatch;
  }
}