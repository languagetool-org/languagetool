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

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.tools.MultiKeyProperties;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;

/**
 * Helper methods to list all supported languages and to get language objects
 * by their name or language code etc.
 * @since 2.9
 */
public final class Languages {

  private static final List<Language> LANGUAGES = getAllLanguages();
  private static final String PROPERTIES_PATH = "META-INF/org/languagetool/language-module.properties";
  private static final String PROPERTIES_KEY = "languageClasses";

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
    List<Language> languages = new ArrayList<>();
    Set<String> languageClassNames = new HashSet<>();
    try {
      Enumeration<URL> propertyFiles = Language.class.getClassLoader().getResources(PROPERTIES_PATH);
      while (propertyFiles.hasMoreElements()) {
        URL url = propertyFiles.nextElement();
        try (InputStream inputStream = url.openStream()) {
          // We want to be able to read properties file with duplicate key, as produced by
          // Maven when merging files:
          MultiKeyProperties props = new MultiKeyProperties(inputStream);
          List<String> classNamesStr = props.getProperty(PROPERTIES_KEY);
          if (classNamesStr == null) {
            throw new RuntimeException("Key '" + PROPERTIES_KEY + "' not found in " + url);
          }
          for (String classNames : classNamesStr) {
            String[] classNamesSplit = classNames.split("\\s*,\\s*");
            for (String className : classNamesSplit) {
              if (languageClassNames.contains(className)) {
                // avoid duplicates - this way we are robust against problems with the maven assembly
                // plugin which aggregates files more than once (in case the deployment descriptor
                // contains both <format>zip</format> and <format>dir</format>):
                continue;
              }
              languages.add(createLanguageObjects(url, className));
              languageClassNames.add(className);
            }
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return Collections.unmodifiableList(languages);
  }

  private static Language createLanguageObjects(URL url, String className) {
    try {
      Class<?> aClass = Class.forName(className);
      Constructor<?> constructor = aClass.getConstructor();
      return (Language) constructor.newInstance();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Class '" + className + "' specified in " + url + " could not be found in classpath", e);
    } catch (Exception e) {
      throw new RuntimeException("Object for class '" + className + "' specified in " + url + " could not created", e);
    }
  }

  /**
   * Get the Language object for the given language name.
   *
   * @param languageName e.g. <code>English</code> or <code>German</code> (case is significant)
   * @return a Language object or {@code null} if there is no such language
   */
  @Nullable
  public static Language getLanguageForName(String languageName) {
    for (Language element : LANGUAGES) {
      if (languageName.equals(element.getName())) {
        return element;
      }
    }
    return null;
  }

  /**
   * Get the Language object for the given short language name.
   *
   * @param langCode e.g. <code>en</code> or <code>es-US</code>
   * @return a Language object
   * @throws IllegalArgumentException if the language is not supported or if the language code is invalid
   */
  public static Language getLanguageForShortName(String langCode) {
    Language language = getLanguageForShortNameOrNull(langCode);
    if (language == null) {
      List<String> codes = new ArrayList<>();
      for (Language realLanguage : LANGUAGES) {
        codes.add(realLanguage.getShortNameWithCountryAndVariant());
      }
      Collections.sort(codes);
      throw new IllegalArgumentException("'" + langCode + "' is not a language code known to LanguageTool." +
              " Supported language codes are: " + StringUtils.join(codes, ", ") + ". The list of languages is read from " + PROPERTIES_PATH +
              " in the Java classpath. See http://wiki.languagetool.org/java-api for details.");
    }
    return language;
  }

  /**
   * Return whether a language with the given language code is supported. Which languages
   * are supported depends on the classpath when the {@code Language} object is initialized.
   *
   * @param langCode e.g. {@code en} or {@code en-US}
   * @return true if the language is supported
   * @throws IllegalArgumentException in some cases of an invalid language code format
   */
  public static boolean isLanguageSupported(String langCode) {
    return getLanguageForShortNameOrNull(langCode) != null;
  }

  /**
   * Get the best match for a locale, using American English as the final fallback if nothing
   * else fits. The returned language will be a country variant language (e.g. British English, not just English)
   * if available.
   * @throws RuntimeException if no language was found and American English as a fallback is not available
   */
  public static Language getLanguageForLocale(Locale locale) {
    Language language = getLanguageForLanguageNameAndCountry(locale);
    if (language != null) {
      return language;
    } else {
      Language firstFallbackLanguage = getLanguageForLanguageNameOnly(locale);
      if (firstFallbackLanguage != null) {
        return firstFallbackLanguage;
      }
    }
    for (Language aLanguage : LANGUAGES) {
      if (aLanguage.getShortNameWithCountryAndVariant().equals("en-US")) {
        return aLanguage;
      }
    }
    throw new RuntimeException("No appropriate language found, not even en-US. Supported languages: " + get());
  }

  @Nullable
  private static Language getLanguageForShortNameOrNull(String langCode) {
    StringTools.assureSet(langCode, "langCode");
    Language result = null;
    if (langCode.contains("-x-")) {
      // e.g. "de-DE-x-simple-language"
      for (Language element : LANGUAGES) {
        if (element.getShortName().equalsIgnoreCase(langCode)) {
          return element;
        }
      }
    } else if (langCode.contains("-")) {
      String[] parts = langCode.split("-");
      if (parts.length == 2) { // e.g. en-US
        for (Language element : LANGUAGES) {
          if (parts[0].equalsIgnoreCase(element.getShortName())
                  && element.getCountries().length == 1
                  && parts[1].equalsIgnoreCase(element.getCountries()[0])) {
            result = element;
            break;
          }
        }
      } else if (parts.length == 3) { // e.g. ca-ES-valencia
        for (Language element : LANGUAGES) {
          if (parts[0].equalsIgnoreCase(element.getShortName())
                  && element.getCountries().length == 1
                  && parts[1].equalsIgnoreCase(element.getCountries()[0])
                  && parts[2].equalsIgnoreCase(element.getVariant())) {
            result = element;
            break;
          }
        }
      } else {
        throw new IllegalArgumentException("'" + langCode + "' isn't a valid language code");
      }
    } else {
      for (Language element : LANGUAGES) {
        if (langCode.equalsIgnoreCase(element.getShortName())) {
          result = element;
            /* TODO: It should return the DefaultLanguageVariant,
             * not the first language found */
          break;
        }
      }
    }
    return result;
  }

  @Nullable
  private static Language getLanguageForLanguageNameAndCountry(Locale locale) {
    for (Language language : LANGUAGES) {
      if (language.getShortName().equals(locale.getLanguage())) {
        List<String> countryVariants = Arrays.asList(language.getCountries());
        if (countryVariants.contains(locale.getCountry())) {
          return language;
        }
      }
    }
    return null;
  }

  @Nullable
  private static Language getLanguageForLanguageNameOnly(Locale locale) {
    // use default variant if available:
    for (Language language : LANGUAGES) {
      if (language.getShortName().equals(locale.getLanguage()) && language.hasVariant()) {
        Language defaultVariant = language.getDefaultLanguageVariant();
        if (defaultVariant != null) {
          return defaultVariant;
        }
      }
    }
    // use the first match otherwise (which should be the only match):
    for (Language language : LANGUAGES) {
      if (language.getShortName().equals(locale.getLanguage()) && !language.hasVariant()) {
        return language;
      }
    }
    return null;
  }

}
