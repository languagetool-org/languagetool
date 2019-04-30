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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

/**
 * A rule that matches spaces before ?,:,; and ! (required for correct French
 * punctuation).
 *
 * @see <a href="http://unicode.org/udhr/n/notes_fra.html">http://unicode.org/udhr/n/notes_fra.html</a>
 *
 * @author Marcin Miłkowski
 */
public class QuestionWhitespaceRule extends Rule {

  // Pattern used to avoid false positive when signaling missing
  // space before and after colon ':' in URL with common schemes.
  private static final Pattern urlPattern = Pattern.compile("^(file|s?ftp|finger|git|gopher|hdl|https?|shttp|imap|mailto|mms|nntp|s?news(post|reply)?|prospero|rsync|rtspu|sips?|svn|svn\\+ssh|telnet|wais)$");

  private final Language FRENCH;

  private static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
      Arrays.asList( // ignore smileys, such as :-)
        new PatternTokenBuilder().tokenRegex("[:;]").build(),
        new PatternTokenBuilder().csToken("-").setIsWhiteSpaceBefore(false).build(),
        new PatternTokenBuilder().tokenRegex("[\\(\\)D]").setIsWhiteSpaceBefore(false).build()
      ),
      Arrays.asList( // ignore smileys, such as :)
        new PatternTokenBuilder().tokenRegex("[:;]").build(),
        new PatternTokenBuilder().tokenRegex("[\\(\\)D]").setIsWhiteSpaceBefore(false).build()
      )
    );

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return makeAntiPatterns(ANTI_PATTERNS, FRENCH);
  }

  public QuestionWhitespaceRule(ResourceBundle messages, Language language) {
    super.setCategory(Categories.MISC.getCategory(messages));
    FRENCH = language;
  }

  @Override
  public String getId() {
    return "FRENCH_WHITESPACE";
  }

  @Override
  public String getDescription() {
    return "Insertion des espaces fines insécables";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokens();
    String prevToken = "";
    for (int i = 1; i < tokens.length; i++) {
      if (tokens[i].isImmunized()) {
        continue;
      }
      String token = tokens[i].getToken();
      boolean isWhiteBefore = tokens[i].isWhitespaceBefore();
      String msg = null;
      int fixLen = 0;
      String suggestionText = null;
      if (!isWhiteBefore) {
        // Strictly speaking, the character before ?!; should be an
        // "espace fine insécable" (U+202f).  In practise, an
        // "espace insécable" (U+00a0) is also often used - or even a common space.
        // Let's accept all - use QuestionWhitespaceStrictRule if this is not strict enough.
        if (token.equals("?") && !prevToken.equals("!")) {
          msg = "Point d'interrogation est précédé d'une espace fine insécable.";
          // non-breaking space
          suggestionText = prevToken + " ?";
          fixLen = 1;
        } else if (token.equals("!") && !prevToken.equals("?")) {
          msg = "Point d'exclamation est précédé d'une espace fine insécable.";
          // non-breaking space
          suggestionText = prevToken + " !";
          fixLen = 1;
        } else if (token.equals(";")) {
          msg = "Point-virgule est précédé d'une espace fine insécable.";
          // non-breaking space
          suggestionText = prevToken + " ;";
          fixLen = 1;
        } else if (token.equals(":")) {
          // Avoid false positive for URL like http://www.languagetool.org.
          Matcher matcherUrl = urlPattern.matcher(prevToken);
          if (!matcherUrl.find()) {
            msg = "Deux-points précédés d'une espace insécable.";
            // non-breaking space
            suggestionText = prevToken + " :";
            fixLen = 1;
          }
        } else if (token.equals("»")) {
          msg = "Le guillemet fermant est précédé d'une espace insécable.";
          // non-breaking space
          suggestionText = prevToken + " »";
          fixLen = 1;
        }
      }

      if (msg != null) {
        int fromPos = tokens[i - 1].getStartPos();
        int toPos = tokens[i - 1].getStartPos() + fixLen
            + tokens[i - 1].getToken().length();
        RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos, toPos, msg,
            "Insérer un espace insécable");
        ruleMatch.setSuggestedReplacement(suggestionText);
        ruleMatches.add(ruleMatch);
      }
      prevToken = token;
    }

    return toRuleMatchArray(ruleMatches);
  }

}
