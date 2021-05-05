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
package org.languagetool.rules.pt;

import org.junit.Test;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.PortugalPortuguese;

import java.io.IOException;
import java.util.Arrays;

public class PortugueseTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    // NOTE: this text needs to be kept in sync with config.ts -> DEMO_TEXTS:
    String s = "Cole aqui seu texto...ou verifique esta texto, afim de revelar alguns dos dos problemas que o LanguageTool consegue detectar. Isto tal vez permita corrigir os seus erro. Nós prometo ajudá-lo. para testar a grafia e as regrs do antigo) Acordo Ortográfico,, verifique o mesmo texto mesmo texto em Português de Angola ou Português do Moçambique e faça a analise dos resultados.. Nossa equipe anuncia a versão 4.5, que será lançada sexta-feira, 26 de março de 2019.";
    PortugalPortuguese lang = new PortugalPortuguese();
    testDemoText(lang, s,
      Arrays.asList("POSSESSIVE_WITHOUT_ARTICLE", "SPACE_AFTER_PUNCTUATION", "GENERAL_GENDER_AGREEMENT_ERRORS", "AFIM_DE", "PORTUGUESE_WORD_REPEAT_RULE",
              "PT_AGREEMENT_REPLACE", "TAL_VEZ", "GENERAL_NUMBER_AGREEMENT_ERRORS", "GENERAL_VERB_AGREEMENT_ERRORS", "UPPERCASE_SENTENCE_START", 
              "HUNSPELL_RULE", "UNPAIRED_BRACKETS", "DOUBLE_PUNCTUATION", "PHRASE_REPETITION", "GENTILICOS_LINGUAS", "GENTILICOS_LINGUAS", "ARTICLES_PRECEDING_LOCATIONS", //"REPEATED_WORDS",
              "PARONYM_ANALISE_363", "DOUBLE_PUNCTUATION", "POSSESSIVE_WITHOUT_ARTICLE", "EQUIPES", "DATE_WEEKDAY")
    );
    runTests(lang, null, "õș");
  }
}
