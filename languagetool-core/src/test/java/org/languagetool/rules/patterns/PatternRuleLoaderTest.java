/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Tag;
import org.languagetool.ToneTag;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.Rule;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.util.*;

import static org.junit.Assert.*;

public class PatternRuleLoaderTest {

  @Test
  public void testGetRules() throws Exception {
    PatternRuleLoader prg = new PatternRuleLoader();
    String name = "/xx/grammar.xml";
    List<AbstractPatternRule> rules = prg.getRules(JLanguageTool.getDataBroker().getFromRulesDirAsStream(name), name, null);
    assertTrue(rules.size() >= 30);

    Rule demoRule1 = getRuleById("DEMO_RULE", rules);
    assertEquals("http://fake-server.org/foo-bar-error-explained", demoRule1.getUrl().toString());
    assertEquals("[This is <marker>fuu bah</marker>.]", demoRule1.getCorrectExamples().toString());
    List<IncorrectExample> incorrectExamples = demoRule1.getIncorrectExamples();
    assertEquals(1, incorrectExamples.size());
    assertEquals("This is <marker>foo bar</marker>.", incorrectExamples.get(0).getExample());

    Rule demoRule2 = getRuleById("API_OUTPUT_TEST_RULE", rules);
    assertNull(demoRule2.getUrl());

    assertEquals(ITSIssueType.Uncategorized, demoRule1.getLocQualityIssueType());
    assertEquals("tag inheritance failed", ITSIssueType.Addition, getRuleById("TEST_GO", rules).getLocQualityIssueType());
    assertEquals("tag inheritance overwrite failed", ITSIssueType.Uncategorized, getRuleById("TEST_PHRASES1", rules).getLocQualityIssueType());
    assertEquals("tag inheritance overwrite failed", ITSIssueType.Characters, getRuleById("test_include", rules).getLocQualityIssueType());

    List<Rule> groupRules1 = getRulesById("test_spacebefore", rules);
    assertEquals("tag inheritance form category failed", ITSIssueType.Addition, groupRules1.get(0).getLocQualityIssueType());
    assertEquals("tag inheritance overwrite failed", ITSIssueType.Duplication, groupRules1.get(1).getLocQualityIssueType());
    List<Rule> groupRules2 = getRulesById("test_unification_with_negation", rules);
    assertEquals("tag inheritance from rulegroup failed", ITSIssueType.Grammar, groupRules2.get(0).getLocQualityIssueType());

    Set<String> categories = getCategoryNames(rules);
    assertEquals(5, categories.size());
    assertTrue(categories.contains("misc"));
    assertTrue(categories.contains("otherCategory"));
    assertTrue(categories.contains("Test tokens with min and max attributes"));
    assertTrue(categories.contains("A category that's off by default"));

    PatternRule demoRuleWithChunk = (PatternRule) getRuleById("DEMO_CHUNK_RULE", rules);
    List<PatternToken> patternTokens = demoRuleWithChunk.getPatternTokens();
    assertEquals(2, patternTokens.size());
    assertEquals(null, patternTokens.get(1).getPOStag());
    assertEquals(new ChunkTag("B-NP-singular"), patternTokens.get(1).getChunkTag());

    List<Rule> orRules = getRulesById("GROUP_WITH_URL", rules);
    assertEquals(3, orRules.size());
    assertEquals("http://fake-server.org/rule-group-url", orRules.get(0).getUrl().toString());
    assertEquals("http://fake-server.org/rule-group-url-overwrite", orRules.get(1).getUrl().toString());
    assertEquals("http://fake-server.org/rule-group-url", orRules.get(2).getUrl().toString());
    
    assertEquals("short message on rule group", ((PatternRule)orRules.get(0)).getShortMessage());
    assertEquals("overwriting short message", ((PatternRule)orRules.get(1)).getShortMessage());
    assertEquals("short message on rule group", ((PatternRule)orRules.get(2)).getShortMessage());
    
    // make sure URLs don't leak to the next rule:
    List<Rule> orRules2 = getRulesById("OR_GROUPS", rules);
    for (Rule rule : orRules2) {
      assertNull("http://fake-server.org/rule-group-url", rule.getUrl());
    }
    Rule nextRule = getRuleById("DEMO_CHUNK_RULE", rules);
    assertNull("http://fake-server.org/rule-group-url", nextRule.getUrl());
  }

