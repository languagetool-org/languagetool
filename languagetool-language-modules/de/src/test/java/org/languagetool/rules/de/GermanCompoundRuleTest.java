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
package org.languagetool.rules.de;

import java.io.IOException;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.AbstractCompoundRuleTest;

public class GermanCompoundRuleTest extends AbstractCompoundRuleTest {

  @Test
  public void testRule() throws IOException {
    lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    rule = new GermanCompoundRule(TestTools.getMessages("de"), Languages.getLanguageForShortCode("de-DE"), null);
    //testAllCompounds();
    runTests();
    rule = new SwissCompoundRule(TestTools.getMessages("de"), Languages.getLanguageForShortCode("de-DE"), null);
    runTests();
    
  }
      
  private void runTests() throws IOException {

    // correct sentences:
    check(0, "Eine tolle CD-ROM");
    check(0, "Eine tolle CD-ROM.");
    check(0, "Ein toller CD-ROM-Test.");
    check(0, "Systemadministrator");
    check(0, "Eine Million Dollar");
    check(0, "Das System des Administrators");
    check(0, "Nur im Stand-by-Betrieb");
    check(0, "Start, Ziel, Sieg");
    check(0, "Roll-on-roll-off-Schiff");
    check(0, "Halswirbelsäule");
    check(0, "Castrop-Rauxel");
    check(0, "Hals-Wirbel-Säule");   // doubtful whether this is correct...
    // incorrect sentences:
    check(1, "System Administrator", new String[]{"Systemadministrator"});
    check(1, "System-Administrator");
    check(1, "bla bla bla bla bla System Administrator bla bla bla bla bla");
    check(1, "System Administrator blubb");
    check(1, "Der System Administrator");
    check(1, "Der dumme System Administrator");
    check(1, "CD ROM", "CD-ROM");
    check(1, "Nur im Stand by Betrieb", "Stand-by-Betrieb");
    check(1, "Ein echter Start Ziel Sieg", "Start-Ziel-Sieg");
    check(1, "Ein echter Start Ziel Sieg.");
    check(1, "Ein Start Ziel Sieg");
    check(1, "Start Ziel Sieg");
    check(1, "Start Ziel Sieg!");
    check(1, "Doppler Effekt");
    check(2, "Der dumme System Administrator legt die CD ROM");
    check(2, "Der dumme System Administrator legt die CD ROM.");
    check(2, "Der dumme System Administrator legt die CD ROM ein blah");
    check(2, "System Administrator CD ROM");
//    check(2, "Hals Wirbel Säule");
    check(1, "Und herum zu knobeln können.", "herumzuknobeln");
    check(1, "Castrop Rauxel", "Castrop-Rauxel");
    // suggestion not on dictionary
    //check(1, "Roll on roll off Schiff", new String[]{"Roll-on-roll-off-Schiff"});
    check(1, "Spin off");
    // no hyphen suggestion for some words:
    check(1, "Das ist Haar sträubend", "Haarsträubend");
    // Only hyphen suggestion for some words:
    // suggestion not on dictionary
    //check(1, "Reality TV", "Reality-TV");
    check(1, "Spin off", "Spin-off");
    // also accept incorrect upper/lowercase spelling:
//    check(1, "Spin Off", new String[]{"Spin-Off"});
//    check(1, "CW Wert", new String[]{"CW-Wert"});
    // also detect an error if only some of the hyphens are missing:
    //check(1, "Roll-on-roll-off Schiff", "Roll-on-roll-off-Schiff");
    check(1, "E-Mail Adressen", "E-Mail-Adressen");
    check(1, "Geräte Wahl", "Geräte-Wahl", "Gerätewahl");
    // first part is a single character:
    check(0, "x-mal");
    check(1, "x mal", "x-mal");
    check(0, "y-Achse");
    check(1, "y Achse", "y-Achse");
    
    // "Blu-ray-Brenner*"
    check(0, "Blu-ray-Brenner");
    check(1, "Blu ray Brenner", "Blu-ray-Brenner");
    // "nach-denken+"
    check(0, "Ich muss nachdenken");
    check(1, "Ich muss  nach denken", "nachdenken");
    // "Afghanistan-Krieg$"
    check(0, "Afghanistankkrieg");
    check(0, "Afghanistank-Krieg");
    check(1, "Afghanistan Krieg", "Afghanistan-Krieg", "Afghanistankrieg");
    // "Aggregat-Zustand?"
    check(0, "Aggregatzustand");
    //check(0, "Aggregat-Zustand");
    check(1, "Aggregat-Zustand", "Aggregatzustand");
    check(1, "Aggregat Zustand", "Aggregatzustand");
    check(1, "Billard Kugel", "Billardkugel");
  }
  
}
