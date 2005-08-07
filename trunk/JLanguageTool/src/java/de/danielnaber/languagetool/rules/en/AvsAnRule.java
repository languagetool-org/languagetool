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
package de.danielnaber.languagetool.rules.en;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;

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

  private Set requiresA;
  private Set requiresAn;
  
  public AvsAnRule() throws IOException {
    String path = "rules" + File.separator + "en" + File.separator;
    requiresA = loadWords(path + "det_a.txt");
    requiresAn = loadWords(path + "det_an.txt");
  }
  
  public String getId() {
    return "EN_A_VS_AN";
  }

  public String getDescription() {
    return "Use of 'a' vs. 'an'";
  }

  public Language getLanguage() {
    return Language.ENGLISH;
  }

  public RuleMatch[] match(AnalyzedSentence text) {
    List ruleMatches = new ArrayList();
    List tokens = text.getTokens();
    String prevToken = "";
    int pos = 0;
    int prevPos = 0;
    for (Iterator iter = tokens.iterator(); iter.hasNext();) {
      String token = (String) iter.next();
      if (token.trim().equals("")) {
        // ignore
      } else {
        char tokenFirstChar = token.charAt(0);
        boolean doesRequireA = false;
        boolean doesRequireAn = false;
        // check for exceptions:
        boolean isException = false;
        if (requiresA.contains(token.toLowerCase())) {
          isException = true;
          doesRequireA = true;
        }
        if (requiresAn.contains(token.toLowerCase())) {
          if (isException) {
            throw new IllegalStateException("");
          }
          isException = true;
          doesRequireAn = true;
        }
        if (!isException) {
          if (isVowel(tokenFirstChar)) {
            doesRequireAn = true;
          } else {
            doesRequireA = true;
          }
        }
        //System.err.println(prevToken + " " +token + ", a="+doesRequireA + ", an="+doesRequireAn);
        String msg = null;
        if (prevToken.toLowerCase().equals("a") && doesRequireAn) {
          msg = "Use <suggestion>an</suggestion> instead of <old>a</old> if the following "+
          "word starts with a vowel sound, e.g. <i>an article</i>, "+
          "<i>an hour</i>";
        } else if (prevToken.toLowerCase().equals("an") && doesRequireA) {
          msg = "Use <suggestion>a</suggestion> instead of <old>an</old> if the following "+
          "word doesn't start with a vowel sound, e.g. <i>a sentence</i>, "+
          "<i>a university</i>";
        }
        if (msg != null) {
          RuleMatch ruleMatch = new RuleMatch(this, prevPos, prevPos+prevToken.length(), msg);
          ruleMatches.add(ruleMatch);
        }
        prevToken = token;
        prevPos = pos;
      }
      pos += token.length();
    }
    return (RuleMatch[])ruleMatches.toArray(new RuleMatch[0]);
  }

  private boolean isVowel(char c) {
    c = Character.toLowerCase(c);
    return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u'; 
  }
  
  private Set loadWords(String filename) throws IOException {
    FileReader fr = null;
    BufferedReader br = null;
    Set set = new HashSet();
    try {
      fr = new FileReader(filename);
      br = new BufferedReader(fr);
      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.startsWith("#"))       // ignore comments
          continue;
        set.add(line);
      }
    } finally {
      if (br != null) br.close();
      if (fr != null) fr.close();
    }
    return set;
  }

}
