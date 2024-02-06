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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.openoffice.MessageHandler;
import org.languagetool.openoffice.ResultCache;
import org.languagetool.rules.AbstractStyleTooOftenUsedWordRule;
import org.languagetool.rules.ReadabilityRule;
import org.languagetool.rules.TextLevelRule;

import com.sun.star.linguistic2.SingleProofreadingError;

/**
 * Adapter between LT Rules (instance of AbstractStyleTooOftenUsedWordRule) and Analyzes Dialog
 * @since 6.2
 * @author Fred Kruse
 */
public class UsedWordRule {
  
  private final static int MAX_LIST_LENGTH = 50;

  private boolean debugMode = false;
  
  private final static ResourceBundle MESSAGES = JLanguageTool.getMessageBundle();
  private final TextLevelRule rule;
  private int procentualStep;
  private int optimalNumberWords;
  private boolean withDirectSpeech;
  private List<Map<String, Integer>> wordMapList = new ArrayList<>();
  private List<String> excludedWords = new ArrayList<>();
  private List<WordFrequency> mostUsed = new ArrayList<>();
  private String selectedWord;
  
  public UsedWordRule(TextLevelRule rule, StatAnCache cache) {
    this.rule = rule;
    withDirectSpeech = true;
    procentualStep = getDefaultRuleStep();
    optimalNumberWords = 3 * procentualStep;
  }
  
  public void generateBasicNumbers(StatAnCache cache) {
    try {
      if (rule instanceof AbstractStyleTooOftenUsedWordRule) {
        if (debugMode) {
          MessageHandler.printToLogFile("withDirectSpeech: " + withDirectSpeech);
        }
        String langCode = cache.getDocShortCodeLanguage();
        for (int i = 0; i < cache.size(); i++) {
          if (langCode.equals(cache.getLanguageFlatParagraph(i))) {
            rule.match(cache.getAnalysedParagraph(i), null);
            wordMapList.add(new HashMap<>(((AbstractStyleTooOftenUsedWordRule) rule).getWordMap()));
            if (debugMode) {
              MessageHandler.printToLogFile("Paragraph " + i + ": Number of words: " + wordMapList.get(i).size());
            }
          } else {
            wordMapList.add(new HashMap<>());
          }
        }
        mostUsed = getMostUsed(0, cache.size());
      }
      cache.setNewResultcache(null, null);
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

  private List<WordFrequency> getMostUsed(int from, int to) {
    Map<String, Integer> wordMap = new HashMap<>();
    List<WordFrequency> wordList = new ArrayList<>();
    for (int i = from; i < to; i++) {
      if (debugMode) {
        MessageHandler.printToLogFile("i = " + i + ": Number of words: " + wordMapList.get(i).keySet().size());
      }
      for (String word : wordMapList.get(i).keySet()) {
        if (!excludedWords.contains(word)) {
          if (wordMap.containsKey(word)) {
            int nWords = wordMap.get(word);
            nWords += wordMapList.get(i).get(word);
            wordMap.put(word, nWords);
          } else {
            wordMap.put(word, wordMapList.get(i).get(word));
          }
        }
      }
    }
    if (debugMode) {
      MessageHandler.printToLogFile("Number of words: " + wordMap.size());
    }
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
  
  public void refreshMostUsed(int from, int to) {
    mostUsed = getMostUsed(from, to);
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
  
  public boolean isRelevantParagraph(int nTPara) {
    if(wordMapList.get(nTPara).get(selectedWord) == null) {
      return false;
    }
    return (wordMapList.get(nTPara).get(selectedWord) > 0);
  }

  private int getDefaultRuleStep() {
    int defValue = rule.getDefaultValue();
    int defStep = (int) ((defValue / 3.) + 0.5);
    if (defStep < 1) {
      defStep = 1;
    }
    return defStep;
  }
  
  public int getDefaultStep() {
    int defStep = getDefaultRuleStep();
    if (debugMode) {
      MessageHandler.printToLogFile("default step: " + defStep);
    }
    return defStep;
  }
  
  public void setCurrentStep(int step) {
    if (step > 0) {
      procentualStep = step;
      optimalNumberWords = 3 * procentualStep;
    }
  }
  
  String getUnitString() {
    return "%";
  }
  
  public String getMessageOfLevel(int level) {
    String sLevel = null;
    if (rule instanceof ReadabilityRule) {
      return ((ReadabilityRule) rule).printMessageLevel(level);
    } else {
      int percent = optimalNumberWords + (3 - level) * procentualStep;
      if (level == 0) {
        sLevel = MESSAGES.getString("loStatisticalAnalysisNumber") + ": &gt " + percent + getUnitString();
      } else if (level >= 1 && level <= 5) {
        sLevel = MESSAGES.getString("loStatisticalAnalysisNumber") + ": " + (percent - procentualStep) + " - " + percent + getUnitString();
      } else if (level == 6) {
        sLevel = MESSAGES.getString("loStatisticalAnalysisNumber") + ": 0" + getUnitString();
      }
      return sLevel;
    }
  }

  /**
   * get level of occurrence of filler words(0 - 6)
   */
  protected int getFoundWordsLevel(double percent) throws Throwable {
    if (percent > optimalNumberWords + 2 * procentualStep) {
      return 0;
    } else if (percent > optimalNumberWords + procentualStep) {
      return 1;
    } else if (percent > optimalNumberWords) {
      return 2;
    } else if (percent > optimalNumberWords - procentualStep) {
      return 3;
    } else if (percent > optimalNumberWords - 2 * procentualStep) {
      return 4;
    } else if (percent > optimalNumberWords - 3 * procentualStep) {
      return 5;
    } else {
      return 6;
    }
  }
  
  /**
   * get level of used words (0 - 6)
   */
  public int getLevel(int from, int to) throws Throwable {
    List<WordFrequency> mostUsed = getMostUsed(from, to);
    if (mostUsed.isEmpty()) {
      return 7;
    }
    double percent = 0;
    for (WordFrequency used : mostUsed) {
      if (selectedWord.equals(used.word)) {
        percent = used.percent;
        break;
      }
    }
    return getFoundWordsLevel(percent);
  }
  
  /**
   * set the selected word
   */
  public void setWord(String word) {
    selectedWord = word;
  }
  
  /**
   * set the Cache for one paragraph
   * @throws IOException 
   */  
  public void setCacheForParagraph(int nFPara, int nTPara, StatAnCache cache) {
    ResultCache statAnalysisCache = new ResultCache();
    List<AnalyzedSentence> analyzedSentences = cache.getAnalysedParagraph(nTPara);
    if (analyzedSentences != null) {
      List<SingleProofreadingError> wordMatches = new ArrayList<>();
      int nSentPos = 0;
      for (AnalyzedSentence sentence : analyzedSentences) {
        for (AnalyzedTokenReadings token : sentence.getTokens()) {
          if (token.hasLemma(selectedWord)) {
            wordMatches.add(cache.createLoError(token.getStartPos() + nSentPos, token.getEndPos() - token.getStartPos(), 
                rule.getId(), selectedWord, null));
          }
        }
        nSentPos += sentence.getCorrectedTextLength();
      }
      statAnalysisCache.put(nFPara, wordMatches.toArray(new SingleProofreadingError[wordMatches.size()]));
    }
    cache.setNewResultcache(rule.getId(), statAnalysisCache);
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
