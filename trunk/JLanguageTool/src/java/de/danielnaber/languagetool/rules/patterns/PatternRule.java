/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package de.danielnaber.languagetool.rules.patterns;

import java.util.ArrayList;
import java.util.List;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * A Rule that drescribes a language error as a simple pattern of words or their
 * part-of-speech tags.
 * 
 * @author Daniel Naber
 */
public class PatternRule extends Rule {

  private String id;
  private Language[] language;
  private String pattern;
  private String description;
  private String message;
  private int startPositionCorrection = 0;
  private int endPositionCorrection = 0;

  private boolean caseSensitive = false;
  private boolean regExp = false;

  private Element[] patternElements;

  PatternRule(String id, Language language, String pattern, String description, String message) {
    if (id == null)
      throw new NullPointerException("id cannot be null");
    if (language == null)
      throw new NullPointerException("language cannot be null");
    if (pattern == null)
      throw new NullPointerException("pattern cannot be null");
    if (description == null)
      throw new NullPointerException("description cannot be null");
    this.id = id;
    this.language = new Language[] { language };
    this.pattern = pattern;
    this.description = description;
    this.message = message;
  }
  
  public String getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public String getMessage() {
    return message;
  }

  public Language[] getLanguages() {
    return language;
  }

  public String toString() {
    return id + ":" + pattern + ":" + description;
  }

  public String getPattern() {
    return pattern;
  }

  public boolean getCaseSensitive() {
    return caseSensitive;
  }
  
  public void setCaseSensitive(boolean caseSensitive) {
	    this.caseSensitive = caseSensitive;
	  }
  
  public boolean getregExpSetting() {
	    return regExp;
	  }

  public void setregExpSetting(boolean regExp) {
    this.regExp = regExp;
  }
  
  public int getStartPositionCorrection() {
    return startPositionCorrection;
  }

  public void setStartPositionCorrection(int startPositionCorrection) {
    this.startPositionCorrection = startPositionCorrection;
  }

  public int getEndPositionCorrection() {
    return endPositionCorrection;
  }

  public void setEndPositionCorrection(int endPositionCorrection) {
    this.endPositionCorrection = endPositionCorrection;
  }

  public void addPatternElements(List elements) {
	  List<Element> elems = new ArrayList<Element>();
	  if (this.patternElements!=null)
	  for (int i = 0; i < this.patternElements.length; i++) {
		elems.add(this.patternElements[i]);  
	  }
	  for(int i=0;i<elements.size();i++)
	  {
		  elems.add((Element)elements.get(i));
	  }
	  this.patternElements=(Element[])elems.toArray(new Element[0]);
  }
  
  public RuleMatch[] match(AnalyzedSentence text) {
    List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>(); 
    AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    if (patternElements == null) {      // lazy init
      patternElements = getPatternElements(pattern); 
    }
    int tokenPos = 0;
    
    int firstMatchToken = -1;
    int lastMatchToken = -1;

    for (int i = 0; i < tokens.length; i++) {
         	 boolean allElementsMatch = true;
    	 int matchingTokens = 0;
    	 for (int k = 0; k < patternElements.length; k++) {
    		 Element elem = patternElements[k];
    		 int nextPos = tokenPos+k;
    		 if (nextPos >= tokens.length) {
    			 allElementsMatch = false;
    			 break;
    		 }
    		 boolean Match = false;
    		 for (int l = 0; l < tokens[nextPos].getReadingslength(); l++) {
    			 AnalyzedToken matchToken = tokens[nextPos].getAnalyzedToken(l);
    			 //Logical OR (cannot be AND):
    			 if (!elem.match(matchToken)) {
    				 Match = Match || false;
    			 }
    			 else {
    				 Match = true;
    			 }
    			 allElementsMatch = Match;
    		 }
    		 if (!allElementsMatch) {
          break;
        } else {
          matchingTokens++;
          lastMatchToken = nextPos;
          if (firstMatchToken == -1)
            firstMatchToken = nextPos;
        }
      }
      if (allElementsMatch) {
        String errMessage = message;
        // replace back references like \1 in message:
        for (int j = 0; j < matchingTokens; j++) {
          errMessage = errMessage.replaceAll("\\\\"+(j+1),
              tokens[firstMatchToken+j].getToken());
        }
        boolean startsWithUppercase = 
          StringTools.startsWithUppercase(tokens[firstMatchToken+startPositionCorrection].toString());
        RuleMatch ruleMatch = new RuleMatch(this,
            tokens[firstMatchToken+startPositionCorrection].getStartPos(), 
            tokens[lastMatchToken+endPositionCorrection].getStartPos()+
            tokens[lastMatchToken+endPositionCorrection].getToken().length(), errMessage,
            startsWithUppercase);
        ruleMatches.add(ruleMatch);
      } else {
        firstMatchToken = -1;
        lastMatchToken = -1;
      }
      tokenPos++;
    }
  
    return (RuleMatch[])ruleMatches.toArray(new RuleMatch[0]);
  }

  private Element[] getPatternElements(String pattern) {
    List<Element> elements = new ArrayList<Element>();
    pattern = pattern.replaceAll("[\\(\\)]", "");       // just ignore parentheses
    String[] parts = pattern.split("\\s+");
    for (int i = 0; i < parts.length; i++) {
      String element = parts[i];
      boolean negation = false;
      if (element.startsWith("^")) {
        negation = true;
        element = element.substring(1);     // cut off "^"
      }
      if ((element.startsWith("\"") && !element.endsWith("\"")) || (element.endsWith("\"") && !element.startsWith("\""))) {
        throw new IllegalArgumentException("Invalid pattern '" + pattern + "': unbalanced quote");
      }
      if (element.startsWith("\"") && element.endsWith("\"")) {         // cut off quotes
        element = element.substring(1, element.length()-1);
        String tokenParts[] = element.split("\\|");
        StringElement stringElement = new StringElement(tokenParts, caseSensitive, false, false); 
        stringElement.setNegation(negation);
        elements.add(stringElement);
      } else if (Character.isUpperCase(element.charAt(0))) {
        // uppercase = POS tag (except: see above)
        String tokenParts[];
        String exceptions[] = null;
        POSElement posElement;
        if (element.indexOf("^") != -1) {
          tokenParts = element.substring(0, element.indexOf("^")).split("\\|");
          exceptions = element.substring(element.indexOf("^")+1).split("\\|");
        } else {
          tokenParts = element.split("\\|");
        }
        posElement = new POSElement(tokenParts, caseSensitive, true, exceptions);
        posElement.setNegation(negation);
        elements.add(posElement);
      } else {
        throw new IllegalArgumentException("Unknown type " + element + " in pattern: " + pattern);
      }
    }
    return (Element[])elements.toArray(new Element[0]);
  }
  
  public void reset() {
    // nothing
  }

}
