package org.languagetool.tagging.disambiguation.uk;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;


class SimpleDisambiguator {

  final Map<String, TokenMatcher> DISAMBIG_REMOVE_MAP = loadMap("/uk/disambig_remove.txt");

  private static Map<String, TokenMatcher> loadMap(String path) {
    Map<String, TokenMatcher> result = new HashMap<>();
    try (InputStream is = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path);
        Scanner scanner = new Scanner(is, "UTF-8")) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
  
        if( line.startsWith("#") || line.trim().isEmpty() )
          continue;
        
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
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void removeRareForms(AnalyzedSentence input) {
    AnalyzedTokenReadings[] tokens = input.getTokensWithoutWhitespace();
    for (int i = 1; i < tokens.length; i++) {

      String token = tokens[i].getToken();
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

      if( tokenMatcher == null )
        continue;

      List<AnalyzedToken> analyzedTokens = tokens[i].getReadings();
      for (int j = analyzedTokens.size()-1; j>=0; j--) {
        AnalyzedToken analyzedToken = analyzedTokens.get(j);

//        if( analyzedToken.getToken() == null )
//          continue;

        if( tokenMatcher.matches(analyzedToken) ) {
          tokens[i].removeReading(analyzedToken);
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
      return lemma.equals(analyzedToken.getLemma())
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
