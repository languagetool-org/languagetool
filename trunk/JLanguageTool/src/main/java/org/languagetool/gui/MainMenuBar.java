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

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

  private final ResourceBundle messages;
  
  // File:
  private String openText;
  private String checkClipboardText;
  private String dockToTrayText;
  private String addLanguageText;
  private String optionsText;
  private String tagText;
  private String quitText;  
  // Help:
  private String aboutText;

  private final Main prg;
  private JMenu fileMenu;
  private JMenu helpMenu;
  
  MainMenuBar(Main prg, ResourceBundle messages) {
    this.prg = prg;
    this.messages = messages;
    initStrings();
    fileMenu.setMnemonic(getMnemonic("guiMenuFile"));
    helpMenu.setMnemonic(getMnemonic("guiMenuHelp"));
    // "Open":
    final JMenuItem openItem = new JMenuItem(openText);
    openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
    openItem.setMnemonic(getMnemonic("guiMenuOpen"));
    openItem.addActionListener(this);
    fileMenu.add(openItem);
    // "Check Text in Clipboard":
    final JMenuItem checkClipboardItem = new JMenuItem(checkClipboardText);
    checkClipboardItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK));
    checkClipboardItem.setMnemonic(getMnemonic("guiMenuCheckClipboard"));
    checkClipboardItem.addActionListener(this);
    fileMenu.add(checkClipboardItem);
    // "Hide to System Tray":
    final JMenuItem dockToTrayItem = new JMenuItem(dockToTrayText);
    dockToTrayItem.setMnemonic(getMnemonic("guiMenuHide"));
    dockToTrayItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK));
    dockToTrayItem.addActionListener(this);
    fileMenu.add(dockToTrayItem);
    // "Add Language":
    final JMenuItem addLanguageItem = new JMenuItem(addLanguageText);
    addLanguageItem.setMnemonic(getMnemonic("guiMenuAddRules"));
    addLanguageItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK));
    addLanguageItem.addActionListener(this);
    fileMenu.add(addLanguageItem);
    // "Tag Text"
    final JMenuItem tagItem = new JMenuItem(tagText);
    tagItem.addActionListener(this);
    tagItem.setMnemonic(getMnemonic("guiTagText"));
    tagItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Event.CTRL_MASK));
    fileMenu.add(tagItem);
    // "Options":
    final JMenuItem optionsItem = new JMenuItem(optionsText);
    optionsItem.setMnemonic(getMnemonic("guiMenuOptions"));
    optionsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
    optionsItem.addActionListener(this);
    fileMenu.add(optionsItem);
    // "Quit":
    final JMenuItem quitItem = new JMenuItem(quitText);
    quitItem.setMnemonic(getMnemonic("guiMenuQuit"));
    quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK));
    quitItem.addActionListener(this);
    fileMenu.add(quitItem);
    // "About":
    final JMenuItem helpItem = new JMenuItem(aboutText);
    helpItem.addActionListener(this);
    helpItem.setMnemonic(getMnemonic("guiMenuAbout"));
    helpMenu.add(helpItem);   
    
    // add menus:
    add(fileMenu);
    add(helpMenu);
  }

  private void initStrings() {
    fileMenu = new JMenu(getLabel("guiMenuFile"));
    helpMenu = new JMenu(getLabel("guiMenuHelp"));
    // File:
    openText = getLabel("guiMenuOpen");
    checkClipboardText = getLabel("guiMenuCheckClipboard");
    dockToTrayText = getLabel("guiMenuHide");
    addLanguageText = getLabel("guiMenuAddRules");
    tagText = getLabel("guiTagText");
    optionsText = getLabel("guiMenuOptions");
    quitText = getLabel("guiMenuQuit");
    // Help:
    aboutText = getLabel("guiMenuAbout");
  }

  private char getMnemonic(String key) {
    return StringTools.getMnemonic(messages.getString(key));
  }

  private String getLabel(String key) {
    return StringTools.getLabel(messages.getString(key));
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
