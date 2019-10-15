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
import org.languagetool.AnalyzedTokenReadings;

import java.util.*;
import java.util.regex.Pattern;

import static org.languagetool.chunking.GermanChunker.PhraseType.*;

/**
 * A rule-based German chunker for noun phrases. Please note that this chunker
 * has not been evaluated as a stand-alone chunker, it has only been used and tested
 * in the context of LanguageTool's error detection rules.
 * @since 2.9
 */
public class GermanChunker implements Chunker {

  private static final Set<String> FILTER_TAGS = new HashSet<>(Arrays.asList("PP", "NPP", "NPS"));
  private static final TokenExpressionFactory FACTORY = new TokenExpressionFactory(false);

  private static final Map<String,String> SYNTAX_EXPANSION = new HashMap<>();
  static {
    SYNTAX_EXPANSION.put("<NP>", "<chunk=B-NP> <chunk=I-NP>*");
    SYNTAX_EXPANSION.put("&prozent;", "Prozent|Kilo|Kilogramm|Gramm|Euro|Pfund");
  }

  enum PhraseType {
    NP,   // "noun phrase", will be assigned as B-NP for the first token and I-NP for following tokens (like OpenNLP)
    NPS,  // "noun phrase singular"
    NPP,  // "noun phrase plural"
    PP    // "prepositional phrase" and similar
  }

  /** @deprecated for internal use only */
  public static void setDebug(boolean debugMode) {
    debug = debugMode;
  }
  /** @deprecated for internal use only */
  public static boolean isDebug() {
    return debug;
  }
  private static boolean debug = false;

  /*
   * REGEXES1 and REGEXES2 are OpenRegex (https:*github.com/knowitall/openregex) expressions.
   * REGEXES1 roughly emulates the behavior of the OpenNLP chunker by tagging the first
   * token of a noun phrase with B-NP and the remaining ones with I-NP.
   * REGEXES2 builds on those annotations to find complex noun phrases.
   *
   * Syntax:
   *    <string|regex|regexCS|chunk|pos|posregex|posre=value>
   *       string: matches the token itself
   *       regex: matches the token against a regular expression
   *       regexCS: is like regex but case-sensitive
   *       chunk: matches the token's chunk
   *       pos: matches the token's POS tags
   *       posregex: matches the token's POS tags against a regular expression
   *       posre: is a synonym for posregex
   *    <foo> is a short form of <string=foo>
   *    <pos=X> will match tokens with POS tags that contain X as a substring
   *
   * Example to combine two conditions via logical AND:
   *    <pos=ADJ & chunk=B-NP>
   * Example: Quote a regular expression so OpenRegex doesn't get confused:
   *    <posre='.*(NOM|AKK).*'>
   *
   * See SYNTAX_EXPANSION for strings that get expanded before interpreted by OpenRegex.
   * The chunks are added to the existing chunks, unless the last argument of build() is
   * true, in which case existing chunks get overwritten.
   */
  
  private static final List<RegularExpressionWithPhraseType> REGEXES1 = Arrays.asList(
      // "das Auto", "das schöne Auto", "das sehr schöne Auto", "die Pariser Innenstadt":
      build("(<posre=^ART.*>|<pos=PRO>)? <pos=ADV>* <pos=PA2>* <pos=ADJ>* <pos=SUB>+", NP),
      // "Mythen und Sagen":
      build("<pos=SUB> (<und|oder>|(<bzw> <.>)) <pos=SUB>", NP),
      // "ältesten und bekanntesten Maßnahmen":
      build("<pos=ADJ> (<und|oder>|(<bzw> <.>)) <pos=PA2> <pos=SUB>", NP),
      // "räumliche und zeitliche Abstände":
      build("<pos=ADJ> (<und|oder>|(<bzw> <.>)) <pos=ADJ> <pos=SUB>", NP),

      // "eine leckere Lasagne":
      build("<posre=^ART.*> <pos=ADV>* <pos=ADJ>* <regexCS=[A-ZÖÄÜ][a-zöäü]+>", NP),  // Lexikon kennt nicht alle Nomen, also so...

      //build("<posre=^ART.*>? <pos=PRO>? <pos=ZAL> <pos=SUB>"),  // "zwei Wochen"
      build("<pos=PRO>? <pos=ZAL> <pos=SUB>", NP),  // "zwei Wochen", "[eines] ihrer drei Autos"

      build("<Herr|Herrn|Frau> <pos=EIG>+", NP),
      build("<Herr|Herrn|Frau> <regexCS=[A-ZÖÄÜ][a-zöäü-]+>+", NP),  // für seltene Nachnamen, die nicht im Lexikon sind

      build("<der>", NP)  // simulate OpenNLP?!
  );

