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
package de.danielnaber.languagetool.rules.de;

import java.io.IOException;
import java.util.*;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Category;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.tagging.de.AnalyzedGermanToken;
import de.danielnaber.languagetool.tagging.de.AnalyzedGermanTokenReadings;
import de.danielnaber.languagetool.tagging.de.GermanTagger;
import de.danielnaber.languagetool.tagging.de.GermanToken;
import de.danielnaber.languagetool.tagging.de.GermanToken.POSType;
import de.danielnaber.languagetool.tools.StringTools;

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
  private static final String[] ADJ_READINGS = new String[] {
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

  
  private static final Set<String> REL_PRONOUN = new HashSet<String>();
  static {
    REL_PRONOUN.add("der");
    REL_PRONOUN.add("die");
    REL_PRONOUN.add("das");
    REL_PRONOUN.add("dessen");
    REL_PRONOUN.add("deren");
    REL_PRONOUN.add("dem");
    REL_PRONOUN.add("den");
    REL_PRONOUN.add("welche");
    REL_PRONOUN.add("welcher");
    REL_PRONOUN.add("welchen");
    REL_PRONOUN.add("welchem");
    REL_PRONOUN.add("welches");
  }

  private static final Set<String> PREPOSITIONS = new HashSet<String>();
  static {
    PREPOSITIONS.add("in");
    PREPOSITIONS.add("auf");
    PREPOSITIONS.add("an");
    PREPOSITIONS.add("ab");
    PREPOSITIONS.add("für");
    PREPOSITIONS.add("zu");
    // TODO: add more
  }
  
  public AgreementRule(final ResourceBundle messages) {
    if (messages != null)
      super.setCategory(new Category(messages.getString("category_grammar")));
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
  public RuleMatch[] match(final AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    int pos = 0;
    for (int i = 0; i < tokens.length; i++) {
      //defaulting to the first reading
      //TODO: check for all readings
      //and replace GermanTokenReading
      final String posToken = tokens[i].getAnalyzedToken(0).getPOSTag();
      if (posToken != null && posToken.equals(JLanguageTool.SENTENCE_START_TAGNAME))
        continue;
      //AnalyzedGermanToken analyzedToken = new AnalyzedGermanToken(tokens[i]);
      
    	final AnalyzedGermanTokenReadings analyzedToken = (AnalyzedGermanTokenReadings)tokens[i];
      final boolean relevantPronoun = isRelevantPronoun(tokens, i);
     
      boolean ignore = couldBeRelativeClause(tokens, i);
      if (i > 0) {
        final String prevToken = tokens[i-1].getToken().toLowerCase();
        if ((prevToken.equals("der") || prevToken.equals("die") || prevToken.equals("das"))
            && tokens[i].getToken().equals("eine")) {
          // TODO: "der eine Polizist" -> nicht ignorieren, sondern "der polizist" checken
          ignore = true;
        }
      }
      
      // avoid false alarm on "nichts Gutes":
      if (analyzedToken.getToken().equals("nichts")) {
        ignore = true;
      }

      if ((analyzedToken.hasReadingOfType(POSType.DETERMINER) || relevantPronoun) && !ignore) {
        int tokenPos = i + 1; 
        if (tokenPos >= tokens.length)
          break;
        AnalyzedGermanTokenReadings nextToken = (AnalyzedGermanTokenReadings)tokens[tokenPos];
        nextToken = maybeAddAdjectiveReadings(nextToken, tokens, tokenPos);
        if (nextToken.hasReadingOfType(POSType.ADJEKTIV)) {
          tokenPos = i + 2; 
          if (tokenPos >= tokens.length)
            break;
          final AnalyzedGermanTokenReadings nextNextToken = (AnalyzedGermanTokenReadings)tokens[tokenPos];
          if (nextNextToken.hasReadingOfType(POSType.NOMEN)) {
            // TODO: add a case (checkAdjNounAgreement) for special cases like "deren",
            // e.g. "deren komisches Geschenke" isn't yet detected as incorrect
            final RuleMatch ruleMatch = checkDetAdjNounAgreement((AnalyzedGermanTokenReadings)tokens[i],
                nextToken, (AnalyzedGermanTokenReadings)tokens[i+2]);
            if (ruleMatch != null) {
              ruleMatches.add(ruleMatch);
            }
          }
        } else if (nextToken.hasReadingOfType(POSType.NOMEN)) {
          final RuleMatch ruleMatch = checkDetNounAgreement((AnalyzedGermanTokenReadings)tokens[i],
              (AnalyzedGermanTokenReadings)tokens[i+1]);
          if (ruleMatch != null) {
            ruleMatches.add(ruleMatch);
          }
        }
      }
     
      pos += tokens[i].getToken().length();
    }
    return toRuleMatchArray(ruleMatches);
  }

  private boolean isRelevantPronoun(AnalyzedTokenReadings[] tokens, int pos) {
    final AnalyzedGermanTokenReadings analyzedToken = (AnalyzedGermanTokenReadings)tokens[pos];
    boolean relevantPronoun = analyzedToken.hasReadingOfType(POSType.PRONOMEN);
    // avoid false alarms:
    final String token = tokens[pos].getToken();
    if (pos > 0 && tokens[pos-1].getToken().equalsIgnoreCase("vor") && tokens[pos].getToken().equalsIgnoreCase("allem"))
      relevantPronoun = false;
    else if (token.equalsIgnoreCase("er") || token.equalsIgnoreCase("sie") || token.equalsIgnoreCase("es"))
      relevantPronoun = false;
    else if (token.equalsIgnoreCase("ich"))
      relevantPronoun = false;
    else if (token.equalsIgnoreCase("du"))
      relevantPronoun = false;
    else if (token.equalsIgnoreCase("dessen"))      // avoid false alarm on: "..., dessen Leiche"
      relevantPronoun = false;
    else if (token.equalsIgnoreCase("deren"))
      relevantPronoun = false;
    else if (token.equalsIgnoreCase("sich"))      // avoid false alarm
      relevantPronoun = false;
    else if (token.equalsIgnoreCase("unser"))      // avoid false alarm "unser Produkt": TODO!
      relevantPronoun = false;
    else if (token.equalsIgnoreCase("aller"))
      relevantPronoun = false;
    else if (token.equalsIgnoreCase("man"))
      relevantPronoun = false;
    else if (token.equalsIgnoreCase("beiden"))
      relevantPronoun = false;
    else if (token.equalsIgnoreCase("wessen"))
      relevantPronoun = false;
    else if (token.equalsIgnoreCase("a"))
      relevantPronoun = false;
    else if (token.equalsIgnoreCase("alle"))
      relevantPronoun = false;
    else if (token.equalsIgnoreCase("etwas"))    // TODO: doesn't have case -- but don't just ignore
      relevantPronoun = false;
    else if (token.equalsIgnoreCase("was"))    // TODO: doesn't have case -- but don't just ignore
      relevantPronoun = false;
    else if (token.equalsIgnoreCase("wer"))
      relevantPronoun = false;
    return relevantPronoun;
  }

  // see the comment at ADJ_READINGS:
  private AnalyzedGermanTokenReadings maybeAddAdjectiveReadings(AnalyzedGermanTokenReadings nextToken,
      AnalyzedTokenReadings[] tokens, int tokenPos) {
    final String nextTerm = nextToken.getToken();
    // Just a heuristic: nouns and proper nouns that end with "er" are considered
    // city names:
    if (nextTerm.endsWith("er") && tokens.length > tokenPos+1) {
      final AnalyzedGermanTokenReadings nextNextToken = (AnalyzedGermanTokenReadings)tokens[tokenPos+1];
      final GermanTagger tagger = (GermanTagger) Language.GERMAN.getTagger();
      try {
        final AnalyzedGermanTokenReadings nextATR = tagger.lookup(nextTerm.substring(0, nextTerm.length()-2));
        final AnalyzedGermanTokenReadings nextNextATR = tagger.lookup(nextNextToken.getToken());
        //System.err.println("nextATR: " + nextATR);
        //System.err.println("nextNextATR: " + nextNextATR);
        // "Münchner": special case as cutting off last two characters doesn't produce city name:
        if ("Münchner".equals(nextTerm) ||
            (nextATR != null &&
            // tagging in Morphy for cities is not coherent:
            (nextATR.hasReadingOfType(POSType.PROPER_NOUN) || nextATR.hasReadingOfType(POSType.NOMEN) &&
            nextNextATR != null && nextNextATR.hasReadingOfType(POSType.NOMEN)))) {
          final AnalyzedGermanToken[] adjReadings = new AnalyzedGermanToken[ADJ_READINGS.length];
          for (int j = 0; j < ADJ_READINGS.length; j++) {
            adjReadings[j] = new AnalyzedGermanToken(nextTerm, ADJ_READINGS[j], null);
          }
          nextToken = new AnalyzedGermanTokenReadings(adjReadings, nextToken.getStartPos());
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

  private RuleMatch checkDetNounAgreement(final AnalyzedGermanTokenReadings token1,
      final AnalyzedGermanTokenReadings token2) {
    // avoid false alarm: "Gebt ihm Macht."
    if (token1.getToken().equalsIgnoreCase("ihm")) {
      return null;
    }
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
      ruleMatch = new RuleMatch(this, token1.getStartPos(), 
          token2.getStartPos() + token2.getToken().length(), msg);
    }
    return ruleMatch;
  }

  private List<String> getCategoriesCausingError(AnalyzedGermanTokenReadings token1, AnalyzedGermanTokenReadings token2) {
    final List<String> categories = new ArrayList<String>();
    final List<GrammarCategory> categoriesToCheck = Arrays.asList(GrammarCategory.KASUS, GrammarCategory.GENUS, GrammarCategory.NUMERUS);
    for (GrammarCategory category : categoriesToCheck) {
      if (agreementWithCategoryRelaxation(token1, token2, category)) {
        categories.add(category.displayName);
      }
    }
    return categories;
  }

  private RuleMatch checkDetAdjNounAgreement(final AnalyzedGermanTokenReadings token1,
      final AnalyzedGermanTokenReadings token2, final AnalyzedGermanTokenReadings token3) {
    final Set<String> set = retainCommonCategories(token1, token2, token3, null);
    RuleMatch ruleMatch = null;
    if (set.size() == 0) {
      // TODO: more detailed error message:
      final String msg = "Möglicherweise fehlende grammatische Übereinstimmung zwischen Artikel, Adjektiv und " +
            "Nomen bezüglich Kasus, Numerus oder Genus. Beispiel: 'mein kleiner Haus' " +
            "statt 'mein kleines Haus'";
      ruleMatch = new RuleMatch(this, token1.getStartPos(), 
          token3.getStartPos()+token3.getToken().length(), msg);
    }
    return ruleMatch;
  }

  private boolean agreementWithCategoryRelaxation(final AnalyzedGermanTokenReadings token1,
                                                  final AnalyzedGermanTokenReadings token2, final GrammarCategory categoryToRelax) {
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
  
  private Set<String> retainCommonCategories(final AnalyzedGermanTokenReadings token1, 
      final AnalyzedGermanTokenReadings token2, final AnalyzedGermanTokenReadings token3,
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

  private Set<String> getAgreementCategories(final AnalyzedGermanTokenReadings aToken) {
    return getAgreementCategories(aToken, new HashSet<GrammarCategory>());
  }
  
  /** Return Kasus, Numerus, Genus. */
  private Set<String> getAgreementCategories(final AnalyzedGermanTokenReadings aToken, Set<GrammarCategory> omit) {
    final Set<String> set = new HashSet<String>();
    final List<AnalyzedGermanToken> readings = aToken.getGermanReadings();
    for (AnalyzedGermanToken reading : readings) {
      if (reading.getCasus() == null && reading.getNumerus() == null &&
          reading.getGenus() == null)
        continue;
      if (reading.getGenus() == null && (reading.getToken().equals("ich") || reading.getToken().equals("wir"))) {
        // "ich" and "wir" contains genus=ALG in the original data. Not sure if
        // this is allowed, but expand this so "Ich Arbeiter" doesn't get flagged
        // as incorrect:
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
    final List<String> l = new ArrayList<String>();
    if (casus != null && !omit.contains(GrammarCategory.KASUS))
      l.add(casus.toString());
    if (num != null && !omit.contains(GrammarCategory.NUMERUS))
      l.add(num.toString());
    if (gen != null && !omit.contains(GrammarCategory.GENUS))
      l.add(gen.toString());
    return StringTools.listToString(l, "/");
  }

  @Override
  public void reset() {
  }

}
