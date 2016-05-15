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
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.ComboBoxModel;

import javax.swing.JComboBox;

import org.languagetool.Language;

/**
 * Combo box with list of available languages.
 */
class LanguageComboBox extends JComboBox<LanguageAdapter> {

  static LanguageComboBox create(ResourceBundle messages, String extLangSuffix,
          boolean withFlag, boolean includeHidden) {
    return create(messages, extLangSuffix, withFlag, includeHidden, null);
  }

  static LanguageComboBox create(ResourceBundle messages, String extLangSuffix,
          boolean withFlag, boolean includeHidden, LanguageAdapter first) {
    LanguageComboBox combo = new LanguageComboBox();
    combo.setModel(LanguageComboBoxModel.create(messages, extLangSuffix, includeHidden, null, first));
    if(withFlag) {
      combo.setRenderer(new LanguageComboBoxRenderer(messages, extLangSuffix));
    }
    return combo;
  }  

  private LanguageComboBox() {
    applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
  }

  void selectLanguage(Language language) {
    ComboBoxModel<LanguageAdapter> model = getModel();
    for(int i = 0; i < model.getSize(); i++) {
      LanguageAdapter adapter = model.getElementAt(i);
      if(adapter.getLanguage() == null) {
        continue;
      }
      if (adapter.getLanguage().toString().equals(language.toString())) {
        setSelectedItem(adapter);
        break;
      }      
    }
  }

  Language getSelectedLanguage() {
    LanguageAdapter adapter = (LanguageAdapter) getSelectedItem();
    return adapter.getLanguage();
  }
}
