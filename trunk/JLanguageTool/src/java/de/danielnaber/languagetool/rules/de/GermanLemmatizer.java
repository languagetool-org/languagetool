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
package de.danielnaber.languagetool.rules.de;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import de.danielnaber.languagetool.JLanguageTool;

/**
 * Trivial German lemmatizer that can simply find the baseforms of
 * those fullforms listed in <code>rules/de/fullform2baseform.txt</code>.
 * 
 * @author Daniel Naber
 */
class GermanLemmatizer {

  private static final String FILE_NAME = "rules" +File.separator+ "de" +File.separator+
    "fullform2baseform.txt";
  private static final String FILE_ENCODING = "utf-8";
  
  private Map<String, String> fullform2baseform;
  
  GermanLemmatizer() throws IOException {
    fullform2baseform = loadWords(JLanguageTool.getAbsoluteFile(FILE_NAME));
  }
  
  String getBaseform(final String fullform) {
    return fullform2baseform.get(fullform);
  }
  
  private Map<String, String> loadWords(File file) throws IOException {
    Map<String, String> map = new HashMap<String, String>();
    FileInputStream fis = null;
    InputStreamReader isr = null;
    BufferedReader br = null;
    try {
      fis = new FileInputStream(file);
      isr = new InputStreamReader(fis, FILE_ENCODING);
      br = new BufferedReader(isr);
      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.startsWith("#"))       // ignore comments
          continue;
        if (line.equals(""))       // ignore empty lines
          continue;
        String[] parts = line.split(":");
        if (parts.length != 2) {
          throw new IOException("Format error in file " +file.getAbsolutePath()+ ", line: " + line);
        }
        String baseform = parts[0];
        String[] fullforms = parts[1].split(",");
        for (int i = 0; i < fullforms.length; i++) {
          map.put(fullforms[i].trim(), baseform);
        }
      }
    } finally {
      if (br != null) br.close();
      if (isr != null) isr.close();
      if (fis != null) fis.close();
    }
    return map;
  }

}
