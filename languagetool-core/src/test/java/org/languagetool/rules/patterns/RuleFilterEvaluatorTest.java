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
package org.languagetool.rules.patterns;

import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RuleFilterEvaluatorTest {

  private final RuleFilterEvaluator eval = new RuleFilterEvaluator(null);

  @Test
  public void testGetResolvedArguments() throws Exception {
    AnalyzedTokenReadings[] readingsList = {
            new AnalyzedTokenReadings(new AnalyzedToken("fake1", "pos", null), 0),
            new AnalyzedTokenReadings(new AnalyzedToken("fake2", "pos", null), 0)
    };
    Map<String,String> map = eval.getResolvedArguments("year:\\1 month:\\2", readingsList, Arrays.asList(1, 1));
    assertThat(map.get("year"), is("fake1"));
    assertThat(map.get("month"), is("fake2"));
    assertThat(map.size(), is(2));
  }

  @Test
  public void testGetResolvedArgumentsWithColon() throws Exception {
    AnalyzedTokenReadings[] readingsList = {
            new AnalyzedTokenReadings(new AnalyzedToken("fake1", "pos", null), 0),
    };
    Map<String,String> map = eval.getResolvedArguments("regex:(?:foo[xyz])bar", readingsList, Arrays.asList(1, 1));
    assertThat(map.get("regex"), is("(?:foo[xyz])bar"));
    assertThat(map.size(), is(1));
  }

  @Test(expected = RuntimeException.class)
  public void testDuplicateKey() throws Exception {
    AnalyzedTokenReadings[] readingsList = {
            new AnalyzedTokenReadings(new AnalyzedToken("fake1", "SENT_START", null), 0),
            new AnalyzedTokenReadings(new AnalyzedToken("fake1", "pos", null), 0),
            new AnalyzedTokenReadings(new AnalyzedToken("fake2", "pos", null), 0)
    };
    eval.getResolvedArguments("year:\\1 year:\\2", readingsList, Arrays.asList(1, 2));
  }

  @Test
  public void testNoBackReference() throws Exception {
    Map<String, String> args = eval.getResolvedArguments("year:2 foo:bar", null, Collections.<Integer>emptyList());
    Map<String, String> expected = new HashMap<>();
    expected.put("year", "2");
    expected.put("foo", "bar");
    assertThat(args, is(expected));
  }

  @Test(expected = RuntimeException.class)
  public void testTooLargeBackRef() throws Exception {
    eval.getResolvedArguments("year:\\1 month:\\2 day:\\3 weekDay:\\4", null, Collections.<Integer>emptyList());
  }

}
