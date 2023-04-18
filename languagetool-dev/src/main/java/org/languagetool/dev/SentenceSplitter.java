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

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

/**
 * Tokenize sentences from an input file. One sentence per line in the output.
 * 
 */
class SentenceSplitter {

  private static final int BATCH_SIZE = 1000;

  private void run(Language language, File inputFile, PrintWriter outputFile) throws IOException {
    JLanguageTool lt = new JLanguageTool(language);
    try (Scanner scanner = new Scanner(inputFile)) {
      int count = 0;
      long startTime = System.currentTimeMillis();
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        List<String> sentences = lt.sentenceTokenize(line);
        for (String sentence : sentences) {
          String cleanSentence = sentence.trim();
          if (!cleanSentence.isEmpty()) {
            outputFile.println(cleanSentence);
          }
        }
        if (++count % BATCH_SIZE == 0) {
          long time = System.currentTimeMillis() - startTime;
          System.out.println(count + ". " + time + "ms per " + BATCH_SIZE + " sentences");
          startTime = System.currentTimeMillis();
        }
      }
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 3) {
      System.err.println(
          "Usage: " + SentenceSplitter.class.getSimpleName() + " <langCode> <inputFile or folderFile> <outputFile>");
      System.exit(1);
    }
    SentenceSplitter splitter = new SentenceSplitter();
    PrintWriter output = new PrintWriter(args[2]);
    File folder = new File(args[1]);
    if (folder.isFile()) {
      splitter.run(Languages.getLanguageForShortCode(args[0]), folder, output);
    }
    if (folder.isDirectory()) {
      for (final File fileEntry : folder.listFiles()) {
        if (fileEntry.isFile()) {
          splitter.run(Languages.getLanguageForShortCode(args[0]), fileEntry, output);
        }
      }
    }
    output.close();
  }
}
