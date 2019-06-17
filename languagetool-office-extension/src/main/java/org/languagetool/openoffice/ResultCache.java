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
import java.util.List;

import com.sun.star.linguistic2.SingleProofreadingError;

/**
 * Class for storing and handle the LT results prepared to use in LO/OO
 * @since 4.3
 * @author Fred Kruse, Marcin Miłkowski
 */
class ResultCache {
  
  private final List<CacheEntry> entries;

  ResultCache() {
    entries = new ArrayList<>();
  }
  
  ResultCache( ResultCache cache) {
    entries = new ArrayList<>();
    for (int i = 0; i < entries.size(); i++) {
      entries.add(cache.entries.get(i));
    }
  }
  
  /**
   *  Remove a cache entry for a sentence
   */
  void remove(int numberOfParagraph, int startOfSentencePosition) {
    for(int i = 0; i < entries.size(); i++) {
      if(entries.get(i).numberOfParagraph == numberOfParagraph
        && entries.get(i).startOfSentencePosition == startOfSentencePosition) {
        entries.remove(i);
        return;
      }
    }
  }
  
  /**
   *  Remove all cache entries for a paragraph
   */
  void remove(int numberOfParagraph) {
    for(int i = 0; i < entries.size(); i++) {
      if(entries.get(i).numberOfParagraph == numberOfParagraph) {
        entries.remove(i);
        i--;
      }
    }
  }
  
  /**
   *  Remove all cache entries between firstParagraph and lastParagraph
   */
  void removeRange(int firstParagraph, int lastParagraph) {
    for(int i = 0; i < entries.size(); i++) {
      if(entries.get(i).numberOfParagraph >= firstParagraph && entries.get(i).numberOfParagraph <= lastParagraph) {
        entries.remove(i);
        i--;
      }
    }
  }
  
  /**
   *  Remove all cache entries between firstPara (included) and lastPara (included)
   *  shift all numberOfParagraph by 'shift'
   */
  void removeAndShift(int firstPara, int lastPara, int shift) {
    for(int i = 0; i < entries.size(); i++) {
      if(entries.get(i).numberOfParagraph >= firstPara && entries.get(i).numberOfParagraph <= lastPara) {
        entries.remove(i);
        i--;
      } 
    }
    for (CacheEntry anEntry : entries) {
      if (anEntry.numberOfParagraph > lastPara) {
        anEntry.numberOfParagraph += shift;
      }
    }
  }
  
  /**
   *  Add an cache entry 
   */
  public void add(int numberOfParagraph, int startOfSentencePosition, int nextSentencePosition, SingleProofreadingError[] errorArray) {
    entries.add(new CacheEntry(numberOfParagraph, startOfSentencePosition, nextSentencePosition, errorArray));
  }

  /**
   *  Add an cache entry for paragraph
   */
  public void add(int numberOfParagraph, SingleProofreadingError[] errorArray) {
    this.add(numberOfParagraph, 0, 0, errorArray);
  }

  /**
   *  replace an cache entry 
   */
  void put(int numberOfParagraph, int startOfSentencePosition, int nextSentencePosition, SingleProofreadingError[] errorArray) {
    remove(numberOfParagraph, startOfSentencePosition);
    add(numberOfParagraph, startOfSentencePosition, nextSentencePosition, errorArray);
  }
  
  /**
   *  replace an cache entry for paragraph
   */
  void put(int numberOfParagraph, SingleProofreadingError[] errorArray) {
    this.put(numberOfParagraph, 0, 0, errorArray);
  }
  
  /**
   *  Remove all cache entries
   */
  void removeAll() {
    entries.clear();
  }
  
  /**
   *  get Proofreading errors from cache
   */
  SingleProofreadingError[] getMatches(int numberOfParagraph, int startOfSentencePosition) {
    for (CacheEntry anEntry : entries) {
      if (anEntry.numberOfParagraph == numberOfParagraph && anEntry.startOfSentencePosition == startOfSentencePosition) {
        return anEntry.errorArray;
      }
    }
    return null;
  }
  
