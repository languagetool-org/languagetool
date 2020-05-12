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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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

  @Test
  public void testGetIsUnmodifiable() {
    List<Language> languages = Languages.get();
    assertThrows(UnsupportedOperationException.class, () ->
      languages.add(languages.get(0)));
  }

  @Test
  public void testGetWithDemoLanguageIsUnmodifiable() {
    List<Language> languages = Languages.getWithDemoLanguage();
    assertThrows(UnsupportedOperationException.class, () ->
      languages.add(languages.get(0)));
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
    assertTrue(Languages.isLanguageSupported("xx"));
    assertTrue(Languages.isLanguageSupported("XX"));
    assertTrue(Languages.isLanguageSupported("en-US"));
    assertTrue(Languages.isLanguageSupported("en-us"));
    assertTrue(Languages.isLanguageSupported("EN-US"));
    assertTrue(Languages.isLanguageSupported("de"));
    assertTrue(Languages.isLanguageSupported("de-DE"));
    assertTrue(Languages.isLanguageSupported("de-DE-x-simple-language"));
    assertTrue(Languages.isLanguageSupported("de-DE-x-simple-LANGUAGE"));
    assertFalse(Languages.isLanguageSupported("yy-ZZ"));
    assertFalse(Languages.isLanguageSupported("zz"));
    assertFalse(Languages.isLanguageSupported("somthing totally invalid"));
  }

  @Test
  public void testIsLanguageSupportedInvalidCode() {
    assertThrows(IllegalArgumentException.class, () ->
      Languages.isLanguageSupported("somthing-totally-inv-alid"));
  }

  @Test
  public void testInvalidShortName1() {
    assertThrows(IllegalArgumentException.class, () ->
      Languages.getLanguageForShortCode("de-"));
  }

  @Test
  public void testInvalidShortName2() {
    assertThrows(IllegalArgumentException.class, () ->
      Languages.getLanguageForShortCode("dexx"));
  }

  @Test
  public void testInvalidShortName3() {
    assertThrows(IllegalArgumentException.class, () ->
      Languages.getLanguageForShortCode("xyz-xx"));
  }

  @Test
  public void testGetLanguageForName() {
    assertEquals("en-US", Languages.getLanguageForName("English (US)").getShortCodeWithCountryAndVariant());
    assertEquals("de", Languages.getLanguageForName("German").getShortCodeWithCountryAndVariant());
    assertEquals(null, Languages.getLanguageForName("Foobar"));
  }

  @Test
  public void testIsVariant() {
    assertTrue(Languages.getLanguageForShortCode("en-US").isVariant());
    assertTrue(Languages.getLanguageForShortCode("de-CH").isVariant());

    assertFalse(Languages.getLanguageForShortCode("en").isVariant());
    assertFalse(Languages.getLanguageForShortCode("de").isVariant());
  }

  @Test
  public void testHasVariant() {
    assertTrue(Languages.getLanguageForShortCode("en").hasVariant());
    assertTrue(Languages.getLanguageForShortCode("de").hasVariant());

    assertFalse(Languages.getLanguageForShortCode("en-US").hasVariant());
    assertFalse(Languages.getLanguageForShortCode("de-CH").hasVariant());
    assertFalse(Languages.getLanguageForShortCode("ast").hasVariant());
    assertFalse(Languages.getLanguageForShortCode("pl").hasVariant());

    for (Language language : Languages.getWithDemoLanguage()) {
      if (language.hasVariant()) {
        assertNotNull(language.getDefaultLanguageVariant(), "Language " + language + " needs a default variant");
      }
    }
  }
  
  @Test
  public void isHiddenFromGui() {
    assertTrue(Languages.getLanguageForShortCode("en").isHiddenFromGui());
    assertTrue(Languages.getLanguageForShortCode("de").isHiddenFromGui());
    assertTrue(Languages.getLanguageForShortCode("pt").isHiddenFromGui());

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
