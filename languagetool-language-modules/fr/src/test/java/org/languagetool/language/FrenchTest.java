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

import org.junit.jupiter.api.Test;
import org.languagetool.tokenizers.SentenceTokenizer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FrenchTest {
  
  @Test
  public void testSentenceTokenizer() {
    SentenceTokenizer tokenizer = new French().getSentenceTokenizer();
    assertThat(tokenizer.tokenize("Arrête de le cajoler... ça ne donnera rien.").size(), is(1));
    assertThat(tokenizer.tokenize("Arrête de le cajoler… ça ne donnera rien.").size(), is(1));
    assertThat(tokenizer.tokenize("Il est possible de le contacter par tous les moyens (courrier, téléphone, mail...) à condition de vous présenter.").size(), is(1));
  }

}
