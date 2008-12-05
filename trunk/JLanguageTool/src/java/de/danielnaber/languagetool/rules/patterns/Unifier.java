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

  private List<AnalyzedToken> tokSequence;

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
   * Set of matched features in the unified sequence.
   */
  private Set<String> equivalencesMatched;

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
    //lazy init
    if (equivalenceTypes==null) {
      
    }

    if (equivalenceFeatures==null) {
      
    }

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

    String indexStr;
    boolean unified = false;
    final String features[] = feature.split(",");
    String types[];    

    for (String feat : features) {
      if ("".equals(type)) {
        types = equivalenceFeatures.get(feat).toArray(
            new String[equivalenceFeatures.get(feat).size()]);
      } else {
        types = type.split(",");
      }
      for (String t : types) {
        indexStr = feat + ":" + t;
        Element testElem = (Element) equivalenceTypes.get(indexStr);
        if (testElem == null) {
          return false;
        }
        if (!allFeatsIn) {        
          if (testElem.isMatched(AT)) {                       
            equivalencesMatched.add(indexStr);
            if (tokSequence == null) {
              tokSequence = new ArrayList<AnalyzedToken>();        
            }
            tokSequence.add(AT);           
          }
          unified |= equivalencesMatched.contains(indexStr); 
        } else {
          if (equivalencesMatched.contains(indexStr)) {
            if (testElem.isMatched(AT)) {                       
              equivalencesMatched.add(indexStr);
              if (tokSequence == null) {
                tokSequence = new ArrayList<AnalyzedToken>();        
              }
              tokSequence.add(AT);
            } else {
              if (equivalencesMatched.contains(indexStr)) {
                equivalencesMatched.remove(indexStr);
              }
            }
            unified |= equivalencesMatched.contains(indexStr);
          }
        }
      }
    }
    return unified ^ negation;
  }

  /**
   * Starts testing only those equivalences
   * that were previously matched.
   */
  public void startUnify() {
    allFeatsIn = true;
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
    if (equivalencesMatched != null) {
      equivalencesMatched.clear();
    }
    allFeatsIn = false;
    negation = false;
  }

  public void clear() {
    equivalencesMatched = new HashSet<String>();
    equivalenceTypes = new HashMap<String, Element>();
    equivalenceFeatures = new HashMap<String, List<String>>(); 
  }
  
  /**
   * Gets a filtered token. Call after every
   * full iteration on AnalyzedTokenReadings.
   * @return AnalyzedTokenReadings that match 
   * equivalence relation defined for features
   * tested.
   */
  public AnalyzedTokenReadings getUnifiedToken() {
    if (tokSequence != null) {
      AnalyzedTokenReadings atr = new AnalyzedTokenReadings(
          tokSequence.toArray(new AnalyzedToken[tokSequence.size()]));
      tokSequence.clear();    
      return atr;
    } else {
      return null;
    }
  } 

}