/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber
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
package org.languagetool.rules;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import lombok.extern.slf4j.Slf4j;
import org.languagetool.UserConfig;
import org.languagetool.markup.AnnotatedText;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Filter spelling error with phrases the users wants to have accepted.
 * Needed so words with spaces (i.e. phrases) can be added to a user's dictionary
 * without LT creating internal anti patterns for each phrase.
 */
@Slf4j
public class DictionarySpellMatchFilter implements RuleMatchFilter {

  private final UserConfig userConfig;

  private static final int CACHE_SIZE = 100;
  private static final long CACHE_TTL_SECONDS = 600;

  private static class PhraseSearchLoader extends CacheLoader<UserConfig, AhoCorasickDoubleArrayTrie<String>> {
    @Override
    public AhoCorasickDoubleArrayTrie<String> load(UserConfig key) throws Exception {
      AhoCorasickDoubleArrayTrie<String> searcher = new AhoCorasickDoubleArrayTrie<>();
      Map<String, String> phrases = new HashMap<>();
      for (String phrase : key.getAcceptedPhrases()) {
        phrases.put(phrase, phrase);
      }
      searcher.build(phrases);
      return searcher;
    }
  }

  private static final LoadingCache<UserConfig, AhoCorasickDoubleArrayTrie<String>> phraseSearcher =
    CacheBuilder.newBuilder().recordStats()
      .maximumSize(CACHE_SIZE)
      .expireAfterAccess(CACHE_TTL_SECONDS, TimeUnit.SECONDS)
      .build(new PhraseSearchLoader());

  public DictionarySpellMatchFilter(UserConfig userConfig) {
    this.userConfig = userConfig;
  }

  @Override
  public List<RuleMatch> filter(List<RuleMatch> ruleMatches, AnnotatedText text) {
    Set<String> dictionary = userConfig.getAcceptedPhrases();
    if (dictionary.size() > 0) {
      List<RuleMatch> cleanMatches = new ArrayList<>(ruleMatches);
      try {
        AhoCorasickDoubleArrayTrie<String> searcher = phraseSearcher.get(userConfig);

        List<AhoCorasickDoubleArrayTrie.Hit<String>> phrases = searcher.parseText(text.getPlainText());

        for (AhoCorasickDoubleArrayTrie.Hit<String> phrase : phrases) {
          Iterator<RuleMatch> iter = cleanMatches.iterator();

          while (iter.hasNext()) {
            RuleMatch match = iter.next();
            if (match.getRule().isDictionaryBasedSpellingRule() &&
                match.getFromPos() >= phrase.begin &&
                match.getToPos() <= phrase.end) {
              // remove all spelling matches that are (subsets) of accepted phrases
              iter.remove();
            }
          }
        }
        return cleanMatches;
      } catch (ExecutionException e) {
        log.error("Couldn't set up phrase search, accepted phrases won't work.", e);
        return ruleMatches;
      }
    }
    return ruleMatches;
  }

  public Map<String, List<RuleMatch>> getPhrases(List<RuleMatch> ruleMatches, AnnotatedText text) {
    Map<String, List<RuleMatch>> phraseToMatches = new HashMap<>();
    int prevToPos = Integer.MIN_VALUE;
    List<RuleMatch> collectedMatches = new ArrayList<>();
    List<String> collectedTerms = new ArrayList<>();
    for (RuleMatch match : ruleMatches) {
      if (match.getRule().isDictionaryBasedSpellingRule()) {
        String covered = text.getPlainText().substring(match.getFromPos(), match.getToPos());
        if (match.getFromPos() == prevToPos + 1) {
          String key = String.join(" ", collectedTerms) + " " + covered;
          ArrayList<RuleMatch> l = new ArrayList<>(collectedMatches);
          l.add(match);
          phraseToMatches.put(key, l);
        } else {
          collectedTerms.clear();
          collectedMatches.clear();
        }
        collectedTerms.add(covered);
        collectedMatches.add(match);
        prevToPos = match.getToPos();
      }
    }
    return phraseToMatches;
  }

}
