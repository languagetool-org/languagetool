/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wikipedia;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Premium;
import org.languagetool.language.English;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.de.GermanSpellerRule;
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

@Ignore
public class SuggestionReplacerTest {

  private final SwebleWikipediaTextFilter filter = new SwebleWikipediaTextFilter();
  private final GermanyGerman germanyGerman = new GermanyGerman();
  private final JLanguageTool lt = getLanguageTool();
  private final JLanguageTool englishLangTool = getLanguageTool(new English());

  @Test
  public void testApplySuggestionToOriginalText() throws Exception {
    SwebleWikipediaTextFilter filter = new SwebleWikipediaTextFilter();
    applySuggestion(lt, filter, "Die CD ROM.", "Die <s>CD-ROM.</s>");
    applySuggestion(lt, filter, "Die [[verlinkte]] CD ROM.", "Die [[verlinkte]] <s>CD-ROM.</s>");
    applySuggestion(lt, filter, "Die [[Link|verlinkte]] CD ROM.", "Die [[Link|verlinkte]] <s>CD-ROM.</s>");
    applySuggestion(lt, filter, "Die [[CD ROM]].", "Die <s>[[CD-ROM]].</s>");
    applySuggestion(lt, filter, "Der [[Abschied]].\n\n==Überschrift==\n\nEin Ab schied.",
                                      "Der [[Abschied]].\n\n==Überschrift==\n\nEin <s>Abschied.</s>");
    applySuggestion(lt, filter, "Ein ökonomischer Gottesdienst.",
                                      "Ein <s>ökumenischer</s> Gottesdienst.");
    applySuggestion(lt, filter, "Ein ökonomischer Gottesdienst mit ökonomischer Planung.",
                                      "Ein <s>ökumenischer</s> Gottesdienst mit ökonomischer Planung.");
    applySuggestion(lt, filter, "\nEin ökonomischer Gottesdienst.\n",
                                      "\nEin <s>ökumenischer</s> Gottesdienst.\n");
    applySuggestion(lt, filter, "\n\nEin ökonomischer Gottesdienst.\n",
                                      "\n\nEin <s>ökumenischer</s> Gottesdienst.\n");
  }

  @Test
  public void testNestedTemplates() throws Exception {
    String markup = "{{FNBox|\n" +
            "  {{FNZ|1|1979 und 1984}}\n" +
            "  {{FNZ|2|[[Rundungsfehler]]}}\n" +
            "}}\n\nEin ökonomischer Gottesdienst.\n";
    applySuggestion(lt, filter, markup, markup.replace("ökonomischer", "<s>ökumenischer</s>"));
  }

  @Test
  public void testReference1() throws Exception {
    String markup = "Hier <ref name=isfdb>\n" +
            "Retrieved 2012-07-31.</ref> steht,, das Haus.";
    applySuggestion(lt, filter, markup, markup.replace("steht,, das Haus.", "<s>steht,</s> das Haus."));
  }

  @Test
  public void testReference2() throws Exception {
    String markup = "Hier <ref name=\"NPOVxxx\" /> steht,, das Haus.";
    applySuggestion(lt, filter, markup, markup.replace("steht,, das Haus.", "<s>steht, das</s> Haus."));
  }

  @Test
  public void testErrorAtTextBeginning() throws Exception {
    String markup = "A hour ago\n";
    applySuggestion(englishLangTool, filter, markup, markup.replace("A", "<s>An</s>"));
  }

  @Test
  public void testErrorAtParagraphBeginning() throws Exception {
    String markup = "X\n\nA hour ago.\n";
    applySuggestion(englishLangTool, filter, markup, markup.replace("A", "<s>An</s>"));
  }

  @Test
  public void testKnownBug() throws Exception {
    String markup = "{{HdBG GKZ|9761000}}.";
    try {
      applySuggestion(lt, filter, markup, markup);
    } catch (RuntimeException e) {
      // known problem - Sweble's location seems to be wrong?!
    }
  }

  @Test
  public void testComplexText() throws Exception {
    String markup = "{{Dieser Artikel|behandelt die freie Onlineenzyklopädie Wikipedia; zu dem gleichnamigen Asteroiden siehe [[(274301) Wikipedia]].}}\n" +
            "\n" +
            "{{Infobox Website\n" +
            "| Name = '''Wikipedia'''\n" +
            "| Logo = [[Datei:Wikipedia-logo-v2-de.svg|180px|Das Wikipedia-Logo]]\n" +
            "| url = [//de.wikipedia.org/ de.wikipedia.org] (deutschsprachige Version)<br />\n" +
            "[//www.wikipedia.org/ www.wikipedia.org] (Übersicht aller Sprachen)\n" +
            "| Kommerziell = nein\n" +
            "| Beschreibung = [[Wiki]] einer freien kollektiv erstellten Online-Enzyklopädie\n" +
            "}}\n" +
            "\n" +
            "'''Wikipedia''' [{{IPA|ˌvɪkiˈpeːdia}}] (auch: ''die Wikipedia'') ist ein am [[15. Januar|15.&nbsp;Januar]] [[2001]] gegründetes Projekt. Und und so.\n";
    applySuggestion(lt, filter, markup, markup.replace("Und und so.", "<s>Und so.</s>"));
  }

