/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Fabian Richter
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
package org.languagetool.server;

enum LimitEnforcementMode {
  DISABLED (1),
  PER_DAY (2);

  private final int id;

  LimitEnforcementMode(int id) {
    this.id = id;
  }

  static LimitEnforcementMode parse(Integer value) {
    if (value == null || value <= 0) {
      return DISABLED;
    }
    for (LimitEnforcementMode mode : LimitEnforcementMode.values()) {
      if (mode.id == value) {
        return mode;
      }
    }
    System.err.println("Invalid value for limit enforcement mode encountered: '" +
      value + "'; Falling back to DISABLED mode.");
    return DISABLED;
  }

  public int getId() {
    return id;
  }
}
