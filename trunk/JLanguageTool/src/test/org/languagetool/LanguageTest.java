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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

public class LanguageTest {

    @Test
    public void testGetAllLanguages() {
        assertTrue(Language.getAllLanguages().size() > 35);
    }

    @Test
    public void testRuleFileName() {
        assertEquals("[/rules/en/grammar.xml, /rules/en/en-GB/grammar.xml]", Language.BRITISH_ENGLISH.getRuleFileName().toString());
        assertEquals("[/rules/en/grammar.xml]", Language.AMERICAN_ENGLISH.getRuleFileName().toString());
        assertEquals("[/rules/en/grammar.xml]", Language.ENGLISH.getRuleFileName().toString());
        assertEquals("[/rules/de/grammar.xml]", Language.GERMAN.getRuleFileName().toString());
    }

    @Test
    public void testGetTranslatedName() {
        assertEquals("English", Language.ENGLISH.getTranslatedName(TestTools.getMessages("en")));
        assertEquals("English (British)", Language.BRITISH_ENGLISH.getTranslatedName(TestTools.getMessages("en")));

        assertEquals("Englisch", Language.ENGLISH.getTranslatedName(TestTools.getMessages("de")));
        assertEquals("Englisch (Großbritannien)", Language.BRITISH_ENGLISH.getTranslatedName(TestTools.getMessages("de")));
        assertEquals("Deutsch", Language.GERMAN.getTranslatedName(TestTools.getMessages("de")));
        assertEquals("Deutsch (Schweiz)", Language.SWISS_GERMAN.getTranslatedName(TestTools.getMessages("de")));
    }

    @Test
    public void testGetShortNameWithVariant() {
        assertEquals("en-US", Language.AMERICAN_ENGLISH.getShortNameWithVariant());
        assertEquals("de", Language.GERMAN.getShortNameWithVariant());
    }

    @Test
    public void testGetLanguageForShortName() {
        assertEquals(Language.AMERICAN_ENGLISH, Language.getLanguageForShortName("en-US"));
        assertEquals(Language.GERMAN, Language.getLanguageForShortName("de"));
        assertEquals(null, Language.getLanguageForShortName("xy"));
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
        assertEquals(Language.AMERICAN_ENGLISH, Language.getLanguageForName("English (US)"));
        assertEquals(Language.GERMAN, Language.getLanguageForName("German"));
        assertEquals(null, Language.getLanguageForName("Foobar"));
    }

    @Test
    public void testIsVariant() {
        assertTrue(Language.getLanguageForShortName("en-US").isVariant());
        assertTrue(Language.getLanguageForShortName("de-CH").isVariant());

        assertFalse(Language.getLanguageForShortName("en").isVariant());
        assertFalse(Language.getLanguageForShortName("de").isVariant());
    }

    @Test
    public void testHasVariant() {
        assertTrue(Language.getLanguageForShortName("en").hasVariant());
        assertTrue(Language.getLanguageForShortName("de").hasVariant());

        assertFalse(Language.getLanguageForShortName("en-US").hasVariant());
        assertFalse(Language.getLanguageForShortName("de-CH").hasVariant());
        assertFalse(Language.getLanguageForShortName("ast").hasVariant());
        assertFalse(Language.getLanguageForShortName("pl").hasVariant());

        for (Language language : Language.LANGUAGES) {
            if (language.hasVariant()) {
                assertNotNull("Language " + language + " needs a default variant", language.getDefaultVariant());
            }
        }
    }

    @Test
    public void testGetLanguageForLocale() {
        assertEquals(Language.GERMANY_GERMAN, Language.getLanguageForLocale(new Locale("de", "DE")));
        assertEquals(Language.AUSTRIAN_GERMAN, Language.getLanguageForLocale(new Locale("de", "AT")));
        assertEquals(Language.AMERICAN_ENGLISH, Language.getLanguageForLocale(new Locale("en", "US")));
        assertEquals(Language.BRITISH_ENGLISH, Language.getLanguageForLocale(new Locale("en", "GB")));
        // fallback to the language's default variant if not specified:
        assertEquals(Language.AMERICAN_ENGLISH, Language.getLanguageForLocale(new Locale("en")));
        assertEquals(Language.GERMANY_GERMAN, Language.getLanguageForLocale(new Locale("de")));
        assertEquals(Language.POLISH, Language.getLanguageForLocale(new Locale("pl")));
        // final fallback is everything else fails:
        assertEquals(Language.AMERICAN_ENGLISH, Language.getLanguageForLocale(Locale.KOREAN));
        assertEquals(Language.AMERICAN_ENGLISH, Language.getLanguageForLocale(new Locale("zz")));
    }

    @Test
    public void testEqualsConsiderVariantIfSpecified() {
        // every language equals itself:
        assertTrue(Language.GERMAN.equalsConsiderVariantsIfSpecified(Language.GERMAN));
        assertTrue(Language.GERMANY_GERMAN.equalsConsiderVariantsIfSpecified(Language.GERMANY_GERMAN));
        assertTrue(Language.ENGLISH.equalsConsiderVariantsIfSpecified(Language.ENGLISH));
        assertTrue(Language.AMERICAN_ENGLISH.equalsConsiderVariantsIfSpecified(Language.AMERICAN_ENGLISH));
        // equal if variant is the same, but only if specified:
        assertTrue(Language.AMERICAN_ENGLISH.equalsConsiderVariantsIfSpecified(Language.ENGLISH));
        assertTrue(Language.ENGLISH.equalsConsiderVariantsIfSpecified(Language.AMERICAN_ENGLISH));

        assertFalse(Language.AMERICAN_ENGLISH.equalsConsiderVariantsIfSpecified(Language.BRITISH_ENGLISH));
        assertFalse(Language.ENGLISH.equalsConsiderVariantsIfSpecified(Language.GERMAN));
    }

    @Test
    public void testGetAllMaintainers() {
        assertTrue(Language.getAllMaintainers(TestTools.getMessages("en")).length() > 100);
    }
}
