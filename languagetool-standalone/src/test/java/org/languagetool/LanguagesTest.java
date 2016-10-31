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
    assertEquals("en-US", Languages.getLanguageForShortCode("en-us").getShortCodeWithCountryAndVariant());
    assertEquals("en-US", Languages.getLanguageForShortCode("EN-US").getShortCodeWithCountryAndVariant());
    assertEquals("en-US", Languages.getLanguageForShortCode("en-US").getShortCodeWithCountryAndVariant());
    assertEquals("de", Languages.getLanguageForShortCode("de").getShortCodeWithCountryAndVariant());
    try {
      Languages.getLanguageForShortCode("xy");
      fail();
    } catch (IllegalArgumentException ignored) {}
    try {
      Languages.getLanguageForShortCode("YY-KK");
      fail();
    } catch (IllegalArgumentException ignored) {}
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
    Languages.getLanguageForShortCode("de-");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testInvalidShortName2() {
    Languages.getLanguageForShortCode("dexx");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testInvalidShortName3() {
    Languages.getLanguageForShortCode("xyz-xx");
  }

  @Test
  public void testGetLanguageForName() {
    assertEquals("en-US", Languages.getLanguageForName("English (US)").getShortCodeWithCountryAndVariant());
    assertEquals("de", Languages.getLanguageForName("German").getShortCodeWithCountryAndVariant());
    assertEquals(null, Languages.getLanguageForName("Foobar"));
  }

  @Test
  public void testIsVariant() {
    Assert.assertTrue(Languages.getLanguageForShortCode("en-US").isVariant());
    Assert.assertTrue(Languages.getLanguageForShortCode("de-CH").isVariant());

    assertFalse(Languages.getLanguageForShortCode("en").isVariant());
    assertFalse(Languages.getLanguageForShortCode("de").isVariant());
  }

  @Test
  public void testHasVariant() {
    Assert.assertTrue(Languages.getLanguageForShortCode("en").hasVariant());
    Assert.assertTrue(Languages.getLanguageForShortCode("de").hasVariant());

    assertFalse(Languages.getLanguageForShortCode("en-US").hasVariant());
    assertFalse(Languages.getLanguageForShortCode("de-CH").hasVariant());
    assertFalse(Languages.getLanguageForShortCode("ast").hasVariant());
    assertFalse(Languages.getLanguageForShortCode("pl").hasVariant());

    for (Language language : Languages.getWithDemoLanguage()) {
      if (language.hasVariant()) {
        assertNotNull("Language " + language + " needs a default variant", language.getDefaultLanguageVariant());
      }
    }
  }
  
  @Test
  public void isHiddenFromGui() {
    Assert.assertTrue(Languages.getLanguageForShortCode("en").isHiddenFromGui());
    Assert.assertTrue(Languages.getLanguageForShortCode("de").isHiddenFromGui());
    Assert.assertTrue(Languages.getLanguageForShortCode("pt").isHiddenFromGui());

    assertFalse(Languages.getLanguageForShortCode("en-US").isHiddenFromGui());
    assertFalse(Languages.getLanguageForShortCode("de-CH").isHiddenFromGui());
    assertFalse(Languages.getLanguageForShortCode("ast").isHiddenFromGui());
    assertFalse(Languages.getLanguageForShortCode("pl").isHiddenFromGui());
    assertFalse(Languages.getLanguageForShortCode("ca-ES").isHiddenFromGui());
    assertFalse(Languages.getLanguageForShortCode("ca-ES-valencia").isHiddenFromGui());
    assertFalse(Languages.getLanguageForShortCode("de-DE-x-simple-language").isHiddenFromGui());
    assertFalse(Languages.getLanguageForShortCode("de-DE").isHiddenFromGui());

  }

  @Test
  public void testGetLanguageForLocale() {
    assertEquals("de", Languages.getLanguageForLocale(Locale.GERMAN).getShortCode());
    assertEquals("de", Languages.getLanguageForLocale(Locale.GERMANY).getShortCode());
    assertEquals("de-DE", Languages.getLanguageForLocale(new Locale("de", "DE")).getShortCodeWithCountryAndVariant());
    assertEquals("de-AT", Languages.getLanguageForLocale(new Locale("de", "AT")).getShortCodeWithCountryAndVariant());
    assertEquals("en-US", Languages.getLanguageForLocale(new Locale("en", "US")).getShortCodeWithCountryAndVariant());
    assertEquals("en-GB", Languages.getLanguageForLocale(new Locale("en", "GB")).getShortCodeWithCountryAndVariant());
    // fallback to the language's default variant if not specified:
    assertEquals("en-US", Languages.getLanguageForLocale(new Locale("en")).getShortCodeWithCountryAndVariant());
    assertEquals("de-DE", Languages.getLanguageForLocale(new Locale("de")).getShortCodeWithCountryAndVariant());
    assertEquals("pl-PL", Languages.getLanguageForLocale(new Locale("pl")).getShortCodeWithCountryAndVariant());
    // final fallback is everything else fails:
    assertEquals("en-US", Languages.getLanguageForLocale(Locale.KOREAN).getShortCodeWithCountryAndVariant());
    assertEquals("en-US", Languages.getLanguageForLocale(new Locale("zz")).getShortCodeWithCountryAndVariant());
  }
}
