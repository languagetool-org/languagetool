/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (www.danielnaber.de)
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

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.German;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.SuggestionFilter;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.GermanSynthesizer;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;

public class AdaptSuggestionFilter extends RuleFilter {
  
  private final static German german = new German();

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {
    RuleMatch newMatch = null;
    List<String> newSugg = new ArrayList<>();
    if (patternTokenPos > 0) {
      AnalyzedTokenReadings[] tokens = match.getSentence().getTokensWithoutWhitespace();
      AnalyzedTokenReadings prevToken = tokens[patternTokenPos-1];
      boolean detNoun = prevToken.hasPosTagStartingWith("ART:") || prevToken.hasPosTagStartingWith("PRO:");
      boolean detAdjNoun = patternTokenPos > 1
        && (tokens[patternTokenPos-2].hasPosTagStartingWith("ART:") || tokens[patternTokenPos-2].hasPosTagStartingWith("PRO:"))
        && prevToken.hasPosTagStartingWith("ADJ:");
      if (detNoun) {
        newMatch = new RuleMatch(match.getRule(), match.getSentence(), prevToken.getStartPos(), match.getToPos(), match.getMessage(), match.getShortMessage());
        for (String replacement : match.getSuggestedReplacements()) {
          List<String> adaptedDets = getAdaptedDet(prevToken, replacement);
          for (String adaptedDet : adaptedDets) {
            String newRepl = adaptedDet + " " + replacement;
            if (!newSugg.contains(newRepl)) {
              newSugg.add(newRepl);
            }
          }
        }
        newMatch.setSuggestedReplacements(newSugg);
      } else if (detAdjNoun && false) {   // TODO: needs more testing before commenting in
        AnalyzedTokenReadings prevPrevToken = tokens[patternTokenPos-2];
        newMatch = new RuleMatch(match.getRule(), match.getSentence(), prevPrevToken.getStartPos(), match.getToPos(), match.getMessage(), match.getShortMessage());
        for (String replacement : match.getSuggestedReplacements()) {
          List<String> adaptedDets = getAdaptedDetAdj(prevPrevToken, prevToken, replacement);
          for (String adaptedDet : adaptedDets) {
            newSugg.add(adaptedDet + " " + replacement);
          }
        }
      }
    }
    AgreementRule agreementRule = new AgreementRule(JLanguageTool.getMessageBundle(), german);
    SuggestionFilter suggestionFilter = new SuggestionFilter(agreementRule, german);
    if (newSugg.size() > 0) {
      if (StringTools.startsWithUppercase(newSugg.get(0))) {
        newSugg = suggestionFilter.filter(newSugg, "{} ist das.");
      } else {
        newSugg = suggestionFilter.filter(newSugg, "Das ist {}.");
      }
      newMatch.setSuggestedReplacements(newSugg);
      return newMatch;
    } else {
      return match;
    }
  }

