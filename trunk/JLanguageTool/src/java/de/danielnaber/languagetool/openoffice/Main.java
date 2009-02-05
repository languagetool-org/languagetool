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
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XDesktop;
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
import de.danielnaber.languagetool.tools.StringTools;

public class Main extends WeakBase implements XJobExecutor,
    XServiceDisplayName, XServiceInfo, XProofreader,
    XLinguServiceEventBroadcaster {

  private Configuration config;
  private JLanguageTool langTool;
  private Language docLanguage;

  /*
   * Rules disabled using the config dialog box rather than Spelling dialog box
   * or the context menu.
   */
  private Set<String> disabledRules;

  private Set<String> disabledRulesUI;

  private List<XLinguServiceEventListener> xEventListeners;

  /**
   * Make another instance of JLanguageTool and assign it to langTool if true.
   */
  private boolean recheck;

  /**
   * Service name required by the OOo API && our own name.
   */
  private static final String[] SERVICE_NAMES = {
      "com.sun.star.linguistic2.Proofreader",
      "de.danielnaber.languagetool.openoffice.Main" };

  // use a different name than the stand-alone version to avoid conflicts:
  private static final String CONFIG_FILE = ".languagetool-ooo.cfg";

  private static final ResourceBundle MESSAGES = JLanguageTool
      .getMessageBundle();

  private XComponentContext xContext;

  public Main(final XComponentContext xCompContext) {
    try {
      changeContext(xCompContext);
      final File homeDir = getHomeDir();
      config = new Configuration(homeDir, CONFIG_FILE);
      disabledRules = config.getDisabledRuleIds();
      if (disabledRules == null) {
        disabledRules = new HashSet<String>();
      }
      disabledRulesUI = new HashSet<String>(disabledRules);
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
      final Object desktop = xMCF.createInstanceWithContext(
          "com.sun.star.frame.Desktop", xContext);
      final XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(
          XDesktop.class, desktop);
      final XComponent xComponent = xDesktop.getCurrentComponent();
      return xComponent;
    } catch (final Throwable t) {
      showError(t);
      return null;
    }
  }

  /**
   * Checks the language under the cursor. Used for opening the configuration
   * dialog.
   * 
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
      final XModel model = (XModel) UnoRuntime.queryInterface(XModel.class,
          xComponent);
      final XTextViewCursorSupplier xViewCursorSupplier = (XTextViewCursorSupplier) UnoRuntime
          .queryInterface(XTextViewCursorSupplier.class, model
              .getCurrentController());
      final XTextViewCursor xCursor = xViewCursorSupplier.getViewCursor();
      if (xCursor.isCollapsed()) { // no text selection
        xCursorProps = (XPropertySet) UnoRuntime.queryInterface(
            XPropertySet.class, xCursor);
      } else { // text is selected, need to create another cursor
        // as multiple languages can occur here - we care only
        // about character under the cursor, which might be wrong
        // but it applies only to the checking dialog to be removed
        xCursorProps = (XPropertySet) UnoRuntime.queryInterface(
            XPropertySet.class, xCursor.getText().createTextCursorByRange(
                xCursor.getStart()));
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
        JOptionPane.showMessageDialog(null,
            "Error: Sorry, the document language '" + charLocale.Language
                + "' is not supported by LanguageTool.");
        return null;
      }
    } catch (final Throwable t) {
      showError(t);
      return null;
    }
    return Language.getLanguageForShortName(charLocale.Language);
  }

  /**
   * Runs the grammar checker on paragraph text.
   * 
   * @param String
   *          docID - document ID
   * @param String
   *          paraText - paragraph text
   * @param locale
   *          Locale - the text Locale
   * @param int startOfSentencePos start of sentence position
   * @param int nSuggestedBehindEndOfSentencePosition end of sentence position
   * @param PropertyValue
   *          [] props - properties
   * @return ProofreadingResult containing the results of the check. *
   * @throws IllegalArgumentException
   *           (not really, LT simply returns the ProofreadingResult with the
   *           values supplied)
   */
  public final ProofreadingResult doProofreading(final String docID,
      final String paraText, final Locale locale, final int startOfSentencePos,
      final int nSuggestedBehindEndOfSentencePosition, PropertyValue[] props) {
    final ProofreadingResult paRes = new ProofreadingResult();
    try {
      paRes.nBehindEndOfSentencePosition = nSuggestedBehindEndOfSentencePosition
          - startOfSentencePos;
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

  synchronized private final ProofreadingResult doGrammarCheckingInternal(
      final String paraText, final Locale locale, final ProofreadingResult paRes) {

    if (paraText == null) {
      return paRes;
    }
    paRes.nBehindEndOfSentencePosition = paraText.length();

    if (!StringTools.isEmpty(paraText)) {
      // TODO: process different language fragments in a paragraph
      // according to their language (currently assumed = locale)
      // note: this is not yet implemented in the API

      if (hasLocale(locale)) {
        // caching the instance of LT
        if (!Language.getLanguageForShortName(locale.Language).equals(
            docLanguage)
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
        final Set<String> disabledCategories = config
            .getDisabledCategoryNames();
        if (disabledCategories != null) {
          for (final String categoryName : disabledCategories) {
            langTool.disableCategory(categoryName);
          }
        }
        try {
          final List<RuleMatch> ruleMatches = langTool.check(paraText);
          if (!ruleMatches.isEmpty()) {
            final SingleProofreadingError[] errorArray = new SingleProofreadingError[ruleMatches
                .size()];
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
   * @param locale
   *          Locale - the text Locale
   * @param myMatch
   *          ruleMatch - LT rule match
   * @return SingleGrammarError - object for OOo checker integration
   */
  private SingleProofreadingError createOOoError(final Locale locale,
      final RuleMatch myMatch) {
    final SingleProofreadingError aError = new SingleProofreadingError();
    aError.nErrorType = com.sun.star.text.TextMarkupType.PROOFREADING;
    // the API currently has no support for formatting text in comments
    final String comment = myMatch.getMessage()
        .replaceAll("<suggestion>", "\"").replaceAll("</suggestion>", "\"")
        .replaceAll("([\r]*\n)", " "); // convert line ends to spaces
    aError.aFullComment = comment;
    // not all rules have short comments
    if (!StringTools.isEmpty(myMatch.getShortMessage())) {
      aError.aShortComment = myMatch.getShortMessage();
    } else {
      aError.aShortComment = aError.aFullComment;
    }
    aError.aSuggestions = myMatch.getSuggestedReplacements().toArray(
        new String[myMatch.getSuggestedReplacements().size()]);
    aError.nErrorStart = myMatch.getFromPos();
    aError.nErrorLength = myMatch.getToPos() - myMatch.getFromPos();
    aError.aRuleIdentifier = myMatch.getRule().getId();
    aError.aProperties = new PropertyValue[0];
    return aError;
  }

  /**
   * LT does not support spell-checking, so we return false.
   * 
   * @return false
   */
  public final boolean isSpellChecker() {
    return false;
  }

  /**
   * Runs LT options dialog box.
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

  /**
   * @return true if LT supports the language of a given locale.
   * @param locale
   *          The Locale to check.
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
   * Add a listener that allow re-checking the document after changing the
   * options in the configuration dialog box.
   * 
   * @param xLinEvLis
   *          - the listener to be added
   * @return true if listener is non-null and has been added, false otherwise.
   */
  public final boolean addLinguServiceEventListener(
      final XLinguServiceEventListener xLinEvLis) {
    if (xLinEvLis == null) {
      return false;
    }
    xEventListeners.add(xLinEvLis);
    return true;
  }

  /**
   * Remove a listener from the event listeners list.
   * 
   * @param xLinEvLis
   *          - the listener to be removed
   * @return true if listener is non-null and has been removed, false otherwise.
   */
  public final boolean removeLinguServiceEventListener(
      final XLinguServiceEventListener xLinEvLis) {
    if (xLinEvLis == null) {
      return false;
    }
    if (xEventListeners.contains(xLinEvLis)) {
      xEventListeners.remove(xLinEvLis);
      return true;
    }
    return false;
  }

  /**
   * Inform listener (grammar checking iterator) that options have changed and
   * the doc should be rechecked.
   * 
   */
  public final void resetDocument() {
    if (!xEventListeners.isEmpty()) {
      for (final XLinguServiceEventListener xEvLis : xEventListeners) {
        if (xEvLis != null) {
          final com.sun.star.linguistic2.LinguServiceEvent xEvent = new com.sun.star.linguistic2.LinguServiceEvent();
          xEvent.nEvent = com.sun.star.linguistic2.LinguServiceEventFlags.PROOFREAD_AGAIN;
          xEvLis.processLinguServiceEvent(xEvent);
        }
      }
      recheck = true;
      disabledRules = config.getDisabledRuleIds();
      if (disabledRules == null) {
        disabledRules = new HashSet<String>();
      }
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

  public static XSingleComponentFactory __getComponentFactory(
      final String sImplName) {
    SingletonFactory xFactory = null;
    if (sImplName.equals(Main.class.getName())) {
      xFactory = new SingletonFactory();
    }
    return xFactory;
  }

  public static boolean __writeRegistryServiceInfo(final XRegistryKey regKey) {
    return Factory.writeRegistryServiceInfo(Main.class.getName(), Main
        .getServiceNames(), regKey);
  }

  public void trigger(final String sEvent) {
    if (!javaVersionOkay()) {
      return;
    }
    try {
      if (sEvent.equals("configure")) {
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
    if (version != null
        && (version.startsWith("1.0") || version.startsWith("1.1")
            || version.startsWith("1.2") || version.startsWith("1.3") || version
            .startsWith("1.4"))) {
      final DialogThread dt = new DialogThread(
          "Error: LanguageTool requires Java 1.5 or later. Current version: "
              + version);
      dt.start();
      return false;
    }
    if ("1.6.0_10".equals(version))   { //no newer version has it
      try {
      UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
      } catch (Exception ex) {
        // Well, what can we do...
      }
    }
    return true;
  }

  static void showError(final Throwable e) {
    String msg = "An error has occured in LanguageTool:\n" + e.toString()
        + "\nStacktrace:\n";
    final StackTraceElement[] elem = e.getStackTrace();
    for (final StackTraceElement element : elem) {
      msg += element.toString() + "\n";
    }
    final DialogThread dt = new DialogThread(msg);
    dt.start();
    // e.printStackTrace();
    // OOo crashes when we throw an Exception :-(
    // throw new RuntimeException(e);
  }

  private File getHomeDir() {
    final String homeDir = System.getProperty("user.home");
    if (homeDir == null) {
      RuntimeException ex = new RuntimeException("Could not get home directory");
      showError(ex);
    }
    return new File(homeDir);
  }

  private class AboutDialogThread extends Thread {

    private ResourceBundle messages;

    AboutDialogThread(final ResourceBundle messages) {
      this.messages = messages;
    }

    @Override
    public void run() {
      final XModel model = (XModel) UnoRuntime.queryInterface(XModel.class,
          getxComponent());
      final XWindow parentWindow = model.getCurrentController().getFrame()
          .getContainerWindow();
      final XWindowPeer parentWindowPeer = (XWindowPeer) UnoRuntime
          .queryInterface(XWindowPeer.class, parentWindow);
      final OOoAboutDialog about = new OOoAboutDialog(messages,
          parentWindowPeer);
      about.show();
    }
  }

  @Override
  public void ignoreRule(String ruleId, Locale locale)
      throws IllegalArgumentException {
    // TODO: config should be locale-dependent
    disabledRulesUI.add(ruleId);
    config.setDisabledRuleIds(disabledRulesUI);
    try {
      config.saveConfiguration();
    } catch (final Throwable t) {
      showError(t);
    }
    recheck = true;
  }

  /**
   * Called on rechecking the document - resets the ignore status for rules that
   * was set in the spelling dialog box or in the context menu.
   * 
   * The rules disabled in the config dialog box are left as intact.
   */
  public void resetIgnoreRules() {
    config.setDisabledRuleIds(disabledRules);
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
class ErrorPositionComparator implements Comparator<SingleProofreadingError> {

  public int compare(SingleProofreadingError match1,
      SingleProofreadingError match2) {
    int error1pos = match1.nErrorStart;
    int error2pos = match2.nErrorStart;
    if (error1pos > error2pos)
      return 1;
    else if (error1pos < error2pos)
      return -1;
    else
      return ((Integer) (match1.aSuggestions.length))
          .compareTo(match2.aSuggestions.length);
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
