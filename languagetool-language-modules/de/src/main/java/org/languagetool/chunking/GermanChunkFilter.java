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
  private static final List<RegularExpressionWithPhraseType> regexes = Arrays.asList(
    // ===== plural and singular noun phrases, based on OpenNLP chunker output ===============
    // "ein Hund und eine Katze":
    build("<chunk=B-NP & !regex=jede[rs]?> <chunk=I-NP>* <und|sowie> <NP>", NPP),
    // "größte und erfolgreichste Erfindung" (fixes mistagging introduced above):
    build("<pos=ADJ> <und|sowie> <chunk=B-NP & !pos=PLU> <chunk=I-NP>*", NPS, true),
    // "deren Bestimmung und Funktion" (fixes mistagging introduced above):
    build("<deren> <chunk=B-NP & !pos=PLU> <und|sowie> <chunk=B-NP>*", NPS, true),
    // "Julia und Karsten ist alt.":
    build("<pos=EIG> <und> <pos=EIG>", NPP),
    // "die älteste und bekannteste Maßnahme" - OpenNLP won't detect that as one NP:
    build("<pos=ART> <pos=ADJ> <und|sowie> (<pos=ADJ>|<pos=PA2>) <chunk=I-NP & !pos=PLU>+", NPS, true),
    // "eine Masseeinheit und keine Gewichtseinheit":
    build("<chunk=B-NP & !pos=PLU> <chunk=I-NP>* <und|sowie> <keine> <chunk=I-NP>+", NPS, true),

    // "eins ihrer drei Autos":
    build("(<eins>|<eines>) <chunk=B-NP> <chunk=I-NP>+", NPS),

    // "er und seine Schwester":
    build("<ich|du|er|sie|es|wir|ihr|sie> <und|oder|sowie> <NP>", NPP),
    // "sowohl sein Vater als auch seine Mutter":
    build("<sowohl> <NP> <als> <auch> <NP>", NPP),
    // "sowohl Tom als auch Maria":
    build("<sowohl> <pos=EIG> <als> <auch> <pos=EIG>", NPP),
    // "sowohl er als auch seine Schwester":
    build("<sowohl> <ich|du|er|sie|es|wir|ihr|sie> <als> <auch> <NP>", NPP),
    // "Rekonstruktionen oder der Wiederaufbau", aber nicht "Isolation und ihre Überwindung":
    build("<pos=SUB> <und|oder|sowie> <chunk=B-NP & !ihre> <chunk=I-NP>*", NPP),
    // "Weder Gerechtigkeit noch Freiheit":
    build("<weder> <pos=SUB> <noch> <pos=SUB>", NPP),

    // "drei Katzen" - needed as ZAL cannot be unified, as it has no features:
    build("(<zwei|drei|vier|fünf|sechs|sieben|acht|neun|zehn|elf|zwölf>) <chunk=I-NP>", NPP),

    // "der von der Regierung geprüfte Hund ist grün":
    build("<chunk=B-NP> <pos=PRP> <NP> <chunk=B-NP & pos=SIN> <chunk=I-NP>*", NPS),
    build("<chunk=B-NP> <pos=PRP> <NP> <chunk=B-NP & pos=PLU> <chunk=I-NP>*", NPP),

    // "der von der Regierung geprüfte Hund":
    build("<chunk=B-NP> <pos=PRP> <NP> <pos=PA2> <chunk=B-NP & !pos=PLU> <chunk=I-NP>*", NPS),
    build("<chunk=B-NP> <pos=PRP> <NP> <pos=PA2> <chunk=B-NP & !pos=SIN> <chunk=I-NP>*", NPP),

    // "Herr und Frau Schröder":
    build("<Herr|Frau> <und> <Herr|Frau> <pos=EIG>*", NPP),

    // "ein Hund", aber nicht: "[kaum mehr als] vier Prozent":
    build("<chunk=B-NP & !pos=ZAL & !pos=PLU & !chunk=NPP & !einige & !(regex=&prozent;)> <chunk=I-NP & !pos=PLU & !und>*", NPS),  //"!und": OpenNLP packt "Bob und Tom" in eine NP
    // "die Hunde":
    build("<chunk=B-NP & !pos=SIN & !chunk=NPS & !Ellen> <chunk=I-NP & !pos=SIN>*", NPP),

    // "die hohe Zahl dieser relativ kleinen Verwaltungseinheiten":
    build("<chunk=NPS> <pos=PRO> <pos=ADJ> <pos=ADJ> <NP>", NPS),

    // "eine der am meisten verbreiteten Krankheiten":
    build("<regex=eine[rs]?> <der> <am> <pos=ADJ> <pos=PA2> <NP>", NPS),

    // "xy Prozent" - beide Varianten okay (zumindest umgangssprachlich):
    // siehe http://www.canoo.net/services/OnlineGrammar/Wort/Verb/Numerus-Person/ProblemNum.html#Anchor-Mengenangabe-49575
    build("<regex=[\\d,.]+> <&prozent;>", NPS),
    build("<regex=[\\d,.]+> <&prozent;>", NPP),

    // "dass sie wie ein Spiel":
    build("<dass> <sie> <wie> <NP>", NPP),
    // "[so dass Knochenbrüche und] Platzwunden die Regel [sind]"
    build("<pos=PLU> <die> <Regel>", NPP),
    // "Veranstaltung, die immer wieder ein kultureller Höhepunkt", aber nicht "... in der Geschichte des Museums, die Sammlung ist seit 2011 zugänglich.":
    build("<NP> <,> <die> <pos=ADV>+ <chunk=NPS>+", NPP),

    // ===== genitive phrases and similar ====================================================

    // "die ältere der beiden Töchter":
    build("<der|die|das> <pos=ADJ & !pos=PLU> <der> <pos=PRO>? <pos=SUB>", NPS),
    // "Synthese organischer Verbindungen", "die Anordnung der vier Achsen", aber nicht "Einige der Inhaltsstoffe":
    build("<chunk=NPS & !einige> <chunk=NPP & (pos=GEN |pos=ZAL)>+", NPS, true),
    // "die Kenntnisse der Sprache":
    build("<chunk=NPP> <chunk=NPS & pos=GEN>+", NPP, true),
    // "die Pyramide des Friedens und der Eintracht":
    build("<chunk=NPS>+ <und> <chunk=NP[SP] & pos=GEN>+", NPS, true),
    // "Teil der dort ausgestellten Bestände":
    build("<chunk=NPS>+ <der> <pos=ADV> <pos=PA2> <NP>", NPS, true),
    // "Teil der umfangreichen dort ausgestellten Bestände":
    build("<chunk=NPS>+ <der> <pos=ADJ> <pos=ADV> <pos=PA2> <NP>", NPS, true),
    // "die Krankheit unserer heutigen Städte und Siedlungen":
    build("<chunk=NPS>+ <pos=PRO:POS> <pos=ADJ> <NP>", NPS, true),
    // "Elemente eines axiomatischen Systems":  -- führt zu Fehlalarm anderswo
    //build("<chunk=B-NP & pos=PLU> <chunk=I-NP>* <chunk=B-NP & pos=GEN> <chunk=I-NP>*", NPP),

    // "eine Menge englischer Wörter":
    build("<eine> <menge> <NP>+", NPP, true),

    // ===== prepositional phrases ===========================================================

    // "bei den sehr niedrigen Oberflächentemperaturen" (OpenNLP doesn't find this)
    build("<pos=PRP> <pos=ART:> <pos=ADV>* <pos=ADJ> <NP>", PP, true),
    // "in den alten Religionen, Mythen und Sagen":
    build("<pos=PRP> <chunk=NPP>+ <,> <NP>", PP, true),
    // "für die Stadtteile und selbständigen Ortsteile":
    build("<pos=PRP> <chunk=NPP>+", PP, true),
    // "in chemischen Komplexverbindungen", "für die Fische":
    build("<pos=PRP> <NP>", PP),
    // "einschließlich der biologischen und sozialen Grundlagen":
    // with OpenNLP: build("<pos=PRP> <NP> <pos=ADJ> (<und>|<oder>|<bzw.>) <pos=ADJ> <NP>", PP),
    build("<pos=PRP> <NP> <pos=ADJ> (<und>|<oder>|<bzw.>) <NP>", PP),
    // "für Ärzte und Ärztinnen festgestellte Risikoprofil", "der als Befestigung gedachte östliche Teil der Burg":
    build("<pos=PRP> (<NP>)+", PP),
    // "in den darauf folgenden Wochen":
    build("<pos=PRP> <chunk=B-NP> <pos=ADV> <NP>", PP),
    // "in nur zwei Wochen":
    build("<pos=PRP> <pos=ADV> <pos=ZAL> <chunk=B-NP>", PP),
    // "in deren deutschen Installationen":
    build("<pos=PRP> <pos=PRO> <NP>", PP),
    // "nach sachlichen und militärischen Kriterien" - we need to help OpenNLP a bit with this one:
    // with OpenNLP: build("<pos=PRP> <pos=ADJ> (<und|oder|sowie>) <pos=ADJ> <chunk=B-NP>", PP),
    build("<pos=PRP> <pos=ADJ> (<und|oder|sowie>) <NP>", PP),
    // "mit über 1000 Handschriften":
    build("<pos=PRP> <pos=ADV> <regex=\\d+> <NP>", PP),
    // "über laufende Sanierungsmaßnahmen":
    build("<pos=PRP> <pos=PA1> <NP>", PP),
    // "durch Einsatz größerer Maschinen und bessere Kapazitätsplanung":
    // with OpenNLP: build("<pos=PRP> <NP> <pos=ADJ> <NP> (<und|oder>) <NP>", PP),
    build("<pos=PRP> <NP> <NP> (<und|oder>) <NP>", PP),
    // "bei sehr guten Beobachtungsbedingungen":
    build("<pos=PRP> <pos=ADV> <pos=ADJ> <NP>", PP),
    // "die Beziehungen zwischen Kanada und dem Iran":
    build("<chunk=NPP> <zwischen> <pos=EIG> <und|sowie> <NP>", NPP),

    // "die darauffolgenden Jahre" -> eigentlich "in den darauffolgenden Jahren":
    build("<die> <pos=ADJ> <Sekunden|Minuten|Stunden|Tage|Wochen|Monate|Jahre|Jahrzehnte|Jahrhunderte> (<NP>)?", PP),
    // "die letzten zwei Monate" -> eigentlich "in den letzten zwei Monaten":
    build("<die> <pos=ADJ> <pos=ZAL> <Sekunden|Minuten|Stunden|Tage|Wochen|Monate|Jahre|Jahrzehnte|Jahrhunderte> (<NP>)?", PP),
    // "letztes Jahr":
    build("<regex=(vor)?letzte[sn]?> <Woche|Monat|Jahr|Jahrzehnt|Jahrhundert>", PP),
    // ", die die hauptsächliche Beute der Eisbären", ", welche der Urstoff aller Körper":
    build("<,> <die|welche> <NP> <chunk=NPS & pos=GEN>+", NPP),
    // "Kommentare, Korrekturen, Kritik":
    build("<NP> <,> <NP> <,> <NP>", NPP)

  );

  private static RegularExpressionWithPhraseType build(String expr, PhraseType phraseType) {
    return build(expr, phraseType, false);
  }

  private static RegularExpressionWithPhraseType build(String expr, PhraseType phraseType, boolean overwrite) {
    String expandedExpr = expr
            .replace("<NP>", "<chunk=B-NP> <chunk=I-NP>*")
            .replace("&prozent;", "Prozent|Kilo|Kilogramm|Gramm|Euro|Pfund");
    RegularExpression<ChunkTaggedToken> expression = RegularExpression.compile(expandedExpr, factory);
    return new RegularExpressionWithPhraseType(expression, phraseType, overwrite);
  }

  List<ChunkTaggedToken> filter(List<ChunkTaggedToken> tokens) {
    if (DEBUG) {
      System.out.println("=============== FILTER INPUT ===============");
      System.out.println(getDebugString(tokens));
    }
    for (RegularExpressionWithPhraseType regex : regexes) {
      apply(regex, tokens);
    }
    return tokens;
  }

  private List<ChunkTaggedToken> apply(RegularExpressionWithPhraseType regex, List<ChunkTaggedToken> tokens) {
    String prevDebug = getDebugString(tokens);
    try {
      AffectedSpans affectedSpans = doApplyRegex(regex, tokens);
      String debug = getDebugString(tokens);
      if (!debug.equals(prevDebug)) {
        printDebugInfo(regex, affectedSpans, debug);
      }
    } catch (Exception e) {
      throw new RuntimeException("Could not apply chunk regexp '" + regex.expression + "' to tokens: " + tokens, e);
    }
    return tokens;
  }

  private void printDebugInfo(RegularExpressionWithPhraseType regex, AffectedSpans affectedSpans, String debug) {
    System.out.println("=== Applied " + regex.phraseType + ": " + regex.expression + " ===");
    if (regex.overwrite) {
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

  private AffectedSpans doApplyRegex(RegularExpressionWithPhraseType regex, List<ChunkTaggedToken> tokens) {
    List<Match<ChunkTaggedToken>> matches = regex.expression.findAll(tokens);
    List<Span> affectedSpans = new ArrayList<>();
    for (Match<ChunkTaggedToken> match : matches) {
      affectedSpans.add(new Span(match.startIndex(), match.endIndex()));
      for (int i = match.startIndex(); i < match.endIndex(); i++) {
        ChunkTaggedToken token = tokens.get(i);
        List<ChunkTag> newChunkTags = new ArrayList<>();
        newChunkTags.addAll(token.getChunkTags());
        if (regex.overwrite) {
          List<ChunkTag> filtered = new ArrayList<>();
          for (ChunkTag newChunkTag : newChunkTags) {
            if (!FILTER_TAGS.contains(newChunkTag.getChunkTag())) {
              filtered.add(newChunkTag);
            }
          }
          newChunkTags = filtered;
        }
        ChunkTag newTag = new ChunkTag(regex.phraseType.name());
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

  private static class RegularExpressionWithPhraseType {
    final RegularExpression<ChunkTaggedToken> expression;
    final PhraseType phraseType;
    final boolean overwrite;
    RegularExpressionWithPhraseType(RegularExpression<ChunkTaggedToken> expression, PhraseType phraseType, boolean overwrite) {
      this.expression = expression;
      this.phraseType = phraseType;
      this.overwrite = overwrite;
    }
  }

}
