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

package de.danielnaber.languagetool.openoffice;

/** OpenOffice 3.x Integration
 * 
 * @author Marcin Miłkowski
 */
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Set;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.sun.star.frame.XDesktop;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.linguistic2.GrammarCheckingResult;
import com.sun.star.linguistic2.XGrammarChecker;
import com.sun.star.linguistic2.SingleGrammarError;
import com.sun.star.linguistic2.XGrammarCheckingResultListener;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.task.XJobExecutor;
import com.sun.star.text.XFlatParagraph;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.Configuration;
import de.danielnaber.languagetool.gui.Tools;
import de.danielnaber.languagetool.rules.RuleMatch;

public class NewChecker extends WeakBase implements XJobExecutor, XServiceInfo, XGrammarChecker {

  private Configuration config;
  private JLanguageTool langTool; 
  private Language docLanguage;
  
  private XTextViewCursor xViewCursor;
  
  /** Service name required by the OOo API (?).
   * 
   */
  private static final String __serviceName = "com.sun.star.linguistic2.GrammarChecker";
  
//use a different name than the stand-alone version to avoid conflicts:
  private static final String CONFIG_FILE = ".languagetool-ooo.cfg";

  
  private ResourceBundle messages = null;
  private File homeDir;
  
  //FIXME: in the dummy implementation, it's a mutex, I'm using a simple list...
  //another problem: how do listeners actually get added??
  private List<XGrammarCheckingResultListener> gcListeners;
  
  private XTextDocument xTextDoc;
  
  /** Document ID. The document IDs can be used 
   * for storing the document-level state (e.g., for
   * document-level spelling consistency).
   * 
   */
  private int docID = -1;
  
