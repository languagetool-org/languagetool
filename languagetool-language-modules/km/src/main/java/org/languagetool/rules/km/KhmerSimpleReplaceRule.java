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
package org.languagetool.rules.km;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Khmer;
import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Category;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tools.StringTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * A rule that matches words which should not be used and suggests correct ones instead. <br/> 
 * Romanian implementations. Loads the list of words from
 * <code>/km/coherency.txt</code>.<br/><br/>
 *
 * Unlike AbstractSimpleReplaceRule, supports multiple words (Ex: "aqua forte" => "acvaforte").<br/><br/>
 *
 * Note: Merge this into {@link AbstractSimpleReplaceRule} eventually and simply extend from AbstractSimpleReplaceRule.<br/>
 *
 * @author Ionuț Păduraru
 * @version $Id: SimpleReplaceRule.java,v 1.9 2010/10/03 13:21:16 archeus Exp $
 *
 */
public class KhmerSimpleReplaceRule extends Rule {

  public static final String KHMER_SIMPLE_REPLACE_RULE = "KM_SIMPLE_REPLACE";

  private static final String FILE_NAME = "/km/coherency.txt";
  private static final String FILE_ENCODING = "utf-8";
  private static final Locale KM_LOCALE = new Locale("km");  // locale used on case-conversion
  
  private final Khmer kmher = new Khmer();

  // list of maps containing error-corrections pairs.
  // the n-th map contains key strings of (n+1) words 
  private final List<Map<String, String>> wrongWords;

  public final String getFileName() {
    return FILE_NAME;
  }

  public KhmerSimpleReplaceRule(final ResourceBundle messages) throws IOException {
    super(messages);
    if (messages != null) {
      super.setCategory(new Category(messages.getString("category_misc")));
    }
    wrongWords = loadWords(JLanguageTool.getDataBroker().getFromRulesDirAsStream(getFileName()));
  }

  @Override
  public final String getId() {
    return KHMER_SIMPLE_REPLACE_RULE;
  }

  @Override
  public String getDescription() {
    return "Words or groups of words that are incorrect or obsolete";
  }

  public String getShort() {
    return "Consider following the spelling of Chuon Nath";
  }

  public String getSuggestion() {
    return " Consider following the spelling of Chuon Nath ";
  }

  /**
   * @return the word used to separate multiple suggestions; used only before last suggestion, the rest are comma-separated.  
   */
  public String getSuggestionsSeparator() {
    return " or ";
  }

  /**
   * use case-insensitive matching.
   */
  public boolean isCaseSensitive() {
    return false;
  }

  /**
   * locale used on case-conversion
   */
  public Locale getLocale() {
    return KM_LOCALE;
  }

  public String getEncoding() {
    return FILE_ENCODING;
  }

  /**
   * @return the word tokenizer used for tokenization on loading words.
   */
  protected Tokenizer getWordTokenizer() {
    return kmher.getWordTokenizer();
  }

  /**
   * @return the list of wrong words for which this rule can suggest correction. The list cannot be modified.
   */
  public List<Map<String, String>> getWrongWords() {
    return wrongWords;
  }

