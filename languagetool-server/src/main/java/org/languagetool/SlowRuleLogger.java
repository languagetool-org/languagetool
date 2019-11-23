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

import org.languagetool.server.DatabaseRuleLogger;

import java.io.PrintStream;
import java.util.function.Predicate;
import java.util.logging.Level;

//public class SlowRuleLogger extends DatabaseRuleLogger implements Predicate<RuleLoggerMessage> {
public class SlowRuleLogger extends OutputStreamLogger implements Predicate<RuleLoggerMessage> {
  private int threshold = 50; // milliseconds

/*  public SlowRuleLogger(Long serverId) {
    super(serverId);
    addFilter(this);
  }

  public SlowRuleLogger(Long serverId, int slowRuleLoggingThreshold) {
    this(serverId);
    setThreshold(slowRuleLoggingThreshold);
  }*/

  public SlowRuleLogger(PrintStream stream) {
    super(stream);
    addFilter(this);
    setLevel(Level.FINE);
  }

  public SlowRuleLogger(PrintStream stream, int slowRuleLoggingThreshold) {
    this(stream);
    setThreshold(slowRuleLoggingThreshold);
  }

  @Override
  public boolean test(RuleLoggerMessage message) {
    if (threshold < 0) {
      return false;
    }
    if (message instanceof RuleCheckTimeMessage) {
      RuleCheckTimeMessage msg = (RuleCheckTimeMessage) message;
      if (msg.getExecutionTime() >= threshold) {
        return true;
      }
    }
    return false;
  }

  public int getThreshold() {
    return threshold;
  }

  public void setThreshold(int threshold) {
    this.threshold = threshold;
  }
}
