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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.languagetool.JLanguageTool;
import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.openoffice.CacheIO.SpellCache;
import org.languagetool.openoffice.OfficeTools.OfficeProductInfo;
import org.languagetool.Language;
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
public class LtSpellChecker extends WeakBase implements XServiceInfo, 
  XServiceDisplayName, XSpellChecker {

  private static final int MAX_WRONG = 10000;
  
  // Service name required by the OOo API && our own name.
  private static final String[] SERVICE_NAMES = {
          "com.sun.star.linguistic2.SpellChecker",
          "org.languagetool.openoffice.LanguageToolSpellChecker" };
  
  private static JLanguageTool lt = null;
  private static Locale lastLocale = null;                //  locale for spell check
  private static SpellingCheckRule spellingCheckRule = null;
  private static MorfologikSpellerRule mSpellRule = null;
  private static HunspellRule hSpellRule = null;
  private static final Map<String, List<String>> lastWrongWords = new HashMap<>();
  private static final Map<String, List<String[]>> lastSuggestions = new HashMap<>();
  private static XComponentContext xContext = null;
  private static boolean noLtSpeller = false;
  
  public LtSpellChecker(XComponentContext xContxt) {
    if (xContext == null) {
      xContext = xContxt;
      OfficeProductInfo officeInfo = OfficeTools.getOfficeProductInfo(xContext);
      if (officeInfo == null || officeInfo.osArch.equals("x86")) {
        noLtSpeller = true;
      } else {
        CacheIO c = new CacheIO(); 
        SpellCache sc = c.new SpellCache();
        if (sc.read()) {
          lastWrongWords.putAll(sc.getWrongWords());
          lastSuggestions.putAll(sc.getSuggestions());
        }
      }
    }
  }

  /**
   * Get XSingleComponentFactory
   * Default method called by LO/OO extensions
   */
  public static XSingleComponentFactory __getComponentFactory(String sImplName) {
    SingletonFactory xFactory = null;
    if (sImplName.equals(LtSpellChecker.class.getName())) {
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
    return Factory.writeRegistryServiceInfo(LtSpellChecker.class.getName(), LtSpellChecker.getServiceNames(), regKey);
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
    return LtSpellChecker.class.getName();
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
      if (noLtSpeller || !hasLocale(locale)) {
        return false;
      }
      String localeStr = OfficeTools.localeToString(locale);
      List<String> wrongWords = lastWrongWords.get(localeStr);
      if (wrongWords != null && wrongWords.contains(word)) {
        return false;
      }
//      MessageHandler.printToLogFile("LanguageToolSpellChecker: isValid: check word: " + word);
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
        if (wrongWords == null) {
          lastWrongWords.put(localeStr, new ArrayList<String>());
          lastSuggestions.put(localeStr, new ArrayList<String[]>());
        }
        if (!lastWrongWords.get(localeStr).contains(word)) {
          lastWrongWords.get(localeStr).add(new String(word));
          lastSuggestions.get(localeStr).add(suggestionsToArray(matches.get(0).getSuggestedReplacements()));
          if (lastWrongWords.get(localeStr).size() >= MAX_WRONG) {
            lastWrongWords.get(localeStr).remove(0);
            lastSuggestions.get(localeStr).remove(0);
          }
        }
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
//        MessageHandler.printToLogFile("LanguageToolSpellChecker: initSpellChecker: lastLocale: "
//            + (lastLocale == null ? "null" : OfficeTools.localeToString(lastLocale)) 
//            + ", locale: " + (locale == null ? "null" : OfficeTools.localeToString(locale))
//            + ", word: " + (word == null ? "null" : word)
//            );
        lastLocale = locale;
        Language lang = MultiDocumentsHandler.getLanguage(locale);
        lt = new JLanguageTool(lang);
        for (Rule rule : lt.getAllRules()) {
          if (rule.isDictionaryBasedSpellingRule()) {
            spellingCheckRule = (SpellingCheckRule) rule;
            if (spellingCheckRule instanceof MorfologikSpellerRule) {
              mSpellRule = (MorfologikSpellerRule) spellingCheckRule;
              hSpellRule = null;
            } else if (spellingCheckRule instanceof HunspellRule) {
              hSpellRule = (HunspellRule) spellingCheckRule;
              mSpellRule = null;
            }
          } else {
            lt.disableRule(rule.getId());
          }
        }
      }
    } catch (Throwable e) {
      MessageHandler.showError(e);
    }
  }
  
  /**
   * Convert list of suggestions to array and reduce it size
   */
  private String[] suggestionsToArray(List<String> suggestions) {
    int numSuggestions = suggestions.size();
    String[] allSuggestions = suggestions.toArray(new String[numSuggestions]);
    if (allSuggestions.length > OfficeTools.MAX_SUGGESTIONS) {
      allSuggestions = Arrays.copyOfRange(allSuggestions, 0, OfficeTools.MAX_SUGGESTIONS);
    }
    return allSuggestions;
  }
  
  /**
   * class for getting spelling alternatives
   */
  class LTSpellAlternatives implements XSpellAlternatives {
    
    Locale locale;
    String word;
    String[] alternatives;
    
    LTSpellAlternatives(String word, Locale locale) {
      this.word = word;
      this.locale = locale;
      if (noLtSpeller) {
        alternatives = new String[0];
        return;
      }
      String localeStr = OfficeTools.localeToString(locale);
      if (lastWrongWords.get(localeStr).contains(word)) {
        int num = lastWrongWords.get(localeStr).indexOf(word);
        alternatives = lastSuggestions.get(localeStr).get(num);
        return;
      }
      try {
        if (mSpellRule != null) {
          alternatives = mSpellRule.getSpellingSuggestions(word).toArray(new String[0]);
        }
        if (hSpellRule != null) {
          alternatives = hSpellRule.getSuggestions(word).toArray(new String[0]);
        }
      } catch (Throwable t) {
        MessageHandler.showError(t);
        alternatives = new String[0];
      }
    }

    @Override
    public String[] getAlternatives() {
      return alternatives;
    }

    @Override
    public short getAlternativesCount() {
      return (short) alternatives.length;
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
  
  public static Map<String, List<String>> getWrongWords() {
    return lastWrongWords;
  }
   
  public static Map<String, List<String[]>> getSuggestions() {
    return lastSuggestions;
  }
  
  public static void resetSpellCache() {
    lastWrongWords.clear();
    lastSuggestions.clear();
  }
   
}
