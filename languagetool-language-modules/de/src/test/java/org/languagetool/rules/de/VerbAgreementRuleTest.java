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
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.language.German;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Markus Brenneis
 */
public class VerbAgreementRuleTest {

  private JLanguageTool lt;
  private VerbAgreementRule rule;
  
  @Before
  public void setUp() {
    lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    rule = new VerbAgreementRule(TestTools.getMessages("de"), (German) Languages.getLanguageForShortCode("de-DE"));
  }

  @Test
  public void testSuggestionSorting() throws IOException {
    RuleMatch[] match1 = rule.match(lt.analyzeText("Wir nenne ihn mal „wild“."));
    assertThat(match1.length, is(1));
    assertThat(match1[0].getSuggestedReplacements().toString(),
      is("[Wir nennen, Wir nennten, Er nenne, Sie nenne, Wir nannten, Ich nenne, Es nenne]"));
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
    
    //TODO: This is a FP to be fixed
    RuleMatch[] match4 = rule.match(lt.analyzeText("Mir ist bewusst, dass viele Menschen wie du empfinden."));
    assertThat(match4.length, is(1));
    assertThat(match4[0].getFromPos(), is(41));
    assertThat(match4[0].getToPos(), is(53));
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
    assertGood("Könntest dir mal eine Scheibe davon abschneiden!");
    assertGood("Müsstest dir das mal genauer anschauen.");
    assertGood("Kannst ein neues Release machen.");
    assertGood("Sie fragte: „Muss ich aussagen?“");
    assertGood("„Können wir bitte das Thema wechseln, denn ich möchte ungern darüber reden?“");
    assertGood("Er sagt: „Willst du behaupten, dass mein Sohn euch liebt?“");
    assertGood("Kannst mich gerne anrufen.");
    assertGood("Kannst ihn gerne anrufen.");
    assertGood("Kannst sie gerne anrufen.");
    assertGood("Aber wie ich sehe, benötigt ihr Nachschub.");
    assertGood("Wie ich sehe, benötigt ihr Nachschub.");
    assertGood("Einer wie du kennt doch bestimmt viele Studenten.");
    assertGood("Für Sie mache ich eine Ausnahme.");
    assertGood("Ohne sie hätte ich das nicht geschafft.");
    assertGood("Ohne Sie hätte ich das nicht geschafft.");
    assertGood("Ich hoffe du auch.");
    assertGood("Ich hoffe ihr auch.");
    assertGood("Wird hoffen du auch.");
    assertGood("Hab einen schönen Tag!");
    assertGood("Tom traue ich mehr als Maria.");
    assertGood("Tom kenne ich nicht besonders gut, dafür aber seine Frau.");
    assertGood("Tom habe ich heute noch nicht gesehen.");
    assertGood("Tom bezahle ich gut.");
    assertGood("Tom werde ich nicht noch mal um Hilfe bitten.");
    assertGood("Tom konnte ich überzeugen, nicht aber Maria.");
    assertGood("Mach du mal!");
    assertGood("Das bekomme ich nicht hin.");
    assertGood("Dies betreffe insbesondere Nietzsches Aussagen zu Kant und der Evolutionslehre.");
    assertGood("❌Du fühlst Dich unsicher?");
    assertGood("Bringst nicht einmal so etwas Einfaches zustande!");
    assertGood("Bekommst sogar eine Sicherheitszulage");
    assertGood("Dallun sagte nur, dass er gleich kommen wird und legte wieder auf.");
    assertGood("Tinne, Elvis und auch ich werden gerne wiederkommen!");
    assertGood("Du bist Lehrer und weißt diese Dinge nicht?");
    assertGood("Die Frage lautet: Bist du bereit zu helfen?");
    assertGood("Ich will nicht so wie er enden.");
    assertGood("Das heißt, wir geben einander oft nach als gute Freunde, ob wir gleich nicht einer Meinung sind.");
    assertGood("Wir seh'n uns in Berlin.");
    assertGood("Bist du bereit, darüber zu sprechen?");
    assertGood("Bist du schnell eingeschlafen?");
    assertGood("Im Gegenzug bin ich bereit, beim Türkischlernen zu helfen.");
    assertGood("Das habe ich lange gesucht.");
    assertGood("Dann solltest du schnell eine Nummer der sexy Omas wählen.");
    assertGood("Vielleicht würdest du bereit sein, ehrenamtlich zu helfen.");
    assertGood("Werde nicht alt, egal wie lange du lebst.");
    assertGood("Du bist hingefallen und hast dir das Bein gebrochen.");
    assertGood("Mögest du lange leben!");
    assertGood("Planst du lange hier zu bleiben?");
    assertGood("Du bist zwischen 11 und 12 Jahren alt und spielst gern Fußball bzw. möchtest damit anfangen?");
    assertGood("Ein großer Hadithwissenschaftler, Scheich Şemseddin Mehmed bin Muhammed-ül Cezri, kam in der Zeit von Mirza Uluğ Bey nach Semerkant.");
    assertGood("Die Prüfbescheinigung bekommst du gleich nach der bestanden Prüfung vom Prüfer.");
    assertGood("Du bist sehr schön und brauchst überhaupt gar keine Schminke zu verwenden.");
    assertGood("Ist das so schnell, wie du gehen kannst?");
    assertGood("Egal wie lange du versuchst, die Leute davon zu überzeugen");
    assertGood("Du bist verheiratet und hast zwei Kinder.");
    assertGood("Du bist aus Berlin und wohnst in Bonn.");
    assertGood("Sie befestigen die Regalbretter vermittelst dreier Schrauben.");
    assertGood("Meine Familie & ich haben uns ein neues Auto gekauft.");
    assertGood("Der Bescheid lasse im übrigen die Abwägungen vermissen, wie die Betriebsprüfung zu den Sachverhaltsbeurteilungen gelange, die den von ihr bekämpften Bescheiden zugrundegelegt worden seien.");
    assertGood("Die Bildung des Samens erfolgte laut Alkmaion im Gehirn, von wo aus er durch die Adern in den Hoden gelange.");
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
    assertBad("Er sagte düster: Ich brauchen mich nicht böse angucken.");
    assertBad("David sagte düster: Ich brauchen mich nicht böse angucken.");
    assertBad("Ich setzet mich auf den weichen Teppich und kreuzte die Unterschenkel wie ein Japaner.");
    assertBad("Ich brauchen einen Karren mit zwei Ochsen.");
    assertBad("Ich haben meinen Ohrring fallen lassen.");
    assertBad("Ich stehen Ihnen gerne für Rückfragen zur Verfügung.");
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
    assertGood("Ja sind ab morgen dabei.");
    assertGood("Oh bin überfragt.");
    // TODO: assertBad("Er fragte irritiert: „Darf ich fragen, die an dich gerichtet werden, beantworten?“");
    assertGood("Angenommen, du wärst ich.");
    assertGood("Ich denke, dass das Haus, in das er gehen will, heute Morgen gestrichen worden ist.");
    assertGood("Ich hab mein Leben, leb du deines!");
    assertGood("Da freut er sich, wenn er schlafen geht und was findet.");
    // incorrect sentences:
    assertBad("Auch morgen leben du.");
    assertBad("Du weiß noch, dass du das gestern gesagt hast.");
    assertBad("Auch morgen leben du"); // do not segfault because "du" is the last token
    assertBad("Auch morgen leben er.");
    assertBad("Auch morgen leben ich.");
    assertBad("Auch morgen lebte wir noch.");
    assertBad("Du bin nett.", 2); // TODO 2 errors
    assertBad("Du können heute leider nicht kommen.");
    assertBad("Du können heute leider nicht kommen.", "Du könnest", "Du kannst", "Du könntest", "Wir können", "Sie können", "Du konntest");
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
    assertBad("Ich leben.", "Ich lebe", "Ich leb", "Ich lebte", "Wir leben", "Sie leben");
    assertBad("Lebe du?");
    assertBad("Lebe du?", "Lebest du", "Lebst du", "Lebe er", "Lebe es", "Lebtest du", "Lebe ich", "Lebe sie");
    assertBad("Leben du?");
    assertBad("Nett bist ich nicht.", 2);
    assertBad("Nett bist ich nicht.", 2, "bin ich", "bist du", "sei ich", "wäre ich", "war ich");
    assertBad("Nett sind du.");
    assertBad("Nett sind er.");
    assertBad("Nett sind er.", "sind wir", "sei er", "ist er", "sind sie", "wäre er", "war er");
    assertBad("Nett warst wir.", 2);
    assertBad("Wir bin nett.", 2);
    assertBad("Wir gelangst zu ihr.", 2);
    assertBad("Wir könnt heute leider nicht kommen.");
    assertBad("Wünscht du dir mehr Zeit?", "Subjekt (du) und Prädikat (Wünscht)");
    assertBad("Wir lebst noch.", 2);
    assertBad("Wir lebst noch.", 2, "Wir leben", "Wir lebten", "Du lebst");
    assertBad("Er sagte düster: „Ich brauchen mich nicht schuldig fühlen.“", 1, "Ich brauche", "Ich brauchte", "Ich brauch", "Ich bräuchte", "Wir brauchen", "Sie brauchen");
    assertBad("Er sagte: „Ich brauchen mich nicht schuldig fühlen.“", 1, "Ich brauche", "Ich brauchte", "Ich brauch", "Ich bräuchte", "Wir brauchen", "Sie brauchen");
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

  private void assertBad(String input, int expectedMatches, String... expectedSuggestions) throws IOException {
    RuleMatch[] matches = rule.match(lt.analyzeText(input));
    assertEquals("Did not find " + expectedMatches + " match(es) in sentence '" + input + "'", expectedMatches, matches.length);
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
