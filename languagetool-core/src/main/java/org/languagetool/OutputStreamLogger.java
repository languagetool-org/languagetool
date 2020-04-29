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

package org.languagetool;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Logs messages to stdout (or any stream given in constructor)
 * @since 4.5
 */
@Experimental
public class OutputStreamLogger extends RuleLogger {

  private final PrintStream stream;
  private final DateFormat dateFormat;

  private Level level = Level.WARNING;

  public OutputStreamLogger() {
    this(System.out);
  }

  public OutputStreamLogger(PrintStream out) {
    stream = out;
    dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZ");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  @Override
  public void log(RuleLoggerMessage message, Level logLevel) {
    if (logLevel.intValue() >= level.intValue()) {
      String timestamp = dateFormat.format(message.getTimestamp());
      stream.printf("%s > [%s]: *%s* %s - %s (%s)%n",
        this.getClass().getSimpleName(),
        timestamp, logLevel.getName(),
        message.getRuleId(), message.getMessage(), message.getLanguage());
    }
  }

  public Level getLevel() {
    return level;
  }

  public void setLevel(Level level) {
    this.level = level;
  }
}
