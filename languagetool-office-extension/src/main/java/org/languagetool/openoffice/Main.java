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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.UIManager;

import com.sun.star.lang.*;
import com.sun.star.linguistic2.LinguServiceEvent;
import com.sun.star.linguistic2.LinguServiceEventFlags;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
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

  private static final String VENDOR_ID = "languagetool.org";
  private static final String APPLICATION_ID = "LanguageTool";
  private static final String OFFICE_EXTENSION_ID = "LibreOffice";
  private static final String CONFIG_FILE = "Languagetool.cfg";
  private static final String OLD_CONFIG_FILE = ".languagetool-ooo.cfg";
  private static final String LOG_FILE = "LanguageTool.log";

  private static final ResourceBundle MESSAGES = JLanguageTool.getMessageBundle();

  // LibreOffice (since 4.2.0) special tag for locale with variant 
  // e.g. language ="qlt" country="ES" variant="ca-ES-valencia":
  private static final String LIBREOFFICE_SPECIAL_LANGUAGE_TAG = "qlt";

  private final List<XLinguServiceEventListener> xEventListeners;

  private boolean docReset = false;

  private XComponentContext xContext;
  
  private MultiDocumentsHandler documents = null;


  public Main(XComponentContext xCompContext) {
    changeContext(xCompContext);
    xEventListeners = new ArrayList<>();
    File homeDir = getHomeDir();
    File configDir = getLOConfigDir();
    String configDirName = configDir == null ? "." : configDir.toString();
    File oldConfigFile = homeDir == null ? null : new File(homeDir, OLD_CONFIG_FILE);
    MessageHandler.init(configDirName, LOG_FILE);
    documents = new MultiDocumentsHandler(xContext, configDir, CONFIG_FILE, oldConfigFile, MESSAGES, this);
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
      paRes = documents.getCheckResults(paraText, locale, paRes, footnotePositions, docReset);
      docReset = false;
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
  
  public SwJLanguageTool getJLanguageTool() {
    return documents.getLanguageTool();
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
    Configuration config = documents.getConfiguration();
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
    resetCheck();
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
      } else if ("ignoreOnce".equals(sEvent)) {
        documents.ignoreOnce();
        resetCheck();
        documents.optimizeReset();
      } else if ("deactivateRule".equals(sEvent)) {
        documents.deactivateRule();
        resetDocument();
      } else if ("remoteHint".equals(sEvent)) {
        if(documents.getConfiguration().useOtherServer()) {
          MessageHandler.showMessage(MessageFormat.format(MESSAGES.getString("loRemoteInfoOtherServer"), 
              documents.getConfiguration().getServerUrl()));
        } else {
          MessageHandler.showMessage(MESSAGES.getString("loRemoteInfoDefaultServer"));
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
         if (System.getProperty("os.name").contains("Linux")) {
         UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
         }
         else {
         UIManager.setLookAndFeel(
            UIManager.getSystemLookAndFeelClassName());
         }
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
   * Returns directory to store every information for LT office extension
   * @since 4.7
   */
  private File getLOConfigDir() {
      String userHome = null;
      File directory;
      try {
        userHome = System.getProperty("user.home");
      } catch (SecurityException ex) {
      }
      if (userHome == null) {
        MessageHandler.showError(new RuntimeException("Could not get home directory"));
        directory = null;
      } else if (SystemUtils.IS_OS_WINDOWS) {
        File appDataDir = null;
        try {
          String appData = System.getenv("APPDATA");
          if (!StringUtils.isEmpty(appData)) {
            appDataDir = new File(appData);
          }
        } catch (SecurityException ex) {
        }
        if (appDataDir != null && appDataDir.isDirectory()) {
          String path = VENDOR_ID + "\\" + APPLICATION_ID + "\\" + OFFICE_EXTENSION_ID + "\\";
          directory = new File(appDataDir, path);
        } else {
          String path = "Application Data\\" + VENDOR_ID + "\\" + APPLICATION_ID + "\\" + OFFICE_EXTENSION_ID + "\\";
          directory = new File(userHome, path);
        }
      } else if (SystemUtils.IS_OS_LINUX) {
        File appDataDir = null;
        try {
          String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
          if (!StringUtils.isEmpty(xdgConfigHome)) {
            appDataDir = new File(xdgConfigHome);
            if (!appDataDir.isAbsolute()) {
              //https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html
              //All paths set in these environment variables must be absolute.
              //If an implementation encounters a relative path in any of these
              //variables it should consider the path invalid and ignore it.
              appDataDir = null;
            }
          }
        } catch (SecurityException ex) {
        }
        if (appDataDir != null && appDataDir.isDirectory()) {
          String path = APPLICATION_ID + "/" + OFFICE_EXTENSION_ID + "/";
          directory = new File(appDataDir, path);
        } else {
          String path = ".config/" + APPLICATION_ID + "/" + OFFICE_EXTENSION_ID + "/";
          directory = new File(userHome, path);
        }
      } else if (SystemUtils.IS_OS_MAC_OSX) {
        String path = "Library/Application Support/" + APPLICATION_ID + "/" + OFFICE_EXTENSION_ID + "/";
        directory = new File(userHome, path);
      } else {
        String path = "." + APPLICATION_ID + "/" + OFFICE_EXTENSION_ID + "/";
        directory = new File(userHome, path);
      }
      if (directory != null && !directory.exists()) {
        directory.mkdirs();
      }
      return directory;
  }

  /**
   * Will throw exception instead of showing errors as dialogs - use only for test cases.
   * @since 2.9
   */
  void setTestMode(boolean mode) {
    documents.setTestMode(mode);
    MessageHandler.setTestMode(mode);
  }
  
  /**
   *  get all disabled rules by context menu or spell dialog
   */
  public Set<String> getDisabledRules() {
    return documents.getDisabledRules();
  }
  
  /**
   *  set disabled rules by context menu or spell dialog
   */
  public void setDisabledRules(Set<String> ruleIds) {
    documents.setDisabledRules(ruleIds);;
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
    documents.addDisabledRule(ruleId);
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
    documents.resetDisabledRules();
    documents.setRecheck();
    documents.resetIgnoreOnce();
    docReset = true;
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
