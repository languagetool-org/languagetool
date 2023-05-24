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
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.openoffice.MessageHandler;
import org.languagetool.rules.AbstractStatisticSentenceStyleRule;
import org.languagetool.rules.AbstractStatisticStyleRule;
import org.languagetool.rules.ReadabilityRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.TextLevelRule;

/**
 * Adapter between LT Rules and Analyzes Dialog 
 * @since 6.2
 * @author Fred Kruse
 */
public class LevelRule {

  private boolean debugMode = false;
  
  private final static ResourceBundle MESSAGES = JLanguageTool.getMessageBundle();
  private final TextLevelRule rule;
  private boolean withDirectSpeech;
  private int procentualStep;
  private int optimalNumberWords;
  private List<Integer> numSyllables = new ArrayList<>();
  private List<Integer> numFound = new ArrayList<>();
  private List<Integer> numBase = new ArrayList<>();
  private double unitFactor;
  
  public LevelRule (TextLevelRule rule, StatAnCache cache) {
    this.rule = rule;
    withDirectSpeech = true;
    procentualStep = getDefaultRuleStep();
    optimalNumberWords = 3 * procentualStep;
    unitFactor = getUnitFactor();
  }
  
  public void generateBasicNumbers(StatAnCache cache) {
    try {
      numFound.clear();
      numBase.clear();
      numSyllables.clear();
      if (debugMode) {
        MessageHandler.printToLogFile("withDirectSpeech: " + withDirectSpeech);
      }
      for (int i = 0; i < cache.size(); i++) {
        //  TODO: Generate a result cache for later evaluations
        rule.match(cache.getAnalysedParagraph(i));
        if (rule instanceof AbstractStatisticSentenceStyleRule) {
          numFound.add(((AbstractStatisticSentenceStyleRule) rule).getNumberOfMatches());
          numBase.add(((AbstractStatisticSentenceStyleRule) rule).getSentenceCount());
        } else if (rule instanceof AbstractStatisticStyleRule) {
          numFound.add(((AbstractStatisticStyleRule) rule).getNumberOfMatches());
          numBase.add(((AbstractStatisticStyleRule) rule).getWordCount());
        } else if (rule instanceof ReadabilityRule) {
          numFound.add(((ReadabilityRule) rule).getAllWords());
          numSyllables.add(((ReadabilityRule) rule).getAllSyllables());
          numBase.add(((ReadabilityRule) rule).getAllSentences());
        }
      }
      if (debugMode) {
        MessageHandler.printToLogFile("Number of: numFound: " + numFound.size() + ", numBase: " + numBase.size() +
            ", numSyllables: " + numSyllables.size());
      }
    } catch (IOException e) {
      MessageHandler.showError(e);
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
   * get level of readability (0 - 6)
   */
  private int getReadabilityLevel(double fre) {
    if (fre < 30) {
      return 0;
    } else if (fre < 50) {
      return 1;
    } else if (fre < 60) {
      return 2;
    } else if (fre < 70) {
      return 3;
    } else if (fre < 80) {
      return 4;
    } else if (fre < 90) {
      return 5;
    } else {
      return 6;
    }
  }
  
  public int getLevel(int from, int to) throws Throwable {
    if (rule instanceof AbstractStatisticSentenceStyleRule || rule instanceof AbstractStatisticStyleRule) {
      int nBase = 0;
      int nFound = 0;
      for (int i = from; i < to && i < numBase.size(); i++) {
        nFound += numFound.get(i);
        nBase += numBase.get(i);
      }
      if (nBase == 0) {
        return 7;
      }
      double percent = ((double) nFound) * unitFactor / ((double) nBase);
      return getFoundWordsLevel(percent);
    } else if (rule instanceof ReadabilityRule) {
      int nAllSentences = 0;
      int nAllSyllables = 0;
      int nAllWords = 0;
      for (int i = from; i < to; i++) {
        nAllWords += numFound.get(i);
        nAllSentences += numBase.get(i);
        nAllSyllables += numSyllables.get(i);
      }
      if (nAllSentences == 0 || nAllWords == 0) {
        return 7;
      }
      double asl = (double) nAllWords / (double) nAllSentences;
      double asw = (double) nAllSyllables / (double) nAllWords;
      double fre = ((ReadabilityRule) rule).getFleschReadingEase(asl, asw);
      return getReadabilityLevel(fre);
    }
    return 7;
  }
  
  public double getUnitFactor() {
    if (rule instanceof AbstractStatisticSentenceStyleRule) {
      return ((AbstractStatisticSentenceStyleRule) rule).denominator();
    } else if (rule instanceof AbstractStatisticStyleRule) {
      return ((AbstractStatisticStyleRule) rule).denominator();
    }
    return 1;
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
  
  public void setWithDirectSpeach(boolean wDirectSpeech, StatAnCache cache) {
    if (debugMode) {
      MessageHandler.printToLogFile("withDirectSpeech: " + withDirectSpeech + ", wDirectSpeech: " + wDirectSpeech);
    }
    if (withDirectSpeech != wDirectSpeech) {
      withDirectSpeech = wDirectSpeech;
      if (rule instanceof AbstractStatisticSentenceStyleRule) {
        ((AbstractStatisticSentenceStyleRule) rule).setWithoutDirectSpeech(!withDirectSpeech);
      } else if (rule instanceof AbstractStatisticStyleRule) {
        ((AbstractStatisticStyleRule) rule).setWithoutDirectSpeech(!withDirectSpeech);
      }
      if (debugMode) {
        MessageHandler.printToLogFile("Generate basic numbers");
      }
      generateBasicNumbers(cache);
    }
  }
  
  public void setCurrentStep(int step) {
    if (step > 0) {
      procentualStep = step;
      optimalNumberWords = 3 * procentualStep;
    }
  }
  
  public boolean getDefaultDirectSpeach() {
    return true;
  }
  
  public String getUnitString() {
    if (unitFactor == 10000) {
      return "‱";
    } else if (unitFactor == 1000) {
      return "‰";
    } else {
      return "%";
    }
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

  public static boolean hasStatisticalStyleRules(Language lang) {
    try {
      for (Rule rule : lang.getRelevantRules(JLanguageTool.getMessageBundle(), null, lang, null)) {
        if (rule instanceof AbstractStatisticSentenceStyleRule || rule instanceof AbstractStatisticStyleRule ||
            rule instanceof ReadabilityRule) {
          return true;
        }
      }
    } catch (IOException e) {
    }
    return false;
  }
  
  public static boolean hasStatisticalOptions(Rule rule) {
    if (rule instanceof AbstractStatisticSentenceStyleRule || rule instanceof AbstractStatisticStyleRule) {
      return true;
    }
    return false;
  }
  
  public static boolean isLevelRule(Rule rule) {
    if (rule instanceof AbstractStatisticSentenceStyleRule || rule instanceof AbstractStatisticStyleRule ||
        rule instanceof ReadabilityRule) {
      return true;
    }
    return false;
  }
  

}
