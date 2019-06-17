/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Markus Brenneis
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

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;

/**
 * Check if there is a confusion of two words (which might have a similar spelling) depending on the context.
 * This rule loads the words and their respective context from an external file with the following 
 * format (tab-separated):
 * 
 * <pre>word1 word2 match1 match2 context1 context2 explanation1 explanation2</pre>
 * 
 * <ul>
 * <li>word1 and word2 are regular expressions of the words that can easily be confused
 * <li>match1 is the substring of word1 that will be replaced with match2 when word1 occurs in a context where it is probably wrong (and vice versa) 
 * <li>context1 is the context in which word1 typically occurs (but which is wrong for word2), given as a regular expression
 * <li>context2 is the context in which word2 typically occurs (but which is wrong for word1), given as a regular expression
 * <li>explanation1 is a short description of word1 (optional)
 * <li>explanation2 is a short description of word2 (optional)
 * </ul>
 * 
 * @author Markus Brenneis
 */
public abstract class WrongWordInContextRule extends Rule {

  private static final LoadingCache<String, List<ContextWords>> cache = CacheBuilder.newBuilder()
          .expireAfterWrite(30, TimeUnit.MINUTES)
          .build(new CacheLoader<String, List<ContextWords>>() {
            @Override
            public List<ContextWords> load(@NotNull String path) {
              return loadContextWords(path);
            }
          });

  private final List<ContextWords> contextWordsSet;
  
  private boolean matchLemmas = false;

  public WrongWordInContextRule(ResourceBundle messages) {
    super.setCategory(new Category(CategoryIds.CONFUSED_WORDS, getCategoryString()));
    contextWordsSet = cache.getUnchecked(getFilename());
    setLocQualityIssueType(ITSIssueType.Misspelling);
  }

  protected abstract String getFilename();

  protected String getCategoryString() {
    return messages.getString("category_misc");
  }
  
  @Override
  public String getId() {
    return "WRONG_WORD_IN_CONTEXT";
  }

  @Override
  public String getDescription() {
    return "Confusion of words";
  }
  
