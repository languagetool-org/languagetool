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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.patterns.PatternRule;

/**
 * Dialog that offers the available rules so they can be turned on/off
 * individually.
 * 
 * @author Daniel Naber
 */
public class ConfigurationDialog implements ActionListener {

  private static final String OK_BUTTON = "OK";
  private static final String CANCEL_BUTTON = "Cancel";
  private static final String NO_MOTHER_TONGUE = "---";
  private JDialog dialog = null;
  
  private JComboBox motherTongueBox;

  private List checkBoxes = new ArrayList();
  private List checkBoxesRuleIds = new ArrayList();
  private Set inactiveRuleIds = new HashSet();
  private Language motherTongue;
  private boolean modal;
  private boolean isClosed = true;
  
  public ConfigurationDialog(boolean modal) {
    this.modal = modal;
  }
  
  public void show(List rules) {
    dialog = new JDialog();
    dialog.setTitle("LanguageTool Options");
    checkBoxes.clear();
    checkBoxesRuleIds.clear();
    
    // close dialog when user presses Escape key:
    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent == null) actionEvent = null;    // avoid compiler warning
        isClosed = true;
        dialog.hide(); 
      }
    };
    JRootPane rootPane = dialog.getRootPane();
    rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    
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
      if (inactiveRuleIds != null && inactiveRuleIds.contains(rule.getId()))
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
    motherTongueBox = new JComboBox(getPossibleMotherTongues());
    if (motherTongue != null)
      motherTongueBox.setSelectedItem(motherTongue);
    cons = new GridBagConstraints();
    cons.insets = new Insets(0, 4, 0, 0);
    buttonPanel.add(new JLabel("Your mother tongue: "), cons);
    buttonPanel.add(motherTongueBox, cons);
    buttonPanel.add(okButton, cons);
    buttonPanel.add(cancelButton, cons);
    
    Container contentPane = dialog.getContentPane();
    contentPane.setLayout(new GridBagLayout());
    cons = new GridBagConstraints();
    cons.insets = new Insets(4, 4, 4, 4);
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
    cons.anchor = GridBagConstraints.EAST;
    contentPane.add(buttonPanel, cons);
    
    dialog.pack();
    dialog.setModal(modal);
    dialog.setSize(500, 500);
    isClosed = false;
    // center on screen:
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = dialog.getSize();
    dialog.setLocation(screenSize.width/2 - (frameSize.width/2), screenSize.height/2 - (frameSize.height/2));
    dialog.setVisible(true);
  }
  
  private Object[] getPossibleMotherTongues() {
    List motherTongues = new ArrayList();
    motherTongues.add(NO_MOTHER_TONGUE);
    for (int i = 0; i < Language.LANGUAGES.length; i++) {
      motherTongues.add(Language.LANGUAGES[i]);
    }
    return motherTongues.toArray();
  }

  public boolean isClosed() {
    return isClosed;
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals(OK_BUTTON)) {
      int i = 0;
      inactiveRuleIds.clear();
      for (Iterator iter = checkBoxes.iterator(); iter.hasNext();) {
        JCheckBox checkBox = (JCheckBox) iter.next();
        if (!checkBox.isSelected()) {
          String ruleId = (String)checkBoxesRuleIds.get(i);
          inactiveRuleIds.add(ruleId);
        }
        i++;
      }
      if (motherTongueBox.getSelectedItem() instanceof String)
        motherTongue = null;
      else
        motherTongue = (Language)motherTongueBox.getSelectedItem();
      isClosed = true;
      dialog.hide(); 
    } else if (e.getActionCommand().equals(CANCEL_BUTTON)) {
      isClosed = true;
      dialog.hide(); 
    }
  }
  
  public void setDisabledRules(Set ruleIDs) {
    inactiveRuleIds = ruleIDs;
  }

  public Set getDisabledRuleIds() {
    return inactiveRuleIds;
  }

  public void setMotherTongue(Language motherTongue) {
    this.motherTongue = motherTongue;
  }

  public Language getMotherTongue() {
    if (motherTongueBox == null)
      return null;
    if (motherTongueBox.getSelectedItem() instanceof String)
      return null;
    return (Language)motherTongueBox.getSelectedItem();
  }

}
