package org.languagetool.rules.spelling.morfologik.suggestions_ordering;

public class SuggestionsOrdererConfig {
  private static String ngramsPath;
  private static String enableMLSuggestionsOrderingProp = "enableMLSuggestionsOrdering";

  public static String getNgramsPath() {
    return ngramsPath;
  }

  public static void setNgramsPath(String ngramsPath) {
    SuggestionsOrdererConfig.ngramsPath = ngramsPath;
  }

  public static boolean isMLSuggestionsOrderingEnabled() {
    String enableMLSuggestionsOrderingProperty = System.getProperty(enableMLSuggestionsOrderingProp, "false");
    return Boolean.parseBoolean(enableMLSuggestionsOrderingProperty);
  }

  public static void setMLSuggestionsOrderingEnabled(boolean MLSuggestionsOrderingEnabled) {
    System.setProperty(enableMLSuggestionsOrderingProp, String.valueOf(MLSuggestionsOrderingEnabled));
  }
}
