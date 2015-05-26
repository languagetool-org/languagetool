/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import org.junit.Test;
import org.languagetool.*;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ConfusionProbabilityRuleTest {

  private final ConfusionProbabilityRule rule = new FakeRule(new FakeLanguageModel(), new FakeLanguage());
  private final JLanguageTool lt = new JLanguageTool(new FakeLanguage());

  @Test
  public void testRule() throws IOException {
    assertMatch("Their are new ideas to explore.");
    assertGood("There are new ideas to explore.");
    assertMatch("Why is there car broken again?");
    assertGood("Why is their car broken again?");
    assertGood("Is this their useful test?");
    assertGood("Is this there useful test?");  // error not found b/c no data 
  }

  @Test
  public void testPseudoProbability() throws IOException {
    ConfusionProbabilityRule.Probability prob1 = rule.getPseudoProbability(Arrays.asList("no", "data", "here"));
    double delta = 0.001;
    assertEquals(0.010, prob1.prob, delta);  // artificially not zero
    assertThat(prob1.coverage, is(0.0f));
    ConfusionProbabilityRule.Probability prob2 = rule.getPseudoProbability(Arrays.asList("1", "2", "3", "4"));
    assertEquals(0.010, prob2.prob, delta);  // artificially not zero
    assertThat(prob2.coverage, is(0.0f));
    ConfusionProbabilityRule.Probability prob3 = rule.getPseudoProbability(Arrays.asList("There", "are"));
    assertEquals(0.119, prob3.prob, delta);
    assertThat(prob3.coverage, is(0.5f));
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testPseudoProbabilityFail1() throws IOException {
    rule.getPseudoProbability(Collections.<String>emptyList());
  }

  @Test
  public void testGetContext() throws IOException {
    List<ConfusionProbabilityRule.GoogleToken> tokens = Arrays.asList(
            new ConfusionProbabilityRule.GoogleToken("This", 0, 0),
            new ConfusionProbabilityRule.GoogleToken("is", 0, 0),
            new ConfusionProbabilityRule.GoogleToken("a", 0, 0),
            new ConfusionProbabilityRule.GoogleToken("test", 0, 0)
    );
    ConfusionProbabilityRule.GoogleToken token = tokens.get(2);
    assertThat(rule.getContext(token, tokens, "XX", 1, 1).toString(), is("[is, XX, test]"));
    assertThat(rule.getContext(token, tokens, "XX", 0, 2).toString(), is("[XX, test, .]"));
    assertThat(rule.getContext(token, tokens, "XX", 2, 0).toString(), is("[This, is, XX]"));
    assertThat(rule.getContext(token, tokens, "XX", 3, 0).toString(), is("[This, is, a]"));
  }

  private void assertMatch(String input) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertThat(matches.length, is(1));
  }

  private void assertGood(String input) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertThat(matches.length, is(0));
  }

  static class FakeLanguageModel implements LanguageModel {
    Map<String,Integer> map = new HashMap<>();
    FakeLanguageModel() {
      // for "Their are new ideas to explore":
      map.put("There are", 10);
      map.put("There are new", 5);
      map.put("Their are", 2);
      map.put("Their are new", 1);
      // for "Why is there car broken again?"
      map.put("Why is", 50);
      map.put("Why is there", 5);
      map.put("Why is their", 5);
      map.put("their car", 11);
      map.put("their car broken", 2);
    }
    @Override
    public long getCount(List<String> tokens) {
      Integer count = map.get(StringTools.listToString(tokens, " "));
      return count == null ? 0 : count;
    }
    @Override
    public long getCount(String token1) {
      return getCount(Arrays.asList(token1));
    }
    @Override
    public long getCount(String token1, String token2) {
      return getCount(Arrays.asList(token1, token2));
    }
    @Override
    public long getCount(String token1, String token2, String token3) {
      return getCount(Arrays.asList(token1, token2, token3));
    }
    @Override
    public long getTotalTokenCount() {
      int sum = 0;
      for (int val : map.values()) {
        sum += val;
      }
      return sum;
    }
    @Override
    public void close() {}
  }

  private static class FakeRule extends ConfusionProbabilityRule {
    private final Language language;
    private FakeRule(LanguageModel languageModel, Language language) {
      super(JLanguageTool.getMessageBundle(), languageModel, language);
      this.language = language;
    }
    @Override
    protected WordTokenizer getTokenizer() {
      return (WordTokenizer) language.getWordTokenizer();
    }
    @Override public String getDescription() { return null; }
    @Override public String getMessage(String suggestion, String description) { return null; }
  }

}
