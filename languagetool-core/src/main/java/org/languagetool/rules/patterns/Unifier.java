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

package org.languagetool.rules.patterns;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

/**
 * Implements unification of features over tokens.
 * 
 * @author Marcin Milkowski
 */
public class Unifier {

  private static final String UNIFY_IGNORE = "unify-ignore";
    
  private final List<AnalyzedTokenReadings> tokSequence;

  /**
   * List of all equivalences matched per tokens in the sequence, kept exactly
   * in sync with the list in tokSequence, so that a reading 2 of token 1 has its
   * equivalence map addressable as tokSequenceEquivalences.get(1).get(2).
   */
  private final List<List<Map<String, Set<String>>>> tokSequenceEquivalences;

  /**
   * A Map for storing the equivalence types for features. Features are
   * specified as Strings, and map into types defined as maps from Strings to
   * Elements.
   */
  private final Map<EquivalenceTypeLocator, PatternToken> equivalenceTypes;

  /**
   * A Map that stores all possible equivalence types listed for features.
   */
  private final Map<String, List<String>> equivalenceFeatures;

  /**
   * Map of sets of matched equivalences in the unified sequence.
   */
  private final List<Map<String, Set<String>>> equivalencesMatched;

  private boolean allFeatsIn;
  private int tokCnt;
  private int readingsCounter;

  // Marks found interpretations in subsequent tokens:
  private List<Boolean> featuresFound;

  // For checking the current token:
  private List<Boolean> tmpFeaturesFound;

  // Maps that store equivalences to be removed or kept after every next token has been analyzed:
  private final Map<String, Set<String>> equivalencesToBeKept;

  // stores uFeatures to keep the same signature of some methods...:
  private Map<String, List<String>> unificationFeats;

  private boolean inUnification;
  private boolean uniMatched;
  private boolean uniAllMatched;

  /**
   * Instantiates the unifier.
   */
  public Unifier(Map<EquivalenceTypeLocator, PatternToken> equivalenceTypes, Map<String, List<String>> equivalenceFeatures) {
    tokCnt = 0;
    readingsCounter = 1;
    equivalencesMatched = new ArrayList<>();
    this.equivalenceTypes = equivalenceTypes;
    this.equivalenceFeatures = equivalenceFeatures;
    equivalencesToBeKept = new ConcurrentHashMap<>();
    featuresFound = new ArrayList<>();
    tmpFeaturesFound = new ArrayList<>();
    tokSequence = new ArrayList<>();
    tokSequenceEquivalences = new ArrayList<>();
  }

  /**
   * Tests if a token has shared features with other tokens.
   * 
   * @param aToken token to be tested
   * @param uFeatures features to be tested
   * @return true if the token shares this type of feature with other tokens
   */
  protected final boolean isSatisfied(AnalyzedToken aToken,
      Map<String, List<String>> uFeatures) {

    if (allFeatsIn && equivalencesMatched.isEmpty()) {
      return false;
    }
    if (uFeatures == null) {
      throw new RuntimeException("isSatisfied called without features being set");
    }
    unificationFeats = uFeatures;

    boolean unified = true;
    if (allFeatsIn) {
      unified = checkNext(aToken, uFeatures);
    } else {
      while (equivalencesMatched.size() <= tokCnt) {
        equivalencesMatched.add(new ConcurrentHashMap<>());
      }
      for (Map.Entry<String, List<String>> feat : uFeatures.entrySet()) {
        List<String> types = feat.getValue();
        if (types == null || types.isEmpty()) {
          types = equivalenceFeatures.get(feat.getKey());
        }
        for (String typeName : types) {
          PatternToken testElem = equivalenceTypes
              .get(new EquivalenceTypeLocator(feat.getKey(), typeName));
          if (testElem == null) {
            return false;
          }
          if (testElem.isMatched(aToken)) {
            if (!equivalencesMatched.get(tokCnt).containsKey(feat.getKey())) {
              Set<String> typeSet = new HashSet<>();
              typeSet.add(typeName);
              equivalencesMatched.get(tokCnt).put(feat.getKey(), typeSet);
            } else {
              equivalencesMatched.get(tokCnt).get(feat.getKey()).add(typeName);
            }
          }
        }
        unified = equivalencesMatched.get(tokCnt).containsKey(feat.getKey());
        if (!unified) {
          equivalencesMatched.remove(tokCnt);
          break;
        }
      }
      if (unified) {
        if (tokCnt == 0 || tokSequence.isEmpty()) {
          tokSequence.add(new AnalyzedTokenReadings(aToken, 0));
          List<Map<String, Set<String>>> equivList = new ArrayList<>();
          equivList.add(equivalencesMatched.get(tokCnt));
          tokSequenceEquivalences.add(equivList);
        } else {
          tokSequence.get(0).addReading(aToken);
          tokSequenceEquivalences.get(0).add(equivalencesMatched.get(tokCnt));
        }
        tokCnt++;
      }
    }
    return unified;
  }

