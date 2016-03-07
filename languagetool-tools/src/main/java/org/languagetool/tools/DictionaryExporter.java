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

import java.io.File;

/**
 * Print the contents of a Morfologik binary dictionary to STDOUT.
 */
final class DictionaryExporter {

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: " + DictionaryExporter.class.getSimpleName() + " <dictionary>");
      System.out.println("   <dictionary> is a binary dictionary file, typically ending with .dict");
      System.exit(1);
    }
    String filename = args[0];
    File inputFile = new File(filename);
    String inputPath = inputFile.toString();
    File outpuFile = new File(filename.replace(".dict", "-dump.txt"));

    if (inputPath.contains("hunspell") || inputPath.contains("spelling")) {
      new FSADecompile(inputFile.toPath(),
          outpuFile.toPath()).call();
    } else {
      new DictDecompile(inputFile.toPath(),
          outpuFile.toPath(), true, true);
    }
    System.out.println("Done. The dictionary export has been written to " + outpuFile.getAbsolutePath());
  }

}
