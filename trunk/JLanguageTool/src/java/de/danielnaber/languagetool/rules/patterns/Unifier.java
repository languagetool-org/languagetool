/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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

package de.danielnaber.languagetool.rules.patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;


/**
 * Implements unification of features over tokens.
 * 
 * @author Marcin Milkowski
 *
 */
public class Unifier {

  /*
   * Negates the meaning of unification
   * just like negation in Element tokens.
   */
  private boolean negation = false;

  private boolean allFeatsIn = false;

  private int tokCnt = -1;

  private int readingsCounter = 1;

  private List<AnalyzedTokenReadings> tokSequence;

  /**
   * A Map for storing the equivalence types for
   * features. Features are specified as Strings,
   * and map into types defined as maps from Strings
   * to Elements.
   */
  private Map<String, Element> equivalenceTypes;

  /**
   * A Map that stores all possible equivalence types
   * listed for features. 
   */
  private Map<String, List<String>> equivalenceFeatures;

  /**
   * Map of sets of matched equivalences in the unified sequence.
   */
  private List<Map<String, Set<String>>> equivalencesMatched;

  /**
   * Marks found interpretations in subsequent tokens.
   */
  private List<Boolean> featuresFound;

  /**
   * For checking the current token.
   */
  private List<Boolean> tmpFeaturesFound;
  
  /**
   * internal flag for checking whether the first
   * token in tokSequence has to be yet unified
   */
  private boolean firstUnified = false;

  public Unifier() {
    clear();
  }

  /**
   * Prepares equivalence types for features to be tested.
   * All equivalence types are given as {@link Elements}.
   * They create an equivalence set (with abstraction).
   * @param feature Feature to be tested, like
   * gender, grammatical case or number. 
   * @param type Type of equivalence for the feature,
   * for example plural, first person, genitive.
   * @param elem Element specifying the equivalence.
   */
  public void setEquivalence(final String feature, final String type,
      final Element elem) {
    if (equivalenceTypes.containsKey(feature + ":" + type)) {
      // shouldn't happen, the throw exception?
    }        
    equivalenceTypes.put(feature + ":" + type, elem);
    List<String> lTypes;
    if (equivalenceFeatures.containsKey(feature)) {
      lTypes = equivalenceFeatures.get(feature);
    } else {
      lTypes = new ArrayList<String>();
    }
    lTypes.add(type);
    equivalenceFeatures.put(feature, lTypes);
  }

  /**
   * Tests if a token has shared features with other tokens.
   * @param AT - token to be tested
   * @param feature - feature to be tested
   * @param type - type of equivalence relation for the feature
   * @return true if the token shares this type of feature with other tokens
   */
  public boolean isSatisfied(final AnalyzedToken AT, final String feature,
      final String type) {

    if (allFeatsIn && equivalencesMatched.isEmpty()) {
      return false;
    }            
    //Error: no feature given!
    if ("".equals(feature)) {
      return false; //throw exception??
    }
    boolean unified = true;
    final String features[] = feature.split(",");
    String types[];    

    if (!allFeatsIn) {
      tokCnt++;
      if (equivalencesMatched.size() <= tokCnt) {
        Map<String, Set<String>> mapTemp = 
          new HashMap<String, Set<String>>();
        equivalencesMatched.add(mapTemp);
      }      
      for (String feat : features) {
        if ("".equals(type)) {
          types = equivalenceFeatures.get(feat).toArray(
              new String[equivalenceFeatures.get(feat).size()]);
        } else {
          types = type.split(",");
        }
        for (String typename : types) {        
          Element testElem = equivalenceTypes.get(feat + ":" + typename);
          if (testElem == null) {
            return false;
          }
          if (testElem.isMatched(AT)) {            
            if (!equivalencesMatched.get(tokCnt).containsKey(feat)) {
              Set<String> typeSet = new HashSet<String>();
              typeSet.add(typename);
              equivalencesMatched.get(tokCnt).put(feat, typeSet);
            } else {
              equivalencesMatched.get(tokCnt).get(feat).add(typename);
            }            
          }
        } 
        unified &= equivalencesMatched.get(tokCnt).containsKey(feat);
        if (!unified) {           
          break;  
        }        
      }
      if (unified) {
        if (tokCnt == 0 || tokSequence.isEmpty()) {
          tokSequence.add(new AnalyzedTokenReadings(AT));
        } else {
          tokSequence.get(0).addReading(AT);
        }    
      }
    } else {        
      unified &= checkNext(AT, features, type);
    }          
    return (unified) ^ negation;
  }

