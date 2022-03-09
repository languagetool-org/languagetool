/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tools;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.languagetool.*;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Write rule matches and some meta information as JSON.
 * @since 3.4, public since 3.6
 */
public class RuleMatchesAsJsonSerializer {

  private static final int API_VERSION = 1;
  private static final String STATUS = "";
  private static final String PREMIUM_HINT = "You might be missing errors only the Premium version can find. Contact us at support<at>languagetoolplus.com.";
  private static final String START_MARKER = "__languagetool_start_marker";
  private static final JsonFactory factory = new JsonFactory();
  
  private final int compactMode;
  private final Language lang;

  public RuleMatchesAsJsonSerializer() {
    this(0, null);
  }

  /**
   * @since 4.7
   */
  public RuleMatchesAsJsonSerializer(int compactMode) {
    this(compactMode, null);
  }

  /**
   * @since 5.1
   */
  public RuleMatchesAsJsonSerializer(int compactMode, Language lang) {
    this.compactMode = compactMode;
    this.lang = lang;
  }

  public String ruleMatchesToJson(List<RuleMatch> matches, String text, int contextSize, DetectedLanguage detectedLang) {
    return ruleMatchesToJson(matches, new ArrayList<>(), text, contextSize, detectedLang, null);
  }

  /**
   * @param incompleteResultsReason use a string that explains why results are incomplete (e.g. due to a timeout) -
   *        a 'warnings' section will be added to the JSON. Use {@code null} if results are complete.
   * @since 3.7
   */
  public String ruleMatchesToJson(List<RuleMatch> matches, List<RuleMatch> hiddenMatches, String text, int contextSize,
                                  DetectedLanguage detectedLang, String incompleteResultsReason) {
    return ruleMatchesToJson(matches, hiddenMatches, new AnnotatedTextBuilder().addText(text).build(), contextSize, detectedLang, incompleteResultsReason, false);
  }

  /**
   * @param incompleteResultsReason use a string that explains why results are incomplete (e.g. due to a timeout) -
   *        a 'warnings' section will be added to the JSON. Use {@code null} if results are complete.
   * @since 4.3
   */
  public String ruleMatchesToJson(List<RuleMatch> matches, List<RuleMatch> hiddenMatches, AnnotatedText text, int contextSize,
                                  DetectedLanguage detectedLang, String incompleteResultsReason, boolean showPremiumHint) {
    return ruleMatchesToJson2(Collections.singletonList(new CheckResults(matches, Collections.emptyList())),
            hiddenMatches, text, contextSize, detectedLang, incompleteResultsReason, showPremiumHint);
  }

