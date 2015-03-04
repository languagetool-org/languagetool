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

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
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
 * @since 2.9
 */
public class CaseGovernmentRule extends Rule {

  private static final ChunkTag B_NP = new ChunkTag("B-NP");
  private static final ChunkTag I_NP = new ChunkTag("I-NP");
  private static final ChunkTag PP = new ChunkTag("PP");

  private final GermanTagger tagger = new GermanTagger();

  private boolean debug;

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
    NOM("Nominativ"), AKK("Akkusativ"), DAT("Dativ"), GEN("Genitiv");

    private final String longForm;
    Case(String longForm) {
      this.longForm = longForm;
    }
    String getLongForm() {
      return longForm;
    }
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

  public CaseGovernmentRule(ResourceBundle messages) {
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
    // TODO: wir brauchen Valenzdaten für viele Verben
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
    if (result != null && !result.correct && !hasUnknownToken(sentence.getTokensWithoutWhitespace())) {
      String message = "Das Verb '" + result.verbLemma + "' benötigt folgende Ergänzungen: " +
              getExpectedStrings(result.verbCases) + "." +
              " Gefunden wurden aber: " + getSentenceStrings(result.analyzedChunks);
      RuleMatch match = new RuleMatch(this, 0, 1, message);  // TODO: positions
      ruleMatches.add(match);
    }
    return toRuleMatchArray(ruleMatches);
  }

  /** @deprecated use for development only */
  void setDebug(boolean debug) {
    this.debug = debug;
  }

  // If there's an unknown word we cannot detect the noun phrases, so we have to ignore the sentence...
  private boolean hasUnknownToken(AnalyzedTokenReadings[] tokens) {
    for (AnalyzedTokenReadings token : tokens) {
      for (AnalyzedToken analyzedToken : token.getReadings()) {
        if (analyzedToken.hasNoTag()) {
          return true;
        }
      }
    }
    return false;
  }

  private String getExpectedStrings(List<ValencyData> result) {
    List<String> longForms = new ArrayList<>();
    for (ValencyData valency : result) {
      if (valency.isRequired) {
        longForms.add(valency.aCase.getLongForm());
      } else {
        longForms.add("optional: " + valency.aCase.getLongForm());
      }
    }
    return StringTools.listToString(longForms, ", ");
  }

  private String getSentenceStrings(List<AnalyzedChunk> chunks) {
    List<String> longForms = new ArrayList<>();
    for (AnalyzedChunk chunk : chunks) {
      List<String> casesStr = new ArrayList<>();
      for (Case aCase : chunk.cases) {
        casesStr.add(aCase.getLongForm());
      }
      longForms.add(StringTools.listToString(casesStr, "/"));
    }
    return StringTools.listToString(longForms, ", ");
  }

  @Override
  public void reset() {
  }

  @Nullable
  CheckResult checkGovernment(AnalyzedSentence sentence) throws IOException {
    String verbLemma = getVerb(sentence.getText());
    if (verbLemma == null) {
      return null;
    }
    List<Chunk> chunks = getChunks(sentence);
    List<AnalyzedChunk> cases = getAnalyzedChunks(chunks);
    if (debug) {
      System.out.println("\nText   : " + sentence.getText());
      System.out.println("Verb   : " + verbLemma);
      System.out.println("Chunks : " + StringUtils.join(chunks, ", "));
      System.out.println("Cases  : " + StringUtils.join(cases, ", "));
      System.out.println("Analysis:");
      for (AnalyzedTokenReadings analyzedTokens : sentence.getTokensWithoutWhitespace()) {
        System.out.println("  " + analyzedTokens);
      }
    }
    List<ValencyData> verbCases = valency.get(verbLemma);
    if (verbCases == null) {
      // well, we have no data, so we cannot test anything
      return null;
    }
    if (debug) {
      System.out.println("Valency: " + StringUtils.join(verbCases, ", "));
    }
    boolean correct = checkCases(cases, verbCases);
    return new CheckResult(correct, cases, verbCases, verbLemma);
  }

  private String getVerb(String sentence) throws IOException {
    JLanguageTool lt = new JLanguageTool(new German());
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(sentence);
    for (AnalyzedTokenReadings tokenReadings : analyzedSentence.getTokensWithoutWhitespace()) {
      for (AnalyzedToken tokenReading : tokenReadings) {
        String posTag = tokenReading.getPOSTag();
        if (posTag != null && posTag.startsWith("VER:") && !posTag.startsWith("VER:AUX") && !posTag.startsWith("VER:MOD")) {
          return tokenReading.getLemma();
        }
      }
    }
    return null;
  }

