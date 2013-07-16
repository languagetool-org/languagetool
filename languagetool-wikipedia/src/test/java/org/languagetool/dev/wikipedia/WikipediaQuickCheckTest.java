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

import junit.framework.TestCase;

public class WikipediaQuickCheckTest extends TestCase {
  
  public void testGetFilteredWikiContent() {
    final WikipediaQuickCheck check = new WikipediaQuickCheck();
    final String filteredContent = check.getPlainText(
            "<?xml version=\"1.0\"?><api><query><normalized><n from=\"Benutzer_Diskussion:Dnaber\" to=\"Benutzer Diskussion:Dnaber\" />" +
                    "</normalized><pages><page pageid=\"143424\" ns=\"3\" title=\"Benutzer Diskussion:Dnaber\"><revisions><rev xml:space=\"preserve\">\n" +
                    "Test [[Link]] Foo&amp;nbsp;bar.\n" +
                    "</rev></revisions></page></pages></query></api>");
    assertEquals("Test Link Foo bar.", filteredContent);
  }

  public void testGetPlainTextMapping() {
    final WikipediaQuickCheck check = new WikipediaQuickCheck();
    final String text = "Test [[Link]] und [[AnotherLink|noch einer]] und [http://test.org external link] Foo&amp;nbsp;bar.\n";
    final PlainTextMapping filteredContent = check.getPlainTextMapping(
            "<?xml version=\"1.0\"?><api><query><normalized><n from=\"Benutzer_Diskussion:Dnaber\" to=\"Benutzer Diskussion:Dnaber\" />" +
                    "</normalized><pages><page pageid=\"143424\" ns=\"3\" title=\"Benutzer Diskussion:Dnaber\"><revisions><rev xml:space=\"preserve\">" +
                    text +
                    "</rev></revisions></page></pages></query></api>");

    assertEquals("Test Link und noch einer und Foo bar.", filteredContent.getPlainText());
    assertEquals(1, filteredContent.getOriginalTextPositionFor(1).line);
    assertEquals(1, filteredContent.getOriginalTextPositionFor(1).column);
    assertEquals(filteredContent.getPlainText().charAt(0), text.charAt(0));

    assertEquals('u', text.charAt(14));  // note that these are zero-based, the others are not
    assertEquals('u', filteredContent.getPlainText().charAt(10));
    assertEquals(1, filteredContent.getOriginalTextPositionFor(11).line);
    assertEquals(15, filteredContent.getOriginalTextPositionFor(11).column);
  }

  public void testGetPlainTextMappingMultiLine() {
    final WikipediaQuickCheck check = new WikipediaQuickCheck();
    final String text = "Test [[Link]] und [[AnotherLink|noch einer]].\nUnd [[NextLink]] Foobar.\n";
    final PlainTextMapping filteredContent = check.getPlainTextMapping(
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
    assertEquals(46, filteredContent.getOriginalTextPositionFor(26).column);
    assertEquals(1, filteredContent.getOriginalTextPositionFor(27).column);
  }

  public void testGetPlainTextMappingMultiLine2() {
    final WikipediaQuickCheck check = new WikipediaQuickCheck();
    final String text = "Test [[Link]] und [[AnotherLink|noch einer]].\n\nUnd [[NextLink]] Foobar.\n";
    final PlainTextMapping filteredContent = check.getPlainTextMapping(
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
    assertEquals(1, filteredContent.getOriginalTextPositionFor(27).column);
    assertEquals(2, filteredContent.getOriginalTextPositionFor(28).column);
  }

  public void testRemoveInterLanguageLinks() {
    final WikipediaQuickCheck check = new WikipediaQuickCheck();
    assertEquals("foo  bar", check.removeInterLanguageLinks("foo [[pt:Some Article]] bar"));
    assertEquals("foo [[some link]] bar", check.removeInterLanguageLinks("foo [[some link]] bar"));
    assertEquals("foo [[Some Link]] bar ", check.removeInterLanguageLinks("foo [[Some Link]] bar [[pt:Some Article]]"));
    assertEquals("foo [[zh-min-nan:Linux]] bar", check.removeInterLanguageLinks("foo [[zh-min-nan:Linux]] bar"));  // known limitation
  }

}
