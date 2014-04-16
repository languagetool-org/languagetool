/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.Language;
import org.languagetool.tokenizers.Tokenizer;

import java.io.*;
import java.util.List;
import java.util.Scanner;

/**
 * Create a Morfologik spelling binary dictionary from plain text data.
 */
final class SpellDictionaryBuilder extends DictionaryBuilder {

  public SpellDictionaryBuilder(File infoFile) throws IOException {
    super(infoFile);
  }

  public static void main(String[] args) throws Exception {
    checkUsageOrExit(SpellDictionaryBuilder.class.getSimpleName(), args);
    String languageCode = args[0];
    String plainTextFile = args[1];
    String infoFile = args[2];
    SpellDictionaryBuilder builder = new SpellDictionaryBuilder(new File(infoFile));
    
    if (args.length == 4) {
      String freqListFile = args[3];
      builder.readFreqList(new File(freqListFile));
      builder.build(languageCode, builder.addFreqData(new File(plainTextFile)));
    } else {
      builder.build(languageCode, new File(plainTextFile));
    }
  }

  protected static void checkUsageOrExit(String className, String[] args) throws IOException {
    if (args.length < 3 || args.length > 4) {
      System.out.println("Usage: " + className + " <languageCode> <dictionary> <infoFile> [frequencyList]");
      System.out.println("   <languageCode> like 'en-US' or 'de-DE'");
      System.out.println("   <dictionary> is a plain text dictionary file, e.g. created from a Hunspell dictionary by 'unmunch'");
      System.out.println("   <infoFile> is the *.info properties file, see http://wiki.languagetool.org/developing-a-tagger-dictionary");
      System.out.println("   [frequencyList] is the *.xml file with a frequency wordlist, see http://wiki.languagetool.org/developing-a-tagger-dictionary");
      System.exit(1);
    }
    File dictFile = new File(args[2]);
    if (!dictFile.exists()) {
      throw new IOException("File does not exist: " + dictFile);
    }
  }

  File build(String languageCode, File plainTextDictFile) throws Exception {
    Language language = Language.getLanguageForShortName(languageCode);
    File tempFile = null;
    try {
      tempFile = tokenizeInput(plainTextDictFile, language);
      return buildDict(tempFile, language);
    } finally {
      if (tempFile != null) {
        tempFile.delete();
      }
    }
  }

  private File tokenizeInput(File plainTextDictFile, Language language) throws IOException {
    Tokenizer wordTokenizer = language.getWordTokenizer();
    String encoding = getOption("fsa.dict.encoding");
    File tempFile = File.createTempFile(SpellDictionaryBuilder.class.getSimpleName(), ".txt");
    try (Scanner scanner = new Scanner(plainTextDictFile, encoding)) {
      try (Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), encoding))) {
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          List<String> tokens = wordTokenizer.tokenize(line);
          for (String token : tokens) {
            if (token.length() > 0) {
              out.write(token);
              out.write("\n");
            }
          }
        }
      }
    }
    return tempFile;
  }

}
