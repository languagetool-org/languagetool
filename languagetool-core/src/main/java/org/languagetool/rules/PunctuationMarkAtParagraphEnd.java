/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://danielnaber.de/)
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
import java.util.*;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.Tag;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tools.Tools;

/**
 * A rule that checks for a punctuation mark at the end of a paragraph.
 * @author Fred Kruse
 * @since 4.1
 */
public class PunctuationMarkAtParagraphEnd extends TextLevelRule {

  private final static String[] PUNCTUATION_MARKS = {".", "!", "?", ":", ",", ";"};
  private final static String[] QUOTATION_MARKS = {"„", "»", "«", "\"", "”", "″", "’", "‚", "‘", "›", "‹", "′", "'"};
  
  private final Language lang;

  /**
   * @since 4.5
   */
  public PunctuationMarkAtParagraphEnd(ResourceBundle messages, Language lang, boolean defaultActive) {
    super(messages);
    this.lang = Objects.requireNonNull(lang);
    super.setCategory(Categories.PUNCTUATION.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Grammar);
    setTags(Collections.singletonList(Tag.picky));
    if (!defaultActive) {
      setDefaultOff();
    }
  }

  public PunctuationMarkAtParagraphEnd(ResourceBundle messages, Language lang) {
    this(messages, lang, true);
  }

  @Override
  public String getId() {
    return "PUNCTUATION_PARAGRAPH_END";
  }

  @Override
  public String getDescription() {
    return messages.getString("punctuation_mark_paragraph_end_desc");
  }
  
  private static boolean stringEqualsAny(String token, String[] any) {
    for (String s : any) {
      if (token.equals(s)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isQuotationMark(AnalyzedTokenReadings tk) {
    return stringEqualsAny(tk.getToken(), QUOTATION_MARKS);
  }

  private static boolean isPunctuationMark(AnalyzedTokenReadings tk) {
    return stringEqualsAny(tk.getToken(), PUNCTUATION_MARKS);
  }

  private static boolean isWord(AnalyzedTokenReadings tk) {
    return Character.isLetter(tk.getToken().charAt(0));
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int lastPara = -1;
    int pos = 0;
    boolean isFirstWord;
    for (int n = 0; n < sentences.size(); n++) {
      AnalyzedSentence sentence = sentences.get(n);
      if (Tools.isParagraphEnd(sentences, n, lang)) {
        AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
        if (tokens.length > 2) {
          isFirstWord = (isWord(tokens[1]) && !isPunctuationMark(tokens[2]))
                || (tokens.length > 3 && isQuotationMark(tokens[1]) && isWord(tokens[2]) && !isPunctuationMark(tokens[3]));
          // paragraphs containing less than two sentences (e.g. headlines, listings) are excluded from rule
          if (n - lastPara > 1 && isFirstWord) {
            int lastNWToken = tokens.length - 1;
            while (tokens[lastNWToken].isLinebreak()) {
              lastNWToken--;
            }
            if (tokens[tokens.length-2].getToken().equalsIgnoreCase(":") &&
                WordTokenizer.isUrl(tokens[tokens.length-1].getToken())) {
              // e.g. "find it at: http://example.com" should not be an error
              lastPara = n;
              pos += sentence.getText().length();
              continue;
            }
            if (isWord(tokens[lastNWToken]) 
                || (isQuotationMark(tokens[lastNWToken]) && isWord(tokens[lastNWToken - 1]))) {
              int fromPos = pos + tokens[lastNWToken].getStartPos();
              int toPos = pos + tokens[lastNWToken].getEndPos();
              RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos, toPos, 
                  messages.getString("punctuation_mark_paragraph_end_msg"));
              List<String> replacements = new ArrayList<>();
              for (String mark : PUNCTUATION_MARKS) {
                replacements.add(tokens[lastNWToken].getToken() + mark);
              }
              ruleMatch.setSuggestedReplacements(replacements);
              ruleMatches.add(ruleMatch);
            }
          }
        }
        lastPara = n;
      }
      pos += sentence.getCorrectedTextLength();
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public int minToCheckParagraph() {
    return 0;
  }

}
