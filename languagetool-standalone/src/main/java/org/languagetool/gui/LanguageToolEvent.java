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

  public static final int CHECKING_STARTED = 0;
  public static final int CHECKING_FINISHED = 1;
  public static final int LANGUAGE_CHANGED = 2;
  public static final int RULE_DISABLED = 3;
  public static final int RULE_ENABLED = 4;
  private LanguageToolSupport source;
  private int type;
  private Object caller;

  LanguageToolEvent(LanguageToolSupport source, int type, Object caller) {
    this.source = source;
    this.type = type;
    this.caller = caller;
  }

  LanguageToolSupport getSource() {
    return source;
  }

  Object getCaller() {
    return caller;
  }

  int getType() {
    return type;
  }
}
