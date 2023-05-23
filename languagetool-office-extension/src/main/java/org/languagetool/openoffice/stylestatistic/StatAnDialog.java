/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.openoffice.stylestatistic;

import java.awt.AWTError;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.openoffice.DocumentCache;
import org.languagetool.openoffice.DocumentCache.TextParagraph;
import org.languagetool.openoffice.MessageHandler;
import org.languagetool.openoffice.ViewCursorTools;
import org.languagetool.openoffice.stylestatistic.StatAnCache.Heading;
import org.languagetool.openoffice.stylestatistic.StatAnCache.Paragraph;
import org.languagetool.openoffice.SingleDocument;
import org.languagetool.rules.AbstractStatisticSentenceStyleRule;
import org.languagetool.rules.AbstractStatisticStyleRule;
import org.languagetool.rules.ReadabilityRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.TextLevelRule;

import com.sun.star.lang.XComponent;

/**
 * Statistical Analyzes Dialog 
 * @since 6.2
 * @author Fred Kruse
 */
public class StatAnDialog extends Thread  {
  
  private final static ResourceBundle MESSAGES = JLanguageTool.getMessageBundle();
  private final static boolean debugMode = false;
  private final static String dialogName = MESSAGES.getString("loStatisticalAnalysis");
  private final static int MIN_DIALOG_WIDTH = 600;
  private final static int MIN_DIALOG_HEIGHT = 380;
  private final static int dialogWidth = 640;
  private final static int dialogHeight = 600;
  private final static int MIN_OPTION_WIDTH = 260;
  private final static int MIN_OPTION_HEIGHT = 170;
  
  private JDialog dialog;
  private Container contentPane;
  private JPanel chapterPanel;
  private JPanel leftPanel;
  private JPanel rightPanel;
  private JPanel mainPanel;
  private JScrollPane subChapterPane;
  private JLabel optionLabel;
  private JCheckBox withoutDirectSpeech;
  private JLabel stepLabel1;
  private JTextField stepField;
  private JLabel stepLabel2;
  private JButton defaultButton;
  private JButton setButton;
  private JComboBox<String> function;
  private JList<String> usedWords;
  private int from = 0;
  private int to = 1;
  private int hierarchy = 1;
  
  private Chapter chapter = null;

  private static final List<TextLevelRule> rules = new ArrayList<>();

  private StatAnCache cache = null;
  private StatAnConfiguration config = null;
  private XComponent lastComponent = null;
  private TextLevelRule selectedRule;
  private LevelRule levelRule;
  private UsedWordRule usedWordRule;
  
  private XComponent xComponent;
  private SingleDocument document;
  private int method = 0;
  private boolean isLevelRule = true;
//  private List<WordFrequency> mostUsed;
  
  public StatAnDialog(SingleDocument document) {
    xComponent = document.getXComponent();
    this.document = document;
    rules.clear();
    Language lang = document.getLanguage();
    try {
      Map<String, Integer> ruleValues = new HashMap<>();
      for (Rule rule : lang.getRelevantRules(JLanguageTool.getMessageBundle(), null, lang, null)) {
        if (rule instanceof AbstractStatisticSentenceStyleRule || rule instanceof AbstractStatisticStyleRule ||
            rule instanceof ReadabilityRule) {
          ruleValues.put(rule.getId(), 0);
        }
      }
      UserConfig userConfig = new UserConfig(ruleValues);
      for (Rule rule : lang.getRelevantRules(JLanguageTool.getMessageBundle(), userConfig, lang, null)) {
        if (rule instanceof AbstractStatisticSentenceStyleRule || rule instanceof AbstractStatisticStyleRule ||
            (rule instanceof ReadabilityRule && !hasReadabilityRule())) {
          rules.add((TextLevelRule)rule);
        }
      }
    } catch (IOException e) {
      MessageHandler.showError(e);
    }
  }
  
  private boolean hasReadabilityRule() {
    for (Rule rule : rules) {
      if (rule instanceof ReadabilityRule) {
        return true;
      }
    }
    return false;
  }
  
