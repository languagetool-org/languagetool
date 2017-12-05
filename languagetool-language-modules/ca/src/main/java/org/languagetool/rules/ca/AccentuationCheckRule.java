/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Jaume Ortolà i Font
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
package org.languagetool.rules.ca;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;

/**
 * This rule checks if a word without graphical accent and with a verb POS tag
 * should be a noun or an adjective with graphical accent. It uses two lists of
 * word pairs: verb-noun and verb-adjective.
 * 
 * @author Jaume Ortolà i Font
 */
public class AccentuationCheckRule extends Rule {

  /**
   * Patterns
   */
  private static final Pattern PREPOSICIO_DE = Pattern.compile("de|d'|del|dels");
  private static final Pattern ARTICLE_EL_MS = Pattern.compile("el|l'|El|L'");
  private static final Pattern ARTICLE_EL_FS = Pattern.compile("la|l'|La|L'");
  private static final Pattern ARTICLE_EL_MP = Pattern.compile("els|Els");
  private static final Pattern ARTICLE_EL_FP = Pattern.compile("les|Les");
  private static final Pattern DETERMINANT = Pattern.compile("D[^R].*");
  private static final Pattern DETERMINANT_MS = Pattern.compile("D[^R].[MC][SN].*");
  private static final Pattern DETERMINANT_FS = Pattern.compile("D[^R].[FC][SN].*");
  private static final Pattern DETERMINANT_MP = Pattern.compile("D[^R].[MC][PN].*");
  private static final Pattern DETERMINANT_FP = Pattern.compile("D[^R].[FC][PN].*");
  private static final Pattern NOM_MS = Pattern.compile("NC[MC][SN].*");
  private static final Pattern NOM_FS = Pattern.compile("NC[FC][SN].*");
  private static final Pattern NOM_MP = Pattern.compile("NC[MC][PN].*");
  private static final Pattern NOM_FP = Pattern.compile("NC[FC][PN].*");
  private static final Pattern ADJECTIU_MS = Pattern.compile("A..[MC][SN].*|V.P..SM.?|PX.MS.*");
  private static final Pattern ADJECTIU_FS = Pattern.compile("A..[FC][SN].*|V.P..SF.?|PX.FS.*");
  private static final Pattern ADJECTIU_MP = Pattern.compile("A..[MC][PN].*|V.P..PM.?|PX.MP.*");
  private static final Pattern ADJECTIU_FP = Pattern.compile("A..[FC][PN].*|V.P..PF.?|PX.FP.*");
  private static final Pattern INFINITIU = Pattern.compile("V.N.*");
  private static final Pattern VERB_CONJUGAT = Pattern.compile("V.[^NGP].*|_GV_");
  private static final Pattern PARTICIPI_MS = Pattern.compile("V.P.*SM.?");
  private static final Pattern GRUP_VERBAL = Pattern.compile("_GV_");
  private static final Pattern VERB_3S = Pattern.compile("V...3S..?");
  private static final Pattern NOT_IN_PREV_TOKEN = Pattern.compile("VA.*|PP.*|P0.*|VSP.*");
  private static final Pattern BEFORE_ADJECTIVE_MS = Pattern.compile("SPS00|D[^R].[MC][SN].*|V.[^NGP].*|PX.*");
  private static final Pattern BEFORE_ADJECTIVE_FS = Pattern.compile("SPS00|D[^R].[FC][SN].*|V.[^NGP].*|PX.*");
  private static final Pattern BEFORE_ADJECTIVE_MP = Pattern.compile("SPS00|D[^R].[MC][PN].*|V.[^NGP].*|PX.*");
  private static final Pattern BEFORE_ADJECTIVE_FP = Pattern.compile("SPS00|D[^R].[FC][PN].*|V.[^NGP].*|PX.*");
  private static final Pattern GN = Pattern.compile(".*_GN_.*|<?/?N[CP].*");
  private static final Pattern EXCEPCIONS_DARRERE_DE = Pattern.compile("forma|manera|por|costat", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern LOCUCIONS = Pattern.compile(".*LOC.*");
  private static final Pattern PRONOM_FEBLE = Pattern.compile("P0.{6}|PP3CN000|PP3NN000|PP3CP000|PP3CSD00"); // Exclosos: PP3..A00 (coincideixe amb articles determinats)

  private static final Map<String, AnalyzedTokenReadings> relevantWords = 
          new AccentuationDataLoader().loadWords("/ca/verb_senseaccent_nom_ambaccent.txt");
  private static final Map<String, AnalyzedTokenReadings> relevantWords2 =
          new AccentuationDataLoader().loadWords("/ca/verb_senseaccent_adj_ambaccent.txt");

  public AccentuationCheckRule(ResourceBundle messages) throws IOException {
    super.setCategory(Categories.MISC.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Misspelling);
  }

  @Override
  public String getId() {
    return "ACCENTUATION_CHECK";
  }

  @Override
  public String getDescription() {
    return "Comprova si la paraula ha de dur accent gr\u00E0fic.";
  }

  @Override
  public RuleMatch[] match(final AnalyzedSentence sentence) {
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    final AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    for (int i = 1; i < tokens.length; i++) { 
      // ignoring token 0, i.e. SENT_START
      final String token;
      if (i == 1) {
        token = tokens[i].getToken().toLowerCase();
      } else {
        token = tokens[i].getToken();
      }
      final String prevToken = tokens[i - 1].getToken();
      String prevPrevToken = "";
      if (i > 2) {
        prevPrevToken = tokens[i - 2].getToken();
      }
      String nextToken = "";
      if (i < tokens.length - 1) {
        nextToken = tokens[i + 1].getToken();
      }
      String nextNextToken = "";
      if (i < tokens.length - 2) {
        nextNextToken = tokens[i + 2].getToken();
      }
      boolean isRelevantWord = false;
      boolean isRelevantWord2 = false;
      if (StringTools.isEmpty(token)) {
        continue;
      }
      if (relevantWords.containsKey(token)) {
        isRelevantWord = true;
      }
      if (relevantWords2.containsKey(token)) {
        isRelevantWord2 = true;
      }

      if (!isRelevantWord && !isRelevantWord2) {
        continue;
      }

      // verb amb pronom feble davant
      if (matchPostagRegexp(tokens[i - 1], PRONOM_FEBLE)
          && !prevToken.startsWith("'")
          && !prevToken.startsWith("-")) {
        continue;
      }
      
      
      String replacement = null;
      final Matcher mPreposicioDE = PREPOSICIO_DE.matcher(nextToken);
      final Matcher mExcepcionsDE = EXCEPCIONS_DARRERE_DE.matcher(nextNextToken);
      final Matcher mArticleELMS = ARTICLE_EL_MS.matcher(prevToken);
      final Matcher mArticleELFS = ARTICLE_EL_FS.matcher(prevToken);
      final Matcher mArticleELMP = ARTICLE_EL_MP.matcher(prevToken);
      final Matcher mArticleELFP = ARTICLE_EL_FP.matcher(prevToken);

      // VERB WITHOUT ACCENT -> NOUN WITH ACCENT
      if (isRelevantWord && !matchPostagRegexp(tokens[i], GN) && !matchPostagRegexp(tokens[i], LOCUCIONS)) {
        // amb renuncies
        if (tokens[i - 1].hasPosTag("SPS00") && !tokens[i - 1].hasPosTag("RG")
            && !matchPostagRegexp(tokens[i - 1], DETERMINANT)
            && !matchPostagRegexp(tokens[i], INFINITIU)) {
          replacement = relevantWords.get(token).getToken();
        }
        else if (i > 2 && tokens[i - 2].hasPosTag("SPS00") 
            && !tokens[i - 2].hasPosTag("RG")
            && !matchPostagRegexp(tokens[i - 2], DETERMINANT)
            && (matchPostagRegexp(tokens[i - 1], DETERMINANT) 
                || mArticleELMS.matches() || mArticleELFS.matches() 
                || mArticleELMP.matches() || mArticleELFP.matches() )
            && !matchPostagRegexp(tokens[i], INFINITIU)) {
          replacement = relevantWords.get(token).getToken();
        }
        // aquestes renuncies
        else if (((matchPostagRegexp(tokens[i - 1], DETERMINANT_MS) && matchPostagRegexp(relevantWords.get(token), NOM_MS) 
              && !token.equals("cantar"))
            || (matchPostagRegexp(tokens[i - 1], DETERMINANT_MP) && matchPostagRegexp(relevantWords.get(token), NOM_MP))
            || (matchPostagRegexp(tokens[i - 1], DETERMINANT_FS) && matchPostagRegexp(relevantWords.get(token), NOM_FS)
                && !token.equals("venia") && !token.equals("tenia") && !token.equals("continua") && !token.equals("genera") 
                && !token.equals("faria")) 
            || (matchPostagRegexp(tokens[i - 1], DETERMINANT_FP) && matchPostagRegexp(relevantWords.get(token), NOM_FP)))) {
          replacement = relevantWords.get(token).getToken();
        }
        // fumaré una faria (correct: fària)
        else if (i > 2
            && matchPostagRegexp(tokens[i - 2], VERB_CONJUGAT)
            && ((matchPostagRegexp(tokens[i - 1], DETERMINANT_MS) && matchPostagRegexp(relevantWords.get(token), NOM_MS))
                || (matchPostagRegexp(tokens[i - 1], DETERMINANT_MP) && matchPostagRegexp(relevantWords.get(token), NOM_MP))
                || (matchPostagRegexp(tokens[i - 1], DETERMINANT_FS) && matchPostagRegexp(relevantWords.get(token), NOM_FS)) 
                || (matchPostagRegexp(tokens[i - 1], DETERMINANT_FP) && matchPostagRegexp(relevantWords.get(token), NOM_FP)))) {
          replacement = relevantWords.get(token).getToken();
        }
        // fem la copia (correct: còpia)
        else if (i > 2
            && matchPostagRegexp(tokens[i - 2], VERB_CONJUGAT)
            && ((mArticleELMS.matches() && matchPostagRegexp(relevantWords.get(token), NOM_MS))
                || (mArticleELMP.matches() && matchPostagRegexp(relevantWords.get(token), NOM_MP))
                || (mArticleELFS.matches() && matchPostagRegexp(relevantWords.get(token), NOM_FS)) 
                || (mArticleELFP.matches() && matchPostagRegexp(relevantWords.get(token),NOM_FP)))) {
          replacement = relevantWords.get(token).getToken();
        }
        // circumstancies d'una altra classe
        else if (!matchPostagRegexp(tokens[i], PARTICIPI_MS)
            && !token.equals("venia") && !token.equals("venies")
            && !token.equals("tenia") && !token.equals("tenies")
            && !token.equals("faria") && !token.equals("faries")
            && !token.equals("espero") && !token.equals("continua")
            && !token.equals("continues") && !token.equals("cantar")
            && !prevToken.equals("que") && !prevToken.equals("qui")
            && !prevToken.equals("què") && mPreposicioDE.matches()
            && !matchPostagRegexp(tokens[i - 1], NOT_IN_PREV_TOKEN)
            && !matchPostagRegexp(tokens[i + 1], LOCUCIONS)
            && (i < tokens.length - 2)
            && !matchPostagRegexp(tokens[i + 2], INFINITIU)
            && !mExcepcionsDE.matches() && !tokens[i - 1].hasPosTag("RG")) {
          replacement = relevantWords.get(token).getToken();
        }
        // la renuncia del president.
        else if (!token.equals("venia")
            && !token.equals("venies")
            && !token.equals("tenia")
            && !token.equals("tenies")
            && !token.equals("faria")
            && !token.equals("faries")
            && !token.equals("continua")
            && !token.equals("continues")
            && !token.equals("cantar")
            && !token.equals("diferencia")
            && !token.equals("diferencies")
            && !token.equals("distancia")
            && !token.equals("distancies")
            && ((mArticleELMS.matches() && matchPostagRegexp(
                relevantWords.get(token), NOM_MS))
                || (mArticleELFS.matches() && matchPostagRegexp(
                    relevantWords.get(token), NOM_FS))
                || (mArticleELMP.matches() && matchPostagRegexp(
                    relevantWords.get(token), NOM_MP)) || (mArticleELFP
                .matches() && matchPostagRegexp(relevantWords.get(token),
                NOM_FP)))

            && mPreposicioDE.matches()) {
          replacement = relevantWords.get(token).getToken();
        }
        // circumstancies extraordinàries
        else if (!token.equals("pronuncia") && !token.equals("espero") && !token.equals("pronuncies")
            && !token.equals("venia") && !token.equals("venies") && !token.equals("tenia")
            && !token.equals("tenies") && !token.equals("continua") && !token.equals("continues")
            && !token.equals("faria") && !token.equals("faries") && !token.equals("genera")
            && !token.equals("figuri")
            && (i < tokens.length - 1)
            && ((matchPostagRegexp(relevantWords.get(token), NOM_MS) && matchPostagRegexp(tokens[i + 1], ADJECTIU_MS))
                || (matchPostagRegexp(relevantWords.get(token), NOM_FS) && matchPostagRegexp(tokens[i + 1], ADJECTIU_FS))
                || (matchPostagRegexp(relevantWords.get(token), NOM_MP) && matchPostagRegexp(tokens[i + 1], ADJECTIU_MP)) 
                || (matchPostagRegexp(relevantWords.get(token), NOM_FP) && matchPostagRegexp(tokens[i + 1], ADJECTIU_FP)))) {
          replacement = relevantWords.get(token).getToken();
        }
        // les seves contraries
        else if ((matchPostagRegexp(relevantWords.get(token), NOM_MS) && matchPostagRegexp(tokens[i - 1], ADJECTIU_MS)
              && !matchPostagRegexp(tokens[i], VERB_3S) && !matchPostagRegexp(tokens[i], GRUP_VERBAL))
            || (matchPostagRegexp(relevantWords.get(token), NOM_FS) && matchPostagRegexp(tokens[i - 1], ADJECTIU_FS) 
              && !matchPostagRegexp(tokens[i], VERB_3S))
            || (matchPostagRegexp(relevantWords.get(token), NOM_MP) && matchPostagRegexp(tokens[i - 1], ADJECTIU_MP))
            || (matchPostagRegexp(relevantWords.get(token), NOM_FP) && matchPostagRegexp(tokens[i - 1], ADJECTIU_FP))) {
          replacement = relevantWords.get(token).getToken();
        }
        //una nova formula que (fórmula)
        else if (nextToken.equals("que") && i>2
            && ((matchPostagRegexp(relevantWords.get(token), NOM_MS) && matchPostagRegexp(tokens[i - 1], ADJECTIU_MS)
                && matchPostagRegexp(tokens[i - 2], DETERMINANT_MS))
            || (matchPostagRegexp(relevantWords.get(token), NOM_FS) && matchPostagRegexp(tokens[i - 1], ADJECTIU_FS)
                && matchPostagRegexp(tokens[i - 2], DETERMINANT_FS))
            || (matchPostagRegexp(relevantWords.get(token), NOM_MP) && matchPostagRegexp(tokens[i - 1], ADJECTIU_MP)
                && matchPostagRegexp(tokens[i - 2], DETERMINANT_MP))
            || (matchPostagRegexp(relevantWords.get(token), NOM_FP) && matchPostagRegexp(tokens[i - 1], ADJECTIU_FP)
                && matchPostagRegexp(tokens[i - 2], DETERMINANT_FP)))) {
          replacement = relevantWords.get(token).getToken();
        }
        // les circumstancies que ens envolten
        else if (nextToken.equals("que")
            && ((mArticleELMS.matches() && matchPostagRegexp(relevantWords.get(token), NOM_MS))
                || (mArticleELFS.matches() && matchPostagRegexp(relevantWords.get(token), NOM_FS))
                || (mArticleELMP.matches() && matchPostagRegexp(relevantWords.get(token), NOM_MP)) 
                || (mArticleELFP.matches() && matchPostagRegexp(relevantWords.get(token), NOM_FP)))) {
          replacement = relevantWords.get(token).getToken();
        }
        // de positiva influencia
        if (!token.equals("pronuncia") && !token.equals("espero") && !token.equals("pronuncies")
                && !token.equals("venia") && !token.equals("venies") && !token.equals("tenia")
                && !token.equals("tenies") && !token.equals("continua") && !token.equals("continues")
                && !token.equals("faria") && !token.equals("faries") && !token.equals("genera")
                && !token.equals("figuri")
            && i>2 
            && tokens[i - 2].hasPosTag("SPS00") && !tokens[i - 2].hasPosTag("RG")           
            && ((matchPostagRegexp(relevantWords.get(token), NOM_MS) && matchPostagRegexp(tokens[i - 1], ADJECTIU_MS))
                || (matchPostagRegexp(relevantWords.get(token), NOM_FS) && matchPostagRegexp(tokens[i - 1], ADJECTIU_FS))
                || (matchPostagRegexp(relevantWords.get(token), NOM_MP) && matchPostagRegexp(tokens[i - 1], ADJECTIU_MP)) 
                || (matchPostagRegexp(relevantWords.get(token), NOM_FP) && matchPostagRegexp(tokens[i - 1], ADJECTIU_FP)))) {
          replacement = relevantWords.get(token).getToken();
        }
      }

      // VERB WITHOUT ACCENT -> ADJECTIVE WITH ACCENT
      if (isRelevantWord2 && !matchPostagRegexp(tokens[i], GN) && !matchPostagRegexp(tokens[i], LOCUCIONS)) {
        // de manera obvia, circumstàncies extraordinaries.
        if ((matchPostagRegexp(relevantWords2.get(token), ADJECTIU_MS) && matchPostagRegexp(tokens[i - 1], NOM_MS)
              && !tokens[i - 1].hasPosTag("_GN_FS") && matchPostagRegexp(tokens[i], VERB_CONJUGAT) 
              && !matchPostagRegexp(tokens[i], VERB_3S))
            || (matchPostagRegexp(relevantWords2.get(token), ADJECTIU_FS) && prevPrevToken.equalsIgnoreCase("de") 
                && (prevToken.equals("manera") || prevToken.equals("forma")))
            || (matchPostagRegexp(relevantWords2.get(token), ADJECTIU_MP) && matchPostagRegexp(tokens[i - 1], NOM_MP))
            || (matchPostagRegexp(relevantWords2.get(token), ADJECTIU_FP) && matchPostagRegexp(tokens[i - 1], NOM_FP))) {
          replacement = relevantWords2.get(token).getToken();
        }
        // de continua disputa
        else if ((i < tokens.length - 1)
            && !prevToken.equals("que")
            && !matchPostagRegexp(tokens[i - 1], NOT_IN_PREV_TOKEN)
            && ((matchPostagRegexp(relevantWords2.get(token), ADJECTIU_MS) && matchPostagRegexp(tokens[i + 1], NOM_MS) 
                && matchPostagRegexp(tokens[i - 1], BEFORE_ADJECTIVE_MS))
            || (matchPostagRegexp(relevantWords2.get(token), ADJECTIU_FS) && matchPostagRegexp(tokens[i + 1], NOM_FS) 
                && matchPostagRegexp(tokens[i - 1], BEFORE_ADJECTIVE_FS))
            || (matchPostagRegexp(relevantWords2.get(token), ADJECTIU_MP) && matchPostagRegexp(tokens[i + 1], NOM_MP) 
                && matchPostagRegexp(tokens[i - 1], BEFORE_ADJECTIVE_MP)) 
            || (matchPostagRegexp(relevantWords2.get(token), ADJECTIU_FP) && matchPostagRegexp(tokens[i + 1], NOM_FP) 
                && matchPostagRegexp(tokens[i - 1], BEFORE_ADJECTIVE_FP)))) {
          replacement = relevantWords2.get(token).getToken();
        }
        // la magnifica conservació
        else if ((i < tokens.length - 1)
            && ((matchPostagRegexp(relevantWords2.get(token), ADJECTIU_MS)
                && matchPostagRegexp(tokens[i + 1], NOM_MS) && mArticleELMS.matches())
            || (matchPostagRegexp(relevantWords2.get(token), ADJECTIU_FS)
                && matchPostagRegexp(tokens[i + 1], NOM_FS) && mArticleELFS.matches())
            || (matchPostagRegexp(relevantWords2.get(token), ADJECTIU_MP)
                && matchPostagRegexp(tokens[i + 1], NOM_MP) && mArticleELMP.matches()) 
            || (matchPostagRegexp(relevantWords2.get(token), ADJECTIU_FP)
                && matchPostagRegexp(tokens[i + 1], NOM_FP) && mArticleELFP.matches()))) {
          replacement = relevantWords2.get(token).getToken();
        }

      }
      if (replacement != null) {
        final String msg = "Si \u00E9s un nom o un adjectiu, ha de portar accent.";
        final RuleMatch ruleMatch = new RuleMatch(this, sentence,
            tokens[i].getStartPos(), tokens[i].getEndPos(),
            msg, "Falta un accent");
        ruleMatch.setSuggestedReplacement(replacement);
        ruleMatches.add(ruleMatch);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  /**
   * Match POS tag with regular expression
   */
  private boolean matchPostagRegexp(AnalyzedTokenReadings aToken,
      Pattern pattern) {
    boolean matches = false;
    for (AnalyzedToken analyzedToken : aToken) {
      final String posTag = analyzedToken.getPOSTag();
      if (posTag != null) {
        final Matcher m = pattern.matcher(posTag);
        if (m.matches()) {
          matches = true;
          break;
        }
      }
    }
    return matches;
  }

}
