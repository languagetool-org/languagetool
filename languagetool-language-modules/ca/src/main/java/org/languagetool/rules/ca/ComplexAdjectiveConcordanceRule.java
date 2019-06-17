/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Jaume Ortolà  i Font
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
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.*;

/**
 * This rule checks if an adjective doesn't agree with the previous noun and at
 * the same time it doesn't agree with any of the previous words. Takes care of
 * some exceptions.
 * 
 * @author Jaume Ortolà i Font
 */
public class ComplexAdjectiveConcordanceRule extends Rule {

  /**
   * Patterns
   */

  private static final Pattern NOM = Pattern.compile("N.*");
  private static final Pattern NOM_MS = Pattern.compile("N.MS.*");
  private static final Pattern NOM_FS = Pattern.compile("N.FS.*");
  private static final Pattern NOM_MP = Pattern.compile("N.MP.*");
  private static final Pattern NOM_FP = Pattern.compile("N.FP.*");
  private static final Pattern NOM_CS = Pattern.compile("N.CS.*");
  private static final Pattern NOM_CP = Pattern.compile("N.CP.*");

  private static final Pattern NOM_DET = Pattern.compile("N.*|D[NDA0I].*");
  private static final Pattern _GN_ = Pattern.compile("_GN_.*");
  private static final Pattern _GN_MS = Pattern.compile("_GN_MS");
  private static final Pattern _GN_FS = Pattern.compile("_GN_FS");
  private static final Pattern _GN_MP = Pattern.compile("_GN_MP");
  private static final Pattern _GN_FP = Pattern.compile("_GN_FP");
  private static final Pattern _GN_CS = Pattern.compile("_GN_[MF]S");
  private static final Pattern _GN_CP = Pattern.compile("_GN_[MF]P");

  private static final Pattern DET = Pattern.compile("D[NDA0IP].*");
  private static final Pattern DET_CS = Pattern.compile("D[NDA0IP]0CS0");
  private static final Pattern DET_MS = Pattern.compile("D[NDA0IP]0MS0");
  private static final Pattern DET_FS = Pattern.compile("D[NDA0IP]0FS0");
  private static final Pattern DET_MP = Pattern.compile("D[NDA0IP]0MP0");
  private static final Pattern DET_FP = Pattern.compile("D[NDA0IP]0FP0");

  private static final Pattern GN_MS = Pattern.compile("N.[MC][SN].*|A..[MC][SN].*|V.P..SM.?|PX.MS.*|D[NDA0I]0MS0");
  private static final Pattern GN_FS = Pattern.compile("N.[FC][SN].*|A..[FC][SN].*|V.P..SF.?|PX.FS.*|D[NDA0I]0FS0");
  private static final Pattern GN_MP = Pattern.compile("N.[MC][PN].*|A..[MC][PN].*|V.P..PM.?|PX.MP.*|D[NDA0I]0MP0");
  private static final Pattern GN_FP = Pattern.compile("N.[FC][PN].*|A..[FC][PN].*|V.P..PF.?|PX.FP.*|D[NDA0I]0FP0");
  private static final Pattern GN_CP = Pattern.compile("N.[FMC][PN].*|A..[FMC][PN].*|D[NDA0I]0[FM]P0");
  private static final Pattern GN_CS = Pattern.compile("N.[FMC][SN].*|A..[FMC][SN].*|D[NDA0I]0[FM]S0");
  
  //private static final Pattern NOM_ADJ = Pattern.compile("N.*|A.*|V.P.*");

