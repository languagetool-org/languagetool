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
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tools.StringTools;

import static java.util.regex.Pattern.compile;

/**
 * Checks that a sentence starts with an uppercase letter.
 * 
 * @author Daniel Naber
 */
public class UppercaseSentenceStartRule extends TextLevelRule {

  private static final Pattern NUMERALS_EN =
          compile("[a-z]|(m{0,4}(c[md]|d?c{0,3})(x[cl]|l?x{0,3})(i[xv]|v?i{0,3}))$");
  private static final Pattern CONTAINS_DIGIT = compile(".*\\d.*");
  private static final Pattern WHITESPACE_OR_QUOTE = compile("[ \"'„«»‘’“”\\n]"); //only ending quote is necessary?
  private static final Pattern SENTENCE_END1 = compile("[.?!…]|");
  private static final Set<String> EXCEPTIONS = new HashSet<>(Arrays.asList(
          "n", // n/a
          "w", // w/o
          "x86",
          "ⓒ",
          "ø", // used as bullet point
          "cc", // cc @daniel => "Cc @daniel" is strange
          "pH"
  ));
  private static final Pattern DIGIT_DOT = compile("\\d+\\. .*");
  private static final Pattern LINEBREAK_DIGIT_DOT = compile(".*\n\\d+\\. ");

  private final Language language;

  /** @since 3.3 */
  public UppercaseSentenceStartRule(ResourceBundle messages, Language language, IncorrectExample incorrectExample, CorrectExample correctExample) {
    this(messages, language, incorrectExample, correctExample, null);
  }

  /** @since 5.9 */
  public UppercaseSentenceStartRule(ResourceBundle messages, Language language, IncorrectExample incorrectExample, CorrectExample correctExample,
                                    URL url) {
    super(messages);
    super.setCategory(Categories.CASING.getCategory(messages));
    this.language = language;
    setLocQualityIssueType(ITSIssueType.Typographical);
    if (incorrectExample != null && correctExample != null) {
      addExamplePair(incorrectExample, correctExample);
    }
    if (url != null) {
      setUrl(url);
    }
  }

  /**
   * @deprecated use {@link #UppercaseSentenceStartRule(ResourceBundle, Language, IncorrectExample, CorrectExample)} instead (deprecated since 3.3)
   */
  public UppercaseSentenceStartRule(ResourceBundle messages, Language language) {
    this(messages, language, null, null);
  }

  @Override
  public final String getId() {
    return "UPPERCASE_SENTENCE_START";
  }

  @Override
  public final String getDescription() {
    return messages.getString("desc_uppercase_sentence");
  }

