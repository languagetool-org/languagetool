/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;

import org.apache.tika.language.LanguageIdentifier;
import org.languagetool.Language;

class LanguageDetectionCheckbox extends JCheckBox {

  private final LanguageComboBox languageBox;

  LanguageDetectionCheckbox(final ResourceBundle messages, final LanguageComboBox languageBox, final Configuration config) {
    super(messages.getString("atd"));
    addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        languageBox.setEnabled(!isSelected());
        config.setAutoDetect(isSelected());
      }
    });
    setSelected(config.getAutoDetect());
    this.languageBox = languageBox;
  }

  Language autoDetectLanguage(String text) {
    final LanguageIdentifier langIdentifier = new LanguageIdentifier(text);
    Language lang;
    try {
      lang = Language.getLanguageForShortName(langIdentifier.getLanguage());
    } catch (IllegalArgumentException e) {
      lang = Language.AMERICAN_ENGLISH;
    }
    if (lang.hasVariant()) {
      // UI only shows variants like "English (American)", not just "English", so use that:
      lang = lang.getDefaultVariant();
    }
    languageBox.selectLanguage(lang);
    return lang;
  }

}
