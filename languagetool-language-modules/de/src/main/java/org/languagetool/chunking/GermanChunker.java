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
    // "das Auto", "das schöne Auto", "das sehr schöne Auto", "die Pariser Innenstadt":
    apply("(<posre=^ART.*>|<pos=PRO>)? <pos=ADV>* <pos=PA2>* <pos=ADJ>* <pos=SUB>+", PhraseType.NP, tokens);
    // "Mythen und Sagen":
    apply("<pos=SUB> (<und|oder>|(<bzw> <.>)) <pos=SUB>", PhraseType.NP, tokens);
    // "ältesten und bekanntesten Maßnahmen":
    apply("<pos=ADJ> (<und|oder>|(<bzw> <.>)) <pos=PA2> <pos=SUB>", PhraseType.NP, tokens);
    // "räumliche und zeitliche Abstände":
    apply("<pos=ADJ> (<und|oder>|(<bzw> <.>)) <pos=ADJ> <pos=SUB>", PhraseType.NP, tokens);

    // "eine leckere Lasagne":
    apply("<posre=^ART.*> <pos=ADV>* <pos=ADJ>* <regexCS=[A-ZÖÄÜ][a-zöäü]+>", PhraseType.NP, tokens);  // Lexikon kennt nicht alle Nomen, also so...

    //apply("<posre=^ART.*>? <pos=PRO>? <pos=ZAL> <pos=SUB>", PhraseType.NP, tokens);  // "zwei Wochen"
    apply("<pos=PRO>? <pos=ZAL> <pos=SUB>", PhraseType.NP, tokens);  // "zwei Wochen", "[eines] ihrer drei Autos"

    apply("<Herr|Herrn|Frau> <pos=EIG>+", PhraseType.NP, tokens);
    apply("<Herr|Herrn|Frau> <regexCS=[A-ZÖÄÜ][a-zöäü-]+>+", PhraseType.NP, tokens);  // für seltene Nachnamen, die nicht im Lexikon sind

    apply("<der>", PhraseType.NP, tokens);  // simulate OpenNLP?!
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
        //System.out.println("***"+newChunkTags);
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

}
