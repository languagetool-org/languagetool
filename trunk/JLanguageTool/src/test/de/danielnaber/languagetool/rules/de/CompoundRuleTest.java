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
package de.danielnaber.languagetool.rules.de;

import java.io.IOException;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.CompoundRuleTestAbs;

/**
 * @author Daniel Naber
 */
public class CompoundRuleTest extends CompoundRuleTestAbs {

  protected void setUp() throws Exception {
    super.setUp();
    langTool = new JLanguageTool(Language.GERMAN);
    rule = new CompoundRule(null);
  }
  
  public void testRule() throws IOException {
    // correct sentences:
    check(0, "Eine tolle CD-ROM");
    check(0, "Eine tolle CD-ROM.");
    check(0, "Ein toller CD-ROM-Test.");
    check(0, "Systemadministrator");
    check(0, "System-Administrator");
    check(0, "Eine Million Dollar");
    check(0, "Das System des Administrators");
    check(0, "Nur im Stand-by-Betrieb");
    check(0, "Start, Ziel, Sieg");
    check(0, "Roll-on-roll-off-Schiff");
    // incorrect sentences:
    check(1, "System Administrator", new String[]{"System-Administrator", "Systemadministrator"});
    check(1, "bla bla bla bla bla System Administrator bla bla bla bla bla");
    check(1, "System Administrator blubb");
    check(1, "Der System Administrator");
    check(1, "Der dumme System Administrator");
    check(1, "CD ROM", new String[]{"CD-ROM"});
    check(1, "Nur im Stand by Betrieb", new String[]{"Stand-by-Betrieb"});
    check(1, "Ein echter Start Ziel Sieg", new String[]{"Start-Ziel-Sieg"});
    check(1, "Ein echter Start Ziel Sieg.");
    check(1, "Ein Start Ziel Sieg");
    check(1, "Start Ziel Sieg");
    check(1, "Start Ziel Sieg!");
    check(2, "Der dumme System Administrator legt die CD ROM");
    check(2, "Der dumme System Administrator legt die CD ROM.");
    check(2, "Der dumme System Administrator legt die CD ROM ein blah");
    check(2, "System Administrator CD ROM");
    //FIXME: suggestions / longest match
    //check(1, "Roll on roll off Schiff", new String[]{"Roll-on-roll-off-Schiff"});
    check(1, "Spin off");
    // no hyphen suggestion for some words:
    check(1, "Das ist Haar sträubend", new String[]{"Haarsträubend"});
    // Only hyphen suggestion for some words:
    check(1, "Reality TV", new String[]{"Reality-TV"});
    check(1, "Spin off", new String[]{"Spin-off"});
    // also accept incorrect upper/lowercase spelling:
    check(1, "Spin Off", new String[]{"Spin-Off"});
    check(1, "CW Wert", new String[]{"CW-Wert"});
    // also detect an error if only some of the hyphens are missing:
    check(1, "Roll-on-roll-off Schiff", new String[]{"Roll-on-roll-off-Schiff"});
    check(1, "E-Mail Adressen", new String[]{"E-Mail-Adressen"});
    // first part is a single character:
    check(0, "x-mal");
    check(1, "x mal", new String[]{"x-mal"});
    check(0, "y-Achse");
    check(1, "y Achse", new String[]{"y-Achse"});
  }
  
}
