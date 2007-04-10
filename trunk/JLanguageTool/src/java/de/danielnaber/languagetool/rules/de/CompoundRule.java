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
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
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

  private static final String FILE_NAME = "/resource/de/compounds.txt";
  
  private final static int MAX_TERMS = 5;
  
  private Set<String> incorrectCompounds = new HashSet<String>();
  private Set<String> noDashSuggestion = new HashSet<String>();
  private Set<String> onlyDashSuggestion = new HashSet<String>();
  
  public CompoundRule(final ResourceBundle messages) throws IOException {
    if (messages != null)
      super.setCategory(new Category(messages.getString("category_misc")));
    loadCompoundFile(this.getClass().getResourceAsStream(FILE_NAME), "UTF-8");
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
    
    RuleMatch prevRuleMatch = null;
    Queue<AnalyzedTokenReadings> prevTokens = new ArrayBlockingQueue<AnalyzedTokenReadings>(MAX_TERMS);
    for (int i = 0; i < tokens.length + MAX_TERMS-1; i++) {
      AnalyzedTokenReadings token = null;
      // we need to extend the token list so we find matches at the end of the original list:
      if (i >= tokens.length)
        token = new AnalyzedTokenReadings(new AnalyzedToken("", "", prevTokens.peek().getStartPos()));
      else
        token = tokens[i];
      if (i == 0) {
        addToQueue(token, prevTokens);
        continue;
      }
      
      StringBuilder sb = new StringBuilder();
      int j = 0;
      AnalyzedTokenReadings firstMatchToken = null;
      List<String> stringsToCheck = new ArrayList<String>();
      List<String> origStringsToCheck = new ArrayList<String>();    // original upper/lowercase spelling
      Map<String, AnalyzedTokenReadings> stringToToken = new HashMap<String, AnalyzedTokenReadings>();
      for (Iterator iter = prevTokens.iterator(); iter.hasNext();) {
        AnalyzedTokenReadings atr = (AnalyzedTokenReadings) iter.next();
        if (j == 0)
          firstMatchToken = atr;
        sb.append(" ");
        sb.append(atr.getToken());
        if (j >= 1) {
          String stringtoCheck = normalize(sb.toString());
          stringsToCheck.add(stringtoCheck);
          origStringsToCheck.add(sb.toString().trim());
          if (!stringToToken.containsKey(stringtoCheck))
            stringToToken.put(stringtoCheck, atr);
        }
        j++;
      }
      // iterate backwards over all potentially incorrect strings to make
      // sure we match longer strings first:
      for (int k = stringsToCheck.size()-1; k >= 0; k--) {
        String stringToCheck = stringsToCheck.get(k);
        String origStringToCheck = origStringsToCheck.get(k);
        //System.err.println("##"+stringtoCheck+"#");
        if (incorrectCompounds.contains(stringToCheck)) {
          AnalyzedTokenReadings atr = stringToToken.get(stringToCheck);
          String msg = null;
          List<String> repl = new ArrayList<String>();
          if (!noDashSuggestion.contains(stringToCheck)) {
            repl.add(origStringToCheck.replace(' ', '-'));
            msg = "Dieses Kompositum wird mit Bindestrich geschrieben.";
          }
          // assume that compounds with more than two parts should always use hyphens:
          if (!hasAllUppercaseParts(origStringToCheck) && countParts(stringToCheck) <= 2
              && !onlyDashSuggestion.contains(stringToCheck)) {
            repl.add(mergeCompound(origStringToCheck));
            msg = "Dieses Kompositum wird zusammengeschrieben.";
          }
          String[] parts = stringToCheck.split(" ");
          if (parts.length > 0 && parts[0].length() == 1) {
            repl.clear();
            repl.add(origStringToCheck.replace(' ', '-'));
            msg = "Dieses Kompositum wird mit Bindestrich geschrieben.";
          } else if (repl.size() == 0 || repl.size() == 2) {     // == 0 shouldn't happen
            msg = "Dieses Kompositum wird zusammen oder mit Bindestrich geschrieben.";
          }
          RuleMatch ruleMatch = new RuleMatch(this, firstMatchToken.getStartPos(), 
              atr.getStartPos() + atr.getToken().length(), msg);
          // avoid duplicate matches:
          if (prevRuleMatch != null && prevRuleMatch.getFromPos() == ruleMatch.getFromPos()) {
            prevRuleMatch = ruleMatch;
            break;
          }
          prevRuleMatch = ruleMatch;
          ruleMatch.setSuggestedReplacements(repl);
          ruleMatches.add(ruleMatch);
          break;
        }
      }
      addToQueue(token, prevTokens);
    }
    return toRuleMatchArray(ruleMatches);
  }

  private String normalize(String str) {
    str = str.trim().toLowerCase();
    if (str.indexOf('-') != -1 && str.indexOf(' ') != -1) {
      // e.g. "E-Mail Adresse" -> "E Mail Adresse" so the error can be detected:
      str = str.replace('-', ' ');
    }
    return str;
  }

  private boolean hasAllUppercaseParts(String str) {
    String[] parts = str.split(" ");
    for (int i = 0; i < parts.length; i++) {
      if (StringTools.isAllUppercase(parts[i])) {
        return true;
      }
    }
    return false;
  }

  private int countParts(String str) {
    String[] parts = str.split(" ");
    return parts.length;
  }

  private String mergeCompound(String str) {
    String[] stringParts = str.split(" ");
    StringBuilder sb = new StringBuilder();
    for (int k = 0; k < stringParts.length; k++) {
      if (k == 0)
        sb.append(stringParts[k]);
      else
        sb.append(stringParts[k].toLowerCase());
    }
    return sb.toString();
  }

  private void addToQueue(AnalyzedTokenReadings token, Queue<AnalyzedTokenReadings> prevTokens) {
    boolean inserted = prevTokens.offer(token);
    if (!inserted) {
      prevTokens.poll();
      prevTokens.offer(token);
    }
  }

  private void loadCompoundFile(final InputStream file, final String encoding) throws IOException {
    InputStreamReader isr = null;
    BufferedReader br = null;   
    try {
      isr = new InputStreamReader(file, encoding);
      br = new BufferedReader(isr);
      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.equals("") || line.startsWith("#"))    // "#" starts a comment
          continue;
        // the set contains the incorrect spellings, i.e. the ones without hyphen
        line = line.replace('-', ' ');
        String[] parts = line.split(" ");
        if (parts.length > MAX_TERMS)
          throw new IOException("Too many compound parts: " + line + ", maximum allowed: " + MAX_TERMS);
        if (parts.length == 1)
          throw new IOException("Not a compound: " + line);
        if (line.endsWith("+")) {
          line = line.substring(0, line.length() - 1);    // cut off "+"
          noDashSuggestion.add(line.toLowerCase());
        } else if (line.endsWith("*")) {
          line = line.substring(0, line.length() - 1);    // cut off "*"
          onlyDashSuggestion.add(line.toLowerCase());
        }
        incorrectCompounds.add(line.toLowerCase());
      }
    } finally {
      if (br != null) br.close();
      if (isr != null) isr.close();
    }
  }

  public void reset() {
  }

}
