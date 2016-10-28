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
package org.languagetool;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.languagetool.JLanguageTool.MESSAGE_BUNDLE;

/**
 * Message bundle helper class used for translation of the user interface.
 * @since 2.3
 */
final class ResourceBundleTools {

  private ResourceBundleTools() {
  }

  /**
   * Gets the ResourceBundle (i18n strings) for the default language of the user's system.
   */
  public static ResourceBundle getMessageBundle() {
    try {
      ResourceBundle bundle = ResourceBundle.getBundle(MESSAGE_BUNDLE);
      ResourceBundle fallbackBundle = ResourceBundle.getBundle(MESSAGE_BUNDLE, Locale.ENGLISH);
      return new ResourceBundleWithFallback(bundle, fallbackBundle);
    } catch (MissingResourceException e) {
      return ResourceBundle.getBundle(MESSAGE_BUNDLE, Locale.ENGLISH);
    }
  }

  /**
   * Gets the ResourceBundle (i18n strings) for the given user interface language.
   */
  static ResourceBundle getMessageBundle(Language lang) {
    try {
      ResourceBundle bundle = ResourceBundle.getBundle(MESSAGE_BUNDLE, lang.getLocaleWithCountryAndVariant());
      if (!isValidBundleFor(lang, bundle)) {
        bundle = ResourceBundle.getBundle(MESSAGE_BUNDLE, lang.getLocale());
        if (!isValidBundleFor(lang, bundle)) {
          // happens if 'xx' is requested but only a MessagesBundle_xx_YY.properties exists:
          Language defaultVariant = lang.getDefaultLanguageVariant();
          if (defaultVariant != null && defaultVariant.getCountries().length > 0) {
            Locale locale = new Locale(defaultVariant.getShortCode(), defaultVariant.getCountries()[0]);
            bundle = ResourceBundle.getBundle(MESSAGE_BUNDLE, locale);
          }
        }
      }
      ResourceBundle fallbackBundle = ResourceBundle.getBundle(MESSAGE_BUNDLE, Locale.ENGLISH);
      return new ResourceBundleWithFallback(bundle, fallbackBundle);
    } catch (MissingResourceException e) {
      return ResourceBundle.getBundle(MESSAGE_BUNDLE, Locale.ENGLISH);
    }
  }

  private static boolean isValidBundleFor(Language lang, ResourceBundle bundle) {
    return lang.getLocale().getLanguage().equals(bundle.getLocale().getLanguage());
  }
}
