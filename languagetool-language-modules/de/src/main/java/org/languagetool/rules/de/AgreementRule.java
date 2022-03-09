/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.German;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.tagging.de.GermanToken.POSType;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.languagetool.rules.de.GermanHelper.*;
import static org.languagetool.tools.StringTools.startsWithUppercase;

/**
 * Simple agreement checker for German noun phrases. Checks agreement in:
 *
 * <ul>
 *  <li>DET/PRO NOUN: e.g. "mein Auto", "der Mann", "die Frau" (correct), "die Haus" (incorrect)</li>
 *  <li>DET/PRO ADJ NOUN: e.g. "der riesige Tisch" (correct), "die riesigen Tisch" (incorrect)</li>
 *  <li>DET/PRO ADJ ADJ NOUN: e.g. "der große riesige Tisch" (correct), "die große riesige Tisch" (incorrect)</li>
 * </ul>
 *
 * Note that this rule only checks agreement inside the noun phrase, not whether
 * e.g. the correct case is used. For example, "Es ist das Haus dem Mann" is not
 * detected as incorrect.
 *
 * <p>TODO: the implementation could use a re-write that first detects the relevant noun phrases and then checks agreement
 *
 * @author Daniel Naber
 */
public class AgreementRule extends Rule {

  private final German language;
  private final Supplier<List<DisambiguationPatternRule>> antiPatterns;

  private JLanguageTool lt;

  enum GrammarCategory {
    KASUS("Kasus (Fall: Wer/Was, Wessen, Wem, Wen/Was - Beispiel: 'das Fahrrads' statt 'des Fahrrads')"),
    GENUS("Genus (männlich, weiblich, sächlich - Beispiel: 'der Fahrrad' statt 'das Fahrrad')"),
    NUMERUS("Numerus (Einzahl, Mehrzahl - Beispiel: 'das Fahrräder' statt 'die Fahrräder')");

    private final String displayName;
    GrammarCategory(String displayName) {
      this.displayName = displayName;
    }
  }
  private static final AnalyzedToken[] INS_REPLACEMENT = {new AnalyzedToken("das", "ART:DEF:AKK:SIN:NEU", "das")};
  private static final AnalyzedToken[] ZUR_REPLACEMENT = {new AnalyzedToken("der", "ART:DEF:DAT:SIN:FEM", "der")};

  enum ReplacementType {
    Ins, Zur
  }

  private static final String MSG = "Möglicherweise fehlende grammatische Übereinstimmung " +
    "von Kasus, Numerus oder Genus. Beispiel: 'mein kleiner Haus' statt 'mein kleines Haus'";
  private static final String MSG2 = "Möglicherweise fehlende grammatische Übereinstimmung " +
    "von Kasus, Numerus oder Genus. Beispiel: 'mein schönes kleiner Haus' statt 'mein schönes kleines Haus'";
  private static final String SHORT_MSG = "Evtl. keine Übereinstimmung von Kasus, Numerus oder Genus";

  private static final Set<String> MODIFIERS = new HashSet<>(Arrays.asList(
    "absolut",
    "ausgesprochen",
    "außergewöhnlich",
    "außerordentlich",
    "äußerst",
    "besonders",
    "dringend",
    "echt",
    "einigermaßen",
    "enorm",
    "extrem",
    "fast",
    "ganz",
    "geradezu",
    "zeitweise",
    "halbwegs",
    "höchst",
    "komplett",
    "laufend",
    "recht",
    "relativ",
    "sehr",
    "total",
    "überaus",
    "ungewöhnlich",
    "unglaublich",
    //"viel",    // "xxx, die viel Platz..."
    "völlig",
    "weit",
    "wirklich",
    "ziemlich"
  ));

  private static final Set<String> VIELE_WENIGE_LOWERCASE = new HashSet<>(Arrays.asList(
    "manche",
    "jegliche",
    "jeglicher",
    "andere",
    "anderer",
    "anderen",
    "sämtlicher",
    "etliche",
    "etlicher",
    "viele",
    "vieler",
    "wenige",
    "weniger",
    "einige",
    "einiger",
    "mehrerer",
    "mehrere"
  ));

