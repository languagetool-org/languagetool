/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wikipedia.atom;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.Language;
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Database that keeps track of matches.
 * @since 2.4
 */
class MatchDatabase {

  private final Connection conn;
  
  MatchDatabase(String dbUrl, String dbUser, String dbPassword) {
    try {
      conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    } catch (SQLException e) {
      throw new RuntimeException("Could not get database connection to " + dbUrl, e);
    }
  }

  void updateRuleMatchPingDate(Language language, Date date) {
    updateRuleMatchDate("pings", language, date);
  }

  void updateRuleMatchCheckDate(Language language, Date date) {
    updateRuleMatchDate("feed_checks", language, date);
  }

  private void updateRuleMatchDate(String tableName, Language language, Date date) {
    String updateSql = "UPDATE " + tableName + " SET check_date = ? WHERE language_code = ?";
    try (PreparedStatement updateSt = conn.prepareStatement(updateSql)) {
      updateSt.setTimestamp(1, new Timestamp(date.getTime()));
      updateSt.setString(2, language.getShortCode());
      int affected = updateSt.executeUpdate();
      if (affected == 0) {
        String insertSql = "INSERT INTO " + tableName + " (language_code, check_date) VALUES (?, ?)";
        try (PreparedStatement insertSt = conn.prepareStatement(insertSql)) {
          insertSt.setString(1, language.getShortCode());
          insertSt.setTimestamp(2, new Timestamp(date.getTime()));
          insertSt.execute();
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Could not store date for " + language + " to database, table " + tableName, e);
    }
  }

  void add(WikipediaRuleMatch ruleMatch) {
    String sql = "INSERT INTO feed_matches " +
            "(title, language_code, rule_id, rule_sub_id, rule_description, rule_message, rule_category, error_context, edit_date, diff_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement prepSt = conn.prepareStatement(sql)) {
      prepSt.setString(1, StringUtils.abbreviate(ruleMatch.getTitle(), 255));
      prepSt.setString(2, ruleMatch.getLanguage().getShortCode());
      prepSt.setString(3, ruleMatch.getRule().getId());
      if (ruleMatch.getRule() instanceof AbstractPatternRule) {
        prepSt.setString(4, ((AbstractPatternRule)ruleMatch.getRule()).getSubId());
      } else {
        prepSt.setString(4, null);
      }
      prepSt.setString(5, StringUtils.abbreviate(ruleMatch.getRule().getDescription(), 255));
      prepSt.setString(6, StringUtils.abbreviate(ruleMatch.getMessage(), 255));
      if (ruleMatch.getRule().getCategory() != null) {
        prepSt.setString(7, StringUtils.abbreviate(ruleMatch.getRule().getCategory().getName(), 255));
      } else {
        prepSt.setString(7, "<no category>");
      }
      prepSt.setString(8, StringUtils.abbreviate(ruleMatch.getErrorContext(), 500));
      prepSt.setTimestamp(9, new Timestamp(ruleMatch.getEditDate().getTime()));
      prepSt.setLong(10, ruleMatch.getDiffId());
      prepSt.execute();
    } catch (SQLException e) {
      if (e.toString().contains("Incorrect string value")) {
        // Let's accept this - i.e. not crash - for now:
        // See http://stackoverflow.com/questions/1168036/ and http://stackoverflow.com/questions/10957238/
        System.err.println("Could not add rule match " + ruleMatch + " to database - stacktrace follows:");
        e.printStackTrace();
      } else {
        throw new RuntimeException("Could not add rule match " + ruleMatch + " to database", e);
      }
    }
  }

  /**
   * @return the number of affected rows, thus {@code 0} means the error was not found in the database
   */
  int markedFixed(WikipediaRuleMatch ruleMatch) {
    String sql = "UPDATE feed_matches SET fix_date = ?, fix_diff_id = ? WHERE language_code = ? AND title = ? AND rule_id = ? AND error_context = ?";
    try (PreparedStatement prepSt = conn.prepareStatement(sql)) {
      prepSt.setTimestamp(1, new Timestamp(ruleMatch.getEditDate().getTime()));
      prepSt.setLong(2, ruleMatch.getDiffId());
      prepSt.setString(3, ruleMatch.getLanguage().getShortCode());
      prepSt.setString(4, ruleMatch.getTitle());
      prepSt.setString(5, ruleMatch.getRule().getId());  // I'm not sure whether we should also consider the sub id...
      prepSt.setString(6, ruleMatch.getErrorContext());
      return prepSt.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Could not mark rule match " + ruleMatch + " as fixed in database", e);
    }
  }

  /**
   * Use this only for test cases - it's Derby-specific.
   */
  void createTables() throws SQLException {
    try (PreparedStatement prepSt = conn.prepareStatement("CREATE TABLE pings (" +
            "  language_code VARCHAR(5) NOT NULL," +
            "  check_date TIMESTAMP NOT NULL" +
            ")")) {
      prepSt.executeUpdate();
    }
    try (PreparedStatement prepSt = conn.prepareStatement("CREATE TABLE feed_checks (" +
            "  language_code VARCHAR(5) NOT NULL," +
            "  check_date TIMESTAMP NOT NULL" +
            ")")) {
      prepSt.executeUpdate();
    }
    try (PreparedStatement prepSt = conn.prepareStatement("CREATE TABLE feed_matches (" +
            "  id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
            "  language_code VARCHAR(5) NOT NULL," +
            "  title VARCHAR(255) NOT NULL," +
            "  rule_id VARCHAR(255) NOT NULL," +
            "  rule_sub_id VARCHAR(255)," +
            "  rule_description VARCHAR(255) NOT NULL," +
            "  rule_message VARCHAR(255) NOT NULL," +
            "  rule_category VARCHAR(255) NOT NULL," +
            "  error_context VARCHAR(500) NOT NULL," +
            "  edit_date TIMESTAMP NOT NULL," +
            "  diff_id INT NOT NULL," +
            "  fix_date TIMESTAMP," +
            "  fix_diff_id INT" +
            ")")) {
      prepSt.executeUpdate();
    }
  }

  /**
   * @return the latest edit date, or a date as of {@code 1970-01-01} if no data is in the database
   */
  Date getLatestDate(Language language) {
    try {
      String sql = "SELECT check_date FROM feed_checks WHERE language_code = ?";
      try (PreparedStatement prepSt = conn.prepareStatement(sql)) {
        prepSt.setString(1, language.getShortCode());
        ResultSet resultSet = prepSt.executeQuery();
        if (resultSet.next() && resultSet.getTimestamp("check_date") != null) {
          return new Date(resultSet.getTimestamp("check_date").getTime());
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Could not get check_date from database for " + language, e);
    }
    return new Date(0);
  }
  
  /**
   * Drop database tables - use this only for test cases.
   */
  void dropTables() throws SQLException {
    dropTable("feed_matches");
    dropTable("feed_checks");
    dropTable("pings");
  }

  private void dropTable(String tableName) {
    try (PreparedStatement prepSt = conn.prepareStatement("DROP TABLE " + tableName)) {
      prepSt.execute();
    } catch (SQLException e){
      System.err.println("Note: could not drop table 'feed_matches' - this is okay on the first run: " + e);
    }
  }

  List<StoredWikipediaRuleMatch> list() throws SQLException {
    try (PreparedStatement prepSt = conn.prepareStatement("SELECT * FROM feed_matches");
         ResultSet resultSet = prepSt.executeQuery()) {
      List<StoredWikipediaRuleMatch> result = new ArrayList<>();
      while (resultSet.next()) {
        String ruleId = resultSet.getString("rule_id");
        String ruleSubId = resultSet.getString("rule_sub_id");
        String ruleDescription = resultSet.getString("rule_description");
        String ruleMessage = resultSet.getString("rule_message");
        String errorContext = resultSet.getString("error_context");
        String title = resultSet.getString("title");
        Date editDate = new Date(resultSet.getTimestamp("edit_date").getTime());
        Timestamp fixTimeStamp = resultSet.getTimestamp("fix_date");
        Date fixDate = fixTimeStamp != null ? new Date(resultSet.getTimestamp("fix_date").getTime()) : null;
        long diffId = resultSet.getLong("diff_id");
        long fixDiffId = resultSet.getLong("fix_diff_id");
        result.add(new StoredWikipediaRuleMatch(ruleId, ruleSubId, ruleDescription, ruleMessage, errorContext,
                title, editDate, fixDate, diffId, fixDiffId));
      }
      return result;
    }
  }

  Map<String,Date> getCheckDates() throws SQLException {
    Map<String,Date> result = new HashMap<>();
    try (PreparedStatement prepSt = conn.prepareStatement("SELECT * FROM feed_checks");
         ResultSet resultSet = prepSt.executeQuery()) {
      while (resultSet.next()) {
        String langCode = resultSet.getString("language_code");
        Date checkDate = new Date(resultSet.getTimestamp("check_date").getTime());
        result.put(langCode, checkDate);
      }
    }
    return result;
  }
}
