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

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.GermanyGerman;

public class GermanChunkerTest {

  private final JLanguageTool lt = new JLanguageTool(new GermanyGerman());
  private final GermanChunker chunker = new GermanChunker();

  @Test
  public void testChunking() throws Exception {
    assertFullChunks("Ein/B Haus/I");
    assertFullChunks("Ein/NPP Hund/NPP und/NPP eine/NPP Katze/NPP stehen dort");
    assertFullChunks("Es war die/NPS größte/NPS und/NPS erfolgreichste/NPS Erfindung/NPS");
    assertFullChunks("Geräte/B , deren/NPS Bestimmung/NPS und/NPS Funktion/NPS unklar sind.");
    assertFullChunks("Julia/NPP und/NPP Karsten/NPP sind alt");
    assertFullChunks("Es ist die/NPS älteste/NPS und/NPS bekannteste/NPS Maßnahme/NPS");
    assertFullChunks("Das ist eine/NPS Masseeinheit/NPS und/NPS keine/NPS Gewichtseinheit/NPS");
    assertFullChunks("Sie fährt nur eins/NPS ihrer/NPS drei/NPS Autos/NPS");
    assertFullChunks("Da sind er/NPP und/NPP seine/NPP Schwester/NPP");

    //assertFullChunks("Sowohl/NPP sein/NPP Vater/NPP als/NPP auch/NPP seine/NPP Mutter/NPP sind da");  //?
    //assertFullChunks("Sowohl/NPP Tom/NPP als/NPP auch/NPP Maria/NPP sind da");
    //assertFullChunks("Sowohl/NPP er/NPP als/NPP auch/NPP seine/NPP Schwester/NPP sind da");

    assertFullChunks("Rekonstruktionen/NPP oder/NPP der/NPP Wiederaufbau/NPP sind das/NPS Ziel/NPS");
    assertFullChunks("Isolation/NPP und/NPP ihre/NPP Überwindung/NPP ist das/NPS Thema/NPS");
    assertFullChunks("Es gibt weder/NPP Gerechtigkeit/NPP noch/NPP Freiheit/NPP");
    assertFullChunks("Da sitzen drei/NPP Katzen/NPP");
    assertFullChunks("Der/NPS von/NPS der/NPS Regierung/NPS geprüfte/NPS Hund/NPS ist grün");
    assertFullChunks("Herr/NPP und/NPP Frau/NPP Schröder/NPP sind betrunken");
    //assertFullChunks("Die/NPS hohe/NPS Zahl/NPS dieser/NPS relativ/NPS kleinen/NPS Verwaltungseinheiten/NPS ist beeindruckend");   //?
    //assertFullChunks("Das ist eine/NPS der/NPS am/NPS meisten/NPS verbreiteten/NPS Krankheiten/NPS");   //?
    assertFullChunks("Das sind 37/NPS Prozent/NPS");
    assertFullChunks("Das sind 37/NPP Prozent/NPP");
    assertFullChunks("Er will die/NPP Arbeitsplätze/NPP so umgestalten , dass/NPP sie/NPP wie/NPP ein/NPP Spiel/NPP sind.");
    assertFullChunks("So dass Knochenbrüche/NPP und/NPP Platzwunden/NPP die/NPP Regel/NPP sind");
    assertFullChunks("Eine/NPS Veranstaltung/NPS ,/NPS die/NPS immer/NPS wieder/NPS ein/NPS kultureller/NPS Höhepunkt/NPS war");

    assertFullChunks("Und die/NPS ältere/NPS der/NPS beiden/NPS Töchter/NPS ist 20.");
    assertFullChunks("Der/NPS Synthese/NPS organischer/NPS Verbindungen/NPS steht nichts im/PP Weg/NPS");
    assertFullChunks("Aber/B die/NPP Kenntnisse/NPP der/NPP Sprache/NPP sind nötig.");  // actually "Aber" should not be tagged
    assertFullChunks("Dort steht die/NPS Pyramide/NPS des/NPS Friedens/NPS und/NPS der/NPS Eintracht/NPS");
    assertFullChunks("Und Teil/B der/NPS dort/NPS ausgestellten/NPS Bestände/NPS wurde privat finanziert.");
    assertFullChunks("Autor/NPS der/NPS ersten/NPS beiden/NPS Bücher/NPS ist Stephen King/NPS");
    assertFullChunks("Autor/NPS der/NPS beiden/NPS Bücher/NPS ist Stephen King/NPS");
    assertFullChunks("Teil/NPS der/NPS umfangreichen/NPS dort/NPS ausgestellten/NPS Bestände/NPS stammt von privat");
    assertFullChunks("Ein/NPS Teil/NPS der/NPS umfangreichen/NPS dort/NPS ausgestellten/NPS Bestände/NPS stammt von privat");
    assertFullChunks("Die/NPS Krankheit/NPS unserer/NPS heutigen/NPS Städte/NPS und/NPS Siedlungen/NPS ist der/NPS Verkehr/NPS");
    assertFullChunks("Der/B Nil/I ist der/NPS letzte/NPS der/NPS vier/NPS großen/NPS Flüsse/NPS");
    assertFullChunks("Der/NPS letzte/NPS der/NPS vier/NPS großen/NPS Flüsse/NPS ist der/B Nil/I");
    assertFullChunks("Sie kennt eine/NPP Menge/NPP englischer/NPP Wörter/NPP");
    assertFullChunks("Eine/NPP Menge/NPP englischer/NPP Wörter/NPP sind aus/PP dem/NPS Lateinischen/NPS abgeleitet.");
    assertFullChunks("Laut/PP den/PP meisten/PP Quellen/PP ist er 35 Jahre/B alt.");
    assertFullChunks("Bei/PP den/PP sehr/PP niedrigen/PP Oberflächentemperaturen/PP verbrennt nichts");
    assertFullChunks("In/PP den/PP alten/PP Religionen/PP ,/PP Mythen/PP und/PP Sagen/PP tauchen Geister/B auf.");
    assertFullChunks("Die/B Straße/I ist wichtig für/PP die/PP Stadtteile/PP und/PP selbständigen/PP Ortsteile/PP");
    assertFullChunks("Es herrscht gute/NPS Laune/NPS in/PP chemischen/PP Komplexverbindungen/PP");
    assertFullChunks("Funktionen/NPP des/NPP Körpers/NPP einschließlich/PP der/PP biologischen/PP und/PP sozialen/PP Grundlagen/PP");
    assertFullChunks("Das/NPS Dokument/NPS umfasst das für/PP Ärzte/PP und/PP Ärztinnen/PP festgestellte/PP Risikoprofil/PP");
    assertFullChunks("In/PP den/PP darauf/PP folgenden/PP Wochen/PP ging es los.");
    assertFullChunks("In/PP nur/PP zwei/PP Wochen/PP geht es los.");
    assertFullChunks("Programme/B , in/PP deren/PP deutschen/PP Installationen/PP nichts funktioniert.");
    assertFullChunks("Nach/PP sachlichen/PP und/PP militärischen/PP Kriterien/PP war das unnötig.");
    assertFullChunks("Mit/PP über/PP 1000/PP Handschriften/PP ist es die/NPS größte/NPS Sammlung/NPS");
    assertFullChunks("Es gab Beschwerden/NPP über/PP laufende/PP Sanierungsmaßnahmen/PP");
    assertFullChunks("Gesteigerte/B Effizienz/I durch/PP Einsatz/PP größerer/PP Maschinen/PP und/PP bessere/PP Kapazitätsplanung/PP");
    assertFullChunks("Bei/PP sehr/PP guten/PP Beobachtungsbedingungen/PP bin ich dabei");
    assertFullChunks("Die/NPP Beziehungen/NPP zwischen/NPP Kanada/NPP und/NPP dem/NPP Iran/NPP sind unterkühlt");
    assertFullChunks("Die/PP darauffolgenden/PP Jahre/PP war es kalt");
    assertFullChunks("Die/NPP darauffolgenden/NPP Jahre/NPP waren kalt");
    assertFullChunks("Die/PP letzten/PP zwei/PP Monate/PP war es kalt");
    //assertFullChunks("Die/NPP letzten/NPP zwei/NPP Monate/NPP waren kalt");
    assertFullChunks("Letztes/PP Jahr/PP war kalt");
    assertFullChunks("Letztes/PP Jahr/PP war es kalt");
    assertFullChunks("Es sind Atome/NPP ,/NPP welche/NPP der/NPP Urstoff/NPP aller/NPP Körper/NPP sind");
    assertFullChunks("Kommentare/NPP ,/NPP Korrekturen/NPP ,/NPP Kritik/NPP bitte nach /dev/null");
    assertFullChunks("Einer/NPS der/NPS beiden/NPS Höfe/NPS war schön");
  }

