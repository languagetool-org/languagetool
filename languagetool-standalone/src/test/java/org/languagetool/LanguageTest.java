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
import static org.junit.Assert.fail;

import java.util.Locale;

import org.junit.Test;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.BritishEnglish;
import org.languagetool.language.English;
import org.languagetool.language.German;
import org.languagetool.language.GermanyGerman;
import org.languagetool.language.SwissGerman;

public class LanguageTest {

  @Test
  public void testGetAllLanguages() {
    assertTrue(Language.getAllLanguages().size() > 35);  // includes variants like en-GB
  }

  @Test
  public void testRuleFileName() {
    assertEquals("[/org/languagetool/rules/en/grammar.xml, /org/languagetool/rules/en/en-GB/grammar.xml]", new BritishEnglish().getRuleFileNames().toString());
    assertEquals("[/org/languagetool/rules/en/grammar.xml, /org/languagetool/rules/en/en-US/grammar.xml]", new AmericanEnglish().getRuleFileNames().toString());
    assertEquals("[/org/languagetool/rules/en/grammar.xml]", new English().getRuleFileNames().toString());
    assertEquals("[/org/languagetool/rules/de/grammar.xml]", new German().getRuleFileNames().toString());
    assertEquals("[/org/languagetool/rules/de/grammar.xml]", new German().getRuleFileName().toString());  // old, deprecated API
  }

  @Test
  public void testGetTranslatedName() {
    assertEquals("English", new English().getTranslatedName(TestTools.getMessages("en")));
    assertEquals("English (British)", new BritishEnglish().getTranslatedName(TestTools.getMessages("en")));

    assertEquals("Englisch", new English().getTranslatedName(TestTools.getMessages("de")));
    assertEquals("Englisch (Großbritannien)", new BritishEnglish().getTranslatedName(TestTools.getMessages("de")));
    assertEquals("Deutsch", new German().getTranslatedName(TestTools.getMessages("de")));
    assertEquals("Deutsch (Schweiz)", new SwissGerman().getTranslatedName(TestTools.getMessages("de")));
  }

  @Test
  public void testGetShortNameWithVariant() {
    assertEquals("en-US", new AmericanEnglish().getShortNameWithCountryAndVariant());
    assertEquals("de", new German().getShortNameWithCountryAndVariant());
  }

  @Test
  public void testGetLanguageForShortName() {
    assertEquals("en-US", Language.getLanguageForShortName("en-us").getShortNameWithCountryAndVariant());
    assertEquals("en-US", Language.getLanguageForShortName("EN-US").getShortNameWithCountryAndVariant());
    assertEquals("en-US", Language.getLanguageForShortName("en-US").getShortNameWithCountryAndVariant());
    assertEquals("de", Language.getLanguageForShortName("de").getShortNameWithCountryAndVariant());
    try {
      Language.getLanguageForShortName("xy");
      fail();
    } catch (IllegalArgumentException expected) {}
    try {
      Language.getLanguageForShortName("YY-KK");
      fail();
    } catch (IllegalArgumentException expected) {}
  }

  @Test
  public void testIsLanguageSupported() {
    assertTrue(Language.isLanguageSupported("xx"));
    assertTrue(Language.isLanguageSupported("XX"));
    assertTrue(Language.isLanguageSupported("en-US"));
    assertTrue(Language.isLanguageSupported("en-us"));
    assertTrue(Language.isLanguageSupported("EN-US"));
    assertTrue(Language.isLanguageSupported("de"));
    assertTrue(Language.isLanguageSupported("de-DE"));
    assertTrue(Language.isLanguageSupported("de-DE-x-simple-language"));
    assertTrue(Language.isLanguageSupported("de-DE-x-simple-LANGUAGE"));
    assertFalse(Language.isLanguageSupported("yy-ZZ"));
    assertFalse(Language.isLanguageSupported("somthing totally invalid"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void testIsLanguageSupportedInvalidCode() {
    Language.isLanguageSupported("somthing-totally-inv-alid");
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
    assertEquals("en-US", Language.getLanguageForName("English (US)").getShortNameWithCountryAndVariant());
    assertEquals("de", Language.getLanguageForName("German").getShortNameWithCountryAndVariant());
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
        assertNotNull("Language " + language + " needs a default variant", language.getDefaultLanguageVariant());
      }
    }
  }

  @Test
  public void testGetLanguageForLocale() {
    assertEquals("de-DE", Language.getLanguageForLocale(new Locale("de", "DE")).getShortNameWithCountryAndVariant());
    assertEquals("de-AT", Language.getLanguageForLocale(new Locale("de", "AT")).getShortNameWithCountryAndVariant());
    assertEquals("en-US", Language.getLanguageForLocale(new Locale("en", "US")).getShortNameWithCountryAndVariant());
    assertEquals("en-GB", Language.getLanguageForLocale(new Locale("en", "GB")).getShortNameWithCountryAndVariant());
    // fallback to the language's default variant if not specified:
    assertEquals("en-US", Language.getLanguageForLocale(new Locale("en")).getShortNameWithCountryAndVariant());
    assertEquals("de-DE", Language.getLanguageForLocale(new Locale("de")).getShortNameWithCountryAndVariant());
    assertEquals("pl-PL", Language.getLanguageForLocale(new Locale("pl")).getShortNameWithCountryAndVariant());
    // final fallback is everything else fails:
    assertEquals("en-US", Language.getLanguageForLocale(Locale.KOREAN).getShortNameWithCountryAndVariant());
    assertEquals("en-US", Language.getLanguageForLocale(new Locale("zz")).getShortNameWithCountryAndVariant());
  }

  @Test
  public void testEqualsConsiderVariantIfSpecified() {
    // every language equals itself:
    assertTrue(new German().equalsConsiderVariantsIfSpecified(new German()));
    assertTrue(new GermanyGerman().equalsConsiderVariantsIfSpecified(new GermanyGerman()));
    assertTrue(new English().equalsConsiderVariantsIfSpecified(new English()));
    assertTrue(new AmericanEnglish().equalsConsiderVariantsIfSpecified(new AmericanEnglish()));
    // equal if variant is the same, but only if specified:
    assertTrue(new AmericanEnglish().equalsConsiderVariantsIfSpecified(new English()));
    assertTrue(new English().equalsConsiderVariantsIfSpecified(new AmericanEnglish()));

    assertFalse(new AmericanEnglish().equalsConsiderVariantsIfSpecified(new BritishEnglish()));
    assertFalse(new English().equalsConsiderVariantsIfSpecified(new German()));
  }

  @Test
  public void testGetAllMaintainers() {
    assertTrue(Language.getAllMaintainers(TestTools.getMessages("en")).length() > 100);
  }
}
