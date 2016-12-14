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

/**
 * LibreOffice/OpenOffice integration.
 * 
 * @author Marcin Miłkowski
 */
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.sun.star.lang.*;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.linguistic2.LinguServiceEvent;
import com.sun.star.linguistic2.LinguServiceEventFlags;
import com.sun.star.text.TextMarkupType;

import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.gui.AboutDialog;
import org.languagetool.gui.Configuration;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
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

  private static final int MAX_SUGGESTIONS = 15;
  
  private static boolean testMode;

  private final List<XLinguServiceEventListener> xEventListeners;

  private Configuration config;
  private JLanguageTool langTool;
  private Language docLanguage;
  private String docID;

  // Rules disabled using the config dialog box rather than Spelling dialog box
  // or the context menu.
  private Set<String> disabledRules;
  private Set<String> disabledRulesUI;

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

  public Main(XComponentContext xCompContext) {
    changeContext(xCompContext);
    xEventListeners = new ArrayList<>();
  }

  private void prepareConfig(Language lang) {
    try {
      config = new Configuration(getHomeDir(), CONFIG_FILE, lang);
      disabledRules = config.getDisabledRuleIds();
      if (disabledRules == null) {
        disabledRules = new HashSet<>();
      }
      disabledRulesUI = new HashSet<>(disabledRules);
    } catch (Throwable t) {
      showError(t);
    }
  }

  public final void changeContext(XComponentContext xCompContext) {
    xContext = xCompContext;
  }

  @Nullable
  private XComponent getXComponent() {
    try {
      XMultiComponentFactory xMCF = xContext.getServiceManager();
      Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
      XDesktop xDesktop = UnoRuntime.queryInterface(XDesktop.class, desktop);
      return xDesktop.getCurrentComponent();
    } catch (Throwable t) {
      showError(t);
      return null;
    }
  }

  /**
   * Checks the language under the cursor. Used for opening the configuration dialog.
   * @return the language under the visible cursor
   */
  @Nullable
  private Language getLanguage() {
    XComponent xComponent = getXComponent();
    Locale charLocale;
    XPropertySet xCursorProps;
    try {
      XModel model = UnoRuntime.queryInterface(XModel.class, xComponent);
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
      showError(t);
      return null;
    }
    return getLanguage(charLocale);
  }

  private Language getLanguage(Locale locale) {
    try {
      if (locale.Language.equalsIgnoreCase(LIBREOFFICE_SPECIAL_LANGUAGE_TAG)) {
        return Languages.getLanguageForShortCode(locale.Variant);
      } else {
        return Languages.getLanguageForShortCode(locale.Language + "-" + locale.Country);
      }
    } catch (java.lang.IllegalArgumentException e) {
      return Languages.getLanguageForShortCode(locale.Language);
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
  public final ProofreadingResult doProofreading(String docID,
      String paraText, Locale locale, int startOfSentencePos,
      int nSuggestedBehindEndOfSentencePosition,
      PropertyValue[] propertyValues) {
    ProofreadingResult paRes = new ProofreadingResult();
    try {
      paRes.nStartOfSentencePosition = startOfSentencePos;
      paRes.xProofreader = this;
      paRes.aLocale = locale;
      paRes.aDocumentIdentifier = docID;
      paRes.aText = paraText;
      paRes.aProperties = propertyValues;
      int[] footnotePositions = getPropertyValues("FootnotePositions", propertyValues);  // since LO 4.3
      return doGrammarCheckingInternal(paraText, locale, paRes, footnotePositions);
    } catch (Throwable t) {
      showError(t);
      return paRes;
    }
  }

  private int[] getPropertyValues(String propName, PropertyValue[] propertyValues) {
    for (PropertyValue propertyValue : propertyValues) {
      if (propName.equals(propertyValue.Name)) {
        if (propertyValue.Value instanceof int[]) {
          return (int[]) propertyValue.Value;
        } else {
          System.err.println("Not of expected type int[]: " + propertyValue.Name + ": " + propertyValue.Value.getClass());
        }
      }
    }
    return new int[]{};  // e.g. for LO/OO < 4.3 and the 'FootnotePositions' property
  }

  private synchronized ProofreadingResult doGrammarCheckingInternal(
      String paraText, Locale locale, ProofreadingResult paRes, int[] footnotePositions) {

    if (!StringTools.isEmpty(paraText) && hasLocale(locale)) {
      Language langForShortName = getLanguage(locale);
      if (!langForShortName.equals(docLanguage) || langTool == null || recheck) {
        docLanguage = langForShortName;
        initLanguageTool();
      }

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
          langTool.disableCategory(categoryName);
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
      try {
        String sentence = getSentence(paraText,
            paRes.nStartOfSentencePosition);
        paRes.nStartOfSentencePosition = position;
        paRes.nStartOfNextSentencePosition = position + sentence.length();
        paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;
        if (!StringTools.isEmpty(sentence)) {
          AnnotatedText annotatedText = getAnnotatedText(sentence, footnotePositions, paRes);
          List<RuleMatch> ruleMatches = langTool.check(annotatedText, false,
              JLanguageTool.ParagraphHandling.ONLYNONPARA);
          SingleProofreadingError[] pErrors = checkParaRules(paraText, paRes.nStartOfSentencePosition,
              paRes.nStartOfNextSentencePosition, paRes.aDocumentIdentifier);
          int pErrorCount = 0;
          if (pErrors != null) {
            pErrorCount = pErrors.length;
          }
          if (!ruleMatches.isEmpty()) {
            SingleProofreadingError[] errorArray = 
                    new SingleProofreadingError[ruleMatches.size() + pErrorCount];
            int i = 0;
            for (RuleMatch myRuleMatch : ruleMatches) {
              errorArray[i] = createOOoError(myRuleMatch, paRes.nStartOfSentencePosition);
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
      } catch (Throwable t) {
        showError(t);
        paRes.nBehindEndOfSentencePosition = paraText.length();
      }
    }
    return paRes;
  }

  private AnnotatedText getAnnotatedText(String sentence, int[] footnotePos, ProofreadingResult paRes) {
    Set<Integer> correctedPos = new HashSet<>();
    for (int pos : footnotePos) {
      correctedPos.add(pos - paRes.nStartOfSentencePosition);
    }
    AnnotatedTextBuilder annotations = new AnnotatedTextBuilder();
    // not very efficient but simple implementation:
    for (int i = 0; i < sentence.length(); i++) {
      if (correctedPos.contains(i)) {
        annotations.addMarkup("\u200B");
      } else {
        annotations.addText(String.valueOf(sentence.charAt(i)));
      }
    }
    return annotations.build();
  }

  private void initLanguageTool() {
    try {
      prepareConfig(docLanguage);
      // not using MultiThreadedJLanguageTool here fixes "osl::Thread::Create failed", see https://bugs.documentfoundation.org/show_bug.cgi?id=90740:
      langTool = new JLanguageTool(docLanguage, config.getMotherTongue());
      File ngramDirectory = config.getNgramDirectory();
      if (ngramDirectory != null) {
        File ngramLangDir = new File(config.getNgramDirectory(), docLanguage.getShortCode());
        if (ngramLangDir.exists()) {  // user might have ngram data only for some languages and that's okay
          langTool.activateLanguageModelRules(ngramDirectory);
        }
      }
      for (Rule rule : langTool.getAllActiveRules()) {
        if (rule.isDictionaryBasedSpellingRule()) {
          langTool.disableRule(rule.getId());
        }
        if (rule.useInOffice()) {
          langTool.enableRule(rule.getId());
        }
      }
      recheck = false;
    } catch (Throwable t) {
      showError(t);
    }
  }

  private synchronized String getSentence(String paraText, int startPos) {
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

  @Nullable
  private synchronized SingleProofreadingError[] checkParaRules(
      String paraText, int startPos,
      int endPos, String docID) {
    if (startPos == 0) {
      try {
        paragraphMatches = langTool.check(paraText, false,
            JLanguageTool.ParagraphHandling.ONLYPARA);
        this.docID = docID;
      } catch (Throwable t) {
        showError(t);
      }
    }
    if (paragraphMatches != null && !paragraphMatches.isEmpty() && docID.equals(this.docID)) {
      List<SingleProofreadingError> errorList = new ArrayList<>(paragraphMatches.size());
      for (RuleMatch myRuleMatch : paragraphMatches) {
        int startErrPos = myRuleMatch.getFromPos();
        int endErrPos = myRuleMatch.getToPos();
        if (startErrPos >= startPos && startErrPos < endPos
            && endErrPos >= startPos && endErrPos < endPos) {
          errorList.add(createOOoError(myRuleMatch, 0));
        }
      }
      if (!errorList.isEmpty()) {
        SingleProofreadingError[] errorArray = errorList.toArray(new SingleProofreadingError[errorList.size()]);
        Arrays.sort(errorArray, new ErrorPositionComparator());
        return errorArray;
      }
    }
    return null;
  }

  /**
   * Creates a SingleGrammarError object for use in LO/OO.
   */
  private SingleProofreadingError createOOoError(RuleMatch ruleMatch, int startIndex) {
    SingleProofreadingError aError = new SingleProofreadingError();
    aError.nErrorType = TextMarkupType.PROOFREADING;
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
    aError.aShortComment = org.languagetool.gui.Tools.shortenComment(aError.aShortComment);
    int numSuggestions = ruleMatch.getSuggestedReplacements().size();
    String[] allSuggestions = ruleMatch.getSuggestedReplacements().toArray(new String[numSuggestions]);
    if (numSuggestions > MAX_SUGGESTIONS) {
      aError.aSuggestions = Arrays.copyOfRange(allSuggestions, 0, MAX_SUGGESTIONS);
    } else {
      aError.aSuggestions = allSuggestions;
    }
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
    Language lang = getLanguage();
    if (lang == null) {
      return;
    }
    prepareConfig(lang);
    ConfigThread configThread = new ConfigThread(lang, config, this);
    configThread.start();
  }

  /**
   * @return An array of Locales supported by LT
   */
  @Override
  public final Locale[] getLocales() {
    try {
      List<Locale> locales = new ArrayList<>();
      for (Language lang : Languages.get()) {
        if (lang.getCountries().length == 0) {
          // e.g. Esperanto
          if (lang.getVariant() != null) {
            locales.add(new Locale(LIBREOFFICE_SPECIAL_LANGUAGE_TAG, "", lang.getShortCodeWithCountryAndVariant()));
          } else {
            locales.add(new Locale(lang.getShortCode(), "", ""));
          }
        } else {
          for (String country : lang.getCountries()) {
            if (lang.getVariant() != null) {
              locales.add(new Locale(LIBREOFFICE_SPECIAL_LANGUAGE_TAG, country, lang.getShortCodeWithCountryAndVariant()));
            } else {
              locales.add(new Locale(lang.getShortCode(), country, ""));
            }
          }
        }
      }
      return locales.toArray(new Locale[locales.size()]);
    } catch (Throwable t) {
      showError(t);
      return new Locale[0];
    }
  }

  /**
   * @return true if LT supports the language of a given locale
   * @param locale The Locale to check
   */
  @Override
  public final boolean hasLocale(Locale locale) {
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
  @Override
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
   * Inform listener (grammar checking iterator) that options have changed and
   * the doc should be rechecked.
   */
  public final void resetDocument() {
    if (!xEventListeners.isEmpty()) {
      for (XLinguServiceEventListener xEvLis : xEventListeners) {
        if (xEvLis != null) {
          LinguServiceEvent xEvent = new LinguServiceEvent();
          xEvent.nEvent = LinguServiceEventFlags.PROOFREAD_AGAIN;
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
  public boolean supportsService(String sServiceName) {
    for (String sName : SERVICE_NAMES) {
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

  public static XSingleComponentFactory __getComponentFactory(String sImplName) {
    SingletonFactory xFactory = null;
    if (sImplName.equals(Main.class.getName())) {
      xFactory = new SingletonFactory();
    }
    return xFactory;
  }

  public static boolean __writeRegistryServiceInfo(XRegistryKey regKey) {
    return Factory.writeRegistryServiceInfo(Main.class.getName(), Main.getServiceNames(), regKey);
  }

  @Override
  public void trigger(String sEvent) {
    if (Thread.currentThread().getContextClassLoader() == null) {
      Thread.currentThread().setContextClassLoader(Main.class.getClassLoader());
    }
    if (!javaVersionOkay()) {
      return;
    }
    try {
      if ("configure".equals(sEvent)) {
        runOptionsDialog();
      } else if ("about".equals(sEvent)) {
        AboutDialogThread aboutThread = new AboutDialogThread(MESSAGES);
        aboutThread.start();
      } else {
        System.err.println("Sorry, don't know what to do, sEvent = " + sEvent);
      }
    } catch (Throwable e) {
      showError(e);
    }
  }

  private boolean javaVersionOkay() {
    String version = System.getProperty("java.version");
    if (version != null
        && (version.startsWith("1.0") || version.startsWith("1.1")
            || version.startsWith("1.2") || version.startsWith("1.3")
            || version.startsWith("1.4") || version.startsWith("1.5")
            || version.startsWith("1.6"))) {
      DialogThread dt = new DialogThread(
          "Error: LanguageTool requires Java 7.0 or later. Current version: " + version);
      dt.start();
      return false;
    }
    try {
      // do not set look and feel for on Mac OS X as it causes the following error:
      // soffice[2149:2703] Apple AWT Java VM was loaded on first thread -- can't start AWT.
      if (!System.getProperty("os.name").contains("OS X")) {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
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

  static void showError(Throwable e) {
    if (testMode) {
      throw new RuntimeException(e);
    }
    String msg = "An error has occurred in LanguageTool "
        + JLanguageTool.VERSION + ":\n" + e + "\nStacktrace:\n";
    msg += Tools.getFullStackTrace(e);
    String metaInfo = "OS: " + System.getProperty("os.name") + " on "
        + System.getProperty("os.arch") + ", Java version "
        + System.getProperty("java.version") + " from "
        + System.getProperty("java.vm.vendor");
    msg += metaInfo;
    DialogThread dt = new DialogThread(msg);
    e.printStackTrace();
    dt.start();
  }

  private File getHomeDir() {
    String homeDir = System.getProperty("user.home");
    if (homeDir == null) {
      showError(new RuntimeException("Could not get home directory"));
    }
    return new File(homeDir);
  }

  /**
   * Will throw exception instead of showing errors as dialogs - use only for test cases.
   * @since 2.9
   */
  static void setTestMode(boolean mode) {
    testMode = mode;
  }

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
  @Override
  public void ignoreRule(String ruleId, Locale locale)
      throws IllegalArgumentException {
    // TODO: config should be locale-dependent
    disabledRulesUI.add(ruleId);
    config.setDisabledRuleIds(disabledRulesUI);
    try {
      config.saveConfiguration(langTool.getLanguage());
    } catch (Throwable t) {
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
    } catch (Throwable t) {
      showError(t);
    }
    recheck = true;
  }

  @Override
  public String getServiceDisplayName(Locale locale) {
    return "LanguageTool";
  }

  static class DialogThread extends Thread {
    private final String text;

    DialogThread(String text) {
      this.text = text;
    }

    @Override
    public void run() {
      JOptionPane.showMessageDialog(null, text);
    }
  }

}
