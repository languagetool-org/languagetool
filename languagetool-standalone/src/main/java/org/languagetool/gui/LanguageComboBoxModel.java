/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
import java.util.ResourceBundle;
import javax.swing.DefaultComboBoxModel;
import org.languagetool.Language;
import org.languagetool.Languages;

/**
 * A DefaultComboBoxModel to be used by LanguageComboBox.
 *
 * @author Panagiotis Minos
 * @since 3.4
 */
class LanguageComboBoxModel extends DefaultComboBoxModel<LanguageAdapter> {

  private LanguageComboBoxModel() {
    super();
  }

  static LanguageComboBoxModel create(ResourceBundle messages,
          String extLangSuffix, boolean includeHidden,
          List<Language> external, LanguageAdapter first) {

    LanguageComparator comparator = new LanguageComparator(messages, extLangSuffix);
    LanguageComboBoxModel model = new LanguageComboBoxModel();

    if (first != null) {
      //e.g. an option like "System Default"
      model.addElement(first);
    }
    if (external != null) {
      // do not sort the original list
      ArrayList<Language> ext = new ArrayList<>(external);
      Collections.sort(ext, comparator);
      for (Language l : ext) {
        model.addElement(new LanguageAdapter(l));
      }
    }
    // the original list is unmodifiable
    ArrayList<Language> internal = new ArrayList<>(Languages.get());
    Collections.sort(internal, comparator);
    for (Language l : internal) {
      if (includeHidden || !l.isHiddenFromGui()) {
        model.addElement(new LanguageAdapter(l));
      }
    }
    return model;
  }
}