  private void runDialog() {
    dialog = new JDialog();
    dialog.setName(dialogName);
    dialog.setTitle(dialogName);
    dialog.setMinimumSize(new Dimension(MIN_DIALOG_WIDTH, MIN_DIALOG_HEIGHT));
    dialog.setSize(new Dimension(dialogWidth, dialogHeight));

    //  initiate
    try {
      if (cache == null || lastComponent == null || !lastComponent.equals(xComponent)) {
        lastComponent = xComponent;
          refreshCache(document);
      }
      config = new StatAnConfiguration(rules);
      selectedRule = rules.get(method);
      isLevelRule = isLevelRule(selectedRule);
      if (isLevelRule) {
        levelRule = new LevelRule(selectedRule, cache);
      } else {
        usedWordRule = new UsedWordRule(selectedRule, cache);
      }
      configRule();
      if (isLevelRule) {
        levelRule.generateBasicNumbers(cache);
      } else {
        //  TODO: usedWordRule.generateBasicNumbers(cache);
      }
      if (debugMode) {
        MessageHandler.printToLogFile("Init done");
      }
    } catch (Throwable e1) {
      MessageHandler.showError(e1);
    }

    
    // main pane
    mainPanel = new JPanel();
    mainPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons2 = new GridBagConstraints();
    cons2.insets = new Insets(2, 4, 2, 4);
    cons2.gridx = 0;
    cons2.gridy = 0;
    cons2.fill = GridBagConstraints.BOTH;
    cons2.anchor = GridBagConstraints.NORTHWEST;
    cons2.weightx = 10.0f;
    cons2.weighty = 10.0f;
    leftPanel = new JPanel();
    rightPanel = new JPanel();
    if(isLevelRule) {
      levelRule = new LevelRule(selectedRule, cache);
      configRule();
      levelRule.generateBasicNumbers(cache);
      setLeftLevelRulePanel();
      setRightLevelRulePanel();
    } else {
      usedWordRule = new UsedWordRule(selectedRule, cache);
      configRule();
//    TODO: usedWordRule.generateBasicNumbers(cache);
      setLeftUsedWordRulePanel();
      setRightUsedWordRulePanel();
    }
    mainPanel.add(leftPanel, cons2);
    cons2.gridx++;
    cons2.fill = GridBagConstraints.NONE;
    cons2.anchor = GridBagConstraints.NORTHEAST;
    cons2.weightx = 0.0f;
    cons2.weighty = 0.0f;
    rightPanel.setMinimumSize(new Dimension(MIN_OPTION_WIDTH, MIN_OPTION_HEIGHT));
    mainPanel.add(rightPanel, cons2);
    if (debugMode) {
      MessageHandler.printToLogFile("Main panel defined");
    }

    // content pane
    contentPane = dialog.getContentPane();
    contentPane.setLayout(new GridBagLayout());
    GridBagConstraints cons = new GridBagConstraints();
    cons.gridx = 0;
    cons.gridy = 0;
    cons.weightx = 0.0f;
    cons.weighty = 0.0f;
    cons.fill = GridBagConstraints.NONE;
    cons.anchor = GridBagConstraints.NORTHWEST;
    cons.insets = new Insets(2, 6, 2, 6);
    function = new JComboBox<String>(getAllRuleNames());
    function.setSelectedIndex(method);
    function.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        String selectedRuleName = (String) function.getSelectedItem();
        if (!selectedRule.getDescription().equals(selectedRuleName)) {
          selectedRule = getRuleByName(selectedRuleName);
          method = this.getMethodByRule(selectedRuleName);
          isLevelRule = isLevelRule(selectedRule);
          try {
            if(isLevelRule) {
              levelRule = new LevelRule(selectedRule, cache);
              configRule();
              levelRule.generateBasicNumbers(cache);
              setLeftLevelRulePanel();
              setRightLevelRulePanel();
            } else {
              usedWordRule = new UsedWordRule(selectedRule, cache);
              configRule();
//            TODO: usedWordRule.generateBasicNumbers(cache);
              setLeftUsedWordRulePanel();
              setRightUsedWordRulePanel();
            }
            dialog.pack();
          } catch (Throwable e1) {
            MessageHandler.showError(e1);
          }
        }
      }
    });
    contentPane.add(function, cons);
    cons.gridy++;
    cons.weightx = 10.0f;
    cons.weighty = 10.0f;
    cons.fill = GridBagConstraints.BOTH;
    cons.anchor = GridBagConstraints.NORTHWEST;
    cons.insets = new Insets(2, 6, 2, 6);
    contentPane.add(mainPanel, cons);
    cons.gridy++;
    cons.fill = GridBagConstraints.NONE;
    cons.anchor = GridBagConstraints.SOUTHEAST;
    cons.weightx = 0.0f;
    cons.weighty = 0.0f;
    cons.gridy++;
    JButton closeButton = new JButton(MESSAGES.getString("loStatisticalAnalysisCloseButton"));
    closeButton.addActionListener(e -> {
      dialog.setVisible(false);
    });
    contentPane.add(closeButton, cons);
    if (debugMode) {
      MessageHandler.printToLogFile("Content pane defined");
    }
    
    dialog.pack();
    // center on screen:
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = dialog.getSize();
    dialog.setLocation(screenSize.width / 2 - frameSize.width / 2,
        screenSize.height / 2 - frameSize.height / 2);
    dialog.setLocationByPlatform(true);
    if (debugMode) {
      MessageHandler.printToLogFile("Dialog set");
    }
  }
  
  private void setLeftLevelRulePanel() {
    if (chapter == null && LevelRule.hasStatisticalOptions(selectedRule)) {
      levelRule.setWithDirectSpeach(!config.isWithoutDirectSpeech(selectedRule), cache);
      levelRule.setCurrentStep(config.getLevelStep(selectedRule));
    }
    //  Define left panel
    leftPanel.removeAll();
    leftPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons11 = new GridBagConstraints();
    cons11.insets = new Insets(14, 6, 2, 6);
    cons11.gridx = 0;
    cons11.gridy = 0;
    cons11.anchor = GridBagConstraints.NORTHWEST;
    cons11.fill = GridBagConstraints.HORIZONTAL;
    cons11.weightx = 0.0;
    cons11.weighty = 0.0;
    leftPanel.add(new JLabel(MESSAGES.getString("loStatisticalAnalysisChapterLabel") + ":"), cons11);
    cons11.gridy++;
    cons11.weightx = 0.0;
    cons11.weighty = 0.0;
    cons11.insets = new Insets(2, 6, 2, 6);
    cons11.anchor = GridBagConstraints.NORTHWEST;
    cons11.fill = GridBagConstraints.HORIZONTAL;
    chapterPanel = new JPanel();
    try {
      setChapterPanel(chapter);
    } catch (Throwable e1) {
      MessageHandler.showError(e1);
    }
    leftPanel.add(chapterPanel, cons11);
    cons11.gridy++;
    cons11.weightx = 0.0f;
    cons11.weighty = 0.0;
    cons11.insets = new Insets(14, 6, 2, 6);
    JLabel subChapterLabel = new JLabel(MESSAGES.getString("loStatisticalAnalysisSubchapterLabel") + ":");
    leftPanel.add(subChapterLabel, cons11);
    cons11.gridy++;
    cons11.weightx = 1.0f;
    cons11.weighty = 1.0f;
    cons11.anchor = GridBagConstraints.NORTHWEST;
    cons11.fill = GridBagConstraints.BOTH;
    cons11.insets = new Insets(2, 6, 2, 6);
    subChapterPane = new JScrollPane();
    subChapterPane.setMinimumSize(new Dimension(0, 30)); 
    try {
      subChapterPane.setViewportView(getSubChapterPanel(0, cache.size(), 1, null));
    } catch (Throwable e1) {
      MessageHandler.showError(e1);
    }
    leftPanel.add(subChapterPane, cons11);
    if (debugMode) {
      MessageHandler.printToLogFile("Left panel defined");
    }
    leftPanel.validate();
  }
  
  private void setLeftUsedWordRulePanel() {
    usedWordRule.setWithDirectSpeach(!config.isWithoutDirectSpeech(selectedRule));
    usedWordRule.setListExcludedWords(config.getExcludedWords(selectedRule));
    //  Define left panel
    leftPanel.removeAll();
    leftPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons11 = new GridBagConstraints();
    cons11.insets = new Insets(14, 6, 2, 6);
    cons11.gridx = 0;
    cons11.gridy = 0;
    cons11.anchor = GridBagConstraints.NORTHWEST;
    cons11.fill = GridBagConstraints.HORIZONTAL;
    cons11.weightx = 0.0;
    cons11.weighty = 0.0;
    leftPanel.add(new JLabel(MESSAGES.getString("loStatisticalAnalysisMostUsedWords") + ":"), cons11);
    usedWords = new JList<String>();
    usedWords.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    String[] mostUsedWords = null;
    try {
      mostUsedWords = getMostUsedWords(usedWordRule);
    } catch (Throwable e1) {
      MessageHandler.showError(e1);
    }
    usedWords.setListData(mostUsedWords);
    if (mostUsedWords != null && mostUsedWords.length > 0) {
      usedWords.setSelectedIndex(0);
    }
    cons11.gridy++;
    cons11.weightx = 1.0f;
    cons11.weighty = 1.0f;
    cons11.anchor = GridBagConstraints.NORTHWEST;
    cons11.fill = GridBagConstraints.BOTH;
    cons11.insets = new Insets(2, 6, 2, 6);
    JScrollPane usedWordsPane = new JScrollPane(usedWords);
    usedWordsPane.setMinimumSize(new Dimension(0, 30)); 
    leftPanel.add(usedWordsPane, cons11);
    leftPanel.validate();
  }
  
  private void setRightLevelRulePanel() {
    //  Define right panel
    rightPanel.removeAll();
    rightPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons21 = new GridBagConstraints();
    //  Option panel
    cons21.insets = new Insets(2, 4, 2, 4);
    cons21.gridx = 0;
    cons21.gridy = 0;
    cons21.fill = GridBagConstraints.NONE;
    cons21.anchor = GridBagConstraints.NORTHWEST;
    cons21.weightx = 0.0;
    cons21.weighty = 0.0;
    optionLabel = new JLabel (MESSAGES.getString("loStatisticalAnalysisOptionsLabel") + ":");
    rightPanel.add(optionLabel, cons21);
    withoutDirectSpeech = new JCheckBox(MESSAGES.getString("loStatisticalAnalysisWithoutDirectSpreech"));
    if (config == null) {
      MessageHandler.showMessage("config == null");
    }
    // Level step panel
    
    JPanel stepPanel = new JPanel();
    stepPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons22 = new GridBagConstraints();
    cons22.insets = new Insets(0, 2, 0, 2);
    cons22.gridx = 0;
    cons22.gridy = 0;
    cons22.fill = GridBagConstraints.NONE;
    cons22.anchor = GridBagConstraints.NORTHWEST;
    cons22.weightx = 0.0;
    cons22.weighty = 0.0;
    stepLabel1 = new JLabel(MESSAGES.getString("loStatisticalAnalysisLevels") + ": ");
    stepField = new JTextField("", 3);
    stepLabel2 = new JLabel();
    setButton = new JButton(MESSAGES.getString("loStatisticalAnalysisSetButton"));
    stepPanel.add(stepLabel1, cons22);
    cons22.gridx++;
    stepPanel.add(stepField, cons22);
    cons22.gridx++;
    stepPanel.add(stepLabel2, cons22);
    // ***
    cons21.gridy++;
    rightPanel.add(withoutDirectSpeech, cons21);
    cons21.gridx++;
    defaultButton = new JButton(MESSAGES.getString("loStatisticalAnalysisDefaultButton"));
    rightPanel.add(defaultButton, cons21);
    cons21.gridx = 0;
    cons21.gridy++;
    rightPanel.add(stepPanel, cons21);
    cons21.gridx++;
    rightPanel.add(setButton, cons21);
    setLevelRuleOptions();
    if (debugMode) {
      MessageHandler.printToLogFile("Right panel defined");
    }
    rightPanel.validate();
  }
    
  private void setLevelRuleOptions() {
    if (LevelRule.isLevelRule(selectedRule) && LevelRule.hasStatisticalOptions(selectedRule)) {
      defaultButton.addActionListener(e -> {
        if (levelRule.getDefaultDirectSpeach() == withoutDirectSpeech.isSelected() 
            || levelRule.getDefaultStep() != config.getLevelStep(selectedRule)) {
          withoutDirectSpeech.setSelected(!levelRule.getDefaultDirectSpeach());
          config.setWithoutDirectSpeech(selectedRule, !levelRule.getDefaultDirectSpeach());
          config.setLevelStep(selectedRule, levelRule.getDefaultStep());
          stepField.setText(Integer.toString(levelRule.getDefaultStep()));
          try {
            config.saveConfiguration();
            levelRule.setWithDirectSpeach(!levelRule.getDefaultDirectSpeach(), cache);
            levelRule.setCurrentStep(levelRule.getDefaultStep());
            runLevelSubDialog(null);
          } catch (Throwable t) {
            MessageHandler.showError(t);
          }
        }
      });
      setButton.addActionListener(e -> {
        int levelStep = Integer.parseInt(stepField.getText().trim());
        if (levelStep > 0 && levelStep < 100) {
          config.setLevelStep(selectedRule, levelStep);
          try {
            config.saveConfiguration();
            levelRule.setCurrentStep(levelStep);
            runLevelSubDialog(null);
          } catch (Throwable t) {
            MessageHandler.showError(t);
          }
        } else {
          stepField.setText(Integer.toString(config.getLevelStep(selectedRule)));
        }
      });
      withoutDirectSpeech.setSelected(config.isWithoutDirectSpeech(selectedRule));
      withoutDirectSpeech.addActionListener(e -> {
        config.setWithoutDirectSpeech(selectedRule, withoutDirectSpeech.isSelected());
        try {
          config.saveConfiguration();
          levelRule.setWithDirectSpeach(!withoutDirectSpeech.isSelected(), cache);
          runLevelSubDialog(null);
        } catch (Throwable t) {
          MessageHandler.showError(t);
        }
      });
      int nStep = 0;
      String sStep = "%";
      if (LevelRule.hasStatisticalOptions(selectedRule)) {
        nStep = config.getLevelStep(selectedRule);
        sStep = levelRule.getUnitString();
      }
      stepField.setText(Integer.toString(nStep));
      stepLabel2.setText(sStep);
      defaultButton.setEnabled(true);
      stepLabel1.setEnabled(true);
      stepField.setEnabled(true);
      stepLabel2.setEnabled(true);
      setButton.setEnabled(true);
      withoutDirectSpeech.setEnabled(true);
      optionLabel.setEnabled(true);
    } else {
      defaultButton.setEnabled(false);
      stepLabel1.setEnabled(false);
      stepField.setEnabled(false);
      stepLabel2.setEnabled(false);
      setButton.setEnabled(false);
      withoutDirectSpeech.setEnabled(false);
      optionLabel.setEnabled(false);
    }
     
  }
  
  private void setRightUsedWordRulePanel() {
    //  Define right panel
    rightPanel.removeAll();
    rightPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons21 = new GridBagConstraints();
    //  Option panel
    cons21.insets = new Insets(2, 4, 2, 4);
    cons21.gridx = 0;
    cons21.gridy = 0;
    cons21.fill = GridBagConstraints.NONE;
    cons21.anchor = GridBagConstraints.NORTHWEST;
    cons21.weightx = 0.0;
    cons21.weighty = 0.0;
    optionLabel = new JLabel(MESSAGES.getString("loStatisticalAnalysisOptionsLabel") + ":");
    rightPanel.add(optionLabel, cons21);
    //  direct speech panel
    JPanel directSpeechPanel = new JPanel();
    rightPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons23 = new GridBagConstraints();
    cons23.insets = new Insets(0, 0, 0, 4);
    cons23.gridx = 0;
    cons23.gridy = 0;
    cons23.fill = GridBagConstraints.NONE;
    cons23.anchor = GridBagConstraints.NORTHWEST;
    cons23.weightx = 0.0;
    cons23.weighty = 0.0;
    withoutDirectSpeech = new JCheckBox(MESSAGES.getString("loStatisticalAnalysisWithoutDirectSpreech"));
    if (config == null) {
      MessageHandler.showMessage("config == null");
    }
    directSpeechPanel.add(withoutDirectSpeech, cons23);
    cons23.gridx++;
    defaultButton = new JButton(MESSAGES.getString("loStatisticalAnalysisDefaultButton"));
    directSpeechPanel.add(defaultButton, cons23);
    cons21.gridy++;
    rightPanel.add(directSpeechPanel, cons21);
    cons21.gridx = 0;
    cons21.gridy++;
    JButton ignore = new JButton(MESSAGES.getString("loStatisticalAnalysisIgnoreWordButton"));
    JButton removeAllIgnored = new JButton(MESSAGES.getString("loStatisticalAnalysisResetIgnoredWordsButton"));
    removeAllIgnored.setEnabled(config.getExcludedWords(selectedRule).size() > 0);
    rightPanel.add(removeAllIgnored, cons21);
    cons21.gridy++;
    rightPanel.add(ignore, cons21);

    ignore.addActionListener(e -> {
      if (!usedWords.isSelectionEmpty()) {
        try {
          String word = getMostUsedWord(usedWords.getSelectedIndex());
          config.addExcludedWord(selectedRule, word);
          config.saveConfiguration();
          usedWordRule.setListExcludedWords(config.getExcludedWords(selectedRule));
          usedWords.setListData(getMostUsedWords(usedWordRule));
          usedWords.setSelectedIndex(0);
          removeAllIgnored.setEnabled(true);
        } catch (Throwable t) {
          MessageHandler.showError(t);
        }
      }
    });
    removeAllIgnored.addActionListener(e -> {
      try {
        config.removeAllExcludedWords(selectedRule);
        config.saveConfiguration();
        usedWordRule.setListExcludedWords(config.getExcludedWords(selectedRule));
        usedWords.setListData(getMostUsedWords(usedWordRule));
        usedWords.setSelectedIndex(0);
        removeAllIgnored.setEnabled(false);
      } catch (Throwable t) {
        MessageHandler.showError(t);
      }
    });
    withoutDirectSpeech.setSelected(config.isWithoutDirectSpeech(selectedRule));
    withoutDirectSpeech.addActionListener(e -> {
      config.setWithoutDirectSpeech(selectedRule, withoutDirectSpeech.isSelected());
      try {
        config.saveConfiguration();
        usedWordRule.setWithDirectSpeach(!withoutDirectSpeech.isSelected());
        usedWords.setListData(getMostUsedWords(usedWordRule));
        usedWords.setSelectedIndex(0);
      } catch (Throwable t) {
        MessageHandler.showError(t);
      }
    });
    defaultButton.addActionListener(e -> {
      if (usedWordRule.getDefaultDirectSpeach() == withoutDirectSpeech.isSelected()) {
        withoutDirectSpeech.setSelected(!usedWordRule.getDefaultDirectSpeach());
        config.setWithoutDirectSpeech(selectedRule, !usedWordRule.getDefaultDirectSpeach());
        try {
          config.saveConfiguration();
          usedWordRule.setWithDirectSpeach(usedWordRule.getDefaultDirectSpeach());
          usedWords.setListData(getMostUsedWords(usedWordRule));
          usedWords.setSelectedIndex(0);
        } catch (Throwable t) {
          MessageHandler.showError(t);
        }
      }
    });
  
    if (debugMode) {
      MessageHandler.printToLogFile("Right panel defined");
    }
    rightPanel.validate();
  }
    
  /**
   * opens the LT check dialog for spell and grammar check
   */
  @Override
  public void run() {
    try {
      runDialog();
      dialog.setVisible(true);
      if (debugMode) {
        MessageHandler.printToLogFile("Dialog visible set");
      }
    } catch (Throwable e) {
      MessageHandler.showError(e);
    }
  }
  
  private void configRule() {
    if (debugMode) {
      MessageHandler.printToLogFile("New configuration set");
    }
    if (UsedWordRule.isUsedWordRule(selectedRule)) {
      usedWordRule.setWithDirectSpeach(!config.isWithoutDirectSpeech(selectedRule));
      usedWordRule.setListExcludedWords(config.getExcludedWords(selectedRule));
    } else if (LevelRule.isLevelRule(selectedRule) && LevelRule.hasStatisticalOptions(selectedRule)) {
      int step = config.getLevelStep(selectedRule);
      if (step > 0) {
        levelRule.setWithDirectSpeach(!config.isWithoutDirectSpeech(selectedRule), cache);
        levelRule.setCurrentStep(step);
      }
    }
/*
    for (TextLevelRule rule : rules) {
      if (UsedWordRule.isUsedWordRule(rule)) {
        usedWordRule.setWithDirectSpeach(!config.isWithoutDirectSpeech(rule));
        usedWordRule.setListExcludedWords(config.getExcludedWords(rule));
      } else if (LevelRule.isLevelRule(rule) && LevelRule.hasStatisticalOptions(rule)) {
        levelRule.setWithDirectSpeach(!config.isWithoutDirectSpeech(rule));
        levelRule.setCurrentStep(config.getLevelStep(rule));
      }
    }
*/
  }
  
  public void refreshCache(SingleDocument document) throws Throwable {
    setJavaLookAndFeel();
    if (debugMode) {
      MessageHandler.printToLogFile("refreshCache called (method = " + method + ")!");
    }
    cache = new StatAnCache(document.getDocumentCache(), document.getMultiDocumentsHandler().getLanguageTool());
  }
  
  private String[] getMostUsedWords(UsedWordRule usedWordRule) throws Throwable {
/*  TODO: this should be a method of UsedWordRule
    mostUsed = usedWordRule.getMostUsed(cache.getAllParagraphs(), 100);
    String[] words = new String[mostUsed.size()];
    for (int i = 0; i < mostUsed.size(); i++) {
      words[i] = String.format("%s (%.1f%%)", mostUsed.get(i).word, mostUsed.get(i).percent);
    }
    return words;
*/  
    return null;
  }

  private String getMostUsedWord(int n) throws Throwable {
//  TODO: this should be a method of UsedWordRule
//    return mostUsed.get(n).word;
    return null;
  }

  private void runLevelSubDialog(Chapter chapter) throws Throwable {
    if (chapter != null && chapter.hierarchy < 0) {
      ViewCursorTools viewCursor = new ViewCursorTools(xComponent);
      viewCursor.setTextViewCursor(0, new TextParagraph(DocumentCache.CURSOR_TYPE_TEXT, chapter.from));
      return;
    }
    UIManager.put("ToolTip.foreground", Color.black);
    UIManager.put("ToolTip.background", Color.yellow);
    hierarchy = chapter == null ? 0 : chapter.hierarchy;
    selectedRule = rules.get(method);
    if (chapter == null && LevelRule.hasStatisticalOptions(selectedRule)) {
      levelRule.setWithDirectSpeach(!config.isWithoutDirectSpeech(selectedRule), cache);
      levelRule.setCurrentStep(config.getLevelStep(selectedRule));
    }
    setChapterPanel(chapter);
    if (chapter == null) {
      from = 0;
      to = cache.size();
      hierarchy = 1;
    } else {
      from = chapter.from + 1;
      to = chapter.to;
      hierarchy = chapter.hierarchy + 1;
    }
    subChapterPane.setViewportView(getSubChapterPanel(from, to, hierarchy, chapter));
    dialog.repaint();
  }
  
  private Color getBackgroundColor(int weight) {
    Color col;
    if (weight == 0) {
      col = new Color(255, 0, 0); 
    } else if (weight == 1) {
      col = new Color(255, 150, 0); 
    } else if (weight == 2) {
      col = new Color(255, 200, 0); 
    } else if (weight == 3) {
      col = new Color(255, 255, 150); 
    } else if (weight == 4) {
      col = new Color(200, 255, 150); 
    } else if (weight == 5) {
      col = new Color(150, 255, 200); 
    } else if (weight == 6) {
      col = new Color(0, 0, 255);
    } else {
      col = new Color(255, 255, 255);
    }
    return col;
  }
  
  private Color getForegroundColor(int weight) {
//    Color col = Color.BLACK;
    Color col;
    if (weight == 0 || weight == 6) {
      col = Color.WHITE; 
    } else {
      col = Color.BLACK;
    }
    return col;
  }
  
  private void setChapterPanel(Chapter chapter) throws Throwable {
    chapterPanel.removeAll();
    chapterPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons = new GridBagConstraints();
    cons.insets = new Insets(4, 0, 4, 0);
    cons.gridx = 0;
    cons.gridy = 0;
    cons.weightx = 0.;
    cons.weighty = 0.;
    cons.anchor = GridBagConstraints.NORTHWEST;
    cons.fill = GridBagConstraints.NONE;

    String chapterTitle;
    int weight;
    if (chapter == null) {
      weight = getWeight(0, cache.size());
      chapterTitle = MESSAGES.getString("loStatisticalAnalysisEntireDocument");
    } else {
      weight = getWeight(chapter.from + 1, chapter.to);
      chapterTitle = chapter.name;
    }
    JLabel fullChapter = new JLabel(chapterTitle);
    fullChapter.setOpaque(true);
    fullChapter.setBackground(getBackgroundColor(weight));
    fullChapter.setForeground(getForegroundColor(weight));
    fullChapter.setMinimumSize(new Dimension(300, 60));
    fullChapter.setHorizontalAlignment(JLabel.CENTER);
    fullChapter.setToolTipText(getToolTippText(weight));
    if (chapter != null) {
      fullChapter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      fullChapter.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          try {
            runLevelSubDialog(chapter.parent);
          } catch (Throwable e1) {
            MessageHandler.showError(e1);
          }
        }
        @Override
        public void mouseEntered(MouseEvent e) {
          fullChapter.setForeground(Color.GRAY);
        }
        @Override
        public void mouseExited(MouseEvent e) {
          fullChapter.setForeground(getForegroundColor(weight));
        }
      });
    }
    chapterPanel.add(fullChapter, cons);
    if (debugMode) {
      MessageHandler.printToLogFile("Set Chapter Panel: " + fullChapter.getText() + ", Panel Size: " + chapterPanel.getComponentCount());
    }
    chapterPanel.setBackground(getBackgroundColor(weight));
    chapterPanel.revalidate();
  }

  
  private JPanel getSubChapterPanel(int from, int to, int hierarchy, Chapter parent) throws Throwable {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints cons = new GridBagConstraints();
    cons.insets = new Insets(2, 0, 2, 0);
    cons.gridx = 0;
    cons.gridy = 0;
    cons.anchor = GridBagConstraints.NORTHWEST;
    cons.fill = GridBagConstraints.BOTH;
    cons.weightx = 10.0f;
    cons.weighty = 10.0f;
    List<Chapter> chapters = getChapters(from, to, hierarchy, parent);
    List<JLabel> chapterButton = new ArrayList<>();
    int nButton = 0;
    for (Chapter chapter: chapters) {
      if (debugMode) {
        MessageHandler.printToLogFile("Chapter: " + chapter.name);
      }
      chapterButton.add(new JLabel(chapter.name));
      JLabel label = chapterButton.get(nButton);
      chapterButton.get(nButton).setOpaque(true);
//      chapterButton.get(nButton).setContentAreaFilled(true);
      chapterButton.get(nButton).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      chapterButton.get(nButton).setHorizontalAlignment(JLabel.CENTER);
      chapterButton.get(nButton).setBackground(getBackgroundColor(chapter.weight));
      chapterButton.get(nButton).setForeground(getForegroundColor(chapter.weight));
      chapterButton.get(nButton).setBorder(BorderFactory.createLineBorder(getBackgroundColor(chapter.weight)));
      chapterButton.get(nButton).setToolTipText(getToolTippText(chapter.weight));
      chapterButton.get(nButton).addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          try {
            runLevelSubDialog(chapter);
          } catch (Throwable e1) {
            MessageHandler.showError(e1);
          }
        }
        @Override
        public void mouseEntered(MouseEvent e) {
          label.setForeground(Color.GRAY);
        }
        @Override
        public void mouseExited(MouseEvent e) {
          label.setForeground(getForegroundColor(chapter.weight));
        }
      });
      panel.add(chapterButton.get(nButton), cons);
      cons.gridy++;
      nButton++;
    }
    return panel;
  }
  
  private int getWeight(int from, int to) throws Throwable {
    return levelRule.getLevel(from, to);
  }
  
  private String getToolTippText(int weight) {
    String txt = levelRule.getMessageOfLevel(weight);
    if (txt == null) {
      txt = MESSAGES.getString("loStatisticalAnalysisNotAnalyzed");
    }
    return "<html><div style='color:black;'>" + txt + "</html>";
  }
  
  private List<Chapter> getChaptersOfHierarchy(int from, int to, int hierarchy, Chapter parent) throws Throwable {
    List<Chapter> sameHeadings = new ArrayList<Chapter>();
    List<Paragraph> paragraphs = cache.getParagraphsfrom(from, to);
    String lastHeading = null;
    int lastNum = 0;
    for(Paragraph paragraph : paragraphs) {
      if (paragraph.hierarchy == hierarchy) {
        if (lastHeading != null) {
          int start = lastNum + (hierarchy < 0 ? 0 : 1);
          long startTime = System.currentTimeMillis();
          int weight = getWeight(start, paragraph.paraNum);
          sameHeadings.add(new Chapter (lastHeading, lastNum, paragraph.paraNum, hierarchy, weight, parent));
          if (debugMode) {
            long secondsNeeded = (long) ((System.currentTimeMillis() - startTime) / 1000.);
            MessageHandler.printToLogFile("Chapter '" + lastHeading + "' analysed. Weight = " + weight + ", hierarchy = " + hierarchy
                + "\nTime needed: " + secondsNeeded + " Seconds");
          }
        }
        lastHeading = paragraph.name;
        lastNum = paragraph.paraNum;
      }
    }
    if (lastHeading != null) {
      long startTime = System.currentTimeMillis();
      int start = lastNum + (hierarchy < 0 ? 0 : 1);
      int end = paragraphs.get(paragraphs.size() - 1).paraNum + 1;
      if (end > cache.size()) {
        end = cache.size();
      }
      int weight = getWeight(start, end);
      sameHeadings.add(new Chapter (lastHeading, lastNum, end, hierarchy, weight, parent));
      if (debugMode) {
        long secondsNeeded = (long) ((System.currentTimeMillis() - startTime) / 1000.);
        MessageHandler.printToLogFile("Chapter '" + lastHeading + "' analysed. Weight = " + weight + ", hierarchy = " + hierarchy
            + "\nTime needed: " + secondsNeeded + " Seconds");
      }
    }
    return sameHeadings;
  }
  
  private List<Chapter> getChapters(int from, int to, int hierarchy, Chapter parent) throws Throwable {
    List<Heading> headings = cache.getAllHeadings();
    if (hierarchy == 0) {
      hierarchy = 1;
    }
    // define the minimal hierarchy of chapter (> 0)
    // if there are no chapters set hierarchy to -1 (paragraph level)
    int minHierarchy = 10000;
    for (Heading heading : headings) {
      if (heading.hierarchy >= hierarchy && heading.hierarchy < minHierarchy) {
        minHierarchy = heading.hierarchy;
      }
      if (minHierarchy == hierarchy) {
        break;
      }
    }
    if (minHierarchy < 10000) {
      hierarchy = minHierarchy;
    } else {
      hierarchy = -1;
    }
    if (debugMode) {
      MessageHandler.printToLogFile("Hierarchy Level: " + hierarchy);
    }
    List<Chapter> chapters = getChaptersOfHierarchy(from, to, hierarchy, parent);
    if (debugMode) {
      MessageHandler.printToLogFile("Number of chapters: " + chapters.size());
    }
    return chapters;
  }

  String[] getAllRuleNames() {
    String[] ruleNames = new String[rules.size()];
    for (int i = 0; i < rules.size(); i++) {
      ruleNames[i] = rules.get(i).getDescription();
    }
    return ruleNames;
  }
  
  boolean isLevelRule(Rule rule) {
    return LevelRule.isLevelRule(rule);
  }
  
  String[] getAllLevelRuleNames() {
    List<String> levelRules = new ArrayList<>();
    for (Rule rule : rules) {
      if (LevelRule.isLevelRule(rule)) {
        levelRules.add(rule.getDescription());
      }
    }
    return levelRules.toArray(new String[levelRules.size()]);
  }
  
  TextLevelRule getRuleByName(String name) {
    for (TextLevelRule rule : rules) {
      if (name.equals(rule.getDescription())) {
        return rule;
      }
    }
    return null;
  }
  
  int getMethodByRule(String name) {
    int method = 0;
    for (Rule rule : rules) {
      if (name.equals(rule.getDescription())) {
        return method;
      }
      method++;
    }
    return -1;
  }
  
  /** 
   * Set Look and Feel for Java Swing Components
   */
  public static void setJavaLookAndFeel() {
    try {
      if (!System.getProperty("os.name").contains("OS X")) {
         if (System.getProperty("os.name").contains("Linux")) {
           UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
         }
         else {
           UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
      }
    } catch (Exception | AWTError ignored) {
    }
  }
  
  class Chapter {
    String name;
    int from;
    int to;
    int hierarchy;
    int weight;
    Chapter parent;
    
    Chapter (String name, int from, int to, int hierarchy ,int weight, Chapter parent) {
      this.name = name;
      this.from = from;
      this.to = to;
      this.hierarchy = hierarchy;
      this.weight = weight;
      this.parent = parent;
    }
  }


}
