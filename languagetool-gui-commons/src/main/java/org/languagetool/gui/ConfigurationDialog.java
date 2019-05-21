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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.languagemodel.LuceneLanguageModel;
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
import java.io.File;
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
  private static final String ACTION_COMMAND_OK = "OK";
  private static final String ACTION_COMMAND_CANCEL = "CANCEL";
  private static final int MAX_PORT = 65536;

  private final ResourceBundle messages;
  private final Configuration original;
  private final Configuration config;
  private final Frame owner;
  private final boolean insideOffice;
  private boolean configChanged = false;

  private JDialog dialog;
  private JCheckBox serverCheckbox;
  private JTextField serverPortField;
  private JTree configTree[];
  private JCheckBox serverSettingsCheckbox;
  private final List<JPanel> extraPanels = new ArrayList<>();
  private final List<Rule> configurableRules = new ArrayList<>();

  public ConfigurationDialog(Frame owner, boolean insideOffice, Configuration config) {
    this.owner = owner;
    this.insideOffice = insideOffice;
    this.original = config;
    this.config = original.copy(original);
    messages = JLanguageTool.getMessageBundle();
  }

  /**
   * Add extra JPanel to this dialog.
   * 
   * If the panel implements {@see SavablePanel}, this dialog will call
   * {@link SavablePanel#save} after the user clicks OK.
   * 
   * @param panel the JPanel to be added to this dialog
   * @since 3.4
   */
  void addExtraPanel(JPanel panel) {
    extraPanels.add(panel);
  }

  private DefaultMutableTreeNode createTree(List<Rule> rules, boolean isStyle, String tabName) {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Rules");
    String lastRuleId = null;
    Map<String, DefaultMutableTreeNode> parents = new TreeMap<>();
    for (Rule rule : rules) {
      if((tabName == null && !config.isSpecialTabCategory(rule.getCategory().getName()) &&
          ((isStyle && config.isStyleCategory(rule.getCategory().getName())) ||
         (!isStyle && !config.isStyleCategory(rule.getCategory().getName())))) || 
          (tabName != null && config.isInSpecialTab(rule.getCategory().getName(), tabName))) {
        if(rule.hasConfigurableValue()) {
          configurableRules.add(rule);
        } else {
          if (!parents.containsKey(rule.getCategory().getName())) {
            boolean enabled = true;
            if (config.getDisabledCategoryNames() != null && config.getDisabledCategoryNames().contains(rule.getCategory().getName())) {
              enabled = false;
            }
            if(rule.getCategory().isDefaultOff() && (config.getEnabledCategoryNames() == null 
                || !config.getEnabledCategoryNames().contains(rule.getCategory().getName()))) {
              enabled = false;
            }
            DefaultMutableTreeNode categoryNode = new CategoryNode(rule.getCategory(), enabled);
            root.add(categoryNode);
            parents.put(rule.getCategory().getName(), categoryNode);
          }
          if (!rule.getId().equals(lastRuleId)) {
            RuleNode ruleNode = new RuleNode(rule, getEnabledState(rule));
            parents.get(rule.getCategory().getName()).add(ruleNode);
          }
          lastRuleId = rule.getId();
        }
      }
    }
    return root;
  }

  private boolean getEnabledState(Rule rule) {
    boolean ret = true;
    if (config.getDisabledRuleIds().contains(rule.getId())) {
      ret = false;
    }
    if (config.getDisabledCategoryNames().contains(rule.getCategory().getName())) {
      ret = false;
    }
    if ((rule.isDefaultOff() || rule.getCategory().isDefaultOff()) && !config.getEnabledRuleIds().contains(rule.getId())) {
      ret = false;
    }
    if (rule.isDefaultOff() && rule.getCategory().isDefaultOff()
            && config.getEnabledRuleIds().contains(rule.getId())) {
      config.getDisabledCategoryNames().remove(rule.getCategory().getName());
    }
    return ret;
  }

  public boolean show(List<Rule> rules) {
    configChanged = false;
    if (original != null) {
      config.restoreState(original);
    }
    dialog = new JDialog(owner, true);
    dialog.setTitle(messages.getString("guiConfigWindowTitle"));
    // close dialog when user presses Escape key:
    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    ActionListener actionListener = new ActionListener() {
      @Override
      public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent) {
        dialog.setVisible(false);
      }
    };
    JRootPane rootPane = dialog.getRootPane();
    rootPane.registerKeyboardAction(actionListener, stroke,
        JComponent.WHEN_IN_FOCUSED_WINDOW);

    configurableRules.clear();

    Language lang = config.getLanguage();
    if (lang == null) {
      lang = Languages.getLanguageForLocale(Locale.getDefault());
    }

    String specialTabNames[] = config.getSpecialTabNames();
    int numConfigTrees = 2 + specialTabNames.length;
    configTree = new JTree[numConfigTrees];
    JPanel checkBoxPanel[] = new JPanel[numConfigTrees];
    DefaultMutableTreeNode rootNode;
    GridBagConstraints cons;

    for (int i = 0; i < numConfigTrees; i++) {
      checkBoxPanel[i] = new JPanel();
      cons = new GridBagConstraints();
      checkBoxPanel[i].setLayout(new GridBagLayout());
      cons.anchor = GridBagConstraints.NORTHWEST;
      cons.gridx = 0;
      cons.weightx = 1.0;
      cons.weighty = 1.0;
      cons.fill = GridBagConstraints.HORIZONTAL;
      Collections.sort(rules, new CategoryComparator());
      if(i == 0) {
        rootNode = createTree(rules, false, null);   //  grammar options
      } else if(i ==1 ) {
        rootNode = createTree(rules, true, null);    //  Style options
      } else {
        rootNode = createTree(rules, true, specialTabNames[i - 2]);    //  Special tab options
      }
      configTree[i] = new JTree(getTreeModel(rootNode));
      
      configTree[i].applyComponentOrientation(ComponentOrientation.getOrientation(lang.getLocale()));
  
      configTree[i].setRootVisible(false);
      configTree[i].setEditable(false);
      configTree[i].setCellRenderer(new CheckBoxTreeCellRenderer());
      TreeListener.install(configTree[i]);
      checkBoxPanel[i].add(configTree[i], cons);
      configTree[i].addMouseListener(getMouseAdapter());
    }
    

    JPanel portPanel = new JPanel();
    portPanel.setLayout(new GridBagLayout());
    cons = new GridBagConstraints();
    cons.insets = new Insets(0, 4, 0, 0);
    cons.gridx = 0;
    cons.gridy = 0;
    cons.anchor = GridBagConstraints.WEST;
    cons.fill = GridBagConstraints.NONE;
    cons.weightx = 0.0f;
    if (!insideOffice) {
      createNonOfficeElements(cons, portPanel);
    }
    else {
      createOfficeElements(cons, portPanel);
    }

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridBagLayout());
    JButton okButton = new JButton(Tools.getLabel(messages.getString("guiOKButton")));
    okButton.setMnemonic(Tools.getMnemonic(messages.getString("guiOKButton")));
    okButton.setActionCommand(ACTION_COMMAND_OK);
    okButton.addActionListener(this);
    JButton cancelButton = new JButton(Tools.getLabel(messages.getString("guiCancelButton")));
    cancelButton.setMnemonic(Tools.getMnemonic(messages.getString("guiCancelButton")));
    cancelButton.setActionCommand(ACTION_COMMAND_CANCEL);
    cancelButton.addActionListener(this);
    cons = new GridBagConstraints();
    cons.insets = new Insets(0, 4, 0, 0);
    buttonPanel.add(okButton, cons);
    buttonPanel.add(cancelButton, cons);

    JTabbedPane tabpane = new JTabbedPane();

    JPanel jPane = new JPanel();
    jPane.setLayout(new GridBagLayout());
    cons = new GridBagConstraints();
    cons.insets = new Insets(4, 4, 4, 4);

    cons.gridx = 0;
    cons.gridy = 0;
    cons.weightx = 10.0f;
    cons.weighty = 0.0f;
    cons.fill = GridBagConstraints.NONE;
    cons.anchor = GridBagConstraints.NORTHWEST;
    cons.gridy++;
    cons.anchor = GridBagConstraints.WEST;
    jPane.add(getMotherTonguePanel(cons), cons);

    if(insideOffice) {
      cons.gridy += 3;
    } else {
      cons.gridy++;
    }
    cons.anchor = GridBagConstraints.WEST;
    jPane.add(getNgramPanel(cons), cons);

    cons.gridy++;
    cons.anchor = GridBagConstraints.WEST;
    jPane.add(getWord2VecPanel(cons), cons);

    cons.gridy++;
    cons.anchor = GridBagConstraints.WEST;
    jPane.add(portPanel, cons);
    cons.fill = GridBagConstraints.HORIZONTAL;
    cons.anchor = GridBagConstraints.WEST;
    for(JPanel extra : extraPanels) {
      //in case it wasn't in a containment hierarchy when user changed L&F
      SwingUtilities.updateComponentTreeUI(extra);
      cons.gridy++;
      jPane.add(extra, cons);
    }

    cons.gridy++;
    cons.fill = GridBagConstraints.BOTH;
    cons.weighty = 1.0f;
    jPane.add(new JPanel(), cons);

    tabpane.addTab(messages.getString("guiGeneral"), jPane);

    jPane = new JPanel();
    jPane.setLayout(new GridBagLayout());
    cons = new GridBagConstraints();
    cons.insets = new Insets(4, 4, 4, 4);
    cons.gridx = 0;
    cons.gridy = 0;
    cons.weightx = 10.0f;
    cons.weighty = 10.0f;
    cons.fill = GridBagConstraints.BOTH;
    jPane.add(new JScrollPane(checkBoxPanel[0]), cons);
    cons.weightx = 0.0f;
    cons.weighty = 0.0f;

    cons.gridx = 0;
    cons.gridy++;
    cons.fill = GridBagConstraints.NONE;
    cons.anchor = GridBagConstraints.LINE_END;
    jPane.add(getTreeButtonPanel(0), cons);

    tabpane.addTab(messages.getString("guiGrammarRules"), jPane);
    
    jPane = new JPanel();
    jPane.setLayout(new GridBagLayout());
    cons = new GridBagConstraints();
    cons.insets = new Insets(4, 4, 4, 4);
    cons.gridx = 0;
    cons.gridy = 0;
    cons.weightx = 10.0f;
    cons.weighty = 10.0f;
    cons.fill = GridBagConstraints.BOTH;
    jPane.add(new JScrollPane(checkBoxPanel[1]), cons);
    cons.weightx = 0.0f;
    cons.weighty = 0.0f;

    cons.gridx = 0;
    cons.gridy++;
    cons.fill = GridBagConstraints.NONE;
    cons.anchor = GridBagConstraints.LINE_END;
    jPane.add(getTreeButtonPanel(1), cons);
    
    cons.gridx = 0;
    cons.gridy++;
    cons.weightx = 5.0f;
    cons.weighty = 5.0f;
    cons.fill = GridBagConstraints.BOTH;
    cons.anchor = GridBagConstraints.WEST;
    jPane.add(new JScrollPane(getSpecialRuleValuePanel()), cons);

    tabpane.addTab(messages.getString("guiStyleRules"), jPane);

    for (int i = 0; i < specialTabNames.length; i++) {
      jPane = new JPanel();
      jPane.setLayout(new GridBagLayout());
      cons = new GridBagConstraints();
      cons.insets = new Insets(4, 4, 4, 4);
      cons.gridx = 0;
      cons.gridy = 0;
      cons.weightx = 10.0f;
      cons.weighty = 10.0f;
      cons.fill = GridBagConstraints.BOTH;
      jPane.add(new JScrollPane(checkBoxPanel[i + 2]), cons);
      cons.weightx = 0.0f;
      cons.weighty = 0.0f;
  
      cons.gridx = 0;
      cons.gridy++;
      cons.fill = GridBagConstraints.NONE;
      cons.anchor = GridBagConstraints.LINE_END;
      jPane.add(getTreeButtonPanel(i + 2), cons);
  
      tabpane.addTab(specialTabNames[i], jPane);
    }

    jPane = new JPanel();
    jPane.setLayout(new GridBagLayout());
    cons = new GridBagConstraints();
    cons.insets = new Insets(4, 4, 4, 4);
    cons.gridx = 0;
    cons.gridy = 0;
    if (insideOffice) {
      JLabel versionText = new JLabel(messages.getString("guiUColorHint"));
      versionText.setForeground(Color.blue);
      jPane.add(versionText, cons);
      cons.gridy++;
    }

    cons.weightx = 2.0f;
    cons.weighty = 2.0f;
    cons.fill = GridBagConstraints.BOTH;
    
    jPane.add(new JScrollPane(getUnderlineColorPanel(rules)), cons);
    
    Container contentPane = dialog.getContentPane();
    contentPane.setLayout(new GridBagLayout());
    cons = new GridBagConstraints();
    cons.insets = new Insets(4, 4, 4, 4);
    cons.gridx = 0;
    cons.gridy = 0;
    cons.weightx = 10.0f;
    cons.weighty = 10.0f;
    cons.fill = GridBagConstraints.BOTH;
    cons.anchor = GridBagConstraints.NORTHWEST;
    contentPane.add(tabpane, cons);
    cons.weightx = 0.0f;
    cons.weighty = 0.0f;
    cons.gridy++;
    cons.fill = GridBagConstraints.NONE;
    cons.anchor = GridBagConstraints.EAST;
    contentPane.add(buttonPanel, cons);

    dialog.pack();
    // center on screen:
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = dialog.getSize();
    dialog.setLocation(screenSize.width / 2 - frameSize.width / 2,
        screenSize.height / 2 - frameSize.height / 2);
    dialog.setLocationByPlatform(true);
    //  add Color tab after dimension was set
    tabpane.addTab(messages.getString("guiUnderlineColor"), jPane);

    for(JPanel extra : this.extraPanels) {
      if(extra instanceof SavablePanel) {
        ((SavablePanel) extra).componentShowing();
      }
    }
    dialog.setVisible(true);
    return configChanged;
  }

  private void createNonOfficeElements(GridBagConstraints cons, JPanel portPanel) {
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
    serverSettingsCheckbox.setMnemonic(Tools.getMnemonic(messages.getString("useGUIConfig")));
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
  
  private void createOfficeElements(GridBagConstraints cons, JPanel portPanel) {
    int numParaCheck = config.getNumParasToCheck();
    JRadioButton[] radioButtons = new JRadioButton[3];
    ButtonGroup numParaGroup = new ButtonGroup();
    radioButtons[0] = new JRadioButton(Tools.getLabel(messages.getString("guiCheckOnlyParagraph")));
    radioButtons[0].setActionCommand("ParagraphCheck");

    radioButtons[1] = new JRadioButton(Tools.getLabel(messages.getString("guiCheckFullText")));
    radioButtons[1].setActionCommand("FullTextCheck");
    
    radioButtons[2] = new JRadioButton(Tools.getLabel(messages.getString("guiCheckNumParagraphs")));
    radioButtons[2].setActionCommand("NParagraphCheck");
    radioButtons[2].setSelected(true);

    JTextField numParaField = new JTextField(Integer.toString(5), 2);
    numParaField.setEnabled(radioButtons[2].isSelected());
    numParaField.setMinimumSize(new Dimension(30, 25));
    
    for (int i = 0; i < 3; i++) {
      numParaGroup.add(radioButtons[i]);
    }
    
    if (numParaCheck == 0) {
      radioButtons[0].setSelected(true);
      numParaField.setEnabled(false);
    } else if (numParaCheck < 0) {
      radioButtons[1].setSelected(true);
      numParaField.setEnabled(false);    
    } else {
      radioButtons[2].setSelected(true);
      numParaField.setText(Integer.toString(numParaCheck));
      numParaField.setEnabled(true);
    }

    radioButtons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        numParaField.setEnabled(false);
        config.setNumParasToCheck(0);
      }
    });
    
    radioButtons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        numParaField.setEnabled(false);
        config.setNumParasToCheck(-1);
      }
    });
    
    radioButtons[2].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int numParaCheck = Integer.parseInt(numParaField.getText());
        if (numParaCheck < 1) numParaCheck = 1;
        else if (numParaCheck > 99) numParaCheck = 99;
        config.setNumParasToCheck(numParaCheck);
        numParaField.setForeground(Color.BLACK);
        numParaField.setText(Integer.toString(numParaCheck));
        numParaField.setEnabled(true);
      }
    });
    
    numParaField.getDocument().addDocumentListener(new DocumentListener() {

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
          int numParaCheck = Integer.parseInt(numParaField.getText());
          if (numParaCheck > 0 && numParaCheck < 99) {
            numParaField.setForeground(Color.BLACK);
            config.setNumParasToCheck(numParaCheck);
          } else {
            numParaField.setForeground(Color.RED);
          }
        } catch (NumberFormatException ex) {
          numParaField.setForeground(Color.RED);
        }
      }
    });


    
    for (int i = 0; i < 3; i++) {
      portPanel.add(radioButtons[i], cons);
      if (i < 2) cons.gridy++;
    }
    cons.gridx = 1;
    portPanel.add(numParaField, cons);
    
    JCheckBox noMultiResetbox = new JCheckBox(Tools.getLabel(messages.getString("guiNoMultiReset")));
    noMultiResetbox.setSelected(config.isNoMultiReset());
    noMultiResetbox.setEnabled(config.isResetCheck());
    noMultiResetbox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        config.setNoMultiReset(noMultiResetbox.isSelected());
      }
    });
    
    JCheckBox resetCheckbox = new JCheckBox(Tools.getLabel(messages.getString("guiDoResetCheck")));
    resetCheckbox.setSelected(config.isResetCheck());
    resetCheckbox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        config.setDoResetCheck(resetCheckbox.isSelected());
        noMultiResetbox.setEnabled(resetCheckbox.isSelected());
      }
    });
    cons.gridx = 0;
    JLabel dummyLabel = new JLabel(" ");
    cons.gridy++;
    portPanel.add(dummyLabel, cons);
    cons.gridy++;
    portPanel.add(resetCheckbox, cons);

    cons.insets = new Insets(0, 30, 0, 0);
    cons.gridx = 0;
    cons.gridy++;
    portPanel.add(noMultiResetbox, cons);
    
    JCheckBox isMultiThreadBox = new JCheckBox(Tools.getLabel(messages.getString("guiIsMultiThread")));
    isMultiThreadBox.setSelected(config.isMultiThread());
    isMultiThreadBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        config.setMultiThreadLO(isMultiThreadBox.isSelected());
      }
    });
    cons.insets = new Insets(0, 4, 0, 0);
    cons.gridx = 0;
    cons.gridy++;
    JLabel dummyLabel2 = new JLabel(" ");
    portPanel.add(dummyLabel2, cons);
    cons.gridy++;
    portPanel.add(isMultiThreadBox, cons);
    
  }

  @NotNull
  private DefaultTreeModel getTreeModel(DefaultMutableTreeNode rootNode) {
    DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
    treeModel.addTreeModelListener(new TreeModelListener() {
      @Override
      public void treeNodesChanged(TreeModelEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getTreePath().getLastPathComponent();
        int index = e.getChildIndices()[0];
        node = (DefaultMutableTreeNode) node.getChildAt(index);
        if (node instanceof RuleNode) {
          RuleNode o = (RuleNode) node;
          if (o.getRule().isDefaultOff() || o.getRule().getCategory().isDefaultOff()) {
            if (o.isEnabled()) {
              config.getEnabledRuleIds().add(o.getRule().getId());
              config.getDisabledRuleIds().remove(o.getRule().getId());
            } else {
              config.getEnabledRuleIds().remove(o.getRule().getId());
              config.getDisabledRuleIds().add(o.getRule().getId());
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
          if (o.getCategory().isDefaultOff()) {
            if (o.isEnabled()) {
              config.getDisabledCategoryNames().remove(o.getCategory().getName());
              config.getEnabledCategoryNames().add(o.getCategory().getName());
            } else {
              config.getDisabledCategoryNames().add(o.getCategory().getName());
              config.getEnabledCategoryNames().remove(o.getCategory().getName());
            }
          } else {
            if (o.isEnabled()) {
              config.getDisabledCategoryNames().remove(o.getCategory().getName());
            } else {
              config.getDisabledCategoryNames().add(o.getCategory().getName());
            }
          }
        }
      }
      @Override
      public void treeNodesInserted(TreeModelEvent e) {}
      @Override
      public void treeNodesRemoved(TreeModelEvent e) {}
      @Override
      public void treeStructureChanged(TreeModelEvent e) {}
    });
    return treeModel;
  }

  @NotNull
  private MouseAdapter getMouseAdapter() {
    return new MouseAdapter() {
        private void handlePopupEvent(MouseEvent e) {
          JTree tree = (JTree) e.getSource();
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
            JMenuItem aboutRuleMenuItem = new JMenuItem(messages.getString("guiAboutRuleMenu"));
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
                        rule.getDescription(), rule, rule.getUrl(), messages,
                        lang.getShortCodeWithCountryAndVariant());
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
  }

  @NotNull
  private JPanel getTreeButtonPanel(int num) {
    GridBagConstraints cons;
    JPanel treeButtonPanel = new JPanel();
    cons = new GridBagConstraints();
    cons.gridx = 0;
    cons.gridy = 0;
    JButton expandAllButton = new JButton(messages.getString("guiExpandAll"));
    treeButtonPanel.add(expandAllButton, cons);
    expandAllButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        TreeNode root = (TreeNode) configTree[num].getModel().getRoot();
        TreePath parent = new TreePath(root);
        for (Enumeration cat = root.children(); cat.hasMoreElements();) {
          TreeNode n = (TreeNode) cat.nextElement();
          TreePath child = parent.pathByAddingChild(n);
          configTree[num].expandPath(child);
        }
      }
    });

    cons.gridx = 1;
    cons.gridy = 0;
    JButton collapseAllButton = new JButton(messages.getString("guiCollapseAll"));
    treeButtonPanel.add(collapseAllButton, cons);
    collapseAllButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TreeNode root = (TreeNode) configTree[num].getModel().getRoot();
        TreePath parent = new TreePath(root);
        for (Enumeration categ = root.children(); categ.hasMoreElements();) {
          TreeNode n = (TreeNode) categ.nextElement();
          TreePath child = parent.pathByAddingChild(n);
          configTree[num].collapsePath(child);
        }
      }
    });
    return treeButtonPanel;
  }

  @NotNull
  private JPanel getMotherTonguePanel(GridBagConstraints cons) {
    JPanel motherTonguePanel = new JPanel();
    if(insideOffice){
      motherTonguePanel.setLayout(new GridBagLayout());
      GridBagConstraints cons1 = new GridBagConstraints();
      cons1.insets = new Insets(0, 0, 0, 0);
      cons1.gridx = 0;
      cons1.gridy = 0;
      cons1.anchor = GridBagConstraints.WEST;
      cons1.fill = GridBagConstraints.NONE;
      cons1.weightx = 0.0f;
      JRadioButton[] radioButtons = new JRadioButton[2];
      ButtonGroup numParaGroup = new ButtonGroup();
      radioButtons[0] = new JRadioButton(Tools.getLabel(messages.getString("guiUseDocumentLanguage")));
      radioButtons[0].setActionCommand("DocLang");
      radioButtons[0].setSelected(true);

      radioButtons[1] = new JRadioButton(Tools.getLabel(messages.getString("guiSetLanguageTo")));
      radioButtons[1].setActionCommand("SelectLang");

      JComboBox<String> motherTongueBox = new JComboBox<>(getPossibleMotherTongues());
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
            config.setUseDocLanguage(false);
            radioButtons[1].setSelected(true);
          }
        }
      });
      
      for (int i = 0; i < 2; i++) {
        numParaGroup.add(radioButtons[i]);
      }
      
      if (config.getUseDocLanguage()) {
        radioButtons[0].setSelected(true);
      } else {
        radioButtons[1].setSelected(true);
      }

      radioButtons[0].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          config.setUseDocLanguage(true);
        }
      });
      
      radioButtons[1].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          config.setUseDocLanguage(false);
          Language motherTongue;
          if (motherTongueBox.getSelectedItem() instanceof String) {
            motherTongue = getLanguageForLocalizedName(motherTongueBox.getSelectedItem().toString());
          } else {
            motherTongue = (Language) motherTongueBox.getSelectedItem();
          }
          config.setMotherTongue(motherTongue);
        }
      });
      motherTonguePanel.add(radioButtons[0], cons1);
      cons1.gridy++;
      motherTonguePanel.add(radioButtons[1], cons1);
      cons1.gridx = 1;
      motherTonguePanel.add(motherTongueBox, cons1);
    } else {
      motherTonguePanel.add(new JLabel(messages.getString("guiMotherTongue")), cons);
      JComboBox<String> motherTongueBox = new JComboBox<>(getPossibleMotherTongues());
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
    }
    return motherTonguePanel;
  }

  private JPanel getNgramPanel(GridBagConstraints cons) {
    JPanel panel = new JPanel();
    panel.add(new JLabel(messages.getString("guiNgramDir")), cons);
    File dir = config.getNgramDirectory();
    int maxDirDisplayLength = 45;
    String buttonText = dir != null ? StringUtils.abbreviate(dir.getAbsolutePath(), maxDirDisplayLength) : messages.getString("guiNgramDirSelect");
    JButton ngramDirButton = new JButton(buttonText);
    ngramDirButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        File newDir = Tools.openDirectoryDialog(owner, dir);
        if (newDir != null) {
          try {
            if (config.getLanguage() != null) {  // may happen in office context
              File checkDir = new File(newDir, config.getLanguage().getShortCode());
              LuceneLanguageModel.validateDirectory(checkDir);
            }
            config.setNgramDirectory(newDir);
            ngramDirButton.setText(StringUtils.abbreviate(newDir.getAbsolutePath(), maxDirDisplayLength));
          } catch (Exception ex) {
            Tools.showErrorMessage(ex);
          }
        } else {
          // not the best UI, but this way user can turn off ngram feature without another checkbox
          config.setNgramDirectory(null);
          ngramDirButton.setText(StringUtils.abbreviate(messages.getString("guiNgramDirSelect"), maxDirDisplayLength));
        }
      }
    });
    panel.add(ngramDirButton, cons);
    JButton helpButton = new JButton(messages.getString("guiNgramHelp"));
    helpButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Tools.openURL("http://wiki.languagetool.org/finding-errors-using-n-gram-data");
      }
    });
    panel.add(helpButton, cons);
    return panel;
  }

  private JPanel getWord2VecPanel(GridBagConstraints cons) {
    JPanel panel = new JPanel();
    panel.add(new JLabel(messages.getString("guiWord2VecDir")), cons);
    File dir = config.getWord2VecDirectory();
    int maxDirDisplayLength = 45;
    String buttonText = dir != null ? StringUtils.abbreviate(dir.getAbsolutePath(), maxDirDisplayLength) : messages.getString("guiWord2VecDirSelect");
    JButton word2vecDirButton = new JButton(buttonText);
    word2vecDirButton.addActionListener(e -> {
      File newDir = Tools.openDirectoryDialog(owner, dir);
      if (newDir != null) {
        try {
          config.setWord2VecDirectory(newDir);
          word2vecDirButton.setText(StringUtils.abbreviate(newDir.getAbsolutePath(), maxDirDisplayLength));
        } catch (Exception ex) {
          Tools.showErrorMessage(ex);
        }
      } else {
        // not the best UI, but this way user can turn off word2vec feature without another checkbox
        config.setWord2VecDirectory(null);
        word2vecDirButton.setText(StringUtils.abbreviate(messages.getString("guiWord2VecDirSelect"), maxDirDisplayLength));
      }
    });
    panel.add(word2vecDirButton, cons);
    JButton helpButton = new JButton(messages.getString("guiWord2VecHelp"));
    helpButton.addActionListener(e -> {
      Tools.openURL("https://github.com/gulp21/languagetool-neural-network");
    });
    panel.add(helpButton, cons);
    return panel;
  }

  private String[] getPossibleMotherTongues() {
    List<String> motherTongues = new ArrayList<>();
    if(!insideOffice) {
      motherTongues.add(NO_MOTHER_TONGUE);
    }
    for (Language lang : Languages.get()) {
     motherTongues.add(lang.getTranslatedName(messages));
    }
    return motherTongues.toArray(new String[motherTongues.size()]);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (ACTION_COMMAND_OK.equals(e.getActionCommand())) {
      if (original != null) {
        original.restoreState(config);
      }
      for(JPanel extra : extraPanels) {
        if(extra instanceof SavablePanel) {
          ((SavablePanel) extra).save();
        }
      }
      configChanged = true;
      dialog.setVisible(false);
    } else if (ACTION_COMMAND_CANCEL.equals(e.getActionCommand())) {
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
  private Language getLanguageForLocalizedName(String languageName) {
    for (Language element : Languages.get()) {
      if (languageName.equals(element.getTranslatedName(messages))) {
        return element;
      }
    }
    return null;
  }

  static class CategoryComparator implements Comparator<Rule> {

    @Override
    public int compare(Rule r1, Rule r2) {
      boolean hasCat = r1.getCategory() != null && r2.getCategory() != null;
      if (hasCat) {
        int res = r1.getCategory().getName().compareTo(r2.getCategory().getName());
        if (res == 0) {
          return r1.getDescription().compareToIgnoreCase(r2.getDescription());
        }
        return res;
      }
      return r1.getDescription().compareToIgnoreCase(r2.getDescription());
    }

  }

/* Panel to set Values for special rules like LongSentenceRule
 * @since 4.1
 */
  private JPanel getSpecialRuleValuePanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints cons = new GridBagConstraints();
    cons.gridx = 0;
    cons.gridy = 0;
    cons.weightx = 0.0f;
    cons.anchor = GridBagConstraints.WEST;
    
    List<JCheckBox> ruleCheckboxes = new ArrayList<JCheckBox>();
    List<JLabel> ruleLabels = new ArrayList<JLabel>();
    List<JTextField> ruleValueFields = new ArrayList<JTextField>();

    for(int i = 0; i < configurableRules.size(); i++) {
      Rule rule = configurableRules.get(i);
      JCheckBox ruleCheckbox = new JCheckBox(rule.getDescription());
      ruleCheckboxes.add(ruleCheckbox);
      ruleCheckbox.setSelected(getEnabledState(rule));
      cons.insets = new Insets(3, 0, 0, 0);
      panel.add(ruleCheckbox, cons);
  
      cons.insets = new Insets(0, 24, 0, 0);
      cons.gridy++;
      JLabel ruleLabel = new JLabel(rule.getConfigureText());
      ruleLabels.add(ruleLabel);
      ruleLabel.setEnabled(ruleCheckbox.isSelected());
      panel.add(ruleLabel, cons);
      
      cons.gridx++;
      int value = config.getConfigurableValue(rule.getId());
      if(config.getConfigurableValue(rule.getId()) < 0) {
        value = rule.getDefaultValue();
      }
      JTextField ruleValueField = new JTextField(Integer.toString(value), 2);
      ruleValueFields.add(ruleValueField);
      ruleValueField.setEnabled(ruleCheckbox.isSelected());
      ruleValueField.setMinimumSize(new Dimension(35, 25));  // without this the box is just a few pixels small, but why?
      panel.add(ruleValueField, cons);
      
      ruleCheckbox.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
          ruleValueField.setEnabled(ruleCheckbox.isSelected());
          ruleLabel.setEnabled(ruleCheckbox.isSelected());
          if (ruleCheckbox.isSelected()) {
            config.getEnabledRuleIds().add(rule.getId());
            config.getDisabledRuleIds().remove(rule.getId());
          } else {
            config.getEnabledRuleIds().remove(rule.getId());
            config.getDisabledRuleIds().add(rule.getId());
          }
        }
      });
  
      ruleValueField.getDocument().addDocumentListener(new DocumentListener() {
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
            int num = Integer.parseInt(ruleValueField.getText());
            if (num < rule.getMinConfigurableValue()) {
              num = rule.getMinConfigurableValue();
              ruleValueField.setForeground(Color.RED);
            } else if (num > rule.getMaxConfigurableValue()) {
              num = rule.getMaxConfigurableValue();
              ruleValueField.setForeground(Color.RED);
            } else {
              ruleValueField.setForeground(null);
            }
            config.setConfigurableValue(rule.getId(), num);
          } catch (Exception ex) {
            ruleValueField.setForeground(Color.RED);
          }
        }
      });

      cons.gridx = 0;
      cons.gridy++;

    }
    return panel;
  }

