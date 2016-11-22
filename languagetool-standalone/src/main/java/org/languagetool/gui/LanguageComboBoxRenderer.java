/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

import java.awt.Component;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.databroker.ResourceDataBroker;

/**
 * A ComboBox Renderer that can display a flag icon along with the language
 *
 * @author Panagiotis Minos
 */
class LanguageComboBoxRenderer extends JLabel implements ListCellRenderer<LanguageAdapter> {

  private static final Border BORDER = new EmptyBorder(1, 3, 1, 1);
  
  private final ResourceBundle messages;
  private final String extLangSuffix;

  LanguageComboBoxRenderer(ResourceBundle messages, String extLangSuffix) {
    super();
    setOpaque(true);
    setBorder(BORDER);
    this.messages = messages;
    this.extLangSuffix = extLangSuffix;
  }

  private String getTranslatedName(Language language) {
    String name = language.getTranslatedName(messages);
    if (language.isExternal()) {
      name += extLangSuffix;
    }
    return name;
  }

  @Override
  public Component getListCellRendererComponent(JList list, LanguageAdapter adapter, int index, boolean isSelected, boolean cellHasFocus) {
    setComponentOrientation(list.getComponentOrientation());
    if (isSelected) {
      setBackground(list.getSelectionBackground());
      setForeground(list.getSelectionForeground());
    } else {
      setBackground(list.getBackground());
      setForeground(list.getForeground());
    }
    if (adapter != null) {
      Language lang = adapter.getLanguage();
      if (lang != null) {
        setText(getTranslatedName(lang));
        String langTag = lang.getLocaleWithCountryAndVariant().toLanguageTag();
        String country = lang.getLocaleWithCountryAndVariant().getCountry().toLowerCase();
        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        String filename = "flags/bytag/" + langTag + ".png";
        if (!dataBroker.resourceExists(filename)) {
          filename = "flags/" + country + ".png";
        }
        if (!dataBroker.resourceExists(filename)) {
          filename = "flags/empty.png";
        }
        ImageIcon icon = new ImageIcon(dataBroker.getFromResourceDirAsUrl(filename));
        setIcon(icon);
      } else {
        setText(adapter.getValue());
        setIcon(null);
      }
    }
    setEnabled(list.isEnabled());
    setFont(list.getFont());
    setBorder(BORDER);
    return this;
  }
}
