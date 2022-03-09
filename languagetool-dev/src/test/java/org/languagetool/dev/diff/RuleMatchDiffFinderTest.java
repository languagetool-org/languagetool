/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.diff;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RuleMatchDiffFinderTest {

  private final RuleMatchDiffFinder diffFinder = new RuleMatchDiffFinder();

  @Test
  public void testNoDiff() {
    List<LightRuleMatch> l1 = new ArrayList<>();
    l1.add(make("my message", "context", "covered text", "suggestion"));
    List<LightRuleMatch> l2 = new ArrayList<>();
    l2.add(make("my message", "context", "covered text", "suggestion"));
    assertThat(diffFinder.getDiffs(l1, l2).toString(), is("[]"));
  }

  @Test
  public void testAddedMatch() {
    List<LightRuleMatch> l1 = new ArrayList<>();
    List<LightRuleMatch> l2 = new ArrayList<>();
    l2.add(make("my message", "context", "covered text", "suggestion"));
    assertThat(diffFinder.getDiffs(l1, l2).toString(),
      is("[ADDED: oldMatch=null, newMatch=1/10 FAKE_ID1[null], msg=my message, covered=covered text, suggestions=[suggestion], title=mytitle, ctx=context]"));
  }

  @Test
  public void testRemovedMatch() {
    List<LightRuleMatch> l1 = new ArrayList<>();
    l1.add(make("my message", "context", "covered text", "suggestion"));
    List<LightRuleMatch> l2 = new ArrayList<>();
    assertThat(diffFinder.getDiffs(l1, l2).toString(),
      is("[REMOVED: oldMatch=1/10 FAKE_ID1[null], msg=my message, covered=covered text, suggestions=[suggestion], title=mytitle, ctx=context, newMatch=null]"));
  }

  @Test
  public void testModifiedMessage() {
    List<LightRuleMatch> l1 = new ArrayList<>();
    l1.add(make("my message", "context", "covered text", "suggestion"));
    List<LightRuleMatch> l2 = new ArrayList<>();
    l2.add(make("my modified message", "context", "covered text", "suggestion"));
    assertThat(diffFinder.getDiffs(l1, l2).toString(),
      is("[MODIFIED: oldMatch=1/10 FAKE_ID1[null], msg=my message, covered=covered text, suggestions=[suggestion], title=mytitle, ctx=context, " +
                    "newMatch=1/10 FAKE_ID1[null], msg=my modified message, covered=covered text, suggestions=[suggestion], title=mytitle, ctx=context]"));
  }

  @Test
  public void testModifiedSuggestions() {
    List<LightRuleMatch> l1 = new ArrayList<>();
    l1.add(make("my message", "context", "covered text", "suggestion"));
    List<LightRuleMatch> l2 = new ArrayList<>();
    l2.add(make("my message", "context", "covered text", "modified suggestion"));
    assertThat(diffFinder.getDiffs(l1, l2).toString(), 
      is("[MODIFIED: oldMatch=1/10 FAKE_ID1[null], msg=my message, covered=covered text, suggestions=[suggestion], title=mytitle, ctx=context, " +
                    "newMatch=1/10 FAKE_ID1[null], msg=my message, covered=covered text, suggestions=[modified suggestion], title=mytitle, ctx=context]"));
  }

  @Test
  public void testModifiedCoveredText() {
    List<LightRuleMatch> l1 = new ArrayList<>();
    l1.add(make("my message", "context", "covered text", "suggestion"));
    List<LightRuleMatch> l2 = new ArrayList<>();
    l2.add(make("my message", "context", "modified covered text", "suggestion"));
    assertThat(diffFinder.getDiffs(l1, l2).toString(),
      is("[ADDED: oldMatch=null, newMatch=1/10 FAKE_ID1[null], msg=my message, covered=modified covered text, suggestions=[suggestion], title=mytitle, ctx=context, " +
         "REMOVED: oldMatch=1/10 FAKE_ID1[null], msg=my message, covered=covered text, suggestions=[suggestion], title=mytitle, ctx=context, newMatch=null]"));
  }

  @NotNull
  private LightRuleMatch make(String msg, String context, String coveredText, String suggestion) {
    return new LightRuleMatch(1, 10, "FAKE_ID1", msg, "FakeCategory", context, coveredText, Arrays.asList(suggestion), "grammar.xml", "mytitle",
      LightRuleMatch.Status.on, Collections.emptyList());
  }

}