  List<String> getAdaptedDet(AnalyzedTokenReadings detToken, String repl) {
    String oldDetBaseform = getBaseform(detToken, "(ART|PRO):.*");
    List<String> result = new ArrayList<>();
    try {
      String replGender = getNounGender(repl);
      if (replGender == null || oldDetBaseform == null) {
        return result;
      }
      for (AnalyzedToken reading : detToken.getReadings()) {
        if (reading.getPOSTag() == null || !(reading.getPOSTag().startsWith("ART:") || reading.getPOSTag().startsWith("PRO:"))) {
          continue;
        }
        String newDetPos = reading.getPOSTag().replaceAll("MAS|FEM|NEU", replGender).replaceFirst("BEG", "(BEG|B/S)").replaceFirst(":STV", "");
        String[] replDet = GermanSynthesizer.INSTANCE.synthesize(new AnalyzedToken(oldDetBaseform, null, oldDetBaseform), newDetPos, true);
        for (String s : replDet) {
          if (StringTools.startsWithUppercase(detToken.getToken())) {
            if (!s.toLowerCase().startsWith(detToken.getToken().substring(0, 1).toLowerCase())) {
              continue;  // see below
            }
            result.add(StringTools.uppercaseFirstChar(s));
          } else {
            if (!s.startsWith(detToken.getToken().substring(0, 1))) {
              continue;  // mein, dein, sein etc. all share the same lemma ("mein"), but don't suggest "dein" for "mein"
            }
            result.add(s);
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  List<String> getAdaptedDetAdj(AnalyzedTokenReadings detToken, AnalyzedTokenReadings adjToken, String repl) {
    String oldDetBaseform = getBaseform(detToken, "(ART|PRO):.*");
    String oldAdjBaseform = getBaseform(adjToken, "ADJ:.*");
    List<String> result = new ArrayList<>();
    try {
      String replGender = getNounGender(repl);
      if (replGender == null || oldDetBaseform == null || oldAdjBaseform == null) {
        return result;
      }
      for (AnalyzedToken reading : detToken.getReadings()) {
        if (reading.getPOSTag() == null || !reading.getPOSTag().matches("(ART|PRO):.*")) {
          continue;
        }
        String newDetPos = reading.getPOSTag().replaceAll("MAS|FEM|NEU", replGender).replaceFirst("BEG", "(BEG|B/S)");
        String newAdjPos;
        if (newDetPos.startsWith("ART:")) {
          newAdjPos = reading.getPOSTag().replaceAll("MAS|FEM|NEU", replGender).replaceFirst("BEG", "(BEG|B/S)").replaceFirst(":STV", "");
        } else if (newDetPos.startsWith("PRO:")) {
          newAdjPos = newDetPos.replaceAll("PRO:POS:(NOM|AKK|GEN|DAT):(SIN|PLU):(MAS|FEM|NEU)", "ADJ:$1:$2:$3").replaceFirst(":(STV|BEG).*", ":GRU:IND");
        } else {
          throw new RuntimeException("Unexpected POS tag: " + newDetPos);
        }
        //System.out.println("newDetPos: " + newDetPos + " for " + oldDetBaseform);
        //System.out.println("newAdjPos: " + newAdjPos + " for " + oldAdjBaseform);
        String[] replDet = GermanSynthesizer.INSTANCE.synthesize(new AnalyzedToken(oldDetBaseform, null, oldDetBaseform), newDetPos, true);
        String[] replAdj = GermanSynthesizer.INSTANCE.synthesize(new AnalyzedToken(oldAdjBaseform, null, oldAdjBaseform), newAdjPos, true);
        //System.out.println("replDet: " + Arrays.toString(replDet));
        //System.out.println("replAdj: " + Arrays.toString(replAdj));
        for (String det : replDet) {
          if (!det.startsWith(detToken.getToken().substring(0, 1))) {
            continue;  // mein, dein, sein etc. all share the same lemma ("mein"), but don't suggest "dein" for "mein"
          }
          for (String adj : replAdj) {
            String newDetAdj = det + " " + adj;
            if (!result.contains(newDetAdj)) {
              result.add(newDetAdj);
            }
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  private String getBaseform(AnalyzedTokenReadings token, String tagStartsWith) {
    String baseform = null;
    for (AnalyzedToken reading : token.getReadings()) {
      if (reading.getPOSTag() != null && reading.getPOSTag().matches(tagStartsWith)) {
        baseform = reading.getLemma();
      }
      // TODO: what if more than one match?
    }
    return baseform;
  }

  @Nullable
  private String getNounGender(String word) throws IOException {
    List<AnalyzedTokenReadings> readings = GermanTagger.INSTANCE.tag(Collections.singletonList(word));
    for (AnalyzedTokenReadings atr : readings) {
      if (atr.getReadings().size() > 0) {
        String pos = atr.getReadings().get(0).getPOSTag();
        if (pos != null && pos.startsWith("SUB:")) {
          String[] parts = pos.split(":");
          if (parts.length >= 4) {
            return parts[3];
          }
        }
      }
    }
    return null;
  }
}
