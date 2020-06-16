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
package org.languagetool.rules.pt;

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
import org.languagetool.rules.*;
import org.languagetool.tools.StringTools;

/**
 * This rule checks if a word without graphical accent and with a verb POS tag
 * should be a noun or an adjective with graphical accent. It uses two lists of
 * word pairs: verb-noun and verb-adjective.
 * 
 * @author Jaume Ortolà i Font
 * l18n by Tiago F. Santos
 * TODO Verify all exceptions that apply to Portuguese
 * FIXME Convert all chunking tags to the ones used in Portuguese
 */
public class PortugueseAccentuationCheckRule extends Rule {

  /**
   * Patterns
   */
  private static final Pattern PREPOSICAO_DE = Pattern.compile("de|d[a|o]s?");
  private static final Pattern ARTIGO_O_MS = Pattern.compile("o|O");
  private static final Pattern ARTIGO_O_FS = Pattern.compile("a|A");
  private static final Pattern ARTIGO_O_MP = Pattern.compile("as|As");
  private static final Pattern ARTIGO_O_FP = Pattern.compile("os|Os");
  private static final Pattern DETERMINANTE = Pattern.compile("D[^R].*");
  private static final Pattern DETERMINANTE_MS = Pattern.compile("D[^R].[MC][SN].*");
  private static final Pattern DETERMINANTE_FS = Pattern.compile("D[^R].[FC][SN].*");
  private static final Pattern DETERMINANTE_MP = Pattern.compile("D[^R].[MC][PN].*");
  private static final Pattern DETERMINANTE_FP = Pattern.compile("D[^R].[FC][PN].*");
  private static final Pattern NOME_MS = Pattern.compile("NC[MC][SN].*");
  private static final Pattern NOME_FS = Pattern.compile("NC[FC][SN].*");
  private static final Pattern NOME_MP = Pattern.compile("NC[MC][PN].*");
  private static final Pattern NOME_FP = Pattern.compile("NC[FC][PN].*");
  private static final Pattern ADJETIVO_MS = Pattern.compile("A..[MC][SN].*|V.P..SM.?|PX.MS.*");
  private static final Pattern ADJETIVO_FS = Pattern.compile("A..[FC][SN].*|V.P..SF.?|PX.FS.*");
  private static final Pattern ADJETIVO_MP = Pattern.compile("A..[MC][PN].*|V.P..PM.?|PX.MP.*");
  private static final Pattern ADJETIVO_FP = Pattern.compile("A..[FC][PN].*|V.P..PF.?|PX.FP.*");
  private static final Pattern INFINITIVO = Pattern.compile("V.N.*");
  private static final Pattern VERBO_CONJUGADO = Pattern.compile("V.[^NGP].*|_GV_");
  private static final Pattern PARTICIPIO_MS = Pattern.compile("V.P.*SM.?");
  private static final Pattern GRUPO_VERBAL = Pattern.compile("_GV_");
  private static final Pattern VERBO_3S = Pattern.compile("V...3S..?");
  private static final Pattern NOT_IN_PREV_TOKEN = Pattern.compile("V..*|PP.*|P0.*|V.P.*");
  private static final Pattern BEFORE_ADJECTIVE_MS = Pattern.compile("SPS00|D[^R].[MC][SN].*|V.[^NGP].*|PX.*");
  private static final Pattern BEFORE_ADJECTIVE_FS = Pattern.compile("SPS00|D[^R].[FC][SN].*|V.[^NGP].*|PX.*");
  private static final Pattern BEFORE_ADJECTIVE_MP = Pattern.compile("SPS00|D[^R].[MC][PN].*|V.[^NGP].*|PX.*");
  private static final Pattern BEFORE_ADJECTIVE_FP = Pattern.compile("SPS00|D[^R].[FC][PN].*|V.[^NGP].*|PX.*");
  private static final Pattern GN = Pattern.compile(".*_GN_.*|<?/?N[CP].*");
  private static final Pattern EXCEPCOES_ANTES_DE = Pattern.compile("forma|manera|por|costat", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
/*  private static final Pattern LOCOCOES = Pattern.compile(".*LOC.*");*/
  private static final Pattern PRONOME_PESSOAL = Pattern.compile("P0.{6}|PP3CN000|PP3NN000|PP3CP000|PP3CSD00"); // TODO Confirmar a exclusão de: PP3..A00 (coincidee COM articles determinats) se aplica ao português

  private static final Map<String, AnalyzedTokenReadings> relevantWords = 
          new PortugueseAccentuationDataLoader().loadWords("/pt/verbos_sem_acento_nomes_com_acento.txt");
  private static final Map<String, AnalyzedTokenReadings> relevantWords2 =
          new PortugueseAccentuationDataLoader().loadWords("/pt/verbos_sem_acento_adj_com_acento.txt");

  public PortugueseAccentuationCheckRule(ResourceBundle messages) throws IOException {
    super.setCategory(Categories.CONFUSED_WORDS.getCategory(messages));
    setDefaultOff(); // FIXME This rule is a basic adaptation that has no exceptions added. Users may test the rule and give the required feedback so that the rule can be on by default
    setLocQualityIssueType(ITSIssueType.Misspelling);
  }

  @Override
  public String getId() {
    return "ACCENTUATION_CHECK_PT";
  }

  @Override
  public String getDescription() {
    return "Confus\u00E3o com acentos gr\u00E1ficos";
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

      // verbo precedido de pronome reflexo
      if (matchPostagRegexp(tokens[i - 1], PRONOME_PESSOAL)
          && !prevToken.startsWith("-")) {
        continue;
      }
      
      
      String replacement = null;
      final Matcher mPreposicaoDE = PREPOSICAO_DE.matcher(nextToken);
      final Matcher mExcepcoesDE = EXCEPCOES_ANTES_DE.matcher(nextNextToken);
      final Matcher mArtigoOMS = ARTIGO_O_MS.matcher(prevToken);
      final Matcher mArtigoOFS = ARTIGO_O_FS.matcher(prevToken);
      final Matcher mArtigoOMP = ARTIGO_O_MP.matcher(prevToken);
      final Matcher mArtigoOFP = ARTIGO_O_FP.matcher(prevToken);

      // VERB WITHOUT ACCENT -> NOUN WITH ACCENT
      if (isRelevantWord && !matchPostagRegexp(tokens[i], GN)/* && !matchPostagRegexp(tokens[i], LOCUCOES)*/) {
        // amb renuncies
        if (tokens[i - 1].hasPosTag("SPS00") && !tokens[i - 1].hasPosTag("RG")
            && !matchPostagRegexp(tokens[i - 1], DETERMINANTE)
            && !matchPostagRegexp(tokens[i], INFINITIVO)) {
          replacement = relevantWords.get(token).getToken();
        }
        // aquestes renuncies
        else if (((matchPostagRegexp(tokens[i - 1], DETERMINANTE_MS) && matchPostagRegexp(relevantWords.get(token), NOME_MS) /*
              && !token.equals("cantar")*/) 
            || (matchPostagRegexp(tokens[i - 1], DETERMINANTE_MP) && matchPostagRegexp(relevantWords.get(token), NOME_MP))
            || (matchPostagRegexp(tokens[i - 1], DETERMINANTE_FS) && matchPostagRegexp(relevantWords.get(token), NOME_FS) /*
                && !token.equals("venia") && !token.equals("tenia") && !token.equals("continua") && !token.equals("genera") 
                && !token.equals("faria")*/) 
            || (matchPostagRegexp(tokens[i - 1], DETERMINANTE_FP) && matchPostagRegexp(relevantWords.get(token), NOME_FP)))) {
          replacement = relevantWords.get(token).getToken();
        }
        // fumaré una faria (correct: fària)
        else if (i > 2
            && matchPostagRegexp(tokens[i - 2], VERBO_CONJUGADO)
            && ((matchPostagRegexp(tokens[i - 1], DETERMINANTE_MS) && matchPostagRegexp(relevantWords.get(token), NOME_MS))
                || (matchPostagRegexp(tokens[i - 1], DETERMINANTE_MP) && matchPostagRegexp(relevantWords.get(token), NOME_MP))
                || (matchPostagRegexp(tokens[i - 1], DETERMINANTE_FS) && matchPostagRegexp(relevantWords.get(token), NOME_FS)) 
                || (matchPostagRegexp(tokens[i - 1], DETERMINANTE_FP) && matchPostagRegexp(relevantWords.get(token), NOME_FP)))) {
          replacement = relevantWords.get(token).getToken();
        }
        // fem la copia (correct: còpia)
        else if (i > 2
            && matchPostagRegexp(tokens[i - 2], VERBO_CONJUGADO)
            && ((mArtigoOMS.matches() && matchPostagRegexp(relevantWords.get(token), NOME_MS))
                || (mArtigoOMP.matches() && matchPostagRegexp(relevantWords.get(token), NOME_MP))
                || (mArtigoOFS.matches() && matchPostagRegexp(relevantWords.get(token), NOME_FS)) 
                || (mArtigoOFP.matches() && matchPostagRegexp(relevantWords.get(token),NOME_FP)))) {
          replacement = relevantWords.get(token).getToken();
        }
        // circumstancies d'una altra classe
        else if (!matchPostagRegexp(tokens[i], PARTICIPIO_MS) /*
            && !token.equals("venia") && !token.equals("venies")
            && !token.equals("tenia") && !token.equals("tenies")
            && !token.equals("faria") && !token.equals("faries")
            && !token.equals("espero") && !token.equals("continua")
            && !token.equals("continues") && !token.equals("cantar")
            && !prevToken.equals("que") && !prevToken.equals("qui")
            && !prevToken.equals("què") && mPreposicaoDE.matches() */
            && !matchPostagRegexp(tokens[i - 1], NOT_IN_PREV_TOKEN)
            /* && !matchPostagRegexp(tokens[i + 1], LOCUCOES) */
            && (i < tokens.length - 2)
            && !matchPostagRegexp(tokens[i + 2], INFINITIVO)
            && !mExcepcoesDE.matches() 
            && !tokens[i - 1].hasPosTag("RG")) {
          replacement = relevantWords.get(token).getToken();
        }
        // la renuncia del president.
        else if (/* !token.equals("venia")
            && !token.equals("venies") && !token.equals("tenia")
            && !token.equals("tenies") && !token.equals("faria")
            && !token.equals("faries") && !token.equals("continua")
            && !token.equals("continues") && !token.equals("cantar")
            && !token.equals("diferencia") && !token.equals("diferencies")
            && !token.equals("distancia")  && !token.equals("distancies") 
            && */ ((mArtigoOMS.matches() && matchPostagRegexp(
                relevantWords.get(token), NOME_MS))
                || (mArtigoOFS.matches() && matchPostagRegexp(
                    relevantWords.get(token), NOME_FS))
                || (mArtigoOMP.matches() && matchPostagRegexp(
                    relevantWords.get(token), NOME_MP)) 
                || (mArtigoOFP.matches() && matchPostagRegexp(
                    relevantWords.get(token), NOME_FP)))

            && mPreposicaoDE.matches()) {
          replacement = relevantWords.get(token).getToken();
        }
        // circunstancias extraordináries
        else if (/*!token.equals("pronuncia") 
            && !token.equals("espero") && !token.equals("pronuncies")
            && !token.equals("venia")  && !token.equals("venies") 
            && !token.equals("tenia")  && !token.equals("tenies") 
            && !token.equals("continua") && !token.equals("continues")
            && !token.equals("faria") && !token.equals("faries") 
            && !token.equals("genera") && !token.equals("figuri")
            && */ (i < tokens.length - 1)
            && ((matchPostagRegexp(relevantWords.get(token), NOME_MS) && matchPostagRegexp(tokens[i + 1], ADJETIVO_MS))
                || (matchPostagRegexp(relevantWords.get(token), NOME_FS) && matchPostagRegexp(tokens[i + 1], ADJETIVO_FS))
                || (matchPostagRegexp(relevantWords.get(token), NOME_MP) && matchPostagRegexp(tokens[i + 1], ADJETIVO_MP)) 
                || (matchPostagRegexp(relevantWords.get(token), NOME_FP) && matchPostagRegexp(tokens[i + 1], ADJETIVO_FP)))) {
          replacement = relevantWords.get(token).getToken();
        }
        // les seves contraries
        else if ((matchPostagRegexp(relevantWords.get(token), NOME_MS) && matchPostagRegexp(tokens[i - 1], ADJETIVO_MS)
              && !matchPostagRegexp(tokens[i], VERBO_3S) && !matchPostagRegexp(tokens[i], GRUPO_VERBAL))
            || (matchPostagRegexp(relevantWords.get(token), NOME_FS) && matchPostagRegexp(tokens[i - 1], ADJETIVO_FS) 
              && !matchPostagRegexp(tokens[i], VERBO_3S))
            || (matchPostagRegexp(relevantWords.get(token), NOME_MP) && matchPostagRegexp(tokens[i - 1], ADJETIVO_MP))
            || (matchPostagRegexp(relevantWords.get(token), NOME_FP) && matchPostagRegexp(tokens[i - 1], ADJETIVO_FP))) {
          replacement = relevantWords.get(token).getToken();
        }
        //uma nova formula que (fórmula)
        else if (nextToken.equals("que") && i>2
            && ((matchPostagRegexp(relevantWords.get(token), NOME_MS) && matchPostagRegexp(tokens[i - 1], ADJETIVO_MS)
                && matchPostagRegexp(tokens[i - 2], DETERMINANTE_MS))
            || (matchPostagRegexp(relevantWords.get(token), NOME_FS) && matchPostagRegexp(tokens[i - 1], ADJETIVO_FS)
                && matchPostagRegexp(tokens[i - 2], DETERMINANTE_FS))
            || (matchPostagRegexp(relevantWords.get(token), NOME_MP) && matchPostagRegexp(tokens[i - 1], ADJETIVO_MP)
                && matchPostagRegexp(tokens[i - 2], DETERMINANTE_MP))
            || (matchPostagRegexp(relevantWords.get(token), NOME_FP) && matchPostagRegexp(tokens[i - 1], ADJETIVO_FP)
                && matchPostagRegexp(tokens[i - 2], DETERMINANTE_FP)))) {
          replacement = relevantWords.get(token).getToken();
        }
        // les circumstancies que ens envolten
        else if (nextToken.equals("que")
            && ((mArtigoOMS.matches() && matchPostagRegexp(relevantWords.get(token), NOME_MS))
                || (mArtigoOFS.matches() && matchPostagRegexp(relevantWords.get(token), NOME_FS))
                || (mArtigoOMP.matches() && matchPostagRegexp(relevantWords.get(token), NOME_MP)) 
                || (mArtigoOFP.matches() && matchPostagRegexp(relevantWords.get(token), NOME_FP)))) {
          replacement = relevantWords.get(token).getToken();
        }
        // de positiva influencia
        if (/*!token.equals("pronuncia") && !token.equals("espero") && !token.equals("pronuncies")
                && !token.equals("venia") && !token.equals("venies") && !token.equals("tenia")
                && !token.equals("tenies") && !token.equals("continua") && !token.equals("continues")
                && !token.equals("faria") && !token.equals("faries") && !token.equals("genera")
                && !token.equals("figuri") 
            && */ i>2 
            && tokens[i - 2].hasPosTag("SPS00") && !tokens[i - 2].hasPosTag("RG")           
            && ((matchPostagRegexp(relevantWords.get(token), NOME_MS) && matchPostagRegexp(tokens[i - 1], ADJETIVO_MS))
                || (matchPostagRegexp(relevantWords.get(token), NOME_FS) && matchPostagRegexp(tokens[i - 1], ADJETIVO_FS))
                || (matchPostagRegexp(relevantWords.get(token), NOME_MP) && matchPostagRegexp(tokens[i - 1], ADJETIVO_MP)) 
                || (matchPostagRegexp(relevantWords.get(token), NOME_FP) && matchPostagRegexp(tokens[i - 1], ADJETIVO_FP)))) {
          replacement = relevantWords.get(token).getToken();
        }
      }

      // VERB WITHOUT ACCENT -> ADJECTIVE WITH ACCENT
      if (isRelevantWord2 && !matchPostagRegexp(tokens[i], GN)/* && !matchPostagRegexp(tokens[i], LOCUCOES) */ ) {
        // de maneira obvia, circumstancias extraordinarias.
        if ((matchPostagRegexp(relevantWords2.get(token), ADJETIVO_MS) && matchPostagRegexp(tokens[i - 1], NOME_MS)
              && !tokens[i - 1].hasPosTag("_GN_FS") && matchPostagRegexp(tokens[i], VERBO_CONJUGADO) 
              && !matchPostagRegexp(tokens[i], VERBO_3S))
            || (matchPostagRegexp(relevantWords2.get(token), ADJETIVO_FS) && prevPrevToken.equalsIgnoreCase("de") 
                && (prevToken.equals("maneira") || prevToken.equals("forma")))
            || (matchPostagRegexp(relevantWords2.get(token), ADJETIVO_MP) && matchPostagRegexp(tokens[i - 1], NOME_MP))
            || (matchPostagRegexp(relevantWords2.get(token), ADJETIVO_FP) && matchPostagRegexp(tokens[i - 1], NOME_FP))) {
          replacement = relevantWords2.get(token).getToken();
        }
        // de continua disputa
        else if ((i < tokens.length - 1)
            && !prevToken.equals("que")
            && !matchPostagRegexp(tokens[i - 1], NOT_IN_PREV_TOKEN)
            && ((matchPostagRegexp(relevantWords2.get(token), ADJETIVO_MS) && matchPostagRegexp(tokens[i + 1], NOME_MS) 
                && matchPostagRegexp(tokens[i - 1], BEFORE_ADJECTIVE_MS))
            || (matchPostagRegexp(relevantWords2.get(token), ADJETIVO_FS) && matchPostagRegexp(tokens[i + 1], NOME_FS) 
                && matchPostagRegexp(tokens[i - 1], BEFORE_ADJECTIVE_FS))
            || (matchPostagRegexp(relevantWords2.get(token), ADJETIVO_MP) && matchPostagRegexp(tokens[i + 1], NOME_MP) 
                && matchPostagRegexp(tokens[i - 1], BEFORE_ADJECTIVE_MP)) 
            || (matchPostagRegexp(relevantWords2.get(token), ADJETIVO_FP) && matchPostagRegexp(tokens[i + 1], NOME_FP) 
                && matchPostagRegexp(tokens[i - 1], BEFORE_ADJECTIVE_FP)))) {
          replacement = relevantWords2.get(token).getToken();
        }
        // a magnifica conservação
        else if ((i < tokens.length - 1)
            && ((matchPostagRegexp(relevantWords2.get(token), ADJETIVO_MS)
                && matchPostagRegexp(tokens[i + 1], NOME_MS) && mArtigoOMS.matches())
            || (matchPostagRegexp(relevantWords2.get(token), ADJETIVO_FS)
                && matchPostagRegexp(tokens[i + 1], NOME_FS) && mArtigoOFS.matches())
            || (matchPostagRegexp(relevantWords2.get(token), ADJETIVO_MP)
                && matchPostagRegexp(tokens[i + 1], NOME_MP) && mArtigoOMP.matches()) 
            || (matchPostagRegexp(relevantWords2.get(token), ADJETIVO_FP)
                && matchPostagRegexp(tokens[i + 1], NOME_FP) && mArtigoOFP.matches()))) {
          replacement = relevantWords2.get(token).getToken();
        }

      }
      if (replacement != null) {
        final String msg = "Se \u00E9 um nome ou um adjectivo, tem acento.";
        final RuleMatch ruleMatch = new RuleMatch(this, sentence,
            tokens[i].getStartPos(), tokens[i].getEndPos(),
            msg, "Falta um acento");
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
