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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.German;
import org.languagetool.rules.Category;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.de.AnalyzedGermanToken;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tagging.de.GermanToken;
import org.languagetool.tagging.de.GermanToken.POSType;
import org.languagetool.tools.StringTools;

import java.io.IOException;
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

  private enum GrammarCategory {
    KASUS("Kasus (Fall: Wer/Was, Wessen, Wem, Wen/Was - Beispiel: 'das Fahrrads' statt 'des Fahrrads')"),
    GENUS("Genus (männlich, weiblich, sächlich - Beispiel: 'der Fahrrad' statt 'das Fahrrad')"),
    NUMERUS("Numerus (Einzahl, Mehrzahl - Beispiel: 'das Fahrräder' statt 'die Fahrräder')");
    
    private final String displayName;
    private GrammarCategory(String displayName) {
      this.displayName = displayName;
    }
  }

  /*
   * City names are incoherently tagged in the Morphy data. To avoid
   * false alarms on phrases like "das Berliner Auto" we have to
   * explicitly add these adjective readings to "Berliner" and to all
   * other potential city names:
   */
  private static final String[] ADJ_READINGS = {
    // singular:
    "ADJ:NOM:SIN:MAS:GRU", "ADJ:NOM:SIN:NEU:GRU", "ADJ:NOM:SIN:FEM:GRU",    // das Berliner Auto
    "ADJ:GEN:SIN:MAS:GRU", "ADJ:GEN:SIN:NEU:GRU", "ADJ:GEN:SIN:FEM:GRU",    // des Berliner Autos 
    "ADJ:DAT:SIN:MAS:GRU", "ADJ:DAT:SIN:NEU:GRU", "ADJ:DAT:SIN:FEM:GRU",    // dem Berliner Auto
    "ADJ:AKK:SIN:MAS:GRU", "ADJ:AKK:SIN:NEU:GRU", "ADJ:AKK:SIN:FEM:GRU",    // den Berliner Bewohner
    // plural:
    "ADJ:NOM:PLU:MAS:GRU", "ADJ:NOM:PLU:NEU:GRU", "ADJ:NOM:PLU:FEM:GRU",    // die Berliner Autos
    "ADJ:GEN:PLU:MAS:GRU", "ADJ:GEN:PLU:NEU:GRU", "ADJ:GEN:PLU:FEM:GRU",    // der Berliner Autos 
    "ADJ:DAT:PLU:MAS:GRU", "ADJ:DAT:PLU:NEU:GRU", "ADJ:DAT:PLU:FEM:GRU",    // den Berliner Autos
    "ADJ:AKK:PLU:MAS:GRU", "ADJ:AKK:PLU:NEU:GRU", "ADJ:AKK:PLU:FEM:GRU",    // den Berliner Bewohnern
  };
  
  /*
   * The heuristic of maybeAddAdjectiveReadings considers every noun ending with "er" as city name.
   * The nouns in this list are NOT considered as city names.
   * NOTE: Only nouns for which cutting off the final "er" produces a valid noun must be added to this list.
   */
  private static final Set<String> ER_TO_BE_IGNORED = new HashSet<>(Arrays.asList(
    "Alter",
    "Kinder",
    "Rinder"
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
    PREPOSITIONS.add("für");
    PREPOSITIONS.add("zu");
    PREPOSITIONS.add("bei");
    PREPOSITIONS.add("nach");
    PREPOSITIONS.add("über");
    PREPOSITIONS.add("von");
    PREPOSITIONS.add("mit");
    // TODO: add more
  }
  
  private final German german = new German();
  private final GermanTagger tagger = (GermanTagger) german.getTagger();

  private static final Set<String> PRONOUNS_TO_BE_IGNORED = new HashSet<>(Arrays.asList(
    "ich",
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
    "diejenigen"
  ));
    
  public AgreementRule(final ResourceBundle messages) {
    if (messages != null) {
      super.setCategory(new Category(messages.getString("category_grammar")));
    }
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
  public RuleMatch[] match(final AnalyzedSentence sentence) {
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    final AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();    
    for (int i = 0; i < tokens.length; i++) {
      //defaulting to the first reading
      //TODO: check for all readings
      //and replace GermanTokenReading
      final String posToken = tokens[i].getAnalyzedToken(0).getPOSTag();
      if (posToken != null && posToken.equals(JLanguageTool.SENTENCE_START_TAGNAME))
        continue;
      //AnalyzedGermanToken analyzedToken = new AnalyzedGermanToken(tokens[i]);
      
      final AnalyzedTokenReadings tokenReadings = tokens[i];
      final boolean relevantPronoun = isRelevantPronoun(tokens, i);
     
      boolean ignore = couldBeRelativeClause(tokens, i);
      if (i > 0) {
        final String prevToken = tokens[i-1].getToken().toLowerCase();
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
      if (detAbbrev || detAdjAbbrev) {
        ignore = true;
      }

      if ((GermanHelper.hasReadingOfType(tokenReadings, POSType.DETERMINER) || relevantPronoun) && !ignore) {
        int tokenPos = i + 1; 
        if (tokenPos >= tokens.length)
          break;
        AnalyzedTokenReadings nextToken = tokens[tokenPos];
        nextToken = maybeAddAdjectiveReadings(nextToken, tokens, tokenPos);
        if (isNonPredicativeAdjective(nextToken)) {
          tokenPos = i + 2; 
          if (tokenPos >= tokens.length)
            break;
          if (GermanHelper.hasReadingOfType(tokens[tokenPos], POSType.NOMEN)) {
            // TODO: add a case (checkAdjNounAgreement) for special cases like "deren",
            // e.g. "deren komisches Geschenke" isn't yet detected as incorrect
            final RuleMatch ruleMatch = checkDetAdjNounAgreement(tokens[i],
                nextToken, tokens[i+2]);
            if (ruleMatch != null) {
              ruleMatches.add(ruleMatch);
            }
          }
        } else if (GermanHelper.hasReadingOfType(nextToken, POSType.NOMEN)) {
          final RuleMatch ruleMatch = checkDetNounAgreement(tokens[i],
                  tokens[i+1]);
          if (ruleMatch != null) {
            ruleMatches.add(ruleMatch);
          }
        }
      }
           
    } // for each token
    return toRuleMatchArray(ruleMatches);
  }

  private boolean isNonPredicativeAdjective(AnalyzedTokenReadings tokensReadings) {
    for (AnalyzedToken reading : tokensReadings.getReadings()) {
      if (reading instanceof AnalyzedGermanToken) {
        final AnalyzedGermanToken germanReading = (AnalyzedGermanToken) reading;
        if (germanReading.getType() == POSType.ADJEKTIV && !germanReading.getPOSTag().contains("PRD")) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isRelevantPronoun(AnalyzedTokenReadings[] tokens, int pos) {
    final AnalyzedTokenReadings analyzedToken = tokens[pos];
    boolean relevantPronoun = GermanHelper.hasReadingOfType(analyzedToken, POSType.PRONOMEN);
    // avoid false alarms:
    final String token = tokens[pos].getToken();
    if (pos > 0 && tokens[pos-1].getToken().equalsIgnoreCase("vor") && tokens[pos].getToken().equalsIgnoreCase("allem")) {
      relevantPronoun = false;
    } else if (PRONOUNS_TO_BE_IGNORED.contains(token.toLowerCase())) {
      relevantPronoun = false;
    }
    return relevantPronoun;
  }

  // see the comment at ADJ_READINGS:
  private AnalyzedTokenReadings maybeAddAdjectiveReadings(AnalyzedTokenReadings nextToken,
      AnalyzedTokenReadings[] tokens, int tokenPos) {
    final String nextTerm = nextToken.getToken();
    // Just a heuristic: nouns and proper nouns that end with "er" are considered
    // city names:
    if (nextTerm.endsWith("er") && tokens.length > tokenPos+1 && !ER_TO_BE_IGNORED.contains(nextTerm)) {
      final AnalyzedTokenReadings nextNextToken = tokens[tokenPos+1];
      try {
        final AnalyzedTokenReadings nextATR = tagger.lookup(nextTerm.substring(0, nextTerm.length()-2));
        final AnalyzedTokenReadings nextNextATR = tagger.lookup(nextNextToken.getToken());
        //System.err.println("nextATR: " + nextATR);
        //System.err.println("nextNextATR: " + nextNextATR);
        // "Münchner": special case as cutting off last two characters doesn't produce city name:
        if ("Münchner".equals(nextTerm) ||
            (nextATR != null &&
            // tagging in Morphy for cities is not coherent:
            (GermanHelper.hasReadingOfType(nextATR, POSType.PROPER_NOUN) || GermanHelper.hasReadingOfType(nextATR, POSType.NOMEN) &&
            nextNextATR != null && GermanHelper.hasReadingOfType(nextNextATR, POSType.NOMEN)))) {
          final AnalyzedGermanToken[] adjReadings = new AnalyzedGermanToken[ADJ_READINGS.length];
          for (int j = 0; j < ADJ_READINGS.length; j++) {
            adjReadings[j] = new AnalyzedGermanToken(nextTerm, ADJ_READINGS[j], null);
          }
          nextToken = new AnalyzedTokenReadings(adjReadings, nextToken.getStartPos());
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return nextToken;
  }

  // TODO: improve this so it only returns true for real relative clauses
  private boolean couldBeRelativeClause(AnalyzedTokenReadings[] tokens, int pos) {
    boolean comma;
    boolean relPronoun;
    if (pos >= 1) {
      // avoid false alarm: "Das Wahlrecht, das Frauen zugesprochen bekamen." etc:
      comma = tokens[pos-1].getToken().equals(",");
      final String term = tokens[pos].getToken().toLowerCase();
      relPronoun = REL_PRONOUN.contains(term);
      if (comma && relPronoun)
        return true;
    }
    if (pos >= 2) {
      // avoid false alarm: "Der Mann, in dem quadratische Fische schwammen."
      comma = tokens[pos-2].getToken().equals(",");
      final String term1 = tokens[pos-1].getToken().toLowerCase();
      final String term2 = tokens[pos].getToken().toLowerCase();
      final boolean prep = PREPOSITIONS.contains(term1);
      relPronoun = REL_PRONOUN.contains(term2);
      return comma && prep && relPronoun;
    }
    return false;
  }

  private RuleMatch checkDetNounAgreement(final AnalyzedTokenReadings token1,
      final AnalyzedTokenReadings token2) {
    RuleMatch ruleMatch = null;
    final Set<String> set1 = getAgreementCategories(token1);
    if (set1 == null) {
      return null;  // word not known, assume it's correct
    }
    final Set<String> set2 = getAgreementCategories(token2);
    if (set2 == null) {
      return null;
    }
    set1.retainAll(set2);
    if (set1.size() == 0) {
      final List<String> errorCategories = getCategoriesCausingError(token1, token2);
      final String errorDetails = errorCategories.size() > 0 ? StringTools.listToString(errorCategories, " und ") : "Kasus, Genus oder Numerus";
      final String msg = "Möglicherweise fehlende grammatische Übereinstimmung zwischen Artikel und Nomen " +
            "bezüglich " + errorDetails + ".";
      final AgreementSuggestor suggestor = new AgreementSuggestor(german.getSynthesizer(), token1, token2);
      final List<String> suggestions = suggestor.getSuggestions();
      final String shortMsg = "Möglicherweise keine Übereinstimmung bezüglich " + errorDetails;
      ruleMatch = new RuleMatch(this, token1.getStartPos(), 
          token2.getStartPos() + token2.getToken().length(), msg, shortMsg);
      ruleMatch.setSuggestedReplacements(suggestions);
    }
    return ruleMatch;
  }

  private List<String> getCategoriesCausingError(AnalyzedTokenReadings token1, AnalyzedTokenReadings token2) {
    final List<String> categories = new ArrayList<>();
    final List<GrammarCategory> categoriesToCheck = Arrays.asList(GrammarCategory.KASUS, GrammarCategory.GENUS, GrammarCategory.NUMERUS);
    for (GrammarCategory category : categoriesToCheck) {
      if (agreementWithCategoryRelaxation(token1, token2, category)) {
        categories.add(category.displayName);
      }
    }
    return categories;
  }

  private RuleMatch checkDetAdjNounAgreement(final AnalyzedTokenReadings token1,
      final AnalyzedTokenReadings token2, final AnalyzedTokenReadings token3) {
    final Set<String> set = retainCommonCategories(token1, token2, token3, null);
    RuleMatch ruleMatch = null;
    if (set.size() == 0) {
      // TODO: more detailed error message:
      final String msg = "Möglicherweise fehlende grammatische Übereinstimmung zwischen Artikel, Adjektiv und " +
            "Nomen bezüglich Kasus, Numerus oder Genus. Beispiel: 'mein kleiner Haus' " +
            "statt 'mein kleines Haus'";
      final String shortMsg = "Möglicherweise keine Übereinstimmung bezüglich Kasus, Numerus oder Genus";
      ruleMatch = new RuleMatch(this, token1.getStartPos(), 
          token3.getStartPos()+token3.getToken().length(), msg, shortMsg);
    }
    return ruleMatch;
  }

  private boolean agreementWithCategoryRelaxation(final AnalyzedTokenReadings token1,
                                                  final AnalyzedTokenReadings token2, final GrammarCategory categoryToRelax) {
    final Set<GrammarCategory> categoryToRelaxSet;
    if (categoryToRelax != null) {
      categoryToRelaxSet = Collections.singleton(categoryToRelax);
    } else {
      categoryToRelaxSet = Collections.emptySet();
    }
    final Set<String> set1 = getAgreementCategories(token1, categoryToRelaxSet);
    if (set1 == null) {
      return true;  // word not known, assume it's correct
    }
    final Set<String> set2 = getAgreementCategories(token2, categoryToRelaxSet);
    if (set2 == null) {
      return true;      
    }
    set1.retainAll(set2);
    return set1.size() > 0;
  }
  
  private Set<String> retainCommonCategories(final AnalyzedTokenReadings token1, 
      final AnalyzedTokenReadings token2, final AnalyzedTokenReadings token3,
      final GrammarCategory categoryToRelax) {
    final Set<GrammarCategory> categoryToRelaxSet;
    if (categoryToRelax == null) {
      categoryToRelaxSet = Collections.singleton(categoryToRelax);
    } else {
      categoryToRelaxSet = Collections.emptySet();
    }
    final Set<String> set1 = getAgreementCategories(token1, categoryToRelaxSet);
    if (set1 == null)
      return null;  // word not known, assume it's correct
    final Set<String> set2 = getAgreementCategories(token2, categoryToRelaxSet);
    if (set2 == null)
      return null;
    final Set<String> set3 = getAgreementCategories(token3, categoryToRelaxSet);
    if (set3 == null)
      return null;
    /*System.err.println(token1.getToken()+"#"+set1);
    System.err.println(token2.getToken()+"#"+set2);
    System.err.println(token3.getToken()+"#"+set3);
    System.err.println("");*/
    set1.retainAll(set2);
    set1.retainAll(set3);
    return set1;
  }

  private Set<String> getAgreementCategories(final AnalyzedTokenReadings aToken) {
    return getAgreementCategories(aToken, new HashSet<GrammarCategory>());
  }
  
  /** Return Kasus, Numerus, Genus. */
  private Set<String> getAgreementCategories(final AnalyzedTokenReadings aToken, Set<GrammarCategory> omit) {
    final Set<String> set = new HashSet<>();
    final List<AnalyzedToken> readings = aToken.getReadings();
    for (AnalyzedToken tmpReading : readings) {
      final AnalyzedGermanToken reading = new AnalyzedGermanToken(tmpReading);
      if (reading.getCasus() == null && reading.getNumerus() == null &&
          reading.getGenus() == null) {
        continue;
      }
      if (reading.getGenus() == GermanToken.Genus.ALLGEMEIN && 
              reading.getPOSTag() != null && !reading.getPOSTag().endsWith(":STV")) {  // STV: stellvertretend (!= begleitend)
        // genus=ALG in the original data. Not sure if this is allowed, but expand this so
        // e.g. "Ich Arbeiter" doesn't get flagged as incorrect:
        set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.MASKULINUM, omit));
        set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.FEMININUM, omit));
        set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.NEUTRUM, omit));
      } else {
        set.add(makeString(reading.getCasus(), reading.getNumerus(), reading.getGenus(), omit));
      }
    }
    return set;
  }

  private String makeString(GermanToken.Kasus casus, GermanToken.Numerus num, GermanToken.Genus gen,
      Set<GrammarCategory> omit) {
    final List<String> l = new ArrayList<>();
    if (casus != null && !omit.contains(GrammarCategory.KASUS)) {
      l.add(casus.toString());
    }
    if (num != null && !omit.contains(GrammarCategory.NUMERUS)) {
      l.add(num.toString());
    }
    if (gen != null && !omit.contains(GrammarCategory.GENUS)) {
      l.add(gen.toString());
    }
    return StringTools.listToString(l, "/");
  }

  @Override
  public void reset() {
  }

}
