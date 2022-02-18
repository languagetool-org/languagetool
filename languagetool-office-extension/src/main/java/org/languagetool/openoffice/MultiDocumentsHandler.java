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
package org.languagetool.openoffice;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.UIManager;

import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.UserConfig;
import org.languagetool.gui.AboutDialog;
import org.languagetool.gui.Configuration;
import org.languagetool.openoffice.DocumentCache.TextParagraph;
import org.languagetool.openoffice.OfficeTools.DocumentType;
import org.languagetool.openoffice.SpellAndGrammarCheckDialog.LtCheckDialog;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.Rule;
import org.languagetool.tools.Tools;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XModel;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.linguistic2.LinguServiceEvent;
import com.sun.star.linguistic2.LinguServiceEventFlags;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.linguistic2.XLinguServiceEventListener;
import com.sun.star.linguistic2.XProofreader;
import com.sun.star.text.XTextDocument;
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
  
  private static final ResourceBundle messages = JLanguageTool.getMessageBundle();

  private static final int HEAP_CHECK_INTERVAL = 500;

  private final List<XLinguServiceEventListener> xEventListeners;

  private boolean docReset = false;

  private static boolean debugMode = false;   //  should be false except for testing
  
  private SwJLanguageTool lt = null;
  private Language docLanguage = null;
  private Language fixedLanguage = null;
  private Language langForShortName;
  private Locale locale;
  private final XEventListener xEventListener;
  private final XProofreader xProofreader;
  private final File configDir;
  private final File oldConfigFile;
  private String configFile;
  private Configuration config = null;
  private LinguisticServices linguServices = null;
  private SortedTextRules sortedTextRules;
  private Map<String, Set<String>> disabledRulesUI; //  Rules disabled by context menu or spell dialog
  private final List<Rule> extraRemoteRules;        //  store of rules supported by remote server but not locally
  private final LtDictionary dictionary;            //  internal dictionary of LT defined words 
  private LtCheckDialog ltDialog = null;            //  LT spelling and grammar check dialog
  private boolean dialogIsRunning = false;          //  The dialog was started     
  
  private XComponentContext xContext;               //  The context of the document
  private final List<SingleDocument> documents;     //  The List of LO documents to be checked
  private boolean isDisposed = false;
  private boolean recheck = true;                   //  if true: recheck the whole document at next iteration
  private int docNum;                               //  number of the current document
  
  private int numSinceHeapTest = 0;                 //  number of checks since last heap test
  private boolean heapLimitReached = false;         //  heap limit is reached

  private boolean noBackgroundCheck = false;        //  is LT switched off by config
  private boolean useQueue = true;                  //  will be overwritten by config

  private String menuDocId = null;                  //  Id of document at which context menu was called 
  private TextLevelCheckQueue textLevelQueue = null; // Queue to check text level rules
  
  private boolean useOrginalCheckDialog = false;    // use original spell and grammar dialog (LT check dialog does not work for OO)
  private boolean isNotTextDodument = false;
  private int heapCheckInterval = HEAP_CHECK_INTERVAL;
  private boolean testMode = false;
  

  MultiDocumentsHandler(XComponentContext xContext, XProofreader xProofreader, XEventListener xEventListener) {
    this.xContext = xContext;
    this.xEventListener = xEventListener;
    this.xProofreader = xProofreader;
    xEventListeners = new ArrayList<>();
    configFile = OfficeTools.CONFIG_FILE;
    configDir = OfficeTools.getLOConfigDir();
    oldConfigFile = OfficeTools.getOldConfigFile();
    MessageHandler.init();
    documents = new ArrayList<>();
    disabledRulesUI = new HashMap<>();
    extraRemoteRules = new ArrayList<>();
    dictionary = new LtDictionary();
  }
  
  /**
   * Runs the grammar checker on paragraph text.
   *
   * @param docID document ID
   * @param paraText paragraph text
   * @param locale Locale the text Locale
   * @param startOfSentencePos start of sentence position
   * @param nSuggestedBehindEndOfSentencePosition end of sentence position
   * @return ProofreadingResult containing the results of the check.
   */
  public final ProofreadingResult doProofreading(String docID,
      String paraText, Locale locale, int startOfSentencePos,
      int nSuggestedBehindEndOfSentencePosition,
      PropertyValue[] propertyValues) {
    ProofreadingResult paRes = new ProofreadingResult();
    paRes.nStartOfSentencePosition = startOfSentencePos;
    paRes.nStartOfNextSentencePosition = nSuggestedBehindEndOfSentencePosition;
    paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;
    paRes.xProofreader = xProofreader;
    paRes.aLocale = locale;
    paRes.aDocumentIdentifier = docID;
    paRes.aText = paraText;
    paRes.aProperties = propertyValues;
    try {
      paRes = getCheckResults(paraText, locale, paRes, propertyValues, docReset);
      docReset = false;
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    return paRes;
  }

  /**
   * distribute the check request to the concerned document
   */
  ProofreadingResult getCheckResults(String paraText, Locale locale, ProofreadingResult paRes, 
      PropertyValue[] propertyValues, boolean docReset) {
    if (lt == null) {
      setJavaLookAndFeel();
    }
    if (!hasLocale(locale)) {
      MessageHandler.printToLogFile("MultiDocumentsHandler: getCheckResults: Sorry, don't have locale: " + OfficeTools.localeToString(locale));
      return paRes;
    }
    if (!noBackgroundCheck) {
      boolean isSameLanguage = true;
      if (fixedLanguage == null || langForShortName == null) {
        langForShortName = getLanguage(locale);
        isSameLanguage = langForShortName.equals(docLanguage) && lt != null;
      }
      if (!isSameLanguage || recheck) {
        boolean initDocs = lt == null || recheck;
        if (!isSameLanguage) {
          docLanguage = langForShortName;
          this.locale = locale;
          extraRemoteRules.clear();
        }
        if (lt == null) {
          testFootnotes(propertyValues);
        }
        lt = initLanguageTool(!isSameLanguage);
        initCheck(lt);
        if (initDocs) {
          initDocuments();
        }
      }
    }
    if (debugMode) {
      MessageHandler.printToLogFile("MultiDocumentsHandler: getCheckResults: Start getNumDoc!");
    }
    docNum = getNumDoc(paRes.aDocumentIdentifier, propertyValues);
    if (noBackgroundCheck) {
      return paRes;
    }
    if (debugMode) {
      MessageHandler.printToLogFile("MultiDocumentsHandler: getCheckResults: Start testHeapSpace!");
    }
    testHeapSpace();
    if (debugMode) {
      MessageHandler.printToLogFile("MultiDocumentsHandler: getCheckResults: Start getCheckResults at single document!");
    }
    paRes = documents.get(docNum).getCheckResults(paraText, locale, paRes, propertyValues, docReset, lt);
    if (lt.doReset()) {
      // langTool.doReset() == true: if server connection is broken ==> switch to internal check
      MessageHandler.showMessage(messages.getString("loRemoteSwitchToLocal"));
      config.setRemoteCheck(false);
      try {
        config.saveConfiguration(docLanguage);
      } catch (IOException e) {
        MessageHandler.showError(e);
      }
      resetDocument();
    }
    if (debugMode) {
      MessageHandler.printToLogFile("MultiDocumentsHandler: getCheckResults: return to LO/OO!");
    }
    return paRes;
  }

  /**
   *  Get the current used document
   */
  public SingleDocument getCurrentDocument() {
    XComponent xComponent = OfficeTools.getCurrentComponent(xContext);
    isNotTextDodument = false;
    if (xComponent != null) {
      for (SingleDocument document : documents) {
        if (xComponent.equals(document.getXComponent())) {
          return document;
        }
      }
      XTextDocument curDoc = UnoRuntime.queryInterface(XTextDocument.class, xComponent);
      if (curDoc == null) {
        String prefix = null;
        if (OfficeDrawTools.isImpressDocument(xComponent)) {
          prefix = "I";
        } else if (OfficeSpreadsheetTools.isSpreadsheetDocument(xComponent)) {
          prefix = "C";
        }
        if (prefix != null) {
          String docID = createOtherDocId(prefix);
          try {
            xComponent.addEventListener(xEventListener);
          } catch (Throwable t) {
            MessageHandler.printToLogFile("MultiDocumentsHandler: getCurrentDocument: Error: Document (ID: " + docID + ") has no XComponent -> Internal space will not be deleted when document disposes");
            xComponent = null;
          }
          SingleDocument newDocument = new SingleDocument(xContext, config, docID, xComponent, this);
          documents.add(newDocument);
          MessageHandler.printToLogFile("Document " + (documents.size() - 1) + " created; docID = " + docID);
          return newDocument;
        }
        MessageHandler.printToLogFile("MultiDocumentsHandler: getCurrentDocument: Is document, but not a text document!");
        isNotTextDodument = true;
      }
    }
    return null;
  }
  
  /**
   * create new Impress document id
   */
  private String createOtherDocId(String prefix) {
    String docID;
    if (documents.size() == 0) {
      return prefix + "1";
    }
    for (int n = 1; n < documents.size() + 1; n++) {
      docID = prefix + n;
      boolean isValid = true;
      for (SingleDocument document : documents) {
        if (docID.equals(document.getDocID())) {
          isValid = false;
          break;
        }
      }
      if (isValid) {
        return docID;
      }
    }
    return null;
  }
  
  /**
   * return true, if a document was found but is not a text document
   */
  boolean isNotTextDocument() {
    return isNotTextDodument;
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
   *  Set pointer to LT spell and grammar check dialog
   */
  public void setLtDialog(LtCheckDialog dialog) {
    ltDialog = dialog;
  }
  
  /**
   *  Set Information LT spell and grammar check dialog was started
   */
  public void setLtDialogIsRunning(boolean running) {
    this.dialogIsRunning = running;
  }
  
  /**
   *  Set Information LT spell and grammar check dialog was started
   */
  public void setConfigFileName(String name) {
    configFile = name;
  }
  
  /**
   *  Set a document as closed
   */
  private void setContextOfClosedDoc(XComponent context) {
    boolean found = false;
    for (SingleDocument document : documents) {
      if (context.equals(document.getXComponent())) {
        found = true;
        document.dispose(true);
        isDisposed = true;
        if (config.saveLoCache()) {
          document.writeCaches();
        }
        document.setXComponent(xContext, null);
        if (document.getDocumentCache().hasNoContent()) {
          //  The delay seems to be necessary as workaround for a GDK bug (Linux) to stabilizes
          //  the load of a document from an empty document 
          MessageHandler.printToLogFile("Disposing document has no content: Wait for 1000 milliseconds");
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            MessageHandler.printException(e);
          }
        }
      }
    }
    if (!found) {
      MessageHandler.printToLogFile("MultiDocumentsHandler: setContextOfClosedDoc: Error: Disposed Document not found - Cache not deleted");
    }
  }
  
  /**
   *  Add a rule to disabled rules by context menu or spell dialog
   */
  void addDisabledRule(String langCode, String ruleId) {
    if (disabledRulesUI.containsKey(langCode)) {
      disabledRulesUI.get(langCode).add(ruleId);
    } else {
      Set<String >rulesIds = new HashSet<>();
      rulesIds.add(ruleId);
      disabledRulesUI.put(langCode, rulesIds);
    }
  }
  
  /**
   *  Remove a rule from disabled rules by spell dialog
   */
  void removeDisabledRule(String ruleId) {
    disabledRulesUI.remove(ruleId);
  }
  
  /**
   *  remove all disabled rules by context menu or spell dialog
   */
  void resetDisabledRules() {
    disabledRulesUI = new HashMap<>();
  }
  
  /**
   *  get disabled rules for a language code by context menu or spell dialog
   */
  Set<String> getDisabledRules(String langCode) {
    if (langCode == null || !disabledRulesUI.containsKey(langCode)) {
      return new HashSet<String>();
    }
    return disabledRulesUI.get(langCode);
  }
  
  /**
   *  get all disabled rules
   */
  Map<String, Set<String>> getAllDisabledRules() {
    return disabledRulesUI;
  }
  
  /**
   *  get all disabled rules
   */
  void setAllDisabledRules(Map<String, Set<String>> disabledRulesUI) {
    this.disabledRulesUI = disabledRulesUI;
  }
  
  /**
   *  get all disabled rules by context menu or spell dialog
   */
  Map<String, String> getDisabledRulesMap(String langCode) {
    if (langCode == null) {
      langCode = OfficeTools.localeToString(locale);
    }
    Map<String, String> disabledRulesMap = new HashMap<>();
    List<Rule> allRules = lt.getAllRules();
    for (String disabledRule : getDisabledRules(langCode)) {
      String ruleDesc = null;
      for (Rule rule : allRules) {
        if (disabledRule.equals(rule.getId())) {
          ruleDesc = rule.getDescription();
          break;
        }
      }
      if (ruleDesc != null) {
        disabledRulesMap.put(disabledRule, ruleDesc);
      }
    }
    for (String disabledRule : config.getDisabledRuleIds()) {
      String ruleDesc = null;
      for (Rule rule : allRules) {
        if (disabledRule.equals(rule.getId())) {
          ruleDesc = rule.getDescription();
          break;
        }
      }
      if (ruleDesc != null) {
        disabledRulesMap.put(disabledRule, ruleDesc);
      }
    }
    return disabledRulesMap;
  }
  
  /**
   *  set disabled rules by context menu or spell dialog
   */
  void setDisabledRules(String langCode, Set<String> ruleIds) {
    disabledRulesUI.put(langCode, new HashSet<>(ruleIds));
  }
  
  /**
   *  get LanguageTool
   */
  SwJLanguageTool getLanguageTool() {
    if (lt == null) {
      if (docLanguage == null) {
        docLanguage = getLanguage();
      }
      lt = initLanguageTool();
    }
    return lt;
  }
  
  /**
   *  get Configuration
   */
  Configuration getConfiguration() {
    try {
      if (config == null || recheck) {
        if (docLanguage == null) {
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
    MessageHandler.setTestMode(mode);
    if (mode) {
      configFile = "dummy_xxxx.cfg";
      File dummy = new File(configDir, configFile);
      if (dummy.exists()) {
        dummy.delete();
      }
    }
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
  public Language getLanguage() {
    Locale locale = getDocumentLocale();
    if (locale == null) {
      locale = new Locale("en","US","");
    }
    if (!hasLocale(locale)) {
      String message = Tools.i18n(messages, "language_not_supported", locale.Language);
      MessageHandler.showMessage(message);
      return null;
    }
    return getLanguage(locale);
  }
  
  /**
   * Checks the language under the cursor. Used for opening the configuration dialog.
   * @return the locale under the visible cursor
   */
  @Nullable
  public Locale getDocumentLocale() {
    if (xContext == null) {
      return null;
    }
    XComponent xComponent = OfficeTools.getCurrentComponent(xContext);
    if (xComponent == null) {
      return null;
    }
    Locale charLocale;
    XPropertySet xCursorProps;
    try {
      XModel model = UnoRuntime.queryInterface(XModel.class, xComponent);
      if (model == null) {
        return null;
      }
      XTextViewCursorSupplier xViewCursorSupplier =
          UnoRuntime.queryInterface(XTextViewCursorSupplier.class, model.getCurrentController());
      if (xViewCursorSupplier == null) {
        return null;
      }
      XTextViewCursor xCursor = xViewCursorSupplier.getViewCursor();
      if (xCursor == null) {
        return null;
      }
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
        return new Locale("km", "", "");
      }
      if (new TamilDetector().isThisLanguage(xCursor.getText().getString())) {
        return new Locale("ta","","");
      }
      if (xCursorProps == null) {
        return null;
      }
      Object obj = xCursorProps.getPropertyValue("CharLocale");
      if (obj == null) {
        return null;
      }
      charLocale = (Locale) obj;
    } catch (Throwable t) {
      MessageHandler.showError(t);
      return null;
    }
    return charLocale;
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
  private void setConfigValues(Configuration config, SwJLanguageTool lt) {
    this.config = config;
    this.lt = lt;
    if (textLevelQueue != null && (heapLimitReached || config.getNumParasToCheck() == 0)) {
      textLevelQueue.setStop();
      textLevelQueue = null;
    }
    useQueue = noBackgroundCheck || heapLimitReached || testMode || config.getNumParasToCheck() == 0 ? false : config.useTextLevelQueue();
    for (SingleDocument document : documents) {
      if (!document.isDisposed()) {
        document.setConfigValues(config);
      }
    }
  }

  /**
   * Get language from locale
   */
  public Language getLanguage(Locale locale) {
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
  private int getNumDoc(String docID, PropertyValue[] propertyValues) {
    for (int i = 0; i < documents.size(); i++) {
      if (documents.get(i).getDocID().equals(docID)) {  //  document exist
        if (!testMode && documents.get(i).getXComponent() == null) {
          XComponent xComponent = OfficeTools.getCurrentComponent(xContext);
          if (xComponent == null) {
            MessageHandler.printToLogFile("MultiDocumentsHandler: getNumDoc: Error: Document (ID: " + docID + ") has no XComponent -> Internal space will not be deleted when document disposes");
          } else {
            try {
              xComponent.addEventListener(xEventListener);
            } catch (Throwable t) {
              MessageHandler.printToLogFile("MultiDocumentsHandler: getNumDoc: Error: Document (ID: " + docID + ") has no XComponent -> Internal space will not be deleted when document disposes");
              xComponent = null;
            }
            if (xComponent != null) {
              documents.get(i).setXComponent(xContext, xComponent);
              MessageHandler.printToLogFile("MultiDocumentsHandler: getNumDoc: Fixed: XComponent set for Document (ID: " + docID + ")");
            }
          }
        }
        if (isDisposed) {
          int n = removeDoc(docID);
          if (n >= 0 && n < i) {
            return i - 1;
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
        MessageHandler.printToLogFile("MultiDocumentsHandler: getNumDoc: Error: Document (ID: " + docID + ") has no XComponent -> Internal space will not be deleted when document disposes");
      } else {
        for (int i = 0; i < documents.size(); i++) {
          //  work around to compensate a bug at LO
          if (xComponent.equals(documents.get(i).getXComponent())) {
            MessageHandler.printToLogFile("Different Doc IDs, but same xComponents!");
            String oldDocId = documents.get(i).getDocID();
            documents.get(i).setDocID(docID);
            MessageHandler.printToLogFile("MultiDocumentsHandler: getNumDoc: Document ID corrected: old: " + oldDocId + ", new: " + docID);
            if (useQueue && textLevelQueue != null) {
              MessageHandler.printToLogFile("MultiDocumentsHandler: getNumDoc: Interrupt text level queue for old document ID: " + oldDocId);
              textLevelQueue.interruptCheck(oldDocId, true);
              MessageHandler.printToLogFile("MultiDocumentsHandler: getNumDoc: Interrupt done");
            }
            if (documents.get(i).isDisposed()) {
              documents.get(i).dispose(false);;
            }
            return i;
          }
        }
        try {
          xComponent.addEventListener(xEventListener);
        } catch (Throwable t) {
          MessageHandler.printToLogFile("MultiDocumentsHandler: getNumDoc: Error: Document (ID: " + docID + ") has no XComponent -> Internal space will not be deleted when document disposes");
          xComponent = null;
        }
      }
    }
    SingleDocument newDocument = new SingleDocument(xContext, config, docID, xComponent, this);
    documents.add(newDocument);
    if (!testMode) {              //  xComponent == null for test cases 
      newDocument.setLanguage(docLanguage);
    }
    if (isDisposed) {
      removeDoc(docID);
    }
    MessageHandler.printToLogFile("MultiDocumentsHandler: getNumDoc: Document " + (documents.size() - 1) + " created; docID = " + docID);
    return documents.size() - 1;
  }

  /**
   * Delete a document number and all internal space
   */
  private int removeDoc(String docID) {
    if (isDisposed) {
      isDisposed = false;
      for (int i = documents.size() - 1; i >= 0; i--) {
        if (!docID.equals(documents.get(i).getDocID())) {
          if (documents.get(i).isDisposed()) {
            if (useQueue && textLevelQueue != null) {
              MessageHandler.printToLogFile("MultiDocumentsHandler: removeDoc: Interrupt text level queue for document " + documents.get(i).getDocID());
              textLevelQueue.interruptCheck(documents.get(i).getDocID(), true);
              MessageHandler.printToLogFile("MultiDocumentsHandler: removeDoc: Interrupt done");
            }
            MessageHandler.printToLogFile("Disposed document " + documents.get(i).getDocID() + " removed");
            documents.remove(i);
            for (int j = 0; j < documents.size(); j++) {
              if (documents.get(j).isDisposed()) {
                isDisposed = true;
              }
            }
            return (i);
          }
        }
      }
    }
    return (-1);
  }
  
  /**
   * Delete the menu listener of a document
   */
  public void removeMenuListener(XComponent xComponent) {
    if (xComponent != null) {
      for (int i = 0; i < documents.size(); i++) {
        XComponent docComponent = documents.get(i).getXComponent();
        if (docComponent != null && xComponent.equals(docComponent)) { //  disposed document found
          documents.get(i).getLtMenu().removeListener();
          if (debugMode) {
            MessageHandler.printToLogFile("MultiDocumentsHandler: removeMenuListener: Menu listener of document " + documents.get(i).getDocID() + " removed");
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
    return initLanguageTool(null, false);
  }

  SwJLanguageTool initLanguageTool(boolean setService) {
    return initLanguageTool(null, setService);
  }

  SwJLanguageTool initLanguageTool(Language currentLanguage, boolean setService) {
    SwJLanguageTool lt = null;
    try {
      config = new Configuration(configDir, configFile, oldConfigFile, docLanguage, true);
      noBackgroundCheck = config.noBackgroundCheck();
      if (linguServices == null) {
        linguServices = new LinguisticServices(xContext);
        Tools.setLinguisticServices(linguServices);
      }
      linguServices.setNoSynonymsAsSuggestions(config.noSynonymsAsSuggestions() || testMode);
      if (this.lt == null) {
        OfficeTools.setLogLevel(config.getlogLevel());
        debugMode = OfficeTools.DEBUG_MODE_MD;
      }
      if (currentLanguage == null) {
        fixedLanguage = config.getDefaultLanguage();
        if (fixedLanguage != null) {
          docLanguage = fixedLanguage;
        }
        currentLanguage = docLanguage;
      }
      // not using MultiThreadedSwJLanguageTool here fixes "osl::Thread::Create failed", see https://bugs.documentfoundation.org/show_bug.cgi?id=90740:
      lt = new SwJLanguageTool(currentLanguage, config.getMotherTongue(),
          new UserConfig(config.getConfigurableValues(), linguServices), config, extraRemoteRules, testMode);
      config.initStyleCategories(lt.getAllRules());
      /* The next row is only for a single line break marks a paragraph
      docLanguage.getSentenceTokenizer().setSingleLineBreaksMarksParagraph(true);
       */
      File ngramDirectory = config.getNgramDirectory();
      if (ngramDirectory != null) {
        File ngramLangDir = new File(config.getNgramDirectory(), currentLanguage.getShortCode());
        if (ngramLangDir.exists()) {  // user might have ngram data only for some languages and that's okay
          lt.activateLanguageModelRules(ngramDirectory);
          if (debugMode) {
            MessageHandler.printToLogFile("MultiDocumentsHandler: initLanguageTool: ngram Model activated for language: " + currentLanguage.getShortCode());
          }
        }
      }
      File word2VecDirectory = config.getWord2VecDirectory();
      if (word2VecDirectory != null) {
        File word2VecLangDir = new File(config.getWord2VecDirectory(), currentLanguage.getShortCode());
        if (word2VecLangDir.exists()) {  // user might have word2vec data only for some languages and that's okay
          lt.activateWord2VecModelRules(word2VecDirectory);
        }
      }
      for (Rule rule : lt.getAllActiveOfficeRules()) {
        if (rule.isDictionaryBasedSpellingRule()) {
          lt.disableRule(rule.getId());
          if (rule.useInOffice()) {
            // set default off so it can be re-enabled by user configuration
            rule.setDefaultOff();
          }
        }
      }
      recheck = false;
      return lt;
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    return lt;
  }

  /**
   * Enable or disable rules as given by configuration file
   */
  void initCheck(SwJLanguageTool lt) {
    Set<String> disabledRuleIds = config.getDisabledRuleIds();
    if (disabledRuleIds != null) {
      // copy as the config thread may access this as well
      List<String> list = new ArrayList<>(disabledRuleIds);
      for (String id : list) {
        lt.disableRule(id);
      }
    }
    Set<String> disabledCategories = config.getDisabledCategoryNames();
    if (disabledCategories != null) {
      // copy as the config thread may access this as well
      List<String> list = new ArrayList<>(disabledCategories);
      for (String categoryName : list) {
        lt.disableCategory(new CategoryId(categoryName));
      }
    }
    Set<String> enabledRuleIds = config.getEnabledRuleIds();
    if (enabledRuleIds != null) {
      // copy as the config thread may access this as well
      List<String> list = new ArrayList<>(enabledRuleIds);
      for (String ruleName : list) {
        lt.enableRule(ruleName);
      }
    }
    Set<String> disabledLocaleRules = getDisabledRules(lt.getLanguage().getShortCodeWithCountryAndVariant());
    if (disabledLocaleRules != null) {
      for (String id : disabledLocaleRules) {
        lt.disableRule(id);
      }
    }
    handleLtDictionary();
  }
  
  /**
   * Initialize single documents, prepare text level rules and start queue
   */
  void initDocuments() {
    setConfigValues(config, lt);
    String langCode = lt.getLanguage().getShortCodeWithCountryAndVariant();
    sortedTextRules = new SortedTextRules(lt, config, getDisabledRules(langCode));
    if (useQueue && !noBackgroundCheck) {
      if (textLevelQueue == null) {
        textLevelQueue = new TextLevelCheckQueue(this);
      } else {
        textLevelQueue.setReset();
      }
    }
    for (SingleDocument document : documents) {
      document.resetCache();
    }
  }
  
  /**
   * Reset ignored matches
   */
  void resetIgnoredMatches() {
    for (SingleDocument document : documents) {
      document.resetIgnoreOnce();
    }
  }

  /**
   * Reset document caches
   */
  void resetDocumentCaches() {
    for (SingleDocument document : documents) {
      document.resetDocumentCache();
    }
  }

  /**
   * Get current locale language
   */
  public Locale getLocale() {
    return locale;
  }
  
  /**
   * Get dictionary access
   */
  public LtDictionary getLtDictionary() {
    return dictionary;
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
    return noBackgroundCheck;
  }

  /**
   *  Toggle Switch Off / On of LT
   *  return true if toggle was done 
   */
  public boolean toggleSwitchedOff() throws IOException {
    if (docLanguage == null) {
      docLanguage = getLanguage();
    }
    if (config == null) {
      config = new Configuration(configDir, configFile, oldConfigFile, docLanguage, true);
    }
    noBackgroundCheck = !noBackgroundCheck;
    if (!noBackgroundCheck && textLevelQueue != null) {
      textLevelQueue.setStop();
      textLevelQueue = null;
    }
    recheck = true;
    config.saveNoBackgroundCheck(noBackgroundCheck, docLanguage);
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
   * Set use original spell und grammar dialog (for OO and old LO)
   */
  public void setUseOriginalCheckDialog() {
    useOrginalCheckDialog = true;
  }
  
  /**
   * Set use original spell und grammar dialog (for OO and old LO)
   */
  public boolean useOriginalCheckDialog() {
    return useOrginalCheckDialog;
  }
  
  /**
   * Is true if footnotes exist (tests if OO or very old LO) 
   */
  private void testFootnotes(PropertyValue[] propertyValues) {
    for (PropertyValue propertyValue : propertyValues) {
      if ("FootnotePositions".equals(propertyValue.Name)) {
        return;
      }
    }
    //  OO and LO < 4.3 do not support 'FootnotePositions' property and other advanced features
    //  switch back to single paragraph check mode
    //  use OOO configuration file - save existing settings if not already done
    useOrginalCheckDialog = true;
    File ooConfigFile = new File(configDir, OfficeTools.OOO_CONFIG_FILE);
    if (!ooConfigFile.exists()) {
      File loConfigFile = new File(configDir, configFile);
      if (loConfigFile.exists()) {
        try {
          Configuration tmpConfig = new Configuration(configDir, configFile, oldConfigFile, docLanguage, true);
          tmpConfig.setConfigFile(ooConfigFile);
          tmpConfig.setNumParasToCheck(0);
          tmpConfig.setUseTextLevelQueue(false);
          tmpConfig.saveConfiguration(docLanguage);
        } catch (IOException e) {
          MessageHandler.showError(e);
        }
      }
    }
    configFile = OfficeTools.OOO_CONFIG_FILE;
    MessageHandler.printToLogFile("No support of Footnotes: Open Office assumed - Single paragraph check mode set!");
  }

  /**
   * Call method ignoreOnce for concerned document 
   */
  public String ignoreOnce() {
    for (SingleDocument document : documents) {
      if (menuDocId.equals(document.getDocID())) {
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
   * Deactivate a rule by rule iD
   */
  public void activateRule(String ruleId) {
    if (ruleId != null) {
      removeDisabledRule(ruleId);
      deactivateRule(ruleId, true);
      resetDocument();
    }
  }
  
  /**
   * Deactivate a rule as requested by the context menu
   */
  public void deactivateRule() {
    for (SingleDocument document : documents) {
      if (menuDocId.equals(document.getDocID())) {
        deactivateRule(document.deactivateRule(), false);
        return;
      }
    }
  }
  
  /**
   * Deactivate a rule by rule iD
   */
  public void deactivateRule(String ruleId, boolean reactivate) {
    if (ruleId != null) {
      try {
        Configuration confg = new Configuration(configDir, configFile, oldConfigFile, docLanguage, true);
        Set<String> ruleIds = new HashSet<>();
        ruleIds.add(ruleId);
        if (reactivate) {
          confg.removeDisabledRuleIds(ruleIds);
        } else {
          confg.addDisabledRuleIds(ruleIds);
        }
        confg.saveConfiguration(docLanguage);
        if (debugMode) {
          MessageHandler.printToLogFile("MultiDocumentsHandler: deactivateRule: Rule Disabled: " + (ruleId == null ? "null" : ruleId));
        }
      } catch (IOException e) {
        MessageHandler.printException(e);
      }
    }
  }
  
  /**
   * reset sorted text level rules
   */
  public void resetSortedTextRules() {
    String langCode = lt.getLanguage().getShortCodeWithCountryAndVariant();
    sortedTextRules = new SortedTextRules(lt, config, getDisabledRules(langCode));
  }

  /**
   * Returns a list of different numbers of paragraphs to check for text level rules
   */
  public List<Integer> getNumMinToCheckParas() {
    if (sortedTextRules == null) {
      return null;
    }
    return sortedTextRules.minToCheckParagraph;
  }

  /**
   * Test if sorted rules for index exist
   */
  public boolean isSortedRuleForIndex(int index) {
    if (index < 0 || index >= sortedTextRules.textLevelRules.size() || sortedTextRules.textLevelRules.get(index).isEmpty()) {
      return false;
    }
    return true;
  }

  /**
   * activate all rules stored under a given index related to the list of getNumMinToCheckParas
   * deactivate all other text level rules
   */
  public void activateTextRulesByIndex(int index, SwJLanguageTool lt) {
    sortedTextRules.activateTextRulesByIndex(index, lt);
  }

  /**
   * reactivate all text level rules
   */
  public void reactivateTextRules(SwJLanguageTool lt) {
    sortedTextRules.reactivateTextRules(lt);
  }

  /**
   * We leave spell checking to OpenOffice/LibreOffice.
   * @return false
   */
  public final boolean isSpellChecker() {
    return false;
  }
  
  /**
   * Returns xContext
   */
  public XComponentContext getContext() {
    return xContext;
  }

  /**
   * Runs LT options dialog box.
   */
  public void runOptionsDialog() {
    try {
      Configuration config = getConfiguration();
      Language lang = config.getDefaultLanguage();
      if (lang == null) {
        lang = getLanguage();
      }
      if (lang == null) {
        return;
      }
      SwJLanguageTool lTool = lt;
      if (!lang.equals(docLanguage)) {
        docLanguage = lang;
        lTool = initLanguageTool();
        initCheck(lTool);
        config = this.config;
      }
      ConfigThread configThread = new ConfigThread(lang, config, lTool, this);
      configThread.start();
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
  }

  /**
   * @return An array of Locales supported by LT
   */
  public final static Locale[] getLocales() {
    try {
      List<Locale> locales = new ArrayList<>();
      Locale locale = null;
      for (Language lang : Languages.get()) {
        if (lang.getCountries().length == 0) {
          if (lang.getDefaultLanguageVariant() != null) {
            if (lang.getDefaultLanguageVariant().getVariant() != null) {
              locale = new Locale(lang.getDefaultLanguageVariant().getShortCode(),
                  lang.getDefaultLanguageVariant().getCountries()[0], lang.getDefaultLanguageVariant().getVariant());
            } else {
              locale = new Locale(lang.getDefaultLanguageVariant().getShortCode(),
                  lang.getDefaultLanguageVariant().getCountries()[0], "");
            }
          }
          else if (lang.getVariant() != null) {  // e.g. Esperanto
            locale =new Locale(LIBREOFFICE_SPECIAL_LANGUAGE_TAG, "", lang.getShortCodeWithCountryAndVariant());
          } else {
            locale = new Locale(lang.getShortCode(), "", "");
          }
          if (locales != null && !OfficeTools.containsLocale(locales, locale)) {
            locales.add(locale);
          }
        } else {
          for (String country : lang.getCountries()) {
            if (lang.getVariant() != null) {
              locale = new Locale(LIBREOFFICE_SPECIAL_LANGUAGE_TAG, country, lang.getShortCodeWithCountryAndVariant());
            } else {
              locale = new Locale(lang.getShortCode(), country, "");
            }
            if (locales != null && !OfficeTools.containsLocale(locales, locale)) {
              locales.add(locale);
            }
          }
        }
      }
      return locales == null ? new Locale[0] : locales.toArray(new Locale[0]);
    } catch (Throwable t) {
      MessageHandler.showError(t);
      return new Locale[0];
    }
  }

  /**
   * Add a listener that allow re-checking the document after changing the
   * options in the configuration dialog box.
   * 
   * @param eventListener the listener to be added
   * @return true if listener is non-null and has been added, false otherwise
   */
  public final boolean addLinguServiceEventListener(XLinguServiceEventListener eventListener) {
    if (eventListener == null) {
      return false;
    }
    xEventListeners.add(eventListener);
    return true;
  }

  /**
   * Remove a listener from the event listeners list.
   * 
   * @param eventListener the listener to be removed
   * @return true if listener is non-null and has been removed, false otherwise
   */
  public final boolean removeLinguServiceEventListener(XLinguServiceEventListener eventListener) {
    if (eventListener == null) {
      return false;
    }
    if (xEventListeners.contains(eventListener)) {
      xEventListeners.remove(eventListener);
      return true;
    }
    return false;
  }

  /**
   * Inform listener that the document should be rechecked for grammar and style check.
   */
  public boolean resetCheck() {
    return resetCheck(LinguServiceEventFlags.PROOFREAD_AGAIN);
  }

  /**
   * Inform listener that the doc should be rechecked for a special event flag.
   */
  public boolean resetCheck(short eventFlag) {
    if (!xEventListeners.isEmpty()) {
      for (XLinguServiceEventListener xEvLis : xEventListeners) {
        if (xEvLis != null) {
          LinguServiceEvent xEvent = new LinguServiceEvent();
          xEvent.nEvent = eventFlag;
          xEvLis.processLinguServiceEvent(xEvent);
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Configuration has be changed
   */
  void resetConfiguration() {
    linguServices = null;
    noBackgroundCheck = false;
    resetIgnoredMatches();
    resetDocument();
  }

  /**
   * Inform listener (grammar checking iterator) that options have changed and
   * the doc should be rechecked.
   */
  void resetDocument() {
    setRecheck();
    resetCheck();
  }

  /**
   * Triggers the events from LT menu
   */
  public void trigger(String sEvent) {
    try {
      if (!testDocLanguage(true)) {
        MessageHandler.printToLogFile("Test for document language failed: Can't trigger event: " + sEvent);
        return;
      }
      if ("configure".equals(sEvent)) {
        runOptionsDialog();
      } else if ("about".equals(sEvent)) {
        AboutDialogThread aboutThread = new AboutDialogThread(messages);
        aboutThread.start();
      } else if ("switchOff".equals(sEvent)) {
        if (toggleSwitchedOff()) {
          resetCheck(); 
        }
      } else if ("ignoreOnce".equals(sEvent)) {
        ignoreOnce();
      } else if ("deactivateRule".equals(sEvent)) {
        deactivateRule();
        resetDocument();
      } else if (sEvent.startsWith("activateRule_")) {
        String ruleId = sEvent.substring(13);
        activateRule(ruleId);
      } else if ("checkDialog".equals(sEvent) || "checkAgainDialog".equals(sEvent)) {
        if (useOrginalCheckDialog) {
          if ("checkDialog".equals(sEvent) ) {
            OfficeTools.dispatchCmd(".uno:SpellingAndGrammarDialog", xContext);
          } else {
            OfficeTools.dispatchCmd(".uno:RecheckDocument", xContext);
          }
          return;
        }
        if (ltDialog != null) {
          ltDialog.closeDialog();
        } 
        if (dialogIsRunning) {
          return;
        }
        setLtDialogIsRunning(true);
        SpellAndGrammarCheckDialog checkDialog = new SpellAndGrammarCheckDialog(xContext, this, docLanguage);
        if ("checkAgainDialog".equals(sEvent)) {
          SingleDocument document = getCurrentDocument();
          if (document != null) {
            XComponent currentComponent = document.getXComponent();
            if (currentComponent != null) {
              if (document.getDocumentType() == DocumentType.WRITER) {
                ViewCursorTools viewCursor = new ViewCursorTools(xContext);
                SpellAndGrammarCheckDialog.setTextViewCursor(0, new TextParagraph (DocumentCache.CURSOR_TYPE_TEXT ,0), viewCursor);
              } else if (document.getDocumentType() == DocumentType.IMPRESS){
                OfficeDrawTools.setCurrentPage(0, currentComponent);
              } else {
                OfficeSpreadsheetTools.setCurrentSheet(0, currentComponent);
              }
            }
          }
          resetIgnoredMatches();
          resetCheck();
        }
        if (debugMode) {
          MessageHandler.printToLogFile("MultiDocumentsHandler: trigger: Start Spell And Grammar Check Dialog");
        }
        checkDialog.start();
      } else if ("nextError".equals(sEvent)) {
        if (this.isSwitchedOff()) {
          MessageHandler.showMessage(messages.getString("loExtSwitchOffMessage"));
          return;
        }
        SpellAndGrammarCheckDialog checkDialog = new SpellAndGrammarCheckDialog(xContext, this, docLanguage);
        checkDialog.nextError();
      } else if ("refreshCheck".equals(sEvent)) {
        if (this.isSwitchedOff()) {
          MessageHandler.showMessage(messages.getString("loExtSwitchOffMessage"));
          return;
        }
        resetIgnoredMatches();
        resetDocumentCaches();
        resetDocument();
      } else if ("remoteHint".equals(sEvent)) {
        if (getConfiguration().useOtherServer()) {
          MessageHandler.showMessage(MessageFormat.format(messages.getString("loRemoteInfoOtherServer"), 
              getConfiguration().getServerUrl()));
        } else {
          MessageHandler.showMessage(messages.getString("loRemoteInfoDefaultServer"));
        }
      } else {
        MessageHandler.printToLogFile("MultiDocumentsHandler: trigger: Sorry, don't know what to do, sEvent = " + sEvent);
      }
    } catch (Throwable e) {
      MessageHandler.showError(e);
    }
  }
  
  /**
   * Test the language of the document
   * switch the check to LT if possible and language is supported
   */
  boolean testDocLanguage(boolean showMessage) {
    if (docLanguage == null) {
      if (linguServices == null) {
        linguServices = new LinguisticServices(xContext);
      }
      if (!linguServices.spellCheckerIsActive()) {
        if (showMessage) {
          MessageHandler.showMessage("LinguisticServices failed! LanguageTool can not be started!");
        } else {
          MessageHandler.printToLogFile("MultiDocumentsHandler: testDocLanguage: LinguisticServices failed! LanguageTool can not be started!");
        }
        return false;
      }
      if (xContext == null) {
        if (showMessage) { 
          MessageHandler.showMessage("There may be a installation problem! \nNo xContext!");
        }
        return false;
      }
      XComponent xComponent = OfficeTools.getCurrentComponent(xContext);
      if (xComponent == null) {
        if (showMessage) { 
          MessageHandler.showMessage("There may be a installation problem! \nNo xComponent!");
        }
        return false;
      }
      Locale locale;
      DocumentType docType;
      if (OfficeDrawTools.isImpressDocument(xComponent)) {
        docType = DocumentType.IMPRESS;
      } else if (OfficeSpreadsheetTools.isSpreadsheetDocument(xComponent)) {
        docType = DocumentType.CALC;
      } else {
        docType = DocumentType.WRITER;
      }
      if (docType == DocumentType.IMPRESS) {
        locale = OfficeDrawTools.getDocumentLocale(xComponent);
      } else if (docType == DocumentType.CALC) {
        locale = OfficeSpreadsheetTools.getDocumentLocale(xComponent);
      } else {
        locale = getDocumentLocale();
      }
      try {
        int n = 0;
        while (locale == null && n < 100) {
          Thread.sleep(500);
          if (debugMode) {
            MessageHandler.printToLogFile("MultiDocumentsHandler: testDocLanguage: Try to get locale: n = " + n);
          }
          if (docType == DocumentType.IMPRESS) {
            locale = OfficeDrawTools.getDocumentLocale(xComponent);
          } else if (docType == DocumentType.CALC) {
            locale = OfficeSpreadsheetTools.getDocumentLocale(xComponent);
          } else {
            locale = getDocumentLocale();
          }
          n++;
        }
      } catch (InterruptedException e) {
        MessageHandler.showError(e);
      }
      if (locale == null) {
        if (showMessage) {
          MessageHandler.showMessage("No Locale! LanguageTool can not be started!");
        } else {
          MessageHandler.printToLogFile("MultiDocumentsHandler: testDocLanguage: No Locale! LanguageTool can not be started!");
        }
        return false;
      } else if (!hasLocale(locale)) {
        String message = Tools.i18n(messages, "language_not_supported", locale.Language);
        MessageHandler.showMessage(message);
        return false;
      }
      if (debugMode) {
        MessageHandler.printToLogFile("MultiDocumentsHandler: testDocLanguage: locale: " + locale.Language + "-" + locale.Country);
      }
      if (!linguServices.setLtAsGrammarService(xContext, locale)) {
        if (showMessage) {
          MessageHandler.showMessage("Can not set LT as grammar check service! LanguageTool can not be started!");
        } else {
          MessageHandler.printToLogFile("MultiDocumentsHandler: testDocLanguage: Can not set LT as grammar check service! LanguageTool can not be started!");
        }
        return false;
      }
      if (docType != DocumentType.WRITER) {
        langForShortName = getLanguage(locale);
        docLanguage = langForShortName;
        this.locale = locale;
        extraRemoteRules.clear();
        lt = initLanguageTool(true);
        initCheck(lt);
        initDocuments();
        setJavaLookAndFeel();
        return true;
      } else {
        resetCheck();
        return false;
      }
    }
    return true;
  }

  /**
   * Test if the needed java version is installed
   */
  public boolean javaVersionOkay() {
    String version = System.getProperty("java.version");
    if (version != null
        && (version.startsWith("1.0") || version.startsWith("1.1")
            || version.startsWith("1.2") || version.startsWith("1.3")
            || version.startsWith("1.4") || version.startsWith("1.5")
            || version.startsWith("1.6") || version.startsWith("1.7"))) {
      MessageHandler.showMessage("Error: LanguageTool requires Java 8 or later. Current version: " + version);
      return false;
    }
    return true;
  }
  
  /** Set Look and Feel for Java Swing Components
   * 
   */
  private void setJavaLookAndFeel() {
    try {
      // do not set look and feel for on Mac OS X as it causes the following error:
      // soffice[2149:2703] Apple AWT Java VM was loaded on first thread -- can't start AWT.
      if (!System.getProperty("os.name").contains("OS X")) {
         // Cross-Platform Look And Feel @since 3.7
         if (System.getProperty("os.name").contains("Linux")) {
           UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
         }
         else {
           UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
      }
    } catch (Exception | AWTError ignored) {
      // Well, what can we do...
    }

  }
  
  /**
   * heap limit is reached
   */
  public boolean heapLimitIsReached() {
    return heapLimitReached;
  }

  /**
   * Test if enough heap space is left
   * Change to single paragraph mode if not
   * return false if heap space is to small 
   */
  public boolean isEnoughHeapSpace() {
    double heapRatio = OfficeTools.getCurrentHeapRatio();
    if (heapRatio >= 1.0) {
      heapLimitReached = true;
      setConfigValues(config, lt);
      MessageHandler.showMessage(messages.getString("loExtHeapMessage"));
      for (SingleDocument document : documents) {
        document.resetCache();
        document.resetDocumentCache();
      }
      return false;
    } else {
      if (heapRatio < 0.5) {
        heapCheckInterval = HEAP_CHECK_INTERVAL;
      } else if (heapRatio > 0.9) {
        heapCheckInterval = (int) (HEAP_CHECK_INTERVAL / (1.0 - heapCheckInterval));
      }
    }
    return true;
  }
  
  /**
   * run heap space test, in intervals
   */
  private void testHeapSpace() {
    if (!heapLimitReached && config.getNumParasToCheck() != 0) {
      if (numSinceHeapTest > heapCheckInterval) {
        isEnoughHeapSpace();
        numSinceHeapTest = 0;
      } else {
        numSinceHeapTest++;
      }
    }
  }
  
  /**
   * class to run the about dialog
   */
  private static class AboutDialogThread extends Thread {

    private final ResourceBundle messages;

    AboutDialogThread(ResourceBundle messages) {
      this.messages = messages;
    }

    @Override
    public void run() {
      // Note: null can cause the dialog to appear on the wrong screen in a
      // multi-monitor setup, but we just don't have a proper java.awt.Component
      // here which we could use instead:
      AboutDialog about = new AboutDialog(messages, null);
      about.show();
    }
  }

  /**
   * Called when "Ignore" is selected e.g. in the context menu for an error.
   */
  public void ignoreRule(String ruleId, Locale locale) {
    addDisabledRule(OfficeTools.localeToString(locale), ruleId);
    setRecheck();
  }

  /**
   * Called on rechecking the document - resets the ignore status for rules that
   * was set in the spelling dialog box or in the context menu.
   * 
   * The rules disabled in the config dialog box are left as intact.
   */
  public void resetIgnoreRules() {
    resetDisabledRules();
    setRecheck();
    resetIgnoreOnce();
    docReset = true;
  }

  /**
   * Get the displayed service name for LT
   */
  public String getServiceDisplayName(Locale locale) {
    return "LanguageTool";
  }

  /**
   * remove internal stored text if document disposes
   */
  public void disposing(EventObject source) {
    //  the data of document will be removed by next call of getNumDocID
    //  to finish checking thread without crashing
    XComponent goneComponent = UnoRuntime.queryInterface(XComponent.class, source.Source);
    if (goneComponent == null) {
      MessageHandler.printToLogFile("MultiDocumentsHandler: disposing: xComponent of closed document is null");
    } else {
      setContextOfClosedDoc(goneComponent);
    }
  }
  
  /**
   *  start a separate thread to add or remove the internal LT dictionary
   */
  
  private void handleLtDictionary() {
    HandleLtDictionary handleDictionary = new HandleLtDictionary();
    handleDictionary.start();
  }

  /**
   *  class to start a separate thread to add or remove the internal LT dictionary
   */
  private class HandleLtDictionary extends Thread {
    @Override
    public void run() {
      if (config.useLtDictionary()) {
        if (dictionary.setLtDictionary(xContext, locale, linguServices)) {
          resetCheck();
        }
      } else {
        if (dictionary.removeLtDictionaries(xContext)) {
          resetCheck();
        }
      }
    }
  }

  /** class to start a separate thread to switch grammar check to LT
   * Experimental currently not used 
   */
  @SuppressWarnings("unused")
  private class LtHelper extends Thread {
    @Override
    public void run() {
      try {
        Thread.sleep(3000);
        testDocLanguage(false);
      } catch (InterruptedException e) {
        MessageHandler.showError(e);
      }
    }
  }

}
