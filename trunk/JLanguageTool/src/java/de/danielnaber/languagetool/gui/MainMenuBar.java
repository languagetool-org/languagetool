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
package de.danielnaber.languagetool.gui;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

class MainMenuBar extends JMenuBar implements ActionListener {

  private ResourceBundle messages = null;
  
  // File:
  private String openText;
  private String checkClipboardText;
  private String docktoTrayText;
  private String optionsText;
  private String quitText;
  // Help:
  private String aboutText;

  private Main prg = null;
  private JMenu fileMenu = null;
  private JMenu helpMenu = null;
  
  MainMenuBar(Main prg, ResourceBundle messages) {
    this.prg = prg;
    this.messages = messages;
    initStrings();
    // FIXME: i18n these:
    fileMenu.setMnemonic('f');
    helpMenu.setMnemonic('h');
    // "Open":
    JMenuItem openItem = new JMenuItem(openText);
    openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
    openItem.setMnemonic('o');
    openItem.addActionListener(this);
    fileMenu.add(openItem);
    // "Check Text in Clipboard":
    JMenuItem checkClipboardItem = new JMenuItem(checkClipboardText);
    checkClipboardItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK));
    checkClipboardItem.setMnemonic('c');
    checkClipboardItem.addActionListener(this);
    fileMenu.add(checkClipboardItem);
    // "Hide to System Tray":
    JMenuItem dockToTrayItem = new JMenuItem(docktoTrayText);
    dockToTrayItem.setMnemonic('d');
    dockToTrayItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK));
    dockToTrayItem.addActionListener(this);
    fileMenu.add(dockToTrayItem);
    // "Options":
    JMenuItem optionsItem = new JMenuItem(optionsText);
    optionsItem.setMnemonic('s');
    optionsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
    optionsItem.addActionListener(this);
    fileMenu.add(optionsItem);
    // "Quit":
    JMenuItem quitItem = new JMenuItem(quitText);
    quitItem.setMnemonic('q');
    quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK));
    quitItem.addActionListener(this);
    fileMenu.add(quitItem);
    // "About":
    JMenuItem helpItem = new JMenuItem(aboutText);
    helpItem.addActionListener(this);
    helpMenu.add(helpItem);
    // add menus:
    add(fileMenu);
    add(helpMenu);
  }

  private void initStrings() {
    fileMenu = new JMenu(messages.getString("guiMenuFile"));
    helpMenu = new JMenu(messages.getString("guiMenuHelp"));
    // File:
    openText = messages.getString("guiMenuOpen");
    checkClipboardText = messages.getString("guiMenuCheckClipboard");
    docktoTrayText = messages.getString("guiMenuHide");
    optionsText = messages.getString("guiMenuOptions");
    quitText = messages.getString("guiMenuQuit");
    // Help:
    aboutText = messages.getString("guiMenuAbout");
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals(openText)) {
      prg.loadFile();
    } else if (e.getActionCommand().equals(checkClipboardText)) {
      prg.checkClipboardText();
    } else if (e.getActionCommand().equals(docktoTrayText)) {
      prg.hideToTray();
    } else if (e.getActionCommand().equals(optionsText)) {
      prg.showOptions();
    } else if (e.getActionCommand().equals(quitText)) {
      prg.quit();
    } else if (e.getActionCommand().equals(aboutText)) {
      AboutDialog about = new AboutDialog(messages);
      about.show();
    } else {
      throw new IllegalArgumentException("Unknown action " + e);
    }
  }
  
}
