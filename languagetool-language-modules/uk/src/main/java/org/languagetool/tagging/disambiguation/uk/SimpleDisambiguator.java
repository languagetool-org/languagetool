package org.languagetool.tagging.disambiguation.uk;

import java.io.IOException;
import java.io.InputStream;
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
  
        String[] parts = line.trim().split(" ");
        result.put(parts[0], new TokenMatcher(parts[1], parts[2]));
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


  static class TokenMatcher {
    private final String lemma;
    private final Pattern tagRegex;

    public TokenMatcher(String lemma, String tagRegex) {
      this.lemma = lemma;
      this.tagRegex = Pattern.compile(tagRegex);
    }

    public boolean matches(AnalyzedToken analyzedToken) {
      return lemma.equals(analyzedToken.getLemma())
          && analyzedToken.getPOSTag() != null
          && tagRegex.matcher(analyzedToken.getPOSTag()).matches();
    }

    @Override
    public String toString() {
      return "TokenMatcher [lemma=" + lemma + ", tagRegex=" + tagRegex + "]";
    }
  }

}
