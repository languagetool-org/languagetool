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

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

import java.util.*;

/**
 * Implements unification of features over tokens.
 *
 * <h2>What unification computes</h2>
 *
 * <p>A unification runs over a sequence of tokens. Every token carries one or
 * more <em>readings</em> (morphological interpretations). A set of
 * <em>features</em> is checked (e.g. {@code number}, {@code gender}). Each
 * feature has a number of <em>type</em> values (e.g. {@code number} &rarr;
 * {@code singular}, {@code plural}); a reading matches a type when the
 * {@link PatternToken} configured for that {@code (feature, type)} pair matches
 * the reading.</p>
 *
 * <p>The sequence unifies when a single <em>combination</em> of type values (one
 * per feature) is shared by all tokens: for every token at least one reading has
 * to match all the chosen types simultaneously. In set terms, each token
 * contributes the set of feature-value combinations that some reading of it
 * supports, and the sequence unifies iff the intersection of these sets over all
 * (non-neutral) tokens is non-empty. A reading is kept in the unified output iff
 * it supports one of the surviving combinations. Readings are OR-ed within a
 * token, tokens are AND-ed. Neutral elements (punctuation, connectives added via
 * {@link #addNeutralElement}) never constrain the intersection and are always
 * kept.</p>
 *
 * <p>Tokens and their readings arrive incrementally through {@link #isSatisfied}
 * (or the higher-level {@link #isUnified}). The first token is collected up to
 * {@link #startUnify}; every following token is delimited by
 * {@link #startNextToken} (or, for the final token, by the query methods
 * themselves). This class therefore keeps the running intersection of shared
 * combinations up to date as tokens come in.</p>
 *
 * @author Marcin Milkowski
 */
public class Unifier {

  private static final String UNIFY_IGNORE = "unify-ignore";

  /** Configured matcher per {@code (feature, type)} pair. */
  private final Map<EquivalenceTypeLocator, PatternToken> equivalenceTypes;

  /** All type values known for each feature, used when a feature is queried with a blank type list. */
  private final Map<String, List<String>> equivalenceFeatures;

  /** Positions already delimited (the first token, plus any neutral element or token closed by {@link #startNextToken}). */
  private final List<Position> committed = new ArrayList<>();

  /** The token currently being read in (between two delimiters); {@code null} when no reading has been collected yet. */
  @Nullable
  private Position current;

  /**
   * Running intersection of shared feature-value combinations over the committed
   * non-neutral positions. A combination is a list of type names aligned to
   * {@link #featureOrder}. {@code null} until the first non-neutral position is
   * committed.
   */
  @Nullable
  private Set<List<String>> agreed;

  /** Stable feature ordering used to build combination tuples; derived from the queried features. */
  @Nullable
  private List<String> featureOrder;

  /** True once {@link #startUnify} has closed the first token. */
  private boolean allFeatsIn;

  // State for the higher-level isUnified() driver:
  private boolean inUnification;
  private boolean uniMatched;
  private boolean uniAllMatched;

  /** One token slot in the sequence: a normal token holds its matching readings, a neutral one is passed through verbatim. */
  private static final class Position {
    private final boolean neutral;
    private final List<AnalyzedToken> tokens = new ArrayList<>();
    /** Per reading, the set of matched type values for each feature. */
    private final List<Map<String, Set<String>>> matched = new ArrayList<>();
    @Nullable
    private final AnalyzedTokenReadings neutralReadings;

    Position(boolean neutral, @Nullable AnalyzedTokenReadings neutralReadings) {
      this.neutral = neutral;
      this.neutralReadings = neutralReadings;
    }

    void add(AnalyzedToken token, Map<String, Set<String>> matchedTypes) {
      tokens.add(token);
      matched.add(matchedTypes);
    }

    boolean isEmpty() {
      return tokens.isEmpty();
    }
  }

  public Unifier(Map<EquivalenceTypeLocator, PatternToken> equivalenceTypes, Map<String, List<String>> equivalenceFeatures) {
    this.equivalenceTypes = equivalenceTypes;
    this.equivalenceFeatures = equivalenceFeatures;
  }

