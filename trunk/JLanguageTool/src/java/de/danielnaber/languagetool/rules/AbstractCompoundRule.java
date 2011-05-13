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
package de.danielnaber.languagetool.rules;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * Checks that compounds (if in the list) are not written as separate words.
 * 
 * @author Daniel Naber & Marcin Miłkowski (refactoring)
 */

public abstract class AbstractCompoundRule extends Rule {

  private static final int MAX_TERMS = 5;  

  private final Set<String> incorrectCompounds = new HashSet<String>();
  private final Set<String> noDashSuggestion = new HashSet<String>();
  private final Set<String> onlyDashSuggestion = new HashSet<String>();

  private final String withHyphenMessage;
  private final String withoutHyphenMessage;
  private final String withOrWithoutHyphenMessage;

  private String shortDesc;

  /** Compounds with more than maxNoHyphensSize parts should always use hyphens */
  private int maxUnHyphenatedWordCount = 2;

  /** Flag to indicate if the hyphen is ignored in the text entered by the user.
   * Set this to false if you want the rule to offer suggestions for words like [ro] "câte-și-trei" (with hyphen), not only for "câte și trei" (with spaces)
   * This is only available for languages with hyphen as a word separator (ie: not available for english, available for Romanian)
   * See Language.getWordTokenizer()
   */
  private boolean hyphenIgnored = true;
  
  public AbstractCompoundRule(final ResourceBundle messages, final String fileName,
        final String withHyphenMessage, final String withoutHyphenMessage, final String withOrWithoutHyphenMessage) throws IOException {
    if (messages != null) {
      super.setCategory(new Category(messages.getString("category_misc")));
    }
    loadCompoundFile(JLanguageTool.getDataBroker().getFromResourceDirAsStream(fileName), "UTF-8");
    this.withHyphenMessage = withHyphenMessage;
    this.withoutHyphenMessage = withoutHyphenMessage;
    this.withOrWithoutHyphenMessage = withOrWithoutHyphenMessage;
  }

  @Override
  public abstract String getId();    

  @Override
  public abstract String getDescription();

  public void setShort(final String shortDescription) {
    shortDesc = shortDescription;
  }

  public boolean isHyphenIgnored() {
    return hyphenIgnored;
  }

  public void setHyphenIgnored(boolean ignoreHyphen) {
    this.hyphenIgnored = ignoreHyphen;
  }

  public int getMaxUnHyphenatedWordCount() {
    return maxUnHyphenatedWordCount;
  }

  public void setMaxUnHyphenatedWordCount(int maxNoHyphensSize) {
    this.maxUnHyphenatedWordCount = maxNoHyphensSize;
  }

  @Override
  public RuleMatch[] match(final AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();

    RuleMatch prevRuleMatch = null;
    final Queue<AnalyzedTokenReadings> prevTokens = new ArrayBlockingQueue<AnalyzedTokenReadings>(MAX_TERMS);
    for (int i = 0; i < tokens.length + MAX_TERMS-1; i++) {
      AnalyzedTokenReadings token = null;
      // we need to extend the token list so we find matches at the end of the original list:
      if (i >= tokens.length) {
        token = new AnalyzedTokenReadings(new AnalyzedToken("", "", null), prevTokens.peek().getStartPos());
      } else {
        token = tokens[i];
      }
      if (i == 0) {
        addToQueue(token, prevTokens);
        continue;
      }

      final StringBuilder sb = new StringBuilder();
      int j = 0;
      AnalyzedTokenReadings firstMatchToken = null;
      final List<String> stringsToCheck = new ArrayList<String>();
      final List<String> origStringsToCheck = new ArrayList<String>();    // original upper/lowercase spelling
      final Map<String, AnalyzedTokenReadings> stringToToken = new HashMap<String, AnalyzedTokenReadings>();
      for (AnalyzedTokenReadings atr : prevTokens) {
        if (j == 0) {
          firstMatchToken = atr;
        }
        sb.append(' ');
        sb.append(atr.getToken());
        if (j >= 1) {
          final String stringToCheck = normalize(sb.toString());
          stringsToCheck.add(stringToCheck);
          origStringsToCheck.add(sb.toString().trim());
          if (!stringToToken.containsKey(stringToCheck))
            stringToToken.put(stringToCheck, atr);
        }
        j++;
      }
      // iterate backwards over all potentially incorrect strings to make
      // sure we match longer strings first:
      for (int k = stringsToCheck.size()-1; k >= 0; k--) {
        final String stringToCheck = stringsToCheck.get(k);
        final String origStringToCheck = origStringsToCheck.get(k);
        if (incorrectCompounds.contains(stringToCheck)) {
          final AnalyzedTokenReadings atr = stringToToken.get(stringToCheck);
          String msg = null;
          final List<String> replacement = new ArrayList<String>();
          if (!noDashSuggestion.contains(stringToCheck)) {
            replacement.add(origStringToCheck.replace(' ', '-'));
            msg = withHyphenMessage;
          }
          // assume that compounds with more than maxUnHyphenatedWordCount (default: two) parts should always use hyphens:
          if (!hasAllUppercaseParts(origStringToCheck) && countParts(stringToCheck) <= getMaxUnHyphenatedWordCount()
              && !onlyDashSuggestion.contains(stringToCheck)) {
            replacement.add(mergeCompound(origStringToCheck));
            msg = withoutHyphenMessage;
          }
          final String[] parts = stringToCheck.split(" ");
          if (parts.length > 0 && parts[0].length() == 1) {
            replacement.clear();
            replacement.add(origStringToCheck.replace(' ', '-'));
            msg = withHyphenMessage;
          } else if (replacement.isEmpty() || replacement.size() == 2) {     // isEmpty shouldn't happen
            msg = withOrWithoutHyphenMessage;
          }
          final RuleMatch ruleMatch = new RuleMatch(this, firstMatchToken.getStartPos(), 
              atr.getStartPos() + atr.getToken().length(), msg, shortDesc);
          // avoid duplicate matches:
          if (prevRuleMatch != null && prevRuleMatch.getFromPos() == ruleMatch.getFromPos()) {
            prevRuleMatch = ruleMatch;
            break;
          }
          prevRuleMatch = ruleMatch;
          ruleMatch.setSuggestedReplacements(replacement);
          ruleMatches.add(ruleMatch);
          break;
        }
      }
      addToQueue(token, prevTokens);
    }
    return toRuleMatchArray(ruleMatches);
  }

