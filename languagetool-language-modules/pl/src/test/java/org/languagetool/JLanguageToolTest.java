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
import org.languagetool.language.Polish;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JLanguageToolTest {

  @Test
  public void testPolish() throws IOException {
    final Polish noXmlRulesPolish = new Polish() {
      @Override
      public List<AbstractPatternRule> getPatternRules() {
        return Collections.emptyList();
      }
    };
    final Polish polish = new Polish();
    JLanguageTool tool = new JLanguageTool(new Polish());
    JLanguageTool noRulesTool = new JLanguageTool(noXmlRulesPolish);
    assertEquals("[PL]", Arrays.toString(polish.getCountries()));
    List<RuleMatch> matches = noRulesTool.check("To jest całkowicie prawidłowe zdanie.");
    assertEquals(0, matches.size());
    matches = noRulesTool.check("To jest jest problem.");
    assertEquals(1, matches.size());
    //no error thanks to disambiguation
    assertEquals(0, noRulesTool.check("Mają one niemałe znaczenie.").size());
    assertEquals(0, noRulesTool.check("Często wystarczy obrócić na wspak wyroki świata, aby trafnie osądzić jakąś osobę.").size());
    //with immunization
    assertEquals(0, noRulesTool.check("A teraz każcie mi dać jaki bądź posiłek.").size());
    assertEquals(0, noRulesTool.check("Kiedym wóz zobaczył, byłbym przysiągł, że wielka przygoda mnie czeka.").size());
    //with antipatterns: "wymaluj" in "wypisz wymaluj" is immunized locally for punctuation mistakes,
    //so it should get no match
    assertEquals(0, noRulesTool.check("Jurek wygląda wypisz wymaluj babcia.").size());
    //but it should get a match with word repetitions:
    assertEquals(1, noRulesTool.check("Jurek wygląda wypisz wypisz wymaluj babcia.").size());
    assertEquals(1, noRulesTool.check("Jurek wygląda wypisz wymaluj wymaluj babcia.").size());
    //check for a weird unification bug:
    assertEquals(0, noRulesTool.check("Zawarł w niej, oprócz swojej twórczości, wybrane epigramaty czterdziestu ośmiu innych greckich poetów i poetek.").size());
    //checking on pattern rules now...
    //now this should be immunized:
    assertEquals(0, tool.check("Nudne brednie tak zamąciły głowę chłopu, że klął na czym ziemia stoi, zmuszonym będąc słuchać tego wszystkiego.").size());
    //but this "chcąc, nie chcąc" immunized only by an antipattern
    assertEquals(1, tool.check("Chcąc, nie chcąc zjadłem pstrąga.").size());
    //this rule is by default off
    matches = tool.check("Był on bowiem pięknym strzelcem bowiem.");
    assertEquals(0, matches.size());
    tool.enableRule("PL_WORD_REPEAT");
    matches = tool.check("Był on bowiem pięknym strzelcem bowiem.");
    assertEquals(1, matches.size());
    matches = tool.check("Premier drapie się w ucho co i rusz.");
    assertEquals(1, matches.size());
    // Polish rule has no effect with English error but will get spelling activated:
    matches = tool.check("I can give you more a detailed description");
    assertEquals(6, matches.size());
    tool.setListUnknownWords(true);
    matches = tool.check("This is not a Polish text.");
    assertEquals(3, matches.size());
    assertEquals("[., Polish, This, is, text]", tool.getUnknownWords().toString());
    //check positions relative to sentence ends
    matches = tool.check("To jest tekst.\nTest 1. To jest linia w której nie ma przecinka.");
    assertEquals(17, matches.get(0).getColumn());
    //with a space...
    matches = tool.check("To jest tekst. \nTest 1. To jest linia w której nie ma przecinka.");
    assertEquals(16, matches.get(0).getColumn());
    matches = tool.check("To jest tekst. Test 1. To jest linia w której nie ma przecinka.");
    assertEquals(32, matches.get(0).getColumn());
    //recheck with the -b mode...
    polish.getSentenceTokenizer().setSingleLineBreaksMarksParagraph(true);
    tool = new JLanguageTool(polish);
    matches = tool.check("To jest tekst.\nTest 1. To jest linia w której nie ma przecinka.");
    assertEquals(17, matches.get(0).getColumn());
    //with a space...
    matches = tool.check("To jest tekst. \nTest 1. To jest linia w której nie ma przecinka.");
    assertEquals(17, matches.get(0).getColumn());
    matches = tool.check("To jest tekst. To jest linia w której nie ma przecinka.");
    assertEquals(24, matches.get(0).getColumn());

    //and let's test other feats
    AnalyzedSentence sent = tool.getAnalyzedSentence("Z powodu pogody dobre buty są wskazane.");
    assertEquals("Disambiguator log: \n" +
            "\n" +
            "prep_verb[2]: Z[z/prep:acc:nwok*,z/prep:gen:nwok*,z/prep:inst:nwok*] -> Z[z/prep:gen:nwok*]\n" +
            "PREP_SUBST[1]: Z[z/prep:gen:nwok*] -> Z[z/prep:gen:nwok*]\n" +
            "PREP_SUBST_2[1]: Z[z/prep:gen:nwok*] -> Z[z/prep:gen:nwok*]\n" +
            "MULTIWORD_CHUNKER: Z[z/prep:gen:nwok*] -> Z[z/prep:gen:nwok*,Z powodu/<PREP:GEN>*]\n" +
            "\n" +
            "prep_verb[2]: powodu[powód/subst:sg:gen:m3] -> powodu[powód/subst:sg:gen:m3]\n" +
            "PREP_SUBST[1]: powodu[powód/subst:sg:gen:m3] -> powodu[powód/subst:sg:gen:m3]\n" +
            "PREP_SUBST_2[1]: powodu[powód/subst:sg:gen:m3] -> powodu[powód/subst:sg:gen:m3]\n" +
            "MULTIWORD_CHUNKER: powodu[powód/subst:sg:gen:m3] -> powodu[powód/subst:sg:gen:m3,Z powodu/</PREP:GEN>]\n" +
            "\n" +
            "PREP_SUBST[17]: pogody[pogoda/subst:pl:acc:f,pogoda/subst:pl:nom:f,pogoda/subst:pl:voc:f,pogoda/subst:sg:gen:f] -> pogody[pogoda/subst:sg:gen:f]\n" +
            "\n" +
            "dobry_adj[1]: dobre[dobre/subst:pl:acc:n2,dobre/subst:pl:nom:n2,dobre/subst:pl:voc:n2,dobre/subst:sg:acc:n2,dobre/subst:sg:nom:n2,dobre/subst:sg:voc:n2,dobry/adj:pl:acc:m2.m3.f.n1.n2.p2.p3:pos,dobry/adj:pl:nom.voc:m2.m3.f.n1.n2.p2.p3:pos,dobry/adj:sg:acc:n1.n2:pos,dobry/adj:sg:nom.voc:n1.n2:pos,dobry/depr:pl:nom:m2,dobry/depr:pl:voc:m2,dobry/subst:pl:acc:m3,dobry/subst:pl:nom:m3,dobry/subst:pl:voc:m3] -> dobre[dobry/adj:pl:acc:m2.m3.f.n1.n2.p2.p3:pos,dobry/adj:pl:nom.voc:m2.m3.f.n1.n2.p2.p3:pos]\n" +
            "unify_adj_subst[2]: dobre[dobry/adj:pl:acc:m2.m3.f.n1.n2.p2.p3:pos,dobry/adj:pl:nom.voc:m2.m3.f.n1.n2.p2.p3:pos] -> dobre[dobry/adj:pl:nom.voc:m2.m3.f.n1.n2.p2.p3:pos]\n" +
            "\n" +
            "dobry_adj[1]: buty[but/subst:pl:acc:m2,but/subst:pl:acc:m3,but/subst:pl:nom:m2,but/subst:pl:nom:m3,but/subst:pl:voc:m2,but/subst:pl:voc:m3,buta/subst:pl:acc:f,buta/subst:pl:nom:f,buta/subst:pl:voc:f,buta/subst:sg:gen:f] -> buty[but/subst:pl:acc:m2,but/subst:pl:acc:m3,but/subst:pl:nom:m2,but/subst:pl:nom:m3,but/subst:pl:voc:m2,but/subst:pl:voc:m3,buta/subst:pl:acc:f,buta/subst:pl:nom:f,buta/subst:pl:voc:f]\n" +
            "buty[1]: buty[but/subst:pl:acc:m2,but/subst:pl:acc:m3,but/subst:pl:nom:m2,but/subst:pl:nom:m3,but/subst:pl:voc:m2,but/subst:pl:voc:m3,buta/subst:pl:acc:f,buta/subst:pl:nom:f,buta/subst:pl:voc:f] -> buty[but/subst:pl:acc:m2,but/subst:pl:acc:m3,but/subst:pl:nom:m2,but/subst:pl:nom:m3,but/subst:pl:voc:m2,but/subst:pl:voc:m3]\n" +
            "nom_jest_nom[1]: buty[but/subst:pl:acc:m2,but/subst:pl:acc:m3,but/subst:pl:nom:m2,but/subst:pl:nom:m3,but/subst:pl:voc:m2,but/subst:pl:voc:m3] -> buty[but/subst:pl:nom:m2,but/subst:pl:nom:m3]\n" +
            "unify_adj_subst[2]: buty[but/subst:pl:nom:m2,but/subst:pl:nom:m3] -> buty[but/subst:pl:nom:m2,but/subst:pl:nom:m3]\n" +
            "SUBST_NOM_VOC_VERB[6]: buty[but/subst:pl:nom:m2,but/subst:pl:nom:m3] -> buty[but/subst:pl:nom:m2,but/subst:pl:nom:m3]\n" +
            "\n" +
            "ppas_jest[1]: są[być/verb:fin:pl:ter:imperf:nonrefl] -> są[być/verb:fin:pl:ter:imperf:nonrefl]\n" +
            "nom_jest_nom[1]: są[być/verb:fin:pl:ter:imperf:nonrefl] -> są[być/verb:fin:pl:ter:imperf:nonrefl]\n" +
            "SUBST_NOM_VOC_VERB[6]: są[być/verb:fin:pl:ter:imperf:nonrefl] -> są[być/verb:fin:pl:ter:imperf:nonrefl]\n" +
            "BYC_ADJ_ACC_NOM[1]: są[być/verb:fin:pl:ter:imperf:nonrefl] -> są[być/verb:fin:pl:ter:imperf:nonrefl]\n" +
            "\n" +
            "ppas_jest[1]: wskazane[wskazany/adj:pl:acc:m2.m3.f.n1.n2.p2.p3:pos,wskazany/adj:pl:nom.voc:m2.m3.f.n1.n2.p2.p3:pos,wskazany/adj:sg:acc:n1.n2:pos,wskazany/adj:sg:nom.voc:n1.n2:pos,wskazać/ppas:pl:nom.acc.voc:m2.m3.f.n1.n2.p2.p3:perf:aff,wskazać/ppas:sg:nom.acc.voc:n1.n2:perf:aff] -> wskazane[wskazać/ppas:pl:nom.acc.voc:m2.m3.f.n1.n2.p2.p3:perf:aff]\n" +
            "nom_jest_nom[1]: wskazane[wskazać/ppas:pl:nom.acc.voc:m2.m3.f.n1.n2.p2.p3:perf:aff] -> wskazane[wskazać/ppas:pl:nom.acc.voc:m2.m3.f.n1.n2.p2.p3:perf:aff]\n" +
            "BYC_ADJ_ACC_NOM[1]: wskazane[wskazać/ppas:pl:nom.acc.voc:m2.m3.f.n1.n2.p2.p3:perf:aff] -> wskazane[wskazać/ppas:pl:nom.acc.voc:m2.m3.f.n1.n2.p2.p3:perf:aff]\n",
        sent.getAnnotations());

  }

}
