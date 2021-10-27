/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.server;

import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Tag;
import org.languagetool.rules.*;

import java.util.*;
import java.util.function.Predicate;

/**
 * Extend results by adding rules matches as hidden matches.
 * @since 4.0
 */
class ResultExtender {

  ResultExtender() {
  }

  /**
   * Filter {@code extensionMatches} so that only those matches are left that don't cover or touch one of the {@code matches}.
   */
  @NotNull
  static List<RuleMatch> getAsHiddenMatches(List<RuleMatch> matches, List<RuleMatch> extensionMatches) {
    List<RuleMatch> filteredExtMatches = new ArrayList<>();
    for (RuleMatch extensionMatch : extensionMatches) {
      Rule rule = extensionMatch.getRule();
      Predicate<RuleMatch> touchedByMatch = m ->
        extensionMatch.getFromPos() <= m.getToPos() && extensionMatch.getToPos() >= m.getFromPos();
      if (matches.stream().noneMatch(touchedByMatch)) {
        AnalyzedSentence sentence = extensionMatch.getSentence();
        String issueType = null;
        if (rule.getLocQualityIssueType() != null) {
          issueType = rule.getLocQualityIssueType().toString();
        }
        String categoryId = rule.getCategory().getId().toString();
        String categoryName = rule.getCategory().getName();
        HiddenRule hiddenRule = new HiddenRule(categoryId, categoryName, issueType, rule.getTags(), rule.estimateContextForSureMatch());
        RuleMatch hiddenRuleMatch = new RuleMatch(hiddenRule, sentence,
          extensionMatch.getFromPos(), extensionMatch.getToPos(), "(hidden message)");
        filteredExtMatches.add(hiddenRuleMatch);
      }
    }
    return filteredExtMatches;
  }

  static class HiddenRule extends Rule {
    final String categoryId;
    final String categoryName;
    final ITSIssueType itsType;
    final int estimatedContextForSureMatch;
    final List<Tag> tags;
    HiddenRule(String categoryId, String categoryName, String type, List<Tag> tags, int estimatedContextForSureMatch) {
      this.categoryId = categoryId;
      this.categoryName = categoryName;
      itsType = type != null ? ITSIssueType.getIssueType(type) : ITSIssueType.Uncategorized;
      this.estimatedContextForSureMatch = estimatedContextForSureMatch;
      this.tags = tags;
    }
    @NotNull
    public final Category getCategory() {
      return new Category(new CategoryId(categoryId), categoryName);
    }
    @Override
    public String getId() {
      return "HIDDEN_RULE";
    }
    @Override
    public ITSIssueType getLocQualityIssueType() {
      return itsType;
    }
    @Override
    public String getDescription() {
      return "(description hidden)";
    }
    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) {
      throw new RuntimeException("not implemented");
    }
    @Override
    public int estimateContextForSureMatch() {
      return estimatedContextForSureMatch;
    }
    @NotNull
    public List<Tag> getTags() {
      return tags == null ? Collections.emptyList() : tags;
    }
  }
}