  protected boolean isException(AnalyzedTokenReadings[] tokens, int tokenIdx) {
    return false;
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    String lastParagraphString = "";
    List<RuleMatch> ruleMatches = new ArrayList<>();
    if (sentences.size() == 1 && sentences.get(0).getTokens().length == 2) {
      // Special case for a single "sentence" with a single word - it's not useful
      // to complain about this (and might hide a typo error):
      return toRuleMatchArray(ruleMatches);
    }
    int pos = 0;
    boolean isPrevSentenceNumberedList = false;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokensWithoutWhitespace();
      if (tokens.length < 2) {
        return toRuleMatchArray(ruleMatches);
      }
      int matchTokenPos = 1; // 0 = SENT_START
      AnalyzedTokenReadings firstTokenObj = tokens[matchTokenPos];
      String firstToken = firstTokenObj.getToken();
      String secondToken = null;
      String thirdToken = null;
      // ignore quote characters:
      if (tokens.length >= 3 && isQuoteStart(firstToken)) {
        matchTokenPos = 2;
        secondToken = tokens[matchTokenPos].getToken();
      }
      String firstDutchToken = dutchSpecialCase(firstToken, secondToken, tokens);
      if (firstDutchToken != null) {
        thirdToken = firstDutchToken;
        matchTokenPos = 3;
      }

      if (isException(tokens, matchTokenPos)) {
        return toRuleMatchArray(ruleMatches);
      }

      String checkToken = firstToken;
      if (thirdToken != null) {
        checkToken = thirdToken;
      } else if (secondToken != null) {
        checkToken = secondToken;
      }

      String lastToken = tokens[tokens.length - 1].getToken();
      if (WHITESPACE_OR_QUOTE.matcher(lastToken).matches()) {
        // ignore trailing whitespace or quote
        lastToken = tokens[tokens.length - 2].getToken();
      }

      boolean preventError = false;
      if (lastParagraphString.equals(",") || lastParagraphString.equals(";")) {
        preventError = true;
      }
      if (CONTAINS_DIGIT.matcher(tokens[matchTokenPos].getToken()).matches()) {
        preventError = true;
      }
      if (!SENTENCE_END1.matcher(lastParagraphString).matches() && !isSentenceEnd(lastToken)) {
        preventError = true;
      }
      
      if (!sentence.getText().replace('\u00A0', ' ').trim().isEmpty()) {
        lastParagraphString = lastToken;
      }

      //allows enumeration with lowercase letters: a), iv., etc.
      if (matchTokenPos+1 < tokens.length
              && NUMERALS_EN.matcher(tokens[matchTokenPos].getToken()).matches()
              && (tokens[matchTokenPos+1].getToken().equals(".")
              || tokens[matchTokenPos+1].getToken().equals(")"))) {
        preventError = true;
      }

      if (isPrevSentenceNumberedList || isUrl(checkToken) || isEMail(checkToken) || firstTokenObj.isImmunized()
          || tokens[matchTokenPos].hasPosTag("_IS_URL")) {
        preventError = true;
      }

      if (checkToken.length() > 0) {
        char firstChar = checkToken.charAt(0);
        if (!preventError && Character.isLowerCase(firstChar) && !EXCEPTIONS.contains(checkToken) && !StringTools.isCamelCase(checkToken)) {
          RuleMatch ruleMatch = new RuleMatch(this, sentence,
                  pos+tokens[matchTokenPos].getStartPos(),
                  pos+tokens[matchTokenPos].getEndPos(),
                  messages.getString("incorrect_case"));
          ruleMatch.setSuggestedReplacement(StringTools.uppercaseFirstChar(checkToken));
          ruleMatches.add(ruleMatch);
        }
      }
      pos += sentence.getCorrectedTextLength();
      // Plain text lists like this are not properly split into sentences, we
      // work around that here so the items don't create an error when starting lowercase:
      // 1. item one
      // 2. item two
      isPrevSentenceNumberedList = DIGIT_DOT.matcher(sentence.getText()).matches() || LINEBREAK_DIGIT_DOT.matcher(sentence.getText()).matches();
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Nullable
  private String dutchSpecialCase(String firstToken,
      String secondToken, AnalyzedTokenReadings[] tokens) {
    if (!language.getShortCode().equals("nl")) {
      return null;
    }
    if (tokens.length > 3 && firstToken.equals("'")
        && isDutchSpecialCase(secondToken)) {
      return tokens[3].getToken();
    }
    return null;
  }

  protected boolean isUrl(String token) {
    return WordTokenizer.isUrl(token);
  }

  protected boolean isEMail(String token) {
    return WordTokenizer.isEMail(token);
  }

  private boolean isDutchSpecialCase(String word) {
    return StringUtils.equalsAny(word, "k", "m", "n", "r", "s", "t");
  }

  private boolean isSentenceEnd(String word) {
    return StringUtils.equalsAny(word, ".", "?", "!", "…");
  }

  private boolean isQuoteStart(String word) {
    String[] baseQuoteStrings = { "\"", "'", "„", "»", "«", "“", "‘", "¡", "¿" };
    // pt-BR uses dashes to introduce dialogue >:(
    // will keep it separate as other locales may expect line-initial m-dashes
    // in enumerations not to introduce capital letters
    String[] searchStrings;
    if (language.getShortCode().equals("pt")) {
      String[] portugueseDialogueDashes = { "-", "–", "—" };
      searchStrings = new String[baseQuoteStrings.length + portugueseDialogueDashes.length];
      System.arraycopy(baseQuoteStrings, 0, searchStrings, 0, baseQuoteStrings.length);
      System.arraycopy(portugueseDialogueDashes, 0, searchStrings, baseQuoteStrings.length, portugueseDialogueDashes.length);
    } else {
      searchStrings = baseQuoteStrings;
    }
    return StringUtils.equalsAny(word, searchStrings);
  }

  @Override
  public int minToCheckParagraph() {
    return 0;
  }
}
