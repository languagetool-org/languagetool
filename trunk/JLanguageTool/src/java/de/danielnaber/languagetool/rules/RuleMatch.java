/* JLanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.rules;

/**
 * A class that holds information about where a rule matches text.
 * 
 * @author Daniel Naber
 */
public class RuleMatch {

  private int fromLine = -1;
  private Rule rule;
  private int fromPos;
  private int toPos;
  private String message;
  
  public RuleMatch(Rule rule, int fromPos, int toPos, String message) {
    this.rule = rule;
    this.fromPos = fromPos;
    this.toPos = toPos;
    this.message = message;
  }

  public Rule getRule() {
    return rule;
  }

  /**
   * Set the line number in which the match occurs.
   */
  public void setLine(int fromLine) {
    this.fromLine = fromLine;
  }

  /**
   * Get the line number in which the match occurs.
   */
  public int getLine() {
    return fromLine;
  }

  /**
   * Position of the start of the error (in characters).
   */
  public int getFromPos() {
    return fromPos;
  }
  
  /**
   * Position of the end of the error (in characters).
   */
  public int getToPos() {
    return toPos;
  }

  /**
   * A short human-readable explanation describing the error.
   */
  public String getMessage() {
    return message;
  }

  public String toString() {
    return rule.getId() + ":" + fromPos + "-" + toPos + ":" + message;
  }

}
