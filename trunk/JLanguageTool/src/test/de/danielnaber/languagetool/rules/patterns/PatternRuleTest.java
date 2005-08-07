/* JLanguageTool, a natural language style checker 
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
package de.danielnaber.languagetool.rules.patterns;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.rules.RuleMatch;
import junit.framework.TestCase;

/**
 * @author Daniel Naber
 */
public class PatternRuleTest extends TestCase {

  public void testRule() {
    PatternRule pr;
    RuleMatch[] matches;

    pr = new PatternRule("ID1", Language.ENGLISH, "\"one\"", "test rule");
    matches = pr.match(TestTools.getAnaylzedText("A non-matching sentence."));
    assertEquals(0, matches.length);
    matches = pr.match(TestTools.getAnaylzedText("A matching sentence with one match."));
    assertEquals(1, matches.length);
    matches = pr.match(TestTools.getAnaylzedText("one one and one: three matches"));
    assertEquals(3, matches.length);

    pr = new PatternRule("ID1", Language.ENGLISH, "\"one\" \"two\"", "test rule");
    matches = pr.match(TestTools.getAnaylzedText("this is one not two"));
    assertEquals(0, matches.length);
    matches = pr.match(TestTools.getAnaylzedText("this is two one"));
    assertEquals(0, matches.length);
    matches = pr.match(TestTools.getAnaylzedText("this is one two three"));
    assertEquals(1, matches.length);
    matches = pr.match(TestTools.getAnaylzedText("one two"));
    assertEquals(1, matches.length);
    
    pr = new PatternRule("ID1", Language.ENGLISH, "\"one|foo|xxxx\" \"two\"", "test rule");
    matches = pr.match(TestTools.getAnaylzedText("one foo three"));
    assertEquals(0, matches.length);
    matches = pr.match(TestTools.getAnaylzedText("one two"));
    assertEquals(1, matches.length);
    matches = pr.match(TestTools.getAnaylzedText("foo two"));
    assertEquals(1, matches.length);
    matches = pr.match(TestTools.getAnaylzedText("one foo two"));
    assertEquals(1, matches.length);
    matches = pr.match(TestTools.getAnaylzedText("y x z one two blah foo"));
    assertEquals(1, matches.length);

    pr = new PatternRule("ID1", Language.ENGLISH, "\"one|foo|xxxx\" \"two|yyy\"", "test rule");
    matches = pr.match(TestTools.getAnaylzedText("one, yyy"));
    assertEquals(0, matches.length);
    matches = pr.match(TestTools.getAnaylzedText("one yyy"));
    assertEquals(1, matches.length);
    matches = pr.match(TestTools.getAnaylzedText("xxxx two"));
    assertEquals(1, matches.length);
    matches = pr.match(TestTools.getAnaylzedText("xxxx yyy"));
    assertEquals(1, matches.length);
  }
  
}