  @Test
  public void testPremiumXmlFlag() throws IOException {
    PatternRuleLoader prg = new PatternRuleLoader();
    String nameNonPremium = "/xx/grammar-nonPremium.xml";
    List<AbstractPatternRule> rulesInNonPremiumFile = prg.getRules(JLanguageTool.getDataBroker().getFromRulesDirAsStream(nameNonPremium), nameNonPremium, null);
    Rule rule1 = getRuleById("F-NP_C-NP_RG-NP_R-NP", rulesInNonPremiumFile);
    assertFalse(rule1.isPremium());
    Rule rule2 = getRuleById("F-NP_C-NP_RG-NP_R-P", rulesInNonPremiumFile);
    assertTrue(rule2.isPremium());
    Rule rule3 = getRuleById("F-NP_C-NP_RG-P_R-NP", rulesInNonPremiumFile);
    assertFalse(rule3.isPremium());
    Rule rule4 = getRuleById("F-NP_C-NP_RG-P_R-P", rulesInNonPremiumFile);
    assertTrue(rule4.isPremium());
    Rule rule5 = getRuleById("F-NP_C-P_RG-NP_R-NP", rulesInNonPremiumFile);
    assertFalse(rule5.isPremium());
    Rule rule6 = getRuleById("F-NP_C-P_RG-NP_R-P", rulesInNonPremiumFile);
    assertFalse(rule6.isPremium());
    Rule rule7 = getRuleById("F-NP_C-P_RG-P_R-NP", rulesInNonPremiumFile);
    assertFalse(rule7.isPremium());
    Rule rule8 = getRuleById("F-NP_C-P_RG-P_R-P", rulesInNonPremiumFile);
    assertTrue(rule8.isPremium());
    
    String namePremium = "/xx/grammar-premium.xml";
    List<AbstractPatternRule> rulesInPremiumFile = prg.getRules(JLanguageTool.getDataBroker().getFromRulesDirAsStream(namePremium), namePremium, null);
    Rule rule9 = getRuleById("F-P_C-P_RG-P_R-P", rulesInPremiumFile);
    assertTrue(rule9.isPremium());
    Rule rule10 = getRuleById("F-P_C-P_RG-P_R-NP", rulesInPremiumFile);
    assertFalse(rule10.isPremium());
    Rule rule11 = getRuleById("F-P_C-P_RG-NP_R-P", rulesInPremiumFile);
    assertTrue(rule11.isPremium());
    Rule rule12 = getRuleById("F-P_C-P_RG-NP_R-NP", rulesInPremiumFile);
    assertFalse(rule12.isPremium());
    Rule rule13 = getRuleById("F-P_C-NP_RG-P_R-NP", rulesInPremiumFile);
    assertFalse(rule13.isPremium());
    Rule rule14 = getRuleById("F-P_C-NP_RG-P_R-P", rulesInPremiumFile);
    assertTrue(rule14.isPremium());
    Rule rule15 = getRuleById("F-P_C-NP_RG-NP_R-NP", rulesInPremiumFile);
    assertFalse(rule15.isPremium());
    Rule rule16 = getRuleById("F-P_C-NP_RG-NP_R-P", rulesInPremiumFile);
    assertTrue(rule16.isPremium());
  }
  
