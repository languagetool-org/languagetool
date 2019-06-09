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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;

import com.google.common.xml.XmlEscapers;

/**
 * Tools for working with strings.
 * 
 * @author Daniel Naber
 */
public final class StringTools {

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

  private static final Pattern XML_COMMENT_PATTERN = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
  private static final Pattern XML_PATTERN = Pattern.compile("(?<!<)<[^<>]+>", Pattern.DOTALL);
  public static final Set<String> UPPERCASE_GREEK_LETTERS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("Α","Β","Γ","Δ","Ε","Ζ","Η","Θ","Ι","Κ","Λ","Μ","Ν","Ξ","Ο","Π","Ρ","Σ","Τ","Υ","Φ","Χ","Ψ","Ω")));
  public static final Set<String> LOWERCASE_GREEK_LETTERS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("α","β","γ","δ","ε","ζ","η","θ","ι","κ","λ","μ","ν","ξ","ο","π","ρ","σ","τ","υ","φ","χ","ψ","ω")));

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
   * Returns true if <code>str</code> is made up of all-lowercase characters
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
  public static boolean isCapitalizedWord(String str) {
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
   * Return <code>str</code> modified so that its first character is now an
   * uppercase character. If <code>str</code> starts with non-alphabetic
   * characters, such as quotes or parentheses, the first character is 
   * determined as the first alphabetic character.
   */
  @Nullable
  public static String uppercaseFirstChar(String str) {
    return changeFirstCharCase(str, true);
  }

  /**
   * Like {@link #uppercaseFirstChar(String)}, but handles a special case for Dutch (IJ in 
   * e.g. "ijsselmeer" -&gt; "IJsselmeer").
   * @param language the language, will be ignored if it's {@code null}
   * @since 2.7
   */
  @Nullable
  public static String uppercaseFirstChar(String str, Language language) {
    if (language != null && "nl".equals(language.getShortCode()) && str != null && str.toLowerCase().startsWith("ij")) {
      // hack to fix https://github.com/languagetool-org/languagetool/issues/148
      return "IJ" + str.substring(2);
    } else {
      return changeFirstCharCase(str, true);
    }
  }

  /**
   * Return <code>str</code> modified so that its first character is now an
   * lowercase character. If <code>str</code> starts with non-alphabetic
   * characters, such as quotes or parentheses, the first character is 
   * determined as the first alphabetic character.
   */
  @Nullable
  public static String lowercaseFirstChar(String str) {
    return changeFirstCharCase(str, false);
  }

  /**
   * Return <code>str</code> modified so that its first character is now an
   * lowercase or uppercase character, depending on <code>toUpperCase</code>.
   * If <code>str</code> starts with non-alphabetic
   * characters, such as quotes or parentheses, the first character is 
   * determined as the first alphabetic character.
   */
  @Nullable
  private static String changeFirstCharCase(String str, boolean toUpperCase) {
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
    return filter.toString();
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
    return s.replaceAll("(?U)[^\\p{Space}\\p{Alnum}\\p{Punct}]", "");
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
    String trimStr = str.trim();
    if (isEmpty(trimStr)) {
      return true;
    }
    if (trimStr.length() == 1) {
      if ("\u200B".equals(str)) {
        // We need u200B​​ to be detected as whitespace for Khmer, as it was the case before Java 7.
        return true;
      } else if ("\u00A0".equals(str) || "\u202F".equals(str)) {  // non-breaking space and narrow non-breaking space
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
  public static boolean isEmpty(String str) {
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
      throw new RuntimeException("Could not load coherency data from " + path, e);
    }
    return Collections.unmodifiableList(l);
  }

}
