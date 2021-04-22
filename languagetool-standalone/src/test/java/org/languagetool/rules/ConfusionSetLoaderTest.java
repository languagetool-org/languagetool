/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.language.Demo;
import org.languagetool.rules.ngrams.FakeLanguageModel;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;


public class ConfusionSetLoaderTest {
  
  @Test
  public void testConfusionSetLoading() throws IOException {
    int count = 0;
    for (Language language : Languages.get()) {
      List<Rule> rules;
      try {
        rules = language.getRelevantLanguageModelRules(JLanguageTool.getMessageBundle(), new FakeLanguageModel(), null);
      } catch (Exception e) {
        throw new RuntimeException("Could not load confusion pairs for " + language.getName(), e);
      }
      if (rules.size() > 0) {
        String path = "/" + language.getShortCode() + "/confusion_sets.txt";
        try (InputStream confusionSetStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path)) {
          ConfusionSetLoader confusionSetLoader = new ConfusionSetLoader(new Demo());
          Map<String, List<ConfusionPair>> set = confusionSetLoader.loadConfusionPairs(confusionSetStream);
          count += set.size();
        }
      }
    }
    int minCount = 1000;
    assertTrue("Only got " + count + " confusion pairs for all languages, expected > " + minCount, count > minCount);
  }
  
  @Test
  @Ignore("one-time use, migrate descriptions to word_definition.txt")
  public void testConfusionSetDescriptionExport() throws IOException {
    for (Language language : Languages.get()) {
      if (language.getShortCode().equals("de")) {
        List<Rule> rules = language.getRelevantLanguageModelRules(JLanguageTool.getMessageBundle(), new FakeLanguageModel(), null);
        if (rules.size() > 0) {
          String path = "/" + language.getShortCode() + "/confusion_sets.txt";
          try (InputStream confusionSetStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path)) {
            ConfusionSetLoader confusionSetLoader = new ConfusionSetLoader(new Demo());
            Map<String, List<ConfusionPair>> set = confusionSetLoader.loadConfusionPairs(confusionSetStream);
            for (Map.Entry<String, List<ConfusionPair>> entry : set.entrySet()) {
              for (ConfusionPair confusionPair : entry.getValue()) {
                printDesc(confusionPair.getTerm1());
                printDesc(confusionPair.getTerm2());
              }
            }
          }
        }
      }
    }
  }

  private void printDesc(ConfusionString confusionPair) {
    if (confusionPair.getDescription() != null) {
      System.out.println(confusionPair.getString() + "\t" + confusionPair.getDescription());
    } else {
      System.out.println("#" + confusionPair.getString() + "\t");
    }
  }

  @Test
  @Ignore("confusion pairs change rarely, so not running this regularly helps the build stay fast")
  public void testConfusionSetSpelling() throws IOException {
    for (Language lang : Languages.get()) {
      if (lang.getShortCode().equals("en") && !lang.getShortCodeWithCountryAndVariant().equals("en-US")) {
        continue;
      }
      if (lang.getShortCode().equals("de") && !lang.getShortCodeWithCountryAndVariant().equals("de-DE")) {
        continue;
      }
      List<Rule> rules;
      try {
        rules = lang.getRelevantLanguageModelRules(JLanguageTool.getMessageBundle(), new FakeLanguageModel(), null);
      } catch (Exception e) {
        throw new RuntimeException("Could not load confusion pairs for " + lang.getName(), e);
      }
      if (rules.size() > 0) {
        JLanguageTool lt = new JLanguageTool(lang);
        SpellingCheckRule spellRule = null;
        for (Rule rule : lt.getAllActiveRules()) {
          if (rule instanceof SpellingCheckRule) {
            spellRule = (SpellingCheckRule) rule;
            break;
          }
        }
        if (spellRule == null) {
          System.out.println("No spell checker rule found for language " + lang + ", skipping check");
          continue;
        }
        String path = "/" + lang.getShortCode() + "/confusion_sets.txt";
        System.out.println("WARN: Spell checking terms in " + path);
        try (InputStream confusionSetStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path)) {
          ConfusionSetLoader confusionSetLoader = new ConfusionSetLoader(new Demo());
          Map<String, List<ConfusionPair>> set = confusionSetLoader.loadConfusionPairs(confusionSetStream);
          for (Map.Entry<String, List<ConfusionPair>> entry : set.entrySet()) {
            //System.out.println(entry.getValue());
            for (ConfusionPair confusionPair : entry.getValue()) {
              checkSpelling(confusionPair.getTerm1(), spellRule, path);
              checkSpelling(confusionPair.getTerm2(), spellRule, path);
            }
          }
        }
      }
    }
  }

  private void checkSpelling(ConfusionString term, SpellingCheckRule spellRule, String path) throws IOException {
    if (spellRule.isMisspelled(term.getString())) {
      System.err.println("Not known to spell checker: " + term.getString() + " in " + path);
    }
  }

}
