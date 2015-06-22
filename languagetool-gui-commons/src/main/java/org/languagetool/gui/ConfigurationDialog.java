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

import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Dialog that offers the available rules so they can be turned on/off
 * individually.
 * 
 * @author Daniel Naber
 */
public class ConfigurationDialog implements ActionListener {

  private static final String NO_MOTHER_TONGUE = "---";
  private static final int MAX_PORT = 65536;

  private final ResourceBundle messages;
  private final Configuration original;
  private final Configuration config;
  private final Frame owner;
  private final boolean insideOffice;

  private JButton okButton;
  private JButton cancelButton;
  private JDialog dialog;
  private JComboBox<String> motherTongueBox;
  private JCheckBox serverCheckbox;
  private JTextField serverPortField;
  private JTree configTree;
  private JCheckBox serverSettingsCheckbox;

  public ConfigurationDialog(Frame owner, boolean insideOffice, Configuration config) {
    this.owner = owner;
    this.insideOffice = insideOffice;
    this.original = config;
    this.config = original.copy(original);
    messages = JLanguageTool.getMessageBundle();
  }

  private DefaultMutableTreeNode createTree(List<Rule> rules) {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Rules");
    String lastRuleId = null;
    Map<String, DefaultMutableTreeNode> parents = new TreeMap<>();
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
      if (!rule.getId().equals(lastRuleId)) {
        RuleNode ruleNode = new RuleNode(rule, getState(rule));
        parents.get(rule.getCategory().getName()).add(ruleNode);
      }
      lastRuleId = rule.getId();
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
    if (rule.isDefaultOff() && rule.getCategory().isDefaultOff()
            && config.getEnabledRuleIds().contains(rule.getId())) {
      config.getDisabledCategoryNames().remove(rule.getCategory().getName());
    }
    return ret;
  }

  public void show(List<Rule> rules) {
    if (original != null) {
      config.restoreState(original);
    }
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
        node = (DefaultMutableTreeNode) node.getChildAt(index);
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

    Language lang = config.getLanguage();
    if (lang == null) {
      lang = Languages.getLanguageForLocale(Locale.getDefault());
    }
    configTree.applyComponentOrientation(
      ComponentOrientation.getOrientation(lang.getLocale()));

    configTree.setRootVisible(false);
    configTree.setEditable(false);
    configTree.setCellRenderer(new CheckBoxTreeCellRenderer());
    TreeListener.install(configTree);
    checkBoxPanel.add(configTree, cons);

    MouseAdapter ma = new MouseAdapter() {
      private void handlePopupEvent(MouseEvent e) {
        final JTree tree = (JTree) e.getSource();

        TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
          return;
        }

        DefaultMutableTreeNode node
                = (DefaultMutableTreeNode) path.getLastPathComponent();

        TreePath[] paths = tree.getSelectionPaths();

        boolean isSelected = false;
        if (paths != null) {
          for (TreePath selectionPath : paths) {
            if (selectionPath.equals(path)) {
              isSelected = true;
            }
          }
        }
        if (!isSelected) {
          tree.setSelectionPath(path);
        }
        if (node.isLeaf()) {
          JPopupMenu popup = new JPopupMenu();
          final JMenuItem aboutRuleMenuItem = new JMenuItem(messages.getString("guiAboutRuleMenu"));
          aboutRuleMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
              RuleNode node = (RuleNode) tree.getSelectionPath().getLastPathComponent();
              Rule rule = node.getRule();
              Language lang = config.getLanguage();
              if(lang == null) {
                lang = Languages.getLanguageForLocale(Locale.getDefault());
              }
              Tools.showRuleInfoDialog(tree, messages.getString("guiAboutRuleTitle"),
                      rule.getDescription(), rule, messages,
                      lang.getShortNameWithCountryAndVariant());
            }
          });
          popup.add(aboutRuleMenuItem);
          popup.show(tree, e.getX(), e.getY());
        }
      }

      @Override
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          handlePopupEvent(e);
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          handlePopupEvent(e);
        }
      }
    };
    configTree.addMouseListener(ma);
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
        for (Enumeration cat = root.children(); cat.hasMoreElements();) {
          TreeNode n = (TreeNode) cat.nextElement();
          TreePath child = parent.pathByAddingChild(n);
          configTree.expandPath(child);
        }
      }
    });

    cons.gridx = 1;
    cons.gridy = 0;
    final JButton collapseAllButton = new JButton(messages.getString("guiCollapseAll"));
    treeButtonPanel.add(collapseAllButton, cons);
    collapseAllButton.addActionListener(new ActionListener() {

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
    motherTongueBox = new JComboBox<>(getPossibleMotherTongues());
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
    cons = new GridBagConstraints();
    cons.insets = new Insets(0, 4, 0, 0);
    cons.gridx = 0;
    cons.gridy = 0;
    cons.anchor = GridBagConstraints.WEST;
    cons.fill = GridBagConstraints.NONE;
    cons.weightx = 0.0f;
    if (!insideOffice) {
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
      serverPortField.setMinimumSize(new Dimension(100, 25));  // without this the box is just a few pixels small, but why?
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
            if (serverPort > -1 && serverPort < MAX_PORT) {
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
    cons.gridy++;
    cons.fill = GridBagConstraints.NONE;
    cons.anchor = GridBagConstraints.LINE_END;
    contentPane.add(treeButtonPanel, cons);
    
    cons.gridy++;
    cons.anchor = GridBagConstraints.WEST;
    contentPane.add(motherTonguePanel, cons);

    cons.gridy++;
    cons.anchor = GridBagConstraints.WEST;
    contentPane.add(portPanel, cons);

    cons.gridy++;
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

  private String[] getPossibleMotherTongues() {
    final List<String> motherTongues = new ArrayList<>();
    motherTongues.add(NO_MOTHER_TONGUE);
    for (final Language lang : Languages.get()) {
     motherTongues.add(lang.getTranslatedName(messages));
    }
    return motherTongues.toArray(new String[motherTongues.size()]);
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

  /**
   * Get the Language object for the given localized language name.
   * 
   * @param languageName e.g. <code>English</code> or <code>German</code> (case is significant)
   * @return a Language object or <code>null</code> if the language could not be found
   */
  @Nullable
  private Language getLanguageForLocalizedName(final String languageName) {
    for (final Language element : Languages.get()) {
      if (languageName.equals(element.getTranslatedName(messages))) {
        return element;
      }
    }
    return null;
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
