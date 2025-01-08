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

import org.junit.Test;
import org.languagetool.language.*;

import static org.junit.Assert.*;

public class LanguageTest {

  @Test
  public void testRuleFileName() {
    if (Premium.isPremiumVersion()) {
      assertTrue(BritishEnglish.getInstance().getRuleFileNames().contains("/org/languagetool/rules/en/grammar.xml"));
      assertTrue(BritishEnglish.getInstance().getRuleFileNames().contains("/org/languagetool/rules/en/en-GB/grammar.xml"));
      assertTrue(AmericanEnglish.getInstance().getRuleFileNames().contains("/org/languagetool/rules/en/grammar.xml"));
      assertTrue(AmericanEnglish.getInstance().getRuleFileNames().contains("/org/languagetool/rules/en/en-US/grammar.xml"));
      assertTrue(BritishEnglish.getInstance().getRuleFileNames().contains("/src/main/resources/org/languagetool/rules/en/grammar-premium.xml"));
      assertTrue(AmericanEnglish.getInstance().getRuleFileNames().contains("/src/main/resources/org/languagetool/rules/en/grammar-premium.xml"));
    } else {
      assertEquals("[/org/languagetool/rules/en/grammar.xml, /org/languagetool/rules/en/style.xml, /org/languagetool/rules/en/en-GB/grammar.xml, /org/languagetool/rules/en/en-GB/style.xml]", BritishEnglish.getInstance().getRuleFileNames().toString());
      assertEquals("[/org/languagetool/rules/en/grammar.xml, /org/languagetool/rules/en/style.xml, /org/languagetool/rules/en/en-US/grammar.xml, /org/languagetool/rules/en/en-US/style.xml]", AmericanEnglish.getInstance().getRuleFileNames().toString());
      assertEquals("[/org/languagetool/rules/en/grammar.xml, /org/languagetool/rules/en/style.xml]", new English().getRuleFileNames().toString());
      assertEquals("[/org/languagetool/rules/de/grammar.xml, /org/languagetool/rules/de/style.xml]", new German().getRuleFileNames().toString());
    }
  }

  @Test
  public void testGetTranslatedName() {
    assertEquals("English", new English().getTranslatedName(TestTools.getMessages("en")));
    assertEquals("English (British)", BritishEnglish.getInstance().getTranslatedName(TestTools.getMessages("en")));

    assertEquals("Englisch", new English().getTranslatedName(TestTools.getMessages("de")));
    assertEquals("Englisch (Großbritannien)", BritishEnglish.getInstance().getTranslatedName(TestTools.getMessages("de")));
    assertEquals("Deutsch", new German().getTranslatedName(TestTools.getMessages("de")));
    assertEquals("Deutsch (Schweiz)", new SwissGerman().getTranslatedName(TestTools.getMessages("de")));
  }

  @Test
  public void testGetShortNameWithVariant() {
    assertEquals("en-US", AmericanEnglish.getInstance().getShortCodeWithCountryAndVariant());
    assertEquals("de", new German().getShortCodeWithCountryAndVariant());
  }

  @Test
  public void testEquals() {
    assertEquals(GermanyGerman.getInstance(), GermanyGerman.getInstance());
    assertNotEquals(new AustrianGerman(), GermanyGerman.getInstance());
    assertNotEquals(new AustrianGerman(), new German());
  }

  @Test
  public void testEqualsConsiderVariantIfSpecified() {
    // every language equals itself:
    assertTrue(new German().equalsConsiderVariantsIfSpecified(new German()));
    assertTrue(GermanyGerman.getInstance().equalsConsiderVariantsIfSpecified(GermanyGerman.getInstance()));
    assertTrue(new English().equalsConsiderVariantsIfSpecified(new English()));
    assertTrue(AmericanEnglish.getInstance().equalsConsiderVariantsIfSpecified(AmericanEnglish.getInstance()));
    // equal if variant is the same, but only if specified:
    assertTrue(AmericanEnglish.getInstance().equalsConsiderVariantsIfSpecified(new English()));
    assertTrue(new English().equalsConsiderVariantsIfSpecified(AmericanEnglish.getInstance()));

    assertFalse(AmericanEnglish.getInstance().equalsConsiderVariantsIfSpecified(BritishEnglish.getInstance()));
    assertFalse(new English().equalsConsiderVariantsIfSpecified(new German()));
  }

  @Test
  public void testCreateDefaultJLanguageTool() {
    Language german = new German();
    Language germanyGerman = GermanyGerman.getInstance();
    JLanguageTool ltGerman = german.createDefaultJLanguageTool();
    JLanguageTool ltGerman2 = german.createDefaultJLanguageTool();
    JLanguageTool ltGermanyGerman = germanyGerman.createDefaultJLanguageTool();
    JLanguageTool ltEnglish = new English().createDefaultJLanguageTool();
    assertNotSame(ltGermanyGerman, ltGerman);
    assertSame(ltGerman2, ltGerman);
    assertEquals(ltGerman.getLanguage(), german);
    assertEquals(ltGermanyGerman.getLanguage(), germanyGerman);
    assertEquals(ltEnglish.getLanguage(), new English());
  }

}
