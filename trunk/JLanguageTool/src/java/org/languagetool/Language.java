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
package org.languagetool;

import java.util.*;

import org.languagetool.language.*;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.Unifier;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.xx.DemoDisambiguator;
import org.languagetool.tagging.xx.DemoTagger;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tools.StringTools;

/**
 * Base class for any supported language (English, German, etc).
 * 
 * @author Daniel Naber
 */
public abstract class Language {

  // NOTE: keep in sync with array below!
  public static final Language BRETON = new Breton();
  //public final static Language CZECH = new Czech();
  public static final Language CHINESE = new Chinese();
  public static final Language DANISH = new Danish();
  public static final Language DUTCH = new Dutch();
  public static final Language ENGLISH = new English();
  public static final Language AMERICAN_ENGLISH = new AmericanEnglish();
  public static final Language AUSTRALIAN_ENGLISH = new AustralianEnglish();
  public static final Language BRITISH_ENGLISH = new BritishEnglish();
  public static final Language CANADIAN_ENGLISH = new CanadianEnglish();
  public static final Language NEW_ZEALAND_ENGLISH = new NewZealandEnglish();
  public static final Language SOUTH_AFRICAN_ENGLISH = new SouthAfricanEnglish();  
  public static final Language ESPERANTO = new Esperanto();
  public static final Language FRENCH = new French();
  public static final Language GERMAN = new German();
  public static final Language GERMANY_GERMAN = new GermanyGerman();
  public static final Language AUSTRIAN_GERMAN = new AustrianGerman();
  public static final Language SWISS_GERMAN = new SwissGerman();
  public static final Language ITALIAN = new Italian();
  public static final Language KHMER = new Khmer();
  public static final Language LITHUANIAN = new Lithuanian();
  public static final Language POLISH = new Polish();
  public static final Language SLOVAK = new Slovak();
  public static final Language SLOVENIAN = new Slovenian();
  public static final Language SPANISH = new Spanish();
  public static final Language SWEDISH = new Swedish();
  public static final Language UKRAINIAN = new Ukrainian();
  public static final Language RUSSIAN = new Russian();
  public static final Language ROMANIAN = new Romanian();
  public static final Language ICELANDIC = new Icelandic();
  public static final Language GALICIAN = new Galician();
  public static final Language CATALAN = new Catalan();
  public static final Language MALAYALAM = new Malayalam();
  public static final Language BELARUSIAN = new Belarusian();
  public static final Language ASTURIAN = new Asturian();
  public static final Language TAGALOG = new Tagalog();
  public static final Language GREEK = new Greek();
  public static final Language PORTUGUESE = new Portuguese();
  
  public static final Language DEMO = new Demo();
  
  private static List<Language> externalLanguages = new ArrayList<Language>();
  
  /**
   * All languages supported by LanguageTool.
   */
  public static Language[] LANGUAGES = {
    ENGLISH, GERMAN, POLISH, FRENCH, SPANISH, ITALIAN, KHMER, DUTCH, LITHUANIAN, UKRAINIAN, RUSSIAN,
    SLOVAK, SLOVENIAN, /*SWEDISH,*/ ROMANIAN, ICELANDIC, GALICIAN, CATALAN, DANISH,
    MALAYALAM, BELARUSIAN, ESPERANTO, CHINESE, ASTURIAN, TAGALOG, BRETON, GREEK,
    AMERICAN_ENGLISH, BRITISH_ENGLISH, CANADIAN_ENGLISH, SOUTH_AFRICAN_ENGLISH, NEW_ZEALAND_ENGLISH, AUSTRALIAN_ENGLISH,
    GERMANY_GERMAN, AUSTRIAN_GERMAN, SWISS_GERMAN, PORTUGUESE,
    DEMO
  };

  /**
   * All languages supported by LanguageTool, but without the demo language.
   */
  public static final Language[] REAL_LANGUAGES = new Language[LANGUAGES.length-1];
  static {
    int i = 0;
    for (final Language lang : LANGUAGES) {
      if (lang != DEMO) {
        REAL_LANGUAGES[i] = lang;
        i++;
      }
    }
  }

