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

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.German;
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.tagging.de.AnalyzedGermanToken;
import org.languagetool.tagging.de.GermanToken;
import org.languagetool.tagging.de.GermanToken.POSType;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;

import java.util.*;

/**
 * Simple agreement checker for German noun phrases. Checks agreement in:
 * 
 * <ul>
 *  <li>DET/PRO NOUN: e.g. "mein Auto", "der Mann", "die Frau" (correct), "die Haus" (incorrect)</li>
 *  <li>DET/PRO ADJ NOUN: e.g. "der riesige Tisch" (correct), "die riesigen Tisch" (incorrect)</li> 
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
public class AgreementRule extends GermanRule {

  private final German language;

  private enum GrammarCategory {
    KASUS("Kasus (Fall: Wer/Was, Wessen, Wem, Wen/Was - Beispiel: 'das Fahrrads' statt 'des Fahrrads')"),
    GENUS("Genus (männlich, weiblich, sächlich - Beispiel: 'der Fahrrad' statt 'das Fahrrad')"),
    NUMERUS("Numerus (Einzahl, Mehrzahl - Beispiel: 'das Fahrräder' statt 'die Fahrräder')");
    
    private final String displayName;
    GrammarCategory(String displayName) {
      this.displayName = displayName;
    }
  }

  private static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("(?i:ist|war)").build(),
      new PatternTokenBuilder().token("das").build(),
      new PatternTokenBuilder().token("Zufall").build()
    ),
    Arrays.asList( // "So hatte das Vorteile|Auswirkungen|Konsequenzen..."
      new PatternTokenBuilder().tokenRegex("(?i:hat(te)?)").build(),
      new PatternTokenBuilder().token("das").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("von|bei").build(),
      new PatternTokenBuilder().tokenRegex("(vielen|allen)").build(),
      new PatternTokenBuilder().posRegex("PA2:.*|ADJ:AKK:PLU:.*").build()  // "ein von vielen bewundertes Haus" / "Das weckte bei vielen ungute Erinnerungen."
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("für").build(),
      new PatternTokenBuilder().tokenRegex("(viele|alle|[dm]ich|ihn|sie|uns)").build(),
      new PatternTokenBuilder().posRegex("ADJ:AKK:.*").build()  // "Ein für viele wichtiges Anliegen."
    ),
    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("das|die").build(),
      new PatternTokenBuilder().token("einem").build(),
      new PatternTokenBuilder().token("Angst").build()  // "Dinge, die/ Etwas, das einem Angst macht"
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("einem").build(),
      new PatternTokenBuilder().token("geschenkten").build(),
      new PatternTokenBuilder().token("Gaul").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("einer").build(),
      new PatternTokenBuilder().token("jeden").build(),
      new PatternTokenBuilder().posRegex("SUB:GEN:.*").build()  // "Kern einer jeden Tragödie..."
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("kein").build(),
      new PatternTokenBuilder().token("schöner").build(),
      new PatternTokenBuilder().token("Land").build()  // https://de.wikipedia.org/wiki/Kein_sch%C3%B6ner_Land
    ),
    Arrays.asList(
        new PatternTokenBuilder().pos("SENT_START").build(),
        new PatternTokenBuilder().tokenRegex("Ist|Sind").build(),
        new PatternTokenBuilder().token("das").build(),
        new PatternTokenBuilder().posRegex("SUB:.*").build(),
        new PatternTokenBuilder().posRegex("PKT").build()// "Ist das Kunst?"
    ),
    Arrays.asList(
        new PatternTokenBuilder().token("des").build(),
        new PatternTokenBuilder().token("Lied").build(),
        new PatternTokenBuilder().token("ich").build()// Wes Brot ich ess, des Lied ich sing
    ),
    Arrays.asList(
        new PatternTokenBuilder().pos("SENT_START").build(),
        new PatternTokenBuilder().tokenRegex("Das|Dies").build(),
        new PatternTokenBuilder().posRegex("VER:[123]:.*").build(),
        new PatternTokenBuilder().posRegex("SUB:NOM:.*").build()// "Das erfordert Können und..." / "Dies bestätigte Polizeimeister Huber"
    ),
    Arrays.asList(
        new PatternTokenBuilder().posRegex("ART:.*").build(), // "Das wenige Kilometer breite Tal"
        new PatternTokenBuilder().posRegex("ADJ:.*").build(),
        new PatternTokenBuilder().tokenRegex("(Kilo|Zenti|Milli)?meter|Jahre|Monate|Wochen|Tage|Stunden|Minuten|Sekunden").build()
    ),
    Arrays.asList(
        new PatternTokenBuilder().token("Van").build(), // https://de.wikipedia.org/wiki/Alexander_Van_der_Bellen
        new PatternTokenBuilder().token("der").build(),
        new PatternTokenBuilder().token("Bellen").build()
    ),
    Arrays.asList(
        new PatternTokenBuilder().token("mehrere").build(), // "mehrere Verwundete" http://forum.languagetool.org/t/de-false-positives-and-false-false/1516
        new PatternTokenBuilder().pos("SUB:NOM:SIN:FEM:ADJ").build()
    )
  );

  private static final Set<String> VIELE_WENIGE_LOWERCASE = new HashSet<>(Arrays.asList(
    "viele",
    "vieler",
    "wenige",
    "weniger",
    "einige",
    "einiger",
    "mehrerer",
    "mehrere"
  ));
  
  private static final Set<String> REL_PRONOUN = new HashSet<>();
  static {
    REL_PRONOUN.add("der");
    REL_PRONOUN.add("die");
    REL_PRONOUN.add("das");
    REL_PRONOUN.add("dessen");
    REL_PRONOUN.add("deren");
    REL_PRONOUN.add("dem");
    REL_PRONOUN.add("den");
    REL_PRONOUN.add("denen");
    REL_PRONOUN.add("welche");
    REL_PRONOUN.add("welcher");
    REL_PRONOUN.add("welchen");
    REL_PRONOUN.add("welchem");
    REL_PRONOUN.add("welches");
  }

  private static final Set<String> PREPOSITIONS = new HashSet<>();
  static {
    PREPOSITIONS.add("in");
    PREPOSITIONS.add("auf");
    PREPOSITIONS.add("an");
    PREPOSITIONS.add("ab");
    PREPOSITIONS.add("aus");
    PREPOSITIONS.add("für");
    PREPOSITIONS.add("zu");
    PREPOSITIONS.add("bei");
    PREPOSITIONS.add("nach");
    PREPOSITIONS.add("über");
    PREPOSITIONS.add("von");
    PREPOSITIONS.add("mit");
    PREPOSITIONS.add("durch");
    PREPOSITIONS.add("während");
    PREPOSITIONS.add("unter");
    PREPOSITIONS.add("ohne");
    // TODO: add more
  }
  
  private static final Set<String> PRONOUNS_TO_BE_IGNORED = new HashSet<>(Arrays.asList(
    "ich",
    "dir",
    "du",
    "er", "sie", "es",
    "wir",
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
    "unser",
    "aller",
    "man",
    "beide",
    "beiden",
    "beider",
    "wessen",
    "a",
    "alle",
    "etwas",
    "was",
    "wer",
    "jenen",      // "...und mit jenen anderer Arbeitsgruppen verwoben"
    "diejenigen",
    "jemand", "jemandes",
    "niemand", "niemandes"
  ));
  
  private static final Set<String> NOUNS_TO_BE_IGNORED = new HashSet<>(Arrays.asList(
    "Prozent",   // Plural "Prozente", trotzdem ist "mehrere Prozent" korrekt
    "Gramm",
    "Kilogramm",
    "Uhr"   // "um ein Uhr"
  ));
    
  public AgreementRule(ResourceBundle messages, German language) {
    this.language = language;
    super.setCategory(Categories.GRAMMAR.getCategory(messages));
    addExamplePair(Example.wrong("<marker>Der Haus</marker> wurde letztes Jahr gebaut."),
                   Example.fixed("<marker>Das Haus</marker> wurde letztes Jahr gebaut."));
  }
  
  @Override
  public String getId() {
    return "DE_AGREEMENT";
  }

  @Override
  public String getDescription() {
    return "Kongruenz von Nominalphrasen (unvollständig!), z.B. 'mein kleiner(kleines) Haus'";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokensWithoutWhitespace();
    for (int i = 0; i < tokens.length; i++) {
      //defaulting to the first reading
      //TODO: check for all readings
      String posToken = tokens[i].getAnalyzedToken(0).getPOSTag();
      if (posToken != null && posToken.equals(JLanguageTool.SENTENCE_START_TAGNAME)) {
        continue;
      }
      if (tokens[i].isImmunized()) {
        continue;
      }

      AnalyzedTokenReadings tokenReadings = tokens[i];
      boolean relevantPronoun = isRelevantPronoun(tokens, i);
     
      boolean ignore = couldBeRelativeClause(tokens, i);
      if (i > 0) {
        String prevToken = tokens[i-1].getToken().toLowerCase();
        if ((tokens[i].getToken().equals("eine") || tokens[i].getToken().equals("einen"))
            && (prevToken.equals("der") || prevToken.equals("die") || prevToken.equals("das") || prevToken.equals("des") || prevToken.equals("dieses"))) {
          // TODO: "der eine Polizist" -> nicht ignorieren, sondern "der polizist" checken; "auf der einen Seite"
          ignore = true;
        }
      }
      
      // avoid false alarm on "nichts Gutes" and "alles Gute"
      if (tokenReadings.getToken().equals("nichts") || tokenReadings.getToken().equals("alles")
          || tokenReadings.getToken().equals("dies")) {
        ignore = true;
      }

      // avoid false alarm on "Art. 1" and "bisherigen Art. 1" (Art. = Artikel):
      boolean detAbbrev = i < tokens.length-2 && tokens[i+1].getToken().equals("Art") && tokens[i+2].getToken().equals(".");
      boolean detAdjAbbrev = i < tokens.length-3 && tokens[i+2].getToken().equals("Art") && tokens[i+3].getToken().equals(".");
      // "einen Hochwasser führenden Fluss", "die Gott zugeschriebenen Eigenschaften":
      boolean followingParticiple = i < tokens.length-3 && (tokens[i+2].hasPartialPosTag("PA1") || tokens[i+2].getToken().matches("zugeschriebenen?|genannten?"));
      if (detAbbrev || detAdjAbbrev || followingParticiple) {
        ignore = true;
      }

      if ((GermanHelper.hasReadingOfType(tokenReadings, POSType.DETERMINER) || relevantPronoun) && !ignore) {
        int tokenPos = i + 1; 
        if (tokenPos >= tokens.length) {
          break;
        }
        AnalyzedTokenReadings nextToken = tokens[tokenPos];
        if (isNonPredicativeAdjective(nextToken) || isParticiple(nextToken)) {
          tokenPos = i + 2; 
          if (tokenPos >= tokens.length) {
            break;
          }
          if (GermanHelper.hasReadingOfType(tokens[tokenPos], POSType.NOMEN)) {
            // TODO: add a case (checkAdjNounAgreement) for special cases like "deren",
            // e.g. "deren komisches Geschenke" isn't yet detected as incorrect
            if (i >= 2 && GermanHelper.hasReadingOfType(tokens[i-2], POSType.ADJEKTIV)
                       && "als".equals(tokens[i-1].getToken())
                       && "das".equals(tokens[i].getToken())) {
              // avoid false alarm for e.g. "weniger farbenprächtig als das anderer Papageien"
              continue;
            }
            RuleMatch ruleMatch = checkDetAdjNounAgreement(tokens[i],
                nextToken, tokens[i+2]);
            if (ruleMatch != null) {
              ruleMatches.add(ruleMatch);
            }
          }
        } else if (GermanHelper.hasReadingOfType(nextToken, POSType.NOMEN) && !"Herr".equals(nextToken.getToken())) {
          RuleMatch ruleMatch = checkDetNounAgreement(tokens[i], tokens[i+1]);
          if (ruleMatch != null) {
            ruleMatches.add(ruleMatch);
          }
        }
      }
    } // for each token
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return makeAntiPatterns(ANTI_PATTERNS, language);
  }

  private boolean isNonPredicativeAdjective(AnalyzedTokenReadings tokensReadings) {
    for (AnalyzedToken reading : tokensReadings.getReadings()) {
      String posTag = reading.getPOSTag();
      if (posTag != null && posTag.startsWith("ADJ:") && !posTag.contains("PRD")) {
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
    boolean relevantPronoun = GermanHelper.hasReadingOfType(analyzedToken, POSType.PRONOMEN);
    // avoid false alarms:
    String token = tokens[pos].getToken();
    if (pos > 0 && tokens[pos-1].getToken().equalsIgnoreCase("vor") && token.equalsIgnoreCase("allem")) {
      relevantPronoun = false;
    } else if (PRONOUNS_TO_BE_IGNORED.contains(token.toLowerCase())) {
      relevantPronoun = false;
    }
    return relevantPronoun;
  }

  // TODO: improve this so it only returns true for real relative clauses
  private boolean couldBeRelativeClause(AnalyzedTokenReadings[] tokens, int pos) {
    boolean comma;
    boolean relPronoun;
    if (pos >= 1) {
      // avoid false alarm: "Das Wahlrecht, das Frauen zugesprochen bekamen." etc:
      comma = tokens[pos-1].getToken().equals(",");
      String term = tokens[pos].getToken();
      relPronoun = comma && REL_PRONOUN.contains(term);
      if (relPronoun && pos+3 < tokens.length) {
        return true;
      }
    }
    if (pos >= 2) {
      // avoid false alarm: "Der Mann, in dem quadratische Fische schwammen."
      comma = tokens[pos-2].getToken().equals(",");
      if(comma) {
        String term1 = tokens[pos-1].getToken();
        String term2 = tokens[pos].getToken();
        boolean prep = PREPOSITIONS.contains(term1);
        relPronoun = REL_PRONOUN.contains(term2);
        return prep && relPronoun;
      }
    }
    return false;
  }

  @Nullable
  private RuleMatch checkDetNounAgreement(AnalyzedTokenReadings token1,
      AnalyzedTokenReadings token2) {
    if (NOUNS_TO_BE_IGNORED.contains(token2.getToken())) {
      return null;
    }
    if (token2.isImmunized()) {
      return null;
    }
    Set<String> set1 = getAgreementCategories(token1);
    if (set1 == null) {
      return null;  // word not known, assume it's correct
    }
    Set<String> set2 = getAgreementCategories(token2);
    if (set2 == null) {
      return null;
    }
    set1.retainAll(set2);
    RuleMatch ruleMatch = null;
    if (set1.isEmpty() && !isException(token1, token2)) {
      List<String> errorCategories = getCategoriesCausingError(token1, token2);
      String errorDetails = errorCategories.size() > 0 ?
              String.join(" und ", errorCategories) : "Kasus, Genus oder Numerus";
      String msg = "Möglicherweise fehlende grammatische Übereinstimmung zwischen Artikel und Nomen " +
            "bezüglich " + errorDetails + ".";
      String shortMsg = "Möglicherweise keine Übereinstimmung bezüglich " + errorDetails;
      ruleMatch = new RuleMatch(this, token1.getStartPos(),
              token2.getEndPos(), msg, shortMsg);
      AgreementSuggestor suggestor = new AgreementSuggestor(language.getSynthesizer(), token1, token2);
      List<String> suggestions = suggestor.getSuggestions();
      ruleMatch.setSuggestedReplacements(suggestions);
    }
    return ruleMatch;
  }

  private boolean isException(AnalyzedTokenReadings token1, AnalyzedTokenReadings token2) {
    String phrase = token1.getToken() + " " + token2.getToken();
    return "allen Grund".equals(phrase); 
  }

  private List<String> getCategoriesCausingError(AnalyzedTokenReadings token1, AnalyzedTokenReadings token2) {
    List<String> categories = new ArrayList<>();
    List<GrammarCategory> categoriesToCheck = Arrays.asList(GrammarCategory.KASUS, GrammarCategory.GENUS, GrammarCategory.NUMERUS);
    for (GrammarCategory category : categoriesToCheck) {
      if (agreementWithCategoryRelaxation(token1, token2, category)) {
        categories.add(category.displayName);
      }
    }
    return categories;
  }

  private RuleMatch checkDetAdjNounAgreement(AnalyzedTokenReadings token1,
      AnalyzedTokenReadings token2, AnalyzedTokenReadings token3) {
    Set<String> set = retainCommonCategories(token1, token2, token3);
    RuleMatch ruleMatch = null;
    if (set == null || set.isEmpty()) {
      // TODO: more detailed error message:
      String msg = "Möglicherweise fehlende grammatische Übereinstimmung zwischen Artikel, Adjektiv und " +
            "Nomen bezüglich Kasus, Numerus oder Genus. Beispiel: 'mein kleiner Haus' " +
            "statt 'mein kleines Haus'";
      String shortMsg = "Möglicherweise keine Übereinstimmung bezüglich Kasus, Numerus oder Genus";
      ruleMatch = new RuleMatch(this, token1.getStartPos(), token3.getEndPos(), msg, shortMsg);
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
    Set<String> set1 = getAgreementCategories(token1, categoryToRelaxSet, true);
    if (set1 == null) {
      return true;  // word not known, assume it's correct
    }
    Set<String> set2 = getAgreementCategories(token2, categoryToRelaxSet, true);
    if (set2 == null) {
      return true;      
    }
    set1.retainAll(set2);
    return set1.size() > 0;
  }

  @Nullable
  private Set<String> retainCommonCategories(AnalyzedTokenReadings token1,
                                             AnalyzedTokenReadings token2, AnalyzedTokenReadings token3) {
    Set<GrammarCategory> categoryToRelaxSet = Collections.emptySet();
    Set<String> set1 = getAgreementCategories(token1, categoryToRelaxSet, true);
    if (set1 == null) {
      return null;  // word not known, assume it's correct
    }
    boolean skipSol = !VIELE_WENIGE_LOWERCASE.contains(token1.getToken().toLowerCase());
    Set<String> set2 = getAgreementCategories(token2, categoryToRelaxSet, skipSol);
    if (set2 == null) {
      return null;
    }
    Set<String> set3 = getAgreementCategories(token3, categoryToRelaxSet, true);
    if (set3 == null) {
      return null;
    }
    set1.retainAll(set2);
    set1.retainAll(set3);
    return set1;
  }

  private Set<String> getAgreementCategories(AnalyzedTokenReadings aToken) {
    return getAgreementCategories(aToken, new HashSet<>(), false);
  }
  
  /** Return Kasus, Numerus, Genus of those forms with a determiner. */
  private Set<String> getAgreementCategories(AnalyzedTokenReadings aToken, Set<GrammarCategory> omit, boolean skipSol) {
    Set<String> set = new HashSet<>();
    List<AnalyzedToken> readings = aToken.getReadings();
    for (AnalyzedToken tmpReading : readings) {
      if (skipSol && tmpReading.getPOSTag() != null && tmpReading.getPOSTag().endsWith(":SOL")) {
        // SOL = alleinstehend - needs to be skipped so we find errors like "An der roter Ampel."
        continue;
      }
      AnalyzedGermanToken reading = new AnalyzedGermanToken(tmpReading);
      if (reading.getCasus() == null && reading.getNumerus() == null &&
          reading.getGenus() == null) {
        continue;
      }
      if (reading.getGenus() == GermanToken.Genus.ALLGEMEIN && 
          tmpReading.getPOSTag() != null && !tmpReading.getPOSTag().endsWith(":STV") &&  // STV: stellvertretend (!= begleitend)
          !possessiveSpecialCase(aToken, tmpReading)) {   
        // genus=ALG in the original data. Not sure if this is allowed, but expand this so
        // e.g. "Ich Arbeiter" doesn't get flagged as incorrect:
        if (reading.getDetermination() == null) {
          // Nouns don't have the determination property (definite/indefinite), and as we don't want to
          // introduce a special case for that, we just pretend they always fulfill both properties:
          set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.MASKULINUM, GermanToken.Determination.DEFINITE, omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.MASKULINUM, GermanToken.Determination.INDEFINITE, omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.FEMININUM, GermanToken.Determination.DEFINITE, omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.FEMININUM, GermanToken.Determination.INDEFINITE, omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.NEUTRUM, GermanToken.Determination.DEFINITE, omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.NEUTRUM, GermanToken.Determination.INDEFINITE, omit));
        } else {
          set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.MASKULINUM, reading.getDetermination(), omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.FEMININUM, reading.getDetermination(), omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.NEUTRUM, reading.getDetermination(), omit));
        }
      } else {
        if (reading.getDetermination() == null || "jed".equals(tmpReading.getLemma()) || "manch".equals(tmpReading.getLemma())) {  // "jeder" etc. needs a special case to avoid false alarm
          set.add(makeString(reading.getCasus(), reading.getNumerus(), reading.getGenus(), GermanToken.Determination.DEFINITE, omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), reading.getGenus(), GermanToken.Determination.INDEFINITE, omit));
        } else {
          set.add(makeString(reading.getCasus(), reading.getNumerus(), reading.getGenus(), reading.getDetermination(), omit));
        }
      }
    }
    return set;
  }

  private boolean possessiveSpecialCase(AnalyzedTokenReadings aToken, AnalyzedToken tmpReading) {
    // would cause error misses as it contains 'ALG', e.g. in "Der Zustand meiner Gehirns."
    return aToken.hasPartialPosTag("PRO:POS") && ("ich".equals(tmpReading.getLemma()) || "sich".equals(tmpReading.getLemma()));
  }

  private String makeString(GermanToken.Kasus casus, GermanToken.Numerus num, GermanToken.Genus gen,
      GermanToken.Determination determination, Set<GrammarCategory> omit) {
    List<String> l = new ArrayList<>();
    if (casus != null && !omit.contains(GrammarCategory.KASUS)) {
      l.add(casus.toString());
    }
    if (num != null && !omit.contains(GrammarCategory.NUMERUS)) {
      l.add(num.toString());
    }
    if (gen != null && !omit.contains(GrammarCategory.GENUS)) {
      l.add(gen.toString());
    }
    if (determination != null) {
      l.add(determination.toString());
    }
    return String.join("/", l);
  }

  @Override
  public void reset() {
  }

}
