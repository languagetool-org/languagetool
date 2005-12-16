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
package de.danielnaber.languagetool.rules.de;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * A rule that matches words for which two different spellings are used
 * throughout the document. Currently only implemented for German. Loads
 * the relavant word from <code>rules/de/coherency.txt</code>.
 * 
 * <p>Note that this should not be used for language variations like
 * American English vs. British English or German "alte Rechtschreibung"
 * vs. "neue Rechtschreibung" -- that's the task of a spell checker.
 * 
 * @author Daniel Naber
 */
public class WordCoherencyRule extends GermanRule {

  private final static String FILE_NAME = "rules" +File.separator+ "de" +File.separator+ "coherency.txt";
  private final static String FILE_ENCODING = "utf-8";
  
  private Map relevantWords;        // e.g. "aufwendig -> aufwändig"
  private Map shouldNotAppearWord = new HashMap();  // e.g. aufwändig -> RuleMatch of aufwendig

  static private GermanLemmatizer lemmatizer = null;

  public WordCoherencyRule() throws IOException {
    relevantWords = loadWords(JLanguageTool.getAbsoluteFile(FILE_NAME)); 
    lemmatizer = new GermanLemmatizer();
  }
  
  public String getId() {
    return "WORD_COHERENCY";
  }

  public String getDescription() {
    return "Use always the same spelling for a word if more than one spelling is valid.";
  }

  public RuleMatch[] match(AnalyzedSentence text) {
    List ruleMatches = new ArrayList();
    AnalyzedToken[] tokens = text.getTokens();
    int pos = 0;
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i].getToken();
      if (token.trim().equals("")) {
        // ignore
      } else {
        String origToken = token;
        String baseform = lemmatizer.getBaseform(token);
        if (baseform != null)
          token = baseform;
        if (shouldNotAppearWord.containsKey(token)) {
          RuleMatch otherMatch = (RuleMatch)shouldNotAppearWord.get(token);
          String otherSpelling = otherMatch.getMessage();
          String msg = "You should probably not use <i>" +token+ "</i> and <i>" +otherSpelling+
            "</i> in the same document, stick to one spelling";
          RuleMatch ruleMatch = new RuleMatch(this, pos, pos+origToken.length(), msg);
          ruleMatch.setSuggestedReplacement(otherSpelling);
          ruleMatches.add(ruleMatch);
        } else if (relevantWords.containsKey(token)) {
          String shouldNotAppear = (String)relevantWords.get(token);
          // only used to display this spelling variation if the other one really occurs:
          String msg = token;
          RuleMatch potentialRuleMatch = new RuleMatch(this, pos, pos+origToken.length(), msg);
          shouldNotAppearWord.put(shouldNotAppear, potentialRuleMatch);
        }
      }
      pos += token.length();
    }
    return toRuleMatchArray(ruleMatches);
  }

  private Map loadWords(File file) throws IOException {
    Map map = new HashMap();
    FileInputStream fis = null;
    InputStreamReader isr = null;
    BufferedReader br = null;
    try {
      fis = new FileInputStream(file);
      isr = new InputStreamReader(fis, FILE_ENCODING);
      br = new BufferedReader(isr);
      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.startsWith("#"))       // ignore comments
          continue;
        String[] parts = line.split(";");
        if (parts.length != 2) {
          throw new IOException("Format error in file " +file.getAbsolutePath()+ ", line: " + line);
        }
        map.put(parts[0], parts[1]);
        map.put(parts[1], parts[0]);
      }
    } finally {
      if (br != null) br.close();
      if (isr != null) isr.close();
      if (fis != null) fis.close();
    }
    return map;
  }
  
  public void reset() {
    shouldNotAppearWord = new HashMap();
  }

}
