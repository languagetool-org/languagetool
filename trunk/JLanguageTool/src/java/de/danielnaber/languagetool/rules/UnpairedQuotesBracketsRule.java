package de.danielnaber.languagetool.rules;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.Language;

/** Rule that finds unpaired quotes, brackets etc. **/
public class UnpairedQuotesBracketsRule extends Rule {

  /** Note that there must be equal length of both arrays,
   * and the sequence of starting symbols must match exactly
   * the sequence of ending symbols.
   */
  private static final String[] START_SYMBOLS = { "[", "(", "{", "\"", "'" };
  private static final String[] END_SYMBOLS = {"]", ")", "}", "\"", "'"};

  private final String[] startSymbols;
  private final String[] endSymbols;
  
  private static final String[] EN_START_SYMBOLS  = {"[", "(", "{","“", "\"", "'"};
  private static final String[] EN_END_SYMBOLS  = {"]", ")", "}", "”", "\"","'"};
    
  private static final String[] PL_START_SYMBOLS  = {"[", "(", "{", "„", "»", "\""};
  private static final String[] PL_END_SYMBOLS  = {"]", ")", "}", "”", "«", "\""};
  
  private static final String[] FR_START_SYMBOLS  = {"[", "(", "{", "»", "‘"};
  private static final String[] FR_END_SYMBOLS  = {"]", ")", "}", "«", "’"};
  
  private static final String[] DE_START_SYMBOLS  = {"[", "(", "{", "„", "»", "‘"};
  private static final String[] DE_END_SYMBOLS  = {"]", ")", "}", "“", "«", "’"};
  
  private static final String[] ES_START_SYMBOLS  = {"[", "(", "{", "“", "«", "¿", "¡"};
  private static final String[] ES_END_SYMBOLS  = {"]", ")", "}", "”", "»", "?", "!"};

  private static final String[] UK_START_SYMBOLS  = {"[", "(", "{", "„", "«"};
  private static final String[] UK_END_SYMBOLS  = {"]", ")", "}", "“", "»"};
  
  private static final String[] NL_START_SYMBOLS  = {"[", "(", "{", "„", "“", "‘"};
  private static final String[] NL_END_SYMBOLS  = {"]", ")", "}", "”", "”", "’"};
  
  private static final String[] IT_START_SYMBOLS  = {"[", "(", "{", "»", "‘"};
  private static final String[] IT_END_SYMBOLS  = {"]", ")", "}", "«", "’"};    
  
  /**
   * The counter used for pairing symbols.
   */
  private int[] symbolCounter;  
    
  private int[] ruleMatchArray; 
  
  private boolean reachedEndOfParagraph = false;
  
  private Language ruleLang;
  
  public UnpairedQuotesBracketsRule(final ResourceBundle messages, final Language language) {
    super(messages);
    super.setCategory(new Category(messages.getString("category_misc")));
   
    setParagraphBackTrack(true);
    
    if (language.equals(Language.POLISH)) {
      startSymbols = PL_START_SYMBOLS;
      endSymbols = PL_END_SYMBOLS;
    } else if (language.equals(Language.FRENCH)) {
      startSymbols = FR_START_SYMBOLS;
      endSymbols = FR_END_SYMBOLS;
    } else if (language.equals(Language.ENGLISH)) {
      startSymbols = EN_START_SYMBOLS;
      endSymbols = EN_END_SYMBOLS;
    } else if (language.equals(Language.GERMAN)) {
      startSymbols = DE_START_SYMBOLS;
      endSymbols = DE_END_SYMBOLS;
    } else if (language.equals(Language.DUTCH)) {
      startSymbols = NL_START_SYMBOLS;
      endSymbols = NL_END_SYMBOLS;
    } else if (language.equals(Language.SPANISH)) {
      startSymbols = ES_START_SYMBOLS;
      endSymbols = ES_END_SYMBOLS;
    } else if (language.equals(Language.UKRAINIAN)) {
      startSymbols = UK_START_SYMBOLS;
      endSymbols = UK_END_SYMBOLS;
    } else if (language.equals(Language.ITALIAN)) {
      startSymbols = IT_START_SYMBOLS;
      endSymbols = IT_END_SYMBOLS;
    } else {
      startSymbols = START_SYMBOLS;
      endSymbols = END_SYMBOLS; 
    }
    
    symbolCounter = new int [startSymbols.length];    
    ruleMatchArray = new int[startSymbols.length];
    
     for (int i = 0; i < startSymbols.length; i++) {
       symbolCounter[i] = 0;       
       ruleMatchArray[i] = 0;
     }
    ruleLang = language;
  }

  public String getId() {
    return "UNPAIRED_BRACKETS";
  }

  public String getDescription() {
    return messages.getString("desc_unpaired_brackets");
  }

  public Language[] getLanguages() {
    return new Language[] { Language.ENGLISH, Language.GERMAN, Language.POLISH, Language.FRENCH, 
            Language.SPANISH, Language.ITALIAN, Language.DUTCH, Language.LITHUANIAN };
  }

