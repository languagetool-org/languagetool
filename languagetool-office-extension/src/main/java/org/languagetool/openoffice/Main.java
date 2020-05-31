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
          "org.languagetool.openoffice.Main" };

  private XComponentContext xContext;
  
  private MultiDocumentsHandler documents = null;


  public Main(XComponentContext xCompContext) {
    documents = new MultiDocumentsHandler(xContext, this, this);
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
    return documents.doProofreading(docID, paraText, locale, startOfSentencePos, nSuggestedBehindEndOfSentencePosition, propertyValues);
  }

  /**
   * We leave spell checking to OpenOffice/LibreOffice.
   * @return false
   */
  @Override
  public final boolean isSpellChecker() {
    return documents.isSpellChecker();
  }
  
  /**
   * @return An array of Locales supported by LT
   */
  @Override
  public final Locale[] getLocales() {
    return documents.getLocales();
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
    return documents.addLinguServiceEventListener(eventListener);
  }

  /**
   * Remove a listener from the event listeners list.
   * 
   * @param eventListener the listener to be removed
   * @return true if listener is non-null and has been removed, false otherwise
   */
  @Override
  public final boolean removeLinguServiceEventListener(XLinguServiceEventListener eventListener) {
    return documents.removeLinguServiceEventListener(eventListener);
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
   * Called when "Ignore" is selected e.g. in the context menu for an error.
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
   */
  @Override
  public void resetIgnoreRules() {
    documents.resetIgnoreRules();
  }

  @Override
  public String getServiceDisplayName(Locale locale) {
    return documents.getServiceDisplayName(locale);
  }

  /**
   * remove internal stored text if document disposes
   */
  @Override
  public void disposing(EventObject source) {
    documents.disposing(source);
  }

}
