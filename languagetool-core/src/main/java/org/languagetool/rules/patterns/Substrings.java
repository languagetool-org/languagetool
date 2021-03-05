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

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * A list of strings that are expected to occur in a larger string in the given order,
 * with optional requirements for them to start/end the text.
 * This class is used to avoid more expensive regular expression matching where possible.
 */
class Substrings {
  final String[] substrings;
  private final int minLength;

  /** Whether the first of {@link #substrings} must occur at the matched fragment start */
  final boolean mustStart;

  /** Whether the last of {@link #substrings} must occur at the matched fragment end */
  final boolean mustEnd;

  Substrings(boolean mustStart, boolean mustEnd, String[] substrings) {
    this(mustStart, mustEnd, substrings, Arrays.stream(substrings).mapToInt(String::length).sum());
  }

  private Substrings(boolean mustStart, boolean mustEnd, String[] substrings, int minLength) {
    this.substrings = substrings;
    this.mustStart = mustStart;
    this.mustEnd = mustEnd;
    this.minLength = minLength;
  }

  /**
   * @return a version of this {@code Substrings} object
   * whose {@link #matches} could completely replace the given regexp,
   * or {@code null} if that's not feasible.
   */
  @Nullable
  Substrings checkCanReplaceRegex(String regexp) {
    if (mustStart || mustEnd) {
      String prefix = mustStart ? substrings[0] : "";
      String suffix = mustEnd ? substrings[substrings.length - 1] : "";
      if (regexp.startsWith(prefix) &&
          regexp.endsWith(suffix) &&
          regexp.length() == prefix.length() + suffix.length() + 2 &&
          regexp.charAt(prefix.length()) == '.') {
        switch (regexp.charAt(prefix.length() + 1)) {
          case '*': return this;
          case '+': return new Substrings(mustStart, mustEnd, substrings, minLength + 1);
        }
      }
    }
    if (regexp.equals((mustStart ? "" : ".*") + String.join(".*", substrings) + (mustEnd ? "" : ".*"))) {
      return this;
    }
    return null;
  }

  @Override
  public String toString() {
    return (mustStart ? "[" : "(") + String.join(", ", substrings) + (mustEnd ? "]" : ")");
  }

  Substrings concat(Substrings another) {
    String[] substrings;
    if (another.substrings.length == 0) {
      substrings = this.substrings;
    } else if (this.substrings.length == 0) {
      substrings = another.substrings;
    } else if (mustEnd && another.mustStart) {
      substrings = new String[this.substrings.length + another.substrings.length - 1];
      System.arraycopy(this.substrings, 0, substrings, 0, this.substrings.length - 1);
      substrings[this.substrings.length - 1] = this.substrings[this.substrings.length - 1] + another.substrings[0];
      System.arraycopy(another.substrings, 0, substrings, this.substrings.length, another.substrings.length - 1);
    } else {
      substrings = new String[this.substrings.length + another.substrings.length];
      System.arraycopy(this.substrings, 0, substrings, 0, this.substrings.length);
      System.arraycopy(another.substrings, 0, substrings, this.substrings.length, another.substrings.length);
    }
    return new Substrings(mustStart, another.mustEnd, substrings);
  }

  /**
   * Check whether {@code text} contains all required substrings and returns the index of the first of them,
   * or -1 if the text doesn't contain these substrings.
   */
  int find(String text, boolean caseSensitive) {
    if (text.length() < minLength) {
      return -1;
    }

    int start = indexOf(text, substrings[0], caseSensitive, 0);
    if (start < 0) {
      return -1;
    }

    if (substrings.length > 1 && !containsSubstrings(text, caseSensitive, start + substrings[0].length(), 1)) {
      return -1;
    }
    return start;
  }

  /**
   * @return whether the given text contains all the required substrings
   */
  boolean matches(String text, boolean caseSensitive) {
    if (text.length() < minLength) {
      return false;
    }
    if (mustStart && !text.regionMatches(!caseSensitive, 0, substrings[0], 0, substrings[0].length())) {
      return false;
    }
    if (mustEnd) {
      String last = substrings[substrings.length - 1];
      if (!text.regionMatches(!caseSensitive, text.length() - last.length(), last, 0, last.length())) {
        return false;
      }
    }

    if (substrings.length == 1 && (mustStart || mustEnd)) {
      return true;
    }
    return containsSubstrings(text, caseSensitive, mustStart ? substrings[0].length() : 0, mustStart ? 1 : 0);
  }

  private boolean containsSubstrings(String text, boolean caseSensitive, int textPos, int firstSubstringIndex) {
    for (int i = firstSubstringIndex; i < substrings.length; i++) {
      textPos = indexOf(text, substrings[i], caseSensitive, textPos);
      if (textPos < 0) {
        return false;
      }
      textPos += substrings[i].length();
    }
    return true;
  }

  private static int indexOf(String text, String substring, boolean caseSensitive, int from) {
    return caseSensitive ? text.indexOf(substring, from) : indexOfIgnoreCase(text, substring, from);
  }

  // a bit more optimized than Apache StringUtil.indexOfIgnoreCase
  private static int indexOfIgnoreCase(String text, String substring, int from) {
    char first = substring.charAt(0);
    char up = Character.toUpperCase(first);
    char low = Character.toLowerCase(first);
    boolean cased = up != first || low != first;
    while (true) {
      from = cased ? findNext(text, from, up, low) : text.indexOf(first, from);
      if (from < 0) {
        return -1;
      }

      if (text.regionMatches(true, from, substring, 0, substring.length())) {
        return from;
      }
      from++;
    }
  }

  private static int findNext(String text, int from, char up, char low) {
    for (int i = from; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == up || c == low || Character.toUpperCase(c) == up || Character.toLowerCase(c) == low) {
        return i;
      }
    }
    return -1;
  }
}
