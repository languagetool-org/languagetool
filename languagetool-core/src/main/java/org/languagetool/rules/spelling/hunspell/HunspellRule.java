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

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.lang3.StringUtils;
import org.languagetool.*;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Categories;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.suggestions.SuggestionsChanges;
import org.languagetool.rules.spelling.suggestions.SuggestionsOrderer;
import org.languagetool.rules.spelling.suggestions.SuggestionsOrdererFeatureExtractor;
import org.languagetool.rules.spelling.suggestions.XGBoostSuggestionsOrderer;
import org.languagetool.tools.Tools;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

  protected static final String FILE_EXTENSION = ".dic";

  protected final SuggestionsOrderer suggestionsOrderer;
  protected boolean needsInit = true;
  protected Hunspell.Dictionary hunspellDict = null;

  private static final ConcurrentLinkedQueue<String> activeChecks = new ConcurrentLinkedQueue<>();
  private static final String NON_ALPHABETIC = "[^\\p{L}]";

  private final boolean monitorRules;
  private final boolean runningExperiment;

  public static Queue<String> getActiveChecks() {
    return activeChecks;
  }

  private static final String[] WHITESPACE_ARRAY = new String[20];
  static {
    for (int i = 0; i < 20; i++) {
      WHITESPACE_ARRAY[i] = StringUtils.repeat(' ', i);
    }
  }
  protected Pattern nonWordPattern;

  private final UserConfig userConfig;

  public HunspellRule(ResourceBundle messages, Language language, UserConfig userConfig) {
    this(messages, language, userConfig, Collections.emptyList());
  }

  /**
   * @since 4.3
   */
   public HunspellRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages) {
     this(messages, language, userConfig, altLanguages, null);
   }
   public HunspellRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages,
                       LanguageModel languageModel) {
    super(messages, language, userConfig, altLanguages, languageModel);
    super.setCategory(Categories.TYPOS.getCategory(messages));
    this.userConfig = userConfig;
    this.monitorRules = System.getProperty("monitorActiveRules") != null;


     if (SuggestionsChanges.isRunningExperiment("NewSuggestionsOrderer")) {
       suggestionsOrderer = new SuggestionsOrdererFeatureExtractor(language, this.languageModel);
       runningExperiment = true;
     } else {
       suggestionsOrderer = new XGBoostSuggestionsOrderer(language, languageModel);
       runningExperiment = false;
     }
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Override
  public String getDescription() {
    return messages.getString("desc_spelling");
  }

  /**
   * Is the given token part of a hyphenated compound preceded by a quoted token (e.g., „Spiegel“-Magazin) 
   * and should be treated as an ordinary hyphenated compound (e.g., „Spiegel-Magazin“)
   */
  protected boolean isQuotedCompound (AnalyzedSentence analyzedSentence, int idx, String token) {
    return false;
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

    String monitoringText = this.getClass().getName() + ":" + this.getId() + ":" + sentence.getText();
    try {
      if (monitorRules) {
        activeChecks.add(monitoringText);
      }
      String[] tokens = tokenizeText(getSentenceTextWithoutUrlsAndImmunizedTokens(sentence));

      // starting with the first token to skip the zero-length START_SENT
      int len;
      if (sentence.getTokens().length > 1) { // if fixes exception in SuggestionsChangesTest
        len = sentence.getTokens()[1].getStartPos();
      } else {
        len = sentence.getTokens()[0].getStartPos();
      }
      for (int i = 0; i < tokens.length; i++) {
        String word = tokens[i];
        if ((ignoreWord(Arrays.asList(tokens), i) || ignoreWord(word)) && !isProhibited(removeTrailingDot(word))) {
          len += word.length() + 1;
          continue;
        }
        if (isMisspelled(word)) {
          String cleanWord = word;
          if (word.endsWith(".")) {
            cleanWord = word.substring(0, word.length()-1);
          }
          RuleMatch ruleMatch = new RuleMatch(this, sentence,
            len, len + cleanWord.length(),
            messages.getString("spelling"),
            messages.getString("desc_spelling_short"));
          ruleMatch.setType(RuleMatch.Type.UnknownWord);
          if (userConfig == null || userConfig.getMaxSpellingSuggestions() == 0 || ruleMatches.size() <= userConfig.getMaxSpellingSuggestions()) {
            List<String> suggestions = getSuggestions(cleanWord);
            if (word.endsWith(".")) {
              int pos = 1;
              for (String suggestion : getSuggestions(word)) {
                if (!suggestions.contains(suggestion)) {
                  suggestions.add(Math.min(pos, suggestions.size()), suggestion.substring(0, suggestion.length()-1));
                  pos += 2;  // we mix the lists, as we don't know which one is the better one
                }
              }
            }
            List<String> additionalTopSuggestions = getAdditionalTopSuggestions(suggestions, cleanWord);
            if (additionalTopSuggestions.isEmpty() && word.endsWith(".")) {
              additionalTopSuggestions = getAdditionalTopSuggestions(suggestions, word).
                stream().map(k -> k + ".").collect(Collectors.toList());
            }
            Collections.reverse(additionalTopSuggestions);
            for (String additionalTopSuggestion : additionalTopSuggestions) {
              if (!cleanWord.equals(additionalTopSuggestion)) {
                suggestions.add(0, additionalTopSuggestion);
              }
            }
            List<String> additionalSuggestions = getAdditionalSuggestions(suggestions, cleanWord);
            for (String additionalSuggestion : additionalSuggestions) {
              if (!cleanWord.equals(additionalSuggestion)) {
                suggestions.addAll(additionalSuggestions);
              }
            }
            Language acceptingLanguage = acceptedInAlternativeLanguage(cleanWord);
            boolean isSpecialCase = cleanWord.matches(".+-[A-ZÖÄÜ].*");
            if (acceptingLanguage != null && !isSpecialCase) {
              if (isAcceptedWordFromLanguage(acceptingLanguage, cleanWord)) {
                break;
              }
              // e.g. "Der Typ ist in UK echt famous" -> could be German 'famos'
              ruleMatch = new RuleMatch(this, sentence,
                len, len + cleanWord.length(),
                Tools.i18n(messages, "accepted_in_alt_language", cleanWord, messages.getString(acceptingLanguage.getShortCode())));
              ruleMatch.setType(RuleMatch.Type.Hint);
            }
            filterSuggestions(suggestions);
            filterDupes(suggestions);

            // TODO user suggestions
            // use suggestionsOrderer only w/ A/B - Testing or manually enabled experiments
            if (runningExperiment) {
              addSuggestionsToRuleMatch(cleanWord, Collections.emptyList(), suggestions,
                suggestionsOrderer, ruleMatch);
            } else if (userConfig != null && userConfig.getAbTest() != null &&
              userConfig.getAbTest().equals("SuggestionsRanker") &&
              suggestionsOrderer.isMlAvailable() && userConfig.getTextSessionId() != null) {
              boolean testingA = userConfig.getTextSessionId() % 2 == 0;
              if (testingA) {
                addSuggestionsToRuleMatch(cleanWord, Collections.emptyList(), suggestions,
                  null, ruleMatch);
              } else {
                addSuggestionsToRuleMatch(cleanWord, Collections.emptyList(), suggestions,
                  suggestionsOrderer, ruleMatch);
              }
            } else {
              addSuggestionsToRuleMatch(cleanWord, Collections.emptyList(), suggestions,
                null, ruleMatch);
            }
          } else {
            // limited to save CPU
            ruleMatch.setSuggestedReplacement(messages.getString("too_many_errors"));
          }
          ruleMatches.add(ruleMatch);
        }
        len += word.length() + 1;
      }
    } finally {
      if (monitorRules) {
        activeChecks.remove(monitoringText);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  /**
   * @since public since 4.1
   */
  @Experimental
  public boolean isMisspelled(String word) {
    try {
      if (needsInit) {
        init();
      }
      boolean isAlphabetic = true;
      if (word.length() == 1) { // hunspell dictionaries usually do not contain punctuation
        isAlphabetic = Character.isAlphabetic(word.charAt(0));
      }
      return (isAlphabetic && !"--".equals(word) && hunspellDict.misspelled(word) && !ignoreWord(word)) || isProhibited(removeTrailingDot(word));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String removeTrailingDot(String word) {
    return StringUtils.removeEnd(word, ".");
  }

  public List<String> getSuggestions(String word) throws IOException {
    if (needsInit) {
      init();
    }
    return hunspellDict.suggest(word);
  }

  protected List<String> sortSuggestionByQuality(String misspelling, List<String> suggestions) {
    return suggestions;
  }


  protected String[] tokenizeText(String sentence) {
    return nonWordPattern.split(sentence);
  }

  protected String getSentenceTextWithoutUrlsAndImmunizedTokens(AnalyzedSentence sentence) {
    StringBuilder sb = new StringBuilder();
    AnalyzedTokenReadings[] sentenceTokens = getSentenceWithImmunization(sentence).getTokens();
    for (int i = 1; i < sentenceTokens.length; i++) {
      String token = sentenceTokens[i].getToken();
      if (sentenceTokens[i].isImmunized() || sentenceTokens[i].isIgnoredBySpeller() || isUrl(token) || isEMail(token) || isQuotedCompound(sentence, i, token)) {
        if (isQuotedCompound(sentence, i, token)) {
          sb.append(" ").append(token.substring(1));
        }
        // replace URLs and immunized tokens with whitespace to ignore them for spell checking:
        else if (token.length() < 20) {
          sb.append(WHITESPACE_ARRAY[token.length()]);
        } else {
          for (int j = 0; j < token.length(); j++) {
            sb.append(' ');
          }
        }
      } else if (token.length() > 1 && token.codePointCount(0, token.length()) != token.length()) {
        // some symbols such as emojis (😂) have a string length that equals 2 
        for (int charIndex = 0; charIndex < token.length();) {
          int unicodeCodePoint = token.codePointAt(charIndex);
          int increment = Character.charCount(unicodeCodePoint);
          if (increment == 1) {
            sb.append(token.charAt(charIndex));
          } else {
            sb.append("  ");
          }
          charIndex += increment;
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
    String langCountry = language.getShortCode();
    if (language.getCountries().length > 0) {
      langCountry += "_" + language.getCountries()[0];
    }
    String shortDicPath = "/"
        + language.getShortCode()
        + "/hunspell/"
        + langCountry
        + FILE_EXTENSION;
    String wordChars = "";
    // set dictionary only if there are dictionary files:
    if (JLanguageTool.getDataBroker().resourceExists(shortDicPath)) {
      String path = getDictionaryPath(langCountry, shortDicPath);
      if ("".equals(path)) {
        hunspellDict = null;
      } else {
        hunspellDict = Hunspell.getInstance().getDictionary(path);
        if (!hunspellDict.getWordChars().isEmpty()) {
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
    hunspellDict.addWord(SpellingCheckRule.LANGUAGETOOLER);
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
    if (StringUtils.equalsAny(dictURL.getProtocol(), "jar", "vfs", "bundle", "bundleresource")) {
      File tempDir = new File(System.getProperty("java.io.tmpdir"));
      File tempDicFile = new File(tempDir, dicName + FILE_EXTENSION);
      JLanguageTool.addTemporaryFile(tempDicFile);
      try (InputStream dicStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(originalPath)) {
        fileCopy(dicStream, tempDicFile);
      }
      File tempAffFile = new File(tempDir, dicName + ".aff");
      JLanguageTool.addTemporaryFile(tempAffFile);
      if (originalPath.endsWith(FILE_EXTENSION)) {
        originalPath = originalPath.substring(0, originalPath.length() - FILE_EXTENSION.length()) + ".aff";
      }
      try (InputStream affStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(originalPath)) {
        fileCopy(affStream, tempAffFile);
      }
      dictionaryPath = tempDir.getAbsolutePath() + "/" + dicName;
    } else {
      int suffixLength = FILE_EXTENSION.length();
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

  /**
   * Used in combination with <code>acceptedInAlternativeLanguage</code> to surpress spelling
   * errors for words from a foreign language
   * @since 4.6
   * @return true if the {@code word} from {@code language} can be considered as correctly spelled
   */
  protected boolean isAcceptedWordFromLanguage(Language language, String word) {
    return false;
  }
}
