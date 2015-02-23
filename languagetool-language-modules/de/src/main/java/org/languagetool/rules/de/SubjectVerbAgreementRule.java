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
import org.languagetool.chunking.ChunkTag;
import org.languagetool.language.German;
import org.languagetool.rules.Category;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.de.GermanTagger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
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
public class SubjectVerbAgreementRule extends GermanRule {

  private static final ChunkTag NPS = new ChunkTag("NPS"); // noun phrase singular
  private static final ChunkTag NPP = new ChunkTag("NPP"); // noun phrase plural
  private static final ChunkTag PP = new ChunkTag("PP");   // prepositional phrase etc.
  private static final List<String> QUESTION_PRONOUNS = Arrays.asList("wie");
  private static final List<String> CURRENCIES = Arrays.asList("Dollar", "Euro", "Yen");

  private final GermanTagger tagger = (GermanTagger)new German().getTagger();

  public SubjectVerbAgreementRule(ResourceBundle messages) {
    if (messages != null) {
      super.setCategory(new Category(messages.getString("category_grammar")));
    }
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
  public URL getUrl() {
    try {
      return new URL("http://www.canoo.net/services/OnlineGrammar/Wort/Verb/Numerus-Person/ProblemNum.html");
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    for (int i = 1; i < tokens.length; i++) {   // start at 1 to skip SENT_START
      AnalyzedTokenReadings token = tokens[i];
      String tokenStr = token.getToken();
      // Detect e.g. "Der Hund und die Katze ist":
      RuleMatch singularMatch = getSingularMatchOrNull(tokens, i, token, tokenStr);
      if (singularMatch != null) {
        ruleMatches.add(singularMatch);
      }
      // Detect e.g. "Der Hund sind":
      RuleMatch pluralMatch = getPluralMatchOrNull(tokens, i, token, tokenStr);
      if (pluralMatch != null) {
        ruleMatches.add(pluralMatch);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Nullable
  private RuleMatch getSingularMatchOrNull(AnalyzedTokenReadings[] tokens, int i, AnalyzedTokenReadings token, String tokenStr) throws IOException {
    if (tokenStr.equals("ist") || tokenStr.equals("war")) {
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
                      && !containsRegexToTheLeft("wer", tokens, i-1)
                      && !containsRegexToTheLeft("(?i)alle[nr]?", tokens, i-1)
                      && !containsRegexToTheLeft("(?i)jede[rs]?", tokens, i-1)
                      && !containsRegexToTheLeft("(?i)manche[nrs]?", tokens, i-1)
                      && !containsOnlyInfinitivesToTheLeft(tokens, i-1);
      if (match) {
        String message = "Bitte prüfen, ob hier <suggestion>" + (tokenStr.equals("ist") ? "sind" : "waren") + "</suggestion> stehen sollte.";
        return new RuleMatch(this, token.getStartPos(), token.getEndPos(), message);
      }
    }
    return null;
  }

  @Nullable
  private RuleMatch getPluralMatchOrNull(AnalyzedTokenReadings[] tokens, int i, AnalyzedTokenReadings token, String tokenStr) {
    if (tokenStr.equals("sind") || tokenStr.equals("waren")) {
      AnalyzedTokenReadings prevToken = tokens[i - 1];
      List<ChunkTag> prevChunkTags = prevToken.getChunkTags();
      boolean match = prevChunkTags.contains(NPS)
                      && !prevChunkTags.contains(NPP)
                      && !prevChunkTags.contains(PP)
                      && !isCurrency(prevToken)
                      && prevChunkIsNominative(tokens, i-1)
                      && !hasUnknownTokenToTheLeft(tokens, i)
                      && !hasUnknownTokenToTheRight(tokens, i+1)
                      && !isFollowedByNominativePlural(tokens, i+1);  // z.B. "Die Zielgruppe sind Männer." - beides Nominativ, aber 'Männer' ist das Subjekt
      if (match) {
        String message = "Bitte prüfen, ob hier <suggestion>" + (tokenStr.equals("sind") ? "ist" : "war") + "</suggestion> stehen sollte.";
        return new RuleMatch(this, token.getStartPos(), token.getEndPos(), message);
      }
    }
    return null;
  }

  private boolean isCurrency(AnalyzedTokenReadings token) {
    return CURRENCIES.contains(token.getToken());
  }

  boolean prevChunkIsNominative(AnalyzedTokenReadings[] tokens, int startPos) {
    for (int i = startPos; i > 0; i--) {
      AnalyzedTokenReadings token = tokens[i];
      List<ChunkTag> chunkTags = token.getChunkTags();
      if (chunkTags.contains(NPS) || chunkTags.contains(NPP)) {
        if (token.hasPartialPosTag("NOM")) {
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
    if (endPos < startPos) {
      throw new RuntimeException("endPos < startPos: " + endPos  + " < " +  startPos);
    }
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
      AnalyzedTokenReadings token = tokens[i];
      if (QUESTION_PRONOUNS.contains(token.getToken().toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  private boolean containsRegexToTheLeft(String regex, AnalyzedTokenReadings[] tokens, int startPos) {
    Pattern p = Pattern.compile(regex);
    for (int i = startPos; i > 0; i--) {
      String token = tokens[i].getToken();
      if (p.matcher(token).matches()) {
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
        if (token.hasPartialPosTag("NOM") && token.hasPartialPosTag("PLU")) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void reset() {}
}
