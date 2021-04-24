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

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
  public void testInvalidRequest() {
    ApiV2 apiV2 = new ApiV2(null, null);
    try {
      apiV2.handleRequest("unknown", new FakeHttpExchange(), null, null, null, new HTTPServerConfig());
      fail();
    } catch (Exception ignored) {}
  }
  
  @Test
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
  
}
