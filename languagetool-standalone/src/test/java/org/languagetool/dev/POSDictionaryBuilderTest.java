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

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.Language;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class POSDictionaryBuilderTest {

  private ByteArrayOutputStream out;
  private PrintStream stdout;
  private PrintStream stderr;

  @Test
  @Ignore("for interactive use only")
  public void testExportAndImport() throws Exception {
    for (Language language : Language.REAL_LANGUAGES) {
      String langCode = language.getShortName();
      File dir = new File("./languagetool-language-modules/" + langCode + "/src/main/resources/org/languagetool/resource/" + langCode);
      File oldBinaryFile = new File(dir, language.getName().toLowerCase() + ".dict");
      File infoFile = new File(dir, language.getName().toLowerCase() + ".info");
      File exportFile = exportDictionaryContents(oldBinaryFile);
      if (exportFile.length() == 0) {
        System.out.println("Zero-size output for " + language + ", skipping dictionary generation");
        exportFile.delete();
        continue;
      }
      POSDictionaryBuilder builder = new POSDictionaryBuilder();
      File newBinaryFile = builder.build(exportFile, infoFile);
      exportFile.delete();
      System.out.println(language + " old binary file size: " + oldBinaryFile.length() + " bytes (" + oldBinaryFile.getName() + ")");
      System.out.println(language + " new binary file size: " + newBinaryFile.length() + " bytes (" + newBinaryFile.getAbsolutePath() + ")");
      System.out.println("");
    }
  }

  private File exportDictionaryContents(File file) throws Exception {
    File outputFile;
    trackOutput();
    try {
      DictionaryExporter.main(new String[]{file.getAbsolutePath()});
    } finally {
      resetOutput();
    }
    outputFile = File.createTempFile(POSDictionaryBuilder.class.getSimpleName(), ".export");
    FileOutputStream fos = new FileOutputStream(outputFile);
    fos.write(out.toByteArray());
    fos.close();
    return outputFile;
  }

  private void trackOutput() {
    this.stdout = System.out;
    this.stderr = System.err;
    this.out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();
    System.setOut(new PrintStream(this.out));
    System.setErr(new PrintStream(err));
  }

  private void resetOutput() {
    System.setOut(this.stdout);
    System.setErr(this.stderr);
  }

}
