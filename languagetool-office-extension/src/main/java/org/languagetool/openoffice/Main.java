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

import com.sun.star.lang.*;
import com.sun.star.beans.PropertyValue;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.linguistic2.XLinguServiceEventBroadcaster;
import com.sun.star.linguistic2.XLinguServiceEventListener;
import com.sun.star.linguistic2.XProofreader;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.task.XJobExecutor;
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
      OfficeTools.LT_SERVICE_NAME };

  private XComponentContext xContext;
  private MultiDocumentsHandler documents;

  public Main(XComponentContext xCompContext) {
    changeContext(xCompContext);
    documents = new MultiDocumentsHandler(xContext, this, this);
  }

  /**
   * Changes the XComponentContext
   */
  void changeContext(XComponentContext xCompContext) {
    xContext = xCompContext;
    if (documents != null) {
      documents.setComponentContext(xCompContext);
    }
  }

  /**
   * Runs the grammar checker on paragraph text.
   * interface: XProofreader
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
    return documents.doProofreading(docID, paraText, locale, startOfSentencePos, nSuggestedBehindEndOfSentencePosition, propertyValues);
  }

  /**
   * We leave spell checking to OpenOffice/LibreOffice.
   * interface: XProofreader
   * @return false
   */
  @Override
  public final boolean isSpellChecker() {
    return documents.isSpellChecker();
  }
  
  /**
   * @return An array of Locales supported by LT
   * interface: XProofreader
   */
  @Override
  public final Locale[] getLocales() {
    return MultiDocumentsHandler.getLocales();
  }

  /**
   * @return true if LT supports the language of a given locale
   * @param locale The Locale to check
   * interface: XProofreader
   */
  @Override
  public final boolean hasLocale(Locale locale) {
    return MultiDocumentsHandler.hasLocale(locale);
  }

  /**
   * Add a listener that allow re-checking the document after changing the
   * options in the configuration dialog box.
   * interface: XLinguServiceEventBroadcaster
   * @param eventListener the listener to be added
   * @return true if listener is non-null and has been added, false otherwise
   */
  @Override
  public final boolean addLinguServiceEventListener(XLinguServiceEventListener eventListener) {
    return documents.addLinguServiceEventListener(eventListener);
  }

  /**
   * Remove a listener from the event listeners list.
   * interface: XLinguServiceEventBroadcaster
   * @param eventListener the listener to be removed
   * @return true if listener is non-null and has been removed, false otherwise
   */
  @Override
  public final boolean removeLinguServiceEventListener(XLinguServiceEventListener eventListener) {
    return documents.removeLinguServiceEventListener(eventListener);
  }

  /**
   * Get the names of supported services
   * interface: XServiceInfo
   */
  @Override
  public String[] getSupportedServiceNames() {
    return getServiceNames();
  }

  /**
   * Get the LT service names
   */
  static String[] getServiceNames() {
    return SERVICE_NAMES;
  }

  /**
   * Test if the service is supported by LT
   * interface: XServiceInfo
   */
  @Override
  public boolean supportsService(String sServiceName) {
    for (String sName : SERVICE_NAMES) {
      if (sServiceName.equals(sName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get the implementation name of the LT service
   * interface: XServiceInfo
   */
  @Override
  public String getImplementationName() {
    return Main.class.getName();
  }

  /**
   * Get XSingleComponentFactory
   * Default method called by LO/OO extensions
   */
  public static XSingleComponentFactory __getComponentFactory(String sImplName) {
    SingletonFactory xFactory = null;
    if (sImplName.equals(Main.class.getName())) {
      xFactory = new SingletonFactory(false);
    } else if (sImplName.equals(LanguageToolSpellChecker.class.getName())) {
      xFactory = new SingletonFactory(true);
    }
    return xFactory;
  }

  /**
   * Write keys to registry
   * Default method called by LO/OO extensions
   */
  public static boolean __writeRegistryServiceInfo(XRegistryKey regKey) {
    boolean ret = Factory.writeRegistryServiceInfo(Main.class.getName(), Main.getServiceNames(), regKey);
    ret = ret && Factory.writeRegistryServiceInfo(LanguageToolSpellChecker.class.getName(), LanguageToolSpellChecker.getServiceNames(), regKey);
    return ret;
  }

  /**
   * Triggers a registered event
   * interface: XJobExecutor
   */
  @Override
  public void trigger(String sEvent) {
    if (Thread.currentThread().getContextClassLoader() == null) {
      Thread.currentThread().setContextClassLoader(Main.class.getClassLoader());
    }
    documents.trigger(sEvent);
  }

  /**
   * Will throw exception instead of showing errors as dialogs - use only for test cases.
   * @since 2.9
   */
  void setTestMode(boolean mode) {
    documents.setTestMode(mode);
  }
  
  /**
   * Give back the MultiDocumentsHandler - use only for test cases.
   * @since 5.3
   */
  MultiDocumentsHandler getMultiDocumentsHandler() {
    return documents;
  }
  
  /**
   * Called when "Ignore" is selected e.g. in the context menu for an error.
   * interface: XProofreader
   */
  @Override
  public void ignoreRule(String ruleId, Locale locale) {
    documents.ignoreRule(ruleId, locale);;
  }

  /**
   * Called on rechecking the document - resets the ignore status for rules that
   * was set in the spelling dialog box or in the context menu.
   * 
   * The rules disabled in the config dialog box are left as intact.
   * interface: XProofreader
   */
  @Override
  public void resetIgnoreRules() {
    documents.resetIgnoreRules();
  }

  /**
   * Get the display name of the LT service
   * Interface: XServiceDisplayName
   */
  @Override
  public String getServiceDisplayName(Locale locale) {
    return MultiDocumentsHandler.getServiceDisplayName(locale);
  }

  /**
   * Remove internal stored text if document disposes
   * Interface: XEventListener
   */
  @Override
  public void disposing(EventObject source) {
    documents.disposing(source);
  }
/*
  @Override
  public boolean isValid(String word, Locale locale, PropertyValue[] Properties) throws IllegalArgumentException {
    return documents.isValid(word, locale, Properties);
  }

  @Override
  public XSpellAlternatives spell(String word, Locale locale, PropertyValue[] properties) throws IllegalArgumentException {
    return documents.spell(word, locale, properties);
  }
*/
}
