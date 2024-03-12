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

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.StringUtils;
import org.languagetool.*;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.languagetool.JLanguageTool.getDataBroker;

/**
 * A rule that matches words which should not be used and suggests correct ones instead.
 * <p>Unlike AbstractSimpleReplaceRule, it supports phrases (Ex: "aqua forte" -&gt; "acvaforte").
 * Note: Merge this into {@link AbstractSimpleReplaceRule}
 *
 * @author Ionuț Păduraru
 */
public abstract class AbstractSimpleReplaceRule2 extends Rule {

  private volatile boolean initialized;
  private Map<String, Integer> mStartSpace;
  private Map<String, Integer> mStartNoSpace;
  private Map<String, SuggestionWithMessage> mFullSpace;
  private Map<String, SuggestionWithMessage> mFullNoSpace;

  protected boolean isCheckingCase() {
    return false;
  }

  private final static int MAX_TOKENS_IN_MULTIWORD = 20;

  //only for CheckCaseRule
  private boolean ignoreShortUppercaseWords = true;
  //only for CheckCaseRule
  private int MAX_LENGTH_SHORT_WORDS = 4;

  private boolean ruleHasSuggestions = true;

  public enum CaseSensitivy {CS, CI, CSExceptAtSentenceStart}

  protected final Language language;

  protected boolean subRuleSpecificIds = false;

  public abstract List<String> getFileNames();

  public List<URL> getFilePaths() {
    return null;
  }

  @Override
  public abstract String getId();

  /**
   * @return A string where {@code $match} will be replaced with the matching word.
   */
  @Override
  public abstract String getDescription();

  public abstract String getShort();

  /**
   * @return A string where {@code $match} will be replaced with the matching word
   * and {@code $suggestions} will be replaced with the alternatives. This is the string
   * shown to the user.
   */
  public abstract String getMessage();

  /**
   * @return the word used to separate multiple suggestions; used only before last suggestion, the rest are
   * comma-separated.
   */
  public String getSuggestionsSeparator() {
    return ", ";
  }

  /**
   * locale used on case-conversion
   */
  public abstract Locale getLocale();


  public AbstractSimpleReplaceRule2(ResourceBundle messages, Language language) {
    super(messages);
    this.language = Objects.requireNonNull(language);
    super.setCategory(Categories.MISC.getCategory(messages));
  }

  /**
   * If this is set, each replacement pair will have its own rule ID, making rule deactivations more specific.
   *
   * @since 5.1
   */
  public void useSubRuleSpecificIds() {
    subRuleSpecificIds = true;
  }

  public CaseSensitivy getCaseSensitivy() {
    return CaseSensitivy.CI;
  }

