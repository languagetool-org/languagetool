package org.languagetool.rules.uk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.uk.PosTagHelper;

class SearchHelper {
  
   public static class Match {
     private boolean ignoreQuotes = true;
     private boolean ignoreInserts = false;
//    private boolean ignoreCase = true;
//    private String[] searchTokens;
    private List<Condition> targets;
    private int limit = -1;
    private List<Condition> skips = new ArrayList<>();

//    public Match tokens(String... tokens) { this.searchTokens = tokens; return this; }
    public Match tokenLine(String tokenLine) { 
      this.targets = Stream.of(tokenLine.split("\\s"))
          .map(s -> Condition.token(s))
          .collect(Collectors.toList());
      return this;
    }
    
    public Match limit(int limit) { this.limit = limit; return this; }
    public Match ignoreInserts() { this.ignoreInserts = true; return this; }
    public Match skip(Condition... conditions) { this.skips = Arrays.asList(conditions); return this; }
    public Match target(Condition... conditions) { this.targets = Arrays.asList(conditions); return this; }
    
    public int mBefore(AnalyzedTokenReadings[] tokens, int pos) {
      boolean foundFirst = false;

      int logicalDistance = 0;

      for (int iCond = targets.size()-1; iCond>=0; pos--) {
        if( pos - 1 < iCond )
          return -1;

        if( limit > 0 && logicalDistance > limit )
          return -1;
        
        logicalDistance++;

        AnalyzedTokenReadings currentToken = tokens[pos];
        if( ignoreQuotes && LemmaHelper.QUOTES_PATTERN.matcher(currentToken.getToken()).matches() ) {
//          pos--;
          continue;
        }

        if( ignoreInserts(tokens, pos, -1) ) {
          pos -= 2;
          continue;
        }
        
        if( ignoreInserts && tokens[pos].getToken().equals(")") ) {
          for(int i=pos-1; i>=1; i--) {
            if( tokens[i].getToken().equals("(") ) {
              pos = i;
              continue;
            }
          }
        }

        Context context = new Context(tokens, pos);
        if( ! targets.get(iCond).matches(currentToken, context) ) {
          if( foundFirst )
            return -1;

          if( ! canSkip(currentToken, context) )
            return -1;
          
          continue;
        }

        foundFirst = true;
        iCond--;
      }
      return pos+1;
    }

    public int mAfter(AnalyzedTokenReadings[] tokens, int pos) {
      boolean foundFirst = false;

      int logicalDistance = 0;

      for (int iCond = 0; iCond < targets.size(); pos++) {
        if( pos + targets.size() - iCond > tokens.length )
          return -1;
        
        if( limit > 0 && logicalDistance > limit )
          return -1;
        
        logicalDistance++;

        AnalyzedTokenReadings currentToken = tokens[pos];
        if( ignoreQuotes && LemmaHelper.QUOTES_PATTERN.matcher(currentToken.getToken()).matches() ) {
//          pos++;
          continue;
        }

        if( ignoreInserts(tokens, pos, +1) ) {
          pos += 2;
          continue;
        }

        if( ignoreInserts && tokens[pos].getToken().equals("(") ) {
          for(int i=pos+1; i<=tokens.length-1; i++) {
            if( tokens[i].getToken().equals(")") ) {
              pos = i;
              continue;
            }
          }
        }

        Context context = new Context(tokens, pos);
        if( ! targets.get(iCond).matches(currentToken, context) ) {
          if( foundFirst )
            return -1;
          
          if( ! canSkip(currentToken, context) )
            return -1;

          continue;
        }

        foundFirst = true;
        iCond++;
      }
      return pos-1;
    }

    private boolean canSkip(AnalyzedTokenReadings currentToken, Context context) {
      return skips.isEmpty() 
          || skips.stream().anyMatch(s -> s.matches(currentToken, context));
    }

    private boolean ignoreInserts(AnalyzedTokenReadings[] tokens, int pos, int dir) {
      return ignoreInserts
          && (dir>0 ? pos + 3 < tokens.length : pos - 3 > 0)
          && ",".equals(tokens[pos].getToken())
          && ",".equals(tokens[pos+2*dir].getToken())
          && (PosTagHelper.hasPosTagPart(tokens[pos+1*dir], "insert")
              || LemmaHelper.hasLemma(tokens[pos+1*dir], Arrays.asList("зокрема", "відповідно")));
    }
    
  }
  
  static class Condition {
    Pattern postag;
    Pattern lemma;
    Pattern tokenPattern;
    String tokenStr;
    private boolean negate;

    public static Condition postag(Pattern pattern) {
      Condition condition = new Condition();
      condition.postag = pattern;
      return condition;
    }

    public static Condition lemma(Pattern pattern) {
      Condition condition = new Condition();
      condition.lemma = pattern;
      return condition;
    }

    public static Condition token(Pattern pattern) {
      Condition condition = new Condition();
      condition.tokenPattern = pattern;
      return condition;
    }

    public static Condition token(String token) {
      Condition condition = new Condition();
      condition.tokenStr = token;
      return condition;
    }
    
    public Condition negate() {
      this.negate = true;
      return this;
    }

    public boolean matches(AnalyzedTokenReadings analyzedTokenReadings, Context context) {
      return negate  
          ^ ((postag == null || PosTagHelper.hasPosTag(analyzedTokenReadings, postag))
          && (lemma == null || LemmaHelper.hasLemma(analyzedTokenReadings, lemma)) 
          && (tokenPattern == null || tokenPattern.matcher(analyzedTokenReadings.getCleanToken()).matches())
          && (tokenStr == null || tokenStr.equalsIgnoreCase(analyzedTokenReadings.getCleanToken())));
    }

    @Override
    public String toString() {
      return "Condition [postag=" + postag + ", lemma=" + lemma + ", token=" + tokenPattern + ", tokenStr=" + tokenStr + "]";
    }
    
  }
  
  static class Context {

    final AnalyzedTokenReadings[] tokens;
    final int pos;

    public Context(AnalyzedTokenReadings[] tokens, int pos) {
      this.tokens = tokens;
      this.pos = pos;
    }
    
  }

}
