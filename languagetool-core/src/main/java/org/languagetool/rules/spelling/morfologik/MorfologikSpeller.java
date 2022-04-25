/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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
package org.languagetool.rules.spelling.morfologik;

import com.google.common.cache.*;
import morfologik.speller.Speller;
import morfologik.stemming.Dictionary;
import org.jetbrains.annotations.NotNull;
import org.languagetool.JLanguageTool;
import org.languagetool.broker.ResourceDataBroker;
import org.languagetool.rules.patterns.StringMatcher;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Morfologik-based spell checker.
 */
public class MorfologikSpeller {

  // Speed up the server use case, where rules get initialized for every call.
  // See https://github.com/morfologik/morfologik-stemming/issues/69 for confirmation that
  // Dictionary is thread-safe:
  private static final LoadingCache<String, Dictionary> dictCache = CacheBuilder.newBuilder()
      //.maximumSize(0)
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build(new CacheLoader<String, Dictionary>() {
        @Override
        public Dictionary load(@NotNull String fileInClassPath) throws IOException {
          ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
          if (dataBroker.resourceExists(fileInClassPath)) {
            return Dictionary.read(dataBroker.getFromResourceDirAsUrl(fileInClassPath));
          } else {
            return Dictionary.read(Paths.get(fileInClassPath));
          }
        }
      });

  private final Dictionary dictionary;
  private final Speller speller;
  private final int maxEditDistance;

  /**
   * Creates a speller with the given maximum edit distance.
   * @param fileInClassPath path in classpath to morfologik dictionary
   */
  public MorfologikSpeller(String fileInClassPath, int maxEditDistance) {
    this(getDictionaryWithCaching(fileInClassPath), maxEditDistance);
  }

  /**
   * Creates a speller with a maximum edit distance of one.
   * @param fileInClassPath path in classpath to morfologik dictionary
   */
  public MorfologikSpeller(String fileInClassPath) throws IOException {
    this(fileInClassPath, 1);
  }

  /** @since 2.9 */
  MorfologikSpeller(Dictionary dictionary, int maxEditDistance) {
    if (maxEditDistance <= 0) {
      throw new RuntimeException("maxEditDistance must be > 0: " + maxEditDistance);
    }
    this.dictionary = dictionary;
    this.maxEditDistance = maxEditDistance;
    speller = new Speller(dictionary, maxEditDistance);
  }

  /**
   * Load a dictionary from the given path or reuse a cached instance, if present.
   * @param fileInClassPath path in classpath to morfologik dictionary
   */
  public static Dictionary getDictionaryWithCaching(@NotNull String fileInClassPath) {
    return dictCache.getUnchecked(fileInClassPath);
  }

  public boolean isMisspelled(String word) {
    if (word.isEmpty() || SpellingCheckRule.LANGUAGETOOL.equals(word) || SpellingCheckRule.LANGUAGETOOLER.equals(word)) {
      return false;
    }
    synchronized (this) {
      return speller.isMisspelled(word);
    }
  }

  public synchronized List<String> findReplacements(String word) {
    return speller.findReplacements(word);
  }

  /**
   * @deprecated use (or introduce) other methods to this class which would take care of the necessary synchronization
   */
  @Deprecated
  public Speller getSpeller() {
    return speller;
  }

  public List<WeightedSuggestion> getSuggestions(String word) {
    List<WeightedSuggestion> suggestions = new ArrayList<>();
    if (word.length() > StringMatcher.MAX_MATCH_LENGTH) {
      return suggestions;
    }
    // needs to be reset every time, possible bug: HMatrix for distance computation is not reset;
    // output changes when reused
    Speller speller = new Speller(dictionary, maxEditDistance);
    List<Speller.CandidateData> replacementCandidates;
    if (word.length() < 50) {   // slow for long words (the limit is arbitrary)
      replacementCandidates = speller.findReplacementCandidates(word);
      for (Speller.CandidateData candidate : replacementCandidates) {
        suggestions.add(new WeightedSuggestion(candidate.getWord(), candidate.getDistance()));
      }
    }
    List<Speller.CandidateData> runOnCandidates = speller.replaceRunOnWordCandidates(word);
    for (Speller.CandidateData runOnCandidate : runOnCandidates) {
      suggestions.add(new WeightedSuggestion(runOnCandidate.getWord(), runOnCandidate.getDistance()));
    }
    
    // all upper-case suggestions if necessary
    if (dictionary.metadata.isConvertingCase() && StringTools.isAllUppercase(word)) {
      for (int i = 0; i < suggestions.size(); i++) {
        WeightedSuggestion sugg = suggestions.get(i);
        String allUppercase = sugg.getWord().toUpperCase();
        // do not use capitalized word if it matches the original word or it's mixed case
        if (allUppercase.equals(word) || StringTools.isMixedCase(suggestions.get(i).getWord())) {
          allUppercase = sugg.getWord();
        }
        // remove duplicates
        int auxIndex = getSuggestionIndex(suggestions, allUppercase);
        if (auxIndex > i) {
          suggestions.remove(auxIndex);
        }
        if (auxIndex > -1 && auxIndex < i) {
          suggestions.remove(i);
          i--;
        } else {
          suggestions.set(i, new WeightedSuggestion(allUppercase, sugg.getWeight()));
        }
      }
    }
    // capitalize suggestions if necessary
    else if (dictionary.metadata.isConvertingCase() && StringTools.startsWithUppercase(word)) {
      for (int i = 0; i < suggestions.size(); i++) {
        WeightedSuggestion sugg = suggestions.get(i);
        String uppercaseFirst = StringTools.uppercaseFirstChar(sugg.getWord());
        // do not use capitalized word if it matches the original word or it's mixed case
        if (uppercaseFirst.equals(word) || StringTools.isMixedCase(suggestions.get(i).getWord())) {
          uppercaseFirst = sugg.getWord();
        }
        // remove capitalized duplicates
        int auxIndex = getSuggestionIndex(suggestions, uppercaseFirst);
        if (auxIndex > i) {
          suggestions.remove(auxIndex);
        }
        if (auxIndex > -1 && auxIndex < i) {
          suggestions.remove(i);
          i--;
        } else {
          suggestions.set(i, new WeightedSuggestion(uppercaseFirst, sugg.getWeight()));
        }
      }
    }
    return suggestions;
  }

  private int getSuggestionIndex(List<WeightedSuggestion> suggestions, String uppercaseFirst) {
    int i = 0;
    for (WeightedSuggestion suggestion : suggestions) {
      if (suggestion.getWord().equals(uppercaseFirst)) {
        return i;
      }
      i++;
    }
    return -1;
  }

  /**
   * Determines whether the dictionary uses case conversions.
   * @return True when the speller uses spell conversions.
   * @since 2.5
   */
  public boolean convertsCase() {
    return speller.convertsCase();
  }

  @Override
  public String toString() {
    return "dist=" + maxEditDistance;
  }

  public synchronized int getFrequency(String word) {
    int freq = speller.getFrequency(word);
    if (freq == 0 && !word.equals(word.toLowerCase())) {
      freq = speller.getFrequency(word.toLowerCase());
    }
    return freq;
  }
}
