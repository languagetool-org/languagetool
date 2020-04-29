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

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

@Experimental
public class RuleLoggerMessage {

  private final static TimeZone timeZone = TimeZone.getTimeZone("UTC");

  private final String ruleId;
  private final String language;
  private final Date timestamp;

  private String message;

  public RuleLoggerMessage(@NotNull String ruleId, @NotNull String language, @NotNull String message) {
    this.ruleId = Objects.requireNonNull(ruleId);
    this.language = Objects.requireNonNull(language);
    this.timestamp = Calendar.getInstance(timeZone).getTime();
    this.message = Objects.requireNonNull(message);
  }

  public String getRuleId() {
    return ruleId;
  }

  public String getLanguage() {
    return language;
  }

  public Date getTimestamp() {
    return (Date) timestamp.clone();
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