  /**
   *  get Proofreading errors from cache
   */
  int getNextSentencePosition(int numberOfParagraph, int startOfSentencePosition) {
    for (CacheEntry anEntry : entries) {
      if (anEntry.numberOfParagraph == numberOfParagraph && anEntry.startOfSentencePosition == startOfSentencePosition) {
        return anEntry.nextSentencePosition;
      }
    }
    return -1;
  }
  
  /**
   *  get Proofreading errors of sentence out of paragraph matches from cache
   */
  SingleProofreadingError[] getFromPara(int numberOfParagraph,
              int startOfSentencePosition, int endOfSentencePosition) {
    for (CacheEntry anEntry : entries) {
      if (anEntry.numberOfParagraph == numberOfParagraph) {
        List<SingleProofreadingError> errorList = new ArrayList<>();
        for (SingleProofreadingError eArray : anEntry.errorArray) {
          if (eArray.nErrorStart >= startOfSentencePosition && eArray.nErrorStart < endOfSentencePosition) {
            errorList.add(eArray);
          }
        }
        return errorList.toArray(new SingleProofreadingError[0]);
      }
    }
    return null;
  }
  
/**
 * get an ResultCache entry by the number of paragraph
 */
  CacheEntry getEntrybyParagraph(int nPara) {
    for (CacheEntry anEntry : entries) {
      if(anEntry.numberOfParagraph == nPara) {
        return anEntry;
      }
    }
    return null;
  }

  /**
   * Compares to Entries
   * true if the both entries are identically 
   */
  private boolean areDifferentEntries (CacheEntry newEntry, CacheEntry oldEntry) {
    if(newEntry == null || oldEntry == null || newEntry.errorArray.length != oldEntry.errorArray.length) {
      return true;
    }
    for (SingleProofreadingError nError : newEntry.errorArray) {
      boolean found = false;
      for (SingleProofreadingError oError : oldEntry.errorArray) {
        if(nError.nErrorStart == oError.nErrorStart && nError.nErrorLength == oError.nErrorLength 
            && nError.aRuleIdentifier.equals(oError.aRuleIdentifier)) {
          found = true;
          break;
        }
      }
      if(!found) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Compares paragraph cache with an other
   * Gives back a list of entries for every paragraph: true if the both entries are identically 
   */
  List<Integer> differenceInCaches(ResultCache oldCache) {
    List<Integer> differentParas = new ArrayList<Integer>();
    CacheEntry oEntry;
    for (CacheEntry nEntry : entries) {
      oEntry = oldCache.getEntrybyParagraph(nEntry.numberOfParagraph);
      boolean isDifferent = areDifferentEntries(nEntry, oEntry);
      if(isDifferent) {
        differentParas.add(nEntry.numberOfParagraph);
      }
    }
    return differentParas;
  }
  
  /**
   *  get number of paragraphs stored in cache
   */
  int getNumberOfParas() {
    int number = 0;
    for (CacheEntry anEntry : entries) {
      if (anEntry.startOfSentencePosition == 0) {
        number++;
      }
    }
    return number;
  }
  
  /**
   *  get number of entries
   */
  int getNumberOfEntries() {
    return entries.size();
  }

  class CacheEntry {
    int numberOfParagraph;
    final int startOfSentencePosition;
    final int nextSentencePosition;
    final SingleProofreadingError[] errorArray;

    CacheEntry(int numberOfParagraph, int startOfSentencePosition, int nextSentencePosition, SingleProofreadingError[] errorArray) {
      this.numberOfParagraph = numberOfParagraph;
      this.startOfSentencePosition = startOfSentencePosition;
      this.nextSentencePosition = nextSentencePosition;
      this.errorArray = errorArray;
    }
  }


} 
