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

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.languagetool.*;
import org.languagetool.synthesis.Synthesizer;

public class AgreementSuggestorTest {

  private final Language german = Languages.getLanguageForShortCode("de-DE");
  private final Synthesizer synthesizer = german.getSynthesizer();
  private final JLanguageTool lt = new JLanguageTool(german);

  @Test
  public void testSuggestions() throws IOException {
    assertSuggestion("das/der/ART:DEF:NOM:SIN:NEU Haus/Haus/SUB:NOM:SIN:NEU", "[das Haus]");
    assertSuggestion("der/der/ART:DEF:NOM:SIN:MAS Haus/Haus/SUB:NOM:SIN:NEU", "[das Haus]");
    assertSuggestion("die/der/ART:DEF:NOM:PLU:FEM Haus/Haus/SUB:NOM:SIN:NEU", "[das Haus]");
    assertSuggestion("das/der/ART:DEF:NOM:PLU:FEM Häuser/Haus/SUB:NOM:PLU:NEU", "[die Häuser]");
    assertSuggestion("das/der/ART:DEF:NOM:PLU:FEM Häusern/Haus/SUB:DAT:PLU:NEU", "[den Häusern]");
    assertSuggestion("dieser/dies/PRO:DEM:GEN:PLU:NEU:B/S Buch/Buch/SUB:NOM:SIN:NEU", "[dies Buch, dieser Bücher, dieses Buch]");
    assertSuggestion("die/der/PRO:IND:NOM:PLU:NEU:B/S Kabels/Kabel/SUB:GEN:PLU:NEU", "[die Kabel]");
    assertSuggestion("die/der/PRO:IND:NOM:PLU:NEU:B/S LAN-Kabels/LAN-Kabel/SUB:GEN:PLU:NEU", "[die LAN-Kabel]");
    assertSuggestion("mehrere/mehrer/PRO:IND:NOM:PLU:NEU:B/S Kabels/Kabel/SUB:GEN:SIN:MAS", "[mehrere Kabel]");
    assertSuggestion("mehrere/mehrer/PRO:IND:NOM:PLU:NEU:B/S LAN-Kabels/LAN-Kabel/SUB:GEN:SIN:MAS", "[mehrere LAN-Kabel]");
    assertSuggestion("mehrere/mehrer/PRO:IND:NOM:PLU:NEU:B/S WLAN-LAN-Kabels/WLAN-LAN-Kabel/SUB:GEN:SIN:MAS", "[mehrere WLAN-LAN-Kabel]");
    assertSuggestion("Ihren/mein/PRO:POS:AKK:SIN:MAS:BEG Verständnis/Verständnis/SUB:NOM:SIN:NEU", "[Ihr Verständnis]");
    assertSuggestion1("das Haus", "[das Haus]");
    assertSuggestion1("der Haus", "[das Haus, dem Haus, der Häuser]");
    assertSuggestion1("die Haus", "[das Haus, dem Haus, die Häuser]");
  }

  @Test
  public void testDetAdjNounSuggestions() throws IOException {
    assertSuggestion2("eine schönes Auto", "[ein schönes Auto, einem schönen Auto]");
    assertSuggestion2("eine schöne Auto", "[ein schönes Auto, einem schönen Auto]");
    assertSuggestion2("ein schöne Auto", "[ein schönes Auto]");
    assertSuggestion2("einen großen Auto", "[ein großes Auto, einem großen Auto]");
    assertSuggestion2("das schönes Auto", "[das schöne Auto]");
    assertSuggestion2("das schöneren Auto", "[das schönere Auto, dem schöneren Auto]");
    assertSuggestion2("das schöneren Auto", "[das schönere Auto, dem schöneren Auto]");
    assertSuggestion2("das schönstem Auto", "[das schönste Auto, dem schönsten Auto]");
    assertSuggestion2("das schönsten Auto", "[das schönste Auto, dem schönsten Auto]");
    assertSuggestion2("der schöne Auto", "[das schöne Auto, dem schönen Auto]");
    assertSuggestion2("der kleine Auto", "[das kleine Auto, dem kleinen Auto]");
    assertSuggestion2("der kleiner Auto", "[das kleine Auto, das kleinere Auto, dem kleinen Auto, dem kleineren Auto]");
    assertSuggestion2("das stärkste Körperteil", "[den stärksten Körperteil, dem stärksten Körperteil, der stärkste Körperteil]");
    assertSuggestion2("die benötigten Unterlage", "[die benötigte Unterlage, der benötigten Unterlage]");
    assertSuggestion2("die voller Verzierungen", "[die vollen Verzierungen, die volleren Verzierungen, den vollen Verzierungen, den volleren Verzierungen, der vollen Verzierungen, der volleren Verzierungen]"); // evtl. Fehlalarm...
    assertSuggestion2("zu zukünftigen Vorstands", "[]");  // ?
    assertSuggestion2("der ikonischsten Gebäuden", "[den ikonischsten Gebäuden]");  // TODO: "der ikonischsten Gebäude"
    assertSuggestion2("des südlichen Kontinent", "[den südlichen Kontinent, dem südlichen Kontinent, der südliche Kontinent]");  // TODO: "des südlichen Kontinents"
    assertSuggestion2("die erwartet Entwicklung", "[die erwartete Entwicklung, der erwarteten Entwicklung]");
    assertSuggestion2("die verschieden Ämter", "[die verschiedenen Ämter, der verschiedenen Ämter]");
    assertSuggestion2("keine richtiger Fahrerin", "[]");  // TODO
  }

