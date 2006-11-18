/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.rules.de;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.rules.Category;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * Checks that compounds (if in the list) are not written as separate words.
 * 
 * @author Daniel Naber
 */
public class CompoundRule extends GermanRule {

  private static final String FILE_NAME = "resource" +File.separator+ "de" +File.separator+
    "compounds.txt";
  
  private Set incorrectCompounds = null;
  
  public CompoundRule(final ResourceBundle messages) throws IOException {
    if (messages != null)
      super.setCategory(new Category(messages.getString("category_misc")));
    incorrectCompounds = loadCompoundFile(FILE_NAME, "UTF-8");
  }
  
  public String getId() {
    return "DE_COMPOUNDS";
  }

  public String getDescription() {
    return "Zusammenschreibung von Komposita, z.B. 'CD-ROM' statt 'CD ROM'";
  }

  public RuleMatch[] match(final AnalyzedSentence text) {
    List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    int pos = 0;
    AnalyzedTokenReadings prevToken = null;
    for (int i = 0; i < tokens.length; i++) {
      AnalyzedTokenReadings token = tokens[i];
      String tokenStr = tokens[i].getToken();
      if (prevToken == null) {
        prevToken = token;
        continue;
      }
      String stringtoCheck = prevToken.getToken() + " "  + tokenStr;
      if (incorrectCompounds.contains(stringtoCheck)) {
        String msg = "Komposita werden üblicherweise zusammen oder mit Bindestrich geschrieben.";
        RuleMatch ruleMatch = new RuleMatch(this, prevToken.getStartPos(), 
            token.getStartPos() + token.getToken().length(), msg);
        List<String> repl = new ArrayList<String>();
        repl.add(prevToken.getToken() + "-" + tokenStr);
        if (!StringTools.isAllUppercase(tokenStr)) {
          repl.add(prevToken.getToken() + tokenStr.toLowerCase());
        }
        ruleMatch.setSuggestedReplacements(repl);
        ruleMatches.add(ruleMatch);
      }
      prevToken = token;
      pos += tokens[i].getToken().length();
    }
    return toRuleMatchArray(ruleMatches);
  }

  public void reset() {
  }

  private Set loadCompoundFile(final String filename, final String encoding) throws IOException {
    InputStreamReader isr = null;
    BufferedReader br = null;
    FileInputStream fis = null;
    Set<String> words = new HashSet<String>();
    try {
      fis = new FileInputStream(filename);
      isr = new InputStreamReader(fis, encoding);
      br = new BufferedReader(isr);
      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.equals("") || line.startsWith("#"))    // "#" starts a comment
          continue;
        // the set contains the incorrect spellings, i.e. the ones without hyphen
        line = line.replace('-', ' ');
        words.add(line);
      }
    } finally {
      if (br != null) br.close();
      if (isr != null) isr.close();
      if (fis != null) fis.close();
    }
    return words;
  }

}
