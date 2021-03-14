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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.languagetool.Language;
import org.languagetool.LinguServices;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.linguistic2.XHyphenator;
import com.sun.star.linguistic2.XLinguServiceManager;
import com.sun.star.linguistic2.XMeaning;
import com.sun.star.linguistic2.XPossibleHyphens;
import com.sun.star.linguistic2.XSpellAlternatives;
import com.sun.star.linguistic2.XSpellChecker;
import com.sun.star.linguistic2.XThesaurus;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Class to handle information from linguistic services of LibreOffice/OpenOffice
 * @since 4.3
 * @author Fred Kruse
 */
public class LinguisticServices extends LinguServices {
  
  private XThesaurus thesaurus = null;
  private XSpellChecker spellChecker = null;
  private XHyphenator hyphenator = null;
  private Map<String, List<String>> synonymsCache;
  private boolean noSynonymsAsSuggestions = false;

  public LinguisticServices(XComponentContext xContext) {
    this(xContext, false);
  }
  
  public LinguisticServices(XComponentContext xContext, boolean noSynonymsAsSuggestions) {
    if (xContext != null) {
      XLinguServiceManager mxLinguSvcMgr = GetLinguSvcMgr(xContext);
      thesaurus = GetThesaurus(mxLinguSvcMgr);
      spellChecker = GetSpellChecker(mxLinguSvcMgr);
      hyphenator = GetHyphenator(mxLinguSvcMgr);
      synonymsCache = new HashMap<String, List<String>>();
      this.noSynonymsAsSuggestions = noSynonymsAsSuggestions;
    }
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
  
  public static Locale getLocale(Language lang) {
    Locale locale = new Locale();
    locale.Language = lang.getShortCode();
    locale.Country = lang.getCountries()[0];
    if(lang.getVariant() == null) {
      locale.Variant = "";
    } else {
      locale.Variant = lang.getVariant();
    }
    return locale;
  }
  
  /**
   * Get all synonyms of a word as list of strings.
   */
  @Override
  public List<String> getSynonyms(String word, Language lang) {
    return getSynonyms(word, getLocale(lang));
  }
  
  public List<String> getSynonyms(String word, Locale locale) {
    if (this.noSynonymsAsSuggestions) {
      return new ArrayList<String>();
    }
    if (synonymsCache.containsKey(word)) {
      return synonymsCache.get(word);
    }
    List<String> synonyms = new ArrayList<String>();
    try {
      if (thesaurus == null) {
        printText("XThesaurus == null");
        return synonyms;
      }
      if (locale == null) {
        printText("Locale == null");
        return synonyms;
      }
      PropertyValue[] properties = new PropertyValue[0];
      XMeaning[] meanings = thesaurus.queryMeanings(word, locale, properties);
      for (XMeaning meaning : meanings) {
        if(synonyms.size() >= OfficeTools.MAX_SUGGESTIONS) {
          break;
        }
        String[] singleSynonyms = meaning.querySynonyms();
        Collections.addAll(synonyms, singleSynonyms);
      }
      synonymsCache.put(word, synonyms);
      return synonyms;
    } catch (Throwable t) {
      // If anything goes wrong, give the user a stack trace
      printMessage(t);
      return synonyms;
    }
  }

  /**
   * Returns true if the spell check is positive
   */
  @Override
  public boolean isCorrectSpell(String word, Language lang) {
    return isCorrectSpell(word, getLocale(lang));
  }
  
  public boolean isCorrectSpell(String word, Locale locale) {
    if(spellChecker == null) {
      printText("XSpellChecker == null");
      return false;
    }
    PropertyValue[] properties = new PropertyValue[0];
    try {
      return spellChecker.isValid(word, locale, properties);
    } catch (Throwable t) {
      // If anything goes wrong, give the user a stack trace
      printMessage(t);
      return false;
    }
  }

  /**
   * Returns Alternatives to  wrong spelled word
   */
  public String[] getSpellAlternatives(String word, Language lang) {
    return getSpellAlternatives(word, getLocale(lang));
  }
  
  public String[] getSpellAlternatives(String word, Locale locale) {
    if(spellChecker == null) {
      printText("XSpellChecker == null");
      return null;
    }
    PropertyValue[] properties = new PropertyValue[0];
    try {
      XSpellAlternatives spellAlternatives = spellChecker.spell(word, locale, properties);
      return spellAlternatives.getAlternatives();
    } catch (Throwable t) {
      // If anything goes wrong, give the user a stack trace
      printMessage(t);
      return null;
    }
  }

  /**
   * Returns the number of syllable of a word
   * Returns -1 if the word was not found or anything goes wrong
   */
  @Override
  public int getNumberOfSyllables(String word, Language lang) {
    return getNumberOfSyllables(word, getLocale(lang));
  }
  
  public int getNumberOfSyllables(String word, Locale locale) {
    if(hyphenator == null) {
      printText("XHyphenator == null");
      return 1;
    }
    PropertyValue[] properties = new PropertyValue[0];
    try {
      XPossibleHyphens possibleHyphens = hyphenator.createPossibleHyphens(word, locale, properties);
      if (possibleHyphens == null) {
        return 1;
      }
      short[] numSyllable = possibleHyphens.getHyphenationPositions();
      return numSyllable.length + 1;
    } catch (Throwable t) {
      // If anything goes wrong, give the user a stack trace
      printMessage(t);
      return 1;
    }
  }

}
