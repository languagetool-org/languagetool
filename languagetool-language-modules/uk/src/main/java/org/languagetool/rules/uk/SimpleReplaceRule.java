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
package org.languagetool.rules.uk;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.AnalyzedToken;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.Category;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;

/**
 * A rule that matches words which should not be used and suggests
 * correct ones instead. 
 * 
 * Ukrainian implementations. Loads the
 * relevant words from <code>rules/uk/replace.txt</code>.
 * 
 * @author Andriy Rysin
 */
public class SimpleReplaceRule extends Rule {

  private static final String FILE_ENCODING = "utf-8";
  private static final String FILE_NAME = "/uk/replace.txt";

  private final Map<String, List<String>> wrongWords;

  public final String getFileName() {
    return FILE_NAME;
  }
  
  public SimpleReplaceRule(final ResourceBundle messages) throws IOException {
    if (messages != null) {
      super.setCategory(new Category(messages.getString("category_misc")));
    }
    wrongWords = loadWords(JLanguageTool.getDataBroker().getFromRulesDirAsStream(getFileName()));
  }

  @Override
  public final String getId() {
    return "UK_SIMPLE_REPLACE";
  }

 @Override
  public String getDescription() {
    return "Пошук помилкових слів";
  }

  public String getShort() {
    return "Помилка?";
  }

  public String getSuggestion() {
    return " - помилкове слово, виправлення: ";
  }

  /**
   * Indicates if the rule is case-sensitive. 
   * @return true if the rule is case-sensitive, false otherwise.
   */
  public boolean isCaseSensitive() {
    return false;  
  }
  
  /**
   * @return the locale used for case conversion when {@link #isCaseSensitive()} is set to <code>false</code>.
   */
  public Locale getLocale() {
    return Locale.getDefault();
  }  
  
  public String getEncoding() {
    return FILE_ENCODING;
  }

  private String cleanup(String word) {
    if( ! isCaseSensitive() ) {
      word = word.toLowerCase(getLocale());
    }
    return word;
  }
  
  @Override
  public final RuleMatch[] match(final AnalyzedSentence text) {
    List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();

    for (AnalyzedTokenReadings tokenReadings: tokens) {
    	String tokenString = cleanup( tokenReadings.getToken() );

        if( ! wrongWords.containsKey(tokenString) ) {
          for(AnalyzedToken analyzedToken: tokenReadings.getReadings()) {
    	    String lemma = analyzedToken.getLemma();
    	    if( lemma != null ) {
    	      lemma = cleanup(lemma);
    	      if( wrongWords.containsKey(lemma) ) {
    	        tokenString = lemma;
    	        break;
    	      }
    	    }
          }
        }

    	List<String> replacements = wrongWords.get(tokenString);
      
    	if (replacements != null && replacements.size() > 0 ) {
    			RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, replacements);
    			ruleMatches.add(potentialRuleMatch);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private RuleMatch createRuleMatch(AnalyzedTokenReadings tokenReadings, List<String> replacements) {
  	String tokenString = tokenReadings.getToken();
  	String origToken = tokenString;
  	String msg = tokenString + getSuggestion() + StringUtils.join(replacements, ", ");
  	int pos = tokenReadings.getStartPos();

  	RuleMatch potentialRuleMatch = new RuleMatch(this, pos, pos + origToken.length(), msg, getShort());

		if (!isCaseSensitive() && StringTools.startsWithUppercase(tokenString)) {
			for(int i = 0; i < replacements.size(); i++) {
				replacements.set(i, StringTools.uppercaseFirstChar(replacements.get(i)));
			} 
  	}

  	potentialRuleMatch.setSuggestedReplacements(replacements);

  	return potentialRuleMatch;
  }

  private Map<String, List<String>> loadWords(final InputStream stream) throws IOException {
  	Map<String, List<String>> map = new HashMap<String, List<String>>();
    Scanner scanner = new Scanner(stream, getEncoding());
    
    try {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.length() < 1 || line.charAt(0) == '#') { //  # = comment
          continue;
        }
        
        if( ! isCaseSensitive() ) {
        	line = line.toLowerCase(getLocale());
        }
        
        String[] parts = line.split("=");
        if (parts.length != 2) {
          throw new IOException("Format error in file "
              + JLanguageTool.getDataBroker().getFromRulesDirAsUrl(getFileName()) + ", line: " + line);
        }
        
        String[] replacements = parts[1].split("\\|");
        
        map.put(parts[0], Arrays.asList(replacements));
      }
    } finally {
      scanner.close();
    }
    return map;
  }

  @Override
  public void reset() {
  }

}
