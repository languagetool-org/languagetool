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
package org.languagetool.rules.de;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.language.German;
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Check subject verb agreement for verb forms "ist", "sind", "war" and "waren".
 * For example, it detects the errors in:
 * <ul>
 *   <li>Das Auto sind schnell.</li>
 *   <li>Das Auto waren schnell.</li>
 *   <li>Die Autos ist schnell.</li>
 *   <li>Die Katze und der Hund ist schön.</li>
 * </ul>
 * See <a href="http://wiki.languagetool.org/german-agreement-check">our wiki</a>
 * for documentation of the steps this rule relies on.
 * @since 2.9
 */
public class SubjectVerbAgreementRule extends Rule {

  private static final ChunkTag NPS = new ChunkTag("NPS"); // noun phrase singular
  private static final ChunkTag NPP = new ChunkTag("NPP"); // noun phrase plural
  private static final ChunkTag PP = new ChunkTag("PP");   // prepositional phrase etc.
  private static final List<String> QUESTION_PRONOUNS = Arrays.asList("wie");
  private static final List<String> CURRENCIES = Arrays.asList("Dollar", "Euro", "Yen");
  
  private static final List<SingularPluralPair> PAIRS = Arrays.asList(
    new SingularPluralPair("ist", "sind"),
    new SingularPluralPair("war", "waren")
    //new SingularPluralPair("geht", "gehen")  // false alarm: "Wohin möchtest du nächsten Sonntag gehen?"
    // add more pairs here to activate more cases step by step
  );
  private final Set<String> singular = new HashSet<>();
  private final Set<String> plural = new HashSet<>();