  /**
   * Load the list of words. <br/>
   * Same as {@link AbstractSimpleReplaceRule#loadWords} but allows multiple words.   
   * @param stream the stream to load.
   * @return the list of maps containing the error-corrections pairs. <br/>The n-th map contains key strings of (n+1) words.
   * @see #getWordTokenizer
   */
  private List<Map<String, String>> loadWords(final InputStream stream)
          throws IOException {
    final List<Map<String, String>> list = new ArrayList<>();
    InputStreamReader isr = null;
    BufferedReader br = null;
    try {
      isr = new InputStreamReader(stream, getEncoding());
      br = new BufferedReader(isr);
      String line;

      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.length() < 1 || line.charAt(0) == '#') { // ignore comments
          continue;
        }

        final String[] parts = line.split("=");
        if (parts.length != 2) {
          throw new IOException("Format error in file "
                  + JLanguageTool.getDataBroker().getFromRulesDirAsUrl(getFileName())
                  + ", line: " + line);

        }

        final String[] wrongForms = parts[0].split("\\|"); // multiple incorrect forms
        for (String wrongForm : wrongForms) {
          int wordCount = 0;
          final List<String> tokens = getWordTokenizer().tokenize(wrongForm);
          for (String token : tokens) {
            if (!StringTools.isWhitespace(token)) {
              wordCount++;
            }
          }
          // grow if necessary
          for (int i = list.size(); i < wordCount; i++) {
            list.add(new HashMap<String, String>());
          }
          list.get(wordCount - 1).put(wrongForm, parts[1]);
        }
      }

    } finally {
      if (br != null) {
        br.close();
      }
      if (isr != null) {
        isr.close();
      }
    }
    // seal the result (prevent modification from outside this class)
    List<Map<String,String>> result = new ArrayList<>();
    for (Map<String, String> map : list) {
      result.add(Collections.unmodifiableMap(map));
    }
    return Collections.unmodifiableList(result);
  }

  private void addToQueue(AnalyzedTokenReadings token,
                          Queue<AnalyzedTokenReadings> prevTokens) {
    final boolean inserted = prevTokens.offer(token);
    if (!inserted) {
      prevTokens.poll();
      prevTokens.offer(token);
    }
  }

  @Override
  public RuleMatch[] match(final AnalyzedSentence sentence) {
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    final AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    final Queue<AnalyzedTokenReadings> prevTokens = new ArrayBlockingQueue<>(wrongWords.size());

    for (int i = 1; i < tokens.length; i++) {
      addToQueue(tokens[i], prevTokens);
      final StringBuilder sb = new StringBuilder();
      final List<String> variants = new ArrayList<>();
      final List<AnalyzedTokenReadings> prevTokensList =
              Arrays.asList(prevTokens.toArray(new AnalyzedTokenReadings[prevTokens.size()]));
      for (int j = prevTokensList.size() - 1; j >= 0; j--) {
        if (j != prevTokensList.size() - 1 && prevTokensList.get(j + 1).isWhitespaceBefore()) {
          sb.insert(0, " ");
        }
        sb.insert(0, prevTokensList.get(j).getToken());
        variants.add(0, sb.toString());
      }
      final int len = variants.size(); // prevTokensList and variants have now the same length
      for (int j = 0; j < len; j++) { // longest words first
        final String crt = variants.get(j);
        final int crtWordCount = len - j;
        final String crtMatch = isCaseSensitive() ? wrongWords.get(crtWordCount - 1).get(crt) : wrongWords.get(crtWordCount- 1).get(crt.toLowerCase(getLocale()));
        if (crtMatch != null) {
          final List<String> replacements = Arrays.asList(crtMatch.split("\\|"));
          String msg = crt + getSuggestion();
          for (int k = 0; k < replacements.size(); k++) {
            if (k > 0) {
              msg = msg + (k == replacements.size() - 1 ? getSuggestionsSeparator(): ", ");
            }
            msg += "<suggestion>" + replacements.get(k) + "</suggestion>";
          }
          final int startPos = prevTokensList.get(len - crtWordCount).getStartPos();
          final int endPos = prevTokensList.get(len - 1).getStartPos() + prevTokensList.get(len - 1).getToken().length();
          final RuleMatch potentialRuleMatch = new RuleMatch(this, startPos, endPos, msg, getShort());

          if (!isCaseSensitive() && StringTools.startsWithUppercase(crt)) {
            for (int k = 0; k < replacements.size(); k++) {
              replacements.set(k, StringTools.uppercaseFirstChar(replacements.get(k)));
            }
          }
          potentialRuleMatch.setSuggestedReplacements(replacements);
          ruleMatches.add(potentialRuleMatch);
          break;
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public void reset() {
  }

}
