/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import org.junit.Test;
import org.languagetool.JLanguageTool.Level;
import org.languagetool.JLanguageTool.Mode;
import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.language.French;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.FakeRule;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JLanguageToolTest {

  @Test
  public void testLanguageDependentFilter() throws IOException {
    Language lang = French.getInstance();
    JLanguageTool tool = new JLanguageTool(lang);

    // picky mode: suggestions with typographical apostrophes
    AnalyzedSentence analyzedSentence = tool.getAnalyzedSentence("De homme");
    AnnotatedText annotatedText = new AnnotatedTextBuilder().addText(analyzedSentence.getText()).build();
    RuleMatchListener listener = null;
    List<RuleMatch> matches = tool.check(annotatedText, true, ParagraphHandling.NORMAL, listener, Mode.ALL,
        Level.PICKY);
    assertEquals(1, matches.size());
    assertEquals("[D'homme]", matches.get(0).getSuggestedReplacements().toString());

    // normal mode: suggestions with straight apostrophes
    analyzedSentence = tool.getAnalyzedSentence("De homme");
    annotatedText = new AnnotatedTextBuilder().addText(analyzedSentence.getText()).build();
    listener = null;
    matches = tool.check(annotatedText, true, ParagraphHandling.NORMAL, listener, Mode.ALL, Level.DEFAULT);
    assertEquals(1, matches.size());
    assertEquals("[D'homme]", matches.get(0).getSuggestedReplacements().toString());
    
    tool.check("vrai/faux avec explication : Les droits d'accès, également appelés permissions ou autorisations, sont des règles définissant");
  }

  @Test
  public void testMultitokenSpeller() throws IOException {
    Language lang = French.getInstance();
    assertEquals("[gréco-romain, Gréco-Romain]", lang.getMultitokenSpeller().getSuggestions("greco-romain").toString());
    assertEquals("[Rimski-Korsakov]", lang.getMultitokenSpeller().getSuggestions("Rinsky-Korsakov").toString());
    assertEquals("[Nikolaï Rimski-Korsakov]", lang.getMultitokenSpeller().getSuggestions("Nikolai Rimski-Korsakov").toString());
  }

  @Test
  public void testMatchfiltering() throws IOException {
    Language lang = French.getInstance();
    JLanguageTool lt = new JLanguageTool(lang);
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("C'est Madame Curie.");
    RuleMatch ruleMatch = new RuleMatch(new FakeRule("AI_FR_GGEC_REPLACEMENT_ORTHOGRAPHY_UPPERCASE_MADAME_MADAME"), analyzedSentence,6, 12, "Possible error");
    ruleMatch.setSuggestedReplacement("madame");
    List<RuleMatch> ruleMatches = new ArrayList<>();
    ruleMatches.add(ruleMatch);
    List<RuleMatch> filteredRuleMatches =lang.filterRuleMatches(ruleMatches, null, null);
    assertEquals("Un usage différent des majuscules et des minuscules est recommandé.", filteredRuleMatches.get(0).getMessage());
    assertEquals("Majuscules et minuscules", filteredRuleMatches.get(0).getShortMessage());
    assertEquals(ITSIssueType.Typographical, filteredRuleMatches.get(0).getRule().getLocQualityIssueType());
    assertEquals("CASING", filteredRuleMatches.get(0).getRule().getCategory().getId().toString());
    assertEquals("AI_FR_GGEC_REPLACEMENT_ORTHOGRAPHY_UPPERCASE_MADAME_MADAME", filteredRuleMatches.get(0).getRule().getFullId());
    assertEquals("AI_FR_GGEC_REPLACEMENT_CASING_UPPERCASE_MADAME_MADAME", filteredRuleMatches.get(0).getSpecificRuleId());
  }

  @Test
  public void testQuotes() throws IOException {
    French lang = French.getInstance();
    JLanguageTool lt = new JLanguageTool(lang);
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("'elle'.");
    RuleMatch ruleMatch = new RuleMatch(new FakeRule("AI_FR_GGEC_REPLACEMENT_PUNCTUATION_QUOTE_TEST"), analyzedSentence, 0, 5, "Possible error");
    Set<String> enabledRules = Collections.emptySet();
    RuleMatch processedMatch = lang.adjustFrenchRuleMatch(ruleMatch, enabledRules);
    assertTrue(processedMatch.getRule().getTags().contains(Tag.picky));
    assertEquals("AI_FR_GGEC_QUOTES", processedMatch.getSpecificRuleId());
  }
  @Test
  public void testMergingOfGrammarCorrections() throws IOException {
    Language lang = French.getInstance();
    JLanguageTool lt = new JLanguageTool(lang);
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("Ce sont de spectateur");

    // Mocking two adjacent grammar issues
    RuleMatch ruleMatch1 = new RuleMatch(new FakeRule("AI_FR_GGEC_DES"), analyzedSentence, 9, 11, "Erreur de nombre");
    ruleMatch1.setSuggestedReplacement("des");
    RuleMatch ruleMatch2 = new RuleMatch(new FakeRule("AI_FR_GGEC_SPECTATEURS"), analyzedSentence, 12, 21, "Erreur de forme plurielle");
    ruleMatch2.setSuggestedReplacement("spectateurs");

    List<RuleMatch> ruleMatches = new ArrayList<>();
    ruleMatches.add(ruleMatch1);
    ruleMatches.add(ruleMatch2);

    // Process the rule matches
    List<RuleMatch> processedMatches = lang.filterRuleMatches(ruleMatches, null, Collections.emptySet());

    // Asserts
    assertEquals("des spectateurs", processedMatches.get(0).getSuggestedReplacements().get(0));
    assertEquals("AI_FR_MERGED_MATCH", processedMatches.get(0).getSpecificRuleId());
  }

}
