/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2019 Fabian Richter
 *  * All rights reserved - not part of the Open Source edition
 *
 */

package org.languagetool.rules.fr;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.GlobalConfig;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

public class GrammalecteRuleTest {

  @Test
  @Ignore("only works with Grammalecte running")
  public void testMatch() throws IOException {
    GlobalConfig cfg = new GlobalConfig();
    cfg.setGrammalecteServer("http://localhost:8080/gc_text/fr");
    GrammalecteRule rule = new GrammalecteRule(JLanguageTool.getMessageBundle(), cfg);
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("fr"));

    String text = "Elles sont aveugle.";
    String expectedMessage = "Accord avec le sujet “Elles” : “aveugle” devrait être au féminin pluriel.";

    AnalyzedSentence sentence = lt.getAnalyzedSentence(text);
    RuleMatch[] matches = rule.match(sentence);

    assertThat(matches.length, is(1));
    assertThat(matches[0].getMessage(), startsWith(expectedMessage));
  }

}
