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
class DictionaryBuilder {

  private final Properties props = new Properties();

  protected DictionaryBuilder(File infoFile) throws IOException {
    props.load(new FileInputStream(infoFile));
  }

  protected static void checkUsageOrExit(String className, String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + className + " <dictionary> <infoFile>");
      System.out.println("   <dictionary> is a plain text dictionary file");
      System.out.println("   <infoFile> is the *.info properties file, see http://wiki.languagetool.org/developing-a-tagger-dictionary");
      System.exit(1);
    }
    File dictFile = new File(args[0]);
    if (!dictFile.exists()) {
      throw new IOException("File does not exist: " + dictFile);
    }
  }

  protected List<String> getTab2MorphOptions(File dictFile, File outputFile) throws IOException {
    List<String> tab2morphOptions = new ArrayList<>();
    String separator = getOption("fsa.dict.separator");
    if (separator != null && !separator.trim().isEmpty()) {
      tab2morphOptions.add("--annotation");
      tab2morphOptions.add(separator);
    }
    if (hasOption("fsa.dict.uses-prefixes")) {
      tab2morphOptions.add("-pre");
    }
    if (hasOption("fsa.dict.uses-infixes")) {
      tab2morphOptions.add("-inf");
    }
    tab2morphOptions.add("-i");
    tab2morphOptions.add(dictFile.getAbsolutePath());
    tab2morphOptions.add("-o");
    tab2morphOptions.add(outputFile.getAbsolutePath());
    return tab2morphOptions;
  }

  protected void prepare(List<String> tab2morphOptions) throws Exception {
    System.out.println("Running Morfologik Launcher.main with these options: " + tab2morphOptions);
    Launcher.main(tab2morphOptions.toArray(new String[tab2morphOptions.size()]));
  }

  protected File buildDict(File tempFile) throws Exception {
    File resultFile = File.createTempFile(DictionaryBuilder.class.getSimpleName(), ".dict");
    String[] buildToolOptions = {"-f", "cfsa2", "-i", tempFile.getAbsolutePath(), "-o", resultFile.getAbsolutePath()};
    System.out.println("Running Morfologik FSABuildTool.main with these options: " + Arrays.toString(buildToolOptions));
    FSABuildTool.main(buildToolOptions);
    System.out.println("Done. The binary dictionary has been written to " + resultFile.getAbsolutePath());
    return resultFile;
  }

  protected String getOption(String option) {
    return props.getProperty(option).trim();
  }

  private boolean hasOption(String option) {
    return "true".equals(getOption(option));
  }

}
