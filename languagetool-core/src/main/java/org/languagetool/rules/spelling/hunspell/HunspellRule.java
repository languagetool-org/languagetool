/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

package org.languagetool.rules.spelling.hunspell;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Category;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tools.StringTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * A hunspell-based spellchecking-rule.
 * 
 * The default dictionary is set to the first country variant on the list - so the order
   in the Language class declaration is important!
 * 
 * @author Marcin Miłkowski
 */
public class HunspellRule extends SpellingCheckRule {

  public static final String RULE_ID = "HUNSPELL_RULE";

  protected boolean needsInit = true;
  protected Hunspell.Dictionary dictionary = null;
  
  private static final String NON_ALPHABETIC = "[^\\p{L}]";

  private Pattern nonWordPattern;

  public HunspellRule(final ResourceBundle messages, final Language language) {
    super(messages, language);
    super.setCategory(new Category(messages.getString("category_typo")));
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Override
  public String getDescription() {
    return messages.getString("desc_spelling");
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence text) throws IOException {
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    if (needsInit) {
      init();
    }
    if (dictionary == null) {
      // some languages might not have a dictionary, be silent about it
      return toRuleMatchArray(ruleMatches);
    }
    final String[] tokens = tokenizeText(getSentenceTextWithoutUrlsAndImmunizedTokens(text));

    // starting with the first token to skip the zero-length START_SENT
    int len = text.getTokens()[1].getStartPos();
    for (final String word : tokens) {
      if (ignoreWord(word)) {
        len += word.length() + 1;
        continue;
      }
      boolean isAlphabetic = true;
      if (word.length() == 1) { // hunspell dictionaries usually do not contain punctuation
        isAlphabetic = StringTools.isAlphabetic(word.charAt(0));
      }
      if (isAlphabetic && !word.equals("--") && dictionary.misspelled(word)) {
        final RuleMatch ruleMatch = new RuleMatch(this,
                len, len + word.length(),
                messages.getString("spelling"),
                messages.getString("desc_spelling_short"));
        final List<String> suggestions = getSuggestions(word);
        suggestions.addAll(getAdditionalSuggestions(suggestions, word));
        if (!suggestions.isEmpty()) {
          ruleMatch.setSuggestedReplacements(suggestions);
        }
        ruleMatches.add(ruleMatch);
      }
      len += word.length() + 1;
    }

    return toRuleMatchArray(ruleMatches);
  }

  public List<String> getSuggestions(String word) throws IOException {
    if (needsInit) {
      init();
    }
    return dictionary.suggest(word);
  }

  protected String[] tokenizeText(final String sentence) {
    return nonWordPattern.split(sentence);
  }

  private String getSentenceTextWithoutUrlsAndImmunizedTokens(final AnalyzedSentence sentence) {
    final StringBuilder sb = new StringBuilder();
    final AnalyzedTokenReadings[] sentenceTokens = sentence.getTokens();
    for (int i = 1; i < sentenceTokens.length; i++) {
      final String token = sentenceTokens[i].getToken();
      if (isUrl(token) || sentenceTokens[i].isImmunized()) {
        // replace URLs and immunized tokens with whitespace to ignore them for spell checking:
        for (int j = 0; j < token.length(); j++) {
          sb.append(" ");
        }
      } else {
        sb.append(token);
      }
    }
    return sb.toString();
  }

  @Override
  protected void init() throws IOException {
    super.init();
    final String langCountry;
    if (language.getCountries().length > 0) {
      langCountry = language.getShortName() + "_" + language.getCountries()[0];
    } else {
      langCountry = language.getShortName();
    }
    final String shortDicPath = "/"
            + language.getShortName()
            + "/hunspell/"
            + langCountry
            + ".dic";
    String wordChars = "";
    // set dictionary only if there are dictionary files:
    if (JLanguageTool.getDataBroker().resourceExists(shortDicPath)) {
      final String path = getDictionaryPath(langCountry, shortDicPath);
      if ("".equals(path)) {
        dictionary = null;
      } else {
        dictionary = Hunspell.getInstance().
                getDictionary(path);

        if (!"".equals(dictionary.getWordChars())) {
          wordChars = "(?![" + dictionary.getWordChars().replace("-", "\\-") + "])";
        }

        dictionary.addWord(SpellingCheckRule.LANGUAGETOOL); // to make demo text check 4 times faster...
        dictionary.addWord(SpellingCheckRule.LANGUAGETOOL_FX);
      }
    }
    nonWordPattern = Pattern.compile(wordChars + NON_ALPHABETIC);
    needsInit = false;
  }

  private String getDictionaryPath(final String dicName,
                                   final String originalPath) throws IOException {

    final URL dictURL = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(originalPath);
    String dictionaryPath;
    //in the webstart version, we need to copy the files outside the jar
    //to the local temporary directory
    if ("jar".equals(dictURL.getProtocol())) {
      final File tempDir = new File(System.getProperty("java.io.tmpdir"));
      File temporaryFile = new File(tempDir, dicName + ".dic");
      JLanguageTool.addTemporaryFile(temporaryFile);
      fileCopy(JLanguageTool.getDataBroker().
              getFromResourceDirAsStream(originalPath), temporaryFile);
      temporaryFile = new File(tempDir, dicName + ".aff");
      JLanguageTool.addTemporaryFile(temporaryFile);
      fileCopy(JLanguageTool.getDataBroker().
              getFromResourceDirAsStream(originalPath.
                      replaceFirst(".dic$", ".aff")), temporaryFile);

      dictionaryPath = tempDir.getAbsolutePath() + "/" + dicName;
    } else {
      final int suffixLength = ".dic".length();
      try {
        dictionaryPath = new File(dictURL.toURI()).getAbsolutePath();
        dictionaryPath = dictionaryPath.substring(0, dictionaryPath.length() - suffixLength);
      } catch (URISyntaxException e) {
        return "";
      }
    }
    return dictionaryPath;
  }

  private void fileCopy(final InputStream in, final File targetFile) throws IOException {
    try (OutputStream out = new FileOutputStream(targetFile)) {
      final byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
      in.close();
    }
  }

}
