/* LanguageTool, a natural language style checker 
 * Copyright (C) 2008 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.synthesis;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class SynthesizerTools {
  
  private SynthesizerTools() {
    // static methods only, no public constructor
  }

  public static List<String> loadWords(InputStream stream) {
    List<String> result = new ArrayList<>();
    try (Scanner scanner = new Scanner(stream, "UTF-8")) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();
        if (line.isEmpty() || line.charAt(0) == '#') {  // ignore empty lines and comments
          continue;
        }
        result.add(line);
      }
    }
    return result;
  }

}
