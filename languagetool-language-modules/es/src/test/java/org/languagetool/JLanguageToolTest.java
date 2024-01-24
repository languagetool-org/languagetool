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
import org.languagetool.language.Spanish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JLanguageToolTest {

  @Test
  public void testXMLRules() throws IOException {
    Language lang = new Spanish();
    JLanguageTool tool = new JLanguageTool(lang);
    List<RuleMatch> matches = tool.check("Al cabo de 28 años, el vicealcalde de Busan, Baek Seung Taek, realiza una visita a Badajoz.");
    assertEquals(4, matches.size());
  }

  @Test
  public void testMultitokenSpeller() throws IOException {
    Language lang = new Spanish();
    assertEquals("[Helmut Kohl]", lang.getMultitokenSpeller().getSuggestions("Helmut Khol").toString());
    assertEquals("[Frederik Willem de Klerk]", lang.getMultitokenSpeller().getSuggestions("Fredrik Willem de Klerk").toString());
    assertEquals("[Macaulay Culkin]", lang.getMultitokenSpeller().getSuggestions("Maukalay Culkin").toString());
    assertEquals("[Dmitri Mendeléyev]", lang.getMultitokenSpeller().getSuggestions("Dimitri Mendeléief").toString());
    assertEquals("[Nikolái Rimski-Kórsakov]", lang.getMultitokenSpeller().getSuggestions("Nikoláy Rimski-Kórsakov").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Luis Leante").toString());
    assertEquals("[in vino veritas]", lang.getMultitokenSpeller().getSuggestions("in vinos verita").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Marina Buisan").toString());
    assertEquals("[Homo sapiens]", lang.getMultitokenSpeller().getSuggestions("Homos Sapiens").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Garcia Horta").toString());
    assertEquals("[John Venn]", lang.getMultitokenSpeller().getSuggestions("Jon Benn").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("josue garcia").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Franco more").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("maria Lopez").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("carlos fesi").toString());
    assertEquals("[Johann Sebastian Bach]", lang.getMultitokenSpeller().getSuggestions("Johan Sebastián Bach").toString());
    assertEquals("[Johann Sebastian Bach]", lang.getMultitokenSpeller().getSuggestions("Johan Sebastián Bach").toString());
    assertEquals("[Johann Sebastian Bach]", lang.getMultitokenSpeller().getSuggestions("Johann Sebastián Bach").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Plantation Boy").toString());
    assertEquals("[Woody Allen]", lang.getMultitokenSpeller().getSuggestions("Woodie Alen").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Eugenio Granjo").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Julia García").toString());
    assertEquals("[Deutsche Bank]", lang.getMultitokenSpeller().getSuggestions("Deustche Bank").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Caralp Mariné").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Andrew Cyrille").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Alejandro Varón").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Alejandro Mellado").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Alejandro Erazo").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Alberto Saoner").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("è più").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Josep Maria Jové").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Josep Maria Canudas").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Francisco Javier Dra.").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("the usage of our").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("A sin").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("A xente").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("A lus").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("A Month").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Jean-François Davy").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("finç abui").toString());
    assertEquals("[Led Zeppelin]", lang.getMultitokenSpeller().getSuggestions("Led Zepelin").toString());
    assertEquals("[Led Zeppelin]", lang.getMultitokenSpeller().getSuggestions("Led Sepelin").toString());
    assertEquals("[Marie Curie]", lang.getMultitokenSpeller().getSuggestions("Marie Cuirie").toString());
    assertEquals("[William Byrd]", lang.getMultitokenSpeller().getSuggestions("William Bird").toString());
    assertEquals("[Lluís Llach]", lang.getMultitokenSpeller().getSuggestions("Lluis Llach").toString());
    assertEquals("[University of Texas]", lang.getMultitokenSpeller().getSuggestions("Universiti of Tejas").toString());
    assertEquals("[Cyndi Lauper]", lang.getMultitokenSpeller().getSuggestions("Cindy Lauper").toString());
    assertEquals("[García Márquez]", lang.getMultitokenSpeller().getSuggestions("Garcìa Mraquez").toString());
    assertEquals("[Yuval Noah Harari]", lang.getMultitokenSpeller().getSuggestions("yuval Noha Harari").toString());
    assertEquals(Collections.emptyList(), lang.getMultitokenSpeller().getSuggestions("Frederic Udina"));
    assertEquals(Collections.emptyList(), lang.getMultitokenSpeller().getSuggestions("Josep Maria Piñol"));
    assertEquals("[José María Aznar]", lang.getMultitokenSpeller().getSuggestions("Jose Maria Asnar").toString());
    assertEquals("[José María Aznar]", lang.getMultitokenSpeller().getSuggestions("José María Asnar").toString());

  }

}
