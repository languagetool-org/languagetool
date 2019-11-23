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
package org.languagetool.rules.patterns;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.InterruptibleCharSequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matches 'regexp' elements from XML rules against sentences.
 *
 * @since 3.2
 */
public class RegexPatternRule extends AbstractPatternRule implements RuleMatcher {

  private static final Pattern suggestionPattern = Pattern.compile("<suggestion>(.*?)</suggestion>");  // TODO: this needs to be cleaned up, there should be no need to parse this?
  private static final Pattern matchPattern = Pattern.compile("\\\\\\d");

  // in suggestions tokens are numbered from 1, anywhere else tokens are numbered from 0.
  // see: http://wiki.languagetool.org/development-overview#toc17
  // But most of the rules tend to use 1 to refer the first capturing group, so keeping that behavior as default
  private static final int MATCHES_IN_SUGGESTIONS_NUMBERED_FROM = 0;

  private final Pattern pattern;
  private final int markGroup;
  private final String shortMessage;

  public RegexPatternRule(String id, String description, String message, String shortMessage, String suggestionsOutMsg, Language language, Pattern regex, int regexpMark) {
    super(id, description, language, regex, regexpMark);
    this.message = message;
    this.pattern = regex;
    this.shortMessage = shortMessage == null ? "" : shortMessage;
    this.suggestionsOutMsg = suggestionsOutMsg;
    markGroup = regexpMark;
  }

  public Pattern getPattern() {
    return pattern;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentenceObj) throws IOException {

    List<Pair<Integer, Integer>> suggestionsInMessage = getClausePositionsInMessage(suggestionPattern, message);
    List<Pair<Integer, Integer>> backReferencesInMessage = getClausePositionsInMessage(matchPattern, message);

    List<Pair<Integer, Integer>> suggestionsInSuggestionsOutMsg = getClausePositionsInMessage(suggestionPattern, suggestionsOutMsg);
    List<Pair<Integer, Integer>> backReferencesInSuggestionsOutMsg = getClausePositionsInMessage(matchPattern, suggestionsOutMsg);

    Matcher patternMatcher = pattern.matcher(new InterruptibleCharSequence(sentenceObj.getText()));
    List<RuleMatch> matches = new ArrayList<>();
    int startPos = 0;

    while (patternMatcher.find(startPos)) {
      try {
        int markStart = patternMatcher.start(markGroup);
        int markEnd = patternMatcher.end(markGroup);

        String processedMessage = processMessage(patternMatcher, message, backReferencesInMessage, suggestionsInMessage, suggestionMatches);
        String processedSuggestionsOutMsg = processMessage(patternMatcher, suggestionsOutMsg, backReferencesInSuggestionsOutMsg,
                suggestionsInSuggestionsOutMsg, suggestionMatchesOutMsg);

        boolean startsWithUpperCase = patternMatcher.start() == 0 && Character.isUpperCase(sentenceObj.getText().charAt(patternMatcher.start()));
        RuleMatch ruleMatch = new RuleMatch(this, sentenceObj, markStart, markEnd, processedMessage, shortMessage, startsWithUpperCase, processedSuggestionsOutMsg);
        matches.add(ruleMatch);

        startPos = patternMatcher.end();
      } catch (IndexOutOfBoundsException e){
        throw new RuntimeException(String.format("Unexpected reference to capturing group in rule with id %s.", this.getFullId()), e);
      } catch (Exception e) {
        throw new RuntimeException(String.format("Unexpected exception when processing regexp in rule with id %s.", this.getFullId()), e);
      }
    }
    return matches.toArray(new RuleMatch[0]);
  }

  @NotNull
  private List<Pair<Integer, Integer>> getClausePositionsInMessage(Pattern pattern, String message) {
    Matcher matcher = pattern.matcher(message);
    List<Pair<Integer, Integer>> clausePositionsInMessage = new ArrayList<>();
    while (matcher.find()) {
      clausePositionsInMessage.add(Pair.of(matcher.start(), matcher.end()));
    }
    return clausePositionsInMessage;
  }

  private String processMessage(Matcher matcher, String message, List<Pair<Integer, Integer>> backReferences,
                                List<Pair<Integer, Integer>> suggestions, List<Match> matches) {

    int closestSuggestionPosition = -1;
    boolean allSuggestionsPassed = suggestions.isEmpty();
    if (!suggestions.isEmpty()) {
      closestSuggestionPosition = 0;
    }

    boolean insideSuggestion;
    StringBuilder processedMessage = new StringBuilder();
    int startOfProcessingPart = 0;
    for (int i = 0; i < backReferences.size(); i++) {
      Pair<Integer, Integer> reference = backReferences.get(i);

      while (!allSuggestionsPassed && (reference.getLeft() > suggestions.get(closestSuggestionPosition).getRight())) {
        closestSuggestionPosition += 1;
        if (closestSuggestionPosition == suggestions.size()) {
          allSuggestionsPassed = true;
        }
      }

      insideSuggestion = !allSuggestionsPassed && reference.getLeft() >= suggestions.get(closestSuggestionPosition).getLeft();

      int inXMLMatchReferenceNo = Integer.parseInt(message.substring(reference.getLeft(), reference.getRight()).split("\\\\")[1]);
      int actualMatchReferenceNo = inXMLMatchReferenceNo - (insideSuggestion ? MATCHES_IN_SUGGESTIONS_NUMBERED_FROM : 0);

      String matchReferenceStringValue = matcher.group(actualMatchReferenceNo);
      if (matchReferenceStringValue == null) {
        matchReferenceStringValue = "";
      }

      Match currentProcessingMatch = matches.get(i);
      String regexReplace = currentProcessingMatch.getRegexReplace();
      String suggestion;
      if (regexReplace != null) {
        suggestion = currentProcessingMatch.getRegexMatch().matcher(matchReferenceStringValue).replaceFirst(regexReplace);
        suggestion = CaseConversionHelper.convertCase(currentProcessingMatch.getCaseConversionType(), suggestion, matchReferenceStringValue, getLanguage());
      } else {
        suggestion = matchReferenceStringValue;
      }
      processedMessage.append(message, startOfProcessingPart, reference.getLeft()).append(suggestion);

      startOfProcessingPart = reference.getRight();
    }
    processedMessage.append(message.substring(startOfProcessingPart));

    return processedMessage.toString();
  }

  @Override
  public int estimateContextForSureMatch() {
    return -1;
  }
  
  @Override
  public String toString() {
    return pattern.toString() + "/flags:" + pattern.flags();
  }

  /* (non-Javadoc)
   * @see org.languagetool.rules.patterns.AbstractPatternRule#getShortMessage()
   */
  @Override
  String getShortMessage() {
    return shortMessage;
  }
}
