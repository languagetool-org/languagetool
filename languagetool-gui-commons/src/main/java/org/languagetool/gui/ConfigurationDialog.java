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
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

/**
 * Dialog that offers the available rules so they can be turned on/off
 * individually.
 * 
 * @author Daniel Naber
 */
public class ConfigurationDialog implements ActionListener {

  private static final String NO_SELECTED_LANGUAGE = "---";
  private static final String ACTION_COMMAND_OK = "OK";
  private static final String ACTION_COMMAND_CANCEL = "CANCEL";
  private static final int MAX_PORT = 65536;

  private static final int SHIFT1 = 4;
  private static final int SHIFT2 = 20;

  private final ResourceBundle messages;
  private final Configuration original;
  private final Configuration config;
  private final Frame owner;
  private final boolean insideOffice;
  private boolean configChanged = false;
  private boolean profileChanged = true;
  private boolean restartShow = false;
  private boolean firstSelection = true;

  private JDialog dialog;
  private JCheckBox serverCheckbox;
  private JTextField serverPortField;
  private JTree[] configTree;
  private DefaultMutableTreeNode[] rootNode;
  private JCheckBox serverSettingsCheckbox;
  private JPanel disabledRulesPanel;
  private JPanel enabledRulesPanel;
  private final List<JPanel> extraPanels = new ArrayList<>();
  private final List<Rule> configurableRules = new ArrayList<>();
  private String category;
  private Rule rule;

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

  private DefaultMutableTreeNode createTree(List<Rule> rules, boolean isStyle, String tabName, DefaultMutableTreeNode root) {
    if (root == null) {
      root = new DefaultMutableTreeNode("Rules");
    } else {
      root.removeAllChildren();
    }
    String lastRuleId = null;
    Map<String, DefaultMutableTreeNode> parents = new TreeMap<>();
    for (Rule rule : rules) {
      if((tabName == null && !config.isSpecialTabCategory(rule.getCategory().getName()) &&
          ((isStyle && config.isStyleCategory(rule.getCategory().getName())) ||
         (!isStyle && !config.isStyleCategory(rule.getCategory().getName())))) || 
          (tabName != null && config.isInSpecialTab(rule.getCategory().getName(), tabName))) {
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
    if (insideOffice && rule.isOfficeDefaultOff() && !config.getEnabledRuleIds().contains(rule.getId())) {
      ret = false;
    }
    if (insideOffice && rule.isOfficeDefaultOn() && !config.getDisabledRuleIds().contains(rule.getId())) {
      ret = true;
    }
    if (rule.isDefaultOff() && rule.getCategory().isDefaultOff()
            && config.getEnabledRuleIds().contains(rule.getId())) {
      config.getDisabledCategoryNames().remove(rule.getCategory().getName());
    }
    return ret;
  }

  public boolean show(List<Rule> rules) {
    restartShow = false;
    do {
      showPanel(rules);
    } while (restartShow);
    return configChanged;
  }
    
  public boolean showPanel(List<Rule> rules) {
    configChanged = false;
    if (original != null && !restartShow) {
      config.restoreState(original);
    }
    restartShow = false;
    dialog = new JDialog(owner, true);
    dialog.setTitle(messages.getString("guiConfigWindowTitle"));
    // close dialog when user presses Escape key:
    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    ActionListener actionListener = actionEvent -> dialog.setVisible(false);
    JRootPane rootPane = dialog.getRootPane();
    rootPane.registerKeyboardAction(actionListener, stroke,
        JComponent.WHEN_IN_FOCUSED_WINDOW);

    configurableRules.clear();

    Language lang = config.getLanguage();
    if (lang == null) {
      lang = Languages.getLanguageForLocale(Locale.getDefault());
    }

    String[] specialTabNames = config.getSpecialTabNames();
    int numConfigTrees = 2 + specialTabNames.length;
    configTree = new JTree[numConfigTrees];
    rootNode = new DefaultMutableTreeNode[numConfigTrees];
    JPanel[] checkBoxPanel = new JPanel[numConfigTrees];
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
        rootNode[i] = createTree(rules, false, null, null);   //  grammar options
      } else if(i ==1 ) {
        rootNode[i] = createTree(rules, true, null, null);    //  Style options
      } else {
        rootNode[i] = createTree(rules, true, specialTabNames[i - 2], null);    //  Special tab options
      }
      configTree[i] = new JTree(getTreeModel(rootNode[i], rules));
      
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
    cons.insets = new Insets(0, SHIFT1, 0, 0);
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
    cons.insets = new Insets(0, SHIFT1, 0, 0);
    buttonPanel.add(okButton, cons);
    buttonPanel.add(cancelButton, cons);

    JTabbedPane tabpane = new JTabbedPane();

//  Profile tab    
    JPanel jProfilePane = new JPanel();
    jProfilePane.setLayout(new GridBagLayout());
    cons = new GridBagConstraints();
    cons.insets = new Insets(4, 4, 4, 4);

    cons.gridx = 0;
    cons.gridy = 0;
    cons.weightx = 10.0f;
    cons.weighty = 10.0f;
    cons.fill = GridBagConstraints.BOTH;
    cons.anchor = GridBagConstraints.NORTHWEST;
    
    jProfilePane.add(new JScrollPane(getProfilePanel(rules)), cons);
    
    //  Disabled default rules
    cons.weighty = 1.0f;
    cons.gridy++;
    cons.insets = new Insets(16, 4, 0, 8);
    jProfilePane.add(new JLabel(addColonToMessageString("guiDisabledDefaultRules")), cons);
    cons.insets = new Insets(8, 4, 0, 8);
    cons.gridy++;
    cons.weighty = 3.0f;
    disabledRulesPanel = getChangedRulesPanel(rules, false, null);
    jProfilePane.add(new JScrollPane(disabledRulesPanel), cons);
    
    //  Enabled optional rules
    cons.gridy++;
    cons.insets = new Insets(16, 4, 0, 8);
    cons.weighty = 1.0f;
    jProfilePane.add(new JLabel(addColonToMessageString("guiEnabledOptionalRules")), cons);
    cons.insets = new Insets(8, 4, 0, 8);
    cons.gridy++;
    cons.weighty = 5.0f;
    enabledRulesPanel = getChangedRulesPanel(rules, true, null);
    jProfilePane.add(new JScrollPane(enabledRulesPanel), cons);
    jProfilePane.setName(messages.getString("guiProfiles"));
    
//  General tab
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

    if(!insideOffice) {
      cons.gridy++;
      cons.anchor = GridBagConstraints.WEST;
      jPane.add(getMotherTonguePanel(cons), cons);
      cons.gridx = 0;
      cons.gridy++;
      jPane.add(getNgramAndWord2VecPanel(), cons);
    }
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
    tabpane.addTab(messages.getString("guiGeneral"), new JScrollPane(jPane));

//  Grammar rules tab    
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
    cons.fill = GridBagConstraints.HORIZONTAL;
    cons.anchor = GridBagConstraints.WEST;
    cons.gridx = 0;
    cons.gridy++;
    jPane.add(getRuleOptionsPanel(0), cons);

    tabpane.addTab(messages.getString("guiGrammarRules"), jPane);
    
//  Style rules tab    
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
    cons.fill = GridBagConstraints.HORIZONTAL;
    cons.anchor = GridBagConstraints.WEST;
    cons.gridx = 0;
    cons.gridy++;
    jPane.add(getRuleOptionsPanel(1), cons);

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
  
      cons.fill = GridBagConstraints.HORIZONTAL;
      cons.anchor = GridBagConstraints.WEST;
      cons.gridx = 0;
      cons.gridy++;
      jPane.add(getRuleOptionsPanel(i + 2), cons);

      tabpane.addTab(specialTabNames[i], jPane);
    }
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
    //  add Profile tab after dimension was set
    tabpane.add(jProfilePane, 0);
    tabpane.setSelectedIndex(0);
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
    serverCheckbox.addActionListener(e -> {
      serverPortField.setEnabled(serverCheckbox.isSelected());
      serverSettingsCheckbox.setEnabled(serverCheckbox.isSelected());
    });
    serverCheckbox.addItemListener(e -> config.setRunServer(serverCheckbox.isSelected()));

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
    serverSettingsCheckbox.addItemListener(e -> config.setUseGUIConfig(serverSettingsCheckbox.isSelected()));
    portPanel.add(serverSettingsCheckbox, cons);
  }
  
