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
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;

/**
 * A ComboBox Renderer that can display a flag icon along with the language
 *
 * @author Panagiotis Minos
 */
class LanguageComboBoxRenderer extends JLabel implements ListCellRenderer<I18nLanguage> {

  private static final Border BORDER = new EmptyBorder(1, 3, 1, 1);

  LanguageComboBoxRenderer() {
    super();
    setOpaque(true);
    setBorder(BORDER);
  }

  @Override
  public Component getListCellRendererComponent(JList list, I18nLanguage value, int index, boolean isSelected, boolean cellHasFocus) {
    setComponentOrientation(list.getComponentOrientation());
    if (isSelected) {
      setBackground(list.getSelectionBackground());
      setForeground(list.getSelectionForeground());
    } else {
      setBackground(list.getBackground());
      setForeground(list.getForeground());
    }
    setText(value.toString());
    String country = value.getLanguage().getLocaleWithCountry().getCountry().toLowerCase();
    String filename = "flags/" + country + ".png";
    ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    if (!dataBroker.resourceExists(filename)) {
      filename = "flags/empty.png";
    }
    ImageIcon icon = new ImageIcon(dataBroker.getFromResourceDirAsUrl(filename));
    setIcon(icon);
    setEnabled(list.isEnabled());
    setFont(list.getFont());
    setBorder(BORDER);
    return this;
  }
}
