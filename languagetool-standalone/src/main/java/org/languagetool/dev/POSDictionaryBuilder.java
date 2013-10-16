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

import morfologik.tools.FSABuildTool;
import morfologik.tools.Launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Create a Morfologik binary dictionary from plain text data.
 */
final class POSDictionaryBuilder {

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.out.println("Usage: " + POSDictionaryBuilder.class.getSimpleName() + " <dictionary> <infoFile>");
      System.out.println("   <dictionary> is a plain text dictionary file");
      System.out.println("   <infoFile> is the properties file, see http://wiki.languagetool.org/developing-a-tagger-dictionary");
      System.exit(1);
    }
    POSDictionaryBuilder builder = new POSDictionaryBuilder();
    builder.build(new File(args[0]), new File(args[1]));
  }

  File build(File dictFile, File infoFile) throws Exception {
    File tempFile = File.createTempFile(POSDictionaryBuilder.class.getSimpleName(), ".txt");
    try {
      if (!dictFile.exists()) {
        throw new IOException("File does not exist: " + dictFile);
      }
      List<String> tab2morphOptions = getTab2MorphOptions(dictFile, infoFile, tempFile);
      System.out.println("Running Morfologik Launcher.main with these options: " + tab2morphOptions);
      Launcher.main(tab2morphOptions.toArray(new String[]{}));
      
      File resultFile = File.createTempFile(POSDictionaryBuilder.class.getSimpleName(), ".dict");
      String[] buildToolOptions = {"-f", "cfsa2", "-i", tempFile.getAbsolutePath(), "-o", resultFile.getAbsolutePath()};
      System.out.println("Running Morfologik FSABuildTool.main with these options: " + Arrays.toString(buildToolOptions));
      FSABuildTool.main(buildToolOptions);
      System.out.println("Done. The binary dictionary has been written to " + resultFile.getAbsolutePath());
      return resultFile;
    } finally {
      tempFile.delete();
    }
  }

  private List<String> getTab2MorphOptions(File dictFile, File infoFile, File tempFile) throws IOException {
    List<String> tab2morphOptions = new ArrayList<>();
    tab2morphOptions.add("tab2morph");
    if (hasOption(infoFile, "fsa.dict.uses-prefixes")) {
      tab2morphOptions.add("-pre");
    }
    if (hasOption(infoFile, "fsa.dict.uses-infixes")) {
      tab2morphOptions.add("-inf");
    }
    tab2morphOptions.add("-i");
    tab2morphOptions.add(dictFile.getAbsolutePath());
    tab2morphOptions.add("-o");
    tab2morphOptions.add(tempFile.getAbsolutePath());
    return tab2morphOptions;
  }

  private boolean hasOption(File infoFile, String option) throws IOException {
    Properties props = new Properties();
    props.load(new FileInputStream(infoFile));
    return props.getProperty(option).trim().equals("true");
  }

}
