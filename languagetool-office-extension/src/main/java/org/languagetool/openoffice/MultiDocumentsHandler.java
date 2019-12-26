/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Fred Kruse
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
package org.languagetool.openoffice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JOptionPane;

import org.jetbrains.annotations.Nullable;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.UserConfig;
import org.languagetool.gui.Configuration;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.Rule;
import org.languagetool.rules.TextLevelRule;
import org.languagetool.tools.Tools;

import com.sun.star.awt.MenuEvent;
import com.sun.star.awt.MenuItemStyle;
import com.sun.star.awt.XMenuBar;
import com.sun.star.awt.XMenuListener;
import com.sun.star.awt.XPopupMenu;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XModel;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Class to handle multiple LO documents for checking
 * @since 4.3
 * @author Fred Kruse, Marcin Miłkowski
 */
public class MultiDocumentsHandler {

  // LibreOffice (since 4.2.0) special tag for locale with variant 
  // e.g. language ="qlt" country="ES" variant="ca-ES-valencia":
  private static final String LIBREOFFICE_SPECIAL_LANGUAGE_TAG = "qlt";
  
  // If anything on the position of LT menu is changed the following has to be changed
  private static final String TOOLS_COMMAND = ".uno:ToolsMenu";             //  Command to open tools menu
  private static final String WORD_COUNT_COMMAND = ".uno:WordCountDialog";  //  Command to open words count menu (LT menu is installed before)
                                                    //  Command to Switch Off/On LT
  private static final String LT_SWITCH_OFF_COMMAND = "service:org.languagetool.openoffice.Main?switchOff";   
  private static final String LT_PROFILE_COMMAND = "service:org.languagetool.openoffice.Main?profileChangeTo:";
  
  private static final boolean debugMode = false;   //  should be false except for testing
  
  private SwJLanguageTool langTool = null;
  private Language docLanguage = null;
  private Language fixedLanguage = null;
  private Language langForShortName;
  private final ResourceBundle messages;
  private final XEventListener xEventListener;
  private final Main mainThread;
  private final File configDir;
  private final String configFile;
  private final File oldConfigFile;
  private Configuration config = null;
  private LinguisticServices linguServices = null;
  private SortedTextRules sortedTextRules;
  private Set<String> disabledRulesUI;      //  Rules disabled by context menu or spell dialog
  private final List<Rule> extraRemoteRules;      //  store of rules supported by remote server but not locally
  
  private XComponentContext xContext;       //  The context of the document
  private List<SingleDocument> documents;   //  The List of LO documents to be checked
  private boolean proofIsRunning = false;   //  true if a check is almost running
  private boolean isParallelThread = false; //  is parallel thread (right mouse click, while iteration)
  private XComponent goneContext = null;    //  save component of closed document
  private boolean recheck = true;
  private int docNum;                       //  number of the current document

  private boolean switchOff = false;        //  is LT switched off
  private boolean noMultiReset = true;      //  will be overwritten by config;

  private String menuDocId = null;          //  Id of document at which context menu was called 

  @SuppressWarnings("unused")
  private LanguagetoolMenu ltMenu = null;

  private boolean testMode = false;


  MultiDocumentsHandler(XComponentContext xContext, File configDir, String configFile, File oldConfigFile,
      ResourceBundle messages, Main mainThread) {
    this.xContext = xContext;
    this.configDir = configDir;
    this.configFile = configFile;
    this.oldConfigFile = oldConfigFile;
    this.messages = messages;
    this.xEventListener = mainThread;
    this.mainThread = mainThread;
    documents = new ArrayList<>();
    disabledRulesUI = new HashSet<>();
    extraRemoteRules = new ArrayList<Rule>();
  }
  
