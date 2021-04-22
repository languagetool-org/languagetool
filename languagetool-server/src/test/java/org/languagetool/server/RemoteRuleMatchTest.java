/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.server;

import org.junit.Test;
import org.languagetool.rules.FakeRule;
import org.languagetool.rules.RuleMatch;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class RemoteRuleMatchTest {
  
  @Test
  public void isTouchedByOneOf() throws Exception {
    List<RuleMatch> origMatches = Arrays.asList(
            new RuleMatch(new FakeRule(), null, 0, 3, "msg"),
            new RuleMatch(new FakeRule(), null, 10, 13, "msg")
    );
    assertTrue(match(0, 5).isTouchedByOneOf(origMatches));
    assertTrue(match(2, 3).isTouchedByOneOf(origMatches));
    
    assertFalse(match(4, 5).isTouchedByOneOf(origMatches));
    assertFalse(match(4, 9).isTouchedByOneOf(origMatches));
    
    assertTrue(match( 8, 10).isTouchedByOneOf(origMatches));
    assertTrue(match(10, 13).isTouchedByOneOf(origMatches));
    assertTrue(match(12, 13).isTouchedByOneOf(origMatches));
    assertTrue(match(12, 15).isTouchedByOneOf(origMatches));
    assertTrue(match( 8, 15).isTouchedByOneOf(origMatches));

    assertFalse(match(14, 20).isTouchedByOneOf(origMatches));
  }

  private RemoteRuleMatch match(int from, int to) {
    return new RemoteRuleMatch("R1", "msg", "...", 0, from, to-from);
  }

}