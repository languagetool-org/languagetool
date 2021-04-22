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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DatabaseAccessLimitLogEntry extends DatabaseLogEntry {
  private final Calendar date;
  private final String type;
  private final Long server;
  private final Long client;
  private final Long user;
  private final String reason;
  private final String referrer;
  private final String userAgent;

  DatabaseAccessLimitLogEntry(String type, Long server, Long client, Long user, String reason, String referrer, String userAgent) {
    this.date = Calendar.getInstance();
    this.type = type;
    this.server = server;
    this.client = client;
    this.user = user;
    this.referrer = referrer;
    this.userAgent = userAgent;
    this.reason = reason;
  }

  @Override
  public Map<Object, Object> getMapping() {
    HashMap<Object, Object> parameters = new HashMap<>();
    parameters.put("type", StringUtils.abbreviate(type, 64));
    parameters.put("date", ServerTools.getSQLDatetimeString(date));
    parameters.put("server", server);
    parameters.put("client", client);
    parameters.put("user", user);
    parameters.put("referrer", StringUtils.abbreviate(referrer, 128));
    parameters.put("user_agent", StringUtils.abbreviate(userAgent,  512));
    parameters.put("reason", StringUtils.abbreviate(reason,  512));
    return parameters;
  }

  @Override
  public String getMappingIdentifier() {
    return "org.languagetool.server.LogMapper.accessLimit";
  }
}
