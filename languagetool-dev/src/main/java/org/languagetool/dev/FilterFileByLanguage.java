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

import org.languagetool.DetectedLanguage;
import org.languagetool.language.identifier.LanguageIdentifier;
import org.languagetool.language.identifier.LanguageIdentifierService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Scanner;

public class FilterFileByLanguage {

  private final static String fastTextBinary = "/home/languagetool/fasttext/fasttext";
  private final static String fastTextModel = "/home/languagetool/fasttext/lid.176.bin";
  private final static String nGramData = "/home/languagetool/model_ml50_new.zip";
  private final static float skipThreshold = 0.95f; // only skip if confidence is higher than this

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + FilterFileByLanguage.class.getSimpleName() + " <langCode> <file>");
      System.exit(1);
    }
    String expectedLang = args[0];
    File input = new File(args[1]);
    File output = new File(input.getAbsoluteFile() + ".filtered");
    LanguageIdentifier ident = LanguageIdentifierService.INSTANCE.getDefaultLanguageIdentifier(0,
      new File(nGramData), new File(fastTextBinary), new File(fastTextModel));
    Scanner sc = new Scanner(input);
    int skipCount = 0;
    try (FileWriter fw = new FileWriter(output)) {
      while (sc.hasNextLine()) {
        String line = sc.nextLine();
        DetectedLanguage lang = ident.detectLanguage(line, Collections.emptyList(), Collections.emptyList());
        if (lang != null && !lang.getDetectedLanguage().getShortCode().equals(expectedLang) && lang.getDetectionConfidence() > skipThreshold) {
          System.out.printf("Skipping (%.2f, %s): %s\n", lang.getDetectionConfidence(), lang.getDetectedLanguage().getShortCode(), line);
          skipCount++;
        } else {
          fw.write(line);
          fw.write("\n");
        }
      }
    }
    System.out.println(skipCount + " lines skipped, confidence threshold was " + skipThreshold);
    System.out.println("Filtered result written to " + output);
  }

}
