/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.server;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.tools.StringTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class DatabaseRuleMatchLogEntry extends DatabaseLogEntry {
  private final List<RuleMatchInfo> matches = new ArrayList<>();

  DatabaseRuleMatchLogEntry(Map<String, Integer> matchCounts) {
    for (Map.Entry<String, Integer> match : matchCounts.entrySet()) {
      matches.add(new RuleMatchInfo(match.getKey(), match.getValue()));
    }
  }

  public int getMatchCount() {
    return matches.size();
  }

  @Override
  public Map<Object, Object> getMapping() {
    HashMap<Object, Object> map = new HashMap<>();
    map.put("matches", matches);
    return map;
  }

  @Override
  public String getMappingIdentifier() {
    return "org.languagetool.server.LogMapper.ruleMatch";
  }

  static class RuleMatchInfo {
    String ruleId;
    int matchCount;

    RuleMatchInfo(String rule_id, int match_count) {
      this.ruleId = StringUtils.abbreviate(rule_id, 128);
      this.matchCount = match_count;
    }
  }
}
