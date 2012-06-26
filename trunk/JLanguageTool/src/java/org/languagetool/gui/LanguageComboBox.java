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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JComboBox;

import org.languagetool.Language;

/**
 * Combo box with list of available languages.
 */
public class LanguageComboBox extends JComboBox {

  private final ResourceBundle messages;
  private final List<I18nLanguage> i18nLanguages = new ArrayList<I18nLanguage>();

  public LanguageComboBox(ResourceBundle messages) {
    this.messages = messages;
    populateLanguageBox();
  }

  void populateLanguageBox() {
    removeAllItems();
    initAllLanguages();
    preselectDefaultLanguage();
  }

  void selectLanguage(Language language) {
    final String translatedName = language.getTranslatedName(messages);
    for (final I18nLanguage i18nLanguage : i18nLanguages) {
      if (i18nLanguage.toString().equals(translatedName)) {
        setSelectedItem(i18nLanguage);
      }
    }
  }

  private void initAllLanguages() {
    i18nLanguages.clear();
    for (Language language : Language.LANGUAGES) {
      final boolean skip = (language == Language.DEMO) || language.hasVariant();
      if (!skip) {
        i18nLanguages.add(new I18nLanguage(language, messages));
      }
    }
    Collections.sort(i18nLanguages);
    for (final I18nLanguage i18nLanguage : i18nLanguages) {
      addItem(i18nLanguage);
    }
  }

  private void preselectDefaultLanguage() {
    final Language defaultLanguage = Language.getLanguageForLocale(Locale.getDefault());
    selectLanguage(defaultLanguage);
  }

}
