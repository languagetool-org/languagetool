/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.language.German;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;

/**
 * Detect case errors like "Die Frau gibt ihre<b>n</b> Bruder den Hut." ()instead of "Die Frau gibt ihre<b>m</b> Bruder den Hut."
 * This rule needs noun phrases that are correct when not considering context, e.g. "die Frau", "ihren Bruder", "den Hut".
 * See {@link org.languagetool.rules.de.AgreementRule} for detecting noun phrases that are not correct by themselves ("der Auto").
 */
public class CaseGovernmentRule extends Rule {

  private final GermanTagger tagger = new GermanTagger();

  // Trying OpenNLP for German noun phrase detection. Erkenntnisse:
  // 1. Fehler eher kein Problem, die Tags sind so grob, dass auch "meine Fahrrad" erkannt wird
  // 2. Relativpronomen werden auch nur als ART erkannt ("Der Hund, der Eier legt.")

  // TODO:
  // -remove opennlp dependency?!
  //   -wie groß? => 10Mb (tokenizer, tagger, chunker)
  //   -wie langsam? => TODO
  //   -wie einfach zu ersetzen?
  // -Wie Einschübe überspringen? (OpenNLP macht es - z.T.? - automatisch)

  enum Case {
    NOM, AKK, DAT, GEN
  }

  static class ValencyData {
    Case aCase;
    boolean isRequired;
    ValencyData(Case aCase, boolean required) {
      this.aCase = aCase;
      this.isRequired = required;
    }
    @Override
    public String toString() {
      return isRequired ? aCase.toString() : "(" + aCase + ")";
    }
  }

  public CaseGovernmentRule() {
  }

  private static final ValencyData NOM = new ValencyData(Case.NOM, true);
  private static final ValencyData AKK = new ValencyData(Case.AKK, true);
  private static final ValencyData DAT = new ValencyData(Case.DAT, true);
  private static final ValencyData GEN = new ValencyData(Case.GEN, true);
  private static final ValencyData AKK_OPT = new ValencyData(Case.AKK, false);
  private static final ValencyData DAT_OPT = new ValencyData(Case.DAT, false);
  private static final ValencyData GEN_OPT = new ValencyData(Case.GEN, false);

  private static final Map<String, List<ValencyData>> valency = new HashMap<>();
  static {
    valency.put("geben", Arrays.asList(NOM, AKK, DAT_OPT, GEN_OPT));
    // Sie gibt ihr Geld.
    // Sie gibt ihr Geld einem Freund.
    // Sie gibt ihr Geld einem Freund ihres Mannes.
  }

  @Override
  public String getId() {
    return "CASE_GOVERNMENT";
  }