  public NewChecker(final XComponentContext xCompContext) {
    try {
      XMultiComponentFactory xMCF = xCompContext.getServiceManager();
      Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xCompContext);
      XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
      XComponent xComponent = xDesktop.getCurrentComponent();
      xTextDoc = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, xComponent);
      homeDir = getHomeDir();
      config = new Configuration(homeDir, CONFIG_FILE);
      messages = JLanguageTool.getMessageBundle();        
    } catch (Throwable e) {
      writeError(e);
      e.printStackTrace();
    }
  }
  
  private Language getLanguage() {
    if (xTextDoc == null)
      return Language.ENGLISH; // for testing with local main() method only
    Locale charLocale;
    try {
      // just look at the first 200 chars in the document and assume that this is
      // the language of the whole document:
      com.sun.star.text.XFlatParagraphIterator xParaAccess = (com.sun.star.text.XFlatParagraphIterator) UnoRuntime
      .queryInterface(com.sun.star.text.XFlatParagraphIterator.class, 
          xTextDoc);
      if (xParaAccess == null) {
        System.err.println("xParaAccess == null");
        return null;
      }          
      com.sun.star.text.XFlatParagraph xParaEnum = xParaAccess.getFirstPara();
      int maxLen = xParaEnum.getText().length();
      if (maxLen > 200) {
        maxLen = 200;
      }
      charLocale = (Locale) xParaEnum.getPrimaryLanguageOfText(1, maxLen);      
      if (!hasLocale(charLocale)) {
        // FIXME: i18n
        DialogThread dt = new DialogThread("Error: Sorry, the document language '" +charLocale.Language+ 
        "' is not supported by LanguageTool.");
        dt.start();
        return null;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return Language.getLanguageForShortName(charLocale.Language);
  }

  /** Runs LT on text.
   * @param arg0 int - document ID
   * arg1 XFlatParagraph - text to check
   * arg2 Locale - the text Locale  
   * arg3 int start of sentence position
   * arg4 int end of sentence position
   */
  public final void doGrammarChecking(int arg0,  
      XFlatParagraph arg1, Locale arg2, 
      int arg3, int arg4) 
  throws IllegalArgumentException {
    if (hasLocale(arg2) 
        && (!arg1.isChecked(com.sun.star.text.TextMarkupType.GRAMMAR))) {
      docLanguage = Language.DEMO;
      for (int i = 0; i < Language.LANGUAGES.length; i++) {
        if (Language.LANGUAGES[i].getShortName().equals(arg2.Language)) {
          docLanguage = Language.LANGUAGES[i];
          break;
        }
        try {
          langTool = new JLanguageTool(docLanguage, config.getMotherTongue());
          langTool.activateDefaultPatternRules();
          langTool.activateDefaultFalseFriendRules();
        } catch (Exception exception) {
          showError(exception);
        }
      }
      if (config.getDisabledRuleIds() != null) {
        for (String id : config.getDisabledRuleIds()) {
          langTool.disableRule(id);
        }
      }
      Set<String> disabledCategories = config.getDisabledCategoryNames();
      if (disabledCategories != null) {
        for (String categoryName : disabledCategories) {
          langTool.disableCategory(categoryName);
        }
      }
      try {
        List<RuleMatch> ruleMatches = langTool.check(arg1.getText());
        if (ruleMatches.size() > 0) {
          GrammarCheckingResult paRes = new GrammarCheckingResult();
          paRes.xPara = arg1;
          paRes.aText = arg1.getText();
          paRes.aLocale = arg2;          
          paRes.nEndOfSentencePos = arg4;
          SingleGrammarError[] errorArray = new SingleGrammarError[ruleMatches.size()];;
          int i = 0;
          for (RuleMatch myRuleMatch : ruleMatches) {
            errorArray[i] = createOOoError(
                arg0, arg1, arg2, arg3, arg4, myRuleMatch);
            i++;
          }
          paRes.aGrammarErrors = errorArray;
          if (gcListeners != null) {
            if (gcListeners.size() > 0) {
              for (XGrammarCheckingResultListener gcL : gcListeners) {
                gcL.GrammarCheckingFinished(paRes);
              }
            }
          }
        } else {
          //mark the text node as checked
          arg1.setChecked(com.sun.star.text.TextMarkupType.GRAMMAR, 
              true);  
        }
      } catch (IOException exception) {
        showError(exception);
      }      
    }
  }

  /** Creates a SingleGrammarError object for use in OOo.
   * @param docId int - document ID
   * para XFlatParagraph - text to check
   * locale Locale - the text Locale  
   * sentStart int start of sentence position
   * sentEnd int end of sentence position
   * MyMatch ruleMatch - LT rule match
   * @return SingleGrammarError - object for OOo checker integration
   */
  private SingleGrammarError createOOoError(final int docId,
      XFlatParagraph para, Locale locale, int sentStart, int SentEnd,
      final RuleMatch myMatch) {
    SingleGrammarError aError = new SingleGrammarError();
    aError.nErrorType = com.sun.star.text.TextMarkupType.GRAMMAR;
    aError.aFullComment = myMatch.getMessage();
    aError.aShortComment = aError.aFullComment; // we don't support two kinds of comments
    aError.aSuggestions = (String[]) myMatch.getSuggestedReplacements().toArray();
    aError.nErrorLevel = 0; // severity level, we don't use it
    aError.nErrorStart = myMatch.getFromPos();
    aError.aNewLocale = locale;
    aError.xGC = this;
    aError.nErrorLen = myMatch.getToPos() - myMatch.getFromPos(); //?
    return aError;
  }
  
 /**
  * Called when the document check is finished.
  * @param arg0 - the ID of the document already checked
  * @throws IllegalArgumentException in case arg0 is not a 
  * valid docID.
  */
  public void endDocument(int arg0) throws IllegalArgumentException {
    if (docID == arg0) {
      docID = -1;
    }
  }

  /**
   * Called to clear the paragraph state. No use in our implementation.
   * 
   * @param arg0 - the ID of the document already checked
   * @throws IllegalArgumentException in case arg0 is not a 
   *  valid docID.
   */
  public void endParagraph(int arg0) throws IllegalArgumentException {
    // TODO Auto-generated method stub

  }

  public int getEndOfSentencePos(int arg0, String arg1, Locale arg2, int arg3)
      throws IllegalArgumentException {
    // TODO Auto-generated method stub
 //   XBreakIterator xBreakIterator = new XBreakIterator();
    
    return 0;
  }  
  
  public int getStartOfSentencePos(int arg0, String arg1, Locale arg2)
      throws IllegalArgumentException {
    // TODO Auto-generated method stub
    return 0;
  }

  public boolean hasCheckingDialog() {
    // TODO Auto-generated method stub
    return false;
  }

  /** LT has an options dialog box,
   * so we return true.
   * @return true
   * */
  public final boolean hasOptionsDialog() {
    return true;
  }

  public boolean isSpellChecker(Locale arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean requiresPreviousText() {
    // TODO Auto-generated method stub
    return false;
  }

  public void runCheckingDialog(int arg0) throws IllegalArgumentException {
    // TODO Auto-generated method stub

  }

  /** Runs LT options dialog box.
   * @param arg0 - the Document ID
   * @throws IllegalArgumentException - not really, we're not using the 
   * DocID at all, so we don't care
   **/
  public final void runOptionsDialog(final int arg0) throws IllegalArgumentException {
    final Language lang = getLanguage();
    if (lang == null)
      return;
    final ConfigThread configThread = new ConfigThread(lang, config);
    configThread.start();
    while (true) {
      if (configThread.done()) {
        break;
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        break;
      }
    }
  }

  /**
   * Called to setup the doc state via ID.
   * @param arg0 - the doc ID
   * @throws IllegalArgumentException in case arg0 is not a 
   *  valid docID.
   **/
  public void startDocument(int arg0) throws IllegalArgumentException {
    docID = arg0;
  }

  /**
   * Called to setup the paragraph state in a doc with some ID.
   * @param arg0 - the doc ID
   * @throws IllegalArgumentException in case arg0 is not a 
   *  valid docID.
   **/
  public void startParagraph(int arg0) throws IllegalArgumentException {
    // TODO Auto-generated method stub

  }

  /**
   * @return An array of Locales supported by LT.
   */
  public final Locale[] getLocales() {
    final Locale[] aLocales = new Locale[Language.LANGUAGES.length];
    for (int i = 0; i < Language.LANGUAGES.length; i++) {
      aLocales[i] = new Locale(
          Language.LANGUAGES[i].getShortName(),
          //FIXME: is the below correct??
          Language.LANGUAGES[i].getLocale().getVariant(),
          "");
    }
    return aLocales;
  }

  /** @return true if LT supports
   * the language of a given locale.
   * @param arg0 The Locale to check.
   */
  public final boolean hasLocale(final Locale arg0) {    
    boolean langIsSupported = false;
    for (int i = 0; i < Language.LANGUAGES.length; i++) {
      if (Language.LANGUAGES[i].getShortName().equals(arg0.Language)) {
        langIsSupported = true;
        break;
      }
    }
    return langIsSupported;
  }
  

  //FIXME: they will be both removed in the new API
  public final boolean addGrammarCheckingResultListener(final XGrammarCheckingResultListener xListener) {
    //FIXME: dummy mutex
   Object myMutex = new Object();
   synchronized(myMutex) {
  if (gcListeners == null) {
    gcListeners = new ArrayList<XGrammarCheckingResultListener>();
   }
  if (xListener != null) {
    gcListeners.add(xListener);
  return true;
  }
  else {
    return false;
  }
  }
  }
  
  public final boolean removeGrammarCheckingResultListener(final XGrammarCheckingResultListener xListener) {
    //FIXME: dummy mutex
    Object myMutex = new Object();
    synchronized(myMutex) {   
    if (gcListeners == null) {
      return true;
     }
    if (xListener != null) {
      gcListeners.remove(xListener);
    return true;
    }
    else {
      return false;
    }
    }           
  }
  
  public String[] getSupportedServiceNames() {
    return getServiceNames();
  }

  public static String[] getServiceNames() {
    String[] sSupportedServiceNames = { __serviceName };
    return sSupportedServiceNames;
  }

  public boolean supportsService(final String sServiceName) {
    return sServiceName.equals(__serviceName);
  }

  public String getImplementationName() {
    return NewChecker.class.getName();
  }

  public static XSingleComponentFactory __getComponentFactory(final String sImplName) {
    XSingleComponentFactory xFactory = null;
    if (sImplName.equals(NewChecker.class.getName()))
      xFactory = Factory.createComponentFactory(NewChecker.class, NewChecker.getServiceNames());
    return xFactory;
  }

  public static boolean __writeRegistryServiceInfo(final XRegistryKey regKey) {
    return Factory.writeRegistryServiceInfo(NewChecker.class.getName(), NewChecker.getServiceNames(), regKey);
  }
    
  public void trigger(final String sEvent) {
    if (!javaVersionOkay()) {
      return;
    }
    try {
      if (sEvent.equals("execute")) {
        //try out the new XFlatParagraph interface...
        TextToCheck textToCheck = getText();
        checkText(textToCheck);
      } else if (sEvent.equals("configure")) {
        final Language lang = getLanguage();
        if (lang == null)
          return;
        final ConfigThread configThread = new ConfigThread(lang, config);
        configThread.start();
        while (true) {
          if (configThread.done()) {
            break;
          }
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            break;
          }
        }
      } else if (sEvent.equals("about")) {
        AboutDialogThread aboutthread = new AboutDialogThread(messages);
        aboutthread.start();
      } else {
        System.err.println("Sorry, don't know what to do, sEvent = " + sEvent);
      }        
    } catch (Throwable e) {
      showError(e);
    }
  }
  
  private void checkText(final TextToCheck textToCheck) {
    if (textToCheck == null) {
      return;
    }
    Language docLanguage = getLanguage();
    if (docLanguage == null) {
      return;
    }
    ProgressDialog progressDialog = new ProgressDialog(messages);
    CheckerThread checkerThread = new CheckerThread(textToCheck.paragraphs, docLanguage, config, 
        progressDialog);
    checkerThread.start();
    while (true) {
      if (checkerThread.done()) {
        break;
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // nothing
      }
    }
    progressDialog.close();

    List<CheckedParagraph> checkedParagraphs = checkerThread.getRuleMatches();
    // TODO: why must these be wrapped in threads to avoid focus problems?
    if (checkedParagraphs.size() == 0) {
      String msg;
      String translatedLangName = messages.getString(docLanguage.getShortName());
      if (textToCheck.isSelection) {
        msg = Tools.makeTexti18n(messages, "guiNoErrorsFoundSelectedText", new String[] {translatedLangName});  
      } else {
        msg = Tools.makeTexti18n(messages, "guiNoErrorsFound", new String[] {translatedLangName});  
      }
      DialogThread dt = new DialogThread(msg);
      dt.start();
      // TODO: display number of active rules etc?
    } else {
      ResultDialogThread dialog;
      if (textToCheck.isSelection) {
        dialog = new ResultDialogThread(config,
            checkerThread.getLanguageTool().getAllRules(),
            xTextDoc, checkedParagraphs, xViewCursor, textToCheck);
      } else {
        dialog = new ResultDialogThread(config,
            checkerThread.getLanguageTool().getAllRules(),
            xTextDoc, checkedParagraphs, null, null);
      }
      dialog.start();
    }
  }  

  private TextToCheck getText() {
    com.sun.star.text.XFlatParagraphIterator xParaAccess = (com.sun.star.text.XFlatParagraphIterator) UnoRuntime
        .queryInterface(com.sun.star.text.XFlatParagraphIterator.class, 
            xTextDoc);
    if (xParaAccess == null) {
      System.err.println("xParaAccess == null");
      return new TextToCheck(new ArrayList<String>(), false);
    }        
    List<String> paragraphs = new ArrayList<String>();
    try {
      com.sun.star.text.XFlatParagraph xParaEnum = xParaAccess.getFirstPara();        
        String paraString = xParaEnum.getText();
        if (paraString == null) {
          paragraphs.add("");
        } else {
          paragraphs.add(paraString);
        }
        while (true) {
          xParaEnum = xParaAccess.getNextPara();
          if (xParaEnum != null) {
          paraString = xParaEnum.getText();
          if (paraString == null) {
            paragraphs.add("");
          } else {
            paragraphs.add(paraString);
          } 
          } else {
            break;
          }
        }        
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return new TextToCheck(paragraphs, false);
  }

  
  private boolean javaVersionOkay() {
    final String version = System.getProperty("java.version");
    if (version != null && (version.startsWith("1.0") || version.startsWith("1.1")
        || version.startsWith("1.2") || version.startsWith("1.3") || version.startsWith("1.4"))) {
      DialogThread dt = new DialogThread("Error: LanguageTool requires Java 1.5 or later. Current version: " + version);
      dt.start();
      return false;
    }    
    return true;
  }

  static void showError(final Throwable e) {
    String msg = "An error has occured:\n" + e.toString() + "\nStacktrace:\n";
    StackTraceElement[] elem = e.getStackTrace();
    for (int i = 0; i < elem.length; i++) {
      msg += elem[i].toString() + "\n";
    }
    DialogThread dt = new DialogThread(msg);
    dt.start();
    e.printStackTrace();
    throw new RuntimeException(e);
  }

  private void writeError(final Throwable e) {
    FileWriter fw;
    try {
      fw = new FileWriter("languagetool.log");
      fw.write(e.toString() + "\r\n");
      StackTraceElement[] el = e.getStackTrace();
      for (int i = 0; i < el.length; i++) {
        fw.write(el[i].toString()+ "\r\n");
      }
      fw.close();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }
  
  private File getHomeDir() {
    final String homeDir = System.getProperty("user.home");
    if (homeDir == null) {
      throw new RuntimeException("Could not get home directory");
    }
    return new File(homeDir);
  }

  
}
