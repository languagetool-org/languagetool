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

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.languagetool.Languages;

/**
 * A panel for selecting the GUI language, can be embedded to the main Configuration Dialog
 *
 * @author Panagiotis Minos
 * @since 3.4
 */
class GuiLangConfigPanel extends JPanel implements SavablePanel, ItemListener {

  private final LocalStorage storage;
  private final LanguageComboBox languageBox;
  private final LanguageAdapter system;
  private LanguageAdapter guiLang = null;

  GuiLangConfigPanel(ResourceBundle messages, LocalStorage storage) {
    super(new GridBagLayout());
    applyComponentOrientation(
      ComponentOrientation.getOrientation(Locale.getDefault()));
    this.storage = storage;
    system = new LanguageAdapter(messages.getString("guiLanguageSystem"));
    //create a ComboBox with flags, do not include hidden languages,
    //use system as first option
    languageBox = LanguageComboBox.create(messages, "", true, false, system);

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 4, 2, 0);
    c.gridx = 0;
    c.gridy = 0;
    add(new JLabel(messages.getString("guiLanguage")), c);
    c.insets = new Insets(2, 4, 2, 0);
    c.gridx = 1;
    add(languageBox, c);
    c.insets = new Insets(2, 4, 2, 0);
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 2;

    c.fill = GridBagConstraints.HORIZONTAL;
    JLabel warn = new JLabel("<html>" + messages.getString("quiLanguageNeedsRestart"));
    warn.setForeground(Color.red);
    add(warn, c);
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      guiLang = (LanguageAdapter) e.getItem();
    }
  }

  @Override
  public void componentShowing() {
    languageBox.removeItemListener(this);
    loadOption();
    languageBox.addItemListener(this);
  }

  @Override
  public void save() {
    if (guiLang == null) {
      return;
    }
    if (guiLang.getLanguage() != null) {
      storage.saveProperty("gui.locale", new LocaleBean(
              guiLang.getLanguage().getLocaleWithCountryAndVariant()));
    } else {
      storage.saveProperty("gui.locale", null);
    }
  }

  private void loadOption() {
    LocaleBean lang = storage.loadProperty("gui.locale", LocaleBean.class);
    if (lang != null) {
      Locale l = lang.asLocale();
      languageBox.selectLanguage(Languages.getLanguageForLocale(l));
    } else {
      languageBox.setSelectedItem(system);
    }
  }
}