  ProofreadingResult getCheckResults(String paraText, Locale locale, ProofreadingResult paRes, 
      int[] footnotePositions, boolean docReset) {
    
    if (!hasLocale(locale)) {
      return paRes;
    }
    if(fixedLanguage == null || langForShortName == null) {
      langForShortName = getLanguage(locale);
    }
    boolean isSameLanguage = langForShortName.equals(docLanguage);
    if (!isSameLanguage || langTool == null || recheck) {
      if (!isSameLanguage) {
        docLanguage = langForShortName;
        extraRemoteRules.clear();
      }
      initLanguageTool();
      initCheck();
    }
    if (proofIsRunning) {
      isParallelThread = true;          //  parallel Thread (right-click or dialog-box while background iteration is running)
    } else {
      proofIsRunning = true;  // main thread is running
    }
    docNum = getNumDoc(paRes.aDocumentIdentifier);
    if(switchOff) {
      return paRes;
    }
    paRes = documents.get(docNum).getCheckResults(paraText, locale, paRes, footnotePositions, isParallelThread, docReset, langTool);
    if(langTool.doReset()) {
      // langTool.doReset() == true: if server connection is broken ==> switch to internal check
      MessageHandler.showMessage(messages.getString("loRemoteSwitchToLocal"));
      config.setRemoteCheck(false);
      try {
        config.saveConfiguration(docLanguage);
      } catch (IOException e) {
        MessageHandler.showError(e);
      }
      mainThread.resetDocument();
    }
    if(isParallelThread) {
      isParallelThread = false;
    } else {
      proofIsRunning = false;
    }
    return paRes;
  }
  
  /**
   *  Set all documents to be checked again
   */
  void setRecheck() {
    recheck = true;
  }
  
  /**
   *  Set XComponentContext
   */
  void setComponentContext(XComponentContext xContext) {
    this.xContext = xContext;
    setRecheck();
  }
  
  /**
   *  Set a document as closed
   */
  void setContextOfClosedDoc(XComponent context) {
    goneContext = context;
  }
  
  /**
   *  Add a rule to disabled rules by context menu or spell dialog
   */
  void addDisabledRule(String ruleId) {
    disabledRulesUI.add(ruleId);
  }
  
  /**
   *  remove all disabled rules by context menu or spell dialog
   */
  void resetDisabledRules() {
    disabledRulesUI = new HashSet<>();
  }
  
  /**
   *  get all disabled rules by context menu or spell dialog
   */
  Set<String> getDisabledRules() {
    return disabledRulesUI;
  }
  
  /**
   *  set disabled rules by context menu or spell dialog
   */
  void setDisabledRules(Set<String> ruleIds) {
    disabledRulesUI = new HashSet<>(ruleIds);
  }
  
  /**
   *  get LanguageTool
   */
  SwJLanguageTool getLanguageTool() {
    return langTool;
  }
  