  /**
   * Tests whether a reading shares features with the readings collected so far.
   *
   * <p>Before {@link #startUnify} this simply records the first token's readings
   * and reports whether the reading matches every queried feature. Afterwards it
   * reports whether the reading is compatible with a combination still shared by
   * all previous tokens, adding it to the current token when so.</p>
   *
   * @param aToken reading to test
   * @param uFeatures features to test
   * @return true if the reading is (still) part of a unified sequence
   */
  protected final boolean isSatisfied(AnalyzedToken aToken, Map<String, List<String>> uFeatures) {
    if (uFeatures == null) {
      throw new IllegalArgumentException("uFeatures must not be null");
    }
    setFeatures(uFeatures);

    if (allFeatsIn && (agreed == null || agreed.isEmpty())) {
      return false;
    }

    Map<String, Set<String>> matched = matchedTypes(aToken, uFeatures);
    boolean allFeaturesMatched = matched != null && noEmptyFeature(matched);

    if (!allFeatsIn) {
      // First token: collect every reading that matches all features.
      if (allFeaturesMatched) {
        openCurrent(false, null).add(aToken, matched);
      }
      return allFeaturesMatched;
    }

    // Subsequent token: open its slot (so a token that matches nothing still
    // narrows the intersection to empty on commit) and keep the reading only if
    // it supports a combination still shared by all previous tokens.
    Position token = openCurrent(false, null);
    boolean compatible = allFeaturesMatched && supportsAny(matched, agreed);
    if (compatible) {
      token.add(aToken, matched);
    }
    return compatible;
  }

  /**
   * Closes the first token and starts testing the following tokens against it.
   */
  public final void startUnify() {
    commitCurrent();
    allFeatsIn = true;
  }

  /**
   * Call after every complete token ({@link AnalyzedTokenReadings}) has been checked.
   */
  public final void startNextToken() {
    commitCurrent();
  }

  /**
   * Adds a neutral element (e.g. punctuation or a connective) to the unified
   * sequence. Neutral elements never constrain unification and are always kept.
   * @since 2.5
   */
  public final void addNeutralElement(AnalyzedTokenReadings analyzedTokenReadings) {
    commitCurrent();
    committed.add(new Position(true, analyzedTokenReadings));
  }

  /**
   * Makes sure all required features of the unification were really matched.
   * @param uFeatures features to be checked
   * @return true if a unified token sequence was found
   * @since 2.5
   */
  public final boolean getFinalUnificationValue(Map<String, List<String>> uFeatures) {
    setFeatures(uFeatures);
    Set<List<String>> shared = finalAgreed();
    return !shared.isEmpty() && hasNonNeutral();
  }

