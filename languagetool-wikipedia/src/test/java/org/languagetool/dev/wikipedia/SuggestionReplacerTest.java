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

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.de.GermanSpellerRule;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SuggestionReplacerTest extends TestCase {

  public void testApplySuggestionToOriginalText() throws Exception {
    JLanguageTool langTool = getLanguageTool();
    SwebleWikipediaTextFilter filter = new SwebleWikipediaTextFilter();
    applySuggestion(langTool, filter, "Die CD ROM.", "Die CD-ROM.");
    applySuggestion(langTool, filter, "Die [[verlinkte]] CD ROM.", "Die [[verlinkte]] CD-ROM.");
    applySuggestion(langTool, filter, "Die [[Link|verlinkte]] CD ROM.", "Die [[Link|verlinkte]] CD-ROM.");
    applySuggestion(langTool, filter, "Die [[CD ROM]].", "Die [[CD-ROM]].");
    applySuggestion(langTool, filter, "Der [[Abschied]].\n\n==Überschrift==\n\nEin Ab schied.",
            "Der [[Abschied]].\n\n==Überschrift==\n\nEin Abschied.");
    applySuggestion(langTool, filter, "Ein ökonomischer Gottesdienst.",
            "Ein ökumenisch Gottesdienst.");  // known problem: does not inflect
    applySuggestion(langTool, filter, "Ein ökonomischer Gottesdienst mit ökonomischer Planung.",
            "Ein ökumenisch Gottesdienst mit ökonomischer Planung.");
  }

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
    JLanguageTool langTool = getLanguageTool();
    SwebleWikipediaTextFilter filter = new SwebleWikipediaTextFilter();
    applySuggestion(langTool, filter, markup, markup.replace("Und und so.", "Und so."));
  }

  public void testCompleteText() throws Exception {
    InputStream stream = SuggestionReplacerTest.class.getResourceAsStream("/org/languagetool/dev/wikipedia/wikipedia.txt");
    String origMarkup = IOUtils.toString(stream);
    JLanguageTool langTool = new JLanguageTool(new GermanyGerman());
    langTool.disableRule(GermanSpellerRule.RULE_ID);
    langTool.disableRule("DE_AGREEMENT");
    langTool.disableRule("GERMAN_WORD_REPEAT_BEGINNING_RULE");
    langTool.disableRule("COMMA_PARENTHESIS_WHITESPACE");
    langTool.disableRule("DE_CASE");
    langTool.disableRule("ABKUERZUNG_LEERZEICHEN");
    langTool.disableRule("TYPOGRAFISCHE_ANFUEHRUNGSZEICHEN");
    SwebleWikipediaTextFilter filter = new SwebleWikipediaTextFilter();
    PlainTextMapping mapping = filter.filter(origMarkup);
    List<RuleMatch> matches = langTool.check(mapping.getPlainText());
    assertThat("Expected 3 matches, got: " + matches, matches.size(), is(3));
    String markup = origMarkup;
    int oldPos = 0;
    for (RuleMatch match : matches) {
      SuggestionReplacer replacer = new SuggestionReplacer(mapping, markup);
      List<String> newTexts = replacer.applySuggestionsToOriginalText(match);
      assertThat(newTexts.size(), is(1));
      String newText = newTexts.get(0);
      assertThat(StringUtils.countMatches(newText, "absichtlicher absichtlicher"), is(2));
      int pos = newText.indexOf("in absichtlicher Fehler");  // 'in' because of Ein/ein
      assertTrue(pos > oldPos);
      oldPos = pos;
    }
  }

  private JLanguageTool getLanguageTool() throws IOException {
    JLanguageTool langTool = new JLanguageTool(new GermanyGerman());
    langTool.activateDefaultPatternRules();
    langTool.disableRule("DE_CASE");
    return langTool;
  }

  private void applySuggestion(JLanguageTool langTool, SwebleWikipediaTextFilter filter, String text, String expected) throws IOException {
    PlainTextMapping mapping = filter.filter(text);
    List<RuleMatch> matches = langTool.check(mapping.getPlainText());
    assertThat("Expected 1 match, got: " + matches, matches.size(), is(1));
    SuggestionReplacer replacer = new SuggestionReplacer(mapping, text);
    List<String> newTexts = replacer.applySuggestionsToOriginalText(matches.get(0));
    assertThat(newTexts.size(), is(1));
    assertThat(newTexts.get(0), is(expected));
  }

}