  private static final Pattern ADJECTIU = Pattern.compile("AQ.*|V.P.*|PX.*|.*LOC_ADJ.*");
  private static final Pattern ADJECTIU_MS = Pattern.compile("A..[MC][SN].*|V.P..SM.?|PX.MS.*");
  private static final Pattern ADJECTIU_FS = Pattern.compile("A..[FC][SN].*|V.P..SF.?|PX.FS.*");
  private static final Pattern ADJECTIU_MP = Pattern.compile("A..[MC][PN].*|V.P..PM.?|PX.MP.*");
  private static final Pattern ADJECTIU_FP = Pattern.compile("A..[FC][PN].*|V.P..PF.?|PX.FP.*");
  private static final Pattern ADJECTIU_CP = Pattern.compile("A..C[PN].*");
  private static final Pattern ADJECTIU_CS = Pattern.compile("A..C[SN].*");
  // private static final Pattern ADJECTIU_M =
  // Pattern.compile("A..[MC].*|V.P...M.?|PX.M.*");
  // private static final Pattern ADJECTIU_F =
  // Pattern.compile("A..[FC].*|V.P...F.?|PX.F.*");
  private static final Pattern ADJECTIU_S = Pattern.compile("A...[SN].*|V.P..S..?|PX..S.*");
  private static final Pattern ADJECTIU_P = Pattern.compile("A...[PN].*|V.P..P..?|PX..P.*");
  private static final Pattern ADVERBI = Pattern.compile("R.|.*LOC_ADV.*");
  private static final Pattern CONJUNCIO = Pattern.compile("C.|.*LOC_CONJ.*");
  private static final Pattern PUNTUACIO = Pattern.compile("_PUNCT.*");
  private static final Pattern LOC_ADV = Pattern.compile(".*LOC_ADV.*");
  private static final Pattern ADVERBIS_ACCEPTATS = Pattern.compile("RG_anteposat");
  private static final Pattern CONCORDA = Pattern.compile("_GN_.*|ignore_concordance|AQ.CN.");
  private static final Pattern UPPERCASE = Pattern.compile("\\p{Lu}[\\p{Ll}\u00B7]*");
  private static final Pattern COORDINACIO = Pattern.compile(",|i|o");
  private static final Pattern COORDINACIO_IONI = Pattern.compile("i|o|ni");
  private static final Pattern KEEP_COUNT = Pattern.compile("A.*|N.*|D[NAIDP].*|SPS.*|.*LOC_ADV.*|V.P.*|_PUNCT.*|.*LOC_ADJ.*|PX.*");
  private static final Pattern KEEP_COUNT2 = Pattern.compile(",|i|o|ni"); // |\\d+%?|%
  private static final Pattern STOP_COUNT = Pattern.compile(";");
  private static final Pattern PREPOSICIONS = Pattern.compile("SPS.*");
  private static final Pattern PREPOSICIO_CANVI_NIVELL = Pattern.compile("de|d'|en|sobre|a|entre|per|pe|amb|sense|contra");
  private static final Pattern VERB = Pattern.compile("V.[^P].*|_GV_");
  private static final Pattern GV = Pattern.compile("_GV_");
  private static final Pattern EXCEPCIONS_PARTICIPI = Pattern.compile("atès|atés|atesa|atesos|ateses|donat|donats|donada|donades");
  private static final Pattern EXCEPCIONS_PREVIA = Pattern.compile("termes?|paraul(a|es)|mots?|vocables?|expressi(ó|ons)|noms?|tipus|denominaci(ó|ons)");
  private static final Pattern EXCEPCIONS_PREVIA_POSTAG = Pattern.compile("_loc_meitat");
  private static final Pattern TOPONIM = Pattern.compile("NP..G..");
  private static final Pattern ORDINAL = Pattern.compile("AO.*");

  boolean adverbAppeared = false;
  boolean conjunctionAppeared = false;
  boolean punctuationAppeared = false;

  public ComplexAdjectiveConcordanceRule(ResourceBundle messages)
      throws IOException {
    super.setCategory(new Category(new CategoryId("CONCORDANCES_GRUPS_NOMINALS"), "Z) Concordances en grups nominals"));
    setLocQualityIssueType(ITSIssueType.Grammar);
    addExamplePair(Example.wrong("Anàlisis <marker>clínic</marker>."),
                   Example.fixed("Anàlisis <marker>clíniques</marker>."));
    addExamplePair(Example.wrong("Tinc dues taules <marker>verds</marker>."),
                   Example.fixed("Tinc dues taules <marker>verdes</marker>."));
  }

  @Override
  public String getId() {
    return "CONCORDANCES_ADJECTIU_POSPOSAT";
  }

  @Override
  public String getDescription() {
    return "Comprova si un adjectiu concorda amb els noms previs.";
  }

