/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.tokenizers.SentenceTokenizer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FrenchTest {
  
  @Test
  public void testSentenceTokenizer() {
    Language lang = new French();
    SentenceTokenizer tokenizer = lang.getSentenceTokenizer();
    assertThat(tokenizer.tokenize("Arrête de le cajoler... ça ne donnera rien.").size(), is(1));
    assertThat(tokenizer.tokenize("Arrête de le cajoler… ça ne donnera rien.").size(), is(1));
    assertThat(tokenizer.tokenize("Il est possible de le contacter par tous les moyens (courrier, téléphone, mail...) à condition de vous présenter.").size(), is(1));
  }
  
  @Test
  public void testAdvancedTypography() {
    Language lang = new French();
    assertEquals(lang.toAdvancedTypography("\"C'est\""), "«\u00a0C’est\u00a0»");
    assertEquals(lang.toAdvancedTypography("\"C'est\" "), "«\u00a0C’est\u00a0» ");
    assertEquals(lang.toAdvancedTypography("'C'est'"), "‘C’est’");
    assertEquals(lang.toAdvancedTypography("Vouliez-vous dire 'C'est'?"), "Vouliez-vous dire ‘C’est’\u202f?");
    assertEquals(lang.toAdvancedTypography("Vouliez-vous dire \"C'est\"?"), "Vouliez-vous dire «\u00a0C’est\u00a0»\u202f?");
    assertEquals(lang.toAdvancedTypography("Confusion possible : \"a\" est une conjugaison du verbe avoir. Vouliez-vous dire « à »?"), 
        "Confusion possible\u00a0: «\u00a0a\u00a0» est une conjugaison du verbe avoir. Vouliez-vous dire «\u00a0à\u00a0»\u202f?");
    assertEquals(lang.toAdvancedTypography("C'est l'\"homme\"."), "C’est l’« homme ».");
  }

}
