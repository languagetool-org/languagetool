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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;

/**
 * Implements unification of features over tokens.
 * 
 * @author Marcin Milkowski
 */
public class Unifier {

  //TODO: add a possibility to negate some features but not all
  /**
   * Negates the meaning of unification just like negation in Element tokens.
   */  
  private boolean negation;

  private boolean allFeatsIn;

  private int tokCnt;

  private int readingsCounter;

  private final List<AnalyzedTokenReadings> tokSequence;

  /**
   * A Map for storing the equivalence types for features. Features are
   * specified as Strings, and map into types defined as maps from Strings to
   * Elements.
   */
  private final Map<EquivalenceTypeLocator, Element> equivalenceTypes;

  /**
   * A Map that stores all possible equivalence types listed for features.
   */
  private final Map<String, List<String>> equivalenceFeatures;

  /**
   * Map of sets of matched equivalences in the unified sequence.
   */
  private final List<Map<String, Set<String>>> equivalencesMatched;

  /**
   * Marks found interpretations in subsequent tokens.
   */
  private List<Boolean> featuresFound;

  /**
   * For checking the current token.
   */
  private List<Boolean> tmpFeaturesFound;

  /**
   * Internal flag for checking whether the first token in tokSequence has to be
   * yet unified.
   */
  private boolean firstUnified;

  private boolean inUnification;
  private boolean uniMatched;
  private boolean uniAllMatched;
  private AnalyzedTokenReadings[] unifiedTokens;

  /**
   * Instantiates the unifier.
   */
  public Unifier() {
    tokCnt = -1;
    readingsCounter = 1;
    equivalencesMatched = new ArrayList<Map<String, Set<String>>>();
    equivalenceTypes = new HashMap<EquivalenceTypeLocator, Element>();
    equivalenceFeatures = new HashMap<String, List<String>>();
    featuresFound = new ArrayList<Boolean>();
    tmpFeaturesFound = new ArrayList<Boolean>();
    tokSequence = new ArrayList<AnalyzedTokenReadings>();
  }

