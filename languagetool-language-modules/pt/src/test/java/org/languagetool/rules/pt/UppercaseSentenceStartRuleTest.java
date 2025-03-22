/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.pt;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;

public class UppercaseSentenceStartRuleTest {

  @Test
  public void testUppercaseRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("pt"));
    TestTools.disableAllRulesExcept(lt, "UPPERCASE_SENTENCE_START");

    assertEquals(1, lt.check("hora de começar").size());

    assertEquals(0, lt.check("palavra").size());  // not a real sentence
    assertEquals(0, lt.check("Frase").size());

    assertEquals(0, lt.check("Começamos uma frase. E agora vem a outra.").size());
    assertEquals(0, lt.check("Mais outra frase. Âmbar é meu álbum favorito da Bethânia.").size());
    assertEquals(0, lt.check("Agora com abreviaturas, p. ex. esta.").size());
    assertEquals(0, lt.check("Agora com aspas. \"Essas aqui!\".").size());
    assertEquals(0, lt.check("\"Aspas desde o começo!\"").size());
    assertEquals(0, lt.check("— Esta, com travessão (M), é nova!").size());
    assertEquals(0, lt.check("– Esta, com travessão (N), é nova!").size());
    assertEquals(0, lt.check("- Esta, com travessão (H), é nova!").size());
    assertEquals(0, lt.check("Prezado Dr. Stein,\ngostaria de marcar uma nova data.").size());

    assertEquals(1, lt.check("legal!").size());
    assertEquals(1, lt.check("Começamos uma frase. e agora vem a outra.").size());
    assertEquals(1, lt.check("Mais outra frase. âmbar é meu álbum favorito da Bethânia.").size());
    assertEquals(1, lt.check("Agora com aspas. \"essas aqui!\".").size());
    assertEquals(1, lt.check("\"aspas desde o começo!\"").size());
    assertEquals(1, lt.check("— esta, com travessão, é nova!").size());
    assertEquals(1, lt.check("– esta, com travessão, é nova!").size());
    assertEquals(1, lt.check("- esta, com travessão, é nova!").size());
  }

}