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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.languagetool.JLanguageTool.getDataBroker;

/**
 * A rule that matches words which should not be used and suggests correct ones instead. 
 * <p>Unlike AbstractSimpleReplaceRule, it supports phrases (Ex: "aqua forte" -&gt; "acvaforte").
 * Note: Merge this into {@link AbstractSimpleReplaceRule}
 *
 * @author Ionuț Păduraru
 */
public abstract class AbstractSimpleReplaceRule2 extends Rule {

  public enum CaseSensitivy {CS, CI, CSExceptAtSentenceStart}

  private final Language language;

  protected boolean subRuleSpecificIds;
  
  public abstract List<String> getFileNames();

  public List<URL> getFilePaths(){
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
   * @return the word used to separate multiple suggestions; used only before last suggestion, the rest are comma-separated.  
   */
  public String getSuggestionsSeparator() {
    return ", ";
  }

  /**
   * locale used on case-conversion
   */
  public abstract Locale getLocale();

  private static final LoadingCache<PathsAndLanguage, List<Map<String, SuggestionWithMessage>>> cache = CacheBuilder.newBuilder()
          .expireAfterWrite(30, TimeUnit.MINUTES)
          .build(new CacheLoader<PathsAndLanguage, List<Map<String, SuggestionWithMessage>>>() {
            @Override
            public List<Map<String, SuggestionWithMessage>> load(@NotNull PathsAndLanguage lap) throws IOException {
              return loadWords(lap.paths, lap.lang, lap.caseSensitive, lap.checkingCase);
            }
          });

  public AbstractSimpleReplaceRule2(ResourceBundle messages, Language language) {
    super(messages);
    this.language = Objects.requireNonNull(language);
    super.setCategory(Categories.MISC.getCategory(messages));
  }

  /**
   * If this is set, each replacement pair will have its own rule ID, making rule deactivations more specific.
   * @since 5.1
   */
  public void useSubRuleSpecificIds() {
    subRuleSpecificIds = true;
  }
  
  public CaseSensitivy getCaseSensitivy() {
    return CaseSensitivy.CI;
  }

  /**
   * @return the list of wrong words for which this rule can suggest corrections. The list cannot be modified.
   */
  public List<Map<String, SuggestionWithMessage>> getWrongWords(boolean checkingCase) {
    try {
      boolean caseSen = getCaseSensitivy() == CaseSensitivy.CS || getCaseSensitivy() == CaseSensitivy.CSExceptAtSentenceStart;
      List<URL> filePaths = new ArrayList<>();
      if (getFilePaths() != null) {
        filePaths.addAll(getFilePaths());
      }
      for (String fileName : getFileNames()) {
        filePaths.add(getDataBroker().getFromRulesDirAsUrl(fileName));
      }
      return cache.get(new PathsAndLanguage(filePaths, language, caseSen, checkingCase));
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Load the list of words.
   * Same as {@link AbstractSimpleReplaceRule#loadFromPath} but allows multiple words and a custom message (optional).
   * @param fileUrls the file from classpath to load
   * @return the list of maps containing the error-corrections pairs. The n-th map contains key strings of (n+1) words.
   */
  private static List<Map<String, SuggestionWithMessage>> loadWords(List<URL> fileUrls, Language lang, boolean caseSensitive, boolean checkingCase)
          throws IOException {
    List<Map<String, SuggestionWithMessage>> list = new ArrayList<>();
    for (URL fileUrl : fileUrls) {
      InputStream stream =  fileUrl.openStream();
      try (
        InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr))
      {
        String line;
        int msgCount = 0;
        int lineCount = 0;
        while ((line = br.readLine()) != null) {
          line = line.trim();
          if (line.isEmpty() || line.charAt(0) == '#') { // ignore comments
            continue;
          }
          if (line.contains("  ") && !lang.getShortCode().equals("ar")) {
            throw new RuntimeException("More than one consecutive space in " + fileUrl.toString() + " - use a tab character as a delimiter for the message: " + line);
          }
          if (checkingCase) {
            String[] parts = line.split("=");
            line = parts[0].toLowerCase().trim() + "=" + parts[0].trim();
            if (parts.length == 2) {
              line = line + "\t" + parts[1].trim();
            }
          }
          String[] parts = line.split("\t");
          String confPair = parts[0];
          lineCount++;
          String msg;
          if (parts.length == 1) {
            msg = null;
          } else if (parts.length == 2) {
            msg = parts[1];
            msgCount++;
          } else {
            throw new IOException("Format error in file " + fileUrl.toString()
              + ". Expected at most 1 '=' character and at most 1 tab character. Line: " + line);
          }
          String[] confPairParts = confPair.split("=");
          String[] wrongForms = confPairParts[0].split("\\|"); // multiple incorrect forms
          for (String wrongForm : wrongForms) {
            int wordCountIndex = getWordCountIndex(wrongForm);
            for (int i = list.size(); i < wordCountIndex + 1; i++) {  // grow if necessary
              list.add(new HashMap<>());
            }
            String searchToken = caseSensitive ? wrongForm : wrongForm.toLowerCase();
            if (!checkingCase && searchToken.equals(confPairParts[1])) {
              throw new IOException("Format error in file " + fileUrl.toString()
                + ". Found same word on left and right side of '='. Line: " + line);
            }
            SuggestionWithMessage sugg = new SuggestionWithMessage(confPairParts[1], msg);
            list.get(wordCountIndex).put(searchToken, sugg);
          }
        }
        //System.out.println(msgCount + " of " + lineCount + " have a specific message in " + filename);
      }
    }
    // seal the result (prevent modification from outside this class)
    List<Map<String,SuggestionWithMessage>> result = new ArrayList<>();
    for (Map<String, SuggestionWithMessage> map : list) {
      result.add(Collections.unmodifiableMap(map));
    }
    return Collections.unmodifiableList(result);
  }

  private static Pattern whiteSpacePattern = Pattern.compile("\\s");

  /*
  Use this method to count tokens in a consistent way.
  This count is used to group phrases in a map with different indexes.
  It doesn't match necessarily the number of tokens from the word tokenizer.
   */
  protected static int getWordCountIndex(String str) {
    Matcher matcher = whiteSpacePattern.matcher(str.trim());
    int count = 0;
    while (matcher.find()) {
      count++;
    }
    return count;
  }

  /**
   * Used if each input form the replacement file has its specific id.
   */
  public String getDescription(String details) {
    return null;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    List<Map<String, SuggestionWithMessage>> wrongWords = getWrongWords(false);
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
        SuggestionWithMessage crtMatch;
        String lcOriginalPhrase = originalPhrase.toLowerCase(getLocale());
        int wordCountIndex = getWordCountIndex(lcOriginalPhrase);
        if (wordCountIndex < 0) {
          continue;
        }
        if (getCaseSensitivy() == CaseSensitivy.CSExceptAtSentenceStart && startIndex == sentStart) {  // at sentence start, words can be uppercase
          crtMatch = wrongWords.get(wordCountIndex).get(lcOriginalPhrase);
        } else {
          boolean caseSen = getCaseSensitivy() == CaseSensitivy.CS || getCaseSensitivy() == CaseSensitivy.CSExceptAtSentenceStart;
          crtMatch = caseSen ?
            wrongWords.get(wordCountIndex).get(originalPhrase) :
            wrongWords.get(wordCountIndex).get(lcOriginalPhrase);
        }
        if (crtMatch == null) {
          continue;
        }
        List<String> replacements = Arrays.asList(crtMatch.getSuggestion().split("\\|"));
        String msgSuggestions = "";
        for (int k = 0; k < replacements.size(); k++) {
          if (k > 0) {
            msgSuggestions += (k == replacements.size() - 1 ? getSuggestionsSeparator(): ", ");
          }
          msgSuggestions += "<suggestion>" + replacements.get(k) + "</suggestion>";
        }
        String msg = getMessage().replaceFirst("\\$match", originalPhrase).replaceFirst("\\$suggestions", msgSuggestions);
        if (crtMatch.getMessage() != null) {
          if (!crtMatch.getMessage().startsWith("http://") && !crtMatch.getMessage().startsWith("https://")) {
            msg = crtMatch.getMessage();
          }
        }
        int startPos = tokens[startIndex].getStartPos();
        int endPos = tokens[endIndex].getEndPos();
        RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, msg, getShort());
        if (subRuleSpecificIds) {
          String id = StringTools.toId(getId() + "_" + originalPhrase, language);
          String desc = getDescription().replace("$match", originalPhrase);
          SpecificIdRule specificIdRule = new SpecificIdRule(id, desc, isPremium(), getCategory(), getLocQualityIssueType(), getTags());
          ruleMatch = new RuleMatch(specificIdRule, sentence, startPos, endPos, msg, getShort());
        }
        if (crtMatch.getMessage() != null && (crtMatch.getMessage().startsWith("http://") || crtMatch.getMessage().startsWith("https://"))) {
          ruleMatch.setUrl(Tools.getUrl(crtMatch.getMessage()));
        }
        if ((getCaseSensitivy() != CaseSensitivy.CS || getCaseSensitivy() == CaseSensitivy.CSExceptAtSentenceStart)
             && StringTools.startsWithUppercase(originalPhrase)) {
          //String covered = sentence.getText().substring(startPos, endPos);
          for (int k = 0; k < replacements.size(); k++) {
            String repl = StringTools.uppercaseFirstChar(replacements.get(k));
            replacements.set(k, repl);
            //if (covered.equals(repl)) {
            //  System.err.println("suggestion == original text for '" + covered + "' in AbstractSimpleReplaceRule2");
            //}
          }
        }
        ruleMatch.setSuggestedReplacements(replacements);
        if (!isException(sentence.getText().substring(startPos, endPos))
          && !isRuleMatchException(ruleMatch)) {
          //keep only the longest match
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
    return toRuleMatchArray(ruleMatches);
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

  static class PathsAndLanguage {
    final List<URL> paths;
    final Language lang;
    final boolean caseSensitive;
    final boolean checkingCase;

    PathsAndLanguage(List<URL> filePaths, Language language, boolean caseSensitive, boolean checkingCase) {
      this.paths = Objects.requireNonNull(filePaths);
      this.lang = Objects.requireNonNull(language);
      this.caseSensitive = caseSensitive;
      this.checkingCase = checkingCase;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      PathsAndLanguage that = (PathsAndLanguage) o;
      return paths.equals(that.paths) && lang.equals(that.lang) && caseSensitive == that.caseSensitive;
    }

    @Override
    public int hashCode() {
      return Objects.hash(paths, lang, caseSensitive);
    }
  }

  protected boolean isPunctuationStart(String word) {
    return StringUtils.getDigits(word).length() > 0 // e.g. postal codes
      || StringTools.isPunctuationMark(word) || StringTools.isNotWordCharacter(word);
  }
}
