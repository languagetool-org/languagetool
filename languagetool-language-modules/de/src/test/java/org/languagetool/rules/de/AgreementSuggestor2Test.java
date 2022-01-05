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
  public void testAdverbSuggestions() throws IOException {
    assertSuggestion3("ein sehr schönes Tisch", "[ein sehr schöner Tisch]", true);
    assertSuggestion3("eines sehr schönen Tisch", "[einen sehr schönen Tisch, einem sehr schönen Tisch, eines sehr schönen Tischs, eines sehr schönen Tisches]", true);
    assertSuggestion3("der sehr schönen Tischen", "[dem sehr schönen Tischen, den sehr schönen Tischen, der sehr schönen Tische]", true);
  }

  @Test
  public void testSuggestions() throws IOException {
    assertSuggestion1("deine Buch", "[dein Buch, deinem Buch, deine Bücher, deinem Buche, deines Buchs, deines Buches, deiner Bücher, deinen Büchern]");
    assertSuggestion1("dieser Buch", "[dieses Buch, diesem Buch, dies Buch, dieser Bücher, diesem Buche, dieses Buchs, dieses Buches, diese Bücher, diesen Büchern]");
    assertSuggestion1("die Kabels", "[die Kabel, des Kabels, der Kabel, den Kabel, dem Kabel, das Kabel, den Kabeln]");
    assertSuggestion1("die LAN-Kabels", "[die LAN-Kabel, des LAN-Kabels, der LAN-Kabel, den LAN-Kabel, dem LAN-Kabel, das LAN-Kabel, den LAN-Kabeln]");
    assertSuggestion1("mehrere Kabels", "[mehrere Kabel, mehreren Kabeln, mehrerer Kabel]");
    assertSuggestion1("mehrere LAN-Kabels", "[mehrere LAN-Kabel, mehreren LAN-Kabeln, mehrerer LAN-Kabel]");
    assertSuggestion1("mehrere WLAN-LAN-Kabels", "[mehrere WLAN-LAN-Kabel, mehreren WLAN-LAN-Kabeln, mehrerer WLAN-LAN-Kabel]");
    assertSuggestion1("Ihren Verständnis", "[Ihrem Verständnis, Ihr Verständnis, Ihrem Verständnisse, Ihres Verständnisses]");
    assertSuggestion1("des Züchten", "[das Züchten, dem Züchten, des Züchtens]");
    assertSuggestion1("die Kühlschranktest", "[die Kühlschrankteste, die Kühlschranktests, der Kühlschranktest, den Kühlschranktest, " +
      "dem Kühlschranktest, des Kühlschranktests, den Kühlschranktests, der Kühlschrankteste, der Kühlschranktests, " +
      "des Kühlschranktestes, den Kühlschranktesten]");
    assertSuggestion1("die Kühlschrankverarbeitungstest", "[die Kühlschrankverarbeitungsteste, die Kühlschrankverarbeitungstests, " +
      "der Kühlschrankverarbeitungstest, den Kühlschrankverarbeitungstest, dem Kühlschrankverarbeitungstest, " +
      "des Kühlschrankverarbeitungstests, den Kühlschrankverarbeitungstests, der Kühlschrankverarbeitungsteste, " +
      "der Kühlschrankverarbeitungstests, des Kühlschrankverarbeitungstestes, den Kühlschrankverarbeitungstesten]");
    assertSuggestion2("den gleiche Gebiete", "[dem gleichen Gebiete, den gleichen Gebieten, der gleichen Gebiete, " +
      "das gleiche Gebiet, die gleichen Gebiete, dem gleichen Gebiet, des gleichen Gebietes, des gleichen Gebiets]");
    assertSuggestion2("den vorangegangen Versuchen", "[den vorangegangenen Versuchen, das vorangegangene Versuchen, " +
      "dem vorangegangenen Versuchen, den vorangegangenen Versuch, der vorangegangene Versuch, des vorangegangenen Versuches, " +
      "das vorangegangene versuchen, dem vorangegangenen versuchen, des vorangegangenen Versuchens, der vorangegangenen Versuche, " +
      "dem vorangegangenen Versuch, des vorangegangenen Versuchs, des vorangegangenen versuchens, die vorangegangenen Versuche]");
    assertSuggestion1("der Blutflusses", "[des Blutflusses, der Blutfluss, der Blutflüsse, den Blutfluss, dem Blutfluss, den Blutflüssen, die Blutflüsse]");
    assertSuggestion2("ein anstrengenden Tag",
      "[ein anstrengender Tag, ein anstrengendes Tag, einen anstrengenden Tag, einem anstrengenden Tag, eines anstrengenden Tags, eines anstrengenden Tages]");
    //assertSuggestion2("vor allem Teams", "[vor allen Teams]");  // TODO: 'allen' is PRO:IND, 'allem' is PRO:DEM, so we don't map yet between those
  }

  @Test
  public void testSuggestionsWithReplType() throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("gehe zur Mann");
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    AgreementSuggestor2 suggestor = new AgreementSuggestor2(synthesizer, tags.get(2), tags.get(3), AgreementRule.ReplacementType.Zur);
    assertThat(suggestor.getSuggestions().toString(), is("[zum Mann, zu Männern]"));

    analyzedSentence = lt.getAnalyzedSentence("gehe zur kuschelige Ferienwohnung");
    tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    suggestor = new AgreementSuggestor2(synthesizer, tags.get(2), tags.get(3), tags.get(4), AgreementRule.ReplacementType.Zur);
    assertThat(suggestor.getSuggestions().toString(), is("[zur kuscheligen Ferienwohnung, zu kuscheligen Ferienwohnungen]"));
  }

  @Test
  public void testSuggestionsWithReplTypeIns() throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("gehe ins Hauses");
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    AgreementSuggestor2 suggestor = new AgreementSuggestor2(synthesizer, tags.get(2), tags.get(3), AgreementRule.ReplacementType.Ins);
    assertThat(suggestor.getSuggestions().toString(), is("[ins Haus, im Hause, im Haus, in die Häuser, in den Häusern]"));
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
    assertSuggestion1("Der Haus", "[Dem Haus, Das Haus, Der Häuser, Dem Hause, Des Hauses, Die Häuser, Den Häusern]");
    assertSuggestion1("der Haus", "[dem Haus, das Haus, der Häuser, dem Hause, des Hauses, die Häuser, den Häusern]");
    assertSuggestion1("das Haus", "[dem Haus, dem Hause, des Hauses, die Häuser, der Häuser, den Häusern]");
    assertSuggestion1("der Haus", "[dem Haus, das Haus, der Häuser, dem Hause, des Hauses, die Häuser, den Häusern]");
    assertSuggestion1("die Haus", "[das Haus, dem Haus, die Häuser, dem Hause, des Hauses, der Häuser, den Häusern]");
    assertSuggestion1("die Hauses", "[des Hauses, die Häuser, dem Hause, das Haus, dem Haus, der Häuser, den Häusern]");
    assertSuggestion1("die Häusern", "[die Häuser, den Häusern, der Häuser, dem Hause, des Hauses, das Haus, dem Haus]");
    // filtered:
    assertSuggestion1("Der Haus", "[Dem Haus, Das Haus, Der Häuser]", true);
    assertSuggestion1("der Haus", "[dem Haus, das Haus, der Häuser]", true);
    assertSuggestion1("das Haus", "[dem Haus]", true);
    assertSuggestion1("der Haus", "[dem Haus, das Haus, der Häuser]", true);
    assertSuggestion1("die Haus", "[das Haus, dem Haus, die Häuser]", true);
    assertSuggestion1("die Hauses", "[des Hauses, die Häuser]", true);
    assertSuggestion1("die Häusern", "[die Häuser, den Häusern]", true);
  }

  @Test
  public void testDetAdjNounSuggestions() throws IOException {
    assertSuggestion2("die neuen Unterlage", "[die neue Unterlage, die neuen Unterlagen, der neuen Unterlage, den neuen Unterlagen, der neuen Unterlagen]");
    assertSuggestion2("der neue Unterlagen", "[der neuen Unterlagen, der neuen Unterlage, den neuen Unterlagen, die neue Unterlage, die neuen Unterlagen]");
    assertSuggestion2("eine schönes Auto", "[ein schönes Auto, einem schönen Auto, eines schönen Autos]");
    assertSuggestion2("eine schöne Auto", "[ein schönes Auto, einem schönen Auto, eines schönen Autos]");
    assertSuggestion2("ein schöne Auto", "[ein schönes Auto, einem schönen Auto, eines schönen Autos]");
    assertSuggestion2("einen großen Auto", "[einem großen Auto, eines großen Autos, ein großes Auto]");
    assertSuggestion2("der schöne Auto", "[das schöne Auto, dem schönen Auto, der schönen Autos, des schönen Autos, den schönen Autos, die schönen Autos]");
    assertSuggestion2("der kleine Auto", "[das kleine Auto, dem kleinen Auto, der kleinen Autos, des kleinen Autos, den kleinen Autos, die kleinen Autos]");
    assertSuggestion2("der kleiner Auto", "[dem kleinen Auto, der kleinen Autos, das kleine Auto, des kleinen Autos, den kleinen Autos, die kleinen Autos]");
    assertSuggestion2("das stärkste Körperteil", "[der stärkste Körperteil, den stärksten Körperteil, dem stärksten Körperteil, des stärksten Körperteils, dem stärksten Körperteile, des stärksten Körperteiles, die stärksten Körperteile, der stärksten Körperteile, den stärksten Körperteilen]");
    // "benötigten" ist PA2:
    assertSuggestion2("die benötigten Unterlage", "[die benötigte Unterlage, die benötigten Unterlagen, der benötigten Unterlage, den benötigten Unterlagen, der benötigten Unterlagen]");
    assertSuggestion2("eine benötigten Unterlage", "[eine benötigte Unterlage, einer benötigten Unterlage]");
    assertSuggestion2("die voller Verzierungen", "[die vollen Verzierungen, die volle Verzierung, den vollen Verzierungen, der vollen Verzierungen, der vollen Verzierung]"); // evtl. Fehlalarm...
    assertSuggestion2("zu zukünftigen Vorstands", "[]");  // ?
    assertSuggestion2("des südlichen Kontinent", "[den südlichen Kontinent, dem südlichen Kontinent, des südlichen Kontinents, des südlichen Kontinentes, der südliche Kontinent, der südlichen Kontinente, die südlichen Kontinente, den südlichen Kontinenten]");
    assertSuggestion2("die erwartet Entwicklung", "[die erwartete Entwicklung, der erwarteten Entwicklung, die erwarteten Entwicklungen, den erwarteten Entwicklungen, der erwarteten Entwicklungen]");
    assertSuggestion2("die verschieden Ämter", "[die verschiedenen Ämter, der verschiedenen Ämter, den verschiedenen Ämtern, das verschiedene Amt, dem verschiedenen Amte, des verschiedenen Amtes, dem verschiedenen Amt, des verschiedenen Amts]");
    assertSuggestion2("keine richtiger Fahrerin", "[keine richtige Fahrerin, keiner richtigen Fahrerin, keine richtigen Fahrerinnen, keinen richtigen Fahrerinnen, keiner richtigen Fahrerinnen]");
    // GRU, KOM, SUP:
    assertSuggestion2("das schönes Auto", "[das schöne Auto, dem schönen Auto, des schönen Autos, die schönen Autos, den schönen Autos, der schönen Autos]");
    assertSuggestion2("das schöneren Auto", "[das schönere Auto, dem schöneren Auto, des schöneren Autos, die schöneren Autos, den schöneren Autos, der schöneren Autos]");
    assertSuggestion2("das schönstem Auto", "[das schönste Auto, dem schönsten Auto, des schönsten Autos, die schönsten Autos, den schönsten Autos, der schönsten Autos]");
    assertSuggestion2("das schönsten Auto", "[das schönste Auto, dem schönsten Auto, das schöne Auto, des schönsten Autos, die schönsten Autos, " +
      "den schönsten Autos, der schönsten Autos, dem schönen Auto, des schönen Autos, die schönen Autos, den schönen Autos, der schönen Autos]");
    assertSuggestion2("der ikonischen Gebäuden", "[den ikonischen Gebäuden, der ikonischen Gebäude, dem ikonischen Gebäude, des ikonischen Gebäudes, die ikonischen Gebäude, das ikonische Gebäude]");
    assertSuggestion2("der ikonischeren Gebäuden", "[den ikonischeren Gebäuden, der ikonischeren Gebäude, dem ikonischeren Gebäude, des ikonischeren Gebäudes, die ikonischeren Gebäude, das ikonischere Gebäude]");
    assertSuggestion2("der ikonischsten Gebäuden", "[den ikonischsten Gebäuden, der ikonischsten Gebäude, dem ikonischsten Gebäude, " +
      "des ikonischsten Gebäudes, die ikonischsten Gebäude, den ikonischen Gebäuden, der ikonischen Gebäude, das ikonischste Gebäude, " +
      "dem ikonischen Gebäude, des ikonischen Gebäudes, die ikonischen Gebäude, das ikonische Gebäude]");
    assertSuggestion2("den meisten Fälle", "[den meisten Fällen, der meisten Fälle, die meisten Fälle]");
  }

  @Test
  public void testDetNounSuggestionsWithPreposition() throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("für dein Schmuck");
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    AgreementSuggestor2 suggestor = new AgreementSuggestor2(synthesizer, tags.get(2), tags.get(3), null);
    assertThat(suggestor.getSuggestions(false).toString(), is("[deinen Schmuck, deinem Schmuck, deines Schmucks, deines Schmuckes]"));
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
    assertThat(suggestor.getSuggestions().toString(), is("[den stacheligen Pflanzenteil, dem stacheligen Pflanzenteile, " +
      "den stacheligen Pflanzenteilen, der stacheligen Pflanzenteile, die stacheligen Pflanzenteile, dem stacheligen Pflanzenteil, " +
      "des stacheligen Pflanzenteiles, des stacheligen Pflanzenteils, der stachelige Pflanzenteil, das stachelige Pflanzenteil]"));
    suggestor.setPreposition(tags.get(1));  // "an"
    assertThat(suggestor.getSuggestions().toString(), is("[den stacheligen Pflanzenteil, dem stacheligen Pflanzenteile, " +
      "den stacheligen Pflanzenteilen, die stacheligen Pflanzenteile, dem stacheligen Pflanzenteil, das stachelige Pflanzenteil]"));
  }

  @Test
  public void testDetAdjAdjNounSuggestions() throws IOException {
    assertSuggestion3("eine solides strategisches Fundament", "[ein solides strategisches Fundament]", true);
    assertSuggestion3("ein solide strategisches Fundament", "[ein solides strategisches Fundament]", true);
    assertSuggestion3("ein solides strategische Fundament", "[ein solides strategisches Fundament]", true);
    assertSuggestion3("ein solides strategisches Fundamente", "[ein solides strategisches Fundament]", true);
    assertSuggestion3("ein solides strategisches Fundamenten", "[ein solides strategisches Fundament]", true);
    assertSuggestion3("die meisten kommerziellen System", "[die meisten kommerziellen Systeme]", true);
    assertSuggestion3("die meisten kommerzielle Systeme", "[die meisten kommerziellen Systeme]", true);
    assertSuggestion3("die meisten kommerziell Systeme", "[die meisten kommerziellen Systeme]", true);
    assertSuggestion3("die meisten kommerziell System", "[die meisten kommerziellen Systeme]", true);
    assertSuggestion3("Die meisten kommerziellen System", "[Die meisten kommerziellen Systeme]", true);
    assertSuggestion3("der meisten kommerziellen System", "[der meisten kommerziellen Systeme]", true);
    assertSuggestion3("der meisten kommerziellen Systems", "[der meisten kommerziellen Systeme]", true);
    //assertSuggestion3("jedes viertes fremdsprachliche Wurzeln", "[jedes vierte fremdsprachliche Wurzeln]", true);  // TODO
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
  
  private void assertSuggestion3(String input, String expectedSuggestions, boolean filter) throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(input);
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    if (analyzedSentence.getTokensWithoutWhitespace().length != 5) {  // 4 tokens + sentence start token
      fail("Please use 4 tokens (det, adj, adj, noun) as input: " + input);
    }
    AgreementSuggestor2 suggestor = new AgreementSuggestor2(synthesizer, tags.get(1), tags.get(2), tags.get(3), tags.get(4), null);
    assertThat(suggestor.getSuggestions(filter).toString(), is(expectedSuggestions));
  }

}