  @Override
  public String getDescription() {
    return "Prüft die Fälle zu einem Verb, z.B. 'geben' braucht Ergänzungen im Nominativ, Akkusativ und optional Dativ.";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    CheckResult result = checkGovernment(sentence);
    List<RuleMatch> ruleMatches = new ArrayList<>();
    if (result.getMissingSlots().size() > 0 || result.getUnexpectedSlots().size() > 0) {
      String message = "Das Verb '" + result.verbLemma + "' benötigt folgende Ergänzungen: " + result.verbCases + "." +
              " Gefunden wurden aber: FIXME";  // TODO: "Die Frau - Akkusativ oder Nominativ" etc.
      RuleMatch match = new RuleMatch(this, 0, 1, message);  // TODO: positions
      ruleMatches.add(match);
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public void reset() {
  }

  CheckResult checkGovernment(AnalyzedSentence sentence) throws IOException {
    String verbLemma = getVerb(sentence.getText());
    if (verbLemma == null) {
      System.err.println("No verb found: " + sentence);
      return null;
    }
    List<String> chunks = getChunks(sentence);
    List<Set<String>> cases = getCases(chunks);
    System.out.println("\nText   : " + sentence);
    System.out.println("Verb   : " + verbLemma);
    System.out.println("Chunks : " + chunks);
    System.out.println("Cases  : " + cases);
    List<ValencyData> verbCases = valency.get(verbLemma);
    if (verbCases == null) {
      // well, we have no data, so we cannot test anything
      return null;
    }
    System.out.println("Valency: " + verbCases);
    return checkCases(verbCases, cases, verbLemma);
  }

  private String getVerb(String sentence) throws IOException {
    JLanguageTool lt = new JLanguageTool(new German());
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(sentence);
    for (AnalyzedTokenReadings tokenReadings : analyzedSentence.getTokensWithoutWhitespace()) {
      for (AnalyzedToken tokenReading : tokenReadings) {
        String posTag = tokenReading.getPOSTag();
        if (posTag != null && posTag.startsWith("VER:") && !posTag.startsWith("VER:AUX")) {
          return tokenReading.getLemma();
        }
      }
    }
    return null;
  }

  List<String> getChunks(AnalyzedSentence analyzedSentence) throws IOException {
    List<String> result = new ArrayList<>();
    StringBuilder currentChunk = new StringBuilder();
    boolean inChunk = false;
    for (AnalyzedTokenReadings tokenReadings : analyzedSentence.getTokensWithoutWhitespace()) {
      List<ChunkTag> chunks = tokenReadings.getChunkTags();
      System.out.println(chunks + " " + tokenReadings.getToken());
      if (chunks.contains(new ChunkTag("B-NP"))) {
        if (currentChunk.length() > 0) {
          result.add(currentChunk.toString().trim());
          //System.out.println("=> " + currentChunk);
        }
        currentChunk.setLength(0);
        inChunk = true;
      } else if (chunks.contains(new ChunkTag("I-NP"))) {
        //
      } else {
        if (currentChunk.length() > 0) {
          result.add(currentChunk.toString().trim());
        }
        //System.out.println("=> " + currentChunk);
        currentChunk.setLength(0);
        inChunk = false;
      }
      if (inChunk) {
        //currentChunk += tokens[i] + " ";
        currentChunk.append(tokenReadings.getToken()).append(" ");
      }
    }
    return result;
  }

  private List<Set<String>> getCases(List<String> chunks) throws IOException {
    List<Set<String>> result = new ArrayList<>();
    for (String chunk : chunks) {
      System.out.println("---");
      String[] words = chunk.split(" ");
      Set<String> commonFeatures = new HashSet<>();
      int i = 0;
      for (String word : words) {
        AnalyzedTokenReadings lookup = tagger.lookup(word);
        if (i == 0 && lookup == null) {
          lookup = tagger.lookup(word.toLowerCase());  // try lowercase at sentence start
        }
        Set<String> tokenFeatures = getTokenFeatures(lookup);
        if (i == 0) {
          commonFeatures.addAll(tokenFeatures);
          System.out.println(lookup + " -> " + tokenFeatures);
        } else {
          commonFeatures.retainAll(tokenFeatures);
          System.out.println(lookup + " -> " + tokenFeatures + " retained: " + commonFeatures + "@" + chunk);
        }
        i++;
      }
      result.add(featuresToCases(commonFeatures));  // TODO: keep chunk
    }
    return result;
  }

  private Set<String> featuresToCases(Set<String> features) {
    Set<String> relevantFeatures = new HashSet<>(Arrays.asList("NOM", "AKK", "DAT", "GEN"));
    Set<String> result = new HashSet<>();
    for (String feature : features) {
      String[] parts = feature.split(":");
      for (String part : parts) {
        if (relevantFeatures.contains(part)) {
          result.add(part);
        }
      }
    }
    return result;
  }

  boolean findMatch(List<Set<String>> sentenceCasesList, List<String> expectedCases) {
    System.out.println(sentenceCasesList + " -- " + expectedCases);
    if (expectedCases.size() == 0) {
      return true;
    }
    String searchedCase = expectedCases.get(0);
    int i = 0;
    for (Set<String> sentenceCase : sentenceCasesList) {
      if (sentenceCase.contains(searchedCase)) {
        List<Set<String>> remaining = new ArrayList<>(sentenceCasesList);
        remaining.remove(i);
        if (findMatch(remaining, expectedCases.subList(1, expectedCases.size()))) {
          return true;
        }
      }
      i++;
    }
    return false;
  }

  private CheckResult checkCases(List<ValencyData> verbCases, List<Set<String>> sentenceCases, String verbLemma) {
    // TODO: use findMatch()
    List<CaseWithText> missingSlots = new ArrayList<>();
    List<CaseWithText> unexpectedSlots = new ArrayList<>();

    for (ValencyData verbCase : verbCases) {
      boolean slotFilled = false;
      for (Set<String> sentenceCase : sentenceCases) {
        if (sentenceCase.contains(verbCase.aCase.name())) {
          if (slotFilled) {
            unexpectedSlots.add(new CaseWithText(verbCase.aCase, "fixme"));   // TODO
          }
          slotFilled = true;
        }
      }
      if (!slotFilled && verbCase.isRequired) {
        missingSlots.add(new CaseWithText(verbCase.aCase, "fixme"));
      }
    }
    // TODO: unexpectedSlots?
    return new CheckResult(missingSlots, unexpectedSlots, verbLemma, verbCases);
  }

  private Set<String> getTokenFeatures(AnalyzedTokenReadings lookup) {
    Set<String> result = new HashSet<>();
    if (lookup == null) {
      return result;
    }
    Set<String> relevantFeatures = new HashSet<>(Arrays.asList(
            "NOM", "AKK", "DAT", "GEN",
            "MAS", "FEM", "NEU",
            "SIN", "PLU"));
    for (AnalyzedToken analyzedToken : lookup) {
      String pos = analyzedToken.getPOSTag();
      List<String> features = new ArrayList<>();
      if (pos != null) {
        String[] posParts = pos.split(":");
        for (String posPart : posParts) {
          if (relevantFeatures.contains(posPart)) {
            features.add(posPart);
          }
        }
      }
      //System.out.println("**features " + features + " in " + pos);
      result.add(StringTools.listToString(features, ":"));
    }
    return result;
  }

  public class CheckResult {

    private final String verbLemma;
    private final List<ValencyData> verbCases;
    private final List<CaseWithText> missingSlots;
    private final List<CaseWithText> unexpectedSlots;

    private CheckResult(List<CaseWithText> missingSlots, List<CaseWithText> unexpectedSlots, String verbLemma, List<ValencyData> verbCases) {
      this.missingSlots = missingSlots;
      this.unexpectedSlots = unexpectedSlots;
      this.verbLemma = verbLemma;
      this.verbCases = verbCases;
    }

    public List<CaseWithText> getMissingSlots() {
      return missingSlots;
    }

    public List<CaseWithText> getUnexpectedSlots() {
      return unexpectedSlots;
    }

    @Override
    public String toString() {
      return "missing=" + missingSlots + ", unexpected=" + unexpectedSlots;
    }
  }

  class CaseWithText {

    private final Case aCase;
    private final String phrase;

    CaseWithText(Case aCase, String phrase) {
      this.aCase = aCase;
      this.phrase = phrase;
    }

    @Override
    public String toString() {
      //return aCase + ": " + phrase;
      return aCase.toString();
    }
  }

}
