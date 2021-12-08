package org.languagetool.tagging.disambiguation.uk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;

class SimpleDisambiguator {

  final Map<String, TokenMatcher> DISAMBIG_REMOVE_MAP = loadMap("/uk/disambig_remove.txt");
  final Map<String, List<String>> DISAMBIG_DUPS_MAP = loadMapDups("/uk/disambig_dups.txt");

  private static Map<String, TokenMatcher> loadMap(String path) {
    Map<String, TokenMatcher> result = new HashMap<>();
    List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(path);
    for (String line : lines) {
      if( line.startsWith("#") || line.trim().isEmpty() ) {
        continue;
      }
      
      line = line.replaceFirst(" *#.*", "");

      String[] parts = line.trim().split(" ", 2);
      
      String[] matchers = parts[1].split("\\|");
      List<MatcherEntry> matcherEntries = new ArrayList<>();
      for (String string : matchers) {
        String[] matcherParts = string.split(" ");
        matcherEntries.add(new MatcherEntry(matcherParts[0], matcherParts[1]));
      }
      
      result.put(parts[0], new TokenMatcher(matcherEntries));
    }
    //        System.err.println("Found disambig remove list: " + result.size());
    return result;
  }

  private static Map<String, List<String>> loadMapDups(String path) {
    Map<String, List<String>> result = new HashMap<>();
    List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(path);
    for (String line : lines) {
      if( line.startsWith("#") || line.trim().isEmpty() ) {
        continue;
      }
      
      line = line.replaceFirst(" *#.*", "");

      String[] parts = line.trim().split(" ");
      
      result.put(parts[0], Arrays.asList(parts).subList(1, parts.length));
    }
    return result;
  }

  
  public void removeRareForms(AnalyzedSentence input) {
    AnalyzedTokenReadings[] tokens = input.getTokensWithoutWhitespace();
    for (int i = 1; i < tokens.length; i++) {

      String token = tokens[i].getCleanToken();
//      if( token == null )
//        continue;

      if( Character.isLowerCase(token.charAt(0)) ) {
        token = token.toLowerCase();
      }

      TokenMatcher tokenMatcher = DISAMBIG_REMOVE_MAP.get(token);
      if( tokenMatcher == null ) {
        String lowerToken = token.toLowerCase();
        tokenMatcher = DISAMBIG_REMOVE_MAP.get(lowerToken);

        if( tokenMatcher == null ) {
          int idx = token.lastIndexOf('-');
          if( idx > 0 && token.matches(".*-(то|от|таки|бо|но)") ) {
            String mainToken = token.substring(0, idx);
            tokenMatcher = DISAMBIG_REMOVE_MAP.get(mainToken);
          }
        }
      }

      List<AnalyzedToken> analyzedTokens = tokens[i].getReadings();
      if( tokenMatcher != null ) {
        for (int j = analyzedTokens.size()-1; j>=0; j--) {
          AnalyzedToken analyzedToken = analyzedTokens.get(j);

          //        if( analyzedToken.getToken() == null )
          //          continue;

          if( tokenMatcher.matches(analyzedToken) ) {
            tokens[i].removeReading(analyzedToken, "dis_remove_rare");
          }
        }
      }
      
      // dups
      
      Set<String> lemmas = analyzedTokens.stream()
          .map(t -> t.getLemma())
          .filter(l -> l != null)
          .distinct()
          .collect(Collectors.toSet());

      lemmas.retainAll(DISAMBIG_DUPS_MAP.keySet());
      
      if( lemmas.size() > 0 ) {
        Set<String> lemmasToRemove = lemmas.stream()
            .map(l -> DISAMBIG_DUPS_MAP.get(l))
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
        
        for (int j = analyzedTokens.size()-1; j>=0; j--) {
          AnalyzedToken analyzedToken = analyzedTokens.get(j);

          if( lemmasToRemove.contains(analyzedToken.getLemma()) ) {
            tokens[i].removeReading(analyzedToken, "dis_remove_dups");
          }
        }
      }
      
    }    
  }

  private static class MatcherEntry {
    private final String lemma;
    private final Pattern tagRegex;

    public MatcherEntry(String lemma, String tagRegex) {
      this.lemma = lemma;
      this.tagRegex = Pattern.compile(tagRegex);
    }

    public boolean matches(AnalyzedToken analyzedToken) {
      return ("*".equals(lemma) || lemma.equals(analyzedToken.getLemma()))
          && ! analyzedToken.hasNoTag()
          && tagRegex.matcher(analyzedToken.getPOSTag()).matches();
    }

    @Override
    public String toString() {
      return "MatcherEntry [lemma=" + lemma + ", tagRegex=" + tagRegex + "]";
    }
  }

  static class TokenMatcher {
    private final List<MatcherEntry> matchers;

    public TokenMatcher(List<MatcherEntry> matchers) {
      this.matchers = matchers;
    }

    public boolean matches(AnalyzedToken analyzedToken) {
      for(MatcherEntry matcher: matchers) {
        if( matcher.matches(analyzedToken) )
          return true;
      }
      return false;
    }

    @Override
    public String toString() {
      return "TokenMatcher " + matchers;
    }
  }

}
