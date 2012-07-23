/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.language.LanguageProfile;
import org.languagetool.JLanguageTool;

public class LanguageIdentifierTools {

  public static final String[] ADDITIONAL_LANGUAGES = {"be", "ca", "eo", "gl", "ro", "sk", "sl", "uk", "ast", "tl", "ja"};

  public static void addLtProfiles() {
    for (String language : ADDITIONAL_LANGUAGES) {
      addProfile(language);
    }
  }

  private static void addProfile(String language) {
    final String PROFILE_SUFFIX = ".ngp";
    final String PROFILE_ENCODING = "UTF-8";

    try {
      final LanguageProfile profile = new LanguageProfile();      

      final InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(
              "/" + language + "/" + language + PROFILE_SUFFIX);
      try {
        final BufferedReader reader =
                new BufferedReader(new InputStreamReader(stream, PROFILE_ENCODING));
        String line = reader.readLine();
        while (line != null) {
          if (line.length() > 0 && !line.startsWith("#")) {
            final int space = line.indexOf(' ');
            profile.add(
                    line.substring(0, space),
                    Long.parseLong(line.substring(space + 1)));
          }
          line = reader.readLine();
        }
      } finally {
        stream.close();
      }

      LanguageIdentifier.addProfile(language, profile);
    } catch (Exception e) {
      throw new RuntimeException("Failed trying to load language profile for language \"" + language + "\".", e);
    }
  }

}
