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
import org.languagetool.tools.StringInterner;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.regex.PatternSyntaxException;

import static org.languagetool.tools.StringInterner.intern;

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

  private static final Map<String, IntPredicate> PROPERTY_PREDICATES = new HashMap<>();
  static {
    PROPERTY_PREDICATES.put("L", Character::isLetter);
    PROPERTY_PREDICATES.put("Ll", cp -> Character.getType(cp) == Character.LOWERCASE_LETTER);
    PROPERTY_PREDICATES.put("Lu", cp -> Character.getType(cp) == Character.UPPERCASE_LETTER);
    PROPERTY_PREDICATES.put("N", cp -> Character.getType(cp) >= Character.DECIMAL_DIGIT_NUMBER
                                      && Character.getType(cp) <= Character.OTHER_NUMBER);
    PROPERTY_PREDICATES.put("P", StringMatcher::isPunctuation);
    // add more here later (e.g. others) using the same map
  }

  private StringMatcher(String pattern, boolean isRegExp, boolean caseSensitive) {
    this.pattern = intern(pattern);
    this.caseSensitive = caseSensitive;
    this.isRegExp = isRegExp;
  }

  /**
   * @return all values that this matcher can possibly accept (e.g. extracted from regexps like "foo|bar"),
   * or {@code null} if it's not possible to determine those.
   */
  @Nullable
  public abstract Set<String> getPossibleValues();

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
    if (!isRegExp || "\\0".equals(pattern)) {
      return stringEquals(pattern, isRegExp, caseSensitive);
    }

    if ("\\p{P}".equals(pattern)) {
      return punctuationMatcher(pattern, caseSensitive);
    }

    // always compile the pattern to check it's well-formed
    Pattern compiled = Pattern.compile(pattern, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    Set<String> possibleRegexpValues = getPossibleRegexpValues(pattern);
    if (possibleRegexpValues != null) {
      Set<String> set = possibleRegexpValues.stream().map(StringInterner::intern).collect(Collectors.toSet());
      if (set.size() == 1) {
        return stringEquals(set.iterator().next(), true, caseSensitive);
      }
      if (!caseSensitive) {
        String[] sorted = set.toArray(new String[0]);
        Arrays.sort(sorted, String.CASE_INSENSITIVE_ORDER);
        return new StringMatcher(pattern, true, false) {
          @Override
          public Set<String> getPossibleValues() {
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
        public Set<String> getPossibleValues() {
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

    StringMatcher sequenceMatcher = tryCreateSequenceMatcher(pattern, caseSensitive, compiled);
    if (sequenceMatcher != null) {
      return sequenceMatcher;
    }

    Substrings required = getRequiredSubstrings(pattern);
    Substrings exhaustive = required == null ? null : required.checkCanReplaceRegex(pattern);
    boolean substringsAreSufficient = exhaustive != null;
    Substrings substrings = substringsAreSufficient ? exhaustive : required;

    return new StringMatcher(pattern, true, caseSensitive) {
      @Nullable
      @Override
      public Set<String> getPossibleValues() {
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
      public Set<String> getPossibleValues() {
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

    RegexpParser<Substrings> parser = new RegexpParser<>(regexp) {
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
    RegexpParser<Stream<String>> parser = new RegexpParser<>(regexp) {
      @Override
      Stream<String> handleConcatenation(Stream<String> left, Stream<String> right) {
        List<String> groupResults = right.toList();
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

    // Max width of a single character-range (e.g. "a-z" has width 25) that we'll still enumerate
    // into a literal set instead of giving up and falling back to full regex matching.
    private static final int MAX_CHAR_RANGE_WIDTH = 64;

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
          if (last == null || next == '\\' || next - last > MAX_CHAR_RANGE_WIDTH) {
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
        // Guard against several small ranges/chars adding up to something large
        if (options != null && options.size() > MAX_CHAR_RANGE_WIDTH) {
          options = null;
        }
      }
      if (options == null) return unknown();
      List<T> components = options.stream().map(this::charLiteral).collect(Collectors.toList());
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
    private static final TooComplexRegexp INSTANCE = new TooComplexRegexp() {
      @Override
      public synchronized Throwable fillInStackTrace() {
        return this;
      }
    };
  }

  @NotNull
  private static StringMatcher punctuationMatcher(String pattern, boolean caseSensitive) {
    return new StringMatcher(pattern, true, caseSensitive) {
      @Nullable
      @Override
      public Set<String> getPossibleValues() {
        return null;
      }

      @Override
      public boolean matches(String s) {
        if (s.length() > MAX_MATCH_LENGTH) {
          return false;
        }
        // \p{P} matches exactly one punctuation code point, no more, no less
        if (s.isEmpty()) {
          return false;
        }
        int cp = s.codePointAt(0);
        if (Character.charCount(cp) != s.length()) {
          return false; // more than one code point in s
        }
        return isPunctuation(cp);
      }
    };
  }

  private static boolean isPunctuation(int cp) {
    int type = Character.getType(cp);
    return (type >= Character.DASH_PUNCTUATION && type <= Character.OTHER_PUNCTUATION)
      || type == Character.INITIAL_QUOTE_PUNCTUATION
      || type == Character.FINAL_QUOTE_PUNCTUATION;
  }

  private static final IntPredicate ANY_CODE_POINT = cp -> !isLineTerminator(cp);
  private static final IntPredicate ASCII_DIGIT = cp -> cp >= '0' && cp <= '9';
  private static final IntPredicate ASCII_WORD = cp ->
    (cp >= 'a' && cp <= 'z') || (cp >= 'A' && cp <= 'Z') || (cp >= '0' && cp <= '9') || cp == '_';
  private static final IntPredicate ASCII_SPACE = cp -> cp == ' ' || cp == '\t' || cp == '\n' || cp == 0x0B || cp == '\f' || cp == '\r';

  private static boolean isLineTerminator(int cp) {
    return cp == '\n' || cp == '\r' || cp == 0x85 || cp == 0x2028 || cp == 0x2029;
  }

  /**
   * Tries to build a specialized matcher for a regexp that consists only of a disjunction of
   * sequences of "simple" atoms: literal characters, {@code .}, {@code [..]} character classes,
   * {@code \p{..}} properties and the {@code \d}/{@code \w}/{@code \s} classes, each optionally
   * followed by a {@code ?}/{@code *}/{@code +}/{@code {m,n}} quantifier. Groups, backreferences,
   * lookaround and other advanced constructs make the method return {@code null}, so the caller
   * falls back to full regexp matching.
   *
   * <p>This avoids the costly {@link java.util.regex} engine for the many simple patterns used in
   * {@code grammar.xml} and {@code disambiguation.xml}, and mirrors its case-sensitivity semantics.
   */
  @Nullable
  private static StringMatcher tryCreateSequenceMatcher(String pattern, boolean caseSensitive, Pattern regexFallback) {
    String regexp = pattern;
    boolean effectiveCaseSensitive = caseSensitive;
    // A leading inline flag that disables case-insensitivity applies to the rest of the pattern.
    if (regexp.startsWith("(?-i)")) {
      regexp = regexp.substring(5);
      effectiveCaseSensitive = true;
    }
    List<List<IntTerm>> alternatives = parseAlternatives(regexp, effectiveCaseSensitive);
    if (alternatives == null) {
      return null;
    }
    List<RunAlternative> runAlternatives = new ArrayList<>(alternatives.size());
    for (List<IntTerm> terms : alternatives) {
      RunAlternative runAlternative = toRunAlternative(terms);
      if (runAlternative == null) {
        return null; // more than one quantified atom: let java.util.regex handle it
      }
      runAlternatives.add(runAlternative);
    }
    // The matcher is built with the original case-sensitivity flag (not the one overridden by a
    // leading "(?-i)"), because PatternToken.isCaseSensitive() is used to inherit the case
    // sensitivity of exceptions. The effective flag is already baked into the predicates.
    return new RunSequenceMatcher(pattern, caseSensitive, runAlternatives, regexFallback);
  }

  @Nullable
  private static RunAlternative toRunAlternative(List<IntTerm> terms) {
    int runIndex = -1;
    for (int i = 0; i < terms.size(); i++) {
      IntTerm term = terms.get(i);
      if (term.min != 1 || term.max != 1) {
        if (runIndex >= 0) {
          return null;
        }
        runIndex = i;
      }
    }
    if (runIndex < 0) {
      CharPredicate[] all = new CharPredicate[terms.size()];
      for (int i = 0; i < terms.size(); i++) {
        all[i] = toCharPredicate(terms.get(i).predicate);
      }
      return new RunAlternative(all, null, 0, 0, EMPTY_PREDICATES);
    }
    CharPredicate[] prefix = new CharPredicate[runIndex];
    for (int i = 0; i < runIndex; i++) {
      prefix[i] = toCharPredicate(terms.get(i).predicate);
    }
    IntTerm run = terms.get(runIndex);
    CharPredicate[] suffix = new CharPredicate[terms.size() - runIndex - 1];
    for (int i = 0; i < suffix.length; i++) {
      suffix[i] = toCharPredicate(terms.get(runIndex + 1 + i).predicate);
    }
    return new RunAlternative(prefix, toCharPredicate(run.predicate), run.min, run.max, suffix);
  }

  private static final CharPredicate[] EMPTY_PREDICATES = new CharPredicate[0];

  private static CharPredicate toCharPredicate(IntPredicate predicate) {
    boolean[] ascii = new boolean[128];
    for (int c = 0; c < ascii.length; c++) {
      ascii[c] = predicate.test(c);
    }
    return new CharPredicate(ascii, predicate);
  }

  @Nullable
  private static List<List<IntTerm>> parseAlternatives(String regexp, boolean caseSensitive) {
    String r = regexp;
    if (r.startsWith("\\b")) {
      r = r.substring(2);
    }
    if (r.startsWith("^")) {
      r = r.substring(1);
    }
    if (r.endsWith("\\b") && !r.endsWith("\\\\b")) {
      r = r.substring(0, r.length() - 2);
    }
    if (r.endsWith("$") && !r.endsWith("\\$")) {
      r = r.substring(0, r.length() - 1);
    }
    List<String> parts = splitTopLevelAlternatives(r);
    if (parts == null) {
      return null;
    }
    List<List<IntTerm>> result = new ArrayList<>(parts.size());
    for (String part : parts) {
      List<IntTerm> terms = parseSequence(part, caseSensitive);
      if (terms == null) {
        return null;
      }
      result.add(terms);
    }
    return result;
  }

  @Nullable
  private static List<IntTerm> parseSequence(String s, boolean caseSensitive) {
    List<IntTerm> terms = new ArrayList<>();
    int pos = 0;
    int len = s.length();
    while (pos < len) {
      char c = s.charAt(pos);
      IntPredicate predicate;
      int next;
      if (c == '\\') {
        if (pos + 1 >= len) return null;
        char escaped = s.charAt(pos + 1);
        if (escaped == 'p') {
          if (pos + 2 >= len || s.charAt(pos + 2) != '{') return null;
          int close = s.indexOf('}', pos + 3);
          if (close < 0) return null;
          IntPredicate base = PROPERTY_PREDICATES.get(s.substring(pos + 3, close));
          if (base == null) return null;
          predicate = caseSensitive ? base : caseInsensitivePredicate(base);
          next = close + 1;
        } else if (escaped == 'd') {
          predicate = ASCII_DIGIT;
          next = pos + 2;
        } else if (escaped == 'w') {
          predicate = ASCII_WORD;
          next = pos + 2;
        } else if (escaped == 's') {
          predicate = ASCII_SPACE;
          next = pos + 2;
        } else if (Character.isLetterOrDigit(escaped)) {
          return null; // \b, \n, \1, ... not supported
        } else {
          predicate = literalPredicate(escaped, caseSensitive);
          next = pos + 2;
        }
      } else if (c == '.') {
        predicate = ANY_CODE_POINT;
        next = pos + 1;
      } else if (c == '[') {
        int close = findCharClassEnd(s, pos);
        if (close < 0) return null;
        IntPredicate classPredicate = parseCharClassPredicate(s, pos + 1, close, caseSensitive);
        if (classPredicate == null) return null;
        predicate = classPredicate;
        next = close + 1;
      } else if (c == '(' || c == ')' || c == '*' || c == '+' || c == '?' || c == '{' || c == '}'
                 || c == '^' || c == '$' || c == '|' || c == ']') {
        return null;
      } else {
        predicate = literalPredicate(c, caseSensitive);
        next = pos + 1;
      }
      int min = 1;
      int max = 1;
      if (next < len) {
        char q = s.charAt(next);
        if (q == '?') {
          min = 0;
          next++;
        } else if (q == '*') {
          min = 0;
          max = Integer.MAX_VALUE;
          next++;
        } else if (q == '+') {
          max = Integer.MAX_VALUE;
          next++;
        } else if (q == '{') {
          int close = s.indexOf('}', next + 1);
          if (close < 0) return null;
          int[] bounds = parseOccurrenceBounds(s.substring(next + 1, close));
          if (bounds == null) return null;
          min = bounds[0];
          max = bounds[1];
          next = close + 1;
        }
        if (next < len) {
          char lazy = s.charAt(next);
          if (lazy == '?') {
            next++; // lazy quantifier accepts the same strings as a greedy one for a full match
          } else if (lazy == '+') {
            return null; // possessive quantifier changes which strings are accepted
          }
        }
      }
      terms.add(new IntTerm(predicate, min, max));
      pos = next;
    }
    return terms;
  }

  @Nullable
  private static int[] parseOccurrenceBounds(String body) {
    int comma = body.indexOf(',');
    try {
      if (comma < 0) {
        int n = Integer.parseInt(body);
        return new int[]{n, n};
      }
      int min = body.substring(0, comma).isEmpty() ? 0 : Integer.parseInt(body.substring(0, comma));
      String maxPart = body.substring(comma + 1);
      int max = maxPart.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(maxPart);
      if (min > max) return null;
      return new int[]{min, max};
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static int findCharClassEnd(String s, int start) {
    int i = start + 1;
    int len = s.length();
    if (i < len && s.charAt(i) == '^') {
      i++;
    }
    if (i < len && s.charAt(i) == ']') {
      i++; // a ']' in the first position is a literal
    }
    while (i < len) {
      char c = s.charAt(i);
      if (c == '\\') {
        i += 2;
        continue;
      }
      if (c == ']') {
        return i;
      }
      if (c == '[') {
        return -1; // nested classes are not supported
      }
      i++;
    }
    return -1;
  }

  @Nullable
  private static IntPredicate parseCharClassPredicate(String s, int start, int end, boolean caseSensitive) {
    boolean negated = false;
    int i = start;
    if (i < end && s.charAt(i) == '^') {
      negated = true;
      i++;
    }
    List<int[]> ranges = new ArrayList<>();
    List<Integer> singles = new ArrayList<>();
    List<IntPredicate> extras = new ArrayList<>();
    while (i < end) {
      char c = s.charAt(i);
      if (c == '\\') {
        if (i + 1 >= end) return null;
        char escaped = s.charAt(i + 1);
        if (escaped == 'd') {
          extras.add(ASCII_DIGIT);
          i += 2;
          continue;
        }
        if (escaped == 'w') {
          extras.add(ASCII_WORD);
          i += 2;
          continue;
        }
        if (escaped == 's') {
          extras.add(ASCII_SPACE);
          i += 2;
          continue;
        }
        if (Character.isLetterOrDigit(escaped)) {
          return null;
        }
        c = escaped;
        i += 2;
      } else {
        i++;
      }
      if (i < end - 1 && s.charAt(i) == '-') {
        char rangeEnd = s.charAt(i + 1);
        int afterRange = i + 2;
        if (rangeEnd == '\\') {
          if (i + 2 >= end) return null;
          char escapedEnd = s.charAt(i + 2);
          if (Character.isLetterOrDigit(escapedEnd)) return null;
          rangeEnd = escapedEnd;
          afterRange = i + 3;
        }
        if (rangeEnd < c || rangeEnd - c > MAX_CLASS_RANGE_WIDTH) {
          return null;
        }
        ranges.add(new int[]{c, rangeEnd});
        i = afterRange;
      } else {
        singles.add((int) c);
      }
    }
    int[] sortedSingles = new int[singles.size()];
    for (int k = 0; k < sortedSingles.length; k++) {
      sortedSingles[k] = singles.get(k);
    }
    Arrays.sort(sortedSingles);
    boolean neg = negated;
    return cp -> {
      boolean member = containsInClass(cp, ranges, sortedSingles, extras);
      if (!caseSensitive) {
        member = member
          || containsInClass(Character.toLowerCase(cp), ranges, sortedSingles, extras)
          || containsInClass(Character.toUpperCase(cp), ranges, sortedSingles, extras);
      }
      return neg ^ member;
    };
  }

  private static boolean containsInClass(int cp, List<int[]> ranges, int[] sortedSingles, List<IntPredicate> extras) {
    for (int[] range : ranges) {
      if (cp >= range[0] && cp <= range[1]) {
        return true;
      }
    }
    if (cp <= Character.MAX_VALUE && Arrays.binarySearch(sortedSingles, (char) cp) >= 0) {
      return true;
    }
    for (IntPredicate extra : extras) {
      if (extra.test(cp)) {
        return true;
      }
    }
    return false;
  }

  private static IntPredicate literalPredicate(char c, boolean caseSensitive) {
    if (caseSensitive) {
      return cp -> cp == c;
    }
    int lower = Character.toLowerCase(c);
    int upper = Character.toUpperCase(c);
    return cp -> cp == c || Character.toLowerCase(cp) == lower || Character.toUpperCase(cp) == upper;
  }

  private static IntPredicate caseInsensitivePredicate(IntPredicate base) {
    return cp -> base.test(cp) || base.test(Character.toLowerCase(cp)) || base.test(Character.toUpperCase(cp));
  }

  /** A single atom together with how many times it may repeat. */
  private static final class IntTerm {
    final IntPredicate predicate;
    final int min;
    final int max;

    IntTerm(IntPredicate predicate, int min, int max) {
      this.predicate = predicate;
      this.min = min;
      this.max = max;
    }
  }

  /**
   * Tests a single code point, with an ASCII lookup table to keep the common case fast.
   * All predicates share this class so that {@link #test} stays monomorphic and gets inlined.
   */
  private static final class CharPredicate {
    private final boolean[] ascii;
    private final IntPredicate fallback;

    CharPredicate(boolean[] ascii, IntPredicate fallback) {
      this.ascii = ascii;
      this.fallback = fallback;
    }

    boolean test(char c) {
      return c < 128 ? ascii[c] : fallback.test(c);
    }
  }

  /** One alternative of a pattern that contains at most one quantified atom. */
  private static final class RunAlternative {
    final CharPredicate[] prefix;
    final CharPredicate run;  // null if there is no quantified atom
    final int runMin;
    final int runMax;
    final CharPredicate[] suffix;

    RunAlternative(CharPredicate[] prefix, CharPredicate run, int runMin, int runMax, CharPredicate[] suffix) {
      this.prefix = prefix;
      this.run = run;
      this.runMin = runMin;
      this.runMax = runMax;
      this.suffix = suffix;
    }
  }

  /**
   * A specialized matcher for a disjunction of sequences that contain at most one quantified atom,
   * implemented with plain character loops instead of {@link java.util.regex}. Supplementary code
   * points are delegated to the compiled regexp to preserve exact semantics.
   */
  private static final class RunSequenceMatcher extends StringMatcher {
    private final List<RunAlternative> alternatives;
    private final Pattern regexFallback;

    RunSequenceMatcher(String pattern, boolean caseSensitive, List<RunAlternative> alternatives, Pattern regexFallback) {
      super(pattern, true, caseSensitive);
      this.alternatives = alternatives;
      this.regexFallback = regexFallback;
    }

    @Nullable
    @Override
    public Set<String> getPossibleValues() {
      return null;
    }

    @Override
    public boolean matches(String s) {
      int len = s.length();
      if (len > MAX_MATCH_LENGTH) {
        return false;
      }
      if (containsSurrogate(s, len)) {
        return regexFallback.matcher(new InterruptibleCharSequence(s)).matches();
      }
      for (RunAlternative alt : alternatives) {
        if (matchesAlternative(alt, s, len)) {
          return true;
        }
      }
      return false;
    }

    private static boolean containsSurrogate(String s, int len) {
      for (int i = 0; i < len; i++) {
        if (Character.isSurrogate(s.charAt(i))) {
          return true;
        }
      }
      return false;
    }

    private static boolean matchesAlternative(RunAlternative alt, String s, int len) {
      int i = 0;
      CharPredicate[] prefix = alt.prefix;
      for (int k = 0; k < prefix.length; k++) {
        if (i >= len || !prefix[k].test(s.charAt(i))) {
          return false;
        }
        i++;
      }
      CharPredicate run = alt.run;
      if (run == null) {
        if (len - i != alt.suffix.length) {
          return false;
        }
        return suffixMatches(alt.suffix, s, i);
      }
      int maxEnd = i;
      while (maxEnd < len && run.test(s.charAt(maxEnd))) {
        maxEnd++;
      }
      int runCount = maxEnd - i;
      if (alt.suffix.length == 0) {
        return maxEnd == len && runCount >= alt.runMin && runCount <= alt.runMax;
      }
      int upper = Math.min(runCount, alt.runMax);
      for (int rc = upper; rc >= alt.runMin; rc--) {
        int pos = i + rc;
        if (len - pos == alt.suffix.length && suffixMatches(alt.suffix, s, pos)) {
          return true;
        }
      }
      return false;
    }

    private static boolean suffixMatches(CharPredicate[] suffix, String s, int pos) {
      for (int k = 0; k < suffix.length; k++) {
        if (!suffix[k].test(s.charAt(pos + k))) {
          return false;
        }
      }
      return true;
    }
  }

  @Nullable
  private static StringMatcher tryCreateHybridPostagMatcher(String pattern, boolean caseSensitive,
                                                            @Nullable Supplier<Set<String>> universeSupplier) {
    if (pattern.indexOf('(') >= 0 || pattern.indexOf(')') >= 0) {
      return null; // grups de captura: fora d'abast, com ja vam acordar
    }
    List<String> parts = splitTopLevelAlternatives(pattern);
    if (parts == null) {
      return null;
    }

    List<PostagTemplate> templates = new ArrayList<>();
    Set<String> literals = new HashSet<>();
    List<String> unresolved = new ArrayList<>();

    for (String part : parts) {
      if (isPlainLiteral(part)) {
        // Cadena sense cap metacaràcter: sempre s'accepta tal qual, mai depèn de l'univers.
        // Cobreix marques com LOC_ADV, _GV_, etc., encara que no siguin postags "reals".
        literals.add(part);
        continue;
      }
      PostagTemplate t = parseSingleTemplate(part);
      if (t != null) {
        templates.add(t);
      } else {
        unresolved.add(part); // p. ex. "P0.{6}", quantificador que la nostra gramàtica no cobreix
      }
    }

    if (templates.isEmpty() && literals.isEmpty() && unresolved.isEmpty()) {
      return null;
    }

    Set<String> internedLiterals = literals.stream().map(StringInterner::intern).collect(Collectors.toSet());
    PostagTemplate[] templateArray = templates.toArray(new PostagTemplate[0]);
    String unresolvedJoined = unresolved.isEmpty() ? null : String.join("|", unresolved);

    return new HybridPostagMatcher(pattern, caseSensitive, internedLiterals, templateArray,
      unresolvedJoined, universeSupplier);
  }

  private static boolean isPlainLiteral(String s) {
    if (s.isEmpty()) return false;
    for (int i = 0; i < s.length(); i++) {
      if (".[]{}*+?^$\\".indexOf(s.charAt(i)) >= 0) return false;
    }
    return true;
  }

  private static final class HybridPostagMatcher extends StringMatcher {
    private final Set<String> literals;
    private final PostagTemplate[] templates;
    @Nullable private final String unresolvedRegexp;
    @Nullable private final Supplier<Set<String>> universeSupplier;

    private volatile Object unresolvedResolved; // Set<String> (enumerat) o Pattern (fallback), calculat un cop

    HybridPostagMatcher(String pattern, boolean caseSensitive, Set<String> literals, PostagTemplate[] templates,
                        @Nullable String unresolvedRegexp, @Nullable Supplier<Set<String>> universeSupplier) {
      super(pattern, true, caseSensitive);
      this.literals = literals;
      this.templates = templates;
      this.unresolvedRegexp = unresolvedRegexp;
      this.universeSupplier = universeSupplier;
    }

    @Nullable
    @Override
    public Set<String> getPossibleValues() {
      return null; // matcher mixt: no exhaustivament enumerable en general
    }

    @Override
    public boolean matches(String s) {
      if (s.length() > MAX_MATCH_LENGTH) {
        return false;
      }
      if (literals.contains(s)) {
        return true;
      }
      for (PostagTemplate t : templates) {
        if (t.matches(s)) {
          return true;
        }
      }
      return unresolvedRegexp != null && matchesUnresolved(s);
    }

    private boolean matchesUnresolved(String s) {
      Object resolved = unresolvedResolved;
      if (resolved == null) {
        resolved = resolveUnresolved();
        unresolvedResolved = resolved;
      }
      if (resolved instanceof Set) {
        //noinspection unchecked
        return ((Set<String>) resolved).contains(s);
      }
      return ((Pattern) resolved).matcher(s).matches();
    }

    private Object resolveUnresolved() {
      if (universeSupplier != null) {
        Set<String> universe = universeSupplier.get();
        if (universe != null && !universe.isEmpty()) {
          Set<String> intersected = intersectWithUniverse(unresolvedRegexp, caseSensitive, universe);
          if (intersected != null) {
            return intersected.stream().map(StringInterner::intern).collect(Collectors.toSet());
          }
        }
      }
      return Pattern.compile(unresolvedRegexp, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }
  }

  @Nullable
  private static StringMatcher tryCreatePostagTemplateMatcher(String pattern, boolean caseSensitive) {
    PostagAlternatives alt = parsePostagAlternatives(pattern);
    if (alt == null) {
      return null;
    }
    return new StringMatcher(pattern, true, caseSensitive) {
      @Nullable
      @Override
      public Set<String> getPossibleValues() {
        return null;
      }

      @Override
      public boolean matches(String s) {
        if (s.length() > MAX_MATCH_LENGTH) {
          return false;
        }
        return alt.matches(s);
      }
    };
  }

  /**
   * Recognizes a disjunction of "postag template" alternatives: sequences of literal chars,
   * "." wildcards, and "[...]" character classes, with an optional trailing ".*" meaning
   * "anything after this point". No capturing groups, no other quantifiers.
   * Returns {@code null} (fall back to full regex) for anything outside this grammar,
   * including any pattern containing '(' or ')' — those are only used for suggestion
   * generation (<match>), never here, so we never need to preserve capture semantics.
   */
  @Nullable
  private static PostagAlternatives parsePostagAlternatives(String regexp) {
    if (regexp.indexOf('(') >= 0 || regexp.indexOf(')') >= 0) {
      return null;
    }
    List<String> parts = splitTopLevelAlternatives(regexp);
    if (parts == null) {
      return null;
    }
    PostagTemplate[] templates = new PostagTemplate[parts.size()];
    for (int i = 0; i < parts.size(); i++) {
      PostagTemplate t = parseSingleTemplate(parts.get(i));
      if (t == null) {
        return null;
      }
      templates[i] = t;
    }
    return new PostagAlternatives(templates);
  }

  @Nullable
  private static List<String> splitTopLevelAlternatives(String regexp) {
    List<String> parts = new ArrayList<>();
    int start = 0;
    int bracketDepth = 0;
    for (int i = 0; i < regexp.length(); i++) {
      char c = regexp.charAt(i);
      if (c == '[') {
        bracketDepth++;
      } else if (c == ']') {
        if (bracketDepth == 0) return null; // malformed class
        bracketDepth--;
      } else if (c == '|' && bracketDepth == 0) {
        parts.add(regexp.substring(start, i));
        start = i + 1;
      }
    }
    if (bracketDepth != 0) return null;
    parts.add(regexp.substring(start));
    return parts;
  }

  @Nullable
  private static PostagTemplate parseSingleTemplate(String s) {
    List<PositionMatcher> positions = new ArrayList<>();
    int pos = 0;
    int len = s.length();
    boolean anyTail = false;

    while (pos < len) {
      char c = s.charAt(pos);

      if (c == '.' && pos + 1 < len && s.charAt(pos + 1) == '*' && pos + 2 == len) {
        anyTail = true;
        break; // ".*" must be the very end of the alternative
      }
      if (c == '.') {
        positions.add(PositionMatcher.WILDCARD);
        pos++;
        continue;
      }
      if (c == '[') {
        int close = s.indexOf(']', pos + 1);
        if (close < 0) return null;
        char[] options = parseCharClass(s, pos + 1, close);
        if (options == null) return null;
        positions.add(new PositionMatcher(options));
        pos = close + 1;
        continue;
      }
      if ("\\^${}*+?".indexOf(c) >= 0) {
        return null; // outside our grammar -> fall back to full regex
      }
      positions.add(new PositionMatcher(new char[]{c}));
      pos++;
    }

    if (positions.isEmpty()) {
      return null;
    }
    return new PostagTemplate(positions.toArray(new PositionMatcher[0]), anyTail);
  }

  private static final int MAX_CLASS_RANGE_WIDTH = 64;

  @Nullable
  private static char[] parseCharClass(String s, int start, int end) {
    if (start >= end || s.charAt(start) == '^') {
      return null; // empty or negated class -> unsupported
    }
    List<Character> options = new ArrayList<>();
    int i = start;
    while (i < end) {
      char c1 = s.charAt(i);
      if (c1 == '-' && i != start && i + 1 < end) {
        char last = options.get(options.size() - 1);
        char next = s.charAt(i + 1);
        if (next < last || next - last > MAX_CLASS_RANGE_WIDTH) return null;
        for (char c = (char) (last + 1); c <= next; c++) options.add(c);
        i += 2;
        continue;
      }
      options.add(c1);
      i++;
    }
    char[] result = new char[options.size()];
    for (int k = 0; k < result.length; k++) result[k] = options.get(k);
    return result;
  }

  private static final class PositionMatcher {
    static final PositionMatcher WILDCARD = new PositionMatcher(null);

    @Nullable final char[] allowed; // null = matches any char

    PositionMatcher(@Nullable char[] allowed) {
      this.allowed = allowed;
    }

    boolean test(char c) {
      if (allowed == null) return true;
      for (char a : allowed) if (a == c) return true;
      return false;
    }
  }

  private static final class PostagTemplate {
    final PositionMatcher[] positions;
    final boolean anyTail; // true = "...*" (length >= positions.length), false = exact length

    PostagTemplate(PositionMatcher[] positions, boolean anyTail) {
      this.positions = positions;
      this.anyTail = anyTail;
    }

    boolean matches(String s) {
      int n = positions.length;
      if (anyTail ? s.length() < n : s.length() != n) {
        return false;
      }
      for (int i = 0; i < n; i++) {
        if (!positions[i].test(s.charAt(i))) return false;
      }
      return true;
    }
  }

  private static final class PostagAlternatives {
    final PostagTemplate[] templates;

    PostagAlternatives(PostagTemplate[] templates) {
      this.templates = templates;
    }

    boolean matches(String s) {
      for (PostagTemplate t : templates) {
        if (t.matches(s)) return true;
      }
      return false;
    }
  }

  private static final int MAX_ENUMERATED_POSTAGS = 30;

  /**
   * Like {@link #create(String, boolean, boolean)}, but for patterns where a finite universe of
   * possible values is known (e.g. every postag a language's tagger/synthesizer can actually
   * produce). Tries, in order:
   *   1. Intersecting the regexp with the supplied universe; if no more than
   *      {@link #MAX_ENUMERATED_POSTAGS} values match, the result is a set matcher that also
   *      exposes those values via {@link #getPossibleValues()} (used for POS hints).
   *      {@code possibleValuesUniverseSupplier.get()} is called at most once, lazily, so callers
   *      whose universe is expensive to compute (e.g. loading a synthesizer dictionary) only pay
   *      that cost for the patterns that actually need it.
   *   2. The postag-template grammar (see {@link #tryCreatePostagTemplateMatcher}) — purely
   *      syntactic, for patterns too broad to enumerate.
   *   3. Falls back to {@link #create(String, boolean, boolean)} otherwise.
   */
  public static StringMatcher createWithKnownValues(String pattern, boolean isRegExp, boolean caseSensitive,
                                                     @Nullable Supplier<Set<String>> possibleValuesUniverseSupplier) {
    if (isRegExp) {
      // Enumerating the regexp against the known universe of postags, when it stays small enough, gives the
      // fastest possible matcher (a set lookup) and - unlike the specialized matchers below - one whose
      // getPossibleValues() is not null, so that rules can be pre-filtered with POS hints.
      if (possibleValuesUniverseSupplier != null) {
        Set<String> universe = possibleValuesUniverseSupplier.get();
        if (universe != null && !universe.isEmpty()) {
          Set<String> matching = intersectWithUniverse(pattern, caseSensitive, universe);
          if (matching != null) {
            return enumeratedSetMatcher(pattern, caseSensitive, matching);
          }
        }
      }
      StringMatcher postagTemplateMatcher = tryCreatePostagTemplateMatcher(pattern, caseSensitive);
      if (postagTemplateMatcher != null) {
        return postagTemplateMatcher;
      }
      StringMatcher hybrid = tryCreateHybridPostagMatcher(pattern, caseSensitive, possibleValuesUniverseSupplier);
      if (hybrid != null) {
        return hybrid;
      }
    }
    return create(pattern, isRegExp, caseSensitive);
  }

  private static StringMatcher enumeratedSetMatcher(String pattern, boolean caseSensitive, Set<String> set) {
    Set<String> interned = new HashSet<>(set.size());
    for (String s : set) {
      interned.add(intern(s));
    }
    return new StringMatcher(pattern, true, caseSensitive) {
      @Override
      public Set<String> getPossibleValues() {
        return Collections.unmodifiableSet(interned);
      }

      @Override
      public boolean matches(String s) {
        if (s.length() > MAX_MATCH_LENGTH) {
          return false;
        }
        return interned.contains(s);
      }
    };
  }

  @Nullable
  private static Set<String> intersectWithUniverse(String pattern, boolean caseSensitive, Set<String> universe) {
    Pattern compiled;
    try {
      compiled = Pattern.compile(pattern, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    } catch (PatternSyntaxException e) {
      return null;
    }
    Set<String> matching = new HashSet<>();
    for (String candidate : universe) {
      if (compiled.matcher(candidate).matches()) {
        matching.add(candidate);
        if (matching.size() > MAX_ENUMERATED_POSTAGS) {
          return null; // massa genèric per valer la pena enumerar-ho
        }
      }
    }
    return matching.isEmpty() ? null : matching;
  }

}