  /**
   * Gets the full sequence of filtered tokens.
   * @return readings that satisfy the unification, or {@code null} if the sequence does not unify
   */
  @Nullable
  public final AnalyzedTokenReadings[] getUnifiedTokens() {
    List<Position> sequence = orderedPositions();
    if (sequence.isEmpty()) {
      return null;
    }
    Set<List<String>> shared = finalAgreed();
    List<AnalyzedTokenReadings> result = new ArrayList<>();
    for (Position position : sequence) {
      AnalyzedTokenReadings atr = unify(position, shared);
      if (atr == null) {
        return null;
      }
      result.add(atr);
    }
    return result.toArray(new AnalyzedTokenReadings[0]);
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

  /**
   * Resets after use of unification. Required.
   */
  public final void reset() {
    committed.clear();
    current = null;
    agreed = null;
    featureOrder = null;
    allFeatsIn = false;
    inUnification = false;
    uniMatched = false;
    uniAllMatched = false;
  }

  // ---------------------------------------------------------------------------
  // Internal helpers
  // ---------------------------------------------------------------------------

  private void setFeatures(Map<String, List<String>> uFeatures) {
    if (featureOrder == null) {
      featureOrder = new ArrayList<>(uFeatures.keySet());
    }
  }

  /**
   * Computes, per queried feature, the set of type values the reading matches.
   * @return the per-feature matches, or {@code null} if a queried type has no configured matcher
   */
  @Nullable
  private Map<String, Set<String>> matchedTypes(AnalyzedToken aToken, Map<String, List<String>> uFeatures) {
    Map<String, Set<String>> matched = new HashMap<>();
    for (Map.Entry<String, List<String>> feat : uFeatures.entrySet()) {
      List<String> types = feat.getValue();
      if (types == null || types.isEmpty()) {
        types = equivalenceFeatures.get(feat.getKey());
      }
      Set<String> matchedForFeat = new HashSet<>();
      for (String typeName : types) {
        PatternToken testElem = equivalenceTypes.get(new EquivalenceTypeLocator(feat.getKey(), typeName));
        if (testElem == null) {
          return null;
        }
        if (testElem.isMatched(aToken)) {
          matchedForFeat.add(typeName);
        }
      }
      matched.put(feat.getKey(), matchedForFeat);
    }
    return matched;
  }

  private static boolean noEmptyFeature(Map<String, Set<String>> matched) {
    for (Set<String> types : matched.values()) {
      if (types.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  private Position openCurrent(boolean neutral, @Nullable AnalyzedTokenReadings neutralReadings) {
    if (current == null) {
      current = new Position(neutral, neutralReadings);
    }
    return current;
  }

  /** Commits the current token (if any reading was collected for it) and folds it into the running intersection. */
  private void commitCurrent() {
    if (current == null) {
      return;
    }
    intersect(current);
    committed.add(current);
    current = null;
  }

  /** Narrows {@link #agreed} to the combinations still supported by the given non-neutral position. */
  private void intersect(Position position) {
    if (position.neutral) {
      return;
    }
    if (agreed == null) {
      agreed = combinationsOf(position);
    } else {
      agreed.removeIf(combination -> !supports(position, combination));
    }
  }

  /**
   * Returns the intersection over all positions <em>including</em> the token still
   * being read in, without mutating the running state (query methods may be called
   * mid-token).
   */
  private Set<List<String>> finalAgreed() {
    if (current == null || current.neutral) {
      return agreed == null ? Collections.emptySet() : agreed;
    }
    // A fed-but-empty current is a token that matched nothing: it must narrow the
    // intersection to empty rather than be ignored.
    if (agreed == null) {
      return combinationsOf(current);
    }
    Set<List<String>> shared = new LinkedHashSet<>(agreed);
    shared.removeIf(combination -> !supports(current, combination));
    return shared;
  }

  private boolean hasNonNeutral() {
    if (current != null && !current.neutral && !current.isEmpty()) {
      return true;
    }
    for (Position position : committed) {
      if (!position.neutral) {
        return true;
      }
    }
    return false;
  }

  private List<Position> orderedPositions() {
    if (current == null) {
      return committed;
    }
    List<Position> all = new ArrayList<>(committed);
    all.add(current);
    return all;
  }

  /** All feature-value combinations supported by at least one reading of the position. */
  private Set<List<String>> combinationsOf(Position position) {
    Set<List<String>> combinations = new LinkedHashSet<>();
    for (Map<String, Set<String>> matched : position.matched) {
      addCombinations(matched, 0, new ArrayList<>(), combinations);
    }
    return combinations;
  }

  private void addCombinations(Map<String, Set<String>> matched, int featureIdx,
      List<String> prefix, Set<List<String>> out) {
    if (featureIdx == featureOrder.size()) {
      out.add(new ArrayList<>(prefix));
      return;
    }
    for (String type : matched.get(featureOrder.get(featureIdx))) {
      prefix.add(type);
      addCombinations(matched, featureIdx + 1, prefix, out);
      prefix.remove(prefix.size() - 1);
    }
  }

  /** True if some reading of the position supports the combination. */
  private boolean supports(Position position, List<String> combination) {
    for (Map<String, Set<String>> matched : position.matched) {
      if (supports(matched, combination)) {
        return true;
      }
    }
    return false;
  }

  /** True if the reading's matches cover the combination (one type per feature). */
  private boolean supports(Map<String, Set<String>> matched, List<String> combination) {
    for (int i = 0; i < featureOrder.size(); i++) {
      if (!matched.get(featureOrder.get(i)).contains(combination.get(i))) {
        return false;
      }
    }
    return true;
  }

  /** True if the reading supports at least one of the given combinations. */
  private boolean supportsAny(Map<String, Set<String>> matched, @Nullable Set<List<String>> combinations) {
    if (combinations == null) {
      return false;
    }
    for (List<String> combination : combinations) {
      if (supports(matched, combination)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Builds the {@link AnalyzedTokenReadings} for one position, keeping the readings
   * that support a surviving combination. Neutral positions are passed through whole.
   * @return the readings, or {@code null} if a non-neutral position keeps nothing
   */
  @Nullable
  private AnalyzedTokenReadings unify(Position position, Set<List<String>> shared) {
    if (position.neutral) {
      AnalyzedTokenReadings neutral = position.neutralReadings;
      AnalyzedTokenReadings atr = null;
      for (int i = 0; i < neutral.getReadingsLength(); i++) {
        atr = append(atr, neutral.getAnalyzedToken(i));
      }
      return atr;
    }
    AnalyzedTokenReadings atr = null;
    for (int i = 0; i < position.tokens.size(); i++) {
      if (supportsAny(position.matched.get(i), shared)) {
        atr = append(atr, position.tokens.get(i));
      }
    }
    return atr;
  }

  private static AnalyzedTokenReadings append(@Nullable AnalyzedTokenReadings atr, AnalyzedToken token) {
    if (atr == null) {
      return new AnalyzedTokenReadings(token, 0);
    }
    atr.addReading(token, "");
    return atr;
  }
}
