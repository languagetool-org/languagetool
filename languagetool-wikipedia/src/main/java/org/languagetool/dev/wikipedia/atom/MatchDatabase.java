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

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

  void add(WikipediaRuleMatch ruleMatch) {
    String sql = "INSERT INTO feed_matches (title, language_code, rule_id, error_context, edit_date, diff_id) VALUES (?, ?, ?, ?, ?, ?)";
    try (PreparedStatement prepSt = conn.prepareStatement(sql)) {
      prepSt.setString(1, ruleMatch.getTitle());
      prepSt.setString(2, ruleMatch.getLanguage().getShortName());
      prepSt.setString(3, ruleMatch.getRule().getId());
      prepSt.setString(4, ruleMatch.getErrorContext());
      prepSt.setTimestamp(5, new Timestamp(ruleMatch.getEditDate().getTime()));
      prepSt.setLong(6, ruleMatch.getDiffId());
      prepSt.execute();
    } catch (SQLException e) {
      throw new RuntimeException("Could not add rule match " + ruleMatch + " to database", e);
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
      prepSt.setString(3, ruleMatch.getLanguage().getShortName());
      prepSt.setString(4, ruleMatch.getTitle());
      prepSt.setString(5, ruleMatch.getRule().getId());
      prepSt.setString(6, ruleMatch.getErrorContext());
      return prepSt.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Could not make rule match " + ruleMatch + " as fixed in database", e);
    }
  }

  /**
   * Use this only for test cases - it's Derby-specific.
   */
  void createTable() throws SQLException {
    try (PreparedStatement prepSt = conn.prepareStatement("CREATE TABLE feed_matches (" +
            "  id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
            "  language_code VARCHAR(5) NOT NULL," +
            "  title VARCHAR(255) NOT NULL," +
            "  rule_id VARCHAR(255) NOT NULL," +
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
   * Drop database - use this only for test cases.
   */
  void drop() throws SQLException {
    try (PreparedStatement prepSt = conn.prepareStatement("DROP TABLE feed_matches")) {
      prepSt.execute();
    } catch (SQLException e){
      System.err.println("Note: could not drop table 'feed_matches' - this is okay on the first run: " + e.toString());
    }
  }

  List<StoredWikipediaRuleMatch> list() throws SQLException {
    try (PreparedStatement prepSt = conn.prepareStatement("SELECT * FROM feed_matches")) {
      ResultSet resultSet = prepSt.executeQuery();
      List<StoredWikipediaRuleMatch> result = new ArrayList<>();
      while (resultSet.next()) {
        String ruleId = resultSet.getString("rule_id");
        String errorContext = resultSet.getString("error_context");
        String title = resultSet.getString("title");
        Date editDate = new Date(resultSet.getTimestamp("edit_date").getTime());
        Timestamp fixTimeStamp = resultSet.getTimestamp("fix_date");
        Date fixDate = fixTimeStamp != null ? new Date(resultSet.getTimestamp("fix_date").getTime()) : null;
        long diffId = resultSet.getLong("diff_id");
        long fixDiffId = resultSet.getLong("fix_diff_id");
        result.add(new StoredWikipediaRuleMatch(ruleId, errorContext, title, editDate, fixDate, diffId, fixDiffId));
      }
      return result;
    }
  }
}
