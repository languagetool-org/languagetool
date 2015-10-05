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

import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matches 'regex' elements from XML rules against sentences.
 * @since 3.2
 */
class RegexPatternRule extends AbstractPatternRule implements RuleMatcher {

  private final Pattern pattern;

  RegexPatternRule(String id, String description, String message, String suggestionsOutMsg, Language language, Pattern regex) {
    super(id, description, language, regex);
    this.message = message;
    this.pattern = regex;
    this.suggestionsOutMsg = suggestionsOutMsg;
  }

  public Pattern getPattern() {
    return pattern;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentenceObj) throws IOException {
    String sentence = sentenceObj.getText();
    Matcher matcher = pattern.matcher(sentence);
    int startPos = 0;
    List<RuleMatch> matches = new ArrayList<>();
    while (matcher.find(startPos)) {
      String msg = replaceBackRefs(matcher, message);
      boolean sentenceStart = matcher.start(0) == 0;
      RuleMatch ruleMatch = new RuleMatch(this, matcher.start(), matcher.end(), msg, null, sentenceStart, null);
      List<String> matchSuggestions = getMatchSuggestions(sentence, matcher);
      List<String> allSuggestions = new ArrayList<>();
      if (matchSuggestions.size() > 0) {
        allSuggestions.addAll(matchSuggestions);
      } else {
        List<String> suggestions = extractSuggestions(matcher, msg);
        allSuggestions.addAll(suggestions);
        List<String> extendedSuggestions = extractSuggestions(matcher, getSuggestionsOutMsg());
        allSuggestions.addAll(extendedSuggestions);
      }
      ruleMatch.setSuggestedReplacements(allSuggestions);
      matches.add(ruleMatch);
      startPos = matcher.end();
    }
    return matches.toArray(new RuleMatch[matches.size()]);
  }

  @NotNull
  private List<String> getMatchSuggestions(String sentence, Matcher matcher) {
    List<String> matchSuggestions = new ArrayList<>();
    for (Match match : getSuggestionMatches()) {
      String errorText = sentence.substring(matcher.start(), matcher.end());
      String regexReplace = match.getRegexReplace();
      if (regexReplace != null) {
        String suggestion = match.getRegexMatch().matcher(errorText).replaceFirst(regexReplace);
        suggestion = CaseConversionHelper.convertCase(match.getCaseConversionType(), suggestion, errorText, getLanguage());
        matchSuggestions.add(suggestion);
      }
    }
    return matchSuggestions;
  }

  private List<String> extractSuggestions(Matcher matcher, String msg) {
    Pattern suggestionPattern = Pattern.compile("<suggestion>(.*?)</suggestion>");  // TODO: this needs to be clean up, there should be no need to parse this
    Matcher sMatcher = suggestionPattern.matcher(msg);
    int startPos = 0;
    List<String> result = new ArrayList<>();
    while (sMatcher.find(startPos)) {
      String suggestion = sMatcher.group(1);
      if (matcher.start() == 0) {
        result.add(replaceBackRefs(matcher, StringTools.uppercaseFirstChar(suggestion)));
      } else {
        result.add(replaceBackRefs(matcher, suggestion));
      }
      startPos = sMatcher.end();
    }
    return result;
  }

  private String replaceBackRefs(Matcher matcher, String msg) {
    String replacedMsg = msg;
    for (int i = 0; i <= matcher.groupCount(); i++) {
      String replacement = matcher.group(i);
      if (replacement != null) {
        replacedMsg = replacedMsg.replace("\\" + i, replacement);
      }
    }
    return replacedMsg;
  }

  @Override
  public String toString() {
    return pattern.toString() + "/flags:" + pattern.flags();
  }
}