  @Test
  public void testToneTagsAttribute() throws IOException {
    PatternRuleLoader prg = new PatternRuleLoader();
    String styleRuleFile = "/xx/style.xml";
    List<AbstractPatternRule> styleRules = prg.getRules(JLanguageTool.getDataBroker().getFromRulesDirAsStream(styleRuleFile), styleRuleFile, null);
    
    Rule formalClarityToneRule = getRuleById("Formal_Clarity_TONE_RULE", styleRules);
    assertTrue(formalClarityToneRule.hasToneTag(ToneTag.formal));
    assertTrue(formalClarityToneRule.hasToneTag(ToneTag.clarity));
    assertEquals(2, formalClarityToneRule.getToneTags().size());
    assertFalse(formalClarityToneRule.isGoalSpecific());
    
    Rule noToneRule = getRuleById("NO_TONE_RULE", styleRules);
    assertTrue(noToneRule.getToneTags().isEmpty());
    assertFalse(noToneRule.isGoalSpecific());
    
    Rule confidentAcademicScientificToneRule = getRuleById("CONFIDENT_ACADEMIC_SCIENTIFIC_TONE_RULE", styleRules);
    assertTrue(confidentAcademicScientificToneRule.hasToneTag(ToneTag.confident));
    assertTrue(confidentAcademicScientificToneRule.hasToneTag(ToneTag.academic));
    assertTrue(confidentAcademicScientificToneRule.hasToneTag(ToneTag.scientific));
    assertEquals(3, confidentAcademicScientificToneRule.getToneTags().size());
    assertFalse(confidentAcademicScientificToneRule.isGoalSpecific());
    
    Rule confidentAcademicToneRule = getRuleById("CONFIDENT_ACADEMIC_TONE_RULE", styleRules);
    assertTrue(confidentAcademicToneRule.hasToneTag(ToneTag.confident));
    assertTrue(confidentAcademicToneRule.hasToneTag(ToneTag.academic));
    assertEquals(2, confidentAcademicToneRule.getToneTags().size());
    assertFalse(confidentAcademicToneRule.isGoalSpecific());
    
    Rule pickyClarityConfidentAcademicToneRule = getRuleById("PICKY-CLARITY_CONFIDENT_ACADEMIC_TONE_RULE",styleRules);
    assertTrue(pickyClarityConfidentAcademicToneRule.hasToneTag(ToneTag.clarity));
    assertTrue(pickyClarityConfidentAcademicToneRule.hasToneTag(ToneTag.confident));
    assertTrue(pickyClarityConfidentAcademicToneRule.hasToneTag(ToneTag.academic));
    assertEquals(3, pickyClarityConfidentAcademicToneRule.getToneTags().size());
    assertTrue(pickyClarityConfidentAcademicToneRule.hasTag(Tag.picky));
    assertFalse(pickyClarityConfidentAcademicToneRule.isGoalSpecific());
    
    Rule pickyClarityConfidentAcademicScientificToneRule = getRuleById("PICKY-CLARITY_CONFIDENT_ACADEMIC_SCIENTIFIC_TONE_RULE", styleRules);
    assertTrue(pickyClarityConfidentAcademicScientificToneRule.hasToneTag(ToneTag.clarity));
    assertTrue(pickyClarityConfidentAcademicScientificToneRule.hasToneTag(ToneTag.confident));
    assertTrue(pickyClarityConfidentAcademicScientificToneRule.hasToneTag(ToneTag.academic));
    assertTrue(pickyClarityConfidentAcademicScientificToneRule.hasToneTag(ToneTag.scientific));
    assertEquals(4, pickyClarityConfidentAcademicScientificToneRule.getToneTags().size());
    assertTrue(pickyClarityConfidentAcademicScientificToneRule.hasTag(Tag.picky));
    assertFalse(pickyClarityConfidentAcademicScientificToneRule.isGoalSpecific());

    Rule persuasiveObjectiveToneRule = getRuleById("PERSUASIVE_OBJECTIVE_TONE_RULE", styleRules);
    assertTrue(persuasiveObjectiveToneRule.hasToneTag(ToneTag.persuasive));
    assertTrue(persuasiveObjectiveToneRule.hasToneTag(ToneTag.objective));
    assertEquals(2, persuasiveObjectiveToneRule.getToneTags().size());
    assertFalse(persuasiveObjectiveToneRule.isGoalSpecific());
    
    Rule persuasiveObjectiveInformalToneRule = getRuleById("PERSUASIVE_OBJECTIVE_INFORMAL_TONE_RULE", styleRules);
    assertTrue(persuasiveObjectiveInformalToneRule.hasToneTag(ToneTag.persuasive));
    assertTrue(persuasiveObjectiveInformalToneRule.hasToneTag(ToneTag.objective));
    assertTrue(persuasiveObjectiveInformalToneRule.hasToneTag(ToneTag.informal));
    assertEquals(3, persuasiveObjectiveInformalToneRule.getToneTags().size());
    assertFalse(persuasiveObjectiveInformalToneRule.isGoalSpecific());
    
    Rule persuasiveGoalSpecificToneRule = getRuleById("PERSUASIVE_GOAL_SPECIFIC_TONE_RULE", styleRules);
    assertTrue(persuasiveGoalSpecificToneRule.hasToneTag(ToneTag.persuasive));
    assertTrue(persuasiveGoalSpecificToneRule.isGoalSpecific());
    
    Rule persuasiveNotGoalSpecificToneRule = getRuleById("PERSUASIVE_NOT_GOAL_SPECIFIC_TONE_RULE", styleRules);
    assertTrue(persuasiveNotGoalSpecificToneRule.hasToneTag(ToneTag.persuasive));
    assertFalse(persuasiveNotGoalSpecificToneRule.isGoalSpecific());
  }

