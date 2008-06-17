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
import com.sun.star.registry.XRegistryKey;
import com.sun.star.task.XJobExecutor;
import com.sun.star.text.XFlatParagraph;
import com.sun.star.text.XFlatParagraphIteratorProvider;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.Configuration;
import de.danielnaber.languagetool.gui.Tools;
import de.danielnaber.languagetool.rules.RuleMatch;

public class Main extends WeakBase implements XJobExecutor, XServiceInfo, XGrammarChecker {

  private Configuration config;
  private JLanguageTool langTool; 
  private Language docLanguage;
  
  private XTextViewCursor xViewCursor;
  
  /** Service name required by the OOo API && our own name.
   * 
   */
  private static final String serviceNames[] = {
    "com.sun.star.linguistic2.GrammarChecker",
    "de.danielnaber.languagetool.openoffice.Main"
  };
  
//use a different name than the stand-alone version to avoid conflicts:
  private static final String CONFIG_FILE = ".languagetool-ooo.cfg";

  
  private ResourceBundle messages = null;
  private File homeDir;
    
  //TODO: remove
  private XTextDocument xTextDoc;
  
  private com.sun.star.text.XFlatParagraphIteratorProvider xFlatPI;
  private XComponent xComponent; 
  
  /** Document ID. The document IDs can be used 
   * for storing the document-level state (e.g., for
   * document-level spelling consistency).
   * 
   */
  private int myDocID = -1;
  
