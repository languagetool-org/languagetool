/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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

import com.google.common.base.Suppliers;
import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.rules.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Finds spellings that were only correct in the pre-reform orthography.
 * @since 3.8
 */
public class OldSpellingRule extends Rule {

  private static final String FILE_PATH = "/de/alt_neu.csv";
  private static final List<String> EXCEPTIONS = Arrays.asList(
    "Schloß Holte",
    "Schloß Neuhaus",
    "Schloß Ricklingen",
    "Schloß-Nauses",
    "Schloß Rötteln",
    "Klinikum Schloß Winnenden",
    "Grazer Schloßberg",
    "Höchster Schloß",
    "Bell Telephone",
    "Telephone Company",
    "American Telephone",
    "England Telephone",
    "Mobile Telephone",
    "Cordless Telephone",
    "Telephone Line",
    "World Telephone",
    "Tip Top",
    "Hans Joachim Blaß"
  );
  private static final Supplier<SpellingData> DATA = Suppliers.memoize(() -> new SpellingData(FILE_PATH));
  private static final Pattern CHARS = Pattern.compile("[a-zA-Zöäüß]");

  private final Language language;

  public OldSpellingRule(ResourceBundle messages, Language language) {
    this.language = language;
    setCategory(Categories.TYPOS.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(Example.wrong("Der <marker>Abfluß</marker> ist schon wieder verstopft."),
                   Example.fixed("Der <marker>Abfluss</marker> ist schon wieder verstopft."));
  }

  @Override
  public String getId() {
    return "OLD_SPELLING_RULE";
  }

  @Override
  public String getDescription() {
    return "Findet Schreibweisen, die nur in der alten Rechtschreibung gültig waren";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> matches = new ArrayList<>();
    String text = sentence.getText();
    List<AhoCorasickDoubleArrayTrie.Hit<String>> hits = DATA.get().getTrie().parseText(text);
    List<AhoCorasickDoubleArrayTrie.Hit<String>> sentStartHits = DATA.get().getSentenceStartTrie().parseText(text);
    Set<Integer> startPositions = new HashSet<>();
    Collections.reverse(hits);  // work on longest matches first
    for (AhoCorasickDoubleArrayTrie.Hit<String> hit : hits) {
      if (startPositions.contains(hit.begin)) {
        continue;   // avoid overlapping matches
      }
      if (!ignoreMatch(hit, text)) {
        addMatch(sentence, hit, matches);
        startPositions.add(hit.begin);
      }
    }
    for (AhoCorasickDoubleArrayTrie.Hit<String> hit : sentStartHits) {
      if (startPositions.contains(hit.begin)) {
        continue;   // avoid overlapping matches
      }
      if (hit.begin == 0 && !ignoreMatch(hit, text)) {   // e.g. "Läßt du das bitte", i.e. uppercase at start of sentence
        addMatch(sentence, hit, matches);
        break;  // there can only be one match at the start of a sentence
      }
    }
    return toRuleMatchArray(matches);
  }

  private void addMatch(AnalyzedSentence sentence, AhoCorasickDoubleArrayTrie.Hit<String> hit, List<RuleMatch> matches) {
    String message = "Diese Schreibweise war nur in der alten Rechtschreibung korrekt.";
    RuleMatch match = new RuleMatch(this, sentence, hit.begin, hit.end, message, "Alte Rechtschreibung");
    String[] suggestions = hit.value.split("\\|");
    match.setSuggestedReplacements(Arrays.asList(suggestions));
    String covered = sentence.getText().substring(hit.begin, hit.end);
    if (suggestions.length > 0 && suggestions[0].replaceFirst("ss", "ß").equals(covered)) {
      if (language.getShortCodeWithCountryAndVariant().equals("de-AT") && covered.toLowerCase().contains("geschoß")) {
        // special case for Austria: "Geschoß" is correct in both old and new spelling in de-AT (because of the pronunciation)
        return;
      } else {
        match.setMessage(message + " Das Wort wird mit 'ss' geschrieben, wenn davor eine kurz gesprochene Silbe steht.");
      }
    }
    matches.add(match);
  }

  private boolean ignoreMatch(AhoCorasickDoubleArrayTrie.Hit<String> hit, String text) {
    for (String exception : EXCEPTIONS) {
      if (text.regionMatches(true, hit.begin, exception, 0, exception.length()) ||
        text.regionMatches(true, hit.end - exception.length(), exception, 0, exception.length())) {
        return true;
      }
    }
    if (hit.begin > 0 && !isBoundary(text.substring(hit.begin-1, hit.begin))) {  // prevent substring matches
      return true;
    }
    if (hit.end < text.length() && !isBoundary(text.substring(hit.end, hit.end+1))) {  // prevent substring matches, e.g. "Foto" for "Photons"
      return true;
    }
    if (hit.begin-6 >= 0) {
      if (text.startsWith("Prof.", hit.begin-6)) {
        return true;
      }
    }
    if (hit.begin-5 >= 0) {
      String before5 = text.substring(hit.begin-5, hit.begin-1);
      if (before5.equals("Herr") || before5.equals("Frau")) {
        return true;
      }
    }
    if (hit.begin-4 >= 0) {
      String before4 = text.substring(hit.begin-4, hit.begin-1);
      if (before4.equals("Hr.") || before4.equals("Fr.") || before4.equals("Dr.")) {
        return true;
      }
    }
    return false;
  }

  private boolean isBoundary(String s) {
    return !CHARS.matcher(s).matches();
  }
  
}