  private void addOfficeLanguageElements(GridBagConstraints cons, JPanel portPanel) {
    JPanel languagePanel = new JPanel();
    languagePanel.setLayout(new GridBagLayout());
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

    JComboBox<String> fixedLanguageBox = new JComboBox<>(getPossibleLanguages(false));
    if (config.getFixedLanguage() != null) {
      fixedLanguageBox.setSelectedItem(config.getFixedLanguage().getTranslatedName(messages));
    }
    fixedLanguageBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        Language fixedLanguage;
        if (fixedLanguageBox.getSelectedItem() instanceof String) {
          fixedLanguage = getLanguageForLocalizedName(fixedLanguageBox.getSelectedItem().toString());
        } else {
          fixedLanguage = (Language) fixedLanguageBox.getSelectedItem();
        }
        config.setFixedLanguage(fixedLanguage);
        config.setUseDocLanguage(false);
        radioButtons[1].setSelected(true);
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

    radioButtons[0].addActionListener(e -> config.setUseDocLanguage(true));
    
    radioButtons[1].addActionListener(e -> {
      config.setUseDocLanguage(false);
      Language fixedLanguage;
      if (fixedLanguageBox.getSelectedItem() instanceof String) {
        fixedLanguage = getLanguageForLocalizedName(fixedLanguageBox.getSelectedItem().toString());
      } else {
        fixedLanguage = (Language) fixedLanguageBox.getSelectedItem();
      }
      config.setFixedLanguage(fixedLanguage);
    });
    languagePanel.add(radioButtons[0], cons1);
    cons1.gridy++;
    languagePanel.add(radioButtons[1], cons1);
    cons1.gridx = 1;
    languagePanel.add(fixedLanguageBox, cons1);

    cons.insets = new Insets(0, SHIFT1, 0, 0);
    cons.gridx = 0;
    cons.gridy++;
    portPanel.add(languagePanel, cons);
  }

