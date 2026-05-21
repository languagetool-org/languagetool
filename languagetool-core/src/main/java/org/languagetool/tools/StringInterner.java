package org.languagetool.tools;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

public final class StringInterner {
  private static final Interner<String> interner = Interners.newWeakInterner();

  private StringInterner() {
  }

  public static String intern(String string) {
    return interner.intern(string);
  }
}
