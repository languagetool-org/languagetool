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
package org.languagetool.rules;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

import org.languagetool.*;
import org.languagetool.tools.Tools;

/**
 * A rule that warns on long paragraphs.
 * @since 4.2
 */
public class LongParagraphRule extends TextLevelRule {

  public static final String RULE_ID = "TOO_LONG_PARAGRAPH";

  private static final boolean DEFAULT_ACTIVATION = false;
  private static final int DEFAULT_MAX_WORDS = 220;

  private final Language lang;

  private int maxWords = DEFAULT_MAX_WORDS;

  public LongParagraphRule(ResourceBundle messages, Language lang, UserConfig userConfig, int defaultWords, boolean defaultActive) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
    this.lang = lang;
    setDefaultOff();
    if (defaultWords > 0) {
      this.maxWords = defaultWords;
    }
    if (userConfig != null) {
      int confWords = userConfig.getConfigValueByID(getId());
      if (confWords > 0) {
        this.maxWords = confWords;
      }
    }
    setLocQualityIssueType(ITSIssueType.Style);
    setTags(Arrays.asList(Tag.picky));
  }

  /** Note: will be off by default. */
  public LongParagraphRule(ResourceBundle messages, Language lang, UserConfig userConfig, int defaultWords) {
    this(messages, lang, userConfig, defaultWords, DEFAULT_ACTIVATION);
  }

  public LongParagraphRule(ResourceBundle messages, Language lang, UserConfig userConfig) {
    this(messages, lang, userConfig, -1, true);
  }

  @Override
  public String getDescription() {
    return MessageFormat.format(messages.getString("long_paragraph_rule_desc"), maxWords);
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Override
  public int getDefaultValue() {
    return maxWords;
  }

  @Override
  public boolean hasConfigurableValue() {
    return true;
  }

  @Override
  public int getMinConfigurableValue() {
    return 5;
  }

  @Override
  public int getMaxConfigurableValue() {
    return 300;
  }

  public String getConfigureText() {
    return messages.getString("guiLongParagraphsText");
  }

  public String getMessage() {
    return MessageFormat.format(messages.getString("long_paragraph_rule_msg"), maxWords);
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0;
    int startPos = 0;
    int endPos = 0;
    int wordCount = 0;
    boolean paraHasLinebreaks = false;
    for (int n = 0; n < sentences.size(); n++) {
      AnalyzedSentence sentence = sentences.get(n);
      boolean paragraphEnd = Tools.isParagraphEnd(sentences, n, lang);
      if (!paragraphEnd && sentence.getText().replaceFirst("^\n+", "").contains("\n")) {
        // e.g. text with manually added line breaks (e.g. issues on github with "- [ ]" syntax)
        paraHasLinebreaks = true;
      }
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      for (AnalyzedTokenReadings token : tokens) {
        if (!token.isWhitespace() && !token.isSentenceStart() && !token.isNonWord()) {
          wordCount++;
          if (wordCount == maxWords) {
            startPos = token.getStartPos() + pos;
            endPos = token.getEndPos() + pos;
          }
        }
      }
      if (paragraphEnd) {
        if (wordCount > maxWords + 5 && !paraHasLinebreaks) {  // + 5: don't show match almost at end of paragraph
          RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, getMessage());
          ruleMatches.add(ruleMatch);
        }
        wordCount = 0;
        paraHasLinebreaks = false;
      }
      pos += sentence.getCorrectedTextLength();
    }
    if (wordCount > maxWords) {
      RuleMatch ruleMatch = new RuleMatch(this, startPos, endPos, getMessage());
      ruleMatches.add(ruleMatch);
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public int minToCheckParagraph() {
    return 0;
  }

}
