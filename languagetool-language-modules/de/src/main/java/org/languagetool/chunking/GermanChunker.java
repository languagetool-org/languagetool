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
 * @since 2.9
 */
public class GermanChunker implements Chunker {

  enum PhraseType {
    NP
  }

  @Deprecated
  public static boolean DEBUG = false;

  private static final Set<String> FILTER_TAGS = new HashSet<>(Arrays.asList("PP", "NPP", "NPS"));
  private static final TokenExpressionFactory factory = new TokenExpressionFactory(false);

  private static final List<RegularExpressionWithPhraseType> regexes = Arrays.asList(
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

  private static RegularExpressionWithPhraseType build(String expr, PhraseType phraseType) {
    String expandedExpr = expr
            .replace("<NP>", "<chunk=B-NP> <chunk=I-NP>*")
            .replace("&prozent;", "Prozent|Kilo|Kilogramm|Gramm|Euro|Pfund");
    RegularExpression<ChunkTaggedToken> expression = RegularExpression.compile(expandedExpr, factory);
    return new RegularExpressionWithPhraseType(expression, phraseType);
  }

  private final GermanChunkFilter chunkFilter;

  public GermanChunker() {
    chunkFilter = new GermanChunkFilter();
  }

  @Override
  public void addChunkTags(List<AnalyzedTokenReadings> tokenReadings) {
    List<AnalyzedTokenReadings> noWhitespaceTokens = new ArrayList<>();
    for (AnalyzedTokenReadings tokenReading : tokenReadings) {
      if (!tokenReading.isWhitespace()) {
        noWhitespaceTokens.add(tokenReading);
      }
    }
    List<ChunkTaggedToken> origChunkTags = getChunkTagsForReadings(noWhitespaceTokens);
    List<ChunkTaggedToken> chunkTags = chunkFilter.filter(origChunkTags);
    assignChunksToReadings(chunkTags);
  }

  private List<ChunkTaggedToken> getChunkTagsForReadings(List<AnalyzedTokenReadings> tokenReadings) {
    List<ChunkTaggedToken> tokens = new ArrayList<>();
    for (AnalyzedTokenReadings tokenReading : tokenReadings) {
      ChunkTaggedToken chunkTaggedToken = new ChunkTaggedToken(tokenReading.getToken(), Collections.singletonList(new ChunkTag("O")), tokenReading);
      tokens.add(chunkTaggedToken);
    }
    if (DEBUG) {
      System.out.println("=============== CHUNKER INPUT ===============");
      System.out.println(getDebugString(tokens));
    }
    for (RegularExpressionWithPhraseType regex : regexes) {
      apply(regex, tokens);
    }
    return tokens;
  }

  private void assignChunksToReadings(List<ChunkTaggedToken> chunkTaggedTokens) {
    for (ChunkTaggedToken taggedToken : chunkTaggedTokens) {
      AnalyzedTokenReadings readings = taggedToken.getReadings();
      if (readings != null) {
        readings.setChunkTags(taggedToken.getChunkTags());
      }
    }
  }


  //
  // TODO: avoid duplication with GermanChunkFilter
  //

  private List<ChunkTaggedToken> apply(RegularExpressionWithPhraseType regex, List<ChunkTaggedToken> tokens) {
    return apply(regex, tokens, false);
  }

  private List<ChunkTaggedToken> apply(RegularExpressionWithPhraseType regex, List<ChunkTaggedToken> tokens, boolean overwrite) {
    String prevDebug = getDebugString(tokens);
    try {
      AffectedSpans affectedSpans = doApplyRegex(regex, tokens, overwrite);
      String debug = getDebugString(tokens);
      if (!debug.equals(prevDebug)) {
        printDebugInfo(regex, overwrite, affectedSpans, debug);
      }
    } catch (Exception e) {
      throw new RuntimeException("Could not apply chunk regexp '" + regex + "' to tokens: " + tokens, e);
    }
    return tokens;
  }

  private void printDebugInfo(RegularExpressionWithPhraseType regex, boolean overwrite, AffectedSpans affectedSpans, String debug) {
    System.out.println("=== Applied " + regex.phraseType + ": " + regex + " ===");
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

  private AffectedSpans doApplyRegex(RegularExpressionWithPhraseType regex, List<ChunkTaggedToken> tokens, boolean overwrite) {
    List<Match<ChunkTaggedToken>> matches = regex.expression.findAll(tokens);
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
        ChunkTag newTag;
        if (i == match.startIndex()) {
          newTag = new ChunkTag("B-NP");
        } else {
          newTag = new ChunkTag("I-NP");
        }
        if (!newChunkTags.contains(newTag)) {
          newChunkTags.add(newTag);
          newChunkTags.remove(new ChunkTag("O"));
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
      sb.append("  " + token + " -- " + token.getReadings().toString().replaceFirst(Pattern.quote(token.getToken()) + "\\[", "[") + "\n");
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
    RegularExpressionWithPhraseType(RegularExpression<ChunkTaggedToken> expression, PhraseType phraseType) {
      this.expression = expression;
      this.phraseType = phraseType;
    }
  }
}
