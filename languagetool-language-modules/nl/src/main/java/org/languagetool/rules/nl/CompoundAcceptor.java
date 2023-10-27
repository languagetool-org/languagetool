/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Mark Baas
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
package org.languagetool.rules.nl;

import org.languagetool.*;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Accept Dutch compounds that are not accepted by the speller. This code
 * is supposed to accept more words in order to extend the speller, but it's not meant
 * to accept all valid compounds.
 * It works on 2-part compounds only for now.
 */
public class CompoundAcceptor {

  // compound parts that need an 's' appended to be used as first part of the compound:
  // "teit", "ing", "heid", "schap", "ker"
  private final Set<String> needsS1 = new HashSet<>(Arrays.asList(
    "bedrijfs", "passagiers" //, "dorps", "gezichts", "lijdens", "besturings", "verbrandings", "bestemmings", "schoonheids"
  ));
  // compound parts that must not have an 's' appended to be used as first part of the compound:
  private final Set<String> noS1 = new HashSet<>(Arrays.asList(
    "sport", "woning" //, "kinder", "fractie", "zout", "schade", "energie", "gemeente", "dienst", "wereld", "telefoon", "winkel", "aandeel", "zwanger", "papier"
  ));

  private final MorfologikDutchSpellerRule speller;

  CompoundAcceptor() {
    try {
      Language dutch = Languages.getLanguageForShortCode("nl");
      speller = new MorfologikDutchSpellerRule(JLanguageTool.getMessageBundle(), dutch, null);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  boolean acceptCompound(String word) throws IOException {
    for (int i = 3; i < word.length() - 3; i++) {
      String part1 = word.substring(0, i);
      String part2 = word.substring(i);
      //System.out.println("parts: " + part1 + " " + part2);
      if (acceptCompound(part1, part2)) {
        System.out.println(part1+part2 + " -> accepted");
        return true;
      }
      //System.out.println(" -> not accepted");
    }

    // add abbreviations
    return false;
  }

  boolean acceptCompound(String part1, String part2) throws IOException {
    boolean okWithS = false;
    boolean okWithDash = false;
    boolean okWithoutS = false;
    if (part1.endsWith("s")) {
      okWithS = spellingOk(part1.replaceFirst("s$", "")) && spellingOk(part2) && needsS1.contains(part1);
    } else if ( part1.endsWith("-")) {
      okWithDash = abbrevOk(part1) && spellingOk(part2);
    } else {
      okWithoutS = spellingOk(part1) && spellingOk(part2) && noS1.contains(part1);
    }
    //System.out.println(" okWithS: " + okWithS + ", okWithoutS " + okWithoutS);
    return okWithS || okWithDash || okWithoutS;
  }

  private boolean abbrevOk(String nonCompound) {
    //for compound words like IRA-akkoord, JPG-bestand
    return nonCompound.matches("[A-Z][A-Z][A-Z]-");
  }

  private boolean spellingOk(String nonCompound) throws IOException {
    AnalyzedSentence as = new AnalyzedSentence(new AnalyzedTokenReadings[] {
      new AnalyzedTokenReadings(new AnalyzedToken(nonCompound, "FAKE_POS", "fakeLemma"))
    });
    RuleMatch[] matches = speller.match(as);
    return matches.length == 0;
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + CompoundAcceptor.class.getName() + " <file>");
      System.exit(1);
    }
    CompoundAcceptor acceptor = new CompoundAcceptor();
    List<String> words = Files.readAllLines(Paths.get(args[0]));
    for (String word : words) {
      boolean accepted = acceptor.acceptCompound(word);
      System.out.println(accepted + " " + word);
    }
  }

}
