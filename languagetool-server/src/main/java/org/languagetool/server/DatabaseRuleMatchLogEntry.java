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

import java.util.HashMap;
import java.util.Map;


class DatabaseRuleMatchLogEntry extends DatabaseLogEntry {
  private Long checkId;
  private final String ruleId;
  private final int matchCount;

  DatabaseRuleMatchLogEntry(String ruleId, int matchCount) {
    this.ruleId = ruleId;
    this.matchCount = matchCount;
  }

  @Override
  public Map<Object, Object> getMapping() {
    HashMap<Object, Object> map = new HashMap<>();
    map.put("check_id", checkId);
    map.put("rule_id", ruleId);
    map.put("match_count", matchCount);
    return map;
  }

  @Override
  public String getMappingIdentifier() {
    return "org.languagetool.server.LogMapper.ruleMatch";
  }

  @Override
  public void followup(Map<Object, Object> parameters) {
    // not needed
  }

  void setCheckId(Long checkId) {
    this.checkId = checkId;
  }
}
