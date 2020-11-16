package org.languagetool.rules.patterns;

import com.google.common.collect.Sets;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.languagetool.rules.patterns.StringMatcher.getPossibleRegexpValues;

public class StringMatcherTest {
  @Test
  public void testGetPossibleValues() {
    assertNull(getPossibleRegexpValues("x.*"));
    assertNull(getPossibleRegexpValues("x+"));
    assertNull(getPossibleRegexpValues("^x$"));
    assertNull(getPossibleRegexpValues("a.c"));
    assertNull(getPossibleRegexpValues("a{2}"));
    assertNull(getPossibleRegexpValues("[a-z]"));
    assertNull(getPossibleRegexpValues("[^a]"));
    assertNull(getPossibleRegexpValues("a[a-z]"));
    assertNull(getPossibleRegexpValues("(?=a)"));

    assertPossibleValues("aa|bb", "aa", "bb");
    assertPossibleValues("aa", "aa");
    assertPossibleValues("aa?", "aa", "a");
    assertPossibleValues("[abc]", "a", "b", "c");
  }

  private static void assertPossibleValues(String regexp, String... expected) {
    assertEquals(Sets.newHashSet(expected), getPossibleRegexpValues(regexp));
  }

}
