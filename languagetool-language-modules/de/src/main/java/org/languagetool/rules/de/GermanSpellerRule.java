/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import de.danielnaber.jwordsplitter.AbstractWordSplitter;
import de.danielnaber.jwordsplitter.GermanWordSplitter;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.spelling.hunspell.CompoundAwareHunspellRule;
import org.languagetool.rules.spelling.morfologik.MorfologikSpeller;
import org.languagetool.tokenizers.CompoundWordTokenizer;
import org.languagetool.tokenizers.de.GermanCompoundTokenizer;

import java.io.IOException;
import java.util.*;

public class GermanSpellerRule extends CompoundAwareHunspellRule {

  public static final String RULE_ID = "GERMAN_SPELLER_RULE";
  
  private static final int MAX_EDIT_DISTANCE = 2;
  private static final int SUGGESTION_MIN_LENGTH = 2;
  private static final List<Replacement> REPL = Arrays.asList(
      // see de_DE.aff:
      new Replacement("f", "ph"),
      new Replacement("ph", "f"),
      new Replacement("ß", "ss"),
      new Replacement("ss", "ß"),
      new Replacement("s", "ss"),
      new Replacement("ss", "s"),
      new Replacement("i", "ie"),
      new Replacement("ie", "i"),
      new Replacement("ee", "e"),
      new Replacement("o", "oh"),
      new Replacement("oh", "o"),
      new Replacement("a", "ah"),
      new Replacement("ah", "a"),
      new Replacement("e", "eh"),
      new Replacement("eh", "e"),
      new Replacement("ae", "ä"),
      new Replacement("oe", "ö"),
      new Replacement("ue", "ü"),
      new Replacement("Ae", "Ä"),
      new Replacement("Oe", "Ö"),
      new Replacement("Ue", "Ü"),
      new Replacement("d", "t"),
      new Replacement("t", "d"),
      new Replacement("th", "t"),
      new Replacement("t", "th"),
      new Replacement("r", "rh"),
      new Replacement("ch", "k"),
      new Replacement("k", "ch"),
      // not in de_DE.aff (not clear what uppercase replacement we need...):
      new Replacement("F", "Ph"),
      new Replacement("Ph", "F")
  );

  private static AbstractWordSplitter wordSplitter;
  private final GermanCompoundTokenizer compoundTokenizer;

  public GermanSpellerRule(ResourceBundle messages, Language language) throws IOException {
    super(messages, language, getCompoundSplitter(), getSpeller(language));
    addExamplePair(Example.wrong("LanguageTool kann mehr als eine <marker>nromale</marker> Rechtschreibprüfung."),
                   Example.fixed("LanguageTool kann mehr als eine <marker>normale</marker> Rechtschreibprüfung."));
    compoundTokenizer = new GermanCompoundTokenizer();
  }

  @Override
  public String getId() {
    return RULE_ID;
  }
  
  private static CompoundWordTokenizer getCompoundSplitter() {
    if(wordSplitter == null) {
         try {
            wordSplitter = new GermanWordSplitter(false);
        } catch (IOException e) {
            throw new RuntimeException("Could not set up German compound splitter", e);
        }
    }
    wordSplitter.setStrictMode(false); // there's a spelling mistake in (at least) one part, so strict mode wouldn't split the word
    ((GermanWordSplitter)wordSplitter).setMinimumWordLength(3);
    return new CompoundWordTokenizer() {
      @Override
      public List<String> tokenize(String word) {
        return new ArrayList<>(wordSplitter.splitWord(word));
      }
    };
  }

  private static MorfologikSpeller getSpeller(Language language) {
    if (!language.getShortName().equals(Locale.GERMAN.getLanguage())) {
      throw new RuntimeException("Language is not a variant of German: " + language);
    }
    try {
      final String morfoFile = "/de/hunspell/de_" + language.getCountries()[0] + ".dict";
      if (JLanguageTool.getDataBroker().resourceExists(morfoFile)) {
        // spell data will not exist in LibreOffice/OpenOffice context 
        return new MorfologikSpeller(morfoFile, MAX_EDIT_DISTANCE);
      } else {
        return null;
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not set up morfologik spell checker", e);
    }
  }

  // Use hunspell-style replacements to get good suggestions for "heisse", namely "heiße" etc
  // TODO: remove this when the Morfologik speller can do this directly during tree iteration:
  @Override
  protected List<String> sortSuggestionByQuality(String misspelling, List<String> suggestions) {
    List<String> sorted1 = sortByReplacements(misspelling, suggestions);
    List<String> sorted2 = sortByCase(misspelling, sorted1);
    return sorted2;
  }

  @Override
  protected boolean ignoreWord(List<String> words, int idx) throws IOException {
    boolean ignore = super.ignoreWord(words, idx);
    boolean ignoreByHyphen = !ignore && words.get(idx).endsWith("-") && ignoreByHangingHyphen(words, idx);
    return ignore || ignoreByHyphen;
  }

  private boolean ignoreByHangingHyphen(List<String> words, int idx) {
    String word = words.get(idx);
    String nextWord = getWordAfterEnumerationOrNull(words, idx);
    boolean isCompound = nextWord != null && compoundTokenizer.tokenize(nextWord).size() > 1;
    if (isCompound) {
      return !hunspellDict.misspelled(word.replaceFirst("-$", ""));  // "Stil- und Grammatikprüfung" or "Stil-, Text- und Grammatikprüfung"
    }
    return false;
  }

  // for "Stil- und Grammatikprüfung", get "Grammatikprüfung" when at position of "Stil-"
  private String getWordAfterEnumerationOrNull(List<String> words, int idx) {
    for (int i = idx; i < words.size(); i++) {
      String word = words.get(i);
      boolean inEnumeration = ",".equals(word) || "und".equals(word) || "oder".equals(word) || word.trim().isEmpty() || word.endsWith("-");
      if (!inEnumeration) {
        return word;
      }
    }
    return null;
  }

  private List<String> sortByReplacements(String misspelling, List<String> suggestions) {
    final List<String> result = new ArrayList<>();
    for (String suggestion : suggestions) {
      boolean moveSuggestionToTop = false;
      for (Replacement replacement : REPL) {
        final String modifiedMisspelling = misspelling.replace(replacement.key, replacement.value);
        final boolean equalsAfterReplacement = modifiedMisspelling.equals(suggestion);
        if (equalsAfterReplacement) {
          moveSuggestionToTop = true;
          break;
        }
      }
      if (!ignoreSuggestion(suggestion)) {
        if (moveSuggestionToTop) {
          // this should be preferred, as the replacements make it equal to the suggestion:
          result.add(0, suggestion);
        } else {
          result.add(suggestion);
        }
      }
    }
    return result;
  }

  private List<String> sortByCase(String misspelling, List<String> suggestions) {
    final List<String> result = new ArrayList<>();
    for (String suggestion : suggestions) {
      if (misspelling.equalsIgnoreCase(suggestion)) {
        // this should be preferred - only case differs:
        result.add(0, suggestion);
      } else {
        result.add(suggestion);
      }
    }
    return result;
  }

  private boolean ignoreSuggestion(String suggestion) {
    String[] parts = suggestion.split(" ");
    if (parts.length > 1) {
      for (String part : parts) {
        if (part.length() < SUGGESTION_MIN_LENGTH) {
          return true;
        }
      }
    }
    return false;
  }

  private static class Replacement {
    String key;
    String value;
    private Replacement(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }
}