  /**
   * Prepares equivalence types for features to be tested. All equivalence types
   * are given as {@link Element}s. They create an equivalence set (with
   * abstraction).
   * 
   * @param feature
   *          Feature to be tested, like gender, grammatical case or number.
   * @param type
   *          Type of equivalence for the feature, for example plural, first
   *          person, genitive.
   * @param elem
   *          Element specifying the equivalence.
   */
  public final void setEquivalence(final String feature, final String type,
      final Element elem) {
    if (equivalenceTypes.containsKey(new EquivalenceTypeLocator(feature, type))) {
      return;
    }
    equivalenceTypes.put(new EquivalenceTypeLocator(feature, type), elem);
    final List<String> lTypes;
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
   * 
   * @param aToken
   *          - token to be tested
   * @param uFeatures
   *          - features to be tested
   * @return true if the token shares this type of feature with other tokens
   */
  protected final boolean isSatisfied(final AnalyzedToken aToken,
      final Map<String, List<String>> uFeatures) {

    if (allFeatsIn && equivalencesMatched.isEmpty()) {
      return false;
    }
    // Error: no feature given!
    if (uFeatures == null) {
      return false; // throw exception??
    }
    boolean unified = true;
    List<String> types;

    if (allFeatsIn) {
      unified &= checkNext(aToken, uFeatures);      
    } else {
      tokCnt++;
      while (equivalencesMatched.size() <= tokCnt) {
        equivalencesMatched.add(new HashMap<String, Set<String>>());
      }      
      for (final Map.Entry<String, List<String>> feat : uFeatures.entrySet()) {        
        types = feat.getValue();
        if (types == null || types.isEmpty()) {
          types = equivalenceFeatures.get(feat.getKey());
        }
        for (final String typeName : types) {
          final Element testElem = equivalenceTypes
          .get(new EquivalenceTypeLocator(feat.getKey(), typeName));
          if (testElem == null) {
            return false;
          }
          if (testElem.isMatched(aToken)) {
            if (!equivalencesMatched.get(tokCnt).containsKey(feat.getKey())) {
              final Set<String> typeSet = new HashSet<String>();
              typeSet.add(typeName);
              equivalencesMatched.get(tokCnt).put(feat.getKey(), typeSet);
            } else {
              equivalencesMatched.get(tokCnt).get(feat.getKey()).add(typeName);
            }
          }
        }
        unified &= equivalencesMatched.get(tokCnt).containsKey(feat.getKey());
        if (!unified) {
          break;
        }
      }
      if (unified) {
        if (tokCnt == 0 || tokSequence.isEmpty()) {
          tokSequence.add(new AnalyzedTokenReadings(aToken, 0));
        } else {
          tokSequence.get(0).addReading(aToken);
        }
      }
    }
    return unified ^ negation;
  }

  private boolean checkNext(final AnalyzedToken aToken,
      final Map<String, List<String>> uFeatures) {
    boolean unifiedNext = true;
    boolean anyFeatUnified = false;    
    List<String> types;
    final ArrayList<Boolean> tokenFeaturesFound = new ArrayList<Boolean>(tmpFeaturesFound);
    if (allFeatsIn) {
      for (int i = 0; i <= tokCnt; i++) {
        boolean allFeatsUnified = true;
        for (Map.Entry<String, List<String>> feat : uFeatures.entrySet()) {
          boolean featUnified = false;
          types = feat.getValue();
          if (types == null || types.isEmpty()) {
            types = equivalenceFeatures.get(feat.getKey());
          }
          for (final String typeName : types) {
            if (featuresFound.get(i)
                && equivalencesMatched.get(i).containsKey(feat.getKey())
                && equivalencesMatched.get(i).get(feat.getKey()).contains(typeName)) {
              final Element testElem = equivalenceTypes
              .get(new EquivalenceTypeLocator(feat.getKey(), typeName));
              featUnified = featUnified || testElem.isMatched(aToken);
            }
          }
          allFeatsUnified &= featUnified;
        }
        tokenFeaturesFound.set(i, allFeatsUnified);
        anyFeatUnified = anyFeatUnified || allFeatsUnified;
      }
      unifiedNext &= anyFeatUnified;
      if (unifiedNext) {
        if (tokSequence.size() == readingsCounter) {
          tokSequence.add(new AnalyzedTokenReadings(aToken, 0));
        } else {
          tokSequence.get(readingsCounter).addReading(aToken);
        }
        tmpFeaturesFound = tokenFeaturesFound;
      }
    }
    return unifiedNext;
  }

  /**
   * Call after every complete token (AnalyzedTokenReadings) checked.
   */
  public final void startNextToken() {
    featuresFound = new ArrayList<Boolean>(tmpFeaturesFound);    
    readingsCounter++;
  }

  /**
   * Starts testing only those equivalences that were previously matched.
   */
  public final void startUnify() {
    allFeatsIn = true;
    for (int i = 0; i <= tokCnt; i++) {
      featuresFound.add(true);
    } 
    tmpFeaturesFound = new ArrayList<Boolean>(featuresFound);
  }

  public final void setNegation(final boolean neg) {
    negation = neg;
  }

  public final boolean getNegation() {
    return negation;
  }
  
  /**
   * Resets after use of unification. Required.
   */
  public final void reset() {
    equivalencesMatched.clear();
    allFeatsIn = false;
    negation = false;
    tokCnt = -1;
    featuresFound.clear();
    tmpFeaturesFound.clear();
    tokSequence.clear();
    readingsCounter = 1;
    firstUnified = false;
    uniMatched = false;
    uniAllMatched = false;
    inUnification = false;
  }

  /**
   * Gets a full sequence of filtered tokens.
   * 
   * @return Array of AnalyzedTokenReadings that match equivalence relation
   *         defined for features tested.
   */
  public final AnalyzedTokenReadings[] getUnifiedTokens() {
    if (tokSequence.isEmpty()) {
      return null;
    }
    if (!firstUnified) {
      final AnalyzedTokenReadings tmpATR;
      int first = 0;
      tmpFeaturesFound.add(true); // Bentley's search idea
      while (!tmpFeaturesFound.get(first)) {
        first++;
      }
      tmpFeaturesFound.remove(tmpFeaturesFound.size() - 1);
      if (first >= tmpFeaturesFound.size()) {
        return null;
      }
      // FIXME: why this happens??
      final int numRead = tokSequence.get(0).getReadingsLength(); 
      if (first < numRead) {
        tmpATR = new AnalyzedTokenReadings(tokSequence.get(0).getAnalyzedToken(
            first), 0);
        for (int i = first + 1; i <= Math.min(numRead - 1, tokCnt); i++) {
          if (tmpFeaturesFound.get(i)) {
            tmpATR.addReading(tokSequence.get(0).getAnalyzedToken(i));
          }
        }
        tokSequence.set(0, tmpATR);
      }
      firstUnified = true;
    }
    final AnalyzedTokenReadings[] atr = tokSequence
    .toArray(new AnalyzedTokenReadings[tokSequence.size()]);
    return atr;
  }

  /**
   * Tests if the token sequence is unified.
   * 
   * @param matchToken
   *          AnalyzedToken token to unify
   * @param isUniNegated
   *          if true, then return negated result
   * @param lastReading
   *          true when the matchToken is the last reading in the
   *          AnalyzedReadings
   * @return True if the tokens in the sequence are unified.
   */
  public final boolean isUnified(final AnalyzedToken matchToken,
      final Map<String, List<String>> uFeatures, final boolean isUniNegated,
      final boolean lastReading) {
    if (inUnification) {      
      uniMatched |= isSatisfied(matchToken, uFeatures);
      uniAllMatched = uniMatched;
      if (lastReading) {
        startNextToken();
        unifiedTokens = getUnifiedTokens();
        uniMatched = false;
      }
      return uniAllMatched;
    }
    if (isUniNegated) {
      setNegation(true);
    }
    isSatisfied(matchToken, uFeatures);
    if (lastReading) {
      inUnification = true;
      uniMatched = false;
      startUnify();
    }
    return true;
  }

  /**
   * Used for getting a unified sequence in case when simple test method
   * {@link #isUnified(de.danielnaber.languagetool.AnalyzedToken, java.util.Map, boolean, boolean)}} was used.
   * 
   * @return An array of {@link AnalyzedTokenReadings}
   */
  public final AnalyzedTokenReadings[] getFinalUnified() {
    if (inUnification) {
      return unifiedTokens;
    }
    return null;
  }
}

class EquivalenceTypeLocator {

  private final String feature;
  private final String type;

  EquivalenceTypeLocator(final String feature, final String type) {
    this.feature = feature;
    this.type = type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((feature == null) ? 0 : feature.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final EquivalenceTypeLocator other = (EquivalenceTypeLocator) obj;
    if (feature == null) {
      if (other.feature != null) {
        return false;
      }
    } else if (!feature.equals(other.feature)) {
      return false;
    }
    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!type.equals(other.type)) {
      return false;
    }
    return true;
  }

}