/**
 * 
 */
package de.danielnaber.languagetool.rules.pl;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.rules.Category;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * @author Marcin Miłkowski
 * 
 * Rule for detecting same words in the sentence
 * but not just in a row 
 *
 */
public class PolishWordRepeatRule extends PolishRule {

  /**
   * Excluded dictionary words.
   */
  private static final Pattern EXC_WORDS 
    = Pattern.compile("nie|to|siebie|być|ani|albo|" +
        "lub|czy|bądź|jako|zł|coraz" +
        "|bardzo|ten|jak|mln|tys|swój|mój|" +
        "twój|nasz|wasz|i|zbyt");
  
  /**
   * Excluded part of speech classes.
   */
  private static final Pattern EXC_POS 
    = Pattern.compile("prep:.*|ppron.*");
  
  /**
   * Excluded non-words (special symbols,
   * Roman numerals etc.
   */
  private static final Pattern EXC_NONWORDS 
    = Pattern.compile("&quot|&gt|&lt|&amp|[0-9].*|" +
        "M*(D?C{0,3}|C[DM])(L?X{0,3}|X[LC])(V?I{0,3}|I[VX])$");

  
  public PolishWordRepeatRule(final ResourceBundle messages) {
    if (messages != null)
      super.setCategory(new Category(messages.getString("category_misc")));
  }
  
	/* (non-Javadoc)
	 * @see de.danielnaber.languagetool.rules.Rule#getId()
	 */
	@Override
	public final String getId() {
		return "PL_WORD_REPEAT";
	}

	/* (non-Javadoc)
	 * @see de.danielnaber.languagetool.rules.Rule#getDescription()
	 */
	@Override
	public final String getDescription() {
		return "Powtórzenia wyrazów w zdaniu (monotonia stylistyczna)";
	}


	/* Tests if any word form is repeated in the sentence.
	 * 
	 */
	@Override
	public final RuleMatch[] match(final AnalyzedSentence text) {
	    List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
	    AnalyzedTokenReadings[] tokens = text.getTokens();
	    boolean repetition = false;
	    HashMap <String, String> inflectedWords = new HashMap<String, String>();
	    String prevLemma, curLemma;
	    int pos = 0;
        
	    for (int i = 0; i < tokens.length; i++) {
	      String token = tokens[i].getToken();
	      if (!token.trim().equals("")) {
	        // avoid "..." etc. to be matched:
	        boolean isWord = true;
	        boolean hasLemma = true;
	        
          if (token.length() < 2) {
            isWord = false;
          }
	        
          int readingsLen = tokens[i].getReadingsLength();
	        for (int k = 0; k < readingsLen; k++) {
	        	String posTag = tokens[i].getAnalyzedToken(k).getPOSTag();
	        	if (posTag != null) {
	        	if (posTag.equals("")) {
	        		isWord = false;
	        		break;
	        	}
           //FIXME: too many false alarms here:     
                String lemma = tokens[i].getAnalyzedToken(k).getLemma();
                if (lemma == null) {
                  hasLemma = false;
                  break;
                }
                Matcher m1 = EXC_WORDS.matcher(lemma);
                if (m1.matches()) {
                    isWord = false;
                    break;
                 }
        
                Matcher m2 = EXC_POS.matcher(posTag);
                if (m2.matches()) {
	        		isWord = false;
	        		break;
	        	 }
                } else {
                    hasLemma = false;
                }
                                       		    
	        }

            Matcher m1 = EXC_NONWORDS.matcher(tokens[i].getToken());
            if (m1.matches()) {
                isWord = false;
            }
	        
	        prevLemma = "";
	        if (isWord) {
	           for (int j = 0; j < readingsLen; j++) {
	        	   if (hasLemma) {
	        	   curLemma = tokens[i].getAnalyzedToken(j).getLemma();
	        	   if (!prevLemma.equals(curLemma)) {
	        	   if (inflectedWords.containsKey(curLemma)) {
	        		   repetition = true;
      	       	   } else {	        			   	           
      	       		   inflectedWords.put(tokens[i].getAnalyzedToken(j).getLemma(), 
                           tokens[i].getToken());
      	       	   }
	        	   }
	        	   prevLemma = curLemma;
	        	   } else {
	        		   if (inflectedWords.containsKey(tokens[i].getToken())) {
	        			   repetition = true;
	        		   } else {
	        			   inflectedWords.put(tokens[i].getToken(), 
                               tokens[i].getToken());
	        		   }
	        	   }
	        	   
	           }
	        }
	        
	         if (repetition) {
	          String msg = "Powtórzony wyraz w zdaniu";
	          RuleMatch ruleMatch = new RuleMatch(this, pos, pos+token.length(), msg);
	          ruleMatch.setSuggestedReplacement(tokens[i].getToken());
	          ruleMatches.add(ruleMatch);
	          repetition = false;
	        }
	      }
	      pos += token.length();
	    }
	    return toRuleMatchArray(ruleMatches);
	  }
	

	/* (non-Javadoc)
	 * @see de.danielnaber.languagetool.rules.Rule#reset()
	 */
	@Override
	public void reset() {
		// nothing

	}

}