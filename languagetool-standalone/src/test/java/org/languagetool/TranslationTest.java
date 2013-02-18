/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import junit.framework.TestCase;
import org.languagetool.language.English;
import org.languagetool.tools.StringTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Check if the translations seem to be complete.
 */
public class TranslationTest extends TestCase {

  public void testTranslationKeyExistence() throws IOException {
    // use English version as the reference:
    final File englishFile = getTranslationFile(new English());
    final Properties enProps = new Properties();
    enProps.load(new FileInputStream(englishFile));
    final Set<Object> englishKeys = enProps.keySet();
    for (int i = 0; i < Language.LANGUAGES.length; i++) {
      final Language lang = Language.LANGUAGES[i];
      if (lang.getShortName().equals("en") || lang == Language.DEMO) {
        continue;
      }
      final Properties langProps = new Properties();
      final File langFile = getTranslationFile(lang);
      if (!langFile.exists()) {
        continue;
      }
      langProps.load(new FileInputStream(langFile));
      final Set<Object> langKeys = langProps.keySet();
      for (Object englishKey : englishKeys) {
        if (!langKeys.contains(englishKey)) {
          System.err.println("***** No key '" + englishKey + "' in file " + langFile);
        }
      }
    }
  }

  /**
   * Make sure values are not empty.
   */
  public void testTranslationsAreNotEmpty() throws IOException {
    for (int i = 0; i < Language.LANGUAGES.length; i++) {
      final Language lang = Language.LANGUAGES[i];
      if (lang == Language.DEMO) {
        continue;
      }
      final File file = getTranslationFile(lang);
      if (!file.exists()) {
        System.err.println("Note: no translation available for " + lang);
        continue;
      }
      final List<String> lines = loadFile(file);
      for (String line : lines) {
        line = line.trim();
        if (StringTools.isEmpty(line) || line.charAt(0)=='#') {
          continue;
        }
        final String[] parts = line.split("=");
        if (parts.length < 2) {
          System.err.println("***** Empty translation: '" + line + "' in file " + file);
          //fail("Empty translation: '" + line + "' in file " + file);
        }
      }
    }
  }
  
  private List<String> loadFile(File file) throws IOException {
    final List<String> l = new ArrayList<String>();
    final Scanner scanner = new Scanner(file);
    try {
      while (scanner.hasNextLine()) {
        l.add(scanner.nextLine());
      }
    } finally {
      scanner.close();
    }
    return l;
  }

  private File getTranslationFile(Language lang) {
    final String langCode = lang.getShortName();
    final String name = "../languagetool-language-modules/" + langCode + "/src/main/resources/org/languagetool" 
            + "/MessagesBundle_" + langCode + ".properties";
    return new File(name.replace("/", File.separator));
  }

}