  private boolean checkNext(AnalyzedToken aToken,
                            Map<String, List<String>> uFeatures) {
    boolean anyFeatUnified = false;
    List<Boolean> tokenFeaturesFound = new ArrayList<>(tmpFeaturesFound);
    Map<String, Set<String>> equivalencesMatchedHere = new ConcurrentHashMap<>();
    if (allFeatsIn) {
      for (int i = 0; i < tokCnt; i++) {
        boolean allFeatsUnified = true;
        for (Map.Entry<String, List<String>> feat : uFeatures.entrySet()) {
          boolean featUnified = false;
          List<String> types = feat.getValue();
          if (types == null || types.isEmpty()) {
            types = equivalenceFeatures.get(feat.getKey());
          }
          for (String typeName : types) {
            if (equivalencesMatched.get(i).containsKey(feat.getKey())
                && equivalencesMatched.get(i).get(feat.getKey()).contains(typeName)) {
              PatternToken testElem = equivalenceTypes.get(new EquivalenceTypeLocator(feat.getKey(), typeName));
              boolean matched = testElem.isMatched(aToken);
              featUnified = featUnified || matched;
              //Stores equivalences to be kept
              if (matched) {
                if (!equivalencesToBeKept.containsKey(feat.getKey())) {
                  Set<String> typeSet = new HashSet<>();
                  typeSet.add(typeName);
                  equivalencesToBeKept.put(feat.getKey(), typeSet);
                } else {
                  equivalencesToBeKept.get(feat.getKey()).add(typeName);
                }
                if (!equivalencesMatchedHere.containsKey(feat.getKey())) { // just for this reading
                  Set<String> typeSet = new HashSet<>();
                  typeSet.add(typeName);
                  equivalencesMatchedHere.put(feat.getKey(), typeSet);
                } else {
                  equivalencesMatchedHere.get(feat.getKey()).add(typeName);
                }
              }
            }
          }
          allFeatsUnified &= featUnified;
        }
        tokenFeaturesFound.set(i, tokenFeaturesFound.get(i) || allFeatsUnified);
        anyFeatUnified = anyFeatUnified || allFeatsUnified;
      }
      if (anyFeatUnified) {
        if (tokSequence.size() == readingsCounter) {
          tokSequence.add(new AnalyzedTokenReadings(aToken, 0));
          List<Map<String, Set<String>>> equivList = new ArrayList<>();
          equivList.add(equivalencesMatchedHere);
          tokSequenceEquivalences.add(equivList);
        } else {
          if (readingsCounter < tokSequence.size()) {
            tokSequence.get(readingsCounter).addReading(aToken);
            tokSequenceEquivalences.get(readingsCounter).add(equivalencesMatchedHere);
          } else {
            anyFeatUnified = false;
          }
        }
        tmpFeaturesFound = tokenFeaturesFound;
      }
    }
    return anyFeatUnified;
  }

