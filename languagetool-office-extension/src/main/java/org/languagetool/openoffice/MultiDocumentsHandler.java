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

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XModel;
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
  
  private static boolean debugMode = false;   //  should be false except for testing
  
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
  private LtDictionary dictionary;
  
  private XComponentContext xContext;       //  The context of the document
  private List<SingleDocument> documents;   //  The List of LO documents to be checked
  private XComponent goneContext = null;    //  save component of closed document
  private boolean recheck = true;
  private int docNum;                       //  number of the current document

  private boolean switchOff = false;        //  is LT switched off
  private boolean useQueue = true;          //  will be overwritten by config;

  private String menuDocId = null;          //  Id of document at which context menu was called 
  private TextLevelCheckQueue textLevelQueue = null; // Queue to check text level rules
  
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
    extraRemoteRules = new ArrayList<>();
    dictionary = new LtDictionary();
  }
  
  /**
   * distribute the check request to the concerned document
   */
  ProofreadingResult getCheckResults(String paraText, Locale locale, ProofreadingResult paRes, 
      PropertyValue[] propertyValues, boolean docReset) {
    
    if (!hasLocale(locale)) {
      return paRes;
    }
    if(!switchOff) {
      boolean isSameLanguage = true;
      if(fixedLanguage == null || langForShortName == null) {
        langForShortName = getLanguage(locale);
        isSameLanguage = langForShortName.equals(docLanguage);
      }
      if (!isSameLanguage || langTool == null || recheck) {
        if (!isSameLanguage) {
          docLanguage = langForShortName;
          extraRemoteRules.clear();
        }
        langTool = initLanguageTool();
        initCheck(langTool);
        initDocuments(locale);
      }
    }
    docNum = getNumDoc(paRes.aDocumentIdentifier);
    if(switchOff) {
      return paRes;
    }
    paRes = documents.get(docNum).getCheckResults(paraText, locale, paRes, propertyValues, docReset, langTool);
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
    return paRes;
  }

  /**
   * reset the Document
   */
  void resetDocument() {
    mainThread.resetDocument();
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
    boolean found = false;
    for (SingleDocument document : documents) {
      if (context.equals(document.getXComponent())) {
        found = true;
        document.dispose();
      }
    }
    if (!found) {
      MessageHandler.printToLogFile("Error: Disposed Document not found - Cache not deleted");
    }
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
    if (langTool == null) {
      if (docLanguage == null) {
        docLanguage = getLanguage();
      }
      langTool = initLanguageTool();
    }
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
   * proofs if test cases
   */
  boolean isTestMode() {
    return testMode;
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
   *  Set configuration Values for all documents
   */
  private void setConfigValues(Configuration config, SwJLanguageTool langTool) {
    this.config = config;
    this.langTool = langTool;
    this.useQueue = testMode ? false : config.useTextLevelQueue();
    for (SingleDocument document : documents) {
      document.setConfigValues(config);
    }
  }

  /**
   * Get language from locale
   */
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
    }
    SingleDocument newDocument = new SingleDocument(xContext, config, docID, xComponent, this);
    documents.add(newDocument);
    if (!testMode) {              //  xComponent == null for test cases 
      newDocument.setLanguage(docLanguage);
      newDocument.setLtMenus(new LanguageToolMenus(xContext, newDocument, config));
    }
    
    if (debugMode) {
      MessageHandler.printToLogFile("Document " + docNum + " created; docID = " + docID);
    }
    return documents.size() - 1;
  }

  /**
   * Delete a document number and all internal space
   */
  private void removeDoc(String docID) {
    for (int i = documents.size() - 1; i >= 0; i--) {
      if(!docID.equals(documents.get(i).getDocID()) && documents.get(i).isDisposed()) {
        if(useQueue && textLevelQueue != null) {
          MessageHandler.printToLogFile("Interrupt text level queue for document " + documents.get(i).getDocID());
          textLevelQueue.interruptCheck(documents.get(i).getDocID());
          MessageHandler.printToLogFile("Interrupt done");
        }
        if (goneContext != null) {
          XComponent xComponent = documents.get(i).getXComponent();
          if (xComponent != null && !xComponent.equals(goneContext)) {
            goneContext = null;
          }
        }
//        if (debugMode) {
          MessageHandler.printToLogFile("Disposed document " + documents.get(i).getDocID() + " removed");
//        }
        documents.remove(i);
      }
    }
  }
  
  /**
   * Delete the menu listener of a document
   */
  public void removeMenuListener(XComponent xComponent) {
    if(xComponent != null) {
      for (int i = 0; i < documents.size(); i++) {
        XComponent docComponent = documents.get(i).getXComponent();
        if (docComponent != null && xComponent.equals(docComponent)) { //  disposed document found
          documents.get(i).getLtMenu().removeListener();
          if (debugMode) {
            MessageHandler.printToLogFile("Menu listener of document " + documents.get(i).getDocID() + " removed");
          }
          break;
        }
      }
    }
  }

  /**
   * Initialize LanguageTool
   */
  SwJLanguageTool initLanguageTool() {
    return initLanguageTool(null);
  }

  SwJLanguageTool initLanguageTool(Language currentLanguage) {
    SwJLanguageTool langTool = null;
    try {
      linguServices = new LinguisticServices(xContext);
      config = new Configuration(configDir, configFile, oldConfigFile, docLanguage, linguServices);
      if (this.langTool == null) {
        OfficeTools.setLogLevel(config.getlogLevel());
        debugMode = OfficeTools.DEBUG_MODE_MD;
      }
      if(currentLanguage == null) {
        fixedLanguage = config.getDefaultLanguage();
        if(fixedLanguage != null) {
          docLanguage = fixedLanguage;
        }
        currentLanguage = docLanguage;
      }
      switchOff = config.isSwitchedOff();
      // not using MultiThreadedSwJLanguageTool here fixes "osl::Thread::Create failed", see https://bugs.documentfoundation.org/show_bug.cgi?id=90740:
      langTool = new SwJLanguageTool(currentLanguage, config.getMotherTongue(),
          new UserConfig(config.getConfigurableValues(), linguServices), config, extraRemoteRules, testMode);
      config.initStyleCategories(langTool.getAllRules());
      /* The next row is only for a single line break marks a paragraph
      docLanguage.getSentenceTokenizer().setSingleLineBreaksMarksParagraph(true);
       */
      File ngramDirectory = config.getNgramDirectory();
      if (ngramDirectory != null) {
        File ngramLangDir = new File(config.getNgramDirectory(), currentLanguage.getShortCode());
        if (ngramLangDir.exists()) {  // user might have ngram data only for some languages and that's okay
          langTool.activateLanguageModelRules(ngramDirectory);
        }
      }
      File word2VecDirectory = config.getWord2VecDirectory();
      if (word2VecDirectory != null) {
        File word2VecLangDir = new File(config.getWord2VecDirectory(), currentLanguage.getShortCode());
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
      recheck = false;
      return langTool;
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    return langTool;
  }

  /**
   * Enable or disable rules as given by configuration file
   */
  void initCheck(SwJLanguageTool langTool) {
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
  }
  
  /**
   * Initialize single documents, prepare text level rules and start queue
   */
  void initDocuments(Locale locale) {
    setConfigValues(config, langTool);
    sortedTextRules = new SortedTextRules();
    for (SingleDocument document : documents) {
      document.resetCache();
    }
    dictionary.setLtDictionary(xContext, locale, linguServices);
    if(useQueue) {
      if(textLevelQueue == null) {
        textLevelQueue = new TextLevelCheckQueue(this);
      } else {
        textLevelQueue.setReset();
      }
    }
  }

  /**
   * Get list of single documents
   */
  public List<SingleDocument> getDocuments() {
    return documents;
  }

  /**
   * Get text level queue
   */
  public TextLevelCheckQueue getTextLevelCheckQueue() {
    return textLevelQueue;
  }
  
  /**
   * true, if LanguageTool is switched off
   */
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
    if(!switchOff && textLevelQueue != null) {
      textLevelQueue.setStop();
      textLevelQueue = null;
    }
    recheck = true;
    config.setSwitchedOff(switchOff, docLanguage);
    for (SingleDocument document : documents) {
      document.setConfigValues(config);
    }
    return true;
  }

  /**
   * Set docID used within menu
   */
  public void setMenuDocId(String docId) {
    menuDocId = docId;
  }

  /**
   * Call method ignoreOnce for concerned document 
   */
  public String ignoreOnce() {
    for (SingleDocument document : documents) {
      if(menuDocId.equals(document.getDocID())) {
        return document.ignoreOnce();
      }
    }
    return null;
  }
  
  /**
   * reset ignoreOnce information in all documents
   */
  public void resetIgnoreOnce() {
    for (SingleDocument document : documents) {
      document.resetIgnoreOnce();
    }
  }

  /**
   * Deactivate a rule as requested by the context menu
   */
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
  
  /**
   * Returns a list of different numbers of paragraphs to check for text level rules
   * (currently only -1 for full text check and n for max number for other text level rules)
   */
  public List<Integer> getNumMinToCheckParas() {
    if(sortedTextRules == null) {
      return null;
    }
    return sortedTextRules.minToCheckParagraph;
  }

  /**
   * Test if sorted rules for index exist
   */
  public boolean isSortedRuleForIndex(int index) {
    if(index < 0 || index > 1 || sortedTextRules.textLevelRules.get(index).isEmpty()) {
      return false;
    }
    return true;
  }

  /**
   * activate all rules stored under a given index related to the list of getNumMinToCheckParas
   * deactivate all other text level rules
   */
  public void activateTextRulesByIndex(int index, SwJLanguageTool langTool) {
    sortedTextRules.activateTextRulesByIndex(index, langTool);
  }

  /**
   * reactivate all text level rules
   */
  public void reactivateTextRules(SwJLanguageTool langTool) {
    sortedTextRules.reactivateTextRules(langTool);;
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
      minToCheckParagraph = new ArrayList<>();
      textLevelRules = new ArrayList<>();
      minToCheckParagraph.add(0);
      textLevelRules.add(new ArrayList<>());
      if(useQueue && config.getNumParasToCheck() != 0) {
        minToCheckParagraph.add(config.getNumParasToCheck());
      } else {
        minToCheckParagraph.add(-1);
      }
      textLevelRules.add(new ArrayList<>());
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

    private void insertRule (int minPara, String ruleId) {
      if(useQueue) {
        if(minPara == 0) {
          textLevelRules.get(0).add(ruleId);
        } else {
          textLevelRules.get(1).add(ruleId);
        }
      } else {
        if(minPara < 0) {
          textLevelRules.get(1).add(ruleId);
        } else {
          if(minPara > minToCheckParagraph.get(0)) {
            minToCheckParagraph.set(0, minPara);
          }
          textLevelRules.get(0).add(ruleId);
        }
      }
    }

    public List<Integer> getMinToCheckParas() {
      return minToCheckParagraph;
    }

    public void activateTextRulesByIndex(int index, SwJLanguageTool langTool) {
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

    public void reactivateTextRules(SwJLanguageTool langTool) {
      for(List<String> textRules : textLevelRules) {
        for (String ruleId : textRules) {
          langTool.enableRule(ruleId);
        }
      }
    }

  }
  
  
}
