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

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * @author Markus Brenneis
 */
public class VerbAgreementRuleTest {

  private JLanguageTool lt;
  private VerbAgreementRule rule;
  
  @Before
  public void setUp() throws IOException {
    lt = new JLanguageTool(new GermanyGerman());
    rule = new VerbAgreementRule(TestTools.getMessages("de"), new GermanyGerman());
  }

  @Test
  public void testPositions() throws IOException {
    RuleMatch[] match1 = rule.match(lt.analyzeText("Du erreichst ich unter 12345"));
    assertThat(match1.length, is(1));
    assertThat(match1[0].getFromPos(), is(3));
    assertThat(match1[0].getToPos(), is(16));
    RuleMatch[] match2 = rule.match(lt.analyzeText("Hallo Karl. Du erreichst ich unter 12345"));
    assertThat(match2.length, is(1));
    assertThat(match2[0].getFromPos(), is(12+3));
    assertThat(match2[0].getToPos(), is(12+16));
    RuleMatch[] match3 = rule.match(lt.analyzeText("Ihr könnt das Training abbrechen, weil es nichts bringen wird. Er geht los und sagt dabei: Werde ich machen."));
    assertThat(match3.length, is(1));
    assertThat(match3[0].getFromPos(), is(97));
    assertThat(match3[0].getToPos(), is(107));
  }
  
  @Test
  public void testWrongVerb() throws IOException {
    // correct sentences:
    assertGood("Du bist in dem Moment angekommen, als ich gegangen bin.");
    assertGood("Kümmere du dich mal nicht darum!");
    assertGood("Ich weiß, was ich tun werde, falls etwas geschehen sollte.");
    assertGood("...die dreißig Jahre jünger als ich ist.");
    assertGood("Ein Mann wie ich braucht einen Hut.");
    assertGood("Egal, was er sagen wird, ich habe meine Entscheidung getroffen.");
    assertGood("Du Beharrst darauf, dein Wörterbuch hätte recht, hast aber von den Feinheiten des Japanischen keine Ahnung!");
    assertGood("Bin gleich wieder da.");
    assertGood("Wobei ich äußerst vorsichtig bin.");
    assertGood("Es ist klar, dass ich äußerst vorsichtig mit den Informationen umgehe");
    assertGood("Es ist klar, dass ich äußerst vorsichtig bin.");
    assertGood("Wobei er äußerst selten darüber spricht.");
    assertGood("Wobei er äußerst selten über seine erste Frau spricht.");
    assertGood("Das Wort „schreibst“ ist schön.");
    assertGood("Die Jagd nach bin Laden.");
    assertGood("Die Unterlagen solltet ihr gründlich durcharbeiten.");
    assertGood("Er reagierte äußerst negativ.");
    assertGood("Max und ich sollten das machen.");
    assertGood("Osama bin Laden stammt aus Saudi-Arabien.");
    assertGood("Solltet ihr das machen?");
    assertGood("Ein Geschenk, das er einst von Aphrodite erhalten hatte.");
    assertGood("Wenn ich sterben sollte, wer würde sich dann um die Katze kümmern?");
    assertGood("Wenn er sterben sollte, wer würde sich dann um die Katze kümmern?");
    assertGood("Wenn sie sterben sollte, wer würde sich dann um die Katze kümmern?");
    assertGood("Wenn es sterben sollte, wer würde sich dann um die Katze kümmern?");
    assertGood("Wenn ihr sterben solltet, wer würde sich dann um die Katze kümmern?");
    assertGood("Wenn wir sterben sollten, wer würde sich dann um die Katze kümmern?");
    assertGood("Dafür erhielten er sowie der Hofgoldschmied Theodor Heiden einen Preis.");
    assertGood("Probst wurde deshalb in den Medien gefeiert.");
    assertGood("/usr/bin/firefox");
    assertGood("Das sind Leute, die viel mehr als ich wissen.");
    assertGood("Das ist mir nicht klar, kannst ja mal beim Kunden nachfragen.");
    assertGood("So tes\u00ADtest Du das mit dem soft hyphen.");
    assertGood("Viele Brunnen in Italiens Hauptstadt sind bereits abgeschaltet.");
    assertGood("„Werde ich tun!“");
    assertGood("Sie fragte: „Muss ich aussagen?“");
    assertGood("„Können wir bitte das Thema wechseln, denn ich möchte ungern darüber reden?“");
    assertGood("Er sagt: „Willst du behaupten, dass mein Sohn euch liebt?“");
    // incorrect sentences:
    assertBad("Als Borcarbid weißt es eine hohe Härte auf.");
    assertBad("Das greift auf Vorläuferinstitutionen bist auf die Zeit von 1234 zurück.");
    assertBad("Die Eisenbahn dienst überwiegend dem Güterverkehr.");
    assertBad("Die Unterlagen solltest ihr gründlich durcharbeiten.");
    assertBad("Peter bin nett.");
    assertBad("Solltest ihr das machen?", "Subjekt und Prädikat (Solltest)");
    assertBad("Weiter befindest sich im Osten die Gemeinde Dorf.");
    assertBad("Ich geht jetzt nach Hause, weil ich schon zu spät bin.");
    assertBad("„Du muss gehen.“");
    assertBad("Du weiß es doch.");
    assertBad("Sie sagte zu mir: „Du muss gehen.“");
    assertBad("„Ich müsst alles machen.“");
    assertBad("„Ich könnt mich sowieso nicht verstehen.“");
  }

