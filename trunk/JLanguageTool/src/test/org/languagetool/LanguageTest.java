/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import static org.junit.Assert.*;

import org.junit.Test;

public class LanguageTest {

	@Test
	public void testGetLanguageForShortName() {
		assertEquals(Language.AMERICAN_ENGLISH, Language.getLanguageForShortName("en-US"));
		assertEquals(Language.GERMAN, Language.getLanguageForShortName("de"));
	}
	
	@Test
	public void testGetShortNameWithVariant() {
		assertEquals("en-US", Language.AMERICAN_ENGLISH.getShortNameWithVariant());
		assertEquals("de", Language.GERMAN.getShortNameWithVariant());
	}

  @Test(expected=IllegalArgumentException.class)
 	public void testInvalidShortName1() {
    Language.getLanguageForShortName("de-");
  }

  @Test(expected=IllegalArgumentException.class)
 	public void testInvalidShortName2() {
    Language.getLanguageForShortName("dexx");
  }

  @Test(expected=IllegalArgumentException.class)
 	public void testInvalidShortName3() {
    Language.getLanguageForShortName("xyz-xx");
  }

	@Test
	public void testGetLanguageForName() {
		assertEquals(Language.AMERICAN_ENGLISH, Language.getLanguageForName("American English"));
		assertEquals(Language.GERMAN, Language.getLanguageForName("German"));
	}

  @Test
 	public void testIsVariant() {
 		assertTrue(Language.getLanguageForShortName("en-US").isVariant());
 		assertFalse(Language.getLanguageForShortName("en").isVariant());
    assertTrue(Language.getLanguageForShortName("de-CH").isVariant());
    assertFalse(Language.getLanguageForShortName("de").isVariant());
 	}

}
