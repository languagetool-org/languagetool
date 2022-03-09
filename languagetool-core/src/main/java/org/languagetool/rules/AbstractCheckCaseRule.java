/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.tools.StringTools;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * A rule that checks case in phrases
 * 
 * @author Jaume Ortolà
 */
public abstract class AbstractCheckCaseRule extends AbstractSimpleReplaceRule2 {

  public AbstractCheckCaseRule(ResourceBundle messages, Language language) {
    super(messages, language);
    super.setLocQualityIssueType(ITSIssueType.Typographical);
    super.setCategory(Categories.CASING.getCategory(messages));
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    List<Map<String, SuggestionWithMessage>> wrongWords = getWrongWords(true);
    if (wrongWords.size() == 0) {
      return toRuleMatchArray(ruleMatches);
    }
    Queue<AnalyzedTokenReadings> prevTokens = new ArrayBlockingQueue<>(wrongWords.size());
    int sentStart = 0;
    while (sentStart + 1 < tokens.length && isPunctuationStart(tokens[sentStart + 1].getToken())) {
      sentStart++;
    }
    for (int i = 1; i < tokens.length; i++) {
      addToQueue(tokens[i], prevTokens);
      StringBuilder sb = new StringBuilder();
      List<String> phrases = new ArrayList<>();
      List<AnalyzedTokenReadings> prevTokensList = Arrays.asList(prevTokens.toArray(new AnalyzedTokenReadings[0]));
      for (int j = prevTokensList.size() - 1; j >= 0; j--) {
        if (j != prevTokensList.size() - 1 && prevTokensList.get(j + 1).isWhitespaceBefore()) {
          sb.insert(0, " ");
        }
        sb.insert(0, prevTokensList.get(j).getToken());
        phrases.add(0, sb.toString());
      }
      if (isTokenException(tokens[i])) {
        continue;
      }
      int len = phrases.size(); // prevTokensList and variants have now the same length
      for (int j = 0; j < len; j++) { // longest words first
        String originalPhrase = phrases.get(j);
        int crtWordCount = len - j;
        SuggestionWithMessage suggMess = wrongWords.get(crtWordCount - 1).get(originalPhrase.toLowerCase(getLocale()));
        if (suggMess == null) {
          continue;
        }
        String correctPhrase = suggMess.getSuggestion();
        String capitalizedCorrect = StringTools.uppercaseFirstChar(correctPhrase);
        int startPos = prevTokensList.get(len - crtWordCount).getStartPos();
        int endPos = prevTokensList.get(len - 1).getEndPos();
        if ((crtWordCount + sentStart == i && originalPhrase.equals(capitalizedCorrect))
            || correctPhrase.equals(originalPhrase)) {
          // remove last match if is contained in a correct phrase
          if (ruleMatches.size() > 0) {
            RuleMatch lastRuleMatch = ruleMatches.get(ruleMatches.size() - 1);
            if (lastRuleMatch.getToPos() > startPos) {
              ruleMatches.remove(ruleMatches.size() - 1);
            }
          }
          // The phrase is correct. Don't look into shorter phrases inside this phrase.
          break;
        }
        if (originalPhrase.equals(originalPhrase.toUpperCase())) {
          continue;
        }
        if (correctPhrase != null && !correctPhrase.equals(originalPhrase)) {
          RuleMatch ruleMatch;
          String msg = suggMess.getMessage();
          if (msg == null) {
            msg = getMessage();
          }
          ruleMatch = new RuleMatch(this, sentence, startPos, endPos, msg, getShort());
          if (subRuleSpecificIds) {
            ruleMatch.setSpecificRuleId(StringTools.toId(getId() + "_" + correctPhrase));
          }
          if (crtWordCount + sentStart == i) {
            // Capitalize suggestion at the sentence start
            correctPhrase = StringTools.uppercaseFirstChar(correctPhrase);
          }
          ruleMatch.addSuggestedReplacement(correctPhrase);
          if (!isException(sentence.getText().substring(startPos, endPos))) {
            // keep only the longest match
            if (ruleMatches.size() > 0) {
              RuleMatch lastRuleMatch = ruleMatches.get(ruleMatches.size() - 1);
              if (lastRuleMatch.getFromPos() == ruleMatch.getFromPos()
                  && lastRuleMatch.getToPos() < ruleMatch.getToPos()) {
                ruleMatches.remove(ruleMatches.size() - 1);
              }
            }
            ruleMatches.add(ruleMatch);
          }
          break;
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private boolean isPunctuationStart(String word) {
    return StringUtils.getDigits(word).length() > 0 // e.g. postal codes
        || StringUtils.equalsAny(word, "\"", "'", "„", "»", "«", "“", "‘", "¡", "¿", "-", "–", "—", "―", "‒");
  }

}