  private static final Language[] BUILTIN_LANGUAGES = LANGUAGES;

  private static final Disambiguator DEMO_DISAMBIGUATOR = new DemoDisambiguator();
  private static final Tagger DEMO_TAGGER = new DemoTagger();
  private static final SentenceTokenizer SENTENCE_TOKENIZER = new SentenceTokenizer();
  private static final WordTokenizer WORD_TOKENIZER = new WordTokenizer();
  private static final Unifier MATCH_UNIFIER = new Unifier();

  // -------------------------------------------------------------------------

  /**
   * Get this language's two character code, e.g. <code>en</code> for English.
   * @return String - language code
   */
  public abstract String getShortName();

  /**
   * Get this language's name in English, e.g. <code>English</code> or <code>German</code>.
   * @return String - language name
   */
  public abstract String getName();
  
  /**
   * Get this language's variants, e.g. <code>US</code> (as in <code>en_US</code>) or
   * <code>PL</code> (as in <code>pl_PL</code>).
   * @return String[] - array of country variants for the language.
   */
  public abstract String[] getCountryVariants();

  /**
   * Get this language's Java locale.
   */
  public abstract Locale getLocale();

  /**
   * Get the name(s) of the maintainer(s) for this language or <code>null</code>.
   */
  public abstract Contributor[] getMaintainers();

  /**
   * Get the rules classes that should run for texts in this language.
   * @since 1.4
   */
  public abstract List<Class<? extends Rule>> getRelevantRules();

  // -------------------------------------------------------------------------

  /**
   * Get the location of the rule file.
   */
  public List<String> getRuleFileName() {
      List<String> ruleFiles = new ArrayList<String>();
      ruleFiles.add(JLanguageTool.getDataBroker().getRulesDir() + 
              "/" + getShortName() + "/" + JLanguageTool.PATTERN_FILE);
      if (getShortNameWithVariant().length() > 2) {
          final String fileName = getShortName() + "/" +
                  getShortNameWithVariant() + 
                  "/" + JLanguageTool.PATTERN_FILE;
          if (JLanguageTool.getDataBroker().ruleFileExists(fileName)) {
              ruleFiles.add(JLanguageTool.getDataBroker().getRulesDir() + 
                      "/" + fileName);
          }
      }
      return ruleFiles;
  }

  /**
   * Languages that have variants need to overwrite this to select their most common variant.
   */
  public Language getDefaultVariant() {
    return null;
  }

  /**
   * Get this language's part-of-speech disambiguator implementation.
   */
  public Disambiguator getDisambiguator() {
    return DEMO_DISAMBIGUATOR;
  }

  /**
   * Get this language's part-of-speech tagger implementation.
   */
  public Tagger getTagger() {
    return DEMO_TAGGER;
  }

  /**
   * Get this language's sentence tokenizer implementation.
   */
  public SentenceTokenizer getSentenceTokenizer() {
    return SENTENCE_TOKENIZER;
  }

  /**
   * Get this language's word tokenizer implementation.
   */
  public Tokenizer getWordTokenizer() {
    return WORD_TOKENIZER;
  }

  /**
   * Get this language's part-of-speech synthesizer implementation or <code>null</code>.
   */
  public Synthesizer getSynthesizer() {
    return null;
  }

  /**
   * Get this language's feature unifier.
   * @return Feature unifier for analyzed tokens.
   */
  public Unifier getUnifier() {
    return MATCH_UNIFIER;
  }
  
  /**
   * Get this language's feature unifier used for disambiguation.
   * Note: it might be different from the normal rule unifier.
   * @return Feature unifier for analyzed tokens.
   */
  public Unifier getDisambiguationUnifier() {
    return MATCH_UNIFIER;
  }
  
