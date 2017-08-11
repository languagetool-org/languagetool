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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Categories;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;

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
  protected Hunspell.Dictionary hunspellDict = null;

  private static final String NON_ALPHABETIC = "[^\\p{L}]";

  protected Pattern nonWordPattern;

  public HunspellRule(ResourceBundle messages, Language language) {
    super(messages, language);
    super.setCategory(Categories.TYPOS.getCategory(messages));
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
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    if (needsInit) {
      init();
    }
    if (hunspellDict == null) {
      // some languages might not have a dictionary, be silent about it
      return toRuleMatchArray(ruleMatches);
    }
    String[] tokens = tokenizeText(getSentenceTextWithoutUrlsAndImmunizedTokens(sentence));

    // starting with the first token to skip the zero-length START_SENT
    int len = sentence.getTokens()[1].getStartPos();
    for (int i = 0; i < tokens.length; i++) {
      String word = tokens[i];
      if (ignoreWord(Arrays.asList(tokens), i) || ignoreWord(word)) {
        len += word.length() + 1;
        continue;
      }
      if (isMisspelled(word)) {
        RuleMatch ruleMatch = new RuleMatch(this,
            len, len + word.length(),
            messages.getString("spelling"),
            messages.getString("desc_spelling_short"));
        List<String> suggestions = getSuggestions(word);
        suggestions.addAll(0, getAdditionalTopSuggestions(suggestions, word));
        suggestions.addAll(getAdditionalSuggestions(suggestions, word));
        if (!suggestions.isEmpty()) {
          filterSuggestions(suggestions);
          filterDupes(suggestions);
          ruleMatch.setSuggestedReplacements(suggestions);
        }
        ruleMatches.add(ruleMatch);
      }
      len += word.length() + 1;
    }

    return toRuleMatchArray(ruleMatches);
  }

  boolean isMisspelled(String word) {
    boolean isAlphabetic = true;
    if (word.length() == 1) { // hunspell dictionaries usually do not contain punctuation
      isAlphabetic = Character.isAlphabetic(word.charAt(0));
    }
    return (isAlphabetic && !word.equals("--") && hunspellDict.misspelled(word)) || isProhibited(removeTrailingDot(word));
  }
  
  void filterDupes(List<String> words) {
    Set<String> seen = new HashSet<>();
    Iterator<String> iterator = words.iterator();
    while (iterator.hasNext()) {
      String word = iterator.next();
      if (seen.contains(word)) {
        iterator.remove();
      }
      seen.add(word);
    }
  }

  private String removeTrailingDot(String word) {
    if (word.endsWith(".")) {
      return word.substring(0, word.length()-1);
    }
    return word;
  }

  public List<String> getSuggestions(String word) throws IOException {
    if (needsInit) {
      init();
    }
    return hunspellDict.suggest(word);
  }

  protected String[] tokenizeText(String sentence) {
    return nonWordPattern.split(sentence);
  }

  private String getSentenceTextWithoutUrlsAndImmunizedTokens(AnalyzedSentence sentence) {
    StringBuilder sb = new StringBuilder();
    AnalyzedTokenReadings[] sentenceTokens = getSentenceWithImmunization(sentence).getTokens();
    for (int i = 1; i < sentenceTokens.length; i++) {
      String token = sentenceTokens[i].getToken();
      if (isUrl(token) || isEMail(token) || sentenceTokens[i].isImmunized() || sentenceTokens[i].isIgnoredBySpeller()) {
        // replace URLs and immunized tokens with whitespace to ignore them for spell checking:
        for (int j = 0; j < token.length(); j++) {
          sb.append(' ');
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
    String langCountry;
    if (language.getCountries().length > 0) {
      langCountry = language.getShortCode() + "_" + language.getCountries()[0];
    } else {
      langCountry = language.getShortCode();
    }
    String shortDicPath = "/"
        + language.getShortCode()
        + "/hunspell/"
        + langCountry
        + ".dic";
    String wordChars = "";
    // set dictionary only if there are dictionary files:
    if (JLanguageTool.getDataBroker().resourceExists(shortDicPath)) {
      String path = getDictionaryPath(langCountry, shortDicPath);
      if ("".equals(path)) {
        hunspellDict = null;
      } else {
        hunspellDict = Hunspell.getInstance().getDictionary(path);
        if (!"".equals(hunspellDict.getWordChars())) {
          wordChars = "(?![" + hunspellDict.getWordChars().replace("-", "\\-") + "])";
        }
        addIgnoreWords();
      }
    }
    nonWordPattern = Pattern.compile(wordChars + NON_ALPHABETIC);
    needsInit = false;
  }

  private void addIgnoreWords() throws IOException {
    hunspellDict.addWord(SpellingCheckRule.LANGUAGETOOL);
    URL ignoreUrl = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(getIgnoreFileName());
    List<String> ignoreLines = Resources.readLines(ignoreUrl, Charsets.UTF_8);
    for (String ignoreLine : ignoreLines) {
      if (!ignoreLine.startsWith("#")) {
        hunspellDict.addWord(ignoreLine);
      }
    }
  }

  private String getDictionaryPath(String dicName,
      String originalPath) throws IOException {

    URL dictURL = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(originalPath);
    String dictionaryPath;
    //in the webstart, java EE or OSGi bundle version, we need to copy the files outside the jar
    //to the local temporary directory
    if ("jar".equals(dictURL.getProtocol()) || "vfs".equals(dictURL.getProtocol()) || "bundle".equals(dictURL.getProtocol()) || "bundleresource".equals(dictURL.getProtocol())) {
      File tempDir = new File(System.getProperty("java.io.tmpdir"));
      File tempDicFile = new File(tempDir, dicName + ".dic");
      JLanguageTool.addTemporaryFile(tempDicFile);
      try (InputStream dicStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(originalPath)) {
        fileCopy(dicStream, tempDicFile);
      }
      File tempAffFile = new File(tempDir, dicName + ".aff");
      JLanguageTool.addTemporaryFile(tempAffFile);
      try (InputStream affStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(originalPath.replaceFirst(".dic$", ".aff"))) {
        fileCopy(affStream, tempAffFile);
      }
      dictionaryPath = tempDir.getAbsolutePath() + "/" + dicName;
    } else {
      int suffixLength = ".dic".length();
      try {
        dictionaryPath = new File(dictURL.toURI()).getAbsolutePath();
        dictionaryPath = dictionaryPath.substring(0, dictionaryPath.length() - suffixLength);
      } catch (URISyntaxException e) {
        return "";
      }
    }
    return dictionaryPath;
  }

  private void fileCopy(InputStream in, File targetFile) throws IOException {
    try (OutputStream out = new FileOutputStream(targetFile)) {
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
      in.close();
    }
  }

}
