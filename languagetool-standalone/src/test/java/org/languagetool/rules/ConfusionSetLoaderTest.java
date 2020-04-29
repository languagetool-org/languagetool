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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.ngrams.FakeLanguageModel;

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
        rules = language.getRelevantLanguageModelRules(JLanguageTool.getMessageBundle(), new FakeLanguageModel());
      } catch (Exception e) {
        throw new RuntimeException("Could not load confusion pairs for " + language.getName(), e);
      }
      if (rules.size() > 0) {
        String path = "/" + language.getShortCode() + "/confusion_sets.txt";
        try (InputStream confusionSetStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path)) {
          ConfusionSetLoader confusionSetLoader = new ConfusionSetLoader();
          Map<String, List<ConfusionPair>> set = confusionSetLoader.loadConfusionPairs(confusionSetStream);
          count += set.size();
        }
      }
    }
    int minCount = 1000;
    assertTrue("Only got " + count + " confusion pairs for all languages, expected > " + minCount, count > minCount);
  }
  
}
