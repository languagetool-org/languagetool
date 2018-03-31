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
import org.languagetool.Experimental;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Write rule matches and some meta information as JSON.
 * @since 3.4, public since 3.6
 */
public class RuleMatchesAsJsonSerializer {

  private static final int API_VERSION = 1;
  private static final String STATUS = "";
  private static final String PREMIUM_HINT = "You might be missing errors only the Premium version can find. Please see https://languagetoolplus.com";
  private static final String START_MARKER = "__languagetool_start_marker";

  private final JsonFactory factory = new JsonFactory();
  
  public String ruleMatchesToJson(List<RuleMatch> matches, String text, int contextSize, Language lang) {
    return ruleMatchesToJson(matches, new ArrayList<>(), text, contextSize, lang, null);
  }

  /**
   * @param incompleteResultsReason use a string that explains why results are incomplete (e.g. due to a timeout) -
   *        a 'warnings' section will be added to the JSON. Use {@code null} if results are complete.
   * @since 3.7
   */
  @Experimental
  public String ruleMatchesToJson(List<RuleMatch> matches, List<RuleMatch> hiddenMatches, String text, int contextSize,
                                  Language lang, String incompleteResultsReason) {
    ContextTools contextTools = new ContextTools();
    contextTools.setEscapeHtml(false);
    contextTools.setContextSize(contextSize);
    contextTools.setErrorMarkerStart(START_MARKER);
    contextTools.setErrorMarkerEnd("");
    StringWriter sw = new StringWriter();
    try {
      try (JsonGenerator g = factory.createGenerator(sw)) {
        g.writeStartObject();
        writeSoftwareSection(g);
        writeWarningsSection(g, incompleteResultsReason);
        writeLanguageSection(g, lang);
        writeMatchesSection("matches", g, matches, text, contextTools);
        if (hiddenMatches != null && hiddenMatches.size() > 0) {
          writeMatchesSection("hiddenMatches", g, hiddenMatches, text, contextTools);
        }
        g.writeEndObject();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return sw.toString();
  }

  private void writeSoftwareSection(JsonGenerator g) throws IOException {
    g.writeObjectFieldStart("software");
    g.writeStringField("name", "LanguageTool");
    g.writeStringField("version", JLanguageTool.VERSION);
    g.writeStringField("buildDate", JLanguageTool.BUILD_DATE);
    g.writeNumberField("apiVersion", API_VERSION);
    g.writeBooleanField("premium", JLanguageTool.isPremiumVersion());
    if (!JLanguageTool.isPremiumVersion()) {
      g.writeStringField("premiumHint", PREMIUM_HINT);
    }
    g.writeStringField("status", STATUS);
    g.writeEndObject();
  }

  private void writeWarningsSection(JsonGenerator g, String incompleteResultsReason) throws IOException {
    g.writeObjectFieldStart("warnings");
    if (incompleteResultsReason != null) {
      g.writeBooleanField("incompleteResults", true);
      g.writeStringField("incompleteResultsReason", incompleteResultsReason);
    } else {
      g.writeBooleanField("incompleteResults", false);
    }
    g.writeEndObject();
  }

  private void writeLanguageSection(JsonGenerator g, Language lang) throws IOException {
    g.writeObjectFieldStart("language");
    g.writeStringField("name", lang.getName());
    g.writeStringField("code", lang.getShortCodeWithCountryAndVariant());
    g.writeEndObject();
  }

  private void writeMatchesSection(String sectionName, JsonGenerator g, List<RuleMatch> matches, String text, ContextTools contextTools) throws IOException {
    g.writeArrayFieldStart(sectionName);
    for (RuleMatch match : matches) {
      g.writeStartObject();
      g.writeStringField("message", cleanSuggestion(match.getMessage()));
      if (match.getShortMessage() != null) {
        g.writeStringField("shortMessage", cleanSuggestion(match.getShortMessage()));
      }
      writeReplacements(g, match);
      g.writeNumberField("offset", match.getFromPos());
      g.writeNumberField("length", match.getToPos()-match.getFromPos());
      writeContext(g, match, text, contextTools);
      writeRule(g, match);
      g.writeEndObject();
    }
    g.writeEndArray();
  }

  private String cleanSuggestion(String s) throws IOException {
    return s.replace("<suggestion>", "\"").replace("</suggestion>", "\"");
  }
  
  private void writeReplacements(JsonGenerator g, RuleMatch match) throws IOException {
    g.writeArrayFieldStart("replacements");
    for (String replacement : match.getSuggestedReplacements()) {
      g.writeStartObject();
      g.writeStringField("value", replacement);
      g.writeEndObject();
    }
    g.writeEndArray();
  }

  private void writeContext(JsonGenerator g, RuleMatch match, String text, ContextTools contextTools) throws IOException {
    String context = contextTools.getContext(match.getFromPos(), match.getToPos(), text);
    int contextOffset = context.indexOf(START_MARKER);
    context = context.replaceFirst(START_MARKER, "");
    g.writeObjectFieldStart("context");
    g.writeStringField("text", context);
    g.writeNumberField("offset", contextOffset);
    g.writeNumberField("length", match.getToPos()-match.getFromPos());
    g.writeEndObject();
    if (match.getSentence() != null) {
      g.writeStringField("sentence", match.getSentence().getText().trim());
    }
  }

  private void writeRule(JsonGenerator g, RuleMatch match) throws IOException {
    g.writeObjectFieldStart("rule");
    g.writeStringField("id", match.getRule().getId());
    if (match.getRule() instanceof AbstractPatternRule) {
      AbstractPatternRule pRule = (AbstractPatternRule) match.getRule();
      if (pRule.getSubId() != null) {
        g.writeStringField("subId", pRule.getSubId());
      }
    }
    g.writeStringField("description", match.getRule().getDescription());
    g.writeStringField("issueType", match.getRule().getLocQualityIssueType().toString());
    if (match.getUrl() != null || match.getRule().getUrl() != null) {
      g.writeArrayFieldStart("urls");  // currently only one, but keep it extensible
      g.writeStartObject();
      if (match.getUrl() != null) {
        g.writeStringField("value", match.getUrl().toString());
      } else {
        g.writeStringField("value", match.getRule().getUrl().toString());
      }
      g.writeEndObject();
      g.writeEndArray();
    }
    writeCategory(g, match.getRule().getCategory());
    g.writeEndObject();
  }

  private void writeCategory(JsonGenerator g, Category category) throws IOException {
    g.writeObjectFieldStart("category");
    CategoryId catId = category.getId();
    if (catId != null) {
      g.writeStringField("id", catId.toString());
      g.writeStringField("name", category.getName());
    }
    g.writeEndObject();
  }

}
