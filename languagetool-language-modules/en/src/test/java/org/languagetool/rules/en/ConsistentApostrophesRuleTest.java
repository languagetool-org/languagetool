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
package org.languagetool.rules.en;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class ConsistentApostrophesRuleTest {

  @Test
  public void testRule() throws IOException {
    ConsistentApostrophesRule rule = new ConsistentApostrophesRule(TestTools.getEnglishMessages());
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("en"));
    RuleMatch[] matches = rule.match(lt.analyzeText("It's a nice idea. But it doesn’t work."));
    assertThat(matches.length, is(2));
    assertThat(matches[0].getFromPos(), is(2));
    assertThat(matches[0].getToPos(), is(4));
    assertThat(matches[0].getSuggestedReplacements().toString(), is("[’s]"));
    assertThat(matches[1].getFromPos(), is(29));
    assertThat(matches[1].getToPos(), is(32));
    assertThat(matches[1].getSuggestedReplacements().toString(), is("[n't]"));

    assertThat(rule.match(lt.analyzeText("It’s a nice idea. But it doesn't work.")).length, is(2));
    assertThat(rule.match(lt.analyzeText("It's a nice idea. But it doesn't work.")).length, is(0));
    assertThat(rule.match(lt.analyzeText("It’s a nice idea. But it doesn’t work.")).length, is(0));
  }

}