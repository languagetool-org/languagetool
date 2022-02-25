/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Peter Gromov
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

import com.google.common.collect.Sets;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.languagetool.rules.patterns.StringMatcher.getPossibleRegexpValues;

public class StringMatcherTest {

  @Test(expected = PatternSyntaxException.class)
  public void syntaxIsValidated() {
    StringMatcher.regexp("tú|?");
  }

  @Test
  public void testGetPossibleValues() {
    assertPossibleValues("x.*");
    assertPossibleValues("x+");
    assertPossibleValues("a.c");
    assertPossibleValues("a{2}");
    assertPossibleValues("[a-z]");
    assertPossibleValues("[^a]");
    assertPossibleValues("a[a-z]");
    assertPossibleValues("(?=a)");

    assertPossibleValues("", "");
    assertPossibleValues("^x$", "x");
    assertPossibleValues("aa|bb", "aa", "bb");
    assertPossibleValues("aa", "aa");
    assertPossibleValues("aa|", "aa", "");
    assertPossibleValues("aa?", "aa", "a");
    assertPossibleValues("[abc]", "a", "b", "c");
    assertPossibleValues("[abc]", "a", "b", "c");
    assertPossibleValues("a(bc)?", "a", "abc");
    assertPossibleValues("a(b|c)", "ab", "ac");
    assertPossibleValues("(a|b)(c|d)", "ac", "ad", "bc", "bd");
    assertPossibleValues("\\.|\\-", ".", "-");
    assertPossibleValues("[\\.\\-]", ".", "-");
    assertPossibleValues("are|is|w(?:as|ere)", "are", "is", "was", "were");
    assertPossibleValues("[,;:-]", ",", ";", ":", "-");
    assertPossibleValues("(?:[-‑])", "-", "‑");
    assertPossibleValues("[0-9]", IntStream.range(0, 10).mapToObj(String::valueOf).toArray(String[]::new));
    assertPossibleValues("[0-9X]", Stream.concat(IntStream.range(0, 10).mapToObj(String::valueOf), Stream.of("X")).toArray(String[]::new));
    assertPossibleValues("tú|\\?", "tú", "?");
    assertPossibleValues("NN|PRP\\$", "NN", "PRP$");
    assertPossibleValues("\\\\b", "\\b");
  }

  private static void assertPossibleValues(String regexp, String... expected) {
    assertEquals(expected.length == 0 ? null : Sets.newHashSet(expected), getPossibleRegexpValues(regexp));

    for (String s : expected) {
      trySomeMutations(regexp, s);
    }
    trySomeMutations(regexp, regexp);
  }

  private static void trySomeMutations(String regexp, String s) {
    assertStringMatcherConsistentWithPattern(regexp, s);
    for (int i = 0; i < s.length(); i++) {
      assertStringMatcherConsistentWithPattern(regexp, s.substring(0, i) + s.substring(i + 1));
      assertStringMatcherConsistentWithPattern(regexp, s.substring(0, i) + "x" + s.substring(i + 1));
    }
    assertStringMatcherConsistentWithPattern(regexp, "a" + s);
    assertStringMatcherConsistentWithPattern(regexp, s + "a");
  }

  private static void assertStringMatcherConsistentWithPattern(String regexp, String s) {
    assertEquals(StringMatcher.regexp(regexp).matches(s),
      s.matches(regexp));
    assertEquals(StringMatcher.create(regexp, true, false).matches(s),
      Pattern.compile(regexp, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(s).matches());
  }

  @Test
  public void requiredSubstrings() {
    assertRequiredSubstrings("", "[]");
    assertRequiredSubstrings("foo", "[foo]");
    assertRequiredSubstrings("foo|bar", null);
    assertRequiredSubstrings("\\w", null);
    assertRequiredSubstrings("PRP.+", "[PRP)");
    assertRequiredSubstrings(".*PRP.+", "(PRP)");
    assertRequiredSubstrings(".*PRP", "(PRP]");
    assertRequiredSubstrings(".+PRP", "(PRP]");
    assertRequiredSubstrings("a.+b", "[a, b]");
    assertRequiredSubstrings("a.*b", "[a, b]");
    assertRequiredSubstrings("\\bZünglein an der (Wage)\\b", "[Zünglein an der Wage]");
    assertRequiredSubstrings("(ökumenische[rn]?) (.*Messen?)", "[ökumenische,  , Messe)");
    assertRequiredSubstrings("(CO2|Kohlendioxid|Schadstoff)\\-?Emulsion(en)?", "(Emulsion)");
    assertRequiredSubstrings("\\bder (\\w*(Verkehrs|Verbots|Namens|Hinweis|Warn)schild)", "[der , schild]");
    assertRequiredSubstrings("\\bvon Seiten\\b", "[von Seiten]");
    assertRequiredSubstrings("((\\-)?[0-9]+[0-9.,]{0,15})(?:[\\s  ]+)(°[^CFK])", "(°)");
    assertRequiredSubstrings("\\b(teils\\s[^,]+\\steils)\\b", "[teils, teils]");
    assertRequiredSubstrings("§ ?(\\d+[a-z]?)", "[§)");
  }

  @Test
  public void noSOEOnLongDisjunction() {
    int count = 100_000;
    String pattern = IntStream.range(0, count).mapToObj(i -> "a" + i).collect(Collectors.joining("|"));
    StringMatcher matcher = StringMatcher.create(pattern, true, true);
    for (int i = 0; i < count; i++) {
      assertTrue(matcher.matches("a" + i));
      assertFalse(matcher.matches("b" + i));
    }
  }

  private static void assertRequiredSubstrings(String regexp, @Nullable String expected) {
    Substrings actual = StringMatcher.getRequiredSubstrings(regexp);
    assertEquals(expected, actual == null ? null : actual.toString());

    trySomeMutations(regexp, regexp);
    if (expected != null) {
      trySomeMutations(regexp, expected);
      trySomeMutations(regexp, expected.substring(1, expected.length() - 1));
    }
    if (actual != null) {
      for (String separator: Arrays.asList("", "a", "0", " ")) {
        trySomeMutations(regexp, String.join(separator, actual.substrings));
        trySomeMutations(regexp, separator + String.join(separator, actual.substrings));
        trySomeMutations(regexp, String.join(separator, actual.substrings) + separator);
      }
    }
  }
}
