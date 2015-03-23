/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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

import java.util.*;

/**
 * Helper methods to list all supported languages and to get language objects
 * by their name or language code etc.
 * @since 2.9
 */
public final class Languages {

  private static final List<Language> LANGUAGES = getAllLanguages();

  private Languages() {
  }

  /**
   * Language classes are detected at runtime by searching the classpath for files named
   * {@code META-INF/org/languagetool/language-module.properties}. Those file(s)
   * need to contain a key {@code languageClasses} which specifies the fully qualified
   * class name(s), e.g. {@code org.languagetool.language.English}. Use commas to specify
   * more than one class.
   * @return an unmodifiable list of all supported languages
   */
  public static List<Language> get() {
    List<Language> result = new ArrayList<>();
    for (Language lang : LANGUAGES) {
      if (!"xx".equals(lang.getShortName())) {  // skip demo language
        result.add(lang);
      }
    }
    return Collections.unmodifiableList(result);
  }

  /**
   * Like {@link #get()} but the list contains also LanguageTool's internal 'Demo'
   * language, if available. Only useful for tests.
   * @return an unmodifiable list
   */
  public static List<Language> getWithDemoLanguage() {
    return LANGUAGES;
  }

  private static List<Language> getAllLanguages() {
    return Collections.unmodifiableList(Arrays.asList(Language.LANGUAGES));
  }

  /**
   * Get the Language object for the given language name.
   *
   * @param languageName e.g. <code>English</code> or <code>German</code> (case is significant)
   * @return a Language object or {@code null} if there is no such language
   */
  public static Language getLanguageForName(final String languageName) {
    return Language.getLanguageForName(languageName);
  }

  /**
   * Get the Language object for the given short language name.
   *
   * @param langCode e.g. <code>en</code> or <code>es-US</code>
   * @return a Language object
   * @throws IllegalArgumentException if the language is not supported or if the language code is invalid
   */
  public static Language getLanguageForShortName(final String langCode) {
    return Language.getLanguageForShortName(langCode);
  }

  /**
   * Return whether a language with the given language code is supported. Which languages
   * are supported depends on the classpath when the {@code Language} object is initialized.
   *
   * @param langCode e.g. {@code en} or {@code en-US}
   * @return true if the language is supported
   * @throws IllegalArgumentException in some cases of an invalid language code format
   */
  public static boolean isLanguageSupported(final String langCode) {
    return Language.isLanguageSupported(langCode);
  }

  /**
   * Get the best match for a locale, using American English as the final fallback if nothing
   * else fits. The returned language will be a country variant language (e.g. British English, not just English)
   * if available.
   * @throws RuntimeException if no language was found and American English as a fallback is not available
   */
  public static Language getLanguageForLocale(final Locale locale) {
    return Language.getLanguageForLocale(locale);
  }

}