  // B = begin, will be expanded to B-NP, I = inner, will be expanded to I-NP
  @Test
  public void testOpenNLPLikeChunking() throws Exception {
    //GermanChunker.setDebug(true);
    assertBasicChunks("Ein/B Haus/I");
    assertBasicChunks("Da steht ein/B Haus/I");
    assertBasicChunks("Da steht ein/B schönes/I Haus/I");
    assertBasicChunks("Da steht ein/B schönes/I großes/I Haus/I");
    assertBasicChunks("Da steht ein/B sehr/I großes/I Haus/I");
    assertBasicChunks("Da steht ein/B sehr/I schönes/I großes/I Haus/I");
    assertBasicChunks("Da steht ein/B sehr/I großes/I Haus/I mit Dach/B");
    assertBasicChunks("Da steht ein/B sehr/I großes/I Haus/I mit einem/B blauen/I Dach/I");
    assertBasicChunks("Eine/B leckere/I Lasagne/I");
    assertBasicChunks("Herr/B Meier/I isst eine/B leckere/I Lasagne/I");
    assertBasicChunks("Herr/B Schrödinger/I isst einen/B Kuchen/I");
    assertBasicChunks("Herr/B Schrödinger/I isst einen/B leckeren/I Kuchen/I");
    assertBasicChunks("Herr/B Karl/I Meier/I isst eine/B leckere/I Lasagne/I");
    assertBasicChunks("Herr/B Finn/I Westerwalbesloh/I isst eine/B leckere/I Lasagne/I");
    assertBasicChunks("Unsere/B schöne/I Heimat/I geht den/B Bach/I runter");
    assertBasicChunks("Er meint das/B Haus/I am grünen/B Hang/I");
    assertBasicChunks("Ich/B muss dem/B Hund/I Futter/I geben");  // TODO: see next line for how it should be (but: 'Pariser Innenstadt' should be one NP)
    //assertChunks("Ich muss dem/B Hund/I Futter/B geben");
    assertBasicChunks("Das/B Wasser/I , das die/B Wärme/I überträgt");
    assertBasicChunks("Er mag das/B Wasser/I , das/B Meer/I und die/B Luft/I");
    assertBasicChunks("Schon mehr als zwanzig/B Prozent/I der/B Arbeiter/I sind im Streik/B");
    assertBasicChunks("Das/B neue/I Gesetz/I betrifft 1000 Bürger/B"); // '1000' sollte evtl. mit in die NP...
    assertBasicChunks("In zwei/B Wochen/I ist Weihnachten/B");
    assertBasicChunks("Eines ihrer/B drei/I Autos/I ist blau");
  }

