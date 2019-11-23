/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Oleg Serikov
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
package org.languagetool.rules.spelling.morfologik.suggestions_ordering;

public class SuggestionsOrdererConfig {

  private static final String PROP_NAME = "enableMLSuggestionsOrdering";

  private static String ngramsPath;

  static String getNgramsPath() {
    return ngramsPath;
  }

  public static void setNgramsPath(String ngramsPath) {
    SuggestionsOrdererConfig.ngramsPath = ngramsPath;
  }

  public static boolean isMLSuggestionsOrderingEnabled() {
    String enableMLSuggestionsOrderingProperty = System.getProperty(PROP_NAME, "false");
    return Boolean.parseBoolean(enableMLSuggestionsOrderingProperty);
  }

  public static void setMLSuggestionsOrderingEnabled(boolean MLSuggestionsOrderingEnabled) {
    System.setProperty(PROP_NAME, String.valueOf(MLSuggestionsOrderingEnabled));
  }
}
