/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Pedro Goulart
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.languagetool.JLanguageTool.getDataBroker;

public class BrazilianStateInfoMapLoader {
  private final String stateMappingFilename = "pt/state_name_mapping.txt";

  private List<String> getStateMappingLines() {
    return getDataBroker().getFromResourceDirAsLines(stateMappingFilename);
  }

  public Map<String, BrazilianStateInfo> buildMap() {
    List<String> stateMappingLines = getStateMappingLines();
    Map<String, BrazilianStateInfo> stateMap = new HashMap<>();
    for (String line : stateMappingLines) {
      if (!line.startsWith("#") && !line.trim().isEmpty()) {
        String[] columns = line.split("\t");
        if (columns.length == 4) {
          String stateName = columns[0];
          String abbreviation = columns[1];
          String[] articles = columns[2].split(",");
          String capital = columns[3];
          stateMap.put(abbreviation, new BrazilianStateInfo(stateName, abbreviation, articles, capital));
        }
      }
    }
    return stateMap;
  }
}
