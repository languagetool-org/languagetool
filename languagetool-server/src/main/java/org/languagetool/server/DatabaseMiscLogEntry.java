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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DatabaseMiscLogEntry extends DatabaseLogEntry {
  private final Calendar date;
  private final Long server;
  private final Long client;
  private final Long user;
  private final String message;

  public DatabaseMiscLogEntry(Long server, Long client, Long user,  String message) {
    this.server = server;
    this.client = client;
    this.user = user;
    this.message = message;
    this.date = Calendar.getInstance();
  }

  @Override
  public Map<Object, Object> getMapping() {
    HashMap<Object, Object> parameters = new HashMap<>();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    parameters.put("date", dateFormat.format(date.getTime()));
    parameters.put("server", server);
    parameters.put("client", client);
    parameters.put("user", user);
    parameters.put("message", message);
    return parameters;
  }

  @Override
  public String getMappingIdentifier() {
    return "org.languagetool.server.LogMapper.miscLogs";
  }
}
