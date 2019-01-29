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
import java.util.function.Predicate;
import java.util.logging.Level;

@Experimental
public abstract class RuleLogger {

  private final List<Predicate<RuleLoggerMessage>> filters = new LinkedList<>();

  public abstract void log(RuleLoggerMessage message, Level level);

  public boolean filter(RuleLoggerMessage message) {
    return filters.stream().allMatch(filter -> filter.test(message));
  }

  public void addFilter(Predicate<RuleLoggerMessage> filter) {
    filters.add(filter);
  }

  public void removeFilter(Predicate<RuleLoggerMessage> filter) {
    filters.remove(filter);
  }

  public void clearFilters() {
    filters.clear();
  }
}
