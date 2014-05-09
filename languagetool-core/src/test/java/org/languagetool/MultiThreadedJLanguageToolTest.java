/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.languagetool.language.Demo;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.UppercaseSentenceStartRule;
import org.languagetool.rules.WhitespaceRule;

public class MultiThreadedJLanguageToolTest {

  @Test
  public void testCheck() throws IOException {
    JLanguageTool tool;
    
    tool = new MultiThreadedJLanguageTool(new Demo());
    final List<String> ruleMatchIds1 = getRuleMatchIds(tool);
    assertTrue(ruleMatchIds1.size() == 10);
    Assert.assertEquals(4, tool.getSentenceCount());
    
    tool = new JLanguageTool(new Demo());
    final List<String> ruleMatchIds2 = getRuleMatchIds(tool);
    assertThat(ruleMatchIds1, is(ruleMatchIds2));
    Assert.assertEquals(4, tool.getSentenceCount());
  }
  
  @Test
  public void testTextAnalysis() throws IOException {
    JLanguageTool tool = new MultiThreadedJLanguageTool(new Demo());
    List<AnalyzedSentence> analyzedSentences = tool.analyzeText("This is a sentence. And another one.");
    assertThat(analyzedSentences.size(), is(2));
    assertThat(analyzedSentences.get(0).getTokens().length, is(10));
    assertThat(analyzedSentences.get(0).getTokensWithoutWhitespace().length, is(6));  // sentence start has its own token
    assertThat(analyzedSentences.get(1).getTokens().length, is(7));
    assertThat(analyzedSentences.get(1).getTokensWithoutWhitespace().length, is(5));
  }
  
  @Test
  public void testConfigurableThreadPoolSize() throws IOException {
    MultiThreadedJLanguageTool tool = new MultiThreadedJLanguageTool(new Demo());
    Assert.assertEquals(Runtime.getRuntime().availableProcessors(), tool.getThreadPoolSize());
    
    tool.setThreadPoolSize(100);
    Assert.assertEquals(100, tool.getThreadPoolSize());

    tool.setThreadPoolSize(Integer.MIN_VALUE);
    Assert.assertEquals(Runtime.getRuntime().availableProcessors(), tool.getThreadPoolSize());

    tool.setThreadPoolSize(0);
    Assert.assertEquals(Runtime.getRuntime().availableProcessors(), tool.getThreadPoolSize());

    tool.setThreadPoolSize(-1);
    Assert.assertEquals(Runtime.getRuntime().availableProcessors(), tool.getThreadPoolSize());
  }

  private List<String> getRuleMatchIds(JLanguageTool langTool) throws IOException {
    langTool.activateDefaultPatternRules();
    final String input = "A small toast. No error here. Foo go bar. First goes last there, please!";
    final List<RuleMatch> matches = langTool.check(input);
    final List<String> ruleMatchIds = new ArrayList<>();
    for (RuleMatch match : matches) {
      ruleMatchIds.add(match.getRule().getId());
    }
    return ruleMatchIds;
  }

  @Test
  public void testTwoRulesOnly() throws IOException {
    MultiThreadedJLanguageTool langTool = new MultiThreadedJLanguageTool(new FakeLanguage() {
      @Override
      public List<Class<? extends Rule>> getRelevantRules() {
        // less rules than processors (depending on the machine), should at least not crash
        return Arrays.asList(
                UppercaseSentenceStartRule.class,
                WhitespaceRule.class
        );
      }
    });
    assertThat(langTool.check("my test  text").size(), is(2));
  }
}