  public final RuleMatch[] match(final AnalyzedSentence text) {
    List < RuleMatch > ruleMatches = new ArrayList<RuleMatch>();
    AnalyzedTokenReadings[] tokens = text.getTokens();
    AnalyzedToken matchToken = null;
    
    if (reachedEndOfParagraph) {
      reset();
    }
    
    int ruleMatchIndex = getMatchesIndex();
        
    int pos = 0;          
      for (int j = 0; j < startSymbols.length; j++) {      
      for (int i = 1; i < tokens.length; i++) {
        String token = tokens[i].getToken();
        boolean precededByWhitespace = tokens[i - 1].isWhitespace()
          || tokens[i - 1].getToken().matches("\\p{Punct}");
        
        boolean followedByWhitespace = true;
        if (i < tokens.length - 1) {
          followedByWhitespace = tokens[i + 1].isWhitespace() 
            || tokens[i + 1].getToken().matches("\\p{Punct}");
        }
        
        if (followedByWhitespace
            && precededByWhitespace) {
          if (i == tokens.length) {
          precededByWhitespace = false;
          } else if (startSymbols[j].equals(endSymbols[j])) {
            if (symbolCounter[j] > 0) {
              precededByWhitespace = false;
            } else {
              followedByWhitespace = false;              
            }
          }
        }
        
        boolean noException = true; 
        
        // exception for English inches, e.g., 20"
        if ((precededByWhitespace || followedByWhitespace)
            && i > 1
            && ruleLang.equals(Language.ENGLISH)
            && token.trim().equals("\"")) {          
          if (tokens[i - 1].getToken().matches("[\\d]+")) {
            noException = false;
          }
          }
        
        // Exception for English plural saxon genetive
        //TODO: add POS checking
        if ((precededByWhitespace || followedByWhitespace) 
            && ruleLang.equals(Language.ENGLISH) 
            && token.trim().equals("'")
            && i > 1
            && noException) {
          if (tokens[i - 1].getToken().charAt(
                  tokens[i - 1].getToken().length() - 1) == 's') {
            noException = false;
          }
          }
               
      if (noException 
          && precededByWhitespace 
          && token.trim().equals(startSymbols[j])) {        
        symbolCounter[j]++;
        pos = i;
      } else if (noException 
          && followedByWhitespace 
          && token.trim().equals(endSymbols[j])) {
        if (i > 2 && endSymbols[j].equals(")") 
            && symbolCounter[j] == 0) {
          // exception for bulletting: 1), 2), 3)...,
          // II), 2') and 1a).
          if (!(tokens[i - 1].
              getToken().
              matches("(?i)\\d{1,2}?[a-z']*|M*(D?C{0,3}|C[DM])(L?X{0,3}|X[LC])(V?I{0,3}|I[VX])$")              
              )) {
            symbolCounter[j]--;
            pos = i;
          }
        } else {
        symbolCounter[j]--;
        pos = i;
        }
      }
      }      
      
      for (int i = 0; i < symbolCounter.length; i++) {
      if (symbolCounter[i] != 0) {                        
        if (ruleMatchArray[i] != 0) {           
            if (isInMatches(ruleMatchArray[i] - 1)) {
              setAsDeleted(ruleMatchArray[i] - 1);
              ruleMatchArray[i] = 0;
            } else {
              ruleMatchIndex++;
              ruleMatchArray[i] = ruleMatchIndex;
              matchToken = tokens[pos].getAnalyzedToken(0);
              String msg = messages.getString("unpaired_brackets");
              @SuppressWarnings("null")
              RuleMatch ruleMatch = new RuleMatch(this, matchToken.getStartPos(), matchToken.getStartPos()+1, msg);
              ruleMatches.add(ruleMatch);
            }
        } else {
          ruleMatchIndex++;
          ruleMatchArray[i] = ruleMatchIndex;
          matchToken = tokens[pos].getAnalyzedToken(0);
          String msg = messages.getString("unpaired_brackets");
          @SuppressWarnings("null")
          RuleMatch ruleMatch = new RuleMatch(this, matchToken.getStartPos(), matchToken.getStartPos()+1, msg);
          ruleMatches.add(ruleMatch);
        }
        
        symbolCounter[i] = 0;        
                
       }
      }
   }      
      
      if (tokens[tokens.length - 1].isParaEnd()) {
        reachedEndOfParagraph = true;
      }     
      
      return toRuleMatchArray(ruleMatches);     
  }

  /**
   * Reset the state information for the rule,
   * including paragraph-level information.
   */
  public final void reset() {
    for (int i = 0; i < symbolCounter.length; i++) {
      symbolCounter[i] = 0;
      ruleMatchArray[i] = 0;
    }
    if (!reachedEndOfParagraph) {
      clearMatches();
    }
    reachedEndOfParagraph = false;
  }

}
