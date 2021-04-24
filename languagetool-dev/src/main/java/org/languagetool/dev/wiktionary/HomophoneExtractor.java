/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wiktionary;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract homophones from Wiktionary XML dump by looking for the {@code homophones} template. 
 * @since 3.3
 */
public class HomophoneExtractor {
  
  private static final Pattern homophonePattern = Pattern.compile("\\{\\{homophones\\|(.*?)\\}\\}");

  private void run(String filename) throws FileNotFoundException {
    try (Scanner scanner = new Scanner(new File(filename))) {
      String title = "";
      int lineCount = 0;
      long startTime = System.currentTimeMillis();
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        lineCount++;
        if (line.contains("<title>") && line.contains("</title>")) {
          title = line.substring(line.indexOf("<title>") + 7, line.indexOf("</title>"));
        } else if (line.contains("lang=en")) {
          Matcher m = homophonePattern.matcher(line);
          if (m.find()) {
            String homophonesData = m.group(1).replaceFirst("\\|?lang=en\\|?", "");
            String[] homophones = homophonesData.split("\\|");
            List<String> allHomophones = new ArrayList<>();
            allHomophones.add(title);
            allHomophones.addAll(Arrays.asList(homophones));
            allHomophones.sort(null);
            System.out.println(String.join(", ", allHomophones));
          }
        }
        if (lineCount % 100_000 == 0) {
          long endTime = System.currentTimeMillis();
          System.err.println(lineCount  + " (" + (endTime-startTime) + "ms)...");
          startTime = System.currentTimeMillis();
        }
      }
    }
  }

  public static void main(String[] args) throws FileNotFoundException {
    if (args.length != 1) {
      System.out.println("Usage: " + HomophoneExtractor.class.getSimpleName() + " <xmlFilename>");
      System.out.println("       <xmlFilename> is an unpacked Wiktionary dump");
      System.exit(1);
    }
    HomophoneExtractor extractor = new HomophoneExtractor();
    extractor.run(args[0]);
  }

}
