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

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.Languages;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class POSDictionaryBuilderTest extends DictionaryBuilderTestHelper {

  @Test
  @Ignore("for interactive use only")
  public void testExportAndImport() throws Exception {
    for (Language language : Languages.get()) {
      String langCode = language.getShortCode();
      File dir = new File("./languagetool-language-modules/" + langCode + "/src/main/resources/org/languagetool/resource/" + langCode);
      File oldBinaryFile = new File(dir, language.getName().toLowerCase() + ".dict");
      File infoFile = new File(dir, language.getName().toLowerCase() + ".info");
      File exportFile = exportDictionaryContents(oldBinaryFile);
      if (exportFile.length() == 0) {
        System.out.println("Zero-size output for " + language + ", skipping dictionary generation");
        exportFile.delete();
        continue;
      }
      POSDictionaryBuilder builder = new POSDictionaryBuilder(infoFile);
      File newBinaryFile = builder.build(exportFile);
      exportFile.delete();
      System.out.println(language + " old binary file size: " + oldBinaryFile.length() + " bytes (" + oldBinaryFile.getName() + ")");
      System.out.println(language + " new binary file size: " + newBinaryFile.length() + " bytes (" + newBinaryFile.getAbsolutePath() + ")");
      // comment in to copy the new files over the old ones:
      /*boolean b = newBinaryFile.renameTo(oldBinaryFile);
      if (!b) {
        throw new RuntimeException("Could not rename" + newBinaryFile.getAbsolutePath() + " to " + oldBinaryFile.getCanonicalPath());
      }*/
      System.out.println("");
    }
  }
  
  private static final String INFO = 
      "fsa.dict.separator=+\n" +
      "fsa.dict.encoding=cp1251\n" +
      "fsa.dict.encoder=SUFFIX";

  @Test
  public void testPOSBuilder() throws Exception {
    Path inputFile = Files.createTempFile("dictTest", ".txt");
    Path infoFile = Files.createTempFile("dictTest", ".info");
    Path outFile = Files.createTempFile("dictTest", ".dict");
    try {
      Files.write(inputFile, Arrays.asList("word\tlemma\ttag"));
      Files.write(infoFile, Arrays.asList(INFO));
      POSDictionaryBuilder.main(new String[] {
          "-i", inputFile.toAbsolutePath().toString(), 
          "-info", infoFile.toAbsolutePath().toString(), 
          "-o", outFile.toAbsolutePath().toString()} );
      
      assertTrue(outFile.toFile().length() >= 40);
    } finally {
      outFile.toFile().deleteOnExit();
      infoFile.toFile().deleteOnExit();
      inputFile.toFile().deleteOnExit();
    }
  }

}