  /*
   *  Match lemmas instead of word forms
   */
  public void setMatchLemmmas() {
    matchLemmas = true;
  }
  
  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    for (ContextWords contextWords : contextWordsSet) {
      boolean[] matchedWord = {false, false};
      Matcher[] matchers = {null, null};
      matchers[0] = contextWords.words[0].matcher("");
      matchers[1] = contextWords.words[1].matcher("");
      //start searching for words
      //ignoring token 0, i.e., SENT_START
      int i;
      String token1 = "";
      for (i = 1; i < tokens.length && !matchedWord[0]; i++) {
        token1 = tokens[i].getToken();
        matchedWord[0] = matchers[0].reset(token1).find();
      }
      int j;
      String token2 = "";
      for (j = 1; j < tokens.length && !matchedWord[1]; j++) {
        token2 = tokens[j].getToken();
        matchedWord[1] = matchers[1].reset(token2).find();
      }
      
      int foundWord = -1;
      int notFoundWord = -1;
      int startPos = 0;
      int endPos = 0;
      String matchedToken = "";
      // when both words have been found, we cannot determine if one of them is wrong
      if (matchedWord[0] && !matchedWord[1]) {
        foundWord = 0;
        notFoundWord = 1;
        matchers[1] = contextWords.contexts[1].matcher("");
        startPos = tokens[i-1].getStartPos();
        endPos = tokens[i-1].getStartPos() + token1.length();
        matchedToken = token1;
      } else if (matchedWord[1] && !matchedWord[0]) {
        foundWord = 1;
        notFoundWord = 0;
        matchers[0] = contextWords.contexts[0].matcher("");
        startPos = tokens[j-1].getStartPos();
        endPos = tokens[j-1].getStartPos() + token2.length();
        matchedToken = token2;
      }
      
      if (foundWord != -1) {
        boolean[] matchedContext = {false, false};
        matchers[foundWord] = contextWords.contexts[foundWord].matcher("");
        matchers[notFoundWord] = contextWords.contexts[notFoundWord].matcher("");
        //start searching for context words
        //ignoring token 0, i.e., SENT_START
        String token;
        for (i = 1; i < tokens.length && !matchedContext[foundWord]; i++) {
          if (matchLemmas) {
            for (j = 0; j < tokens[i].getReadingsLength() && !matchedContext[foundWord]; j++) {
              String lemma = tokens[i].getAnalyzedToken(j).getLemma();
              if (lemma != null && !lemma.isEmpty()) {
                matchedContext[foundWord] = matchers[foundWord].reset(lemma).find();
              }
            }
          } else {
            token = tokens[i].getToken();
            matchedContext[foundWord] = matchers[foundWord].reset(token).find();
          }
        }
        for (i = 1; i < tokens.length && !matchedContext[notFoundWord]; i++) {
          if (matchLemmas) {
            for (j = 0; j < tokens[i].getReadingsLength() && !matchedContext[notFoundWord]; j++) {
              String lemma = tokens[i].getAnalyzedToken(j).getLemma();
              if (lemma != null && !lemma.isEmpty()) {
                matchedContext[notFoundWord] = matchers[notFoundWord].reset(lemma).find();
              }
            }
          } else {
            token = tokens[i].getToken();
            matchedContext[notFoundWord] = matchers[notFoundWord].reset(token).find();
          }
        }
        if (matchedContext[notFoundWord] && !matchedContext[foundWord]) {
          String msg = getMessage(matchedToken, matchedToken.replaceFirst(contextWords.matches[foundWord],contextWords.matches[notFoundWord]),
                  contextWords.explanations[notFoundWord], contextWords.explanations[foundWord]);
          RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, msg, getShortMessageString());
          ruleMatches.add(ruleMatch);
        }
      } // if foundWord != -1
    } // for each contextWords in contextWordsSet
    return toRuleMatchArray(ruleMatches);
  }
  
  /**
   * @return a string like "Possible confusion of words: Did you mean &lt;suggestion&gt;$SUGGESTION&lt;/suggestion&gt; instead of '$WRONGWORD'?"
   */
  protected abstract String getMessageString();
  
  /**
   * @return a string like "Possible confusion of words"
   */
  protected abstract String getShortMessageString();
  
  /**
   * @return a string like "Possible confusion of words: Did you mean &lt;suggestion&gt;$SUGGESTION&lt;/suggestion&gt;
   * (= $EXPLANATION_SUGGESTION) instead of '$WRONGWORD' (= $EXPLANATION_WRONGWORD)?"
   */
  protected abstract String getLongMessageString();
  
  private String getMessage(String wrongWord, String suggestion, String explanationSuggestion, String explanationWrongWord) {
    String quotedSuggestion = Matcher.quoteReplacement(suggestion);
    String quotedWrongWord = Matcher.quoteReplacement(wrongWord);
    String quotedExplanationSuggestion = Matcher.quoteReplacement(explanationSuggestion);
    String quotedExplanationWrongWord = Matcher.quoteReplacement(explanationWrongWord);
    if (explanationSuggestion.isEmpty() || explanationWrongWord.isEmpty()) {
      return getMessageString()
        .replaceFirst("\\$SUGGESTION", quotedSuggestion)
        .replaceFirst("\\$WRONGWORD", quotedWrongWord);
    } else {
      return getLongMessageString()
        .replaceFirst("\\$SUGGESTION", quotedSuggestion)
        .replaceFirst("\\$WRONGWORD", quotedWrongWord)
        .replaceFirst("\\$EXPLANATION_SUGGESTION", quotedExplanationSuggestion)
        .replaceFirst("\\$EXPLANATION_WRONGWORD", quotedExplanationWrongWord);
    }
  }
  
  /**
   * Load words, contexts, and explanations.
   */
  private static List<ContextWords> loadContextWords(String path) {
    List<ContextWords> set = new ArrayList<>();
    InputStream stream = JLanguageTool.getDataBroker().getFromRulesDirAsStream(path);
    try (Scanner scanner = new Scanner(stream, "utf-8")) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.trim().isEmpty() || line.charAt(0) == '#') {
          continue;
        }
        String[] column = line.split("\t");
        if (column.length >= 6) {
          ContextWords contextWords = new ContextWords();
          contextWords.setWord(0, column[0]);
          contextWords.setWord(1, column[1]);
          contextWords.matches[0] = column[2];
          contextWords.matches[1] = column[3];
          contextWords.setContext(0, column[4]);
          contextWords.setContext(1, column[5]);
          if (column.length > 6) {
            contextWords.explanations[0] = column[6];
            if (column.length > 7) {
              contextWords.explanations[1] = column[7];
            }
          }
          set.add(contextWords);
        } // if (column.length >= 6)
      }
    }
    return Collections.unmodifiableList(set);
  }
  
  static class ContextWords {
    
    final String[] matches = {"", ""};
    final String[] explanations = {"", ""};
    final Pattern[] words;
    final Pattern[] contexts;
    
    ContextWords() {
      words = new Pattern[2];
      contexts = new Pattern[2];
    }
    
    private String addBoundaries(String str) {
      String ignoreCase = "";
      if (str.startsWith("(?i)")) {
        str = str.substring(4);
        ignoreCase = "(?i)";
      }
      return ignoreCase + "\\b(" + str + ")\\b";
    }
    
    void setWord(int i, String word) {
      words[i] = Pattern.compile(addBoundaries(word));
    }
    
    void setContext(int i, String context) {
      contexts[i] = Pattern.compile(addBoundaries(context));
    }
    
  }

}
