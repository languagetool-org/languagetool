/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.spelling.suggestions.XGBoostSuggestionsOrderer;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@Ignore("Requires ngram data to run")
public class SuggestionsRankerTest {

  private final Language german = Languages.getLanguageForShortCode("de-DE");
  private final Language english = Languages.getLanguageForShortCode("en-US");
  private final String agent = "ltorg";
  private HTTPServer server;

  @Before
  public void setUp() throws Exception {
    HTTPServerConfig config = new HTTPServerConfig();
    config.setAbTest("SuggestionsRanker");
    config.setLanguageModelDirectory("data/ngrams/");
    config.setDatabaseDriver("org.hsqldb.jdbcDriver");
    config.setDatabaseUrl("jdbc:hsqldb:mem:testdb");
    config.setDatabaseUsername("");
    config.setDatabasePassword("");
    config.setSecretTokenKey("myfoo");
    config.setCacheSize(100);
    DatabaseAccess.init(config);
    DatabaseLogger.getInstance().disableLogging();
    server = new HTTPServer(config, false);
    server.run();
  }

  private String getReplacements(String response) {
    int replacementsIndex = response.indexOf("replacements");
    if (replacementsIndex == -1) {
      return "";
    }
    int startList = response.indexOf('[', replacementsIndex);
    if (startList == -1) {
      return "";
    }
    int endList = response.indexOf(']', startList);
    if (endList == -1) {
      return "";
    }
    return response.substring(startList, endList + 1);
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }

  @Test
  public void testRankingWithUserDict() throws IOException {
    // no need to also create test tables for logging
    try {
      DatabaseAccess.createAndFillTestTables();
      XGBoostSuggestionsOrderer.setAutoCorrectThresholdForLanguage(english, 0.5f);

      String autoCorrectReplacements = getReplacements(check(english, "This is a mistak.", agent, 3L,
        UserDictTest.USERNAME1, UserDictTest.API_KEY1));
      System.out.println(autoCorrectReplacements);
      assertThat(autoCorrectReplacements, allOf(containsString("confidence"),
        containsString("autoCorrect")));
      addWord("mistaki", UserDictTest.USERNAME1, UserDictTest.API_KEY1);

      String replacementsData = getReplacements(check(english, "This is a mistak.", agent, 3L,
        UserDictTest.USERNAME1, UserDictTest.API_KEY1));
      System.out.println(replacementsData);
      ObjectMapper mapper = new ObjectMapper();
      List<Map<String, Object>> replacements = mapper.readValue(replacementsData, List.class);
      assertThat(replacements.get(0).get("value"), is("mistaki"));
      assertThat(replacements.get(0).get("confidence"), is(nullValue()));
      assertThat(replacements.get(0).containsKey("autoCorrect"), is(false));
      assertThat(replacements.get(1).get("value"), is("mistake"));
      assertThat(replacements.get(1).get("confidence"), is(notNullValue()));
      assertThat(replacements.get(1).get("confidence"), not(0.0));
    } finally {
      DatabaseAccess.deleteTestTables();
    }
  }

  @Test
  public void testRanking() throws IOException {
    assertThat(getReplacements(check(english, "This is a mistak.", agent, 3L)),
      containsString("confidence"));
  }


  @Test
  public void testNotRanking() throws IOException {
    // not ranking when:
    // model for language disabled
    assertThat(getReplacements(check(german, "Das ist ein Fehlar.", agent, 2L)),
      not(containsString("confidence")));

    assertThat(getReplacements(check(german, "Das ist ein Fehlar.", agent, 3L)),
      not(containsString("confidence")));

    // in group A of A/B test
    assertThat(getReplacements(check(english, "This is a mistak.", agent, 2L)),
      not(containsString("confidence")));

    // no A/B test for these clients
    assertThat(getReplacements(check(english, "This is a mistak.", null, 3L)),
      not(containsString("confidence")));
    assertThat(getReplacements(check(english, "This is a mistak.", "webextension-chrome-ng", 3L)),
      not(containsString("confidence")));
  }

  private String check(Language lang, String text, String userAgent, Long textSessionId) throws IOException {
    return check(lang, text, userAgent, textSessionId, null, null);
  }

  private String check(Language lang, String text, String userAgent, Long textSessionId, String username, String apiKey)
    throws IOException {
    String urlOptions = "?language=" + lang.getShortCodeWithCountryAndVariant();
    urlOptions += "&text=" + URLEncoder.encode(text, "UTF-8");
    if (userAgent != null) {
      urlOptions += "&useragent=" + URLEncoder.encode(userAgent, "UTF-8");
    }
    if (textSessionId != null) {
      urlOptions += "&textSessionId=" + URLEncoder.encode(textSessionId.toString(), "UTF-8");
    }
    if (username != null && apiKey != null) {
      urlOptions += "&username=" + URLEncoder.encode(username, "UTF-8");
      urlOptions += "&apiKey=" + URLEncoder.encode(apiKey, "UTF-8");
    }
    URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + "/v2/check" + urlOptions);
    return HTTPTools.checkAtUrl(url);
  }

  private String addWord(String word, String user, String key) throws IOException {
    URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + "/v2/words/add");
    return HTTPTools.checkAtUrlByPost(url, "word=" + word + "&username=" + user + "&apiKey=" + key);
  }
}
