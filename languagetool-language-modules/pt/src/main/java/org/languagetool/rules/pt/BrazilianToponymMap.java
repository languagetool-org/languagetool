/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Jaume Ortolà i Font
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
package org.languagetool.rules.pt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BrazilianToponymMap {
  private final Map<String, List<String>> map;

  BrazilianToponymMap() {
    map = new BrazilianToponymMapLoader().buildMap();
  }

  // Since the actually toponym string is only a heuristic, it could be that we match more than we need, e.g.:
  // "Venho do Rio de Janeiro" will match, hungrily, the whole thing, rather than just "Rio de Janeiro".
  // To account for this, we loop, dropping the leftmost element of the toponym until we can't check any more.
  private <T> T toponymIter(String toponym, Function<String, T> processor, T defaultValue) {
    String normalisedToponym = toponym.replace('-', ' ').toLowerCase();
    String[] toponymParts = normalisedToponym.split(" ");
    int toponymLength = toponymParts.length;
    for (int i = 0; i < toponymLength; i++) {
      String toponymToCheck = String.join(" ", Arrays.copyOfRange(toponymParts, i, toponymLength));
      T result = processor.apply(toponymToCheck);
      if (result != null) {
        return result;
      }
    }
    return defaultValue;
  }

  public boolean isValidToponym(String toponym) {
    return toponymIter(toponym, toponymToCheck ->
        map.values().stream().anyMatch(list -> list.contains(toponymToCheck)) ? true : null,
      false);
  }

  public List<String> getStatesWithMunicipality(String toponym) {
    List<String> states = new ArrayList<>();
    map.forEach((state, municipalities) -> {
      if (municipalities.contains(toponym)) {
        states.add(state);
      }
    });
    return states;
  }

  public boolean isToponymInState(String toponym, String state) {
    List<String> municipalities = map.get(state);
    return municipalities != null && municipalities.contains(toponym);
  }
}
