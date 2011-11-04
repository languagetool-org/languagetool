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
import java.awt.Frame;
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
import java.util.ResourceBundle;
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

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.server.HTTPServer;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * Dialog that offers the available rules so they can be turned on/off
 * individually.
 * 
 * @author Daniel Naber
 */
public class ConfigurationDialog implements ActionListener {

  private static final String NO_MOTHER_TONGUE = "---";

  private JButton okButton;
  private JButton cancelButton;

  private final ResourceBundle messages;
  private JDialog dialog;

  private JComboBox motherTongueBox;
  
  private JCheckBox serverCheckbox;
  private JTextField serverPortField;

  private final List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
  private final List<String> checkBoxesRuleIds = new ArrayList<String>();
  private final List<String> checkBoxesCategories = new ArrayList<String>();

  private final List<String> defaultOffRules = new ArrayList<String>();

  private Set<String> inactiveRuleIds = new HashSet<String>();
  private Set<String> enabledRuleIds = new HashSet<String>();
  private Set<String> inactiveCategoryNames = new HashSet<String>();
  private final List<JCheckBox> categoryCheckBoxes = new ArrayList<JCheckBox>();
  private final List<String> checkBoxesCategoryNames = new ArrayList<String>();
  private Language motherTongue;
  private boolean serverMode;
  private int serverPort;

  private final Frame owner;
  private final boolean insideOOo;

  public ConfigurationDialog(Frame owner, boolean insideOOo) {
    this.owner = owner;
    this.insideOOo = insideOOo;
    messages = JLanguageTool.getMessageBundle();
  }

  public void show(List<Rule> rules) {
    dialog = new JDialog(owner, true);
    dialog.setTitle(messages.getString("guiConfigWindowTitle"));
    checkBoxes.clear();
    checkBoxesRuleIds.clear();
    categoryCheckBoxes.clear();
    checkBoxesCategoryNames.clear();

    Collections.sort(rules, new CategoryComparator());

    // close dialog when user presses Escape key:
    final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    final ActionListener actionListener = new ActionListener() {
      @Override
      public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent) {
        dialog.setVisible(false);
      }
    };
    final JRootPane rootPane = dialog.getRootPane();
    rootPane.registerKeyboardAction(actionListener, stroke,
        JComponent.WHEN_IN_FOCUSED_WINDOW);

    // JPanel
    final JPanel checkBoxPanel = new JPanel();
    checkBoxPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons = new GridBagConstraints();
    cons.anchor = GridBagConstraints.NORTHWEST;
    cons.gridx = 0;
    int row = 0;
    String prevID = null;
    String prevCategory = null;
    for (final Rule rule : rules) {
      // avoid displaying rules from rule groups more than once:
      if (prevID == null || !rule.getId().equals(prevID)) {
        cons.gridy = row;
        final JCheckBox checkBox = new JCheckBox(rule.getDescription());
        if (inactiveRuleIds != null
            && (inactiveRuleIds.contains(rule.getId()) || inactiveCategoryNames
                .contains(rule.getCategory().getName()))) {
          checkBox.setSelected(false);
        } else {
          checkBox.setSelected(true);
        }

        if (rule.isDefaultOff() && !enabledRuleIds.contains(rule.getId())) {
          checkBox.setSelected(false);
        }

        if (rule.isDefaultOff()) {
          defaultOffRules.add(rule.getId());
          if (rule.getCategory().isDefaultOff()) {
            inactiveCategoryNames.add(rule.getCategory().getName());
          }
        } else {
          if (rule.getCategory().isDefaultOff()) {
            inactiveCategoryNames.remove(rule.getCategory().getName());
          }
        }

        final ActionListener ruleCheckBoxListener = makeRuleCheckboxListener();
        checkBox.addActionListener(ruleCheckBoxListener);
        checkBoxes.add(checkBox);
        checkBoxesRuleIds.add(rule.getId());
        checkBoxesCategories.add(rule.getCategory().getName());
        final boolean showHeadline = rule.getCategory() != null
            && !rule.getCategory().getName().equals(prevCategory);
        if ((showHeadline || prevCategory == null)
            && rule.getCategory() != null) {

          // TODO: maybe use a Tree of Checkboxes here, like in:
          // http://www.javaworld.com/javaworld/jw-09-2007/jw-09-checkboxtree.html
          final JCheckBox categoryCheckBox = new JCheckBox(rule.getCategory()
              .getName());
          if (inactiveCategoryNames != null
              && inactiveCategoryNames.contains(rule.getCategory().getName())) {
            categoryCheckBox.setSelected(false);
          } else {
            categoryCheckBox.setSelected(true);
          }

          final ActionListener categoryCheckBoxListener = makeCategoryCheckboxListener();
          categoryCheckBox.addActionListener(categoryCheckBoxListener);
          categoryCheckBoxes.add(categoryCheckBox);
          checkBoxesCategoryNames.add(rule.getCategory().getName());
          checkBoxPanel.add(categoryCheckBox, cons);
          prevCategory = rule.getCategory().getName();
          cons.gridy++;
          row++;
        }
        checkBox.setMargin(new Insets(0, 20, 0, 0)); // indent
        checkBoxPanel.add(checkBox, cons);
        row++;
      }
      prevID = rule.getId();
    }

