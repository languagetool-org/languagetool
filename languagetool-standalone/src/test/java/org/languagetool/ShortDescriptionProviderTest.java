/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (www.danielnaber.de)
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ShortDescriptionProviderTest {
  
  @Test
  public void testGetShortDescription() {
    Language de = Languages.getLanguageForShortCode("de-DE");
    ShortDescriptionProvider providerDE = new ShortDescriptionProvider();
    assertNotNull(providerDE.getShortDescription("fielen", de));
    assertNull(providerDE.getShortDescription("fake-word-doesnt-exist", de));

    Language en = Languages.getLanguageForShortCode("en-US");
    ShortDescriptionProvider providerEN = new ShortDescriptionProvider();
    assertNotNull(providerEN.getShortDescription("adopting", en));
    assertNull(providerEN.getShortDescription("fake-word-doesnt-exist", en));
  }

  @Test
  public void testDescriptionLength() {
    int limit = 45;
    int count = 0;
    List<Language> langs = Languages.get();
    for (Language lang : langs) {
      if (lang.getShortCode().equals("en") && !lang.getShortCodeWithCountryAndVariant().equals("en-US")) {
        continue;  // avoid running several times for same data
      }
      if (lang.getShortCode().equals("de") && !lang.getShortCodeWithCountryAndVariant().equals("de-DE")) {
        continue;
      }
      ShortDescriptionProvider provider = new ShortDescriptionProvider();
      Map<String, String> map = provider.getAllDescriptions(lang);
      for (Map.Entry<String, String> entry : map.entrySet()) {
        String desc = entry.getValue();
        int len = desc.length();
        if (len > limit) {
          System.err.println("WARNING: [" + lang.getShortCode() + "] description for " + entry.getKey() +
            " is " + len + " chars, should be <= " + limit + ": " + desc);
        }
        count++;
      }
    }
    if (count == 0) {
      fail("No word descriptions found at all for " + langs.size() + " languages");
    }
  }

}
