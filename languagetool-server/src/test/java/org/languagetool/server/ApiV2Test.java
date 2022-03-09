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
package org.languagetool.server;

import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.CheckResults;
import org.languagetool.DetectedLanguage;
import org.languagetool.FakeLanguage;
import org.languagetool.Language;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.*;

public class ApiV2Test {

  @Test
  public void testLanguages() throws IOException {
    String json = new ApiV2(null, null).getLanguages();
    assertTrue(json.contains("\"German (Germany)\""));
    assertTrue(json.contains("\"de\""));
    assertTrue(json.contains("\"de-DE\""));
    assertTrue(StringUtils.countMatches(json, "\"name\"") >= 43);
  }
  
  @Test
  public void testInvalidRequest() throws Exception {
    ApiV2 apiV2 = new ApiV2(null, null);
    try {
      apiV2.handleRequest("unknown", new FakeHttpExchange(), null, null, null, new HTTPServerConfig());
      fail();
    } catch (PathNotFoundException ignored) {}
  }
  
  @Test
  public void testInvalidJsonRequest() throws Exception {
    ApiV2 apiV2 = new ApiV2(new FakeTextChecker(new HTTPServerConfig(), false, null, new RequestCounter()), null);
    try {
      Map<String, String> params = new HashMap<>();
      params.put("data", "{\"annotation\":{\"text\": \"A \"}]}");
      apiV2.handleRequest("check", new FakeHttpExchange(), params, null, null, new HTTPServerConfig());
      fail();
    } catch (BadRequestException ignored) {}
  }
  
  @Test
  @Ignore("code is currently commented out")
  public void testRuleExamples() throws Exception {
    HTTPServerConfig config = new HTTPServerConfig();
    ApiV2 apiV2 = new ApiV2(new V2TextChecker(config, false, new LinkedBlockingQueue<>(), new RequestCounter()), null);
    FakeHttpExchange httpExchange = new FakeHttpExchange();
    Map<String, String> params = new HashMap<>();
    params.put("lang", "en");
    params.put("ruleId", "EN_A_VS_AN");
    apiV2.handleRequest("rule/examples", httpExchange, params, null, null, config);
    //System.out.println(httpExchange.getOutput());
    assertTrue(httpExchange.getOutput().contains("The train arrived <marker>an hour</marker> ago."));
    assertTrue(httpExchange.getOutput().contains("The train arrived <marker>a hour</marker> ago."));
  }
  
  static class FakeTextChecker extends TextChecker {
    FakeTextChecker(HTTPServerConfig config, boolean internalServer, Queue<Runnable> workQueue, RequestCounter reqCounter) {
      super(config, internalServer, workQueue, reqCounter);
    }
    @Override
    protected void setHeaders(HttpExchange httpExchange) {
    }
    @Override
    protected String getResponse(AnnotatedText text, Language language, DetectedLanguage lang, Language motherTongue, List<CheckResults> matches, List<RuleMatch> hiddenMatches, String incompleteResultReason, int compactMode, boolean showPremiumHint) {
      return "";
    }
    @NotNull
    @Override
    protected List<String> getPreferredVariants(Map<String, String> parameters) {
      return new ArrayList<>();
    }
    @Override
    protected DetectedLanguage getLanguage(String text, Map<String, String> parameters, List<String> preferredVariants, List<String> additionalDetectLangs, List<String> preferredLangs, boolean testMode) {
      return new DetectedLanguage(new FakeLanguage(), new FakeLanguage());
    }
    @Override
    protected boolean getLanguageAutoDetect(Map<String, String> parameters) {
      return false;
    }
    @NotNull
    @Override
    protected List<String> getEnabledRuleIds(Map<String, String> parameters) {
      return new ArrayList<>();
    }
    @NotNull
    @Override
    protected List<String> getDisabledRuleIds(Map<String, String> parameters) {
      return new ArrayList<>();
    }
  }
}