    final JPanel motherTonguePanel = new JPanel();
    motherTonguePanel.add(new JLabel(messages.getString("guiMotherTongue")), cons);
    motherTongueBox = new JComboBox(getPossibleMotherTongues());
    if (motherTongue != null) {
      if (motherTongue == Language.DEMO) {
        motherTongueBox.setSelectedItem(NO_MOTHER_TONGUE);
      } else {
        motherTongueBox.setSelectedItem(messages.getString(motherTongue
            .getShortName()));
      }
    }
    motherTonguePanel.add(motherTongueBox, cons);
    
    final JPanel portPanel = new JPanel();
    portPanel.setLayout(new GridBagLayout());
    // TODO: why is this now left-aligned?!?!
    cons = new GridBagConstraints();
    cons.insets = new Insets(0, 4, 0, 0);
    cons.gridx = 0;
    cons.gridy = 0;
    cons.anchor = GridBagConstraints.WEST;
    cons.fill = GridBagConstraints.NONE;
    cons.weightx = 0.0f;
    if (!insideOOo) {
      serverCheckbox = new JCheckBox(StringTools.getLabel(messages
          .getString("guiRunOnPort")));
      serverCheckbox.setMnemonic(StringTools.getMnemonic(messages
          .getString("guiRunOnPort")));
      serverCheckbox.setSelected(serverMode);
      portPanel.add(serverCheckbox, cons);
      serverPortField = new JTextField(Integer.toString(serverPort));
      serverPortField.setEnabled(serverCheckbox.isSelected());
      // TODO: without this the box is just a few pixels small, but why??:
      serverPortField.setMinimumSize(new Dimension(100, 25));
      cons.gridx = 1;
      serverCheckbox.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
          serverPortField.setEnabled(serverCheckbox.isSelected());
        }
      });
      portPanel.add(serverPortField, cons);
    }

    final JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridBagLayout());
    okButton = new JButton(StringTools.getLabel(messages
        .getString("guiOKButton")));
    okButton.setMnemonic(StringTools.getMnemonic(messages
        .getString("guiOKButton")));
    okButton.addActionListener(this);
    cancelButton = new JButton(StringTools.getLabel(messages
        .getString("guiCancelButton")));
    cancelButton.setMnemonic(StringTools.getMnemonic(messages
        .getString("guiCancelButton")));
    cancelButton.addActionListener(this);
    cons = new GridBagConstraints();
    cons.insets = new Insets(0, 4, 0, 0);
    buttonPanel.add(okButton, cons);
    buttonPanel.add(cancelButton, cons);

    final Container contentPane = dialog.getContentPane();
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
    dialog.setSize(500, 500);
    // center on screen:
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final Dimension frameSize = dialog.getSize();
    dialog.setLocation(screenSize.width / 2 - frameSize.width / 2,
        screenSize.height / 2 - frameSize.height / 2);
    dialog.setVisible(true);
  }

  private ActionListener makeRuleCheckboxListener() {
    return new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent actionEvent) {
        final JCheckBox cBox = (JCheckBox) actionEvent.getSource();
        final boolean selected = cBox.getModel().isSelected();
        int i = 0;
        for (final JCheckBox chBox : checkBoxes) {
          if (chBox.equals(cBox)) {
            final int catNo = checkBoxesCategoryNames
                    .indexOf(checkBoxesCategories.get(i));
            if (selected && !categoryCheckBoxes.get(catNo).isSelected()) {
              categoryCheckBoxes.get(catNo).setSelected(true);
            }
          }
          i++;
        }
      }
    };
  }

  private ActionListener makeCategoryCheckboxListener() {
    return new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent actionEvent) {
        final JCheckBox cBox = (JCheckBox) actionEvent.getSource();
        final boolean selected = cBox.getModel().isSelected();
        int i = 0;
        for (final JCheckBox ruleBox : checkBoxes) {
          if (ruleBox.isSelected() != selected) {
            if (checkBoxesCategories.get(i).equals(cBox.getText())) {
              ruleBox.setSelected(selected);
            }
          }
          i++;
        }
      }
    };
  }

  private Object[] getPossibleMotherTongues() {
    final List<Object> motherTongues = new ArrayList<Object>();
    motherTongues.add(NO_MOTHER_TONGUE);
    for (final Language lang : Language.LANGUAGES) {
      if (lang != Language.DEMO) {
        motherTongues.add(messages.getString(lang.getShortName()));
      }
    }
    return motherTongues.toArray();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == okButton) {
      int i = 0;
      inactiveCategoryNames.clear();
      for (final JCheckBox checkBox : categoryCheckBoxes) {
        if (!checkBox.isSelected()) {
          final String categoryName = checkBoxesCategoryNames.get(i);
          inactiveCategoryNames.add(categoryName);
        }
        i++;
      }
      i = 0;
      inactiveRuleIds.clear();
      enabledRuleIds.clear();
      for (final JCheckBox checkBox : checkBoxes) {
        if (!checkBox.isSelected()) {
          final String ruleId = checkBoxesRuleIds.get(i);
          if (!defaultOffRules.contains(ruleId)) {
            inactiveRuleIds.add(ruleId);
          }
        }

        if (checkBox.isSelected()) {
          final String ruleId = checkBoxesRuleIds.get(i);
          if (defaultOffRules.contains(ruleId)) {
            enabledRuleIds.add(ruleId);
          }
        }

        i++;
      }

      if (motherTongueBox.getSelectedItem() instanceof String) {
        motherTongue = getLanguageForLocalizedName(motherTongueBox
            .getSelectedItem().toString());
      } else {
        motherTongue = (Language) motherTongueBox.getSelectedItem();
      }
      
      if (serverCheckbox != null) {
        serverMode = serverCheckbox.isSelected();
        serverPort = Integer.parseInt(serverPortField.getText());
      }
      dialog.setVisible(false);
    } else if (e.getSource() == cancelButton) {
      dialog.setVisible(false);
    }
  }

  public void setDisabledRules(Set<String> ruleIDs) {
    inactiveRuleIds = ruleIDs;
  }

  public Set<String> getDisabledRuleIds() {
    return inactiveRuleIds;
  }

  public void setEnabledRules(Set<String> ruleIDs) {
    enabledRuleIds = ruleIDs;
  }

  public Set<String> getEnabledRuleIds() {
    return enabledRuleIds;
  }

  public void setDisabledCategories(Set<String> categoryNames) {
    inactiveCategoryNames = categoryNames;
  }

  public Set<String> getDisabledCategoryNames() {
    return inactiveCategoryNames;
  }

  public void setMotherTongue(Language motherTongue) {
    this.motherTongue = motherTongue;
  }

  public Language getMotherTongue() {
    return motherTongue;
  }
  
  /**
   * Get the Language object for the given localized language name.
   * 
   * @param languageName
   *          e.g. <code>English</code> or <code>German</code> (case is
   *          significant)
   * @return a Language object or <code>null</code>
   */
  private Language getLanguageForLocalizedName(final String languageName) {
    for (final Language element : Language.LANGUAGES) {
      if (NO_MOTHER_TONGUE.equals(languageName)) {
        return Language.DEMO;
      }
      if (languageName.equals(messages.getString(element.getShortName()))) {
        return element;
      }
    }
    return null;
  }

  public void setRunServer(boolean serverMode) {
    this.serverMode = serverMode;
  }

  public boolean getRunServer() {
    if (serverCheckbox == null) {
      return false;
    }
    return serverCheckbox.isSelected();
  }

  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  public int getServerPort() {
    if (serverPortField == null) {
      return HTTPServer.DEFAULT_PORT;
    }
    return Integer.parseInt(serverPortField.getText());
  }

  /**
   * Opens the dialog - for internal testing only.
   */
  public static void main(String[] args) throws IOException {
    final ConfigurationDialog dlg = new ConfigurationDialog(null, false);
    final List<Rule> rules = new ArrayList<Rule>();
    final JLanguageTool lt = new JLanguageTool(Language.ENGLISH);
    lt.activateDefaultPatternRules();
    rules.addAll(lt.getAllRules());
    dlg.show(rules);
  }

}

class CategoryComparator implements Comparator<Rule> {

  @Override
  public int compare(final Rule r1, final Rule r2) {
    final boolean hasCat = r1.getCategory() != null && r2.getCategory() != null;
    if (hasCat) {
      final int res = r1.getCategory().getName().compareTo(
          r2.getCategory().getName());
      if (res == 0) {
        return r1.getDescription().compareToIgnoreCase(r2.getDescription());
      }
      return res;
    }
    return r1.getDescription().compareToIgnoreCase(r2.getDescription());
  }

}
