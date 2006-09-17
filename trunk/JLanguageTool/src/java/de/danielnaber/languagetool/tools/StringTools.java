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
import java.util.List;
import java.util.regex.*;

import de.danielnaber.languagetool.gui.Tools;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * Tools for reading files etc.
 * 
 * @author Daniel Naber
 */
public class StringTools {

  private StringTools() {
    // only static stuff
  }

  /**
   * Read a file's content.
   */
  public static String readFile(String filename) throws IOException {
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
  public static String readFile(String filename, String encoding) throws IOException {
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
  public static boolean startsWithUppercase(String str) {
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
  public static String uppercaseFirstChar(String str) {
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

  public static String escapeXML(String s) {
    return escapeHTML(s);
  }
  
  public static String escapeHTML(String s) {
    //replaceAll is slightly slower
    //TODO: should be replaced using StringBuilder
    //see struts TextUtil.escapeHTML
    s = s.replace("&", "&amp;");
    s = s.replace("<", "&lt;");
    s = s.replace(">", "&gt;");
    s = s.replace("\"", "&quot;");    
    return s;
  }
  
  /**
   * Get an XML representation of the given rule matches.
   *
   * @param text the original text that was checked, used to get the context of the matches
   * @param contextSize the desired context size in characters
   */
  public static String ruleMatchesToXML(List<RuleMatch> ruleMatches, String text, int contextSize) {
    //
    // IMPORTANT: people rely on this format, don't change it!
    //
    StringBuilder xml = new StringBuilder();
    xml.append("<matches>\n");
    int i = 1;
    for (RuleMatch match : ruleMatches) {
      xml.append("\t<match count=\"" + i + "\"" +
          " line=\"" + (match.getLine()+1) + "\"" + 
          " column=\"" + (match.getColumn()+1) + "\"" +
          " ruleId=\"" +match.getRule().getId()+ "\"" + 
          ">\n"
          );
      xml.append("\t\t<message>" +match.getMessage()+ "</message>\n");
      String context = Tools.getContext(match.getFromPos(), match.getToPos(),
          escapeXML(text), contextSize, "<marker>", "</marker>");
      xml.append("\t\t<replacements>\n");
      for (String replacement : match.getSuggestedReplacements()) {
        xml.append("\t\t\t<replacement>");
        xml.append(replacement);
        xml.append("</replacement>\n");
      }
      xml.append("\t\t</replacements>\n");
      xml.append("\t\t<context>" +context+ "</context>\n");
      xml.append("\t</match>\n");
      i++;
    }
    xml.append("</matches>\n");
    return xml.toString();
  }

}
