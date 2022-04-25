/* LanguageTool, a natural language style checker 
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.language.GermanyGerman;

@Ignore
public class WikipediaQuickCheckTest {

  // only for interactive use, as it accesses a remote API
  public void noTestCheckPage() throws IOException, PageNotFoundException {
    WikipediaQuickCheck check = new WikipediaQuickCheck();
    //String url = "http://de.wikipedia.org/wiki/Benutzer_Diskussion:Dnaber";
    //String url = "http://de.wikipedia.org/wiki/OpenThesaurus";
    //String url = "http://de.wikipedia.org/wiki/Gütersloh";
    //String url = "http://de.wikipedia.org/wiki/Bielefeld";
    String url = "https://de.wikipedia.org/wiki/Augsburg";
    MarkupAwareWikipediaResult result = check.checkPage(new URL(url));
    List<AppliedRuleMatch> appliedMatches = result.getAppliedRuleMatches();
    System.out.println("ruleApplications: " + appliedMatches.size());
    for (AppliedRuleMatch appliedMatch : appliedMatches) {
      System.out.println("=====");
      System.out.println("Rule     : " + appliedMatch.getRuleMatch().getRule().getDescription() + "\n");
      for (RuleMatchApplication ruleMatchApplication : appliedMatch.getRuleMatchApplications()) {
        System.out.println("Original : " + ruleMatchApplication.getOriginalErrorContext(10).replace("\n", " "));
        if (ruleMatchApplication.hasRealReplacement()) {
          System.out.println("Corrected: " + ruleMatchApplication.getCorrectedErrorContext(10).replace("\n", " "));
        }
        System.out.println();
      }
    }
  }

  @Test
  public void testCheckWikipediaMarkup() throws IOException {
    WikipediaQuickCheck check = new WikipediaQuickCheck();
    String markup = "== Beispiele ==\n\n" +
            "Eine kleine Auswahl von Fehlern.\n\n" +
            "Das Komma ist richtig, wegen dem Leerzeichen.";
    MediaWikiContent wikiContent = new MediaWikiContent(markup, "2012-11-11T20:00:00");
    ErrorMarker errorMarker = new ErrorMarker("<err>", "</err>");
    MarkupAwareWikipediaResult result = check.checkWikipediaMarkup(new URL("http://fake-url.org"), wikiContent, new GermanyGerman(), errorMarker);
    assertThat(result.getLastEditTimestamp(), is("2012-11-11T20:00:00"));
    List<AppliedRuleMatch> appliedMatches = result.getAppliedRuleMatches();
    // even though this error has no suggestion, there's a (pseudo) correction:
    assertThat(appliedMatches.size(), is(1));
    AppliedRuleMatch firstAppliedMatch = appliedMatches.get(0);
    assertThat(firstAppliedMatch.getRuleMatchApplications().size(), is(1));
    RuleMatchApplication ruleMatchApplication = firstAppliedMatch.getRuleMatchApplications().get(0);
    assertTrue("Got: " + ruleMatchApplication.getTextWithCorrection(),
            ruleMatchApplication.getTextWithCorrection().contains("<err>wegen des Leerzeichens.</err>"));
    assertThat(ruleMatchApplication.getOriginalErrorContext(12), is("st richtig, <err>wegen dem Leerzeichen.</err>"));
    assertThat(ruleMatchApplication.getCorrectedErrorContext(12), is("st richtig, <err>wegen des Leerzeichens.</err>"));
  }

  @Test
  public void testGetPlainText() {
    WikipediaQuickCheck check = new WikipediaQuickCheck();
    String filteredContent = check.getPlainText(
            "<?xml version=\"1.0\"?><api><query><normalized><n from=\"Benutzer_Diskussion:Dnaber\" to=\"Benutzer Diskussion:Dnaber\" />" +
                    "</normalized><pages><page pageid=\"143424\" ns=\"3\" title=\"Benutzer Diskussion:Dnaber\"><revisions><rev xml:space=\"preserve\">\n" +
                    "Test [[Link]] Foo&amp;nbsp;bar.\n" +
                    "</rev></revisions></page></pages></query></api>");
    assertEquals("Test Link Foo\u00A0bar.", filteredContent);
  }

  @Test
  public void testGetPlainTextMapping() {
    WikipediaQuickCheck check = new WikipediaQuickCheck();
    String text = "Test [[Link]] und [[AnotherLink|noch einer]] und [http://test.org external link] Foo&amp;nbsp;bar.\n";
    PlainTextMapping filteredContent = check.getPlainTextMapping(
            "<?xml version=\"1.0\"?><api><query><normalized><n from=\"Benutzer_Diskussion:Dnaber\" to=\"Benutzer Diskussion:Dnaber\" />" +
                    "</normalized><pages><page pageid=\"143424\" ns=\"3\" title=\"Benutzer Diskussion:Dnaber\"><revisions><rev xml:space=\"preserve\">" +
                    text +
                    "</rev></revisions></page></pages></query></api>");

    assertEquals("Test Link und noch einer und external link Foo\u00A0bar.", filteredContent.getPlainText());
    assertEquals(1, filteredContent.getOriginalTextPositionFor(1).line);
    assertEquals(1, filteredContent.getOriginalTextPositionFor(1).column);
    assertEquals(filteredContent.getPlainText().charAt(0), text.charAt(0));

    assertEquals('u', text.charAt(14));  // note that these are zero-based, the others are not
    assertEquals('u', filteredContent.getPlainText().charAt(10));
    assertEquals(1, filteredContent.getOriginalTextPositionFor(11).line);
    assertEquals(15, filteredContent.getOriginalTextPositionFor(11).column);
  }

  @Test
  public void testGetPlainTextMappingMultiLine1() {
    WikipediaQuickCheck check = new WikipediaQuickCheck();
    String text = "Test [[Link]] und [[AnotherLink|noch einer]].\nUnd [[NextLink]] Foobar.\n";
    PlainTextMapping filteredContent = check.getPlainTextMapping(
            "<?xml version=\"1.0\"?><api><query><normalized><n from=\"Benutzer_Diskussion:Dnaber\" to=\"Benutzer Diskussion:Dnaber\" />" +
                    "</normalized><pages><page pageid=\"143424\" ns=\"3\" title=\"Benutzer Diskussion:Dnaber\"><revisions><rev xml:space=\"preserve\">" +
                    text +
                    "</rev></revisions></page></pages></query></api>");
    assertEquals("Test Link und noch einer. Und NextLink Foobar.", filteredContent.getPlainText());
    assertEquals(1, filteredContent.getOriginalTextPositionFor(1).line);
    assertEquals(1, filteredContent.getOriginalTextPositionFor(1).column);
    assertEquals(filteredContent.getPlainText().charAt(0), text.charAt(0));

    assertEquals('U', text.charAt(46));  // note that these are zero-based, the others are not
    assertEquals(' ', filteredContent.getPlainText().charAt(25));
    assertEquals('U', filteredContent.getPlainText().charAt(26));
    assertEquals(2, filteredContent.getOriginalTextPositionFor(27).line);

    assertEquals(45, filteredContent.getOriginalTextPositionFor(25).column);
    assertEquals(1, filteredContent.getOriginalTextPositionFor(26).column);
    assertEquals(2, filteredContent.getOriginalTextPositionFor(27).column);
  }

  @Test
  public void testGetPlainTextMappingMultiLine2() {
    WikipediaQuickCheck check = new WikipediaQuickCheck();
    String text = "Test [[Link]] und [[AnotherLink|noch einer]].\n\nUnd [[NextLink]] Foobar.\n";
    PlainTextMapping filteredContent = check.getPlainTextMapping(
            "<?xml version=\"1.0\"?><api><query><normalized><n from=\"Benutzer_Diskussion:Dnaber\" to=\"Benutzer Diskussion:Dnaber\" />" +
                    "</normalized><pages><page pageid=\"143424\" ns=\"3\" title=\"Benutzer Diskussion:Dnaber\"><revisions><rev xml:space=\"preserve\">" +
                    text +
                    "</rev></revisions></page></pages></query></api>");
    assertEquals("Test Link und noch einer.\n\nUnd NextLink Foobar.", filteredContent.getPlainText());
    assertEquals(1, filteredContent.getOriginalTextPositionFor(1).line);
    assertEquals(1, filteredContent.getOriginalTextPositionFor(1).column);
    assertEquals(filteredContent.getPlainText().charAt(0), text.charAt(0));

    assertEquals('U', text.charAt(47));  // note that these are zero-based, the others are not
    assertEquals('U', filteredContent.getPlainText().charAt(27));
    assertEquals(3, filteredContent.getOriginalTextPositionFor(28).line);
    assertEquals(45, filteredContent.getOriginalTextPositionFor(25).column);
    assertEquals(46, filteredContent.getOriginalTextPositionFor(26).column);
    assertEquals(47, filteredContent.getOriginalTextPositionFor(27).column);
    assertEquals(1, filteredContent.getOriginalTextPositionFor(28).column);
  }

  @Test
  public void testRemoveInterLanguageLinks() {
    WikipediaQuickCheck check = new WikipediaQuickCheck();
    assertEquals("foo  bar", check.removeWikipediaLinks("foo [[pt:Some Article]] bar"));
    assertEquals("foo [[some link]] bar", check.removeWikipediaLinks("foo [[some link]] bar"));
    assertEquals("foo [[Some Link]] bar ", check.removeWikipediaLinks("foo [[Some Link]] bar [[pt:Some Article]]"));
    assertEquals("foo [[zh-min-nan:Linux]] bar", check.removeWikipediaLinks("foo [[zh-min-nan:Linux]] bar"));  // known limitation
    assertEquals("[[Scultura bronzea di Gaudí mentre osserva il suo ''[[Il Capriccio|Capriccio]]'']]", check.removeWikipediaLinks("[[File:Gaudì-capriccio.JPG|thumb|left|Scultura bronzea di Gaudí mentre osserva il suo ''[[Il Capriccio|Capriccio]]'']]"));
    assertEquals("[[[[Palau de la Música Catalana]], entrada]]", check.removeWikipediaLinks("[[Fitxer:Palau_de_musica_2.JPG|thumb|[[Palau de la Música Catalana]], entrada]]"));
    assertEquals("foo  bar", check.removeWikipediaLinks("foo [[Kategorie:Kurgebäude]] bar"));
    assertEquals("foo [[''Kursaal Palace'' in San Sebastián]] bar", check.removeWikipediaLinks("foo [[Datei:FestivalSS.jpg|miniatur|''Kursaal Palace'' in San Sebastián]] bar"));
    assertEquals("[[Yupana, emprat pels [[Inques]].]]", check.removeWikipediaLinks("[[Fitxer:Yupana 1.GIF|thumb|Yupana, emprat pels [[Inques]].]]"));
  }

}
