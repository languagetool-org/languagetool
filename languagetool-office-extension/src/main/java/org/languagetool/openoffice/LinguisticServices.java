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
  
  private static boolean isSetLt = false;
  private XThesaurus thesaurus = null;
  private XSpellChecker spellChecker = null;
  private XHyphenator hyphenator = null;
  private Map<String, List<String>> synonymsCache;
  private boolean noSynonymsAsSuggestions = false;

  public LinguisticServices(XComponentContext xContext) {
    if (xContext != null) {
      XLinguServiceManager mxLinguSvcMgr = getLinguSvcMgr(xContext);
      thesaurus = getThesaurus(mxLinguSvcMgr);
      spellChecker = getSpellChecker(mxLinguSvcMgr);
      hyphenator = getHyphenator(mxLinguSvcMgr);
      synonymsCache = new HashMap<>();
    }
  }

  /**
   * Set Parameter to generate no synonyms (makes some rules faster, but results in no suggestions)
   */
  public void setNoSynonymsAsSuggestions (boolean noSynonymsAsSuggestions) {
    this.noSynonymsAsSuggestions = noSynonymsAsSuggestions;
  }
  
  /**
   * returns if spell checker can be used
   * if false initialize LinguisticServices again
   */
  public boolean spellCheckerIsActive () {
    return (spellChecker != null);
  }
  
  /** 
   * Get the LinguServiceManager to be used for example 
   * to access spell checker, thesaurus and hyphenator
   */
  private XLinguServiceManager getLinguSvcMgr(XComponentContext xContext) {
    try {
      XMultiComponentFactory xMCF = UnoRuntime.queryInterface(XMultiComponentFactory.class,
          xContext.getServiceManager());
      if (xMCF == null) {
        MessageHandler.printToLogFile("LinguisticServices: getLinguSvcMgr: XMultiComponentFactory == null");
        return null;
      }
      // retrieve Office's remote component context as a property
      XPropertySet props = UnoRuntime.queryInterface(XPropertySet.class, xMCF);
      if (props == null) {
        MessageHandler.printToLogFile("LinguisticServices: getLinguSvcMgr: XPropertySet == null");
        return null;
      }
      Object defaultContext = props.getPropertyValue("DefaultContext");
      // get the remote interface XComponentContext
      XComponentContext xComponentContext = UnoRuntime.queryInterface(XComponentContext.class, defaultContext);
      if (xComponentContext == null) {
        MessageHandler.printToLogFile("LinguisticServices: getLinguSvcMgr: XComponentContext == null");
        return null;
      }
      Object o = xMCF.createInstanceWithContext("com.sun.star.linguistic2.LinguServiceManager", xComponentContext);     
      // create service component using the specified component context
      XLinguServiceManager mxLinguSvcMgr = UnoRuntime.queryInterface(XLinguServiceManager.class, o);
      if (mxLinguSvcMgr == null) {
        MessageHandler.printToLogFile("LinguisticServices: getLinguSvcMgr: XLinguServiceManager2 == null");
        return null;
      }
      return mxLinguSvcMgr;
    } catch (Throwable t) {
      // If anything goes wrong, give the user a stack trace
      MessageHandler.printException(t);
    }
    return null;
  }
  
  /** 
   * Get the Thesaurus to be used.
   */
  private XThesaurus getThesaurus(XLinguServiceManager mxLinguSvcMgr) {
    try {
      if (mxLinguSvcMgr != null) {
        return mxLinguSvcMgr.getThesaurus();
      }
    } catch (Throwable t) {
      // If anything goes wrong, give the user a stack trace
      MessageHandler.printException(t);
    }
    return null;
  }

  /** 
   * Get the Hyphenator to be used.
   */
  private XHyphenator getHyphenator(XLinguServiceManager mxLinguSvcMgr) {
    try {
      if (mxLinguSvcMgr != null) {
        return mxLinguSvcMgr.getHyphenator();
      }
    } catch (Throwable t) {
      // If anything goes wrong, give the user a stack trace
      MessageHandler.printException(t);
    }
    return null;
  }

  /** 
   * Get the SpellChecker to be used.
   */
  private XSpellChecker getSpellChecker(XLinguServiceManager mxLinguSvcMgr) {
    try {
      if (mxLinguSvcMgr != null) {
        return mxLinguSvcMgr.getSpellChecker();
      }
    } catch (Throwable t) {
      // If anything goes wrong, give the user a stack trace
      MessageHandler.printException(t);
    }
    return null;
  }

  /**
   * Get a Locale from a LT defined language
   */
  public static Locale getLocale(Language lang) {
    Locale locale = new Locale();
    locale.Language = lang.getShortCode();
    if ((lang.getCountries() == null || lang.getCountries().length != 1) && lang.getDefaultLanguageVariant() != null) {
      locale.Country = lang.getDefaultLanguageVariant().getCountries()[0];
    } else if (lang.getCountries() != null && lang.getCountries().length > 0) {
      locale.Country = lang.getCountries()[0];
    } else {
      locale.Country = "";
    }
    if (lang.getVariant() == null) {
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
    if (noSynonymsAsSuggestions) {
      return new ArrayList<>();
    }
    if (synonymsCache.containsKey(word)) {
      return synonymsCache.get(word);
    }
    List<String> synonyms = new ArrayList<>();
    try {
      if (thesaurus == null) {
        MessageHandler.printToLogFile("LinguisticServices: getSynonyms: XThesaurus == null");
        return synonyms;
      }
      if (locale == null) {
        MessageHandler.printToLogFile("LinguisticServices: getSynonyms: Locale == null");
        return synonyms;
      }
      PropertyValue[] properties = new PropertyValue[0];
      XMeaning[] meanings = thesaurus.queryMeanings(word, locale, properties);
      for (XMeaning meaning : meanings) {
        if (synonyms.size() >= OfficeTools.MAX_SUGGESTIONS) {
          break;
        }
        String[] singleSynonyms = meaning.querySynonyms();
        Collections.addAll(synonyms, singleSynonyms);
      }
      synonymsCache.put(word, synonyms);
      return synonyms;
    } catch (Throwable t) {
      // If anything goes wrong, give the user a stack trace
      MessageHandler.printException(t);
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
    if (spellChecker == null) {
      MessageHandler.printToLogFile("LinguisticServices: isCorrectSpell: XSpellChecker == null");
      return false;
    }
    PropertyValue[] properties = new PropertyValue[0];
    try {
      return spellChecker.isValid(word, locale, properties);
    } catch (Throwable t) {
      // If anything goes wrong, give the user a stack trace
      MessageHandler.printException(t);
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
    if (spellChecker == null) {
      MessageHandler.printToLogFile("LinguisticServices: getSpellAlternatives: XSpellChecker == null");
      return null;
    }
    PropertyValue[] properties = new PropertyValue[0];
    try {
      XSpellAlternatives spellAlternatives = spellChecker.spell(word, locale, properties);
      if (spellAlternatives == null) {
        return null;
      }
      return spellAlternatives.getAlternatives();
    } catch (Throwable t) {
      // If anything goes wrong, give the user a stack trace
      MessageHandler.printException(t);
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
    if (hyphenator == null) {
      MessageHandler.printToLogFile("LinguisticServices: getNumberOfSyllables: XHyphenator == null");
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
      MessageHandler.printException(t);
      return 1;
    }
  }
  
  /**
   * Set LT as grammar checker for a specific language
   * is normally used deactivate lightproof 
   */
  public boolean setLtAsGrammarService(XComponentContext xContext, Locale locale) {
    if (xContext != null) {
      XLinguServiceManager mxLinguSvcMgr = getLinguSvcMgr(xContext); 
      if (mxLinguSvcMgr == null) {
        MessageHandler.printToLogFile("LinguisticServices: setLtAsGrammarService: XLinguServiceManager == null");
        return false;
      }
      Locale[] locales = MultiDocumentsHandler.getLocales();
      for (Locale loc : locales) {
        if (OfficeTools.isEqualLocale(locale, loc)) {
          String[] serviceNames = mxLinguSvcMgr.getConfiguredServices("com.sun.star.linguistic2.Proofreader", locale);
          if (serviceNames.length == 0) {
            MessageHandler.printToLogFile("LinguisticServices: setLtAsGrammarService: No configured Service for: " + OfficeTools.localeToString(locale));
          } else {
            for (String service : serviceNames) {
              MessageHandler.printToLogFile("Configured Linguistic Service: " + service + ", " + OfficeTools.localeToString(locale));
            }
          }
          if (serviceNames.length != 1 || !serviceNames[0].equals(OfficeTools.LT_SERVICE_NAME)) {
            String[] aServiceNames = mxLinguSvcMgr.getAvailableServices("com.sun.star.linguistic2.Proofreader", locale);
            for (String service : aServiceNames) {
              MessageHandler.printToLogFile("Available Linguistic Service: " + service + ", " + OfficeTools.localeToString(locale));
            }
            String[] configuredServices = new String[1];
            configuredServices[0] = new String(OfficeTools.LT_SERVICE_NAME);
            mxLinguSvcMgr.setConfiguredServices("com.sun.star.linguistic2.Proofreader", locale, configuredServices);
            MessageHandler.printToLogFile("LT set as configured Service for Language: " + OfficeTools.localeToString(locale));
          }
          return true;
        }
      }
      MessageHandler.printToLogFile("LT doesn't support language: " + OfficeTools.localeToString(locale));
    }
    return false;
  }

  /**
   * Set LT as grammar checker for all supported languages
   * is normally used deactivate lightproof 
   */
  public boolean setLtAsGrammarService(XComponentContext xContext) {
    if (xContext != null) {
      return setLtAsGrammarService(getLinguSvcMgr(xContext));
    } else {
      return false;
    }
  }

  /**
   * Set LT as grammar checker for all supported languages
   * is normally used deactivate lightproof 
   */
  private boolean setLtAsGrammarService(XLinguServiceManager mxLinguSvcMgr) {
    if (isSetLt) {
      return true;
    }
    if (mxLinguSvcMgr == null) {
      MessageHandler.printToLogFile("LinguisticServices: setLtAsGrammarService: XLinguServiceManager == null");
      return false;
    }
    isSetLt = true;
    Locale[] locales = MultiDocumentsHandler.getLocales();
    for (Locale locale : locales) {
      String[] serviceNames = mxLinguSvcMgr.getConfiguredServices("com.sun.star.linguistic2.Proofreader", locale);
      if (serviceNames.length != 1 || !serviceNames[0].equals(OfficeTools.LT_SERVICE_NAME)) {
        String[] aServiceNames = mxLinguSvcMgr.getAvailableServices("com.sun.star.linguistic2.Proofreader", locale);
        for (String service : aServiceNames) {
          MessageHandler.printToLogFile("Available Linguistic Service: " + service + ", " + OfficeTools.localeToString(locale));
        }
        String[] configuredServices = new String[1];
        configuredServices[0] = new String(OfficeTools.LT_SERVICE_NAME);
        mxLinguSvcMgr.setConfiguredServices("com.sun.star.linguistic2.Proofreader", locale, configuredServices);
        MessageHandler.printToLogFile("LT set as configured Service for Language: " + OfficeTools.localeToString(locale));
      }
    }
    return true;
  }

}
