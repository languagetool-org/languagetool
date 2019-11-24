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
  
  private final int line;
  private final int column;
  private final String ruleId;
  private final String message;
  private final String coveredText;
  private final String suggestions;

  LightRuleMatch(int line, int column, String ruleId, String message, String coveredText, String suggestions) {
    this.line =  line;
    this.column = column;
    this.ruleId = Objects.requireNonNull(ruleId);
    this.message = Objects.requireNonNull(message);
    this.coveredText = Objects.requireNonNull(coveredText);
    this.suggestions = suggestions == null ? "" : suggestions;
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

  String getCoveredText() {
    return coveredText;
  }

  String getSuggestions() {
    return suggestions;
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
