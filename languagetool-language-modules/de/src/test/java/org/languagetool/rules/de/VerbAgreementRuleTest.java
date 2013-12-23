/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Markus Brenneis
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

import junit.framework.TestCase;
import org.languagetool.JLanguageTool;
import org.languagetool.language.German;

import java.io.IOException;

/**
 * @author Markus Brenneis
 */
public class VerbAgreementRuleTest extends TestCase {

  private VerbAgreementRule rule;
  private JLanguageTool langTool;
  
  @Override
  public void setUp() throws IOException {
    rule = new VerbAgreementRule(null);
    langTool = new JLanguageTool(new German());
  }
  
  public void testWrongVerb() throws IOException {
    // correct sentences:
    assertGood("Das Wort „schreibst“ ist schön.");
    assertGood("Die Jagd nach bin Laden.");
    assertGood("Die Unterlagen solltet ihr gründlich durcharbeiten.");
    assertGood("Er reagierte äußerst negativ.");
    assertGood("Max und ich sollten das machen.");
    assertGood("Osama bin Laden stammt aus Saudi-Arabien.");
    assertGood("Solltet ihr das machen?");
    assertGood("/usr/bin/firefox");
    // incorrect sentences:
    assertBad("Als Borcarbid weißt es eine hohe Härte auf.");
    assertBad("Das greift auf Vorläuferinstitutionen bist auf die Zeit von 1234 zurück.");
    assertBad("Die Eisenbahn dienst überwiegend dem Güterverkehr.");
    assertBad("Die Unterlagen solltest ihr gründlich durcharbeiten.");
    assertBad("Peter bin nett.");
    assertBad("Solltest ihr das machen?", "Subjekt und Prädikat (Solltest)");
    assertBad("Weiter befindest sich im Osten die Gemeinde Dorf.");
  }
  
