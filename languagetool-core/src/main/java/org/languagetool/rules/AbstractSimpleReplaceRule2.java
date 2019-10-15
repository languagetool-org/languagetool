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
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.tools.StringTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * A rule that matches words which should not be used and suggests correct ones instead. 
 * Loads the list of words from <code>/xx/replace.txt</code>.
 *
 * <p>Unlike AbstractSimpleReplaceRule, supports phrases (Ex: "aqua forte" -&gt; "acvaforte").
 *
 * Note: Merge this into {@link AbstractSimpleReplaceRule}
 *
 * @author Ionuț Păduraru
 */
public abstract class AbstractSimpleReplaceRule2 extends Rule {

  private final Language language;

  public abstract String getFileName();
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
  public abstract String getSuggestion();
  /**
   * @return the word used to separate multiple suggestions; used only before last suggestion, the rest are comma-separated.  
   */
  public abstract String getSuggestionsSeparator();
  /**
   * locale used on case-conversion
   */
  public abstract Locale getLocale();

  private static final LoadingCache<PathAndLanguage, List<Map<String, String>>> cache = CacheBuilder.newBuilder()
          .expireAfterWrite(30, TimeUnit.MINUTES)
          .build(new CacheLoader<PathAndLanguage, List<Map<String, String>>>() {
            @Override
            public List<Map<String, String>> load(@NotNull PathAndLanguage lap) throws IOException {
              return loadWords(lap.path, lap.lang);
            }
          });

  public AbstractSimpleReplaceRule2(ResourceBundle messages, Language language) {
    super(messages);
    this.language = Objects.requireNonNull(language);
    super.setCategory(Categories.MISC.getCategory(messages));
  }

  /**
   * use case-insensitive matching.
   */
  public boolean isCaseSensitive() {
    return false;
  }

  /**
   * @return the list of wrong words for which this rule can suggest correction. The list cannot be modified.
   */
  public List<Map<String, String>> getWrongWords() {
    try {
      return cache.get(new PathAndLanguage(getFileName(), language));
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Load the list of words.
   * Same as {@link AbstractSimpleReplaceRule#loadFromPath} but allows multiple words.   
   * @param filename the file from classpath to load
   * @return the list of maps containing the error-corrections pairs. The n-th map contains key strings of (n+1) words.
   */
  private static List<Map<String, String>> loadWords(String filename, Language lang)
          throws IOException {
    List<Map<String, String>> list = new ArrayList<>();
    InputStream stream = JLanguageTool.getDataBroker().getFromRulesDirAsStream(filename);
    try (
      InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8);
      BufferedReader br = new BufferedReader(isr)) 
    {
      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.charAt(0) == '#') { // ignore comments
          continue;
        }

        String[] parts = line.split("=");
        if (parts.length != 2) {
          throw new IOException("Format error in file "
                  + JLanguageTool.getDataBroker().getFromRulesDirAsUrl(filename)
                  + ". Expected exactly 1 '=' character. Line: " + line);
        }

        String[] wrongForms = parts[0].split("\\|"); // multiple incorrect forms
        for (String wrongForm : wrongForms) {
          int wordCount = 0;
          List<String> tokens = lang.getWordTokenizer().tokenize(wrongForm);
          for (String token : tokens) {
            if (!StringTools.isWhitespace(token)) {
              wordCount++;
            }
          }
          // grow if necessary
          for (int i = list.size(); i < wordCount; i++) {
            list.add(new HashMap<>());
          }
          list.get(wordCount - 1).put(wrongForm, parts[1]);
        }
      }
    }
    // seal the result (prevent modification from outside this class)
    List<Map<String,String>> result = new ArrayList<>();
    for (Map<String, String> map : list) {
      result.add(Collections.unmodifiableMap(map));
    }
    return Collections.unmodifiableList(result);
  }

  private void addToQueue(AnalyzedTokenReadings token,
                          Queue<AnalyzedTokenReadings> prevTokens) {
    boolean inserted = prevTokens.offer(token);
    if (!inserted) {
      prevTokens.poll();
      prevTokens.offer(token);
    }
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    List<Map<String, String>> wrongWords = getWrongWords();
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
      int len = variants.size(); // prevTokensList and variants have now the same length
      for (int j = 0; j < len; j++) { // longest words first
        String crt = variants.get(j);
        int crtWordCount = len - j;
        String crtMatch = isCaseSensitive() ? wrongWords.get(crtWordCount - 1).get(crt) : wrongWords.get(crtWordCount- 1).get(crt.toLowerCase(getLocale()));
        if (crtMatch != null) {
          List<String> replacements = Arrays.asList(crtMatch.split("\\|"));
          String msgSuggestions = "";
          for (int k = 0; k < replacements.size(); k++) {
            if (k > 0) {
              msgSuggestions += (k == replacements.size() - 1 ? getSuggestionsSeparator(): ", ");
            }
            msgSuggestions += "<suggestion>" + replacements.get(k) + "</suggestion>";
          }
          String msg = getSuggestion().replaceFirst("\\$match", crt).replaceFirst("\\$suggestions", msgSuggestions);
          int startPos = prevTokensList.get(len - crtWordCount).getStartPos();
          int endPos = prevTokensList.get(len - 1).getEndPos();
          RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, msg, getShort());
          if (!isCaseSensitive() && StringTools.startsWithUppercase(crt)) {
            for (int k = 0; k < replacements.size(); k++) {
              replacements.set(k, StringTools.uppercaseFirstChar(replacements.get(k)));
            }
          }
          ruleMatch.setSuggestedReplacements(replacements);
          if (!isException(sentence.getText().substring(startPos, endPos))) {
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

  class PathAndLanguage {
    final String path;
    final Language lang;
    PathAndLanguage(String fileName, Language language) {
      this.path = Objects.requireNonNull(fileName);
      this.lang = Objects.requireNonNull(language);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      PathAndLanguage that = (PathAndLanguage) o;
      return path.equals(that.path) && lang.equals(that.lang);
    }

    @Override
    public int hashCode() {
      return Objects.hash(path, lang);
    }
  }
}
