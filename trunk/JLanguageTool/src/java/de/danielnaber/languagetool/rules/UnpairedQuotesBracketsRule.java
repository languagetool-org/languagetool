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
  private static final String[] START_SYMBOLS = { "[", "(", "{" };
  private static final String[] END_SYMBOLS = {"]", ")", "}"};

  private final String[] startSymbols;
  private final String[] endSymbols;
  
  private static final String[] EN_START_SYMBOLS  = {"[", "(", "{","“"};
  private static final String[] EN_END_SYMBOLS  = {"]", ")", "}", "”"};
    
  private static final String[] PL_START_SYMBOLS  = {"[", "(", "{", "„", "»"};
  private static final String[] PL_END_SYMBOLS  = {"]", ")", "}", "”", "«"};
  
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
  
  public UnpairedQuotesBracketsRule(final ResourceBundle messages, final Language language) {
    super(messages);
    super.setCategory(new Category(messages.getString("category_misc")));
   
    if (language == Language.POLISH) {
      startSymbols = PL_START_SYMBOLS;
      endSymbols = PL_END_SYMBOLS;
    } else if (language == Language.FRENCH) {
      startSymbols = FR_START_SYMBOLS;
      endSymbols = FR_END_SYMBOLS;
    } else if (language == Language.ENGLISH) {
      startSymbols = EN_START_SYMBOLS;
      endSymbols = EN_END_SYMBOLS;
    } else if (language == Language.GERMAN) {
      startSymbols = DE_START_SYMBOLS;
      endSymbols = DE_END_SYMBOLS;
    } else if (language == Language.DUTCH) {
      startSymbols = NL_START_SYMBOLS;
      endSymbols = NL_END_SYMBOLS;
    } else if (language == Language.SPANISH) {
      startSymbols = ES_START_SYMBOLS;
      endSymbols = ES_END_SYMBOLS;
    } else if (language == Language.UKRAINIAN) {
      startSymbols = UK_START_SYMBOLS;
      endSymbols = UK_END_SYMBOLS;
    } else if (language == Language.ITALIAN) {
      startSymbols = IT_START_SYMBOLS;
      endSymbols = IT_END_SYMBOLS;
    } else {
      startSymbols = START_SYMBOLS;
      endSymbols = END_SYMBOLS; 
    }    
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

  public RuleMatch[] match(final AnalyzedSentence text) {
    List < RuleMatch > ruleMatches = new ArrayList<RuleMatch>();
    AnalyzedTokenReadings[] tokens = text.getTokens();
    AnalyzedToken matchToken = null;
            
    int symbolCounter = 0;
    int pos = 0;          
      for (int j = 0; j < startSymbols.length; j++) {
        symbolCounter = 0;        
      for (int i = 0; i < tokens.length; i++) {
        String token = tokens[i].getToken();
      if (token.trim().equals(startSymbols[j])) {
        symbolCounter++;
        pos = i;
      } else if (token.trim().equals(endSymbols[j])) {
        symbolCounter--;
        pos = i;
      }
      }
      if (symbolCounter != 0) {
        matchToken = tokens[pos].getAnalyzedToken(0);
        String msg = messages.getString("unpaired_brackets");
        @SuppressWarnings("null")
        RuleMatch ruleMatch = new RuleMatch(this, matchToken.getStartPos(), matchToken.getStartPos()+1, msg);
        ruleMatches.add(ruleMatch);
    }    
   }
    return toRuleMatchArray(ruleMatches);
  }

  public void reset() {
    /** FIXME: check previous sentence match (create internal list,
    and add new matches only if they don't pair with previous ones)
    How can I know that the rule found paragraph end? (resetting at 
    paragraph ends seems best but it depends on tokenizers...) 
    **/
  }

}