  private static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("ist|war").build(),
      new PatternTokenBuilder().token("gemeinsam").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().pos(JLanguageTool.SENTENCE_START_TAGNAME).build(),
      new PatternTokenBuilder().pos("ZAL").build(),
      new PatternTokenBuilder().tokenRegex("Tage|Monate|Jahre").build(),
      new PatternTokenBuilder().posRegex("VER:3:SIN:.*").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().pos(JLanguageTool.SENTENCE_START_TAGNAME).build(),
      new PatternTokenBuilder().posRegex("ADV:MOD|ADJ:PRD:GRU").build(),
      new PatternTokenBuilder().pos("ZAL").build(),
      new PatternTokenBuilder().tokenRegex("Tage|Monate|Jahre").build(),
      new PatternTokenBuilder().posRegex("VER:3:SIN:.*").build()
    )
  );

  private final GermanTagger tagger;
  private final German language;

  public SubjectVerbAgreementRule(ResourceBundle messages, German language) {
    super.setCategory(Categories.GRAMMAR.getCategory(messages));
    this.language = language;
    tagger = (GermanTagger) language.getTagger();
    for (SingularPluralPair pair : PAIRS) {
      singular.add(pair.singular);
      plural.add(pair.plural);
    }
    addExamplePair(Example.wrong("Die Autos <marker>ist</marker> schnell."),
                   Example.fixed("Die Autos <marker>sind</marker> schnell."));
  }

  @Override
  public String getId() {
    return "DE_SUBJECT_VERB_AGREEMENT";
  }

  @Override
  public String getDescription() {
    return "Kongruenz von Subjekt und Prädikat (unvollständig)";
  }

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return makeAntiPatterns(ANTI_PATTERNS, language);
  }

  @Override
  public URL getUrl() {
    return Tools.getUrl("http://www.canoo.net/services/OnlineGrammar/Wort/Verb/Numerus-Person/ProblemNum.html");
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokensWithoutWhitespace();
    for (int i = 1; i < tokens.length; i++) {   // start at 1 to skip SENT_START
      if (tokens[i].isImmunized()) {
        continue;
      }
      String tokenStr = tokens[i].getToken();
      // Detect e.g. "Der Hund und die Katze ist":
      RuleMatch singularMatch = getSingularMatchOrNull(tokens, i, tokens[i], tokenStr, sentence);
      if (singularMatch != null) {
        ruleMatches.add(singularMatch);
      }
      // Detect e.g. "Der Hund sind":
      RuleMatch pluralMatch = getPluralMatchOrNull(tokens, i, tokens[i], tokenStr, sentence);
      if (pluralMatch != null) {
        ruleMatches.add(pluralMatch);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Nullable
  private RuleMatch getSingularMatchOrNull(AnalyzedTokenReadings[] tokens, int i, AnalyzedTokenReadings token, String tokenStr, AnalyzedSentence sentence) throws IOException {
    if (singular.contains(tokenStr)) {
      AnalyzedTokenReadings prevToken = tokens[i - 1];
      AnalyzedTokenReadings nextToken = i + 1 < tokens.length ? tokens[i + 1] : null;
      List<ChunkTag> prevChunkTags = prevToken.getChunkTags();
      boolean match = prevChunkTags.contains(NPP)
                      && !prevChunkTags.contains(PP)
                      && !prevToken.getToken().equals("Uhr")   // 'um 18 Uhr ist Feierabend'
                      && !isCurrency(prevToken)
                      && !(nextToken != null && nextToken.getToken().equals("es"))   // 'zehn Jahre ist es her'
                      && prevChunkIsNominative(tokens, i-1)
                      && !hasUnknownTokenToTheLeft(tokens, i)
                      && !hasQuestionPronounToTheLeft(tokens, i-1)
                      && !hasVerbToTheLeft(tokens, i-1)
                      && !containsRegexToTheLeft("wer", tokens, i-1)
                      && !containsRegexToTheLeft("(?i)alle[nr]?", tokens, i-1)
                      && !containsRegexToTheLeft("(?i)jede[rs]?", tokens, i-1)
                      && !containsRegexToTheLeft("(?i)manche[nrs]?", tokens, i-1)
                      && !containsOnlyInfinitivesToTheLeft(tokens, i-1);
      if (match) {
        String message = "Bitte prüfen, ob hier <suggestion>" + getPluralFor(tokenStr) + "</suggestion> stehen sollte.";
        return new RuleMatch(this, sentence, token.getStartPos(), token.getEndPos(), message);
      }
    }
    return null;
  }

  @Nullable
  private RuleMatch getPluralMatchOrNull(AnalyzedTokenReadings[] tokens, int i, AnalyzedTokenReadings token, String tokenStr, AnalyzedSentence sentence) {
    if (plural.contains(tokenStr)) {
      AnalyzedTokenReadings prevToken = tokens[i - 1];
      List<ChunkTag> prevChunkTags = prevToken.getChunkTags();
      boolean match = prevChunkTags.contains(NPS)
                      && !prevChunkTags.contains(NPP)
                      && !prevChunkTags.contains(PP)
                      && !isCurrency(prevToken)
                      && prevChunkIsNominative(tokens, i-1)
                      && !hasUnknownTokenToTheLeft(tokens, i)
                      && !hasUnknownTokenToTheRight(tokens, i+1)
                      && !tokens[1].getToken().matches("Alle|Viele") // "Viele Brunnen in Italiens Hauptstadt sind bereits abgeschaltet."
                      && !isFollowedByNominativePlural(tokens, i+1);  // z.B. "Die Zielgruppe sind Männer." - beides Nominativ, aber 'Männer' ist das Subjekt
      if (match) {
        String message = "Bitte prüfen, ob hier <suggestion>" + getSingularFor(tokenStr) + "</suggestion> stehen sollte.";
        return new RuleMatch(this, sentence, token.getStartPos(), token.getEndPos(), message);
      }
    }
    return null;
  }

  private boolean isCurrency(AnalyzedTokenReadings token) {
    return CURRENCIES.contains(token.getToken());
  }

  boolean prevChunkIsNominative(AnalyzedTokenReadings[] tokens, int startPos) {
    for (int i = startPos; i > 0; i--) {
      List<ChunkTag> chunkTags = tokens[i].getChunkTags();
      if (chunkTags.contains(NPS) || chunkTags.contains(NPP)) {
        if (tokens[i].hasPartialPosTag("NOM")) {
          return true;
        }
      } else {
        return false;
      }
    }
    return false;
  }

  // needed to avoid false alarms, because we cannot unify noun phrases if we cannot analyse a noun
  private boolean hasUnknownTokenToTheLeft(AnalyzedTokenReadings[] tokens, int startPos) {
    return hasUnknownTokenAt(tokens, 0, startPos);
  }

  private boolean hasUnknownTokenToTheRight(AnalyzedTokenReadings[] tokens, int startPos) {
    return hasUnknownTokenAt(tokens, startPos, tokens.length-1);
  }

  private boolean hasUnknownTokenAt(AnalyzedTokenReadings[] tokens, int startPos, int endPos) {
    for (int i = startPos; i < endPos; i++) {
      AnalyzedTokenReadings token = tokens[i];
      for (AnalyzedToken analyzedToken : token.getReadings()) {
        if (analyzedToken.hasNoTag()) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean hasQuestionPronounToTheLeft(AnalyzedTokenReadings[] tokens, int startPos) {
    for (int i = startPos; i > 0; i--) {
      if (QUESTION_PRONOUNS.contains(tokens[i].getToken().toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  private boolean hasVerbToTheLeft(AnalyzedTokenReadings[] tokens, int startPos) {
    for (int i = startPos; i > 0; i--) {
      if (tokens[i].matchesPosTagRegex("VER:[1-3]:.*")) {
        return true;
      }
    }
    return false;
  }

  private boolean containsRegexToTheLeft(String regex, AnalyzedTokenReadings[] tokens, int startPos) {
    Pattern p = Pattern.compile(regex);
    for (int i = startPos; i > 0; i--) {
      if (p.matcher(tokens[i].getToken()).matches()) {
        return true;
      }
    }
    return false;
  }

  // No false alarm on ""Das Kopieren und Einfügen ist sehr nützlich." etc.
  private boolean containsOnlyInfinitivesToTheLeft(AnalyzedTokenReadings[] tokens, int startPos) throws IOException {
    int infinitives = 0;
    for (int i = startPos; i > 0; i--) {
      String token = tokens[i].getToken();
      if (tokens[i].hasPartialPosTag("SUB:")) {
        AnalyzedTokenReadings lookup = tagger.lookup(token.toLowerCase());
        if (lookup != null && lookup.hasPartialPosTag("VER:INF:")) {
          infinitives++;
        } else {
          return false;
        }
      }
    }
    return infinitives >= 2;
  }

  boolean isFollowedByNominativePlural(AnalyzedTokenReadings[] tokens, int startPos) {
    for (int i = startPos; i < tokens.length; i++) {
      AnalyzedTokenReadings token = tokens[i];
      if (token.hasPartialPosTag("SUB") || token.hasPartialPosTag("PRO")) {
        if (token.hasPartialPosTag("NOM:PLU") || token.getChunkTags().contains(new ChunkTag("NPP"))) {  // NPP catches 'und' phrases
          return true;
        }
      }
    }
    return false;
  }

  private String getSingularFor(String token) {
    for (SingularPluralPair pair : PAIRS) {
      if (pair.plural.equals(token)) {
        return pair.singular;
      }
    }
    throw new RuntimeException("No singular found for '" + token + "'");
  }

  private String getPluralFor(String token) {
    for (SingularPluralPair pair : PAIRS) {
      if (pair.singular.equals(token)) {
        return pair.plural;
      }
    }
    throw new RuntimeException("No plural found for '" + token + "'");
  }

  private static class SingularPluralPair {
    String singular;
    String plural;
    SingularPluralPair(String singular, String plural) {
      this.singular = singular;
      this.plural = plural;
    }
  }
}