  /**
   * Call after every complete token (AnalyzedTokenReadings) checked.
   */
  public final void startNextToken() {
    featuresFound = new ArrayList<>(tmpFeaturesFound);
    readingsCounter++;
    // Removes features
    for (int j = 0; j < tokSequence.size(); j++) {
      for (int i = 0; i < tokSequenceEquivalences.get(j).size(); i++) {
        for (Map.Entry<String, List<String>> feat : equivalenceFeatures.entrySet()) {
          if (!UNIFY_IGNORE.equals(feat.getKey())) {
            if (tokSequenceEquivalences.get(j).get(i).containsKey(feat.getKey())) {
              if (equivalencesToBeKept.containsKey(feat.getKey())) {
                tokSequenceEquivalences.get(j).get(i).get(feat.getKey()).retainAll(equivalencesToBeKept.get(feat.getKey()));
              } else {
                tokSequenceEquivalences.get(j).get(i).remove(feat.getKey());
              }
            } else {
              tokSequenceEquivalences.get(j).get(i).remove(feat.getKey());
            }
          }
        }
      }
    }
    equivalencesToBeKept.clear();
  }

  /**
   * Starts testing only those equivalences that were previously matched.
   */
  public final void startUnify() {
    allFeatsIn = true;
    for (int i = 0; i < tokCnt; i++) {
      featuresFound.add(false);
    }
    tmpFeaturesFound = new ArrayList<>(featuresFound);
  }

  /**
   * Make sure that we really matched all the required features of the unification.
   * @param uFeatures Features to be checked
   * @return True if the token sequence has been found.
   * @since 2.5
   */
  public final boolean getFinalUnificationValue(Map<String, List<String>> uFeatures) {
    int tokUnified = 0;
    for (int j = 0; j < tokSequence.size(); j++) {
      boolean unifiedTokensFound = false; // assume that nothing has been found
      for (int i = 0; i < tokSequenceEquivalences.get(j).size(); i++) {
        int featUnified = 0;
        if (tokSequenceEquivalences.get(j).get(i).containsKey(UNIFY_IGNORE)) {
          if (i == 0) {
            tokUnified++;
          }
          unifiedTokensFound = true;
          continue;
        } else {
          for (Map.Entry<String, List<String>> feat : uFeatures.entrySet()) {
            if (tokSequenceEquivalences.get(j).get(i).containsKey(feat.getKey()) &&
                tokSequenceEquivalences.get(j).get(i).get(feat.getKey()).isEmpty()) {
              featUnified = 0;
            } else {
              featUnified++;
            }
            if (featUnified == unificationFeats.entrySet().size() && tokUnified <= j) {
              tokUnified++;
              unifiedTokensFound = true;
              break;
            }
          }
        }

      }
      if (!unifiedTokensFound) {
        return false;
      }
    }
    if (tokUnified == tokSequence.size()) {
      return true;
    }
    return false;
  }

  /**
   * Resets after use of unification. Required.
   */
  public final void reset() {
    equivalencesMatched.clear();
    allFeatsIn = false;
    tokCnt = 0;
    featuresFound.clear();
    tmpFeaturesFound.clear();
    tokSequence.clear();
    tokSequenceEquivalences.clear();
    readingsCounter = 1;
    uniMatched = false;
    uniAllMatched = false;
    inUnification = false;
  }

  /**
   * Gets a full sequence of filtered tokens.
   * @return Array of AnalyzedTokenReadings that match equivalence relation
   *         defined for features tested, or {@code null}
   */
  @Nullable
  public final AnalyzedTokenReadings[] getUnifiedTokens() {
    if (tokSequence.isEmpty()) {
      return null;
    }
    List<AnalyzedTokenReadings> uTokens = new ArrayList<>();
    for (int j = 0; j < tokSequence.size(); j++) {
      boolean unifiedTokensFound = false; // assume that nothing has been found
      for (int i = 0; i < tokSequenceEquivalences.get(j).size(); i++) {
        int featUnified = 0;
        if (tokSequenceEquivalences.get(j).get(i).containsKey(UNIFY_IGNORE)) {
          addTokenToSequence(uTokens, tokSequence.get(j).getAnalyzedToken(i), j);
          unifiedTokensFound = true;
        } else {
          for (Map.Entry<String, List<String>> feat : unificationFeats.entrySet()) {
            if (tokSequenceEquivalences.get(j).get(i).containsKey(feat.getKey()) &&
                    tokSequenceEquivalences.get(j).get(i).get(feat.getKey()).isEmpty()) {
              featUnified = 0;
            } else {
              featUnified++;
            }
            if (featUnified == unificationFeats.entrySet().size()) {
              addTokenToSequence(uTokens, tokSequence.get(j).getAnalyzedToken(i), j);
              unifiedTokensFound = true;
            }
          }
        }
      }
      if (!unifiedTokensFound) {
        return null;
      }
    }
    return uTokens.toArray(new AnalyzedTokenReadings[0]);
  }

