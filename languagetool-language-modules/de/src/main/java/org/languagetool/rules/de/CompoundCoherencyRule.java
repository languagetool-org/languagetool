/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.*;

import java.io.IOException;
import java.util.*;

/**
 * Check consistent spelling for German compounds. It's a style issue
 * if a compound is written once with and once without hyphen in the same text.
 * E.g. "Zahnärzteverband" and "Zahnärzte-Verband"
 * @author Daniel Naber
 */
public class CompoundCoherencyRule extends TextLevelRule {

  public CompoundCoherencyRule(ResourceBundle messages) {
    super.setCategory(Categories.STYLE.getCategory(messages));
    addExamplePair(Example.wrong("Ein Helpdesk gliedert sich in verschiedene Level. Die Qualität des <marker>Help-Desks</marker> ist wichtig."),
                   Example.fixed("Ein Helpdesk gliedert sich in verschiedene Level. Die Qualität des <marker>Helpdesks</marker> ist wichtig."));
  }

  @Override
  public String getId() {
    return "DE_COMPOUND_COHERENCY";
  }

  @Override
  public String getDescription() {
    return "Einheitliche Schreibung bei Komposita (mit oder ohne Bindestrich)";
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    Map<String,List<String>> normToTextOccurrences = new HashMap<>();
    int pos = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      for (AnalyzedTokenReadings atr : tokens) {
        String lemmaOrNull = getLemma(atr);
        String token = atr.getToken();
        if (token.isEmpty()) {
          continue;
        }
        // The whole implementation could be simpler, but this way we also catch cases where
        // the word (and thus its lemma) isn't known.
        String lemma = lemmaOrNull != null ? lemmaOrNull : token;
        String normToken = lemma.replace("-", "").toLowerCase();
        if (StringUtils.isNumeric(normToken)) {
          // avoid messages about "2-3" and "23" both being used
          break;
        }
        List<String> textOcc = normToTextOccurrences.get(normToken);
        if (textOcc != null) {
          if (textOcc.stream().noneMatch(f -> f.equalsIgnoreCase(lemma))) {
            String other = textOcc.get(0);
            if (containsHyphenInside(other) || containsHyphenInside(token)) {
              String msg = "Uneinheitliche Verwendung von Bindestrichen. Der Text enthält sowohl '" + token + "' als auch '" + other + "'.";
              RuleMatch ruleMatch = new RuleMatch(this, sentence, pos + atr.getStartPos(), pos + atr.getEndPos(), msg);
              if (token.replace("-", "").equalsIgnoreCase(other.replace("-", ""))) {
                // might be different inflected forms, so only suggest if really just the hyphen is different:
                ruleMatch.setSuggestedReplacement(other);
              }
              ruleMatches.add(ruleMatch);
            }
          }
        } else {
          List<String> l = new ArrayList<>();
          l.add(lemma);
          normToTextOccurrences.putIfAbsent(normToken, l);
        }
      }
      pos += sentence.getCorrectedTextLength();
    }
    return toRuleMatchArray(ruleMatches);
  }

  private boolean containsHyphenInside(String token) {
    return token.contains("-") && !token.startsWith("-") && !token.endsWith("-");
  }

  @Nullable
  private String getLemma(AnalyzedTokenReadings atr) {
    String lemmaOrNull = atr.hasSameLemmas() && atr.getReadingsLength() > 0 ? atr.getReadings().get(0).getLemma() : null;
    if (lemmaOrNull != null) {
      // our analysis gets lemma "Jugendfoto" for "Jugend-Fotos", we 'fix' that here
      String token = atr.getToken();
      if (!lemmaOrNull.contains("-") && token.contains("-")) {
        StringBuilder lemmaBuilder = new StringBuilder();
        for (int lemmaPos = 0, tokenPos = 0; lemmaPos < lemmaOrNull.length(); lemmaPos++, tokenPos++) {
          if (tokenPos >= token.length()) {
            break;
          }
          char lemmaChar = lemmaOrNull.charAt(lemmaPos);
          char tokenChar = token.charAt(tokenPos);
          if (lemmaChar == tokenChar) {
            lemmaBuilder.append(lemmaChar);
          } else if (token.charAt(tokenPos) == '-') {
            tokenPos++;  // skip hyphen
            lemmaBuilder.append('-');
            if (lemmaPos + 1 < token.length() && Character.isUpperCase(token.charAt(tokenPos))) {
              lemmaBuilder.append(Character.toUpperCase(lemmaChar));
            } else {
              lemmaBuilder.append(lemmaChar);
            }
          }
        }
        return lemmaBuilder.toString();
      }
      return lemmaOrNull;
    } else {
      return null;
    }
  }

  @Override
  public int minToCheckParagraph() {
    return -1;
  }

}
