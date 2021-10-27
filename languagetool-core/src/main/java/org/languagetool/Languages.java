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

import org.jetbrains.annotations.Nullable;
import org.languagetool.noop.NoopLanguage;
import org.languagetool.tools.MultiKeyProperties;
import org.languagetool.tools.StringTools;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper methods to list all supported languages and to get language objects
 * by their name or language code etc.
 * @since 2.9
 */
public final class Languages {

  private static final String PROPERTIES_PATH = "META-INF/org/languagetool/language-module.properties";
  private static final String PROPERTIES_KEY = "languageClasses";
  private static final Language NOOP_LANGUAGE = new NoopLanguage();

  private static final List<Language> languages = getAllLanguages();
  private static final List<Language> dynLanguages = new ArrayList<>();
  
  private Languages() {
  }

  /**
   * @since 4.5
   */
  public static Language addLanguage(String name, String code, File dictPath) {
    Language lang;
    if (dictPath.getName().endsWith(JLanguageTool.DICTIONARY_FILENAME_EXTENSION)) {
      lang = new DynamicMorfologikLanguage(name, code, dictPath);
    } else if (dictPath.getName().endsWith(".dic")) {
      lang = new DynamicHunspellLanguage(name, code, dictPath);
    } else {
      throw new RuntimeException("Please specify a dictPath that ends in '.dict' (Morfologik binary dictionary) or '.dic' (Hunspell dictionary): " + dictPath);
    }
    dynLanguages.add(lang);
    return lang;
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
    for (Language lang : getStaticAndDynamicLanguages()) {
      if (!"xx".equals(lang.getShortCode()) && !"zz".equals(lang.getShortCode())) {  // skip demo and noop language
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
    return Collections.unmodifiableList(getStaticAndDynamicLanguages());
  }

  private static List<Language> getStaticAndDynamicLanguages() {
    return Stream.concat(languages.stream(), dynLanguages.stream()).collect(Collectors.toList());
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
      if (Premium.isPremiumVersion() && hasPremium(className)) {
        className = className + "Premium";
      }
      Class<?> aClass = JLanguageTool.getClassBroker().forName(className);
      Constructor<?> constructor = aClass.getConstructor();
      return (Language) constructor.newInstance();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Class '" + className + "' specified in " + url + " could not be found in classpath", e);
    } catch (Exception e) {
      throw new RuntimeException("Object for class '" + className + "' specified in " + url + " could not be created", e);
    }
  }

  private static boolean hasPremium(String className) {
    return className.matches("org\\.languagetool\\.language\\.(German|GermanyGerman|AustrianGerman|SwissGerman|Dutch|French|Spanish|English|AustralianEnglish|AmericanEnglish|BritishEnglish|CanadianEnglish|NewZealandEnglish|SouthAfricanEnglish)");
  }

  /**
   * Get the Language object for the given language class name or try to create it and add to dynamic languages.
   *
   * @param className e.g. <code>org.languagetool.language.English</code>
   * @return a Language object
   * @throws RuntimeException if language not found in classpath
   * @since 5.0
   */
  public static Language getOrAddLanguageByClassName(String className) {
    for (Language element : getStaticAndDynamicLanguages()) {
      if (className.equals(element.getClass().getName())) {
        return element;
      }
    }
    try {
      Class<?> aClass = JLanguageTool.getClassBroker().forName(className);
      Constructor<?> constructor = aClass.getConstructor();
      Language language = (Language) constructor.newInstance();
      dynLanguages.add(language);
      return language;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Class '" + className + " could not be found in classpath", e);
    } catch (Exception e) {
      throw new RuntimeException("Object for class '" + className + " could not be created", e);
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
    for (Language element : getStaticAndDynamicLanguages()) {
      if (languageName.equals(element.getName())) {
        return element;
      }
    }
    return null;
  }

  /**
   * Get the Language object for the given language code.
   * @param langCode e.g. <code>en</code> or <code>en-US</code>
   * @throws IllegalArgumentException if the language is not supported or if the language code is invalid
   * @since 3.6
   */
  public static Language getLanguageForShortCode(String langCode) {
    return getLanguageForShortCode(langCode, Collections.emptyList());
  }
  
  /**
   * Get the Language object for the given language code.
   * @param langCode e.g. <code>en</code> or <code>en-US</code>
   * @param noopLanguageCodes list of languages that can be detected but that will not actually find any errors
   *                           (can be used so non-supported languages are not detected as some other language)
   * @throws IllegalArgumentException if the language is not supported or if the language code is invalid
   * @since 4.4
   */
  public static Language getLanguageForShortCode(String langCode, List<String> noopLanguageCodes) {
    Language language = getLanguageForShortCodeOrNull(langCode);
    if (language == null) {
      if (noopLanguageCodes.contains(langCode)) {
        return NOOP_LANGUAGE;
      } else {
        List<String> codes = new ArrayList<>();
        for (Language realLanguage : getStaticAndDynamicLanguages()) {
          codes.add(realLanguage.getShortCodeWithCountryAndVariant());
        }
        Collections.sort(codes);
        throw new IllegalArgumentException("'" + langCode + "' is not a language code known to LanguageTool." +
                " Supported language codes are: " + String.join(", ", codes) + ". The list of languages is read from " + PROPERTIES_PATH +
                " in the Java classpath. See https://dev.languagetool.org/java-api for details.");
      }
    }
    return language;
  }

  /**
   * Return whether a language with the given language code is supported. Which languages
   * are supported depends on the classpath when the {@code Language} object is initialized.
   * @param langCode e.g. {@code en} or {@code en-US}
   * @return true if the language is supported
   * @throws IllegalArgumentException in some cases of an invalid language code format
   */
  public static boolean isLanguageSupported(String langCode) {
    return getLanguageForShortCodeOrNull(langCode) != null;
  }

  /**
   * Get the best match for a locale, using American English as the final fallback if nothing
   * else fits. The returned language will be a country variant language (e.g. British English, not just English)
   * if available.
   * Note: this does not consider languages added dynamically
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
    for (Language aLanguage : languages) {
      if (aLanguage.getShortCodeWithCountryAndVariant().equals("en-US")) {
        return aLanguage;
      }
    }
    throw new RuntimeException("No appropriate language found, not even en-US. Supported languages: " + get());
  }

  @Nullable
  private static Language getLanguageForShortCodeOrNull(String langCode) {
    StringTools.assureSet(langCode, "langCode");
    Language result = null;
    if (langCode.contains("-x-")) {
      // e.g. "de-DE-x-simple-language"
      for (Language element : getStaticAndDynamicLanguages()) {
        if (element.getShortCode().equalsIgnoreCase(langCode)) {
          return element;
        }
      }
    } else if (langCode.contains("-")) {
      String[] parts = langCode.split("-");
      if (parts.length == 2) { // e.g. en-US
        for (Language element : getStaticAndDynamicLanguages()) {
          if (parts[0].equalsIgnoreCase(element.getShortCode())
                  && element.getCountries().length == 1
                  && parts[1].equalsIgnoreCase(element.getCountries()[0])) {
            result = element;
            break;
          }
        }
      } else if (parts.length == 3) { // e.g. ca-ES-valencia
        for (Language element : getStaticAndDynamicLanguages()) {
          if (parts[0].equalsIgnoreCase(element.getShortCode())
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
      for (Language element : getStaticAndDynamicLanguages()) {
        if (langCode.equals("global")) {
          // for disambiguation-global.xml take any language
          result = element;
          break;
        }
        if (langCode.equalsIgnoreCase(element.getShortCode())) {
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
    for (Language language : getStaticAndDynamicLanguages()) {
      if (language.getShortCode().equals(locale.getLanguage())) {
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
    for (Language language : getStaticAndDynamicLanguages()) {
      if (language.getShortCode().equals(locale.getLanguage()) && language.hasVariant()) {
        Language defaultVariant = language.getDefaultLanguageVariant();
        if (defaultVariant != null) {
          return defaultVariant;
        }
      }
    }
    // use the first match otherwise (which should be the only match):
    for (Language language : getStaticAndDynamicLanguages()) {
      if (language.getShortCode().equals(locale.getLanguage()) && !language.hasVariant()) {
        return language;
      }
    }
    return null;
  }

}