  @Test
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
//    assertGood("Die Zeitschrift film-dienst.");
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
    assertGood("Er sagte: „Darf ich bitten, mir zu folgen?“");
    // TODO: assertBad("Er fragte irritiert: „Darf ich fragen, die an dich gerichtet werden, beantworten?“");
//     assertGood("Angenommen, du wärst ich."); TODO
    assertGood("Ich denke, dass das Haus, in das er gehen will, heute Morgen gestrichen worden ist.");
    // incorrect sentences:
    assertBad("Auch morgen leben du.");
    assertBad("Du weiß noch, dass du das gestern gesagt hast.");
    assertBad("Auch morgen leben du"); // do not segfault because "du" is the last token
    assertBad("Auch morgen leben er.");
    assertBad("Auch morgen leben ich.");
    assertBad("Auch morgen lebte wir noch.");
    assertBad("Du bin nett.", 2); // TODO 2 errors
    assertBad("Du können heute leider nicht kommen.");
    assertBad("Du können heute leider nicht kommen.", "Du kannst", "Du konntest", "Du könnest", "Du könntest", "Wir können", "Sie können");
    assertBad("Du leben.");
    assertBad("Du wünscht dir so viel.");
    assertBad("Er bin nett.", 2);
    assertBad("Er gelangst zu ihr.", 2);
    assertBad("Er können heute leider nicht kommen.", "Subjekt (Er) und Prädikat (können)");
    assertBad("Er lebst.", 2);
    assertBad("Ich bist nett.", 2);
//     assertBad("Ich geht jetzt nach Hause und dort gehe ich sofort unter die Dusche."); TODO
    assertBad("Ich kannst heute leider nicht kommen.", 2);
    assertBad("Ich leben.");
    assertBad("Ich leben.", "Ich lebe", "Ich lebte", "Wir leben", "Sie leben");
    assertBad("Lebe du?");
    assertBad("Lebe du?", "Lebest du", "Lebst du", "Lebtest du", "Lebe ich", "Lebe er", "Lebe sie", "Lebe es");
    assertBad("Leben du?");
    assertBad("Nett bist ich nicht.", 2);
    assertBad("Nett bist ich nicht.", 2, "bin ich", "sei ich", "war ich", "wäre ich", "bist du");
    assertBad("Nett sind du.");
    assertBad("Nett sind er.");
    assertBad("Nett sind er.", "ist er", "sei er", "war er", "wäre er", "sind wir", "sind sie");
    assertBad("Nett warst wir.", 2);
    assertBad("Wir bin nett.", 2);
    assertBad("Wir gelangst zu ihr.", 2);
    assertBad("Wir könnt heute leider nicht kommen.");
    assertBad("Wünscht du dir mehr Zeit?", "Subjekt (du) und Prädikat (Wünscht)");
    assertBad("Wir lebst noch.", 2);
    assertBad("Wir lebst noch.", 2, "Wir leben", "Wir lebten", "Du lebst");
  }

  private void assertGood(String s) throws IOException {
    RuleMatch[] matches = rule.match(lt.analyzeText(s));
    if (matches.length != 0) {
      fail("Got > 0 matches for '" + s + "': " + Arrays.toString(matches));
    }
  }

  private void assertBad(String s, int n) throws IOException {
    assertEquals(n, rule.match(lt.analyzeText(s)).length);
  }

  private void assertBad(String s) throws IOException {
    assertBad(s, 1);
  }

  private void assertBad(String s, String expectedErrorSubstring) throws IOException {
    assertEquals(1, rule.match(lt.analyzeText(s)).length);
    final String errorMessage = rule.match(lt.analyzeText(s))[0].getMessage();
    assertTrue("Got error '" + errorMessage + "', expected substring '" + expectedErrorSubstring + "'",
            errorMessage.contains(expectedErrorSubstring));
  }

  private void assertBad(String s, int n, String... expectedSuggestions) throws IOException {
    RuleMatch[] matches = rule.match(lt.analyzeText(s));
    assertEquals("Did not find " + n + " match(es) in sentence '" + s + "'", n, matches.length);
    if (expectedSuggestions.length > 0) {
      RuleMatch match = matches[0];
      // When two errors are reported by the rule (so TODO above), it might happen that the first match does not have the suggestions, but the second one
      if(matches.length > 1 && match.getSuggestedReplacements().isEmpty()) {
        match = matches[1];
      }
      List<String> suggestions = match.getSuggestedReplacements();
      assertThat(suggestions, is(Arrays.asList(expectedSuggestions)));
    }
  }

  private void assertBad(String s, String... expectedSuggestions) throws IOException {
    assertBad(s, 1, expectedSuggestions);
  }
  
}
