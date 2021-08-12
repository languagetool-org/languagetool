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
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.languagetool.*;
import org.languagetool.rules.spelling.morfologik.MorfologikMultiSpeller;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class UserDictTest {

  public static final String TOKEN_V2_1 = "4472b043ce935018e1a5bf5ef4b8a21b";
  public static final Long USER_GROUP_1 = 1L;
  public static final String NAME1 = "One";

  protected static final String USERNAME1 = "test@test.de";
  protected static final String API_KEY1 = "foo";
  protected static final long USER_ID1 = 1;
  protected static final Date PREMIUM_FROM1 = Date.valueOf("1970-01-01");
  protected static final String NAME2 = "Two";
  protected static final String USERNAME2 = "two@test.de";
  protected static final String API_KEY2 = "foo-two";
  protected static final Date PREMIUM_FROM2 = Date.valueOf("2000-01-01");
  protected static final long USER_ID2 = 2;
  protected static final String USERNAME3 = "free-account@example.com";
  protected static final String NAME3 = "Three";
  protected static final String PASSWORD3 = "password";
  protected static final long USER_ID3 = 3;
  protected static final String apiEndpoint = "http://localhost:" + HTTPTools.getDefaultPort();

  @Test
  public void testMorfologikDictCache() throws IOException {
    // see https://github.com/languagetooler-gmbh/languagetool-premium/issues/2093
    // caching dictionaries and re-use across languages could make results disappear, fixed now
    UserConfig config = new UserConfig(asList("foo", "bar"), Collections.emptyMap(), 0, 1234L,
      "default", 100000L, null);
    JLanguageTool frenchLT = new JLanguageTool(Languages.getLanguageForShortCode("fr"), null, config);
    JLanguageTool englishLT = new JLanguageTool(Languages.getLanguageForShortCode("en-US"), null, config);

    String s = "2èmex";
    assertFalse("Produces spelling match", frenchLT.check(s).isEmpty());
    englishLT.check(s);
    assertFalse("Produces spelling match", frenchLT.check(s).isEmpty());

    MorfologikMultiSpeller.clearUserDictCache();
    frenchLT = new JLanguageTool(Languages.getLanguageForShortCode("fr"), null, config);
    englishLT = new JLanguageTool(Languages.getLanguageForShortCode("en-US"), null, config);

    englishLT.check(s);
    assertFalse("Produces spelling match", frenchLT.check(s).isEmpty());
  }

  protected void run() throws Exception {
    Language enUS = Languages.getLanguageForShortCode("en-US");
    testSentence(enUS, "This is Mysurname.", "This is Mxsurname.", "Mysurname", "MORFOLOGIK_RULE_EN_US");
    testSentence(enUS, "Mysurname is my name.", "Mxsurname is my name.", "Mysurname", "MORFOLOGIK_RULE_EN_US");
    Language deDE = Languages.getLanguageForShortCode("de-DE");
    testSentence(deDE, "Das ist Meinname.", "Das ist Mxinname.", "Meinname", "GERMAN_SPELLER_RULE");
    testSentence(deDE, "Meinname steht hier.", "Mxinname steht hier.", "Meinname", "GERMAN_SPELLER_RULE");
    testSentence(deDE, "Hier steht Schöckl.", "Das ist Schückl.", "Schöckl", "GERMAN_SPELLER_RULE");
    String res = check(deDE, "Hier steht Schockl", USERNAME1, API_KEY1, null);
    assertThat(StringUtils.countMatches(res, "GERMAN_SPELLER_RULE"), is(1));  // 'Schöckl' accepted, but not 'Schockl' (NOTE: depends on encoding/collation of database)
    try {
      System.out.println("=== Testing multi word insertion now, ignore stack trace: ===");
      addWord("multi word", USERNAME1, API_KEY1, null);
      fail("Should not be able to insert multi words");
    } catch (IOException ignore) {
    }
    testUserDictGroups();
    testTokenAuth();
  }

  private void testTokenAuth() throws Exception {
    Language lang = Languages.getLanguageForShortCode("en-US");
    String word = "Tokenauthtest";
    String dict = null;//"testTokenAuth";
    String text = word + " is in my dictionary.";
    String urlOptions = "?language=" + lang.getShortCodeWithCountryAndVariant();
    // see TextCheckerTest.makeToken()
    String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjEsInByZW1pdW0iOnRydWUsImlzcyI6Imh0dHA6Ly9mb29iYXIiLCJpYXQiOjE1NDIyOTcwMTMsIm1heFRleHRMZW5ndGgiOjUwMDB9.ruvsjcWftFy8wZmL08q_1LfnWefzP8aMvWG83Z63znA";
    urlOptions += "&text=" + URLEncoder.encode(text, "UTF-8");
    urlOptions += "&token=" + URLEncoder.encode(token, "UTF-8");
    //urlOptions += "&dicts=" + dict;
    URL url = new URL(apiEndpoint + "/v2/check" + urlOptions);
    String responseBefore = HTTPTools.checkAtUrl(url);
    assertEquals("user dictionaries are available with token authentification (word not added)",
      1, StringUtils.countMatches(responseBefore, "MORFOLOGIK_RULE_EN_US"));
    addWord(word, USERNAME1, API_KEY1, dict);
    String responseAfter = HTTPTools.checkAtUrl(url);
    assertEquals("user dictionaries are available with token authentification (word added)",
      0, StringUtils.countMatches(responseAfter, "MORFOLOGIK_RULE_EN_US"));
  }

  @Test
  public void testHTTPServer() throws Exception {
    HTTPServerConfig config = getHttpServerConfig();
    DatabaseAccess.init(config);
    // no need to also create test tables for logging
    DatabaseLogger.getInstance().disableLogging();
    try {
      DatabaseAccess.deleteTestTables();
      DatabaseAccess.createAndFillTestTables();
      HTTPServer server = new HTTPServer(config);
      try {
        server.run();
        run();
      } finally {
        server.stop();
      }
    } finally {
      DatabaseAccess.deleteTestTables();
    }
  }

  static void configureTestDatabase(HTTPServerConfig config) {
    // generate unique name for database, so there are no conflicts between tests run in parallel
    // may fix broken CircleCI build
    String dbName = "testDb_" + System.currentTimeMillis() + "_" +  Math.random();
    config.setDatabaseDriver("org.hsqldb.jdbcDriver");
    config.setDatabaseUrl("jdbc:hsqldb:mem:" + dbName);
    config.setDatabaseUsername("");
    config.setDatabasePassword("");
    config.setSecretTokenKey("myfoo");
    config.setCacheSize(100);
  }

  @NotNull
  protected HTTPServerConfig getHttpServerConfig() {
    HTTPServerConfig config = new HTTPServerConfig(HTTPTools.getDefaultPort());
    configureTestDatabase(config);
    return config;
  }

  protected void testSentence(Language lang, String input, String inputWithTypo, String name, String errorRuleId) throws Exception {
    assertRuleMatch(1, input, lang, errorRuleId, USERNAME1, API_KEY1, null);
    assertRuleMatch(1, input, lang, errorRuleId, USERNAME2, API_KEY2, null);
    assertRuleMatch(1, input, lang, errorRuleId, null, null, null);  // anonymous user
    assertThat(getWords(USERNAME1, API_KEY1, null).toString(), is("[]"));
    addWord(name, USERNAME1, API_KEY1, null);
    assertThat(getWords(USERNAME1, API_KEY1, null).toString(), is("[" + name + "]"));
    assertThat(getWords(USERNAME2, API_KEY2, null).toString(), is("[]"));
    assertRuleMatch(0, input, lang, errorRuleId, USERNAME1, API_KEY1, null);
    assertRuleMatch(1, input, lang, errorRuleId, USERNAME2, API_KEY2, null);  // cache must not mix up users
    assertRuleMatch(1, input, lang, errorRuleId, null, null, null);  // anonymous user
    assertRuleMatch(0, input, lang, errorRuleId, USERNAME1, API_KEY1, null);
    String json = assertRuleMatch(1, inputWithTypo, lang, errorRuleId, USERNAME1, API_KEY1, null);
    assertTrue("Missing suggestion '" + name + "': " + json, json.contains("\"" + name + "\"") || json.contains("\"" + name + ".\""));
    deleteWord(name, USERNAME1, API_KEY1, null);
    assertThat(getWords(USERNAME1, API_KEY1, null).toString(), is("[]"));
    assertRuleMatch(1, input, lang, errorRuleId, USERNAME1, API_KEY1, null);
    assertRuleMatch(1, input, lang, errorRuleId, USERNAME2, API_KEY2, null);
    assertRuleMatch(1, input, lang, errorRuleId, null, null, null);
  }

  protected void testUserDictGroups() throws IOException {
    // test groups
    List<String> group;

    group = getWords(USERNAME1, API_KEY1, asList("group1"));
    assertEquals("dicts start out empty", "[]", group.toString());

    addWord("word1", USERNAME1, API_KEY1, "group1");
    group = getWords(USERNAME1, API_KEY1, asList("group1"));
    assertEquals("adding a single word", "[word1]", group.toString());

    group = getWords(USERNAME2, API_KEY2, asList("group1"));
    assertEquals("dicts are protected / bound to one user", "[]", group.toString());

    addWord("word2", USERNAME2, API_KEY2, "group1");
    group  = getWords(USERNAME2, API_KEY2, asList("group1"));
    assertEquals("adding a single word with another user", "[word2]", group.toString());

    group = getWords(USERNAME1, API_KEY1, asList("group1"));
    assertEquals("modifications do not affect other users", "[word1]", group.toString());

    addWord("word3", USERNAME1, API_KEY1, "group1");
    group = getWords(USERNAME1, API_KEY1, asList("group1"));
    assertEquals("adding a second word to a group", "[word1, word3]", group.toString());

    deleteWord("word1", USERNAME1, API_KEY1, "group1");
    group = getWords(USERNAME1, API_KEY1, asList("group1"));
    assertEquals("deleting a word from a group", "[word3]", group.toString());

    group = getWords(USERNAME2, API_KEY2, asList("group1"));
    assertEquals("modifications do not affect other users", "[word2]", group.toString());

    addWord("anotherword", USERNAME1, API_KEY1, "group2");
    group = getWords(USERNAME1, API_KEY1, asList("group2"));
    assertEquals("adding a word to a second group", "[anotherword]", group.toString());

    group = getWords(USERNAME1, API_KEY1, asList("group1"));
    assertEquals("modifications do not affect other groups", "[word3]", group.toString());

    group = getWords(USERNAME1, API_KEY1, asList("group1", "group2"));
    assertEquals("group combination works", "[word3, anotherword]", group.toString());

    group = getWords(USERNAME2, API_KEY2, asList("group1", "group2"));
    assertEquals("group combination keeps groups protected", "[word2]", group.toString());

    // test /check
    String mistake1 = "lkwqurjo";
    String mistake2 = "qjoeprixn";
    String mistake3 = "akldfjqp";
    Language lang = Languages.getLanguageForShortCode("en-US");

    assertRuleMatch(2, "This is a " + mistake1 + ", my dear " + mistake2 + ".", lang, "MORFOLOGIK_RULE_EN_US", USERNAME1, API_KEY1, null);

    addWord(mistake1, USERNAME1, API_KEY1, "dict1");
    assertRuleMatch(1, "This is a " + mistake1 + ", my dear " + mistake2 + ".", lang, "MORFOLOGIK_RULE_EN_US", USERNAME1, API_KEY1, asList("dict1"));

    addWord(mistake2, USERNAME1, API_KEY1, "dict2");
    assertRuleMatch(1, "This is a " + mistake1 + ", my dear " + mistake2 + ".", lang, "MORFOLOGIK_RULE_EN_US", USERNAME1, API_KEY1, asList("dict2"));
    assertRuleMatch(0, "This is a " + mistake1 + ", my dear " + mistake2 + ".", lang, "MORFOLOGIK_RULE_EN_US", USERNAME1, API_KEY1, asList("dict1", "dict2"));

    addWord(mistake3, USERNAME1, API_KEY1, "dict3");
    assertRuleMatch(1, "This is a " + mistake1 + ", my dear " + mistake2 + ".", lang, "MORFOLOGIK_RULE_EN_US", USERNAME1, API_KEY1, asList("dict1", "dict3"));
    assertRuleMatch(1, "This is a " + mistake1 + ", my dear " + mistake2 + ".", lang, "MORFOLOGIK_RULE_EN_US", USERNAME1, API_KEY1, asList("dict2", "dict3"));
    assertRuleMatch(2, "This is a " + mistake1 + ", my dear " + mistake2 + ".", lang, "MORFOLOGIK_RULE_EN_US", USERNAME1, API_KEY1, asList("dict3"));
  }

  protected String assertRuleMatch(int expectedTypoCount, String input, Language lang, String errorRuleId, String username, String apiKey, List<String> dicts) throws IOException {
    String json = check(lang, input, username, apiKey, dicts);
    int realTypoCount = StringUtils.countMatches(json, errorRuleId);
    //System.out.println(json);
    assertThat("Expected " + expectedTypoCount + " rule matches (id " + errorRuleId + ") for '" + input + "', got " +
               realTypoCount, realTypoCount, is(expectedTypoCount));
    return json;
  }
  protected String check(Language lang, String text, String username, String apiKey, List<String> dicts) throws IOException {
    return check(lang, text, username, apiKey, dicts, "");
  }

  protected String check(Language lang, String text, String username, String apiKey, List<String> dicts, String option) throws IOException {
    String urlOptions = "?language=" + lang.getShortCodeWithCountryAndVariant();
    urlOptions += "&text=" + URLEncoder.encode(text, "UTF-8");
    if (username != null && apiKey != null) {
      urlOptions += "&username=" + URLEncoder.encode(username, "UTF-8");
      urlOptions += "&apiKey=" + URLEncoder.encode(apiKey, "UTF-8");
    }
    if (dicts != null && dicts.size() != 0) {
      urlOptions += "&dicts=" + URLEncoder.encode(String.join(",", dicts));
    }
    urlOptions += option;
    URL url = new URL(apiEndpoint + "/v2/check" + urlOptions);
    return HTTPTools.checkAtUrl(url);
  }

  protected List<String> getWords(String username, String apiKey, List<String> dicts) throws IOException {
    String postData = "username=" + username + "&apiKey=" + apiKey + "&limit=10000000";
    if (dicts != null) {
      postData += "&dicts=" + String.join(",", dicts);
    }
    URL url = new URL(apiEndpoint + "/v2/words?" + postData);
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
  
  protected String addWord(String word, String username, String apiKey, String dict) throws IOException {
    URL url = new URL(apiEndpoint + "/v2/words/add");
    String postData = "word=" + word + "&username=" + username + "&apiKey=" + apiKey;
    if (dict != null) {
      postData += "&dict=" + dict;
    }
    return HTTPTools.checkAtUrlByPost(url, postData);
  }  
  
  protected void deleteWord(String word, String username, String apiKey, String dict) throws IOException {
    URL url = new URL(apiEndpoint + "/v2/words/delete");
    String postData = "word=" + word + "&username=" + username + "&apiKey=" + apiKey;
    if (dict != null) {
      postData += "&dict=" + dict;
    }
    HTTPTools.checkAtUrlByPost(url, postData);
  }
  
}
