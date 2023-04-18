/* LanguageTool, a natural language style checker
 * Copyright (C) 2022 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev;

import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.languagetool.tools.StringTools.uppercaseFirstChar;

/**
 * "Expand" regular expressions like {@code [Ss](?:e[gx]|áb)} to make them readable.
 * To use this, you need a list of "all" words, e.g. exported (using 'unmunch') from the spell
 * checker's *.dic files. Then copy the regexp in question to `String regex = ...` here,
 * set the file to the list of words at `String wordListFile = ...` and run the main
 * method to print the expanded regex. Note that words that are not in your list of
 * words but that the regex would match will be missing from the new regex.
 */
public class RegexExpander {

  private final static List<String> entities = Arrays.asList(
        "       <!ENTITY conjuncoes_coordenativas \"(?:e(?:ntão|ntretanto)?|p(?:ois|orém|or(?:t|qu)anto)|mas|ou|nem|contudo|logo|todavia)\">");
  private final static String wordListFile = "/home/dnaber/lt/pt-words.txt";
  private final static Set<String> printed = new HashSet<>();

  public static void main(String[] args) throws IOException {
    //Pattern tempP = Pattern.compile("(?:e(?:stere|ur|g)|(?:cent|sac)r|a(?:str|udi)|t(?:erm|urb)|f(?:il|ot)|i(?:ntr|d)|bronc|labi|mon|vas|zo)o");
    //System.out.println(tempP.matcher("estereo").matches());
    //System.exit(0);
    List<String> lines = Files.readAllLines(Paths.get(wordListFile));
    for (String s : entities) {

      Matcher matcher = Pattern.compile("<!ENTITY (.*?) ").matcher(s);
      boolean found = matcher.find();
      if (!found) {
        System.out.println("Entity name not found: " + s);
      }
      String entityName = matcher.group(1);
      s = s.replaceFirst("<!ENTITY .*? \"(.*)\">", "$1").trim();
      System.out.print("<!ENTITY " + entityName + " \"");
      Pattern p = Pattern.compile(s);
      int i = 0;
      for (String line : lines) {
        line = line.trim();
        boolean lcMatch = false;
        boolean ucMatch = false;
        if (p.matcher(line).matches()) {
          lcMatch = true;
        }
        if (StringTools.startsWithLowercase(line) && p.matcher(uppercaseFirstChar(line)).matches()) {
          ucMatch = true;
        }
        if (lcMatch && ucMatch) {
          printToken(i, "[" + uppercaseFirstChar(line).charAt(0) + StringTools.lowercaseFirstChar(line).charAt(0) + "]" + line.substring(1));
          i++;
        } else if (lcMatch && !printed.contains(line)) {
          printToken(i, line);
          i++;
        } else if (ucMatch && !printed.contains(uppercaseFirstChar(line))) {
          printToken(i, uppercaseFirstChar(line));
          i++;
        }
      }
      System.out.println("\">");
    }
  }

  private static void printToken(int i, String s) {
    if (i == 0) {
      System.out.print(s);
    } else {
      System.out.print("|" + s);
    }
    printed.add(s);
  }

}
