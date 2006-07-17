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
import java.io.InputStreamReader;

/**
 * Tools for reading files etc.
 * 
 * @author Daniel Naber
 */
public class StringTools {

  private StringTools() {
    // only static stuff
  }
  
  public static String readFile(String filename) throws IOException {
    return readFile(filename, null);
  }
  
  public static String readFile(String filename, String encoding) throws IOException {
    InputStreamReader isr = null;
    BufferedReader br = null;
    FileInputStream fis = null;
    StringBuffer sb = new StringBuffer();
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
  
}
