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
package de.danielnaber.languagetool.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Pattern;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.Tools;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.rules.patterns.PatternRule;

/**
 * Tools for reading files etc.
 * 
 * @author Daniel Naber
 */
public final class StringTools {

  private static final int DEFAULT_CONTEXT_SIZE = 25;  

  /**
   * Constants for printing XML rule matches.
   */
  public static enum XmlPrintMode {
    /**
     * Normally output the rule matches by starting and
     * ending the XML output on every call.
     */
    NORMAL_XML,
    /**
     * Start XML output by printing the preamble and the
     * start of the root element.
     */
    START_XML,
    /**
     * End XML output by closing the root element.
     */
    END_XML,
    /**
     * Simply continue rule match output.
     */
    CONTINUE_XML
  }

  private static final Pattern XML_COMMENT_PATTERN = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
  private static final Pattern XML_PATTERN = Pattern.compile("(?<!<)<[^<>]+>", Pattern.DOTALL);


  private StringTools() {
    // only static stuff
  }

  /**
   * Throw exception if the given string is null or empty or only whitespace.
   */
  public static void assureSet(final String s, final String varName) {
    if (s == null) {
      throw new NullPointerException(varName + " cannot be null");
    }
    if (isEmpty(s.trim())) {
      throw new IllegalArgumentException(varName
          + " cannot be empty or whitespace only");
    }
  }

  /**
   * Read a file's content.
   */
  public static String readFile(final InputStream file) throws IOException {
    return readFile(file, null);
  }