  private void addOfficeTextruleElements(GridBagConstraints cons, JPanel portPanel, JCheckBox useQueueResetbox, JCheckBox saveCacheBox) {
    int numParaCheck = config.getNumParasToCheck();
    boolean useTextLevelQueue = config.useTextLevelQueue();
    JRadioButton[] radioButtons = new JRadioButton[3];
    ButtonGroup numParaGroup = new ButtonGroup();
    radioButtons[0] = new JRadioButton(Tools.getLabel(messages.getString("guiTextCheckMode")));
    radioButtons[0].setActionCommand("FullTextCheck");
    
    radioButtons[1] = new JRadioButton(Tools.getLabel(messages.getString("guiParagraphCheckMode")));
    radioButtons[1].setActionCommand("ParagraphCheck");

    radioButtons[2] = new JRadioButton(Tools.getLabel(messages.getString("guiDeveloperModeCheck")));
    radioButtons[2].setActionCommand("NParagraphCheck");

    JTextField numParaField = new JTextField(Integer.toString(5), 2);
    numParaField.setEnabled(radioButtons[2].isSelected());
    numParaField.setMinimumSize(new Dimension(30, 25));
    
    for (int i = 0; i < 3; i++) {
      numParaGroup.add(radioButtons[i]);
    }
    
    if (numParaCheck == 0 || config.onlySingleParagraphMode()) {
      radioButtons[1].setSelected(true);
      numParaField.setEnabled(false);
      saveCacheBox.setEnabled(false);
      config.setUseTextLevelQueue(false);
//      useQueueResetbox.setEnabled(false);
      if (config.onlySingleParagraphMode()) {
        radioButtons[0].setEnabled(false);
        radioButtons[2].setEnabled(false);
      }
    } else if (useTextLevelQueue) {
      radioButtons[0].setSelected(true);
      numParaField.setEnabled(false);
      config.setNumParasToCheck(-2);
    } else {
      radioButtons[2].setSelected(true);
      numParaField.setText(Integer.toString(numParaCheck));
      numParaField.setEnabled(true);
    }

    radioButtons[0].addActionListener(e -> {
      numParaField.setEnabled(false);
      config.setNumParasToCheck(-2);
      config.setUseTextLevelQueue(true);
//      useQueueResetbox.setEnabled(false);
      saveCacheBox.setEnabled(true);
    });
    
    radioButtons[1].addActionListener(e -> {
      numParaField.setEnabled(false);
      config.setNumParasToCheck(0);
      config.setUseTextLevelQueue(false);
//      useQueueResetbox.setEnabled(true);
      saveCacheBox.setEnabled(false);
    });
    
    radioButtons[2].addActionListener(e -> {
      int numParaCheck1 = Integer.parseInt(numParaField.getText());
      if (numParaCheck1 < -2) numParaCheck1 = -2;
      else if (numParaCheck1 > 99) numParaCheck1 = 99;
      config.setNumParasToCheck(numParaCheck1);
      numParaField.setForeground(Color.BLACK);
      numParaField.setText(Integer.toString(numParaCheck1));
      numParaField.setEnabled(true);
      config.setUseTextLevelQueue(false);
//      useQueueResetbox.setEnabled(true);
      saveCacheBox.setEnabled(true);
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
          if (numParaCheck > -3 && numParaCheck < 99) {
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

    JLabel textChangedLabel = new JLabel(Tools.getLabel(messages.getString("guiSentenceExceedingRules")));
    cons.gridy++;
    portPanel.add(textChangedLabel, cons);
    
    JPanel radioPanel = new JPanel();
    radioPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons1 = new GridBagConstraints();
    cons1.insets = new Insets(0, 0, 0, 0);
    cons1.gridx = 0;
    cons1.gridy = 0;
    cons1.anchor = GridBagConstraints.WEST;
    cons1.fill = GridBagConstraints.NONE;
    cons1.weightx = 0.0f;
    for (int i = 0; i < 3; i++) {
      radioPanel.add(radioButtons[i], cons1);
      if (i < 2) cons1.gridy++;
    }
    cons1.gridx = 1;
    radioPanel.add(numParaField, cons1);
    cons.insets = new Insets(0, SHIFT2, 0, 0);
    cons.gridy++;
    portPanel.add(radioPanel, cons);
  }
  
  private void addOfficeTechnicalElements(GridBagConstraints cons, JPanel portPanel) {
    JLabel typeOfCheckLabel = new JLabel(Tools.getLabel(messages.getString("guiTechnicalSettings")));
    // technical settings
    cons.gridy++;
    portPanel.add(typeOfCheckLabel, cons);
    JTextField otherServerNameField = new JTextField(config.getServerUrl() ==  null ? "" : config.getServerUrl(), 25);
    otherServerNameField.setMinimumSize(new Dimension(100, 25));
    otherServerNameField.getDocument().addDocumentListener(new DocumentListener() {
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
        String serverName = otherServerNameField.getText();
        serverName = serverName.trim();
        if(serverName.isEmpty()) {
          serverName = null;
        }
        if (config.isValidServerUrl(serverName)) {
          otherServerNameField.setForeground(Color.BLACK);
          config.setOtherServerUrl(serverName);
        } else {
          otherServerNameField.setForeground(Color.RED);
        }
      }
    });

    JCheckBox useServerBox = new JCheckBox(Tools.getLabel(messages.getString("guiUseServer")) + " ");
    useServerBox.setSelected(config.useOtherServer());
    useServerBox.addItemListener(e -> {
      int select = JOptionPane.OK_OPTION;
      boolean selected = useServerBox.isSelected();
      if(selected && firstSelection) {
        select = showRemoteServerHint(useServerBox, true);
        firstSelection = false;
      } else {
        firstSelection = true;
      }
      if(select == JOptionPane.OK_OPTION) {
        useServerBox.setSelected(selected);
        config.setUseOtherServer(useServerBox.isSelected());
        otherServerNameField.setEnabled(useServerBox.isSelected());
      } else {
        useServerBox.setSelected(false);
        firstSelection = true;
      }
    });
    JRadioButton[] typeOfCheckButtons = new JRadioButton[3];
    ButtonGroup typeOfCheckGroup = new ButtonGroup();
    typeOfCheckButtons[0] = new JRadioButton(Tools.getLabel(messages.getString("guiOneThread")));
    typeOfCheckButtons[0].addActionListener(e -> {
      otherServerNameField.setEnabled(false);
      useServerBox.setEnabled(false);
      config.setMultiThreadLO(false);
      config.setRemoteCheck(false);
    });
    typeOfCheckButtons[1] = new JRadioButton(Tools.getLabel(messages.getString("guiIsMultiThread")));
    typeOfCheckButtons[1].addActionListener(e -> {
      otherServerNameField.setEnabled(false);
      useServerBox.setEnabled(false);
      config.setMultiThreadLO(true);
      config.setRemoteCheck(false);
    });
    typeOfCheckButtons[2] = new JRadioButton(Tools.getLabel(messages.getString("guiUseRemoteServer")));
    typeOfCheckButtons[2].addActionListener(e -> {
      int select = JOptionPane.OK_OPTION;
      boolean selected = typeOfCheckButtons[2].isSelected();
      if(selected && firstSelection) {
        select = showRemoteServerHint(typeOfCheckButtons[2], false);
        firstSelection = false;
      } else {
        firstSelection = true;
      }
      if(select == JOptionPane.OK_OPTION) {
//        typeOfCheckButtons[2].setSelected(selected);
        otherServerNameField.setEnabled(useServerBox.isSelected());
        useServerBox.setEnabled(true);
        config.setMultiThreadLO(false);
        config.setRemoteCheck(true);
      } else {
        if (config.isMultiThread()) {
          typeOfCheckButtons[1].setSelected(true);
        } else {
          typeOfCheckButtons[0].setSelected(true);
        }
        firstSelection = true;
      }
    });
    for (int i = 0; i < 3; i++) {
      typeOfCheckGroup.add(typeOfCheckButtons[i]);
    }
    if (config.doRemoteCheck()) {
      typeOfCheckButtons[2].setSelected(true);
      otherServerNameField.setEnabled(useServerBox.isSelected());
      useServerBox.setEnabled(true);
      config.setMultiThreadLO(false);
      config.setRemoteCheck(true);
    } else if (config.isMultiThread()) {
      typeOfCheckButtons[1].setSelected(true);
      otherServerNameField.setEnabled(false);
      useServerBox.setEnabled(false);
      config.setMultiThreadLO(true);
      config.setRemoteCheck(false);
    } else {
      typeOfCheckButtons[0].setSelected(true);
      otherServerNameField.setEnabled(false);
      useServerBox.setEnabled(false);
      config.setMultiThreadLO(false);
      config.setRemoteCheck(false);
    }
    cons.gridy++;
    cons.insets = new Insets(0, SHIFT2, 0, 0);
    for (int i = 0; i < 3; i++) {
      portPanel.add(typeOfCheckButtons[i], cons);
      if (i < 3) cons.gridy++;
    }

    JPanel serverPanel = new JPanel();
    serverPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons1 = new GridBagConstraints();
    cons1.insets = new Insets(0, SHIFT2, 0, 0);
    cons1.gridx = 0;
    cons1.gridy = 0;
    cons1.anchor = GridBagConstraints.WEST;
    cons1.fill = GridBagConstraints.NONE;
    cons1.weightx = 0.0f;
    serverPanel.add(useServerBox, cons1);
    cons1.gridx++;
    serverPanel.add(otherServerNameField, cons1);
    JLabel serverExampleLabel = new JLabel(" " + Tools.getLabel(messages.getString("guiUseServerExample")));
    serverExampleLabel.setEnabled(false);
    cons1.gridy++;
    serverPanel.add(serverExampleLabel, cons1);

    cons.gridx = 0;
    cons.gridy++;
    portPanel.add(serverPanel, cons);
  }
  
  private void createOfficeElements(GridBagConstraints cons, JPanel portPanel) {

    JCheckBox useQueueResetbox = new JCheckBox(Tools.getLabel(messages.getString("guiUseTextLevelQueue")));
    JCheckBox saveCacheBox = new JCheckBox(Tools.getLabel(messages.getString("guiSaveCacheToFile")));
    
    addOfficeLanguageElements(cons, portPanel);

    cons.gridx = 0;
    cons.gridy++;
    portPanel.add(new JLabel(" "), cons);
    
    cons.gridy++;
    portPanel.add(getMotherTonguePanel(cons), cons);
    
    cons.gridx = 0;
    cons.gridy++;
    portPanel.add(new JLabel(" "), cons);
    
    JCheckBox markSingleCharBold = new JCheckBox(Tools.getLabel(messages.getString("guiMarkSingleCharBold")));
    markSingleCharBold.setSelected(config.markSingleCharBold());
    markSingleCharBold.addItemListener(e -> config.setMarkSingleCharBold(markSingleCharBold.isSelected()));
    cons.gridy++;
    portPanel.add(markSingleCharBold, cons);

    JCheckBox useLtDictionaryBox = new JCheckBox(Tools.getLabel(messages.getString("guiUseLtDictionary")));
    useLtDictionaryBox.setSelected(config.useLtDictionary());
    useLtDictionaryBox.addItemListener(e -> {
      config.setUseLtDictionary(useLtDictionaryBox.isSelected());
    });
    cons.gridy++;
    portPanel.add(useLtDictionaryBox, cons);

    JCheckBox noSynonymsAsSuggestionsBox = new JCheckBox(Tools.getLabel(messages.getString("guiNoSynonymsAsSuggestions")));
    noSynonymsAsSuggestionsBox.setSelected(config.noSynonymsAsSuggestions());
    noSynonymsAsSuggestionsBox.addItemListener(e -> {
      config.setNoSynonymsAsSuggestions(noSynonymsAsSuggestionsBox.isSelected());
    });
    cons.gridy++;
    portPanel.add(noSynonymsAsSuggestionsBox, cons);

    JCheckBox noBackgroundCheckBox = new JCheckBox(Tools.getLabel(messages.getString("guiNoBackgroundCheck")));
    noBackgroundCheckBox.setSelected(config.noBackgroundCheck());
    noBackgroundCheckBox.addItemListener(e -> config.setNoBackgroundCheck(noBackgroundCheckBox.isSelected()));
    cons.gridy++;
    portPanel.add(noBackgroundCheckBox, cons);

    cons.gridy++;
    portPanel.add(new JLabel(" "), cons);
    
    addOfficeTextruleElements(cons, portPanel, useQueueResetbox, saveCacheBox);
    
    cons.insets = new Insets(0, SHIFT1, 0, 0);
    cons.gridx = 0;
/*
    cons.gridy++;
    JLabel dummyLabel4 = new JLabel(" ");
    portPanel.add(dummyLabel4, cons);
*/    
    cons.gridy++;
    portPanel.add(new JLabel(" "), cons);
    
    addOfficeTechnicalElements(cons, portPanel);
/*
    useQueueResetbox.setSelected(config.useTextLevelQueue());
    useQueueResetbox.addItemListener(e -> {
      config.setUseTextLevelQueue(useQueueResetbox.isSelected());
    });
    cons.insets = new Insets(0, SHIFT1, 0, 0);
    cons.gridx = 0;
    cons.gridy++;
    portPanel.add(useQueueResetbox, cons);
*/
    saveCacheBox.setSelected(config.saveLoCache());
    saveCacheBox.addItemListener(e -> {
      config.setSaveLoCache(saveCacheBox.isSelected());
    });
    cons.insets = new Insets(0, SHIFT2, 0, 0);
    cons.gridx = 0;
    cons.gridy++;
    portPanel.add(saveCacheBox, cons);
    
    cons.gridy++;
    portPanel.add(getNgramAndWord2VecPanel(), cons);
  }
  
  private int showRemoteServerHint(Component component, boolean otherServer) {
    if(config.useOtherServer() || otherServer) {
        return JOptionPane.showConfirmDialog(component, 
            MessageFormat.format(messages.getString("loRemoteInfoOtherServer"), config.getServerUrl()), 
          messages.getString("loMenuRemoteInfo"), JOptionPane.OK_CANCEL_OPTION);
    } else {
      return JOptionPane.showConfirmDialog(component, messages.getString("loRemoteInfoDefaultServer"), 
          messages.getString("loMenuRemoteInfo"), JOptionPane.OK_CANCEL_OPTION);
    }
  }

  @NotNull
  private DefaultTreeModel getTreeModel(DefaultMutableTreeNode rootNode, List<Rule> rules) {
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
          updateProfileRules(rules);
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
            aboutRuleMenuItem.addActionListener(actionEvent -> {
              RuleNode node1 = (RuleNode) tree.getSelectionPath().getLastPathComponent();
              Rule rule = node1.getRule();
              Language lang = config.getLanguage();
              if(lang == null) {
                lang = Languages.getLanguageForLocale(Locale.getDefault());
              }
              Tools.showRuleInfoDialog(tree, messages.getString("guiAboutRuleTitle"),
                      rule.getDescription(), rule, rule.getUrl(), messages,
                      lang.getShortCodeWithCountryAndVariant());
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
    expandAllButton.addActionListener(e -> {
      TreeNode root = (TreeNode) configTree[num].getModel().getRoot();
      TreePath parent = new TreePath(root);
      for (Enumeration<?> cat = root.children(); cat.hasMoreElements();) {
        TreeNode n = (TreeNode) cat.nextElement();
        TreePath child = parent.pathByAddingChild(n);
        configTree[num].expandPath(child);
      }
    });

    cons.gridx = 1;
    cons.gridy = 0;
    JButton collapseAllButton = new JButton(messages.getString("guiCollapseAll"));
    treeButtonPanel.add(collapseAllButton, cons);
    collapseAllButton.addActionListener(e -> {
      TreeNode root = (TreeNode) configTree[num].getModel().getRoot();
      TreePath parent = new TreePath(root);
      for (Enumeration<?> categ = root.children(); categ.hasMoreElements();) {
        TreeNode n = (TreeNode) categ.nextElement();
        TreePath child = parent.pathByAddingChild(n);
        configTree[num].collapsePath(child);
      }
    });
    return treeButtonPanel;
  }
  
  @NotNull
  private JPanel getProfilePanel(List<Rule> rules) {
    profileChanged = true;
    JPanel profilePanel = new JPanel();
    profilePanel.setLayout(new GridBagLayout());
    GridBagConstraints cons = new GridBagConstraints();
    cons.gridx = 0;
    cons.gridy = 0;
    cons.weightx = 1.0f;
    cons.anchor = GridBagConstraints.WEST;
    List<String> profiles = new ArrayList<>();
    String defaultOptions = messages.getString("guiDefaultOptions");
    String userOptions = messages.getString("guiUserProfile");
    profiles.addAll(config.getDefinedProfiles());
    profiles.sort(null);
    profiles.add(0, userOptions);
    String currentProfile = config.getCurrentProfile();
    JComboBox<String> profileBox = new JComboBox<>(profiles.toArray(new String[0]));
    if(currentProfile == null || currentProfile.isEmpty()) {
      profileBox.setSelectedItem(userOptions);
    } else {
      profileBox.setSelectedItem(currentProfile);
    }
    profileBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        if(profileChanged) {
          try {
            //  The configuration has to be saved first to save previous changes
            config.saveConfiguration(null);
            List<String> saveProfiles = new ArrayList<>();
            saveProfiles.addAll(config.getDefinedProfiles());
            if(e.getItem().equals(userOptions)) {
              config.initOptions();
              config.loadConfiguration("");
              config.setCurrentProfile(null);
            } else {
              config.initOptions();
              config.loadConfiguration((String) e.getItem());
              config.setCurrentProfile((String) e.getItem());
            }
            config.addProfiles(saveProfiles);
            restartShow = true;
            dialog.setVisible(false);
          } catch (IOException e1) {
          }
        } else {
          profileChanged = true;
        }
      }
    });
      
    profilePanel.add(new JLabel(addColonToMessageString("guiCurrentProfile")), cons);
    cons.insets = new Insets(6, 12, 0, 8);
    cons.gridy++;
    profilePanel.add(profileBox, cons);
    
    JButton renameButton = new JButton(messages.getString("guiRenameProfile") + "...");
    renameButton.setEnabled(!profileBox.getSelectedItem().equals(defaultOptions) 
        && !profileBox.getSelectedItem().equals(userOptions));
    renameButton.addActionListener(e -> {
      boolean noName = true;
      String profileName = (String) profileBox.getSelectedItem();
      while (noName) {
        profileName = JOptionPane.showInputDialog(dialog, messages.getString("guiRenameProfile") + ":", profileName);
        if (profileName == null || profileName.equals("")) {
          break;
        }
        profileName = profileName.replaceAll("[ \t=]", "_");
        noName = false;
        while(config.getDefinedProfiles().contains(profileName) || userOptions.equals(profileName)) {
          profileName += "_new";
          noName = true;
        }
      }
      if (profileName != null && !profileName.equals("")) {
        config.removeProfile(config.getCurrentProfile());
        config.addProfile(profileName);
        config.setCurrentProfile(profileName);
        restartShow = true;
        dialog.setVisible(false);
      }
    });
    cons.gridy++;
    profilePanel.add(renameButton, cons);
    
    JButton exportButton = new JButton(messages.getString("guiExportProfile") + "...");
    exportButton.setEnabled(!profileBox.getSelectedItem().equals(defaultOptions) 
        && !profileBox.getSelectedItem().equals(userOptions));
    exportButton.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      int choose = fileChooser.showSaveDialog(dialog);
      if (choose == JFileChooser.APPROVE_OPTION) {
        try {
          config.exportProfile((String) profileBox.getSelectedItem(), fileChooser.getSelectedFile());
        } catch (IOException e1) {
        }
      }
    });
    cons.gridx++;
    profilePanel.add(exportButton, cons);
    
