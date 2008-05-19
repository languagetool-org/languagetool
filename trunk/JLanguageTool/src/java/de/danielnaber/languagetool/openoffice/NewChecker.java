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
import java.util.Set;
import java.io.IOException;

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

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.Configuration;
import de.danielnaber.languagetool.rules.RuleMatch;

public class NewChecker implements XGrammarChecker {

  private Configuration config;
  private JLanguageTool langTool; 
  private Language docLanguage;
  
  private XTextDocument xTextDoc;
  
  /** Document ID, used by the interface.
   * is this used to mark which document is checked?
   * should there be an array indexed by doc Ids for
   * maintaining some doc info?
   */
  private int docID = -1;

  //TODO: remove this method and replace with proper one
  // based on XFlatParagraph
  private Language getLanguage() {
    if (xTextDoc == null)
      return Language.ENGLISH; // for testing with local main() method only
    Locale charLocale;
    try {
      // just look at the first position in the document and assume that this character's
      // language is the language of the whole document:
      XTextCursor textCursor = xTextDoc.getText().createTextCursor();
      textCursor.gotoStart(false);
      XPropertySet xCursorProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class,
          textCursor);
      charLocale = (Locale) xCursorProps.getPropertyValue("CharLocale");      
      if (!hasLocale(charLocale)) {
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

  /** Runs LT on text.
   * @param arg0 int - document ID
   * arg1 XFlatParagraph - text to check
   * arg2 Locale - the text Locale  
   * arg3 int start of sentence position
   * arg4 int end of sentence position
   */
  public final void doGrammarChecking(int arg0,  
      XFlatParagraph arg1, Locale arg2, 
      int arg3, int arg4 
      ) throws IllegalArgumentException {
    if (hasLocale(arg2) && 
        (!arg1.isChecked(com.sun.star.text.TextMarkupType.GRAMMAR))) {
      docLanguage = Language.DEMO;
      for (int i = 0; i < Language.LANGUAGES.length; i++) {
        if (Language.LANGUAGES[i].getShortName().equals(arg2.Language)) {
          docLanguage = Language.LANGUAGES[i];
          break;
        }
      try {
      langTool = new JLanguageTool(docLanguage, config.getMotherTongue());
      langTool.activateDefaultPatternRules();
      langTool.activateDefaultFalseFriendRules();
      } catch (Exception exception) {
        //FIXME: write some code?
        };
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
        List<RuleMatch> ruleMatches = langTool.check(arg1.getText());
        if (ruleMatches.size() > 0) {
          //TODO: The error should be formatted as 
          //com.sun.star.linguistic2.SingleGrammarError
          //note that "errortype" there is left undefined...
          //GrammarCheckingResult is a structure that contains all errors
          //so we need to convert ruleMatches to GrammarCheckingResult
          //how to call XGrammarCheckingIterator implemented by the service css.linguistic2.GrammarCheckingIterator
        } else {
          //mark the text node as checked
          arg1.setChecked(com.sun.star.text.TextMarkupType.GRAMMAR, 
              true);  
        }
      } catch (IOException exception) {
        //FIXME: do something with the exception
        };      
      }
    }

  public void endDocument(int arg0) throws IllegalArgumentException {
    if (docID == arg0) {
      docID = -1;
    }
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
    docID = arg0;
  }

  public void startParagraph(int arg0) throws IllegalArgumentException {
    // TODO Auto-generated method stub

  }

  /**
   * @return An array of Locales supported by LT.
   */
  public final Locale[] getLocales() {
    Locale[] aLocales = new Locale[Language.LANGUAGES.length];
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
    boolean langIsSupported = false;
    for (int i = 0; i < Language.LANGUAGES.length; i++) {
      if (Language.LANGUAGES[i].getShortName().equals(arg0.Language)) {
        langIsSupported = true;
        break;
      }
    }
    return langIsSupported;
  }

}
