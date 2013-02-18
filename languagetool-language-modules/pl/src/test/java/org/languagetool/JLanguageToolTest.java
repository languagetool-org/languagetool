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

import junit.framework.TestCase;
import org.languagetool.language.Polish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JLanguageToolTest extends TestCase {

  public void testPolish() throws IOException {
    final Polish polish = new Polish();
    JLanguageTool tool = new JLanguageTool(polish);
    assertEquals("[PL]", Arrays.toString(polish.getCountryVariants()));
    List<RuleMatch> matches = tool.check("To jest całkowicie prawidłowe zdanie.");
    assertEquals(0, matches.size());
    matches = tool.check("To jest jest problem.");
    assertEquals(1, matches.size());
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
    assertEquals("Disambiguator log: "
            + "\n\nMULTIWORD_CHUNKER: Z[z/prep:gen.inst*] -> Z[z/prep:gen.inst*,Z powodu/<PREP:GEN>*]"
            + "\n\nMULTIWORD_CHUNKER: powodu[powód/subst:sg:gen:m3] -> powodu[powód/subst:sg:gen:m3,Z powodu/</PREP:GEN>]\n",
            sent.getAnnotations());
    sent = tool.getAnalyzedSentence("Nie mamy żadnej ryby.");
    assertEquals("Disambiguator log: "
            + "\n\nNIE_ADAMP:1 Nie[nie/qub*,on/ppron3:pl:acc:f.m2.m3.n.p2.p3:ter:praep*,on/ppron3:sg:acc:n:ter:praep*] -> Nie[nie/qub*]"
            + "\n\nunify_adj_subst:1 żadnej[żaden/adj:sg:dat:f:pos,żaden/adj:sg:gen:f:pos,żaden/adj:sg:loc:f:pos] -> żadnej[żaden/adj:sg:gen:f:pos]" +
            "\n\nunify_adj_subst:1 ryby[ryba/subst:pl:acc:f,ryba/subst:pl:nom:f,ryba/subst:pl:voc:f,ryba/subst:sg:gen:f] -> ryby[ryba/subst:sg:gen:f]\n",
            sent.getAnnotations());
    
  }
  
}
