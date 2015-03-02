/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.chunking;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.German;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.fail;

public class GermanChunkerTest {

  private final JLanguageTool lt = new JLanguageTool(new German());

  // B = begin, will be expanded to B-NP, I = inner, will be expanded to I-NP
  @Test
  public void testOpenNLPLikeChunking() throws Exception {
    //GermanChunker.setDebug(true);
    assertChunks("Ein/B Haus/I");
    assertChunks("Da steht ein/B Haus/I");
    assertChunks("Da steht ein/B schönes/I Haus/I");
    assertChunks("Da steht ein/B schönes/I großes/I Haus/I");
    assertChunks("Da steht ein/B sehr/I großes/I Haus/I");
    assertChunks("Da steht ein/B sehr/I schönes/I großes/I Haus/I");
    assertChunks("Da steht ein/B sehr/I großes/I Haus/I mit Dach/B");
    assertChunks("Da steht ein/B sehr/I großes/I Haus/I mit einem/B blauen/I Dach/I");
    assertChunks("Eine/B leckere/I Lasagne/I");
    assertChunks("Herr/B Meier/I isst eine/B leckere/I Lasagne/I");
    assertChunks("Herr/B Schrödinger/I isst einen/B Kuchen/I");
    assertChunks("Herr/B Schrödinger/I isst einen/B leckeren/I Kuchen/I");
    assertChunks("Herr/B Karl/I Meier/I isst eine/B leckere/I Lasagne/I");
    assertChunks("Herr/B Finn/I Westerwalbesloh/I isst eine/B leckere/I Lasagne/I");
    assertChunks("Unsere/B schöne/I Heimat/I geht den/B Bach/I runter");
    assertChunks("Er meint das/B Haus/I am grünen/B Hang/I");
    assertChunks("Ich muss dem/B Hund/I Futter/I geben");  // TODO: see next line for how it should be (but: 'Pariser Innenstadt' should be one NP)
    //assertChunks("Ich muss dem/B Hund/I Futter/B geben");
    assertChunks("Das/B Wasser/I , das die/B Wärme/I überträgt");
    assertChunks("Er mag das/B Wasser/I , das/B Meer/I und die/B Luft/I");
    assertChunks("Schon mehr als zwanzig/B Prozent/I der/B Arbeiter/I sind im Streik/B");
    assertChunks("Das/B neue/I Gesetz/I betrifft 1000 Bürger/B"); // '1000' sollte evtl. mit in die NP...
    assertChunks("In zwei/B Wochen/I ist Weihnachten/B");
    assertChunks("Eines ihrer/B drei/I Autos/I ist blau");
  }

  @Test
  public void testTemp() throws Exception {
    assertChunks("Ein/B Haus/I");
    //TODO:
    //assertChunks("Eines ihrer/B drei/I Autos/I ist blau");
    //assertChunks("Das/B Wasser/I , das Wärme/B überträgt");  // keine Kongruenz bzgl. Genus -> keine NP
    //assertChunks("Das/B Wasser/I , das viel/B Wärme/I überträgt");  // keine Kongruenz bzgl. Genus -> keine NP
    //assertChunks("Das/B Wasser/I , das wohlige/B Wärme/I überträgt");  // keine Kongruenz bzgl. Genus -> keine NP
    // das bereits erreichte Ergebnis
    // die privat Krankenversicherten
    // die auf ihre Tochter stolze Frau
    // jemand Schönes
    // fast eine Millionen Studenten
    // nahezu alle Studenten
    // rund 40 Gäste
    // über 20 Besucher
    // der fleißige Dr. Christoph Schmidt
    // der Hund von Peter
    // Peters Hund
    // ein Mann mit einem Radio in der Nase
    // die Hoffnung auf ein baldiges Ende
  }

  private void assertChunks(String input) throws Exception {
    String plainInput = input.replaceAll("/[A-Z-]*", "").replace(" ,", ",");
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(plainInput);
    AnalyzedTokenReadings[] result = analyzedSentence.getTokensWithoutWhitespace();
    GermanChunker chunker = new GermanChunker();
    List<ChunkTaggedToken> basicChunks = chunker.getBasicChunks(Arrays.asList(result));
    List<String> expectedChunks = getExpectedChunks(input);
    assertChunks(input, plainInput, basicChunks, expectedChunks);
  }

  private List<String> getExpectedChunks(String input) {
    List<String> expectedChunks = new ArrayList<>();
    String[] parts = input.split(" ");
    for (String part : parts) {
      String[] tokenParts = part.split("/");
      if (tokenParts.length == 2) {
        String chunk = tokenParts[1];
        if (chunk.equals("B")) {
          expectedChunks.add("B-NP");
        } else if (chunk.equals("I")) {
          expectedChunks.add("I-NP");
        } else {
          throw new RuntimeException("Unknown chunk type: '" + chunk + "'");
        }
      } else {
        expectedChunks.add("O");
      }
    }
    return expectedChunks;
  }

  private void assertChunks(String input, String plainInput, List<ChunkTaggedToken> basicChunks, List<String> expectedChunks) {
    int i = 0;
    for (String expectedChunk : expectedChunks) {
      ChunkTaggedToken outputChunksHere = basicChunks.get(i);
      if (!outputChunksHere.getChunkTags().contains(new ChunkTag(expectedChunk))) {
        fail("Expected " + expectedChunk + " but got " + outputChunksHere + " at position " + i + " for input:\n  " + input +
             "\nPlain input:\n  " + plainInput +
             "\nBasic chunks:\n  " + basicChunks +
             "\nExpected:\n  " + expectedChunks);
      }
      i++;
    }
  }

}