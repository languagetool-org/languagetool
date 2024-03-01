/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.openoffice;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.ResultCache;
import org.languagetool.ToneTag;
import org.languagetool.UserConfig;
import org.languagetool.JLanguageTool.Mode;
import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.gui.Configuration;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.openoffice.DocumentCache.AnalysedText;
import org.languagetool.openoffice.DocumentCache.TextParagraph;
import org.languagetool.openoffice.OfficeTools.RemoteCheck;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

/**
 * Class to switch between running LanguageTool in multi or single thread mode
 * @since 4.6
 * @author Fred Kruse
 */
public class SwJLanguageTool {
  
  private static final ResourceBundle MESSAGES = JLanguageTool.getMessageBundle();

  private final MultiThreadedJLanguageToolLo mlt;
  private final LORemoteLanguageTool rlt;
  private JLanguageToolLo lt;

  private boolean isMultiThread;
  private boolean isRemote;
  private boolean doReset;
  private Configuration config;

  public SwJLanguageTool(Language language, Language motherTongue, UserConfig userConfig, 
      Configuration config, List<Rule> extraRemoteRules, boolean testMode) throws MalformedURLException {
    this.config = config;
    isMultiThread = config.isMultiThread();
    isRemote = config.doRemoteCheck() && !testMode;
    doReset = false;
    if (isRemote) {
      lt = null;
      mlt = null;
      rlt = new LORemoteLanguageTool(language, motherTongue, config, extraRemoteRules, userConfig);
      if (!rlt.remoteRun()) {
        MessageHandler.showMessage(MESSAGES.getString("loRemoteSwitchToLocal"));
        isRemote = false;
        isMultiThread = false;
        lt = new JLanguageToolLo(language, motherTongue, null, userConfig);
      }
    } else if (isMultiThread) {
      lt = null;
      mlt = new MultiThreadedJLanguageToolLo(language, motherTongue, userConfig);
      rlt = null;
    } else {
      lt = new JLanguageToolLo(language, motherTongue, null, userConfig);
      mlt = null;
      rlt = null;
    }
  }
  
  /**
   * Return true if check is done by a remote server
   */
  public boolean isRemote() {
    return isRemote;
  }

  /**
   * Get all rules
   */
  public List<Rule> getAllRules() {
    if (isRemote) {
      return rlt.getAllRules();
    } else if (isMultiThread) {
      return mlt.getAllRules(); 
    } else {
      return lt.getAllRules(); 
    }
  }

  /**
   * Get all active rules
   */
  public List<Rule> getAllActiveRules() {
    if (isRemote) {
      return rlt.getAllActiveRules();
    } else if (isMultiThread) {
        return mlt.getAllActiveRules(); 
    } else {
      return lt.getAllActiveRules(); 
    }
  }

  /**
   * Get all active office rules
   */
  public List<Rule> getAllActiveOfficeRules() {
    if (isRemote) {
      return rlt.getAllActiveOfficeRules();
    } else if (isMultiThread) {
        return mlt.getAllActiveOfficeRules(); 
    } else {
      return lt.getAllActiveOfficeRules(); 
    }
  }

  /**
   * Enable a rule by ID
   */
  public void enableRule(String ruleId) {
    if (isRemote) {
      rlt.enableRule(ruleId);
    } else if (isMultiThread) {
      mlt.enableRule(ruleId); 
    } else {
      lt.enableRule(ruleId); 
    }
  }

  /**
   * Disable a rule by ID
   */
  public void disableRule(String ruleId) {
    if (isRemote) {
      rlt.disableRule(ruleId);
    } else if (isMultiThread) {
      mlt.disableRule(ruleId); 
    } else {
      lt.disableRule(ruleId); 
    }
  }

  /**
   * Get disabled rules
   */
  public Set<String> getDisabledRules() {
    if (isRemote) {
      return rlt.getDisabledRules();
    } else if (isMultiThread) {
        return mlt.getDisabledRules(); 
    } else {
      return lt.getDisabledRules(); 
    }
  }

  /**
   * Disable a category by ID
   */
  public void disableCategory(CategoryId id) {
    if (isRemote) {
      rlt.disableCategory(id);
    } else if (isMultiThread) {
        mlt.disableCategory(id); 
    } else {
      lt.disableCategory(id); 
    }
  }

  /**
   * Activate language model (ngram) rules
   */
  public void activateLanguageModelRules(File indexDir) throws IOException {
    if (!isRemote) {
      if (isMultiThread) {
        mlt.activateLanguageModelRules(indexDir); 
      } else {
        lt.activateLanguageModelRules(indexDir); 
      }
    }
  }

  /**
   * check text by LT
   * default: check only grammar
   * local: LT checks only grammar (spell check is not implemented locally)
   * remote: spell checking is used for LT check dialog (is needed because method getAnalyzedSentence is not supported by remote check)
   */
  public List<RuleMatch> check(TextParagraph from, TextParagraph to, String text, ParagraphHandling paraMode, 
      SingleDocument document) throws IOException {
    return check(from, to, text, paraMode, document, RemoteCheck.ALL);
  }

