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
}
