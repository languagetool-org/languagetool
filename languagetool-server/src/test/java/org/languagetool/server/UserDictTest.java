/* LanguageTool, a natural language style checker 
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class UserDictTest {

  static final String USERNAME1 = "test@test.de";
  static final String API_KEY1 = "foo";
  static final String USERNAME2 = "two@test.de";
  static final String API_KEY2 = "foo-two";

  @Test
  public void testHTTPServer() throws Exception {
    HTTPServerConfig config = new HTTPServerConfig(HTTPTools.getDefaultPort());
    config.setDatabaseDriver("org.hsqldb.jdbcDriver");
    config.setDatabaseUrl("jdbc:hsqldb:mem:testdb");
    config.setDatabaseUsername("");
    config.setDatabasePassword("");
    config.setSecretTokenKey("myfoo");
    config.setCacheSize(100);
    DatabaseAccess.init(config);
    // no need to also create test tables for logging
    DatabaseLogger.getInstance().disableLogging();
    try {
      DatabaseAccess.createAndFillTestTables();
      HTTPServer server = new HTTPServer(config);
      try {
        server.run();
        Language enUS = Languages.getLanguageForShortCode("en-US");
        runTests(enUS, "This is Mysurname.", "This is Mxsurname.", "Mysurname", "MORFOLOGIK_RULE_EN_US");
        runTests(enUS, "Mysurname is my name.", "Mxsurname is my name.", "Mysurname", "MORFOLOGIK_RULE_EN_US");
        Language deDE = Languages.getLanguageForShortCode("de-DE");
        runTests(deDE, "Das ist Meinname.", "Das ist Mxinname.", "Meinname", "GERMAN_SPELLER_RULE");
        runTests(deDE, "Meinname steht hier.", "Mxinname steht hier.", "Meinname", "GERMAN_SPELLER_RULE");
        runTests(deDE, "Hier steht Schöckl.", "Das ist Schückl.", "Schöckl", "GERMAN_SPELLER_RULE");
        String res = check(deDE, "Hier steht Schockl", USERNAME1, API_KEY1);
        assertThat(StringUtils.countMatches(res, "GERMAN_SPELLER_RULE"), is (1));  // 'Schöckl' accepted, but not 'Schockl' (NOTE: depends on encoding/collation of database) 
        try {
          System.out.println("=== Testing multi word insertion now, ignore stack trace: ===");
          addWord("multi word", USERNAME1, API_KEY1);
          fail("Should not be able to insert multi words");
        } catch (IOException ignore) {}
      } finally {
        server.stop();
      }
    } finally {
      DatabaseAccess.deleteTestTables();
    }
  }

  private void runTests(Language lang, String input, String inputWithTypo, String name, String errorRuleId) throws Exception {
    assertRuleMatch(1, input, lang, errorRuleId, USERNAME1, API_KEY1);
    assertRuleMatch(1, input, lang, errorRuleId, USERNAME2, API_KEY2);
    assertRuleMatch(1, input, lang, errorRuleId, null, null);  // anonymous user
    assertThat(getWords(USERNAME1, API_KEY1).toString(), is("[]"));
    addWord(name, USERNAME1, API_KEY1);
    assertThat(getWords(USERNAME1, API_KEY1).toString(), is("[" + name + "]"));
    assertThat(getWords(USERNAME2, API_KEY2).toString(), is("[]"));
    assertRuleMatch(0, input, lang, errorRuleId, USERNAME1, API_KEY1);
    assertRuleMatch(1, input, lang, errorRuleId, USERNAME2, API_KEY2);  // cache must not mix up users
    assertRuleMatch(1, input, lang, errorRuleId, null, null);  // anonymous user
    assertRuleMatch(0, input, lang, errorRuleId, USERNAME1, API_KEY1);
    String json = assertRuleMatch(1, inputWithTypo, lang, errorRuleId, USERNAME1, API_KEY1);
    assertTrue("Missing suggestion '" + name + "': " + json, json.contains("\"" + name + "\"") || json.contains("\"" + name + ".\""));
    deleteWord(name, USERNAME1, API_KEY1);
    assertThat(getWords(USERNAME1, API_KEY1).toString(), is("[]"));
    assertRuleMatch(1, input, lang, errorRuleId, USERNAME1, API_KEY1);
    assertRuleMatch(1, input, lang, errorRuleId, USERNAME2, API_KEY2);
    assertRuleMatch(1, input, lang, errorRuleId, null, null);
  }

  private String assertRuleMatch(int expectedTypoCount, String input, Language lang, String errorRuleId, String username, String apiKey) throws IOException {
    String json = check(lang, input, username, apiKey);
    int realTypoCount = StringUtils.countMatches(json, errorRuleId);
    //System.out.println(json);
    assertThat("Expected " + expectedTypoCount + " rule matches (id " + errorRuleId + ") for '" + input + "', got " +
               realTypoCount, realTypoCount, is(expectedTypoCount));
    return json;
  }

  private String check(Language lang, String text, String username, String apiKey) throws IOException {
    String urlOptions = "?language=" + lang.getShortCodeWithCountryAndVariant();
    urlOptions += "&text=" + URLEncoder.encode(text, "UTF-8");
    if (username != null && apiKey != null) {
      urlOptions += "&username=" + URLEncoder.encode(username, "UTF-8");
      urlOptions += "&apiKey=" + URLEncoder.encode(apiKey, "UTF-8");
    }
    URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + "/v2/check" + urlOptions);
    return HTTPTools.checkAtUrl(url);
  }

  private List<String> getWords(String username, String apiKey) throws IOException {
    URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + "/v2/words?username=" + username + "&apiKey=" + apiKey);
    ObjectMapper mapper = new ObjectMapper();
    String result = HTTPTools.checkAtUrl(url);
    JsonNode data = mapper.readTree(result);
    JsonNode list = data.get("words");
    List<String> words = new ArrayList<>();
    for (JsonNode jsonNode : list) {
      words.add(jsonNode.asText());
    }
    return words;
  }
  
  private String addWord(String word, String username, String apiKey) throws IOException {
    URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + "/v2/words/add");
    return HTTPTools.checkAtUrlByPost(url, "word=" + word + "&username=" + username + "&apiKey=" + apiKey);
  }  
  
  private void deleteWord(String word, String username, String apiKey) throws IOException {
    URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + "/v2/words/delete");
    HTTPTools.checkAtUrlByPost(url, "word=" + word + "&username=" + username + "&apiKey=" + apiKey);
  }
  
}
