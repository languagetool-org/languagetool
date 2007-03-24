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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

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
    if (s == null)
      throw new NullPointerException(varName + " cannot be null");
    if (s.trim().equals(""))
      throw new IllegalArgumentException(varName + " cannot be empty or whitespace only");
  }
  
  /**
   * Read a file's content.
   */
  public static String readFile(final String filename) throws IOException {
    return readFile(filename, null);
  }
  
  /**
   * Read the text file using the given encoding.
   * 
   * @param filename name of the file
   * @param encoding the file's character encoding (e.g. <code>iso-8859-1</code>)
   * @return a string with the file's content, lines separated by <code>\n</code>
   * @throws IOException
   */
  public static String readFile(final String filename, final String encoding) throws IOException {
    InputStreamReader isr = null;
    BufferedReader br = null;
    FileInputStream fis = null;
    StringBuilder sb = new StringBuilder();
    try {
      fis = new FileInputStream(filename);
      if (encoding != null)
        isr = new InputStreamReader(fis, encoding);
      else
        isr = new InputStreamReader(fis);
      br = new BufferedReader(isr);
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
        sb.append("\n");
      }
    } finally {
      if (br != null) br.close();
      if (isr != null) isr.close();
      if (fis != null) fis.close();
    }
    return sb.toString();
  }

  /**
   * Returns true if <code>str</code> is made up of all-uppercase characters
   * (ignoring characters for which no upper-/lowercase distinction exists).
   */
  public static boolean isAllUppercase(String str) {
    if (str.toUpperCase().equals(str)) {
      return true;
    }
    return false;
  }
  
  /**
   * Whether the first character of <code>str</code> is an uppercase character.
   */
  public static boolean startsWithUppercase(final String str) {
    if (str.length() == 0)
      return false;
    char firstChar = str.charAt(0);
    if (Character.isUpperCase(firstChar))
      return true;
    return false;
  }

  /**
   * Return <code>str</code> modified so that its first character is now an 
   * uppercase character.
   */
  public static String uppercaseFirstChar(final String str) {
    if (str.length() == 0)
      return str;
    char firstChar = str.charAt(0);
    if (str.length() == 1)
      return str.toUpperCase();
    else
      return Character.toUpperCase(firstChar) + str.substring(1);
  }
  
  public static String readerToString(Reader reader) throws IOException {
    StringBuilder sb = new StringBuilder();
    int readbytes = 0;
    char[] chars = new char[4000];
    while (readbytes >= 0) {
      readbytes = reader.read(chars, 0, 4000);
      if (readbytes <= 0) {
        break;
      }
      sb.append(new String(chars, 0, readbytes));
    }
    return sb.toString();
  }

  public static String streamToString(InputStream is) throws IOException {
    InputStreamReader isr = new InputStreamReader(is);
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
    StringBuilder sb = new StringBuilder();
    int n = s.length();
    for (int i = 0; i < n; i++) {
       char c = s.charAt(i);
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
    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"" +System.getProperty("file.encoding")+ "\"?>\n");
    xml.append("<matches>\n");
    int i = 1;
    for (RuleMatch match : ruleMatches) {
      xml.append("<error" +
          " fromy=\"" + match.getLine() + "\"" + 
          " fromx=\"" + match.getColumn() + "\"" +
          " toy=\"" + match.getEndLine() + "\"" +
          " tox=\"" + match.getEndColumn() + "\"" +
          " ruleId=\"" +match.getRule().getId()+ "\"" 
          );
      String msg = match.getMessage().replaceAll("</?suggestion>", "'");
      xml.append(" msg=\"" + escapeXMLForAPIOutput(msg)+ "\"");
      final String START_MARKER = "__languagetoo_start_marker";
      String context = Tools.getContext(match.getFromPos(), match.getToPos(),
          escapeXML(text), contextSize, START_MARKER, "");
      xml.append(" replacements=\"" + 
          escapeXMLForAPIOutput(listToString(match.getSuggestedReplacements(), "#")) + "\"");
      // get position of error in context and remove artificial marker again:
      int contextOffset = context.indexOf(START_MARKER);
      context = context.replaceFirst(START_MARKER, "");
      xml.append(" context=\"" +escapeXMLForAPIOutput(context)+ "\"");
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
  
  public static String listToString(List l, String delimiter) {
    StringBuilder sb = new StringBuilder();
    for (Iterator iter = l.iterator(); iter.hasNext();) {
      String str = (String) iter.next();
      sb.append(str);
      if (iter.hasNext())
        sb.append(delimiter);
    }
    return sb.toString();
  }

  public static String getContext(int fromPos, int toPos, String fileContents) {
    return getContext(fromPos, toPos, fileContents, DEFAULT_CONTEXT_SIZE);
  }
  
  public static String getContext(int fromPos, int toPos, String fileContents, int contextSize) {
    fileContents = fileContents.replaceAll("\n", " ");
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
    StringBuilder marker = new StringBuilder();
    for (int i = 0; i < fileContents.length() + prefix.length(); i++) {
      if (i >= fromPos && i < toPos)
        marker.append("^");
      else
        marker.append(" ");
    }
    // now build context string plus marker:
    StringBuilder sb = new StringBuilder();
    sb.append(prefix);
    sb.append(fileContents.substring(startContent, endContent));
    sb.append(postfix);
    sb.append("\n");
    sb.append(markerPrefix + marker.substring(startContent, endContent));
    return sb.toString();
  }

}
