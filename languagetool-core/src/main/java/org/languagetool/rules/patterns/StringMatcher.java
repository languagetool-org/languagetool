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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.tools.InterruptibleCharSequence;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An object encapsulating a text pattern and the way it's matched (case-sensitivity / regular expression),
 * plus some optimizations over standard regular expression matching.
 */
abstract class StringMatcher {
  final String pattern;
  final boolean caseSensitive;
  final boolean isRegExp;

  private StringMatcher(String pattern, boolean isRegExp, boolean caseSensitive) {
    this.pattern = pattern;
    this.caseSensitive = caseSensitive;
    this.isRegExp = isRegExp;
  }

  /**
   * @return all values that this matcher can possibly accept (e.g. extracted from regexps like "foo|bar"),
   * or {@code null} if it's not possible to determine those.
   */
  @Nullable
  abstract Set<String> getPossibleValues();

  /**
   * @return whether the given string is accepted by this matcher.
   */
  abstract boolean matches(String s);

  static StringMatcher create(String pattern, boolean isRegExp, boolean caseSensitive) {
    return create(pattern, isRegExp, caseSensitive, Function.identity());
  }

  static StringMatcher create(String pattern, boolean isRegExp, boolean caseSensitive, Function<String, String> internString) {
    if (!isRegExp || "\\0".equals(pattern)) {
      return stringEquals(pattern, isRegExp, caseSensitive);
    }

    Set<String> possibleRegexpValues = getPossibleRegexpValues(pattern);
    if (possibleRegexpValues != null) {
      Set<String> set = possibleRegexpValues.stream().map(internString).collect(Collectors.toSet());
      if (set.size() == 1) {
        return stringEquals(set.iterator().next(), true, caseSensitive);
      }
      if (!caseSensitive) {
        String[] sorted = set.toArray(new String[0]);
        Arrays.sort(sorted, String.CASE_INSENSITIVE_ORDER);
        return new StringMatcher(pattern, true, false) {
          @Override
          Set<String> getPossibleValues() {
            return Sets.newHashSet(sorted);
          }

          @Override
          boolean matches(String s) {
            return Arrays.binarySearch(sorted, s, String.CASE_INSENSITIVE_ORDER) >= 0;
          }
        };
      }
      return new StringMatcher(pattern, true, true) {
        @Override
        Set<String> getPossibleValues() {
          return Collections.unmodifiableSet(set);
        }

        @Override
        boolean matches(String s) {
          return set.contains(s);
        }
      };
    }

    Pattern compiled = Pattern.compile(pattern, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    return new StringMatcher(pattern, true, caseSensitive) {
      @Nullable
      @Override
      Set<String> getPossibleValues() {
        return null;
      }

      @Override
      boolean matches(String s) {
        return compiled.matcher(new InterruptibleCharSequence(s)).matches();
      }
    };
  }

  @NotNull
  private static StringMatcher stringEquals(String pattern, final boolean isRegExp, boolean caseSensitive) {
    return new StringMatcher(pattern, isRegExp, caseSensitive) {
      @Override
      Set<String> getPossibleValues() {
        return Collections.singleton(pattern);
      }

      @Override
      boolean matches(String s) {
        return caseSensitive ? s.equals(pattern) : s.equalsIgnoreCase(pattern);
      }
    };
  }

  @Nullable
  @VisibleForTesting
  static Set<String> getPossibleRegexpValues(String stringToken) {
    if (StringUtils.containsAny(stringToken, "()*+.\\^${}")) {
      return null;
    }
    Set<String> result = new HashSet<>();
    Queue<String> unprocessed = new ArrayDeque<>(Arrays.asList(stringToken.split("\\|")));
    while (!unprocessed.isEmpty()) {
      String regex = unprocessed.poll();
      int lBracket = regex.indexOf('[');
      if (lBracket >= 0) {
        int rBracket = regex.indexOf(']', lBracket + 1);
        if (rBracket < 0) {
          return null;
        }
        for (int i = lBracket + 1; i < rBracket; i++) {
          char c = regex.charAt(i);
          if (c == '-') return null;
          unprocessed.add(regex.substring(0, lBracket) + c + regex.substring(rBracket + 1));
        }
        continue;
      }

      int question = regex.indexOf('?');
      if (question <= 0) {
        result.add(regex);
      } else {
        unprocessed.add(regex.substring(0, question) + regex.substring(question + 1));
        unprocessed.add(regex.substring(0, question - 1) + regex.substring(question + 1));
      }
    }
    return result;
  }


}