  public Main(final XComponentContext xCompContext) {
    try {
      XMultiComponentFactory xMCF = xCompContext.getServiceManager();
      Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xCompContext);
      XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);      
      xComponent = xDesktop.getCurrentComponent();
      xTextDoc = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, xComponent);
      xFlatPI = 
        (XFlatParagraphIteratorProvider) UnoRuntime.queryInterface(XFlatParagraphIteratorProvider.class,
            xComponent);
      homeDir = getHomeDir();
      config = new Configuration(homeDir, CONFIG_FILE);
      messages = JLanguageTool.getMessageBundle();
      
    } catch (Throwable e) {
      writeError(e);
      e.printStackTrace();
    }
  }
  
  private Language getLanguage() {
    if (xFlatPI == null) {
      return Language.ENGLISH; // for testing with local main() method only
    }
    Locale charLocale;
    try {
      // just look at the first 200 chars in the document and assume that this is
      // the language of the whole document:
      if (xFlatPI == null) {
        return null;
      }
      com.sun.star.text.XFlatParagraphIterator xParaAccess = xFlatPI.getFlatParagraphIterator(com.sun.star.text.TextMarkupType.GRAMMAR, true);
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

  /** Runs the grammar checker on paragraph text.
   * @param docID int - document ID
   * xPara XFlatParagraph - text to check
   * locale Locale - the text Locale  
   * startOfSentencePos int start of sentence position
   * suggEndOfSentencePos int end of sentence position
   * @return GrammarCheckingResult containing the results of the check.
   */
  public final GrammarCheckingResult doGrammarChecking(int docID,  
      XFlatParagraph xPara, String paraText, Locale locale, 
      int startOfSentencePos, int suggEndOfSentencePos) 
  throws IllegalArgumentException {    
    GrammarCheckingResult paRes = new GrammarCheckingResult();
    paRes.nEndOfSentencePos = paraText.length(); //suggEndOfSentencePos;
    if (hasLocale(locale)
        //&& (!xPara.isChecked(com.sun.star.text.TextMarkupType.GRAMMAR))    
    ) {
      //caching the instance of LT
      if (!Language.getLanguageForShortName(locale.Language).equals(docLanguage)
          || langTool == null) {
        docLanguage = Language.getLanguageForShortName(locale.Language);
        if (docLanguage == null) {
          return paRes;
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
        List<RuleMatch> ruleMatches = langTool.check(paraText);
        if (ruleMatches.size() > 0) {          
          paRes.xFlatParagraph = xPara;
          paRes.aText = paraText; //xPara.getText();
          paRes.aLocale = locale;                    
          SingleGrammarError[] errorArray = new SingleGrammarError[ruleMatches.size()];;
          int i = 0;
          for (RuleMatch myRuleMatch : ruleMatches) {
            errorArray[i] = createOOoError(
                docID, xPara, locale, startOfSentencePos, suggEndOfSentencePos, myRuleMatch);
            i++;
          }
          paRes.aGrammarErrors = errorArray;
        }
      } catch (IOException exception) {
        showError(exception);
      }      
    } 
     return paRes;    
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
    aError.aSuggestions = (String[]) myMatch.getSuggestedReplacements().toArray(new String [myMatch.getSuggestedReplacements().size ()]);
    aError.nErrorLevel = 0; // severity level, we don't use it
    aError.nErrorStart = myMatch.getFromPos();
    aError.aNewLocale = locale;    
    aError.nErrorLength = myMatch.getToPos() - myMatch.getFromPos(); //?
    return aError;
  }
  
 /**
  * Called when the document check is finished.
  * @param oldDocID - the ID of the document already checked
  * @throws IllegalArgumentException in case arg0 is not a 
  * valid myDocID.
  */
  public void endDocument(int oldDocID) throws IllegalArgumentException {
    if (myDocID == oldDocID) {
      myDocID = -1;
    }
  }

  /**
   * Called to clear the paragraph state. No use in our implementation.
   * 
   * @param myDocID - the ID of the document already checked
   * @throws IllegalArgumentException in case arg0 is not a 
   *  valid myDocID.
   */
  public void endParagraph(int docID) throws IllegalArgumentException {
    // TODO Auto-generated method stub

  }

  public int getEndOfSentencePos(int docID, XFlatParagraph para, String paraText, 
      Locale locale, 
      int startOfSentence,
      int suggestedEndOfSentencePos)
      throws IllegalArgumentException {
    return paraText.length();
  }  
  
  public int getStartOfSentencePos(int docID, XFlatParagraph para, String paraText, 
      Locale locale, 
      int startOfSentence,
      int suggestedEndOfSentencePos) {
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

  /** LT does not support spell-checking,
   * so we return false.
   * @return false
   */
  public final boolean isSpellChecker() {
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
   **/
  public final void runOptionsDialog() {
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
   * @param docID - the doc ID
   * @throws IllegalArgumentException in case arg0 is not a 
   *  valid myDocID.
   **/
  public void startDocument(int docID) throws IllegalArgumentException {
    myDocID = docID;
    docLanguage = getLanguage();
    try {
      langTool = new JLanguageTool(docLanguage, config.getMotherTongue());
      langTool.activateDefaultPatternRules();
      langTool.activateDefaultFalseFriendRules();
    } catch (Exception exception) {
      showError(exception);
    }
    DialogThread dt = new DialogThread("Starting the check!");
    dt.start();
  }

  /**
   * Called to setup the paragraph state in a doc with some ID.
   * @param docID - the doc ID
   * @throws IllegalArgumentException in case arg0 is not a 
   *  valid myDocID.
   **/
  public void startParagraph(int docID) throws IllegalArgumentException {
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
    for (int i = 0; i < Language.LANGUAGES.length; i++) {
      if (Language.LANGUAGES[i].getShortName().equals(arg0.Language)) {
        return true;
      }
    }
    return false;
  }
  

  public String[] getSupportedServiceNames() {
    return getServiceNames();
  }

  public static String[] getServiceNames() {
    return serviceNames;
  }

  public boolean supportsService(final String sServiceName) {
    for (String sName : serviceNames) {
      if (sServiceName.equals(sName)) {
        return true; 
      }
    }
    return false;
  }

  public String getImplementationName() {
    return Main.class.getName();
  }

  public static XSingleComponentFactory __getComponentFactory(final String sImplName) {
    XSingleComponentFactory xFactory = null;
    if (sImplName.equals(Main.class.getName()))
      xFactory = Factory.createComponentFactory(Main.class, Main.getServiceNames());
    return xFactory;
  }

  public static boolean __writeRegistryServiceInfo(final XRegistryKey regKey) {
    return Factory.writeRegistryServiceInfo(Main.class.getName(), Main.getServiceNames(), regKey);
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

  private TextToCheck getText() throws IllegalArgumentException {
    if (xFlatPI == null) {
      return new TextToCheck(new ArrayList<String>(), false);
    }
    com.sun.star.text.XFlatParagraphIterator xParaAccess = 
      xFlatPI.getFlatParagraphIterator(com.sun.star.text.TextMarkupType.GRAMMAR, false);
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
