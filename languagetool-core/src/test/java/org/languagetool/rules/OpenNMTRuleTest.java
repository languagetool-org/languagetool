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
package org.languagetool.rules;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class OpenNMTRuleTest {

  @Test
  @Ignore("only works when OpenNMT server is running")
  public void testRule() throws IOException {
    OpenNMTRule rule = new OpenNMTRule();
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("xx"));
    String input = "This were an example.";
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    // TODO: check result - depends on model...
    System.out.println("-->"  + Arrays.toString(matches));
  }
  
  @Test
  public void testGetFirstDiffPosition() throws IOException {
    OpenNMTRule r = new OpenNMTRule();
    testFirst(r, "", "", -1);
    testFirst(r, "a", "", 0);
    testFirst(r, "a", "a", -1);
    testFirst(r, "ab", "ab", -1);
    testFirst(r, "ab", "ba", 0);
    testFirst(r, "xa", "xb", 1);
    testFirst(r, "xax", "xbx", 1);
    testFirst(r, "xxxa", "xxxb", 3);
    testFirst(r, "xxxa", "xxx", 3);
    testFirst(r, "xxx", "xxxb", 3);
    testFirst(r, "xxxyyy", "xxxbyyy", 3);
    testFirst(r, "This were a example.", "This were an example.", 11);
  }

  @Test
  public void testGetLastDiffPosition() throws IOException {
    OpenNMTRule r = new OpenNMTRule();
    testLast(r, "", "", -1);
    testLast(r, "a", "", 1);
    testLast(r, "a", "a", -1);
    testLast(r, "ba", "a", 1);
    testLast(r, "a", "ba", 0);
    testLast(r, "xba", "bba", 1);
    testLast(r, "bba", "xba", 1);
    testLast(r, "aa", "aa", -1);
    testLast(r, "bbb", "bbb", -1);
    testLast(r, "bb", "b", 1);
    testLast(r, "bbb", "b", 2);
    //testLast(r, "bbb", "bb", 3);
    testLast(r, "b", "bb", 0);
    testLast(r, "b", "bbb", 0);
    //testLast(r, "bb", "bbb", 2);
    testLast(r, "This were a example.", "This were an example.", 11);
  }

  @Test
  public void testGetLeftWordBoundary() throws IOException {
    OpenNMTRule r = new OpenNMTRule();
    assertThat(r.getLeftWordBoundary("foo", 0), is(0));
    assertThat(r.getLeftWordBoundary("foo", 2), is(0));
    assertThat(r.getLeftWordBoundary("foo bar", 0), is(0));
    assertThat(r.getLeftWordBoundary("foo. bar", 2), is(0));
    assertThat(r.getLeftWordBoundary("foo. bar", 5), is(5));
    assertThat(r.getLeftWordBoundary("foo bar", 4), is(4));
    assertThat(r.getLeftWordBoundary("foo bar", 6), is(4));
    assertThat(r.getLeftWordBoundary(".föö.", 3), is(1));
  }

  @Test
  public void testGetRightWordBoundary() throws IOException {
    OpenNMTRule r = new OpenNMTRule();
    assertThat(r.getRightWordBoundary("foo", 0), is(3));
    assertThat(r.getRightWordBoundary("foo", 3), is(3));
    assertThat(r.getRightWordBoundary("foo bar", 0), is(3));
    assertThat(r.getRightWordBoundary("foo.", 0), is(3));
    assertThat(r.getRightWordBoundary("föö.", 0), is(3));
    assertThat(r.getRightWordBoundary("foo bar", 4), is(7));
  }
  
  private void testFirst(OpenNMTRule rule, String text1, String text2, int expectedResult) {
    assertThat(rule.getFirstDiffPosition(text1, text2), is(expectedResult));
    assertThat(rule.getFirstDiffPosition(text2, text1), is(expectedResult));  // needs to be symmetrical
  }
  
  private void testLast(OpenNMTRule rule, String text1, String text2, int expectedResult) {
    assertThat(rule.getLastDiffPosition(text1, text2), is(expectedResult));
  }
  
}