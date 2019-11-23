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

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * @since 4.5
 * Flexible, unified logging from inside rules; messages can be read from other modules,
 * e.g. languagetool-servers DatabaseLogger
 */
@Experimental
public final class RuleLoggerManager {
  private static final RuleLoggerManager instance = new RuleLoggerManager();

  public static RuleLoggerManager getInstance() {
    return instance;
  }

  private final List<RuleLogger> loggerList = new LinkedList<>();

  private Level level = Level.ALL;

  public RuleLoggerManager() {
    addLogger(new OutputStreamLogger());
  }

  public void addLogger(RuleLogger logger) {
    loggerList.add(logger);
  }

  public void removeLogger(RuleLogger logger) {
    loggerList.remove(logger);
  }

  public void clearLoggers() {
    loggerList.clear();
  }
  public void log(RuleLoggerMessage message) {
    log(message, Level.INFO);
  }

  public void log(RuleLoggerMessage message, Level logLevel) {
    if (logLevel.intValue() >= level.intValue()) {
      loggerList.stream()
        .filter(logger -> logger.filter(message))
        .forEach(logger -> logger.log(message, logLevel));
    }
  }

  public Level getLevel() {
    return level;
  }

  public void setLevel(Level level) {
    this.level = level;
  }
}
