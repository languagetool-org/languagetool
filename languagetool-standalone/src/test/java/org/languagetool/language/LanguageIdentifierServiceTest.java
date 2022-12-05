/*
 * LanguageTool, a natural language style checker 
 * Copyright (c) 2022.  Stefan Viol (https://stevio.de)
 *  
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  USA
 */

package org.languagetool.language;

import org.junit.Test;
import org.languagetool.language.identifier.DefaultLanguageIdentifier;
import org.languagetool.language.identifier.LanguageIdentifier;
import org.languagetool.language.identifier.LanguageIdentifierService;
import org.languagetool.language.identifier.SimpleLanguageIdentifier;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotEquals;

public class LanguageIdentifierServiceTest {

  @Test
  public void testFactory() {
    LanguageIdentifier default1 = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("default")
            .getDefaultLanguageIdentifier(0, null, null, null);
    LanguageIdentifier default2 = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("default")
            .getDefaultLanguageIdentifier(1000, null, null, null);
    LanguageIdentifier simple1 = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("simple")
            .getSimpleLanguageIdentifier(null);
    LanguageIdentifier simple2 = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("simple")
            .getSimpleLanguageIdentifier(null);

    assertTrue(default1 instanceof DefaultLanguageIdentifier);
    assertTrue(default2 instanceof DefaultLanguageIdentifier);
    assertNotEquals(default1, default2);
    assertTrue(simple1 instanceof SimpleLanguageIdentifier);
    assertTrue(simple2 instanceof SimpleLanguageIdentifier);
    assertNotEquals(simple1, simple2);
  }

  @Test
  public void testFactoryWithoutReset() {
    LanguageIdentifier default1 = LanguageIdentifierService.INSTANCE
            .getDefaultLanguageIdentifier(0, null, null, null);

    LanguageIdentifier default2 = LanguageIdentifierService.INSTANCE
            .getDefaultLanguageIdentifier(1000, null, null, null);

    LanguageIdentifier simple1 = LanguageIdentifierService.INSTANCE
            .getSimpleLanguageIdentifier(Collections.emptyList());
    LanguageIdentifier simple2 = LanguageIdentifierService.INSTANCE
            .getSimpleLanguageIdentifier(Collections.emptyList());
    assertEquals(default1, default2);
    assertEquals(simple1, simple2);
    assertNotEquals(simple1, default1);
    assertNotEquals(simple1, default2);
    assertNotEquals(simple2, default1);
    assertNotEquals(simple2, default2);
  }

}
