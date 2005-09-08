/* JLanguageTool, a natural language style checker 
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

  private static final String FILE_ENCODING = System.getProperty("file.encoding", "latin1");

  private StringTools() {
    // only static stuff
  }
  
  public static String readFile(String filename) throws IOException {
    InputStreamReader isr = null;
    BufferedReader br = null;
    StringBuffer sb = new StringBuffer();
    try {
      isr = new InputStreamReader(new FileInputStream(filename), FILE_ENCODING);
      br = new BufferedReader(isr);
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
        sb.append("\n");
      }
    } finally {
      if (br != null) br.close();
      if (isr != null) isr.close();
    }
    return sb.toString();
  }

}
