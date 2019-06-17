/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.bigdata;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Re-Indent confusion_set.txt files.
 */
public class ConfusionFileIndenter {

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + ConfusionFileIndenter.class.getSimpleName() + " <file>");
      System.exit(1);
    }
    List<String> lines = Files.readAllLines(Paths.get(args[0]));
    System.out.println(indent(lines));
  }

  static String indent(List<String> lines) {
    StringBuilder indentedLines = new StringBuilder();
    Set<String> alreadyDone = new HashSet<>();
    for (String line : lines) {
      if (!line.startsWith("#") && !line.isEmpty()) {
        String[] parts = line.replaceFirst("\\s*#.*", "").split(";\\s*");
        String key = parts[0] + ";" + parts[1];
        if (alreadyDone.contains(key)) {
          //System.err.println("Skipping, already appeared: " + key);
          continue;
        }
        alreadyDone.add(key);
      }
      int commentPos = line.lastIndexOf('#');
      if (commentPos <= 0) {
        indentedLines.append(line).append("\n");
      } else {
        int endData = commentPos;
        while (Character.isWhitespace(line.charAt(endData - 1))) {
          endData--;
        }
        String spaces = StringUtils.repeat(" ", Math.max(1, 82-endData));
        indentedLines.append(line, 0, endData).append(spaces).append(line.substring(commentPos)).append("\n");
      }
    }
    return indentedLines.toString();
  }

}
