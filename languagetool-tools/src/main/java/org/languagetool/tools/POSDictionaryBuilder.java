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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;

/**
 * Create a Morfologik binary dictionary from plain text data.
 * @since public since 2.8
 */
public final class POSDictionaryBuilder extends DictionaryBuilder {

  public POSDictionaryBuilder(File infoFile) throws IOException {
    super(infoFile);
  }

  public static void main(String[] args) throws Exception {
    CommandLine cmdLine = new BuilderWithFreqOptions().parseArguments(args, POSDictionaryBuilder.class);
    
    POSDictionaryBuilder builder = new POSDictionaryBuilder(new File(cmdLine.getOptionValue(BuilderOptions.INFO_OPTION)));

    builder.setOutputFilename(cmdLine.getOptionValue(BuilderOptions.OUTPUT_OPTION));
    File inputFile = new File(cmdLine.getOptionValue(BuilderOptions.INPUT_OPTION));

    if ( cmdLine.hasOption(BuilderWithFreqOptions.FREQ_OPTION) ) {
      builder.readFreqList(new File(cmdLine.getOptionValue(BuilderWithFreqOptions.FREQ_OPTION)));
      inputFile = builder.addFreqData(inputFile);
    } 

    builder.build(inputFile);
  }

  public File build(File dictFile) throws Exception {
    File tempFile = File.createTempFile(POSDictionaryBuilder.class.getSimpleName(), ".txt");
    
    try {
      List<String> tab2morphOptions = getTab2MorphOptions(dictFile, tempFile);
      tab2morphOptions.add(0, "tab2morph");
      prepare(tab2morphOptions);
      return buildDict(tempFile);
    } finally {
      tempFile.delete();
    }
  }

}