    JButton defaultButton = new JButton(defaultOptions);
    defaultButton.addActionListener(e -> {
      List<String> saveProfiles = new ArrayList<>();
      saveProfiles.addAll(config.getDefinedProfiles());
      String saveCurrent = config.getCurrentProfile() == null ? null : config.getCurrentProfile();
      config.initOptions();
      config.addProfiles(saveProfiles);
      config.setCurrentProfile(saveCurrent);
      restartShow = true;
      dialog.setVisible(false);
    });
    cons.gridx = 0;
    cons.gridy++;
    profilePanel.add(defaultButton, cons);
    
    JButton deleteButton = new JButton(messages.getString("guiDeleteProfile"));
    deleteButton.setEnabled(!profileBox.getSelectedItem().equals(defaultOptions) 
        && !profileBox.getSelectedItem().equals(userOptions));
    deleteButton.addActionListener(e -> {
      List<String> saveProfiles = new ArrayList<>();
      saveProfiles.addAll(config.getDefinedProfiles());
      config.initOptions();
      try {
        config.loadConfiguration("");
      } catch (IOException e1) {
      }
      config.setCurrentProfile(null);
      config.addProfiles(saveProfiles);
      config.removeProfile((String)profileBox.getSelectedItem());
      restartShow = true;
      dialog.setVisible(false);
    });
    cons.gridx++;
    profilePanel.add(deleteButton, cons);
    cons.insets = new Insets(16, 0, 0, 8);
    cons.gridx = 0;
    cons.gridy++;
    profilePanel.add(new JLabel(addColonToMessageString("guiAddNewProfile")), cons);
    cons.insets = new Insets(6, 12, 0, 8);
    
    
    JButton addButton = new JButton(messages.getString("guiAddProfile") + "...");
    addButton.addActionListener(e -> {
      boolean noName = true;
      String profileName = "";
      while (noName) {
        profileName = JOptionPane.showInputDialog(dialog, messages.getString("guiAddNewProfile"), profileName);
        if (profileName == null || profileName.equals("")) {
          break;
        }
        profileName = profileName.replaceAll("[ \t=]", "_");
        noName = false;
        while(config.getDefinedProfiles().contains(profileName) || userOptions.equals(profileName)) {
          profileName += "_new";
          noName = true;
        }
      }
      if (profileName != null && !profileName.equals("")) {
        //  The configuration has to be saved and reloaded first to save previous changes
        try {
          config.saveConfiguration(null);
          config.initOptions();
          config.loadConfiguration(config.getCurrentProfile());
        } catch (IOException e1) {
        }
        config.addProfile(profileName);
        config.setCurrentProfile(profileName);
        profileChanged = false;
        profileBox.addItem(profileName);
        profileBox.setSelectedItem(profileName);
        deleteButton.setEnabled(true);
        renameButton.setEnabled(true);
        exportButton.setEnabled(true);
      }
    });
    cons.gridx = 0;
    cons.gridy++;
    profilePanel.add(addButton, cons);
    
