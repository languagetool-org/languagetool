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
package org.languagetool.dev;

import java.io.*;
import java.util.*;

/**
 * Create a Morfologik binary dictionary with word frequency data.
 * Input must be two tab-separated columns, the first column the ngram (words separated by space),
 * the second column the frequency count.
 */
final class FrequencyDictionaryBuilder extends DictionaryBuilder {

  public FrequencyDictionaryBuilder(File infoFile) throws IOException {
    super(infoFile);
  }

  public static void main(String[] args) throws Exception {
    checkUsageOrExit(FrequencyDictionaryBuilder.class.getSimpleName(), args);
    File infoFile = new File(args[1]);
    FrequencyDictionaryBuilder builder = new FrequencyDictionaryBuilder(infoFile);
    builder.build(new File(args[0]), infoFile);
  }
  
  File build(File freqFile, File infoFile) throws Exception {
    File tempFile = File.createTempFile(FrequencyDictionaryBuilder.class.getSimpleName(), ".txt");
    try {
      List<String> tab2morphOptions = getTab2MorphOptions(freqFile, tempFile);
      tab2morphOptions.add(0, "tab2morph");
      tab2morphOptions.add(1, "-nw");  // avoid useless "has 2 columns" warning
      prepare(tab2morphOptions);
      return buildDict(tempFile, null);
    } finally {
      tempFile.delete();
    }
  }

}
