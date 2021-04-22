/* LanguageTool, a natural language style checker 
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
package org.languagetool.gui;

/**
 *
 * @author Panagiotis Minos
 */
class LanguageToolEvent {

  public enum Type {
    CHECKING_STARTED,
    CHECKING_FINISHED,
    LANGUAGE_CHANGED,
    RULE_DISABLED,
    RULE_ENABLED
  }

  private final LanguageToolSupport source;
  private final Type type;
  private final Object caller;
  private final long elapsedTime;

  LanguageToolEvent(LanguageToolSupport source, Type type, Object caller) {
    this(source, type, caller, 0);
  }

  LanguageToolEvent(LanguageToolSupport source, Type type, Object caller, long elapsedTime) {
    this.source = source;
    this.type = type;
    this.caller = caller;
    this.elapsedTime = elapsedTime;
  }

  LanguageToolSupport getSource() {
    return source;
  }

  Object getCaller() {
    return caller;
  }

  Type getType() {
    return type;
  }

  long getElapsedTime() {
    return elapsedTime;
  }

}
