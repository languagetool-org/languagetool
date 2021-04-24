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

import java.util.*;

import static org.languagetool.JLanguageTool.MESSAGE_BUNDLE;

/**
 * Message bundle helper class used for translation of the user interface.
 * @since 2.3
 */
public final class ResourceBundleTools {

  private ResourceBundleTools() {
  }

  /**
   * Gets the ResourceBundle (i18n strings) for the default language of the user's system.
   */
  public static ResourceBundle getMessageBundle() {
    try {
      ResourceBundle bundle = JLanguageTool.getDataBroker().getResourceBundle(MESSAGE_BUNDLE, Locale.getDefault());
      ResourceBundle fallbackBundle = JLanguageTool.getDataBroker().getResourceBundle(MESSAGE_BUNDLE, Locale.ENGLISH);
      return new ResourceBundleWithFallback(bundle, fallbackBundle);
    } catch (MissingResourceException e) {
      return JLanguageTool.getDataBroker().getResourceBundle(MESSAGE_BUNDLE, Locale.ENGLISH);
    }
  }

  /**
   * Gets the ResourceBundle (i18n strings) for the given user interface language.
   */
  public static ResourceBundle getMessageBundle(Language lang) {
    try {
      ResourceBundle bundle = JLanguageTool.getDataBroker().getResourceBundle(MESSAGE_BUNDLE, lang.getLocaleWithCountryAndVariant());
      if (!isValidBundleFor(lang, bundle)) {
        bundle = JLanguageTool.getDataBroker().getResourceBundle(MESSAGE_BUNDLE, lang.getLocale());
        if (!isValidBundleFor(lang, bundle)) {
          // happens if 'xx' is requested but only a MessagesBundle_xx_YY.properties exists:
          Language defaultVariant = lang.getDefaultLanguageVariant();
          if (defaultVariant != null && defaultVariant.getCountries().length > 0) {
            Locale locale = new Locale(defaultVariant.getShortCode(), defaultVariant.getCountries()[0]);
            bundle = JLanguageTool.getDataBroker().getResourceBundle(MESSAGE_BUNDLE, locale);
          }
        }
      }
      ResourceBundle fallbackBundle = JLanguageTool.getDataBroker().getResourceBundle(MESSAGE_BUNDLE, Locale.ENGLISH);
      return new ResourceBundleWithFallback(bundle, fallbackBundle);
    } catch (MissingResourceException e) {
      return JLanguageTool.getDataBroker().getResourceBundle(MESSAGE_BUNDLE, Locale.ENGLISH);
    }
  }

  private static boolean isValidBundleFor(Language lang, ResourceBundle bundle) {
    return lang.getLocale().getLanguage().equals(bundle.getLocale().getLanguage());
  }
}
