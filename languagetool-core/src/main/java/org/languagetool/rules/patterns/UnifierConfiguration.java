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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  private final Map<EquivalenceTypeLocator, Element> equivalenceTypes;

  /**
   * A Map that stores all possible equivalence types listed for features.
   */
  private final Map<String, List<String>> equivalenceFeatures;

  public UnifierConfiguration() {
    equivalenceTypes = new HashMap<>();
    equivalenceFeatures = new HashMap<>();
  }

  /**
   * Prepares equivalence types for features to be tested. All equivalence
   * types are given as {@link Element}s. They create an equivalence set (with
   * abstraction).
   *
   * @param feature Feature to be tested, like gender, grammatical case or number.
   * @param type Type of equivalence for the feature, for example plural, first person, genitive.
   * @param elem Element specifying the equivalence.
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
      lTypes = new ArrayList<>();
    }
    lTypes.add(type);
    equivalenceFeatures.put(feature, lTypes);
  }

  public Map<String, List<String>> getEquivalenceFeatures() {
    return equivalenceFeatures;
  }

  public Map<EquivalenceTypeLocator, Element> getEquivalenceTypes() {
    return equivalenceTypes;
  }

  public Unifier createUnifier() {
    return new Unifier(equivalenceTypes, equivalenceFeatures);
  }
}
