/*
 *  LanguageTool, a natural language style checker
 *  Copyright (C) 2020 Daniel Naber
 *  
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  USA
 */
package org.languagetool.server;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DatabasePingLogEntry extends DatabaseLogEntry {
  
  private final Calendar date;
  private final Long client;
  private final Long user;

  public DatabasePingLogEntry(Long client, Long user) {
    this.client = client;
    this.user = user;
    this.date = Calendar.getInstance();
  }

  @Override
  public Map<Object, Object> getMapping() {
    HashMap<Object, Object> parameters = new HashMap<>();
    parameters.put("day", ServerTools.getSQLDateString(date));
    parameters.put("created_at", ServerTools.getSQLDatetimeString(date));
    parameters.put("client", client);
    parameters.put("user_id", user);
    return parameters;
  }

  @Override
  public String getMappingIdentifier() {
    return "org.languagetool.server.LogMapper.pings";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DatabasePingLogEntry that = (DatabasePingLogEntry) o;
    return Objects.equals(client, that.client) && Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(client, user);
  }
}
