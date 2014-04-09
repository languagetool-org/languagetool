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
import org.languagetool.Language;

public final class LanguageIdentifierTools {

  private static final String PROFILE_SUFFIX = ".ngp";
  private static final String PROFILE_ENCODING = "UTF-8";

  private LanguageIdentifierTools() {
  }

  public static void addLtProfiles() {
    for (Language language : Language.REAL_LANGUAGES) {
      addProfile(language);
    }
  }

  private static void addProfile(Language language) {
    try {
      final LanguageProfile profile = new LanguageProfile();

      final String languageCode = language.getShortName();
      final String detectionFile = "/" + languageCode + "/" + languageCode + PROFILE_SUFFIX;
      if (!JLanguageTool.getDataBroker().resourceExists(detectionFile)) {
        // that's okay, not every language comes with its own detection file,
        // as Tika supports most languages out of the box.
        return;
      }
      try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(detectionFile)) {
        final InputStreamReader in = new InputStreamReader(stream, PROFILE_ENCODING);
        final BufferedReader reader =
                new BufferedReader(in);
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
      }

      LanguageIdentifier.addProfile(languageCode, profile);
    } catch (Exception e) {
      throw new RuntimeException("Failed trying to load language profile for language \"" + language + "\".", e);
    }
  }

}