  public List<RuleMatch> check(TextParagraph from, TextParagraph to, String text, ParagraphHandling paraMode, 
      SingleDocument document, RemoteCheck checkMode) throws IOException {
    if (isRemote) {
      List<RuleMatch> ruleMatches = rlt.check(text, paraMode, checkMode);
      if (ruleMatches == null) {
        doReset = true;
        ruleMatches = new ArrayList<>();
      }
      return ruleMatches;
    } else {
      Mode mode;
      if (paraMode == ParagraphHandling.ONLYNONPARA) {
        mode = Mode.ALL_BUT_TEXTLEVEL_ONLY;
      } else if (paraMode == ParagraphHandling.ONLYPARA) {
        mode = Mode.TEXTLEVEL_ONLY;
      } else {
        mode = Mode.ALL;
      }
      Set<ToneTag> toneTags = config.enableGoalSpecificRules() ? Collections.singleton(ToneTag.ALL_TONE_RULES) : Collections.emptySet();
      if (isMultiThread) {
        synchronized(mlt) {
          return mlt.check(from, to, paraMode, mode, document, this, toneTags);
        }
      } else {
        return lt.check(from, to, paraMode, mode, document, this, toneTags);
      }
    }
  }

  public List<RuleMatch> check(String text, ParagraphHandling paraMode, 
      int nFPara, SingleDocument document) throws IOException {
    return check(text, paraMode, nFPara, document, RemoteCheck.ALL);
  }

  public List<RuleMatch> check(String text, ParagraphHandling paraMode, 
      int nFPara, SingleDocument document, RemoteCheck checkMode) throws IOException {
    if (isRemote) {
      List<RuleMatch> ruleMatches = rlt.check(text, paraMode, checkMode);
      if (ruleMatches == null) {
        doReset = true;
        ruleMatches = new ArrayList<>();
      }
      return ruleMatches;
    } else {
      Mode mode;
      if (paraMode == ParagraphHandling.ONLYNONPARA) {
        mode = Mode.ALL_BUT_TEXTLEVEL_ONLY;
      } else if (paraMode == ParagraphHandling.ONLYPARA) {
        mode = Mode.TEXTLEVEL_ONLY;
      } else {
        mode = Mode.ALL;
      }
      Set<ToneTag> toneTags = config.enableGoalSpecificRules() ? Collections.singleton(ToneTag.ALL_TONE_RULES) : Collections.emptySet();
      if (isMultiThread) {
        synchronized(mlt) {
          return mlt.check(text, paraMode, mode, nFPara, document, this, toneTags);
        }
      } else {
        return lt.check(text, paraMode, mode, nFPara, document, this, toneTags);
      }
    }
  }
  /**
   * Get a list of tokens from a sentence
   * This Method may be used only for local checks
   * use local lt for remote checks
   */
  public List<String> sentenceTokenize(String text) {
    if (isRemote) {
      return lt.sentenceTokenize(text);
    } else if (isMultiThread) {
        return mlt.sentenceTokenize(text); 
    } else {
      return lt.sentenceTokenize(text); 
    }
  }

  /**
   * Analyze sentence
   * This Method may be used only for local checks
   * use local lt for remote checks
   */
  public AnalyzedSentence getAnalyzedSentence(String sentence) throws IOException {
    if (isRemote) {
      return lt.getAnalyzedSentence(sentence);
    } else if (isMultiThread) {
        return mlt.getAnalyzedSentence(sentence); 
    } else {
      return lt.getAnalyzedSentence(sentence); 
    }
  }

  /**
   * Analyze text
   * This Method may be used only for local checks
   * use local lt for remote checks
   */
  public List<AnalyzedSentence> analyzeText(String text) throws IOException {
    if (isRemote) {
      return lt.analyzeText(text);
    } else if (isMultiThread) {
        return mlt.analyzeText(text); 
    } else {
      return lt.analyzeText(text); 
    }
  }

  /**
   * get the lemmas of a word
   * @throws IOException 
   */
  public List<String> getLemmasOfWord(String word) throws IOException {
    List<String> lemmas = new ArrayList<String>();
    Language language = getLanguage();
    List<String> words = new ArrayList<>();
    words.add(word);
    List<AnalyzedTokenReadings> aTokens = language.getTagger().tag(words);
    for (AnalyzedTokenReadings aToken : aTokens) {
      List<AnalyzedToken> readings = aToken.getReadings();
      for (AnalyzedToken reading : readings) {
        String lemma = reading.getLemma();
        if (lemma != null) {
          lemmas.add(lemma);
        }
      }
    }
    return lemmas;
  }

