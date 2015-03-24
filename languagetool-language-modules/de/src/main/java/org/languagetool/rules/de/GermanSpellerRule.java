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

import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.German;
import org.languagetool.rules.Example;
import org.languagetool.rules.spelling.hunspell.CompoundAwareHunspellRule;
import org.languagetool.rules.spelling.morfologik.MorfologikMultiSpeller;
import org.languagetool.tokenizers.de.GermanCompoundTokenizer;
import org.languagetool.tools.StringTools;

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
  
  private final GermanCompoundTokenizer compoundTokenizer;

  public GermanSpellerRule(ResourceBundle messages, German language) {
    super(messages, language, language.getNonStrictCompoundSplitter(), getSpeller(language));
    addExamplePair(Example.wrong("LanguageTool kann mehr als eine <marker>nromale</marker> Rechtschreibprüfung."),
                   Example.fixed("LanguageTool kann mehr als eine <marker>normale</marker> Rechtschreibprüfung."));
    compoundTokenizer = language.getStrictCompoundTokenizer();
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Nullable
  private static MorfologikMultiSpeller getSpeller(Language language) {
    if (!language.getShortName().equals(Locale.GERMAN.getLanguage())) {
      throw new RuntimeException("Language is not a variant of German: " + language);
    }
    try {
      final String morfoFile = "/de/hunspell/de_" + language.getCountries()[0] + ".dict";
      if (JLanguageTool.getDataBroker().resourceExists(morfoFile)) {
        // spell data will not exist in LibreOffice/OpenOffice context
        return new MorfologikMultiSpeller(morfoFile, "/de/hunspell/spelling.txt", MAX_EDIT_DISTANCE);
      } else {
        return null;
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not set up morfologik spell checker", e);
    }
  }

  @Override
  protected void filterForLanguage(List<String> suggestions) {
    if (language.getShortNameWithCountryAndVariant().equals("de-CH")) {
      for (int i = 0; i < suggestions.size(); i++) {
        String s = suggestions.get(i);
        suggestions.set(i, s.replace("ß", "ss"));
      }
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

  @Override
  protected List<String> getAdditionalTopSuggestions(List<String> suggestions, String word) {
    String w = word.replaceFirst("\\.$", "");
    if ("unzwar".equals(w)) {
      return Collections.singletonList("und zwar");
    } else if ("desweiteren".equals(w)) {
      return Collections.singletonList("des Weiteren");
    } else if ("wieviel".equals(w)) {
      return Collections.singletonList("wie viel");
    } else if ("wieviele".equals(w)) {
      return Collections.singletonList("wie viele");
    } else if ("wievielen".equals(w)) {
      return Collections.singletonList("wie vielen");
    } else if ("vorteilen".equals(w)) {
      return Collections.singletonList("Vorteilen");
    } else if ("Trons".equals(w)) {
      return Collections.singletonList("Trance");
    } else if ("einzigste".equals(w)) {
      return Collections.singletonList("einzige");
    } else if (!StringTools.startsWithUppercase(word)) {
      String ucWord = StringTools.uppercaseFirstChar(word);
      if (!hunspellDict.misspelled(ucWord)) {
        // Hunspell doesn't always automatically offer the most obvious suggestion for compounds:
        return Collections.singletonList(ucWord);
      }
    }
    return Collections.emptyList();
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
  @Nullable
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
    final String key;
    final String value;
    private Replacement(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }
}