  /**
   * Read the text file using the given encoding.
   * 
   * @param file
   *          InputStream to a file to be read
   * @param encoding
   *          the file's character encoding (e.g. <code>iso-8859-1</code>)
   * @return a string with the file's content, lines separated by
   *         <code>\n</code>
   * @throws IOException
   */
  public static String readFile(final InputStream file, final String encoding)
  throws IOException {
    InputStreamReader isr = null;
    BufferedReader br = null;
    final StringBuilder sb = new StringBuilder();
    try {
      if (encoding == null) {
        isr = new InputStreamReader(file);
      } else {
        isr = new InputStreamReader(file, encoding);
      }
      br = new BufferedReader(isr);
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
        sb.append('\n');
      }
    } finally {
      if (br != null) {
        br.close();
      }
      if (isr != null) {
        isr.close();
      }
    }
    return sb.toString();
  }

  /**
   * Returns true if <code>str</code> is made up of all-uppercase characters
   * (ignoring characters for which no upper-/lowercase distinction exists).
   */
  public static boolean isAllUppercase(final String str) {
    return str.equals(str.toUpperCase());
  }

  /**
   * @param str - input str
   * Returns true if str is MixedCase.
   */
  public static boolean isMixedCase(final String str) {
    return !isAllUppercase(str)
    && !isCapitalizedWord(str)
    && !str.equals(str.toLowerCase());
  }

  /**
   * @param str - input string
   */
  public static boolean isCapitalizedWord(final String str) {
    if (isEmpty(str)) {
      return false;
    }
    final char firstChar = str.charAt(0);
    if (Character.isUpperCase(firstChar)) {
      return str.substring(1).equals(str.substring(1).toLowerCase());
    }
    return false;
  }

  /**
   * Whether the first character of <code>str</code> is an uppercase character.
   */
  public static boolean startsWithUppercase(final String str) {
    if (isEmpty(str)) {
      return false;
    }
    final char firstChar = str.charAt(0);
    if (Character.isUpperCase(firstChar)) {
      return true;
    }
    return false;
  }

  /**
   * Return <code>str</code> modified so that its first character is now an
   * uppercase character. If <code>str</code> starts with non-alphabetic
   * characters, such as quotes or parentheses, the first character is 
   * determined as the first alphabetic character.
   */
  public static String uppercaseFirstChar(final String str) {
      return changeFirstCharCase(str, true);
  }

  /**
   * Return <code>str</code> modified so that its first character is now an
   * lowercase character. If <code>str</code> starts with non-alphabetic
   * characters, such as quotes or parentheses, the first character is 
   * determined as the first alphabetic character.
   */
  public static String lowercaseFirstChar(final String str) {
      return changeFirstCharCase(str, false);
  }

  /**
   * Return <code>str</code> modified so that its first character is now an
   * lowercase or uppercase character, depending on <code>toUpperCase</code>.
   * If <code>str</code> starts with non-alphabetic
   * characters, such as quotes or parentheses, the first character is 
   * determined as the first alphabetic character.
   */
  private static String changeFirstCharCase(final String str, final boolean toUpperCase) {
    if (isEmpty(str)) {
      return str;
    }
    if (str.length() == 1) {
      return toUpperCase ? str.toUpperCase() : str.toLowerCase();
    }
    int pos = 0;
    final int len = str.length() - 1;
    while (!Character.isLetterOrDigit(str.charAt(pos)) && len > pos) {
      pos++;
    }
    final char firstChar = str.charAt(pos);    
    return str.substring(0, pos) 
        + (toUpperCase ? Character.toUpperCase(firstChar) : Character.toLowerCase(firstChar))
        + str.substring(pos + 1);
  }

  public static String readerToString(final Reader reader) throws IOException {
    final StringBuilder sb = new StringBuilder();
    int readBytes = 0;
    final char[] chars = new char[4000];
    while (readBytes >= 0) {
      readBytes = reader.read(chars, 0, 4000);
      if (readBytes <= 0) {
        break;
      }
      sb.append(new String(chars, 0, readBytes));
    }
    return sb.toString();
  }

  public static String streamToString(final InputStream is) throws IOException {
    final InputStreamReader isr = new InputStreamReader(is);
    try {
      return readerToString(isr);
    } finally {
      isr.close();
    }
  }

  /**
   * Calls escapeHTML(String).
   */
  public static String escapeXML(final String s) {
    return escapeHTML(s);
  }

  /**
   * Escapes these characters: less than, bigger than, quote, ampersand.
   */
  public static String escapeHTML(final String s) {
    // this version is much faster than using s.replaceAll
    final StringBuilder sb = new StringBuilder();
    final int n = s.length();
    for (int i = 0; i < n; i++) {
      final char c = s.charAt(i);
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
   * Get an XML representation of the given rule matches.
   * 
   * @param text
   *          the original text that was checked, used to get the context of the
   *          matches
   * @param contextSize
   *          the desired context size in characters
   * @deprecated Use {@link #ruleMatchesToXML(List,String,int,XmlPrintMode)} instead
   */
  public static String ruleMatchesToXML(final List<RuleMatch> ruleMatches,
      final String text, final int contextSize) {
    return ruleMatchesToXML(ruleMatches, text, contextSize, XmlPrintMode.NORMAL_XML);
  }

  /**
   * Get an XML representation of the given rule matches.
   * @param text
   *          the original text that was checked, used to get the context of the
   *          matches
   * @param contextSize
   *          the desired context size in characters
   * @param xmlMode how to print the XML
   */
  public static String ruleMatchesToXML(final List<RuleMatch> ruleMatches,
      final String text, final int contextSize, final XmlPrintMode xmlMode) {
    //
    // IMPORTANT: people rely on this format, don't change it!
    //
    final StringBuilder xml = new StringBuilder();

    if (xmlMode == XmlPrintMode.NORMAL_XML || xmlMode == XmlPrintMode.START_XML) {
      xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      xml.append("<matches>\n");
    }

    for (final RuleMatch match : ruleMatches) {
      String subId = "";
      if (match.getRule() instanceof PatternRule) {
        final PatternRule pRule = (PatternRule) match.getRule();
        if (pRule.getSubId() != null) {
          subId = " subId=\"" + escapeXMLForAPIOutput(pRule.getSubId()) + "\" ";
        }
      }
      xml.append("<error" + " fromy=\"" + match.getLine() + "\"" + " fromx=\""
          + (match.getColumn() - 1) + "\"" + " toy=\"" + match.getEndLine() + "\""
          + " tox=\"" + (match.getEndColumn() - 1) + "\"" + " ruleId=\""
          + match.getRule().getId() + "\"");
      final String msg = match.getMessage().replaceAll("</?suggestion>", "'");
      xml.append(subId);
      xml.append(" msg=\"" + escapeXMLForAPIOutput(msg) + "\"");
      final String START_MARKER = "__languagetool_start_marker";
      String context = Tools.getContext(match.getFromPos(), match.getToPos(),
          text, contextSize, START_MARKER, "", true);
      xml.append(" replacements=\""
          + escapeXMLForAPIOutput(listToString(
              match.getSuggestedReplacements(), "#")) + "\"");
      // get position of error in context and remove artificial marker again:
      final int contextOffset = context.indexOf(START_MARKER);
      context = context.replaceFirst(START_MARKER, "");
      context = context.replaceAll("[\n\r]", " ");
      xml.append(" context=\"" + context + "\"");
      xml.append(" contextoffset=\"" + contextOffset + "\"");
      xml.append(" errorlength=\"" + (match.getToPos() - match.getFromPos())
          + "\"");
      xml.append("/>\n");
    }
    if (xmlMode == XmlPrintMode.END_XML || xmlMode == XmlPrintMode.NORMAL_XML) {
      xml.append("</matches>\n");
    }
    return xml.toString();
  }

  private static String escapeXMLForAPIOutput(final String s) {
    // this is simplified XML, i.e. put the "<error>" in one line:
    return escapeXML(s).replaceAll("[\n\r]", " ");
  }

  public static String listToString(final Collection<String> l, final String delimiter) {
    final StringBuilder sb = new StringBuilder();
    for (final Iterator<String> iter = l.iterator(); iter.hasNext();) {
      final String str = iter.next();
      sb.append(str);
      if (iter.hasNext()) {
        sb.append(delimiter);
      }
    }
    return sb.toString();
  }

  public static String getContext(final int fromPos, final int toPos,
      final String fileContents) {
    return getContext(fromPos, toPos, fileContents, DEFAULT_CONTEXT_SIZE);
  }

  public static String getContext(final int fromPos, final int toPos,
      final String contents, final int contextSize) {
    final String fileContents = contents.replace('\n', ' ');
    // calculate context region:
    int startContent = fromPos - contextSize;
    String prefix = "...";
    String postfix = "...";
    String markerPrefix = "   ";
    if (startContent < 0) {
      prefix = "";
      markerPrefix = "";
      startContent = 0;
    }
    int endContent = toPos + contextSize;
    if (endContent > fileContents.length()) {
      postfix = "";
      endContent = fileContents.length();
    }
    // make "^" marker. inefficient but robust implementation:
    final StringBuilder marker = new StringBuilder();
    for (int i = 0; i < fileContents.length() + prefix.length(); i++) {
      if (i >= fromPos && i < toPos) {
        marker.append('^');
      } else {
        marker.append(' ');
      }
    }
    // now build context string plus marker:
    final StringBuilder sb = new StringBuilder();
    sb.append(prefix);
    sb.append(fileContents.substring(startContent, endContent));
    sb.append(postfix);
    sb.append('\n');
    sb.append(markerPrefix);
    sb.append(marker.substring(startContent, endContent));
    return sb.toString();
  }

  /**
   * Filters any whitespace characters. Useful for trimming the contents of
   * token elements that cannot possibly contain any spaces.
   * 
   * @param str
   *          String to be filtered.
   * @return Filtered string.
   */
  public static String trimWhitespace(final String str) {
    final StringBuilder filter = new StringBuilder();
    for (int i = 0; i < str.length(); i++) {
      final char c = str.charAt(i);
      if (c != '\n' && c != ' ' && c != '\t') {
        filter.append(c);
      }
    }
    return filter.toString();
  }

  /**
   * Adds spaces before words that are not punctuation.
   * 
   * @param word
   *          Word to add the preceding space.
   * @param language
   *          Language of the word (to check typography conventions). Currently
   *          French convention of not adding spaces only before '.' and ',' is
   *          implemented; other languages assume that before ,.;:!? no spaces
   *          should be added.
   * @return String containing a space or an empty string.
   */
  public static String addSpace(final String word, final Language language) {
    String space = " ";
    final int len = word.length();
    if (len == 1) {
      final char c = word.charAt(0);
      if (Language.FRENCH.equals(language)) {
        if (c == '.' || c == ',') {
          space = "";
        }
      } else {
        if (c == '.' || c == ',' || c == ';' || c == ':' || c == '?'
          || c == '!') {
          space = "";
        }
      }
    }
    return space;
  }

  /**
   * Returns translation of the UI element without the control character "&". To
   * have "&" in the UI, use "&&".
   * 
   * @param label
   *          Label to convert.
   * @return String UI element string without mnemonics.
   */
  public static String getLabel(final String label) {
    return label.replaceAll("&([^&])", "$1").
    replaceAll("&&", "&");
  }

  /**
   * Returns the UI element string with mnemonics encoded in OpenOffice.org
   * convention (using "~").
   * 
   * @param label
   *          Label to convert
   * @return String UI element with ~ replacing &.
   */
  public static String getOOoLabel(final String label) {
    return label.replaceAll("&([^&])", "~$1").
    replaceAll("&&", "&");
  }

  /**
   * Returns mnemonic of a UI element.
   * 
   * @param label
   *          String Label of the UI element
   * @return @char Mnemonic of the UI element, or \u0000 in case of no mnemonic
   *         set.
   */
  public static char getMnemonic(final String label) {
    int mnemonicPos = label.indexOf('&');
    while (mnemonicPos != -1 && mnemonicPos == label.indexOf("&&")
        && mnemonicPos < label.length()) {
      mnemonicPos = label.indexOf('&', mnemonicPos + 2);
    }
    if (mnemonicPos == -1 || mnemonicPos == label.length()) {
      return '\u0000';
    }
    return label.charAt(mnemonicPos + 1);
  }

  /**
   * Checks if a string contains only whitespace, including all Unicode
   * whitespace.
   * 
   * @param str
   *          String to check
   * @return true if the string is whitespace-only.
   */
  public static boolean isWhitespace(final String str) {
    if ("\u0002".equals(str) // unbreakable field, e.g. a footnote number in OOo
        || "\u0001".equals(str)) { // breakable field in OOo
      return false;
    }
    final String trimStr = str.trim();
    if (isEmpty(trimStr)) {
      return true;
    }
    if (trimStr.length() == 1) {
      return java.lang.Character.isWhitespace(trimStr.charAt(0));
    }
    return false;
  }

  /**
   * 
   * @param ch
   *          Character to check
   * @return True if the character is a positive number (decimal digit from 1 to
   *         9).
   */
  public static boolean isPositiveNumber(final char ch) {
    return ch >= '1' && ch <= '9';
  }

  /**
   * Helper method to replace calls to "".equals().
   * 
   * @param str
   *          String to check
   * @return true if string is empty OR null
   */
  public static boolean isEmpty(final String str) {
    return str == null || str.length() == 0;
  }

  /**
   * Simple XML filtering routing
   * @param str XML string to be filtered.
   * @return Filtered string without XML tags.
   */
  public static String filterXML(final String str) {
    String s = str;       
    s = XML_COMMENT_PATTERN.matcher(s).replaceAll(" ");        
    s = XML_PATTERN.matcher(s).replaceAll("");
    return s;
  }

  public static String asString(final CharSequence s) {
    if (s == null) {
      return null;
    }
    return s.toString();
  }

}
