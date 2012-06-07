/* LanguageTool, a natural language style checker 
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.gui;

import org.languagetool.Language;

import javax.swing.*;
import java.util.*;

/**
 * Combo box with list of available languages.
 */
public class LanguageComboBox extends JComboBox {

  private final ResourceBundle messages;

  public LanguageComboBox(ResourceBundle messages) {
    this.messages = messages;
    populateLanguageBox();
  }

  void populateLanguageBox() {
    removeAllItems();
    final List<I18nLanguage> i18nLanguages = getAllLanguages();
    preselectDefaultLanguage(i18nLanguages);
  }

  private List<I18nLanguage> getAllLanguages() {
    final List<I18nLanguage> i18nLanguages = new ArrayList<I18nLanguage>();
    for (Language language : Language.LANGUAGES) {
      final boolean skip = (language == Language.DEMO) || language.hasVariant();
      if (!skip) {
        i18nLanguages.add(new I18nLanguage(language, messages));
      }
    }
    Collections.sort(i18nLanguages);
    return i18nLanguages;
  }

  private void preselectDefaultLanguage(List<I18nLanguage> i18nLanguages) {
    final String defaultGuiLocale = getDefaultGuiLanguage(Locale.getDefault());
    for (final I18nLanguage i18nLanguage : i18nLanguages) {
      addItem(i18nLanguage);
      if (i18nLanguage.toString().equals(defaultGuiLocale)) {
        setSelectedItem(i18nLanguage);
      }
    }
  }

  private String getDefaultGuiLanguage(Locale defaultLocale) {
    String defaultGuiLocale = null;
    try {
      defaultGuiLocale = messages.getString(defaultLocale.getLanguage() + "-" + defaultLocale.getCountry());
    } catch (final MissingResourceException e) {
      // this specific language/variant combination is not supported
      try {
        defaultGuiLocale = messages.getString(defaultLocale.getLanguage());
      } catch (final MissingResourceException e2) {
        // language not supported, so don't select a default
      }
    }
    return defaultGuiLocale;
  }

}
