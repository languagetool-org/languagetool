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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.UIManager;

import com.sun.star.lang.*;
import com.sun.star.linguistic2.LinguServiceEvent;
import com.sun.star.linguistic2.LinguServiceEventFlags;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.gui.AboutDialog;
import org.languagetool.gui.Configuration;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.linguistic2.XLinguServiceEventBroadcaster;
import com.sun.star.linguistic2.XLinguServiceEventListener;
import com.sun.star.linguistic2.XProofreader;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.task.XJobExecutor;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * LibreOffice/OpenOffice integration.
 *
 * @author Marcin Miłkowski, Fred Kruse
 */
public class Main extends WeakBase implements XJobExecutor,
    XServiceDisplayName, XServiceInfo, XProofreader,
    XLinguServiceEventBroadcaster, XEventListener {

  // Service name required by the OOo API && our own name.
  private static final String[] SERVICE_NAMES = {
          "com.sun.star.linguistic2.Proofreader",
          "org.languagetool.openoffice.Main" };

  // use a different name than the stand-alone version to avoid conflicts:
  private static final String CONFIG_FILE = ".languagetool-ooo.cfg";

  // use a log-file for output of messages and debug information:
  private static final String LOG_FILE = ".LanguageTool.log";

  private static final ResourceBundle MESSAGES = JLanguageTool.getMessageBundle();

  // LibreOffice (since 4.2.0) special tag for locale with variant 
  // e.g. language ="qlt" country="ES" variant="ca-ES-valencia":
  private static final String LIBREOFFICE_SPECIAL_LANGUAGE_TAG = "qlt";

  private final List<XLinguServiceEventListener> xEventListeners;

  // Rules disabled using the config dialog box rather than Spelling dialog box
  // or the context menu.
  private Set<String> disabledRules = null;
  private Set<String> disabledRulesUI;

  private XComponentContext xContext;
  
  private MultiDocumentsHandler documents = null;


  public Main(XComponentContext xCompContext) {
    changeContext(xCompContext);
    xEventListeners = new ArrayList<>();
    File homeDir = getHomeDir();
    String homeDirName = homeDir == null ? "." : homeDir.toString();
    MessageHandler.init(homeDirName, LOG_FILE);
    documents = new MultiDocumentsHandler(xContext, getHomeDir(), CONFIG_FILE, MESSAGES, this);
  }

  private Configuration prepareConfig() {
    try {
      Configuration config = documents.getConfiguration();
      if (config != null) {
        disabledRules = config.getDisabledRuleIds();
      }
      if (disabledRules == null) {
        disabledRules = new HashSet<>();
      }
      disabledRulesUI = new HashSet<>(disabledRules);
      return config;

    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    return null;
  }

  void changeContext(XComponentContext xCompContext) {
    xContext = xCompContext;
    if(documents != null) {
      documents.setComponentContext(xCompContext);
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
    paRes.nStartOfSentencePosition = startOfSentencePos;
    paRes.nStartOfNextSentencePosition = nSuggestedBehindEndOfSentencePosition;
    paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;
    paRes.xProofreader = this;
    paRes.aLocale = locale;
    paRes.aDocumentIdentifier = docID;
    paRes.aText = paraText;
    paRes.aProperties = propertyValues;
    try {
      int[] footnotePositions = getPropertyValues("FootnotePositions", propertyValues);  // since LO 4.3
      paRes = documents.getCheckResults(paraText, locale, paRes, footnotePositions);
      if (disabledRules == null) {
        prepareConfig();
      }
      if(documents.doResetCheck()) {
        resetCheck();
        documents.optimizeReset();
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    return paRes;
  }

  private int[] getPropertyValues(String propName, PropertyValue[] propertyValues) {
    for (PropertyValue propertyValue : propertyValues) {
      if (propName.equals(propertyValue.Name)) {
        if (propertyValue.Value instanceof int[]) {
          return (int[]) propertyValue.Value;
        } else {
          MessageHandler.printToLogFile("Not of expected type int[]: " + propertyValue.Name + ": " + propertyValue.Value.getClass());
        }
      }
    }
    return new int[]{};  // e.g. for LO/OO < 4.3 and the 'FootnotePositions' property
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
   * Returns xContext
   */
  public XComponentContext getContext() {
    return xContext;
  }

  /**
   * Runs LT options dialog box.
   */
  private void runOptionsDialog() {
    Configuration config = prepareConfig();
    Language lang = config.getDefaultLanguage();
    if (lang == null) {
      lang = documents.getLanguage();
    }
    if (lang == null) {
      return;
    }
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
      return locales.toArray(new Locale[0]);
    } catch (Throwable t) {
      MessageHandler.showError(t);
      return new Locale[0];
    }
  }

  /**
   * @return true if LT supports the language of a given locale
   * @param locale The Locale to check
   */
  @Override
  public final boolean hasLocale(Locale locale) {
    return documents.hasLocale(locale);
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
   * Inform listener that the doc should be rechecked.
   */
  private boolean resetCheck() {
    if (!xEventListeners.isEmpty()) {
      for (XLinguServiceEventListener xEvLis : xEventListeners) {
        if (xEvLis != null) {
          LinguServiceEvent xEvent = new LinguServiceEvent();
          xEvent.nEvent = LinguServiceEventFlags.PROOFREAD_AGAIN;
          xEvLis.processLinguServiceEvent(xEvent);
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Inform listener (grammar checking iterator) that options have changed and
   * the doc should be rechecked.
   */
  void resetDocument() {
    documents.setRecheck();
    if (resetCheck()) {
      Configuration config = documents.getConfiguration();
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

  static String[] getServiceNames() {
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
      } else if ("switchOff".equals(sEvent)) {
        if(documents.toggleSwitchedOff()) {
          resetCheck();
        }
      } else {
        MessageHandler.printToLogFile("Sorry, don't know what to do, sEvent = " + sEvent);
      }
    } catch (Throwable e) {
      MessageHandler.showError(e);
    }
  }

  private boolean javaVersionOkay() {
    String version = System.getProperty("java.version");
    if (version != null
        && (version.startsWith("1.0") || version.startsWith("1.1")
            || version.startsWith("1.2") || version.startsWith("1.3")
            || version.startsWith("1.4") || version.startsWith("1.5")
            || version.startsWith("1.6") || version.startsWith("1.7"))) {
      MessageHandler.showMessage("Error: LanguageTool requires Java 8 or later. Current version: " + version);
      return false;
    }
    try {
      // do not set look and feel for on Mac OS X as it causes the following error:
      // soffice[2149:2703] Apple AWT Java VM was loaded on first thread -- can't start AWT.
      if (!System.getProperty("os.name").contains("OS X")) {
         // Cross-Platform Look And Feel @since 3.7
         UIManager.setLookAndFeel(
            UIManager.getSystemLookAndFeelClassName());
      }
    } catch (Exception ignored) {
      // Well, what can we do...
    }
    return true;
  }

  private File getHomeDir() {
    String homeDir = System.getProperty("user.home");
    if (homeDir == null) {
      MessageHandler.showError(new RuntimeException("Could not get home directory"));
      return null;
    }
    return new File(homeDir);
  }

  /**
   * Will throw exception instead of showing errors as dialogs - use only for test cases.
   * @since 2.9
   */
  void setTestMode(boolean mode) {
    documents.setTestMode(mode);
    MessageHandler.setTestMode(mode);
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
  public void ignoreRule(String ruleId, Locale locale) {
    /* TODO: config should be locale-dependent */
    Configuration config = documents.getConfiguration();
    disabledRulesUI.add(ruleId);
    config.setDisabledRuleIds(disabledRulesUI);
    try {
      SwJLanguageTool langTool = documents.getLanguageTool();
      documents.initCheck();
      config.saveConfiguration(langTool.getLanguage());
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    documents.setRecheck();
  }

  /**
   * Called on rechecking the document - resets the ignore status for rules that
   * was set in the spelling dialog box or in the context menu.
   * 
   * The rules disabled in the config dialog box are left as intact.
   */
  @Override
  public void resetIgnoreRules() {
    Configuration config = documents.getConfiguration();
    config.setDisabledRuleIds(disabledRules);
    try {
      SwJLanguageTool langTool = documents.getLanguageTool();
      documents.initCheck();
      config.saveConfiguration(langTool.getLanguage());
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    documents.setRecheck();
  }

  @Override
  public String getServiceDisplayName(Locale locale) {
    return "LanguageTool";
  }

  /**
   * remove internal stored text if document disposes
   */
  @Override
  public void disposing(EventObject source) {
    //  the data of document will be removed by next call of getNumDocID
    //  to finish checking thread without crashing
    XComponent goneContext = UnoRuntime.queryInterface(XComponent.class, source.Source);
    documents.setContextOfClosedDoc(goneContext);
    goneContext.removeEventListener(this); 
  }

}
