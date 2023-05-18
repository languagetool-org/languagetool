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

import java.io.IOException;
import java.util.List;

import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.openoffice.OfficeTools.OfficeProductInfo;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XServiceDisplayName;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.linguistic2.XSpellAlternatives;
import com.sun.star.linguistic2.XSpellChecker;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.uno.XComponentContext;

/**
 * Class for spell checking by LanguageTool
 * @since 6.1
 * @author Fred Kruse
 */
public class LanguageToolSpellChecker extends WeakBase implements XServiceInfo, 
  XServiceDisplayName, XSpellChecker {

  // Service name required by the OOo API && our own name.
  private static final String[] SERVICE_NAMES = {
          "com.sun.star.linguistic2.SpellChecker",
          "org.languagetool.openoffice.LanguageToolSpellChecker" };
  
  private static JLanguageTool lt = null;
  private static Locale lastLocale = null;                //  locale for spell check
  private static SpellingCheckRule spellingCheckRule = null;
  private static XComponentContext xContext = null;
  private static boolean noLtSpeller = false;
  
  public LanguageToolSpellChecker(XComponentContext xContxt) {
    if (xContext == null) {
      xContext = xContxt;
      OfficeProductInfo officeInfo = OfficeTools.getOfficeProductInfo(xContext);
      if (officeInfo == null || officeInfo.osArch.equals("x86")) {
        noLtSpeller = true;
      }
    }
  }

  /**
   * Get XSingleComponentFactory
   * Default method called by LO/OO extensions
   */
  public static XSingleComponentFactory __getComponentFactory(String sImplName) {
    SingletonFactory xFactory = null;
    if (sImplName.equals(LanguageToolSpellChecker.class.getName())) {
      xFactory = new SingletonFactory(true);
    }
    return xFactory;
  }

  /**
   * Write keys to registry
   * Default method called by LO/OO extensions
   */
  public static boolean __writeRegistryServiceInfo(XRegistryKey regKey) {
//    MessageHandler.printToLogFile("XRegistryKey (LanguageToolSpellChecker): KeyName: " + regKey.getKeyName());
    return Factory.writeRegistryServiceInfo(LanguageToolSpellChecker.class.getName(), LanguageToolSpellChecker.getServiceNames(), regKey);
  }

  @Override
  public Locale[] getLocales() {
    return MultiDocumentsHandler.getLocales();
  }

  @Override
  public boolean hasLocale(Locale locale) {
    return MultiDocumentsHandler.hasLocale(locale);
  }

  @Override
  public String getImplementationName() {
    return LanguageToolSpellChecker.class.getName();
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

  @Override
  /**
   * is a correct spelled word
   */
  public boolean isValid(String word, Locale locale, PropertyValue[] Properties) throws IllegalArgumentException {
    try {
      if (noLtSpeller) {
        return false;
      }
      initSpellChecker(locale);
      if (spellingCheckRule != null) {
        if (!spellingCheckRule.isMisspelled(word)) {
          return true;
        }
        List<RuleMatch> matches = lt.check(word,true, ParagraphHandling.ONLYNONPARA);
//        MessageHandler.printToLogFile("LanguageToolSpellChecker: isValid: advanced check: word: " + word + ", matches: " + matches.size());
        if (matches == null || matches.size() == 0) {
          return true;
        }
//        if (word.endsWith(".") && !spellingCheckRule.isMisspelled(word.substring(0, word.length() - 1))) {
//          return true;
//        }
//        MessageHandler.printToLogFile("LanguageToolSpellChecker: isValid: misspelled word: " + word);
        return false;
      }
    } catch (Throwable e) {
      MessageHandler.showError(e);
    }
    return true;
  }

  /**
   * get alternatives for a non correct spelled word
   */
  @Override
  public XSpellAlternatives spell(String word, Locale locale, PropertyValue[] properties) throws IllegalArgumentException {
    LTSpellAlternatives alternatives = new LTSpellAlternatives(word, locale);
    return alternatives;
  }

  /**
   * Init spell checker for locale
   */
  private void initSpellChecker(Locale locale) {
    try {
      if (lastLocale == null || !OfficeTools.isEqualLocale(lastLocale, locale)) {
        if (hasLocale(locale)) {
          lastLocale = locale;
          Language lang = MultiDocumentsHandler.getLanguage(locale);
          lt = new JLanguageTool(lang);
          for (Rule rule : lt.getAllRules()) {
            if (rule.isDictionaryBasedSpellingRule()) {
              spellingCheckRule = (SpellingCheckRule) rule;
  //            break;
            } else {
              lt.disableRule(rule.getId());
            }
          }
        }
      }
    } catch (Throwable e) {
      MessageHandler.showError(e);
    }
  }
  
  /**
   * class for getting spelling alternatives
   */
  class LTSpellAlternatives implements XSpellAlternatives {
    
    Locale locale;
    String word;
    MorfologikSpellerRule mSpellRule = null;
    HunspellRule hSpellRule = null;
    
    LTSpellAlternatives(String word, Locale locale) {
      this.word = word;
      this.locale = locale;
      if (spellingCheckRule instanceof MorfologikSpellerRule) {
        mSpellRule = (MorfologikSpellerRule) spellingCheckRule;
      } else if (spellingCheckRule instanceof HunspellRule) {
        hSpellRule = (HunspellRule) spellingCheckRule;
      }
    }

    @Override
    public String[] getAlternatives() {
      if (noLtSpeller) {
        return new String[0];
      }
      try {
        if (mSpellRule != null) {
          return mSpellRule.getSpellingSuggestions(word).toArray(new String[0]);
        }
        if (hSpellRule != null) {
          return hSpellRule.getSuggestions(word).toArray(new String[0]);
        }
      } catch (Throwable t) {
        MessageHandler.showError(t);
      }
      return null;
    }

    @Override
    public short getAlternativesCount() {
      if (noLtSpeller) {
        return 0;
      }
      try {
        if (mSpellRule != null) {
          return (short) mSpellRule.getSpellingSuggestions(word).size();
        }
        if (hSpellRule != null) {
          return (short) hSpellRule.getSuggestions(word).size();
        }
      } catch (Throwable t) {
        MessageHandler.showError(t);
      }
      return 0;
    }

    @Override
    public short getFailureType() {
      // ??? unclear
      return 0;
    }

    @Override
    public Locale getLocale() {
      return locale;
    }

    @Override
    public String getWord() {
      return word;
    }
    
  }

  @Override
  public String getServiceDisplayName(Locale locale) {
    return MultiDocumentsHandler.getServiceDisplayName(locale);
  }
  
}
