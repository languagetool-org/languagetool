/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.CleanOverlappingFilter;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.SameRuleGroupFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

public class MultiLingualLanguageTool {

  private final Language mainLang;
  private final Language secondLang;
  private final JLanguageTool lt1;
  private final JLanguageTool lt2;

  public MultiLingualLanguageTool(Language mainLang, Language secondLang) {
    this.mainLang = mainLang;
    this.secondLang = secondLang;
    lt1 = new JLanguageTool(mainLang);
    lt2 = new JLanguageTool(secondLang);
  }

  public List<RuleMatch> check(AnnotatedText annotatedText, RuleMatchListener listener, JLanguageTool.Mode mode) throws IOException {
    List<String> sentences = lt1.sentenceTokenize(annotatedText.getPlainText());

    List<AnalyzedSentence> analyzedSentences = analyzeSentences(sentences);
    LanguageAnnotator annotator = new LanguageAnnotator(mainLang, secondLang);
    annotator.annotateWithLanguage(analyzedSentences);
    String mainLangText = getTextForLanguages(analyzedSentences, mainLang);
    String secondLangText = getTextForLanguages(analyzedSentences, secondLang);
    System.out.println("MAIN: " + mainLangText);
    System.out.println("SEC: " + secondLangText);

    List<RuleMatch> matches1 = lt1.check(mainLangText, listener);
    List<RuleMatch> matches2 = lt2.check(secondLangText, listener);
    ArrayList<RuleMatch> matches = new ArrayList<>();
    matches.addAll(matches1);
    matches.addAll(matches2);
    return matches;
  }

  private String getTextForLanguages(List<AnalyzedSentence> analyzedSentences, Language lang) {
    StringBuilder sb = new StringBuilder();
    for (AnalyzedSentence analyzedSentence : analyzedSentences) {
      for (AnalyzedTokenReadings token : analyzedSentence.getTokens()) {
        if (token.getLanguage() == lang) {
          sb.append(token.getToken());
        }
      }
    }
    return sb.toString();
  }

  private List<AnalyzedSentence> analyzeSentences(List<String> sentences) throws IOException {
    List<AnalyzedSentence> analyzedSentences = new ArrayList<>();
    int j = 0;
    for (String sentence : sentences) {
      AnalyzedSentence analyzedSentence = lt1.getAnalyzedSentence(sentence);
      //rememberUnknownWords(analyzedSentence);
      if (++j == sentences.size()) {
        AnalyzedTokenReadings[] anTokens = analyzedSentence.getTokens();
        anTokens[anTokens.length - 1].setParagraphEnd();
        analyzedSentence = new AnalyzedSentence(anTokens);
      }
      analyzedSentences.add(analyzedSentence);
      //printSentenceInfo(analyzedSentence);
    }
    return analyzedSentences;
  }


  public List<RuleMatch> checkFIXME(AnnotatedText annotatedText, JLanguageTool.ParagraphHandling paraMode, RuleMatchListener listener, JLanguageTool.Mode mode) throws IOException {
    List<RuleMatch> matches1 = lt1.check(annotatedText, true, JLanguageTool.ParagraphHandling.NORMAL, listener, mode);
    List<RuleMatch> matches2 = lt1.check(annotatedText, true, JLanguageTool.ParagraphHandling.NORMAL, listener, mode);
    List<RuleMatch> matches = new ArrayList<>(matches1);
    matches.addAll(matches2);
    return matches;
  }
}
