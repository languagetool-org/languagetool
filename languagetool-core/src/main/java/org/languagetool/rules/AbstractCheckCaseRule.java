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
  private final Language language;
  private boolean ignoreShortUppercaseWords = true;
  private int MAX_LENGTH_SHORT_WORDS = 4;

  public AbstractCheckCaseRule(ResourceBundle messages, Language language) {
    super(messages, language);
    this.language = language;
    setLocQualityIssueType(ITSIssueType.Typographical);
    setCategory(Categories.CASING.getCategory(messages));
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    List<Map<String, SuggestionWithMessage>> wrongWords = getWrongWords(true);
    if (wrongWords.size() == 0) {
      return toRuleMatchArray(ruleMatches);
    }
    int sentStart = 1;
    while (sentStart < tokens.length && isPunctuationStart(tokens[sentStart].getToken())) {
      sentStart++;
    }
    int endIndex;
    int startIndex;
    for (endIndex = 1; endIndex < tokens.length; endIndex++) {
      startIndex = endIndex;
      StringBuilder sb = new StringBuilder();
      List<String> phrases = new ArrayList<>();
      List<Integer> phrasesStartIndex = new ArrayList<>();
      while (startIndex > 0) {
        if (startIndex != endIndex && tokens[startIndex + 1].isWhitespaceBefore()) {
          sb.insert(0, " ");
        }
        sb.insert(0, tokens[startIndex].getToken());
        if (getWordCountIndex(sb.toString()) < wrongWords.size()) {
          phrases.add(0, sb.toString());
          phrasesStartIndex.add(0, startIndex);
          startIndex--;
        } else {
          startIndex = -1; // end while
        }
      }
      if (isTokenException(tokens[endIndex])) {
        continue;
      }
      int len = phrases.size(); // prevTokensList and variants have now the same length
      for (int j = 0; j < len; j++) { // longest words first
        String originalPhrase = phrases.get(j);
        startIndex = phrasesStartIndex.get(j);
        String lcOriginalPhrase = originalPhrase.toLowerCase(getLocale());
        int wordCountIndex = getWordCountIndex(lcOriginalPhrase);
        if (wordCountIndex < 0) {
          continue;
        }
        SuggestionWithMessage suggMess = wrongWords.get(wordCountIndex).get(lcOriginalPhrase);
        if (suggMess == null) {
          continue;
        }
        String correctPhrase = suggMess.getSuggestion();
        String capitalizedCorrect = StringTools.uppercaseFirstChar(correctPhrase);
        int startPos = tokens[startIndex].getStartPos();
        int endPos = tokens[endIndex].getEndPos();
        if ((sentStart == startIndex && originalPhrase.equals(capitalizedCorrect))
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
          if (ignoreShortUppercaseWords) {
            continue;
          } else {
            if ( originalPhrase.length() <= MAX_LENGTH_SHORT_WORDS ){
              // correct uppercase words of max X characters
            } else{
              continue;
            }
          }
        }
        if (correctPhrase != null && !correctPhrase.equals(originalPhrase)) {
          RuleMatch ruleMatch;
          String msg = suggMess.getMessage();
          if (msg == null) {
            msg = getMessage();
          }
          ruleMatch = new RuleMatch(this, sentence, startPos, endPos, msg, getShort());
          if (subRuleSpecificIds) {
            ruleMatch.setSpecificRuleId(StringTools.toId(getId() + "_" + correctPhrase, language));
          }
          if (sentStart == startIndex) {
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

  protected boolean isIgnoreShortUppercaseWords() {
    return ignoreShortUppercaseWords;
  }

  protected void setIgnoreShortUppercaseWords(boolean value) {
    ignoreShortUppercaseWords = value;
  }

}
