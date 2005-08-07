/* JLanguageTool, a natural language style checker 
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
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * A Rule that drescribes a language error as a simple pattern of words or their
 * part-of-speech tags.
 * 
 * @author Daniel Naber
 */
public class PatternRule extends Rule {

  private String id;
  private Language language;
  private String pattern;
  private String description;

  private Element[] patternElements;

  PatternRule(String id, Language language, String pattern, String description) {
    this.id = id;
    this.language = language;
    this.pattern = pattern;
    this.description = description;
  }

  public String getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public Language getLanguage() {
    return language;
  }

  public String toString() {
    return id + ":" + pattern + ":" + description;
  }

  public RuleMatch[] match(AnalyzedSentence text) {
    List ruleMatches = new ArrayList(); 
    AnalyzedToken[] tokens = text.getTokensWithoutWhitespace();
    if (patternElements == null) {
      patternElements = getPatternElements(pattern); 
    }
    int tokenPos = 0;
    AnalyzedToken firstMatchToken = null;
    AnalyzedToken lastMatchToken = null;

    for (int i = 0; i < tokens.length; i++) {
      boolean allElementsMatch = true;
      for (int k = 0; k < patternElements.length; k++) {
        Element elem = patternElements[k];
        int nextPos = tokenPos+k;
        if (nextPos >= tokens.length) {
          allElementsMatch = false;
          break;
        }
        AnalyzedToken matchToken = tokens[nextPos];
        if (!elem.match(matchToken)) {
          allElementsMatch = false;
          break;
        } else {
          lastMatchToken = matchToken;
          if (firstMatchToken == null)
            firstMatchToken = matchToken;
        }
      }
      if (allElementsMatch) {
        //System.err.println("--->Match: " + this + ", t="+token);
        RuleMatch ruleMatch = new RuleMatch(this, firstMatchToken.getStartPos(), 
            lastMatchToken.getStartPos()+lastMatchToken.getToken().length(), description);
        ruleMatches.add(ruleMatch);
      }
      tokenPos++;
    }      
    return (RuleMatch[])ruleMatches.toArray(new RuleMatch[0]);
  }

  private Element[] getPatternElements(String pattern) {
    List elements = new ArrayList();
    pattern = pattern.replaceAll("[\\(\\)]", "");
    String[] parts = pattern.split("\\s+");
    for (int i = 0; i < parts.length; i++) {
      String element = parts[i];
      if (element.startsWith("\"") && !element.endsWith("\"") || element.endsWith("\"") && !element.startsWith("\"")) {
        throw new IllegalArgumentException("Invalid pattern '" + pattern + "': unbalanced quote");
      }
      if (element.startsWith("\"") && element.endsWith("\"")) {         // cut off quotes
        element = element.substring(1, element.length()-1);
        String tokenParts[] = element.split("\\|");
        // TODO: make case sensitiviy optional:
        StringElement stringElement = new StringElement(tokenParts, false); 
        elements.add(stringElement);
      } else {
        throw new IllegalArgumentException("Unknown type in pattern: " + pattern);
      }
    }
    return (Element[])elements.toArray(new Element[0]);
  }
  
}
