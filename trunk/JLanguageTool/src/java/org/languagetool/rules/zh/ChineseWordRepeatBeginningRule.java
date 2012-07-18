package org.languagetool.rules.zh;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Category;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

public class ChineseWordRepeatBeginningRule extends Rule{

	  private String lastEndToken ="" ;
	  private String lastToken = "";
	  private String beforeLastToken = "";
	  
	  public ChineseWordRepeatBeginningRule(final ResourceBundle messages, final Language language) {
	    super(messages);
	    super.setCategory(new Category(messages.getString("category_misc")));
	  }

	  @Override
	  public String getId() {
	    return "CHINESE_WORD_REPEAT_BEGINNING_RULE";
	  }

	  @Override
	  public String getDescription() {
	    return messages.getString("desc_repetition_beginning");
	  }
	  
	  protected boolean isAdverb(AnalyzedTokenReadings token) {
	    return false;
	  }
	  
	  public boolean isException(String token) {
	    // avoid warning when having lists like "2007: ..." or the like
	    if (token.equals(":") || token.equals("–") || token.equals("-")) {
	        return true;
	    }
	    return false;
	  }

	  @Override
	  public RuleMatch[] match(final AnalyzedSentence text) {
		
	    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
	    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
	    
	    if (lastEndToken.equals("，")){
	    	lastEndToken = tokens[tokens.length -1].getToken();
	    	return toRuleMatchArray(ruleMatches);
	    }
	    
	   
	    
	    if (tokens.length>3) {
	      final AnalyzedTokenReadings analyzedToken = tokens[1];
	      final String token = analyzedToken.getToken();
	      // avoid "..." etc. to be matched:
	      boolean isWord = true;
	      if (token.length() == 1) {
	        final char c = token.charAt(0);
	        if (!Character.isLetter(c)) {
	          isWord = false;
	        }
	      }
	      
	      if (isWord && lastToken.equals(token)
	          && !isException(token) && !isException(tokens[2].getToken()) && !isException(tokens[3].getToken())) {
	        final String shortMsg;
	        if (isAdverb(analyzedToken)) {
	          shortMsg = messages.getString("desc_repetition_beginning_adv");
	        } else if (beforeLastToken.equals(token)) {
	          shortMsg = messages.getString("desc_repetition_beginning_word");
	        } else {
	          shortMsg = "";
	        }
	          
	        if (!shortMsg.equals("")) {
	          final String msg = shortMsg + " " + messages.getString("desc_repetition_beginning_thesaurus");
	          final int startPos = analyzedToken.getStartPos();
	          final int endPos = startPos + token.length();
	          final RuleMatch ruleMatch = new RuleMatch(this, startPos, endPos, msg, shortMsg);
	          ruleMatches.add(ruleMatch);
	        }
	      }
	      beforeLastToken = lastToken;
	      lastToken = token;
	      
	      lastEndToken = tokens[tokens.length -1].getToken();
	    }
	    
	    //TODO should we ignore repetitions involving multiple paragraphs?
	    //if (tokens[tokens.length - 1].isParaEnd()) beforeLastToken = "";
	    
	    return toRuleMatchArray(ruleMatches);
	  }

	  @Override
	  public void reset() {
	    lastToken = "";
	    beforeLastToken = "";
	  }


}
