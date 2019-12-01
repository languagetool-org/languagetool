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

class MatchKey {

  private final int line;
  private final int column;
  private final String ruleId;
  private final String subId;   // not considered in equals
  private final String title;
  private final String coveredText;

  MatchKey(int line, int column, String ruleId, String title, String coveredText) {
    this.line = line;
    this.column = column;
    this.ruleId = DiffTools.getMasterId(ruleId);
    this.subId = DiffTools.getSubId(ruleId);
    this.title = title.trim();
    this.coveredText = Objects.requireNonNull(coveredText);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MatchKey matchKey = (MatchKey) o;
    return line == matchKey.line &&
      column == matchKey.column &&
      ruleId.equals(matchKey.ruleId) &&
      Objects.equals(title, matchKey.title) &&
      coveredText.equals(matchKey.coveredText);
  }

  @Override
  public int hashCode() {
    return Objects.hash(line, column, ruleId, title, coveredText);
  }

  @Override
  public String toString() {
    return "MatchKey{" +
      "line=" + line +
      ", column=" + column +
      ", ruleId='" + ruleId + '\'' +
      //", (subId='" + subId + '\'' +
      ", title='" + title + '\'' +
      ", coveredText='" + coveredText + '\'' +
      '}';
  }
}
