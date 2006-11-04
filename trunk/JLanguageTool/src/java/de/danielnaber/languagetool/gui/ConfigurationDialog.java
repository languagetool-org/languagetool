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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
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
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Rule;

/**
 * Dialog that offers the available rules so they can be turned on/off
 * individually.
 * 
 * @author Daniel Naber
 */
public class ConfigurationDialog implements ActionListener {

  private static final String OK_BUTTON = "    OK    ";
  private static final String CANCEL_BUTTON = "  Cancel  ";
  private static final String NO_MOTHER_TONGUE = "---";
  private JDialog dialog = null;
  
  private JComboBox motherTongueBox;

  private JCheckBox serverCheckbox;
  private JTextField serverPortField;

  private List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
  private List<String> checkBoxesRuleIds = new ArrayList<String>();
  private Set<String> inactiveRuleIds = new HashSet<String>();
  private Language motherTongue;
  private boolean serverMode = false;
  private int serverPort;
  
  private boolean modal;
  private boolean isClosed = true;
  
  public ConfigurationDialog(boolean modal) {
    this.modal = modal;
  }
  
  public void show(List<Rule> rules) {
    dialog = new JDialog();
    // TODO: i18n:
    dialog.setTitle("LanguageTool " +JLanguageTool.VERSION+ " Options");
    checkBoxes.clear();
    checkBoxesRuleIds.clear();
    
    Collections.sort(rules, new CategoryComparator());
    
    // close dialog when user presses Escape key:
    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    ActionListener actionListener = new ActionListener() {
      @SuppressWarnings("unused")
      public void actionPerformed(ActionEvent actionEvent) {
        isClosed = true;
        dialog.setVisible(false); 
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
    String prevID = null;
    String prevCategory = null;
    for (Rule rule : rules) {
      // avoid displaying rules from rule groups more than once:
      if (prevID == null || (prevID != null && !prevID.equals(rule.getId()))) {
        cons.gridy = row;
        JCheckBox checkBox = new JCheckBox(rule.getDescription());
        if (inactiveRuleIds != null && inactiveRuleIds.contains(rule.getId()))
          checkBox.setSelected(false);
        else
          checkBox.setSelected(true);
        checkBoxes.add(checkBox);
        checkBoxesRuleIds.add(rule.getId());
        boolean showHeadline = (rule.getCategory() != null && !rule.getCategory().getName().equals(prevCategory));
        if ((showHeadline || prevCategory == null) && rule.getCategory() != null) {
          checkBoxPanel.add(new JLabel(rule.getCategory().getName()), cons);
          prevCategory = rule.getCategory().getName();
          cons.gridy++;
          row++;
        }
        checkBox.setMargin(new Insets(0, 20, 0, 0));    // indent
        checkBoxPanel.add(checkBox, cons);
        row++;
      }
      prevID = rule.getId();
    }

    JPanel motherTonguePanel = new JPanel();
    motherTonguePanel.add(new JLabel("Your mother tongue: "), cons);
    motherTongueBox = new JComboBox(getPossibleMotherTongues());
    if (motherTongue != null)
      motherTongueBox.setSelectedItem(motherTongue);
    motherTonguePanel.add(motherTongueBox, cons);

    JPanel portPanel = new JPanel();
    portPanel.setLayout(new GridBagLayout());
    // TODO: why is this now left-aligned?!?!
    serverCheckbox = new JCheckBox("Run as server on port");
    serverCheckbox.setMnemonic('r');
    serverCheckbox.setSelected(serverMode);
    cons = new GridBagConstraints();
    cons.insets = new Insets(0, 4, 0, 0);
    cons.gridx = 0;
    cons.gridy = 0;
    cons.anchor = GridBagConstraints.WEST;
    cons.fill = GridBagConstraints.NONE;
    cons.weightx = 0.0f;
    portPanel.add(serverCheckbox, cons);
    serverPortField = new JTextField(serverPort + "");
    serverPortField.setEnabled(serverCheckbox.isSelected());
    // TODO: without this the box is just a few pixels small, but why??:
    serverPortField.setMinimumSize(new Dimension(200, 15));
    cons.gridx = 1;
    serverCheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(@SuppressWarnings("unused")ActionEvent e) {
        serverPortField.setEnabled(serverCheckbox.isSelected());
      }});
    portPanel.add(serverPortField, cons);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridBagLayout());
    JButton okButton = new JButton(OK_BUTTON);
    okButton.setMnemonic('o');
    okButton.addActionListener(this);
    JButton cancelButton = new JButton(CANCEL_BUTTON);
    cancelButton.setMnemonic('c');
    cancelButton.addActionListener(this);
    cons = new GridBagConstraints();
    cons.insets = new Insets(0, 4, 0, 0);
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
    cons.anchor = GridBagConstraints.WEST;
    contentPane.add(motherTonguePanel, cons);

