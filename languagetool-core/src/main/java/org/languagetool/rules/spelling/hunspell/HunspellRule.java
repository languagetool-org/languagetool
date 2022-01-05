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

import com.google.common.io.Resources;
import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.languagetool.*;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.noop.NoopLanguage;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.SuggestedReplacement;
import org.languagetool.rules.spelling.RuleWithLanguage;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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

  private volatile boolean needsInit = true;
  protected volatile HunspellDictionary hunspell = null;

  private static final Logger logger = LoggerFactory.getLogger(HunspellRule.class);
  private static final ConcurrentLinkedQueue<String> activeChecks = new ConcurrentLinkedQueue<>();
  private static final String NON_ALPHABETIC = "[^\\p{L}]";

  private static final boolean monitorRules = System.getProperty("monitorActiveRules") != null;

  //300 most common Portuguese words. They are used to avoid wrong split suggestions
  private final List<String> commonPortugueseWords = Arrays.asList("eu", "ja", "so", "de", "e", "a", "o", "da", "do", "em", "que", "uma", "um", "com", "no", "se", "na", "para", "por", "os", "foi", "como", "dos", "as", "ao", "mais", "sua", "das", "não", "ou", "km", "seu", "pela", "ser", "pelo", "são", "também", "anos", "cidade", "entre", "era", "tem", "mas", "habitantes", "nos", "seus", "área", "até", "ele", "onde", "foram", "população", "região", "sobre", "nas", "nome", "parte", "quando", "ano", "aos", "grande", "mesmo", "pode", "primeiro", "segundo", "sendo", "suas", "ainda", "dois", "estado", "está", "família", "já", "muito", "outros", "americano", "depois", "durante", "maior", "primeira", "forma", "apenas", "banda", "densidade", "dia", "então", "município", "norte", "tempo", "após", "duas", "num", "pelos", "qual", "século", "ter", "todos", "três", "vez", "água", "acordo", "cobertos", "comuna", "contra", "ela", "grupo", "principal", "quais", "sem", "tendo", "às", "álbum", "alguns", "assim", "asteróide", "bem", "brasileiro", "cerca", "desde", "este", "localizada", "mundo", "outras", "período", "seguinte", "sido", "vida", "através", "cada", "conhecido", "final", "história", "partir", "país", "pessoas", "sistema", "terra", "teve", "tinha", "época", "administrativa", "censo", "departamento", "dias", "esta", "filme", "francesa", "música", "província", "série", "vezes", "além", "antes", "eles", "eram", "espécie", "governo", "podem", "vários", "censos", "distrito", "estão", "exemplo", "hoje", "início", "jogo", "lhe", "lugar", "muitos", "média", "novo", "numa", "número", "pois", "possui", "sob", "só", "todo", "tornou", "trabalho", "algumas", "devido", "estava", "fez", "filho", "fim", "grandes", "há", "isso", "lado", "local", "morte", "orbital", "outro", "passou", "países", "quatro", "representa", "seja", "sempre", "sul", "várias", "capital", "chamado", "começou", " enquanto", "fazer", "lançado", "meio", "nova", "nível", "pelas", "poder", "presidente", "redor", "rio", "tarde", "todas", "carreira", "casa", "década", "estimada", "guerra", "havia", "livro", "localidades", "maioria", "muitas", "obra", "origem", "pai", "pouco", "principais", "produção", "programa", "qualquer", "raio", "seguintes", "sucesso", "título", "aproximadamente", "caso", "centro", "conhecida", "construção", "desta", "diagrama", "faz", "ilha", "importante", "mar", "melhor", "menos", "mesma", "metros", "mil", "nacional", "populacional", "quase", "rei", "sede", "segunda", "tipo", "toda", "uso", "velocidade", "vizinhança", "volta", "base", "brasileira", "clube", "desenvolvimento", "deste", "diferentes", "diversos", "empresa", "entanto", "futebol", "geral", "junto", "longo", "obras", "outra", "pertencente", "política", "português", "principalmente", "processo", "quem", "seria", "têm", "versão", "TV", "acima", "atual", "bairro", "chamada", "cinco", "conta", "corpo", "dentro", "deve");

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
  private final List<RuleWithLanguage> enSpellRules;

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
    enSpellRules = getEnglishSpellingRules();
  }

  private List<RuleWithLanguage> getEnglishSpellingRules() {
    List<RuleWithLanguage> spellingRules = new ArrayList<>();
    Language en;
    try {
      en = Languages.getLanguageForShortCode("en-US");
    } catch (IllegalArgumentException e) {
      logger.warn("Could not create en-US language for spell-check fallback, multi-lingual spell checking is not available");
      return spellingRules;
    }
    List<Rule> rules;
    try {
      rules = new ArrayList<>(en.getRelevantRules(messages, userConfig, null, Collections.emptyList()));
      rules.addAll(en.getRelevantLanguageModelCapableRules(messages, null,
              null, userConfig, null, Collections.emptyList()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    for (Rule rule : rules) {
      if (rule.isDictionaryBasedSpellingRule()) {
        spellingRules.add(new RuleWithLanguage(rule, en));
      }
    }
    return spellingRules;
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
    ensureInitialized();
    if (hunspell == null) {
      // some languages might not have a dictionary, be silent about it
      return toRuleMatchArray(ruleMatches);
    }

    long sentLength = Arrays.stream(sentence.getTokensWithoutWhitespace()).filter(k -> !k.isNonWord()).count() - 1;  // -1 for the SENT_START token
    String monitoringText = getClass().getName() + ":" + getId() + ":" + sentence.getText();
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
      int prevStartPos = -1;
      int misspelledButEnglish = 0;
      for (int i = 0; i < tokens.length; i++) {
        String word = tokens[i];
        int dashCorr = 0;
        if ((ignoreWord(Arrays.asList(tokens), i) || ignoreWord(word)) && !isProhibited(cutOffDot(word))) {
          prevStartPos = len;
          len += word.length() + 1;
          continue;
        }
        if (isMisspelled(word)) {
          if (isEnglish(word)) {
            misspelledButEnglish++;
          }
          String cleanWord = word.endsWith(".") ? word.substring(0, word.length() - 1) : word;
          if (word.startsWith("-")) {
            if (!isMisspelled(cleanWord.substring(1)) || cleanWord.matches("-+")) {
              len += word.length() + 1;
              continue;
            } else {
              dashCorr++;
            }
          }
          if (i > 0 && prevStartPos != -1) {
            String prevWord = tokens[i-1];
            boolean ignoreSplitting = false;
            if (this.language.getShortCode().equals("pt") && (commonPortugueseWords.contains(prevWord.toLowerCase())
                || commonPortugueseWords.contains(word.toLowerCase()))) {
              ignoreSplitting = true;
            }
            if (!ignoreSplitting && prevWord.length() > 0) {
              // "thanky ou" -> "thank you"
              String sugg1a = prevWord.substring(0, prevWord.length()-1);
              String sugg1b = cutOffDot(prevWord.substring(prevWord.length()-1) + word);
              if (!isMisspelled(sugg1a) && !isMisspelled(sugg1b) && acceptSuggestion(sugg1a + " " + sugg1b)) {
                RuleMatch rm = createWrongSplitMatch(sentence, ruleMatches, len, cleanWord, sugg1a, sugg1b, prevStartPos);
                if (rm != null) {
                  ruleMatches.add(rm);
                }
              }
              // "than kyou" -> "thank you"
              String sugg2a = prevWord + word.charAt(0);
              String sugg2b = cutOffDot(word.substring(1));
              if (!isMisspelled(sugg2a) && !isMisspelled(sugg2b) && acceptSuggestion(sugg2a + " " + sugg2b)) {
                RuleMatch rm = createWrongSplitMatch(sentence, ruleMatches, len, cleanWord, sugg2a, sugg2b, prevStartPos);
                if (rm != null) {
                  ruleMatches.add(rm);
                }
              }
            }
          }
          
          RuleMatch ruleMatch = new RuleMatch(this, sentence,
            len + dashCorr, len + cleanWord.length(),
            messages.getString("spelling"),
            messages.getString("desc_spelling_short"));
          ruleMatch.setType(RuleMatch.Type.UnknownWord);
          String cleanWord2 = cleanWord.substring(dashCorr);
          if (userConfig == null || userConfig.getMaxSpellingSuggestions() == 0 || ruleMatches.size() <= userConfig.getMaxSpellingSuggestions()) {
            ruleMatch.setLazySuggestedReplacements(() -> {
              try {
                List<SuggestedReplacement> sugg = calcSuggestions(word, cleanWord2);
                if (isFirstItemHighConfidenceSuggestion(word, sugg)) {
                  sugg.get(0).setConfidence(HIGH_CONFIDENCE);
                }
                return sugg;
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
          } else {
            // limited to save CPU
            ruleMatch.setSuggestedReplacement(messages.getString("too_many_errors"));
          }
          ruleMatches.add(ruleMatch);
          if (sentLength > 3) {
            float enRatio = (float)misspelledButEnglish / sentLength;
            //System.out.println("ER en??: " + enRatio + ": " + misspelledButEnglish + " /  " + sentLength + " - " + sentence.getText());
            if (enRatio > 0.66) {
              //System.out.println("ER en!!: " + enRatio + ": " + misspelledButEnglish + " /  " + sentLength + " - " + sentence.getText());
              ruleMatch.setErrorLimitLang("en");
              //break; -- don't stop to keep current behaviour
            } else {
              float otherRatio = (float)ruleMatches.size() / sentLength;
              //System.out.println("ER other??: " + otherRatio + ": " + ruleMatches.size() + " /  " + sentLength + " - " + sentence.getText());
              if (otherRatio > 0.66) {
                //System.out.println("ER other!!: " + otherRatio + ": " + ruleMatches.size() + " /  " + sentLength + " - " + sentence.getText());
                ruleMatch.setErrorLimitLang(NoopLanguage.SHORT_CODE);
                //break; -- don't stop to keep current behaviour
              }
            }
          }
        }
        prevStartPos = len + dashCorr;
        len += word.length() + 1;
      }
    } finally {
      if (monitorRules) {
        activeChecks.remove(monitoringText);
      }
    }
    /*if (sentence.getErrorLimitReached()) {
      return toRuleMatchArray(Collections.emptyList());
    }*/
    /*if (language.getShortCode().equals("de")) {
      for (RuleMatch ruleMatch : ruleMatches) {
        int i = 1;
        for (String repl : ruleMatch.getSuggestedReplacements()) {
          if (i <= 5 && repl.matches("[A-ZÖÄÜ][a-zöäüß]+ [a-zöäüß]+")) {
            // potential word with "Deppenleerzeichen":
            System.out.println("repl: " + sentence.getText().substring(ruleMatch.getFromPos(), ruleMatch.getToPos()) + " => " +  repl + " - " + i);
          }
          i++;
        }
      }
    }*/
    return toRuleMatchArray(ruleMatches);
  }

  protected boolean acceptSuggestion(String suggestion) {
    return true;
  }

  boolean isFirstItemHighConfidenceSuggestion(String word, List<SuggestedReplacement> sugg) {
    // finds cases like "HAus", where "Haus" is surely the proper suggestion:
    if (sugg.size() > 0 &&
        !word.equals("IPs") &&
        word.equalsIgnoreCase(sugg.get(0).getReplacement()) &&
        word.matches("[A-Z][A-Z]\\p{javaLowerCase}+") &&
        language.getShortCode().equals("de")) {
      System.out.println("speller high conf case: " + word + "; " + sugg.get(0).getReplacement() + "; " + language.getShortCodeWithCountryAndVariant());
      if (word.endsWith("s") && StringUtils.isAllUpperCase(sugg.get(0).getReplacement())) {
        // e.g. "DMs" could mean plural of "DM"
        return false;
      }
      return true;
    }
    return false;
  }

  private boolean isEnglish(String word) throws IOException {
    for (RuleWithLanguage altRule : enSpellRules) {
      AnalyzedToken token = new AnalyzedToken(word, null, null);
      AnalyzedToken sentenceStartToken = new AnalyzedToken("", JLanguageTool.SENTENCE_START_TAGNAME, null);
      AnalyzedTokenReadings startTokenReadings = new AnalyzedTokenReadings(sentenceStartToken, 0);
      AnalyzedTokenReadings atr = new AnalyzedTokenReadings(token, 0);
      RuleMatch[] matches = altRule.getRule().match(new AnalyzedSentence(new AnalyzedTokenReadings[]{startTokenReadings, atr}));
      if (matches.length == 0) {
        return true;
      } else {
        if (word.endsWith(".")) {
          return isEnglish(word.substring(0, word.length() - 1));
        }
      }
    }
    return false;
  }

  private List<SuggestedReplacement> calcSuggestions(String word, String cleanWord) throws IOException {
    List<SuggestedReplacement> onlySuggestions = getOnlySuggestions(cleanWord);
    if (!onlySuggestions.isEmpty()) {
      return onlySuggestions;
    }
    List<SuggestedReplacement> suggestions = SuggestedReplacement.convert(getSuggestions(cleanWord));
    if (word.endsWith(".")) {
      int pos = 1;
      for (String suggestion : getSuggestions(word)) {
        if (suggestions.stream().noneMatch(sr -> suggestion.equals(sr.getReplacement()))) {
          suggestions.add(Math.min(pos, suggestions.size()), new SuggestedReplacement(suggestion.substring(0, suggestion.length()-1)));
          pos += 2;  // we mix the lists, as we don't know which one is the better one
        }
      }
    }
    List<SuggestedReplacement> additionalTopSuggestions = getAdditionalTopSuggestions(suggestions, cleanWord);
    if (additionalTopSuggestions.isEmpty() && word.endsWith(".")) {
      additionalTopSuggestions = getAdditionalTopSuggestions(suggestions, word).
        stream()
        .map(sugg -> {
          if (sugg.getReplacement().endsWith(".")) {
            return sugg;
          } else {
            SuggestedReplacement newSugg = new SuggestedReplacement(sugg);
            newSugg.setReplacement(sugg.getReplacement() + ".");
            return newSugg;
          }
        }).collect(Collectors.toList());
    }
    Collections.reverse(additionalTopSuggestions);
    for (SuggestedReplacement additionalTopSuggestion : additionalTopSuggestions) {
      if (!cleanWord.equals(additionalTopSuggestion.getReplacement())) {
        suggestions.add(0, additionalTopSuggestion);
      }
    }
    List<SuggestedReplacement> additionalSuggestions = getAdditionalSuggestions(suggestions, cleanWord);
    for (SuggestedReplacement additionalSuggestion : additionalSuggestions) {
      if (!cleanWord.equals(additionalSuggestion.getReplacement())) {
        suggestions.addAll(additionalSuggestions);
      }
    }
    suggestions = suggestions.stream().filter(k -> acceptSuggestion(k.getReplacement())).collect(Collectors.toList());
    suggestions = filterDupes(filterSuggestions(suggestions));
    // Find potentially missing compounds with privacy-friendly logging: we only log a single unknown word with no
    // meta data and only if it's made up of two valid words, similar to the "UNKNOWN" logging in
    // GermanSpellerRule:
    /*if (language.getShortCode().equals("de")) {
      String covered = sentence.getText().substring(len, len + cleanWord.length());
      if (suggestions.stream().anyMatch(
            k -> k.getReplacement().contains(" ") &&
            StringTools.uppercaseFirstChar(k.getReplacement().replaceAll(" ", "").toLowerCase()).equals(covered) &&
            k.getReplacement().length() > 6 && k.getReplacement().length() < 25 &&
            k.getReplacement().matches("[a-zA-ZÖÄÜöäüß -]+")
          )) {
        logger.info("COMPOUND: " + covered);
      }
    }*/
    // TODO user suggestions
    return suggestions;
  }

  private static String cutOffDot(String s) {
    return s.endsWith(".") ? s.substring(0, s.length()-1) : s;
  }

  /**
   * @since public since 4.1
   */
  @Override
  public boolean isMisspelled(String word) {
    try {
      ensureInitialized();
      boolean isAlphabetic = true;
      if (word.length() == 1) { // hunspell dictionaries usually do not contain punctuation
        isAlphabetic = Character.isAlphabetic(word.charAt(0));
      }
      return (
              isAlphabetic && !"--".equals(word)
              && (hunspell != null && !hunspell.spell(word))
              && !ignoreWord(word)
             )
             || isProhibited(cutOffDot(word));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<String> getSuggestions(String word) throws IOException {
    ensureInitialized();
    return hunspell.suggest(word);
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
          sb.append(' ').append(token.substring(1));
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
        // some symbols such as emojis (😂) have a string length larger than 1 
        List<String> emojis = EmojiParser.extractEmojis(token);
        for (String emoji : emojis) {
          token = StringUtils.replace(token, emoji, WHITESPACE_ARRAY[emoji.length()]);
        }
        sb.append(token);
      } else {
        sb.append(token);
      }
    }
    return sb.toString();
  }

  protected final void ensureInitialized() throws IOException {
    if (needsInit) {
      synchronized (this) {
        if (needsInit) {
          try {
            init();
          } finally {
            needsInit = false;
          }
        }
      }
    }
  }

  @Override
  protected synchronized void init() throws IOException {
    super.init();
    String langCountry = language.getShortCode();
    if (language.getCountries().length > 0) {
      langCountry += "_" + language.getCountries()[0];
    }
    String shortDicPath = getDictFilenameInResources(langCountry);
    String wordChars = "";
    // set dictionary only if there are dictionary files:
    Path affPath = null;
    if (JLanguageTool.getDataBroker().resourceExists(shortDicPath)) {
      String path = getDictionaryPath(langCountry, shortDicPath);
      if ("".equals(path)) {
        hunspell = null;
      } else {
        affPath = Paths.get(path + ".aff");
        hunspell = Hunspell.getDictionary(Paths.get(path + ".dic"), affPath);
        addIgnoreWords();
      }
    } else if (new File(shortDicPath + ".dic").exists()) {
      // for dynamic languages
      affPath = Paths.get(shortDicPath + ".aff");
      hunspell = Hunspell.getDictionary(Paths.get(shortDicPath + ".dic"), affPath);
    }
    if (affPath != null) {
      try(Scanner sc = new Scanner(affPath)){
        while (sc.hasNextLine()) {
          String line = sc.nextLine();
          if (line.startsWith("WORDCHARS ")) {
            String wordCharsFromAff = line.substring("WORDCHARS ".length());
            //System.out.println("#" + wordCharsFromAff+ "#");
            wordChars = "(?![" + wordCharsFromAff.replace("-", "\\-") + "])";
            break;
          }
        }
      }
      
    }
    nonWordPattern = Pattern.compile(wordChars + NON_ALPHABETIC);
  }

  @NotNull
  protected String getDictFilenameInResources(String langCountry) {
    return "/" + language.getShortCode() + "/hunspell/" + langCountry + FILE_EXTENSION;
  }

  private void addIgnoreWords() throws IOException {
    URL ignoreUrl = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(getIgnoreFileName());
    List<String> ignoreLines = Resources.readLines(ignoreUrl, StandardCharsets.UTF_8);
    for (String ignoreLine : ignoreLines) {
      if (!ignoreLine.startsWith("#")) {
        wordsToBeIgnored.add(ignoreLine);
      }
    }
  }

  @Override
  protected boolean isInIgnoredSet(String word) {
    return super.isInIgnoredSet(word) ||
           // We don't add these words to the main set to prevent startsWithIgnoredWord honoring them,
           // and thus to avoid the bogus "LanguageToolaccount" suggestion in AgreementRuleTest.
           // A better way might be nice.
           SpellingCheckRule.LANGUAGETOOL.equals(word) || SpellingCheckRule.LANGUAGETOOLER.equals(word);
  }

  private static String getDictionaryPath(String dicName,
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

  private static void fileCopy(InputStream in, File targetFile) throws IOException {
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
