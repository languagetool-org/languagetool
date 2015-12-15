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
package org.languagetool.rules.ngrams;

import org.junit.Test;
import org.languagetool.FakeLanguage;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneSingleIndexLanguageModel;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
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
    ConfusionProbabilityRule ruleWithException = new FakeRule(new FakeLanguageModel(), new FakeLanguage()) {
      @Override
      protected boolean isException(String sentenceText) {
        return sentenceText.contains("Their are");
      }
    };
    assertGood("Their are new ideas to explore.", ruleWithException);
  }

  @Test
  public void testGetContext() throws IOException {
    List<GoogleToken> tokens = Arrays.asList(
            new GoogleToken(LanguageModel.GOOGLE_SENTENCE_START, 0, 0),
            new GoogleToken("This", 0, 0),
            new GoogleToken("is", 0, 0),
            new GoogleToken("a", 0, 0),
            new GoogleToken("test", 0, 0)
    );
    GoogleToken token = tokens.get(3);
    assertThat(rule.getContext(token, tokens, "XX", 1, 1).toString(), is("[is, XX, test]"));
    assertThat(rule.getContext(token, tokens, "XX", 0, 2).toString(), is("[XX, test, .]"));
    assertThat(rule.getContext(token, tokens, "XX", 2, 0).toString(), is("[This, is, XX]"));
    assertThat(rule.getContext(token, tokens, "XX", 3, 0).toString(), is("[_START_, This, is, XX]"));
  }

  @Test
  public void testGetContext2() throws IOException {
    List<GoogleToken> tokens = Arrays.asList(
            new GoogleToken(LanguageModel.GOOGLE_SENTENCE_START, 0, 0),
            new GoogleToken("This", 0, 0),
            new GoogleToken("is", 0, 0)
    );
    GoogleToken token = tokens.get(2);
    assertThat(rule.getContext(token, tokens, "XX", 1, 1).toString(), is("[This, XX, .]"));
    assertThat(rule.getContext(token, tokens, "XX", 2, 1).toString(), is("[_START_, This, XX, .]"));
    assertThat(rule.getContext(token, tokens, "XX", 0, 2).toString(), is("[XX, ., .]"));
    assertThat(rule.getContext(token, tokens, "XX", 2, 0).toString(), is("[_START_, This, XX]"));
    assertThat(rule.getContext(token, tokens, "XX", 3, 0).toString(), is("[_START_, This, XX]"));
  }

  @Test
  public void testGetContext3() throws IOException {
    List<GoogleToken> tokens = Arrays.asList(
            new GoogleToken("This", 0, 0)
    );
    GoogleToken token = tokens.get(0);
    assertThat(rule.getContext(token, tokens, "XX", 1, 1).toString(), is("[XX]"));
    assertThat(rule.getContext(token, tokens, "XX", 0, 2).toString(), is("[XX, ., .]"));
    assertThat(rule.getContext(token, tokens, "XX", 2, 0).toString(), is("[XX]"));
    assertThat(rule.getContext(token, tokens, "XX", 3, 0).toString(), is("[XX]"));
  }

  @Test
  public void testGetContext4() throws IOException {
    List<GoogleToken> tokens = Arrays.asList(
            new GoogleToken(LanguageModel.GOOGLE_SENTENCE_START, 0, 0),
            new GoogleToken("This", 0, 0)
    );
    GoogleToken token = tokens.get(1);
    assertThat(rule.getContext(token, tokens, "XX", 1, 1).toString(), is("[_START_, XX, .]"));
    assertThat(rule.getContext(token, tokens, "XX", 0, 2).toString(), is("[XX, ., .]"));
    assertThat(rule.getContext(token, tokens, "XX", 2, 0).toString(), is("[_START_, XX]"));
    assertThat(rule.getContext(token, tokens, "XX", 3, 0).toString(), is("[_START_, XX]"));
  }

  private void assertMatch(String input, Rule rule) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertThat("Did not find match in: " + input, matches.length, is(1));
  }

  private void assertMatch(String input) throws IOException {
    assertMatch(input, rule);
  }

  private void assertGood(String input, Rule rule) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertThat("Got unexpected match in: " + input, matches.length, is(0));
  }

  private void assertGood(String input) throws IOException {
    assertGood(input, rule);
  }

  static class FakeLanguageModel extends LuceneSingleIndexLanguageModel {
    static Map<String,Integer> map = new HashMap<>();
    FakeLanguageModel() {
      super(3);
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
    public void doValidateDirectory(File topIndexDir) {
    }
    @Override
    public long getCount(List<String> tokens) {
      Integer count = map.get(String.join(" ", tokens));
      return count == null ? 0 : count;
    }
    @Override
    public long getCount(String token1) {
      return getCount(Arrays.asList(token1));
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
    private FakeRule(LanguageModel languageModel, Language language) {
      super(JLanguageTool.getMessageBundle(), languageModel, language);
    }
  }

}
