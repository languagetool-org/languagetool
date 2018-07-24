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
  public void tearDown() throws Exception {
    SuggestionsOrdererConfig.setNgramsPath(originalConfigNgramsPathValue);
    SuggestionsOrdererConfig.setMLSuggestionsOrderingEnabled(originalConfigMLSuggestionsOrderingEnabledValue);
  }


  @Test
  public void orderSuggestionsUsingModelNonExistingRuleId() throws Exception {
    Language language = new Demo();
    String rule_id = "rule_id";

    testOrderingHappened(language, rule_id);
  }

  @Test
  public void orderSuggestionsUsingModelExistingRuleId() throws Exception {
    Language language = new Demo();
    String rule_id = "MORFOLOGIK_RULE_EN_US";

    testOrderingHappened(language, rule_id);
  }

  @Test
  public void orderSuggestionsWithEnabledML() throws Exception {
    SuggestionsOrdererConfig.setMLSuggestionsOrderingEnabled(true);
    orderSuggestionsUsingModelExistingRuleId();
  }

  @Test
  public void orderSuggestionsWithDisabledML() throws Exception {
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
    List<String> suggestionsOrdered = suggestionsOrderer.orderSuggestionsUsingModel(suggestions, word, languageTool.getAnalyzedSentence(sentence), startPos, wordLength);
    assertTrue(suggestionsOrdered.containsAll(suggestions));
  }

}