/* LanguageTool, a natural language style checker
 * Copyright (C) 2026 Jaume Ortolà
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

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.PhraseRepeatRule;
import org.languagetool.tools.StringTools;

import java.util.*;


/**
 * Avoid false alarms in the phrase repetition rule.
 *
 * <p>This mirrors, in Java, all the exceptions of the {@code PHRASE_REPETITION}
 * rule in {@code ca/grammar.xml}: its {@code <antipattern>} blocks and the
 * {@code <exception>}s inside its {@code <pattern>}.
 */
public class CatalanPhraseRepeatRule extends PhraseRepeatRule {

  // from ca/resource/ca/entities.ent: <!ENTITY unitats_temps "segon|minut|..."/>
  private static final Set<String> UNITATS_TEMPS = new HashSet<>(Arrays.asList(
    "segon", "minut", "hora", "horeta", "dia", "jorn", "jornada", "setmana", "quinzena",
    "mes", "trimestre", "quadrimestre", "semestre", "any", "lustre", "dècada", "decenni",
    "segle", "mil·lenni", "mil·lenari", "minutet", "segonet", "anyet"));

  private static final List<String> POSTAG_EXCEPTIONS = Arrays.asList("_emoji_", "_PUNCT", "_PUNCT_CONT"
      , "allow_repetition", "LOC_ADJ", "LOC_ADV", "LOC_CONJ", "LOC_PREP", "SENT_START", "UNKNOWN");

  // fixed 2-, 3- and 4-token antipatterns from the XML rule (as literal surface sequences)
  private static final String[][] LITERAL_ANTIPATTERNS = {
    {"casa", "a", "casa", "a"},
    {"boca", "a", "boca", "a"},
    {"braç", "a", "braç", "a"},
    {"gen", "a", "gen", "a"},
    {"de", "tu", "a", "tu"},
    {"milions", "de"},
  };

  public CatalanPhraseRepeatRule(final ResourceBundle messages, final Language language) {
    super(messages, language);
  }

  @Override
  public String getId() {
    return "CATALAN_PHRASE_REPEAT_RULE";
  }

  @Override
  public boolean ignore(AnalyzedTokenReadings[] tokens, int position, int phraseLength) {
    if (phraseLength == 2 && ignorePhraseRepetitionRule(tokens, position)) {
      return true;
    }
    return super.ignore(tokens, position, phraseLength);
  }

  private boolean ignorePhraseRepetitionRule(AnalyzedTokenReadings[] tokens, int position) {
    int matchEnd = position + 3; // word1 word2 word1 word2
    return violatesPatternExceptions(tokens, position) || matchesAntipattern(tokens, position, matchEnd);
  }

  /**
   * Reproduces the &lt;exception&gt;s inside the rule's &lt;pattern&gt;:
   * word1 must not be punctuation/"i" nor SENT_START/allow_repetition/UNKNOWN/_PUNCT/_PUNCT_CONT/_emoji_;
   * word2 must additionally not be a LOC_ADV/LOC_ADJ/LOC_PREP/LOC_CONJ locution;
   * word3/word4 (the repeated pair) must not be such a locution either.
   */
  private boolean violatesPatternExceptions(AnalyzedTokenReadings[] tokens, int position) {

    for (int i = position; i < position + 4; i++) {
      AnalyzedTokenReadings token = tokens[i];
      String tokenStr = token.getToken();
      if (token.isPosTagUnknown()) {
        return true;
      }
      if (tokenStr.equalsIgnoreCase("i") || (StringTools.isPunctuationOrSymbol(tokenStr))) {
        return true;
      }
      for (String postagException : POSTAG_EXCEPTIONS) {
        if (token.hasPosTag(postagException)) {
          return true;
        }
      }
    }
    return false;

  }

  /**
   * Reproduces the rule's &lt;antipattern&gt; blocks: fixed idioms ("boca a boca a", "milions de", ...),
   * the "ni massa poc(s)" and "de tu a tu" contexts, the &unitats_temps; + "a" + repeat + "a" construction
   * (e.g. "dia a dia a"), and quotation marks immediately wrapping a single token.
   * Antipatterns block the rule wherever they overlap the current 2-word-phrase match.
   */
  private boolean matchesAntipattern(AnalyzedTokenReadings[] tokens, int matchStart, int matchEnd) {
    int windowStart = Math.max(1, matchStart - 3);
    int windowEnd = Math.min(tokens.length - 1, matchEnd + 3);

    for (String[] antipattern : LITERAL_ANTIPATTERNS) {
      for (int j = windowStart; j + antipattern.length - 1 <= windowEnd; j++) {
        if (sequenceMatches(tokens, j, antipattern) && overlaps(j, j + antipattern.length - 1, matchStart, matchEnd)) {
          return true;
        }
      }
    }
    for (int j = windowStart; j + 3 <= windowEnd; j++) {
      // <token regexp="yes">&unitats_temps;</token> <token>a</token> <match no="0"/> <token>a</token>
      if (UNITATS_TEMPS.contains(tokens[j].getToken().toLowerCase())
        && "a".equalsIgnoreCase(tokens[j + 1].getToken())
        && tokens[j].getToken().equalsIgnoreCase(tokens[j + 2].getToken())
        && "a".equalsIgnoreCase(tokens[j + 3].getToken())
        && overlaps(j, j + 3, matchStart, matchEnd)) {
        return true;
      }
    }
    for (int j = windowStart; j + 2 <= windowEnd; j++) {
      // <token>ni</token> <token>massa</token> <token inflected="yes">poc</token>
      if ("ni".equalsIgnoreCase(tokens[j].getToken()) && "massa".equalsIgnoreCase(tokens[j + 1].getToken())
        && tokens[j + 2].hasLemma("poc") && overlaps(j, j + 2, matchStart, matchEnd)) {
        return true;
      }
      // <token postag="_QM_OPEN"/> <token spacebefore="no"/> <token postag="_QM_CLOSE" spacebefore="no"/>
      if (tokens[j].hasPosTag("_QM_OPEN") && tokens[j + 2].hasPosTag("_QM_CLOSE") && overlaps(j, j + 2, matchStart, matchEnd)) {
        return true;
      }
    }
    return false;
  }

  private boolean sequenceMatches(AnalyzedTokenReadings[] tokens, int start, String[] words) {
    for (int k = 0; k < words.length; k++) {
      if (!tokens[start + k].getToken().equalsIgnoreCase(words[k])) {
        return false;
      }
    }
    return true;
  }

  private boolean overlaps(int aStart, int aEnd, int bStart, int bEnd) {
    return aStart <= bEnd && bStart <= aEnd;
  }

}