  private String normalize(final String inStr) {
    String str = inStr.trim().toLowerCase();
    if (str.indexOf('-') != -1 && str.indexOf(' ') != -1) {
      if (isHyphenIgnored()) {
        // e.g. "E-Mail Adresse" -> "E Mail Adresse" so the error can be detected:
        str = str.replace('-', ' ');
      } else {
        str = str.replace(" - ", " ");
      }
    }
    return str;
  }

  private boolean hasAllUppercaseParts(final String str) {
    final String[] parts = str.split(" ");
    for (String part : parts) {
      if (isHyphenIgnored() || !"-".equals(part)) { // do not treat '-' as an upper-case word
        if (StringTools.isAllUppercase(part)) {
          return true;
        }
      }
    }
    return false;
  }

  private int countParts(final String str) {    
    return str.split(" ").length;
  }

  private String mergeCompound(final String str) {
    final String[] stringParts = str.split(" ");
    final StringBuilder sb = new StringBuilder();
    for (int k = 0; k < stringParts.length; k++) {
      if (isHyphenIgnored() || !"-".equals(stringParts[k])) {
        if (k == 0)
          sb.append(stringParts[k]);
        else
          sb.append(stringParts[k].toLowerCase());
      }  
    }
    return sb.toString();
  }

  private void addToQueue(final AnalyzedTokenReadings token, final Queue<AnalyzedTokenReadings> prevTokens) {
    final boolean inserted = prevTokens.offer(token);
    if (!inserted) {
      prevTokens.poll();
      prevTokens.offer(token);
    }
  }

  private void loadCompoundFile(final InputStream file, final String encoding) throws IOException {
    final Scanner scanner = new Scanner(file, encoding);
    try {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();
        if (line.length() < 1 || line.charAt(0) == '#') {
          continue;     // ignore comments
        }
        // the set contains the incorrect spellings, i.e. the ones without hyphen
        line = line.replace('-', ' ');
        final String[] parts = line.split(" ");
        if (parts.length > MAX_TERMS) {
          throw new IOException("Too many compound parts: " + line + ", maximum allowed: " + MAX_TERMS);
        }
        if (parts.length == 1) {
          throw new IOException("Not a compound: " + line);
        }
        if (line.endsWith("+")) {
          line = removeLastCharacter(line);
          noDashSuggestion.add(line.toLowerCase());
        } else if (line.endsWith("*")) {
          line = removeLastCharacter(line);
          onlyDashSuggestion.add(line.toLowerCase());
        }
        incorrectCompounds.add(line.toLowerCase());
      }
    } finally {
      scanner.close();
    }
  }

  private String removeLastCharacter(String str) {
    return str.substring(0, str.length() - 1);
  }

  @Override
  public void reset() {
  }

}
