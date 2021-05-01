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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.linguistic2.SingleProofreadingError;

/**
 * Class for storing and handle the LT results prepared to use in LO/OO
 *
 * @author Fred Kruse
 * @since 4.3
 */
class ResultCache implements Serializable {

  private static final long serialVersionUID = 1L;
  private Map<Integer, CacheEntry> entries;

  ResultCache() {
    this(null);
  }

  ResultCache(ResultCache cache) {
    this.entries = Collections.synchronizedMap(new HashMap<>());
    if (cache != null) {
      synchronized(cache.entries) {
        this.entries.putAll(cache.entries);
      }
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
    Map<Integer, CacheEntry> tmpEntries = entries;
    entries = Collections.synchronizedMap(new HashMap<>());
    synchronized (tmpEntries) {
      for (int i : tmpEntries.keySet()) {
        if (i >= firstParagraph && i + shift >= 0) {
          entries.put(i + shift, tmpEntries.get(i));
        } else if (i < firstParagraph + shift) {
          entries.put(i, tmpEntries.get(i));
        } 
      }
    }
  }

  /**
   * add or replace a cache entry
   */
  void put(int numberOfParagraph, List<Integer> nextSentencePositions, SingleProofreadingError[] errorArray) {
    entries.put(numberOfParagraph, new CacheEntry(nextSentencePositions, errorArray));
  }

  /**
   * add or replace a cache entry for paragraph
   */
  void put(int numberOfParagraph, SingleProofreadingError[] errorArray) {
    entries.put(numberOfParagraph, new CacheEntry(null, errorArray));
  }

  /**
   * Remove all cache entries
   */
  void removeAll() {
    entries.clear();
  }

  /**
   * get cache entry of paragraph
   */
  CacheEntry getCacheEntry(int numberOfParagraph) {
    return entries.get(numberOfParagraph);
  }

  /**
   * get Proofreading errors of on paragraph from cache
   */
  SingleProofreadingError[] getMatches(int numberOfParagraph) {
    CacheEntry entry = getCacheEntry(numberOfParagraph);
    if (entry == null) {
      return null;
    }
    return entry.getErrorArray();
  }

  /**
   * get start sentence position from cache
   */
  int getStartSentencePosition(int numberOfParagraph, int sentencePosition) {
    CacheEntry entry = entries.get(numberOfParagraph);
    if (entry == null) {
      return 0;
    }
    List<Integer> nextSentencePositions = entry.nextSentencePositions;
    if (nextSentencePositions == null || nextSentencePositions.size() < 2) {
      return 0;
    }
    int startPosition = 0;
    for (int position : nextSentencePositions) {
      if (position >= sentencePosition) {
        return position == sentencePosition ? position : startPosition;
      }
      startPosition = position;
    }
    return nextSentencePositions.get(nextSentencePositions.size() - 2);
  }

  /**
   * get next sentence position from cache
   */
  int getNextSentencePosition(int numberOfParagraph, int sentencePosition) {
    CacheEntry entry = entries.get(numberOfParagraph);
    if (entry == null) {
      return 0;
    }
    List<Integer> nextSentencePositions = entry.nextSentencePositions;
    if (nextSentencePositions == null || nextSentencePositions.size() == 0) {
      return 0;
    }
    for (int position : nextSentencePositions) {
      if (position > sentencePosition) {
        return position;
      }
    }
    return nextSentencePositions.get(nextSentencePositions.size() - 1);
  }

  /**
   * get Proofreading errors of sentence out of paragraph matches from cache
   */
  SingleProofreadingError[] getFromPara(int numberOfParagraph,
                                        int startOfSentencePosition, int endOfSentencePosition) {
    CacheEntry entry = entries.get(numberOfParagraph);
    if (entry == null) {
      return null;
    }
    List<SingleProofreadingError> errorList = new ArrayList<>();
    for (SingleProofreadingError eArray : entry.getErrorArray()) {
      if (eArray.nErrorStart >= startOfSentencePosition && eArray.nErrorStart < endOfSentencePosition) {
        errorList.add(eArray);
      }
    }
    return errorList.toArray(new SingleProofreadingError[0]);
  }

  /**
   * Compares to Entries
   * true if the both entries are identically
   */
  static boolean areDifferentEntries(CacheEntry newEntries, CacheEntry oldEntries) {
    if (newEntries == null || oldEntries == null) {
      return true;
    }
    SingleProofreadingError[] oldErrorArray = oldEntries.getErrorArray();
    SingleProofreadingError[] newErrorArray = newEntries.getErrorArray();
    if (oldErrorArray == null || newErrorArray == null || oldErrorArray.length != newErrorArray.length) {
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
    return false;
  }

  /**
   * Compares a paragraph cache with another cache.
   * Gives back a list of entries for every paragraph: true if the both entries are identically
   */
  List<Integer> differenceInCaches(ResultCache oldCache) {
    List<Integer> differentParas = new ArrayList<>();
    CacheEntry oEntry;
    CacheEntry nEntry;
    boolean isDifferent = true;
    synchronized(entries) {
      Set<Integer> entrySet = new HashSet<>(entries.keySet());
      for (int nPara : entrySet) {
        if (oldCache != null) {
          nEntry = entries.get(nPara);
          oEntry = oldCache.getCacheEntry(nPara);
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
    return entries.size();
  }

  /**
   * get number of matches
   */
  int getNumberOfMatches() {
    int number = 0;
    for (int n : entries.keySet()) {
      number += entries.get(n).errorArray.length;
    }
    return number;
  }

  /**
   * get an error from a position within a paragraph
   * if there are more than one error at the position return the one which begins at second
   * if there are more than one that begins at the same position return the one with the smallest size
   */
  SingleProofreadingError getErrorAtPosition(int numPara, int numChar) {
    CacheEntry entry = entries.get(numPara);
    if (entry == null) {
      return null;
    }
    SingleProofreadingError error = null;
    for (SingleProofreadingError err : entry.getErrorArray()) {
      if (numChar >= err.nErrorStart && numChar <= err.nErrorStart + err.nErrorLength) {
        if (error == null || error.nErrorStart < err.nErrorStart
            || (error.nErrorStart == err.nErrorStart && error.nErrorLength > err.nErrorLength)) {
          error = err;
        } 
      }
    }
    return error;
  }

  /**
   * Class of serializable cache entries
   */
  public class CacheEntry implements Serializable {
    private static final long serialVersionUID = 2L;
    final SerialProofreadingError[] errorArray;
    List<Integer> nextSentencePositions = null;

    CacheEntry(List<Integer> nextSentencePositions, SingleProofreadingError[] sErrorArray) {
      if (nextSentencePositions != null) {
        this.nextSentencePositions = new ArrayList<Integer>(nextSentencePositions);
      }
      this.errorArray = new SerialProofreadingError[sErrorArray.length];
      for (int i = 0; i < sErrorArray.length; i++) {
        this.errorArray[i] = new SerialProofreadingError(sErrorArray[i]);
      }
    }
    
    /**
     * Get an SingleProofreadingError array for one entry
     */
    SingleProofreadingError[] getErrorArray() {
      SingleProofreadingError[] eArray = new SingleProofreadingError[errorArray.length];
      for (int i = 0; i < errorArray.length; i++) {
        eArray[i] = errorArray[i].toSingleProofreadingError();
      }
      return eArray;
    }
  }
  
  /**
   * Class of serializable proofreading errors
   */
  class SerialProofreadingError implements Serializable {

    private static final long serialVersionUID = 1L;
    int nErrorStart;
    int nErrorLength;
    int nErrorType;
    String aFullComment;
    String aRuleIdentifier;
    String aShortComment;
    String[] aSuggestions;
    SerialPropertyValue[] aProperties = null;
    
    SerialProofreadingError(SingleProofreadingError error) {
      nErrorStart = error.nErrorStart;
      nErrorLength = error.nErrorLength;
      nErrorType = error.nErrorType;
      aFullComment = error.aFullComment;
      aRuleIdentifier = error.aRuleIdentifier;
      aShortComment = error.aShortComment;
      aSuggestions = error.aSuggestions;
      if (error.aProperties != null) {
        aProperties = new SerialPropertyValue[error.aProperties.length];
        for (int i = 0; i < error.aProperties.length; i++) {
          aProperties[i] = new SerialPropertyValue(error.aProperties[i]);
        }
      }
    }
    
    SingleProofreadingError toSingleProofreadingError () {
      SingleProofreadingError error = new SingleProofreadingError();
      error.nErrorStart = nErrorStart;
      error.nErrorLength = nErrorLength;
      error.nErrorType = nErrorType;
      error.aFullComment = aFullComment;
      error.aRuleIdentifier = aRuleIdentifier;
      error.aShortComment = aShortComment;
      error.aSuggestions = aSuggestions;
      if (aProperties != null) {
        error.aProperties = new PropertyValue[aProperties.length];
        for (int i = 0; i < aProperties.length; i++) {
          error.aProperties[i] = aProperties[i].toPropertyValue();
        }
      } else {
        error.aProperties = null;
      }
      return error;
    }
  }
  
  /**
   * Class of serializable property values
   */
  class SerialPropertyValue implements Serializable {

    private static final long serialVersionUID = 1L;
    String name;
    Object value;
    
    SerialPropertyValue(PropertyValue properties) {
      name = properties.Name;
      value = properties.Value;
    }
    
    PropertyValue toPropertyValue() {
      PropertyValue properties = new PropertyValue();
      properties.Name = name;
      properties.Value = value;
      properties.Handle = -1;
      properties.State = PropertyState.DIRECT_VALUE;
      return properties;
    }
  }

}
