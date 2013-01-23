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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.languagetool.tools.StringTools;

/**
 * The menu bar of the main dialog.
 * 
 * @author Daniel Naber
 */
class MainMenuBar extends JMenuBar implements ActionListener {

  private static final long serialVersionUID = -7160998682243081767L;
  private static final int NO_KEY_EVENT = -1;

  private final ResourceBundle messages;
  private final Main prg;

  // File:
  private final String openText;
  private final String checkClipboardText;
  private final String dockToTrayText;
  private final String addLanguageText;
  private final String optionsText;
  private final String tagText;
  private final String quitText;
  // Help:
  private final String aboutText;

  MainMenuBar(Main prg, ResourceBundle messages) {
    this.prg = prg;
    this.messages = messages;
    final JMenu fileMenu = new JMenu(getLabel("guiMenuFile"));
    fileMenu.setMnemonic(getMnemonic("guiMenuFile"));
    final JMenu helpMenu = new JMenu(getLabel("guiMenuHelp"));
    helpMenu.setMnemonic(getMnemonic("guiMenuHelp"));

    openText = addMenuItem("guiMenuOpen", KeyEvent.VK_O, fileMenu);
    checkClipboardText = addMenuItem("guiMenuCheckClipboard", KeyEvent.VK_Y, fileMenu);
    tagText = addMenuItem("guiTagText", KeyEvent.VK_T, fileMenu);
    addLanguageText = addMenuItem("guiMenuAddRules", NO_KEY_EVENT, fileMenu);
    optionsText = addMenuItem("guiMenuOptions", KeyEvent.VK_S, fileMenu);
    fileMenu.addSeparator();
    dockToTrayText = addMenuItem("guiMenuHide", KeyEvent.VK_D, fileMenu);
    fileMenu.addSeparator();
    quitText = addMenuItem("guiMenuQuit", KeyEvent.VK_Q, fileMenu);
    add(fileMenu);

    aboutText = addMenuItem("guiMenuAbout", NO_KEY_EVENT, helpMenu);
    add(helpMenu);
  }

  private char getMnemonic(String key) {
    return StringTools.getMnemonic(messages.getString(key));
  }

  private String addMenuItem(String key, int keyEvent, JMenu menu) {
    final String label = getLabel(key);
    final JMenuItem openItem = new JMenuItem(label);
    openItem.setMnemonic(getMnemonic(key));
    openItem.addActionListener(this);
    if (keyEvent != NO_KEY_EVENT) {
      openItem.setAccelerator(getCtrlKeyStroke(keyEvent));
    }
    menu.add(openItem);
    return label;
  }

  private String getLabel(String key) {
    return StringTools.getLabel(messages.getString(key));
  }

  private KeyStroke getCtrlKeyStroke(int keyEvent) {
    return KeyStroke.getKeyStroke(keyEvent, InputEvent.CTRL_MASK);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals(openText)) {
      prg.loadFile();
    } else if (e.getActionCommand().equals(checkClipboardText)) {
      prg.checkClipboardText();
    } else if (e.getActionCommand().equals(dockToTrayText)) {
      prg.hideToTray();
    } else if (e.getActionCommand().equals(addLanguageText)) {
      prg.addLanguage();
    } else if (e.getActionCommand().equals(tagText)) {
      prg.tagText();
    } else if (e.getActionCommand().equals(optionsText)) {
      prg.showOptions();
    } else if (e.getActionCommand().equals(quitText)) {
      prg.quit();
    } else if (e.getActionCommand().equals(aboutText)) {
      final AboutDialog about = new AboutDialog(messages);
      about.show();
    } else {
      throw new IllegalArgumentException("Unknown action " + e);
    }
  }
  
}
