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
package org.languagetool.rules.de;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;

/**
 * Create suggestions for German noun phrases that lack agreement.
 * @since 2.4
 */
class AgreementSuggestor {

  private final Synthesizer synthesizer;
  private final AnalyzedTokenReadings determinerToken;
  private final AnalyzedTokenReadings nounToken;

  AgreementSuggestor(Synthesizer synthesizer, AnalyzedTokenReadings determinerToken, AnalyzedTokenReadings nounToken) {
    this.synthesizer = synthesizer;
    this.determinerToken = determinerToken;
    this.nounToken = nounToken;
  }

  List<String> getSuggestions() {
    Set<String> suggestionSet = new HashSet<>();
    try {
      for (AnalyzedToken token2Reading : nounToken.getReadings()) {
        String nounCase = GermanHelper.getNounCase(token2Reading.getPOSTag());
        String nounNumber = GermanHelper.getNounNumber(token2Reading.getPOSTag());
        String nounGender = GermanHelper.getNounGender(token2Reading.getPOSTag());
        for (AnalyzedToken token1Reading : determinerToken.getReadings()) {
          List<String> articleSuggestions = getArticleSuggestions(nounCase, nounNumber, nounGender, token1Reading);
          suggestionSet.addAll(articleSuggestions);
          List<String> pronounSuggestions = getPronounSuggestions(nounCase, nounNumber, nounGender, token1Reading);
          suggestionSet.addAll(pronounSuggestions);
          List<String> nounSuggestions = getNounSuggestions(token2Reading, token1Reading);
          suggestionSet.addAll(nounSuggestions);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    List<String> suggestions = new ArrayList<>(suggestionSet);
    Collections.sort(suggestions);
    return suggestions;
  }

  private List<String> getArticleSuggestions(String nounCase, String nounNumber, String nounGender, AnalyzedToken article) throws IOException {
    String determinerDefiniteness = GermanHelper.getDeterminerDefiniteness(article.getPOSTag());
    if (StringTools.isEmpty(determinerDefiniteness)) {
      return Collections.emptyList();
    }
    List<String> result = new ArrayList<>();
    String correctPosTag1 = "ART:" + determinerDefiniteness + ":" + nounCase + ":" + nounNumber + ":" + nounGender;
    result.addAll(getDeterminerSuggestionsForPosTag(article, correctPosTag1, null));
    String correctPosTag2 = "PRO:DEM:" + nounCase + ":" + nounNumber + ":" + nounGender + ":B/S";
    result.addAll(getDeterminerSuggestionsForPosTag(article, correctPosTag2, null));
    return result;
  }

  private List<String> getPronounSuggestions(String nounCase, String nounNumber, String nounGender, AnalyzedToken pronoun) throws IOException {
    String correctPosTag = "PRO:POS:" + nounCase + ":" + nounNumber + ":" + nounGender + ":BEG";  // BEG = begleitend
    return getDeterminerSuggestionsForPosTag(pronoun, correctPosTag, determinerToken.getToken().substring(0, 1));
  }

  private List<String> getNounSuggestions(AnalyzedToken token2Reading, AnalyzedToken determiner) throws IOException {
    if (determiner.getPOSTag() != null && determiner.getPOSTag().endsWith(":STV")) {  // STV = stellvertretend
      return Collections.emptyList();
    }
    String determinerCase = GermanHelper.getDeterminerCase(determiner.getPOSTag());
    String determinerNumber = GermanHelper.getDeterminerNumber(determiner.getPOSTag());
    String determinerGender = GermanHelper.getDeterminerGender(determiner.getPOSTag());
    String correctPosTag = "SUB:" + determinerCase + ":" + determinerNumber + ":" + determinerGender;
    return getNounSuggestionsForPosTag(determinerToken, token2Reading, correctPosTag);
  }

  private List<String> getDeterminerSuggestionsForPosTag(AnalyzedToken token1Reading, String correctPosTag, String startsWith) throws IOException {
    List<String> suggestions = new ArrayList<>();
    String[] correctedDeterminer = synthesizer.synthesize(token1Reading, correctPosTag);
    for (String determiner : correctedDeterminer) {
      if (startsWith != null && !determiner.startsWith(startsWith)) {
        // ignore so we don't create suggestions like "unsere" for "mein":
        continue;
      }
      String correctDeterminer = StringTools.isCapitalizedWord(determinerToken.getToken()) ? StringTools.uppercaseFirstChar(determiner) : determiner;
      suggestions.add(correctDeterminer + " " + nounToken.getToken());
    }
    return suggestions;
  }

  private List<String> getNounSuggestionsForPosTag(AnalyzedTokenReadings token1, AnalyzedToken token2Reading, String correctPosTag) throws IOException {
    List<String> suggestions = new ArrayList<>();
    String[] correctedNouns = synthesizer.synthesize(token2Reading, correctPosTag);
    String firstPart = null;
    if (correctedNouns.length == 0 && token2Reading.getToken().contains("-")) {
      // e.g. "LAN-Kabel", which is not in dictionary, but "Kabel" is:
      firstPart = token2Reading.getToken().substring(0, token2Reading.getToken().lastIndexOf('-') + 1);
      String lastTokenPart = token2Reading.getToken().replaceFirst(".*-", "");
      String lastLemmaPart = token2Reading.getLemma() != null ? token2Reading.getLemma().replaceFirst(".*-", "") : null;
      correctedNouns = synthesizer.synthesize(new AnalyzedToken(lastTokenPart, token2Reading.getPOSTag(), lastLemmaPart), correctPosTag);
    }
    for (String correctedNoun : correctedNouns) {
      if (firstPart != null) {
        suggestions.add(token1.getToken() + " " + firstPart  + correctedNoun);
      } else {
        suggestions.add(token1.getToken() + " " + correctedNoun);
      }
    }
    return suggestions;
  }

}
