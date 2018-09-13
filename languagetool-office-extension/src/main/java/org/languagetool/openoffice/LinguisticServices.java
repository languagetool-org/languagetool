/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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

import java.util.ArrayList;
import java.util.List;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.linguistic2.XHyphenator;
import com.sun.star.linguistic2.XLinguServiceManager;
import com.sun.star.linguistic2.XMeaning;
import com.sun.star.linguistic2.XSpellChecker;
import com.sun.star.linguistic2.XThesaurus;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Class to handle information from linguistic services of LibreOffice/OpenOffice
 * @since 4.3
 * @author Fred Kruse
 */
public class LinguisticServices {
  
  private XThesaurus thesaurus;
  private XSpellChecker spellChecker;
  private XHyphenator hyphenator;
  
  public LinguisticServices(XComponentContext xContext) {
    XLinguServiceManager mxLinguSvcMgr = GetLinguSvcMgr(xContext);
    thesaurus = GetThesaurus(mxLinguSvcMgr);
    spellChecker = GetSpellChecker(mxLinguSvcMgr);
    hyphenator = GetHyphenator(mxLinguSvcMgr);
  }
  
  /** 
   * Get the LinguServiceManager to be used for example 
   * to access spell checker, thesaurus and hyphenator
   */
  private XLinguServiceManager GetLinguSvcMgr(XComponentContext xContext) {
    try {
      XMultiComponentFactory xMCF = UnoRuntime.queryInterface(XMultiComponentFactory.class,
          xContext.getServiceManager());
      if (xMCF == null) {
        printText("XMultiComponentFactory == null");
        return null;
      }
      // retrieve Office's remote component context as a property
      XPropertySet props = UnoRuntime.queryInterface(XPropertySet.class, xMCF);
      if (props == null) {
        printText("XPropertySet == null");
        return null;
      }
      // initObject);
      Object defaultContext = props.getPropertyValue("DefaultContext");
      // get the remote interface XComponentContext
      XComponentContext xComponentContext = UnoRuntime.queryInterface(XComponentContext.class, defaultContext);
      if (xComponentContext == null) {
        printText("XComponentContext == null");
        return null;
      }
      Object o = xMCF.createInstanceWithContext("com.sun.star.linguistic2.LinguServiceManager", xComponentContext);     
      // create service component using the specified component context
      XLinguServiceManager mxLinguSvcMgr = UnoRuntime.queryInterface(XLinguServiceManager.class, o);
      if (mxLinguSvcMgr == null) {
        printText("XLinguServiceManager2 == null");
        return null;
      }
      return mxLinguSvcMgr;
    } catch (Throwable t) {
      // If anything goes wrong, give the user a stack trace
      printMessage(t);
    }
    return null;
  }
  
  /** 
   * Get the Thesaurus to be used.
   */
  private XThesaurus GetThesaurus(XLinguServiceManager mxLinguSvcMgr) {
    try {
      if (mxLinguSvcMgr != null) {
        return mxLinguSvcMgr.getThesaurus();
      }
    } catch (Throwable t) {
      // If anything goes wrong, give the user a stack trace
      printMessage(t);
    }
    return null;
  }

  /** 
   * Get the Hyphenator to be used.
   */
  private XHyphenator GetHyphenator(XLinguServiceManager mxLinguSvcMgr) {
    try {
      if (mxLinguSvcMgr != null) {
        return mxLinguSvcMgr.getHyphenator();
      }
    } catch (Throwable t) {
      // If anything goes wrong, give the user a stack trace
      printMessage(t);
    }
    return null;
  }

  /** 
   * Get the SpellChecker to be used.
   */
  private XSpellChecker GetSpellChecker(XLinguServiceManager mxLinguSvcMgr) {
    try {
      if (mxLinguSvcMgr != null) {
        return mxLinguSvcMgr.getSpellChecker();
      }
    } catch (Throwable t) {
      // If anything goes wrong, give the user a stack trace
      printMessage(t);
    }
    return null;
  }

  private static void printText(String txt) {
    MessageHandler.printToLogFile(txt);
  }
  
  private static void printMessage(Throwable t) {
    MessageHandler.printException(t);
  }
  
  public List<String> getSynonyms(String word, Locale lang) {
    try {
      if (thesaurus == null) {
        printText("XThesaurus == null");
        return null;
      }
      if (lang == null) {
        printText("Locale == null");
        return null;
      }
      PropertyValue[] properties = new PropertyValue[0];
      XMeaning meanings[] = thesaurus.queryMeanings(word, lang, properties);
      List<String> synonyms = new ArrayList<String>();
      for (XMeaning meaning : meanings) {
        String singleSynonyms[] = meaning.querySynonyms();
        for (String synonym : singleSynonyms) {
          synonyms.add(synonym);
        }
      }
      return synonyms;
    } catch (Throwable t) {
      // If anything goes wrong, give the user a stack trace
      printMessage(t);
      return null;
    }
  }


}
