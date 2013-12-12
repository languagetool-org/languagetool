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

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Create a Morfologik binary dictionary from plain text data.
 */
final class POSDictionaryBuilder extends DictionaryBuilder {


  
  POSDictionaryBuilder(File infoFile) throws IOException {
    super(infoFile);
  }

  public static void main(String[] args) throws Exception {
    checkUsageOrExit(POSDictionaryBuilder.class.getSimpleName(), args);
    POSDictionaryBuilder builder = new POSDictionaryBuilder(new File(args[1]));
    if (args.length == 3) {
      builder.readFreqList(new File(args[2]));
      builder.build(builder.addFreqData(new File(args[0])));
    } else {
      builder.build(new File(args[0]));
    }
  }

  File build(File dictFile) throws Exception {
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