  private static final String[] REL_PRONOUN_LEMMAS = {"der", "welch"};

  private static final Set<String> PRONOUNS_TO_BE_IGNORED = new HashSet<>(Arrays.asList(
    "nichts",
    "alles",   // "Ruhe vor dem alles verheerenden Sturm", "Alles Große und Edle ist einfacher Art."
    "dies",
    "ich",
    "dir",
    "dich",
    "du",
    "d",
    "er", "sie", "es",
    "wir",
    "mich",
    "mir",
    "uns",
    "ihnen",
    "euch",
    "ihm",
    "ihr",
    "ihn",
    "dessen",
    "deren",
    "denen",
    "sich",
    "aller",
    "allen",  // "das allen bekannte Wollnashorn"
    "man",
    "beide",
    "beiden",
    "beider",
    "wessen",
    "a",
    "alle",
    "etwas",
    "irgendetwas",
    "irgendwas",
    "irgendwer",
    "was",
    "wer",
    "wem",
    "jenen",      // "...und mit jenen anderer Arbeitsgruppen verwoben"
    "diejenigen",
    "irgendjemand", "irgendjemandes",
    "jemand", "jemandes",
    "niemand", "niemandes"
  ));

  private static final Set<String> NOUNS_TO_BE_IGNORED = new HashSet<>(Arrays.asList(
    "A",
    "Prozent",   // Plural "Prozente", trotzdem ist "mehrere Prozent" korrekt
    "Wollen",  // das Wollen
    "Gramm",
    "Kilogramm",
    "Piepen", // Die Piepen
    "Badlands",
    "Visual", // englisch
    "Special", // englisch
    "Multiple", // englisch
    "Chief", // Chief Executive Officer
    "Carina", // Name
    "Wüstenrot", // Name
    "Rückgrad", // found by speller
    "Rückgrads", // found by speller
    "Aalen", // Plural form of "Aal" but also large city in Germany
    "Meter", // Das Meter (Objekt zum Messen)
    "Boots", // "Die neuen Boots" (englisch Stiefel)
    "Taxameter", // Beides erlaubt "Das" und "Die"
    "Bild", // die Bild (Zeitung)
    "Emirates", // "Mit einem Flug der Emirates" (Fluggesellschaft)
    "Uhr",   // "um ein Uhr"
    "cm", // "Die letzten cm" können
    "km",
    "Nr",
    "RP" // "Die RP (Rheinische Post)"
  ));

