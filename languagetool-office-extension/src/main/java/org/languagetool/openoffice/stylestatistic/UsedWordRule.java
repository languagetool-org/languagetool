/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.openoffice.stylestatistic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.languagetool.AnalyzedSentence;
import org.languagetool.openoffice.MessageHandler;
import org.languagetool.rules.AbstractStyleTooOftenUsedWordRule;
import org.languagetool.rules.TextLevelRule;

/**
 * Adapter between LT Rules (instance of AbstractStyleTooOftenUsedWordRule) and Analyzes Dialog
 * @since 6.2
 * @author Fred Kruse
 */
public class UsedWordRule {
  
  private final static int MAX_LIST_LENGTH = 50;

  private boolean debugMode = false;
  
  private final TextLevelRule rule;
  private boolean withDirectSpeech;
//  private Map<String, Integer> wordMap;
  private List<String> excludedWords = new ArrayList<>();
  private List<WordFrequency> mostUsed = new ArrayList<>();
  
  public UsedWordRule(TextLevelRule rule, StatAnCache cache) {
    this.rule = rule;
    withDirectSpeech = true;
  }
  
  public void generateBasicNumbers(StatAnCache cache) {
    try {
      if (debugMode) {
        MessageHandler.printToLogFile("withDirectSpeech: " + withDirectSpeech);
      }
      List<AnalyzedSentence> sentences = new ArrayList<>();
      for (int i = 0; i < cache.size(); i++) {
        sentences.addAll(cache.getAnalysedParagraph(i));
      }
      //  TODO: Generate a result cache for later evaluations
      rule.match(sentences, null);
      if (rule instanceof AbstractStyleTooOftenUsedWordRule) {
        Map<String, Integer> wordMap = ((AbstractStyleTooOftenUsedWordRule) rule).getWordMap();
        if (debugMode) {
          MessageHandler.printToLogFile("Number of words: " + wordMap.size());
        }
        mostUsed = getMostUsed(wordMap);
      }
    } catch (IOException e) {
      MessageHandler.showError(e);
    }
  }

  public void setWithDirectSpeach(boolean wDirectSpeech, StatAnCache cache) {
    if (debugMode) {
      MessageHandler.printToLogFile("withDirectSpeech: " + withDirectSpeech + ", wDirectSpeech: " + wDirectSpeech);
    }
    if (withDirectSpeech != wDirectSpeech) {
      withDirectSpeech = wDirectSpeech;
      ((AbstractStyleTooOftenUsedWordRule) rule).setWithoutDirectSpeech(!withDirectSpeech);
      if (debugMode) {
        MessageHandler.printToLogFile("Generate basic numbers");
      }
      generateBasicNumbers(cache);
    }
  }
  
  public void setListExcludedWords(List<String> words) {
    excludedWords.clear();
    if (words != null) {
      excludedWords.addAll(words);
    }
  }
  
  public boolean getDefaultDirectSpeach() {
    return true;
  }

  public static boolean isUsedWordRule(TextLevelRule rule) {
    if (rule instanceof AbstractStyleTooOftenUsedWordRule) {
      return true;
    }
    return false;
  }

  private List<WordFrequency> getMostUsed(Map<String, Integer> wordMap) {
    List<WordFrequency> wordList = new ArrayList<>();
    int nWords = 0;
    for (String word : wordMap.keySet()) {
      nWords += wordMap.get(word);
    }
    if (debugMode) {
      MessageHandler.printToLogFile("Number of words: " + nWords);
    }
    if (nWords < 1) {
      return wordList;
    }
    Set<String> words = new HashSet<>(wordMap.keySet());
    int limit = MAX_LIST_LENGTH <= words.size() ? MAX_LIST_LENGTH : words.size();
    for (int i = 0; i < limit; i++) {
      String mostUsed = null;
      int num = 0;
      for (String word : words) {
        int wordNum = wordMap.get(word);
        if (wordNum > num) {
          mostUsed = word;
          num = wordNum;
        }
      }
      double percent = ((double) num) * 100. / ((double) nWords);
      wordList.add(new WordFrequency(mostUsed, percent));
      words.remove(mostUsed);
    }
    return wordList;
  }
  
  public String[] getMostUsedWords() throws Throwable {
    String[] words = new String[mostUsed.size()];
    for (int i = 0; i < mostUsed.size(); i++) {
      words[i] = String.format("%s (%.1f%%)", mostUsed.get(i).word, mostUsed.get(i).percent);
    }
    return words;
  }

  public String getMostUsedWord(int n) throws Throwable {
    return mostUsed.get(n).word;
  }
  
  public class WordFrequency {
    public String word;
    public double percent;
    
    WordFrequency(String word, double percent) {
      this.word = word;
      this.percent = percent;
    }
  }

}
