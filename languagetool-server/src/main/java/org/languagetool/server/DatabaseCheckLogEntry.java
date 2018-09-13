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

import org.languagetool.Language;

import java.text.SimpleDateFormat;
import java.util.*;

class DatabaseCheckLogEntry extends DatabaseLogEntry {
  private final Long userId;
  private final Long client;
  private final Long server;
  private final int textSize;
  private final int matches;
  private final Language lang;
  private final Language langDetected;
  private final int computationTime;
  private final Long textSessionId;
  private final Calendar date;
  private final List<DatabaseRuleMatchLogEntry> ruleMatches = new ArrayList<>();

  public DatabaseCheckLogEntry(Long userId, Long client, Long server, int textSize, int matches, Language lang, Language langDetected, int computationTime, Long textSessionId) {
    this.userId = userId;
    this.client = client;
    this.server = server;
    this.textSize = textSize;
    this.matches = matches;
    this.lang = lang;
    this.langDetected = langDetected;
    this.computationTime = computationTime;
    this.textSessionId = textSessionId;
    this.date = Calendar.getInstance();
  }

  public void addRuleMatch(DatabaseRuleMatchLogEntry entry) {
    ruleMatches.add(entry);
  }

  @Override
  public Map<Object, Object> getMapping() {
    HashMap<Object, Object> map = new HashMap<>();
    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    map.put("day", dayFormat.format(date.getTime()));
    map.put("date", dateFormat.format(date.getTime()));
    map.put("user_id", userId);
    map.put("textsize", textSize);
    map.put("matches", matches);
    map.put("language", lang.getShortCodeWithCountryAndVariant());
    map.put("language_detected", langDetected.getShortCodeWithCountryAndVariant());
    map.put("computation_time", computationTime);
    map.put("text_session_id", textSessionId);
    map.put("server", server);
    map.put("client", client);
    return map;
  }

  @Override
  public String getMappingIdentifier() {
    return "org.languagetool.server.LogMapper.logCheck";
  }

  @Override
  public void followup(Map<Object, Object> parameters) {
    Long checkId = (Long) parameters.get("id");
    if (checkId == null) {
      System.err.println("Could not get generated key for check log entry in database.");
      return;
    }
    DatabaseLogger logger = DatabaseLogger.getInstance();
    for (DatabaseRuleMatchLogEntry entry : ruleMatches) {
      entry.setCheckId(checkId);
      logger.log(entry);
    }
  }

  //@Override
  //public void print(PrintStream out) {
  // TODO
  //}
}
