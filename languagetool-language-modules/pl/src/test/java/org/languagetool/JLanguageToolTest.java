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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.languagetool.language.Polish;
import org.languagetool.rules.RuleMatch;

public class JLanguageToolTest extends TestCase {

  public void testPolish() throws IOException {
    final Polish polish = new Polish();
    JLanguageTool tool = new JLanguageTool(polish);
    assertEquals("[PL]", Arrays.toString(polish.getCountries()));
    List<RuleMatch> matches = tool.check("To jest całkowicie prawidłowe zdanie.");
    assertEquals(0, matches.size());
    matches = tool.check("To jest jest problem.");
    assertEquals(1, matches.size());
    //no error thanks to disambiguation
    assertEquals(0, tool.check("Mają one niemałe znaczenie.").size());
    //with immunization
    assertEquals(0, tool.check("A teraz każcie mi dać jaki bądź posiłek.").size());
    //this rule is by default off
    matches = tool.check("Był on bowiem pięknym strzelcem bowiem.");
    assertEquals(0, matches.size());
    tool.enableDefaultOffRule("PL_WORD_REPEAT");
    matches = tool.check("Był on bowiem pięknym strzelcem bowiem.");
    assertEquals(1, matches.size());
    tool.activateDefaultPatternRules();
    matches = tool.check("Premier drapie się w ucho co i rusz.");
    assertEquals(1, matches.size());
    // Polish rule has no effect with English error but will get spelling activated:
    matches = tool.check("I can give you more a detailed description");
    assertEquals(6, matches.size());
    tool.setListUnknownWords(true);
    matches = tool.check("This is not a Polish text.");
    assertEquals(3, matches.size());
    assertEquals("[Polish, This, is, text]", tool.getUnknownWords().toString());
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
    tool.activateDefaultPatternRules();
    matches = tool.check("To jest tekst.\nTest 1. To jest linia w której nie ma przecinka.");
    assertEquals(17, matches.get(0).getColumn());
    //with a space...
    matches = tool.check("To jest tekst. \nTest 1. To jest linia w której nie ma przecinka.");
    assertEquals(17, matches.get(0).getColumn());
    matches = tool.check("To jest tekst. To jest linia w której nie ma przecinka.");
    assertEquals(24, matches.get(0).getColumn());

    //and let's test other feats
    AnalyzedSentence sent = tool.getAnalyzedSentence("Z powodu pogody dobre buty są wskazane.");
    assertEquals("Disambiguator log: \n\n"+
        "prep_verb:2 Z[z/prep:acc:nwok*,z/prep:gen:nwok*,z/prep:inst:nwok*] -> Z[z/prep:gen:nwok*]\n"+
        "PREP_SUBST:1 Z[z/prep:gen:nwok*] -> Z[z/prep:gen:nwok*]\n"+
        "PREP_SUBST_2:1 Z[z/prep:gen:nwok*] -> Z[z/prep:gen:nwok*]\n"+
        "MULTIWORD_CHUNKER: Z[z/prep:gen:nwok*] -> Z[z/prep:gen:nwok*,Z powodu/<PREP:GEN>*]\n\n" +

        "prep_verb:2 powodu[powód/subst:sg:gen:m3] -> powodu[powód/subst:sg:gen:m3]\n"+
        "PREP_SUBST:1 powodu[powód/subst:sg:gen:m3] -> powodu[powód/subst:sg:gen:m3]\n"+
        "PREP_SUBST_2:1 powodu[powód/subst:sg:gen:m3] -> powodu[powód/subst:sg:gen:m3]\n"+

        "MULTIWORD_CHUNKER: powodu[powód/subst:sg:gen:m3] -> powodu[powód/subst:sg:gen:m3,Z powodu/</PREP:GEN>]\n",
        sent.getAnnotations());

  }

}
