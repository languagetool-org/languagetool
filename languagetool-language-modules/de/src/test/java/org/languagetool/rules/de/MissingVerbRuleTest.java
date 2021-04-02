/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.language.GermanyGerman;

public class MissingVerbRuleTest {

  private final MissingVerbRule rule = new MissingVerbRule(TestTools.getEnglishMessages(), (GermanyGerman)Languages.getLanguageForShortCode("de-DE"));

  @Test
  public void test() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    
    assertGood("Da ist ein Verb, mal so zum testen.", lt);
    assertGood("Überschrift ohne Verb aber doch nicht zu kurz", lt);
    assertGood("Sprechen Sie vielleicht zufällig Türkisch?", lt);
    assertGood("Leg den Tresor in den Koffer im Kofferraum.", lt);
    assertGood("Bring doch einfach deine Kinder mit.", lt);
    assertGood("Gut so.", lt);  // no verb, but very short
    assertGood("Ja!", lt);  // no verb, but very short
    assertGood("Vielen Dank für alles, was Du für mich getan hast.", lt);
    assertGood("Herzlichen Glückwunsch zu Deinem zwanzigsten Geburtstag.", lt);
    
    assertBad("Dieser Satz kein Verb.", lt);
    assertBad("Aus einer Idee sich erste Wortgruppen, aus Wortgruppen einzelne Sätze, aus Sätzen ganze Texte.", lt);
    assertBad("Ich ein neues Rad.", lt);
    //assertBad("Ich einen neuen Fehler gefunden.", lt);  // see issue #42
  }

  private void assertGood(String text, JLanguageTool lt) throws IOException {
    assertEquals("Found unexpected error in: '" + text + "'", 0, rule.match(lt.getAnalyzedSentence(text)).length);
  }

  private void assertBad(String text, JLanguageTool lt) throws IOException {
    assertEquals("Did not find expected error in: '" + text + "'", 1, rule.match(lt.getAnalyzedSentence(text)).length);
  }

}
