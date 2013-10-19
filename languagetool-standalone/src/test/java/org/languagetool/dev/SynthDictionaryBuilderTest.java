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

import java.io.File;

public class SynthDictionaryBuilderTest extends DictionaryBuilderTestHelper {

  @Test
  @Ignore("for interactive use only")
  public void testExportPosDictAndCreateSynth() throws Exception {
    for (Language language : Language.REAL_LANGUAGES) {
      if (language.getShortName().equals("pl")) {
        System.out.println("WARN: skipping Polish. TODO: make it work for Polish, too (problem: synth file is too large: 36MB!)");  // TODO
        continue;
      }
      String langCode = language.getShortName();
      File dir = new File("./languagetool-language-modules/" + langCode + "/src/main/resources/org/languagetool/resource/" + langCode);
      File oldBinarySynthFile = new File(dir, language.getName().toLowerCase() + "_synth.dict");
      if (!oldBinarySynthFile.exists()) {
        System.out.println("Ignoring " + language + ", no synth file found");
        continue;
      }
      File oldBinaryFile = new File(dir, language.getName().toLowerCase() + ".dict");
      File infoFile = new File(dir, language.getName().toLowerCase() + "_synth.info");
      File exportFile = exportDictionaryContents(oldBinaryFile);
      if (exportFile.length() == 0) {
        System.out.println("Zero-size output for " + language + ", skipping dictionary generation");
        exportFile.delete();
        continue;
      }
      SynthDictionaryBuilder builder = new SynthDictionaryBuilder(infoFile);
      File newBinarySynthFile = builder.build(exportFile, infoFile);
      exportFile.delete();
      System.out.println(language + " old binary file size: " + oldBinarySynthFile.length() + " bytes (" + oldBinarySynthFile.getName() + ")");
      System.out.println(language + " new binary file size: " + newBinarySynthFile.length() + " bytes (" + newBinarySynthFile.getAbsolutePath() + ")");
      // comment in to copy the new files over the old ones:
      /*boolean b = newBinarySynthFile.renameTo(oldBinarySynthFile);
      if (!b) {
        throw new RuntimeException("Could not rename " + newBinarySynthFile.getAbsolutePath() + " to " + oldBinarySynthFile.getCanonicalPath());
      }*/
      System.out.println("");
    }
  }

}
