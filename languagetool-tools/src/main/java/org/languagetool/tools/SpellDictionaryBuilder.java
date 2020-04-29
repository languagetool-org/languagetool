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
package org.languagetool.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;

/**
 * Create a Morfologik spelling binary dictionary from plain text data.
 */
final class SpellDictionaryBuilder extends DictionaryBuilder {

  SpellDictionaryBuilder(File infoFile) throws IOException {
    super(infoFile);
  }
  
  public static void main(String[] args) throws Exception {
    BuilderOptions builderOptions = new BuilderOptions();
    builderOptions.addOption(BuilderOptions.INPUT_OPTION, true, 
        "plain text dictionary file, e.g. created from a Hunspell dictionary by 'unmunch'", true);
    builderOptions.addOption(BuilderOptions.INFO_OPTION, true, 
        BuilderOptions.INFO_HELP, true);
    builderOptions.addOption(BuilderOptions.FREQ_OPTION, true, 
        BuilderOptions.FREQ_HELP, false);
    CommandLine cmdLine = builderOptions.parseArguments(args, SpellDictionaryBuilder.class);
    
    String plainTextFile = cmdLine.getOptionValue(BuilderOptions.INPUT_OPTION);
    String infoFile = cmdLine.getOptionValue(BuilderOptions.INFO_OPTION);
    
    SpellDictionaryBuilder builder = new SpellDictionaryBuilder(new File(infoFile));
    builder.setOutputFilename(cmdLine.getOptionValue(BuilderOptions.OUTPUT_OPTION));

    File inputFile = new File(plainTextFile);

    if (cmdLine.hasOption(BuilderOptions.FREQ_OPTION)) {
      builder.readFreqList(new File(cmdLine.getOptionValue(BuilderOptions.FREQ_OPTION)));
      inputFile = builder.addFreqData(inputFile, true);
    }
    
    builder.build(inputFile);
  }

  private File build(File plainTextDictFile) throws Exception {
    File tempFile = null;
    try {
      tempFile = tokenizeInput(plainTextDictFile);
      return buildFSA(tempFile);
    } finally {
      if (tempFile != null) {
        tempFile.delete();
      }
    }
  }

  private File tokenizeInput(File plainTextDictFile) throws IOException {
//    Tokenizer wordTokenizer = language.getWordTokenizer();
    String encoding = getOption("fsa.dict.encoding");
    String separatorChar = hasOption("fsa.dict.separator") ? getOption("fsa.dict.separator") : "";
    File tempFile = File.createTempFile(SpellDictionaryBuilder.class.getSimpleName(), ".txt");
    try (Scanner scanner = new Scanner(plainTextDictFile, encoding)) {
      try (Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), encoding))) {
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          int sepPos = separatorChar.isEmpty() ? -1 : line.indexOf(separatorChar);
          String occurrences = sepPos != -1 ? line.substring(sepPos + separatorChar.length()) : "";
          String lineWithoutOcc = sepPos != -1 ? line.substring(0, sepPos) : line;
//          List<String> tokens = wordTokenizer.tokenize(lineWithoutOcc);
          List<String> tokens = Arrays.asList(lineWithoutOcc);
          for (String token : tokens) {
            if (token.length() > 0) {
              out.write(token);
              if (sepPos != -1) {
                out.write(separatorChar);
                if (tokens.size() == 1) {
                  out.write(occurrences);
                } else {
                  // TODO: as the word occurrence data from
                  // https://github.com/mozilla-b2g/gaia/tree/master/apps/keyboard/js/imes/latin/dictionaries
                  // has already been assigned in a previous step, we now cannot just use
                  // that value after having changed the tokenization...
                  out.write("A");  // assume least frequent
                }
              }
              out.write("\n");
            }
          }
        }
      }
    }
    return tempFile;
  }

}
