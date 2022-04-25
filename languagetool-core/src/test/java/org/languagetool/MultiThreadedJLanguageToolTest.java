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

import org.junit.Test;
import org.languagetool.language.Demo;
import org.languagetool.rules.MultipleWhitespaceRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.UppercaseSentenceStartRule;
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SuppressWarnings("ResultOfObjectAllocationIgnored")
public class MultiThreadedJLanguageToolTest {

  @Test
  public void testCheck() throws IOException {
    MultiThreadedJLanguageTool lt1 = new MultiThreadedJLanguageTool(new Demo());
    lt1.setCleanOverlappingMatches(false);
    List<String> ruleMatchIds1 = getRuleMatchIds(lt1);
    assertEquals(9, ruleMatchIds1.size()); 
    lt1.shutdown();

    JLanguageTool lt2 = new JLanguageTool(new Demo());
    lt2.setCleanOverlappingMatches(false);
    List<String> ruleMatchIds2 = getRuleMatchIds(lt2);
    assertEquals(ruleMatchIds1, ruleMatchIds2);
  }
  
  @Test
  public void testShutdownException() throws IOException {
    MultiThreadedJLanguageTool tool = new MultiThreadedJLanguageTool(new Demo());
    getRuleMatchIds(tool);
    tool.shutdown();
    try {
      getRuleMatchIds(tool);
      fail("should have been rejected as the thread pool has been shut down");
    } catch (RejectedExecutionException ignore) {}
  }
  
  @Test
  public void testTextAnalysis() throws IOException {
    MultiThreadedJLanguageTool lt = new MultiThreadedJLanguageTool(new Demo());
    List<AnalyzedSentence> analyzedSentences = lt.analyzeText("This is a sentence. And another one.");
    assertThat(analyzedSentences.size(), is(2));
    assertThat(analyzedSentences.get(0).getTokens().length, is(10));
    assertThat(analyzedSentences.get(0).getTokensWithoutWhitespace().length, is(6));  // sentence start has its own token
    assertThat(analyzedSentences.get(1).getTokens().length, is(7));
    assertThat(analyzedSentences.get(1).getTokensWithoutWhitespace().length, is(5));
    lt.shutdown();
  }
  
  @Test
  public void testConfigurableThreadPoolSize() throws IOException {
    MultiThreadedJLanguageTool lt = new MultiThreadedJLanguageTool(new Demo());
    assertEquals(Runtime.getRuntime().availableProcessors(), lt.getThreadPoolSize());
    lt.shutdown();
  }

  private List<String> getRuleMatchIds(JLanguageTool lt) throws IOException {
    String input = "A small toast. No error here. Foo go bar. First goes last there, please!";
    List<RuleMatch> matches = lt.check(input);
    List<String> ruleMatchIds = new ArrayList<>();
    for (RuleMatch match : matches) {
      ruleMatchIds.add(match.getRule().getId());
    }
    return ruleMatchIds;
  }

  @Test
  public void testTwoRulesOnly() throws IOException {
    MultiThreadedJLanguageTool lt = new MultiThreadedJLanguageTool(new FakeLanguage() {
      @Override
      protected synchronized List<AbstractPatternRule> getPatternRules() {
        return Collections.emptyList();
      }

      @Override
      public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) {
        // fewer rules than processors (depending on the machine), should at least not crash
        return Arrays.asList(
                new UppercaseSentenceStartRule(messages, this),
                new MultipleWhitespaceRule(messages, this)
        );
      }
    });
    assertThat(lt.check("my test  text").size(), is(2));
    lt.shutdown();
  }

}
