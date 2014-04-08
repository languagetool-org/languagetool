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

package org.languagetool.openoffice;

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

import com.sun.star.lang.*;
import com.sun.star.lang.IllegalArgumentException;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.gui.AboutDialog;
import org.languagetool.gui.Configuration;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XModel;
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

public class Main extends WeakBase implements XJobExecutor,
    XServiceDisplayName, XServiceInfo, XProofreader,
    XLinguServiceEventBroadcaster {

  // Service name required by the OOo API && our own name.
  private static final String[] SERVICE_NAMES = {
          "com.sun.star.linguistic2.Proofreader",
          "org.languagetool.openoffice.Main" };

  // use a different name than the stand-alone version to avoid conflicts:
  private static final String CONFIG_FILE = ".languagetool-ooo.cfg";

  private static final ResourceBundle MESSAGES = JLanguageTool.getMessageBundle();

  // LibreOffice (since 4.2.0) special tag for locale with variant 
  // e.g. language ="qlt" country="ES" variant="ca-ES-valencia":
  private static final String LIBREOFFICE_SPECIAL_LANGUAGE_TAG = "qlt";

  private Configuration config;
  private JLanguageTool langTool;
  private Language docLanguage;

  private String docID;

  // Rules disabled using the config dialog box rather than Spelling dialog box
  // or the context menu.
  private Set<String> disabledRules;
  private Set<String> disabledRulesUI;

  private List<XLinguServiceEventListener> xEventListeners;

  // Make another instance of JLanguageTool and assign it to langTool if true.
  private boolean recheck;

  /**
   * Sentence tokenization-related members.
   */
  private String currentPara;
  private List<String> tokenizedSentences;
  private int position;
  private List<RuleMatch> paragraphMatches;

  private XComponentContext xContext;

  public Main(final XComponentContext xCompContext) {
    changeContext(xCompContext);
    xEventListeners = new ArrayList<>();
  }

  private void prepareConfig(final Language lang) {
    try {
      final File homeDir = getHomeDir();
      config = new Configuration(homeDir, CONFIG_FILE, lang);
      disabledRules = config.getDisabledRuleIds();
      if (disabledRules == null) {
        disabledRules = new HashSet<>();
      }
      disabledRulesUI = new HashSet<>(disabledRules);
    } catch (final Throwable t) {
      showError(t);
    }
  }

  public final void changeContext(final XComponentContext xCompContext) {
    xContext = xCompContext;
  }

  private XComponent getXComponent() {
    try {
      final XMultiComponentFactory xMCF = xContext.getServiceManager();
      final Object desktop = xMCF.createInstanceWithContext(
          "com.sun.star.frame.Desktop", xContext);
      final XDesktop xDesktop = UnoRuntime.queryInterface(XDesktop.class, desktop);
      return xDesktop.getCurrentComponent();
    } catch (final Throwable t) {
      showError(t);
      return null;
    }
  }

  /**
   * Checks the language under the cursor. Used for opening the configuration
   * dialog.
   * @return the language under the visible cursor
   */
  private Language getLanguage() {
    final XComponent xComponent = getXComponent();
    final Locale charLocale;
    final XPropertySet xCursorProps;
    try {
      final XModel model = UnoRuntime.queryInterface(XModel.class, xComponent);
      final XTextViewCursorSupplier xViewCursorSupplier = UnoRuntime
          .queryInterface(XTextViewCursorSupplier.class,
              model.getCurrentController());
      final XTextViewCursor xCursor = xViewCursorSupplier.getViewCursor();
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
      // whether the text is Khmer (the only "complex text layout (CTL)" language we support so far).
      // Thus we check the text itself:
      final KhmerDetector khmerDetector = new KhmerDetector();
      if (khmerDetector.isKhmer(xCursor.getText().getString())) {
        return Language.getLanguageForShortName("km"); // Khmer
      }

      final Object obj = xCursorProps.getPropertyValue("CharLocale");
      if (obj == null) {
        return Language.getLanguageForShortName("en-US");
      }
      charLocale = (Locale) obj;
      boolean langIsSupported = false;
      for (Language element : Language.LANGUAGES) {
        if (charLocale.Language.equalsIgnoreCase(LIBREOFFICE_SPECIAL_LANGUAGE_TAG)
            && element.getShortNameWithCountryAndVariant().equalsIgnoreCase(
                charLocale.Variant)) {
          langIsSupported = true;
          break;
        }
        if (element.getShortName().equals(charLocale.Language)) {
          langIsSupported = true;
          break;
        }
      }
      if (!langIsSupported) {
        final String message = org.languagetool.gui.Tools.makeTexti18n(
            MESSAGES, "language_not_supported", charLocale.Language);
        JOptionPane.showMessageDialog(null, message);
        return null;
      }
    } catch (final Throwable t) {
      showError(t);
      return null;
    }

    try {
      if (charLocale.Language.equalsIgnoreCase(LIBREOFFICE_SPECIAL_LANGUAGE_TAG)) {
        return Language.getLanguageForShortName(charLocale.Variant);
      } else {
        return Language.getLanguageForShortName(charLocale.Language + "-"
            + charLocale.Country);
      }
    } catch (java.lang.IllegalArgumentException e) {
      return Language.getLanguageForShortName(charLocale.Language);
    }

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
  @Override
  public final ProofreadingResult doProofreading(final String docID,
      final String paraText, final Locale locale, final int startOfSentencePos,
      final int nSuggestedBehindEndOfSentencePosition,
      final PropertyValue[] propertyValues) {
    final ProofreadingResult paRes = new ProofreadingResult();
    try {
      paRes.nStartOfSentencePosition = startOfSentencePos;
      paRes.xProofreader = this;
      paRes.aLocale = locale;
      paRes.aDocumentIdentifier = docID;
      paRes.aText = paraText;
      paRes.aProperties = propertyValues;
      return doGrammarCheckingInternal(paraText, locale, paRes);
    } catch (final Throwable t) {
      showError(t);
      return paRes;
    }
  }

  private synchronized ProofreadingResult doGrammarCheckingInternal(
      final String paraText, final Locale locale, final ProofreadingResult paRes) {

    if (!StringTools.isEmpty(paraText) && hasLocale(locale)) {
      Language langForShortName;
      try {
        if (locale.Language.equalsIgnoreCase(LIBREOFFICE_SPECIAL_LANGUAGE_TAG)) {
          langForShortName = Language.getLanguageForShortName(locale.Variant);
        } else {
          langForShortName = Language.getLanguageForShortName(locale.Language
              + "-" + locale.Country);
        }
      } catch (java.lang.IllegalArgumentException e) {
        langForShortName = Language.getLanguageForShortName(locale.Language);
      }
      if (!langForShortName.equals(docLanguage) || langTool == null || recheck) {
        docLanguage = langForShortName;
        if (docLanguage == null) {
          return paRes;
        }
        initLanguageTool();
      }

      final Set<String> disabledRuleIds = config.getDisabledRuleIds();
      if (disabledRuleIds != null) {
        // copy as the config thread may access this as well
        final List<String> list = new ArrayList<>(disabledRuleIds);
        for (final String id : list) {
          langTool.disableRule(id);
        }
      }
      final Set<String> disabledCategories = config.getDisabledCategoryNames();
      if (disabledCategories != null) {
        // copy as the config thread may access this as well
        final List<String> list = new ArrayList<>(disabledCategories);
        for (final String categoryName : list) {
          langTool.disableCategory(categoryName);
        }
      }
      final Set<String> enabledRuleIds = config.getEnabledRuleIds();
      if (enabledRuleIds != null) {
        // copy as the config thread may access this as well
        final List<String> list = new ArrayList<>(enabledRuleIds);
        for (String ruleName : list) {
          langTool.enableDefaultOffRule(ruleName);
          langTool.enableRule(ruleName);
        }
      }
      try {
        final String sentence = getSentence(paraText,
            paRes.nStartOfSentencePosition);
        paRes.nStartOfSentencePosition = position;
        paRes.nStartOfNextSentencePosition = position + sentence.length();
        paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;
        if (!StringTools.isEmpty(sentence)) {
          final List<RuleMatch> ruleMatches = langTool.check(sentence, false,
              JLanguageTool.ParagraphHandling.ONLYNONPARA);
          final SingleProofreadingError[] pErrors = checkParaRules(paraText,
              locale, paRes.nStartOfSentencePosition,
              paRes.nStartOfNextSentencePosition, paRes.aDocumentIdentifier);
          int pErrorCount = 0;
          if (pErrors != null) {
            pErrorCount = pErrors.length;
          }
          if (!ruleMatches.isEmpty()) {
            final SingleProofreadingError[] errorArray = new SingleProofreadingError[ruleMatches
                .size() + pErrorCount];
            int i = 0;
            for (final RuleMatch myRuleMatch : ruleMatches) {
              errorArray[i] = createOOoError(myRuleMatch,
                  paRes.nStartOfSentencePosition);
              i++;
            }
            // add para matches
            if (pErrors != null) {
              for (SingleProofreadingError paraError : pErrors) {
                if (paraError != null) {
                  errorArray[i] = paraError;
                  i++;
                }
              }
            }
            Arrays.sort(errorArray, new ErrorPositionComparator());
            paRes.aErrors = errorArray;

          } else {
            if (pErrors != null) {
              paRes.aErrors = pErrors;
            }
          }
        }
      } catch (final Throwable t) {
        showError(t);
        paRes.nBehindEndOfSentencePosition = paraText.length();
      }
    }
    return paRes;
  }

  private void initLanguageTool() {
    try {
      prepareConfig(docLanguage);
      langTool = new MultiThreadedJLanguageTool(docLanguage,
          config.getMotherTongue());
      langTool.activateDefaultPatternRules();
      langTool.activateDefaultFalseFriendRules();
      for (Rule rule : langTool.getAllActiveRules()) {
        if (rule.isDictionaryBasedSpellingRule()) {
          langTool.disableRule(rule.getId());
        }
      }
      recheck = false;
    } catch (final Throwable t) {
      showError(t);
    }
  }

  private synchronized String getSentence(final String paraText,
      final int startPos) {
    if (paraText.equals(currentPara) && tokenizedSentences != null) {
      int i = 0;
      int index = -1;
      while (index < startPos && i < tokenizedSentences.size()) {
        index += tokenizedSentences.get(i).length();
        if (index < startPos) {
          i++;
        }
      }
      position = index + 1;
      if (i < tokenizedSentences.size()) {
        position -= tokenizedSentences.get(i).length();
        return tokenizedSentences.get(i);
      }
      return "";
    }
    currentPara = paraText;
    tokenizedSentences = langTool.sentenceTokenize(cleanFootnotes(paraText));
    position = 0;
    if (!tokenizedSentences.isEmpty()) {
      return tokenizedSentences.get(0);
    }
    return "";
  }

  // Fix numbers that are (probably) foot notes. 
  // See https://bugs.freedesktop.org/show_bug.cgi?id=69416
  // non-private for test case
  String cleanFootnotes(String paraText) {
    return paraText.replaceAll("([^\\d][.!?])\\d ", "$1¹ ");
  }

  private synchronized SingleProofreadingError[] checkParaRules(
      final String paraText, final Locale locale, final int startPos,
      final int endPos, final String docID) {
    if (startPos == 0) {
      try {
        paragraphMatches = langTool.check(paraText, false,
            JLanguageTool.ParagraphHandling.ONLYPARA);
        this.docID = docID;
      } catch (final Throwable t) {
        showError(t);
      }
    }
    if (paragraphMatches != null && !paragraphMatches.isEmpty()
        && docID.equals(this.docID)) {
      final List<SingleProofreadingError> errorList = new ArrayList<>(
          paragraphMatches.size());
      for (final RuleMatch myRuleMatch : paragraphMatches) {
        final int startErrPos = myRuleMatch.getFromPos();
        final int endErrPos = myRuleMatch.getToPos();
        if (startErrPos >= startPos && startErrPos < endPos
            && endErrPos >= startPos && endErrPos < endPos) {
          errorList.add(createOOoError(myRuleMatch, 0));
        }
      }
      if (!errorList.isEmpty()) {
        final SingleProofreadingError[] errorArray = errorList
            .toArray(new SingleProofreadingError[errorList.size()]);
        Arrays.sort(errorArray, new ErrorPositionComparator());
        return errorArray;
      }
    }
    return null;
  }

  /**
   * Creates a SingleGrammarError object for use in LO/OO.
   */
  private SingleProofreadingError createOOoError(final RuleMatch ruleMatch,
      final int startIndex) {
    final SingleProofreadingError aError = new SingleProofreadingError();
    aError.nErrorType = com.sun.star.text.TextMarkupType.PROOFREADING;
    // the API currently has no support for formatting text in comments
    aError.aFullComment = ruleMatch.getMessage()
        .replaceAll("<suggestion>", "\"").replaceAll("</suggestion>", "\"")
        .replaceAll("([\r]*\n)", " ");
    // not all rules have short comments
    if (!StringTools.isEmpty(ruleMatch.getShortMessage())) {
      aError.aShortComment = ruleMatch.getShortMessage();
    } else {
      aError.aShortComment = aError.aFullComment;
    }
    aError.aShortComment = org.languagetool.gui.Tools
        .shortenComment(aError.aShortComment);

    aError.aSuggestions = ruleMatch.getSuggestedReplacements().toArray(
        new String[ruleMatch.getSuggestedReplacements().size()]);
    aError.nErrorStart = ruleMatch.getFromPos() + startIndex;
    aError.nErrorLength = ruleMatch.getToPos() - ruleMatch.getFromPos();
    aError.aRuleIdentifier = ruleMatch.getRule().getId();
    // LibreOffice since version 3.5 supports an URL that provides more
    // information about the error,
    // older version will simply ignore the property:
    if (ruleMatch.getRule().getUrl() != null) {
      aError.aProperties = new PropertyValue[] { new PropertyValue(
          "FullCommentURL", -1, ruleMatch.getRule().getUrl().toString(),
          PropertyState.DIRECT_VALUE) };
    } else {
      aError.aProperties = new PropertyValue[0];
    }
    return aError;
  }

  /**
   * We leave spell checking to OpenOffice/LibreOffice.
   * @return false
   */
  @Override
  public final boolean isSpellChecker() {
    return false;
  }

  /**
   * Runs LT options dialog box.
   */
  public final void runOptionsDialog() {
    final Language lang = getLanguage();
    if (lang == null) {
      return;
    }
    prepareConfig(lang);
    final ConfigThread configThread = new ConfigThread(lang, config, this);
    configThread.start();
  }

  /**
   * @return An array of Locales supported by LT
   */
  @Override
  public final Locale[] getLocales() {
    try {
      List<Locale> locales = new ArrayList<>();
      for (final Language lang : Language.LANGUAGES) {
        if (lang.getCountries().length == 0) {
          // e.g. Esperanto
          if (lang.getVariant() != null) {
            locales.add(new Locale(LIBREOFFICE_SPECIAL_LANGUAGE_TAG, "", lang.getShortNameWithCountryAndVariant()));
          } else {
            locales.add(new Locale(lang.getShortName(), "", ""));
          }
        } else {
          for (final String country : lang.getCountries()) {
            if (lang.getVariant() != null) {
              locales.add(new Locale(LIBREOFFICE_SPECIAL_LANGUAGE_TAG, country, lang.getShortNameWithCountryAndVariant()));
            } else {
              locales.add(new Locale(lang.getShortName(), country, ""));
            }
          }
        }
      }
      return locales.toArray(new Locale[locales.size()]);
    } catch (final Throwable t) {
      showError(t);
      return new Locale[0];
    }
  }

  /**
   * @return true if LT supports the language of a given locale
   * @param locale The Locale to check
   */
  @Override
  public final boolean hasLocale(final Locale locale) {
    try {
      for (final Language element : Language.LANGUAGES) {
        if (locale.Language.equalsIgnoreCase(LIBREOFFICE_SPECIAL_LANGUAGE_TAG)
            && element.getShortNameWithCountryAndVariant().equals(locale.Variant)) {
          return true;
        }
        if (element.getShortName().equals(locale.Language)) {
          return true;
        }
      }
    } catch (final Throwable t) {
      showError(t);
    }
    return false;
  }

  /**
   * Add a listener that allow re-checking the document after changing the
   * options in the configuration dialog box.
   * 
   * @param eventListener the listener to be added
   * @return true if listener is non-null and has been added, false otherwise
   */
  @Override
  public final boolean addLinguServiceEventListener(
      final XLinguServiceEventListener eventListener) {
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
  @Override
  public final boolean removeLinguServiceEventListener(
      final XLinguServiceEventListener eventListener) {
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
   * Inform listener (grammar checking iterator) that options have changed and
   * the doc should be rechecked.
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
        disabledRules = new HashSet<>();
      }
    }
  }

  @Override
  public String[] getSupportedServiceNames() {
    return getServiceNames();
  }

  public static String[] getServiceNames() {
    return SERVICE_NAMES;
  }

  @Override
  public boolean supportsService(final String sServiceName) {
    for (final String sName : SERVICE_NAMES) {
      if (sServiceName.equals(sName)) {
        return true;
      }
    }
    return false;
  }

  @Override
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
    return Factory.writeRegistryServiceInfo(Main.class.getName(),
        Main.getServiceNames(), regKey);
  }

  @Override
  public void trigger(final String sEvent) {
    if(Thread.currentThread().getContextClassLoader() == null) {
      Thread.currentThread().setContextClassLoader(Main.class.getClassLoader());
    }
    if (!javaVersionOkay()) {
      return;
    }
    try {
      if ("configure".equals(sEvent)) {
        runOptionsDialog();
      } else if ("about".equals(sEvent)) {
        final AboutDialogThread aboutThread = new AboutDialogThread(MESSAGES);
        aboutThread.start();
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
            || version.startsWith("1.2") || version.startsWith("1.3")
            || version.startsWith("1.4") || version.startsWith("1.5")
            || version.startsWith("1.6"))) {
      final DialogThread dt = new DialogThread(
          "Error: LanguageTool requires Java 7.0 or later. Current version: " + version);
      dt.start();
      return false;
    }
    try {
      // do not set look and feel for on Mac OS X as it causes the following error:
      // soffice[2149:2703] Apple AWT Java VM was loaded on first thread -- can't start AWT.
      if (!System.getProperty("os.name").contains("OS X")) {
        for (UIManager.LookAndFeelInfo info : UIManager
            .getInstalledLookAndFeels()) {
          if ("Nimbus".equals(info.getName())) {
            UIManager.setLookAndFeel(info.getClassName());
            break;
          }
        }
      }
    } catch (Exception ignored) {
      // Well, what can we do...
    }
    return true;
  }

  static void showError(final Throwable e) {
    String msg = "An error has occurred in LanguageTool "
        + JLanguageTool.VERSION + ":\n" + e.toString() + "\nStacktrace:\n";
    msg += Tools.getFullStackTrace(e);
    final String metaInfo = "OS: " + System.getProperty("os.name") + " on "
        + System.getProperty("os.arch") + ", Java version "
        + System.getProperty("java.version") + " from "
        + System.getProperty("java.vm.vendor");
    msg += metaInfo;
    final DialogThread dt = new DialogThread(msg);
    dt.start();
  }

  private File getHomeDir() {
    final String homeDir = System.getProperty("user.home");
    if (homeDir == null) {
      @SuppressWarnings("ThrowableInstanceNeverThrown")
      final RuntimeException ex = new RuntimeException("Could not get home directory");
      showError(ex);
    }
    return new File(homeDir);
  }

  private class AboutDialogThread extends Thread {

    private final ResourceBundle messages;

    AboutDialogThread(final ResourceBundle messages) {
      this.messages = messages;
    }

    @Override
    public void run() {
      // TODO: null can cause the dialog to appear on the wrong screen in a
      // multi-monitor setup, but we just don't have a proper java.awt.Component
      // here which we could use instead:
      final AboutDialog about = new AboutDialog(messages, null);
      about.show();
    }
  }

  /**
   * Called when "Ignore" is selected e.g. in the context menu for an error.
   */
  @Override
  public void ignoreRule(final String ruleId, final Locale locale)
      throws IllegalArgumentException {
    // TODO: config should be locale-dependent
    disabledRulesUI.add(ruleId);
    config.setDisabledRuleIds(disabledRulesUI);
    try {
      config.saveConfiguration(langTool.getLanguage());
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
  @Override
  public void resetIgnoreRules() {
    config.setDisabledRuleIds(disabledRules);
    try {
      config.saveConfiguration(langTool.getLanguage());
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
 */
class ErrorPositionComparator implements Comparator<SingleProofreadingError> {

  @Override
  public int compare(final SingleProofreadingError match1,
      final SingleProofreadingError match2) {
    if (match1.aSuggestions.length == 0 && match2.aSuggestions.length > 0) {
      return 1;
    }
    if (match2.aSuggestions.length == 0 && match1.aSuggestions.length > 0) {
      return -1;
    }
    final int error1pos = match1.nErrorStart;
    final int error2pos = match2.nErrorStart;
    if (error1pos > error2pos) {
      return 1;
    } else if (error1pos < error2pos) {
      return -1;
    } else {
      if (match1.aSuggestions.length != 0 && match2.aSuggestions.length != 0
          && match1.aSuggestions.length != match2.aSuggestions.length) {
        return ((Integer) (match1.aSuggestions.length))
            .compareTo(match2.aSuggestions.length);
      }
    }
    return match1.aRuleIdentifier.compareTo(match2.aRuleIdentifier);
  }
}

class DialogThread extends Thread {
  private final String text;

  DialogThread(final String text) {
    this.text = text;
  }

  @Override
  public void run() {
    JOptionPane.showMessageDialog(null, text);
  }
}
