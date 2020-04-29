/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfusionSetFileFormatter {

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + ConfusionSetFileFormatter.class.getSimpleName() + " <confusion_set.txt>");
      System.exit(1);
    }
    List<String> lines = Files.readAllLines(Paths.get(args[0]));
    for (String line : lines) {
      System.out.println(reformat(line));
    }
  }

  private static String reformat(String s) {
    Pattern pattern = Pattern.compile(";\\s*\\d+");
    Matcher matcher = pattern.matcher(s);
    if (matcher.find()) {
      int spaceStart = matcher.end();
      int spaceEnd = s.indexOf('#', 2);
      if (spaceStart > 0 && spaceEnd > 0) {
        String spaces = StringUtils.repeat(" ", 52-spaceStart);
        return s.substring(0, spaceStart+1) + spaces + s.substring(spaceEnd);
      }
    }
    return s;
  }

}
