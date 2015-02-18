/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.chunking;

import edu.washington.cs.knowitall.regex.Match;
import edu.washington.cs.knowitall.regex.RegularExpression;

import java.util.*;
import java.util.regex.Pattern;

import static org.languagetool.chunking.GermanChunkFilter.PhraseType.*;

/**
 * German chunk filter - needs to run after disambiguation.
 * @since 2.9
 */
public class GermanChunkFilter {

  enum PhraseType {
    NPS,  // "noun phrase singular"
    NPP,  // "noun phrase plural"
    PP    // "prepositional phrase" and similar
  }

  @Deprecated
  public static boolean DEBUG = false;

  private static final Set<String> FILTER_TAGS = new HashSet<>(Arrays.asList("PP", "NPP", "NPS"));
  private static final TokenExpressionFactory factory = new TokenExpressionFactory(false);

  List<ChunkTaggedToken> filter(List<ChunkTaggedToken> tokens) {

    if (DEBUG) {
      System.out.println("=============== FILTER INPUT ===============");
      System.out.println(getDebugString(tokens));
    }

    // These are OpenRegex (https://github.com/knowitall/openregex) expressions.
    // Syntax:
    //    <string|regex|chunk|pos|posregex|=value>
    //    <foo> is the short form of <string=foo>
    //    <pos=X> will match tokens with POS tags that contain X as a substring
    // Example: Combine two conditions via logical AND:
    //    <pos=ADJ & chunk=B-NP>
    // Example: Quote a regular expression so OpenRegex doesn't get confused:
    //    <posre='.*(NOM|AKK).*'>
    // NOTE:
    //   "<NP>" is a special case that gets expanded to: <chunk=B-NP> <chunk=I-NP>*
    //   "&prozent;" also gets expanded

    // ===== plural and singular noun phrases, based on OpenNLP chunker output ===============

    // "ein Hund und eine Katze":
    apply("<chunk=B-NP & !regex=jede[rs]?> <chunk=I-NP>* <und|sowie> <NP>", NPP, tokens);
    // "größte und erfolgreichste Erfindung" (fixes mistagging introduced above):
    apply("<pos=ADJ> <und|sowie> <chunk=B-NP & !pos=PLU> <chunk=I-NP>*", NPS, tokens, true);
    // "deren Bestimmung und Funktion" (fixes mistagging introduced above):
    apply("<deren> <chunk=B-NP & !pos=PLU> <und|sowie> <chunk=B-NP>*", NPS, tokens, true);
    // "Julia und Karsten ist alt.":
    apply("<pos=EIG> <und> <pos=EIG>", NPP, tokens);
    // "die älteste und bekannteste Maßnahme" - OpenNLP won't detect that as one NP:
    apply("<pos=ART> <pos=ADJ> <und|sowie> (<pos=ADJ>|<pos=PA2>) <chunk=I-NP & !pos=PLU>+", NPS, tokens, true);
    // "eine Masseeinheit und keine Gewichtseinheit":
    apply("<chunk=B-NP & !pos=PLU> <chunk=I-NP>* <und|sowie> <keine> <chunk=I-NP>+", NPS, tokens, true);

    // "eins ihrer drei Autos":
    apply("(<eins>|<eines>) <chunk=B-NP> <chunk=I-NP>+", NPS, tokens);

    // "er und seine Schwester":
    apply("<ich|du|er|sie|es|wir|ihr|sie> <und|oder|sowie> <NP>", NPP, tokens);
    // "sowohl sein Vater als auch seine Mutter":
    apply("<sowohl> <NP> <als> <auch> <NP>", NPP, tokens);
    // "sowohl Tom als auch Maria":
    apply("<sowohl> <pos=EIG> <als> <auch> <pos=EIG>", NPP, tokens);
    // "sowohl er als auch seine Schwester":
    apply("<sowohl> <ich|du|er|sie|es|wir|ihr|sie> <als> <auch> <NP>", NPP, tokens);
    // "Rekonstruktionen oder der Wiederaufbau", aber nicht "Isolation und ihre Überwindung":
    apply("<pos=SUB> <und|oder|sowie> <chunk=B-NP & !ihre> <chunk=I-NP>*", NPP, tokens);
    // "Weder Gerechtigkeit noch Freiheit":
    apply("<weder> <pos=SUB> <noch> <pos=SUB>", NPP, tokens);

    // "drei Katzen" - needed as ZAL cannot be unified, as it has no features:
    apply("(<zwei|drei|vier|fünf|sechs|sieben|acht|neun|zehn|elf|zwölf>) <chunk=I-NP>", NPP, tokens);

    // "der von der Regierung geprüfte Hund ist grün":
    apply("<chunk=B-NP> <pos=PRP> <NP> <chunk=B-NP & pos=SIN> <chunk=I-NP>*", NPS, tokens);
    apply("<chunk=B-NP> <pos=PRP> <NP> <chunk=B-NP & pos=PLU> <chunk=I-NP>*", NPP, tokens);

    // "der von der Regierung geprüfte Hund":
    apply("<chunk=B-NP> <pos=PRP> <NP> <pos=PA2> <chunk=B-NP & !pos=PLU> <chunk=I-NP>*", NPS, tokens);
    apply("<chunk=B-NP> <pos=PRP> <NP> <pos=PA2> <chunk=B-NP & !pos=SIN> <chunk=I-NP>*", NPP, tokens);

    // "Herr und Frau Schröder":
    apply("<Herr|Frau> <und> <Herr|Frau> <pos=EIG>*", NPP, tokens);

    // "ein Hund", aber nicht: "[kaum mehr als] vier Prozent":
    apply("<chunk=B-NP & !pos=ZAL & !pos=PLU & !chunk=NPP & !einige & !(regex=&prozent;)> <chunk=I-NP & !pos=PLU & !und>*", NPS, tokens);  //"!und": OpenNLP packt "Bob und Tom" in eine NP
    // "die Hunde":
    apply("<chunk=B-NP & !pos=SIN & !chunk=NPS & !Ellen> <chunk=I-NP & !pos=SIN>*", NPP, tokens);

    // "die hohe Zahl dieser relativ kleinen Verwaltungseinheiten":
    apply("<chunk=NPS> <pos=PRO> <pos=ADJ> <pos=ADJ> <NP>", NPS, tokens);

    // "eine der am meisten verbreiteten Krankheiten":
    apply("<regex=eine[rs]?> <der> <am> <pos=ADJ> <pos=PA2> <NP>", NPS, tokens);

    // "xy Prozent" - beide Varianten okay (zumindest umgangssprachlich):
    // siehe http://www.canoo.net/services/OnlineGrammar/Wort/Verb/Numerus-Person/ProblemNum.html#Anchor-Mengenangabe-49575
    apply("<regex=[\\d,.]+> <&prozent;>", NPS, tokens);
    apply("<regex=[\\d,.]+> <&prozent;>", NPP, tokens);

    // "dass sie wie ein Spiel":
    apply("<dass> <sie> <wie> <NP>", NPP, tokens);
    // "[so dass Knochenbrüche und] Platzwunden die Regel [sind]"
    apply("<pos=PLU> <die> <Regel>", NPP, tokens);
    // "Veranstaltung, die immer wieder ein kultureller Höhepunkt", aber nicht "... in der Geschichte des Museums, die Sammlung ist seit 2011 zugänglich.":
    apply("<NP> <,> <die> <pos=ADV>+ <chunk=NPS>+", NPP, tokens);

    // ===== genitive phrases and similar ====================================================

    // "die ältere der beiden Töchter":
    apply("<der|die|das> <pos=ADJ & !pos=PLU> <der> <pos=PRO>? <pos=SUB>", NPS, tokens);
    // "Synthese organischer Verbindungen", "die Anordnung der vier Achsen", aber nicht "Einige der Inhaltsstoffe":
    apply("<chunk=NPS & !einige> <chunk=NPP & (pos=GEN |pos=ZAL)>+", NPS, tokens, true);
    // "die Kenntnisse der Sprache":
    apply("<chunk=NPP> <chunk=NPS & pos=GEN>+", NPP, tokens, true);
    // "die Pyramide des Friedens und der Eintracht":
    apply("<chunk=NPS>+ <und> <chunk=NP[SP] & pos=GEN>+", NPS, tokens, true);
    // "Teil der dort ausgestellten Bestände":
    apply("<chunk=NPS>+ <der> <pos=ADV> <pos=PA2> <NP>", NPS, tokens, true);
    // "Teil der umfangreichen dort ausgestellten Bestände":
    apply("<chunk=NPS>+ <der> <pos=ADJ> <pos=ADV> <pos=PA2> <NP>", NPS, tokens, true);
    // "die Krankheit unserer heutigen Städte und Siedlungen":
    apply("<chunk=NPS>+ <pos=PRO:POS> <pos=ADJ> <NP>", NPS, tokens, true);
    // "Elemente eines axiomatischen Systems":  -- führt zu Fehlalarm anderswo
    //apply("<chunk=B-NP & pos=PLU> <chunk=I-NP>* <chunk=B-NP & pos=GEN> <chunk=I-NP>*", NPP, tokens);

    // "eine Menge englischer Wörter":
    apply("<eine> <menge> <NP>+", NPP, tokens, true);

    // ===== prepositional phrases ===========================================================

    // "bei den sehr niedrigen Oberflächentemperaturen" (OpenNLP doesn't find this)
    apply("<pos=PRP> <pos=ART:> <pos=ADV>* <pos=ADJ> <NP>", PP, tokens, true);
    // "in den alten Religionen, Mythen und Sagen":
    apply("<pos=PRP> <chunk=NPP>+ <,> <NP>", PP, tokens, true);
    // "für die Stadtteile und selbständigen Ortsteile":
    apply("<pos=PRP> <chunk=NPP>+", PP, tokens, true);
    // "in chemischen Komplexverbindungen", "für die Fische":
    apply("<pos=PRP> <NP>", PP, tokens);
    // "einschließlich der biologischen und sozialen Grundlagen":
    // with OpenNLP: apply("<pos=PRP> <NP> <pos=ADJ> (<und>|<oder>|<bzw.>) <pos=ADJ> <NP>", PP, tokens);
    apply("<pos=PRP> <NP> <pos=ADJ> (<und>|<oder>|<bzw.>) <NP>", PP, tokens);
    // "für Ärzte und Ärztinnen festgestellte Risikoprofil", "der als Befestigung gedachte östliche Teil der Burg":
    apply("<pos=PRP> (<NP>)+", PP, tokens);
    // "in den darauf folgenden Wochen":
    apply("<pos=PRP> <chunk=B-NP> <pos=ADV> <NP>", PP, tokens);
    // "in nur zwei Wochen":
    apply("<pos=PRP> <pos=ADV> <pos=ZAL> <chunk=B-NP>", PP, tokens);
    // "in deren deutschen Installationen":
    apply("<pos=PRP> <pos=PRO> <NP>", PP, tokens);
    // "nach sachlichen und militärischen Kriterien" - we need to help OpenNLP a bit with this one:
    // with OpenNLP: apply("<pos=PRP> <pos=ADJ> (<und|oder|sowie>) <pos=ADJ> <chunk=B-NP>", PP, tokens);
    apply("<pos=PRP> <pos=ADJ> (<und|oder|sowie>) <NP>", PP, tokens);
    // "mit über 1000 Handschriften":
    apply("<pos=PRP> <pos=ADV> <regex=\\d+> <NP>", PP, tokens);
    // "über laufende Sanierungsmaßnahmen":
    apply("<pos=PRP> <pos=PA1> <NP>", PP, tokens);
    // "durch Einsatz größerer Maschinen und bessere Kapazitätsplanung":
    // with OpenNLP: apply("<pos=PRP> <NP> <pos=ADJ> <NP> (<und|oder>) <NP>", PP, tokens);
    apply("<pos=PRP> <NP> <NP> (<und|oder>) <NP>", PP, tokens);
    // "bei sehr guten Beobachtungsbedingungen":
    apply("<pos=PRP> <pos=ADV> <pos=ADJ> <NP>", PP, tokens);
    // "die Beziehungen zwischen Kanada und dem Iran":
    apply("<chunk=NPP> <zwischen> <pos=EIG> <und|sowie> <NP>", NPP, tokens);

    // "die darauffolgenden Jahre" -> eigentlich "in den darauffolgenden Jahren":
    apply("<die> <pos=ADJ> <Sekunden|Minuten|Stunden|Tage|Wochen|Monate|Jahre|Jahrzehnte|Jahrhunderte> (<NP>)?", PP, tokens);
    // "die letzten zwei Monate" -> eigentlich "in den letzten zwei Monaten":
    apply("<die> <pos=ADJ> <pos=ZAL> <Sekunden|Minuten|Stunden|Tage|Wochen|Monate|Jahre|Jahrzehnte|Jahrhunderte> (<NP>)?", PP, tokens);
    // "letztes Jahr":
    apply("<regex=(vor)?letzte[sn]?> <Woche|Monat|Jahr|Jahrzehnt|Jahrhundert>", PP, tokens);
    // ", die die hauptsächliche Beute der Eisbären", ", welche der Urstoff aller Körper":
    apply("<,> <die|welche> <NP> <chunk=NPS & pos=GEN>+", NPP, tokens);
    // "Kommentare, Korrekturen, Kritik":
    apply("<NP> <,> <NP> <,> <NP>", NPP, tokens);

    return tokens;
  }

