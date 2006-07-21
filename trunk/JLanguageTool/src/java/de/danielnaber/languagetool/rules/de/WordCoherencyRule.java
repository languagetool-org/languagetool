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
import de.danielnaber.languagetool.AnalyzedTokenReadings;
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
  
  private Map<String, String> relevantWords;        // e.g. "aufwendig -> aufwändig"
  private Map<String, RuleMatch> shouldNotAppearWord = new HashMap<String, RuleMatch>();  // e.g. aufwändig -> RuleMatch of aufwendig

  public WordCoherencyRule() throws IOException {
    relevantWords = loadWords(JLanguageTool.getAbsoluteFile(FILE_NAME)); 
  }
  
  public String getId() {
    return "DE_WORD_COHERENCY";
  }

  public String getDescription() {
    return "Einheitliche Schreibweise für Wörter mit mehr als einer korrekten Schreibweise";
  }

  public RuleMatch[] match(AnalyzedSentence text) {
    List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    AnalyzedTokenReadings[] tokens = text.getTokens();
    int pos = 0;
    for (int i = 0; i < tokens.length; i++) {
    	//TODO: definitely should be changed
    	//if the general lemmatizer is working
    	//defaulting to the first element because the
    	//general German lemmatizer is not (yet) there
      String token = tokens[i].getToken();
      if (token.trim().equals("")) {
        // ignore
      } else {
        String origToken = token;
        List<AnalyzedToken> readings = tokens[i].getReadings();
        // TODO: in theory we need to care about the other readings, too:
        if (readings != null && readings.size() > 0) {
          String baseform = readings.get(0).getLemma();
          if (baseform != null)
            token = baseform;
        }
        if (shouldNotAppearWord.containsKey(token)) {
          RuleMatch otherMatch = (RuleMatch)shouldNotAppearWord.get(token);
          String otherSpelling = otherMatch.getMessage();
          String msg = "<b>" +token+ "</b> und <b>" +otherSpelling+
            "</b> sollten nicht gleichzeitig benutzt werden";
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
      pos += tokens[i].getToken().length();
    }
    return toRuleMatchArray(ruleMatches);
  }

  private Map<String, String> loadWords(File file) throws IOException {
    Map<String, String> map = new HashMap<String, String>();
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
    shouldNotAppearWord = new HashMap<String, RuleMatch>();
  }

}