  @Test
  @Ignore("fails for premium")
  public void testCompleteText() throws Exception {
    InputStream stream = SuggestionReplacerTest.class.getResourceAsStream("/org/languagetool/dev/wikipedia/wikipedia.txt");
    String origMarkup = IOUtils.toString(stream, StandardCharsets.UTF_8);
    JLanguageTool lt = new JLanguageTool(new GermanyGerman() {
      @Override
      protected synchronized List<AbstractPatternRule> getPatternRules() {
        return Collections.emptyList();
      }
    });
    lt.disableRule(GermanSpellerRule.RULE_ID);
    lt.disableRule("DE_AGREEMENT");
    lt.disableRule("GERMAN_WORD_REPEAT_BEGINNING_RULE");
    lt.disableRule("COMMA_PARENTHESIS_WHITESPACE");
    lt.disableRule("DE_CASE");
    lt.disableRule("ABKUERZUNG_LEERZEICHEN");
    lt.disableRule("TYPOGRAFISCHE_ANFUEHRUNGSZEICHEN");
    lt.disableRule("OLD_SPELLING");
    lt.disableRule("DE_TOO_LONG_SENTENCE_40");
    lt.disableRule("TOO_LONG_SENTENCE_DE");
    lt.disableRule("PUNCTUATION_PARAGRAPH_END");
    PlainTextMapping mapping = filter.filter(origMarkup);
    List<RuleMatch> matches = lt.check(mapping.getPlainText());
    if (Premium.isPremiumVersion()) {
      assertThat("Expected 3 matches, got: " + matches, matches.size(), is(10));
      System.out.println("Skipping part of testCompleteText() in premium mode...");
    } else {
      assertThat("Expected 3 matches, got: " + matches, matches.size(), is(3));
      int oldPos = 0;
      for (RuleMatch match : matches) {
        SuggestionReplacer replacer = new SuggestionReplacer(mapping, origMarkup, new ErrorMarker("<s>", "</s>"));
        List<RuleMatchApplication> ruleMatchApplications = replacer.applySuggestionsToOriginalText(match);
        assertThat(ruleMatchApplications.size(), is(1));
        RuleMatchApplication ruleMatchApplication = ruleMatchApplications.get(0);
        assertThat(StringUtils.countMatches(ruleMatchApplication.getTextWithCorrection(), "absichtlicher absichtlicher"), is(2));
        int pos = ruleMatchApplication.getTextWithCorrection().indexOf("<s>absichtlicher</s> Fehler");
        if (pos == -1) {
          // markup area varies because our mapping is sometimes a bit off:
          pos = ruleMatchApplication.getTextWithCorrection().indexOf("<s>absichtlicher Fehler</s>");
        }
        assertTrue("Found correction at: " + pos, pos > oldPos);
        oldPos = pos;
      }
    }
  }

  @Test
  public void testCompleteText2() throws Exception {
    InputStream stream = SuggestionReplacerTest.class.getResourceAsStream("/org/languagetool/dev/wikipedia/wikipedia2.txt");
    String origMarkup = IOUtils.toString(stream, StandardCharsets.UTF_8);
    JLanguageTool lt = new JLanguageTool(germanyGerman);
    PlainTextMapping mapping = filter.filter(origMarkup);
    lt.disableRule("PUNCTUATION_PARAGRAPH_END");  //  added to prevent crash; TODO: check if needed
    List<RuleMatch> matches = lt.check(mapping.getPlainText());
    assertTrue("Expected >= 29 matches, got: " + matches, matches.size() >= 29);
    for (RuleMatch match : matches) {
      SuggestionReplacer replacer = new SuggestionReplacer(mapping, origMarkup, new ErrorMarker("<s>", "</s>"));
      List<RuleMatchApplication> ruleMatchApplications = replacer.applySuggestionsToOriginalText(match);
      if (ruleMatchApplications.isEmpty()) {
        continue;
      }
      RuleMatchApplication ruleMatchApplication = ruleMatchApplications.get(0);
      assertThat(StringUtils.countMatches(ruleMatchApplication.getTextWithCorrection(), "<s>"), is(1));
    }
  }

  private JLanguageTool getLanguageTool() {
    JLanguageTool lt = getLanguageTool(germanyGerman);
    lt.disableRule("DE_CASE");
    return lt;
  }

  private JLanguageTool getLanguageTool(Language language) {
    return new JLanguageTool(language);
  }

  private void applySuggestion(JLanguageTool lt, SwebleWikipediaTextFilter filter, String text, String expected) throws IOException {
    PlainTextMapping mapping = filter.filter(text);
    List<RuleMatch> matches = lt.check(mapping.getPlainText());
    assertThat("Expected 1 match, got: " + matches, matches.size(), is(1));
    SuggestionReplacer replacer = new SuggestionReplacer(mapping, text, new ErrorMarker("<s>", "</s>"));
    List<RuleMatchApplication> ruleMatchApplications = replacer.applySuggestionsToOriginalText(matches.get(0));
    assertThat(ruleMatchApplications.size(), is(1));
    assertThat(ruleMatchApplications.get(0).getTextWithCorrection(), is(expected));
  }

}
