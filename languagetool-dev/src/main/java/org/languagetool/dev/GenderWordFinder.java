/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.JLanguageTool;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.Rule;
import org.languagetool.rules.de.GermanSpellerRule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple script to find words like "Schülerlots*innen" that might not be accepted
 * by the speller.
 */
public class GenderWordFinder {

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + GenderWordFinder.class.getSimpleName() + " <file>");
      System.exit(1);
    }
    List<String> lines = Files.readAllLines(Paths.get(args[0]));
    Set<String> candidates = new HashSet<>();
    for (String line : lines) {
      if (line.endsWith("e")) {  // this will probably filter too much...
        candidates.add(line);
      }
    }
    GermanyGerman de = new GermanyGerman();
    JLanguageTool lt = new JLanguageTool(de);
    for (Rule rule : lt.getAllActiveRules()) {
      if (!rule.getId().equals("GERMAN_SPELLER_RULE")) {
        lt.disableRule(rule.getId());
      }
    }
    GermanSpellerRule speller = new GermanSpellerRule(JLanguageTool.getMessageBundle(), de);
    for (String line : lines) {
      if (line.endsWith("innen") && candidates.contains(line.replace("innen", "e")) &&
          speller.isMisspelled(line.replace("innen", "")) &&
          lt.check(line.replace("innen", "*innen")).size() > 0) {
        System.out.println(line.replace("innen", "*innen"));
      }
    }
  }
}
