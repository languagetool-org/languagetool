/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import org.junit.Test;
import org.languagetool.*;
import org.languagetool.synthesis.Synthesizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AgreementSuggestor2Test {

  private final Language german = Languages.getLanguageForShortCode("de-DE");
  private final Synthesizer synthesizer = german.getSynthesizer();
  private final JLanguageTool lt = new JLanguageTool(german);

  @Test
  public void testSuggestions() throws IOException {
    assertSuggestion1("deine Buch", "[dein Buch, deinem Buch, deinem Buche, deines Buches, deines Buchs, deine Bücher, deinen Büchern, deiner Bücher]");
    assertSuggestion1("dieser Buch", "[dies Buch, dieses Buch, diesem Buch, diesem Buche, dieses Buches, dieses Buchs, diese Bücher, diesen Büchern, dieser Bücher]");
    assertSuggestion1("die Kabels", "[der Kabel, den Kabel, dem Kabel, des Kabels, das Kabel, die Kabel, den Kabeln]");
    assertSuggestion1("die LAN-Kabels", "[der LAN-Kabel, den LAN-Kabel, dem LAN-Kabel, des LAN-Kabels, das LAN-Kabel, die LAN-Kabel, den LAN-Kabeln]");
    assertSuggestion1("mehrere Kabels", "[mehrere Kabel, mehreren Kabeln, mehrerer Kabel]");
    assertSuggestion1("mehrere LAN-Kabels", "[mehrere LAN-Kabel, mehreren LAN-Kabeln, mehrerer LAN-Kabel]");
    assertSuggestion1("mehrere WLAN-LAN-Kabels", "[mehrere WLAN-LAN-Kabel, mehreren WLAN-LAN-Kabeln, mehrerer WLAN-LAN-Kabel]");
    assertSuggestion1("Ihren Verständnis", "[Ihr Verständnis, Ihrem Verständnis, Ihrem Verständnisse, Ihres Verständnisses]");
    assertSuggestion1("die Kühlschranktest", "[der Kühlschranktest, den Kühlschranktest, dem Kühlschranktest, dem Kühlschrankteste, " +
      "des Kühlschranktestes, des Kühlschranktests, die Kühlschrankteste, die Kühlschranktests, den Kühlschranktesten, den Kühlschranktests, " +
      "der Kühlschrankteste, der Kühlschranktests]");
    assertSuggestion1("die Kühlschrankverarbeitungstest", "[der Kühlschrankverarbeitungstest, den Kühlschrankverarbeitungstest, " +
      "dem Kühlschrankverarbeitungstest, dem Kühlschrankverarbeitungsteste, des Kühlschrankverarbeitungstestes, " +
      "des Kühlschrankverarbeitungstests, die Kühlschrankverarbeitungsteste, die Kühlschrankverarbeitungstests, " +
      "den Kühlschrankverarbeitungstesten, den Kühlschrankverarbeitungstests, der Kühlschrankverarbeitungsteste, der Kühlschrankverarbeitungstests]");
  }

  @Test
  public void testSuggestionsWithReplType() throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("gehe zur Mann");
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    AgreementSuggestor2 suggestor = new AgreementSuggestor2(synthesizer, tags.get(2), tags.get(3), AgreementRule.ReplacementType.Zur);
    assertThat(suggestor.getSuggestions().toString(), is("[zum Mann, zum Manne, zu den Männern]"));
  }

  @Test
  public void testSuggestionsHaus() throws IOException {
    String res = "[das Haus, dem Haus, dem Hause, des Hauses, die Häuser, den Häusern, der Häuser]";
    assertSuggestion1("der Haus", res);
    assertSuggestion1("das Haus", res);
    assertSuggestion1("der Haus", res);
    assertSuggestion1("die Haus", res);
    assertSuggestion1("die Hauses", res);
    assertSuggestion1("die Häusern", res);
  }

  @Test
  public void testDetAdjNounSuggestions() throws IOException {
    assertSuggestion2("die neuen Unterlage", "[die neue Unterlage, der neuen Unterlage, die neuen Unterlagen, den neuen Unterlagen, der neuen Unterlagen]");
    assertSuggestion2("der neue Unterlagen", "[die neue Unterlage, der neuen Unterlage, die neuen Unterlagen, den neuen Unterlagen, der neuen Unterlagen]");
    assertSuggestion2("eine schönes Auto", "[ein schönes Auto, einem schönen Auto, eines schönen Autos]");
    assertSuggestion2("eine schöne Auto", "[ein schönes Auto, einem schönen Auto, eines schönen Autos]");
    assertSuggestion2("ein schöne Auto", "[ein schönes Auto, einem schönen Auto, eines schönen Autos]");
    assertSuggestion2("einen großen Auto", "[ein großes Auto, einem großen Auto, eines großen Autos]");
    assertSuggestion2("das schönes Auto", "[das schöne Auto, dem schönen Auto, des schönen Autos, die schönen Autos, den schönen Autos, der schönen Autos]");
    assertSuggestion2("das schöneren Auto", "[das schöne Auto, dem schönen Auto, des schönen Autos, die schönen Autos, den schönen Autos, der schönen Autos]");
    assertSuggestion2("das schöneren Auto", "[das schöne Auto, dem schönen Auto, des schönen Autos, die schönen Autos, den schönen Autos, der schönen Autos]");
    assertSuggestion2("das schönstem Auto", "[das schöne Auto, dem schönen Auto, des schönen Autos, die schönen Autos, den schönen Autos, der schönen Autos]");
    assertSuggestion2("das schönsten Auto", "[das schöne Auto, dem schönen Auto, des schönen Autos, die schönen Autos, den schönen Autos, der schönen Autos]");
    assertSuggestion2("der schöne Auto", "[das schöne Auto, dem schönen Auto, des schönen Autos, die schönen Autos, den schönen Autos, der schönen Autos]");
    assertSuggestion2("der kleine Auto", "[das kleine Auto, dem kleinen Auto, des kleinen Autos, die kleinen Autos, den kleinen Autos, der kleinen Autos]");
    assertSuggestion2("der kleiner Auto", "[das kleine Auto, dem kleinen Auto, des kleinen Autos, die kleinen Autos, den kleinen Autos, der kleinen Autos]");
    assertSuggestion2("das stärkste Körperteil", "[der starke Körperteil, den starken Körperteil, dem starken Körperteil, des starken Körperteils, das starke Körperteil, dem starken Körperteile, des starken Körperteiles, die starken Körperteile, den starken Körperteilen, der starken Körperteile]");
    // "benötigten" ist PA2:
    assertSuggestion2("die benötigten Unterlage", "[die benötigte Unterlage, der benötigten Unterlage, die benötigten Unterlagen, den benötigten Unterlagen, der benötigten Unterlagen]");   // ist PA2
    assertSuggestion2("eine benötigten Unterlage", "[eine benötigte Unterlage, einer benötigten Unterlage]");
    assertSuggestion2("die voller Verzierungen", "[die volle Verzierung, der vollen Verzierung, die vollen Verzierungen, den vollen Verzierungen, der vollen Verzierungen]"); // evtl. Fehlalarm...
    assertSuggestion2("zu zukünftigen Vorstands", "[]");  // ?
    assertSuggestion2("der ikonischsten Gebäuden", "[das ikonische Gebäude, dem ikonischen Gebäude, des ikonischen Gebäudes, die ikonischen Gebäude, den ikonischen Gebäuden, der ikonischen Gebäude]");
    assertSuggestion2("des südlichen Kontinent", "[der südliche Kontinent, den südlichen Kontinent, dem südlichen Kontinent, dem südlichen Kontinente, des südlichen Kontinentes, des südlichen Kontinents, die südlichen Kontinente, den südlichen Kontinenten, der südlichen Kontinente]");
    assertSuggestion2("die erwartet Entwicklung", "[die erwartete Entwicklung, der erwarteten Entwicklung, die erwarteten Entwicklungen, den erwarteten Entwicklungen, der erwarteten Entwicklungen]");
    assertSuggestion2("die verschieden Ämter", "[das verschiedene Amt, dem verschiedenen Amt, dem verschiedenen Amte, des verschiedenen Amtes, des verschiedenen Amts, die verschiedenen Ämter, den verschiedenen Ämtern, der verschiedenen Ämter]");
    assertSuggestion2("keine richtiger Fahrerin", "[keine richtige Fahrerin, keiner richtigen Fahrerin, keine richtigen Fahrerinnen, keinen richtigen Fahrerinnen, keiner richtigen Fahrerinnen]");
  }

  @Test
  public void testDetNounSuggestionsWithPreposition() throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("für dein Schmuck");
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    AgreementSuggestor2 suggestor = new AgreementSuggestor2(synthesizer, tags.get(2), tags.get(3), null);
    assertThat(suggestor.getSuggestions().toString(), is("[dein Schmuck, deinen Schmuck, deinem Schmuck, deinem Schmucke, deines Schmuckes, deines Schmucks]"));
    suggestor.setPreposition(tags.get(1));  // "für"
    assertThat(suggestor.getSuggestions().toString(), is("[deinen Schmuck]"));
  }

  @Test
  public void testDetAdjNounSuggestionsWithPreposition() throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("über ein hilfreichen Tipp");
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    AgreementSuggestor2 suggestor = new AgreementSuggestor2(synthesizer, tags.get(2), tags.get(3), tags.get(4), null);
    assertThat(suggestor.getSuggestions().toString(), is("[ein hilfreicher Tipp, einen hilfreichen Tipp, einem hilfreichen Tipp, eines hilfreichen Tipps]"));
    suggestor.setPreposition(tags.get(1));  // "über"
    assertThat(suggestor.getSuggestions().toString(), is("[einen hilfreichen Tipp, einem hilfreichen Tipp]"));
  }

  @Test
  public void testCase1() throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("an den stacheligen Pflanzenteile");
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    AgreementSuggestor2 suggestor = new AgreementSuggestor2(synthesizer, tags.get(2), tags.get(3), tags.get(4), null);
    assertThat(suggestor.getSuggestions().toString(), is("[der stachelige Pflanzenteil, den stacheligen Pflanzenteil, dem stacheligen Pflanzenteil, dem stacheligen Pflanzenteile, des stacheligen Pflanzenteiles, des stacheligen Pflanzenteils, das stachelige Pflanzenteil, die stacheligen Pflanzenteile, den stacheligen Pflanzenteilen, der stacheligen Pflanzenteile]"));
    suggestor.setPreposition(tags.get(1));  // "an"
    assertThat(suggestor.getSuggestions().toString(), is("[den stacheligen Pflanzenteil, dem stacheligen Pflanzenteil, dem stacheligen Pflanzenteile, das stachelige Pflanzenteil, die stacheligen Pflanzenteile, den stacheligen Pflanzenteilen]"));
  }

  private void assertSuggestion1(String input, String expectedSuggestions) throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(input);
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    if (analyzedSentence.getTokensWithoutWhitespace().length != 3) {  // 2 tokens + sentence start token
      fail("Please use 2 tokens (det, noun) as input: " + input);
    }
    AgreementSuggestor2 suggestor = new AgreementSuggestor2(synthesizer, tags.get(1), tags.get(2), null);
    assertThat(suggestor.getSuggestions().toString(), is(expectedSuggestions));
  }
  
  private void assertSuggestion2(String input, String expectedSuggestions) throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(input);
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    if (analyzedSentence.getTokensWithoutWhitespace().length != 4) {  // 3 tokens + sentence start token
      fail("Please use 3 tokens (det, adj, noun) as input: " + input);
    }
    AgreementSuggestor2 suggestor = new AgreementSuggestor2(synthesizer, tags.get(1), tags.get(2), tags.get(3), null);
    assertThat(suggestor.getSuggestions().toString(), is(expectedSuggestions));
  }
  
}