  /**
   * Get the name of the language translated to the current locale,
   * if available. Otherwise, get the untranslated name.
   */
  public final String getTranslatedName(final ResourceBundle messages) {
	  try {
		  return messages.getString(getShortNameWithVariant());
	  } catch (final MissingResourceException e) {
		  try {
			  return messages.getString(getShortName());
		  } catch (final MissingResourceException e1) {
			  return getName();
		  }
	  }
  }
  
  /**
   * Get the short name of the language with a country variant, if it is
   * a single-variant language. For generic language classes, get only a two- or
   * three-character code.
   */
  public final String getShortNameWithVariant() {
	  String name = getShortName();
	  if (getCountryVariants().length == 1) {
		  name += "-" + getCountryVariants()[0];
	  }
	  return name;
  }
  
  
  /**
   * Start symbols used by {@link org.languagetool.rules.GenericUnpairedBracketsRule}.
   * Note that the array must be of equal length as {@link #getUnpairedRuleEndSymbols()} and the sequence of
   * starting symbols must match exactly the sequence of ending symbols.
   */
  public String[] getUnpairedRuleStartSymbols() {
    return new String[]{ "[", "(", "{", "\"", "'" };
  }

  /**
   * End symbols used by {@link org.languagetool.rules.GenericUnpairedBracketsRule}.
   * @see #getUnpairedRuleStartSymbols()
   */
  public String[] getUnpairedRuleEndSymbols() {
    return new String[]{ "]", ")", "}", "\"", "'" };
  }
  
  // -------------------------------------------------------------------------
  
  /**
   * Re-inits the built-in languages and adds the specified ones.
   */
  public static void reInit(final List<Language> languages) {
    LANGUAGES = new Language[BUILTIN_LANGUAGES.length + languages.size()];
    int i = BUILTIN_LANGUAGES.length;
    System.arraycopy(BUILTIN_LANGUAGES, 0,
        LANGUAGES, 0, BUILTIN_LANGUAGES.length);
    for (final Language lang : languages) {
      LANGUAGES[i++] = lang;
    }
    externalLanguages = languages;
  }

  /**
   * Return languages that are not built-in but have been added manually.
   */
  public static List<Language> getExternalLanguages() {
    return externalLanguages;
  }
  
  /**
   * Return all languages supported by LanguageTool.
   * @return A list of all languages, including external ones.
   */
  public static List<Language> getAllLanguages() {
	  final List<Language> langList = new ArrayList<Language>();
    Collections.addAll(langList, LANGUAGES);
	  langList.addAll(externalLanguages);
	  return langList;
  }
  
  /**
   * Get the Language object for the given short language name.
   * 
   * @param shortLanguageCode e.g. <code>en</code> or <code>de</code>
   * @return a Language object or <code>null</code>
   */
  public static Language getLanguageForShortName(final String shortLanguageCode) {
    StringTools.assureSet(shortLanguageCode, "shortLanguageCode");
    	
    if (shortLanguageCode.indexOf('-') != -1) {
    	final String[] parts = shortLanguageCode.split("-");
    	if (parts.length != 2) {
    		throw new IllegalArgumentException("'" + shortLanguageCode + "' isn't a valid language code"); 
    	}
    	for (Language element : Language.LANGUAGES) {
    		if (parts[0].equals(element.getShortName())
    				&& element.getCountryVariants().length == 1    				
    				&& parts[1].equals(element.getCountryVariants()[0])) {
    			return element;
    		}
    	}
    	throw new IllegalArgumentException("'" + shortLanguageCode + "' is not a language code known to LanguageTool");
    }	  
    if (shortLanguageCode.length() != "xx".length() && shortLanguageCode.length() != "xxx".length()) {
      throw new IllegalArgumentException("'" + shortLanguageCode + "' isn't a two- or three-character code");
    }
        
    for (Language element : Language.LANGUAGES) {
      if (shortLanguageCode.equals(element.getShortName())) {
        return element;
      }
    }
    return null;
  }

