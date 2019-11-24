/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.dev.diff;

import java.util.Objects;

class LightRuleMatch {
  
  enum Status {
    temp_off, off, on
  }
  
  private final int line;
  private final int column;
  private final String ruleId;
  private final String message;
  private final String context;
  private final String coveredText;
  private final String suggestions;
  private final String source;
  private final Status status;

  LightRuleMatch(int line, int column, String ruleId, String message, String context, String coveredText,
                 String suggestions, String source, Status status) {
    this.line =  line;
    this.column = column;
    this.ruleId = Objects.requireNonNull(ruleId);
    this.message = Objects.requireNonNull(message);
    this.context = Objects.requireNonNull(context);
    this.coveredText = Objects.requireNonNull(coveredText);
    this.suggestions = suggestions == null ? "" : suggestions;
    this.source = source;
    this.status = status;
  }

  int getLine() {
    return line;
  }

  int getColumn() {
    return column;
  }

  String getRuleId() {
    return ruleId;
  }

  String getMessage() {
    return message;
  }

  String getContext() {
    return context;
  }

  String getCoveredText() {
    return coveredText;
  }

  String getSuggestions() {
    return suggestions;
  }

  String getSource() {
    return source;
  }

  Status getStatus() {
    return status;
  }
  
  @Override
  public String toString() {
    return line + "/" + column +
      " " + ruleId +
      ", msg=" + message +
      ", covered=" + coveredText +
      ", suggestions=" + suggestions;
  }
}