  private static final List<RegularExpressionWithPhraseType> REGEXES2 = Arrays.asList(
      // ===== plural and singular noun phrases, based on OpenNLP chunker output ===============
      // "In christlichen, islamischen und jüdischen Traditionen":
      build("<pos=ADJ> <,> <chunk=B-NP> <chunk=I-NP>* <und|sowie> <NP>", NPP),
      // "ein Hund und eine Katze":
      build("<chunk=B-NP & !regex=jede[rs]?> <chunk=I-NP>* <und|sowie> <pos=ADV>? <NP>", NPP),
      // "größte und erfolgreichste Erfindung" (fixes mistagging introduced above):
      build("<pos=ADJ> <und|sowie> <chunk=B-NP & !pos=PLU> <chunk=I-NP>*", NPS, true),
      // "deren Bestimmung und Funktion" (fixes mistagging introduced above):
      build("<deren> <chunk=B-NP & !pos=PLU> <und|sowie> <chunk=B-NP>*", NPS, true),
      // "Julia und Karsten":
      build("<pos=EIG> <und> <pos=EIG>", NPP),
      // "die älteste und bekannteste Maßnahme" - OpenNLP won't detect that as one NP:
      build("<pos=ART> <pos=ADJ> <und|sowie> (<pos=ADJ>|<pos=PA2>) <chunk=I-NP & !pos=PLU>+", NPS, true),
      // "eine Masseeinheit und keine Gewichtseinheit":
      build("<chunk=B-NP & !pos=PLU> <chunk=I-NP>* <und|sowie> <keine> <chunk=I-NP>+", NPS, true),
      // "Der See und das anliegende Marschland":
      build("<NP> <und|sowie> <pos=ART> <pos=PA1> <pos=SUB>", NPP, true),

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
      // "einer der beiden Höfe":
      build("<regex=eine[rs]?> <der> <beiden> <pos=ADJ>* <pos=SUB>", NPS),
      // "Einer seiner bedeutendsten Kämpfe":
      build("<regex=eine[rs]?> <seiner|ihrer> <pos=PA1> <pos=SUB>", NPS),

      // "xy Prozent" - beide Varianten okay (zumindest umgangssprachlich):
      // siehe http://www.canoonet.eu/services/OnlineGrammar/Wort/Verb/Numerus-Person/ProblemNum.html#Anchor-Mengenangabe-49575
      build("<regex=[\\d,.]+> <&prozent;>", NPS),
      build("<regex=[\\d,.]+> <&prozent;>", NPP),

      // "[alle Arbeitsplätze so umzugestalten,] dass sie wie ein Spiel":
      build("<dass> <sie> <wie> <NP>", NPP),
      // "[so dass Knochenbrüche und] Platzwunden die Regel [sind]"
      build("<pos=PLU> <die> <Regel>", NPP),
      // "Veranstaltung, die immer wieder ein kultureller Höhepunkt", aber nicht "... in der Geschichte des Museums, die Sammlung ist seit 2011 zugänglich.":
      build("<chunk=B-NP & pos=SIN> <chunk=I-NP & pos=SIN>* <,> <die> <pos=ADV>+ <chunk=NPS>+", NPS),
      // "Die Nauheimer Musiktage, die immer wieder ein kultureller Höhepunkt sind":
      build("<chunk=B-NP & pos=PLU> <chunk=I-NP & pos=PLU>* <,> <die> <pos=ADV>+ <chunk=NPS>+", NPP),

      // ===== genitive phrases and similar ====================================================
      
      // "Das letzte der teilnehmenden Länder":
      build("<der|die|das> <pos=ADJ> <der> <pos=PA1> <pos=SUB>", NPS),
      // "Ursachen der vorliegenden Durchblutungsstörung":
      build("<pos=SUB & pos=PLU> <der> <pos=PA1> <pos=SUB>", NPP),
      // "die ältere der beiden Töchter":
      build("<der|die|das> <pos=ADJ> <der> <pos=PRO>? <pos=SUB>", NPS),
      // "Synthese organischer Verbindungen", "die Anordnung der vier Achsen", aber nicht "Einige der Inhaltsstoffe":
      build("<chunk=NPS & !einige> <chunk=NPP & (pos=GEN |pos=ZAL)>+", NPS, true),
      // "die Kenntnisse der Sprache":
      build("<chunk=NPP> <chunk=NPS & pos=GEN>+", NPP, true),
      // "die Pyramide des Friedens und der Eintracht":
      build("<chunk=NPS>+ <und> <chunk=NP[SP] & (pos=GEN | pos=ADV)>+", NPS, true),
      // "Teil der dort ausgestellten Bestände":
      build("<chunk=NPS>+ <der> <pos=ADV> <pos=PA2> <chunk=I-NP>", NPS, true),
      // "Autor der ersten beiden Bücher":
      build("<chunk=NPS>+ <der> (<pos=ADJ>|<pos=ZAL>) <NP>", NPS, true),
      // "Autor der beiden Bücher":
      build("<chunk=NPS>+ <der> <NP>", NPS, true),
      // "Teil der umfangreichen dort ausgestellten Bestände":
      build("<chunk=NPS>+ <der> <pos=ADJ> <pos=ADV> <pos=PA2> <NP>", NPS, true),
      // "die Krankheit unserer heutigen Städte und Siedlungen":
      build("<chunk=NPS>+ <pos=PRO:POS> <pos=ADJ> <NP>", NPS, true),
      // "der letzte der vier großen Flüsse":
      build("<der|das> <pos=ADJ> <der> <pos=ZAL> <NP>", NPS, true),
      // "Elemente eines axiomatischen Systems":  -- führt zu Fehlalarm anderswo
      //build("<chunk=B-NP & pos=PLU> <chunk=I-NP>* <chunk=B-NP & pos=GEN> <chunk=I-NP>*", NPP),

      // "eine Menge englischer Wörter [sind aus dem Lateinischen abgeleitet]":
      // NPP stimmt aber nicht für Mathematik, dort NPS: "Eine Menge ist ein Konzept der Mathematik."
      build("<eine> <menge> <NP>+", NPP, true),
      // "dass [sie und sein Sohn ein Paar] sind":
      build("<er|sie|es> <und> <NP> <NP>", NPP),

      // ===== prepositional phrases ===========================================================

      // "laut den meisten Quellen":
      build("<laut> <regex=.*>{0,3} <Quellen>", PP, true),
      // "bei den sehr niedrigen Oberflächentemperaturen" (OpenNLP doesn't find this)
      build("<pos=PRP> <pos=ART:> <pos=ADV>* <pos=ADJ> <NP>", PP, true),
      // "in den alten Religionen, Mythen und Sagen":
      build("<pos=PRP> <chunk=NPP>+ <,> <NP>", PP, true),
      // "für die Stadtteile und selbständigen Ortsteile":
      build("<pos=PRP> <chunk=NPP>+", PP, true),
      // "Das Bündnis zwischen der Sowjetunion und Kuba":
      build("<pos=PRP> <der> <chunk=NPP>+", PP),
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
      // "Aufgrund stark schwankender Absatzmärkte war die GEFA-Flug..."
      build("<pos=PRP> <pos=ADJ> <pos=PA1> <NP>", PP),
      // "durch Einsatz größerer Maschinen und bessere Kapazitätsplanung":
      // with OpenNLP: build("<pos=PRP> <NP> <pos=ADJ> <NP> (<und|oder>) <NP>", PP),
      build("<pos=PRP> <NP> <NP> (<und|oder>) <NP>", PP),
      // "bei sehr guten Beobachtungsbedingungen":
      build("<pos=PRP> <pos=ADV> <pos=ADJ> <NP>", PP),
      // "[Von ursprünglich drei Almhütten] ist noch eine erhalten":
      build("<pos=PRP> <pos=ADJ:PRD:GRU> <pos=ZAL> <NP>", PP),

      // "die darauffolgenden Jahre" -> eigentlich "in den darauffolgenden Jahren":
      build("<die> <pos=ADJ> <Sekunden|Minuten|Stunden|Tage|Wochen|Monate|Jahre|Jahrzehnte|Jahrhunderte> (<NP>)?", PP),
      // "die letzten zwei Monate" -> eigentlich "in den letzten zwei Monaten":
      build("<die> <pos=ADJ> <pos=ZAL> <Sekunden|Minuten|Stunden|Tage|Wochen|Monate|Jahre|Jahrzehnte|Jahrhunderte> (<NP>)?", PP),
      // "letztes Jahr":
      build("<regex=(vor)?letzte[sn]?> <Woche|Monat|Jahr|Jahrzehnt|Jahrhundert>", PP),
      // "Für in Österreich lebende Afrikaner und Afrikanerinnen":
      build("<für> <in> <pos=EIG> <pos=PA1> <pos=SUB> <und> <pos=SUB>", PP, true),

      // "die Beziehungen zwischen Kanada und dem Iran":
      build("<chunk=NPP> <zwischen> <pos=EIG> <und|sowie> <NP>", NPP),
      // ", die die hauptsächliche Beute der Eisbären", ", welche der Urstoff aller Körper":
      build("<,> <die|welche> <NP> <chunk=NPS & pos=GEN>+", NPP),
      // "Kommentare, Korrekturen, Kritik":
      build("<NP> <,> <NP> <,> <NP>", NPP),
      // "Details, Dialoge, wie auch die Typologie der Charaktere":
      build("<NP> <,> <NP> <,> <wie> <auch> <chunk=NPS>+", NPP)
  );

