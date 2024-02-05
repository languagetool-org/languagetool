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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.jetbrains.annotations.NotNull;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.UserConfig;
import org.languagetool.gui.Configuration;
import org.languagetool.openoffice.DocumentCache;
import org.languagetool.openoffice.DocumentCache.TextParagraph;
import org.languagetool.openoffice.MessageHandler;
import org.languagetool.openoffice.MultiDocumentsHandler.WaitDialogThread;
import org.languagetool.openoffice.OfficeTools;
import org.languagetool.openoffice.ViewCursorTools;
import org.languagetool.openoffice.stylestatistic.StatAnCache.Heading;
import org.languagetool.openoffice.stylestatistic.StatAnCache.Paragraph;
import org.languagetool.openoffice.SingleDocument;
import org.languagetool.rules.AbstractStatisticSentenceStyleRule;
import org.languagetool.rules.AbstractStatisticStyleRule;
import org.languagetool.rules.AbstractStyleTooOftenUsedWordRule;
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
  private final static int MIN_DIALOG_WIDTH = 640;
  private final static int MIN_DIALOG_HEIGHT = 420;
//  private final static int dialogWidth = 640;
//  private final static int dialogHeight = 600;
  private final static int MIN_OPTION_WIDTH = 260;
  private final static int MIN_OPTION_HEIGHT = 400;
  
  private JDialog dialog;
  private Container contentPane;
  private JPanel chapterPanel;
  private JPanel leftPanel;
  private JPanel rightPanel;
  private JPanel mainPanel;
  private JPanel colorPanel;
  private JScrollPane subChapterPane;
  private JLabel optionLabel;
  private JCheckBox withoutDirectSpeech;
  private JCheckBox showAdditionalOptions;
  private JCheckBox showParagraphsWithoutMatch;
  private JLabel stepLabel1;
  private JTextField stepField;
  private JLabel stepLabel2;
  private JButton defaultButton;
  private JButton setButton;
  private JButton ignore;
  private JButton removeAllIgnored;
  private JComboBox<String> function;
  private JComboBox<String> usedWords;
  private int from = 0;
  private int to = 1;
  private int hierarchy = 1;
  
  private int lastSinglePara = -1;
  private String lastParaText = null;
  
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
  
  public StatAnDialog(SingleDocument document) {
    xComponent = document.getXComponent();
    this.document = document;
    document.getMultiDocumentsHandler().setStatAnDialogRunning(true);
    rules.clear();
    Language lang = document.getLanguage();
    if (lang != null) {
      try {
        Map<String, Integer> ruleValues = new HashMap<>();
        for (Rule rule : lang.getRelevantRules(JLanguageTool.getMessageBundle(), null, lang, null)) {
          if (rule instanceof AbstractStatisticSentenceStyleRule || rule instanceof AbstractStatisticStyleRule ||
              rule instanceof ReadabilityRule || rule instanceof AbstractStyleTooOftenUsedWordRule) {
            ruleValues.put(rule.getId(), 0);
          }
        }
        UserConfig userConfig = new UserConfig(ruleValues);
        for (Rule rule : lang.getRelevantRules(JLanguageTool.getMessageBundle(), userConfig, lang, null)) {
          if (rule instanceof AbstractStatisticSentenceStyleRule || rule instanceof AbstractStatisticStyleRule ||
              (rule instanceof ReadabilityRule && !hasReadabilityRule()) || rule instanceof AbstractStyleTooOftenUsedWordRule) {
            rules.add((TextLevelRule)rule);
          }
        }
      } catch (IOException e) {
        MessageHandler.showError(e);
      }
    }
  }
  
  private void closeDialog(WaitDialogThread waitdialog) {
    cache.setNewResultcache(null, null);
    dialog.setVisible(false);
    document.getMultiDocumentsHandler().setStatAnDialogRunning(false);
    waitdialog.close();
  }
  
  private boolean hasReadabilityRule() {
    for (Rule rule : rules) {
      if (rule instanceof ReadabilityRule) {
        return true;
      }
    }
    return false;
  }
  
  private void runDialog(WaitDialogThread waitdialog) {
    if (rules.isEmpty()) {
      Language lang = document.getLanguage();
      String shortCode = lang == null ? "unknown" : lang.getShortCode();
      MessageHandler.printToLogFile("Statistical Rules are not supported for language: " + shortCode);
    }
    dialog = new JDialog();
    dialog.setName(dialogName);
    dialog.setTitle(dialogName);
    dialog.setMinimumSize(new Dimension(MIN_DIALOG_WIDTH, MIN_DIALOG_HEIGHT));
//    dialog.setSize(new Dimension(dialogWidth, dialogHeight));
    dialog.addWindowListener(new WindowListener() {
      @Override
      public void windowOpened(WindowEvent e) {
      }
      @Override
      public void windowClosing(WindowEvent e) {
        closeDialog(waitdialog);
      }
      @Override
      public void windowClosed(WindowEvent e) {
      }
      @Override
      public void windowIconified(WindowEvent e) {
      }
      @Override
      public void windowDeiconified(WindowEvent e) {
      }
      @Override
      public void windowActivated(WindowEvent e) {
      }
      @Override
      public void windowDeactivated(WindowEvent e) {
      }
    });
    
    dialog.addWindowFocusListener(new WindowFocusListener() {

      @Override
      public void windowGainedFocus(WindowEvent e) {
        if (lastSinglePara < 0 || lastParaText.equals(document.getDocumentCache().getFlatParagraph(lastSinglePara))) {
          return;
        }
        try {
          if(isLevelRule) {
            levelRule.generateBasicNumbers(cache);
            setLeftLevelRulePanel();
          } else {
            usedWordRule.generateBasicNumbers(cache);
            setLeftUsedWordRulePanel();
          }
          setRightRulePanel();
          dialog.repaint();
        } catch (Throwable e1) {
          MessageHandler.showError(e1);
        }
      }

      @Override
      public void windowLostFocus(WindowEvent e) {
        if (lastSinglePara >= 0) {
          lastParaText = new String(document.getDocumentCache().getFlatParagraph(lastSinglePara));
        }
      }
      
    });

    //  initiate
    try {
      config = new StatAnConfiguration(rules);
      if (cache == null || lastComponent == null || !lastComponent.equals(xComponent)) {
        lastComponent = xComponent;
          refreshCache(document, waitdialog);
      }
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
        usedWordRule.generateBasicNumbers(cache);
      }
      if (debugMode) {
        MessageHandler.printToLogFile("Init done");
      }
    } catch (Throwable e1) {
      MessageHandler.showError(e1);
      closeDialog(waitdialog);
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
    } else {
      usedWordRule = new UsedWordRule(selectedRule, cache);
      configRule();
      usedWordRule.generateBasicNumbers(cache);
      setLeftUsedWordRulePanel();
    }
    setRightRulePanel();
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
            } else {
              usedWordRule = new UsedWordRule(selectedRule, cache);
              configRule();
              usedWordRule.generateBasicNumbers(cache);
              setLeftUsedWordRulePanel();
            }
            setRightRulePanel();
            dialog.repaint();
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
      closeDialog(waitdialog);
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
    try {
      usedWordRule.setWithDirectSpeach(!config.isWithoutDirectSpeech(selectedRule), cache);
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
      String[] mostUsedWords = null;
      mostUsedWords = usedWordRule.getMostUsedWords();
      usedWords = new JComboBox<String>(mostUsedWords);
      usedWords.addItemListener(e -> {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          int selectedIndex = usedWords.getSelectedIndex();
          try {
            usedWordRule.setWord(usedWordRule.getMostUsedWord(selectedIndex));
            setChapterPanel(chapter);
            subChapterPane.setViewportView(getSubChapterPanel(0, cache.size(), 1, null));
            dialog.repaint();
          } catch (Throwable e1) {
            MessageHandler.showError(e1);
          }
        }
      });
      if (mostUsedWords != null && mostUsedWords.length > 0) {
        usedWords.setSelectedIndex(0);
        usedWordRule.setWord(usedWordRule.getMostUsedWord(0));
      }
      cons11.gridy++;
      cons11.weightx = 0.0f;
      cons11.weighty = 0.0f;
      cons11.anchor = GridBagConstraints.NORTHWEST;
      cons11.fill = GridBagConstraints.BOTH;
      cons11.insets = new Insets(2, 6, 2, 6);
      leftPanel.add(usedWords, cons11);
      cons11.gridy++;
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
      setChapterPanel(chapter);
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
      subChapterPane.setViewportView(getSubChapterPanel(0, cache.size(), 1, null));
      leftPanel.add(subChapterPane, cons11);
      leftPanel.validate();
    } catch (Throwable e1) {
      MessageHandler.showError(e1);
    }
  }
  
  private void setRightRulePanel() {
    //  Define right panel
    rightPanel.removeAll();
    rightPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons20 = new GridBagConstraints();
    cons20.insets = new Insets(2, 4, 2, 4);
    cons20.gridx = 0;
    cons20.gridy = 0;
    cons20.fill = GridBagConstraints.NONE;
    cons20.anchor = GridBagConstraints.NORTHWEST;
    cons20.weightx = 0.0;
    cons20.weighty = 0.0;
    
    //  Option panel
    JPanel optionPanel = new JPanel();
    optionPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons21 = new GridBagConstraints();
    cons21.insets = new Insets(4, 4, 4, 4);
    cons21.gridx = 0;
    cons21.gridy = 0;
    cons21.fill = GridBagConstraints.NONE;
    cons21.anchor = GridBagConstraints.NORTHWEST;
    cons21.weightx = 0.0;
    cons21.weighty = 0.0;
    optionLabel = new JLabel (MESSAGES.getString("loStatisticalAnalysisOptionsLabel") + ":");
    optionPanel.add(optionLabel, cons21);
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
    optionPanel.add(stepPanel, cons21);
    cons21.gridx++;
    optionPanel.add(setButton, cons21);

    cons21.gridx = 0;
    cons21.gridy++;
    optionPanel.add(withoutDirectSpeech, cons21);
    cons21.gridx++;
    defaultButton = new JButton(MESSAGES.getString("loStatisticalAnalysisDefaultButton"));
    optionPanel.add(defaultButton, cons21);
    rightPanel.add(optionPanel, cons20);

    if (isLevelRule) {
      setLevelRuleOptions();
    } else {
      cons20.gridx = 0;
      cons20.gridy++;
      ignore = new JButton(MESSAGES.getString("loStatisticalAnalysisIgnoreWordButton"));
      removeAllIgnored = new JButton(MESSAGES.getString("loStatisticalAnalysisResetIgnoredWordsButton"));
      removeAllIgnored.setEnabled(config.getExcludedWords(selectedRule).size() > 0);
      rightPanel.add(removeAllIgnored, cons20);
      cons20.gridy++;
      rightPanel.add(ignore, cons20);
      setUsedWordRuleOptions();
    }

    cons20.gridy++;
    showAdditionalOptions = new JCheckBox("Show additional Options");
//    showAdditionalOptions = new JCheckBox(MESSAGES.getString("loStatisticalAnalysisWithoutDirectSpreech"));

    showParagraphsWithoutMatch = new JCheckBox("Show paragraphs without matches");
//  showParagraphsWithoutMatch = new JCheckBox(MESSAGES.getString("loStatisticalAnalysisWithoutDirectSpreech"));
    showParagraphsWithoutMatch.setVisible(false);

    showAdditionalOptions.setSelected(config.showAdditionalOptions());
    showAdditionalOptions.addActionListener(e -> {
      colorPanel.setVisible(showAdditionalOptions.isSelected());
      showParagraphsWithoutMatch.setVisible(showAdditionalOptions.isSelected());
      config.setShowAdditionalOptions(showAdditionalOptions.isSelected());
    });

    showParagraphsWithoutMatch.setSelected(config.showAllParagraphs());
    showParagraphsWithoutMatch.addActionListener(e -> {
      config.setShowAllParagraphs(showParagraphsWithoutMatch.isSelected());
      try {
        runLevelSubDialog(null);
      } catch (Throwable e1) {
        MessageHandler.printException(e1);
      }
    });

    cons20.gridy++;
    rightPanel.add(showAdditionalOptions, cons20);
    
    cons20.gridy++;
    rightPanel.add(showParagraphsWithoutMatch, cons20);
    
    cons20.gridy++;
    cons20.insets = new Insets(6, 4, 2, 4);
    rightPanel.add(getColorOptionsPanel(), cons20);
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
  
  private void setUsedWordRuleOptions() {
    defaultButton.addActionListener(e -> {
      if (usedWordRule.getDefaultDirectSpeach() == withoutDirectSpeech.isSelected() 
          || usedWordRule.getDefaultStep() != config.getLevelStep(selectedRule)) {
        withoutDirectSpeech.setSelected(!usedWordRule.getDefaultDirectSpeach());
        config.setWithoutDirectSpeech(selectedRule, !usedWordRule.getDefaultDirectSpeach());
        config.setLevelStep(selectedRule, usedWordRule.getDefaultStep());
        stepField.setText(Integer.toString(usedWordRule.getDefaultStep()));
        try {
          config.saveConfiguration();
          usedWordRule.setWithDirectSpeach(!usedWordRule.getDefaultDirectSpeach(), cache);
          usedWordRule.setCurrentStep(usedWordRule.getDefaultStep());
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
          usedWordRule.setCurrentStep(levelStep);
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
        usedWordRule.setWithDirectSpeach(!withoutDirectSpeech.isSelected(), cache);
        runLevelSubDialog(null);
      } catch (Throwable t) {
        MessageHandler.showError(t);
      }
    });
    ignore.addActionListener(e -> {
      if (usedWords.getItemCount() > 0) {
        try {
          String word = usedWordRule.getMostUsedWord(usedWords.getSelectedIndex());
          config.addExcludedWord(selectedRule, word);
          config.saveConfiguration();
          usedWordRule.setListExcludedWords(config.getExcludedWords(selectedRule));
          usedWordRule.refreshMostUsed(0, cache.size());
          usedWords.removeAllItems();
          for (String usedWord : usedWordRule.getMostUsedWords()) {
            usedWords.addItem(usedWord);
          }
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
        usedWordRule.refreshMostUsed(0, cache.size());
        usedWords.removeAllItems();
        for (String usedWord : usedWordRule.getMostUsedWords()) {
          usedWords.addItem(usedWord);
        }
        usedWords.setSelectedIndex(0);
        removeAllIgnored.setEnabled(false);
      } catch (Throwable t) {
        MessageHandler.showError(t);
      }
    });
    int nStep = 0;
    String sStep = "%";
    nStep = config.getLevelStep(selectedRule);
    stepField.setText(Integer.toString(nStep));
    stepLabel2.setText(sStep);
    defaultButton.setEnabled(true);
    stepLabel1.setEnabled(true);
    stepField.setEnabled(true);
    stepLabel2.setEnabled(true);
    setButton.setEnabled(true);
    withoutDirectSpeech.setEnabled(true);
    optionLabel.setEnabled(true);
    if (usedWords.getItemCount() > 0) {
      ignore.setEnabled(true);
    } else {
      ignore.setEnabled(false);
    }
    List<String> exWords = config.getExcludedWords(selectedRule);
    if (exWords != null && !exWords.isEmpty()) {
      removeAllIgnored.setEnabled(true);
    } else {
      removeAllIgnored.setEnabled(false);
    }
  }

  /**
   * opens the LT check dialog for spell and grammar check
   */
  @Override
  public void run() {
    try {
      WaitDialogThread waitdialog = 
          document.getMultiDocumentsHandler().new WaitDialogThread("Please wait", MESSAGES.getString("loWaitMessage"));
      waitdialog.start();
      runDialog(waitdialog);
      waitdialog.close();
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
      usedWordRule.setWithDirectSpeach(!config.isWithoutDirectSpeech(selectedRule), cache);
      usedWordRule.setListExcludedWords(config.getExcludedWords(selectedRule));
      int step = config.getLevelStep(selectedRule);
      if (step > 0) {
        usedWordRule.setCurrentStep(step);
      }
    } else if (LevelRule.isLevelRule(selectedRule) && LevelRule.hasStatisticalOptions(selectedRule)) {
      levelRule.setWithDirectSpeach(!config.isWithoutDirectSpeech(selectedRule), cache);
      int step = config.getLevelStep(selectedRule);
      if (step > 0) {
        levelRule.setCurrentStep(step);
      }
    }
  }
  
  public void refreshCache(SingleDocument document, WaitDialogThread waitdialog) throws Throwable {
    setJavaLookAndFeel();
    if (debugMode) {
      MessageHandler.printToLogFile("refreshCache called (method = " + method + ")!");
    }
    cache = new StatAnCache(document, config, waitdialog);
  }
  
  private void runLevelSubDialog(Chapter chapter) throws Throwable {
    if (chapter != null && chapter.hierarchy < 0) {
      ViewCursorTools viewCursor = new ViewCursorTools(xComponent);
      TextParagraph tPara = new TextParagraph(DocumentCache.CURSOR_TYPE_TEXT, chapter.from);
      viewCursor.setTextViewCursor(0, tPara);
      lastSinglePara = document.getDocumentCache().getFlatParagraphNumber(tPara);
      if (!isLevelRule) {
        usedWordRule.setCacheForParagraph(lastSinglePara, chapter.from, cache);
      }
      cache.markParagraph(lastSinglePara, config.getUnderlineType(), config.getUnderlineColor());
      return;
    }
    lastSinglePara = -1;
    UIManager.put("ToolTip.foreground", Color.black);
    UIManager.put("ToolTip.background", Color.yellow);
    hierarchy = chapter == null ? 0 : chapter.hierarchy;
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
      if (chapter.hierarchy >= 0 || config.showAllParagraphs() 
          || cache.isRelevantParagraph(chapter.from, selectedRule, usedWordRule)) {
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
    }
    return panel;
  }
  
  private int getWeight(int from, int to) throws Throwable {
    if (UsedWordRule.isUsedWordRule(selectedRule)) {
      return usedWordRule.getLevel(from, to);
    } else {
      return levelRule.getLevel(from, to);
    }
  }
  
  private String getToolTippText(int weight) {
    String txt = UsedWordRule.isUsedWordRule(selectedRule) ? 
        usedWordRule.getMessageOfLevel(weight) : levelRule.getMessageOfLevel(weight);
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
  
  private String[] getUnderlineTypes() {
    String[] types = {
        MESSAGES.getString("guiUTypeWave"),
        MESSAGES.getString("guiUTypeBoldWave"),
        MESSAGES.getString("guiUTypeBold"),
        MESSAGES.getString("guiUTypeDash")};
    return types;
  }

  private int getUnderlineType() {
    short nType = config.getUnderlineType();
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

  private void setUnderlineType(int index) {
    if (index == 1) {
      config.setUnderlineType(Configuration.UNDERLINE_BOLDWAVE);
    } else if (index == 2) {
      config.setUnderlineType(Configuration.UNDERLINE_BOLD);
    } else if (index == 3) {
      config.setUnderlineType(Configuration.UNDERLINE_DASH);
    } else {
      config.setUnderlineType((short) 0);
    }
  }

  /**  Panel to choose underline Colors
   *   and rule options (if exists)
   *   @since 5.3
   */
  @NotNull
  private JPanel getColorOptionsPanel() {
    //  Color Panel
    colorPanel = new JPanel();
    colorPanel.setLayout(null);
    colorPanel.setBounds(0, 0, 120, 10);
    colorPanel.setBorder(BorderFactory.createLineBorder(Color.black));

    colorPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons1 = new GridBagConstraints();
    cons1.insets = new Insets(5, 5, 5, 5);
    cons1.gridx = 0;
    cons1.gridy = 0;
    cons1.weightx = 0.0f;
    cons1.fill = GridBagConstraints.NONE;
    cons1.anchor = GridBagConstraints.NORTHWEST;

    JLabel underlineStyle = new JLabel(MESSAGES.getString("guiUColorStyleLabel") + " ");
    colorPanel.add(underlineStyle);

    JLabel underlineLabel = new JLabel(" \u2588\u2588\u2588 ");  // \u2587 is smaller
    underlineLabel.setForeground(config.getUnderlineColor());
    underlineLabel.setBackground(config.getUnderlineColor());

    JComboBox<String> underlineType = new JComboBox<>(getUnderlineTypes());
    underlineType.setSelectedIndex(getUnderlineType());
    underlineType.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setUnderlineType(underlineType.getSelectedIndex());
        try {
          config.saveConfiguration();
        } catch (Throwable e1) {
          MessageHandler.showError(e1);
        }
      }
    });
    JPanel showPanel = new JPanel();
    showPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons11 = new GridBagConstraints();
    cons11.insets = new Insets(0, 0, 0, 0);
    cons11.gridx = 0;
    cons11.gridy = 0;
    cons11.weightx = 0.0f;
    cons11.fill = GridBagConstraints.NONE;
    cons11.anchor = GridBagConstraints.NORTHWEST;
    showPanel.add(underlineType, cons11);
    cons11.gridx++;
    showPanel.add(underlineLabel, cons11);

    cons1.gridy++;
    colorPanel.add(showPanel, cons1);
    
    JPanel buttonPanel = new JPanel();
    showPanel.setLayout(new GridBagLayout());
    GridBagConstraints cons12 = new GridBagConstraints();
    cons12.insets = new Insets(0, 0, 0, 0);
    cons12.gridx = 0;
    cons12.gridy = 0;
    cons12.weightx = 0.0f;
    cons12.fill = GridBagConstraints.NONE;
    cons12.anchor = GridBagConstraints.NORTHWEST;
    JButton changeButton = new JButton(MESSAGES.getString("guiUColorChange"));
    changeButton.addActionListener(e -> {
      Color oldColor = underlineLabel.getForeground();
      dialog.setAlwaysOnTop(false);
      JColorChooser colorChooser = new JColorChooser(oldColor);
      ActionListener okActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
          Color newColor = colorChooser.getColor();
          if(newColor != null && !newColor.equals(oldColor)) {
            underlineLabel.setForeground(newColor);
            underlineLabel.setBackground(newColor);
            config.setUnderlineColor(newColor);
            try {
              config.saveConfiguration();
            } catch (Throwable e1) {
              MessageHandler.showError(e1);
            }
          }
          dialog.setAlwaysOnTop(true);
        }
      };
      // For cancel selection, change button background to red
      ActionListener cancelActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
          dialog.setAlwaysOnTop(true);
        }
      };
      JDialog colorDialog = JColorChooser.createDialog(dialog, MESSAGES.getString("guiUColorDialogHeader"), true,
          colorChooser, okActionListener, cancelActionListener);
      colorDialog.setAlwaysOnTop(true);
      colorDialog.toFront();
      colorDialog.setVisible(true);
    });
    buttonPanel.add(changeButton, cons12);
  
    JButton defaultButton = new JButton(MESSAGES.getString("guiUColorDefault"));
    defaultButton.addActionListener(e -> {
      config.setDefaultUnderlineColor();
      underlineLabel.setForeground(config.getUnderlineColor());
      config.setUnderlineType((short) 0);
      underlineType.setSelectedIndex(getUnderlineType());
    });
    cons12.gridx++;
    buttonPanel.add(defaultButton, cons12);
    
    cons1.gridy++;
    colorPanel.add(buttonPanel, cons1);
    colorPanel.setVisible(showAdditionalOptions.isSelected());
    // End of Color Panel

    return colorPanel;
  }

  public static String getStatisticalRulesSupportedLanguages() {
    String out = "Statistical Style Rules are supported by the following Languages:\n";
    for (Language lang : Languages.get()) {
      if (OfficeTools.hasStatisticalStyleRules(lang)) {
        out += (lang.getShortCodeWithCountryAndVariant() + "\n");
      }
    }
    return out;
  }


}
