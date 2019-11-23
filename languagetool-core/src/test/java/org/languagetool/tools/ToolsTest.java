/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tools;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Demo;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.CategoryIds;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternToken;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ToolsTest {
  
  @Test
  public void testCorrectTextFromMatches() {
    RuleMatch match1 = new RuleMatch(new FakeRule(), null, 0, 9, "msg1");
    match1.setSuggestedReplacement("I've had");
    RuleMatch match2 = new RuleMatch(new FakeRule(), null, 0, 9, "msg2");
    match2.setSuggestedReplacement("I have");
    List<RuleMatch> matches = Arrays.asList(match1, match2);
    assertThat(Tools.correctTextFromMatches("I've have", matches), is("I've had"));
  }

  @Test
  public void testSelectRules() {
    Demo demo = new Demo();
    expectDemoRuleId(Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), false, demo);
    expectNotDemoRuleId(Collections.emptySet(), Collections.emptySet(), Collections.singleton("DEMO_RULE"), Collections.emptySet(), false, demo);
    expectNotDemoRuleId(Collections.singleton(CategoryIds.MISC), Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), false, demo);
    // disable category, but enable rule:
    expectDemoRuleId(Collections.singleton(CategoryIds.MISC), Collections.emptySet(), Collections.emptySet(), Collections.singleton("DEMO_RULE"), false, demo);

    // now with enablesOnly=true:
    expectDemoRuleId(Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), true, demo);
    expectNotDemoRuleId(Collections.emptySet(), Collections.emptySet(), Collections.singleton("DEMO_RULE"), Collections.emptySet(), true, demo);
    expectNotDemoRuleId(Collections.singleton(CategoryIds.MISC), Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), true, demo);
    expectDemoRuleId(Collections.emptySet(), Collections.singleton(CategoryIds.MISC), Collections.emptySet(), Collections.emptySet(), true, demo);
    expectNotDemoRuleId(Collections.emptySet(), Collections.singleton(CategoryIds.CASING), Collections.emptySet(), Collections.emptySet(), true, demo);
  }

  private void expectDemoRuleId(Set<CategoryId> disabledCategories, Set<CategoryId> enabledCategories,
                                Set<String> disabledRules, Set<String> enabledRules, boolean useEnabledOnly, Demo demo) {
    JLanguageTool lt = new JLanguageTool(demo);
    Tools.selectRules(lt, disabledCategories, enabledCategories, disabledRules, enabledRules, useEnabledOnly);
    assertTrue(getRuleIds(lt).contains("DEMO_RULE"));
  }

  private void expectNotDemoRuleId(Set<CategoryId> disabledCategories, Set<CategoryId> enabledCategories,
                                   Set<String> disabledRules, Set<String> enabledRules, boolean useEnabledOnly, Demo demo) {
    JLanguageTool lt = new JLanguageTool(demo);
    Tools.selectRules(lt, disabledCategories, enabledCategories, disabledRules, enabledRules, useEnabledOnly);
    assertFalse(getRuleIds(lt).contains("DEMO_RULE"));
  }

  private List<String> getRuleIds(JLanguageTool lt) {
    List<Rule> allActiveRules = lt.getAllActiveRules();
    return allActiveRules.stream().map(Rule::getId).collect(Collectors.toList());
  }

  private static class FakeRule extends PatternRule {
    FakeRule() {
      super("FAKE_ID", TestTools.getDemoLanguage(), Collections.singletonList(new PatternToken("foo", true, false, false)),
              "My fake description", "Fake message", "Fake short message");
    }
  }
  
}