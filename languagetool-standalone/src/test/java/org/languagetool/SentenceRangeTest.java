/*
 * LanguageTool, a natural language style checker
 * Copyright (c) 2022.  Stefan Viol (https://stevio.de)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  USA
 */

package org.languagetool;

import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.GermanyGerman;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.RemoteRuleConfig;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;

public class SentenceRangeTest {

  private class NoRulesGerman extends GermanyGerman {
    @Override
    public List<Rule> getRelevantRemoteRules(ResourceBundle messageBundle, List<RemoteRuleConfig> configs, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages, boolean inputLogging) throws IOException {
      return new ArrayList<>();
    }

    @Nullable
    @Override
    public SpellingCheckRule getDefaultSpellingRule(ResourceBundle messages) {
      return null;
    }

    @Override
    protected synchronized List<AbstractPatternRule> getPatternRules() throws IOException {
      return new ArrayList<>();
    }

    @Override
    public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel, UserConfig userConfig) throws IOException {
      return new ArrayList<>();
    }

    @Override
    public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
      return new ArrayList<>();
    }

    @Override
    public List<Rule> getRelevantLanguageModelCapableRules(ResourceBundle messages, @Nullable LanguageModel languageModel, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
      return new ArrayList<>();
    }
  }

  private class NoRulesEnglish extends AmericanEnglish {
    @Override
    public List<Rule> getRelevantRemoteRules(ResourceBundle messageBundle, List<RemoteRuleConfig> configs, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages, boolean inputLogging) throws IOException {
      return new ArrayList<>();
    }

    @Nullable
    @Override
    public SpellingCheckRule getDefaultSpellingRule(ResourceBundle messages) {
      return null;
    }

    @Override
    protected synchronized List<AbstractPatternRule> getPatternRules() throws IOException {
      return new ArrayList<>();
    }

    @Override
    public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel, UserConfig userConfig) throws IOException {
      return new ArrayList<>();
    }

    @Override
    public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
      return new ArrayList<>();
    }

    @Override
    public List<Rule> getRelevantLanguageModelCapableRules(ResourceBundle messages, @Nullable LanguageModel languageModel, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
      return new ArrayList<>();
    }
  }

  @Test
  public void testGermanSentenceRange() throws IOException {
    JLanguageTool jLanguageTool = new JLanguageTool(new NoRulesGerman());
    String text = "\n" +
            "\n" +
            "\n" +
            "\n" +
            "LanguageTool\n" +
            "\n" +
            "\n" +
            "\n" +
            "Unsere Grammatik-, Stil- und Rechtschreibprüfung ist in vielen Sprachen verfügbar und wird von Millionen Menschen weltweit genutzt\n" +
            "\n" +
            "\n" +
            "\n" +
            "\uFEFF\uFEFF\uFEFF\uFEFF\n" +
            "\n" +
            "Probieren Sie den LanguageTool-Editor aus.\n" +
            "\n" +
            "\n" +
            "\n" +
            "Bekommen Sie Tipps zur Verbesserung Ihrer Rechtschreibung (inklusive Kommasetzung u.v.m.) während Sie E-Mails schreiben, bloggen oder einfach nur twittern. LanguageTool erkennt automatisch, in welcher Sprache Sie schreiben. Um Ihre Daten zu schützen, werden vom Browser-Add-on keine Texte gespeichert.\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "Holen Sie alles aus Ihren Dokumenten heraus und liefern Sie fehlerfreie Ergebnisse ab. Egal, ob Sie an einer Dissertation arbeiten, einen Aufsatz oder ein Buch schreiben oder einfach nur Notizen machen.\n" +
            "\n" +
            "\n" +
            "\n" +
            "\uFEFF\u2063\n" +
            "\n" +
            "\n" +
            "\n" +
            "\uFEFFProfessionalisieren Sie die Kommunikation Ihres Teams mit der Grammatik- und Stilprüfung von LanguageTool.\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "Voll unterstützt (Rechtschreibung, Grammatik- und Stilhinweise):\n" +
            "\n" +
            "\n" +
            "\n" +
            "Englisch\n" +
            "\n" +
            "\n" +
            "\n" +
            "Deutsch\n" +
            "\n" +
            "\n" +
            "\n" +
            "Französisch\n" +
            "\n" +
            "\n" +
            "\n" +
            "Spanisch\n" +
            "\n" +
            "\n" +
            "\n" +
            "Niederländisch\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "Danke, dass Sie es ausprobieren!\n";
    AnnotatedText annotatedText = new AnnotatedTextBuilder().addText(text).build();
    CheckResults checkResults = jLanguageTool.check2(annotatedText,
            true,
            JLanguageTool.ParagraphHandling.NORMAL,
            ruleMatch -> {
            },
            JLanguageTool.Mode.ALL_BUT_TEXTLEVEL_ONLY,
            JLanguageTool.Level.DEFAULT,
            null);
    List<SentenceRange> sentenceRanges = checkResults.getSentenceRanges();
    assertEquals(17, sentenceRanges.size());
    assertEquals("LanguageTool",
            text.substring(
                    sentenceRanges.get(0).getFromPos(),
                    sentenceRanges.get(0).getToPos()));

    assertEquals("Unsere Grammatik-, Stil- und Rechtschreibprüfung ist in vielen Sprachen verfügbar und wird von Millionen Menschen weltweit genutzt",
            text.substring(
                    sentenceRanges.get(1).getFromPos(),
                    sentenceRanges.get(1).getToPos()));

    assertEquals("Probieren Sie den LanguageTool-Editor aus.",
            text.substring(
                    sentenceRanges.get(2).getFromPos(),
                    sentenceRanges.get(2).getToPos()));
    assertEquals("Bekommen Sie Tipps zur Verbesserung Ihrer Rechtschreibung (inklusive Kommasetzung u.v.m.) während Sie E-Mails schreiben, bloggen oder einfach nur twittern.",
            text.substring(
                    sentenceRanges.get(3).getFromPos(),
                    sentenceRanges.get(3).getToPos()));
    assertEquals("LanguageTool erkennt automatisch, in welcher Sprache Sie schreiben.",
            text.substring(
                    sentenceRanges.get(4).getFromPos(),
                    sentenceRanges.get(4).getToPos()));
    assertEquals("Um Ihre Daten zu schützen, werden vom Browser-Add-on keine Texte gespeichert.",
            text.substring(
                    sentenceRanges.get(5).getFromPos(),
                    sentenceRanges.get(5).getToPos()));
    assertEquals("Holen Sie alles aus Ihren Dokumenten heraus und liefern Sie fehlerfreie Ergebnisse ab.",
            text.substring(
                    sentenceRanges.get(6).getFromPos(),
                    sentenceRanges.get(6).getToPos()));
    assertEquals("Egal, ob Sie an einer Dissertation arbeiten, einen Aufsatz oder ein Buch schreiben oder einfach nur Notizen machen.",
            text.substring(
                    sentenceRanges.get(7).getFromPos(),
                    sentenceRanges.get(7).getToPos()));
    assertEquals("Professionalisieren Sie die Kommunikation Ihres Teams mit der Grammatik- und Stilprüfung von LanguageTool.",
            text.substring(
                    sentenceRanges.get(9).getFromPos(),
                    sentenceRanges.get(9).getToPos()));
    assertEquals("Voll unterstützt (Rechtschreibung, Grammatik- und Stilhinweise):",
            text.substring(
                    sentenceRanges.get(10).getFromPos(),
                    sentenceRanges.get(10).getToPos()));
    assertEquals("Englisch",
            text.substring(
                    sentenceRanges.get(11).getFromPos(),
                    sentenceRanges.get(11).getToPos()));
    assertEquals("Deutsch",
            text.substring(
                    sentenceRanges.get(12).getFromPos(),
                    sentenceRanges.get(12).getToPos()));
    assertEquals("Französisch",
            text.substring(
                    sentenceRanges.get(13).getFromPos(),
                    sentenceRanges.get(13).getToPos()));
    assertEquals("Spanisch",
            text.substring(
                    sentenceRanges.get(14).getFromPos(),
                    sentenceRanges.get(14).getToPos()));
    assertEquals("Niederländisch",
            text.substring(
                    sentenceRanges.get(15).getFromPos(),
                    sentenceRanges.get(15).getToPos()));
    assertEquals("Danke, dass Sie es ausprobieren!",
            text.substring(
                    sentenceRanges.get(16).getFromPos(),
                    sentenceRanges.get(16).getToPos()));
  }

  @Test
  public void testEnglishSentenceRange() throws IOException {
    JLanguageTool jLanguageTool = new JLanguageTool(new NoRulesEnglish());
    String text = "\n" +
            "\n" +
            "\n" +
            "\n" +
            "LanguageTool\n" +
            "\n" +
            "\n" +
            "\n" +
            "LanguageTool’s multilingual grammar, style, and spell checker is used by millions of people around the world.\n" +
            "\n" +
            "\n" +
            "\n" +
            "\uFEFF\uFEFF\uFEFF\uFEFF\n" +
            "\n" +
            "Trusted by our partners and customers\n" +
            "\n" +
            "\n" +
            "\n" +
            "Receive tips on how to improve your text (including punctuation advice etc.) while typing an e-mail, a blog post or just a simple tweet. Whatever language you're using, LanguageTool will automatically detect it and provide suggestions. To respect your privacy, no text is stored by the browser add-on.\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "Get the best out of your docs and deliver error-free results. No matter whether you're working on a dissertation, an essay, or a book, or you just want to note down something.\n" +
            "\n" +
            "\n" +
            "\n" +
            "\uFEFF\u2063\n" +
            "\n" +
            "\n" +
            "\n" +
            "\uFEFFProfessionalize your team's communication with LanguageTool's grammar and style checker.\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "Fully supported (spelling, grammar, style hints):\n" +
            "\n" +
            "\n" +
            "\n" +
            "English\n" +
            "\n" +
            "\n" +
            "\n" +
            "German\n" +
            "\n" +
            "\n" +
            "\n" +
            "French\n" +
            "\n" +
            "\n" +
            "\n" +
            "Spanish\n" +
            "\n" +
            "\n" +
            "\n" +
            "Dutch\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "Thanks for checking it out!\n";
    AnnotatedText annotatedText = new AnnotatedTextBuilder().addText(text).build();
    CheckResults checkResults = jLanguageTool.check2(annotatedText,
            true,
            JLanguageTool.ParagraphHandling.NORMAL,
            ruleMatch -> {
            },
            JLanguageTool.Mode.ALL_BUT_TEXTLEVEL_ONLY,
            JLanguageTool.Level.DEFAULT,
            null);
    List<SentenceRange> sentenceRanges = checkResults.getSentenceRanges();
    assertEquals(17, sentenceRanges.size());
    assertEquals("LanguageTool",
            text.substring(
                    sentenceRanges.get(0).getFromPos(),
                    sentenceRanges.get(0).getToPos()));

    assertEquals("LanguageTool’s multilingual grammar, style, and spell checker is used by millions of people around the world.",
            text.substring(
                    sentenceRanges.get(1).getFromPos(),
                    sentenceRanges.get(1).getToPos()));

    assertEquals("Trusted by our partners and customers",
            text.substring(
                    sentenceRanges.get(2).getFromPos(),
                    sentenceRanges.get(2).getToPos()));
    assertEquals("Receive tips on how to improve your text (including punctuation advice etc.) while typing an e-mail, a blog post or just a simple tweet.",
            text.substring(
                    sentenceRanges.get(3).getFromPos(),
                    sentenceRanges.get(3).getToPos()));
    assertEquals("Whatever language you're using, LanguageTool will automatically detect it and provide suggestions.",
            text.substring(
                    sentenceRanges.get(4).getFromPos(),
                    sentenceRanges.get(4).getToPos()));
    assertEquals("To respect your privacy, no text is stored by the browser add-on.",
            text.substring(
                    sentenceRanges.get(5).getFromPos(),
                    sentenceRanges.get(5).getToPos()));
    assertEquals("Get the best out of your docs and deliver error-free results.",
            text.substring(
                    sentenceRanges.get(6).getFromPos(),
                    sentenceRanges.get(6).getToPos()));
    assertEquals("No matter whether you're working on a dissertation, an essay, or a book, or you just want to note down something.",
            text.substring(
                    sentenceRanges.get(7).getFromPos(),
                    sentenceRanges.get(7).getToPos()));
    assertEquals("Professionalize your team's communication with LanguageTool's grammar and style checker.",
            text.substring(
                    sentenceRanges.get(9).getFromPos(),
                    sentenceRanges.get(9).getToPos()));
    assertEquals("Fully supported (spelling, grammar, style hints):",
            text.substring(
                    sentenceRanges.get(10).getFromPos(),
                    sentenceRanges.get(10).getToPos()));
    assertEquals("English",
            text.substring(
                    sentenceRanges.get(11).getFromPos(),
                    sentenceRanges.get(11).getToPos()));
    assertEquals("German",
            text.substring(
                    sentenceRanges.get(12).getFromPos(),
                    sentenceRanges.get(12).getToPos()));
    assertEquals("French",
            text.substring(
                    sentenceRanges.get(13).getFromPos(),
                    sentenceRanges.get(13).getToPos()));
    assertEquals("Spanish",
            text.substring(
                    sentenceRanges.get(14).getFromPos(),
                    sentenceRanges.get(14).getToPos()));
    assertEquals("Dutch",
            text.substring(
                    sentenceRanges.get(15).getFromPos(),
                    sentenceRanges.get(15).getToPos()));
    assertEquals("Thanks for checking it out!",
            text.substring(
                    sentenceRanges.get(16).getFromPos(),
                    sentenceRanges.get(16).getToPos()));
  }

  @Test
  public void testCorrectSentenceRange() {
    // An sentence list as it would come from a sentenceTokenizer        
    List<String> sentences = Arrays.asList(
            "Hallo,\n\n",
            "Das ist ein neuer Satz.",
            "\n\nEin Satz mit \uFEFFSonderzeichen.",
            "\n\n\n\n\nSatz mehreren Leerzeichen.",
            " Hier sind die Zeichen mal am Ende.\n\n\n",
            "\n\n\n\uFeFFNoch ein Satz.\n\n\n\n");
    String text = String.join("", sentences);
    AnnotatedText annotatedText = new AnnotatedTextBuilder().addText(text).build();
    List<SentenceRange> ranges = SentenceRange.getRangesFromSentences(annotatedText, sentences);
    assertEquals(6, ranges.size());

    SentenceRange sr1 = ranges.get(0);
    assertEquals(0, sr1.getFromPos());
    assertEquals(6, sr1.getToPos());

    SentenceRange sr2 = ranges.get(1);
    assertEquals(8, sr2.getFromPos());
    assertEquals(31, sr2.getToPos());

    SentenceRange sr3 = ranges.get(2);
    assertEquals(33, sr3.getFromPos());
    assertEquals(61, sr3.getToPos());

    SentenceRange sr4 = ranges.get(3);
    assertEquals(66, sr4.getFromPos());
    assertEquals(92, sr4.getToPos());

    SentenceRange sr5 = ranges.get(4);
    assertEquals(93, sr5.getFromPos());
    assertEquals(127, sr5.getToPos());

    SentenceRange sr6 = ranges.get(5);
    assertEquals(133, sr6.getFromPos());
    assertEquals(148, sr6.getToPos());

    StringBuilder sb = new StringBuilder();

    //Check if we get the trimmed sentences with the ranges from text
    for (SentenceRange sr : ranges) {
      sb.append(text, sr.getFromPos(), sr.getToPos());
    }
    assertEquals("Hallo,Das ist ein neuer Satz.Ein Satz mit \uFEFFSonderzeichen.Satz mehreren Leerzeichen.Hier sind die Zeichen mal am Ende.\uFEFFNoch ein Satz.", sb.toString());
  }

  @Test
  public void testSpecialCase() throws IOException {
    JLanguageTool jLanguageTool = new JLanguageTool(new NoRulesEnglish());
    String text = "\"This\"+is+Mr.+Pigfat+calling.\n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\nThis+is+an+\"test\".\n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\n \n\n\n\nHe+was+very+\"afraid\"+of+the+consequeces  ";
    AnnotatedText annotatedText = new AnnotatedTextBuilder().addText(text).build();

    CheckResults checkResults = jLanguageTool.check2(annotatedText,
      true,
      JLanguageTool.ParagraphHandling.NORMAL,
      ruleMatch -> {
      },
      JLanguageTool.Mode.ALL_BUT_TEXTLEVEL_ONLY,
      JLanguageTool.Level.PICKY,
      null);
    List<SentenceRange> sentenceRanges = checkResults.getSentenceRanges();
    assertEquals(51, sentenceRanges.size());

    SentenceRange testSentence1 = sentenceRanges.get(0);
    assertEquals(0, testSentence1.getFromPos());
    assertEquals(29, testSentence1.getToPos());
    assertEquals("\"This\"+is+Mr.+Pigfat+calling.", text.substring(testSentence1.getFromPos(), testSentence1.getToPos()));

    assertEquals(33, sentenceRanges.get(1).getFromPos());
    assertEquals(34, sentenceRanges.get(1).getToPos());

    assertEquals(38, sentenceRanges.get(2).getFromPos());
    assertEquals(39, sentenceRanges.get(2).getToPos());

    assertEquals(43, sentenceRanges.get(3).getFromPos());
    assertEquals(44, sentenceRanges.get(3).getToPos());

    assertEquals(48, sentenceRanges.get(4).getFromPos());
    assertEquals(49, sentenceRanges.get(4).getToPos());

    assertEquals(53, sentenceRanges.get(5).getFromPos());
    assertEquals(54, sentenceRanges.get(5).getToPos());

    assertEquals(58, sentenceRanges.get(6).getFromPos());
    assertEquals(59, sentenceRanges.get(6).getToPos());

    assertEquals(63, sentenceRanges.get(7).getFromPos());
    assertEquals(64, sentenceRanges.get(7).getToPos());

    assertEquals(68, sentenceRanges.get(8).getFromPos());
    assertEquals(69, sentenceRanges.get(8).getToPos());

    assertEquals(73, sentenceRanges.get(9).getFromPos());
    assertEquals(74, sentenceRanges.get(9).getToPos());

    assertEquals(78, sentenceRanges.get(10).getFromPos());
    assertEquals(79, sentenceRanges.get(10).getToPos());

    assertEquals(83, sentenceRanges.get(11).getFromPos());
    assertEquals(84, sentenceRanges.get(11).getToPos());

    assertEquals(88, sentenceRanges.get(12).getFromPos());
    assertEquals(89, sentenceRanges.get(12).getToPos());

    assertEquals(93, sentenceRanges.get(13).getFromPos());
    assertEquals(94, sentenceRanges.get(13).getToPos());

    assertEquals(98, sentenceRanges.get(14).getFromPos());
    assertEquals(99, sentenceRanges.get(14).getToPos());

    assertEquals(103, sentenceRanges.get(15).getFromPos());
    assertEquals(104, sentenceRanges.get(15).getToPos());

    assertEquals(108, sentenceRanges.get(16).getFromPos());
    assertEquals(109, sentenceRanges.get(16).getToPos());

    assertEquals(113, sentenceRanges.get(17).getFromPos());
    assertEquals(114, sentenceRanges.get(17).getToPos());

    assertEquals(118, sentenceRanges.get(18).getFromPos());
    assertEquals(119, sentenceRanges.get(18).getToPos());

    assertEquals(123, sentenceRanges.get(19).getFromPos());
    assertEquals(124, sentenceRanges.get(19).getToPos());

    assertEquals(128, sentenceRanges.get(20).getFromPos());
    assertEquals(129, sentenceRanges.get(20).getToPos());

    assertEquals(133, sentenceRanges.get(21).getFromPos());
    assertEquals(134, sentenceRanges.get(21).getToPos());

    assertEquals(138, sentenceRanges.get(22).getFromPos());
    assertEquals(139, sentenceRanges.get(22).getToPos());

    assertEquals(143, sentenceRanges.get(23).getFromPos());
    assertEquals(144, sentenceRanges.get(23).getToPos());

    assertEquals(148, sentenceRanges.get(24).getFromPos());
    assertEquals(149, sentenceRanges.get(24).getToPos());

    SentenceRange testSentence2 = sentenceRanges.get(25);
    assertEquals(153, testSentence2.getFromPos());
    assertEquals(171, testSentence2.getToPos());
    assertEquals("This+is+an+\"test\".", text.substring(testSentence2.getFromPos(), testSentence2.getToPos()));

    assertEquals(175, sentenceRanges.get(26).getFromPos());
    assertEquals(176, sentenceRanges.get(26).getToPos());

    assertEquals(180, sentenceRanges.get(27).getFromPos());
    assertEquals(181, sentenceRanges.get(27).getToPos());

    assertEquals(185, sentenceRanges.get(28).getFromPos());
    assertEquals(186, sentenceRanges.get(28).getToPos());

    assertEquals(190, sentenceRanges.get(29).getFromPos());
    assertEquals(191, sentenceRanges.get(29).getToPos());

    assertEquals(195, sentenceRanges.get(30).getFromPos());
    assertEquals(196, sentenceRanges.get(30).getToPos());

    assertEquals(200, sentenceRanges.get(31).getFromPos());
    assertEquals(201, sentenceRanges.get(31).getToPos());

    assertEquals(205, sentenceRanges.get(32).getFromPos());
    assertEquals(206, sentenceRanges.get(32).getToPos());

    assertEquals(210, sentenceRanges.get(33).getFromPos());
    assertEquals(211, sentenceRanges.get(33).getToPos());

    assertEquals(215, sentenceRanges.get(34).getFromPos());
    assertEquals(216, sentenceRanges.get(34).getToPos());

    assertEquals(220, sentenceRanges.get(35).getFromPos());
    assertEquals(221, sentenceRanges.get(35).getToPos());

    assertEquals(225, sentenceRanges.get(36).getFromPos());
    assertEquals(226, sentenceRanges.get(36).getToPos());

    assertEquals(230, sentenceRanges.get(37).getFromPos());
    assertEquals(231, sentenceRanges.get(37).getToPos());

    assertEquals(235, sentenceRanges.get(38).getFromPos());
    assertEquals(236, sentenceRanges.get(38).getToPos());

    assertEquals(240, sentenceRanges.get(39).getFromPos());
    assertEquals(241, sentenceRanges.get(39).getToPos());

    assertEquals(245, sentenceRanges.get(40).getFromPos());
    assertEquals(246, sentenceRanges.get(40).getToPos());

    assertEquals(250, sentenceRanges.get(41).getFromPos());
    assertEquals(251, sentenceRanges.get(41).getToPos());

    assertEquals(255, sentenceRanges.get(42).getFromPos());
    assertEquals(256, sentenceRanges.get(42).getToPos());

    assertEquals(260, sentenceRanges.get(43).getFromPos());
    assertEquals(261, sentenceRanges.get(43).getToPos());

    assertEquals(265, sentenceRanges.get(44).getFromPos());
    assertEquals(266, sentenceRanges.get(44).getToPos());

    assertEquals(270, sentenceRanges.get(45).getFromPos());
    assertEquals(271, sentenceRanges.get(45).getToPos());

    assertEquals(275, sentenceRanges.get(46).getFromPos());
    assertEquals(276, sentenceRanges.get(46).getToPos());

    assertEquals(280, sentenceRanges.get(47).getFromPos());
    assertEquals(281, sentenceRanges.get(47).getToPos());

    assertEquals(285, sentenceRanges.get(48).getFromPos());
    assertEquals(286, sentenceRanges.get(48).getToPos());

    assertEquals(290, sentenceRanges.get(49).getFromPos());
    assertEquals(291, sentenceRanges.get(49).getToPos());

    SentenceRange testSentence3 = sentenceRanges.get(50);
    assertEquals(295, testSentence3.getFromPos());
    assertEquals(336, testSentence3.getToPos());
    assertEquals("He+was+very+\"afraid\"+of+the+consequeces  ", text.substring(testSentence3.getFromPos(), testSentence3.getToPos()));
  }
}