    JButton importButton = new JButton(messages.getString("guiImportProfile") + "...");
    importButton.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      int choose = fileChooser.showOpenDialog(dialog);
      if (choose == JFileChooser.APPROVE_OPTION) {
        try {
          //  The configuration has to be saved and reloaded first to save previous changes
          config.saveConfiguration(null);
          config.initOptions();
          config.loadConfiguration(config.getCurrentProfile());
          List<String> saveProfiles = new ArrayList<>();
          saveProfiles.addAll(config.getDefinedProfiles());
          Configuration saveConfig = config.copy(config);
          config.initOptions();
          config.importProfile(fileChooser.getSelectedFile());
          String profileName = config.getCurrentProfile();
          if (profileName != null) {
            config.addProfiles(saveProfiles);
            profileName = profileName.replaceAll("[ \t=]", "_");
            while(config.getDefinedProfiles().contains(profileName) || userOptions.equals(profileName)) {
              profileName += "_new";
            }
            config.setCurrentProfile(profileName);
            config.addProfile(profileName);
            config.saveConfiguration(null);
          } else {
            config.restoreState(saveConfig);;
          }
          restartShow = true;
          dialog.setVisible(false);
        } catch (IOException e1) {
        }
      }
    });
    cons.gridx++;
    profilePanel.add(importButton, cons);
    return profilePanel;
  }
  
  private String addColonToMessageString(String message) {
    String str = messages.getString(message);
    if (!str.endsWith(":")) {
      return str + ":";
    }
    return str;
  }

  @NotNull
  private JPanel getMotherTonguePanel(GridBagConstraints cons) {
    JPanel motherTonguePanel = new JPanel();
    motherTonguePanel.add(new JLabel(messages.getString("guiMotherTongue")), cons);
    JComboBox<String> motherTongueBox = new JComboBox<>(getPossibleLanguages(true));
    if (config.getMotherTongue() != null) {
      motherTongueBox.setSelectedItem(config.getMotherTongue().getTranslatedName(messages));
    }
    motherTongueBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        Language motherTongue;
        if (motherTongueBox.getSelectedItem() instanceof String) {
          motherTongue = getLanguageForLocalizedName(motherTongueBox.getSelectedItem().toString());
        } else {
          motherTongue = (Language) motherTongueBox.getSelectedItem();
        }
        config.setMotherTongue(motherTongue);
      }
    });
    motherTonguePanel.add(motherTongueBox, cons);
    return motherTonguePanel;
  }
  
  private JPanel getNgramAndWord2VecPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints cons1 = new GridBagConstraints();
    cons1.insets = new Insets(0, 0, 0, 0);
    cons1.gridx = 0;
    cons1.gridy = 0;
    cons1.anchor = GridBagConstraints.WEST;
    cons1.fill = GridBagConstraints.NONE;
    cons1.weightx = 0.0f;
    addNgramPanel(cons1, panel);
    cons1.gridy++;
    addWord2VecPanel(cons1, panel);
    return panel;
  }

  private void addNgramPanel(GridBagConstraints cons, JPanel panel) {
    cons.gridx = 0;
    panel.add(new JLabel((messages.getString("guiNgramDir")) + "  "), cons);
    File dir = config.getNgramDirectory();
    int maxDirDisplayLength = 45;
    String buttonText = dir != null ? StringUtils.abbreviate(dir.getAbsolutePath(), maxDirDisplayLength) : messages.getString("guiNgramDirSelect");
    JButton ngramDirButton = new JButton(buttonText);
    ngramDirButton.addActionListener(e -> {
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
    });
    cons.gridx++;
    panel.add(ngramDirButton, cons);
    JButton helpButton = new JButton(messages.getString("guiNgramHelp"));
    helpButton.addActionListener(e -> Tools.openURL("https://dev.languagetool.org/finding-errors-using-n-gram-data"));
    cons.gridx++;
    panel.add(helpButton, cons);
  }

  private void addWord2VecPanel(GridBagConstraints cons, JPanel panel) {
    cons.gridx = 0;
    panel.add(new JLabel((messages.getString("guiWord2VecDir")) + "  "), cons);
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
    cons.gridx++;
    panel.add(word2vecDirButton, cons);
    JButton helpButton = new JButton(messages.getString("guiWord2VecHelp"));
    helpButton.addActionListener(e -> {
      Tools.openURL("https://github.com/gulp21/languagetool-neural-network");
    });
    cons.gridx++;
    panel.add(helpButton, cons);
  }

  private String[] getPossibleLanguages(boolean addNoSeletion) {
    List<String> languages = new ArrayList<>();
    if(addNoSeletion) {
      languages.add(NO_SELECTED_LANGUAGE);
    }
    for (Language lang : Languages.get()) {
      languages.add(lang.getTranslatedName(messages));
      languages.sort(null);
    }
    return languages.toArray(new String[0]);
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
      if(insideOffice && config.doRemoteCheck() && config.useOtherServer()) {
        String serverName = config.getServerUrl();
        if(serverName == null || (!serverName.startsWith("http://") && !serverName.startsWith("https://"))
            || serverName.endsWith("/") || serverName.endsWith("/v2")) {
          JOptionPane.showMessageDialog(dialog, Tools.getLabel(messages.getString("guiUseServerWarning1")) + "\n" + Tools.getLabel(messages.getString("guiUseServerWarning2")));
          if(serverName.endsWith("/")) {
            serverName = serverName.substring(0, serverName.length() - 1);
            config.setOtherServerUrl(serverName);
          }
          if(serverName.endsWith("/v2")) {
            serverName = serverName.substring(0, serverName.length() - 3);
            config.setOtherServerUrl(serverName);
          }
          restartShow = true;
          dialog.setVisible(false);
          return;
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
          return r1.getDescription() != null && r2.getDescription() != null ? r1.getDescription().compareToIgnoreCase(r2.getDescription()) : 0;
        }
        return res;
      }
      return r1.getDescription() != null && r2.getDescription() != null ? r1.getDescription().compareToIgnoreCase(r2.getDescription()) : 0;
    }

  }
  
  /**
   * Update display of rules tree
   */
  private void updateRulesTrees(List<Rule> rules) {
    String[] specialTabNames = config.getSpecialTabNames();
    int numConfigTrees = 2 + specialTabNames.length;
    for (int i = 0; i < numConfigTrees; i++) {
      if(i == 0) {
        rootNode[i] = createTree(rules, false, null, rootNode[i]);   //  grammar options
      } else if(i ==1 ) {
        rootNode[i] = createTree(rules, true, null, rootNode[i]);    //  Style options
      } else {
        rootNode[i] = createTree(rules, true, specialTabNames[i - 2], rootNode[i]);    //  Special tab options
      }
      configTree[i].setModel(getTreeModel(rootNode[i], rules));
    }
  }
  
  /**
   * Update display of profile rules
   */
  private void updateProfileRules(List<Rule> rules) {
    getChangedRulesPanel(rules, false, disabledRulesPanel);
    getChangedRulesPanel(rules, true , enabledRulesPanel);
  }

  
  /** Panel to select disabled default rules
   * @since 5.4
   */
  private JPanel getChangedRulesPanel(List<Rule> rules, boolean enabledRules, JPanel panel) {
    if (panel == null) {
      panel = new JPanel();
    } else {
      panel.removeAll();
    }
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createLineBorder(Color.black));
    panel.setLayout(new GridBagLayout());
    GridBagConstraints cons = new GridBagConstraints();
    cons.gridx = 0;
    cons.gridy = 0;
    cons.weightx = 1.0f;
    cons.anchor = GridBagConstraints.WEST;
    cons.fill = GridBagConstraints.NONE;
    cons.insets = new Insets(4, 3, 0, 4);
    
    Set<String> changedRuleIds;
    if (enabledRules) {
      changedRuleIds = config.getEnabledRuleIds();
    } else {
      changedRuleIds = config.getDisabledRuleIds();
    }
    
    if (changedRuleIds != null) {
      List<JCheckBox> ruleCheckboxes = new ArrayList<>();
      for (String ruleId : changedRuleIds) {
        String ruleDescription = null;
        for (Rule rule : rules) {
          if (rule.getId().equals(ruleId)) {
            ruleDescription = rule.getDescription();
            break;
          }
        }
        if (ruleDescription != null) {
          JCheckBox ruleCheckbox = new JCheckBox(ruleDescription);
          ruleCheckbox.setName(ruleId);
          ruleCheckboxes.add(ruleCheckbox);
          ruleCheckbox.setSelected(enabledRules);
          panel.add(ruleCheckbox, cons);
          ruleCheckbox.addActionListener(e -> {
            if (ruleCheckbox.isSelected()) {
              config.getEnabledRuleIds().add(ruleCheckbox.getName());
              config.getDisabledRuleIds().remove(ruleCheckbox.getName());
              updateRulesTrees(rules);
            } else {
              config.getEnabledRuleIds().remove(ruleCheckbox.getName());
              config.getDisabledRuleIds().add(ruleCheckbox.getName());
              updateRulesTrees(rules);
            }
          });
          cons.gridx = 0;
          cons.gridy++;
        }
      }
    }
    return panel;
  }
  
  private String[] getUnderlineTypes() {
    String[] types = {
      messages.getString("guiUTypeWave"),
      messages.getString("guiUTypeBoldWave"),
      messages.getString("guiUTypeBold"),
      messages.getString("guiUTypeDash")};
    return types;
  }

  private int getUnderlineType(String category, String ruleId) {
    short nType = config.getUnderlineType(category, ruleId);
    if (nType == Configuration.UNDERLINE_BOLDWAVE) {
      return 1;
    } else if (nType == Configuration.UNDERLINE_BOLD) {
      return 2;
    } else if (nType == Configuration.UNDERLINE_DASH) {
      return 3;
    } else {
      return 0;
    }
  }

  private void setUnderlineType(int index, String category, String ruleId) {
    if (ruleId == null) {
      if (index == 1) {
        config.setUnderlineType(category, Configuration.UNDERLINE_BOLDWAVE);
      } else if (index == 2) {
        config.setUnderlineType(category, Configuration.UNDERLINE_BOLD);
      } else if (index == 3) {
        config.setUnderlineType(category, Configuration.UNDERLINE_DASH);
      } else {
        config.setDefaultUnderlineType(category);
      }
    } else {
      if (index == 1) {
        config.setUnderlineRuleType(ruleId, Configuration.UNDERLINE_BOLDWAVE);
      } else if (index == 2) {
        config.setUnderlineRuleType(ruleId, Configuration.UNDERLINE_BOLD);
      } else if (index == 3) {
        config.setUnderlineRuleType(ruleId, Configuration.UNDERLINE_DASH);
      } else {
        config.setDefaultUnderlineRuleType(ruleId);
      }
    }
  }

  /**  Panel to choose underline Colors
   *   @since 4.2
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

    List<String> categories = new ArrayList<>();
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
    List<JLabel> categoryLabel = new ArrayList<>();
    List<JLabel> underlineLabel = new ArrayList<>();
    List<JButton> changeButton = new ArrayList<>();
    List<JButton> defaultButton = new ArrayList<>();
    List<JComboBox<String>> underlineType  = new ArrayList<>();
    for(int nCat = 0; nCat < categories.size(); nCat++) {
      categoryLabel.add(new JLabel(categories.get(nCat) + " "));
      underlineLabel.add(new JLabel(" \u2588\u2588\u2588 "));  // \u2587 is smaller
      underlineLabel.get(nCat).setForeground(config.getUnderlineColor(categories.get(nCat), null));
      underlineLabel.get(nCat).setBackground(config.getUnderlineColor(categories.get(nCat), null));
      JLabel uLabel = underlineLabel.get(nCat);
      String cLabel = categories.get(nCat);
      panel.add(categoryLabel.get(nCat), cons);

      underlineType.add(new JComboBox<>(getUnderlineTypes()));
      JComboBox<String> uLineType = underlineType.get(nCat);
      if(insideOffice) {
        uLineType.setSelectedIndex(getUnderlineType(cLabel, null));
        uLineType.addItemListener(e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            setUnderlineType(uLineType.getSelectedIndex(), cLabel, null);
          }
        });
        cons.gridx++;
        panel.add(uLineType, cons);
      }
      cons.gridx++;
      panel.add(underlineLabel.get(nCat), cons);

      changeButton.add(new JButton(messages.getString("guiUColorChange")));
      changeButton.get(nCat).addActionListener(e -> {
        Color oldColor = uLabel.getForeground();
        Color newColor = JColorChooser.showDialog( null, messages.getString("guiUColorDialogHeader"), oldColor);
        if(newColor != null && newColor != oldColor) {
          uLabel.setForeground(newColor);
          config.setUnderlineColor(cLabel, newColor);
        }
      });
      cons.gridx++;
      panel.add(changeButton.get(nCat), cons);
  
      defaultButton.add(new JButton(messages.getString("guiUColorDefault")));
      defaultButton.get(nCat).addActionListener(e -> {
        config.setDefaultUnderlineColor(cLabel);
        uLabel.setForeground(config.getUnderlineColor(cLabel, null));
        if(insideOffice) {
          config.setDefaultUnderlineType(cLabel);
          uLineType.setSelectedIndex(getUnderlineType(cLabel, null));
        }
      });
      cons.gridx++;
      panel.add(defaultButton.get(nCat), cons);
      cons.gridx = 0;
      cons.gridy++;
    }
    
    return panel;
  }

  /**  Panel to choose underline Colors
   *   and rule options (if exists)
   *   @since 5.3
   */
  @NotNull
  private JPanel getRuleOptionsPanel(int num) {
    category = "";
    rule = null;
    JPanel ruleOptionsPanel = new JPanel();
    ruleOptionsPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons0 = new GridBagConstraints();
    cons0.gridx = 0;
    cons0.gridy = 0;
    cons0.fill = GridBagConstraints.NONE;
    cons0.anchor = GridBagConstraints.NORTHWEST;
    cons0.weightx = 2.0f;
    cons0.weighty = 0.0f;
    cons0.insets = new Insets(3, 8, 3, 0);
    ruleOptionsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
    
    //  Color Panel
    JPanel colorPanel = new JPanel();
    colorPanel.setLayout(null);
    colorPanel.setBounds(0, 0, 120, 10);

    colorPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons1 = new GridBagConstraints();
    cons1.insets = new Insets(0, 0, 0, 0);
    cons1.gridx = 0;
    cons1.gridy = 0;
    cons1.weightx = 0.0f;
    cons1.fill = GridBagConstraints.NONE;
    cons1.anchor = GridBagConstraints.NORTHWEST;

    JLabel underlineStyle = new JLabel(messages.getString("guiUColorStyleLabel") + " ");
    colorPanel.add(underlineStyle);

    JLabel underlineLabel = new JLabel(" \u2588\u2588\u2588 ");  // \u2587 is smaller

    JComboBox<String> underlineType = new JComboBox<>(getUnderlineTypes());
    if(insideOffice) {
      underlineType.setSelectedIndex(getUnderlineType(category, (rule == null ? null : rule.getId())));
      underlineType.addItemListener(e -> {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          setUnderlineType(underlineType.getSelectedIndex(), category, (rule == null ? null : rule.getId()));
        }
      });
      cons1.gridx++;
      colorPanel.add(underlineType);
    }
    cons1.gridx++;
    colorPanel.add(underlineLabel);

    JButton changeButton = new JButton(messages.getString("guiUColorChange"));
    changeButton.addActionListener(e -> {
      Color oldColor = underlineLabel.getForeground();
      Color newColor = JColorChooser.showDialog( null, messages.getString("guiUColorDialogHeader"), oldColor);
      if(newColor != null && newColor != oldColor) {
        underlineLabel.setForeground(newColor);
        if (rule == null) {
          config.setUnderlineColor(category, newColor);
        } else {
          config.setUnderlineRuleColor(rule.getId(), newColor);
        }
      }
    });
    cons1.gridx++;
    colorPanel.add(changeButton);
  
    JButton defaultButton = new JButton(messages.getString("guiUColorDefault"));
    defaultButton.addActionListener(e -> {
      String ruleId = (rule == null ? null : rule.getId());
      if (rule == null) {
        config.setDefaultUnderlineColor(category);
      } else {
        config.setDefaultUnderlineRuleColor(ruleId);
      }
      underlineLabel.setForeground(config.getUnderlineColor(category, ruleId));
      if(insideOffice) {
        if ( rule == null) {
          config.setDefaultUnderlineType(category);
        } else {
          config.setDefaultUnderlineRuleType(ruleId);
        }
        underlineType.setSelectedIndex(getUnderlineType(category, ruleId));
      }
    });
    cons1.gridx++;
    colorPanel.add(defaultButton);
    colorPanel.setVisible(false);
    // End of Color Panel
    
    // Start of special option panel
    JPanel specialOptionPanel = new JPanel();
    specialOptionPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons2 = new GridBagConstraints();
    cons2.gridx = 0;
    cons2.gridy = 0;
    cons2.weightx = 2.0f;
    cons2.anchor = GridBagConstraints.WEST;
    
    JLabel ruleLabel = new JLabel("");
    specialOptionPanel.add(ruleLabel, cons2);

    cons2.gridx++;
    JTextField ruleValueField = new JTextField("   ", 3);
    ruleValueField.setMinimumSize(new Dimension(50, 28));  // without this the box is just a few pixels small, but why?
    specialOptionPanel.add(ruleValueField, cons2);

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
          if (rule != null) {
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
          }
        } catch (Exception ex) {
          ruleValueField.setForeground(Color.RED);
        }
      }
    });
    specialOptionPanel.setVisible(false);
    // End of special option panel
    
    ruleOptionsPanel.add(colorPanel, cons0);
    cons0.gridx = 0;
    cons0.gridy = 1;
    ruleOptionsPanel.add(specialOptionPanel, cons0);
    ruleOptionsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
    
    configTree[num].addTreeSelectionListener(e -> {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)
          configTree[num].getLastSelectedPathComponent();
      if (node != null) {
        if (node instanceof RuleNode) {
          RuleNode o = (RuleNode) node;
          rule = o.getRule();
          category = rule.getCategory().getName();
          String ruleId = rule.getId();
          underlineLabel.setForeground(config.getUnderlineColor(category, ruleId));
          underlineLabel.setBackground(config.getUnderlineColor(category, ruleId));
          if(insideOffice) {
            underlineType.setSelectedIndex(getUnderlineType(category, ruleId));
          }
          colorPanel.setVisible(true);
          if (rule.hasConfigurableValue()) {
            ruleLabel.setText(rule.getConfigureText() + " ");
            int value = config.getConfigurableValue(rule.getId());
            if (value < 0) {
              value = rule.getDefaultValue();
            }
            ruleValueField.setText(Integer.toString(value));
            specialOptionPanel.setVisible(true);
          } else {
            specialOptionPanel.setVisible(false);
          }
        } else if (node instanceof CategoryNode) {
          CategoryNode o = (CategoryNode) node;
          category = o.getCategory().getName();
          underlineLabel.setForeground(config.getUnderlineColor(category, null));
          underlineLabel.setBackground(config.getUnderlineColor(category, null));
          if(insideOffice) {
            underlineType.setSelectedIndex(getUnderlineType(category, null));
          }
          colorPanel.setVisible(true);
          specialOptionPanel.setVisible(false);
          rule = null;
        }
      }
    });
    return ruleOptionsPanel;
  }

}