  private List<ChunkTaggedToken> apply(String regexStr, PhraseType newChunkName, List<ChunkTaggedToken> tokens) {
    return apply(regexStr, newChunkName, tokens, false);
  }

  private List<ChunkTaggedToken> apply(String regexStr, PhraseType newChunkName, List<ChunkTaggedToken> tokens, boolean overwrite) {
    String prevDebug = getDebugString(tokens);
    try {
      AffectedSpans affectedSpans = doApplyRegex(regexStr, newChunkName, tokens, overwrite);
      String debug = getDebugString(tokens);
      if (!debug.equals(prevDebug)) {
        printDebugInfo(regexStr, newChunkName, overwrite, affectedSpans, debug);
      }
    } catch (Exception e) {
      throw new RuntimeException("Could not apply chunk regexp '" + regexStr + "' to tokens: " + tokens, e);
    }
    return tokens;
  }

  private void printDebugInfo(String regexStr, PhraseType newChunkName, boolean overwrite, AffectedSpans affectedSpans, String debug) {
    System.out.println("=== Applied " + newChunkName + ": " + regexStr + " ===");
    if (overwrite) {
      System.out.println("Note: overwrite mode, replacing old " + FILTER_TAGS + " tags");
    }
    String[] debugLines = debug.split("\n");
    int i = 0;
    for (String debugLine : debugLines) {
      if (affectedSpans.isAffected(i)) {
        System.out.println(debugLine.replaceFirst("^  ", " *"));
      } else {
        System.out.println(debugLine);
      }
      i++;
    }
    System.out.println();
  }

