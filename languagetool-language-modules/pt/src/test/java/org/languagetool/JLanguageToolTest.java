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
package org.languagetool;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

public class JLanguageToolTest {

  @Test
  public void testPortugueseVariants() throws IOException {
    String sentence = "Isto é uma característica sua.";
    String sentence2 = "Isto é uma características sua.";
    for (String langCode : new String[] { "pt-PT", "pt-BR", "pt-AO", "pt-MZ" }) {
      JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode(langCode));
      assertEquals(0, lt.check(sentence).size());
      assertEquals(1, lt.check(sentence2).size());

    }
  }

  @Test
  public void testSomeSentences() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("pt-BR"));
    lt.check("™ ® Marcas registradas da Corteva Agriscience e de suas companhias afiliadas.");
    lt.check("Vatatzes teve que lutar contra a reivindicação de Isaac e Aleixo, os irmãos de Teodoro I, que fugiram para o Império Latino e procuraram ajuda.\n" +
      "Outros artistas que participaram do Vevo Lift são One Direction, 5 Seconds of Summer, Fifth Harmony, Sam Smith, Iggy Azalea, Rita Ora, Avicii, entre outros.\n" +
      "Astarte (desambiguação)\n" +
      "Drocourt (pas-de-calais)\n" +
      "O principal trabalho do G.E.R.E.C foi desenvolver, a partir de 1976, a escrita do crioulo, incluindo uma família de padrões de ortografia.\n" +
      "Zeth foi o mais novo de dez filhos.\n" +
      "Segundo Ajahn Mudito, a proposta do site é dar às pessoas a oportunidade de ter esses ensinamentos traduzidos diretamente do tailandês para o português, sem ter que passar pela costumeira “retradução”, onde primeiro os ensinamentos são traduzidos para o inglês para, só depois, chegar ao nosso idioma, fato que, segundo ele, gera muitas distorções e perda de significado do ensinamento original.\n" +
      "No entanto, em julho de 2016 foi anunciado como vice-prefeito na candidatura de Hélio Godoy.\n" +
      "As dissecções que envolvem a aorta descendente são geralmente tratadas com medicação para baixar a pressão arterial e o ritmo cardíaco, a não ser que ocorram complicações.\n" +
      "Margarethe era filha de Karl Krambeck e Catharina Neve, que emigraram de Sehestedt para o Brasil em 10.03.1852, com o Vapor Princess Louize e Capitão Bähr, dirigindo-se ao Distrito de Valença/RJ, onde trabalharam na Fazenda Independência, A família Krambeck originou-se no Estado de Schleswig-Holstein, no Norte da Alemanha, e os registros existentes têm início com Claus Krambeck, por volta de 1700, em Sehestedt.");
  }

}