  List<Chunk> getChunks(AnalyzedSentence analyzedSentence) throws IOException {
    List<Chunk> result = new ArrayList<>();
    StringBuilder currentChunk = new StringBuilder();
    boolean currentChunkPrecededByComma = false;
    boolean inChunk = false;
    boolean prevIsComma = false;
    for (AnalyzedTokenReadings tokenReadings : analyzedSentence.getTokensWithoutWhitespace()) {
      List<ChunkTag> chunks = tokenReadings.getChunkTags();
      //System.out.println(chunks + " " + tokenReadings.getToken());
      if (chunks.contains(B_NP) && !chunks.contains(PP)) {
        if (currentChunk.length() > 0) {
          result.add(new Chunk(currentChunk.toString().trim(), currentChunkPrecededByComma));
        }
        currentChunk.setLength(0);
        currentChunkPrecededByComma = false;
        inChunk = true;
      } else {
        if (chunks.contains(I_NP)) {
          //
        } else {
          if (currentChunk.length() > 0) {
            result.add(new Chunk(currentChunk.toString().trim(), currentChunkPrecededByComma));
          }
          currentChunk.setLength(0);
          currentChunkPrecededByComma = false;
          inChunk = false;
        }
      }
      if (inChunk) {
        currentChunk.append(tokenReadings.getToken()).append(" ");
        if (prevIsComma) {
          currentChunkPrecededByComma = true;
        }
      }
      prevIsComma = tokenReadings.getToken().equals(",");
    }
    if (currentChunk.length() > 0) {
      result.add(new Chunk(currentChunk.toString().trim(), currentChunkPrecededByComma));
    }
    return result;
  }

  /**
   * Get the chunk's case (often ambiguous).
   */
  List<AnalyzedChunk> getAnalyzedChunks(List<Chunk> chunks) throws IOException {
    List<AnalyzedChunk> result = new ArrayList<>();
    for (Chunk chunk : chunks) {
      //System.out.println("---");
      String[] words = chunk.chunk.split(" ");
      Set<String> commonFeatures = null;
      int i = 0;
      for (String word : words) {
        AnalyzedTokenReadings lookup = tagger.lookup(word);
        if (i == 0 && chunk.precededByComma) {
          // "die Frau, die Wasser trinkt"
          i++;
          continue;
        }
        if (i == 0 && lookup == null) {
          lookup = tagger.lookup(word.toLowerCase());  // try lowercase at sentence start
        }
        Set<String> tokenFeatures = getTokenFeatures(lookup);
        if (tokenFeatures.size() > 0 ) {  // e.g. for adverbs (ADV) there are no features
          if (commonFeatures == null) {
            commonFeatures = new HashSet<>();
            commonFeatures.addAll(tokenFeatures);
          } else {
            commonFeatures.retainAll(tokenFeatures);
          }
        }
        i++;
      }
      if (commonFeatures != null) {
        AnalyzedChunk analyzedChunk = featuresToCases(commonFeatures, chunk);
        result.add(analyzedChunk);
      }
    }
    return result;
  }

  private AnalyzedChunk featuresToCases(Set<String> features, Chunk chunk) {
    Set<String> relevantFeatures = new HashSet<>(Arrays.asList("NOM", "AKK", "DAT", "GEN"));
    Set<Case> result = new HashSet<>();
    for (String feature : features) {
      String[] parts = feature.split(":");
      for (String part : parts) {
        if (relevantFeatures.contains(part)) {
          result.add(Case.valueOf(part));
        }
      }
    }
    return new AnalyzedChunk(chunk, result);
  }

  /**
   * Find the given {@code expectedCases} in {@code analyzedChunks} so that
   * one set from {@code analyzedChunks} isn't used twice.
   * Return true if all {@code expectedCases} can be found.
   */
  boolean checkCases(List<AnalyzedChunk> sentenceCasesList, List<ValencyData> expectedCases) {
    if (sentenceCasesList.size() == 0 && (expectedCases.size() == 0 || allOptional(expectedCases))) {
      return true;
    } else if (sentenceCasesList.size() == 0 || expectedCases.size() == 0) {
      return false;
    }
    Case searchedCase = expectedCases.get(0).aCase;
    int i = 0;
    for (AnalyzedChunk chunk : sentenceCasesList) {
      if (chunk.cases.contains(searchedCase)) {
        List<AnalyzedChunk> remaining = new ArrayList<>(sentenceCasesList);
        remaining.remove(i);
        if (checkCases(remaining, expectedCases.subList(1, expectedCases.size()))) {
          return true;
        }
      }
      i++;
    }
    return false;
  }

  private boolean allOptional(List<ValencyData> expectedCases) {
    for (ValencyData valency : expectedCases) {
      if (valency.isRequired) {
        return false;
      }
    }
    return true;
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
      if (features.size() > 0) {
        result.add(StringTools.listToString(features, ":"));
      }
    }
    return result;
  }

  class CheckResult {

    private final boolean correct;
    private final String verbLemma;
    private final List<AnalyzedChunk> analyzedChunks;
    private final List<ValencyData> verbCases;

    private CheckResult(boolean correct, List<AnalyzedChunk> analyzedChunks, List<ValencyData> verbCases, String verbLemma) {
      this.correct = correct;
      this.analyzedChunks = analyzedChunks;
      this.verbCases = verbCases;
      this.verbLemma = verbLemma;
    }

    boolean isCorrect() {
      return correct;
    }

    @Override
    public String toString() {
      return "sentence=" + analyzedChunks + ", expectedByVerb=" + verbCases;
    }
  }

  static class Chunk {
    String chunk;
    boolean precededByComma;

    Chunk(String chunk, boolean precededByComma) {
      this.chunk = chunk;
      this.precededByComma = precededByComma;
    }

    @Override
    public String toString() {
      return chunk;
    }
  }

  static class AnalyzedChunk extends Chunk {
    Set<Case> cases;

    AnalyzedChunk(Chunk chunk, Set<Case> cases) {
      super(chunk.chunk, chunk.precededByComma);
      this.cases = cases;
    }

    @Override
    public String toString() {
      return chunk + ":" + cases;
    }
  }

}
