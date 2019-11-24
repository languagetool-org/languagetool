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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RuleMatchDiffFinderTest {

  private final RuleMatchDiffFinder diffFinder = new RuleMatchDiffFinder();

  @Test
  public void testNoDiff() {
    List<LightRuleMatch> l1 = new ArrayList<>();
    l1.add(new LightRuleMatch(1, 10, "FAKE_ID1", "my message", "covered text", "suggestion"));
    List<LightRuleMatch> l2 = new ArrayList<>();
    l2.add(new LightRuleMatch(1, 10, "FAKE_ID1", "my message", "covered text", "suggestion"));
    assertThat(diffFinder.getDiffs(l1, l2).toString(), is("[]"));
  }

  @Test
  public void testAddedMatch() {
    List<LightRuleMatch> l1 = new ArrayList<>();
    List<LightRuleMatch> l2 = new ArrayList<>();
    l2.add(new LightRuleMatch(1, 10, "FAKE_ID1", "my message", "covered text", "suggestion"));
    assertThat(diffFinder.getDiffs(l1, l2).toString(),
      is("[ADDED: oldMatch=null, newMatch=1/10 FAKE_ID1, msg=my message, covered=covered text, suggestions=suggestion]"));
  }

  @Test
  public void testRemovedMatch() {
    List<LightRuleMatch> l1 = new ArrayList<>();
    l1.add(new LightRuleMatch(1, 10, "FAKE_ID1", "my message", "covered text", "suggestion"));
    List<LightRuleMatch> l2 = new ArrayList<>();
    assertThat(diffFinder.getDiffs(l1, l2).toString(),
      is("[REMOVED: oldMatch=1/10 FAKE_ID1, msg=my message, covered=covered text, suggestions=suggestion, newMatch=null]"));
  }

  @Test
  public void testModifiedMessage() {
    List<LightRuleMatch> l1 = new ArrayList<>();
    l1.add(new LightRuleMatch(1, 10, "FAKE_ID1", "my message", "covered text", "suggestion"));
    List<LightRuleMatch> l2 = new ArrayList<>();
    l2.add(new LightRuleMatch(1, 10, "FAKE_ID1", "my modified message", "covered text", "suggestion"));
    assertThat(diffFinder.getDiffs(l1, l2).toString(),
      is("[MODIFIED: oldMatch=1/10 FAKE_ID1, msg=my message, covered=covered text, suggestions=suggestion, " +
                    "newMatch=1/10 FAKE_ID1, msg=my modified message, covered=covered text, suggestions=suggestion]"));
  }

  @Test
  public void testModifiedSuggestions() {
    List<LightRuleMatch> l1 = new ArrayList<>();
    l1.add(new LightRuleMatch(1, 10, "FAKE_ID1", "my message", "covered text", "suggestion"));
    List<LightRuleMatch> l2 = new ArrayList<>();
    l2.add(new LightRuleMatch(1, 10, "FAKE_ID1", "my message", "covered text", "modified suggestion"));
    assertThat(diffFinder.getDiffs(l1, l2).toString(), 
      is("[MODIFIED: oldMatch=1/10 FAKE_ID1, msg=my message, covered=covered text, suggestions=suggestion, " +
                    "newMatch=1/10 FAKE_ID1, msg=my message, covered=covered text, suggestions=modified suggestion]"));
  }

  @Test
  public void testModifiedCoveredText() {
    List<LightRuleMatch> l1 = new ArrayList<>();
    l1.add(new LightRuleMatch(1, 10, "FAKE_ID1", "my message", "covered text", "suggestion"));
    List<LightRuleMatch> l2 = new ArrayList<>();
    l2.add(new LightRuleMatch(1, 10, "FAKE_ID1", "my message", "modified covered text", "suggestion"));
    assertThat(diffFinder.getDiffs(l1, l2).toString(),
      is("[MODIFIED: oldMatch=1/10 FAKE_ID1, msg=my message, covered=covered text, suggestions=suggestion, " +
                    "newMatch=1/10 FAKE_ID1, msg=my message, covered=modified covered text, suggestions=suggestion]"));
  }

}