  private AffectedSpans doApplyRegex(String regexStr, PhraseType newChunkName, List<ChunkTaggedToken> tokens, boolean overwrite) {
    String expandedRegex = regexStr
            .replace("<NP>", "<chunk=B-NP> <chunk=I-NP>*")
            .replace("&prozent;", "Prozent|Kilo|Kilogramm|Gramm|Euro|Pfund");
    RegularExpression<ChunkTaggedToken> regex = RegularExpression.compile(expandedRegex, factory);
    List<Match<ChunkTaggedToken>> matches = regex.findAll(tokens);
    List<Span> affectedSpans = new ArrayList<>();
    for (Match<ChunkTaggedToken> match : matches) {
      affectedSpans.add(new Span(match.startIndex(), match.endIndex()));
      for (int i = match.startIndex(); i < match.endIndex(); i++) {
        ChunkTaggedToken token = tokens.get(i);
        List<ChunkTag> newChunkTags = new ArrayList<>();
        newChunkTags.addAll(token.getChunkTags());
        if (overwrite) {
          List<ChunkTag> filtered = new ArrayList<>();
          for (ChunkTag newChunkTag : newChunkTags) {
            if (!FILTER_TAGS.contains(newChunkTag.getChunkTag())) {
              filtered.add(newChunkTag);
            }
          }
          newChunkTags = filtered;
        }
        ChunkTag newTag = new ChunkTag(newChunkName.name());
        if (!newChunkTags.contains(newTag)) {
          newChunkTags.add(newTag);
        }
        tokens.set(i, new ChunkTaggedToken(token.getToken(), newChunkTags, token.getReadings()));
      }
    }
    return new AffectedSpans(affectedSpans);
  }

  private String getDebugString(List<ChunkTaggedToken> tokens) {
    if (!DEBUG) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (ChunkTaggedToken token : tokens) {
      if (token.getReadings() != null) {
        sb.append("  " + token + " -- " + token.getReadings().toString().replaceFirst(Pattern.quote(token.getToken()) + "\\[", "[") + "\n");
      } else {
        sb.append("  " + token + " -- " + token.getReadings() + "\n");
      }
    }
    return sb.toString();
  }

  private class Span {
    final int startIndex;
    final int endIndex;
    Span(int startIndex, int endIndex) {
      this.startIndex = startIndex;
      this.endIndex = endIndex;
    }
  }

  private class AffectedSpans {
    final List<Span> spans;
    AffectedSpans(List<Span> spans) {
      this.spans = spans;
    }
    boolean isAffected(int pos) {
      for (Span span : spans) {
        if (pos >= span.startIndex && pos < span.endIndex) {
          return true;
        }
      }
      return false;
    }
  }

}
