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
import de.danielnaber.languagetool.tools.StringTools;

/**
 * Implements unification of features over tokens.
 * 
 * @author Marcin Milkowski
 * 
 */
public class Unifier {

  private static final String FEATURE_TYPE_SEPARATOR = ":";

  private static final String FEATURE_SEPARATOR = ",";

  /*
   * Negates the meaning of unification just like negation in Element tokens.
   */
  private boolean negation;

  private boolean allFeatsIn;

  private int tokCnt;

  private int readingsCounter;

  private List<AnalyzedTokenReadings> tokSequence;

  /**
   * A Map for storing the equivalence types for features. Features are
   * specified as Strings, and map into types defined as maps from Strings to
   * Elements.
   */
  private Map<String, Element> equivalenceTypes;

  /**
   * A Map that stores all possible equivalence types listed for features.
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
   * Internal flag for checking whether the first token in tokSequence has to be
   * yet unified.
   */
  private boolean firstUnified;

  private boolean inUnification;
  private boolean uniMatched;
  AnalyzedTokenReadings[] unifiedTokens;

  public Unifier() {
    tokCnt = -1;
    readingsCounter = 1;
    equivalencesMatched = new ArrayList<Map<String, Set<String>>>();
    equivalenceTypes = new HashMap<String, Element>();
    equivalenceFeatures = new HashMap<String, List<String>>();
    featuresFound = new ArrayList<Boolean>();
    tmpFeaturesFound = new ArrayList<Boolean>();
    tokSequence = new ArrayList<AnalyzedTokenReadings>();
  }

  /**
   * Prepares equivalence types for features to be tested. All equivalence types
   * are given as {@link Elements}. They create an equivalence set (with
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
    if (equivalenceTypes.containsKey(feature + FEATURE_TYPE_SEPARATOR + type)) {
      return;
    }
    equivalenceTypes.put(feature + FEATURE_TYPE_SEPARATOR + type, elem);
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
   * 
   * @param aToken
   *          - token to be tested
   * @param feature
   *          - feature to be tested
   * @param type
   *          - type of equivalence relation for the feature
   * @return true if the token shares this type of feature with other tokens
   */
  public final boolean isSatisfied(final AnalyzedToken aToken,
      final String feature, final String type) {

    if (allFeatsIn && equivalencesMatched.isEmpty()) {
      return false;
    }
    // Error: no feature given!
    if (StringTools.isEmpty(feature)) {
      return false; // throw exception??
    }
    boolean unified = true;
    final String[] features = feature.trim().split(FEATURE_SEPARATOR);
    String[] types;

    if (!allFeatsIn) {
      tokCnt++;
      if (equivalencesMatched.size() <= tokCnt) {
        final Map<String, Set<String>> mapTemp = new HashMap<String, Set<String>>();
        equivalencesMatched.add(mapTemp);
      }
      for (final String feat : features) {
        if (StringTools.isEmpty(type)) {
          types = equivalenceFeatures.get(feat).toArray(
              new String[equivalenceFeatures.get(feat).size()]);
        } else {
          types = type.split(FEATURE_SEPARATOR);
        }
        for (final String typename : types) {
          final Element testElem = equivalenceTypes.get(feat
              + FEATURE_TYPE_SEPARATOR + typename);
          if (testElem == null) {
            return false;
          }
          if (testElem.isMatched(aToken)) {
            if (!equivalencesMatched.get(tokCnt).containsKey(feat)) {
              final Set<String> typeSet = new HashSet<String>();
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
          tokSequence.add(new AnalyzedTokenReadings(aToken));
        } else {
          tokSequence.get(0).addReading(aToken);
        }
      }
    } else {
      unified &= checkNext(aToken, features, type);
    }
    return unified ^ negation;
  }

  private boolean checkNext(final AnalyzedToken AT, final String[] features,
      final String type) {
    boolean unifiedNext = true;
    boolean anyFeatUnified = false;
    String[] types;
    if (allFeatsIn) {
      for (int i = 0; i <= tokCnt; i++) {
        boolean allFeatsUnified = true;
        for (final String feat : features) {
          boolean featUnified = false;
          if (StringTools.isEmpty(type)) {
            types = equivalenceFeatures.get(feat).toArray(
                new String[equivalenceFeatures.get(feat).size()]);
          } else {
            types = type.split(FEATURE_SEPARATOR);
          }
          for (final String typename : types) {
            if (featuresFound.get(i)
                && equivalencesMatched.get(i).containsKey(feat)
                && equivalencesMatched.get(i).get(feat).contains(typename)) {
              final Element testElem = equivalenceTypes.get(feat
                  + FEATURE_TYPE_SEPARATOR + typename);
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
    inUnification = false;
  }

  /**
   * Gets a full sequence of filtered tokens.
   * 
   * @return Array of AnalyzedTokenReadings that match equivalence relation
   *         defined for features tested.
   */
  public final AnalyzedTokenReadings[] getUnifiedTokens() {
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
      // FIXME: why this happens??
      if (first < tokSequence.get(0).getReadingsLength()) {
        tmpATR = new AnalyzedTokenReadings(tokSequence.get(0).getAnalyzedToken(
            first));
        for (int i = first + 1; i <= tokCnt; i++) {
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
   * @param matchToken
   *        AnalazydToken token to unify
   * @param feature
   *        String: feature to unify over
   * @param type
   *        String: value types of the feature 
   * @param isUniNegated
   *        if true, then return negated result
   * @param lastReading
   *        true when the matchToken is the last
   *        reading in the AnalyzedReadings 
   * @return
   */
  public boolean isUnified(final AnalyzedToken matchToken,
      final String feature, final String type, boolean isUniNegated,
      boolean lastReading) {
    if (inUnification) {
      uniMatched = uniMatched || isSatisfied(matchToken, feature, type);
      if (lastReading) {
        startNextToken();
        unifiedTokens = getUnifiedTokens();
      }
      return uniMatched;
    }
    if (isUniNegated) {
      setNegation(true);
    }
    isSatisfied(matchToken, feature, type);
    if (lastReading) {
      inUnification = true;
      uniMatched = false;
      startUnify();
    }
    return true;
  }

  public AnalyzedTokenReadings[] getFinalUnified() {
    if (inUnification) 
      return unifiedTokens;
    return null;
  }
}
