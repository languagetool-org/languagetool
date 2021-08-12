/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  * All rights reserved - not part of the Open Source edition
 *
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
