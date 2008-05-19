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
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.Locale;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.linguistic2.XGrammarChecker;
import com.sun.star.text.XFlatParagraph;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.Configuration;

public class NewChecker implements XGrammarChecker {

  private Configuration config;
  
  private XTextDocument xTextDoc;

  //TODO: remove this method and replace with proper one
  // based on XFlatParagraph
  private Language getLanguage() {
    if (xTextDoc == null)
      return Language.ENGLISH; // for testing with local main() method only
    Locale charLocale;
    try {
      // look at the global document language
      /* TODO: make this work
      XDocumentInfoSupplier xdis = (XDocumentInfoSupplier)
        UnoRuntime.queryInterface(XDocumentInfoSupplier.class, xTextDoc);
      XPropertySet docInfo = (XPropertySet) UnoRuntime.queryInterface
        (XPropertySet.class, xdis.getDocumentInfo()); 
      Object lang = docInfo.getPropertyValue("language");
      */
      // just look at the first position in the document and assume that this character's
      // language is the language of the whole document:
      XTextCursor textCursor = xTextDoc.getText().createTextCursor();
      textCursor.gotoStart(false);
      XPropertySet xCursorProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class,
          textCursor);
      charLocale = (Locale) xCursorProps.getPropertyValue("CharLocale");
      boolean langIsSupported = false;
      for (int i = 0; i < Language.LANGUAGES.length; i++) {
        if (Language.LANGUAGES[i].getShortName().equals(charLocale.Language)) {
          langIsSupported = true;
          break;
        }
      }
      if (!langIsSupported) {
        // FIXME: i18n
        DialogThread dt = new DialogThread("Error: Sorry, the document language '" +charLocale.Language+ 
            "' is not supported by LanguageTool.");
        dt.start();
        return null;
      }
      //checkTables();
    } catch (UnknownPropertyException e) {
      throw new RuntimeException(e);
    } catch (WrappedTargetException e) {
      throw new RuntimeException(e);
    }
    return Language.getLanguageForShortName(charLocale.Language);
  }

  
  public void doGrammarChecking(int arg0, XFlatParagraph arg1, Locale arg2,
      int arg3, int arg4) throws IllegalArgumentException {
    // TODO Auto-generated method stub

  }

  public void endDocument(int arg0) throws IllegalArgumentException {
    // TODO Auto-generated method stub

  }

  public void endParagraph(int arg0) throws IllegalArgumentException {
    // TODO Auto-generated method stub

  }

  public int getEndOfSentencePos(int arg0, String arg1, Locale arg2, int arg3)
      throws IllegalArgumentException {
    // TODO Auto-generated method stub
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

  /** Runs LT options dialog box. **/
  public final void runOptionsDialog(int arg0) throws IllegalArgumentException {
    // TODO Auto-generated method stub
    Language lang = getLanguage();
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

  public void startDocument(int arg0) throws IllegalArgumentException {
    // TODO Auto-generated method stub

  }

  public void startParagraph(int arg0) throws IllegalArgumentException {
    // TODO Auto-generated method stub

  }

  public Locale[] getLocales() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean hasLocale(Locale arg0) {
    // TODO Auto-generated method stub
    return false;
  }

}
