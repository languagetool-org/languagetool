package org.languagetool.rules.spelling.morfologik.suggestions_ordering;

public class SuggestionsOrdererConfig {
  private static String ngramsPath;
  private static boolean MLSuggestionsOrderingEnabled = false;

  public static String getNgramsPath() {
    return ngramsPath;
  }

  public static void setNgramsPath(String ngramsPath) {
    SuggestionsOrdererConfig.ngramsPath = ngramsPath;
  }

  public static boolean isMLSuggestionsOrderingEnabled() {
    return MLSuggestionsOrderingEnabled;
  }

  public static void setMLSuggestionsOrderingEnabled(boolean MLSuggestionsOrderingEnabled) {
    SuggestionsOrdererConfig.MLSuggestionsOrderingEnabled = MLSuggestionsOrderingEnabled;
  }
}
