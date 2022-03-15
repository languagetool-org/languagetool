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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.language.GermanyGerman;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

public class JLanguageToolTest {

  @Test
  public void testGerman() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de"));
    Assertions.assertEquals(0, lt.check("Ein Test, der keine Fehler geben sollte.").size());
    Assertions.assertEquals(1, lt.check("Ein Test Test, der Fehler geben sollte.").size());
    lt.setListUnknownWords(true);
    // no spelling mistakes as we have not created a variant:
    if (Premium.isPremiumVersion()) {
      Assertions.assertEquals(1, lt.check("I can give you more a detailed description").size());
    } else {
      Assertions.assertEquals(0, lt.check("I can give you more a detailed description").size());
    }
    //test unknown words listing
    Assertions.assertEquals("[I, can, description, detailed, give, more, you]", lt.getUnknownWords().toString());    
  }

  @Test
  public void testGermanyGerman() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    Assertions.assertEquals(0, lt.check("Ein Test, der keine Fehler geben sollte.").size());
    Assertions.assertEquals(1, lt.check("Ein Test Test, der Fehler geben sollte.").size());
    lt.setListUnknownWords(true);
    // German rule has no effect with English error, but they are spelling mistakes:
    if (Premium.isPremiumVersion()) {
      Assertions.assertEquals(7, lt.check("I can give you more a detailed description").size());
    } else {
      Assertions.assertEquals(6, lt.check("I can give you more a detailed description").size());
    }
    //test unknown words listing
    Assertions.assertEquals("[I, can, description, detailed, give, more, you]", lt.getUnknownWords().toString());
  }

  @Test
  public void testPositionsWithGerman() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    List<RuleMatch> matches = lt.check("Stundenkilometer");
    Assertions.assertEquals(1, matches.size());
    RuleMatch match = matches.get(0);
    Assertions.assertEquals(0, match.getLine());
    Assertions.assertEquals(1, match.getColumn());
  }
  
  @Test
  public void testCleanOverlappingWithGerman() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    // Juxtaposed errors in "TRGS - Technische" should not be removed.
    String text = "TRGS - Technische Regeln für Gefahrstoffe";
    List<RuleMatch> matches = lt.check(new AnnotatedTextBuilder().addText(text).build(), true,
      JLanguageTool.ParagraphHandling.NORMAL, null, JLanguageTool.Mode.ALL, JLanguageTool.Level.PICKY);
    Assertions.assertEquals(3, matches.size());
  }
  
  @Test
  public void testAdvancedTypography() {
    Language lang = new GermanyGerman();
    Assertions.assertEquals(lang.toAdvancedTypography("Das ist..."), "Das ist…");
    Assertions.assertEquals(lang.toAdvancedTypography("Meinten Sie \"entschieden\" oder \"entscheidend\"?"), "Meinten Sie „entschieden“ oder „entscheidend“?");
    Assertions.assertEquals(lang.toAdvancedTypography("Meinten Sie 'entschieden' oder 'entscheidend'?"), "Meinten Sie ‚entschieden‘ oder ‚entscheidend‘?");
    
    Assertions.assertEquals(lang.toAdvancedTypography("z. B."), "z.\u00a0B.");
    Assertions.assertEquals(lang.toAdvancedTypography("z.B."), "z.\u00a0B.");
    Assertions.assertEquals(lang.toAdvancedTypography("i.d.R."), "i.\u00a0d.\u00a0R.");
    Assertions.assertEquals(lang.toAdvancedTypography("i. d. R."), "i.\u00a0d.\u00a0R."); 
    
    Assertions.assertEquals(lang.toAdvancedTypography("Zeichen ohne sein Gegenstück: '\"' scheint zu fehlen"), "Zeichen ohne sein Gegenstück: ‚\"‘ scheint zu fehlen");
    
  }
}
