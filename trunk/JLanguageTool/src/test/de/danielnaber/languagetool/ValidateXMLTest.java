/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import de.danielnaber.languagetool.rules.patterns.PatternRuleTest;

import junit.framework.TestCase;

public class ValidateXMLTest extends TestCase {

  public void testPatternFile() throws IOException {
    XMLValidator validator = new XMLValidator();
    for (int i = 0; i < Language.LANGUAGES.length; i++) {
      Language lang = Language.LANGUAGES[i];
      String grammarFile = "/rules/" + lang.getShortName() + "/grammar.xml";
      validator.validate(grammarFile, "/rules/rules.dtd", "rules");
    }
  }

  public void testFalseFriendsXML() throws IOException {
    XMLValidator validator = new XMLValidator();
    validator.validate("/rules/false-friends.xml", "/rules/false-friends.dtd", "rules");
  }

  public void testDisambiguationRuleFile() throws IOException {
    XMLValidator validator = new XMLValidator();
    //for (int i = 0; i < Language.LANGUAGES.length; i++) {
    //  Language lang = Language.LANGUAGES[i];
    Language lang = Language.FRENCH;
    String grammarFile = "/resource/" + lang.getShortName() + "/disambiguation.xml";
    validator.validate(grammarFile, "/resource/disambiguation.dtd", "rules");
    lang = Language.ENGLISH;
    grammarFile = "/resource/" + lang.getShortName() + "/disambiguation.xml";
    validator.validate(grammarFile, "/resource/disambiguation.dtd", "rules");
    lang = Language.DUTCH;
    grammarFile = "/resource/" + lang.getShortName() + "/disambiguation.xml";
    validator.validate(grammarFile, "/resource/disambiguation.dtd", "rules");
    lang = Language.POLISH;
    grammarFile = "/resource/" + lang.getShortName() + "/disambiguation.xml";
    validator.validate(grammarFile, "/resource/disambiguation.dtd", "rules");
    // }
  }

  /**
   * Validate XML files, as a help for people developing rules that are not programmers.
   */
  public static void main(final String[] args) throws IOException {
    final ValidateXMLTest prt = new ValidateXMLTest();
    System.out.println("Validating XML grammar files ...");  
    prt.testPatternFile();
    prt.testFalseFriendsXML();
    prt.testDisambiguationRuleFile();
    System.out.println("Validation tests successful.");
  }
}
