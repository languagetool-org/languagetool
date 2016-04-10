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

import java.awt.ComponentOrientation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JComboBox;

import org.languagetool.Language;
import org.languagetool.Languages;

/**
 * Combo box with list of available languages.
 */
public class LanguageComboBox extends JComboBox<Language> {

  private final List<Language> languages = new ArrayList<>();
  private final LanguageComparator langComparator;

  public LanguageComboBox(ResourceBundle messages, String extLangSuffix) {
    this.langComparator = new LanguageComparator(messages, extLangSuffix);
    populateLanguageBox();
  }

  final void populateLanguageBox(List<Language> externalLanguages) {
    removeAllItems();
    initAllLanguages(externalLanguages);
  }

  final void populateLanguageBox() {
    populateLanguageBox(Collections.<Language>emptyList());
  }

  void selectLanguage(Language language) {
    for (Language lang : languages) {
      if (lang.toString().equals(language.toString())) {
        setSelectedItem(lang);
      }
    }
  }

  private void initAllLanguages(List<Language> externalLanguages) {
    applyComponentOrientation(
      ComponentOrientation.getOrientation(Locale.getDefault()));
    languages.clear();
    for (Language language : Languages.get()) {  // the method returns both built-in and external languages
      if (!language.isHiddenFromGui()) {
        languages.add(language);
      }
    }
    for (Language externalLanguage : externalLanguages) {
      addItem(externalLanguage);
    }
    Collections.sort(languages, langComparator);
    for (Language language : languages) {
      addItem(language);
    }
  }
}
