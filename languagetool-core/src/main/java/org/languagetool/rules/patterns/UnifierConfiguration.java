/* LanguageTool, a natural language style checker
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
 * Copyright (C) 2013 Stefan Lotties
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Stefan Lotties
 * @since 2.3
 */
public class UnifierConfiguration {
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

  public UnifierConfiguration() {
    // workaround for issue #13
    equivalenceTypes = new ConcurrentHashMap<>();
    equivalenceFeatures = new ConcurrentHashMap<>();
  }

  /**
   * Prepares equivalence types for features to be tested. All equivalence
   * types are given as {@link PatternToken}s. They create an equivalence set (with
   * abstraction).
   *
   * @param feature Feature to be tested, like gender, grammatical case or number.
   * @param type Type of equivalence for the feature, for example plural, first person, genitive.
   * @param elem Element specifying the equivalence.
   */
  public final void setEquivalence(String feature, String type,
                                   PatternToken elem) {

    EquivalenceTypeLocator typeKey = new EquivalenceTypeLocator(feature, type);
    if (equivalenceTypes.containsKey(typeKey)) {
      return;
    }
    equivalenceTypes.put(typeKey, elem);
    
    List<String> lTypes;
    if (equivalenceFeatures.containsKey(feature)) {
      lTypes = equivalenceFeatures.get(feature);
    } else {
      // workaround for issue #13
      lTypes = new CopyOnWriteArrayList<>();
      equivalenceFeatures.put(feature, lTypes);
    }
    lTypes.add(type);
  }

  public Map<EquivalenceTypeLocator, PatternToken> getEquivalenceTypes() {
    return Collections.unmodifiableMap(equivalenceTypes);
  }

  public Map<String, List<String>> getEquivalenceFeatures() {
    return Collections.unmodifiableMap(equivalenceFeatures);
  }

  public Unifier createUnifier() {
    return new Unifier(getEquivalenceTypes(), getEquivalenceFeatures());
  }
}
