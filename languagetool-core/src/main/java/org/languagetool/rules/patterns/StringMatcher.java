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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.tools.InterruptibleCharSequence;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    ensureCorrectRegexp(pattern);

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

  private static final String unsupported = "?$^{}*+.";
  private static final String finishing = ")|";
  private static final String starting = "([\\";
  private static final String nonLiteral = finishing + unsupported + starting;

  @Nullable
  @VisibleForTesting
  static Set<String> getPossibleRegexpValues(String regexp) {
    return new Object() {
      int pos;

      private Stream<String> disjunction() {
        Stream<String> result = Stream.empty();
        while (true) {
          result = Stream.concat(result, concatenation());
          if (pos >= regexp.length() || regexp.charAt(pos) != '|') {
            return result;
          }
          pos++;
        }
      }

      private Stream<String> concatenation() {
        Stream<String> result = Stream.of("");

        while (pos < regexp.length()) {
          int literalStart = pos;
          while (pos < regexp.length() && nonLiteral.indexOf(regexp.charAt(pos)) < 0) pos++;
          if (literalStart < pos && pos < regexp.length() && regexp.charAt(pos) == '?') pos--;
          if (literalStart < pos) {
            String literal = regexp.substring(literalStart, pos);
            result = result.map(s -> s + literal);
            continue;
          }

          char c = regexp.charAt(pos);
          if (finishing.indexOf(c) >= 0) break;
          if (unsupported.indexOf(c) >= 0) throw TooComplexRegexp.INSTANCE;

          List<String> groupResults = atom().collect(Collectors.toList());
          if (pos < regexp.length() && regexp.charAt(pos) == '?') {
            pos++;
            result = result.flatMap(s1 -> Stream.concat(Stream.of(s1), groupResults.stream().map(s2 -> s1 + s2)));
          } else {
            result = result.flatMap(s1 -> groupResults.stream().map(s2 -> s1 + s2));
          }
        }
        return result;
      }

      private Stream<String> atom() {
        switch (regexp.charAt(pos)) {
          case '(':
            if (regexp.charAt(++pos) == '?') {
              if (regexp.charAt(++pos) != ':') {
                throw TooComplexRegexp.INSTANCE;
              }
              pos++;
            }
            Stream<String> group = disjunction();
            if (regexp.charAt(pos++) != ')') throw TooComplexRegexp.INSTANCE;
            return group;
          case '[':
            pos++;
            int start = pos;
            List<String> options = new ArrayList<>();
            while (true) {
              char c1 = regexp.charAt(pos++);
              if (c1 == ']') break;

              if (c1 == '-' && pos != start + 1 && regexp.charAt(pos) != ']') {
                char last = options.get(options.size() - 1).charAt(0);
                char next = regexp.charAt(pos++);
                if (next == '\\' || next - last > 10) {
                  throw TooComplexRegexp.INSTANCE;
                }
                for (int c = last + 1; c <= next; c++) {
                  options.add(String.valueOf((char)c));
                }
              } else if (c1 == '^' || c1 == '[') {
                throw TooComplexRegexp.INSTANCE;
              } else {
                options.add(String.valueOf(c1 == '\\' ? escape() : c1));
              }
            }
            return options.stream();
          case '\\':
            pos++;
            return Stream.of(String.valueOf(escape()));
          default:
            return Stream.of(String.valueOf(regexp.charAt(pos++)));
        }
      }

      private char escape() {
        char next = regexp.charAt(pos++);
        if (Character.isLetterOrDigit(next)) throw TooComplexRegexp.INSTANCE;
        return next;
      }

      @Nullable
      Set<String> getPossibleValues() {
        try {
          return disjunction().collect(Collectors.toSet());
        } catch (TooComplexRegexp e) {
          return null;
        } catch (IndexOutOfBoundsException e) {
          ensureCorrectRegexp(regexp);
          throw e;
        }
      }
    }.getPossibleValues();
  }

  private static void ensureCorrectRegexp(String regexp) {
    //noinspection ResultOfMethodCallIgnored
    Pattern.compile(regexp);
  }

  private static class TooComplexRegexp extends RuntimeException {
    private static final TooComplexRegexp INSTANCE = new TooComplexRegexp();
  }

}
