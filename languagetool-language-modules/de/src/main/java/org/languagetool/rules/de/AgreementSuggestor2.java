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
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.*;

/**
 * Create suggestions for German noun phrases that lack agreement.
 */
class AgreementSuggestor2 {

  private final static String detTemplate = "ART:IND/DEF:NOM/AKK/DAT/GEN:SIN/PLU:MAS/FEM/NEU";
  private final static String proPosTemplate = "PRO:POS:NOM/AKK/DAT/GEN:SIN/PLU:MAS/FEM/NEU:BEG";
  private final static List<String> proDemTemplates = Arrays.asList(
    "PRO:DEM:NOM/AKK/DAT/GEN:SIN/PLU:MAS/FEM/NEU:BEG",
    "PRO:DEM:NOM/AKK/DAT/GEN:SIN/PLU:MAS/FEM/NEU:B/S");
  private final static List<String> proIndTemplates = Arrays.asList(
    "PRO:IND:NOM/AKK/DAT/GEN:SIN/PLU:MAS/FEM/NEU:BEG",
    "PRO:IND:NOM/AKK/DAT/GEN:SIN/PLU:MAS/FEM/NEU:B/S"
  );
  private final static String adjTemplate = "ADJ:NOM/AKK/DAT/GEN:SIN/PLU:MAS/FEM/NEU:GRU:IND/DEF";
  private final static String pa2Template = "PA2:NOM/AKK/DAT/GEN:SIN/PLU:MAS/FEM/NEU:GRU:IND/DEF:VER";
  private final static List<String> nounTemplates = Arrays.asList(
    "SUB:NOM/AKK/DAT/GEN:SIN/PLU:MAS/FEM/NEU",
    "SUB:NOM/AKK/DAT/GEN:SIN/PLU:MAS/FEM/NEU:INF");   // INF is for cases like "das Züchten" etc.
  private final static List<String> number = Arrays.asList("SIN", "PLU");
  private final static List<String> gender = Arrays.asList("MAS", "FEM", "NEU");
  private final static List<String> cases = Arrays.asList("NOM", "AKK", "DAT", "GEN");
  private final static List<String> nounCases = Arrays.asList("NOM", "AKK", "DAT", "GEN");

  private final Synthesizer synthesizer;
  private final AnalyzedTokenReadings determinerToken;
  private final AnalyzedTokenReadings adjToken;
  private final AnalyzedTokenReadings nounToken;
  private final AgreementRule.ReplacementType replacementType;

  private AnalyzedTokenReadings prepositionToken;

  AgreementSuggestor2(Synthesizer synthesizer, AnalyzedTokenReadings determinerToken, AnalyzedTokenReadings nounToken,
                      AgreementRule.ReplacementType replacementType) {
    this(synthesizer, determinerToken, null, nounToken, replacementType);
  }

  /** @since 5.4 */
  AgreementSuggestor2(Synthesizer synthesizer, AnalyzedTokenReadings determinerToken, AnalyzedTokenReadings adjToken, AnalyzedTokenReadings nounToken,
                      AgreementRule.ReplacementType replacementType) {
    this.synthesizer = synthesizer;
    this.determinerToken = determinerToken;
    this.adjToken = adjToken;
    this.nounToken = nounToken;
    this.replacementType = replacementType;
  }

  void setPreposition(AnalyzedTokenReadings prep) {
    this.prepositionToken = prep;
  }

  List<String> getSuggestions() {
    return getSuggestions(false);
  }

