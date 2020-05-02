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

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

class LightRuleMatch {
  
  enum Status {
    temp_off, on
  }
  
  private final int line;
  private final int column;
  private final String fullRuleId;
  private final String message;
  private final String context;
  private final String coveredText;
  private final String suggestions;
  private final String ruleSource;  // e.g. grammar.xml
  private final String title;
  private final Status status;

  LightRuleMatch(int line, int column, String ruleId, String message, String context, String coveredText,
                 String suggestions, String ruleSource, String title, Status status) {
    this.line =  line;
    this.column = column;
    this.fullRuleId = Objects.requireNonNull(ruleId);
    this.message = Objects.requireNonNull(message);
    this.context = Objects.requireNonNull(context);
    this.coveredText = Objects.requireNonNull(coveredText);
    this.suggestions = suggestions == null ? "" : suggestions;
    this.ruleSource = ruleSource;
    this.title = title;
    this.status = Objects.requireNonNull(status);
  }

  int getLine() {
    return line;
  }

  int getColumn() {
    return column;
  }

  String getFullRuleId() {
    return fullRuleId;
  }
  
  String getRuleId() {
    return DiffTools.getMasterId(fullRuleId);
  }
  
  @Nullable
  String getSubId() {
    return DiffTools.getSubId(fullRuleId);
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

  String getRuleSource() {
    return ruleSource;
  }
  
  String getTitle() {
    return title;
  }

  Status getStatus() {
    return status;
  }
  
  @Override
  public String toString() {
    return line + "/" + column +
      " " + getRuleId() + "[" + getSubId() + "]" +
      ", msg=" + message +
      ", covered=" + coveredText +
      ", suggestions=" + suggestions +
      ", title=" + title +
      //", status=" + status +
      ", ctx=" + context;
  }
}
