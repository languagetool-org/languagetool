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
import java.util.Iterator;
import java.util.List;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.Tools;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * Tools for reading files etc.
 * 
 * @author Daniel Naber
 */
public final class StringTools {

  private static final int DEFAULT_CONTEXT_SIZE = 25;

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
    if (s.trim().equals("")) {
      throw new IllegalArgumentException(varName + " cannot be empty or whitespace only");
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
   * @param file InputStream to a file to be read
   * @param encoding the file's character encoding (e.g. <code>iso-8859-1</code>)
   * @return a string with the file's content, lines separated by <code>\n</code>
   * @throws IOException
   */
  public static String readFile(final InputStream file, final String encoding) throws IOException {
    InputStreamReader isr = null;
    BufferedReader br = null;
    final StringBuilder sb = new StringBuilder();
    try {
      if (encoding != null) {
        isr = new InputStreamReader(file, encoding);
      } else {
        isr = new InputStreamReader(file);
      }
      br = new BufferedReader(isr);
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
        sb.append("\n");
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
    if (str.toUpperCase().equals(str)) {
      return true;
    }
    return false;
  }
  
  /**
   * Whether the first character of <code>str</code> is an uppercase character.
   */
  public static boolean startsWithUppercase(final String str) {
    if (str.length() == 0) {
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
   * uppercase character.
   */
  public static String uppercaseFirstChar(final String str) {
    if (str.length() == 0) {
      return str;
    }
    final char firstChar = str.charAt(0);
    if (str.length() == 1) {
      return str.toUpperCase();
    } else {
      return Character.toUpperCase(firstChar) + str.substring(1);
    }
  }
  
  public static String readerToString(final Reader reader) throws IOException {
    final StringBuilder sb = new StringBuilder();
    int readbytes = 0;
    final char[] chars = new char[4000];
    while (readbytes >= 0) {
      readbytes = reader.read(chars, 0, 4000);
      if (readbytes <= 0) {
        break;
      }
      sb.append(new String(chars, 0, readbytes));
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
    //this version is much faster
    //than using s.replaceAll        
    final StringBuilder sb = new StringBuilder();
    final int n = s.length();
    for (int i = 0; i < n; i++) {
       final char c = s.charAt(i);
       switch (c) {
         case '<': sb.append("&lt;"); break;
         case '>': sb.append("&gt;"); break;
         case '&': sb.append("&amp;"); break;
         case '"': sb.append("&quot;"); break;
         
         default:  sb.append(c); break;
       }
    }
    return sb.toString();    
  }
  
  /**
   * Get an XML representation of the given rule matches.
   *
   * @param text the original text that was checked, used to get the context of the matches
   * @param contextSize the desired context size in characters
   */
  public static String ruleMatchesToXML(final List<RuleMatch> ruleMatches, final String text,
      final int contextSize) {
    //
    // IMPORTANT: people rely on this format, don't change it!
    //
    final StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"" +System.getProperty("file.encoding")+ "\"?>\n");
    xml.append("<matches>\n");
    int i = 1;
    for (final RuleMatch match : ruleMatches) {
      xml.append("<error" +
          " fromy=\"" + match.getLine() + "\"" + 
          " fromx=\"" + match.getColumn() + "\"" +
          " toy=\"" + match.getEndLine() + "\"" +
          " tox=\"" + match.getEndColumn() + "\"" +
          " ruleId=\"" +match.getRule().getId()+ "\"" 
          );
      final String msg = match.getMessage().replaceAll("</?suggestion>", "'");
      xml.append(" msg=\"" + escapeXMLForAPIOutput(msg)+ "\"");
      final String START_MARKER = "__languagetool_start_marker";
      String context = Tools.getContext(match.getFromPos(), match.getToPos(),
          text, contextSize, START_MARKER, "", true);
      xml.append(" replacements=\"" + 
          escapeXMLForAPIOutput(listToString(match.getSuggestedReplacements(), "#")) + "\"");
      // get position of error in context and remove artificial marker again:
      final int contextOffset = context.indexOf(START_MARKER);
      context = context.replaceFirst(START_MARKER, "");
      context = context.replaceAll("[\n\r]", " ");
      xml.append(" context=\"" +context+ "\"");
      xml.append(" contextoffset=\"" +contextOffset+ "\"");
      xml.append(" errorlength=\"" +(match.getToPos()-match.getFromPos())+ "\"");
      xml.append("/>\n");
      i++;
    }
    xml.append("</matches>\n");
    return xml.toString();
  }

  private static String escapeXMLForAPIOutput(String s) {
    s = escapeXML(s);
    // this is simplified XML, i.e. put the "<error>" in one line: 
    s = s.replaceAll("[\n\r]", " ");
    return s;
  }
  
  public static String listToString(final List<String> l, final String delimiter) {
    final StringBuilder sb = new StringBuilder();
    for (final Iterator<String> iter = l.iterator(); iter.hasNext();) {
      final String str = (String) iter.next();
      sb.append(str);
      if (iter.hasNext()) {
        sb.append(delimiter);
      }
    }
    return sb.toString();
  }

  public static String getContext(final int fromPos, final int toPos, final String fileContents) {
    return getContext(fromPos, toPos, fileContents, DEFAULT_CONTEXT_SIZE);
  }
  
  public static String getContext(final int fromPos, final int toPos, String fileContents, final int contextSize) {
    fileContents = fileContents.replace('\n', ' ');
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
        marker.append("^");
      } else {
        marker.append(" ");
      }
    }
    // now build context string plus marker:
    final StringBuilder sb = new StringBuilder();
    sb.append(prefix);
    sb.append(fileContents.substring(startContent, endContent));
    sb.append(postfix);
    sb.append("\n");
    sb.append(markerPrefix + marker.substring(startContent, endContent));
    return sb.toString();
  }

  /**
   * Filters any whitespace characters. Useful for
   * trimming the contents of token elements that
   * cannot possibly contain any spaces.
   * @param str String to be filtered.
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
   * @param word Word to add the space before.
   * @param language Language of the word (to check 
   * typography conventions). Currently French 
   * convention of not adding spaces only before '.' and
   * '.' is implemented; other languages assume that before
   * ,.;:!? no spaces should be added.
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
        if (c == '.' || c == ','
          || c == ';' || c == ':'
            || c == '?' || c == '!') {
          space = "";
        } 
      }
    }
    return space;
  }
  
  /**
   * Returns translation of the UI element without the
   * control character "&". To have "&" in the UI, use "&&".
   * @param label Label to convert.
   * @return String UI element string without mnemonics.
   */
  public static String getLabel(final String label) {
   final String noAmpersand = label.replaceAll("&([^&])", "$1");   
   return noAmpersand.replaceAll("&&", "&"); 
  }
  
  /**
   * Returns mnemonic of a UI element.
   * @param label String Label of the UI element
   * @return @char Mnemonic of the UI element, or
   * \u0000 in case of no mnemonic set. 
   */
  public static char getMnemonic(final String label) {
    int mnemonicPos = label.indexOf('&');
    while (mnemonicPos != -1 
        && mnemonicPos == label.indexOf("&&")
        && mnemonicPos < label.length()) {
      mnemonicPos = label.indexOf('&', mnemonicPos + 2);
    }
    if (mnemonicPos == -1 || mnemonicPos == label.length()) {
      return '\u0000';
    } else {
      return label.charAt(mnemonicPos + 1);
    }
  }
  
  }
 
