/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Yakov Reztsov (http://www.languagetool.org)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.Y
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.chunking;

import org.languagetool.Experimental;
import edu.washington.cs.knowitall.regex.Match;
import edu.washington.cs.knowitall.regex.RegularExpression;
import org.languagetool.AnalyzedTokenReadings;

import java.util.*;
import java.util.regex.Pattern;

import static org.languagetool.chunking.RussianChunker.PhraseType.*;

/**
 * A rule-based prototype Russian chunker. Please note that this chunker
 * has not been evaluated as a stand-alone chunker, it has only been used
 * in the context of LanguageTool's error detection rules.
 * @author Yakov Reztsov
 * Based on idea of German LanguageTool Сhunker.
 * @since 5.6
 */
@Experimental
public class RussianChunker implements Chunker {

  private static final Set<String> FILTER_TAGS = new HashSet<>(Arrays.asList("PP", "NPP", "NPS", "MayMissingYO", "VP", "SBAR"));
  private static final TokenExpressionFactory FACTORY = new TokenExpressionFactory(false);

  private static final Map<String,String> SYNTAX_EXPANSION = new HashMap<>();
  static {
    SYNTAX_EXPANSION.put("<NP>", "<chunk=B-NP> <chunk=I-NP>*");
    SYNTAX_EXPANSION.put("<VP>", "<chunk=B-VP> <chunk=I-VP>*");
  }

  enum PhraseType {
    NP,   // "noun phrase", will be assigned as B-NP for the first token and I-NP for following tokens (like OpenNLP)
    NPS,  // "noun phrase singular"
    NPP,  // "noun phrase plural"
    PP,    // "prepositional phrase" and similar
    MayMissingYO,
    VP,     // verb phrase
    SBAR
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
      // Иванов Иван Иванович
      build("<posre='NN:(Name|Fam|Patr):.*'> <posre='NN:(Name|Fam|Patr):.*'>+ " , NP, true),
      // Иванов И.И.
      build("<posre='NN:Fam:.*'> <regexCS=[А-ЯЁ]> <.> <regexCS=[А-ЯЁ]> <.> ", NP, true),
      // И.И. Иванов
      build("<regexCS=[А-ЯЁ]> <.> <regexCS=[А-ЯЁ]> <.> <posre='NN:Fam:.*'> ", NP, true),
      // verb+verb
      build("<posre='VB:.*:.*'>* " , VP),
      build("<если>", SBAR),  //
      build("<поэтому>", SBAR),  //
      
      //
      build("<тов>", NP)  // simulate OpenNLP?!
  );

  private static final List<RegularExpressionWithPhraseType> REGEXES2 = Arrays.asList(
      // ===== plural and singular noun phrases, based on OpenNLP chunker output ===============
      // "Маша и Миша":
      build("<posre=NN:Name:.*> <и> <posre=NN:Name:.*>", NPP, true)
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

  public RussianChunker() {
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
      if ((!tokenReading.isWhitespace()) && (!tokenReading.getChunkTags().contains(new ChunkTag("MayMissingYO"))))    {
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
    } else if (regex.phraseType == VP) {
      // we assign the same tags as the OpenNLP chunker
      if (i == match.startIndex()) {
        newTag = new ChunkTag("B-VP");
      } else {
        newTag = new ChunkTag("I-VP");
      }
    }   else {
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
      sb.append("  ").append(token).append(" -- ").append(tokenReadingStr).append('\n');
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
