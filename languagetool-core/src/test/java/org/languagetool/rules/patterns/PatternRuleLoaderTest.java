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
import org.languagetool.chunking.ChunkTag;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.Rule;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
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
    List<AbstractPatternRule> rules = prg.getRules(JLanguageTool.getDataBroker().getFromRulesDirAsStream(name), name);
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
    List<AbstractPatternRule> rules = loader.getRules(new ByteArrayInputStream(decrypted.getBytes(StandardCharsets.UTF_8)), "<unknown>");
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
