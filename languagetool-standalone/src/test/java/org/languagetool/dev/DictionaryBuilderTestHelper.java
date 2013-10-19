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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class DictionaryBuilderTestHelper {

  private ByteArrayOutputStream out;
  private PrintStream stdout;
  private PrintStream stderr;

  protected File exportDictionaryContents(File file) throws Exception {
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