    cons.gridx = 0;
    cons.gridy = 2;
    cons.weightx = 0.0f;
    cons.weighty = 0.0f;
    cons.fill = GridBagConstraints.NONE;
    cons.anchor = GridBagConstraints.WEST;
    contentPane.add(portPanel, cons);

    cons.gridx = 0;
    cons.gridy = 3;
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
    List<Object> motherTongues = new ArrayList<Object>();
    motherTongues.add(NO_MOTHER_TONGUE);
    for (Language lang : Language.LANGUAGES) {
      if (lang != Language.DEMO) {
        motherTongues.add(lang);
      }
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
      for (JCheckBox checkBox : checkBoxes) {
        if (!checkBox.isSelected()) {
          String ruleId = checkBoxesRuleIds.get(i);
          inactiveRuleIds.add(ruleId);
        }
        i++;
      }
      if (motherTongueBox.getSelectedItem() instanceof String)
        motherTongue = null;
      else
        motherTongue = (Language)motherTongueBox.getSelectedItem();
      serverMode = serverCheckbox.isSelected();
      serverPort = Integer.parseInt(serverPortField.getText());
      isClosed = true;
      dialog.setVisible(false);
    } else if (e.getActionCommand().equals(CANCEL_BUTTON)) {
      isClosed = true;
      dialog.setVisible(false);
    }
  }
  
  public void setDisabledRules(Set<String> ruleIDs) {
    inactiveRuleIds = ruleIDs;
  }

  public Set<String> getDisabledRuleIds() {
    return inactiveRuleIds;
  }

  public void setMotherTongue(Language motherTongue) {
    this.motherTongue = motherTongue;
  }

  public Language getMotherTongue() {
    return motherTongue;
  }

  public void setRunServer(boolean serverMode) {
    this.serverMode = serverMode;
  }

  public boolean getRunServer() {
    return serverCheckbox.isSelected();
  }

  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  public int getServerPort() {
    return Integer.parseInt(serverPortField.getText());
  }

  /**
   * For internal testing only.
   */
  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    ConfigurationDialog dlg = new ConfigurationDialog(false);
    List<Rule> rules = new ArrayList<Rule>();
    JLanguageTool lt = new JLanguageTool(Language.ENGLISH);
    lt.activateDefaultPatternRules();
    rules.addAll(lt.getAllRules());
    dlg.show(rules);
  }
  
}

class CategoryComparator implements Comparator<Rule> {

  public int compare(Rule r1, Rule r2) {
    boolean hasCat = r1.getCategory() != null && r2.getCategory() != null;
    if (hasCat) {
      int res = r1.getCategory().getName().compareTo(r2.getCategory().getName());
      if (res == 0) {
        return r1.getDescription().compareToIgnoreCase(r2.getDescription());  
      } else {
        return res;
      }
    } else {
      return r1.getDescription().compareToIgnoreCase(r2.getDescription());
    }
  }

}
