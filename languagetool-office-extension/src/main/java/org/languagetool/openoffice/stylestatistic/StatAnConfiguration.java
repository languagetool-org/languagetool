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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.languagetool.JLanguageTool;
import org.languagetool.openoffice.MessageHandler;
import org.languagetool.openoffice.OfficeTools;
import org.languagetool.rules.AbstractStatisticSentenceStyleRule;
import org.languagetool.rules.AbstractStatisticStyleRule;
import org.languagetool.rules.TextLevelRule;

/**
 * Statistical Analyzes Configuration 
 * @since 6.2
 * @author Fred Kruse
 */
public class StatAnConfiguration {
  private final List<TextLevelRule> rules;
  private final Map<String, Boolean> withoutDirectSpeech = new HashMap<String, Boolean>();
  private final Map<String, Integer> levelStep = new HashMap<String, Integer>();
  private final Map<String, List<String>> excludedWords = new HashMap<String, List<String>>();
  
  StatAnConfiguration(List<TextLevelRule> rules) throws Throwable {
    this.rules = rules;
    loadConfiguration();
  }
  
  boolean isWithoutDirectSpeech(TextLevelRule rule) {
    if (!withoutDirectSpeech.containsKey(rule.getId())) {
      return false;
    }
    return withoutDirectSpeech.get(rule.getId());
  }
  
  void setWithoutDirectSpeech(TextLevelRule rule, boolean wDS) {
    withoutDirectSpeech.put(rule.getId(), wDS);
  }
  
  int getLevelStep(TextLevelRule rule) {
    if (!levelStep.containsKey(rule.getId())) {
      return rule.getDefaultValue();
    }
    return levelStep.get(rule.getId());
  }
  
  void setLevelStep(TextLevelRule rule, int step) {
    levelStep.put(rule.getId(), step);
  }

  List<String> getExcludedWords(TextLevelRule rule) {
    return excludedWords.get(rule.getId());
  }

  void setAllExcludedWords(TextLevelRule rule, List<String> words) {
    excludedWords.put(rule.getId(), words);
  }

  void removeAllExcludedWords(TextLevelRule rule) {
    excludedWords.get(rule.getId()).clear();
  }

  void addExcludedWord(TextLevelRule rule, String word) {
    List<String> words = excludedWords.get(rule.getId());
    words.add(word);
    excludedWords.put(rule.getId(), words);
  }
  
  private String createLevelRuleProperties(String ruleId) throws Throwable {
    return Boolean.toString(withoutDirectSpeech.get(ruleId)) + ";" + Integer.toString(levelStep.get(ruleId));
  }
  
  private String createUsedWordRuleProperties(String ruleId) throws Throwable {
    String txt = Boolean.toString(withoutDirectSpeech.get(ruleId)) + ";";
    List<String> excWords = excludedWords.get(ruleId);
    for (int i = 0; i < excWords.size(); i++) {
      txt += excWords.get(i);
      if (i < excWords.size() - 1) {
        txt += ",";
      }
    }
    return txt;
  }
  
  private void setLevelRuleProperties(TextLevelRule rule, String propString) throws Throwable {
    int defaultStep;
    if (rule instanceof AbstractStatisticSentenceStyleRule) {
      defaultStep = (int) (((((AbstractStatisticSentenceStyleRule) rule).getDefaultValue() - 1) / 3.) + 0.5);
    } else if (rule instanceof AbstractStatisticStyleRule) {
      defaultStep = (int) (((((AbstractStatisticStyleRule) rule).getDefaultValue() - 1) / 3.) + 0.5);
    } else {
      return;
    }
    if (propString == null) {
      withoutDirectSpeech.put(rule.getId(), false);
      levelStep.put(rule.getId(), defaultStep);
    } else {
      String[] props = propString.split(";");
      if (props.length > 0 && !props[0].isEmpty()) {
        withoutDirectSpeech.put(rule.getId(), Boolean.parseBoolean(props[0]));
      } else {
        withoutDirectSpeech.put(rule.getId(), false);
      }
      if (props.length > 1 && props[1] != null && !props[1].isEmpty()) {
        levelStep.put(rule.getId(), Integer.parseInt(props[1]));
      } else {
        levelStep.put(rule.getId(), defaultStep);
      }
    }
  }
  
  private void setUsedWordRuleProperties(TextLevelRule rule, String propString) throws Throwable {
/*      TODO: Add support for used word rules
    try {
    UsedWordRule usedWordRule = (UsedWordRule) rule;
    List<String> excWords = new ArrayList<String>();
    if (propString == null) {
//      MessageHandler.printToLogFile("Rule: " + usedWordRule.getId() + "; PropString == null");
//      MessageHandler.printToLogFile("DefaultDirectSpeach(): " + usedWordRule.getDefaultDirectSpeach() + "; excWords.size: " + excWords.size());
      withoutDirectSpeech.put(usedWordRule.getId(), !usedWordRule.getDefaultDirectSpeach());
      excludedWords.put(usedWordRule.getId(), excWords);
    } else {
      String[] props = propString.split(";");
      if (props.length > 0 && !props[0].isEmpty()) {
        withoutDirectSpeech.put(usedWordRule.getId(), Boolean.parseBoolean(props[0]));
      } else {
        withoutDirectSpeech.put(usedWordRule.getId(), !usedWordRule.getDefaultDirectSpeach());
      }
      if (props.length > 1 && props[1] != null && !props[1].isEmpty()) {
        String[] words = props[1].split(",");
        for (String word : words) {
          excWords.add(word);
        }
      }
      excludedWords.put(usedWordRule.getId(), excWords);
    }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
*/
  }
  
  void saveConfiguration() throws Throwable {
    Properties props = new Properties();
    for (TextLevelRule rule : rules) {
      if (LevelRule.hasStatisticalOptions(rule)) {
        props.setProperty(rule.getId(), createLevelRuleProperties(rule.getId()));
/*        
        } else if (rule instanceof UsedWordRule) {
          props.setProperty(rule.getId(), createUsedWordRuleProperties(rule.getId()));
        }
*/
      }
    }
    try (FileOutputStream fos = new FileOutputStream(OfficeTools.getStatisticalConfigFilePath())) {
      props.store(fos, "LT statistical analyzes configuration (" + JLanguageTool.VERSION + "/" + JLanguageTool.BUILD_DATE + ")");
    } catch (Throwable e) {
      MessageHandler.showError(e);
    }
  }
  
  void loadConfiguration() throws Throwable {
    Properties props = new Properties();
    try (FileInputStream fis = new FileInputStream(OfficeTools.getStatisticalConfigFilePath())) {
      props.load(fis);
    } catch (Throwable e) {
//      MessageHandler.showError(e);
    }
    withoutDirectSpeech.clear();
    levelStep.clear();
    excludedWords.clear();
    for (TextLevelRule rule : rules) {
      boolean hasOptions = LevelRule.hasStatisticalOptions(rule);
      MessageHandler.printToLogFile("Rule: " + rule.getId() + "; has options: " + hasOptions);
      if (hasOptions) {
        setLevelRuleProperties(rule, props.getProperty(rule.getId()));
/*
        } else if (rule instanceof UsedWordRule) {
          setUsedWordRuleProperties(rule, props.getProperty(rule.getId()));
        }
*/
      }
    }
    MessageHandler.printToLogFile("Config load done");
  }

}
