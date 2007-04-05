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
package de.danielnaber.languagetool.rules.en;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import de.danielnaber.languagetool.AnalyzedSentence;
//import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.rules.Category;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * Check if the determiner (if any) preceding a word is:
 * <ul>
 *   <li><i>an</i> if the next word starts with a vowel
 *   <li><i>a</i> if the next word does not start with a vowel
 * </ul>
 *  This rule loads some exceptions from external files (e.g. <i>an hour</i>).
 *   
 * @author Daniel Naber
 */
public class AvsAnRule extends EnglishRule {

  private static final String FILENAME_A = "rules" +File.separator+ "en" +File.separator+ "det_a.txt";
  private static final String FILENAME_AN = "rules" +File.separator+ "en" +File.separator+ "det_an.txt";

  private Set<String> requiresA;
  private Set<String> requiresAn;
  
  public AvsAnRule(final ResourceBundle messages) throws IOException {
    if (messages != null)
      super.setCategory(new Category(messages.getString("category_misc")));
    requiresA = loadWords(JLanguageTool.getAbsoluteFile(FILENAME_A));
    requiresAn = loadWords(JLanguageTool.getAbsoluteFile(FILENAME_AN));
  }
  
  public String getId() {
    return "EN_A_VS_AN";
  }

  public String getDescription() {
    return "Use of 'a' vs. 'an'";
  }

  public RuleMatch[] match(final AnalyzedSentence text) {
    List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    AnalyzedTokenReadings[] tokens = text.getTokens();
    String prevToken = "";
    int pos = 0;
    int prevPos = 0;
    for (int i = 0; i < tokens.length; i++) {
    	//defaulting to the first token
    	//the rule is based on spelling
    	//so it should be safe
      String token = tokens[i].getAnalyzedToken(0).getToken();
      String origToken = token;
      if (token.trim().equals("")) {
        // ignore
      } else {
        boolean doesRequireA = false;
        boolean doesRequireAn = false;
        // check for exceptions:
        boolean isException = false;
        String[] parts = token.split("[-']");  // for example, in "one-way" only "one" is relevant
        if (parts.length >= 1 &&
            !parts[0].equalsIgnoreCase("a")) {  // avoid false alarm on "A-levels are..."
          token = parts[0];
        }
        //html entities!
        token = token.replaceAll("&quot|&amp|&lt|&gt|[^a-zA-Z0-9]", "");         // e.g. >>an "industry party"<<
        if (token.length() == 0) {
          pos += origToken.length();
          continue;
        }
        char tokenFirstChar = token.charAt(0);
        if (requiresA.contains(token.toLowerCase()) || requiresA.contains(token)) {
          isException = true;
          doesRequireA = true;
        }
        if (requiresAn.contains(token.toLowerCase()) || requiresAn.contains(token)) {
          if (isException) {
            throw new IllegalStateException(token + " is listed in both det_a.txt and det_an.txt");
          }
          isException = true;
          doesRequireAn = true;
        }
        if (!isException) {
          if (StringTools.isAllUppercase(token)) {
            // we don't know how all-uppercase words (often abbreviations) are pronounced, 
            // so never complain about these:
            doesRequireAn = false;
            doesRequireA = false;
          } else if (isVowel(tokenFirstChar)) {
            doesRequireAn = true;
          } else {
            doesRequireA = true;
          }
        }
        //System.err.println(prevToken + " " +token + ", a="+doesRequireA + ", an="+doesRequireAn);
        //TODO: add exception for 'A and B are cities...'
        String msg = null;
        if (prevToken.equalsIgnoreCase("a") && doesRequireAn) {
          String repl = "an";
          if (prevToken.equals("A"))
            repl = "An";
          msg = "Use <suggestion>" +repl+ "</suggestion> instead of '" +prevToken+ "' if the following "+
          "word starts with a vowel sound, e.g. 'an article', "+
          "'an hour'";
        } else if (prevToken.equalsIgnoreCase("an") && doesRequireA) {
          String repl = "a";
          if (prevToken.equals("An"))
            repl = "A";
          msg = "Use <suggestion>" +repl+ "</suggestion> instead of '" +prevToken+ "' if the following "+
          "word doesn't start with a vowel sound, e.g. 'a sentence', "+
          "'a university'";
        }
        if (msg != null) {
          RuleMatch ruleMatch = new RuleMatch(this, prevPos, prevPos+prevToken.length(), msg);
          ruleMatches.add(ruleMatch);
        }
        prevToken = token;
        prevPos = pos;
      }
      pos += origToken.length();
    }
    return toRuleMatchArray(ruleMatches);
  }

  private boolean isVowel(char c) {
    c = Character.toLowerCase(c);
    return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u'; 
  }
  
  /**
   * Load words, normalized to lowercase.
   */
  private Set<String> loadWords(final File file) throws IOException {
    FileReader fr = null;
    BufferedReader br = null;
    Set<String> set = new HashSet<String>();
    try {
      fr = new FileReader(file);
      br = new BufferedReader(fr);
      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.charAt(0) == '#')       // ignore comments
          continue;
        if (line.charAt(0) == '*')       // case sensitive
          set.add(line.substring(1));
        else
          set.add(line.toLowerCase());
      }
    } finally {
      if (br != null) br.close();
      if (fr != null) fr.close();
    }
    return set;
  }

  public void reset() {
    // nothing
  }

}