  private boolean checkNext(final AnalyzedToken AT, final String features[],
      final String type) {      
    boolean unifiedNext = true;
    boolean anyFeatUnified = false;
    String types[];        
    if (allFeatsIn) {
      for (int i = 0; i <= tokCnt; i++) {      
        boolean allFeatsUnified = true;
        for (String feat : features) {
          boolean featUnified = false;
          if ("".equals(type)) {
            types = equivalenceFeatures.get(feat).toArray(
                new String[equivalenceFeatures.get(feat).size()]);
          } else {
            types = type.split(",");
          }
          for (String typename : types) {                            
            if (featuresFound.get(i)
                && equivalencesMatched.get(i).containsKey(feat)
                && equivalencesMatched.get(i).get(feat).contains(typename)) {
              Element testElem = equivalenceTypes.get(feat + ":" + typename);
              featUnified = featUnified || testElem.isMatched(AT);
            }        
          }
          allFeatsUnified &= featUnified;          
        }
        tmpFeaturesFound.set(i, allFeatsUnified);
        anyFeatUnified |= allFeatsUnified;
      }
      unifiedNext &= anyFeatUnified;
      if (unifiedNext) {
        if (tokSequence.size() == readingsCounter) {
          tokSequence.add(new AnalyzedTokenReadings(AT));
        } else {
          tokSequence.get(readingsCounter).addReading(AT);
        }          
      }
    }
    return unifiedNext;
  }

  /**
   * Call after every complete token (AnalyzedTokenReadings) checked.
   */
  public void startNextToken() {
    featuresFound = new ArrayList<Boolean>(tmpFeaturesFound);
    readingsCounter++;
  }

  /**
   * Starts testing only those equivalences
   * that were previously matched.
   */
  public void startUnify() {
    allFeatsIn = true;
    for (int i = 0; i <= tokCnt; i++) {
      featuresFound.add(true);
    }
    tmpFeaturesFound = new ArrayList<Boolean>(featuresFound);
  }

  public void setNegation(final boolean neg) {
    negation = neg;
  }

  public boolean getNegation() {
    return negation;
  }

  /**
   * Resets after use of unification. Required.
   */
  public void reset() {
    equivalencesMatched.clear();
    allFeatsIn = false;
    negation = false;
    tokCnt = -1;
    featuresFound.clear();
    tmpFeaturesFound.clear();
    tokSequence.clear();
    readingsCounter = 1;
    firstUnified = false;
  }

  public void clear() {
    equivalencesMatched = new ArrayList<Map<String, Set<String>>>();
    equivalenceTypes = new HashMap<String, Element>();
    equivalenceFeatures = new HashMap<String, List<String>>();
    featuresFound = new ArrayList<Boolean>();
    tmpFeaturesFound = new ArrayList<Boolean>();
    tokSequence = new ArrayList<AnalyzedTokenReadings>();    
  }

  /**
   * Gets a full sequence of filtered tokens. 
   * @return Array of AnalyzedTokenReadings that match 
   * equivalence relation defined for features
   * tested.
   */
  public AnalyzedTokenReadings[] getUnifiedTokens() {
    if (!firstUnified) {
    AnalyzedTokenReadings tmpATR;
    int first = -1;
    for (int i = 0; i <= tokCnt; i++) {
      if (tmpFeaturesFound.get(i)) {
        first = i;
      }
    }
    if (first == -1) {
      return null;
    }
    //FIXME: why this happens??
    if (first < tokSequence.get(0).getReadingsLength()) {
    tmpATR = new AnalyzedTokenReadings(
        tokSequence.get(0).getAnalyzedToken(first));           
    for (int i = first + 1; i <= tokCnt; i++) {
      if (tmpFeaturesFound.get(i)) {      
        tmpATR.addReading(tokSequence.get(0).getAnalyzedToken(i));          
      }
    }
    //List<AnalyzedTokenReadings> tempSeq = new ArrayList<AnalyzedTokenReadings>(tokSequence);
    //tempSeq.set(0, tmpATR);
    tokSequence.set(0, tmpATR);
    }
    firstUnified = true;
    }
    AnalyzedTokenReadings[] atr = 
      //tempSeq.toArray(new AnalyzedTokenReadings[tempSeq.size()]);          
      tokSequence.toArray(new AnalyzedTokenReadings[tokSequence.size()]);
    return atr;
  } 

}