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

import org.jetbrains.annotations.NotNull;
import org.languagetool.Experimental;
import org.languagetool.RuleLogger;
import org.languagetool.RuleLoggerMessage;

import java.util.Objects;
import java.util.logging.Level;

/**
 * @since 4.5
 * Logs messages to database as well as System.out
 */
@Experimental
public abstract class DatabaseRuleLogger extends RuleLogger {

  protected final Long serverId;

  public DatabaseRuleLogger(Long serverId) {
    this.serverId = serverId;
  }

  @Override
  public void log(RuleLoggerMessage message, Level level) {

    String text = String.format("[%s] %s - %s (%s): %s", level.getName(), message.getClass().getSimpleName(),
      message.getRuleId(), message.getLanguage(), message.getMessage());

    DatabaseLogger.getInstance().log(new DatabaseMiscLogEntry(serverId, null, null, text));

    System.out.println(text);
  }

}