  @Test
  public void testDetNounSuggestionsWithPreposition() throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("für dein Schmuck");
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    AgreementSuggestor suggestor = new AgreementSuggestor(synthesizer, tags.get(2), tags.get(3), null);
    assertThat(suggestor.getSuggestions().toString(), is("[dein Schmuck, deinem Schmuck, deinen Schmuck]"));
    suggestor.setPreposition(tags.get(1));  // "für"
    assertThat(suggestor.getSuggestions().toString(), is("[deinen Schmuck]"));
  }

  @Test
  public void testDetAdjNounSuggestionsWithPreposition() throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("über ein hilfreichen Tipp");
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    AgreementSuggestor suggestor = new AgreementSuggestor(synthesizer, tags.get(2), tags.get(3), tags.get(4), null);
    assertThat(suggestor.getSuggestions().toString(), is("[einen hilfreichen Tipp, einem hilfreichen Tipp, ein hilfreicher Tipp]"));
    suggestor.setPreposition(tags.get(1));  // "über"
    assertThat(suggestor.getSuggestions().toString(), is("[einem hilfreichen Tipp, einen hilfreichen Tipp]"));
  }

  private void assertSuggestion1(String input, String expectedSuggestions) throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(input);
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    if (analyzedSentence.getTokensWithoutWhitespace().length != 3) {  // 2 tokens + sentence start token
      fail("Please use 2 tokens (det, noun) as input: " + input);
    }
    AgreementSuggestor suggestor = new AgreementSuggestor(synthesizer, tags.get(1), tags.get(2), null);
    assertThat(suggestor.getSuggestions().toString(), is(expectedSuggestions));
  }
  
  private void assertSuggestion2(String input, String expectedSuggestions) throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(input);
    List<AnalyzedTokenReadings> tags = Arrays.asList(analyzedSentence.getTokensWithoutWhitespace());
    if (analyzedSentence.getTokensWithoutWhitespace().length != 4) {  // 3 tokens + sentence start token
      fail("Please use 3 tokens (det, adj, noun) as input: " + input);
    }
    AgreementSuggestor suggestor = new AgreementSuggestor(synthesizer, tags.get(1), tags.get(2), tags.get(3), null);
    assertThat(suggestor.getSuggestions().toString(), is(expectedSuggestions));
  }
  
  private void assertSuggestion(String input, String expectedSuggestions) {
    String[] tokens = input.split(" ");
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;
    for (String inputToken : tokens) {
      String[] parts = inputToken.split("/");
      String token = parts[0];
      String lemma = parts[1];
      String posTag = parts[2];
      tokenReadings.add(new AnalyzedTokenReadings(new AnalyzedToken(token, posTag, lemma), pos++));
    }
    if (tokenReadings.size() != 2) {
      throw new RuntimeException("Size of input not yet supported: " + tokenReadings.size());
    }
    AgreementSuggestor suggestor = new AgreementSuggestor(synthesizer, tokenReadings.get(0), tokenReadings.get(1), null);
    List<String> suggestions = suggestor.getSuggestions();
    assertThat(suggestions.toString(), is(expectedSuggestions));
  }
}
