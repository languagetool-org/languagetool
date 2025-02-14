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

package org.languagetool.tools.Cache;


import lombok.extern.slf4j.Slf4j;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Premium;
import org.languagetool.rules.GRPCUtils;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.ml.MLServerProto;
import org.languagetool.tools.grpc.RuleData;


import java.net.MalformedURLException;
import java.net.URL;

import static org.languagetool.tools.grpc.ProtoHelper.getUrl;
import static org.languagetool.tools.grpc.ProtoHelper.nullAsEmpty;

@Slf4j
public final class CacheUtils {

  private CacheUtils() {
  }

  public static ProtoResultCache.CachedResultMatch serializeResultMatch(RuleMatch ruleMatch) {
    return ProtoResultCache.CachedResultMatch.newBuilder()
      .setRule(
        ProtoResultCache.CachedRule.newBuilder()
          .setId(ruleMatch.getRule().getId())
          .setSubId(nullAsEmpty(ruleMatch.getRule().getSubId()))
          .setDescription(nullAsEmpty(ruleMatch.getRule().getDescription()))
          .setEstimateContextForSureMatch(ruleMatch.getRule().estimateContextForSureMatch())
          .setSourceFile(nullAsEmpty(ruleMatch.getRule().getSourceFile()))
          .setIssueType(ruleMatch.getRule().getLocQualityIssueType().name())
          .setTempOff(ruleMatch.getRule().isDefaultTempOff())
          .setCategory(MLServerProto.RuleCategory.newBuilder()
            .setId(ruleMatch.getRule().getCategory().getId().toString())
            .setName(ruleMatch.getRule().getCategory().getName())
            .build())
          .setIsPremium(Premium.get().isPremiumRule(ruleMatch.getRule()))
          .addAllTags(ruleMatch.getRule().getTags().stream()
            .map(t -> ProtoResultCache.CachedRule.Tag.valueOf(t.name())).toList())
          .build()
      )
      .setMessage(ruleMatch.getMessage())
      .setShortMessage(ruleMatch.getShortMessage())
      .setOffsetPosition(
        ProtoResultCache.MatchPosition.newBuilder()
          .setStart(ruleMatch.getFromPos())
          .setEnd(ruleMatch.getToPos())
          .build()
      )
      .setPatternPosition(
        ProtoResultCache.MatchPosition.newBuilder()
          .setStart(ruleMatch.getPatternFromPos())
          .setEnd(ruleMatch.getPatternToPos())
          .build()
      )
      .setSentencePosition(
        ProtoResultCache.MatchPosition.newBuilder()
          .setStart(ruleMatch.getFromPosSentence())
          .setEnd(ruleMatch.getToPosSentence())
          .build()
      )
      .addAllSuggestedReplacements(ruleMatch.getSuggestedReplacementObjects().stream()
        .map(GRPCUtils::toGRPC).toList())
      .setUrl(getUrl(ruleMatch))
      .setType(ProtoResultCache.CachedResultMatch.MatchType.valueOf(ruleMatch.getType().name()))
      .setAutoCorrect(ruleMatch.isAutoCorrect())
      .addAllNewLanguageMatches(
        ruleMatch.getNewLanguageMatches().entrySet().stream()
          .map(stringFloatEntry -> ProtoResultCache.NewLanguageMatch.newBuilder()
            .setLanguageCode(stringFloatEntry.getKey())
            .setConfidence(stringFloatEntry.getValue())
            .build()
          ).toList()
      )
      .setSpecificRuleId(ruleMatch.getSpecificRuleId())
      .setOriginalErrorString(ruleMatch.getOriginalErrorStr())
      .build();
  }

  public static RuleMatch deserializeResultMatch(ProtoResultCache.CachedResultMatch cachedResultMatch, AnalyzedSentence analyzedSentence) {
    Rule rule = new RuleData(cachedResultMatch.getRule());
    RuleMatch ruleMatch = new RuleMatch(rule, analyzedSentence, cachedResultMatch.getOffsetPosition().getStart(), cachedResultMatch.getOffsetPosition().getEnd(), cachedResultMatch.getMessage(), cachedResultMatch.getShortMessage());
    ruleMatch.setSuggestedReplacementObjects(cachedResultMatch.getSuggestedReplacementsList().stream().map(GRPCUtils::fromGRPC).toList());
    ruleMatch.setAutoCorrect(cachedResultMatch.getAutoCorrect());
    ruleMatch.setType(RuleMatch.Type.valueOf(cachedResultMatch.getType().name()));
    ruleMatch.setSentencePosition(cachedResultMatch.getSentencePosition().getStart(), cachedResultMatch.getSentencePosition().getEnd());
    ruleMatch.setPatternPosition(cachedResultMatch.getPatternPosition().getStart(), cachedResultMatch.getPatternPosition().getEnd());
    ruleMatch.setOriginalErrorStr(cachedResultMatch.getOriginalErrorString());
    if (!cachedResultMatch.getUrl().isEmpty()) {
      try {
        ruleMatch.setUrl(new URL(cachedResultMatch.getUrl()));
      } catch (MalformedURLException e) {
        log.warn("Got invalid URL from ProtoResultCache.CachedResultMatch: {}", e);
      }
    }
    return ruleMatch;
  }
}