  private static RegularExpressionWithPhraseType build(String expr, PhraseType phraseType) {
    return build(expr, phraseType, false);
  }

  private static RegularExpressionWithPhraseType build(String expr, PhraseType phraseType, boolean overwrite) {
    String expandedExpr = expr;
    for (Map.Entry<String, String> entry : SYNTAX_EXPANSION.entrySet()) {
      expandedExpr = expandedExpr.replace(entry.getKey(), entry.getValue());
    }
    RegularExpression<ChunkTaggedToken> expression = RegularExpression.compile(expandedExpr, FACTORY);
    return new RegularExpressionWithPhraseType(expression, phraseType, overwrite);
  }

  public GermanChunker() {
  }

  @Override
  public void addChunkTags(List<AnalyzedTokenReadings> tokenReadings) {
    List<ChunkTaggedToken> chunkTaggedTokens = getBasicChunks(tokenReadings);
    for (RegularExpressionWithPhraseType regex : REGEXES2) {
      apply(regex, chunkTaggedTokens);
    }
    assignChunksToReadings(chunkTaggedTokens);
  }

  List<ChunkTaggedToken> getBasicChunks(List<AnalyzedTokenReadings> tokenReadings) {
    List<ChunkTaggedToken> chunkTaggedTokens = new ArrayList<>();
    for (AnalyzedTokenReadings tokenReading : tokenReadings) {
      if (!tokenReading.isWhitespace()) {
        List<ChunkTag> chunkTags = Collections.singletonList(new ChunkTag("O"));
        ChunkTaggedToken chunkTaggedToken = new ChunkTaggedToken(tokenReading.getToken(), chunkTags, tokenReading);
        chunkTaggedTokens.add(chunkTaggedToken);
      }
    }
    if (debug) {
      System.out.println("=============== CHUNKER INPUT ===============");
      System.out.println(getDebugString(chunkTaggedTokens));
    }
    for (RegularExpressionWithPhraseType regex : REGEXES1) {
      apply(regex, chunkTaggedTokens);
    }
    return chunkTaggedTokens;
  }