  /**
   *  get Configuration
   */
  Configuration getConfiguration() {
    try {
      if (config == null || recheck) {
        if(xContext != null) {
          linguServices = new LinguisticServices(xContext);
        }
        if(docLanguage == null) {
          docLanguage = getLanguage();
        }
        initLanguageTool();
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    return config;
  }
  
  /**
   * Allow xContext == null for test cases
   */
  void setTestMode(boolean mode) {
    testMode = mode;
  }

  
  /** 
   * Do a reset to check document again
   */
  boolean doResetCheck() {
    if(documents.isEmpty() || (documents.size() > 1 && noMultiReset)) {
      return false;
    }
    return documents.get(docNum).doResetCheck();
  }

  /** 
   * Reset only changed paragraphs
   */
  void optimizeReset() {
    if(documents.size() > 0) {
      documents.get(docNum).optimizeReset();
    }
  }
  
  /**
   * Checks the language under the cursor. Used for opening the configuration dialog.
   * @return the language under the visible cursor
   */
  @Nullable
  public Language getLanguage() {
    XComponent xComponent = OfficeTools.getCurrentComponent(xContext);
    Locale charLocale;
    XPropertySet xCursorProps;
    try {
      XModel model = UnoRuntime.queryInterface(XModel.class, xComponent);
      if(model == null) {
        return Languages.getLanguageForShortCode("en-US");
      }
      XTextViewCursorSupplier xViewCursorSupplier =
          UnoRuntime.queryInterface(XTextViewCursorSupplier.class, model.getCurrentController());
      XTextViewCursor xCursor = xViewCursorSupplier.getViewCursor();
      if (xCursor.isCollapsed()) { // no text selection
        xCursorProps = UnoRuntime.queryInterface(XPropertySet.class, xCursor);
      } else { // text is selected, need to create another cursor
        // as multiple languages can occur here - we care only
        // about character under the cursor, which might be wrong
        // but it applies only to the checking dialog to be removed
        xCursorProps = UnoRuntime.queryInterface(
            XPropertySet.class,
            xCursor.getText().createTextCursorByRange(xCursor.getStart()));
      }

      // The CharLocale and CharLocaleComplex properties may both be set, so we still cannot know
      // whether the text is e.g. Khmer or Tamil (the only "complex text layout (CTL)" languages we support so far).
      // Thus we check the text itself:
      if (new KhmerDetector().isThisLanguage(xCursor.getText().getString())) {
        return Languages.getLanguageForShortCode("km");
      }
      if (new TamilDetector().isThisLanguage(xCursor.getText().getString())) {
        return Languages.getLanguageForShortCode("ta");
      }

      Object obj = xCursorProps.getPropertyValue("CharLocale");
      if (obj == null) {
        return Languages.getLanguageForShortCode("en-US");
      }
      charLocale = (Locale) obj;
      boolean langIsSupported = false;
      for (Language element : Languages.get()) {
        if (charLocale.Language.equalsIgnoreCase(LIBREOFFICE_SPECIAL_LANGUAGE_TAG)
            && element.getShortCodeWithCountryAndVariant().equalsIgnoreCase(charLocale.Variant)) {
          langIsSupported = true;
          break;
        }
        if (element.getShortCode().equals(charLocale.Language)) {
          langIsSupported = true;
          break;
        }
      }
      if (!langIsSupported) {
        String message = Tools.i18n(messages, "language_not_supported", charLocale.Language);
        JOptionPane.showMessageDialog(null, message);
        return null;
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
      return null;
    }
    return getLanguage(charLocale);
  }

  /**
   * @return true if LT supports the language of a given locale
   * @param locale The Locale to check
   */
  final boolean hasLocale(Locale locale) {
    try {
      for (Language element : Languages.get()) {
        if (locale.Language.equalsIgnoreCase(LIBREOFFICE_SPECIAL_LANGUAGE_TAG)
            && element.getShortCodeWithCountryAndVariant().equals(locale.Variant)) {
          return true;
        }
        if (element.getShortCode().equals(locale.Language)) {
          return true;
        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    return false;
  }

  /**
   *  Set config Values for all documents
   */
  private void setConfigValues(Configuration config, SwJLanguageTool langTool) {
    this.config = config;
    this.langTool = langTool;
    this.noMultiReset = config.isNoMultiReset();
    for (SingleDocument document : documents) {
      document.setConfigValues(config);
    }
  }

  private Language getLanguage(Locale locale) {
    try {
      if (locale.Language.equalsIgnoreCase(LIBREOFFICE_SPECIAL_LANGUAGE_TAG)) {
        return Languages.getLanguageForShortCode(locale.Variant);
      } else {
        return Languages.getLanguageForShortCode(locale.Language + "-" + locale.Country);
      }
    } catch (IllegalArgumentException e) {
      return Languages.getLanguageForShortCode(locale.Language);
    }
  }

  /**
   * Get or Create a Number from docID
   * Return -1 if failed
   */
  private int getNumDoc(String docID) {
    if (goneContext != null ) {
      removeDoc(docID);
    }
    for (int i = 0; i < documents.size(); i++) {
      if (documents.get(i).getDocID().equals(docID)) {  //  document exist
        if(!testMode && documents.get(i).getXComponent() == null) {
          XComponent xComponent = OfficeTools.getCurrentComponent(xContext);
          if (xComponent == null) {
            MessageHandler.printToLogFile("Error: Document (ID: " + docID + ") has no XComponent -> Internal space will not be deleted when document disposes");
          } else {
            try {
              xComponent.addEventListener(xEventListener);
            } catch (Throwable t) {
              MessageHandler.printToLogFile("Error: Document (ID: " + docID + ") has no XComponent -> Internal space will not be deleted when document disposes");
              xComponent = null;
            }
            if(xComponent != null) {
              documents.get(i).setXComponent(xContext, xComponent);
              MessageHandler.printToLogFile("Fixed: XComponent set for Document (ID: " + docID + ")");
            }
          }
        }
        return i;
      }
    }
    //  Add new document
    XComponent xComponent = null;
    if (!testMode) {              //  xComponent == null for test cases 
      xComponent = OfficeTools.getCurrentComponent(xContext);
      if (xComponent == null) {
        MessageHandler.printToLogFile("Error: Document (ID: " + docID + ") has no XComponent -> Internal space will not be deleted when document disposes");
      } else {
        try {
          xComponent.addEventListener(xEventListener);
        } catch (Throwable t) {
          MessageHandler.printToLogFile("Error: Document (ID: " + docID + ") has no XComponent -> Internal space will not be deleted when document disposes");
          xComponent = null;
        }
      }
      ltMenu = new LanguagetoolMenu(xContext);
    }
    documents.add(new SingleDocument(xContext, config, docID, xComponent, this));
    if (debugMode) {
      MessageHandler.printToLogFile("Document " + docNum + " created; docID = " + docID);
    }
    return documents.size() - 1;
  }

  /**
   * Delete a document number and all internal space
   */
  private void removeDoc(String docID) {
    int rmNum = -1;
    int docNum = -1;
    for (int i = 0; i < documents.size(); i++) {
      XComponent xComponent = documents.get(i).getXComponent();
      if (xComponent != null && xComponent.equals(goneContext)) { //  disposed document found
        rmNum = i;
        break;
      }
    }
    if(rmNum < 0) {
      MessageHandler.printToLogFile("Error: Disposed document not found");
      goneContext = null;
    }
    for (int i = 0; i < documents.size(); i++) {
      if (documents.get(i).getDocID().equals(docID)) {  //  document exist
        docNum = i;
        break;
      }
    }
    if(rmNum >= 0 && docNum != rmNum ) {  // don't delete a closed document before the last check is done
      documents.remove(rmNum);
      goneContext = null;
      if (debugMode) {
        MessageHandler.printToLogFile("Document " + rmNum + " deleted");
      }
    }
  }

  private void initLanguageTool() {
    try {
      linguServices = new LinguisticServices(xContext);
      config = new Configuration(configDir, configFile, oldConfigFile, docLanguage, linguServices);
      fixedLanguage = config.getDefaultLanguage();
      if(fixedLanguage != null) {
        docLanguage = fixedLanguage;
      }
      switchOff = config.isSwitchedOff();
      // not using MultiThreadedSwJLanguageTool here fixes "osl::Thread::Create failed", see https://bugs.documentfoundation.org/show_bug.cgi?id=90740:
      langTool = new SwJLanguageTool(docLanguage, config.getMotherTongue(),
          new UserConfig(config.getConfigurableValues(), linguServices), config, extraRemoteRules, testMode);
      config.initStyleCategories(langTool.getAllRules());
      /* The next row is only for a single line break marks a paragraph
      docLanguage.getSentenceTokenizer().setSingleLineBreaksMarksParagraph(true);
       */
      File ngramDirectory = config.getNgramDirectory();
      if (ngramDirectory != null) {
        File ngramLangDir = new File(config.getNgramDirectory(), docLanguage.getShortCode());
        if (ngramLangDir.exists()) {  // user might have ngram data only for some languages and that's okay
          langTool.activateLanguageModelRules(ngramDirectory);
        }
      }
      File word2VecDirectory = config.getWord2VecDirectory();
      if (word2VecDirectory != null) {
        File word2VecLangDir = new File(config.getWord2VecDirectory(), docLanguage.getShortCode());
        if (word2VecLangDir.exists()) {  // user might have ngram data only for some languages and that's okay
          langTool.activateWord2VecModelRules(word2VecDirectory);
        }
      }
      for (Rule rule : langTool.getAllActiveOfficeRules()) {
        if (rule.isDictionaryBasedSpellingRule()) {
          langTool.disableRule(rule.getId());
        }
        if (rule.useInOffice()) {
          langTool.enableRule(rule.getId());
        }
      }
      setConfigValues(config, langTool);
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
  }

  void initCheck() {
    Set<String> disabledRuleIds = config.getDisabledRuleIds();
    if (disabledRuleIds != null) {
      // copy as the config thread may access this as well
      List<String> list = new ArrayList<>(disabledRuleIds);
      for (String id : list) {
        langTool.disableRule(id);
      }
    }
    Set<String> disabledCategories = config.getDisabledCategoryNames();
    if (disabledCategories != null) {
      // copy as the config thread may access this as well
      List<String> list = new ArrayList<>(disabledCategories);
      for (String categoryName : list) {
        langTool.disableCategory(new CategoryId(categoryName));
      }
    }
    Set<String> enabledRuleIds = config.getEnabledRuleIds();
    if (enabledRuleIds != null) {
      // copy as the config thread may access this as well
      List<String> list = new ArrayList<>(enabledRuleIds);
      for (String ruleName : list) {
        langTool.enableRule(ruleName);
      }
    }
    if (disabledRulesUI != null) {
      for (String id : disabledRulesUI) {
        langTool.disableRule(id);
      }
    }
    recheck = false;
    sortedTextRules = new SortedTextRules();
    setConfigValues(config, langTool);
    for (SingleDocument document : documents) {
      document.resetCache();
    }
  }
  
  
  public boolean isSwitchedOff() {
    return switchOff;
  }

/**
 *  Toggle Switch Off / On of LT
 *  return true if toggle was done 
 */
  public boolean toggleSwitchedOff() throws IOException {
    if(docLanguage == null) {
      docLanguage = getLanguage();
    }
    if (config == null) {
      config = new Configuration(configDir, configFile, oldConfigFile, docLanguage, linguServices);
    }
    switchOff = !switchOff;
//    boolean ret = setMenuTextForSwitchOff(xContext);
/*    
    if(!ret) {
      switchOff = !switchOff;
    }
*/    
    langTool = null;
    config.setSwitchedOff(switchOff, docLanguage);
    return true;
  }
  
  public void setMenuDocId(String docId) {
    menuDocId = new String(docId);
  }
  
  public void ignoreOnce() {
    for (SingleDocument document : documents) {
      if(menuDocId.equals(document.getDocID())) {
        document.ignoreOnce();
        break;
      }
    }
  }
  
  public void deactivateRule() {
    for (SingleDocument document : documents) {
      if(menuDocId.equals(document.getDocID())) {
        String ruleId = document.deactivateRule();
        if (ruleId != null) {
          try {
            Configuration confg = new Configuration(configDir, configFile, oldConfigFile, docLanguage, linguServices);
            Set<String> ruleIds = new HashSet<>();
            ruleIds.add(ruleId);
            confg.addDisabledRuleIds(ruleIds);
            confg.saveConfiguration(docLanguage);
          } catch (IOException e) {
            MessageHandler.printException(e);
          }
        }
        if (debugMode) {
          MessageHandler.printToLogFile("Rule Disabled: " + (ruleId == null ? "null" : ruleId));
        }
        break;
      }
    }
  }
  
  public void resetIgnoreOnce() {
    for (SingleDocument document : documents) {
      document.resetIgnoreOnce();
    }
  }

  /**
   * Returns a list of different numbers of paragraphs to check for text level rules
   * (currently only -1 for full text check and n for max number for other text level rules)
   */
  public List<Integer> getNumMinToCheckParas() {
    return sortedTextRules.minToCheckParagraph;
  }

  /**
   * activate all rules stored under a given index related to the list of getNumMinToCheckParas
   * deactivate all other text level rules
   */
  public void activateTextRulesByIndex(int index) {
    sortedTextRules.activateTextRulesByIndex(index);
  }

  /**
   * reactivate all text level rules
   */
  public void reactivateTextRules() {
    sortedTextRules.reactivateTextRules();;
  }

  /**
   * class to store all text level rules sorted by the minimum to check paragraphs
   * (currently only full text check and all other text level rules)
   *
   */
  class SortedTextRules { 
    List<Integer> minToCheckParagraph;
    List<List<String>> textLevelRules;
    SortedTextRules () {
      minToCheckParagraph = new ArrayList<Integer>();
      textLevelRules = new ArrayList<List<String>>();
      List<Rule> rules = langTool.getAllActiveOfficeRules();
      for(Rule rule : rules) {
        if(rule instanceof TextLevelRule && !langTool.getDisabledRules().contains(rule.getId()) 
            && !disabledRulesUI.contains(rule.getId())) {
          insertRule(((TextLevelRule) rule).minToCheckParagraph(), rule.getId());
        }
      }
      if(debugMode) {
        MessageHandler.printToLogFile("Number different minToCheckParagraph: " + minToCheckParagraph.size());
        for( int i = 0; i < minToCheckParagraph.size(); i++) {
          MessageHandler.printToLogFile("minToCheckParagraph: " + minToCheckParagraph.get(i));
          for (int j = 0; j < textLevelRules.get(i).size(); j++) {
            MessageHandler.printToLogFile("RuleId: " + textLevelRules.get(i).get(j));
          }
        }
      }
    }

    private void insertRule (int minPara, String RuleId) {
      if(minPara < 0) {
        int n = minToCheckParagraph.indexOf(minPara);
        if( n < 0) {
          minToCheckParagraph.add(minPara);
          textLevelRules.add(new ArrayList<String>());
        }
        textLevelRules.get(textLevelRules.size() - 1).add(new String(RuleId));
      } else {
        int n = minToCheckParagraph.indexOf(-1);
        if( n == 0 || minToCheckParagraph.size() == 0) {
          minToCheckParagraph.add(0, minPara);
          textLevelRules.add(0, new ArrayList<String>());
        } else if(minPara > minToCheckParagraph.get(0)) {
          minToCheckParagraph.set(0, minPara);
        }
        textLevelRules.get(0).add(new String(RuleId));
      }
    }

/*
 * This rule was commented out for performance reasons and replaced by the same named rule before 
 *
    private void insertRule (int minPara, String RuleId) {
      int n = minToCheckParagraph.indexOf(minPara); 
      if( n >= 0) {
        textLevelRules.get(n).add(new String(RuleId));
        return;
      } else {
        if(minPara < 0) {
          n = minToCheckParagraph.size();
        } else {
          for (n = 0; n < minToCheckParagraph.size(); n++) {
            if(minPara < minToCheckParagraph.get(n) || minToCheckParagraph.get(n) < 0) {
              break;
            }
          }
        }
        minToCheckParagraph.add(n, minPara);
        textLevelRules.add(n, new ArrayList<String>());
        textLevelRules.get(n).add(new String(RuleId));
      }
    }
*/
    public List<Integer> getMinToCheckParas() {
      return minToCheckParagraph;
    }

    public void activateTextRulesByIndex(int index) {
      for(int i = 0; i < textLevelRules.size(); i++) {
        if(i == index) {
          for (String ruleId : textLevelRules.get(i)) {
            langTool.enableRule(ruleId);
          }
        } else {
          for (String ruleId : textLevelRules.get(i)) {
            langTool.disableRule(ruleId);
          }
        }
      }
    }

    public void reactivateTextRules() {
      for(List<String> textRules : textLevelRules) {
        for (String ruleId : textRules) {
          langTool.enableRule(ruleId);
        }
      }
    }

  }

  class LanguagetoolMenu implements XMenuListener {
    
    XMenuBar menubar = null;
    XPopupMenu toolsMenu = null;
    XPopupMenu ltMenu = null;
    short toolsId = 0;
    short ltId = 0;
    private XPopupMenu xProfileMenu = null;
    private List<String> definedProfiles = null;
    private String currentProfile = null;
    
    public LanguagetoolMenu(XComponentContext xContext) {
      menubar = OfficeTools.getMenuBar(xContext);
      if (menubar == null) {
        MessageHandler.printToLogFile("Menubar is null");
        return;
      }
      for (short i = 0; i < menubar.getItemCount(); i++) {
        toolsId = menubar.getItemId(i);
        String command = menubar.getCommand(toolsId);
        if(TOOLS_COMMAND.equals(command)) {
          toolsMenu = menubar.getPopupMenu(toolsId);
          break;
        }
      }
      if (toolsMenu == null) {
        MessageHandler.printToLogFile("Tools Menu is null");
        return;
      }
      for (short i = 0; i < toolsMenu.getItemCount(); i++) {
        String command = toolsMenu.getCommand(toolsMenu.getItemId(i));
        if(WORD_COUNT_COMMAND.equals(command)) {
          ltId = toolsMenu.getItemId((short) (i - 1));
          ltMenu = toolsMenu.getPopupMenu(ltId);
          break;
        }
      }
      if (ltMenu == null) {
        MessageHandler.printToLogFile("LT Menu is null");
        return;
      }

      toolsMenu.addMenuListener(new XMenuListener() {

        @Override
        public void disposing(EventObject arg0) {
        }

        @Override
        public void itemActivated(MenuEvent arg0) {
        }

        @Override
        public void itemDeactivated(MenuEvent arg0) {
        }

        @Override
        public void itemHighlighted(MenuEvent event) {
          if(event.MenuId == ltId) {
            setLtMenu();
          }
        }

        @Override
        public void itemSelected(MenuEvent event) {
          if(event.MenuId == ltId) {
            setLtMenu();
          }
        }
      });
      if (debugMode) {
        MessageHandler.printToLogFile("Menu listener set");
      }

    }
    
    private void setLtMenu() {
      short switchOffId = 0;
      short switchOffPos = 0;
      for (short i = 0; i < ltMenu.getItemCount(); i++) {
        String command = ltMenu.getCommand(ltMenu.getItemId(i));
        if(LT_SWITCH_OFF_COMMAND.equals(command)) {
          switchOffId = ltMenu.getItemId(i);
          switchOffPos = i;
          break;
        }
      }
      if (switchOffId == 0) {
        MessageHandler.printToLogFile("switchOffId not found");
        return;
      }
      
      ltMenu.setItemText(switchOffId, messages.getString("loMenuSwitchOff"));
      
      if(switchOff) {
        ltMenu.checkItem(switchOffId, true);
      } else {
        ltMenu.checkItem(switchOffId, false);
      }
      short profilesId = (short)(switchOffId + 10);
      short profilesPos = (short)(switchOffPos + 2);
      setProfileMenu();
      if(xProfileMenu != null) {
        if(ltMenu.getItemId(profilesPos) != profilesId) {
          ltMenu.insertItem(profilesId, messages.getString("loMenuChangeProfiles"), MenuItemStyle.AUTOCHECK, profilesPos);
        }
        ltMenu.setPopupMenu(profilesId, xProfileMenu);
      } else if(ltMenu.getItemId(profilesPos) == profilesId) {
        ltMenu.removeItem(profilesPos, (short)1);
      }
      toolsMenu.setPopupMenu(ltId, ltMenu);
      menubar.setPopupMenu(toolsId, toolsMenu);
    }
      
    private void setProfileMenu() {
      List<String> profiles = null;
      if(config != null) {
        profiles = config.getDefinedProfiles();
      }
      if(profiles != null && !profiles.isEmpty()) {
        definedProfiles = profiles;
      }
      if(definedProfiles != null) {
        XPopupMenu xPopupMenu = OfficeTools.getPopupMenu(xContext);
        currentProfile = config.getCurrentProfile();
        xProfileMenu = getProfileMenu(xPopupMenu);
      }
    }
    
    private XPopupMenu getProfileMenu(XPopupMenu xPopupMenu) {
      if(xPopupMenu != null) {
        short nId = 1;
        short nPos = 0;
        xPopupMenu.insertItem(nId, messages.getString("guiUserProfile"), (short) 0, nPos);
        xPopupMenu.setCommand(nId, LT_PROFILE_COMMAND);
        if(currentProfile == null || currentProfile.isEmpty()) {
          xPopupMenu.enableItem(nId , false);
        } else {
          xPopupMenu.enableItem(nId , true);
        }
        for (int i = 0; i < definedProfiles.size(); i++) {
          nId++;
          nPos++;
          xPopupMenu.insertItem(nId, definedProfiles.get(i), (short) 0, nPos);
          xPopupMenu.setCommand(nId, LT_PROFILE_COMMAND + definedProfiles.get(i));
          if(currentProfile != null && currentProfile.equals(definedProfiles.get(i))) {
            xPopupMenu.enableItem(nId , false);
          } else {
            xPopupMenu.enableItem(nId , true);
          }
        }
        xPopupMenu.addMenuListener(this);
      }
      return xPopupMenu;
    }

    private void runProfileAction(String profile) {
      List<String> definedProfiles = config.getDefinedProfiles();
      if(profile != null && (definedProfiles == null || !definedProfiles.contains(profile))) {
        MessageHandler.showMessage("profile '" + profile + "' not found");
      } else {
        try {
          List<String> saveProfiles = new ArrayList<String>(); 
          saveProfiles.addAll(config.getDefinedProfiles());
          config.initOptions();
          config.loadConfiguration(profile == null ? "" : profile);
          config.setCurrentProfile(profile);
          config.addProfiles(saveProfiles);
          config.saveConfiguration(docLanguage);
          mainThread.resetDocument();
        } catch (IOException e) {
          MessageHandler.showError(e);
        }
      }
    }
    
    @Override
    public void itemSelected(MenuEvent event) {
      if(event.MenuId == 1) {
        runProfileAction(null);
      } else {
        runProfileAction(definedProfiles.get(event.MenuId - 2));
      }
    }
  
    @Override
    public void disposing(EventObject arg0) {
    }

    @Override
    public void itemActivated(MenuEvent arg0) {
    }

    @Override
    public void itemDeactivated(MenuEvent arg0) {
    }

    @Override
    public void itemHighlighted(MenuEvent arg0) {
    }
  }
  
}
