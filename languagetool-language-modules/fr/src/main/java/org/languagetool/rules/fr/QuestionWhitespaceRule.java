/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.fr;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.languagetool.tools.StringTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A rule that matches spaces before ?,:,; and ! (required for correct French
 * punctuation).
 *
 * @see <a href=
 *      "http://unicode.org/udhr/n/notes_fra.html">http://unicode.org/udhr/n/notes_fra.html</a>
 *
 * @author Marcin Miłkowski
 */
public class QuestionWhitespaceRule extends Rule {

  // Pattern used to avoid false positive when signaling missing
  // space before and after colon ':' in URL with common schemes.
  private static final Pattern urlPattern = Pattern.compile(
      "^(file|s?ftp|finger|git|gopher|hdl|https?|shttp|imap|mailto|mms|nntp|s?news(post|reply)?|prospero|rsync|rtspu|sips?|svn|svn\\+ssh|telnet|wais)$");

  private static final String ESPACE_FINE_INSECABLE = "\u202F";
  private static final String NBSP = "\u00A0";

  private static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(Arrays.asList(
      // ignore smileys, such as :-)
      new PatternTokenBuilder().tokenRegex("[:;]").build(),
      new PatternTokenBuilder().csToken("-").setIsWhiteSpaceBefore(false).build(),
      new PatternTokenBuilder().tokenRegex("[\\(\\)D]").setIsWhiteSpaceBefore(false).build()),
      Arrays.asList( // ignore smileys, such as :)
          new PatternTokenBuilder().tokenRegex("[:;]").build(),
          new PatternTokenBuilder().tokenRegex("[\\(\\)D]").setIsWhiteSpaceBefore(false).build()),
      Arrays.asList( // times like 23:20
          new PatternTokenBuilder().tokenRegex(".*\\d{1,2}").build(), new PatternTokenBuilder().token(":").build(),
          new PatternTokenBuilder().tokenRegex("\\d{1,2}").build()),
      Arrays.asList( // "??"
          new PatternTokenBuilder().tokenRegex("[?!]").build(), new PatternTokenBuilder().tokenRegex("[?!]").build()),
      Arrays.asList( // mac address
          new PatternTokenBuilder().tokenRegex("[a-z0-9]{2}").build(), new PatternTokenBuilder().token(":").build(),
          new PatternTokenBuilder().tokenRegex("[a-z0-9]{2}").build(), new PatternTokenBuilder().token(":").build(),
          new PatternTokenBuilder().tokenRegex("[a-z0-9]{2}").build()),
      Arrays.asList( // csv markup (not sure why we need this, but there were a lot of users ignoring
                     // this specific case)
          new PatternTokenBuilder().token(";").build(),
          new PatternTokenBuilder().tokenRegex(".+").setIsWhiteSpaceBefore(false).build(),
          new PatternTokenBuilder().token(";").setIsWhiteSpaceBefore(false).build()),
      Arrays.asList( // csv markup (not sure why we need this, but there were a lot of users ignoring
                     // this specific case)
          new PatternTokenBuilder().tokenRegex(".+").setIsWhiteSpaceBefore(false).build(),
          new PatternTokenBuilder().token(";").setIsWhiteSpaceBefore(false).build(),
          new PatternTokenBuilder().tokenRegex(".+").setIsWhiteSpaceBefore(false).build()));
  private final Supplier<List<DisambiguationPatternRule>> antiPatterns;

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return antiPatterns.get();
  }

  public QuestionWhitespaceRule(ResourceBundle messages, Language language) {
    super.setCategory(Categories.MISC.getCategory(messages));
    antiPatterns = cacheAntiPatterns(language, ANTI_PATTERNS);
  }

  @Override
  public String getId() {
    return "FRENCH_WHITESPACE";
  }

  @Override
  public String getDescription() {
    return "Insertion des espaces fines insécables";
  }

  protected boolean isAllowedWhitespaceChar(AnalyzedTokenReadings[] tokens, int i) {
    return i >= 0 ? tokens[i].isWhitespace() : false;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokens();
    String prevPrevToken = "";
    String prevToken = "";
    for (int i = 1; i < tokens.length; i++) {
      String token = tokens[i].getToken();
      if (tokens[i].isImmunized() || prevToken.equals("(") || prevToken.equals("[")) {
        prevPrevToken = prevToken;
        prevToken = token;
        continue;
      }
      String msg = null;
      String suggestionText = null;
      int iFrom = i - 1;
      int iTo = i;
      boolean isPreviousWhitespace = i > 0 ? tokens[i - 1].isWhitespace() : false;
      String prevTokenToChange = prevToken;
      if (isPreviousWhitespace) {
        prevTokenToChange = "";
      }
      if (!isAllowedWhitespaceChar(tokens, i - 1)) {
        // Strictly speaking, the character before ?!; should be an "espace fine
        // insécable" (U+202f). In practise, an "espace insécable" (U+00a0) is also
        // often used - or even a common space. Let's accept all - use
        // QuestionWhitespaceStrictRule if this is not strict enough.
        if (token.equals("?") && !prevToken.equals("!")) {
          msg = "Le point d'interrogation est précédé d'une espace fine insécable.";
          suggestionText = prevTokenToChange + ESPACE_FINE_INSECABLE + "?";
        } else if (token.equals("!") && !prevToken.equals("?")) {
          msg = "Le point d'exclamation est précédé d'une espace fine insécable.";
          suggestionText = prevTokenToChange + ESPACE_FINE_INSECABLE + "!";
        } else if (token.equals(";")) {
          msg = "Le point-virgule est précédé d'une espace fine insécable.";
          suggestionText = prevTokenToChange + ESPACE_FINE_INSECABLE + ";";
        } else if (token.equals(":")) {
          // Avoid false positive for URL like http://www.languagetool.org.
          Matcher matcherUrl = urlPattern.matcher(prevToken);
          if (!matcherUrl.find()) {
            msg = "Les deux-points sont précédés d'une espace insécable.";
            suggestionText = prevTokenToChange + NBSP + ":";
          }
        } else if (token.equals("»")) {
          if (prevPrevToken.equals("«")) {
            msg = "Les guillemets sont toujours accompagnés d'une espace insécable.";
            suggestionText = "«" + NBSP + prevTokenToChange + NBSP + "»";
            iFrom = i - 2;
          } else {
            msg = "Le guillemet fermant est précédé d'une espace insécable.";
            suggestionText = prevTokenToChange + NBSP + "»";
          }
        }
      }

      if (prevToken.equals("«")) {
        if (StringTools.isEmpty(token)) {
          msg = "Le guillemet ouvrant est suivi d'une espace insécable.";
          suggestionText = "«" + NBSP;
          iTo = i - 1;
        } else if (!isAllowedWhitespaceChar(tokens, i)) {
          String nextToken = "";
          if (i + 1 < tokens.length) {
            nextToken = tokens[i + 1].getToken();
          }
          if (!nextToken.equals("»")) {
            msg = "Le guillemet ouvrant est suivi d'une espace insécable.";
            if (!tokens[i].isWhitespace()) {
              suggestionText = "«" + NBSP + token;
            } else {
              suggestionText = "«" + NBSP;
            }
          }
        }
      }

      if (msg != null) {
        int fromPos = tokens[iFrom].getStartPos();
        int toPos = tokens[iTo].getEndPos();
        RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos, toPos, msg, "Insérer une espace insécable");
        ruleMatch.setSuggestedReplacement(suggestionText);
        ruleMatches.add(ruleMatch);
      }
      prevPrevToken = prevToken;
      prevToken = token;
    }

    return toRuleMatchArray(ruleMatches);
  }

}
