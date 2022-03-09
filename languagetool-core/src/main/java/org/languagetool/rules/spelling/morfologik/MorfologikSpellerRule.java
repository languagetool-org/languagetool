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
package org.languagetool.rules.spelling.morfologik;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.vdurmont.emoji.EmojiManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.noop.NoopLanguage;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.SuggestedReplacement;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.suggestions.SuggestionsChanges;
import org.languagetool.rules.translation.TranslationEntry;
import org.languagetool.rules.translation.Translator;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.languagetool.JLanguageTool.getDataBroker;

public abstract class MorfologikSpellerRule extends SpellingCheckRule {

  private static final Logger logger = LoggerFactory.getLogger(MorfologikSpellerRule.class);

  protected MorfologikMultiSpeller speller1;
  protected MorfologikMultiSpeller speller2;
  protected MorfologikMultiSpeller speller3;
  protected Locale conversionLocale;
  protected final Language motherTongue;
  protected final GlobalConfig globalConfig;

  private boolean ignoreTaggedWords = false;
  private boolean checkCompound = false;
  private Pattern compoundRegex = Pattern.compile("-");
  private final UserConfig userConfig;
 
  //do not use very frequent words in split word suggestions ex. to *thow ≠ tot how 
  static final int MAX_FREQUENCY_FOR_SPLITTING = 21; //0..21

  /**
   * Get the filename, e.g., <tt>/resource/pl/spelling.dict</tt>.
   */
  public abstract String getFileName();

  @Override
  public abstract String getId();

  public MorfologikSpellerRule(ResourceBundle messages, Language language) throws IOException {
    this(messages, language, null);
  }
  
