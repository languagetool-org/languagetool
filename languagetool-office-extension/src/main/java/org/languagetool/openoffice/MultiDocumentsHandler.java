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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JOptionPane;

import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.UserConfig;
import org.languagetool.gui.Configuration;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.Rule;
import org.languagetool.tools.Tools;

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

  private static final boolean debugMode = false;   //  should be false except for testing
  
  private JLanguageTool langTool = null;
  private Language docLanguage;
  private ResourceBundle MESSAGES;
  private XEventListener xEventListener;
  private final File configDir;
  private final String configFile;
  private Configuration config;
  private LinguisticServices linguServices = null;
  
  private XComponentContext xContext;       //  The context of the document
  private List<SingleDocument> documents;   //  The List of LO documents to be checked
  private boolean proofIsRunning = false;   //  true if a check is almost running
  private boolean isParallelThread;         //  is parallel thread (right mouse click, while iteration)
  private XComponent goneContext = null;    //  save component of closed document
  private boolean recheck = true;
  private int docNum;                       //  number of the current document

  private boolean testMode = false;


  MultiDocumentsHandler(XComponentContext xContext, File configDir, String configFile,
      ResourceBundle MESSAGES, XEventListener xEventListener) {
    this.xContext = xContext;
    this.configDir = configDir;
    this.configFile = configFile;
    this.MESSAGES = MESSAGES;
    this.xEventListener = xEventListener;
    documents = new ArrayList<>();
  }
  
  ProofreadingResult getCheckResults(String paraText, Locale locale, ProofreadingResult paRes, int[] footnotePositions) {
    
    if (!hasLocale(locale)) {
      return paRes;
    }
    Language langForShortName = getLanguage(locale);
    if (!langForShortName.equals(docLanguage) || langTool == null || recheck) {
      docLanguage = langForShortName;
      initLanguageTool();
      initCheck();
    }

    if (proofIsRunning) {
      isParallelThread = true;          //  parallel Thread (right-click or dialog-box while background iteration is running)
    } else {
      proofIsRunning = true;  // main thread is running
    }
    
    docNum = getNumDoc(paRes.aDocumentIdentifier);
    paRes = documents.get(docNum).getCheckResults(paraText, locale, paRes, footnotePositions, isParallelThread, langTool);
    
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
   *  get LanguageTool
   */
  JLanguageTool getLanguageTool() {
    return langTool;
  }
  
  /**
   *  get Configuration
   */
  Configuration getConfiguration() {
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
    if(documents.size() == 0) {
      return false;
    }
    return documents.get(docNum).doresetCheck();
  }

  /** 
   * Reset only changed paragraphs
   */
  void optimizeReset() {
    if(documents.size() == 1) {
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
        String message = Tools.i18n(MESSAGES, "language_not_supported", charLocale.Language);
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
  private void setConfigValues(Configuration config, JLanguageTool langTool) {
    this.config = config;
    this.langTool = langTool;
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
            MessageHandler.printToLogFile("Error: Document (ID: " + docID + ") has no XComponent -> Internal space can not be deleted when document disposes");
          } else {
            documents.get(i).setXComponent(xContext, xComponent);
            xComponent.addEventListener(xEventListener);
            MessageHandler.printToLogFile("Fixed: XComponent set for Document (ID: " + docID + ")");
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
        MessageHandler.printToLogFile("Error: Document (ID: " + docID + ") has no XComponent -> Internal space can not be deleted when document disposes");
      } else {
        xComponent.addEventListener(xEventListener);
      }
    }
    documents.add(new SingleDocument(xContext, config, docID, xComponent));
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
      if(xContext != null) {
        linguServices = new LinguisticServices(xContext);
      }
      config = new Configuration(configDir, configFile, docLanguage, linguServices);
      // not using MultiThreadedJLanguageTool here fixes "osl::Thread::Create failed", see https://bugs.documentfoundation.org/show_bug.cgi?id=90740:
      langTool = new JLanguageTool(docLanguage, config.getMotherTongue(), null, 
          new UserConfig(config.getConfigurableValues(), linguServices));
      config.initStyleCategories(langTool.getAllRules());
      docLanguage.getSentenceTokenizer().setSingleLineBreaksMarksParagraph(true);
      File ngramDirectory = config.getNgramDirectory();
      if (ngramDirectory != null) {
        File ngramLangDir = new File(config.getNgramDirectory(), docLanguage.getShortCode());
        if (ngramLangDir.exists()) {  // user might have ngram data only for some languages and that's okay
          langTool.activateLanguageModelRules(ngramDirectory);
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
    for (SingleDocument document : documents) {
      document.resetCache();
    }
    recheck = false;
  }

}
