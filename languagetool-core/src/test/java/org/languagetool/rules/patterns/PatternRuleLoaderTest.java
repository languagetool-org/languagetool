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

import java.io.ByteArrayInputStream;
import java.io.FilePermission;
import java.net.URL;
import java.security.*;
import java.util.*;

import junit.framework.TestCase;

import org.languagetool.JLanguageTool;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.Rule;

public class PatternRuleLoaderTest extends TestCase {

  public void testGetRules() throws Exception {
    final PatternRuleLoader prg = new PatternRuleLoader();
    final String name = "/xx/grammar.xml";
    final List<PatternRule> rules = prg.getRules(JLanguageTool.getDataBroker().getFromRulesDirAsStream(name), name);
    assertTrue(rules.size() >= 30);

    final Rule demoRule1 = getRuleById("DEMO_RULE", rules);
    final List<URL> urls = demoRule1.getUrls();
    assertEquals("http://fake-server.org/foo-bar-error-explained", urls.get(0).toString());
    assertEquals("http://fake-server.org/foo-bar-error-explained-alternative", urls.get(1).toString());
    assertEquals("[This is <marker>fuu bah</marker>.]", demoRule1.getCorrectExamples().toString());
    final List<IncorrectExample> incorrectExamples = demoRule1.getIncorrectExamples();
    assertEquals(1, incorrectExamples.size());
    assertEquals("This is <marker>foo bar</marker>.", incorrectExamples.get(0).getExample());

    final Rule demoRule2 = getRuleById("API_OUTPUT_TEST_RULE", rules);
    assertEquals(0, demoRule2.getUrls().size());

    assertEquals(ITSIssueType.Uncategorized, demoRule1.getLocQualityIssueType());
    assertEquals("tag inheritance failed", ITSIssueType.Addition, getRuleById("TEST_GO", rules).getLocQualityIssueType());
    assertEquals("tag inheritance overwrite failed", ITSIssueType.Uncategorized, getRuleById("TEST_PHRASES1", rules).getLocQualityIssueType());
    assertEquals("tag inheritance overwrite failed", ITSIssueType.Characters, getRuleById("test_include", rules).getLocQualityIssueType());

    final List<Rule> groupRules1 = getRulesById("test_spacebefore", rules);
    assertEquals("tag inheritance form category failed", ITSIssueType.Addition, groupRules1.get(0).getLocQualityIssueType());
    assertEquals("tag inheritance overwrite failed", ITSIssueType.Duplication, groupRules1.get(1).getLocQualityIssueType());
    final List<Rule> groupRules2 = getRulesById("test_unification_with_negation", rules);
    assertEquals("tag inheritance from rulegroup failed", ITSIssueType.Grammar, groupRules2.get(0).getLocQualityIssueType());

    final Set<String> categories = getCategoryNames(rules);
    assertEquals(3, categories.size());
    assertTrue(categories.contains("misc"));
    assertTrue(categories.contains("otherCategory"));
    assertTrue(categories.contains("Test tokens with min and max attributes"));

    final PatternRule demoRuleWithChunk = (PatternRule) getRuleById("DEMO_CHUNK_RULE", rules);
    final List<PatternToken> patternTokens = demoRuleWithChunk.getPatternTokens();
    assertEquals(2, patternTokens.size());
    assertEquals(null, patternTokens.get(1).getPOStag());
    assertEquals(new ChunkTag("B-NP-singular"), patternTokens.get(1).getChunkTag());

    final List<Rule> orRules = getRulesById("GROUP_WITH_URL", rules);
    assertEquals(3, orRules.size());
    assertEquals("http://fake-server.org/rule-group-url", orRules.get(0).getUrls().get(0).toString());
    assertEquals("http://fake-server.org/rule-group-url-overwrite", orRules.get(1).getUrls().get(0).toString());
    assertEquals("http://fake-server.org/rule-group-url", orRules.get(2).getUrls().get(0).toString());

    assertEquals("short message on rule group", ((PatternRule)orRules.get(0)).getShortMessage());
    assertEquals("overwriting short message", ((PatternRule)orRules.get(1)).getShortMessage());
    assertEquals("short message on rule group", ((PatternRule)orRules.get(2)).getShortMessage());

    // make sure URLs don't leak to the next rule:
    final List<Rule> orRules2 = getRulesById("OR_GROUPS", rules);
    for (Rule rule : orRules2) {
      assertEquals(0, rule.getUrls().size());
    }
    final Rule nextRule = getRuleById("DEMO_CHUNK_RULE", rules);
      assertEquals(0, nextRule.getUrls().size());
    }

  public void testPermissionManager() throws Exception {
    Policy.setPolicy(new MyPolicy());
    System.setSecurityManager(new SecurityManager());
    try {
      PatternRuleLoader loader = new PatternRuleLoader();
      // do not crash if Authenticator.setDefault() is forbidden,
      // see https://github.com/languagetool-org/languagetool/issues/255
      loader.getRules(new ByteArrayInputStream("<rules lang='xx'></rules>".getBytes("utf-8")), "fakeName");
    } finally {
      System.setSecurityManager(null);
    }
  }

  private Set<String> getCategoryNames(List<PatternRule> rules) {
    final Set<String> categories = new HashSet<>();
    for (PatternRule rule : rules) {
      categories.add(rule.getCategory().getName());
    }
    return categories;
  }

  private Rule getRuleById(String id, List<PatternRule> rules) {
    for (Rule rule : rules) {
      if (rule.getId().equals(id)) {
        return rule;
      }
    }
    throw new RuntimeException("No rule found for id '" + id + "'");
  }

  private List<Rule> getRulesById(String id, List<PatternRule> rules) {
    final List<Rule> result = new ArrayList<>();
    for (Rule rule : rules) {
      if (rule.getId().equals(id)) {
        result.add(rule);
      }
    }
    return result;
  }

  static class MyPolicy extends Policy {
    @Override
    public PermissionCollection getPermissions(CodeSource codesource) {
      PermissionCollection perms = new MyPermissionCollection();
      perms.add(new RuntimePermission("setIO"));
      perms.add(new RuntimePermission("setSecurityManager"));
      perms.add(new FilePermission("<<ALL FILES>>", "read"));
      return perms;
    }
  }

  static class MyPermissionCollection extends PermissionCollection {
    private final List<Permission> perms = new ArrayList<>();
    @Override
    public void add(Permission p) {
      perms.add(p);
    }
    @Override
    public boolean implies(Permission p) {
      for (Permission perm : perms) {
        if (perm.implies(p)) {
          return true;
        }
      }
      return false;
    }
    @Override
    public Enumeration<Permission> elements() {
      return Collections.enumeration(perms);
    }
    @Override
    public boolean isReadOnly() {
      return false;
    }
  }

}