  public void testWrongVerbSubject() throws IOException {
    // correct sentences:
    assertGood("Auch morgen lebe ich.");
    assertGood("Auch morgen leben wir noch.");
    assertGood("Auch morgen lebst du.");
    assertGood("Auch morgen lebt er.");
    assertGood("Auch wenn du leben möchtest.");
    assertGood("auf der er sieben Jahre blieb.");
    assertGood("Das absolute Ich ist nicht mit dem individuellen Geist zu verwechseln.");
    assertGood("Das Ich ist keine Einbildung");
    assertGood("Das lyrische Ich ist verzweifelt.");
    assertGood("Den Park, von dem er äußerst genaue Karten zeichnete.");
    assertGood("Der auffälligste Ring ist der erster Ring, obwohl er verglichen mit den anderen Ringen sehr schwach erscheint.");
    assertGood("Der Fehler, falls er bestehen sollte, ist schwerwiegend.");
    assertGood("Der Vorfall, bei dem er einen Teil seines Vermögens verloren hat, ist lange vorbei.");
    assertGood("Diese Lösung wurde in der 64'er beschrieben, kam jedoch nie.");
    assertGood("Die Theorie, mit der ich arbeiten konnte.");
//     assertGood("Die Zeitschrift film-dienst."); TODO
    assertGood("Du bist nett.");
    assertGood("Du kannst heute leider nicht kommen.");
    assertGood("Du lebst.");
    assertGood("Du wünschst dir so viel.");
    assertGood("Er geht zu ihr.");
    assertGood("Er ist nett.");
    assertGood("Er kann heute leider nicht kommen.");
    assertGood("Er lebt.");
    assertGood("Er wisse nicht, ob er lachen oder weinen solle.");
    assertGood("Er und du leben.");
    assertGood("Er und ich leben.");
    assertGood("Falls er bestehen sollte, gehen sie weg.");
//     assertGood("Fest wie unsere Eichen halten allezeit wir stand, wenn Stürme brausen übers Land."); // TODO (remembers "brausen", forgets about "halten")
    assertGood("Heere, des Gottes der Schlachtreihen Israels, den du verhöhnt hast.");
    assertGood("Ich bin");
    assertGood("Ich bin Frankreich!");
    assertGood("Ich bin froh, dass ich arbeiten kann.");
    assertGood("Ich bin nett.");
    assertGood("‚ich bin tot‘"); // TODO 1st token is "‚ich" ?
    assertGood("Ich kann heute leider nicht kommen.");
    assertGood("Ich lebe.");
    assertGood("Lebst du?");
    assertGood("Morgen kommen du und ich.");
    assertGood("Morgen kommen er, den ich sehr mag, und ich.");
    assertGood("Morgen kommen er und ich.");
    assertGood("Morgen kommen ich und sie.");
    assertGood("Morgen kommen wir und sie.");
    assertGood("nachdem er erfahren hatte");
    assertGood("Nett bin ich.");
    assertGood("Nett bist du.");
    assertGood("Nett ist er.");
    assertGood("Nett sind wir.");
    assertGood("Niemand ahnte, dass er gewinnen könne.");
    assertGood("Sie lebt und wir leben.");
    assertGood("Sie und er leben.");
    assertGood("Sind ich und Peter nicht nette Kinder?");
    assertGood("Sodass ich sagen möchte, dass unsere schönen Erinnerungen gut sind.");
    assertGood("Wann ich meinen letzten Film drehen werde, ist unbekannt.");
    assertGood("Was ich tun muss.");
    assertGood("Welche Aufgaben er dabei tatsächlich übernehmen könnte");
    assertGood("wie er beschaffen war");
    assertGood("Wir gelangen zu dir.");
    assertGood("Wir können heute leider nicht kommen.");
    assertGood("Wir leben noch.");
    assertGood("Wir sind nett.");
    assertGood("Wobei wir benutzt haben, dass der Satz gilt.");
    assertGood("Wünschst du dir mehr Zeit?");
    assertGood("Wyrjtjbst du?"); // make sure that "UNKNOWN" is handled correctly
    assertGood("Wenn ich du wäre, würde ich das nicht machen.");
//     assertGood("Angenommen, du wärst ich."); TODO
//     assertGood("Ich denke, dass das Haus, in das er gehen will, heute Morgen gestrichen worden ist."); TODO
    // incorrect sentences:
    assertBad("Auch morgen leben du.");
    assertBad("Auch morgen leben du"); // do not segfault because "du" is the last token
    assertBad("Auch morgen leben er.");
    assertBad("Auch morgen leben ich.");
    assertBad("Auch morgen lebte wir noch.");
    assertBad2("Du bin nett."); // TODO 2 errors
    assertBad("Du können heute leider nicht kommen.");
    assertBad("Du leben.");
    assertBad("Du wünscht dir so viel.");
    assertBad2("Er bin nett.");
    assertBad2("Er gelangst zu ihr.");
    assertBad("Er können heute leider nicht kommen.", "Subjekt (Er) und Prädikat (können)");
    assertBad2("Er lebst.");
    assertBad2("Ich bist nett.");
//     assertBad("Ich geht jetzt nach Hause und dort gehe ich sofort unter die Dusche."); TODO
    assertBad2("Ich kannst heute leider nicht kommen.");
    assertBad("Ich leben.");
    assertBad("Lebe du?");
//     assertBad("Leben du?"); // TODO "Leben" not tagged as verb
    assertBad2("Nett bist ich nicht.");
    assertBad("Nett sind du.");
    assertBad("Nett sind er.");
    assertBad2("Nett warst wir.");
    assertBad2("Wir bin nett.");
    assertBad2("Wir gelangst zu ihr.");
    assertBad("Wir könnt heute leider nicht kommen.");
    assertBad2("Wir lebst noch.");
    assertBad("Wünscht du dir mehr Zeit?", "Subjekt (du) und Prädikat (Wünscht)");
  }

  private void assertGood(String s) throws IOException {
    assertEquals(0, rule.match(langTool.getAnalyzedSentence(s)).length);
  }

  private void assertBad(String s) throws IOException {
    assertEquals(1, rule.match(langTool.getAnalyzedSentence(s)).length);
  }
  
  private void assertBad2(String s) throws IOException {
    assertEquals(2, rule.match(langTool.getAnalyzedSentence(s)).length);
  }

  private void assertBad(String s, String expectedErrorSubstring) throws IOException {
    assertEquals(1, rule.match(langTool.getAnalyzedSentence(s)).length);
    final String errorMessage = rule.match(langTool.getAnalyzedSentence(s))[0].getMessage();
    assertTrue("Got error '" + errorMessage + "', expected substring '" + expectedErrorSubstring + "'",
            errorMessage.contains(expectedErrorSubstring));
  }
  
}
