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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JOptionPane;

import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XModel;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceDisplayName;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.linguistic2.SingleProofreadingError;
import com.sun.star.linguistic2.XLinguServiceEventBroadcaster;
import com.sun.star.linguistic2.XLinguServiceEventListener;
import com.sun.star.linguistic2.XProofreader;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.task.XJobExecutor;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.Configuration;
import de.danielnaber.languagetool.rules.RuleMatch;

public class Main extends WeakBase implements 
    XJobExecutor, XServiceDisplayName, XServiceInfo, 
    XProofreader, XLinguServiceEventBroadcaster {

  private Configuration config;
  private JLanguageTool langTool; 
  private Language docLanguage; 

  private List <XLinguServiceEventListener> xEventListeners;

  /**
   * Make another instance of JLanguageTool
   * and assign it to langTool if true.
   */
  private boolean recheck = false;

  /** 
   * Service name required by the OOo API && our own name.
   */
  private static final String[] SERVICE_NAMES = {
    "com.sun.star.linguistic2.Proofreader",
    "de.danielnaber.languagetool.openoffice.Main"
  };

//use a different name than the stand-alone version to avoid conflicts:
  private static final String CONFIG_FILE = ".languagetool-ooo.cfg";

  private static final ResourceBundle MESSAGES = JLanguageTool.getMessageBundle();

  private XComponentContext xContext;

  public Main(final XComponentContext xCompContext) {
    try {
      changeContext(xCompContext);
      final File homeDir = getHomeDir();
      config = new Configuration(homeDir, CONFIG_FILE);
      xEventListeners = new ArrayList<XLinguServiceEventListener>();
    } catch (final Throwable t) {
      showError(t);
    }
  }

  public void changeContext(final XComponentContext xCompContext) {
    xContext = xCompContext;          
  }
  
  private XComponent getxComponent() {
    try {
      final XMultiComponentFactory xMCF = xContext.getServiceManager();
      final Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
      final XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
      final XComponent xComponent = xDesktop.getCurrentComponent();
      return xComponent;
    } catch (final Throwable t) {
      showError(t);
      return null;
    }
  }  

  /**
   * Checks the language under the cursor. Used for opening the
   * configuration dialog. 
   * @return Language - the language under the visible cursor.
   */
  private Language getLanguage() {
    final XComponent xComponent = getxComponent(); 
    if (xComponent == null) {
      return Language.ENGLISH; // for testing with local main() method only
    }
    Locale charLocale;
    XPropertySet xCursorProps;
    try {     
      final XModel model = 
        (XModel) UnoRuntime.queryInterface(XModel.class, xComponent);
      final XTextViewCursorSupplier 
      xViewCursorSupplier = (XTextViewCursorSupplier) 
      UnoRuntime.queryInterface(XTextViewCursorSupplier.class, 
          model.getCurrentController());
      final XTextViewCursor xCursor = 
        xViewCursorSupplier.getViewCursor();
      if (xCursor.isCollapsed()) { //no text selection
        xCursorProps = (XPropertySet) 
        UnoRuntime.queryInterface(XPropertySet.class, xCursor);        
      } else { //text is selected, need to create another cursor  
              //as multiple languages can occur here - we care only 
              //about character under the cursor, which might be wrong
              //but it applies only to the checking dialog to be removed 
        xCursorProps = (XPropertySet) 
        UnoRuntime.queryInterface(XPropertySet.class,
            xCursor.getText().
            createTextCursorByRange(xCursor.getStart()));              
      }
      final Object obj = xCursorProps.getPropertyValue("CharLocale");
      if (obj == null) {
        return Language.ENGLISH; // fallback
      } 
      charLocale = (Locale) obj; 
      boolean langIsSupported = false;
      for (Language element : Language.LANGUAGES) {
        if (element.getShortName().equals(charLocale.Language)) {
          langIsSupported = true;
          break;
        }
      }
      if (!langIsSupported) {
        // FIXME: i18n
        JOptionPane.showMessageDialog(null, "Error: Sorry, the document language '" +charLocale.Language+ 
          "' is not supported by LanguageTool.");
        return null;
      }
    } catch (final Throwable t) {
      showError(t);
      return null;
    }
    return Language.getLanguageForShortName(charLocale.Language);
  }

  /** Runs the grammar checker on paragraph text.
   * @param String docID - document ID
   * @param String paraText - paragraph text
   * @param locale Locale - the text Locale  
   * @param int startOfSentencePos start of sentence position
   * @param int nSuggestedBehindEndOfSentencePosition end of sentence position
   * @param PropertyValue[] props - properties 
   * @return ProofreadingResult containing the results of the check.   * 
   * @throws IllegalArgumentException (not really, LT simply returns
   * the ProofreadingResult with the values supplied)
   */
  public final ProofreadingResult doProofreading(final String docID, final String paraText,
      final Locale locale, final int startOfSentencePos, final int nSuggestedBehindEndOfSentencePosition,
      PropertyValue[] props) {
    final ProofreadingResult paRes = new ProofreadingResult();
    try {
      //.nEndOfSentencePos 
      paRes.nBehindEndOfSentencePosition = nSuggestedBehindEndOfSentencePosition - startOfSentencePos;// paraText.length(); //suggEndOfSentencePos - startOfSentencePos;
      paRes.xProofreader = this;
      paRes.aLocale = locale;
      paRes.aDocumentIdentifier = docID;
      paRes.aText = paraText;
      paRes.aProperties = props;
      return doGrammarCheckingInternal(paraText, locale, paRes);
    } catch (final Throwable t) {
      showError(t);
      return paRes;
    }
  }
  
  private final ProofreadingResult doGrammarCheckingInternal(final String paraText,
      final Locale locale, final ProofreadingResult paRes) {

    if (paraText == null) {
      return paRes;
    } else {
      paRes.nBehindEndOfSentencePosition = paraText.length();
    }

    if (!"".equals(paraText)) {
      // TODO: process different language fragments in a paragraph
      // according to their language (currently assumed = locale)
      // note: this is not yet implemented in the API

      if (hasLocale(locale)) {
        // caching the instance of LT
        if (!Language.getLanguageForShortName(locale.Language).equals(docLanguage)
            || langTool == null || recheck) {
          docLanguage = Language.getLanguageForShortName(locale.Language);
          if (docLanguage == null) {
            return paRes;
          }
          try {
            langTool = new JLanguageTool(docLanguage, config.getMotherTongue());
            langTool.activateDefaultPatternRules();
            langTool.activateDefaultFalseFriendRules();
            recheck = false;
          } catch (final Throwable t) {
            showError(t);
          }
        }

        if (config.getDisabledRuleIds() != null) {
          for (final String id : config.getDisabledRuleIds()) {
            langTool.disableRule(id);
          }
        }
        final Set<String> disabledCategories = config.getDisabledCategoryNames();
        if (disabledCategories != null) {
          for (final String categoryName : disabledCategories) {
            langTool.disableCategory(categoryName);
          }
        }
        try {
          final List<RuleMatch> ruleMatches = langTool.check(paraText);
          if (!ruleMatches.isEmpty()) {
            final SingleProofreadingError[] errorArray = new SingleProofreadingError[ruleMatches.size()];
            int i = 0;
            for (final RuleMatch myRuleMatch : ruleMatches) {
              errorArray[i] = createOOoError(locale, myRuleMatch);
              i++;
            }
            Arrays.sort(errorArray, new ErrorPositionComparator());
            paRes.aErrors = errorArray;
          }
        } catch (final Throwable t) {
          showError(t);
        }
      }
    }
    return paRes;
  }

  /**
   * Creates a SingleGrammarError object for use in OOo.
   * 
   * @param locale Locale - the text Locale
   * @param myMatch ruleMatch - LT rule match
   * @return SingleGrammarError - object for OOo checker integration
   */
  private SingleProofreadingError createOOoError(final Locale locale, 
      final RuleMatch myMatch) {
    final SingleProofreadingError aError = new SingleProofreadingError();
    aError.nErrorType = com.sun.star.text.TextMarkupType.PROOFREADING;    
    //  the API currently has no support for formatting text in comments 
    final String comment =  myMatch.getMessage().
      replaceAll("<suggestion>", "\"").
      replaceAll("</suggestion>", "\"").
      replaceAll("([\r]*\n)"," "); //convert line ends to spaces     
    aError.aFullComment = comment;    
    //  not all rules have short comments
    if (myMatch.getShortMessage()!= null ) {
      aError.aShortComment = myMatch.getShortMessage();
    } else {
      aError.aShortComment = aError.aFullComment;
    }
    aError.aSuggestions = myMatch.getSuggestedReplacements()
      .toArray(new String [myMatch.getSuggestedReplacements().size()]);    
    aError.nErrorStart = myMatch.getFromPos();      
    aError.nErrorLength = myMatch.getToPos() - myMatch.getFromPos();
    aError.aRuleIdentifier = myMatch.getRule().getId();
    return aError;
  }
 
  /** LT does not support spell-checking,
   * so we return false.
   * @return false
   */
  public final boolean isSpellChecker() {
    return false;
  }

  /** Runs LT options dialog box.
   **/
  public final void runOptionsDialog() {        
    final Language lang = getLanguage();
    if (lang == null) {
      return;
    }    
    final ConfigThread configThread = new ConfigThread(lang, config, this);
    configThread.start();
  }

  /**
   * @return An array of Locales supported by LT.
   */
  public final Locale[] getLocales() {
    int dims = 0;
    for (final Language element : Language.LANGUAGES) {
      dims += element.getCountryVariants().length;
    }
    final Locale[] aLocales = new Locale[dims];
    int cnt = 0;
    for (final Language element : Language.LANGUAGES) {
      for (final String variant : element.getCountryVariants()) {
        aLocales[cnt] = new Locale(element.getShortName(), variant, "");
        cnt++; 
      }
    }
    return aLocales;
  }

  /** @return true if LT supports
   * the language of a given locale.
   * @param locale The Locale to check.
   */
  public final boolean hasLocale(final Locale locale) {    
    for (final Language element : Language.LANGUAGES) {
      if (element.getShortName().equals(locale.Language)) {
        return true;
      }
    }
    return false;
  }


  /**
   * Add a listener that allow re-checking the document after
   * changing the options in the configuration dialog box.
   * @param xLinEvLis - the listener to be added
   * @return true if listener is non-null and has been added, false otherwise.
   */
  public final boolean addLinguServiceEventListener(
      final XLinguServiceEventListener xLinEvLis) {
    if (xLinEvLis == null) {
      return false;
    } else {
      xEventListeners.add(xLinEvLis);
      return true;      
    }
  }

  /** Remove a listener from the event listeners list.
   * @param xLinEvLis - the listener to be removed
   * @return true if listener is non-null and has been removed, false otherwise.
   */
  public final boolean removeLinguServiceEventListener(
      final XLinguServiceEventListener xLinEvLis) {
    if (xLinEvLis == null) {
      return false;
    } else {
      if (xEventListeners.contains(xLinEvLis)) {
        xEventListeners.remove(xLinEvLis);
        return true;
      } else { 
        return false;
      }

    }
  }

  /**
   * Inform listener (grammar checking iterator)
   * that options have changed and the doc should be rechecked.
   *
   */
  public final void resetDocument() {
    if (!xEventListeners.isEmpty()) {
      for (final XLinguServiceEventListener xEvLis : xEventListeners) {
        if (xEvLis != null) {
          final com.sun.star.linguistic2.LinguServiceEvent 
          xEvent = new com.sun.star.linguistic2.LinguServiceEvent();
          xEvent.nEvent = 
            com.sun.star.linguistic2.LinguServiceEventFlags.PROOFREAD_AGAIN;
          xEvLis.processLinguServiceEvent(xEvent);
        }
      }
      recheck = true;
    }
  }

  public String[] getSupportedServiceNames() {
    return getServiceNames();
  }

  public static String[] getServiceNames() {
    return SERVICE_NAMES;
  }

  public boolean supportsService(final String sServiceName) {
    for (final String sName : SERVICE_NAMES) {
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
    SingletonFactory xFactory = null;
    if (sImplName.equals(Main.class.getName())) {
      xFactory = new SingletonFactory();
    }
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
        //FIXME: run OOo dialog only with grammar        
        // 1) save the state of the Hunspell Spellchecker service
        // (or any other spellchecker) if it's enabled
        // 2) run .uno:SpellingAndGrammarDialog
        // 3) reset the state of the spellchecker service
        // Now 3) is too fast, we need to synchronize on the
        //dispatcher...
        final XMultiComponentFactory xMCF = xContext.getServiceManager();
        Object aObj = xMCF.createInstanceWithContext(
            "com.sun.star.linguistic2.LinguServiceManager", xContext);
        com.sun.star.linguistic2.XLinguServiceManager mxLinguSvcMgr = (com.sun.star.linguistic2.XLinguServiceManager) 
          UnoRuntime.queryInterface(com.sun.star.linguistic2.XLinguServiceManager.class, aObj);        
        if (mxLinguSvcMgr != null) {
          Map<Locale, String[]> previousServices = new HashMap<Locale, String[]>();
          Locale[] locales = this.getLocales(); 
          for (Locale loc: locales) {
            String[] spServices = mxLinguSvcMgr.getConfiguredServices("com.sun.star.linguistic2.SpellChecker", loc);
            if (spServices != null) {
              previousServices.put(loc, spServices);
              System.err.println(loc.toString() + " " + Arrays.toString(spServices));
              mxLinguSvcMgr.setConfiguredServices("com.sun.star.linguistic2.SpellChecker", loc, new String[0]);
            }
          }          
          com.sun.star.lang.XMultiServiceFactory xMultiServiceManager = 
            (com.sun.star.lang.XMultiServiceFactory)UnoRuntime.queryInterface(
                com.sun.star.lang.XMultiServiceFactory.class, 
                xContext.getServiceManager() );
          XModel xModel = (XModel)UnoRuntime.queryInterface(XModel.class, getxComponent());          
          com.sun.star.frame.XFrame xFrame = xModel.getCurrentController().getFrame( );
          
          Object objDispatchHelper = xMultiServiceManager.createInstance("com.sun.star.frame.DispatchHelper");

          XDispatchHelper xDispatchHelper = (XDispatchHelper)UnoRuntime.queryInterface( XDispatchHelper.class, objDispatchHelper );
          XDispatchProvider xDispatchProvider = (XDispatchProvider)UnoRuntime.queryInterface( XDispatchProvider.class, xFrame );                     
          UnoRuntime.queryInterface(com.sun.star.frame.XDispatchProvider.class, xFrame);          
          xDispatchHelper.executeDispatch(xDispatchProvider, ".uno:SpellingAndGrammarDialog", 
              "", 0, new PropertyValue[0]);
//FIXME: executeDispatch() returns immediately, we should know when it finishes...          
          for (Locale loc: locales) {
            if (previousServices.get(loc) != null)
            mxLinguSvcMgr.setConfiguredServices("com.sun.star.linguistic2.SpellChecker", loc,
                previousServices.get(loc));
          }
        }
        
      } else if (sEvent.equals("configure")) {                
        runOptionsDialog();        
      } else if (sEvent.equals("about")) {
        final AboutDialogThread aboutthread = new AboutDialogThread(MESSAGES);
        aboutthread.start();
      } else {
        System.err.println("Sorry, don't know what to do, sEvent = " + sEvent);
      }        
    } catch (final Throwable e) {
      showError(e);
    }
  }

  private boolean javaVersionOkay() {
    final String version = System.getProperty("java.version");
    if (version != null && (version.startsWith("1.0") || version.startsWith("1.1")
        || version.startsWith("1.2") || version.startsWith("1.3") || version.startsWith("1.4"))) {
      final DialogThread dt = new DialogThread("Error: LanguageTool requires Java 1.5 or later. Current version: " + version);
      dt.start();
      return false;
    }    
    return true;
  }

  static void showError(final Throwable e) {
    String msg = "An error has occured in LanguageTool:\n" + e.toString() + "\nStacktrace:\n";
    final StackTraceElement[] elem = e.getStackTrace();
    for (final StackTraceElement element : elem) {
      msg += element.toString() + "\n";
    }
    final DialogThread dt = new DialogThread(msg);
    dt.start();
    //e.printStackTrace();
    // OOo crashes when we throw an Exception :-(
    //throw new RuntimeException(e);
  }

  private File getHomeDir() {
    final String homeDir = System.getProperty("user.home");
    if (homeDir == null) {
      RuntimeException ex = new RuntimeException("Could not get home directory");
      showError(ex);
    }
    return new File(homeDir);
  }

  class AboutDialogThread extends Thread {

    private ResourceBundle messages;

    AboutDialogThread(final ResourceBundle messages) {
      this.messages = messages;
    }

    @Override
    public void run() {
      final XModel model = (XModel)UnoRuntime.queryInterface(XModel.class, getxComponent());
      final XWindow parentWindow = model.getCurrentController().getFrame().getContainerWindow();
      final XWindowPeer parentWindowPeer = (XWindowPeer) UnoRuntime.queryInterface(XWindowPeer.class, parentWindow);
      final OOoAboutDialog about = 
        new OOoAboutDialog(messages, parentWindowPeer);
      about.show();
    }
  }  

  @Override
  public void ignoreRule(String ruleId, Locale locale)
      throws IllegalArgumentException {
//TODO: config should be locale-dependent    
    Set<String> disabledRules = config.getDisabledRuleIds();
    if (disabledRules == null) {
        disabledRules = new HashSet<String>();
    }
     disabledRules.add(ruleId);
    config.setDisabledRuleIds(disabledRules);
    try {
    config.saveConfiguration();
    } catch (final Throwable t) {
      showError(t);
    }
    recheck = true;           
  }

  @Override
  public void resetIgnoreRules() {
    config.setDisabledRuleIds(new HashSet<String>());
    try {
      config.saveConfiguration();
      } catch (final Throwable t) {
        showError(t);
      }
    recheck = true;      
  }

  @Override
  public String getServiceDisplayName(Locale locale) {    
    return "LanguageTool";
  }


}

/**
 * A simple comparator for sorting errors by their position.
 *
 */
class ErrorPositionComparator implements Comparator<SingleProofreadingError>{

  public int compare(SingleProofreadingError match1, SingleProofreadingError match2){
    int error1pos = match1.nErrorStart;
    int error2pos = match2.nErrorStart;
    if( error1pos > error2pos )
      return 1;
    else if( error1pos < error2pos )
      return -1;
    else
      return 0;
  }
}

class DialogThread extends Thread {
  private String text;

  DialogThread(final String text) {
    this.text = text;
  }

  @Override
  public void run() {
    JOptionPane.showMessageDialog(null, text);
  }
}
  

