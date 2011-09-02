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

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

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
  private String showRulesText;
  private String showCoveringRulesText;
  private String showOriginalFileText;

  // Edit:
  private String saveRuleText;
  private String removeCoveringRulesText;
  private String removeWarningsText;
  private String cutText;
  private String copyText;
  private String pasteText;
  
  // Navigate:
  private String nextRuleText;
  private String prevRuleText;
  
  // Help:
  private String aboutText;
  
  //secret
  private String allRulesExclusiveText;

  private final Main prg;
  private JMenu fileMenu;
  private JMenu editMenu;
  private JMenu navigateMenu;
  private JMenu helpMenu;
  
  MainMenuBar(Main prg) {
    this.prg = prg;
    initStrings();
    fileMenu.setMnemonic(0);
    editMenu.setMnemonic(0);
    navigateMenu.setMnemonic(0);
    helpMenu.setMnemonic(0);  
    
    // file menu
    
    // "Open":
    final JMenuItem openItem = new JMenuItem(openText);
    openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
    openItem.setMnemonic(0);
    openItem.addActionListener(this);
    fileMenu.add(openItem);
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
    // "Show covering rules"
    final JMenuItem showCoveringRulesItem = new JMenuItem(showCoveringRulesText);
    showCoveringRulesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,Event.CTRL_MASK));
    showCoveringRulesItem.addActionListener(this);
    fileMenu.add(showCoveringRulesItem);
    // "Show original rule file"
    final JMenuItem showOriginalFileItem = new JMenuItem(showOriginalFileText);
    showOriginalFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,Event.CTRL_MASK));
    showOriginalFileItem.addActionListener(this);
    fileMenu.add(showOriginalFileItem);
    // "Quit":
    final JMenuItem quitItem = new JMenuItem(quitText);
    quitItem.setMnemonic(0);        
    quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK));
    quitItem.addActionListener(this);
    fileMenu.add(quitItem);
    
    // edit menu
    
    // "Save current rule"
    final JMenuItem saveRuleItem = new JMenuItem(saveRuleText);
    saveRuleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
    saveRuleItem.addActionListener(this);
    editMenu.add(saveRuleItem);
    // "Remove covering rules"
    final JMenuItem removeCoveringRulesItem = new JMenuItem(removeCoveringRulesText);
    removeCoveringRulesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK));
    removeCoveringRulesItem.addActionListener(this);
    editMenu.add(removeCoveringRulesItem);
    // "Remove warnings"
    final JMenuItem removeWarningsItem = new JMenuItem(removeWarningsText);
    removeWarningsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, Event.CTRL_MASK));
    removeWarningsItem.addActionListener(this);
    editMenu.add(removeWarningsItem);
    // "Make all rules exclusive"
    final JMenuItem allRulesExclusiveItem = new JMenuItem(allRulesExclusiveText);
    allRulesExclusiveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK|Event.ALT_MASK|Event.SHIFT_MASK));
    allRulesExclusiveItem.addActionListener(this);
    editMenu.add(allRulesExclusiveItem);
    // "Cut"
    final JMenuItem cutItem = new JMenuItem(cutText);
    cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));
    cutItem.addActionListener(this);
    editMenu.add(cutItem);
    // "Copy"
    final JMenuItem copyItem = new JMenuItem(copyText);
    copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));
    copyItem.addActionListener(this);
    editMenu.add(copyItem);
    // "Paste" 
    final JMenuItem pasteItem = new JMenuItem(pasteText);
    pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK));
    pasteItem.addActionListener(this);
    editMenu.add(pasteItem);
    
    // navigate menu
    
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
    
    // help menu
    
    // "About":
    final JMenuItem aboutItem = new JMenuItem(aboutText);
    aboutItem.addActionListener(this);
    aboutItem.setMnemonic(0);        
    helpMenu.add(aboutItem); 
    
    // add menus:
    add(fileMenu);
    add(editMenu);
    add(navigateMenu);
    add(helpMenu);
  }

  private void initStrings() {
    fileMenu = new JMenu("File");
    editMenu = new JMenu("Edit");
    navigateMenu = new JMenu("Navigate");
    helpMenu = new JMenu("Help");
    // File:
    openText = "Open";
    writeText = "Write rules to file";
    showRulesText = "Show all rules";
    showCoveringRulesText = "Show covering rules";
    showOriginalFileText = "Show original rule file";
    quitText = "Quit";
    // Edit:
    saveRuleText = "Save current rule";
    removeCoveringRulesText = "Remove covering rules";
    removeWarningsText = "Remove warnings";
    allRulesExclusiveText = "All rules exclusive";
    cutText = "Cut";
    copyText = "Copy";
    pasteText = "Paste";
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
    } else if (e.getActionCommand().equals(nextRuleText)) {
    	prg.nextRule();
    } else if (e.getActionCommand().equals(prevRuleText)) {
    	prg.prevRule();
    } else if (e.getActionCommand().equals(saveRuleText)) {
    	prg.clickSaveButton();
    } else if (e.getActionCommand().equals(showRulesText)) {
    	prg.showAllRules();
    } else if (e.getActionCommand().equals(showCoveringRulesText)) {
    	prg.displayCoveringRules();
    } else if (e.getActionCommand().equals(aboutText)) {
    	prg.displayAboutDialog();
    } else if (e.getActionCommand().equals(cutText)) {
    	prg.cutSelectedText();
    } else if (e.getActionCommand().equals(copyText)) {
    	prg.copySelectedText();
    } else if (e.getActionCommand().equals(pasteText)) {
    	prg.pasteText();
    } else if (e.getActionCommand().equals(removeCoveringRulesText)) {
    	prg.removeCoveringRules();
    } else if (e.getActionCommand().equals(removeWarningsText)) {
    	prg.removeWarnings();
    } else if (e.getActionCommand().equals(showOriginalFileText)) {
    	prg.showOriginalRuleFile();
    } else if (e.getActionCommand().equals(allRulesExclusiveText)) {
    	prg.makeAllRulesExclusive();
    }
    else {
      throw new IllegalArgumentException("Unknown action " + e);
    }
  }
  
}