  @Override
  public RuleMatch[] match(final AnalyzedSentence sentence) {
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    final AnalyzedTokenReadings[] tokens = sentence
        .getTokensWithoutWhitespace();
    // ignoring token 0, i.e. SENT_START
    goToNextToken: for (int i = 1; i < tokens.length; i++) {
      if (matchPostagRegexp(tokens[i], ADJECTIU)
          && !matchPostagRegexp(tokens[i], CONCORDA)
          && !matchPostagRegexp(tokens[i], GV)) {
        final String token = tokens[i].getToken();
        final String prevToken = tokens[i - 1].getToken();
        String prevPrevToken = "";
        if (i > 2) {
          prevPrevToken = tokens[i - 2].getToken();
        }
        String nextToken = "";
        if (i < tokens.length - 1) {
          nextToken = tokens[i + 1].getToken();
        }
        int j;
        // boolean adjectiveAgrees = false;
        // boolean theRuleMaches = false;
        // boolean isException = false;
        boolean isPlural = true;
        boolean isPrevNoun = false;
        Pattern substPattern = null;
        Pattern gnPattern = null;
        Pattern adjPattern = null;
        Matcher isUpperCase = UPPERCASE.matcher(token);

        /* SOME EXCEPTIONS */
        // per molt lleuger
        if (prevPrevToken.equals("per") && prevToken.equals("molt")) {
          continue goToNextToken;
        }
        // esquerra-dreta
        if (i < tokens.length - 2 && token.equalsIgnoreCase("esquerra")
            && (nextToken.equals("-") || nextToken.equals("/"))
            && tokens[i + 2].getToken().equalsIgnoreCase("dreta")) {
          continue goToNextToken;
        }
        // exception: noun (or adj) plural + two or more adjectives
        if (i < tokens.length - 2) {
          Matcher pCoordina = COORDINACIO.matcher(nextToken);
          if (pCoordina.matches()) {
            if (((matchPostagRegexp(tokens[i - 1], NOM_MP) || matchPostagRegexp(
                tokens[i - 1], ADJECTIU_MP))
                && matchPostagRegexp(tokens[i], ADJECTIU_MS) && matchPostagRegexp(
                  tokens[i + 2], ADJECTIU_MS))
                || ((matchPostagRegexp(tokens[i - 1], NOM_MP) || matchPostagRegexp(
                    tokens[i - 1], ADJECTIU_MP))
                    && matchPostagRegexp(tokens[i], ADJECTIU_MP) && matchPostagRegexp(
                      tokens[i + 2], ADJECTIU_MP))
                || ((matchPostagRegexp(tokens[i - 1], NOM_FP) || matchPostagRegexp(
                    tokens[i - 1], ADJECTIU_FP))
                    && matchPostagRegexp(tokens[i], ADJECTIU_FS) && matchPostagRegexp(
                      tokens[i + 2], ADJECTIU_FS))
                || ((matchPostagRegexp(tokens[i - 1], NOM_FP) || matchPostagRegexp(
                    tokens[i - 1], ADJECTIU_FP))
                    && matchPostagRegexp(tokens[i], ADJECTIU_FP) && matchPostagRegexp(
                      tokens[i + 2], ADJECTIU_FP))) {
              continue goToNextToken;
            }
          }
        }
        // exception: termes, paraules, etc.
        if (matchRegexp(prevToken, EXCEPCIONS_PREVIA)) {
          continue goToNextToken;
        }
        // exceptions: la meitat mascles
        if (matchPostagRegexp(tokens[i - 1], EXCEPCIONS_PREVIA_POSTAG)) {
          continue goToNextToken;
        }
        // exceptions: llevat de, tret de, majúsucula inicial
        if ((((token.equals("tret") || token.equals("llevat")) && (nextToken
            .equals("de") || nextToken.equals("que")))
            || token.equals("primer")
            || token.equals("junts")
            || token.equals("plegats") || isUpperCase.matches())) {
          continue goToNextToken;
        }
        // exceptions: atès, atesos..., donat, donats...
        if (matchRegexp(token, EXCEPCIONS_PARTICIPI)) {
          continue goToNextToken;
        }
        // exceptions: un cop, una volta, una vegada...
        if ((((prevPrevToken.equals("un") || prevPrevToken.equals("altre")) && (prevToken.equals("cop") || prevToken.equals("colp"))) 
            || ((prevPrevToken.equals("una") || prevPrevToken.equals("altra") || prevPrevToken.equals("aquesta")) && (prevToken.equals("volta") || prevToken.equals("vegada"))))
            ) {
          continue goToNextToken;
        }
        // exceptions: per primera vegada...
        if (i > 2 && tokens[i - 2].getToken().equalsIgnoreCase("per")
            && matchPostagRegexp(tokens[i - 1], ORDINAL)
            && (prevToken.equals("volta") || prevToken.equals("vegada")
                || prevToken.equals("cop") || prevToken.equals("colp"))) {
          continue goToNextToken;
        }
        // exceptions: segur que, just a
        if (i < tokens.length - 1) {
          if ((token.equals("segur") || token.equals("major") || token
              .equals("menor")) && nextToken.equals("que")) {
            continue goToNextToken;
          }
        }
        // exceptions: sota mateix, dins mateix,
        if (token.equals("mateix") && matchPostagRegexp(tokens[i - 1], ADVERBI)) {
          continue goToNextToken;
        }
        // exceptions: a França mateix
        if (token.equals("mateix") && matchPostagRegexp(tokens[i - 1], TOPONIM)
            && prevPrevToken.equals("a")) {
          continue goToNextToken;
        }

        /* Count all nouns and determiners before the adjectives */
        // Takes care of acceptable combinations.
        int maxLevels = 4;
        int[] cNt = new int[maxLevels];
        int[] cNMS = new int[maxLevels];
        int[] cNFS = new int[maxLevels];
        int[] cNMP = new int[maxLevels];
        int[] cNFP = new int[maxLevels];
        int[] cNCS = new int[maxLevels];
        int[] cNCP = new int[maxLevels];
        int[] cDMS = new int[maxLevels];
        int[] cDFS = new int[maxLevels];
        int[] cDMP = new int[maxLevels];
        int[] cDFP = new int[maxLevels];
        int[] cN = new int[maxLevels];
        int[] cD = new int[maxLevels];
        int level = 0;
        j = 1;
        initializeApparitions();
        while (i - j > 0 && keepCounting(tokens[i - j]) && level < maxLevels) {
          if (!isPrevNoun) {
            if (matchPostagRegexp(tokens[i - j], NOM) || (
            // adjectiu o participi sense nom, però amb algun determinant davant
                i - j - 1 > 0 && !matchPostagRegexp(tokens[i - j], NOM)
                    && matchPostagRegexp(tokens[i - j], ADJECTIU) 
                    && matchPostagRegexp(tokens[i - j - 1], DET))) {
              if (matchPostagRegexp(tokens[i - j], _GN_MS)) {
                cNMS[level]++;
              }
              if (matchPostagRegexp(tokens[i - j], _GN_FS)) {
                cNFS[level]++;
              }
              if (matchPostagRegexp(tokens[i - j], _GN_MP)) {
                cNMP[level]++;
              }
              if (matchPostagRegexp(tokens[i - j], _GN_FP)) {
                cNFP[level]++;
              }
            }
            if (!matchPostagRegexp(tokens[i - j], _GN_)) {
              if (matchPostagRegexp(tokens[i - j], NOM_MS)) {
                cNMS[level]++;
              } else if (matchPostagRegexp(tokens[i - j], NOM_FS)) {
                cNFS[level]++;
              } else if (matchPostagRegexp(tokens[i - j], NOM_MP)) {
                cNMP[level]++;
              } else if (matchPostagRegexp(tokens[i - j], NOM_FP)) {
                cNFP[level]++;
              } else if (matchPostagRegexp(tokens[i - j], NOM_CS)) {
                cNCS[level]++;
              } else if (matchPostagRegexp(tokens[i - j], NOM_CP)) {
                cNCP[level]++;
              }
            }
          }
          // avoid two consecutive nouns
          if (matchPostagRegexp(tokens[i - j], NOM)) {
            cNt[level]++;
            isPrevNoun = true;
            //initializeApparitions();
          } else {
            isPrevNoun = false;
          }

          if (matchPostagRegexp(tokens[i - j], DET_CS)) {
            if (matchPostagRegexp(tokens[i - j + 1], NOM_MS)) {
              cDMS[level]++;
            }
            if (matchPostagRegexp(tokens[i - j + 1], NOM_FS)) {
              cDFS[level]++;
            }
          }
          if (!matchPostagRegexp(tokens[i - j], ADVERBI)) {
            if (matchPostagRegexp(tokens[i - j], DET_MS)) {
              cDMS[level]++;
            }
            if (matchPostagRegexp(tokens[i - j], DET_FS)) {
              cDFS[level]++;
            }
            if (matchPostagRegexp(tokens[i - j], DET_MP)) {
              cDMP[level]++;
            }
            if (matchPostagRegexp(tokens[i - j], DET_FP)) {
              cDFP[level]++;
            }
          }
          if (i - j > 0) {
            if (matchRegexp(tokens[i - j].getToken(), PREPOSICIO_CANVI_NIVELL)
                && !matchRegexp(tokens[i - j - 1].getToken(), COORDINACIO_IONI)
                && !matchPostagRegexp(tokens[i - j + 1], ADVERBI)) {
              level++;
            }
          }
          if (level > 0
              && matchRegexp(tokens[i - j].getToken(), COORDINACIO_IONI)) {
            int k = 1;
            while (k < 4
                && i - j - k > 0
                && (matchPostagRegexp(tokens[i - j - k], KEEP_COUNT)
                    || matchRegexp(tokens[i - j - k].getToken(), KEEP_COUNT2) || matchPostagRegexp(
                      tokens[i - j - k], ADVERBIS_ACCEPTATS))
                && (!matchRegexp(tokens[i - j - k].getToken(), STOP_COUNT))) {
              if (matchPostagRegexp(tokens[i - j - k], PREPOSICIONS)) {
                j = j + k;
                break;
              }
              k++;
            }
          }
          updateApparitions(tokens[i - j]);
          j++;
        }
        level++;
        if (level > maxLevels) {
          level = maxLevels;
        }
        j = 0;
        int cNtotal = 0;
        int cDtotal = 0;
        while (j < level) {
          cN[j] = cNMS[j] + cNFS[j] + cNMP[j] + cNFP[j] + cNCS[j] + cNCP[j];
          cD[j] = cDMS[j] + cDFS[j] + cDMP[j] + cDFP[j];
          cNtotal += cN[j];
          cDtotal += cD[j];

          // exceptions: adjective is plural and there are several nouns before
          if (matchPostagRegexp(tokens[i], ADJECTIU_MP)
              && (cN[j] > 1 || cD[j] > 1)
              && (cNMS[j] + cNMP[j] + cNCS[j] + cNCP[j] + cDMS[j] + cDMP[j]) > 0
              && (cNFS[j] + cNFP[j] <= cNt[j])) {
            continue goToNextToken;
          }
          if (matchPostagRegexp(tokens[i], ADJECTIU_FP)
              && (cN[j] > 1 || cD[j] > 1)
              && ((cNMS[j] + cNMP[j] + cDMS[j] + cDMP[j]) == 0 || (cNt[j] > 0 && cNFS[j]
                  + cNFP[j] >= cNt[j]))) {
            continue goToNextToken;
          }
          // Adjective can't be singular
          if (cN[j] + cD[j] > 0) { // && level>1
            isPlural = isPlural && cD[j] > 1; // cN[j]>1
          }
          j++;
        }
        // there is no noun, (no determinant --> && cDtotal==0)
        if (cNtotal == 0 && cDtotal == 0) { 
          continue goToNextToken;
        }        

        // patterns according to the analyzed adjective 
        if (matchPostagRegexp(tokens[i], ADJECTIU_CS)) {
          substPattern = GN_CS;
          adjPattern = ADJECTIU_S;
          gnPattern = _GN_CS;
        } else if (matchPostagRegexp(tokens[i], ADJECTIU_CP)) {
          substPattern = GN_CP;
          adjPattern = ADJECTIU_P;
          gnPattern = _GN_CP;
        } else if (matchPostagRegexp(tokens[i], ADJECTIU_MS)) {
          substPattern = GN_MS;
          adjPattern = ADJECTIU_MS;
          gnPattern = _GN_MS;
        } else if (matchPostagRegexp(tokens[i], ADJECTIU_FS)) {
          substPattern = GN_FS;
          adjPattern = ADJECTIU_FS;
          gnPattern = _GN_FS;
        } else if (matchPostagRegexp(tokens[i], ADJECTIU_MP)) {
          substPattern = GN_MP;
          adjPattern = ADJECTIU_MP;
          gnPattern = _GN_MP;
        } else if (matchPostagRegexp(tokens[i], ADJECTIU_FP)) {
          substPattern = GN_FP;
          adjPattern = ADJECTIU_FP;
          gnPattern = _GN_FP;
        }

        if (substPattern == null || gnPattern == null || adjPattern == null) {
          continue goToNextToken;
        }

        // combinations Det/Nom + adv (1,2..) + adj. 
        // If there is agreement, the rule doesn't match
        j = 1;
        boolean keepCount = true;
        while (i - j > 0 && keepCount) {
          if (matchPostagRegexp(tokens[i - j], NOM_DET)
              && matchPostagRegexp(tokens[i - j], gnPattern)) {
            continue goToNextToken; // there is a previous agreeing noun
          } else if (!matchPostagRegexp(tokens[i - j], _GN_)
              && matchPostagRegexp(tokens[i - j], substPattern)) {
            continue goToNextToken; // there is a previous agreeing noun
          }
          keepCount = !matchPostagRegexp(tokens[i - j], NOM_DET);
          j++;
        }

        // Necessary condition: previous token is a non-agreeing noun
        // or it is adjective or adverb (not preceded by verb)
        // /*&& !matchPostagRegexp(tokens[i],NOM)*/
        if ((matchPostagRegexp(tokens[i - 1], NOM) && !matchPostagRegexp(tokens[i - 1], substPattern))
            || (matchPostagRegexp(tokens[i - 1], ADJECTIU) && !matchPostagRegexp(tokens[i - 1], gnPattern))
            || (matchPostagRegexp(tokens[i - 1], ADJECTIU) && !matchPostagRegexp(tokens[i - 1], adjPattern))
            || (i>2 && matchPostagRegexp(tokens[i - 1], ADVERBIS_ACCEPTATS) && !matchPostagRegexp(tokens[i - 2], VERB) && !matchPostagRegexp(tokens[i - 2], PREPOSICIONS))
            || (i>3 && matchPostagRegexp(tokens[i - 1], LOC_ADV) && matchPostagRegexp(tokens[i - 2], LOC_ADV) && !matchPostagRegexp(tokens[i - 3], VERB) && !matchPostagRegexp(tokens[i - 3], PREPOSICIONS))
            ) {
          
        } else {
          continue goToNextToken;
        }        

        // Adjective can't be singular. The rule matches
        if (!(isPlural && matchPostagRegexp(tokens[i], ADJECTIU_S))) {
          //look into previous words
          j = 1;
          initializeApparitions();
          while (i - j > 0 && keepCounting(tokens[i - j])) {
            // there is a previous agreeing noun
            if (!matchPostagRegexp(tokens[i - j], _GN_)
                && matchPostagRegexp(tokens[i - j], NOM_DET)
                && matchPostagRegexp(tokens[i - j], substPattern)) {
              continue goToNextToken; 
            // there is a previous agreeing adjective (in a nominal group) 
            } else if ( matchPostagRegexp(tokens[i - j], gnPattern)) {
              continue goToNextToken;
            // if there is no nominal group, it requires noun
            } /*else if (!matchPostagRegexp(tokens[i - j], _GN_) 
                && matchPostagRegexp(tokens[i - j], substPattern)) {
              continue goToNextToken; // there is a previous agreeing noun
            }*/
            updateApparitions(tokens[i - j]);
            j++;
          }
        }

        // The rule matches
        final String msg = "Reviseu la concordança de la paraula \u00AB"
            + token + "\u00BB.";
        final RuleMatch ruleMatch = new RuleMatch(this, sentence,
            tokens[i].getStartPos(), tokens[i].getEndPos(),
            msg, "Reviseu la concordança.");
        ruleMatches.add(ruleMatch);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private boolean keepCounting(AnalyzedTokenReadings aTr) {
    // stop searching if there is some of these combinations:
    // adverb+comma, adverb+conjunction, comma+conjunction,
    // punctuation+punctuation
    if ((adverbAppeared && conjunctionAppeared)
        || (adverbAppeared && punctuationAppeared)
        || (conjunctionAppeared && punctuationAppeared)
        || (punctuationAppeared && matchPostagRegexp(aTr, PUNTUACIO))) {
      return false;
    }
    return (matchPostagRegexp(aTr, KEEP_COUNT)
        || matchRegexp(aTr.getToken(), KEEP_COUNT2) || matchPostagRegexp(aTr,
          ADVERBIS_ACCEPTATS))
        && !matchRegexp(aTr.getToken(), STOP_COUNT) 
        && (!matchPostagRegexp(aTr, GV) || matchPostagRegexp(aTr, _GN_));
  }

  private void initializeApparitions() {
    adverbAppeared = false;
    conjunctionAppeared = false;
    punctuationAppeared = false;
  }

  private void updateApparitions(AnalyzedTokenReadings aTr) {
    if (matchPostagRegexp(aTr, NOM) || matchPostagRegexp(aTr, ADJECTIU)) {
      initializeApparitions();
      return;
    }
    adverbAppeared |= matchPostagRegexp(aTr, ADVERBI);
    conjunctionAppeared |= matchPostagRegexp(aTr, CONJUNCIO);
    punctuationAppeared |= matchPostagRegexp(aTr, PUNTUACIO);
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

  /**
   * Match String with regular expression
   */
  private boolean matchRegexp(String s, Pattern pattern) {
    final Matcher m = pattern.matcher(s);
    return m.matches();
  }

}
