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
package de.danielnaber.languagetool.dev.conversion.gui;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import de.danielnaber.languagetool.tools.StringTools;

/**
 * The menu bar of the main dialog.
 * 
 * @author Daniel Naber
 */
class MainMenuBar extends JMenuBar implements ActionListener {

  private static final long serialVersionUID = -7160998682243081767L;

  // File:
  private String openText;
  private String quitText;
  private String writeText;
  private String saveRuleText;
  private String optionsText;
  private String showRulesText;
  
  // Navigate:
  private String nextRuleText;
  private String prevRuleText;
  
  // Help:
  private String aboutText;

  private final Main prg;
  private JMenu fileMenu;
  private JMenu navigateMenu;
  private JMenu helpMenu;
  
  MainMenuBar(Main prg) {
    this.prg = prg;
    initStrings();
    fileMenu.setMnemonic(0);
    helpMenu.setMnemonic(0);        
    // "Open":
    final JMenuItem openItem = new JMenuItem(openText);
    openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
    openItem.setMnemonic(0);
    openItem.addActionListener(this);
    fileMenu.add(openItem);
    // "Save current rule"
    final JMenuItem saveRuleItem = new JMenuItem(saveRuleText);
    saveRuleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
    saveRuleItem.setMnemonic('s');
    saveRuleItem.addActionListener(this);
    fileMenu.add(saveRuleItem);
    // "Write rules to file"
    final JMenuItem writeItem = new JMenuItem(writeText);
    writeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK|Event.SHIFT_MASK));
    writeItem.setMnemonic(0);
    writeItem.addActionListener(this);
    fileMenu.add(writeItem);
    // "Show all rules"
    final JMenuItem showRulesItem = new JMenuItem(showRulesText);
    showRulesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.CTRL_MASK));
    showRulesItem.setMnemonic(0);
    showRulesItem.addActionListener(this);
    fileMenu.add(showRulesItem);
    // "Options"
    final JMenuItem optionsItem = new JMenuItem(optionsText);
    optionsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, Event.CTRL_MASK));
    optionsItem.setMnemonic(0);
    optionsItem.addActionListener(this);
    fileMenu.add(optionsItem);
    // "Quit":
    final JMenuItem quitItem = new JMenuItem(quitText);
    quitItem.setMnemonic(0);        
    quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK));
    quitItem.addActionListener(this);
    fileMenu.add(quitItem);
    // "Next rule":
    final JMenuItem nextRuleItem = new JMenuItem(nextRuleText);
    nextRuleItem.setMnemonic(0);
    nextRuleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.CTRL_MASK));
    nextRuleItem.addActionListener(this);
    navigateMenu.add(nextRuleItem);
    // "Previous rule":
    final JMenuItem prevRuleItem = new JMenuItem(prevRuleText);
    prevRuleItem.setMnemonic(0);
    prevRuleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.CTRL_MASK));
    prevRuleItem.addActionListener(this);
    navigateMenu.add(prevRuleItem);
    // "About":
    final JMenuItem aboutItem = new JMenuItem(aboutText);
    aboutItem.addActionListener(this);
    aboutItem.setMnemonic(0);        
    helpMenu.add(aboutItem);   
    
    // add menus:
    add(fileMenu);
    add(navigateMenu);
    add(helpMenu);
  }

  private void initStrings() {
    fileMenu = new JMenu("File");
    navigateMenu = new JMenu("Navigate");
    helpMenu = new JMenu("Help");
    // File:
    openText = "Open";
    writeText = "Write rules to file";
    saveRuleText = "Save current rule";
    showRulesText = "Show all rules";
    optionsText = "Options";
    quitText = "Quit";
    // Navigate:
    nextRuleText = "Next rule";
    prevRuleText = "Previous rule";
    // Help:
    aboutText = "About";
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals(openText)) {
    	prg.loadFile();
    } else if (e.getActionCommand().equals(quitText)) {
    	prg.quit();
    } else if (e.getActionCommand().equals(writeText)) {
    	try {
    		prg.writeRulesToFile();
    	} catch (IOException ex) {
    		ex.printStackTrace();
    	}
    } else if (e.getActionCommand().equals(optionsText)) {
    	prg.showOptions();
    } else if (e.getActionCommand().equals(nextRuleText)) {
    	prg.nextRule();
    } else if (e.getActionCommand().equals(prevRuleText)) {
    	prg.prevRule();
    } else if (e.getActionCommand().equals(saveRuleText)) {
    	prg.saveEditedVisibleRule();
    } else if (e.getActionCommand().equals(showRulesText)) {
    	prg.showAllRules();
    }
    else {
      throw new IllegalArgumentException("Unknown action " + e);
    }
  }
  
}
