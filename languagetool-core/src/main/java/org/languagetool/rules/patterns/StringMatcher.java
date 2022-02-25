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
 * @since 5.3
 */
public abstract class StringMatcher {
  
  final String pattern;
  final boolean caseSensitive;
  final boolean isRegExp;
  
  public final static int MAX_MATCH_LENGTH = 250;

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
  public abstract boolean matches(String s);

  /**
   * Create a case-sensitive regexp matcher.
   * @since 5.6
   */
  public static StringMatcher regexp(String pattern) {
    return create(pattern, true, true);
  }

  public static StringMatcher create(String pattern, boolean isRegExp, boolean caseSensitive) {
    return create(pattern, isRegExp, caseSensitive, Function.identity());
  }

  static StringMatcher create(String pattern, boolean isRegExp, boolean caseSensitive, Function<String, String> internString) {
    if (!isRegExp || "\\0".equals(pattern)) {
      return stringEquals(pattern, isRegExp, caseSensitive);
    }

    // always compile the pattern to check it's well-formed
    Pattern compiled = Pattern.compile(pattern, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

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
          public boolean matches(String s) {
            if (s.length() > MAX_MATCH_LENGTH) {
              return false;
            }
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
        public boolean matches(String s) {
          if (s.length() > MAX_MATCH_LENGTH) {
            return false;
          }
          return set.contains(s);
        }
      };
    }

    Substrings required = getRequiredSubstrings(pattern);
    Substrings exhaustive = required == null ? null : required.checkCanReplaceRegex(pattern);
    boolean substringsAreSufficient = exhaustive != null;
    Substrings substrings = substringsAreSufficient ? exhaustive : required;

    return new StringMatcher(pattern, true, caseSensitive) {
      @Nullable
      @Override
      Set<String> getPossibleValues() {
        return null;
      }

      @Override
      public boolean matches(String s) {
        if (s.length() > MAX_MATCH_LENGTH) {
          return false;
        }
        if (substrings != null && !substrings.matches(s, caseSensitive)) return false;
        if (substringsAreSufficient) return true;
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
      public boolean matches(String s) {
        if (s.length() > MAX_MATCH_LENGTH) {
          return false;
        }
        return caseSensitive ? s.equals(pattern) : s.equalsIgnoreCase(pattern);
      }
    };
  }

  /**
   * @return the substrings that any text would necessarily contain or start/end with if it matches the given regexp,
   * or {@code null} if no such substrings can be found
   */
  @Nullable
  static Substrings getRequiredSubstrings(String regexp) {
    Substrings UNKNOWN = new Substrings(false, false, new String[0]);

    RegexpParser<Substrings> parser = new RegexpParser<Substrings>(regexp) {
      @Override
      Substrings handleConcatenation(Substrings left, Substrings right) {
        return left.concat(right);
      }

      @Override
      Substrings handleOr(List<Substrings> components) {
        return UNKNOWN;
      }

      @Override
      protected Substrings optional(Substrings groupResults, char op) {
        return UNKNOWN;
      }

      @Override
      protected Substrings unknown() {
        return UNKNOWN;
      }

      @Override
      protected Substrings literal(String literal) {
        return new Substrings(true, true, new String[]{literal});
      }
    };
    try {
      Substrings result = parser.disjunction();
      return result.substrings.length == 0 ? null : result;
    } catch (TooComplexRegexp e) {
      return null;
    }
  }

  /**
   * @return all strings that the given regexp can ever match, or {@code null} if such set couldn't be enumerated
   */
  @Nullable
  @VisibleForTesting
  static Set<String> getPossibleRegexpValues(String regexp) {
    RegexpParser<Stream<String>> parser = new RegexpParser<Stream<String>>(regexp) {
      @Override
      Stream<String> handleConcatenation(Stream<String> left, Stream<String> right) {
        List<String> groupResults = right.collect(Collectors.toList());
        return left.flatMap(s1 -> groupResults.stream().map(s2 -> s1 + s2));
      }

      @Override
      Stream<String> handleOr(List<Stream<String>> components) {
        return components.stream().flatMap(Function.identity());
      }

      @Override
      protected Stream<String> optional(Stream<String> groupResults, char op) {
        return op == '?' ? Stream.concat(Stream.of(""), groupResults) : unknown();
      }

      @Override
      protected Stream<String> unknown() {
        throw TooComplexRegexp.INSTANCE;
      }

      @Override
      protected Stream<String> literal(String literal) {
        return Stream.of(literal);
      }
    };
    try {
      return parser.disjunction().collect(Collectors.toSet());
    } catch (TooComplexRegexp e) {
      return null;
    }
  }

  private static abstract class RegexpParser<T> {
    private static final String unsupported = "?$^{}*+";
    private static final String finishing = ")|";
    private static final String starting = "([\\";
    private static final String nonLiteral = finishing + unsupported + starting + ".";

    private final String regexp;
    private int pos;

    RegexpParser(String regexp) {
      if (regexp.startsWith("\\b")) {
        regexp = regexp.substring(2);
      }
      if (regexp.startsWith("^")) {
        regexp = regexp.substring(1);
      }
      if (regexp.endsWith("\\b") && !regexp.endsWith("\\\\b")) {
        regexp = regexp.substring(0, regexp.length() - 2);
      }
      if (regexp.endsWith("$") && !regexp.endsWith("\\$")) {
        regexp = regexp.substring(0, regexp.length() - 1);
      }
      this.regexp = regexp;
    }

    T disjunction() {
      List<T> components = new ArrayList<>();
      components.add(concatenation());
      while (true) {
        if (pos >= regexp.length() || regexp.charAt(pos) != '|') {
          return components.size() == 1 ? components.get(0) : handleOr(components);
        }
        pos++;
        components.add(concatenation());
      }
    }

    abstract T handleOr(List<T> components);

    abstract T handleConcatenation(T left, T right);

    protected abstract T optional(T groupResults, char op);

    protected abstract T literal(String literal);

    protected abstract T unknown();

    private T concatenation() {
      T result = postfix();

      while (pos < regexp.length()) {
        char c = regexp.charAt(pos);
        if (finishing.indexOf(c) >= 0) break;
        if (unsupported.indexOf(c) >= 0) throw TooComplexRegexp.INSTANCE;

        result = handleConcatenation(result, postfix());
      }
      return result;
    }

    private T postfix() {
      T groupResults = atom();
      if (pos < regexp.length()) {
        char next = regexp.charAt(pos);
        if (next == '{') {
          int closing = regexp.indexOf('}', pos + 1);
          if (closing < 0) throw new AssertionError("Closing } expected after " + pos);
          pos = closing + 1;
          groupResults = unknown();
          if (pos >= regexp.length()) {
            return groupResults;
          }
          next = regexp.charAt(pos);
        }
        if ("*+?".indexOf(next) >= 0) {
          pos++;
          return optional(groupResults, next);
        }
      }
      return groupResults;
    }

    private T atom() {
      if (pos >= regexp.length()) return literal("");
      
      switch (regexp.charAt(pos)) {
        case '(':
          if (regexp.charAt(++pos) == '?') {
            if (regexp.charAt(++pos) != ':') {
              throw TooComplexRegexp.INSTANCE;
            }
            pos++;
          }
          T group = disjunction();
          if (regexp.charAt(pos++) != ')') throw TooComplexRegexp.INSTANCE;
          return group;
        case '[':
          return squareBracketGroup();
        case '\\':
          pos++;
          return charLiteral(escape());
        case '.':
          pos++;
          return unknown();
        default:
          int literalStart = pos;
          while (pos < regexp.length() && nonLiteral.indexOf(regexp.charAt(pos)) < 0) pos++;
          if (literalStart + 1 < pos && pos < regexp.length() && regexp.charAt(pos) == '?') pos--;
          return literal(regexp.substring(literalStart, pos));
      }
    }

    private T squareBracketGroup() {
      int start = ++pos;
      List<Character> options = new ArrayList<>();
      while (true) {
        char c1 = regexp.charAt(pos++);
        if (c1 == ']') break;

        if (c1 == '-' && pos != start + 1 && regexp.charAt(pos) != ']') {
          Character last = options == null ? null : options.get(options.size() - 1);
          char next = regexp.charAt(pos++);
          if (last == null || next == '\\' || next - last > 10) {
            options = null;
          }
          if (options != null) {
            for (int c = last + 1; c <= next; c++) {
              options.add((char) c);
            }
          }
        } else if (c1 == '^') {
          options = null;
        } else if (c1 == '[') {
          throw TooComplexRegexp.INSTANCE;
        } else {
          Character simpleChar = c1 == '\\' ? escape() : (Character) c1;
          if (options != null) {
            options.add(simpleChar);
          }
        }
      }
      if (options == null) return unknown();
      List<T> components = options.stream().map(c -> charLiteral(c)).collect(Collectors.toList());
      if (components.isEmpty()) throw TooComplexRegexp.INSTANCE;
      return components.size() == 1 ? components.get(0) : handleOr(components);
    }

    private T charLiteral(@Nullable Character c) {
      return c == null ? unknown() : literal(String.valueOf(c));
    }

    @Nullable
    private Character escape() {
      char next = regexp.charAt(pos++);
      if ("0xucpP".indexOf(next) >= 0) throw TooComplexRegexp.INSTANCE;
      if (Character.isLetterOrDigit(next)) return null;
      return next;
    }

  }

  private static class TooComplexRegexp extends RuntimeException {
    private static final TooComplexRegexp INSTANCE = new TooComplexRegexp();
  }

}