  private void addTokenToSequence(List<AnalyzedTokenReadings> tokenSequence, AnalyzedToken token, int pos) {
    if (tokenSequence.size() <= pos || tokenSequence.isEmpty()) {
      AnalyzedTokenReadings tmpATR = new AnalyzedTokenReadings(token, 0);
      tokenSequence.add(tmpATR);
    } else {
      tokenSequence.get(pos).addReading(token);
    }
  }

  /**
   * Tests if the token sequence is unified.
   * 
   * <p>Usage note: to test if the sequence of tokens is unified (i.e.,
   * shares a group of features, such as the same gender, number,
   * grammatical case etc.), you need to test all tokens but the last one
   * in the following way: call {@code isUnified()} for every reading of a token,
   * and set {@code lastReading} to {@code true}. For the last token, check the
   * truth value returned by this method. In previous cases, it may actually be
   * discarded before the final check. See {@link AbstractPatternRule} for
   * an example.</p>
   * 
   * To make it work in XML rules, the Elements built based on {@code <token>}s inside
   * the unify block have to be processed in a special way: namely the last Element has to be
   * marked as the last one (by using {@link PatternToken#setLastInUnification}).
   * 
   * @param matchToken {@link AnalyzedToken} token to unify
   * @param lastReading true when the matchToken is the last reading in the {@link AnalyzedTokenReadings}
   * @param isMatched true if the reading matches the element in the pattern rule,
   *          otherwise the reading is not considered in the unification
   * @return true if the tokens in the sequence are unified
   */
  public final boolean isUnified(AnalyzedToken matchToken,
      Map<String, List<String>> uFeatures, boolean lastReading, boolean isMatched) {
    if (inUnification) {
      if (isMatched) {
        uniMatched |= isSatisfied(matchToken, uFeatures);
      }
      uniAllMatched = uniMatched;

      if (lastReading) {
        startNextToken();
        uniMatched = false;
      }
      return uniAllMatched && getFinalUnificationValue(uFeatures);
    } else {
      if (isMatched) {
        isSatisfied(matchToken, uFeatures);
      }
    }
    if (lastReading) {
      inUnification = true;
      uniMatched = false;
      startUnify();
    }
    return true;
  }

  public final boolean isUnified(AnalyzedToken matchToken,
      Map<String, List<String>> uFeatures, boolean lastReading) {
    return this.isUnified(matchToken, uFeatures, lastReading, true);
  }

  /**
   * Used to add neutral elements ({@link AnalyzedTokenReadings} to the
   * unified sequence. Useful if the sequence contains punctuation or connectives, for example.
   * @param analyzedTokenReadings A neutral element to be added.
   * @since 2.5
   */
  public final void addNeutralElement(AnalyzedTokenReadings analyzedTokenReadings) {
    tokSequence.add(analyzedTokenReadings);
    List<Map<String, Set<String>>> tokEquivs = new ArrayList<>(analyzedTokenReadings.getReadingsLength());
    Map<String, Set<String>> map = new ConcurrentHashMap<>();
    map.put(UNIFY_IGNORE, new HashSet<>());
    for (int i = 0; i < analyzedTokenReadings.getReadingsLength(); i++) {
      tokEquivs.add(map);
    }
    tokSequenceEquivalences.add(tokEquivs);
    readingsCounter++;
  }

  /**
   * Used for getting a unified sequence in case when simple test method
   * {@link #isUnified(AnalyzedToken, Map, boolean)}} was used.
   * @return An array of {@link AnalyzedTokenReadings} or {@code null} when not in unification
   */
  @Nullable
  public final AnalyzedTokenReadings[] getFinalUnified() {
    if (inUnification) {
      return getUnifiedTokens();
    }
    return null;
  }
}