  /**
   * get the lemmas of a word
   * @throws IOException 
   */
  public List<String> getLemmasOfParagraph(String para, int startPos) throws IOException {
    List<String> lemmas = new ArrayList<String>();
    List<AnalyzedSentence> sentences = analyzeText(para);
    int pos = 0;
    for (AnalyzedSentence sentence : sentences) {
      for (AnalyzedTokenReadings token : sentence.getTokens()) {
        if (pos + token.getStartPos() == startPos) {
          for (AnalyzedToken reading : token) {
            String lemma = reading.getLemma();
            if (lemma != null) {
              lemmas.add(lemma);
            }
          }
        }
      }
      pos += sentence.getCorrectedTextLength();
    }
    return lemmas;
  }

  /**
   * Get the language from LT
   */
  public Language getLanguage() {
    if (isRemote) {
      return rlt.getLanguage();
    } else if (isMultiThread) {
      return mlt.getLanguage(); 
    } else {
      return lt.getLanguage(); 
    }
  }
  
  /**
   * Set reset flag
   */
  public boolean doReset() {
    return doReset;
  }
  
  public class JLanguageToolLo extends JLanguageTool {

    public JLanguageToolLo(Language language, Language motherTongue, ResultCache cache, UserConfig userConfig) {
      super(language, motherTongue, cache, userConfig);
    }

    public List<RuleMatch> check(String text, ParagraphHandling paraMode, Mode mode, 
        int nFPara, SingleDocument document, SwJLanguageTool lt, @NotNull Set<ToneTag> toneTags) throws IOException {

      List<AnalyzedSentence> analyzedSentences;
      List<String> sentences;
      if (nFPara < 0) {
        analyzedSentences = this.analyzeText(text);
        sentences = new ArrayList<>();
        for (AnalyzedSentence analyzedSentence : analyzedSentences) {
          sentences.add(analyzedSentence.getText());
        }
      } else {
        AnalysedText analysedText = document.getDocumentCache().getOrCreateAnalyzedParagraph(nFPara, lt);
        analyzedSentences = analysedText.analyzedSentences;
        sentences = analysedText.sentences;
        text = analysedText.text;
      }
      return checkInternal(new AnnotatedTextBuilder().addText(text).build(), paraMode, null, mode, 
          Level.PICKY, toneTags, null, sentences, analyzedSentences).getRuleMatches();
    }

    public List<RuleMatch> check(TextParagraph from, TextParagraph to, ParagraphHandling paraMode, Mode mode, 
        SingleDocument document, SwJLanguageTool lt, @NotNull Set<ToneTag> toneTags) throws IOException {
      AnalysedText analysedText = document.getDocumentCache().getAnalyzedParagraphs(from, to, lt);
      if (analysedText == null) {
        return null;
      }
      List<AnalyzedSentence> analyzedSentences = analysedText.analyzedSentences;
      List<String> sentences = analysedText.sentences;
      String text = analysedText.text;
      return checkInternal(new AnnotatedTextBuilder().addText(text).build(), paraMode, null, mode, 
          Level.PICKY, toneTags, null, sentences, analyzedSentences).getRuleMatches();
    }

  }

  public class MultiThreadedJLanguageToolLo extends MultiThreadedJLanguageTool {

    public MultiThreadedJLanguageToolLo(Language language, Language motherTongue, UserConfig userConfig) {
      super(language, motherTongue, userConfig);
    }

    public List<RuleMatch> check(String text, ParagraphHandling paraMode, Mode mode, 
        int nFPara, SingleDocument document, SwJLanguageTool lt, @NotNull Set<ToneTag> toneTags) throws IOException {

      List<AnalyzedSentence> analyzedSentences;
      List<String> sentences;
      if (nFPara < 0) {
        analyzedSentences = this.analyzeText(text);
        sentences = new ArrayList<>();
        for (AnalyzedSentence analyzedSentence : analyzedSentences) {
          sentences.add(analyzedSentence.getText());
        }
      } else {
        AnalysedText analysedText = document.getDocumentCache().getOrCreateAnalyzedParagraph(nFPara, lt);
        analyzedSentences = analysedText.analyzedSentences;
        sentences = analysedText.sentences;
        text = analysedText.text;
      }
      return checkInternal(new AnnotatedTextBuilder().addText(text).build(), paraMode, null, mode, 
          Level.PICKY, toneTags, null, sentences, analyzedSentences).getRuleMatches();
    }

    public List<RuleMatch> check(TextParagraph from, TextParagraph to, ParagraphHandling paraMode, Mode mode, 
        SingleDocument document, SwJLanguageTool lt, @NotNull Set<ToneTag> toneTags) throws IOException {
      AnalysedText analysedText = document.getDocumentCache().getAnalyzedParagraphs(from, to, lt);
      if (analysedText == null) {
        return null;
      }
      List<AnalyzedSentence> analyzedSentences = analysedText.analyzedSentences;
      List<String> sentences = analysedText.sentences;
      String text = analysedText.text;
      return checkInternal(new AnnotatedTextBuilder().addText(text).build(), paraMode, null, mode, 
          Level.PICKY, toneTags, null, sentences, analyzedSentences).getRuleMatches();
    }

  }

}
