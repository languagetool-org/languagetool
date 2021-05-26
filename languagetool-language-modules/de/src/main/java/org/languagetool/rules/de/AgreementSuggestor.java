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

import org.jetbrains.annotations.NotNull;
import org.languagetool.*;
import org.languagetool.language.German;
import org.languagetool.rules.RuleMatch;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;

import static org.languagetool.rules.de.GermanHelper.getComparison;
import static org.languagetool.rules.de.GermanHelper.getDeterminerDefiniteness;

/**
 * Create suggestions for German noun phrases that lack agreement.
 * @since 2.4
 */
class AgreementSuggestor {

  private final Synthesizer synthesizer;
  private final AnalyzedTokenReadings determinerToken;
  private final AnalyzedTokenReadings adjToken;
  private final AnalyzedTokenReadings nounToken;
  private final AgreementRule.ReplacementType replacementType;
  private final AgreementRule agreementRule;
  private final JLanguageTool lt;
  
  private AnalyzedTokenReadings prepositionToken;

  AgreementSuggestor(Synthesizer synthesizer, AnalyzedTokenReadings determinerToken, AnalyzedTokenReadings nounToken,
                     AgreementRule.ReplacementType replacementType) {
    this(synthesizer, determinerToken, null, nounToken, replacementType);
  }

  /**
   * @since 5.4
   */
  AgreementSuggestor(Synthesizer synthesizer, AnalyzedTokenReadings determinerToken, AnalyzedTokenReadings adjToken, AnalyzedTokenReadings nounToken,
                     AgreementRule.ReplacementType replacementType) {
    this.synthesizer = synthesizer;
    this.determinerToken = determinerToken;
    this.adjToken = adjToken;
    this.nounToken = nounToken;
    this.replacementType = replacementType;
    German german = (German) Languages.getLanguageForShortCode("de");
    agreementRule = new AgreementRule(JLanguageTool.getMessageBundle(), german);
    agreementRule.disableSuggestions();
    lt = new JLanguageTool(german);
  }

  void setPreposition(AnalyzedTokenReadings prep) {
    this.prepositionToken = prep;
  }

  List<String> getSuggestions() {
    Set<String> suggestionSet = new HashSet<>();
    try {
      List<String> adjResult = new ArrayList<>();
      for (AnalyzedToken token2Reading : nounToken.getReadings()) {
        List<String> nounCases = new ArrayList<>();
        nounCases.add(GermanHelper.getNounCase(token2Reading.getPOSTag()));
        String nounNumber = GermanHelper.getNounNumber(token2Reading.getPOSTag());
        String nounGender = GermanHelper.getNounGender(token2Reading.getPOSTag());
        if (prepositionToken != null) {
          // some prepositions require specific cases, so only generated those:
          List<PrepositionToCases.Case> cases = PrepositionToCases.getCasesFor(prepositionToken.getToken());
          if (cases.size() > 0) {
            nounCases = new ArrayList<>();
            for (PrepositionToCases.Case aCase : cases) {
              String val = aCase.name().toLowerCase();
              if (!nounCases.contains(val)) {
                nounCases.add(val.toUpperCase());
              }
            }
          }
        }
        for (String nounCase : nounCases) {
          for (AnalyzedToken token1Reading : determinerToken.getReadings()) {
            List<String> articleSuggestions = getArticleSuggestions(nounCase, nounNumber, nounGender, token1Reading);
            suggestionSet.addAll(articleSuggestions);
            List<String> pronounSuggestions = getPronounSuggestions(nounCase, nounNumber, nounGender, token1Reading);
            suggestionSet.addAll(pronounSuggestions);
            List<String> nounSuggestions = getNounSuggestions(token2Reading, token1Reading);
            suggestionSet.addAll(nounSuggestions);
            if (adjToken != null) {
              fillAdjResult(suggestionSet, nounCase, nounNumber, nounGender, token1Reading, adjResult);
            }
          }
        }
      }
      if (adjToken != null) {
        return filterResult(adjResult);
      }
      List<String> suggestions = new ArrayList<>(filterResult(suggestionSet));
      Collections.sort(suggestions);
      return suggestions;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // over-generates, result needs to be filtered (i.e. erroneous entries be removed again):
  private void fillAdjResult(Set<String> suggestionSet, String nounCase, String nounNumber, String nounGender, AnalyzedToken detReading, List<String> adjResult) throws IOException {
    for (AnalyzedToken token : adjToken.getReadings()) {
      String correctPosTag1 = "ADJ:" + nounCase + ":" + nounNumber + ":" + nounGender + ":" + 
          getComparison(token.getPOSTag()) + ":" + getDeterminerDefiniteness(detReading.getPOSTag());
      inflect(suggestionSet, adjResult, token, correctPosTag1);
      String correctPosTag2 = "PA2:" + nounCase + ":" + nounNumber + ":" + nounGender + ":" + 
          getComparison(token.getPOSTag()) + ":" + getDeterminerDefiniteness(detReading.getPOSTag()) + ":VER";
      inflect(suggestionSet, adjResult, token, correctPosTag2);
    }
  }

  private void inflect(Set<String> suggestionSet, List<String> adjResult, AnalyzedToken token, String correctPosTag) throws IOException {
    String[] synthesized = synthesizer.synthesize(token, correctPosTag);
    for (String s : synthesized) {
      for (String res : suggestionSet) {
        String det = res.split(" ")[0];
        String adjRes = det + " " + s + " " + nounToken.getToken();
        if (!adjResult.contains(adjRes)) {
          adjResult.add(adjRes);
        }
      }
    }
  }

  @NotNull
  private List<String> filterResult(Collection<String> result) throws IOException {
    List<String> cleanResult = new ArrayList<>();
    for (String s : result) {
      RuleMatch[] matches = agreementRule.match(lt.getAnalyzedSentence(s));
      if (matches.length == 0) {
        cleanResult.add(s);
      }
    }
    return cleanResult;
  }

  private List<String> getArticleSuggestions(String nounCase, String nounNumber, String nounGender, AnalyzedToken article) throws IOException {
    String determinerDefiniteness = getDeterminerDefiniteness(article.getPOSTag());
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
      if (replacementType == AgreementRule.ReplacementType.Zur) {
        if (correctDeterminer.equals("der") && correctPosTag.contains(":SIN:")) {
          suggestions.add("zur " + nounToken.getToken());
        } else if (correctDeterminer.equals("dem")) {
          suggestions.add("zum " + nounToken.getToken());
        } else if (correctDeterminer.equals("den") || correctPosTag.contains(":SIN:")) {
          suggestions.add("zu " + correctDeterminer + " " + nounToken.getToken());
        } else if (correctPosTag.contains(":PLU:")) {
          suggestions.add("zu " + nounToken.getToken());
        }
      } else {
        suggestions.add(correctDeterminer + " " + nounToken.getToken());
      }
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
      String sugg = firstPart != null ?
              (token1.getToken() + " " + firstPart + correctedNoun) : (token1.getToken() + " " + correctedNoun);
      if (replacementType == AgreementRule.ReplacementType.Zur) {
        suggestions.add("zur " + sugg.replaceFirst("der ", ""));
      } else {
        suggestions.add(sugg);
      }
    }
    return suggestions;
  }
}
