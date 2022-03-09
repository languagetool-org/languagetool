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

import morfologik.tools.DictDecompile;
import morfologik.tools.FSADecompile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;

/**
 * Print the contents of a Morfologik binary dictionary to STDOUT.
 */
final class DictionaryExporter extends DictionaryBuilder {

  protected DictionaryExporter(File infoFile) throws IOException {
    super(infoFile);
  }
  
  public static void main(String[] args) throws Exception {
    BuilderOptions builderOptions = new BuilderOptions();
    builderOptions.addOption(BuilderOptions.INPUT_OPTION, true, 
        "binary Morfologik dictionary file (.dict)", true);
    builderOptions.addOption(BuilderOptions.INFO_OPTION, true, 
        BuilderOptions.INFO_HELP, true);
    CommandLine cmdLine = builderOptions.parseArguments(args, DictionaryExporter.class);
    
    File binaryDictFile = new File(cmdLine.getOptionValue(BuilderOptions.INPUT_OPTION));
    File infoFile = new File(cmdLine.getOptionValue(BuilderOptions.INFO_OPTION));
    
    DictionaryExporter builder = new DictionaryExporter(infoFile);
    builder.setOutputFilename(cmdLine.getOptionValue(BuilderOptions.OUTPUT_OPTION));

    builder.build(binaryDictFile);
  }
  
  private void build(File binaryDictFile) throws RuntimeException, IOException {
    String inputPath = binaryDictFile.toString();
    File tmpOutputFile = File.createTempFile(
        DictionaryExporter.class.getSimpleName() + "_separator", ".txt");
    tmpOutputFile.deleteOnExit();
    
    if (inputPath.contains("hunspell") || inputPath.contains("spelling")) {
      String[] buildOptions = {
          "--exit", "false",
          "-i", binaryDictFile.toString(), 
          "-o", tmpOutputFile.toString()
      };
      System.out.println("Running Morfologik FSADecompile.main with these options: " + Arrays.toString(buildOptions));
      FSADecompile.main(buildOptions);      
    } else {
      String[] buildOptions = {"--exit", "false",
          "-i", binaryDictFile.toString(), 
          "-o", tmpOutputFile.toString()
      };
      System.out.println("Running Morfologik DictDecompile.main with these options: " + Arrays.toString(buildOptions));
      DictDecompile.main(buildOptions);
    }
    outputSeparatorToTab(tmpOutputFile);
    System.out.println("Done. The dictionary export has been written to " + getOutputFilename());
  }
  
  protected void outputSeparatorToTab(File inputFile) throws RuntimeException, IOException {
    File outputFile = new File(getOutputFilename());
    String separator = getOption("fsa.dict.separator");
    if (separator == null || separator.trim().isEmpty()) {
      throw new IOException(
          "A separator character (fsa.dict.separator) must be defined in the dictionary info file.");
    }
    boolean hasFrequency = isOptionTrue("fsa.dict.frequency-included");
    String encoding = getOption("fsa.dict.encoding");
    
    try (Scanner scanner = new Scanner(inputFile, encoding);
         Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), encoding))) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] parts = line.split(Pattern.quote(separator));
        if (parts.length == 3) {
          if (hasFrequency) { // remove frequency data in the last byte
            parts[2] = parts[2].substring(0, parts[2].length() - 1);
          }
          out.write(parts[1] + "\t" + parts[0] + "\t" + parts[2] + "\n");
        } else if (parts.length == 2) {
//          if (hasFrequency) {
//            out.write(parts[1] + "\n");
//          }
          out.write(parts[1] + "\t" + parts[0] + "\n");
        } else if (parts.length == 1) {
          out.write(parts[0] + "\n");
        } else {
          System.err
              .println("Invalid input, expected one, two or three columns separated with "
                  + separator + " in " + inputFile + ": " + line + " => ignoring");
        }
      }
    }
  }

}
