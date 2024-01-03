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
package org.languagetool.dev;

import org.languagetool.AnalyzedToken;
import org.languagetool.language.de.GermanyGerman;
import org.languagetool.synthesis.Synthesizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Help finding old German spelling that lack their new version,
 * like "abschloß" without "abschloss".
 */
public class GermanOldSpellingFinder {

  public static void main(String[] args) throws IOException {
    GermanyGerman lang = new GermanyGerman();
    Synthesizer synth = lang.getSynthesizer();
    List<String> words = Files.readAllLines(Paths.get(args[0]));
    int i = 0;
    for (String word : words) {
      if (i++ % 1000 == 0) {
        System.out.println(i + "...");
      }
      if (!word.matches("^[a-zöäü].*")) {
        continue;
      }
      String[] formsAr = synth.synthesize(new AnalyzedToken(word, "FAKE", word), ".*", true);
      List<String> forms = Arrays.asList(formsAr);
      for (String form : forms) {
        if (form.matches(".*oß") && !forms.contains(form.replaceFirst("ß", "ss"))) {
          System.out.println("No 'ss' form found: " + form);
        }
      }
    }
  }

  public static void main2(String[] args) throws IOException {
    GermanyGerman lang = new GermanyGerman();
    Synthesizer synth = lang.getSynthesizer();
    String tmpWord = "hintergießen";
    String[] formsAr = synth.synthesize(new AnalyzedToken(tmpWord, "FAKE", tmpWord), ".*", true);
    System.out.println(Arrays.toString(formsAr));
    System.exit(1);
  }
}