    /**
     * @param incompleteResultsReason use a string that explains why results are incomplete (e.g. due to a timeout) -
     *        a 'warnings' section will be added to the JSON. Use {@code null} if results are complete.
     * @since 5.3
     */
  public String ruleMatchesToJson2(List<CheckResults> res, List<RuleMatch> hiddenMatches, AnnotatedText text, int contextSize,
                                   DetectedLanguage detectedLang, String incompleteResultsReason, boolean showPremiumHint) {
    ContextTools contextTools = new ContextTools();
    contextTools.setEscapeHtml(false);
    contextTools.setContextSize(contextSize);
    contextTools.setErrorMarker(START_MARKER, "");
    StringWriter sw = new StringWriter();
    try {
      try (JsonGenerator g = factory.createGenerator(sw)) {
        g.writeStartObject();
        writeSoftwareSection(g, showPremiumHint);
        writeWarningsSection(g, incompleteResultsReason);
        writeLanguageSection(g, detectedLang);
        writeMatchesSection("matches", g, res, text, contextTools);
        if (hiddenMatches != null && hiddenMatches.size() > 0) {
          writeMatchesSection("hiddenMatches", g, Collections.singletonList(new CheckResults(hiddenMatches, Collections.emptyList())), text, contextTools);
        }
        writeIgnoreRanges(g, res);
        g.writeEndObject();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return sw.toString();
  }

  private void writeSoftwareSection(JsonGenerator g, boolean showPremiumHint) throws IOException {
    if (compactMode == 1) {
      return;
    }
    g.writeObjectFieldStart("software");
    g.writeStringField("name", "LanguageTool");
    g.writeStringField("version", JLanguageTool.VERSION);
    g.writeStringField("buildDate", JLanguageTool.BUILD_DATE);
    g.writeNumberField("apiVersion", API_VERSION);
    g.writeBooleanField("premium", Premium.isPremiumVersion());
    if (showPremiumHint) {
      g.writeStringField("premiumHint", PREMIUM_HINT);
    }
    g.writeStringField("status", STATUS);
    g.writeEndObject();
  }

  private void writeWarningsSection(JsonGenerator g, String incompleteResultsReason) throws IOException {
    if (compactMode == 1 && incompleteResultsReason == null) {
      return;
    }
    g.writeObjectFieldStart("warnings");
    if (incompleteResultsReason != null) {
      g.writeBooleanField("incompleteResults", true);
      g.writeStringField("incompleteResultsReason", incompleteResultsReason);
    } else {
      g.writeBooleanField("incompleteResults", false);
    }
    g.writeEndObject();
  }

  private void writeLanguageSection(JsonGenerator g, DetectedLanguage detectedLang) throws IOException {
    g.writeObjectFieldStart("language");
    g.writeStringField("name", detectedLang.getGivenLanguage().getName());
    g.writeStringField("code", detectedLang.getGivenLanguage().getShortCodeWithCountryAndVariant());
    if (detectedLang.getGivenLanguage().isSpellcheckOnlyLanguage()) {
      g.writeBooleanField("spellCheckOnly", true);
    }
    g.writeObjectFieldStart("detectedLanguage");
    g.writeStringField("name", detectedLang.getDetectedLanguage().getName());
    g.writeStringField("code", detectedLang.getDetectedLanguage().getShortCodeWithCountryAndVariant());
    g.writeNumberField("confidence", detectedLang.getDetectionConfidence());
    if (detectedLang.getDetectedLanguage().isSpellcheckOnlyLanguage()) {
      g.writeBooleanField("spellCheckOnly", true);
    }
    g.writeEndObject();
    g.writeEndObject();
  }

  private void writeMatchesSection(String sectionName, JsonGenerator g, List<CheckResults> res, AnnotatedText text, ContextTools contextTools) throws IOException {
    g.writeArrayFieldStart(sectionName);
    for (CheckResults r : res) {
      for (RuleMatch match : r.getRuleMatches()) {
        g.writeStartObject();
        g.writeStringField("message", cleanSuggestion(match.getMessage()));
        if (match.getShortMessage() != null) {
          g.writeStringField("shortMessage", cleanSuggestion(match.getShortMessage()));
        }
        writeReplacements(g, match);
        g.writeNumberField("offset", match.getFromPos());
        g.writeNumberField("length", match.getToPos()-match.getFromPos());
        writeContext(g, match, text, contextTools);
        g.writeObjectFieldStart("type");
        g.writeStringField("typeName", match.getType().toString());
        g.writeEndObject();
        writeRule(g, match);
        // 3 is a guess - key 'ignoreForIncompleteSentence' isn't official and can hopefully be removed in the future
        // now that we have 'contextForSureMatch':
        int contextEstimate = match.getRule().estimateContextForSureMatch();
        g.writeBooleanField("ignoreForIncompleteSentence", contextEstimate == -1 || contextEstimate > 3);
        g.writeNumberField("contextForSureMatch", contextEstimate);
        g.writeEndObject();
      }
    }
    g.writeEndArray();
  }

  private void writeIgnoreRanges(JsonGenerator g, List<CheckResults> res) throws IOException {
    if (res.stream().allMatch(k -> k.getIgnoredRanges().size() == 0)) {
      return;
    }
    g.writeArrayFieldStart("ignoreRanges");
    for (CheckResults r : res) {
      for (Range range : r.getIgnoredRanges()) {
        g.writeStartObject();
        g.writeNumberField("from", range.getFromPos());
        g.writeNumberField("to", range.getToPos());
        g.writeObjectFieldStart("language");
        g.writeStringField("code", range.getLang());
        g.writeEndObject();
        g.writeEndObject();
      }
    }
    g.writeEndArray();
  }

  private String cleanSuggestion(String s) {
    if (lang != null) {
      return lang.toAdvancedTypography(s); //.replaceAll("<suggestion>", lang.getOpeningDoubleQuote()).replaceAll("</suggestion>", lang.getClosingDoubleQuote())
    } else {
      return s.replace("<suggestion>", "\"").replace("</suggestion>", "\"");
    }
  }
  
  private void writeReplacements(JsonGenerator g, RuleMatch match) throws IOException {
    g.writeArrayFieldStart("replacements");
    boolean autoCorrect = match.isAutoCorrect();
    int i = 0;
    for (SuggestedReplacement replacement : match.getSuggestedReplacementObjects()) {
      i++;
      if (compactMode == 1 && i > 5) {  // these clients only show up to 5 suggestions anyway
        break;
      }
      g.writeStartObject();
      g.writeStringField("value", replacement.getReplacement());
      if (replacement.getShortDescription() != null) {
        g.writeStringField("shortDescription", replacement.getShortDescription());
      }
      if (replacement.getSuffix() != null) {
        g.writeStringField("suffix", replacement.getSuffix());
      }
      if (replacement.getType() != SuggestedReplacement.SuggestionType.Default) {
        g.writeStringField("type", replacement.getType().name());
      }
      if (autoCorrect) {
        g.writeBooleanField("autoCorrect", true);
        autoCorrect = false; // only for first replacement
      }
      if (replacement.getConfidence() != null) {
        g.writeNumberField("confidence", replacement.getConfidence());
      }
      g.writeEndObject();
    }
    g.writeEndArray();
  }

  private void writeContext(JsonGenerator g, RuleMatch match, AnnotatedText text, ContextTools contextTools) throws IOException {
    String context = contextTools.getContext(match.getFromPos(), match.getToPos(), text.getTextWithMarkup());
    int contextOffset = context.indexOf(START_MARKER);
    context = context.replaceFirst(START_MARKER, "");
    if (compactMode != 1) {
      g.writeObjectFieldStart("context");
      g.writeStringField("text", context);
      g.writeNumberField("offset", contextOffset);
      g.writeNumberField("length", match.getToPos()-match.getFromPos());
      g.writeEndObject();
      if (match.getSentence() != null) {
        g.writeStringField("sentence", match.getSentence().getText().trim());
      }
    }
  }

  private void writeRule(JsonGenerator g, RuleMatch match) throws IOException {
    g.writeObjectFieldStart("rule");
    Rule rule = match.getRule();
    g.writeStringField("id", match.getSpecificRuleId()); // rule.getId()
    if (rule instanceof AbstractPatternRule) {
      AbstractPatternRule pRule = (AbstractPatternRule) rule;
      if (pRule.getSubId() != null) {
        g.writeStringField("subId", pRule.getSubId());
      }
      if (pRule.getSourceFile() != null && compactMode != 1) {
        g.writeStringField("sourceFile", pRule.getSourceFile().replaceFirst(".*/", ""));
      }
    }
    g.writeStringField("description", rule.getDescription());
    g.writeStringField("issueType", rule.getLocQualityIssueType().toString());
    if (rule.isDefaultTempOff()) {
      g.writeBooleanField("tempOff", true);
    }
    if (match.getUrl() != null || rule.getUrl() != null) {
      g.writeArrayFieldStart("urls");  // currently only one, but keep it extensible
      g.writeStartObject();
      if (match.getUrl() != null) {
        g.writeStringField("value", match.getUrl().toString());
      } else if (rule.getUrl() != null) {
        g.writeStringField("value", rule.getUrl().toString());
      }
      g.writeEndObject();
      g.writeEndArray();
    }
    writeCategory(g, rule.getCategory());
    if (Premium.isPremiumVersion()) {
      g.writeBooleanField("isPremium", Premium.get().isPremiumRule(rule));
    }
    if (rule.getTags().size() > 0) {
      g.writeArrayFieldStart("tags");
      for (Tag tag : rule.getTags()) {
        g.writeString(tag.name());
      }
      g.writeEndArray();
    }
    g.writeEndObject();
  }

  private void writeCategory(JsonGenerator g, Category category) throws IOException {
    g.writeObjectFieldStart("category");
    CategoryId catId = category.getId();
    g.writeStringField("id", catId.toString());
    g.writeStringField("name", category.getName());
    g.writeEndObject();
  }

}