  private Set<String> getCategoryNames(List<AbstractPatternRule> rules) {
    Set<String> categories = new HashSet<>();
    for (AbstractPatternRule rule : rules) {
      categories.add(rule.getCategory().getName());
    }
    return categories;
  }

  private Rule getRuleById(String id, List<AbstractPatternRule> rules) {
    for (Rule rule : rules) {
      if (rule.getId().equals(id)) {
        return rule;
      }
    }
    throw new RuntimeException("No rule found for id '" + id + "'");
  }

  private List<Rule> getRulesById(String id, List<AbstractPatternRule> rules) {
    List<Rule> result = new ArrayList<>();
    for (Rule rule : rules) {
      if (rule.getId().equals(id)) {
        result.add(rule);
      }
    }
    return result;
  }

  @Test
  @Ignore
  public void testEncryptDecrypt() throws Exception {
    String encrypted = encrypt();
    System.out.println("encrypted: " + decrypt(encrypted));
    String decrypted = decrypt(encrypted);
    System.out.println("decrypted: " + decrypted);
    PatternRuleLoader loader = new PatternRuleLoader();
    List<AbstractPatternRule> rules = loader.getRules(new ByteArrayInputStream(decrypted.getBytes(StandardCharsets.UTF_8)), "<unknown>", null);
    System.out.println("Loaded " + rules.size() + " rules");
  }

  private String encrypt() throws Exception {
    Key key = generateKey();
    Cipher c = Cipher.getInstance("AES");
    c.init(Cipher.ENCRYPT_MODE, key);
    List<String> lines = Files.readAllLines(Paths.get("grammar-premium.xml"));
    String val = String.join("\n", lines);
    //String val = "my-secret-data";
    byte[] encVal = c.doFinal(val.getBytes(StandardCharsets.UTF_8));
    String encoded = Base64.getEncoder().encodeToString(encVal);
    try (FileWriter fw = new FileWriter("/tmp/encoded")) {
      fw.write(encoded);
    }
    return encoded;
  }

  private String decrypt(String encrypted) throws Exception {
    Key key = generateKey();
    Cipher c = Cipher.getInstance("AES");
    c.init(Cipher.DECRYPT_MODE, key);
    byte[] decodedValue = Base64.getDecoder().decode(encrypted);
    byte[] decValue = c.doFinal(decodedValue);
    return new String(decValue);
  }

  private static Key generateKey() {
    return new SecretKeySpec("mykey...........".getBytes(StandardCharsets.UTF_8), "AES");
  }
  
}