  private void apply(RegularExpressionWithPhraseType regex, List<ChunkTaggedToken> tokens) {
    String prevDebug = getDebugString(tokens);
    try {
      AffectedSpans affectedSpans = doApplyRegex(regex, tokens);
      String debug = getDebugString(tokens);
      if (!debug.equals(prevDebug)) {
        printDebugInfo(regex, affectedSpans, debug);
      }
    } catch (Exception e) {
      throw new RuntimeException("Could not apply chunk regexp '" + regex + "' to tokens: " + tokens, e);
    }
  }

  private void assignChunksToReadings(List<ChunkTaggedToken> chunkTaggedTokens) {
    for (ChunkTaggedToken taggedToken : chunkTaggedTokens) {
      AnalyzedTokenReadings readings = taggedToken.getReadings();
      if (readings != null) {
        readings.setChunkTags(taggedToken.getChunkTags());
      }
    }
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
        ChunkTag newTag = getChunkTag(regex, match, i);
        if (!newChunkTags.contains(newTag)) {
          newChunkTags.add(newTag);
          newChunkTags.remove(new ChunkTag("O"));
        }
        tokens.set(i, new ChunkTaggedToken(token.getToken(), newChunkTags, token.getReadings()));
      }
    }
    return new AffectedSpans(affectedSpans);
  }

  private ChunkTag getChunkTag(RegularExpressionWithPhraseType regex, Match<ChunkTaggedToken> match, int i) {
    ChunkTag newTag;
    if (regex.phraseType == NP) {
      // we assign the same tags as the OpenNLP chunker
      if (i == match.startIndex()) {
        newTag = new ChunkTag("B-NP");
      } else {
        newTag = new ChunkTag("I-NP");
      }
    } else {
      newTag = new ChunkTag(regex.phraseType.name());
    }
    return newTag;
  }

  private void printDebugInfo(RegularExpressionWithPhraseType regex, AffectedSpans affectedSpans, String debug) {
    System.out.println("=== Applied " + regex + " ===");
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

  private String getDebugString(List<ChunkTaggedToken> tokens) {
    if (!debug) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (ChunkTaggedToken token : tokens) {
      String tokenReadingStr = token.getReadings().toString().replaceFirst(Pattern.quote(token.getToken()) + "\\[", "[");
      sb.append("  ").append(token).append(" -- ").append(tokenReadingStr).append("\n");
    }
    return sb.toString();
  }

  private static class Span {
    final int startIndex;
    final int endIndex;
    Span(int startIndex, int endIndex) {
      this.startIndex = startIndex;
      this.endIndex = endIndex;
    }
  }

  private static class AffectedSpans {
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
    @Override
    public String toString() {
      return phraseType + " <= " + expression + " (overwrite: " + overwrite + ")";
    }
  }
}
