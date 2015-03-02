/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests a core class as its behavior depends on files in the classpath
 * that don't exist in core.
 */
public class LanguagesTest {

  @Test
  public void testGet() {
    List<Language> languages = Languages.get();
    List<Language> languagesWithDemo = Languages.getWithDemoLanguage();
    assertThat(languages.size() + 1, is(languagesWithDemo.size()));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetIsUnmodifiable() {
    List<Language> languages = Languages.get();
    languages.add(languages.get(0));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetWithDemoLanguageIsUnmodifiable() {
    List<Language> languages = Languages.getWithDemoLanguage();
    languages.add(languages.get(0));
  }

  @Test
  public void testGetLanguageForShortName() {
    assertEquals("en-US", Languages.getLanguageForShortName("en-us").getShortNameWithCountryAndVariant());
    assertEquals("en-US", Languages.getLanguageForShortName("EN-US").getShortNameWithCountryAndVariant());
    assertEquals("en-US", Languages.getLanguageForShortName("en-US").getShortNameWithCountryAndVariant());
    assertEquals("de", Languages.getLanguageForShortName("de").getShortNameWithCountryAndVariant());
    try {
      Languages.getLanguageForShortName("xy");
      fail();
    } catch (IllegalArgumentException expected) {}
    try {
      Languages.getLanguageForShortName("YY-KK");
      fail();
    } catch (IllegalArgumentException expected) {}
  }

  @Test
  public void testIsLanguageSupported() {
    Assert.assertTrue(Languages.isLanguageSupported("xx"));
    Assert.assertTrue(Languages.isLanguageSupported("XX"));
    Assert.assertTrue(Languages.isLanguageSupported("en-US"));
    Assert.assertTrue(Languages.isLanguageSupported("en-us"));
    Assert.assertTrue(Languages.isLanguageSupported("EN-US"));
    Assert.assertTrue(Languages.isLanguageSupported("de"));
    Assert.assertTrue(Languages.isLanguageSupported("de-DE"));
    Assert.assertTrue(Languages.isLanguageSupported("de-DE-x-simple-language"));
    Assert.assertTrue(Languages.isLanguageSupported("de-DE-x-simple-LANGUAGE"));
    assertFalse(Languages.isLanguageSupported("yy-ZZ"));
    assertFalse(Languages.isLanguageSupported("zz"));
    assertFalse(Languages.isLanguageSupported("somthing totally invalid"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void testIsLanguageSupportedInvalidCode() {
    Languages.isLanguageSupported("somthing-totally-inv-alid");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testInvalidShortName1() {
    Languages.getLanguageForShortName("de-");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testInvalidShortName2() {
    Languages.getLanguageForShortName("dexx");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testInvalidShortName3() {
    Languages.getLanguageForShortName("xyz-xx");
  }

  @Test
  public void testGetLanguageForName() {
    assertEquals("en-US", Languages.getLanguageForName("English (US)").getShortNameWithCountryAndVariant());
    assertEquals("de", Languages.getLanguageForName("German").getShortNameWithCountryAndVariant());
    assertEquals(null, Languages.getLanguageForName("Foobar"));
  }

  @Test
  public void testIsVariant() {
    Assert.assertTrue(Languages.getLanguageForShortName("en-US").isVariant());
    Assert.assertTrue(Languages.getLanguageForShortName("de-CH").isVariant());

    assertFalse(Languages.getLanguageForShortName("en").isVariant());
    assertFalse(Languages.getLanguageForShortName("de").isVariant());
  }

  @Test
  public void testHasVariant() {
    Assert.assertTrue(Languages.getLanguageForShortName("en").hasVariant());
    Assert.assertTrue(Languages.getLanguageForShortName("de").hasVariant());

    assertFalse(Languages.getLanguageForShortName("en-US").hasVariant());
    assertFalse(Languages.getLanguageForShortName("de-CH").hasVariant());
    assertFalse(Languages.getLanguageForShortName("ast").hasVariant());
    assertFalse(Languages.getLanguageForShortName("pl").hasVariant());

    for (Language language : Language.LANGUAGES) {
      if (language.hasVariant()) {
        assertNotNull("Language " + language + " needs a default variant", language.getDefaultLanguageVariant());
      }
    }
  }

  @Test
  public void testGetLanguageForLocale() {
    assertEquals("de", Languages.getLanguageForLocale(Locale.GERMAN).getShortName());
    assertEquals("de", Languages.getLanguageForLocale(Locale.GERMANY).getShortName());
    assertEquals("de-DE", Languages.getLanguageForLocale(new Locale("de", "DE")).getShortNameWithCountryAndVariant());
    assertEquals("de-AT", Languages.getLanguageForLocale(new Locale("de", "AT")).getShortNameWithCountryAndVariant());
    assertEquals("en-US", Languages.getLanguageForLocale(new Locale("en", "US")).getShortNameWithCountryAndVariant());
    assertEquals("en-GB", Languages.getLanguageForLocale(new Locale("en", "GB")).getShortNameWithCountryAndVariant());
    // fallback to the language's default variant if not specified:
    assertEquals("en-US", Languages.getLanguageForLocale(new Locale("en")).getShortNameWithCountryAndVariant());
    assertEquals("de-DE", Languages.getLanguageForLocale(new Locale("de")).getShortNameWithCountryAndVariant());
    assertEquals("pl-PL", Languages.getLanguageForLocale(new Locale("pl")).getShortNameWithCountryAndVariant());
    // final fallback is everything else fails:
    assertEquals("en-US", Languages.getLanguageForLocale(Locale.KOREAN).getShortNameWithCountryAndVariant());
    assertEquals("en-US", Languages.getLanguageForLocale(new Locale("zz")).getShortNameWithCountryAndVariant());
  }
}