  /**
   * @param filter to filter only the best suggestions, as in: if there are suggestions that replace only
   *               one word in the original phrase, don't return those that replace two words etc.
   */
  List<String> getSuggestions(boolean filter) {
    try {
      List<Suggestion> suggestions = getSuggestionsInternal();
      sort(suggestions);  // sort so that suggestions with fewer edits come first
      if (filter) {
        List<Suggestion> filteredSuggestions = new ArrayList<>();
        int prevCorrections = suggestions.size() > 0 ? suggestions.get(0).corrections : 0;
        boolean hadRealSuggestions = false;
        for (Suggestion suggestion : suggestions) {
          if (hadRealSuggestions && suggestion.corrections > prevCorrections) {
            break;
          }
          hadRealSuggestions = suggestion.corrections > 0;
          filteredSuggestions.add(suggestion);
        }
        return filteredSuggestions.stream().map(k -> k.phrase).collect(Collectors.toList());
      } else {
        return suggestions.stream().map(k -> k.phrase).collect(Collectors.toList());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private List<Suggestion> getSuggestionsInternal() throws IOException {
    List<String> nounCases = getNounCases();
    List<Suggestion> result = new ArrayList<>();
    for (String num : number) {
      for (String gen : gender) {
        for (String aCase : cases) {
          if (!nounCases.contains(aCase)) {
            continue;
          }
          for (AnalyzedToken detReading : determinerToken.getReadings()) {
            String[] detSynthesized = getDetOrPronounSynth(num, gen, aCase, detReading);
            String[] adjSynthesized = getAdjSynth(num, gen, aCase, detReading);
            String[] nounSynthesized = getNounSynth(num, gen, aCase);
            combineSynth(result, detSynthesized, adjSynthesized, nounSynthesized);
          }
        }
      }
    }
    return result;
  }

  private List<String> getNounCases() {
    if (prepositionToken != null &&
        replacementType == null) {  // "bis zur" = "bis" must not be considered preposition: "zur" = "zu der"
      // some prepositions require specific cases, so only generated those:
      List<String> result = new ArrayList<>();
      List<PrepositionToCases.Case> casesForToken = PrepositionToCases.getCasesFor(prepositionToken.getToken());
      if (casesForToken.size() > 0) {
        for (PrepositionToCases.Case aCase : casesForToken) {
          String val = aCase.name().toLowerCase();
          if (!result.contains(val)) {
            result.add(val.toUpperCase());
          }
        }
        return result;
      }
    }
    return AgreementSuggestor2.nounCases;
  }

  private String[] getDetOrPronounSynth(String num, String gen, String aCase, AnalyzedToken detReading) throws IOException {
    String detPos = detReading.getPOSTag();
    if (detPos == null) {
      return new String[]{};
    }
    boolean isDef = detPos.contains(":DEF:");
    List<String> templates;
    if (detPos.contains("ART:")) {
      templates = singletonList(detTemplate);
    } else if (detPos.contains("PRO:POS:")) {
      templates = singletonList(proPosTemplate);
    } else if (detPos.contains("PRO:DEM:")) {
      templates = proDemTemplates;
    } else if (detPos.contains("PRO:IND:")) {
      templates = proIndTemplates;
    } else if (detReading.getToken().equals("zur")) {
      templates = singletonList(detTemplate);
      detReading = new AnalyzedToken("der", "", "der");
      isDef = true;
    } else {
      return new String[]{};
    }
    List<String> synthesized = new ArrayList<>();
    for (String template : templates) {
      template = template.replaceFirst("IND/DEF", isDef ? "DEF" : "IND");
      String pos = replaceVars(template, num, gen, aCase);
      String[] tmp = synthesizer.synthesize(detReading, pos);
      String origFirstChar = detReading.getToken().substring(0, 1);
      if (replacementType == AgreementRule.ReplacementType.Zur) {
        List<String> adaptedDet = new ArrayList<>();
        for (String synthesizeDet : tmp) {
          if (synthesizeDet.equals("der") && pos.contains(":SIN:") && pos.contains(":MAS")) {
            adaptedDet.add("zum");
          } else if (synthesizeDet.equals("der") && pos.contains(":SIN:") && pos.contains(":FEM")) {
            adaptedDet.add("zur");
          } else if (synthesizeDet.equals("dem")) {
            adaptedDet.add("zum");
          } else if (synthesizeDet.equals("den") && pos.contains(":PLU:")) {
            adaptedDet.add("zu " + synthesizeDet);
          }
        }
        synthesized.addAll(adaptedDet);
      } else {
        synthesized.addAll(Arrays.stream(tmp)
          .filter(k -> k.toLowerCase().startsWith(origFirstChar.toLowerCase()))
          .map(k -> Character.isUpperCase(origFirstChar.charAt(0)) ? StringTools.uppercaseFirstChar(k) : k)  // don't suggest "dein" for "mein" etc.
          .collect(Collectors.toList()));
      }
    }
    return synthesized.toArray(new String[0]);
  }

  private String[] getAdjSynth(String num, String gen, String aCase, AnalyzedToken detReading) throws IOException {
    List<String> adjSynthesized = new ArrayList<>();
    if (adjToken != null) {
      for (AnalyzedToken adjReading : adjToken.getReadings()) {
        if (adjReading.getPOSTag() == null || detReading.getPOSTag() == null) {
          continue;
        }
        boolean detIsDef = detReading.getPOSTag().contains(":DEF:");
        String template = adjReading.getPOSTag().startsWith("PA2") ? pa2Template : adjTemplate;
        if (adjReading.getPOSTag().contains(":KOM:")) {
          template = template.replace(":GRU:", ":KOM:");
        } else if (adjReading.getPOSTag().contains(":SUP:")) {
          template = template.replace(":GRU:", ":SUP:");
        }
        template = template.replaceFirst("IND/DEF", detIsDef ? "DEF" : "IND");
        String adjPos = replaceVars(template, num, gen, aCase);

        String[] synthesize = synthesizer.synthesize(adjReading, adjPos);
        for (String synthNoun : synthesize) {
          if (!adjSynthesized.contains(synthNoun)) {
            adjSynthesized.add(synthNoun);
          }
        }
      }
    } else {
      adjSynthesized.add("");  // noun phrase without an adjective
    }
    return adjSynthesized.toArray(new String[0]);
  }

  private String[] getNounSynth(String num, String gen, String aCase) throws IOException {
    AnalyzedToken nounReading = nounToken.getReadings().get(0);
    List<String> result = new ArrayList<>();
    for (String nounTemplate : nounTemplates) {
      String nounPos = replaceVars(nounTemplate, num, gen, aCase);
      String[] nounSynthesized = synthesizer.synthesize(nounReading, nounPos);
      if (nounSynthesized.length == 0 && nounReading.getToken().contains("-")) {
        String firstPart = nounReading.getToken().substring(0, nounReading.getToken().lastIndexOf('-') + 1);
        String lastTokenPart = nounToken.getToken().replaceFirst(".*-", "");
        String lastLemmaPart = nounReading.getLemma() != null ? nounReading.getLemma().replaceFirst(".*-", "") : null;
        nounSynthesized = synthesizer.synthesize(new AnalyzedToken(lastTokenPart, "fake_value", lastLemmaPart), nounPos);
        for (String lastPartInflected : nounSynthesized) {
          result.add(firstPart + lastPartInflected);
        }
      } else {
        result.addAll(Arrays.asList(nounSynthesized));
      }
    }
    return result.toArray(new String[0]);
  }

  private void combineSynth(List<Suggestion> result, String[] detSynthesized, String[] adjSynthesized, String[] nounSynthesized) {
    for (String detSynthesizedElem : detSynthesized) {
      for (String adjSynthesizedElem : adjSynthesized) {
        for (String nounSynthesizedElem : nounSynthesized) {
          String elem = adjSynthesizedElem.isEmpty() ?
              detSynthesizedElem + " " + nounSynthesizedElem :
              detSynthesizedElem + " " + adjSynthesizedElem + " " + nounSynthesizedElem;
          int corrections = (detSynthesizedElem.equals(determinerToken.getToken()) ? 0 : 1) +
                            (adjToken == null || adjSynthesizedElem.equals(adjToken.getToken()) ? 0 : 1) +
                            (nounSynthesizedElem.equals(nounToken.getToken()) ? 0 : 1);
          if (corrections == 0) {
            continue;
          }
          Suggestion suggestion = new Suggestion(elem, corrections);
          if (!result.contains(suggestion)) {
            result.add(suggestion);
          }
        }
      }
    }
  }

  private String replaceVars(String template, String num, String gen, String aCase) {
    return template.replaceFirst("SIN/PLU", num).replaceFirst("MAS/FEM/NEU", gen).replaceFirst("NOM/AKK/DAT/GEN", aCase);
  }

  private static class Suggestion implements Comparable<Suggestion> {
    String phrase;
    int corrections;
    Suggestion(String phrase, int corrections) {
      this.phrase = Objects.requireNonNull(phrase);
      this.corrections = corrections;
    }
    @Override
    public int compareTo(@NotNull Suggestion o) {
      return corrections - o.corrections;
    }
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Suggestion that = (Suggestion) o;
      return corrections == that.corrections && phrase.equals(that.phrase);
    }
    @Override
    public int hashCode() {
      return Objects.hash(phrase, corrections);
    }
    @Override
    public String toString() {
      return phrase + "/c=" + corrections;
    }
  }

}
