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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import de.danielnaber.languagetool.JLanguageTool;

class MainMenuBar extends JMenuBar implements ActionListener {

  // File:
  private static final String DOCK_TO_TRAY = "Hide to System Tray";
  private static final String OPTIONS = "Options...";
  private static final String QUIT = "Quit";
  // Help:
  private static final String ABOUT = "About...";

  private Main prg = null;
  private JMenu fileMenu = new JMenu("File");
  private JMenu helpMenu = new JMenu("Help");
  
  MainMenuBar(Main prg) {
    this.prg = prg;
    // "Hide to System Tray":
    JMenuItem dockToTrayItem = new JMenuItem(DOCK_TO_TRAY);
    dockToTrayItem.addActionListener(this);
    fileMenu.add(dockToTrayItem);
    // "Options":
    JMenuItem optionsItem = new JMenuItem(OPTIONS);
    optionsItem.addActionListener(this);
    fileMenu.add(optionsItem);
    // "Quit":
    JMenuItem quitItem = new JMenuItem(QUIT);
    quitItem.addActionListener(this);
    fileMenu.add(quitItem);
    // "About":
    JMenuItem helpItem = new JMenuItem(ABOUT);
    helpItem.addActionListener(this);
    helpMenu.add(helpItem);
    // add menus:
    add(fileMenu);
    add(helpMenu);
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals(DOCK_TO_TRAY)) {
      prg.hideToTray();
    } else if (e.getActionCommand().equals(OPTIONS)) {
      prg.showOptions();
    } else if (e.getActionCommand().equals(QUIT)) {
      prg.quit();
    } else if (e.getActionCommand().equals(ABOUT)) {
      JOptionPane.showMessageDialog(null, "LanguageTool " + JLanguageTool.VERSION + "\n" + 
          "Copyright (C) 2005-2006 Daniel Naber\n"+
          "This software is licensed under the GNU Lesser General Public License.\n"+
          "LanguageTool Homepage: http://www.danielnaber.de/languagetool");
    }
  }
  
}
