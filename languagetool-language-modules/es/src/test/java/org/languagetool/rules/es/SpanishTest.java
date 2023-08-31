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
package org.languagetool.rules.es;

import org.junit.Test;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.Spanish;

import java.io.IOException;
import java.util.Arrays;

public class SpanishTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    // NOTE: this text needs to be kept in sync with config.ts -> DEMO_TEXTS:
    String s = "Escribe o pega tu texto aqui para tenerlo revisado contínuamente. los errores se subrayaran en diferentes colores: marcaremos los errores ortograficos en rojo; los errores de gramática son resaltado en amarillo; los problemas relacionados al estilo serán marcados en azul. Sabías que te proponemos sinónimos al hacer doble clic sobre una palabra? LanguageTool es un herramienta para textos impecables, sean e-mails, artículos, blogs o otros, incluso cuando el texto se complejice.";
    Spanish lang = new Spanish();
    testDemoText(lang, s,
      Arrays.asList("ES_SIMPLE_REPLACE_SIMPLE_AQUI", "MORFOLOGIK_RULE_ES", "UPPERCASE_SENTENCE_START", "SUBJUNTIVO_FUTURO", "MORFOLOGIK_RULE_ES", "AGREEMENT_VERB_PARTICIPLE", "RELACIONADO_A", "ES_QUESTION_MARK", "AGREEMENT_DET_NOUN", "Y_E_O_U", "COMPLEJIZAR")
    );
    // , "ES_WIKIPEDIA_COMMON_ERRORS"
    runTests(lang, null, "ÍÚÑ");
  }
}