  @Test
  public void testTemp() throws Exception {
    //GermanChunker.setDebug(true);
    //TODO:
    //assertFullChunks("Seine Novelle, die eigentlich eine Glosse ist, war so.");
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

  private void assertBasicChunks(String input) throws Exception {
    String plainInput = getPlainInput(input);
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(plainInput);
    AnalyzedTokenReadings[] result = analyzedSentence.getTokensWithoutWhitespace();
    List<ChunkTaggedToken> basicChunks = chunker.getBasicChunks(Arrays.asList(result));
    List<String> expectedChunks = getExpectedChunks(input);
    assertChunks(input, plainInput, basicChunks, expectedChunks);
  }

  private void assertFullChunks(String input) throws Exception {
    String plainInput = getPlainInput(input);
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(plainInput);
    AnalyzedTokenReadings[] result = analyzedSentence.getTokensWithoutWhitespace();
    chunker.addChunkTags(Arrays.asList(result));
    List<String> expectedChunks = getExpectedChunks(input);
    List<ChunkTaggedToken> result2 = new ArrayList<>();
    int i = 0;
    for (AnalyzedTokenReadings readings : result) {
      if (i > 0) {
        ChunkTaggedToken chunkTaggedToken = new ChunkTaggedToken(readings.getToken(), readings.getChunkTags(), readings);
        result2.add(chunkTaggedToken);
      }
      i++;
    }
    assertChunks(input, plainInput, result2, expectedChunks);
  }

  private String getPlainInput(String input) {
    return input.replaceAll("/[A-Z-]*", "").replace(" ,", ",");
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
        } else if (chunk.equals("NPP")) {
          expectedChunks.add("NPP");
        } else if (chunk.equals("NPS")) {
          expectedChunks.add("NPS");
        } else if (chunk.equals("PP")) {
          expectedChunks.add("PP");
        } else {
          throw new RuntimeException("Unknown chunk type: '" + chunk + "'");
        }
      } else {
        expectedChunks.add("O");
      }
    }
    return expectedChunks;
  }

  private void assertChunks(String input, String plainInput, List<ChunkTaggedToken> chunks, List<String> expectedChunks) {
    int i = 0;
    for (String expectedChunk : expectedChunks) {
      ChunkTaggedToken outputChunksHere = chunks.get(i);
      if (!outputChunksHere.getChunkTags().contains(new ChunkTag(expectedChunk))) {
        fail("Expected '" + expectedChunk + "' but got '" + outputChunksHere + "' at position " + i + " for input:\n  " + input +
             "\nPlain input:\n  " + plainInput +
             "\nChunks:\n  " + chunks +
             "\nExpected:\n  " + expectedChunks);
      }
      i++;
    }
  }

}