/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Oleg Serikov
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
package org.languagetool.rules.spelling.morfologik.suggestions_ordering;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.Demo;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class SuggestionsOrdererTest {
  
  private String originalConfigNgramsPathValue;
  private boolean originalConfigMLSuggestionsOrderingEnabledValue;

  @Before
  public void setUp() throws Exception {
    originalConfigNgramsPathValue = SuggestionsOrdererConfig.getNgramsPath();
    originalConfigMLSuggestionsOrderingEnabledValue = SuggestionsOrdererConfig.isMLSuggestionsOrderingEnabled();
  }

  @After
  public void tearDown() {
    SuggestionsOrdererConfig.setNgramsPath(originalConfigNgramsPathValue);
    SuggestionsOrdererConfig.setMLSuggestionsOrderingEnabled(originalConfigMLSuggestionsOrderingEnabledValue);
  }
  
  @Test
  public void orderSuggestionsUsingModelNonExistingRuleId() throws IOException {
    Language language = new Demo();
    String rule_id = "rule_id";
    testOrderingHappened(language, rule_id);
  }

  @Test
  public void orderSuggestionsUsingModelExistingRuleId() throws IOException {
    Language language = new Demo();
    String rule_id = "MORFOLOGIK_RULE_EN_US";
    testOrderingHappened(language, rule_id);
  }

  @Test
  public void orderSuggestionsWithEnabledML() throws IOException {
    SuggestionsOrdererConfig.setMLSuggestionsOrderingEnabled(true);
    orderSuggestionsUsingModelExistingRuleId();
  }

  @Test
  public void orderSuggestionsWithDisabledML() throws IOException {
    SuggestionsOrdererConfig.setMLSuggestionsOrderingEnabled(false);
    orderSuggestionsUsingModelExistingRuleId();
  }

  private void testOrderingHappened(Language language, String rule_id) throws IOException {
    JLanguageTool languageTool = new JLanguageTool(language);
    SuggestionsOrderer suggestionsOrderer = new SuggestionsOrderer(language, rule_id);

    String word = "wprd";
    String sentence = String.join(" ","a", word, "containing", "sentence");

    LinkedList<String> suggestions = new LinkedList<>();
    suggestions.add("word");
    suggestions.add("weird");

    int startPos = sentence.indexOf(word);
    int wordLength = word.length();
    List<String> suggestionsOrdered = suggestionsOrderer.orderSuggestionsUsingModel(
            suggestions, word, languageTool.getAnalyzedSentence(sentence), startPos, wordLength);
    assertTrue(suggestionsOrdered.containsAll(suggestions));
  }

}