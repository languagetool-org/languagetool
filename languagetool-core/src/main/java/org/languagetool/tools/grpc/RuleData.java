/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2025 Stefan Viol (https://stevio.de)
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

package org.languagetool.tools.grpc;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Tag;
import org.languagetool.rules.*;
import org.languagetool.rules.ml.MLServerProto;
import org.languagetool.tools.Cache.ProtoResultCache;

import java.io.IOException;

import static org.languagetool.tools.grpc.ProtoHelper.emptyAsNull;

public class RuleData extends Rule {

  private final String id;
  private final String subId;
  private final String description;
  private final int estimateContextForSureMatch;
  private final String sourceFile;

  public RuleData(MLServerProto.Match match) {
    this.id = match.getId();
    this.subId = match.getSubId();
    this.description = match.getRuleDescription();
    this.estimateContextForSureMatch = match.getContextForSureMatch();
    this.sourceFile = match.getRule().getSourceFile();
    // keep default values from Rule baseclass
    if (!match.getRule().getIssueType().isEmpty()) {
      setLocQualityIssueType(ITSIssueType.valueOf(match.getRule().getIssueType()));
    }
    if (match.getRule().getTempOff()) {
      setDefaultTempOff();
    }
    if (match.getRule().hasCategory()) {
      Category c = new Category(new CategoryId(match.getRule().getCategory().getId()),
        match.getRule().getCategory().getName());
      setCategory(c);
    }
    setPremium(match.getRule().getIsPremium());
    setTags(match.getRule().getTagsList().stream().map(t -> Tag.valueOf(t.name())).toList());
  }

  public RuleData(ProtoResultCache.CachedRule rule) {
    this.id = rule.getId();
    this.subId = rule.getSubId();
    this.description = rule.getDescription();
    this.estimateContextForSureMatch = rule.getEstimateContextForSureMatch();
    this.sourceFile = rule.getSourceFile();
    if (!rule.getIssueType().isEmpty()) {
      setLocQualityIssueType(ITSIssueType.valueOf(rule.getIssueType()));
    }
    if (rule.getTempOff()) {
      setDefaultTempOff();
    }
    if (rule.hasCategory()) {
      Category c = new Category(new CategoryId(rule.getCategory().getId()),
        rule.getCategory().getName());
      setCategory(c);
    }
    setPremium(rule.getIsPremium());
    setTags(rule.getTagsList().stream().map(t -> Tag.valueOf(t.name())).toList());
  }

  @Nullable
  @Override
  public String getSourceFile() {
    return emptyAsNull(sourceFile);
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public String getSubId() {
    return emptyAsNull(this.subId);
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public int estimateContextForSureMatch() {
    // 0 is okay as default value
    return this.estimateContextForSureMatch;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    throw new UnsupportedOperationException(
      "Not implemented; internal class used for returning match" + " information from remote endpoint");
  }
}
