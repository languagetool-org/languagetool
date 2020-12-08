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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.apache.commons.lang3.StringUtils;
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
 * A rule that checks case in phrases
 * 
 * @author Jaume Ortolà
 */
public abstract class AbstractCheckCaseRule extends Rule {

  private final Language language;
  private boolean subRuleSpecificIds;

  public abstract List<String> getFileNames();

  @Override
  public abstract String getId();

  @Override
  public abstract String getDescription();

  public abstract String getShort();

  public abstract String getMessage();

  public abstract Locale getLocale();

  private static final LoadingCache<PathsAndLanguage, List<Map<String, String>>> cache = CacheBuilder.newBuilder()
      .expireAfterWrite(30, TimeUnit.MINUTES).build(new CacheLoader<PathsAndLanguage, List<Map<String, String>>>() {
        @Override
        public List<Map<String, String>> load(@NotNull PathsAndLanguage lap) throws IOException {
          List<Map<String, String>> lists = new ArrayList<>();
          for (String path : lap.paths) {
            List<Map<String, String>> l = loadWords(path, lap.lang);
            lists.addAll(l);
          }
          return lists;
        }
      });

  public AbstractCheckCaseRule(ResourceBundle messages, Language language) {
    super(messages);
    this.language = Objects.requireNonNull(language);
    super.setLocQualityIssueType(ITSIssueType.Typographical);
    super.setCategory(Categories.CASING.getCategory(messages));
  }

  /**
   * If this is set, each replacement pair will have its own rule ID, making rule
   * deactivations more specific.
   * 
   * @since 5.1
   */
  public void useSubRuleSpecificIds() {
    subRuleSpecificIds = true;
  }

  /**
   * @return the list of wrong words for which this rule can suggest corrections.
   *         The list cannot be modified.
   */
  public List<Map<String, String>> getWrongWords() {
    try {
      return cache.get(new PathsAndLanguage(getFileNames(), language));
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Load the list of words. Same as
   * {@link AbstractSimpleReplaceRule#loadFromPath} but allows multiple words and
   * a custom message (optional).
   * 
   * @param filename the file from classpath to load
   * @return the list of maps containing the error-corrections pairs. The n-th map
   *         contains key strings of (n+1) words.
   */
  private static List<Map<String, String>> loadWords(String filename, Language lang) throws IOException {
    List<Map<String, String>> list = new ArrayList<>();
    InputStream stream = JLanguageTool.getDataBroker().getFromRulesDirAsStream(filename);
    try (InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr)) {
      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.charAt(0) == '#') { // ignore comments
          continue;
        }
        int wordCount = 0;
        List<String> tokens = lang.getWordTokenizer().tokenize(line);
        for (String token : tokens) {
          if (!StringTools.isWhitespace(token)) {
            wordCount++;
          }
        }
        // grow if necessary
        for (int i = list.size(); i < wordCount; i++) {
          list.add(new HashMap<>());
        }
        list.get(wordCount - 1).put(line.toLowerCase(), line);
      }
    }
    // seal the result (prevent modification from outside this class)
    List<Map<String, String>> result = new ArrayList<>();
    for (Map<String, String> map : list) {
      result.add(Collections.unmodifiableMap(map));
    }
    return Collections.unmodifiableList(result);
  }

  private void addToQueue(AnalyzedTokenReadings token, Queue<AnalyzedTokenReadings> prevTokens) {
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
        String correctPhrase = wrongWords.get(crtWordCount - 1).get(originalPhrase.toLowerCase(getLocale()));

        String capitalizedCorrect = StringTools.uppercaseFirstChar(correctPhrase);
        if (crtWordCount + sentStart == i && originalPhrase.equals(capitalizedCorrect)) {
          continue;
        }

        if (correctPhrase != null && !correctPhrase.equals(originalPhrase)) {
          // the rule matches
          int startPos = prevTokensList.get(len - crtWordCount).getStartPos();
          int endPos = prevTokensList.get(len - 1).getEndPos();
          RuleMatch ruleMatch;
          if (subRuleSpecificIds) {
            String id = StringTools.toId(getId() + "_" + correctPhrase);
            ruleMatch = new RuleMatch(new SpecificIdRule(id, getDescription(), messages), sentence, startPos, endPos,
                getMessage(), getShort());
          } else {
            ruleMatch = new RuleMatch(this, sentence, startPos, endPos, getMessage(), getShort());
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

  protected boolean isException(String matchedText) {
    return false;
  }

  protected boolean isTokenException(AnalyzedTokenReadings atr) {
    return false;
  }

  private boolean isPunctuationStart(String word) {
    return StringUtils.equalsAny(word, "\"", "'", "„", "»", "«", "“", "‘", "¡", "¿", "-", "–", "—", "―", "‒");
  }

  static class PathsAndLanguage {
    final List<String> paths;
    final Language lang;

    PathsAndLanguage(List<String> fileNames, Language language) {
      this.paths = Objects.requireNonNull(fileNames);
      this.lang = Objects.requireNonNull(language);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      PathsAndLanguage that = (PathsAndLanguage) o;
      return paths.equals(that.paths) && lang.equals(that.lang);
    }

    @Override
    public int hashCode() {
      return Objects.hash(paths, lang);
    }
  }

  static class SpecificIdRule extends AbstractSimpleReplaceRule {
    private final String id;
    private final String desc;

    SpecificIdRule(String id, String desc, ResourceBundle messages) {
      super(messages);
      this.id = Objects.requireNonNull(id);
      this.desc = desc;
    }

    @Override
    protected Map<String, List<String>> getWrongWords() {
      throw new RuntimeException("not implemented");
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public String getDescription() {
      return desc;
    }
  }

}