  /**
   * Used if each input form the replacement file has its specific id.
   */
  public String getDescription(String details) {
    return null;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    lazyInit();
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    int sentStart = 1;
    while (sentStart < tokens.length && isPunctuationStart(tokens[sentStart].getToken())) {
      sentStart++;
    }
    int[] checkCaseCoveredUpto = new int[1];
    checkCaseCoveredUpto[0] = 0;
    for (int startIndex = sentStart; startIndex < tokens.length; startIndex++) {
      if (isTokenException(tokens[startIndex])) {
        continue;
      }
      String tok = tokens[startIndex].getToken();
      if (tok.length() < 1) {
        continue;
      }
      // If there is no whitespace to the next token, concatenate the next token
      int k = startIndex + 1;
      while (k < tokens.length && !tokens[k].isWhitespaceBefore()) {
        tok = tok + tokens[k].getToken();
        k++;
      }
      if (getCaseSensitivy() == CaseSensitivy.CI) {
        tok = tok.toLowerCase();
      }
      if (mStartSpace.containsKey(tok)) {
        StringBuilder keyBuilder = new StringBuilder();
        int maxTokenLen = mStartSpace.get(tok);
        int endIndex = startIndex;
        while (endIndex < tokens.length && endIndex - startIndex < MAX_TOKENS_IN_MULTIWORD) {
          if (endIndex > startIndex && tokens[endIndex].isWhitespaceBefore()) {
            keyBuilder.append(" ");
          }
          keyBuilder.append(tokens[endIndex].getToken());
          String originalStr = keyBuilder.toString();
          int numberOfSpaces = StringTools.numberOf(originalStr, " ");
          if (numberOfSpaces + 1 > maxTokenLen) {
            break;
          }
          if (numberOfSpaces > 0) {
            String keyStr = originalStr;
            if (getCaseSensitivy() == CaseSensitivy.CI) {
              keyStr = keyStr.toLowerCase();
            }
            SuggestionWithMessage suggestionWithMessage = mFullSpace.get(keyStr);
            createMatch(ruleMatches, suggestionWithMessage, startIndex, endIndex, originalStr, tokens, sentence,
              sentStart, checkCaseCoveredUpto);
            //No language uses this. It could be removed.
            if (suggestionWithMessage == null && sentStart == startIndex && getCaseSensitivy() == CaseSensitivy.CSExceptAtSentenceStart
              && !keyStr.equals(StringTools.lowercaseFirstChar(keyStr))) {
              keyStr = StringTools.lowercaseFirstChar(keyStr);
              suggestionWithMessage = mFullSpace.get(keyStr);
              createMatch(ruleMatches, suggestionWithMessage, startIndex, endIndex, originalStr, tokens, sentence,
                sentStart, checkCaseCoveredUpto);
            }
          }
          endIndex++;
        }
      }
      if (mStartNoSpace.containsKey(tok.substring(0, 1))) {
        int endIndex = startIndex;
        StringBuilder keyBuilder = new StringBuilder();
        while (endIndex < tokens.length && endIndex - startIndex < MAX_TOKENS_IN_MULTIWORD) {
          if (endIndex > startIndex && tokens[endIndex].isWhitespaceBefore()) {
            break;
          }
          keyBuilder.append(tokens[endIndex].getToken());
          String originalStr = keyBuilder.toString();
          String keyStr = originalStr;
          if (getCaseSensitivy() == CaseSensitivy.CI) {
            keyStr = keyStr.toLowerCase();
          }
          SuggestionWithMessage suggestionWithMessage = mFullNoSpace.get(keyStr);
          createMatch(ruleMatches, suggestionWithMessage, startIndex, endIndex, originalStr, tokens, sentence,
            sentStart, checkCaseCoveredUpto);
          //No language uses this. It could be removed.
          if (suggestionWithMessage == null && sentStart == startIndex && getCaseSensitivy() == CaseSensitivy.CSExceptAtSentenceStart
            && !keyStr.equals(StringTools.lowercaseFirstChar(keyStr))) {
            keyStr = StringTools.lowercaseFirstChar(keyStr);
            suggestionWithMessage = mFullNoSpace.get(keyStr);
            createMatch(ruleMatches, suggestionWithMessage, startIndex, endIndex, originalStr, tokens, sentence,
              sentStart, checkCaseCoveredUpto);
          }
          endIndex++;
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private void createMatch(List<RuleMatch> ruleMatches, SuggestionWithMessage suggestionWithMessage, int startIndex,
                           int endIndex, String originalStr, AnalyzedTokenReadings[] tokens, AnalyzedSentence sentence,
                           int sentStart, int[] checkCaseCoveredUpto) {
    if (suggestionWithMessage == null || isException(originalStr)) {
      return;
    }
    List<String> replacements = Arrays.asList(suggestionWithMessage.getSuggestion().split("\\|"));
    int fromPos = tokens[startIndex].getStartPos();
    int toPos = tokens[endIndex].getEndPos();
    //keep only the longest match
    if (ruleMatches.size() > 0) {
      RuleMatch lastRuleMatch = ruleMatches.get(ruleMatches.size() - 1);
      if (lastRuleMatch.getFromPos() <= fromPos
        && lastRuleMatch.getToPos() >= toPos) {
        return;
      }
    }
    boolean firstWordInSuggIsCamelCase = replacements.stream().anyMatch(k -> StringTools.isCamelCase(k.split(" ")[0]));
    boolean isAllUppercase = StringTools.isAllUppercase(originalStr);
    boolean isCapitalized = StringTools.isCapitalizedWord(originalStr.split(" ")[0]);
    // exceptions for checking case
    if (isCheckingCase()) {
      if (endIndex <= checkCaseCoveredUpto[0]) {
        return;
      }
      String replacementCheckCase = replacements.get(0);
      if ((sentStart == startIndex && originalStr.equals(StringTools.uppercaseFirstChar(replacementCheckCase)))
        || originalStr.equals(replacementCheckCase)) {
        if (ruleMatches.size() > 0) {
          // remove last match if is contained in a correct phrase
          RuleMatch lastRuleMatch = ruleMatches.get(ruleMatches.size() - 1);
          if (lastRuleMatch.getToPos() > fromPos) {
            ruleMatches.remove(ruleMatches.size() - 1);
          }
        }
        checkCaseCoveredUpto[0] = endIndex;
        return;
      }
      //Allow all-upper case, except for CamelCase and short words in Dutch (length<MAX_LENGTH_SHORT_WORDS)
      if (!firstWordInSuggIsCamelCase && originalStr.equals(originalStr.toUpperCase())) {
        if (ignoreShortUppercaseWords || originalStr.length() > MAX_LENGTH_SHORT_WORDS) {
          checkCaseCoveredUpto[0] = endIndex;
          return;
        }
      }
    }
    List<String> finalReplacements = new ArrayList<>();
    // adjust casing of suggestions
    for (String repl : replacements) {
      String finalRepl = repl;
      if (!firstWordInSuggIsCamelCase && (sentStart == startIndex || (isCapitalized && !isCheckingCase()))) {
        finalRepl = StringTools.uppercaseFirstChar(repl);
      }
      if (!isCheckingCase() && isAllUppercase) {
        finalRepl = repl.toUpperCase();
      }
      if (!repl.equals(originalStr) && !finalRepl.equals(originalStr) && !finalReplacements.contains(finalRepl)) {
        finalReplacements.add(finalRepl);
      }
    }
    if (ruleHasSuggestions && finalReplacements.isEmpty()) {
      return;
    }
    // Begin of match creation
    String msg = suggestionWithMessage.getMessage();
    String url = null;
    if (msg != null && (msg.startsWith("http://") || msg.startsWith("https://"))) {
      msg = null;
      url = msg;
    }
    if (msg == null) {
      String msgSuggestions = "";
      for (int k = 0; k < replacements.size(); k++) {
        if (k > 0) {
          msgSuggestions += (k == replacements.size() - 1 ? getSuggestionsSeparator() : ", ");
        }
        msgSuggestions += "<suggestion>" + replacements.get(k) + "</suggestion>";
      }
      msg = getMessage().replaceFirst("\\$match", originalStr).replaceFirst("\\$suggestions", msgSuggestions);
    }
    RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos, toPos, msg, getShort());
    if (subRuleSpecificIds) {
      String id = StringTools.toId(getId() + "_" + originalStr, language);
      String desc = getDescription().replace("$match", originalStr);
      SpecificIdRule specificIdRule = new SpecificIdRule(id, desc, isPremium(), getCategory(),
        getLocQualityIssueType(), getTags());
      ruleMatch = new RuleMatch(specificIdRule, sentence, fromPos, toPos, msg, getShort());
    }
    if (url != null) {
      ruleMatch.setUrl(Tools.getUrl(url));
    }
    if (ruleHasSuggestions) {
      ruleMatch.setSuggestedReplacements(finalReplacements);
    }
    // End of match creation
    if (isRuleMatchException(ruleMatch)) {
      return;
    }
    //keep only the longest match
    if (ruleMatches.size() > 0) {
      RuleMatch lastRuleMatch = ruleMatches.get(ruleMatches.size() - 1);
      if (lastRuleMatch.getFromPos() >= fromPos
        && lastRuleMatch.getToPos() <= toPos) {
        ruleMatches.remove(ruleMatches.size() - 1);
      }
    }
    ruleMatches.add(ruleMatch);
  }

  protected boolean isRuleMatchException(RuleMatch ruleMatch) {
    return false;
  }

  protected boolean isException(String matchedText) {
    return false;
  }

  protected boolean isTokenException(AnalyzedTokenReadings atr) {
    return false;
  }

  /**
   * Create a warning if a key word of the replacement rule is not allowed by the speller rule.
   */
  public boolean checkKeyWordsAreKnownToSpeller() {
    return false;
  }

  /**
   * Create a warning if a key word of the replacement rule is allowed by the speller rule.
   */
  public boolean checkKeyWordsAreUnknownToSpeller() {
    return false;
  }

  public boolean separateKeyWordsBySpeller() {
    return false;
  }


  protected boolean isPunctuationStart(String word) {
    return StringUtils.getDigits(word).length() > 0 // e.g. postal codes
      || StringTools.isPunctuationMark(word) || StringTools.isNotWordCharacter(word);
  }

  private void lazyInit() {
    if (initialized) {
      return;
    }
    synchronized (this) {
      if (initialized) return;
      Object2IntOpenHashMap<String> mStartSpace = new Object2IntOpenHashMap<>();
      Object2IntOpenHashMap<String> mStartNoSpace = new Object2IntOpenHashMap<>();
      Object2ObjectOpenHashMap<String, SuggestionWithMessage> mFullSpace = new Object2ObjectOpenHashMap<>();
      Object2ObjectOpenHashMap<String, SuggestionWithMessage> mFullNoSpace = new Object2ObjectOpenHashMap<>();
      fillMaps(mStartSpace, mStartNoSpace, mFullSpace, mFullNoSpace);
      mStartSpace.trim();
      mStartNoSpace.trim();
      mFullSpace.trim();
      mFullNoSpace.trim();
      this.mStartSpace = mStartSpace;
      this.mStartNoSpace = mStartNoSpace;
      this.mFullSpace = mFullSpace;
      this.mFullNoSpace = mFullNoSpace;
      initialized = true;
    }
  }

  private void fillMaps(Map<String, Integer> mStartSpace, Map<String, Integer> mStartNoSpace, Map<String,
    SuggestionWithMessage> mFullSpace, Map<String, SuggestionWithMessage> mFullNoSpace) {
    try {
      for (URL filePath : getAllFilePaths()) {
        BufferedReader br = new BufferedReader(new InputStreamReader(filePath.openStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = br.readLine()) != null) {
          line = line.trim();
          if (line.isEmpty() || line.charAt(0) == '#') { // ignore comments
            continue;
          }
          if (line.contains("  ") && !language.getShortCode().equals("ar")) {
            throw new RuntimeException("More than one consecutive space in " + filePath + " - use a tab character as " +
              "a delimiter for the message: " + line);
          }
          line = StringUtils.substringBefore(line, "#").trim();
          if (isCheckingCase()) {
            // It is a rule for checking chase
            String[] parts = line.split("=");
            line = parts[0].toLowerCase().trim() + "=" + parts[0].trim();
            if (parts.length == 2) {
              line = line + "\t" + parts[1].trim();
            }
          }
          String[] parts = line.split("\t");
          String confPair = parts[0];
          String msg;
          if (parts.length == 1) {
            msg = null;
          } else if (parts.length == 2) {
            msg = parts[1];
          } else {
            throw new IOException("Format error in file " + filePath + ". Expected at most 1 '=' character and at " +
              "most 1 tab character. Line: " + line);
          }
          String[] confPairParts = confPair.split("=");
          String suggestion = "";
          if (ruleHasSuggestions) {
            if (confPairParts.length < 2) {
              throw new IOException("Format error in file " + filePath
                + ". Missing suggestion after character '='. Line: " + line);
            }
            suggestion = confPairParts[1];
          }
          String[] wrongForms = confPairParts[0].split("\\|"); // multiple incorrect forms
          for (String wrongForm : wrongForms) {
            String searchKey = getCaseSensitivy() == CaseSensitivy.CI ? wrongForm.toLowerCase() : wrongForm;
            if (!isCheckingCase() && searchKey.equals(suggestion)) {
              throw new IOException("Format error in file " + filePath
                + ". Found same word on left and right side of '='. Line: " + line);
            }
            SuggestionWithMessage suggestionWithMessage = new SuggestionWithMessage(suggestion, msg);
            boolean containsSpace = wrongForm.indexOf(' ') > 0;
            if (!containsSpace) {
              String firstChar = searchKey.substring(0, 1);
              if (mStartNoSpace.containsKey(firstChar)) {
                if (mStartNoSpace.get(firstChar) < searchKey.length()) {
                  mStartNoSpace.put(firstChar, searchKey.length());
                }
              } else {
                mStartNoSpace.put(firstChar, searchKey.length());
              }
              mFullNoSpace.put(searchKey, suggestionWithMessage);
            } else {
              String[] tokens = searchKey.split(" ");
              String firstToken = tokens[0];
              if (mStartSpace.containsKey(firstToken)) {
                if (mStartSpace.get(firstToken) < tokens.length) {
                  mStartSpace.put(firstToken, tokens.length);
                }
              } else {
                mStartSpace.put(firstToken, tokens.length);
              }
              mFullSpace.put(searchKey, suggestionWithMessage);
            }
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private List<URL> getAllFilePaths() {
    List<URL> filePaths = new ArrayList<>();
    if (getFilePaths() != null) {
      filePaths.addAll(getFilePaths());
    }
    for (String fileName : getFileNames()) {
      filePaths.add(getDataBroker().getFromRulesDirAsUrl(fileName));
    }
    return filePaths;
  }

  // Only for checking the spelling of the suggestions
  public List<Map<String, SuggestionWithMessage>> getWrongWords() {
    lazyInit();
    List<Map<String, SuggestionWithMessage>> wrongWords = new ArrayList<>();
    wrongWords.add(mFullSpace);
    wrongWords.add(mFullNoSpace);
    return wrongWords;
  }

  // only for check_case rule
  protected boolean isIgnoreShortUppercaseWords() {
    return ignoreShortUppercaseWords;
  }

  // only for check_case rule
  protected void setIgnoreShortUppercaseWords(boolean value) {
    ignoreShortUppercaseWords = value;
  }

  protected void setRuleHasSuggestions(boolean value) {
    ruleHasSuggestions = value;
  }

}
