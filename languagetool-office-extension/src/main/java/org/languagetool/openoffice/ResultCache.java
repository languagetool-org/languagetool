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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.star.linguistic2.SingleProofreadingError;

/**
 * Class for storing and handle the LT results prepared to use in LO/OO
 *
 * @author Fred Kruse
 * @since 4.3
 */
class ResultCache {

  private Map<Integer, CacheSentenceEntries> entries;

  ResultCache() {
    this(null);
  }

  ResultCache(ResultCache cache) {
    this.entries = Collections.synchronizedMap(new HashMap<>());
    if(cache != null) {
      synchronized(cache.entries) {
        this.entries.putAll(cache.entries);
      }
    }
  }

  /**
   * Remove a cache entry for a sentence
   */
  void remove(int numberOfParagraph, int startOfSentencePosition) {
    CacheSentenceEntries sentenceEntries = entries.get(numberOfParagraph);
    if(sentenceEntries != null) {
      sentenceEntries.remove(startOfSentencePosition);
    }
  }

  /**
   * Remove all cache entries for a paragraph
   */
  void remove(int numberOfParagraph) {
    entries.remove(numberOfParagraph);
  }

  /**
   * Remove all cache entries between firstParagraph and lastParagraph
   */
  void removeRange(int firstParagraph, int lastParagraph) {
    for (int i = firstParagraph; i <= lastParagraph; i++) {
      entries.remove(i);
    }
  }

  /**
   * Remove all cache entries between firstPara (included) and lastPara (included)
   * shift all numberOfParagraph by 'shift'
   */
  void removeAndShift(int firstParagraph, int lastParagraph, int shift) {
    for (int i = firstParagraph; i <= lastParagraph; i++) {
      entries.remove(i);
    }
    
    Map<Integer, CacheSentenceEntries> tmpEntries = entries;
    entries = Collections.synchronizedMap(new HashMap<>());
    synchronized (tmpEntries) {
      for(int i : tmpEntries.keySet()) {
        if(i > lastParagraph) {
          entries.put(i + shift, tmpEntries.get(i));
        } else {
          entries.put(i, tmpEntries.get(i));
        } 
      }
    }
  }

  /**
   * add / replace a cache entry
   */
  void put(int numberOfParagraph, int startOfSentencePosition, int nextSentencePosition, SingleProofreadingError[] errorArray) {
    CacheSentenceEntries sentenceEntries = entries.get(numberOfParagraph);
    if(sentenceEntries == null) {
      entries.put(numberOfParagraph, new CacheSentenceEntries(startOfSentencePosition, nextSentencePosition, errorArray));
    } else {
      sentenceEntries.put(startOfSentencePosition, nextSentencePosition, errorArray);
    }
  }

  /**
   * add / replace a cache entry for paragraph
   */
  void put(int numberOfParagraph, SingleProofreadingError[] errorArray) {
    entries.put(numberOfParagraph, new CacheSentenceEntries(0, 0, errorArray));
  }

  /**
   * Remove all cache entries
   */
  void removeAll() {
    entries.clear();
  }

  /**
   * get Proofreading errors from cache
   */
  SingleProofreadingError[] getMatches(int numberOfParagraph, int startOfSentencePosition) {
    CacheSentenceEntries sentenceEntries = entries.get(numberOfParagraph);
    if(sentenceEntries == null) {
      return null;
    }
    return sentenceEntries.getErrorArray(startOfSentencePosition);
  }

  /**
   * get all Proofreading errors of on paragraph from cache
   */
  SingleProofreadingError[] getMatches(int numberOfParagraph) {
    CacheSentenceEntries sentenceEntries = entries.get(numberOfParagraph);
    if(sentenceEntries == null) {
      return null;
    }
    List<SingleProofreadingError> allErrors = new ArrayList<>();
    for (int pos : sentenceEntries.keySet()) {
      SingleProofreadingError[] errors = sentenceEntries.getErrorArray(pos);
      for (SingleProofreadingError error : errors) {
        allErrors.add(error);
      }
    }
    return allErrors.toArray(new SingleProofreadingError[0]);
  }

  /**
   * get next sentence position from cache
   */
  int getNextSentencePosition(int numberOfParagraph, int startOfSentencePosition) {
    CacheSentenceEntries sentenceEntries = entries.get(numberOfParagraph);
    if(sentenceEntries == null) {
      return -1;
    }
    return sentenceEntries.getNextSentencePosition(startOfSentencePosition);
  }

  /**
   * get Proofreading errors of sentence out of paragraph matches from cache
   */
  SingleProofreadingError[] getFromPara(int numberOfParagraph,
                                        int startOfSentencePosition, int endOfSentencePosition) {
    CacheSentenceEntries sentenceEntries = entries.get(numberOfParagraph);
    if(sentenceEntries == null) {
      return null;
    }
    List<SingleProofreadingError> errorList = new ArrayList<>();
    for (int i : sentenceEntries.keySet()) {
      for (SingleProofreadingError eArray : sentenceEntries.getErrorArray(i)) {
        if (eArray.nErrorStart >= startOfSentencePosition && eArray.nErrorStart < endOfSentencePosition) {
          errorList.add(eArray);
        }
      }
    }
    return errorList.toArray(new SingleProofreadingError[0]);
  }

