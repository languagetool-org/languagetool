/* LanguageTool, a natural language style checker
 * Copyright (C) 2025 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.language.Demo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class DemoRuleTest {

  @Test
  public void testDemoRule() throws IOException {
    JLanguageTool lt1 = new JLanguageTool(new DemoLang1());
    assertThat(lt1.check("This is a sentence").size(), is(0));
    assertThat(lt1.check("This is a foobar sentence").size(), is(1));
    JLanguageTool lt2 = new JLanguageTool(new DemoLang2());
    assertThat(lt2.check("This is a sentence").size(), is(0));
    assertThat(lt2.check("This is a foobar sentence").size(), is(0));  // DEMO_RULE_FOR_TESTS turned off in DemoLang2
  }

  public static class DemoRule extends Rule {
    @Override
    public String getId() {
      return "DEMO_RULE_FOR_TESTS";
    }
    @Override
    public String getDescription() {
      return "A demo rule for tests";
    }
    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
      if (sentence.getText().contains("foobar")) {
        return new RuleMatch[] { new RuleMatch(this, sentence, 0, 1, "A demo rule match somewhere in the input") };
      }
      return RuleMatch.EMPTY_ARRAY;
    }
  }

  public static class DemoLang1 extends Demo {
    @Override
    public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) {
      return Collections.singletonList(new DemoRule());
    }
    @Override
    public List<String> getDefaultDisabledRulesForVariant() {
      return Arrays.asList("test_unification_with_negation");
    }
  }

  public static class DemoLang2 extends DemoLang1 {
    @Override
    public List<String> getDefaultDisabledRulesForVariant() {
      return Arrays.asList("DEMO_RULE_FOR_TESTS", "test_unification_with_negation");
    }
  }
}
