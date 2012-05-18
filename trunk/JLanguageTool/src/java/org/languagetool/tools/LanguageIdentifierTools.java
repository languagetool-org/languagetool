package org.languagetool.tools;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.tika.language.*;

import org.languagetool.JLanguageTool;

public class LanguageIdentifierTools {

  public static final String[] ADDITIONAL_LANGUAGES = {"be", "ca", "eo", "gl", "ro", "sk", "sl", "uk", "ast", "tl"};

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