  public MorfologikSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig) throws IOException {
    this(messages, language, userConfig, Collections.emptyList());
  }

  public MorfologikSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    this(messages, language, null, userConfig, altLanguages, null, null);
  }

  public MorfologikSpellerRule(ResourceBundle messages, Language language, GlobalConfig globalConfig, UserConfig userConfig,
                               List<Language> altLanguages, LanguageModel languageModel, Language motherTongue) throws IOException {
    super(messages, language, userConfig, altLanguages, languageModel);
    this.globalConfig = globalConfig;
    this.userConfig = userConfig;
    this.motherTongue = motherTongue;
    super.setCategory(Categories.TYPOS.getCategory(messages));
    conversionLocale = conversionLocale != null ? conversionLocale : Locale.getDefault();
    init();
    setLocQualityIssueType(ITSIssueType.Misspelling);
  }

  @Override
  public String getDescription() {
    return messages.getString("desc_spelling");
  }

  public void setLocale(Locale locale) {
    conversionLocale = locale;
  }

  /**
   * Skip words that are known in the POS tagging dictionary, assuming they
   * cannot be incorrect.
   */
  public void setIgnoreTaggedWords() {
    ignoreTaggedWords = true;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokensWithoutWhitespace();
    if (initSpellers()) return toRuleMatchArray(ruleMatches);
    int idx = -1;
    long sentLength = Arrays.stream(sentence.getTokensWithoutWhitespace()).filter(k -> !k.isNonWord()).count() - 1;  // -1 for the SENT_START token
    for (AnalyzedTokenReadings token : tokens) {
      idx++;
      if (canBeIgnored(tokens, idx, token)) {
        continue;
      }
      int startPos = token.getStartPos();
      // if we use token.getToken() we'll get ignored characters inside and speller will choke
      String word = token.getAnalyzedToken(0).getToken();
      
      /*String normalizedWord = StringTools.normalizeNFKC(word);
      if (word.length() > 1 && !word.equals(normalizedWord) && !normalizedWord.contains(" ")
          && isMisspelled(speller1, word)) {
        if (!isMisspelled(speller1, normalizedWord)) {
          // The normalized word is a good suggestion
          RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, startPos + word.length(),
              messages.getString("spelling"), messages.getString("desc_spelling_short"));
          ruleMatch.addSuggestedReplacement(normalizedWord);
          ruleMatches.add(ruleMatch);
        } else {
          // Try to find suggestions from the normalized word.
          List<String> suggestions = speller1.getSuggestions(normalizedWord);
          RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, startPos + word.length(),
              messages.getString("spelling"), messages.getString("desc_spelling_short"));
          ruleMatch.addSuggestedReplacements(suggestions);
          ruleMatches.add(ruleMatch);
        }
        // Keep it simple. Don't do translations, split words, etc.
        continue;
      }*/   
      
      int newRuleIdx = ruleMatches.size();
      Pattern pattern = tokenizingPattern();
      if (pattern == null) {
        ruleMatches.addAll(getRuleMatches(word, startPos, sentence, ruleMatches, idx, tokens));
      } else {
        int index = 0;
        Matcher m = pattern.matcher(word);
        while (m.find()) {
          String match = word.subSequence(index, m.start()).toString();
          ruleMatches.addAll(getRuleMatches(match, startPos + index, sentence, ruleMatches, idx, tokens));
          index = m.end();
        }
        if (index == 0) { // tokenizing char not found
          ruleMatches.addAll(getRuleMatches(word, startPos, sentence, ruleMatches, idx, tokens));
        } else {
          ruleMatches.addAll(getRuleMatches(word.subSequence(index, word.length()).toString(), startPos + index, sentence, ruleMatches, idx, tokens));
        }
      }

      if (ruleMatches.size() > newRuleIdx) {
        // matches added for current token - need to adjust for hidden characters
        int hiddenCharOffset = token.getToken().length() - word.length();
        if (hiddenCharOffset > 0) {
          for (int i = newRuleIdx; i < ruleMatches.size(); i++) {
            RuleMatch ruleMatch = ruleMatches.get(i);
            if (token.getEndPos() < ruleMatch.getToPos()) { // done by multi-token speller, no need to adjust
              continue;
            }
            ruleMatch.setOffsetPosition(ruleMatch.getFromPos(), ruleMatch.getToPos()+hiddenCharOffset);
          }
        }
      }

      if (sentLength > 3) {
        float errRatio = (float)ruleMatches.size() / sentLength;
        if (errRatio >= 0.5) {
          ruleMatches.get(0).setErrorLimitLang(NoopLanguage.SHORT_CODE);
        }
      }

    }

    return toRuleMatchArray(ruleMatches);
  }

  @Nullable
  protected Translator getTranslator(GlobalConfig globalConfig) {
    return null;
  }

  private boolean initSpellers() throws IOException {
    if (speller1 == null) {
      String binaryDict = null;
      if (getDataBroker().resourceExists(getFileName()) || Paths.get(getFileName()).toFile().exists()) {
        binaryDict = getFileName();
      }
      if (binaryDict != null) {
        initSpeller(binaryDict);
      } else {
        // should not happen, as we only configure this rule (or rather its subclasses)
        // when we have the resources:
        return true;
      }
    }
    return false;
  }

  private void initSpeller(String binaryDict) throws IOException {
    List<String> plainTextDicts = new ArrayList<>();
    String languageVariantPlainTextDict = null;
    if (getSpellingFileName() != null && getDataBroker().resourceExists(getSpellingFileName())) {
      plainTextDicts.add(getSpellingFileName());
    }
    for (String fileName : getAdditionalSpellingFileNames()) {
      if (getDataBroker().resourceExists(fileName)) {
        plainTextDicts.add(fileName);
      }
    }
    if (getLanguageVariantSpellingFileName() != null && getDataBroker().resourceExists(getLanguageVariantSpellingFileName())) {
      languageVariantPlainTextDict = getLanguageVariantSpellingFileName();
    }
    speller1 = new MorfologikMultiSpeller(binaryDict, plainTextDicts, languageVariantPlainTextDict, userConfig, 1);
    speller2 = new MorfologikMultiSpeller(binaryDict, plainTextDicts, languageVariantPlainTextDict, userConfig, 2);
    speller3 = new MorfologikMultiSpeller(binaryDict, plainTextDicts, languageVariantPlainTextDict, userConfig, 3);
    setConvertsCase(speller1.convertsCase());
  }

  private boolean canBeIgnored(AnalyzedTokenReadings[] tokens, int idx, AnalyzedTokenReadings token) throws IOException {
    return token.isSentenceStart() ||
           token.isImmunized() ||
           token.isIgnoredBySpeller() ||
           isUrl(token.getToken()) ||
           isEMail(token.getToken()) ||
           (ignoreTaggedWords && token.isTagged() && !isProhibited(token.getToken())) ||
           ignoreToken(tokens, idx);
  }


  /**
   * @since 4.8
   */
  @Experimental
  @Override
  public boolean isMisspelled(String word) throws IOException {
    initSpellers();
    return isMisspelled(speller1, word);
  }
  
  /**
   * @return true if the word is misspelled
   * @since 2.4
   */
  protected boolean isMisspelled(MorfologikMultiSpeller speller, String word) {
    if (speller == null && Tools.isExternSpeller()) {  // use of external speller for LO/OO extension
      if (Tools.getLinguisticServices().isCorrectSpell(word, language)) {
        return false;
      }
    } else {
      if (!speller.isMisspelled(word)) {
        return false;
      }
    }
    if (checkCompound && compoundRegex.matcher(word).find()) {
      String[] words = compoundRegex.split(word);
      for (String singleWord: words) {
        if (speller == null && Tools.isExternSpeller()) {  // use of external speller for LO/OO extension
          if (!Tools.getLinguisticServices().isCorrectSpell(singleWord, language)) {
            return true;
          }
        } else {
          if (speller.isMisspelled(singleWord)) {
            return true;
          }
        }
      }
      return false;
    }
    return true;
  }
  
  private static int getFrequency(MorfologikMultiSpeller speller, String word) {
    return speller.getFrequency(word);
  }

  protected List<RuleMatch> getRuleMatches(String word, int startPos, AnalyzedSentence sentence, List<RuleMatch> ruleMatchesSoFar, int idx, AnalyzedTokenReadings[] tokens) throws IOException {
    // We create only one rule match. 
    // Several rule matches on the same word or words can not be shown to the user.
    List<RuleMatch> ruleMatches = new ArrayList<>();
    RuleMatch ruleMatch = null;
    
    if (!isMisspelled(speller1, word) && !isProhibited(word)) {
      return ruleMatches;
    }
    
    //the current word is already dealt with in the previous match, so do nothing
    if (ruleMatchesSoFar.size() > 0 && ruleMatchesSoFar.get(ruleMatchesSoFar.size() - 1).getToPos() > startPos) {
      return ruleMatches;
    }
    
    String beforeSuggestionStr = ""; //to be added before the suggestion if there is a suggestion for a split word
    String afterSuggestionStr = "";  //to be added after
    
    // Check for split word with previous word
    if (idx > 0 && tokens[idx].isWhitespaceBefore()) {
      String prevWord = tokens[idx - 1].getToken();
      if (prevWord.length() > 0 && !StringUtils.containsAny(prevWord, "0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
          && getFrequency(speller1, prevWord) < MAX_FREQUENCY_FOR_SPLITTING) {
        int prevStartPos = tokens[idx - 1].getStartPos();
        // "thanky ou" -> "thank you"
        String sugg1a = prevWord.substring(0, prevWord.length() - 1);
        String sugg1b = prevWord.substring(prevWord.length() - 1) + word;
        if (sugg1a.length() > 1 && sugg1b.length() > 2 && !isMisspelled(speller1, sugg1a)
            && !isMisspelled(speller1, sugg1b)
            && getFrequency(speller1, sugg1a) + getFrequency(speller1, sugg1b) > getFrequency(speller1, prevWord)) {
          ruleMatch = createWrongSplitMatch(sentence, ruleMatchesSoFar, startPos, word, sugg1a, sugg1b, prevStartPos);
          beforeSuggestionStr = prevWord + " ";
        }
        // "than kyou" -> "thank you" ; but not "She awaked" -> "Shea waked"
        String sugg2a = prevWord + word.charAt(0);
        String sugg2b = word.substring(1);
        if (sugg2b.length() > 2 && !isMisspelled(speller1, sugg2a) && !isMisspelled(speller1, sugg2b)) {
          if (ruleMatch == null) {
            if (getFrequency(speller1, sugg2a) + getFrequency(speller1, sugg2b) > getFrequency(speller1, prevWord)) {
              ruleMatch = createWrongSplitMatch(sentence, ruleMatchesSoFar, startPos, word, sugg2a, sugg2b,
                  prevStartPos);
              beforeSuggestionStr = prevWord + " ";
            }
          } else {
            ruleMatch.addSuggestedReplacement((sugg2a + " " + sugg2b).trim());
          }
        }
        // "g oing-> "going"
        String sugg = prevWord + word;
        if (word.equals(word.toLowerCase()) && !isMisspelled(speller1, sugg)) {
          if (ruleMatch == null) {
            if (getFrequency(speller1, sugg) >= getFrequency(speller1, prevWord)) {
              ruleMatch = new RuleMatch(this, sentence, prevStartPos, startPos + word.length(),
                  messages.getString("spelling"), messages.getString("desc_spelling_short"));
              beforeSuggestionStr = prevWord + " ";
              ruleMatch.setSuggestedReplacement(sugg);
            }
          } else {
            ruleMatch.addSuggestedReplacement(sugg);
          }
        }
        if (ruleMatch != null && isMisspelled(speller1, prevWord)) {
          ruleMatches.add(ruleMatch);
          return ruleMatches;
        }
      }
    }
        
    // Check for split word with next word
    if (ruleMatch == null && idx < tokens.length - 1 && tokens[idx + 1].isWhitespaceBefore()) {
      String nextWord = tokens[idx + 1].getToken();
      if (nextWord.length() > 0 && !StringUtils.containsAny(nextWord, '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
          && getFrequency(speller1, nextWord) < MAX_FREQUENCY_FOR_SPLITTING) {
        int nextStartPos = tokens[idx + 1].getStartPos();
        String sugg1a = word.substring(0, word.length() - 1);
        String sugg1b = word.substring(word.length() - 1) + nextWord;
        if (sugg1a.length() > 1 && sugg1b.length() > 2 && !isMisspelled(speller1, sugg1a) && !isMisspelled(speller1, sugg1b) &&
            getFrequency(speller1, sugg1a) + getFrequency(speller1, sugg1b) > getFrequency(speller1, nextWord)) {
          ruleMatch = createWrongSplitMatch(sentence, ruleMatchesSoFar, nextStartPos, nextWord, sugg1a, sugg1b, startPos);
          afterSuggestionStr = " " + nextWord;
        }
        String sugg2a = word + nextWord.charAt(0);
        String sugg2b = nextWord.substring(1);
        if (sugg2b.length() > 2 && !isMisspelled(speller1, sugg2a) && !isMisspelled(speller1, sugg2b)) {
          if (ruleMatch == null) {
            if (getFrequency(speller1, sugg2a) + getFrequency(speller1, sugg2b) > getFrequency(speller1, nextWord)) {
              ruleMatch = createWrongSplitMatch(sentence, ruleMatchesSoFar, nextStartPos, nextWord, sugg2a, sugg2b, startPos);
              afterSuggestionStr = " " + nextWord;
            }
          } else {
            ruleMatch.addSuggestedReplacement((sugg2a + " " + sugg2b).trim());
          }
        }
        String sugg = word + nextWord;
        if (nextWord.equals(nextWord.toLowerCase()) && !isMisspelled(speller1, sugg)) {
          if (ruleMatch == null) {
            if (getFrequency(speller1, sugg) >= getFrequency(speller1, nextWord)) {
              ruleMatch = new RuleMatch(this, sentence, startPos, nextStartPos + nextWord.length(),
                  messages.getString("spelling"), messages.getString("desc_spelling_short"));
              afterSuggestionStr = " " + nextWord;
              ruleMatch.setSuggestedReplacement(sugg);
            }
          } else {
            ruleMatch.addSuggestedReplacement(sugg);
          }
        }
        if (ruleMatch != null && isMisspelled(speller1, nextWord)) {
          ruleMatches.add(ruleMatch);
          return ruleMatches;
        }
      }
    }
 
    int translationSuggestionCount = 0;
    boolean preventFurtherSuggestions = false;
    Translator translator = getTranslator(globalConfig);
    if (translator != null && ruleMatch == null && motherTongue != null &&
        language.getShortCode().equals("en") && motherTongue.getShortCode().equals("de")) {
      List<PhraseToTranslate> phrasesToTranslate = new ArrayList<>();
      if (idx + 1 < tokens.length) {
        String nextWord = tokens[idx + 1].getToken();
        if (isMisspelled(nextWord)) {
          phrasesToTranslate.add(new PhraseToTranslate(word + " " + nextWord, tokens[idx + 1].getEndPos()));
        }
      }
      phrasesToTranslate.add(new PhraseToTranslate(word, startPos + word.length()));
      for (PhraseToTranslate phraseToTranslate : phrasesToTranslate) {
        List<TranslationEntry> translations = translator.translate(phraseToTranslate.phrase, motherTongue.getShortCode(), language.getShortCode());
        if (!translations.isEmpty()) {
          logger.info("Translated: {}", word);   // privacy: logging a single word without IP address etc. is okay
          ruleMatch = new RuleMatch(this, sentence, startPos, phraseToTranslate.endPos, translator.getMessage());
          ruleMatch.setType(RuleMatch.Type.Hint);
          ruleMatch.setSuggestedReplacements(new ArrayList<>());
          List<SuggestedReplacement> l = new ArrayList<>();
          String prevWord = idx > 0 ? tokens[idx-1].getToken() : null;
          for (TranslationEntry translation : translations) {
            for (String s : translation.getL2()) {
              String suffix = translator.getTranslationSuffix(s);
              SuggestedReplacement repl = new SuggestedReplacement(translator.cleanTranslationForReplace(s, prevWord), String.join(", ", translation.getL1()), suffix.isEmpty() ? null : suffix);
              repl.setType(SuggestedReplacement.SuggestionType.Translation);
              if (!repl.getReplacement().equals(word)) {
                l.add(repl);
              }
            }
          }
          List<SuggestedReplacement> mergedRepl = mergeSuggestionsWithSameTranslation(l);
          if (!mergedRepl.isEmpty()) {
            ruleMatch.setSuggestedReplacementObjects(mergedRepl);
            translationSuggestionCount = mergedRepl.size();
            if (phraseToTranslate.phrase.contains(" ")) {
              preventFurtherSuggestions = true;  // mark gets extended, so suggestions for the original marker won't make sense
            }
            break;  // let's assume the first phrase is the best because it's longer
          }
        }
      }
    }

    if (ruleMatch == null) {
      ruleMatch = new RuleMatch(this, sentence, startPos, startPos + word.length(), messages.getString("spelling"),
              messages.getString("desc_spelling_short"));
    }
    boolean fullResults = SuggestionsChanges.getInstance() != null &&
      SuggestionsChanges.getInstance().getCurrentExperiment() != null &&
      (boolean) SuggestionsChanges.getInstance().getCurrentExperiment()
        .parameters.getOrDefault("fullSuggestionCandidates", Boolean.FALSE);

    if (userConfig == null || userConfig.getMaxSpellingSuggestions() == 0 
        || ruleMatchesSoFar.size() <= userConfig.getMaxSpellingSuggestions()) {
      if (translationSuggestionCount > 0) {
        List<SuggestedReplacement> prev = ruleMatch.getSuggestedReplacementObjects();
        ruleMatch = new RuleMatch(ruleMatch.getRule(), ruleMatch.getSentence(), ruleMatch.getFromPos(), ruleMatch.getToPos(),
          messages.getString("spelling") + " Translations to English are also offered.");
        ruleMatch.setSuggestedReplacementObjects(prev);
      }

      if (!preventFurtherSuggestions) {
        ruleMatch.setLazySuggestedReplacements(appendLazySuggestions(word, beforeSuggestionStr, afterSuggestionStr,
          fullResults, ruleMatch.getSuggestedReplacementObjects()));
      }
    } else {
      // limited to save CPU
      ruleMatch.setSuggestedReplacement(messages.getString("too_many_errors"));
    }

    ruleMatches.add(ruleMatch);
    return ruleMatches;
  }

  private Supplier<List<SuggestedReplacement>> appendLazySuggestions(String word, String beforeSuggestionStr, String afterSuggestionStr, boolean fullResults, List<SuggestedReplacement> prev) {
    return () -> {
      List<SuggestedReplacement> joined;
      try {
        List<SuggestedReplacement> fromSpeller = calcSpellerSuggestions(word, fullResults);
        joined = joinBeforeAfterSuggestions(fromSpeller, beforeSuggestionStr, afterSuggestionStr);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return Lists.newArrayList(Iterables.concat(prev, joined));
    };
  }

  private List<SuggestedReplacement> calcSpellerSuggestions(String word, boolean fullResults) throws IOException {
    List<SuggestedReplacement> onlySuggestions = getOnlySuggestions(word);
    if (!onlySuggestions.isEmpty()) {
      return onlySuggestions;
    }
    List<SuggestedReplacement> defaultSuggestions = SuggestedReplacement.convert(speller1.getSuggestionsFromDefaultDicts(word));
    List<SuggestedReplacement> userSuggestions = SuggestedReplacement.convert(speller1.getSuggestionsFromUserDicts(word));
    //System.out.println("speller1: " + suggestions);
    boolean onlyCaseDiffers = false;
    if (defaultSuggestions.size() > 0 && word.equalsIgnoreCase(defaultSuggestions.get(0).getReplacement())) {
      // We have no good concept yet for showing both translations and standard suggestions, so
      // use a hack to fix e.g. "muslims" not suggesting "Muslims" (https://github.com/languagetool-org/languagetool/issues/3333)
      onlyCaseDiffers = true;
    }
    if (word.length() >= 3 && (onlyCaseDiffers || fullResults || defaultSuggestions.isEmpty())) {
      // speller1 uses a maximum edit distance of 1, it won't find suggestion for "garentee", "greatful" etc.
      //System.out.println("speller2: " + speller2.getSuggestions(word));
      defaultSuggestions.addAll(SuggestedReplacement.convert(speller2.getSuggestionsFromDefaultDicts(word)));
      userSuggestions.addAll(SuggestedReplacement.convert(speller2.getSuggestionsFromUserDicts(word)));
      if (word.length() >= 5 && (fullResults || defaultSuggestions.isEmpty())) {
        //System.out.println("speller3: " + speller3.getSuggestions(word));
        defaultSuggestions.addAll(SuggestedReplacement.convert(speller3.getSuggestionsFromDefaultDicts(word)));
        userSuggestions.addAll(SuggestedReplacement.convert(speller3.getSuggestionsFromUserDicts(word)));
      }
    }
    //System.out.println("getAdditionalTopSuggestions(suggestions, word): " + getAdditionalTopSuggestions(suggestions, word));
    List<SuggestedReplacement> topSuggestions = getAdditionalTopSuggestions(defaultSuggestions, word);
    topSuggestions.forEach(s -> s.setType(SuggestedReplacement.SuggestionType.Curated));
    defaultSuggestions.addAll(0, topSuggestions);
    //System.out.println("getAdditionalSuggestions(suggestions, word): " + getAdditionalSuggestions(suggestions, word));
    defaultSuggestions.addAll(getAdditionalSuggestions(defaultSuggestions, word));

    if (defaultSuggestions.isEmpty() && userSuggestions.isEmpty()) {
      return Collections.emptyList();
    }

    defaultSuggestions = filterSuggestions(defaultSuggestions);
    userSuggestions = filterDupes(userSuggestions);
    defaultSuggestions = orderSuggestions(defaultSuggestions, word);

    return Lists.newArrayList(Iterables.concat(userSuggestions, defaultSuggestions));
  }

  @NotNull
  private static List<SuggestedReplacement> mergeSuggestionsWithSameTranslation(List<SuggestedReplacement> l) {
    List<SuggestedReplacement> mergedRepl = new ArrayList<>();
    Set<String> handledReplacements = new HashSet<>();
    for (SuggestedReplacement repl : l) {
      List<SuggestedReplacement> sameRepl = l.stream()
        .filter(k -> k.getReplacement().equals(repl.getReplacement()))
        .filter(k -> k.getSuffix() == null || (k.getSuffix() != null && k.getSuffix().equals(repl.getSuffix())))
        .collect(Collectors.toList());
      if (sameRepl.size() > 1) {
        if (!handledReplacements.contains(repl.getReplacement())) {
          List<String> joinedRepls = new ArrayList<>();
          for (SuggestedReplacement r : sameRepl) {
            joinedRepls.add("* " + r.getShortDescription());
          }
          mergedRepl.add(new SuggestedReplacement(repl.getReplacement(), String.join("\n", joinedRepls), repl.getSuffix()));
          handledReplacements.add(repl.getReplacement());
        }
      } else {
        mergedRepl.add(repl);
      }
    }
    return mergedRepl;
  }

  /**
   * Get the regular expression pattern used to tokenize
   * the words as in the source dictionary. For example,
   * it may contain a hyphen, if the words with hyphens are
   * not included in the dictionary
   * @return A compiled {@link Pattern} that is used to tokenize words or {@code null}.
   */
  @Nullable
  public Pattern tokenizingPattern() {
    return null;
  }

  protected List<SuggestedReplacement> orderSuggestions(List<SuggestedReplacement> suggestions, String word) {
    return suggestions;
  }

  /**
   * @param checkCompound If true and the word is not in the dictionary
   * it will be split (see {@link #setCompoundRegex(String)})
   * and each component will be checked separately
   * @since 2.4
   */
  protected void setCheckCompound(boolean checkCompound) {
    this.checkCompound = checkCompound;
  }

  /**
   * @param compoundRegex see {@link #setCheckCompound(boolean)}
   * @since 2.4
   */
  protected void setCompoundRegex(String compoundRegex) {
    this.compoundRegex = Pattern.compile(compoundRegex);
  }

  /**
   * Checks whether a given String is an Emoji with a string length larger 1.
   * @param word to be checked
   * @since 4.2
   */
  protected static boolean isEmoji(String word) {
    if (word.length() > 1 && word.codePointCount(0, word.length()) != word.length()) {
      // some symbols such as emojis (😂) have a string length that equals 2
      return EmojiManager.isOnlyEmojis(word);
    }
    return false;
  }

  /**
   * Ignore surrogate pairs (emojis) 
   * @since 4.3 
   * @see org.languagetool.rules.spelling.SpellingCheckRule#ignoreWord(java.lang.String)
   */
  @Override
  protected boolean ignoreWord(String word) throws IOException {
    return super.ignoreWord(word) || isEmoji(word);
  }
  
  /**
   * 
   * Join strings before and after a suggestion.
   * Used when there is also suggestion for split words
   * Ex. to thow > tot how | to throw
   * 
   */
  private static List<SuggestedReplacement> joinBeforeAfterSuggestions(List<SuggestedReplacement> suggestionsList, String beforeSuggestionStr,
                                                                       String afterSuggestionStr) {
    List<SuggestedReplacement> newSuggestionsList = new ArrayList<>();
    for (SuggestedReplacement suggestion : suggestionsList) {
      String str = suggestion.getReplacement();
      SuggestedReplacement newSuggestion = new SuggestedReplacement(suggestion);
      newSuggestion.setReplacement(beforeSuggestionStr + str + afterSuggestionStr);
      newSuggestionsList.add(newSuggestion);
    }
    return newSuggestionsList;
  }

  static class PhraseToTranslate {
    String phrase;
    int endPos;
    PhraseToTranslate(String phrase, int endPos) {
      this.phrase = phrase;
      this.endPos = endPos;
    }
  }
  
  /*
   * Get suggestions for a single word, using all the features of the Morfologik
   * spelling rule
   * 
   * @since 5.6
   */
  public List<String> getSpellingSuggestions(String w) throws IOException {
    List<String> suggestions = new ArrayList<>();
    AnalyzedTokenReadings[] atk = new AnalyzedTokenReadings[1];
    AnalyzedToken token = new AnalyzedToken(w, null, null);
    atk[0] = new AnalyzedTokenReadings(token);
    AnalyzedSentence sentence = new AnalyzedSentence(atk);
    RuleMatch[] matches = this.match(sentence);
    if (matches.length > 0) {
      suggestions.addAll(matches[0].getSuggestedReplacements());
    }
    return suggestions;
  }
  
}
