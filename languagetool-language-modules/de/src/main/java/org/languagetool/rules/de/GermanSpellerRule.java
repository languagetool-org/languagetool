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

import de.abelssoft.wordtools.jwordsplitter.AbstractWordSplitter;
import de.abelssoft.wordtools.jwordsplitter.impl.GermanWordSplitter;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.spelling.hunspell.CompoundAwareHunspellRule;
import org.languagetool.rules.spelling.morfologik.MorfologikSpeller;
import org.languagetool.tokenizers.CompoundWordTokenizer;

import java.io.IOException;
import java.util.*;

public class GermanSpellerRule extends CompoundAwareHunspellRule {

  public static final String RULE_ID = "GERMAN_SPELLER_RULE";
  
  private static final int MAX_EDIT_DISTANCE = 2;
  private static final int SUGGESTION_MIN_LENGTH = 2;
  private static final List<Replacement> REPL = new ArrayList<>();
  static {
    // see de_DE.aff:
    REPL.add(new Replacement("f", "ph"));
    REPL.add(new Replacement("ph", "f"));
    REPL.add(new Replacement("ß", "ss"));
    REPL.add(new Replacement("ss", "ß"));
    REPL.add(new Replacement("s", "ss"));
    REPL.add(new Replacement("ss", "s"));
    REPL.add(new Replacement("i", "ie"));
    REPL.add(new Replacement("ie", "i"));
    REPL.add(new Replacement("ee", "e"));
    REPL.add(new Replacement("o", "oh"));
    REPL.add(new Replacement("oh", "o"));
    REPL.add(new Replacement("a", "ah"));
    REPL.add(new Replacement("ah", "a"));
    REPL.add(new Replacement("e", "eh"));
    REPL.add(new Replacement("eh", "e"));
    REPL.add(new Replacement("ae", "ä"));
    REPL.add(new Replacement("oe", "ö"));
    REPL.add(new Replacement("ue", "ü"));
    REPL.add(new Replacement("Ae", "Ä"));
    REPL.add(new Replacement("Oe", "Ö"));
    REPL.add(new Replacement("Ue", "Ü"));
    REPL.add(new Replacement("d", "t"));
    REPL.add(new Replacement("t", "d"));
    REPL.add(new Replacement("th", "t"));
    REPL.add(new Replacement("t", "th"));
    REPL.add(new Replacement("r", "rh"));
    REPL.add(new Replacement("ch", "k"));
    REPL.add(new Replacement("k", "ch"));
    // not in de_DE.aff (not clear what uppercase replacement we need...):
    REPL.add(new Replacement("F", "Ph"));
    REPL.add(new Replacement("Ph", "F"));
  }

  public GermanSpellerRule(ResourceBundle messages, Language language) {
    super(messages, language, getCompoundSplitter(), getSpeller(language));
    addExamplePair(Example.wrong("LanguageTool kann mehr als eine <marker>nromale</marker> Rechtschreibprüfung."),
                   Example.fixed("LanguageTool kann mehr als eine <marker>normale</marker> Rechtschreibprüfung."));
  }

  @Override
  public String getId() {
    return RULE_ID;
  }
  
  private static CompoundWordTokenizer getCompoundSplitter() {
    try {
      final AbstractWordSplitter wordSplitter = new GermanWordSplitter(false);
      wordSplitter.setStrictMode(false); // there's a spelling mistake in (at least) one part, so strict mode wouldn't split the word
      ((GermanWordSplitter)wordSplitter).setMinimumWordLength(3);
      return new CompoundWordTokenizer() {
        @Override
        public List<String> tokenize(String word) {
          return new ArrayList<>(wordSplitter.splitWord(word));
        }
      };
    } catch (IOException e) {
      throw new RuntimeException("Could not set up German compound splitter", e);
    }
  }

  private static MorfologikSpeller getSpeller(Language language) {
    if (!language.getShortName().equals(Locale.GERMAN.getLanguage())) {
      throw new RuntimeException("Language is not a variant of German: " + language);
    }
    try {
      final String morfoFile = "/de/hunspell/de_" + language.getCountries()[0] + ".dict";
      if (JLanguageTool.getDataBroker().resourceExists(morfoFile)) {
        // spell data will not exist in LibreOffice/OpenOffice context 
        return new MorfologikSpeller(morfoFile, Locale.getDefault(), MAX_EDIT_DISTANCE);
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
