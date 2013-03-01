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
    assertEquals("Disambiguator log: \n" +
            "\n" +
            "MULTIWORD_CHUNKER: Z[z/depr:pl:nom:m2*,z/depr:pl:voc:m2*,z/prep:acc:nwok*,z/prep:gen:nwok*,z/prep:inst:nwok*,z/subst:pl:acc:f*,z/subst:pl:acc:m1*,z/subst:pl:acc:m2*,z/subst:pl:acc:n2*,z/subst:pl:dat:f*,z/subst:pl:dat:m1*,z/subst:pl:dat:m2*,z/subst:pl:dat:n2*,z/subst:pl:gen:f*,z/subst:pl:gen:m1*,z/subst:pl:gen:m2*,z/subst:pl:gen:n2*,z/subst:pl:inst:f*,z/subst:pl:inst:m1*,z/subst:pl:inst:m2*,z/subst:pl:inst:n2*,z/subst:pl:loc:f*,z/subst:pl:loc:m1*,z/subst:pl:loc:m2*,z/subst:pl:loc:n2*,z/subst:pl:nom:f*,z/subst:pl:nom:m1*,z/subst:pl:nom:m2*,z/subst:pl:nom:n2*,z/subst:pl:voc:f*,z/subst:pl:voc:m1*,z/subst:pl:voc:m2*,z/subst:pl:voc:n2*,z/subst:sg:acc:f*,z/subst:sg:acc:m1*,z/subst:sg:acc:m2*,z/subst:sg:acc:m3*,z/subst:sg:acc:n2*,z/subst:sg:dat:f*,z/subst:sg:dat:m1*,z/subst:sg:dat:m2*,z/subst:sg:dat:n2*,z/subst:sg:gen:f*,z/subst:sg:gen:m1*,z/subst:sg:gen:m2*,z/subst:sg:gen:n2*,z/subst:sg:inst:f*,z/subst:sg:inst:m1*,z/subst:sg:inst:m2*,z/subst:sg:inst:n2*,z/subst:sg:loc:f*,z/subst:sg:loc:m1*,z/subst:sg:loc:m2*,z/subst:sg:loc:n2*,z/subst:sg:nom:f*,z/subst:sg:nom:m1*,z/subst:sg:nom:m2*,z/subst:sg:nom:m3*,z/subst:sg:nom:n2*,z/subst:sg:voc:f*,z/subst:sg:voc:m1*,z/subst:sg:voc:m2*,z/subst:sg:voc:n2*] -> Z[z/depr:pl:nom:m2*,z/depr:pl:voc:m2*,z/prep:acc:nwok*,z/prep:gen:nwok*,z/prep:inst:nwok*,z/subst:pl:acc:f*,z/subst:pl:acc:m1*,z/subst:pl:acc:m2*,z/subst:pl:acc:n2*,z/subst:pl:dat:f*,z/subst:pl:dat:m1*,z/subst:pl:dat:m2*,z/subst:pl:dat:n2*,z/subst:pl:gen:f*,z/subst:pl:gen:m1*,z/subst:pl:gen:m2*,z/subst:pl:gen:n2*,z/subst:pl:inst:f*,z/subst:pl:inst:m1*,z/subst:pl:inst:m2*,z/subst:pl:inst:n2*,z/subst:pl:loc:f*,z/subst:pl:loc:m1*,z/subst:pl:loc:m2*,z/subst:pl:loc:n2*,z/subst:pl:nom:f*,z/subst:pl:nom:m1*,z/subst:pl:nom:m2*,z/subst:pl:nom:n2*,z/subst:pl:voc:f*,z/subst:pl:voc:m1*,z/subst:pl:voc:m2*,z/subst:pl:voc:n2*,z/subst:sg:acc:f*,z/subst:sg:acc:m1*,z/subst:sg:acc:m2*,z/subst:sg:acc:m3*,z/subst:sg:acc:n2*,z/subst:sg:dat:f*,z/subst:sg:dat:m1*,z/subst:sg:dat:m2*,z/subst:sg:dat:n2*,z/subst:sg:gen:f*,z/subst:sg:gen:m1*,z/subst:sg:gen:m2*,z/subst:sg:gen:n2*,z/subst:sg:inst:f*,z/subst:sg:inst:m1*,z/subst:sg:inst:m2*,z/subst:sg:inst:n2*,z/subst:sg:loc:f*,z/subst:sg:loc:m1*,z/subst:sg:loc:m2*,z/subst:sg:loc:n2*,z/subst:sg:nom:f*,z/subst:sg:nom:m1*,z/subst:sg:nom:m2*,z/subst:sg:nom:m3*,z/subst:sg:nom:n2*,z/subst:sg:voc:f*,z/subst:sg:voc:m1*,z/subst:sg:voc:m2*,z/subst:sg:voc:n2*,Z powodu/<PREP:GEN>*]\n" +
            "AZ:1 Z[z/depr:pl:nom:m2*,z/depr:pl:voc:m2*,z/prep:acc:nwok*,z/prep:gen:nwok*,z/prep:inst:nwok*,z/subst:pl:acc:f*,z/subst:pl:acc:m1*,z/subst:pl:acc:m2*,z/subst:pl:acc:n2*,z/subst:pl:dat:f*,z/subst:pl:dat:m1*,z/subst:pl:dat:m2*,z/subst:pl:dat:n2*,z/subst:pl:gen:f*,z/subst:pl:gen:m1*,z/subst:pl:gen:m2*,z/subst:pl:gen:n2*,z/subst:pl:inst:f*,z/subst:pl:inst:m1*,z/subst:pl:inst:m2*,z/subst:pl:inst:n2*,z/subst:pl:loc:f*,z/subst:pl:loc:m1*,z/subst:pl:loc:m2*,z/subst:pl:loc:n2*,z/subst:pl:nom:f*,z/subst:pl:nom:m1*,z/subst:pl:nom:m2*,z/subst:pl:nom:n2*,z/subst:pl:voc:f*,z/subst:pl:voc:m1*,z/subst:pl:voc:m2*,z/subst:pl:voc:n2*,z/subst:sg:acc:f*,z/subst:sg:acc:m1*,z/subst:sg:acc:m2*,z/subst:sg:acc:m3*,z/subst:sg:acc:n2*,z/subst:sg:dat:f*,z/subst:sg:dat:m1*,z/subst:sg:dat:m2*,z/subst:sg:dat:n2*,z/subst:sg:gen:f*,z/subst:sg:gen:m1*,z/subst:sg:gen:m2*,z/subst:sg:gen:n2*,z/subst:sg:inst:f*,z/subst:sg:inst:m1*,z/subst:sg:inst:m2*,z/subst:sg:inst:n2*,z/subst:sg:loc:f*,z/subst:sg:loc:m1*,z/subst:sg:loc:m2*,z/subst:sg:loc:n2*,z/subst:sg:nom:f*,z/subst:sg:nom:m1*,z/subst:sg:nom:m2*,z/subst:sg:nom:m3*,z/subst:sg:nom:n2*,z/subst:sg:voc:f*,z/subst:sg:voc:m1*,z/subst:sg:voc:m2*,z/subst:sg:voc:n2*,Z powodu/<PREP:GEN>*] -> Z[z/depr:pl:nom:m2*,z/depr:pl:voc:m2*,z/prep:acc:nwok*,z/prep:gen:nwok*,z/prep:inst:nwok*,Z powodu/<PREP:GEN>*]\n" +
            "\n" +
            "MULTIWORD_CHUNKER: powodu[powód/subst:sg:gen:m3] -> powodu[powód/subst:sg:gen:m3,Z powodu/</PREP:GEN>]\n",
            sent.getAnnotations());
       
  }
  
}
