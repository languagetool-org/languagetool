/* JLanguageTool, a natural language style checker 
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

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.patterns.PatternRule;

/**
 * Dialog that offers the available rules so they can be turned on/off
 * individually.
 * 
 * @author Daniel Naber
 */
class ConfigurationDialog  implements ActionListener {

  private static final String OK_BUTTON = "OK";
  private static final String CANCEL_BUTTON = "Cancel";
  private JDialog dialog = null;
  
  private List checkBoxes = new ArrayList();
  private List checkBoxesRuleIds = new ArrayList();
  private Set inactivateRuleIds = new HashSet();

  ConfigurationDialog() {
  }
  
  void show(List rules) {
    dialog = new JDialog();
    dialog.setTitle("Options");
    checkBoxes.clear();
    checkBoxesRuleIds.clear();
    
    JPanel checkBoxPanel = new JPanel();
    checkBoxPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons = new GridBagConstraints();
    cons.anchor = GridBagConstraints.NORTHWEST;
    cons.gridx = 0;
    int row = 0;
    for (Iterator iter = rules.iterator(); iter.hasNext();) {
      Rule rule = (Rule) iter.next();
      cons.gridy = row;
      
      JCheckBox checkBox = null;
      if (rule instanceof PatternRule) {
        PatternRule patternRule = (PatternRule) rule;
        checkBox = new JCheckBox(rule.getDescription() + " -- " + patternRule.getPattern());
      } else {
        checkBox = new JCheckBox(rule.getDescription());
      }
      if (inactivateRuleIds != null && inactivateRuleIds.contains(rule.getId()))
        checkBox.setSelected(false);
      else
        checkBox.setSelected(true);
      checkBoxes.add(checkBox);
      checkBoxesRuleIds.add(rule.getId());
      checkBoxPanel.add(checkBox, cons);
      row++;
    }
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridBagLayout());
    JButton okButton = new JButton(OK_BUTTON);
    okButton.addActionListener(this);
    JButton cancelButton = new JButton(CANCEL_BUTTON);
    cancelButton.addActionListener(this);
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    
    Container contentPane = dialog.getContentPane();
    contentPane.setLayout(new GridBagLayout());
    cons.gridx = 0;
    cons.gridy = 0;
    cons.weightx = 10.0f;
    cons.weighty = 10.0f;
    cons.fill = GridBagConstraints.BOTH;
    contentPane.add(new JScrollPane(checkBoxPanel), cons);
    cons.gridx = 0;
    cons.gridy = 1;
    cons.weightx = 0.0f;
    cons.weighty = 0.0f;
    cons.fill = GridBagConstraints.NONE;
    contentPane.add(buttonPanel, cons);
    
    dialog.pack();
    dialog.setSize(500, 500);
    dialog.setVisible(true);
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals(OK_BUTTON)) {
      int i = 0;
      inactivateRuleIds.clear();
      for (Iterator iter = checkBoxes.iterator(); iter.hasNext();) {
        JCheckBox checkBox = (JCheckBox) iter.next();
        if (!checkBox.isSelected()) {
          String ruleId = (String)checkBoxesRuleIds.get(i);
          inactivateRuleIds.add(ruleId);
        }
        i++;
      }
      dialog.hide(); 
    } else if (e.getActionCommand().equals(CANCEL_BUTTON)) {
      dialog.hide(); 
    }
  }
  
  Set getdisabledRuleIds() {
    return inactivateRuleIds;
  }
  
}