  /**
   * Get the best match for a locale, using American English as the final fallback if nothing
   * else fits. The returned language will be a variant language (e.g. British English, not just English)
   * if available.
   */
  public static Language getLanguageForLocale(final Locale locale) {
    Language language = getLanguageForLanguageNameAndCountry(locale);
    if (language != null) {
      return language;
    } else {
      language = getLanguageForLanguageNameOnly(locale);
      if (language != null) {
        return language;
      }
    }
    return Language.AMERICAN_ENGLISH;  // final fallback
  }

  private static Language getLanguageForLanguageNameAndCountry(Locale locale) {
    for (Language language : Language.REAL_LANGUAGES) {
      if (language.getShortName().equals(locale.getLanguage())) {
        final List<String> countryVariants = Arrays.asList(language.getCountryVariants());
        if (countryVariants.contains(locale.getCountry())) {
          return language;
        }
      }
    }
    return null;
  }

  private static Language getLanguageForLanguageNameOnly(Locale locale) {
    for (Language language : Language.REAL_LANGUAGES) {
      if (language.getShortName().equals(locale.getLanguage()) && !language.hasVariant()) {
        return language;
      }
    }
    return null;
  }

  /**
   * Get the Language object for the given language name.
   * 
   * @param languageName e.g. <code>English</code> or <code>German</code> (case is significant)
   * @return a Language object or <code>null</code>
   */
  public static Language getLanguageForName(final String languageName) {
    for (Language element : Language.LANGUAGES) {
      if (languageName.equals(element.getName())) {
        return element;
      }
    }
    return null;
  }  
  
  @Override
  public final String toString() {
    return getName();
  }
  
  /**
   * Get sorted info about all maintainers (without language variants) to be used in the About dialog.
   * @since 0.9.9
   * @param messages
   *        {{@link ResourceBundle} language bundle to translate
   *        the info
   * @return
   *        A sorted list of maintainers.
   */
  public static String getAllMaintainers(final ResourceBundle messages) {
    final StringBuilder maintainersInfo = new StringBuilder();
    final List<String> toSort = new ArrayList<String>();
    for (final Language lang : Language.LANGUAGES) {
      if (lang != Language.DEMO && !lang.isVariant()) {
        if (lang.getMaintainers() != null) {
          final List<String> names = new ArrayList<String>();
          for (Contributor contributor : lang.getMaintainers()) {
            names.add(contributor.getName());
          }
          toSort.add(messages.getString(lang.getShortName()) +
              ": " + listToStringWithLineBreaks(names));
        }
      }            
    }    
    Collections.sort(toSort);
    for (final String lElem : toSort) {
      maintainersInfo.append(lElem);
      maintainersInfo.append('\n');
    }
    return maintainersInfo.toString();
  }

  /**
   * Whether this is a variant of another language, i.e. whether it doesn't
   * directly extend {@link Language}, but a subclass of {@link Language}.
   */
  public final boolean isVariant() {
    for (Language language : LANGUAGES) {
      final boolean skip = language.getShortNameWithVariant().equals(getShortNameWithVariant());
      if (!skip && language.getClass().isAssignableFrom(getClass())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Whether this class has at least one subclass that implements variants of this language.
   */
  public final boolean hasVariant() {
    for (Language language : LANGUAGES) {
      final boolean skip = language.getShortNameWithVariant().equals(getShortNameWithVariant());
      if (!skip && getClass().isAssignableFrom(language.getClass())) {
        return true;
      }
    }
    return false;
  }

  public boolean isExternal() {
    return false;
  }

  private static String listToStringWithLineBreaks(final Collection<String> l) {
    final StringBuilder sb = new StringBuilder();
    int i = 0;
    for (final Iterator<String> iter = l.iterator(); iter.hasNext();) {
      final String str = iter.next();
      sb.append(str);
      if (iter.hasNext()) {
        if (i > 0 && i % 3 == 0) {
          sb.append(",\n    ");
        } else {
          sb.append(", ");
        }
      }
      i++;
    }
    return sb.toString();
  }

}
