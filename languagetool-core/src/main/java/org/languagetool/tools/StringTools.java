/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tools;

import com.google.common.xml.XmlEscapers;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static java.util.regex.Pattern.*;

/**
 * Tools for working with strings.
 * 
 * @author Daniel Naber
 */
public final class StringTools {

  private static final Pattern NONCHAR = compile("[^A-Z\\u00c0-\\u00D6\\u00D8-\\u00DE]");
  private static final Pattern WORD_FOR_SPELLER = Pattern.compile("^[\\p{L}\\d\\p{P}\\p{Zs}]+$");
  private static final Pattern IS_NUMERIC = Pattern.compile("^[\\d\\s\\.,]*\\d$");

  /**
   * Constants for printing XML rule matches.
   */
  public enum ApiPrintMode {
    /**
     * Normally output the rule matches by starting and
     * ending the XML/JSON output on every call.
     */
    NORMAL_API,
    /**
     * Start XML/JSON output by printing the preamble and the
     * start of the root element.
     */
    START_API,
    /**
     * End XML/JSON output by closing the root element.
     */
    END_API,
    /**
     * Simply continue rule match output.
     */
    CONTINUE_API
  }

  public static final Set<String> UPPERCASE_GREEK_LETTERS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("Α","Β","Γ","Δ","Ε","Ζ","Η","Θ","Ι","Κ","Λ","Μ","Ν","Ξ","Ο","Π","Ρ","Σ","Τ","Υ","Φ","Χ","Ψ","Ω")));
  public static final Set<String> LOWERCASE_GREEK_LETTERS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("α","β","γ","δ","ε","ζ","η","θ","ι","κ","λ","μ","ν","ξ","ο","π","ρ","σ","τ","υ","φ","χ","ψ","ω")));

  private static final String[] WHITESPACE_ARRAY = new String[20];
  static {
    for (int i = 0; i < 20; i++) {
      WHITESPACE_ARRAY[i] = StringUtils.repeat(' ', i);
    }
  }

  public static final Pattern CHARS_NOT_FOR_SPELLING = compile("[^\\p{L}\\d\\p{P}\\p{Zs}]");
  private static final Pattern XML_COMMENT_PATTERN = compile("<!--.*?-->", DOTALL);
  private static final Pattern XML_PATTERN = compile("(?<!<)<[^<>]+>", DOTALL);
  private static final Pattern PUNCTUATION_PATTERN = compile("[\\p{IsPunctuation}']", DOTALL);
  private static final Pattern NOT_WORD_CHARACTER = compile("[^\\p{L}]", DOTALL);
  private static final Pattern NOT_WORD_STR = compile("[^\\p{L}]+", DOTALL);
  private static final Pattern PATTERN = compile("(?U)[^\\p{Space}\\p{Alnum}\\p{Punct}]");
  private static final Pattern DIACRIT_MARKS = compile("[\\p{InCombiningDiacriticalMarks}]");
  // Sets of words used for titlecasing in a few locales; useful for named entities in foreign languages, esp. English
  private static final Set<String> ENGLISH_TITLECASE_EXCEPTIONS = Collections.unmodifiableSet(
    new HashSet<>(Arrays.asList("of", "in", "on", "the", "a", "an", "and", "or"))
  );
  private static final Set<String> PORTUGUESE_TITLECASE_EXCEPTIONS = Collections.unmodifiableSet(
    new HashSet<>(Arrays.asList("e", "ou", "que",
      "de", "do", "dos", "da", "das",
      "o", "a", "os", "as",
      "no", "nos", "na", "nas",
      "ao", "aos", "à", "às"))
  );
  private static final Set<String> FRENCH_TITLECASE_EXCEPTIONS = Collections.unmodifiableSet(
    new HashSet<>(Arrays.asList("et", "ou", "que", "qui",
      "de", "du", "des", "en",
      "le", "les", "la",
      "un", "une",
      "à", "au", "aux"))
  );
  private static final Set<String> SPANISH_TITLECASE_EXCEPTIONS = Collections.unmodifiableSet(
    new HashSet<>(Arrays.asList("y", "e", "o", "u", "que",
      "el", "la", "los", "las",
      "un", "unos", "una", "unas",
      "del", "nel", "de", "en", "a", "al"))
  );
  private static final Set<String> GERMAN_TITLECASE_EXCEPTIONS = Collections.unmodifiableSet(
    new HashSet<>(Arrays.asList("von", "in", "im", "an", "am", "vom", "und", "oder", "dass", "ob",
      "der", "die", "das", "dem", "den", "des",
      "ein", "eines", "einem", "einen", "einer", "eine",
      "kein", "keines", "keinem", "keinen", "keiner", "keine"))
  );
  private static final Set<String> DUTCH_TITLECASE_EXCEPTIONS = Collections.unmodifiableSet(
    new HashSet<>(Arrays.asList("van", "in", "de", "het", "een", "en", "of"))
  );

  private static final Set<String> ALL_TITLECASE_EXCEPTIONS = collectAllTitleCaseExceptions();

  private StringTools() {
    // only static stuff
  }

  /**
   * Throw exception if the given string is null or empty or only whitespace.
   */
  public static void assureSet(String s, String varName) {
    Objects.requireNonNull(varName);
    if (isEmpty(s.trim())) {
      throw new IllegalArgumentException(varName + " cannot be empty or whitespace only");
    }
  }

  /**
   * Read the text stream using the given encoding.
   *
   * @param stream InputStream the stream to be read
   * @param encoding the stream's character encoding, e.g. {@code utf-8}, or {@code null} to use the system encoding
   * @return a string with the stream's content, lines separated by {@code \n} (note that {@code \n} will
   *  be added to the last line even if it is not in the stream)
   * @since 2.3
   */
  public static String readStream(InputStream stream, String encoding) throws IOException {
    InputStreamReader isr = null;
    StringBuilder sb = new StringBuilder();
    try {
      if (encoding == null) {
        isr = new InputStreamReader(stream);
      } else {
        isr = new InputStreamReader(stream, encoding);
      }
      try (BufferedReader br = new BufferedReader(isr)) {
        String line;
        while ((line = br.readLine()) != null) {
          sb.append(line);
          sb.append('\n');
        }
      }
    } finally {
      if (isr != null) {
        isr.close();
      }
    }
    return sb.toString();
  }

  /**
   * Returns true if the given string is made up of all-uppercase characters
   * (ignoring characters for which no upper-/lowercase distinction exists).
   */
  public static boolean isAllUppercase(String str) {
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (Character.isLetter(c) && Character.isLowerCase(c)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if the given list of string is made up of all-uppercase words.
   * If the list contains only numbers or punctuation marks it is not considered all-uppercase
   */
  public static boolean isAllUppercase(List<String> strList) {
    boolean isInputAllUppercase = true;
    boolean isAllNotLetters = true;
    for (int i = 0; i < strList.size(); i++) {
      isInputAllUppercase = isInputAllUppercase && StringTools.isAllUppercase(strList.get(i));
      isAllNotLetters = isAllNotLetters && (StringTools.isNotWordString(strList.get(i))
        || StringTools.isPunctuationMark(strList.get(i)));
    }
    return isInputAllUppercase && ! isAllNotLetters;
  }

  /**
   * Returns true if the given string is mixed case, like {@code MixedCase} or {@code mixedCase}
   * (but not {@code Mixedcase}).
   * @param str input str
   */
  public static boolean isMixedCase(String str) {
    return !isAllUppercase(str)
        && !isCapitalizedWord(str)
        && isNotAllLowercase(str);
  }

  /**
   * Returns true if <code>str</code> is not made up of all-lowercase characters
   * (ignoring characters for which no upper-/lowercase distinction exists).
   * @since 2.5
   */
  public static boolean isNotAllLowercase(String str) {
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (Character.isLetter(c) && !Character.isLowerCase(c)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param str input string
   * @return true if word starts with an uppercase letter and all other letters are lowercase
   */
  @Contract("null -> false")
  public static boolean isCapitalizedWord(@Nullable String str) {
    if (!isEmpty(str) && Character.isUpperCase(str.charAt(0))) {
      for (int i = 1; i < str.length(); i++) {
        char c = str.charAt(i);
        if (Character.isLetter(c) && !Character.isLowerCase(c)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Whether the first character of <code>str</code> is an uppercase character.
   */
  public static boolean startsWithUppercase(String str) {
    if (isEmpty(str)) {
      return false;
    }
    return Character.isUpperCase(str.charAt(0));
  }

  /**
   * Whether the first character of <code>str</code> is an uppercase character.
   * @since 4.9
   */
  public static boolean startsWithLowercase(String str) {
    if (isEmpty(str)) {
      return false;
    }
    return Character.isLowerCase(str.charAt(0));
  }

  public static boolean allStartWithLowercase(String str) {
    String[] strParts = str.split(" ");
    if (strParts.length < 2) {
      return startsWithLowercase(str);
    }
      for (String strPart : strParts) {
        if (!startsWithLowercase(strPart)) {
          return false;
        }
      }
      return true;
    }

  /**
   * Return <code>str</code> modified so that its first character is now an
   * uppercase character. If <code>str</code> starts with non-alphabetic
   * characters, such as quotes or parentheses, the first character is 
   * determined as the first alphabetic character.
   */
  @Contract("!null -> !null")
  @Nullable
  public static String uppercaseFirstChar(@Nullable String str) {
    return changeFirstCharCase(str, true);
  }

  /**
   * Like {@link #uppercaseFirstChar(String)}, but handles a special case for Dutch (IJ in 
   * e.g. "ijsselmeer" -&gt; "IJsselmeer").
   * @param language the language, will be ignored if it's {@code null}
   * @since 2.7
   */
  @Contract("!null, _ -> !null")
  @Nullable
  public static String uppercaseFirstChar(@Nullable String str, Language language) {
    if (language != null && "nl".equals(language.getShortCode()) && str != null && str.toLowerCase().startsWith("ij")) {
      // hack to fix https://github.com/languagetool-org/languagetool/issues/148
      return "IJ" + str.substring(2);
    } else {
      return changeFirstCharCase(str, true);
    }
  }

  private static Set<String> collectAllTitleCaseExceptions() {
    List<Set<String>> setList = Arrays.asList(ENGLISH_TITLECASE_EXCEPTIONS, PORTUGUESE_TITLECASE_EXCEPTIONS,
      FRENCH_TITLECASE_EXCEPTIONS, SPANISH_TITLECASE_EXCEPTIONS, GERMAN_TITLECASE_EXCEPTIONS, DUTCH_TITLECASE_EXCEPTIONS);
    Set<String> union = setList.stream().flatMap(Set::stream).collect(Collectors.toSet());
    return union;
  }

  /**
   * Title case a string ignoring a list of words. These words are ignored due to titlecasing conventions in the most
   * frequent languages. Differs from {@link #convertToTitleCaseIteratingChars(String)} in that it is less aggressive,
   * i.e., we do not force titlecase in all caps words (e.g. IDEA does not become Idea).
   * This method behaves the same regardless of the language, and is rather aggressive in its ignoring of words.
   * We can, possibly, in the future, have language-specific titlecasing conventions.
   */
  @Contract("!null -> !null")
  @Nullable
  public static String titlecaseGlobal(@Nullable final String str) {
    assert str != null;
    String[] strParts = str.split(" ");
    if (strParts.length == 1) {
      return uppercaseFirstChar(str);
    }
    StringJoiner titlecasedStr = new StringJoiner(" ");
    for (int i=0; i < strParts.length; i++) {
      String strPart = strParts[i];
      if (i == 0) {
        titlecasedStr.add(uppercaseFirstChar(strPart));
        continue;
      }
      if (ALL_TITLECASE_EXCEPTIONS.contains(strPart.toLowerCase())) {
        titlecasedStr.add(lowercaseFirstCharIfCapitalized(strPart));
      } else {
        titlecasedStr.add(uppercaseFirstChar(strPart));
      }
    }
    return titlecasedStr.toString();
  }

  /**
   * Return <code>str</code> modified so that its first character is now an
   * lowercase character. If <code>str</code> starts with non-alphabetic
   * characters, such as quotes or parentheses, the first character is 
   * determined as the first alphabetic character.
   */
  @Contract("!null -> !null")
  @Nullable
  public static String lowercaseFirstChar(@Nullable String str) {
    return changeFirstCharCase(str, false);
  }

  /**
   * Return <code>str</code> if str is capitalized {@link #isCapitalizedWord(String)},
   * otherwise return modified <code>str</code> so that its first character
   * is now a lowercase character.
   */
  @Contract("!null, -> !null")
  @Nullable
  public static String lowercaseFirstCharIfCapitalized(@Nullable String str) {
    if (!isCapitalizedWord(str)) return str;
    return changeFirstCharCase(str, false);
  }

  /**
   * Return <code>str</code> modified so that its first character is now an
   * lowercase or uppercase character, depending on <code>toUpperCase</code>.
   * If <code>str</code> starts with non-alphabetic
   * characters, such as quotes or parentheses, the first character is 
   * determined as the first alphabetic character.
   */
  @Contract("!null, _ -> !null")
  @Nullable
  private static String changeFirstCharCase(@Nullable String str, boolean toUpperCase) {
    if (isEmpty(str)) {
      return str;
    }
    if (str.length() == 1) {
      return toUpperCase ? str.toUpperCase(Locale.ENGLISH) : str.toLowerCase();
    }
    int pos = 0;
    int len = str.length() - 1;
    while (!Character.isLetterOrDigit(str.charAt(pos)) && len > pos) {
      pos++;
    }
    char firstChar = str.charAt(pos);    
    return str.substring(0, pos) 
        + (toUpperCase ? Character.toUpperCase(firstChar) : Character.toLowerCase(firstChar))
        + str.substring(pos + 1);
  }

  public static String readerToString(Reader reader) throws IOException {
    StringBuilder sb = new StringBuilder();
    int readBytes = 0;
    char[] chars = new char[4000];
    while (readBytes >= 0) {
      readBytes = reader.read(chars, 0, 4000);
      if (readBytes <= 0) {
        break;
      }
      sb.append(new String(chars, 0, readBytes));
    }
    return sb.toString();
  }

  public static String streamToString(InputStream is, String charsetName) throws IOException {
    try (InputStreamReader isr = new InputStreamReader(is, charsetName)) {
      return readerToString(isr);
    }
  } 
  
  /**
   * Calls {@link #escapeHTML(String)}.
   */
  public static String escapeXML(String s) {
    return escapeHTML(s);
  }

  /**
   * @since 2.9
   */
  public static String escapeForXmlAttribute(String s) {
    return XmlEscapers.xmlAttributeEscaper().escape(s);
  }

  /**
   * @since 2.9
   */
  public static String escapeForXmlContent(String s) {
    return XmlEscapers.xmlContentEscaper().escape(s);
  }

  /**
   * Escapes these characters: less than, greater than, quote, ampersand.
   */
  public static String escapeHTML(String s) {
    // this version is much faster than using s.replaceAll()
    StringBuilder sb = new StringBuilder();
    int n = s.length();
    for (int i = 0; i < n; i++) {
      char c = s.charAt(i);
      switch (c) {
        case '<':
          sb.append("&lt;");
          break;
        case '>':
          sb.append("&gt;");
          break;
        case '&':
          sb.append("&amp;");
          break;
        case '"':
          sb.append("&quot;");
          break;
        default:
          sb.append(c);
          break;
      }
    }
    return sb.toString();
  }

  /**
   * Filters any whitespace characters. Useful for trimming the contents of
   * token elements that cannot possibly contain any spaces, with the exception
   * for a single space in a word (for example, if the language supports numbers
   * formatted with spaces as single tokens, as Catalan in LanguageTool).
   * 
   * @param s String to be filtered.
   * @return Filtered s.
   */
  public static String trimWhitespace(String s) {
    StringBuilder filter = new StringBuilder();
    String str = s.trim();
    for (int i = 0; i < str.length(); i++) {
      while (str.charAt(i) <= ' ' && i < str.length() &&
          (str.charAt(i + 1) <= ' ' || i > 1 && str.charAt(i - 1) <= ' ')) {
        i++;
      }
      char c = str.charAt(i);
      if (c != '\n' && c != '\t' && c != '\r') {
        filter.append(c);
      }
    }
    return filter.length() == str.length() ? str : filter.toString();
  }

  /**
   * eliminate special (unicode) characters, e.g. soft hyphens
   * @since 4.3
   * @param s String to filter
   * @return s, with non-(alphanumeric, punctuation, space) characters deleted
   */
  public static String trimSpecialCharacters(String s) {
    // need unicode character classes -> (?U)
    // lists all allowed character classes, replace everything else
    return PATTERN.matcher(s).replaceAll("");
  }

  /**
   * Adds spaces before words that are not punctuation.
   * 
   * @param word Word to add the preceding space.
   * @param language
   *          Language of the word (to check typography conventions). Currently
   *          French convention of not adding spaces only before '.' and ',' is
   *          implemented; other languages assume that before ,.;:!? no spaces
   *          should be added.
   * @return String containing a space or an empty string.
   */
  public static String addSpace(String word, Language language) {
    String space = " ";
    if (word.length() == 1) {
      char c = word.charAt(0);
      if ("fr".equals(language.getShortCode())) {
        if (c == '.' || c == ',') {
          space = "";
        }
      } else {
        if (c == '.' || c == ',' || c == ';' || c == ':' || c == '?' || c == '!') {
          space = "";
        }
      }
    }
    return space;
  }

  /**
   * Checks if a string contains a whitespace, including:
   * <ul>
   * <li>all Unicode whitespace
   * <li>the non-breaking space (U+00A0)
   * <li>the narrow non-breaking space (U+202F)
   * <li>the zero width space (U+200B), used in Khmer
   * </ul>
   * @param str String to check
   * @return true if the string is a whitespace character
   */
  public static boolean isWhitespace(String str) {
    if ("\u0002".equals(str) // unbreakable field, e.g. a footnote number in OOo
        || "\u0001".equals(str)) { // breakable field in OOo
      return false;
    }

    if ("\uFEFF".equals(str)) {
      return true;
    }
    String trimStr = str.trim();
    if (isEmpty(trimStr)) {
      return true;
    }
    if (trimStr.length() == 1) {
      if ("\u200B".equals(str) ||// We need u200B​​ to be detected as whitespace for Khmer, as it was the case before Java 7.
          "\u00A0".equals(str) || "\u202F".equals(str)) { // non-breaking space and narrow non-breaking space
        return true;
      }
      return Character.isWhitespace(trimStr.charAt(0));
    }
    return false;
  }
  
  /**
   * Checks if a string is the non-breaking whitespace (<code>\u00A0</code>).
   * @since 2.1
   */
  public static boolean isNonBreakingWhitespace(String str) {
    return "\u00A0".equals(str);
  }

  /**
   * @param ch Character to check
   * @return True if the character is a positive number (decimal digit from 1 to 9).
   */
  public static boolean isPositiveNumber(char ch) {
    return ch >= '1' && ch <= '9';
  }

  /**
   * Helper method to replace calls to {@code "".equals()}.
   * 
   * @param str String to check
   * @return true if string is empty or {@code null}
   */
  public static boolean isEmpty(@Nullable String str) {
    return str == null || str.length() == 0;
  }

  /**
   * Simple XML filtering for XML tags.
   * @param str XML string to be filtered.
   * @return Filtered string without XML tags.
   */
  public static String filterXML(String str) {
    String s = str;       
    if (s.contains("<")) { // don't run slow regex unless we have to
      s = XML_COMMENT_PATTERN.matcher(s).replaceAll(" ");
      s = XML_PATTERN.matcher(s).replaceAll("");
    }
    return s;
  }
  
  public static boolean hasDiacritics(String str) {
    return !str.equals(removeDiacritics(str));
  }
  
  public static String removeDiacritics(String str) {
    String s = Normalizer.normalize(str, Normalizer.Form.NFD);
    return DIACRIT_MARKS.matcher(s).replaceAll("");
  }
  
  public static String normalizeNFKC(String str) {
    return Normalizer.normalize(str, Normalizer.Form.NFKC);
  }
  
  public static String normalizeNFC(String str) {
    return Normalizer.normalize(str, Normalizer.Form.NFC);
  }
  
  /**
   * Apply to inputString the casing of modelString
   * @param inputString, modelString 
   * @return string
   */
  public static String preserveCase(String inputString, String modelString) {
    if (modelString.isEmpty()) {
      return inputString; 
    }
    // modelString="L'" is ambiguous, apply capitalization
    if (isCapitalizedWord(modelString)) {
      return uppercaseFirstChar(inputString.toLowerCase()); 
    }
    if (isAllUppercase(modelString)) {
      return inputString.toUpperCase(); 
    }  
//    if (!isNotAllLowercase(modelString)) {
//      return inputString.toLowerCase();
//    }
    return inputString;
    
  }

  @Nullable
  public static String asString(CharSequence s) {
    if (s == null) {
      return null;
    }
    return s.toString();
  }

  /**
   * @since 4.3
   */
  public static boolean isParagraphEnd(String sentence, boolean singleLineBreaksMarksPara) {
    boolean isParaEnd = false;
    if (singleLineBreaksMarksPara) {
      if (sentence.endsWith("\n") || sentence.endsWith("\n\r")) {
        isParaEnd = true;
      }
    } else if (sentence.endsWith("\n\n") || sentence.endsWith("\n\r\n\r") || sentence.endsWith("\r\n\r\n")) {
      isParaEnd = true;
    }
    return isParaEnd;
  }

  /**
   * Loads file, ignoring comments (lines starting with {@code #}).
   * @param path path in resource dir
   * @since 4.6
   * @deprecated use DataBroker#getFromResourceDirAsLines(java.lang.String) instead (NOTE: it won't handle comments)
   */
  public static List<String> loadLines(String path) {
    InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path);
    List<String> l = new ArrayList<>();
    try (
      InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
      BufferedReader br = new BufferedReader(reader)
    ) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.isEmpty() || line.charAt(0) == '#') {   // ignore comments
          continue;
        }
        l.add(line);
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not load data from " + path, e);
    }
    return Collections.unmodifiableList(l);
  }

  /**
   * Will turn a string into a typical rule ID, i.e. uppercase and
   * "_" instead of spaces.
   *
   * All non-ASCII characters are replaced with "_", EXCEPT for
   * Latin-1 ranges U+00C0-U+00D6 and U+00D8-U+00DE.
   *
   * "de" locales have a special implementation (ä =&gt; ae, etc.).
   *
   * @param language LT language object, used to apply language-specific normalisation rules.
   *
   * @since 5.1
   */
  public static String toId(String input, Language language) {
    String languageCode = language.getShortCode();
    String normalisedId;
    normalisedId = input.toUpperCase().trim()
      .replace(' ', '_')
      .replace("'", "_Q_");
    // Standard toUpperCase implementation already converts ß to SS, so that'll be done for all locales and there's no
    // need to run a separate replace here.
    if (Objects.equals(languageCode, "de")) {
      normalisedId = normalisedId
        .replace("Ä", "AE")
        .replace("Ü", "UE")
        .replace("Ö", "OE");
    }
    normalisedId = NONCHAR.matcher(normalisedId).replaceAll("_");
    return normalisedId;
  }

  /**
   * Whether the string is camelCase. Works only with ASCII input and with single words.
   * @since 5.3
   */
  public static boolean isCamelCase(String token) {
    return token.matches("[a-z]+[A-Z][A-Za-z]+");
  }
  
  /**
   * Whether the string is a punctuation mark
   * @since 5.5
   */
  public static boolean isPunctuationMark(String input) {
    return PUNCTUATION_PATTERN.matcher(input).matches();
  }
  
  /**
   * Whether the string is a punctuation mark
   * @since 6.1
   */
  public static boolean isNotWordCharacter(String input) {
    return NOT_WORD_CHARACTER.matcher(input).matches();
  }
  
  
  /**
   * Difference between two strings (only one difference)
   * @return List of strings: 0: common string at the start; 1: diff in string1; 2: diff in string2; 3: common string at the end
   * @since 6.2
   */
  
  public static List<String> getDifference(String s1, String s2) {
    List<String> results = new ArrayList<>();
    if (s1.equals(s2)) {
      results.add(s1);
      results.add("");
      results.add("");
      results.add("");
      return results;
    }
    int l1 = s1.length();
    int l2 = s2.length();
    int fromStart = 0;
    while (fromStart < l1 && fromStart < l2 && s1.charAt(fromStart) == s2.charAt(fromStart)) {
      fromStart++;
    }
    int fromEnd = 0;
    while (fromEnd < l1 && fromEnd < l2 && s1.charAt(l1 - 1 - fromEnd) == s2.charAt(l2 - 1 - fromEnd)) {
      fromEnd++;
    }
    // corrections (e.g. stress vs stresses)
    while (fromStart > l1 - fromEnd) {
      fromEnd--;
    }
    while (fromStart > l2 - fromEnd) {
      fromEnd--;
    }
    // common string at start
    results.add(s1.substring(0, fromStart));
    // diff in string1
    results.add(s1.substring(fromStart, l1 - fromEnd));
    // diff in string2
    results.add(s2.substring(fromStart, l2 - fromEnd));
    // common string at end
    results.add(s1.substring(l1 - fromEnd, l1));
    return results;
  }
  
  
  /*
   * Invent a wrong word to find possible replacements. 
   */
  
  public static String makeWrong(String s) {
    if (s.contains("a")) {
      return s.replace("a", "ä");
    }
    if (s.contains("e")) {
      return s.replace("e", "ë");
    }
    if (s.contains("i")) {
      return s.replace("i", "ï");
    }
    if (s.contains("o")) {
      return s.replace("o", "ö");
    }
    if (s.contains("u")) {
      return s.replace("u", "ù");
    }
    if (s.contains("á")) {
      return s.replace("á", "ä");
    }
    if (s.contains("é")) {
      return s.replace("é", "ë");
    }
    if (s.contains("í")) {
      return s.replace("í", "ï");
    }
    if (s.contains("ó")) {
      return s.replace("ó", "ö");
    }
    if (s.contains("ú")) {
      return s.replace("ú", "ù");
    }
    if (s.contains("à")) {
      return s.replace("à", "ä");
    }
    if (s.contains("è")) {
      return s.replace("è", "ë");
    }
    if (s.contains("ì")) {
      return s.replace("ì", "i");
    }
    if (s.contains("ò")) {
      return s.replace("ò", "ö");
    }
    if (s.contains("ï")) {
      return s.replace("ï", "ì");
    }
    if (s.contains("ü")) {
      return s.replace("ü", "ù");
    }
    return s + "-";
  }

  /**
    * Return <code>str</code> without tashkeel characters
    * @param str input str
    */
   public static String removeTashkeel(String str) {
     String striped = str.replaceAll("["
       + "\u064B"  // Fathatan
       + "\u064C"  // Dammatan
       + "\u064D"  // Kasratan
       + "\u064E"  // Fatha
       + "\u064F"  // Damma
       + "\u0650"  // Kasra
       + "\u0651"  // Shadda
       + "\u0652"  // Sukun
       + "\u0653"  // Maddah Above
       + "\u0654"  // Hamza Above
       + "\u0655"  // Hamza Below
       + "\u0656"  // Subscript Alef
       + "\u0640"  // Tatweel
       + "]", "");
      return striped;
    }

  public static boolean isNotWordString(String input) {
    return NOT_WORD_STR.matcher(input).matches();
  }

  /*
   * Number of ocurreces of string t inside string s
   */
  public static int numberOf(String s, String t) {
    return s.length() - s.replaceAll(t, "").length();
  }

  public static String convertToTitleCaseIteratingChars(String text) {
    if (text == null || text.isEmpty()) {
      return text;
    }
    StringBuilder converted = new StringBuilder();
    boolean convertNext = true;
    for (char ch : text.toCharArray()) {
      if (Character.isSpaceChar(ch) || ch == '-') {
        convertNext = true;
      } else if (convertNext) {
        ch = Character.toTitleCase(ch);
        convertNext = false;
      } else {
        ch = Character.toLowerCase(ch);
      }
      converted.append(ch);
    }
    return converted.toString();
  }

  /**
   * Checks whether a given String is an Emoji with a string length larger 1.
   * @param word to be checked
   * @since 6.4
   */
  public static boolean isEmoji(String word) {
    if (word.length() > 1 && word.codePointCount(0, word.length()) != word.length()) {
      // some symbols such as emojis (😂) have a string length that equals 2
      return !WORD_FOR_SPELLER.matcher(word).matches();
    }
    return false;
  }

  /*
   * Replace characters that are not letters, digits, punctuation or white spaces
   * by white spaces
   * @param word to be checked
   * @since 6.4
   */
  public static String stringForSpeller(String s) {
    if (s.length() > 1 && s.codePointCount(0, s.length()) != s.length()) {
      Matcher matcher = CHARS_NOT_FOR_SPELLING.matcher(s);
      while (matcher.find()) {
        String found = matcher.group(0);
        // some symbols such as emojis (😂) have a string length larger than 1
        s = s.replace(found, WHITESPACE_ARRAY[found.length()]);
      }
    }
    return s;
  }

  public static String[] splitCamelCase(String input) {
    if (isAllUppercase(input)) {
      return new String[]{input};
    }
    StringBuilder word = new StringBuilder();
    StringBuilder result = new StringBuilder();
    boolean previousIsUppercase = false;
    for (int i = 0; i < input.length(); i++) {
      char currentChar = input.charAt(i);
      if (Character.isUpperCase(currentChar)) {
        if (!previousIsUppercase) {
          result.append(word).append(" ");
          word.setLength(0);
        }
        previousIsUppercase = true;
      } else {
        previousIsUppercase = false;
      }
      word.append(currentChar);
    }
    result.append(word);
    return result.toString().trim().split(" ");
  }

  public static String[] splitDigitsAtEnd(String input) {
    int lastIndex = input.length() - 1;
    while (lastIndex >= 0 && Character.isDigit(input.charAt(lastIndex))) {
      lastIndex--;
    }
    String nonDigitPart = input.substring(0, lastIndex + 1);
    String digitPart = input.substring(lastIndex + 1);
    if (!nonDigitPart.isEmpty() && !digitPart.isEmpty()) {
      return new String[]{nonDigitPart, digitPart};
    }
    return new String[]{input};
  }

  public static boolean isAnagram(String string1, String string2) {
    if (string1.length() != string2.length()) {
      return false;
    }
    char[] charArray1 = string1.toCharArray();
    char[] charArray2 = string2.toCharArray();
    Arrays.sort(charArray1);
    Arrays.sort(charArray2);
    return Arrays.equals(charArray1, charArray2);
  }

  public static boolean isNumeric(String string) {
    return IS_NUMERIC.matcher(string).matches();
  }
}
