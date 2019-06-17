/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.dumpcheck;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.languagetool.Language;
import org.languagetool.Languages;

/**
 * Command line tool to extract sentences from a (optionally bz2-compressed) Wikipedia XML dump.
 * @since 2.6
 */
class WikipediaSentenceExtractor {

  private void extract(Language language, String xmlDumpPath) throws IOException, CompressorException {
    try (FileInputStream fis = new FileInputStream(xmlDumpPath);
         BufferedInputStream bis = new BufferedInputStream(fis)) {
      InputStream input;
      if (xmlDumpPath.endsWith(".bz2")) {
        input = new CompressorStreamFactory().createCompressorInputStream(bis);
      } else if (xmlDumpPath.endsWith(".xml")) {
        input = bis;
      } else {
        throw new IllegalArgumentException("Unknown file name, expected '.xml' or '.bz2': " + xmlDumpPath);
      }
      int sentenceCount = 0;
      WikipediaSentenceSource source = new WikipediaSentenceSource(input, language);
      while (source.hasNext()) {
        String sentence = source.next().getText();
        if (skipSentence(sentence)) {
          continue;
        }
        System.out.println(sentence);
        sentenceCount++;
        if (sentenceCount % 1000 == 0) {
          System.err.println("Exporting sentence #" + sentenceCount + "...");
        }
      }
    }
  }

  private boolean skipSentence(String sentence) {
    return sentence.trim().length() == 0 || Character.isLowerCase(sentence.trim().charAt(0));
  }

  public static void main(String[] args) throws IOException, CompressorException {
    if (args.length != 2) {
      System.out.println("Usage: " + WikipediaSentenceExtractor.class.getSimpleName() + " <langCode> <wikipediaXmlDump>");
      System.exit(1);
    }
    WikipediaSentenceExtractor extractor = new WikipediaSentenceExtractor();
    extractor.extract(Languages.getLanguageForShortCode(args[0]), args[1]);
  }
}