/*  Panel to choose underline Colors  
 *  @since 4.2
 */
  
  JPanel getUnderlineColorPanel(List<Rule> rules) {
    JPanel panel = new JPanel();

    panel.setLayout(new GridBagLayout());
    GridBagConstraints cons = new GridBagConstraints();
    cons.gridx = 0;
    cons.gridy = 0;
    cons.weightx = 0.0f;
    cons.fill = GridBagConstraints.NONE;
    cons.anchor = GridBagConstraints.NORTHWEST;

    List<String> categories = new ArrayList<String>();
    for (Rule rule : rules) {
      String category = rule.getCategory().getName();
      boolean contain = false;
      for(String c : categories) {
        if (c.equals(category)) {
          contain = true;
          break;
        }
      }
      if (!contain) {
        categories.add(category);
      }
    }
    List<JLabel> categorieLabel = new ArrayList<JLabel>();
    List<JLabel> underlineLabel = new ArrayList<JLabel>();
    List<JButton> changeButton = new ArrayList<JButton>();
    List<JButton> defaultButton = new ArrayList<JButton>();
    for(int nCat = 0; nCat < categories.size(); nCat++) {
      categorieLabel.add(new JLabel(categories.get(nCat) + " "));
      underlineLabel.add(new JLabel(" \u2588\u2588\u2588 "));  // \u2587 is smaller
      underlineLabel.get(nCat).setForeground(config.getUnderlineColor(categories.get(nCat)));
      underlineLabel.get(nCat).setBackground(config.getUnderlineColor(categories.get(nCat)));
      JLabel uLabel = underlineLabel.get(nCat);
      String cLabel = categories.get(nCat);
      panel.add(categorieLabel.get(nCat), cons);
      cons.gridx++;
      panel.add(underlineLabel.get(nCat), cons);

      changeButton.add(new JButton(messages.getString("guiUColorChange")));
      changeButton.get(nCat).addActionListener( new ActionListener() {
        @Override public void actionPerformed( ActionEvent e ) {
          Color oldColor = uLabel.getForeground();
          Color newColor = JColorChooser.showDialog( null, messages.getString("guiUColorDialogHeader"), oldColor);
          if(newColor != null && newColor != oldColor) {
            uLabel.setForeground(newColor);
            config.setUnderlineColor(cLabel, newColor);
          }
        }
      });
      cons.gridx++;
      panel.add(changeButton.get(nCat), cons);
  
      defaultButton.add(new JButton(messages.getString("guiUColorDefault")));
      defaultButton.get(nCat).addActionListener( new ActionListener() {
        @Override public void actionPerformed( ActionEvent e ) {
          config.setDefaultUnderlineColor(cLabel);
          uLabel.setForeground(config.getUnderlineColor(cLabel));
        }
      });
      cons.gridx++;
      panel.add(defaultButton.get(nCat), cons);
      cons.gridx = 0;
      cons.gridy++;
    }
    return panel;
  }
  
  
}
