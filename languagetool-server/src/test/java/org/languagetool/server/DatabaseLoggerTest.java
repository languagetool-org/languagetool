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

import org.apache.ibatis.jdbc.SQL;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.ResultSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@Ignore("Requires local MySQL/MariaDB")
public class DatabaseLoggerTest {

  @Test
  public void testHTTPServer() throws Exception {
    HTTPServerConfig config = new HTTPServerConfig(HTTPTools.getDefaultPort());
    config.setDatabaseDriver("org.mariadb.jdbc.Driver");
    config.setDatabaseUrl("jdbc:mysql://localhost:3306/lt_test");
    config.setDatabaseUsername("lt");
    config.setDatabasePassword("languagetool");
    DatabaseAccess.init(config);
    DatabaseAccess db = DatabaseAccess.getInstance();
    DatabaseLogger logger = DatabaseLogger.getInstance();
    try {
      logger.createTestTables(true);
      DatabaseAccess.createAndFillTestTables(true);

      HTTPServer server = new HTTPServer(config);
      Language en = Languages.getLanguageForShortCode("en-US");
      try {
        server.run();
        check(en, "This is a test.", UserDictTest.USERNAME1, UserDictTest.API_KEY1, null, null);
        check(en, "This is a test.", null, null, null, "agent1");
        check(en, "This is a test.", UserDictTest.USERNAME2, UserDictTest.API_KEY2, null, "agent2");
        check(en, "This is an mistak.", null, null, "123456789", null);
        check(en, "This is a mistak.", null, null,  "123456789", null);
        check(en, "This is a mistake.", null, null, "123456789", null);
        check(en, "This is not a mistake.", null, null, "987654321", null);
        SQL checkLogQuery = new SQL(){{
          SELECT("id", "user_id", "text_session_id", "client", "server");
          FROM("check_log");
          ORDER_BY("id");
        }};
        Long serverId = db.getOrCreateServerId();
        Long agent1 = db.getOrCreateClientId("agent1");
        Long agent2 = db.getOrCreateClientId("agent2");
        Long user1 = db.getUserId(UserDictTest.USERNAME1, UserDictTest.API_KEY1);
        Long user2 = db.getUserId(UserDictTest.USERNAME2, UserDictTest.API_KEY2);
        Thread.sleep(DatabaseLogger.SQL_BATCH_WAITING_TIME);
        try (ResultSet results = DatabaseAccess.executeStatement(checkLogQuery)) {
          results.next();
          assertEquals(results.getLong(1), 1);
          assertEquals(Long.valueOf(results.getLong(2)), user1);
          assertNull(results.getObject(3));
          assertNull(results.getObject(4));
          assertEquals(Long.valueOf(results.getLong(5)), serverId);

          results.next();
          assertEquals(results.getLong(1), 2);
          assertNull(results.getObject(2));
          assertNull(results.getObject(3));
          assertEquals(Long.valueOf(results.getLong(4)), agent1);
          assertEquals(Long.valueOf(results.getLong(5)), serverId);

          results.next();
          assertEquals(results.getLong(1), 3);
          assertEquals(Long.valueOf(results.getLong(2)), user2);
          assertNull(results.getObject(3));
          assertEquals(Long.valueOf(results.getLong(4)), agent2);
          assertEquals(Long.valueOf(results.getLong(5)), serverId);

          results.next();
          assertEquals(results.getLong(1), 4);
          assertNull(results.getObject(2));
          assertEquals(results.getInt(3), 123456789);
          assertNull(results.getObject(4));
          assertEquals(Long.valueOf(results.getLong(5)), serverId);

          results.next();
          assertEquals(results.getLong(1), 5);
          assertNull(results.getObject(2));
          assertEquals(results.getInt(3), 123456789);
          assertNull(results.getObject(4));
          assertEquals(Long.valueOf(results.getLong(5)), serverId);

          results.next();
          assertEquals(results.getLong(1), 6);
          assertNull(results.getObject(2));
          assertEquals(results.getInt(3), 123456789);
          assertNull(results.getObject(4));
          assertEquals(Long.valueOf(results.getLong(5)), serverId);

          results.next();
          assertEquals(results.getLong(1), 7);
          assertNull(results.getObject(2));
          assertEquals(results.getInt(3), 987654321);
          assertNull(results.getObject(4));
          assertEquals(Long.valueOf(results.getLong(5)), serverId);
        }

        SQL ruleMatchQuery1 = new SQL(){{
          SELECT("match_id", "check_id", "rule_id", "match_count");
          FROM("rule_matches");
          WHERE("check_id in (1, 2, 3)");
        }};
        try (ResultSet results = DatabaseAccess.executeStatement(ruleMatchQuery1)) {
          assertFalse(results.next());
        }

        SQL ruleMatchQuery2 = new SQL(){{
          SELECT("r.match_id", "r.check_id", "r.rule_id", "r.match_count");
          FROM("rule_matches r");
          INNER_JOIN("check_log c on c.id = r.check_id");
          WHERE("c.text_session_id = 123456789");
          ORDER_BY("r.match_id");
        }};
        try (ResultSet results = DatabaseAccess.executeStatement(ruleMatchQuery2)) {
          results.next();
          assertEquals(results.getLong(1), 1);
          assertEquals(results.getLong(2), 4);
          assertEquals(results.getString(3), "EN_A_VS_AN");
          assertEquals(results.getInt(4), 1);

          results.next();
          assertEquals(results.getLong(1), 2);
          assertEquals(results.getLong(2), 4);
          assertEquals(results.getString(3), "MORFOLOGIK_RULE_EN_US");
          assertEquals(results.getInt(4), 1);

          results.next();
          assertEquals(results.getLong(1), 3);
          assertEquals(results.getLong(2), 5);
          assertEquals(results.getString(3), "MORFOLOGIK_RULE_EN_US");
          assertEquals(results.getInt(4), 1);
        }

        int check_count;
        // test committing after batch size is reached
        SQL checkCount = new SQL(){{
          SELECT("COUNT(*)");
          FROM("check_log");
        }};
        try (ResultSet results = DatabaseAccess.executeStatement(checkCount)) {
          results.next();
          check_count = results.getInt(1);
        }
        for (int i = 0; i < DatabaseLogger.SQL_BATCH_SIZE; i++) {
          check(en, String.format("This is the check with batch number %d.", i), null, null, null, null);
        }
        int check_count2;
        try (ResultSet results = DatabaseAccess.executeStatement(checkCount)) {
          results.next();
          check_count2 = results.getInt(1);
          assertThat(check_count2, is(check_count + DatabaseLogger.SQL_BATCH_SIZE));
        }
        // test committing after wait time elapsed
        check(en, "This is a check I am waiting for.", null, null, null, null);
        int check_count3;
        try (ResultSet results = DatabaseAccess.executeStatement(checkCount)) {
          results.next();
          check_count3 = results.getInt(1);
          assertThat(check_count2, is(check_count3));
        }
        Thread.sleep(DatabaseLogger.SQL_BATCH_WAITING_TIME);
        int check_count4;
        try (ResultSet results = DatabaseAccess.executeStatement(checkCount)) {
          results.next();
          check_count4 = results.getInt(1);
          assertThat(check_count4, is(check_count3 + 1));
        }
      } finally {
        server.stop();
      }
    } finally {
      logger.dropTestTables();
      DatabaseAccess.deleteTestTables();
    }
  }

  private String check(Language lang, String text, String username, String apiKey, String textSessionId, String agent) throws IOException {
    String urlOptions = "?language=" + lang.getShortCodeWithCountryAndVariant();
    urlOptions += "&text=" + URLEncoder.encode(text, "UTF-8");
    if (username != null && apiKey != null) {
      urlOptions += "&username=" + URLEncoder.encode(username, "UTF-8");
      urlOptions += "&apiKey=" + URLEncoder.encode(apiKey, "UTF-8");
    }
    if (textSessionId != null) {
      urlOptions += "&textSessionId=" + URLEncoder.encode(textSessionId, "UTF-8");
    }
    if (agent != null) {
      urlOptions += "&useragent=" + URLEncoder.encode(agent, "UTF-8");
    }
    URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + "/v2/check" + urlOptions);
    return HTTPTools.checkAtUrl(url);
  }

}
