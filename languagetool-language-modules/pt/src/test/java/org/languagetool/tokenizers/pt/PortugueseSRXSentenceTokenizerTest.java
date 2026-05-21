/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tokenizers.pt;

import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Portuguese;
import org.languagetool.tokenizers.SRXSentenceTokenizer;

public class PortugueseSRXSentenceTokenizerTest {

  private final SRXSentenceTokenizer tokenizer = new SRXSentenceTokenizer(Portuguese.getInstance());

  @Test
  public void testTokenize() {
    // NOTE: sentences here need to end with a space character so they
    // have correct whitespace when appended:
    testSplit("Cola o teu próprio texto aqui.");
    testSplit("Cola o teu próprio texto aqui. ", "Ou verifica este texto.");

    // Missing white space between sentences: do not split
    testSplit("Esta é a primeira frase.Esta é a segunda.");

    // Basic sentence splitting
    testSplit("O Brasil é um país muito grande. ", "Tem muitos estados e cidades.");
    testSplit("Hoje está fazendo muito calor. ", "Vamos tomar sorvete. ", "Que boa ideia!");
    testSplit("Você gosta de futebol? ", "Eu adoro!");

    // Abbreviations that should NOT split
    testSplit("O Sr. João foi ao mercado.");
    testSplit("A Sra. Silva mora na Rua das Flores.");
    testSplit("O Dr. Carlos atendeu o paciente ontem.");
    testSplit("A Dra. Ana é especialista em pediatria.");
    testSplit("O Prof. Souza deu uma aula excelente.");
    testSplit("Comprei frutas, legumes, etc. no supermercado.");
    testSplit("São precisos documentos, certidões, etc. para o processo.");
    testSplit("Havia problemas de logística, infraestrutura, etc. ", "Tudo precisava ser resolvido.");
    testSplit("Comprei maçãs, peras, laranjas, etc. ", "Depois fui para casa.");
    testSplit("O endereço é Av. Paulista, 1000.");
    testSplit("Moro na R. das Flores, n.º 25.");
    testSplit("Consulte o cap. 3 para mais informações.");
    testSplit("Veja a fig. 2 abaixo.");
    testSplit("O evento ocorreu em jan. de 2023.");

    // Abbreviations followed by sentence boundary
    testSplit("O contrato foi assinado ontem. ", "Depois foi registrado em cartório.");
    testSplit("O Prof. Silva chegou tarde. ", "A aula começou com atraso.");
    testSplit("Consulte o Dr. Almeida. ", "Ele poderá ajudá-lo.");

    // Question marks and exclamation marks
    testSplit("Você viu o filme? ", "Eu achei incrível!");
    testSplit("Como você está? ", "Estou bem, obrigado.");
    testSplit("Que dia bonito! ", "Vamos passear no parque.");
    testSplit("Será que vai chover? ", "Melhor levar guarda-chuva.");

    // Ellipsis
    testSplit("Não sei o que dizer... ", "É uma situação muito difícil.");
    testSplit("Ele hesitou por um momento... e então decidiu partir.");
    testSplit("Ele viria ... ?");
    testSplit("Ele viria, ... ?");

    // Ordinal numbers with dot
    testSplit("O 1.º lugar foi do Brasil.");
    testSplit("A 2.ª colocada foi a Argentina. ", "O 3.º lugar ficou com o Uruguai.");

    // Numbers with dots (should not split)
    testSplit("O evento começa às 10.30 e termina às 12.00.");
    testSplit("O texto tem 3.500 palavras ao todo.");

    // Initials and proper names
    testSplit("J. K. Rowling é a autora de Harry Potter.");
    testSplit("O presidente L. I. Lula assinou o decreto. ", "Será implementado em breve.");

    // Quotes with sentence boundaries
    testSplit("\"Vou embora!\", avisou ela. ", "Todos ficaram tristes.");
    testSplit("\"Não aguento mais!\", gritou ela. ", "Todos olharam.");

    // URLs (should not split)
    testSplit("Acesse o site em http://www.exemplo.com.br para mais informações.");

    // Mixed punctuation
    testSplit("O Brasil ganhou! ", "Que festa incrível! ", "Todos comemoraram.");
  }

  private void testSplit(String... sentences) {
    TestTools.testSplit(sentences, tokenizer);
  }

}
