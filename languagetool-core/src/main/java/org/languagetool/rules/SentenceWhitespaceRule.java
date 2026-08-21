/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tools.StringTools;

/**
 * Checks whitespace between sentences:
 * <ul>
 *   <li>missing whitespace after sentence-ending punctuation, e.g. {@code "First.Second"}</li>
 *   <li>repeated plain spaces between sentences, e.g. {@code "First.  Second"}</li>
 * </ul>
 * Line and paragraph breaks are accepted as sentence separators and are not flagged
 * as repeated sentence whitespace.
 *   
 * @author Daniel Naber
 * @since 2.5
 */
public class SentenceWhitespaceRule extends TextLevelRule {

  private final int maxSpacesBetweenSentences;

  public SentenceWhitespaceRule(ResourceBundle messages) {
    this(messages, 1);
  }

  /**
   * @param maxSpacesBetweenSentences maximum number of plain spaces accepted between sentences.
   *        For English, use {@code new SentenceWhitespaceRule(messages, 2)} to allow traditional
   *        double spacing. For any language, use {@code Integer.MAX_VALUE} to allow any number
   *        of spaces.
   */
  public SentenceWhitespaceRule(ResourceBundle messages, int maxSpacesBetweenSentences) {
    super(messages);
    this.maxSpacesBetweenSentences = maxSpacesBetweenSentences;
    super.setCategory(Categories.TYPOGRAPHY.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Whitespace);
  }
  
  @Override
  public String getId() {
    return "SENTENCE_WHITESPACE";
  }

  @Override
  public String getDescription() {
    return messages.getString("missing_space_between_sentences");
  }