  /**
   * get an ResultCache entry by the number of paragraph
   */
  CacheSentenceEntries getEntryByParagraph(int numberOfParagraph) {
    return entries.get(numberOfParagraph);
  }

  /**
   * Compares to Entries
   * true if the both entries are identically
   */
  private boolean areDifferentEntries(CacheSentenceEntries newEntries, CacheSentenceEntries oldEntries) {
    if (newEntries == null || oldEntries == null || newEntries.size() != oldEntries.size()) {
      return true;
    }
    for(int startSentence : newEntries.keySet()) {
      SingleProofreadingError[] oldErrorArray = oldEntries.getErrorArray(startSentence);
      SingleProofreadingError[] newErrorArray = newEntries.getErrorArray(startSentence);
      if(oldErrorArray == null || newErrorArray == null || oldErrorArray.length != newErrorArray.length) {
        return true;
      }
      for (SingleProofreadingError nError : newErrorArray) {
        boolean found = false;
        for (SingleProofreadingError oError : oldErrorArray) {
          if (nError.nErrorStart == oError.nErrorStart && nError.nErrorLength == oError.nErrorLength
                  && nError.aRuleIdentifier.equals(oError.aRuleIdentifier)) {
            found = true;
            break;
          }
        }
        if (!found) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Compares a paragraph cache with another cache.
   * Gives back a list of entries for every paragraph: true if the both entries are identically
   */
  List<Integer> differenceInCaches(ResultCache oldCache) {
    List<Integer> differentParas = new ArrayList<>();
    CacheSentenceEntries oEntry;
    CacheSentenceEntries nEntry;
    boolean isDifferent = true;
    synchronized(entries) {
      Set<Integer> entrySet = new HashSet<>(entries.keySet());
      for (int nPara : entrySet) {
        if(oldCache != null) {
          nEntry = entries.get(nPara);
          oEntry = oldCache.getEntryByParagraph(nPara);
          isDifferent = areDifferentEntries(nEntry, oEntry);
        }
        if (isDifferent) {
          differentParas.add(nPara);
        }
      }
    }
    return differentParas;
  }

  /**
   * get number of paragraphs stored in cache
   */
  int getNumberOfParas() {
    return entries.size();
  }

  /**
   * get number of entries
   */
  int getNumberOfEntries() {
    int number = 0;
    for(int n : entries.keySet()) {
      number += entries.get(n).size();
    }
    return number;
  }

  /**
   * get an error from a position within a paragraph
   * if there are more than one error at the position return the one which begins at second
   * if there are more than one that begins at the same position return the one with the smallest size
   */
  SingleProofreadingError getErrorAtPosition(int numPara, int numChar) {
    CacheSentenceEntries sentenceEntries = entries.get(numPara);
    if(sentenceEntries == null) {
      return null;
    }
    SingleProofreadingError error = null;
    for(int sentenceStart : sentenceEntries.keySet()) {
      int sentenceNext = sentenceEntries.getNextSentencePosition(sentenceStart);
      if(sentenceStart <= numChar &&  (sentenceNext == 0 || sentenceNext <= numChar)) {
        for(SingleProofreadingError err : sentenceEntries.getErrorArray(sentenceStart)) {
          if(numChar >= err.nErrorStart && numChar <= err.nErrorStart + err.nErrorLength) {
            if(error == null || error.nErrorStart < err.nErrorStart
                || (error.nErrorStart == err.nErrorStart && error.nErrorLength > err.nErrorLength)) {
              error = err;
            } 
          }
        }
      }
    }
    return error;
  }

  static class CacheSentenceEntries {
    private Map<Integer, CacheEntry> sentenceEntry;

    CacheSentenceEntries() {
      sentenceEntry = new HashMap<>();
    }
    
    CacheSentenceEntries(int startOfSentencePosition, int nextSentencePosition, SingleProofreadingError[] errorArray) {
      sentenceEntry = new HashMap<>();
      sentenceEntry.put(startOfSentencePosition, new CacheEntry(nextSentencePosition, errorArray));
    }
    
    void remove(int startOfSentencePosition) {
      sentenceEntry.remove(startOfSentencePosition);
    }
    
    void put(int startOfSentencePosition, int nextSentencePosition, SingleProofreadingError[] errorArray) {
      sentenceEntry.put(startOfSentencePosition, new CacheEntry(nextSentencePosition, errorArray));
    }
    
    Set<Integer> keySet() {
      return sentenceEntry.keySet();
    }
    
    int size() {
      return sentenceEntry.size();
    }

    SingleProofreadingError[] getErrorArray(int startOfSentencePosition) {
      CacheEntry entry = sentenceEntry.get(startOfSentencePosition);
      if (entry == null) {
        return null;
      }
      return entry.errorArray;
    }
    
    int getNextSentencePosition(int startOfSentencePosition) {
      CacheEntry entry = sentenceEntry.get(startOfSentencePosition);
      if (entry == null) {
        return -1;
      }
      return entry.nextSentencePosition;
    }
    
    private static class CacheEntry {
      final int nextSentencePosition;
      final SingleProofreadingError[] errorArray;

      CacheEntry(int nextSentencePosition, SingleProofreadingError[] errorArray) {
        this.nextSentencePosition = nextSentencePosition;
        this.errorArray = errorArray;
      }
    }

  }

} 