  private final static List<List<PatternToken>> allAntiPatterns =
    Stream.of(AgreementRuleAntiPatterns1.ANTI_PATTERNS, AgreementRuleAntiPatterns2.ANTI_PATTERNS)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());

  public AgreementRule(ResourceBundle messages, German language) {
    this.language = language;
    super.setCategory(Categories.GRAMMAR.getCategory(messages));
    addExamplePair(Example.wrong("<marker>Der Haus</marker> wurde letztes Jahr gebaut."),
                   Example.fixed("<marker>Das Haus</marker> wurde letztes Jahr gebaut."));
    antiPatterns = cacheAntiPatterns(language, allAntiPatterns);
  }

  @Override
  public String getId() {
    return "DE_AGREEMENT";
  }

  @Override
  public int estimateContextForSureMatch() {
    return allAntiPatterns.stream().mapToInt(List::size).max().orElse(0);
  }

  @Override
  public String getDescription() {
    return "Kongruenz von Nominalphrasen (unvollständig!), z.B. 'mein kleiner(kleines) Haus'";
  }

  private Map<Integer,ReplacementType> replacePrepositionsByArticle (AnalyzedTokenReadings[] tokens) {
    Map<Integer, ReplacementType> map = new HashMap<>();
    for (int i = 0; i < tokens.length; i++) {
      if (StringUtils.equalsAny(tokens[i].getToken(), "ins", "ans", "aufs", "vors", "durchs", "hinters", "unters", "übers", "fürs", "ums")) {
        tokens[i] = new AnalyzedTokenReadings(INS_REPLACEMENT, tokens[i].getStartPos());
        map.put(i, ReplacementType.Ins);
      } else if (StringUtils.equalsAny(tokens[i].getToken(), "zur")) {
        tokens[i] = new AnalyzedTokenReadings(ZUR_REPLACEMENT, tokens[i].getStartPos());
        map.put(i, ReplacementType.Zur);
      }
    }
    return map;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokensWithoutWhitespace();
    AnalyzedTokenReadings[] origTokens = Arrays.copyOf(tokens, tokens.length);
    Map<Integer, ReplacementType> replMap = replacePrepositionsByArticle(tokens);
    for (int i = 0; i < tokens.length; i++) {
      String posToken = tokens[i].getAnalyzedToken(0).getPOSTag();  //TODO: check for all readings?
      if (JLanguageTool.SENTENCE_START_TAGNAME.equals(posToken) || tokens[i].isImmunized() || origTokens[i].isImmunized()) {
        continue;
      }
      if (couldBeRelativeOrDependentClause(tokens, i)) {
        continue;
      }
      if (i > 0) {
        String prevToken = tokens[i-1].getToken().toLowerCase();
        if (StringUtils.equalsAny(tokens[i].getToken(), "eine", "einen")
            && StringUtils.equalsAny(prevToken, "der", "die", "das", "des", "dieses")) {
          // TODO: "der eine Polizist" -> nicht ignorieren, sondern "der polizist" checken; "auf der einen Seite"
          continue;
        }
      }
      // avoid false alarm on "nichts Gutes" and "alles Gute"
      AnalyzedTokenReadings tokenReadings = tokens[i];
      // avoid false alarm on "Art. 1" and "bisherigen Art. 1" (Art. = Artikel):
      boolean detAbbrev = i < tokens.length-2 && tokens[i+1].getToken().equals("Art") && tokens[i+2].getToken().equals(".");
      boolean detAdjAbbrev = i < tokens.length-3 && tokens[i+2].getToken().equals("Art") && tokens[i+3].getToken().equals(".");
      // "einen Hochwasser führenden Fluss", "die Gott zugeschriebenen Eigenschaften":
      boolean followingParticiple = i < tokens.length-3 && (tokens[i+2].hasPartialPosTag("PA1") || tokens[i+2].getToken().matches("zugeschriebenen?|genannten?"));
      if (detAbbrev || detAdjAbbrev || followingParticiple) {
        continue;
      }
      if (hasReadingOfType(tokenReadings, POSType.DETERMINER) || isRelevantPronoun(tokens, i)) {
        int tokenPosAfterModifier = getPosAfterModifier(i+1, tokens);
        String skippedStr = null;
        if (tokenPosAfterModifier > i+1) {
          skippedStr = sentence.getText().substring(tokens[i+1].getStartPos(), tokens[tokenPosAfterModifier-1].getEndPos());
        }
        int tokenPos = tokenPosAfterModifier;
        if (tokenPos >= tokens.length) {
          break;
        }
        AnalyzedTokenReadings nextToken = tokens[tokenPos];
        AnalyzedTokenReadings maybePreposition = i-1 >= 0 ? tokens[i-1] : null;
        if (i-2 >= 0 && "was".equalsIgnoreCase(tokens[i-2].getToken())) {
          maybePreposition = null;  // avoid preposition filtering on "Was für eine schöner Sonnenuntergang!"
        }
        if (isNonPredicativeAdjective(nextToken) || isParticiple(nextToken)) {
          tokenPos = tokenPosAfterModifier + 1;
          if (tokenPos >= tokens.length) {
            break;
          }
          if (hasReadingOfType(tokens[tokenPos], POSType.NOMEN)) {
            // TODO: add a case (checkAdjNounAgreement) for special cases like "deren",
            // e.g. "deren komisches Geschenke" isn't yet detected as incorrect
            if (i >= 2 && hasReadingOfType(tokens[i-2], POSType.ADJEKTIV)
                       && "als".equals(tokens[i-1].getToken())
                       && "das".equals(tokens[i].getToken())) {
              // avoid false alarm for e.g. "weniger farbenprächtig als das anderer Papageien"
              continue;
            }
            boolean allowSuggestion = tokenPos == i + 2;  // prevent incomplete suggestion for e.g. "einen 142 Meter hoher Obelisken" (-> "einen hohen Obelisken")
            RuleMatch ruleMatch = checkDetAdjNounAgreement(maybePreposition, tokens[i],
                nextToken, tokens[tokenPos], sentence, i, allowSuggestion ? replMap : null, skippedStr);
            if (ruleMatch != null) {
              ruleMatches.add(ruleMatch);
            }
          } else if (tokenPos+1 < tokens.length && hasReadingOfType(tokens[tokenPos+1], POSType.NOMEN) && GermanHelper.hasReadingOfType(tokens[tokenPos], POSType.ADJEKTIV)) {
            RuleMatch ruleMatch = checkDetAdjAdjNounAgreement(maybePreposition, tokens[i],
              nextToken, tokens[tokenPos], tokens[tokenPos+1], sentence, i, replMap);
            if (ruleMatch != null) {
              ruleMatches.add(ruleMatch);
            }
          }
        } else if (hasReadingOfType(nextToken, POSType.NOMEN) && !"Herr".equals(nextToken.getToken())) {
          RuleMatch ruleMatch = checkDetNounAgreement(maybePreposition, tokens[i], nextToken, sentence, i, replMap);
          if (ruleMatch != null) {
            ruleMatches.add(ruleMatch);
          }
        }
      }
    } // for each token
    return toRuleMatchArray(ruleMatches);
  }

  /**
   * Search for modifiers (such as "sehr", "1,4 Meter") which can expand a
   * determiner - adjective - noun group ("ein hohes Haus" -> "ein sehr hohes Haus",
   * "ein 500 Meter hohes Haus") and return the index of the first non-modifier token ("Haus")
   * @param startAt index of array where to start searching for modifier
   * @return index of first non-modifier token
   */
  private int getPosAfterModifier(int startAt, AnalyzedTokenReadings[] tokens) {
    if (startAt + 1 < tokens.length && MODIFIERS.contains(tokens[startAt].getToken())) {
      startAt++;
    }
    if (startAt + 1 < tokens.length && (StringUtils.isNumeric(tokens[startAt].getToken()) || tokens[startAt].hasPosTag("ZAL"))) {
      int posAfterModifier = startAt + 1;
      if (startAt + 3 < tokens.length && ",".equals(tokens[startAt+1].getToken()) && StringUtils.isNumeric(tokens[startAt+2].getToken())) {
        posAfterModifier = startAt + 3;
      }
      if (StringUtils.endsWithAny(tokens[posAfterModifier].getToken(), "gramm", "Gramm", "Meter", "meter")) {
        return posAfterModifier + 1;
      }
    }
    return startAt;
  }

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return antiPatterns.get();
  }

  private boolean isNonPredicativeAdjective(AnalyzedTokenReadings tokensReadings) {
    for (AnalyzedToken reading : tokensReadings.getReadings()) {
      String posTag = reading.getPOSTag();
      if (posTag != null && posTag.startsWith("ADJ") && !posTag.contains("PRD")) {
        return true;
      }
    }
    return false;
  }

  private boolean isParticiple(AnalyzedTokenReadings tokensReadings) {
    return tokensReadings.hasPartialPosTag("PA1") || tokensReadings.hasPartialPosTag("PA2");
  }

  private boolean isRelevantPronoun(AnalyzedTokenReadings[] tokens, int pos) {
    AnalyzedTokenReadings analyzedToken = tokens[pos];
    boolean relevantPronoun = hasReadingOfType(analyzedToken, POSType.PRONOMEN);
    // avoid false alarms:
    String token = tokens[pos].getToken();
    if (PRONOUNS_TO_BE_IGNORED.contains(token.toLowerCase()) ||
        (pos > 0 && tokens[pos-1].getToken().equalsIgnoreCase("vor") && token.equalsIgnoreCase("allem"))) {
      relevantPronoun = false;
    }
    return relevantPronoun;
  }

  // TODO: improve this so it only returns true for real relative clauses
  private boolean couldBeRelativeOrDependentClause(AnalyzedTokenReadings[] tokens, int pos) {
    boolean comma;
    boolean relPronoun;
    if (pos >= 1) {
      // avoid false alarm: "Das Wahlrecht, das Frauen zugesprochen bekamen." etc:
      comma = tokens[pos-1].getToken().equals(",");
      relPronoun = comma && tokens[pos].hasAnyLemma(REL_PRONOUN_LEMMAS);
      if (relPronoun && pos+3 < tokens.length) {
        return true;
      }
    }
    if (pos >= 2) {
      // avoid false alarm: "Der Mann, in dem quadratische Fische schwammen."
      // or: "Die Polizei erwischte die Diebin, weil diese Ausweis und Visitenkarte hinterließ."
      comma = tokens[pos-2].getToken().equals(",");
      if (comma) {
        boolean prep = tokens[pos-1].hasPosTagStartingWith("PRP:");
        relPronoun = tokens[pos].hasAnyLemma(REL_PRONOUN_LEMMAS);
        return prep && relPronoun || (tokens[pos-1].hasPosTag("KON:UNT") && (tokens[pos].hasLemma("jen") || tokens[pos].hasLemma("dies")));
      }
    }
    return false;
  }

  @Nullable
  private RuleMatch checkDetNounAgreement(AnalyzedTokenReadings maybePreposition, AnalyzedTokenReadings token1,
                                          AnalyzedTokenReadings token2, AnalyzedSentence sentence, int tokenPos, Map<Integer, ReplacementType> replMap) {
    // TODO: remove "-".equals(token2.getToken()) after the bug fix
    // see Daniel's comment from 20.12.2016 at https://github.com/languagetool-org/languagetool/issues/635
    if (token2.isImmunized() || NOUNS_TO_BE_IGNORED.contains(token2.getToken()) || "-".equals(token2.getToken())) {
      return null;
    }

    Set<String> set1;
    if (token1.getReadings().size() == 1 &&
        token1.getReadings().get(0).getPOSTag() != null &&
        token1.getReadings().get(0).getPOSTag().endsWith(":STV")) {
      // catch the error in "Meiner Chef raucht."
      set1 = Collections.emptySet();
    } else {
      set1 = getAgreementCategories(token1);
    }
    Set<String> set2 = getAgreementCategories(token2);
    set1.retainAll(set2);
    RuleMatch ruleMatch = null;
    if (set1.isEmpty() && !isException(token1, token2)) {
      RuleMatch compoundMatch = getCompoundError(token1, token2, tokenPos, sentence);
      if (compoundMatch != null) {
        return compoundMatch;
      }
      List<String> errorCategories = getCategoriesCausingError(token1, token2);
      String errorDetails = errorCategories.isEmpty() ?
            "Kasus, Genus oder Numerus" : String.join(" und ", errorCategories);
      String msg = "Möglicherweise fehlende grammatische Übereinstimmung des " + errorDetails + ".";
      String shortMsg = "Evtl. keine Übereinstimmung von Kasus, Genus oder Numerus";
      ruleMatch = new RuleMatch(this, sentence, token1.getStartPos(),
              token2.getEndPos(), msg, shortMsg);
      // this will not give a match for compounds that are not in the dictionary...
      //ruleMatch.setUrl(Tools.getUrl("https://www.korrekturen.de/flexion/deklination/" + token2.getToken() + "/"));
      AgreementSuggestor2 suggestor = new AgreementSuggestor2(language.getSynthesizer(), token1, token2, replMap.get(tokenPos));
      suggestor.setPreposition(maybePreposition);
      ruleMatch.setSuggestedReplacements(suggestor.getSuggestions(true));
    }
    return ruleMatch;
  }

  // z.B. "die Original Mail" -> "die Originalmail"
  @Nullable
  private RuleMatch getCompoundError(AnalyzedTokenReadings token1, AnalyzedTokenReadings token2, int tokenPos, AnalyzedSentence sentence) {
    if (tokenPos != -1 && tokenPos + 2 < sentence.getTokensWithoutWhitespace().length) {
      AnalyzedTokenReadings nextToken = sentence.getTokensWithoutWhitespace()[tokenPos + 2];
      if (startsWithUppercase(nextToken.getToken())) {
        String potentialCompound = token2.getToken() + StringTools.lowercaseFirstChar(nextToken.getToken());
        String origToken1 = sentence.getTokensWithoutWhitespace()[tokenPos].getToken();  // before 'ins' etc. replacement
        String testPhrase = origToken1 + " " + potentialCompound;
        String hyphenPotentialCompound = token2.getToken() + "-" + nextToken.getToken();
        String hyphenTestPhrase = origToken1 + " " + hyphenPotentialCompound;
        return getRuleMatch(token1, nextToken, sentence, testPhrase, hyphenTestPhrase);
      }
    }
    return null;
  }

  // z.B. "die neue Original Mail" -> "die neue Originalmail"
  @Nullable
  private RuleMatch getCompoundError(AnalyzedTokenReadings token1, AnalyzedTokenReadings token2, AnalyzedTokenReadings token3,
                                     int tokenPos, AnalyzedSentence sentence) {
    if (tokenPos != -1 && tokenPos + 3 < sentence.getTokensWithoutWhitespace().length) {
      AnalyzedTokenReadings nextToken = sentence.getTokensWithoutWhitespace()[tokenPos + 3];
      if (startsWithUppercase(nextToken.getToken())) {
        String potentialCompound = token3.getToken() + StringTools.lowercaseFirstChar(nextToken.getToken());
        String origToken1 = sentence.getTokensWithoutWhitespace()[tokenPos].getToken();  // before 'ins' etc. replacement
        String testPhrase = origToken1 + " " + token2.getToken() + " " + potentialCompound;
        String hyphenPotentialCompound = token3.getToken() + "-" + nextToken.getToken();
        String hyphenTestPhrase = origToken1 + " " + token2.getToken() + " " + hyphenPotentialCompound;
        return getRuleMatch(token1, nextToken, sentence, testPhrase, hyphenTestPhrase);
      }
    }
    return null;
  }

  // z.B. "die ganz neue Original Mail" -> "die ganz neue Originalmail"
  @Nullable
  private RuleMatch getCompoundError(AnalyzedTokenReadings token1, AnalyzedTokenReadings token2, AnalyzedTokenReadings token3,
                                     AnalyzedTokenReadings token4, int tokenPos, AnalyzedSentence sentence) {
    if (tokenPos != -1 && tokenPos + 4 < sentence.getTokensWithoutWhitespace().length) {
      AnalyzedTokenReadings nextToken = sentence.getTokensWithoutWhitespace()[tokenPos + 4];
      String potentialCompound = token4.getToken() + StringTools.lowercaseFirstChar(nextToken.getToken());
      if (startsWithUppercase(token4.getToken()) && startsWithUppercase(nextToken.getToken())) {
        String origToken1 = sentence.getTokensWithoutWhitespace()[tokenPos].getToken();  // before 'ins' etc. replacement
        String testPhrase = origToken1 + " " + token2.getToken() + " " + token3.getToken() + " " + potentialCompound;
        String hyphenPotentialCompound = token4.getToken() + "-" + nextToken.getToken();
        String hyphenTestPhrase = origToken1 + " " + token2.getToken() + " " + token3.getToken() + " " + hyphenPotentialCompound;
        return getRuleMatch(token1, nextToken, sentence, testPhrase, hyphenTestPhrase);
      }
    }
    return null;
  }

  @Nullable
  private RuleMatch getRuleMatch(AnalyzedTokenReadings token, AnalyzedTokenReadings token2, AnalyzedSentence sentence, String testPhrase, String hyphenTestPhrase) {
    try {
      initLt();
      if (token2.getReadings().stream().allMatch(k -> k.getPOSTag() != null && !k.getPOSTag().startsWith("SUB"))) {
        return null;
      }
      List<String> replacements = new ArrayList<>();
      if (lt.check(testPhrase).isEmpty() && token2.isTagged()) {
        replacements.add(testPhrase);
      }
      if (lt.check(hyphenTestPhrase).isEmpty() && token2.isTagged()) {
        replacements.add(hyphenTestPhrase);
      }
      if (replacements.size() > 0) {
        String message = "Wenn es sich um ein zusammengesetztes Nomen handelt, wird es zusammengeschrieben.";
        RuleMatch ruleMatch = new RuleMatch(this, sentence, token.getStartPos(), token2.getEndPos(), message);
        ruleMatch.addSuggestedReplacements(replacements);
        ruleMatch.setUrl(Tools.getUrl("https://dict.leo.org/grammatik/deutsch/Rechtschreibung/Regeln/Getrennt-zusammen/Nomen.html#grammarAnchor-Nomen-49575"));
        return ruleMatch;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return null;
  }
  
  private void initLt() {
    if (lt == null) {
      lt = new JLanguageTool(language);
      for (Rule rule : lt.getAllActiveRules()) {
        if (!rule.getId().equals("DE_AGREEMENT") && !rule.getId().equals("GERMAN_SPELLER_RULE")) {
          lt.disableRule(rule.getId());
        }
      }
    }
  }

  private boolean isException(AnalyzedTokenReadings token1, AnalyzedTokenReadings token2) {
    return "allen".equals(token1.getToken()) && "Grund".equals(token2.getToken());
  }

  List<String> getCategoriesCausingError(AnalyzedTokenReadings token1, AnalyzedTokenReadings token2) {
    List<String> categories = new ArrayList<>();
    List<GrammarCategory> categoriesToCheck = Arrays.asList(GrammarCategory.KASUS, GrammarCategory.GENUS, GrammarCategory.NUMERUS);
    for (GrammarCategory category : categoriesToCheck) {
      if (agreementWithCategoryRelaxation(token1, token2, category)) {
        categories.add(category.displayName);
      }
    }
    return categories;
  }

  private RuleMatch checkDetAdjNounAgreement(AnalyzedTokenReadings maybePreposition, AnalyzedTokenReadings token1,
                                             AnalyzedTokenReadings token2, AnalyzedTokenReadings token3, AnalyzedSentence sentence,
                                             int tokenPos, Map<Integer, ReplacementType> replMap, String skippedStr) {
    // TODO: remove (token3 == null || token3.getToken().length() < 2)
    // see Daniel's comment from 20.12.2016 at https://github.com/languagetool-org/languagetool/issues/635
    if (token3 == null || token3.getToken().length() < 2) {
      return null;
    }
    Set<String> set = retainCommonCategories(token1, token2, token3);
    RuleMatch ruleMatch = null;
    if (set.isEmpty()) {
      if (token3.getToken().matches("Herr|Frau") && tokenPos + 3 < sentence.getTokensWithoutWhitespace().length) {
        AnalyzedTokenReadings token4 = sentence.getTokensWithoutWhitespace()[tokenPos + 3];
        if (!token4.isTagged() || token4.hasPosTagStartingWith("EIG:")) {
          // 'Aber das ignorierte Herr Grey bewusst.'
          return null;
        }
      }
      if (tokenPos + 4 < sentence.getTokensWithoutWhitespace().length) {
        RuleMatch compoundMatch = getCompoundError(sentence.getTokensWithoutWhitespace()[tokenPos],
                sentence.getTokensWithoutWhitespace()[tokenPos+1],
                sentence.getTokensWithoutWhitespace()[tokenPos+2],
                sentence.getTokensWithoutWhitespace()[tokenPos+3], tokenPos, sentence);
        if (compoundMatch != null) {
          return compoundMatch;
        }
      }
      RuleMatch compoundMatch = getCompoundError(token1, token2, token3, tokenPos, sentence);
      if (compoundMatch != null) {
        return compoundMatch;
      }
      if (token3.hasPosTagStartingWith("ABK")) {
        return null;
      }
      ruleMatch = new RuleMatch(this, sentence, token1.getStartPos(), token3.getEndPos(), MSG, SHORT_MSG);
      AgreementSuggestor2 suggestor = new AgreementSuggestor2(language.getSynthesizer(), token1, token2, token3, replMap != null ? replMap.get(tokenPos) : null);
      suggestor.setPreposition(maybePreposition);
      suggestor.setSkipped(skippedStr);
      ruleMatch.setSuggestedReplacements(suggestor.getSuggestions(true));
    }
    return ruleMatch;
  }

  private RuleMatch checkDetAdjAdjNounAgreement(AnalyzedTokenReadings maybePreposition, AnalyzedTokenReadings token1,
                                             AnalyzedTokenReadings token2, AnalyzedTokenReadings token3, AnalyzedTokenReadings token4,
                                             AnalyzedSentence sentence, int tokenPos, Map<Integer, ReplacementType> replMap) {
    Set<String> set = retainCommonCategories(token1, token2, token3, token4);
    RuleMatch ruleMatch = null;
    if (set.isEmpty()) {
      RuleMatch compoundMatch = getCompoundError(token1, token2, token3, token4, tokenPos, sentence);
      if (compoundMatch != null) {
        return compoundMatch;
      }
      if (token4.hasPosTagStartingWith("ABK")) {
        return null;
      }
      ruleMatch = new RuleMatch(this, sentence, token1.getStartPos(), token4.getEndPos(), MSG2, SHORT_MSG);
      if (replMap != null) {
        AgreementSuggestor2 suggestor = new AgreementSuggestor2(language.getSynthesizer(), token1, token2, token3, token4, replMap.get(tokenPos));
        suggestor.setPreposition(maybePreposition);
        ruleMatch.setSuggestedReplacements(suggestor.getSuggestions(true));
      }
    }
    return ruleMatch;
  }

  private boolean agreementWithCategoryRelaxation(AnalyzedTokenReadings token1,
                                                  AnalyzedTokenReadings token2, GrammarCategory categoryToRelax) {
    Set<GrammarCategory> categoryToRelaxSet;
    if (categoryToRelax != null) {
      categoryToRelaxSet = Collections.singleton(categoryToRelax);
    } else {
      categoryToRelaxSet = Collections.emptySet();
    }
    Set<String> set1 = AgreementTools.getAgreementCategories(token1, categoryToRelaxSet, true);
    Set<String> set2 = AgreementTools.getAgreementCategories(token2, categoryToRelaxSet, true);
    set1.retainAll(set2);
    return !set1.isEmpty();
  }

  @NotNull
  private Set<String> retainCommonCategories(AnalyzedTokenReadings token1,
                                             AnalyzedTokenReadings token2, AnalyzedTokenReadings token3) {
    Set<GrammarCategory> categoryToRelaxSet = Collections.emptySet();
    boolean skipSol = !VIELE_WENIGE_LOWERCASE.contains(token1.getToken().toLowerCase());
    Set<String> set1 = AgreementTools.getAgreementCategories(token1, categoryToRelaxSet, skipSol);
    Set<String> set2 = AgreementTools.getAgreementCategories(token2, categoryToRelaxSet, skipSol);
    Set<String> set3 = AgreementTools.getAgreementCategories(token3, categoryToRelaxSet, true);
    set1.retainAll(set2);
    set1.retainAll(set3);
    return set1;
  }

  @NotNull
  private Set<String> retainCommonCategories(AnalyzedTokenReadings token1,
                                             AnalyzedTokenReadings token2, AnalyzedTokenReadings token3, AnalyzedTokenReadings token4) {
    Set<GrammarCategory> categoryToRelaxSet = Collections.emptySet();
    boolean skipSol = !VIELE_WENIGE_LOWERCASE.contains(token1.getToken().toLowerCase());
    Set<String> set1 = AgreementTools.getAgreementCategories(token1, categoryToRelaxSet, skipSol);
    Set<String> set2 = AgreementTools.getAgreementCategories(token2, categoryToRelaxSet, skipSol);
    Set<String> set3 = AgreementTools.getAgreementCategories(token3, categoryToRelaxSet, skipSol);
    Set<String> set4 = AgreementTools.getAgreementCategories(token4, categoryToRelaxSet, true);
    set1.retainAll(set2);
    set1.retainAll(set3);
    set1.retainAll(set4);
    return set1;
  }

  private Set<String> getAgreementCategories(AnalyzedTokenReadings aToken) {
    return AgreementTools.getAgreementCategories(aToken, new HashSet<>(), false);
  }

}
