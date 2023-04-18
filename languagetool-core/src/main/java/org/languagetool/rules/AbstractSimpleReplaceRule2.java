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
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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
  @Override
  public abstract String getId();
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
              List<Map<String, SuggestionWithMessage>> maps = new ArrayList<>();
              for (String path : lap.paths) {
                List<Map<String, SuggestionWithMessage>> l = loadWords(path, lap.lang, lap.caseSensitive, lap.checkingCase);
                maps.addAll(l);
              }
              return maps;
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
      return cache.get(new PathsAndLanguage(getFileNames(), language, caseSen, checkingCase));
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Load the list of words.
   * Same as {@link AbstractSimpleReplaceRule#loadFromPath} but allows multiple words and a custom message (optional).
   * @param filename the file from classpath to load
   * @return the list of maps containing the error-corrections pairs. The n-th map contains key strings of (n+1) words.
   */
  private static List<Map<String, SuggestionWithMessage>> loadWords(String filename, Language lang, boolean caseSensitive, boolean checkingCase)
          throws IOException {
    List<Map<String, SuggestionWithMessage>> list = new ArrayList<>();
    InputStream stream = getDataBroker().getFromRulesDirAsStream(filename);
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
          throw new IOException("Format error in file " + getDataBroker().getFromRulesDirAsUrl(filename)
            + ". Expected at most 1 '=' character and at most 1 tab character. Line: " + line);
        }
        String[] confPairParts = confPair.split("=");
        String[] wrongForms = confPairParts[0].split("\\|"); // multiple incorrect forms
        for (String wrongForm : wrongForms) {
          int wordCount = getWordCount(lang, wrongForm);
          for (int i = list.size(); i < wordCount; i++) {  // grow if necessary
            list.add(new HashMap<>());
          }
          String searchToken = caseSensitive ? wrongForm : wrongForm.toLowerCase();
          if (!checkingCase && searchToken.equals(confPairParts[1])) {
            throw new IOException("Format error in file " +  getDataBroker().getFromRulesDirAsUrl(filename)
              + ". Found same word on left and right side of '='. Line: " + line);
          }
          SuggestionWithMessage sugg = new SuggestionWithMessage(confPairParts[1], msg);
          list.get(wordCount - 1).put(searchToken, sugg);
        }
      }
      //System.out.println(msgCount + " of " + lineCount + " have a specific message in " + filename);
    }
    // seal the result (prevent modification from outside this class)
    List<Map<String,SuggestionWithMessage>> result = new ArrayList<>();
    for (Map<String, SuggestionWithMessage> map : list) {
      result.add(Collections.unmodifiableMap(map));
    }
    return Collections.unmodifiableList(result);
  }

  private static int getWordCount(Language lang, String wrongForm) {
    int wordCount = 0;
    List<String> tokens = lang.getWordTokenizer().tokenize(wrongForm);
    for (String token : tokens) {
      if (!StringTools.isWhitespace(token)) {
        wordCount++;
      }
    }
    return wordCount;
  }

  protected void addToQueue(AnalyzedTokenReadings token,
                          Queue<AnalyzedTokenReadings> prevTokens) {
    boolean inserted = prevTokens.offer(token);
    if (!inserted) {
      prevTokens.poll();
      prevTokens.offer(token);
    }
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
    Queue<AnalyzedTokenReadings> prevTokens = new ArrayBlockingQueue<>(wrongWords.size());

    for (int i = 1; i < tokens.length; i++) {
      addToQueue(tokens[i], prevTokens);
      StringBuilder sb = new StringBuilder();
      List<String> variants = new ArrayList<>();
      List<AnalyzedTokenReadings> prevTokensList =
              Arrays.asList(prevTokens.toArray(new AnalyzedTokenReadings[0]));
      for (int j = prevTokensList.size() - 1; j >= 0; j--) {
        if (j != prevTokensList.size() - 1 && prevTokensList.get(j + 1).isWhitespaceBefore()) {
          sb.insert(0, " ");
        }
        sb.insert(0, prevTokensList.get(j).getToken());
        variants.add(0, sb.toString());
      }
      if (isTokenException(tokens[i])) {
        continue;
      }
      int len = variants.size(); // prevTokensList and variants have now the same length
      for (int j = 0; j < len; j++) { // longest words first
        String crt = variants.get(j);
        int crtWordCount = len - j;
        SuggestionWithMessage crtMatch;
        if (getCaseSensitivy() == CaseSensitivy.CSExceptAtSentenceStart && i - crtWordCount == 0) {  // at sentence start, words can be uppercase
          crtMatch = wrongWords.get(crtWordCount - 1).get(crt.toLowerCase(getLocale()));
        } else {
          boolean caseSen = getCaseSensitivy() == CaseSensitivy.CS || getCaseSensitivy() == CaseSensitivy.CSExceptAtSentenceStart;
          crtMatch = caseSen ?
            wrongWords.get(crtWordCount - 1).get(crt) :
            wrongWords.get(crtWordCount - 1).get(crt.toLowerCase(getLocale()));
        }
        if (crtMatch != null) {
          List<String> replacements = Arrays.asList(crtMatch.getSuggestion().split("\\|"));
          String msgSuggestions = "";
          for (int k = 0; k < replacements.size(); k++) {
            if (k > 0) {
              msgSuggestions += (k == replacements.size() - 1 ? getSuggestionsSeparator(): ", ");
            }
            msgSuggestions += "<suggestion>" + replacements.get(k) + "</suggestion>";
          }
          String msg = getMessage().replaceFirst("\\$match", crt).replaceFirst("\\$suggestions", msgSuggestions);
          if (crtMatch.getMessage() != null) {
            if (!crtMatch.getMessage().startsWith("http://") && !crtMatch.getMessage().startsWith("https://")) {
              msg = crtMatch.getMessage();
            }
          }
          int startPos = prevTokensList.get(len - crtWordCount).getStartPos();
          int endPos = prevTokensList.get(len - 1).getEndPos();
          RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, msg, getShort());
          if (crtMatch.getMessage() != null && (crtMatch.getMessage().startsWith("http://") || crtMatch.getMessage().startsWith("https://"))) {
            ruleMatch.setUrl(Tools.getUrl(crtMatch.getMessage()));
          }
          if (subRuleSpecificIds) {
            ruleMatch.setSpecificRuleId(StringTools.toId(getId() + "_" + crt));
          }
          if ((getCaseSensitivy() != CaseSensitivy.CS || getCaseSensitivy() == CaseSensitivy.CSExceptAtSentenceStart)
               && StringTools.startsWithUppercase(crt)) {
            for (int k = 0; k < replacements.size(); k++) {
              replacements.set(k, StringTools.uppercaseFirstChar(replacements.get(k)));
            }
          }
          ruleMatch.setSuggestedReplacements(replacements);
          if (!isException(sentence.getText().substring(startPos, endPos))) {
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
    }
    return toRuleMatchArray(ruleMatches);
  }

  protected boolean isException(String matchedText) {
    return false;
  }
  
  protected boolean isTokenException(AnalyzedTokenReadings atr) {
    return false;
  }

  static class PathsAndLanguage {
    final List<String> paths;
    final Language lang;
    final boolean caseSensitive;
    final boolean checkingCase;

    PathsAndLanguage(List<String> fileNames, Language language, boolean caseSensitive, boolean checkingCase) {
      this.paths = Objects.requireNonNull(fileNames);
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

}