  public String getMessage(boolean prevSentenceEndsWithNumber) {
    return messages.getString("addSpaceBetweenSentences");
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    boolean isFirstSentence = true;
    String prevSentenceEndingWhitespace = "";
    boolean prevSentenceEndsWithLineBreak = false;
    boolean prevSentenceEndsWithNumber = false;
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokens();
      addRepeatedWhitespaceMatches(sentence, tokens, pos, ruleMatches);
      if (isFirstSentence) {
        isFirstSentence = false;
      } else if (!prevSentenceEndsWithLineBreak && !startsWithLineBreak(tokens)) {
        int leadingSpacesLength = getLeadingSpacesLength(tokens);
        if (isOnlySpaces(prevSentenceEndingWhitespace)
            && prevSentenceEndingWhitespace.length() + leadingSpacesLength > maxSpacesBetweenSentences
            && (prevSentenceEndingWhitespace.length() > 0 || leadingSpacesLength > 0)
            && hasTextAfterLeadingSpaces(tokens, leadingSpacesLength)) {
          RuleMatch ruleMatch = new RuleMatch(this, sentence, pos - prevSentenceEndingWhitespace.length(),
              pos + leadingSpacesLength,
              messages.getString("whitespace_repetition"));
          ruleMatch.setSuggestedReplacement(" ");
          ruleMatches.add(ruleMatch);
        } else if (prevSentenceEndingWhitespace.isEmpty() && tokens.length > 1) {
          String firstToken = tokens[1].getToken();
          RuleMatch ruleMatch = new RuleMatch(this, sentence, pos, pos+firstToken.length(), getMessage(prevSentenceEndsWithNumber));
          ruleMatch.setSuggestedReplacement(" " + firstToken);
          ruleMatches.add(ruleMatch);
        }
      }
      if (tokens.length > 0) {
        AnalyzedTokenReadings lastTokenReadings = tokens[tokens.length-1];
        String lastToken = lastTokenReadings.getToken();
        prevSentenceEndingWhitespace = lastToken.replace('\u00A0',' ').trim().isEmpty() ? lastToken : "";
        prevSentenceEndsWithLineBreak = isLineBreakToken(lastTokenReadings);
      }
      if (tokens.length > 1) {
        String prevLastToken = tokens[tokens.length-2].getToken();
        prevSentenceEndsWithNumber = StringUtils.isNumeric(prevLastToken);
      }
      pos += sentence.getCorrectedTextLength();
    }
    return toRuleMatchArray(ruleMatches);
  }

  // Some tokenizers keep "Sentence.  Next" as one analyzed sentence, so repeated
  // spaces after sentence-ending punctuation need to be detected inside the token stream.
  private void addRepeatedWhitespaceMatches(AnalyzedSentence sentence, AnalyzedTokenReadings[] tokens, int pos,
                                            List<RuleMatch> ruleMatches) {
    for (int i = 1; i < tokens.length; i++) {
      if (isSpaceToken(tokens[i]) && followsSentenceEnd(tokens, i)) {
        int firstWhitespace = i;
        int lastWhitespace = i;
        for (i++; i < tokens.length && isSpaceToken(tokens[i]); i++) {
          lastWhitespace = i;
        }
        i--;
        if (getWhitespaceLength(tokens, firstWhitespace, lastWhitespace) > maxSpacesBetweenSentences
            && isFollowedByNonWhitespaceToken(tokens, lastWhitespace)) {
          RuleMatch ruleMatch = new RuleMatch(this, sentence, pos + tokens[firstWhitespace].getStartPos(),
              pos + tokens[lastWhitespace].getEndPos(), messages.getString("whitespace_repetition"));
          ruleMatch.setSuggestedReplacement(" ");
          ruleMatches.add(ruleMatch);
        }
      }
    }
  }

  private static boolean isOnlySpaces(String token) {
    for (int i = 0; i < token.length(); i++) {
      if (token.charAt(i) != ' ') {
        return false;
      }
    }
    return true;
  }

  private static boolean isSpaceToken(AnalyzedTokenReadings token) {
    return isOnlySpaces(token.getToken());
  }

  private static boolean followsSentenceEnd(AnalyzedTokenReadings[] tokens, int whitespacePos) {
    for (int i = whitespacePos - 1; i > 0; i--) {
      if (!tokens[i].isWhitespace()) {
        return isSentenceEndToken(tokens[i].getToken());
      }
    }
    return false;
  }

  private static boolean isSentenceEndToken(String token) {
    return ".".equals(token) || "!".equals(token) || "?".equals(token);
  }

  private static int getWhitespaceLength(AnalyzedTokenReadings[] tokens, int from, int to) {
    int length = 0;
    for (int i = from; i <= to; i++) {
      length += tokens[i].getToken().length();
    }
    return length;
  }

  private static int getLeadingSpacesLength(AnalyzedTokenReadings[] tokens) {
    int length = 0;
    for (int i = 1; i < tokens.length && isSpaceToken(tokens[i]); i++) {
      length += tokens[i].getToken().length();
    }
    return length;
  }

  private static boolean hasTextAfterLeadingSpaces(AnalyzedTokenReadings[] tokens, int leadingSpacesLength) {
    int pos = getTokenIndexAfterLeadingSpaces(tokens, leadingSpacesLength);
    return pos < tokens.length && !tokens[pos].isWhitespace() && !isLineBreakToken(tokens[pos]);
  }

  private static int getTokenIndexAfterLeadingSpaces(AnalyzedTokenReadings[] tokens, int leadingSpacesLength) {
    int pos = 1;
    int spacesLength = 0;
    while (pos < tokens.length && spacesLength < leadingSpacesLength) {
      spacesLength += tokens[pos].getToken().length();
      pos++;
    }
    return pos;
  }

  private static boolean isFollowedByNonWhitespaceToken(AnalyzedTokenReadings[] tokens, int whitespacePos) {
    return whitespacePos + 1 < tokens.length
        && !tokens[whitespacePos + 1].isWhitespace()
        && !isLineBreakToken(tokens[whitespacePos + 1]);
  }

  private static boolean startsWithLineBreak(AnalyzedTokenReadings[] tokens) {
    return tokens.length > 1 && isLineBreakToken(tokens[1]);
  }

  private static boolean isLineBreakToken(AnalyzedTokenReadings token) {
    return token.isLinebreak() || StringTools.containsLineBreak(token.getToken());
  }

  @Override
  public int minToCheckParagraph() {
    return 0;
  }
  
}
