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
import org.languagetool.Language;
import org.languagetool.tools.StringTools;

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
  private final Language lang;

  private boolean matchLemmas = false;

  public WrongWordInContextRule(ResourceBundle messages, Language lang) {
    super.setCategory(new Category(CategoryIds.CONFUSED_WORDS, getCategoryString()));
    contextWordsSet = cache.getUnchecked(getFilename());
    setLocQualityIssueType(ITSIssueType.Misspelling);
    this.lang = lang;
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
    String sentenceLower = sentence.getText().toLowerCase(Locale.ROOT);
    for (ContextWords contextWords : contextWordsSet) {
      if (!contextWords.couldMatchSentence(sentenceLower)) {
        continue;
      }
      boolean[] matchedWord = {false, false};
      Matcher[] matchers = {contextWords.words[0].matcher(""), contextWords.words[1].matcher("")};
      int matchedPos0 = -1, matchedPos1 = -1;
      String token1 = "", token2 = "";
      // search for word1 and word2 in a single pass, ignoring token 0 (SENT_START)
      for (int k = 1; k < tokens.length && (!matchedWord[0] || !matchedWord[1]); k++) {
        if (!tokens[k].hasPartialPosTag("IS_URL")) {
          String t = tokens[k].getToken();
          if (!matchedWord[0] && matchers[0].reset(t).find()) {
            matchedWord[0] = true;
            matchedPos0 = k;
            token1 = t;
          }
          if (!matchedWord[1] && matchers[1].reset(t).find()) {
            matchedWord[1] = true;
            matchedPos1 = k;
            token2 = t;
          }
        }
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
        startPos = tokens[matchedPos0].getStartPos();
        endPos = tokens[matchedPos0].getStartPos() + token1.length();
        matchedToken = token1;
      } else if (matchedWord[1] && !matchedWord[0]) {
        foundWord = 1;
        notFoundWord = 0;
        startPos = tokens[matchedPos1].getStartPos();
        endPos = tokens[matchedPos1].getStartPos() + token2.length();
        matchedToken = token2;
      }

      if (foundWord != -1) {
        boolean[] matchedContext = {false, false};
        matchers[foundWord] = contextWords.contexts[foundWord].matcher("");
        matchers[notFoundWord] = contextWords.contexts[notFoundWord].matcher("");
        // search for both context patterns in a single pass, ignoring token 0 (SENT_START)
        for (int k = 1; k < tokens.length && (!matchedContext[foundWord] || !matchedContext[notFoundWord]); k++) {
          if (matchLemmas) {
            for (int j = 0; j < tokens[k].getReadingsLength() && (!matchedContext[foundWord] || !matchedContext[notFoundWord]); j++) {
              String lemma = tokens[k].getAnalyzedToken(j).getLemma();
              if (lemma != null && !lemma.isEmpty()) {
                if (!matchedContext[foundWord]) {
                  matchedContext[foundWord] = matchers[foundWord].reset(lemma).find();
                }
                if (!matchedContext[notFoundWord]) {
                  matchedContext[notFoundWord] = matchers[notFoundWord].reset(lemma).find();
                }
              }
            }
          } else {
            String token = tokens[k].getToken();
            if (!matchedContext[foundWord]) {
              matchedContext[foundWord] = matchers[foundWord].reset(token).find();
            }
            if (!matchedContext[notFoundWord]) {
              matchedContext[notFoundWord] = matchers[notFoundWord].reset(token).find();
            }
          }
        }
        if (matchedContext[notFoundWord] && !matchedContext[foundWord]) {
          String originalStr = contextWords.matches[foundWord];
          String replacementStr = contextWords.matches[notFoundWord];
          String repl = StringTools.preserveCase(matchedToken.replaceFirst("(?i)" + originalStr, replacementStr)
            , matchedToken);
          String msg = getMessage(matchedToken, repl, contextWords.explanations[notFoundWord], contextWords.explanations[foundWord]);
          String id = StringTools.toId(getId() + "_" + matchedToken + "_" + repl, lang);
          String desc = getDescription().replace("$match", matchedToken + "/" + repl);
          SpecificIdRule specificIdRule = new SpecificIdRule(id, desc, isPremium(), getCategory(), getLocQualityIssueType(), getTags());
          RuleMatch ruleMatch = new RuleMatch(specificIdRule, sentence, startPos, endPos, msg, getShortMessageString());
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

    private static final int MIN_PREFIX_LENGTH = 3;

    final String[] matches = {"", ""};
    final String[] explanations = {"", ""};
    final Pattern[] words;
    final Pattern[] contexts;
    final String[] wordPrefixes = {"", ""};

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
      String pattern = word;
      if (pattern.startsWith("(?i)")) {
        pattern = pattern.substring(4);
      } else if (pattern.startsWith("(?-i)")) {
        pattern = pattern.substring(5);
      }
      wordPrefixes[i] = extractWordPrefix(pattern);
    }

    void setContext(int i, String context) {
      contexts[i] = Pattern.compile(addBoundaries(context));
    }

    boolean couldMatchSentence(String sentenceLower) {
      boolean word0Could = wordPrefixes[0].isEmpty() || sentenceLower.contains(wordPrefixes[0]);
      boolean word1Could = wordPrefixes[1].isEmpty() || sentenceLower.contains(wordPrefixes[1]);
      return word0Could || word1Could;
    }

    private static String extractWordPrefix(String pattern) {
      List<String> alternatives = splitTopLevelAlternatives(pattern);
      List<String> prefixes = new ArrayList<>();
      for (String alt : alternatives) {
        String p = extractSimplePrefix(alt);
        if (p.length() < MIN_PREFIX_LENGTH) {
          return "";
        }
        prefixes.add(p.toLowerCase(Locale.ROOT));
      }
      if (prefixes.isEmpty()) {
        return "";
      }
      String common = prefixes.get(0);
      for (int i = 1; i < prefixes.size(); i++) {
        common = commonPrefix(common, prefixes.get(i));
        if (common.length() < MIN_PREFIX_LENGTH) {
          return "";
        }
      }
      return common;
    }

    private static List<String> splitTopLevelAlternatives(String pattern) {
      List<String> parts = new ArrayList<>();
      int depth = 0;
      int start = 0;
      for (int i = 0; i < pattern.length(); i++) {
        char c = pattern.charAt(i);
        if (c == '\\') { i++; continue; }
        if (c == '(' || c == '[') depth++;
        else if (c == ')' || c == ']') depth--;
        else if (c == '|' && depth == 0) {
          parts.add(pattern.substring(start, i));
          start = i + 1;
        }
      }
      parts.add(pattern.substring(start));
      return parts;
    }

    private static String extractSimplePrefix(String alt) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < alt.length(); i++) {
        char c = alt.charAt(i);
        if (c == '\\' && i + 1 < alt.length()) {
          i++;
          char escaped = alt.charAt(i);
          if (i + 1 < alt.length() && (alt.charAt(i + 1) == '?' || alt.charAt(i + 1) == '*')) {
            break; // escaped char is optional
          }
          sb.append(escaped);
          if (i + 1 < alt.length() && alt.charAt(i + 1) == '+') {
            i++; // skip quantifier
          }
        } else if ("[(|.^$\\{".indexOf(c) >= 0) {
          break;
        } else if (c == '?' || c == '*') {
          if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1); // preceding char was optional
          break;
        } else if (c == '+') {
          break;
        } else {
          if (i + 1 < alt.length() && (alt.charAt(i + 1) == '?' || alt.charAt(i + 1) == '*')) {
            break; // this char is optional
          }
          sb.append(c);
        }
      }
      return sb.toString();
    }

    private static String commonPrefix(String a, String b) {
      int len = Math.min(a.length(), b.length());
      for (int i = 0; i < len; i++) {
        if (a.charAt(i) != b.charAt(i)) return a.substring(0, i);
      }
      return a.substring(0, len);
    }

  }

}
