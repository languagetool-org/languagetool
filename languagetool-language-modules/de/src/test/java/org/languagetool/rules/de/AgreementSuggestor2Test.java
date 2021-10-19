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
  public void testInteractive() throws IOException {
    //assertSuggestion1("des Züchten", "[das Züchten, dem Züchten, des Züchtens]");
  }

  @Test
  public void testSuggestions() throws IOException {
    assertSuggestion1("deine Buch", "[dein Buch, deinem Buch, deine Bücher, deinem Buche, deines Buches, deines Buchs, deinen Büchern, deiner Bücher]");
    assertSuggestion1("dieser Buch", "[dies Buch, dieses Buch, diesem Buch, dieser Bücher, diesem Buche, dieses Buches, dieses Buchs, diese Bücher, diesen Büchern]");
    assertSuggestion1("die Kabels", "[des Kabels, die Kabel, der Kabel, den Kabel, dem Kabel, das Kabel, den Kabeln]");
    assertSuggestion1("die LAN-Kabels", "[des LAN-Kabels, die LAN-Kabel, der LAN-Kabel, den LAN-Kabel, dem LAN-Kabel, das LAN-Kabel, den LAN-Kabeln]");
    assertSuggestion1("mehrere Kabels", "[mehrere Kabel, mehreren Kabeln, mehrerer Kabel]");
    assertSuggestion1("mehrere LAN-Kabels", "[mehrere LAN-Kabel, mehreren LAN-Kabeln, mehrerer LAN-Kabel]");
    assertSuggestion1("mehrere WLAN-LAN-Kabels", "[mehrere WLAN-LAN-Kabel, mehreren WLAN-LAN-Kabeln, mehrerer WLAN-LAN-Kabel]");
    assertSuggestion1("Ihren Verständnis", "[Ihr Verständnis, Ihrem Verständnis, Ihrem Verständnisse, Ihres Verständnisses]");
    assertSuggestion1("des Züchten", "[das Züchten, dem Züchten, des Züchtens]");
    assertSuggestion1("die Kühlschranktest", "[der Kühlschranktest, den Kühlschranktest, dem Kühlschranktest, die Kühlschrankteste, " +
      "die Kühlschranktests, dem Kühlschrankteste, des Kühlschranktestes, des Kühlschranktests, den Kühlschranktesten, " +
      "den Kühlschranktests, der Kühlschrankteste, der Kühlschranktests]");
    assertSuggestion1("die Kühlschrankverarbeitungstest", "[der Kühlschrankverarbeitungstest, den Kühlschrankverarbeitungstest, " +
      "dem Kühlschrankverarbeitungstest, die Kühlschrankverarbeitungsteste, die Kühlschrankverarbeitungstests, " +
      "dem Kühlschrankverarbeitungsteste, des Kühlschrankverarbeitungstestes, des Kühlschrankverarbeitungstests, " +
      "den Kühlschrankverarbeitungstesten, den Kühlschrankverarbeitungstests, der Kühlschrankverarbeitungsteste, " +
      "der Kühlschrankverarbeitungstests]");
    assertSuggestion2("den gleiche Gebiete", "[das gleiche Gebiet, dem gleichen Gebiete, die gleichen Gebiete, " +
      "den gleichen Gebieten, der gleichen Gebiete, dem gleichen Gebiet, des gleichen Gebietes, des gleichen Gebiets]");
  }

  @Test
  public void testSuggestionsWithReplType() throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("gehe zur Mann");
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    AgreementSuggestor2 suggestor = new AgreementSuggestor2(synthesizer, tags.get(2), tags.get(3), AgreementRule.ReplacementType.Zur);
    assertThat(suggestor.getSuggestions().toString(), is("[zum Mann, zum Manne, zu den Männern]"));

    analyzedSentence = lt.getAnalyzedSentence("gehe zur kuschelige Ferienwohnung");
    tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    suggestor = new AgreementSuggestor2(synthesizer, tags.get(2), tags.get(3), tags.get(4), AgreementRule.ReplacementType.Zur);
    assertThat(suggestor.getSuggestions().toString(), is("[zur kuscheligen Ferienwohnung, zu den kuscheligen Ferienwohnungen]"));
  }

  @Test
  public void testSuggestionsWithReplTypeIns() throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("gehe ins Hauses");
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    AgreementSuggestor2 suggestor = new AgreementSuggestor2(synthesizer, tags.get(2), tags.get(3), AgreementRule.ReplacementType.Ins);
    assertThat(suggestor.getSuggestions().toString(), is("[ins Haus, im Haus, im Hause, in die Häuser, in den Häusern]"));
  }

  @Test
  public void testSuggestionsWithReplTypeInsAdj() throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("gehe ins großen Haus");
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    AgreementSuggestor2 suggestor = new AgreementSuggestor2(synthesizer, tags.get(2), tags.get(3), tags.get(4), AgreementRule.ReplacementType.Ins);
    assertThat(suggestor.getSuggestions().toString(), is("[im großen Haus, ins große Haus, im großen Hause, in die großen Häuser, in den großen Häusern]"));
  }

  @Test
  public void testSuggestionsHaus() throws IOException {
    assertSuggestion1("Der Haus", "[Das Haus, Dem Haus, Der Häuser, Dem Hause, Des Hauses, Die Häuser, Den Häusern]");
    assertSuggestion1("der Haus", "[das Haus, dem Haus, der Häuser, dem Hause, des Hauses, die Häuser, den Häusern]");
    assertSuggestion1("das Haus", "[dem Haus, dem Hause, des Hauses, die Häuser, den Häusern, der Häuser]");
    assertSuggestion1("der Haus", "[das Haus, dem Haus, der Häuser, dem Hause, des Hauses, die Häuser, den Häusern]");
    assertSuggestion1("die Haus", "[das Haus, dem Haus, die Häuser, dem Hause, des Hauses, den Häusern, der Häuser]");
    assertSuggestion1("die Hauses", "[des Hauses, die Häuser, das Haus, dem Haus, dem Hause, den Häusern, der Häuser]");
    assertSuggestion1("die Häusern", "[die Häuser, den Häusern, das Haus, dem Haus, dem Hause, des Hauses, der Häuser]");
    // filtered:
    assertSuggestion1("Der Haus", "[Das Haus, Dem Haus, Der Häuser]", true);
    assertSuggestion1("der Haus", "[das Haus, dem Haus, der Häuser]", true);
    assertSuggestion1("das Haus", "[dem Haus]", true);
    assertSuggestion1("der Haus", "[das Haus, dem Haus, der Häuser]", true);
    assertSuggestion1("die Haus", "[das Haus, dem Haus, die Häuser]", true);
    assertSuggestion1("die Hauses", "[des Hauses, die Häuser]", true);
    assertSuggestion1("die Häusern", "[die Häuser, den Häusern]", true);
  }

  @Test
  public void testDetAdjNounSuggestions() throws IOException {
    assertSuggestion2("die neuen Unterlage", "[die neue Unterlage, der neuen Unterlage, die neuen Unterlagen, den neuen Unterlagen, der neuen Unterlagen]");
    assertSuggestion2("der neue Unterlagen", "[der neuen Unterlagen, die neue Unterlage, der neuen Unterlage, die neuen Unterlagen, den neuen Unterlagen]");
    assertSuggestion2("eine schönes Auto", "[ein schönes Auto, einem schönen Auto, eines schönen Autos]");
    assertSuggestion2("eine schöne Auto", "[ein schönes Auto, einem schönen Auto, eines schönen Autos]");
    assertSuggestion2("ein schöne Auto", "[ein schönes Auto, einem schönen Auto, eines schönen Autos]");
    assertSuggestion2("einen großen Auto", "[einem großen Auto, ein großes Auto, eines großen Autos]");
    assertSuggestion2("der schöne Auto", "[das schöne Auto, dem schönen Auto, der schönen Autos, des schönen Autos, die schönen Autos, den schönen Autos]");
    assertSuggestion2("der kleine Auto", "[das kleine Auto, dem kleinen Auto, der kleinen Autos, des kleinen Autos, die kleinen Autos, den kleinen Autos]");
    assertSuggestion2("der kleiner Auto", "[das kleine Auto, dem kleinen Auto, der kleinen Autos, des kleinen Autos, die kleinen Autos, den kleinen Autos]");
    assertSuggestion2("das stärkste Körperteil", "[der stärkste Körperteil, den stärksten Körperteil, dem stärksten Körperteil, des stärksten Körperteils, dem stärksten Körperteile, des stärksten Körperteiles, die stärksten Körperteile, den stärksten Körperteilen, der stärksten Körperteile]");
    // "benötigten" ist PA2:
    assertSuggestion2("die benötigten Unterlage", "[die benötigte Unterlage, der benötigten Unterlage, die benötigten Unterlagen, den benötigten Unterlagen, der benötigten Unterlagen]");   // ist PA2
    assertSuggestion2("eine benötigten Unterlage", "[eine benötigte Unterlage, einer benötigten Unterlage]");
    assertSuggestion2("die voller Verzierungen", "[die vollen Verzierungen, die volle Verzierung, den vollen Verzierungen, der vollen Verzierungen, der vollen Verzierung]"); // evtl. Fehlalarm...
    assertSuggestion2("zu zukünftigen Vorstands", "[]");  // ?
    assertSuggestion2("des südlichen Kontinent", "[den südlichen Kontinent, dem südlichen Kontinent, des südlichen Kontinentes, des südlichen Kontinents, der südliche Kontinent, dem südlichen Kontinente, die südlichen Kontinente, den südlichen Kontinenten, der südlichen Kontinente]");
    assertSuggestion2("die erwartet Entwicklung", "[die erwartete Entwicklung, der erwarteten Entwicklung, die erwarteten Entwicklungen, den erwarteten Entwicklungen, der erwarteten Entwicklungen]");
    assertSuggestion2("die verschieden Ämter", "[die verschiedenen Ämter, der verschiedenen Ämter, das verschiedene Amt, dem verschiedenen Amt, dem verschiedenen Amte, des verschiedenen Amtes, des verschiedenen Amts, den verschiedenen Ämtern]");
    assertSuggestion2("keine richtiger Fahrerin", "[keine richtige Fahrerin, keiner richtigen Fahrerin, keine richtigen Fahrerinnen, keinen richtigen Fahrerinnen, keiner richtigen Fahrerinnen]");
    // GRU, KOM, SUP:
    assertSuggestion2("das schönes Auto", "[das schöne Auto, dem schönen Auto, des schönen Autos, die schönen Autos, den schönen Autos, der schönen Autos]");
    assertSuggestion2("das schöneren Auto", "[das schönere Auto, dem schöneren Auto, des schöneren Autos, die schöneren Autos, den schöneren Autos, der schöneren Autos]");
    assertSuggestion2("das schönstem Auto", "[das schönste Auto, dem schönsten Auto, des schönsten Autos, die schönsten Autos, den schönsten Autos, der schönsten Autos]");
    assertSuggestion2("das schönsten Auto", "[das schönste Auto, das schöne Auto, dem schönsten Auto, dem schönen Auto, des schönsten Autos, " +
      "die schönsten Autos, den schönsten Autos, der schönsten Autos, des schönen Autos, die schönen Autos, den schönen Autos, der schönen Autos]");
    assertSuggestion2("der ikonischen Gebäuden", "[den ikonischen Gebäuden, der ikonischen Gebäude, dem ikonischen Gebäude, des ikonischen Gebäudes, die ikonischen Gebäude, das ikonische Gebäude]");
    assertSuggestion2("der ikonischeren Gebäuden", "[den ikonischeren Gebäuden, der ikonischeren Gebäude, dem ikonischeren Gebäude, des ikonischeren Gebäudes, die ikonischeren Gebäude, das ikonischere Gebäude]");
    assertSuggestion2("der ikonischsten Gebäuden", "[den ikonischsten Gebäuden, der ikonischsten Gebäude, dem ikonischsten Gebäude, des ikonischsten Gebäudes, die ikonischsten Gebäude, den ikonischen Gebäuden, " +
      "der ikonischen Gebäude, das ikonischste Gebäude, das ikonische Gebäude, dem ikonischen Gebäude, des ikonischen Gebäudes, die ikonischen Gebäude]");
  }

  @Test
  public void testDetNounSuggestionsWithPreposition() throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("für dein Schmuck");
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    AgreementSuggestor2 suggestor = new AgreementSuggestor2(synthesizer, tags.get(2), tags.get(3), null);
    assertThat(suggestor.getSuggestions(false).toString(), is("[deinen Schmuck, deinem Schmuck, deinem Schmucke, deines Schmuckes, deines Schmucks]"));
    assertThat(suggestor.getSuggestions(true).toString(), is("[deinen Schmuck, deinem Schmuck]"));
    suggestor.setPreposition(tags.get(1));  // "für"
    assertThat(suggestor.getSuggestions(false).toString(), is("[deinen Schmuck]"));
    assertThat(suggestor.getSuggestions(true).toString(), is("[deinen Schmuck]"));
  }

  @Test
  public void testDetAdjNounSuggestionsWithPreposition() throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("über ein hilfreichen Tipp");
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    AgreementSuggestor2 suggestor = new AgreementSuggestor2(synthesizer, tags.get(2), tags.get(3), tags.get(4), null);
    assertThat(suggestor.getSuggestions().toString(), is("[ein hilfreicher Tipp, einen hilfreichen Tipp, einem hilfreichen Tipp, eines hilfreichen Tipps]"));
    suggestor.setPreposition(tags.get(1));  // "über"
    assertThat(suggestor.getSuggestions().toString(), is("[einen hilfreichen Tipp, einem hilfreichen Tipp]"));

    analyzedSentence = lt.getAnalyzedSentence("an den stacheligen Pflanzenteile");
    tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    suggestor = new AgreementSuggestor2(synthesizer, tags.get(2), tags.get(3), tags.get(4), null);
    assertThat(suggestor.getSuggestions().toString(), is("[den stacheligen Pflanzenteil, dem stacheligen Pflanzenteile, die stacheligen Pflanzenteile, den stacheligen Pflanzenteilen, der stacheligen Pflanzenteile, dem stacheligen Pflanzenteil, des stacheligen Pflanzenteiles, des stacheligen Pflanzenteils, der stachelige Pflanzenteil, das stachelige Pflanzenteil]"));
    suggestor.setPreposition(tags.get(1));  // "an"
    assertThat(suggestor.getSuggestions().toString(), is("[den stacheligen Pflanzenteil, dem stacheligen Pflanzenteile, die stacheligen Pflanzenteile, den stacheligen Pflanzenteilen, dem stacheligen Pflanzenteil, das stachelige Pflanzenteil]"));
  }

  private void assertSuggestion1(String input, String expectedSuggestions) throws IOException {
    assertSuggestion1(input, expectedSuggestions, false);
  }

  private void assertSuggestion1(String input, String expectedSuggestions, boolean filter) throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(input);
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    if (analyzedSentence.getTokensWithoutWhitespace().length != 3) {  // 2 tokens + sentence start token
      fail("Please use 2 tokens (det, noun) as input: " + input);
    }
    AgreementSuggestor2 suggestor = new AgreementSuggestor2(synthesizer, tags.get(1), tags.get(2), null);
    assertThat(suggestor.getSuggestions(filter).toString(), is(expectedSuggestions));
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
