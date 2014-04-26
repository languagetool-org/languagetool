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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Rule;

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
  private JTree configTree;
  
  private final Configuration original;
  private final Configuration config;

  private final Frame owner;
  private final boolean insideOOo;
  
  private JCheckBox serverSettingsCheckbox;

  @Deprecated
  public ConfigurationDialog(Frame owner, boolean insideOOo) {
    this.owner = owner;
    this.insideOOo = insideOOo;
    this.original = null;
    this.config = new Configuration();
    messages = JLanguageTool.getMessageBundle();
  }

  public ConfigurationDialog(Frame owner, boolean insideOOo, Configuration config) {
    this.owner = owner;
    this.insideOOo = insideOOo;
    this.original = config;
    this.config = original.copy(original);
    messages = JLanguageTool.getMessageBundle();
  }

  private DefaultMutableTreeNode createTree(List<Rule> rules) {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Rules");
    String lastRule = null;
    TreeMap<String, DefaultMutableTreeNode> parents = new TreeMap<>();
    for (final Rule rule : rules) {
      if (!parents.containsKey(rule.getCategory().getName())) {

        boolean enabled = true;
        if (config.getDisabledCategoryNames() != null && config.getDisabledCategoryNames().contains(rule.getCategory().getName())) {
          enabled = false;
        }
        DefaultMutableTreeNode categoryNode = new CategoryNode(rule.getCategory(), enabled);
        root.add(categoryNode);
        parents.put(rule.getCategory().getName(), categoryNode);
      }
      if (!rule.getId().equals(lastRule)) {
        RuleNode ruleNode = new RuleNode(rule, getState(rule));
        parents.get(rule.getCategory().getName()).add(ruleNode);
      }
      lastRule = rule.getId();
    }
    return root;
  }

  private boolean getState(Rule rule) {
    boolean ret = true;

    if (config.getDisabledRuleIds().contains(rule.getId())) {
      ret = false;
    }
    if (config.getDisabledCategoryNames().contains(rule.getCategory().getName())) {
      ret = false;
    }
    if (rule.isDefaultOff() && !config.getEnabledRuleIds().contains(rule.getId())) {
      ret = false;
    }

    if (rule.isDefaultOff()) {
      if (rule.getCategory().isDefaultOff()) {
        config.getDisabledCategoryNames().add(rule.getCategory().getName());
      }
    } else {
      if (rule.getCategory().isDefaultOff()) {
        config.getDisabledCategoryNames().remove(rule.getCategory().getName());
      }
    }
    return ret;
  }

  public void show(List<Rule> rules) {
    if(original != null)
      config.restoreState(original);
    dialog = new JDialog(owner, true);
    dialog.setTitle(messages.getString("guiConfigWindowTitle"));

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
    cons.weightx = 1.0;
    cons.weighty = 1.0;
    cons.fill = GridBagConstraints.BOTH;
    DefaultMutableTreeNode rootNode = createTree(rules);
    DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
    treeModel.addTreeModelListener(new TreeModelListener() {

      @Override
      public void treeNodesChanged(TreeModelEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getTreePath().getLastPathComponent();
        int index = e.getChildIndices()[0];
        node = (DefaultMutableTreeNode) (node.getChildAt(index));
        if (node instanceof RuleNode) {
          RuleNode o = (RuleNode) node;
          if (o.getRule().isDefaultOff()) {
            if (o.isEnabled()) {
              config.getEnabledRuleIds().add(o.getRule().getId());
            } else {
              config.getEnabledRuleIds().remove(o.getRule().getId());
            }
          } else {
            if (o.isEnabled()) {
              config.getDisabledRuleIds().remove(o.getRule().getId());
            } else {
              config.getDisabledRuleIds().add(o.getRule().getId());
            }
          }
        }
        if (node instanceof CategoryNode) {
          CategoryNode o = (CategoryNode) node;
          if (o.isEnabled()) {
            config.getDisabledCategoryNames().remove(o.getCategory().getName());
          } else {
            config.getDisabledCategoryNames().add(o.getCategory().getName());
          }
        }
      }

      @Override
      public void treeNodesInserted(TreeModelEvent e) {
      }

      @Override
      public void treeNodesRemoved(TreeModelEvent e) {
      }

      @Override
      public void treeStructureChanged(TreeModelEvent e) {
      }
    });
    configTree = new JTree(treeModel);
    configTree.setRootVisible(false);
    configTree.setEditable(false);
    configTree.setCellRenderer(new CheckBoxTreeCellRenderer());
    TreeListener.install(configTree);
    checkBoxPanel.add(configTree, cons);

    final JPanel treeButtonPanel = new JPanel();
    cons = new GridBagConstraints();
    cons.gridx = 0;
    cons.gridy = 0;
    final JButton expandAllButton = new JButton(messages.getString("guiExpandAll"));
    treeButtonPanel.add(expandAllButton, cons);
    expandAllButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        TreeNode root = (TreeNode) configTree.getModel().getRoot();
        TreePath parent = new TreePath(root);
        for (Enumeration categ = root.children(); categ.hasMoreElements();) {
          TreeNode n = (TreeNode) categ.nextElement();
          TreePath child = parent.pathByAddingChild(n);
          configTree.expandPath(child);
        }
      }
    });

    cons.gridx = 1;
    cons.gridy = 0;
    final JButton collapseAllbutton = new JButton(messages.getString("guiCollapseAll"));
    treeButtonPanel.add(collapseAllbutton, cons);
    collapseAllbutton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        TreeNode root = (TreeNode) configTree.getModel().getRoot();
        TreePath parent = new TreePath(root);
        for (Enumeration categ = root.children(); categ.hasMoreElements();) {
          TreeNode n = (TreeNode) categ.nextElement();
          TreePath child = parent.pathByAddingChild(n);
          configTree.collapsePath(child);
        }
      }
    });

    final JPanel motherTonguePanel = new JPanel();
    motherTonguePanel.add(new JLabel(messages.getString("guiMotherTongue")), cons);
    motherTongueBox = new JComboBox(getPossibleMotherTongues());
    if (config.getMotherTongue() != null) {
      motherTongueBox.setSelectedItem(config.getMotherTongue().getTranslatedName(messages));
    }
    motherTongueBox.addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          Language motherTongue;
          if (motherTongueBox.getSelectedItem() instanceof String) {
            motherTongue = getLanguageForLocalizedName(motherTongueBox.getSelectedItem().toString());
          } else {
            motherTongue = (Language) motherTongueBox.getSelectedItem();
          }
          config.setMotherTongue(motherTongue);
        }
      }
    });
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
      serverCheckbox = new JCheckBox(Tools.getLabel(messages.getString("guiRunOnPort")));
      serverCheckbox.setMnemonic(Tools.getMnemonic(messages.getString("guiRunOnPort")));
      serverCheckbox.setSelected(config.getRunServer());
      portPanel.add(serverCheckbox, cons);
      serverCheckbox.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
          serverPortField.setEnabled(serverCheckbox.isSelected());
          serverSettingsCheckbox.setEnabled(serverCheckbox.isSelected());
        }
      });
      serverCheckbox.addItemListener(new ItemListener() {

        @Override
        public void itemStateChanged(ItemEvent e) {
          config.setRunServer(serverCheckbox.isSelected());
        }
      });

      serverPortField = new JTextField(Integer.toString(config.getServerPort()));
      serverPortField.setEnabled(serverCheckbox.isSelected());
      serverSettingsCheckbox = new JCheckBox(Tools.getLabel(messages.getString("useGUIConfig")));
      // TODO: without this the box is just a few pixels small, but why??:
      serverPortField.setMinimumSize(new Dimension(100, 25));
      cons.gridx = 1;
      portPanel.add(serverPortField, cons);
      serverPortField.getDocument().addDocumentListener(new DocumentListener() {

        @Override
        public void insertUpdate(DocumentEvent e) {
          changedUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
          changedUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
          try {
            int serverPort = Integer.parseInt(serverPortField.getText());
            if (serverPort > -1 && serverPort < 65536) {
              serverPortField.setForeground(null);
              config.setServerPort(serverPort);
            } else {
              serverPortField.setForeground(Color.RED);
            }
          } catch (NumberFormatException ex) {
            serverPortField.setForeground(Color.RED);
          }
        }
      });

      cons.gridx = 0;
      cons.gridy = 10;      
      serverSettingsCheckbox.setMnemonic(Tools.getMnemonic(messages
          .getString("useGUIConfig")));
      serverSettingsCheckbox.setSelected(config.getUseGUIConfig());
      serverSettingsCheckbox.setEnabled(config.getRunServer());
      serverSettingsCheckbox.addItemListener(new ItemListener() {

        @Override
        public void itemStateChanged(ItemEvent e) {
          config.setUseGUIConfig(serverSettingsCheckbox.isSelected());
        }
      });
      portPanel.add(serverSettingsCheckbox, cons);
    }

    final JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridBagLayout());
    okButton = new JButton(Tools.getLabel(messages.getString("guiOKButton")));
    okButton.setMnemonic(Tools.getMnemonic(messages.getString("guiOKButton")));
    okButton.addActionListener(this);
    cancelButton = new JButton(Tools.getLabel(messages.getString("guiCancelButton")));
    cancelButton.setMnemonic(Tools.getMnemonic(messages.getString("guiCancelButton")));
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
    cons.anchor = GridBagConstraints.LINE_END;
    contentPane.add(treeButtonPanel, cons);
    
    cons.gridx = 0;
    cons.gridy = 2;
    cons.weightx = 0.0f;
    cons.weighty = 0.0f;
    cons.fill = GridBagConstraints.NONE;
    cons.anchor = GridBagConstraints.WEST;
    contentPane.add(motherTonguePanel, cons);

    cons.gridx = 0;
    cons.gridy = 3;
    cons.weightx = 0.0f;
    cons.weighty = 0.0f;
    cons.fill = GridBagConstraints.NONE;
    cons.anchor = GridBagConstraints.WEST;
    contentPane.add(portPanel, cons);

    cons.gridx = 0;
    cons.gridy = 4;
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
    dialog.setLocationByPlatform(true);
    dialog.setVisible(true);
  }

  private Object[] getPossibleMotherTongues() {
    final List<Object> motherTongues = new ArrayList<>();
    motherTongues.add(NO_MOTHER_TONGUE);
    for (final Language lang : Language.REAL_LANGUAGES) {
     motherTongues.add(lang.getTranslatedName(messages));
    }
    return motherTongues.toArray();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == okButton) {
      if(original != null) {
        original.restoreState(config);
      }
      dialog.setVisible(false);
    } else if (e.getSource() == cancelButton) {
      dialog.setVisible(false);
    }
  }

  @Deprecated
  public void setDisabledRules(Set<String> ruleIDs) {
    config.setDisabledRuleIds(ruleIDs);
  }

  @Deprecated
  public Set<String> getDisabledRuleIds() {
    return config.getDisabledRuleIds();
  }

  @Deprecated
  public void setEnabledRules(Set<String> ruleIDs) {
    config.setEnabledRuleIds(ruleIDs);
  }

  @Deprecated
  public Set<String> getEnabledRuleIds() {
    return config.getEnabledRuleIds();
  }

  @Deprecated
  public void setDisabledCategories(Set<String> categoryNames) {
    config.setDisabledCategoryNames(categoryNames);
  }

  @Deprecated
  public Set<String> getDisabledCategoryNames() {
    return config.getDisabledCategoryNames();
  }

  @Deprecated
  public void setMotherTongue(Language motherTongue) {
    config.setMotherTongue(motherTongue);
  }

  @Deprecated
  public Language getMotherTongue() {
    return config.getMotherTongue();
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
    for (final Language element : Language.REAL_LANGUAGES) {
      if (languageName.equals(element.getTranslatedName(messages))) {
        return element;
      }
    }
    return null;
  }

  @Deprecated
  public void setRunServer(boolean serverMode) {
    config.setRunServer(serverMode);
  }

  @Deprecated
  public void setUseGUIConfig(boolean useGUIConfig) {
    config.setUseGUIConfig(useGUIConfig);
  }

  @Deprecated
  public boolean getUseGUIConfig() {
    return config.getUseGUIConfig();
  }

  @Deprecated
  public boolean getRunServer() {
    return config.getRunServer();
  }

  @Deprecated
  public void setServerPort(int serverPort) {
    config.setServerPort(serverPort);
  }

  @Deprecated
  public int getServerPort() {
    return config.getServerPort();
  }

  static class CategoryComparator implements Comparator<Rule> {

    @Override
    public int compare(final Rule r1, final Rule r2) {
      final boolean hasCat = r1.getCategory() != null && r2.getCategory() != null;
      if (hasCat) {
        final int res = r1.getCategory().getName().compareTo(r2.getCategory().getName());
        if (res == 0) {
          return r1.getDescription().compareToIgnoreCase(r2.getDescription());
        }
        return res;
      }
      return r1.getDescription().compareToIgnoreCase(r2.getDescription());
    }

  }

}
