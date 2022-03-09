/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;
import org.jetbrains.annotations.NotNull;
import org.languagetool.rules.RuleMatch;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A cache to speed up text checking for use cases where sentences are checked more than once. This
 * typically happens when using LT as a server and texts get re-checked after corrections have been applied
 * for some sentences. Use the same cache object for all {@link JLanguageTool} objects <strong>only if
 * the JLanguageTool objects all use the same rules.</strong> For example, if you call {@code JLanguageTool.addRule()}
 * in different ways for the different instances that you use the same cache for, the cache will return invalid results.
 * Using a cache with bitext rules isn't supported either.
 * It is okay however, to use the same cache for {@link JLanguageTool} objects with different languages, as
 * cached results are not used for a different language.
 * @since 3.7
 */
public class ResultCache {

  /**
   * rules can fail individually, results can be partial ->
   * store list if success (can be empty), null -> failure/not checked
   */
  private final Cache<InputSentence, Map<String, List<RuleMatch>>> remoteMatchesCache;
  private final Cache<InputSentence, List<RuleMatch>> matchesCache;
  private final Cache<SimpleInputSentence, AnalyzedSentence> sentenceCache;

  /**
   * Create a cache that expires items 5 minutes after the latest read access.
   * @param maxSize maximum cache size in number of sentences
   */
  public ResultCache(long maxSize) {
    this(maxSize, 5, TimeUnit.MINUTES);
  }

  /**
   * @param maxSize maximum cache size in number of sentences
   * @param expireAfter time to expire sentences from the cache after last read access 
   */
  public ResultCache(long maxSize, long expireAfter, TimeUnit timeUnit) {
    if (maxSize < 0) {
      throw new IllegalArgumentException("Result cache size must be >= 0: " + maxSize);
    }
    matchesCache = CacheBuilder.newBuilder().
            maximumWeight(maxSize/2).weigher(new MatchesWeigher()).
            recordStats().
            expireAfterAccess(expireAfter, timeUnit).
            build();
    remoteMatchesCache = CacheBuilder.newBuilder().
      maximumWeight(maxSize/2).weigher(new RemoteMatchesWeigher()).
      recordStats().
      expireAfterAccess(expireAfter, timeUnit).
      build();
    sentenceCache = CacheBuilder.newBuilder().
            maximumWeight(maxSize/2).weigher(new SentenceWeigher()).
            recordStats().
            expireAfterAccess(expireAfter, timeUnit).
            build();
  }
  
  static class MatchesWeigher implements Weigher<InputSentence, List<RuleMatch>> {
    @Override
    public int weigh(InputSentence sentence, List<RuleMatch> matches) {
      // this is just a rough guesstimate so that the cacheSize given by the user
      // is very roughly the number of average sentences the cache can keep:
      return sentence.getText().length() / 75 + matches.size();
    }
  }

  static class RemoteMatchesWeigher implements Weigher<InputSentence, Map<String, List<RuleMatch>>> {
    @Override
    public int weigh(InputSentence sentence, @NotNull Map<String, List<RuleMatch>> matches) {
      // this is just a rough guesstimate so that the cacheSize given by the user
      // is very roughly the number of average sentences the cache can keep:
      return sentence.getText().length() / 75;
    }
  }

  static class SentenceWeigher implements Weigher<SimpleInputSentence, AnalyzedSentence> {
    @Override
    public int weigh(SimpleInputSentence sentence, @NotNull AnalyzedSentence analyzedSentence) {
      return sentence.getText().length() / 75;
    }
  }
  
  public double hitRate() {
    return (matchesCache.stats().hitRate() + sentenceCache.stats().hitRate()) / 2.0;
  }

  public double requestCount() {
    return matchesCache.stats().requestCount() + sentenceCache.stats().requestCount();
  }

  public long hitCount() {
    return matchesCache.stats().hitCount() + sentenceCache.stats().hitCount();
  }

  public List<RuleMatch> getIfPresent(InputSentence key) {
    return matchesCache.getIfPresent(key);
  }

  public AnalyzedSentence getIfPresent(SimpleInputSentence key) {
    return sentenceCache.getIfPresent(key);
  }

  public void put(InputSentence key, List<RuleMatch> sentenceMatches) {
    matchesCache.put(key, sentenceMatches);
  }

  public void put(SimpleInputSentence key, AnalyzedSentence aSentence) {
    sentenceCache.put(key, aSentence);
  }

  /** @since 4.1 */
  public Cache<InputSentence, List<RuleMatch>> getMatchesCache() {
    return matchesCache;
  }

  /** @since 5.0 */
  public Cache<InputSentence, Map<String, List<RuleMatch>>> getRemoteMatchesCache() {
    return remoteMatchesCache;
  }

  /** @since 4.1 */
  public Cache<SimpleInputSentence, AnalyzedSentence> getSentenceCache() {
    return sentenceCache;
  }
}
