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
import org.languagetool.language.Catalan;
import org.languagetool.language.ValencianCatalan;
import org.languagetool.language.BalearicCatalan;
import org.languagetool.rules.CommaWhitespaceRule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.ca.SimpleReplaceAnglicism;
import org.languagetool.rules.ca.SimpleReplaceMultiwordsRule;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JLanguageToolTest {


  private  Language lang = new Catalan();
  private JLanguageTool tool = new JLanguageTool(lang);


  @Test
  public void testCleanOverlappingErrors() throws IOException {
    List<RuleMatch> matches = tool.check("prosper");
    assertEquals(1, matches.size());
    assertEquals("CA_SIMPLE_REPLACE_BALEARIC_PROSPER", matches.get(0).getRule().getId());

    matches = tool.check("Potser siga el millor");
    assertEquals(1, matches.size());
    assertEquals("POTSER_SIGUI", matches.get(0).getRule().getId());
    
    //ChunkTags
 
    assertEquals("[<S> Ho[ho/PP3NN000] deu[deure/VMIP3S00,GV] haver[haver/VAN00000,haver/_GV_,haver/_perfet,GV] tornat[tornar/VMP00SM0,GV] a[a/SPS00,GV] fer[fer/VMN00000,fer/complement,GV].[</S>./_PUNCT,<P/>]]",
        tool.analyzeText("Ho deu haver tornat a fer.").toString());

    
    assertEquals("[<S> Ho[ho/PP3NN000] he[haver/VAIP1S00,haver/_obligacio,GV] de[de/SPS00,GV] continuar[continuar/VMN00000,continuar/_GV_,GV] fent[fer/VMG00000,fent/_GV_,GV] així[així/RG].[</S>./_PUNCT,<P/>]]",
        tool.analyzeText("Ho he de continuar fent així.").toString());

  }

  @Test
  public void testValecianVariant() throws IOException {
    Language lang = new ValencianCatalan();
    JLanguageTool tool = new JLanguageTool(lang);
    List<RuleMatch> matches = tool.check("Cal usar mètodes d'anàlisi adequats.");
    assertEquals(0, matches.size());

    matches = tool.check("Aquests ganivets no corresponen amb estes forquilles.");
    assertEquals(1, matches.size());
    assertEquals( "aquestes", matches.get(0).getSuggestedReplacements().get(0));

    matches = tool.check("Estes forquilles, aquestos ganivets.");
    assertEquals(1, matches.size());
    assertEquals( "estos", matches.get(0).getSuggestedReplacements().get(0));

    matches = tool.check("Estes forquilles, aquests ganivets.");
    assertEquals(1, matches.size());
    assertEquals( "estos", matches.get(0).getSuggestedReplacements().get(0));

    matches = tool.check("Aqueixes forquilles, eixos ganivets.");
    assertEquals(1, matches.size());
    assertEquals( "aqueixos", matches.get(0).getSuggestedReplacements().get(0));

    matches = tool.check("Eixes forquilles, aqueixos ganivets.");
    assertEquals(1, matches.size());
    assertEquals( "eixos", matches.get(0).getSuggestedReplacements().get(0));

    matches = tool.check("Estos ganivets no corresponen amb estes forquilles.");
    assertEquals(0, matches.size());
    matches = tool.check("Aquests ganivets no corresponen amb aquestes forquilles.");
    assertEquals(0, matches.size());


  }
  
  @Test
  public void testBalearicVariant() throws IOException {
    Language lang = new BalearicCatalan();
    JLanguageTool tool = new JLanguageTool(lang);
    List<RuleMatch> matches = tool.check("Cal usar mètodes d'anàlisi adequats.");
    assertEquals(0, matches.size());
  }
  
  @Test
  public void testAdvancedTypography() throws IOException {
    assertEquals(lang.toAdvancedTypography("És l'\"hora\"!"), "És l’«hora»!");
    assertEquals(lang.toAdvancedTypography("És l''hora'!"), "És l’‘hora’!");
    assertEquals(lang.toAdvancedTypography("És l'«hora»!"), "És l’«hora»!");
    assertEquals(lang.toAdvancedTypography("És l''hora'."), "És l’‘hora’.");
    assertEquals(lang.toAdvancedTypography("Cal evitar el \"'lo' neutre\"."), "Cal evitar el «‘lo’ neutre».");
    assertEquals(lang.toAdvancedTypography("És \"molt 'important'\"."), "És «molt ‘important’».");
    assertEquals(lang.toAdvancedTypography("Si és del v. 'haver'."), "Si és del v.\u00a0‘haver’.");
    assertEquals(lang.toAdvancedTypography("Amb el so de 's'."), "Amb el so de ‘s’.");

    assertEquals(lang.adaptSuggestion("L'IEC"), "L'IEC");
    assertEquals(lang.adaptSuggestion("te estimava"), "t'estimava");
    assertEquals(lang.adaptSuggestion("el Albert"), "l'Albert");
    assertEquals(lang.adaptSuggestion("l'Albert"), "l'Albert");
    assertEquals(lang.adaptSuggestion("l'«Albert»"), "l'«Albert»");
    assertEquals(lang.adaptSuggestion("l’«Albert»"), "l’«Albert»");
    assertEquals(lang.adaptSuggestion("l'\"Albert\""), "l'\"Albert\"");
    assertEquals(lang.adaptSuggestion("m'tancava"), "em tancava");
    assertEquals(lang.adaptSuggestion("s'tancava"), "es tancava");
    assertEquals(lang.adaptSuggestion("l'R+D"), "l'R+D");
    assertEquals(lang.adaptSuggestion("l'FBI"), "l'FBI");

  }

  @Test
  public void testAdaptSuggestions() throws IOException {
    List<RuleMatch> matches = tool.check(
        "Els valencians hem sigut valencians des que Jaume I creà el regne de València i poc a poc es conformà una nova identitat política (que en l'edat mitjana, per exemple, no entrava en contradicció amb la consciència clara que teníem un origen i una llengua comuns amb els catalans).");
    assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[a poc a poc]");

    matches = tool.check("A nivell d'ensenyament superior.");
    assertEquals(matches.get(0).getSuggestedReplacements().toString(),
        "[En l'àmbit d', A escala d', A , En , Pel que fa a , Quant a ]");

  }

  @Test
  public void testMultitokenSpeller() throws IOException {
    assertEquals("[Jacques-Louis David]", lang.getMultitokenSpeller().getSuggestions("Jacques Louis David").toString());
    assertEquals("[Chiang Kai-shek]", lang.getMultitokenSpeller().getSuggestions("Chiang Kaishek").toString());
    assertEquals("[Comédie-Française]", lang.getMultitokenSpeller().getSuggestions("Comédie Français").toString());
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
    assertEquals("[Nikolai Rimski-Kórsakov]", lang.getMultitokenSpeller().getSuggestions("Nicolai Rimski-Kórsakov").toString());
    assertEquals("[Rimski-Kórsakov]", lang.getMultitokenSpeller().getSuggestions("Rimsky-Korsakov").toString());
    assertEquals("[Johann Sebastian Bach]", lang.getMultitokenSpeller().getSuggestions("Johan Sebastián Bach").toString());
    assertEquals("[Johann Sebastian Bach]", lang.getMultitokenSpeller().getSuggestions("Johan Sebastián Bach").toString());
    assertEquals("[Johann Sebastian Bach]", lang.getMultitokenSpeller().getSuggestions("Johann Sebastián Bach").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Plantation Boy").toString());
    assertEquals("[Woody Allen]", lang.getMultitokenSpeller().getSuggestions("Woodie Alen").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Eugenio Granjo").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("Julia García").toString());
    assertEquals("[Deutsche Bank]", lang.getMultitokenSpeller().getSuggestions("Deustche Bank").toString());
    assertEquals("[Dmitri Mendeléiev]", lang.getMultitokenSpeller().getSuggestions("Dimitri Mendeleev").toString());
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
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("A paso").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("A sin").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("A xente").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("A lus").toString());
    assertEquals("[]", lang.getMultitokenSpeller().getSuggestions("A Month").toString());
    assertEquals("[peix espasa]", lang.getMultitokenSpeller().getSuggestions("peis espaba").toString());
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

  @Test
  public void testCommaWhitespaceRule() throws IOException {
    CommaWhitespaceRule rule = new CommaWhitespaceRule(TestTools.getEnglishMessages());

    RuleMatch[] matches = rule.match(tool.getAnalyzedSentence("Sol Picó (\uD83D\uDC0C+\uD83D\uDC1A)"));
    assertEquals(0, matches.length);

    List<RuleMatch> matches1 = tool.check("Continuo veien cada dia gent amb ID baixa ");
    assertEquals("GERUNDI_PERD_T", matches1.get(0).getRule().getId());

//    matches1 = lt.check("Vine canta i balla.");
//    assertEquals("GERUNDI_PERD_T", matches1.get(0).getRule().getId());
  }

  @Test
  public void testReplaceMultiwords() throws IOException {
    SimpleReplaceMultiwordsRule rule = new SimpleReplaceMultiwordsRule(TestTools.getEnglishMessages());
    RuleMatch[] matches = rule.match(tool.getAnalyzedSentence("Les persones membres"));
    assertEquals(1, matches.length);
    assertEquals("Els membres", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(tool.getAnalyzedSentence("LES PERSONES MEMBRES"));
    assertEquals(1, matches.length);
    assertEquals("ELS MEMBRES", matches[0].getSuggestedReplacements().get(0));


  }

  @Test
  public void testReplaceAnglicisms() throws IOException {
    SimpleReplaceAnglicism rule = new SimpleReplaceAnglicism(TestTools.getEnglishMessages());
    RuleMatch[] matches = rule.match(tool.getAnalyzedSentence("que són la revolució física (Bacon, Galileu)"));
    assertEquals(0, matches.length);

    matches = rule.match(tool.getAnalyzedSentence("de E-Commerce"));
    assertEquals(1, matches.length);
  }


}
