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

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;

/**
 * Tests a core class as its behavior depends on files in the classpath
 * that don't exist in core.
 */
public class LanguagesTest {

  @Test
  public void testGet() {
    List<Language> languages = Languages.get();
    List<Language> languagesWithDemo = Languages.getWithDemoLanguage();
    MatcherAssert.assertThat(languages.size() + 1, is(languagesWithDemo.size()));
  }

  @Test
  public void testGetIsUnmodifiable() {
    UnsupportedOperationException thrown = Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      List<Language> languages = Languages.get();
      languages.add(languages.get(0));
    });
  }

  @Test
  public void testGetWithDemoLanguageIsUnmodifiable() {
    UnsupportedOperationException thrown = Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      List<Language> languages = Languages.getWithDemoLanguage();
      languages.add(languages.get(0));
    });
  }

  @Test
  public void testGetLanguageForShortName() {
    Assertions.assertEquals("en-US", Languages.getLanguageForShortCode("en-us").getShortCodeWithCountryAndVariant());
    Assertions.assertEquals("en-US", Languages.getLanguageForShortCode("EN-US").getShortCodeWithCountryAndVariant());
    Assertions.assertEquals("en-US", Languages.getLanguageForShortCode("en-US").getShortCodeWithCountryAndVariant());
    Assertions.assertEquals("de", Languages.getLanguageForShortCode("de").getShortCodeWithCountryAndVariant());
    try {
      Languages.getLanguageForShortCode("xy");
      Assertions.fail();
    } catch (IllegalArgumentException ignored) {}
    try {
      Languages.getLanguageForShortCode("YY-KK");
      Assertions.fail();
    } catch (IllegalArgumentException ignored) {}
  }

  @Test
  public void testIsLanguageSupported() {
    Assertions.assertTrue(Languages.isLanguageSupported("xx"));
    Assertions.assertTrue(Languages.isLanguageSupported("XX"));
    Assertions.assertTrue(Languages.isLanguageSupported("en-US"));
    Assertions.assertTrue(Languages.isLanguageSupported("en-us"));
    Assertions.assertTrue(Languages.isLanguageSupported("EN-US"));
    Assertions.assertTrue(Languages.isLanguageSupported("de"));
    Assertions.assertTrue(Languages.isLanguageSupported("de-DE"));
    Assertions.assertTrue(Languages.isLanguageSupported("de-DE-x-simple-language"));
    Assertions.assertTrue(Languages.isLanguageSupported("de-DE-x-simple-LANGUAGE"));
    Assertions.assertFalse(Languages.isLanguageSupported("yy-ZZ"));
    Assertions.assertFalse(Languages.isLanguageSupported("zz"));
    Assertions.assertFalse(Languages.isLanguageSupported("somthing totally invalid"));
  }

  @Test
  public void testIsLanguageSupportedInvalidCode() {
    IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
       Languages.isLanguageSupported("somthing-totally-inv-alid");
    });
  }

  @Test
  public void testInvalidShortName1() {
    IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
      Languages.getLanguageForShortCode("de-");
    });
  }

  @Test
  public void testInvalidShortName2() {
    IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
      Languages.getLanguageForShortCode("dexx");
    });
  }

  @Test
  public void testInvalidShortName3() {
    IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
      Languages.getLanguageForShortCode("xyz-xx");
    });
  }

  @Test
  public void testGetLanguageForName() {
    Assertions.assertEquals("en-US", Languages.getLanguageForName("English (US)").getShortCodeWithCountryAndVariant());
    Assertions.assertEquals("de", Languages.getLanguageForName("German").getShortCodeWithCountryAndVariant());
    Assertions.assertNull(Languages.getLanguageForName("Foobar"));
  }

  @Test
  public void testIsVariant() {
    Assertions.assertTrue(Languages.getLanguageForShortCode("en-US").isVariant());
    Assertions.assertTrue(Languages.getLanguageForShortCode("de-CH").isVariant());

    Assertions.assertFalse(Languages.getLanguageForShortCode("en").isVariant());
    Assertions.assertFalse(Languages.getLanguageForShortCode("de").isVariant());
  }

  @Test
  public void testHasVariant() {
    Assertions.assertTrue(Languages.getLanguageForShortCode("en").hasVariant());
    Assertions.assertTrue(Languages.getLanguageForShortCode("de").hasVariant());

    Assertions.assertFalse(Languages.getLanguageForShortCode("en-US").hasVariant());
    Assertions.assertFalse(Languages.getLanguageForShortCode("de-CH").hasVariant());
    Assertions.assertFalse(Languages.getLanguageForShortCode("ast").hasVariant());
    Assertions.assertFalse(Languages.getLanguageForShortCode("pl").hasVariant());

    for (Language language : Languages.getWithDemoLanguage()) {
      if (language.hasVariant()) {
        Assertions.assertNotNull(language.getDefaultLanguageVariant(), "Language " + language + " needs a default variant");
      }
    }
  }
  
  @Test
  public void isHiddenFromGui() {
    Assertions.assertTrue(Languages.getLanguageForShortCode("en").isHiddenFromGui());
    Assertions.assertTrue(Languages.getLanguageForShortCode("de").isHiddenFromGui());
    Assertions.assertTrue(Languages.getLanguageForShortCode("pt").isHiddenFromGui());

    Assertions.assertFalse(Languages.getLanguageForShortCode("en-US").isHiddenFromGui());
    Assertions.assertFalse(Languages.getLanguageForShortCode("de-CH").isHiddenFromGui());
    Assertions.assertFalse(Languages.getLanguageForShortCode("ast").isHiddenFromGui());
    Assertions.assertFalse(Languages.getLanguageForShortCode("pl").isHiddenFromGui());
    Assertions.assertFalse(Languages.getLanguageForShortCode("ca-ES").isHiddenFromGui());
    Assertions.assertFalse(Languages.getLanguageForShortCode("ca-ES-valencia").isHiddenFromGui());
    Assertions.assertFalse(Languages.getLanguageForShortCode("de-DE-x-simple-language").isHiddenFromGui());
    Assertions.assertFalse(Languages.getLanguageForShortCode("de-DE").isHiddenFromGui());

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
