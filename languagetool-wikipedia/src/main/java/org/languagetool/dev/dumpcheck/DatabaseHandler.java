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
package org.languagetool.dev.dumpcheck;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.tools.ContextTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Store rule matches to a database.
 * @since 2.4
 */
class DatabaseHandler extends ResultHandler {

  private static final int MAX_CONTEXT_LENGTH = 500;
  private static final int SMALL_CONTEXT_LENGTH = 40;  // do not modify - it would break lookup of errors marked as 'false alarm'

  private final Connection conn;
  private final ContextTools contextTools;
  private final ContextTools smallContextTools;

  private final PreparedStatement insertSt;
  private final int batchSize;
  
  private int batchCount = 0;

  DatabaseHandler(File propertiesFile, int maxSentences, int maxErrors) {
    super(maxSentences, maxErrors);

    String insertSql = "INSERT INTO corpus_match " +
            "(version, language_code, ruleid, rule_category, rule_subid, rule_description, message, error_context, small_error_context, corpus_date, " +
            "check_date, sourceuri, source_type, is_visible) "+
            "VALUES (0, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";

    Properties dbProperties = new Properties();
    try (FileInputStream inStream = new FileInputStream(propertiesFile)) {
      dbProperties.load(inStream);
      String dbUrl = getProperty(dbProperties, "dbUrl");
      String dbUser = getProperty(dbProperties, "dbUser");
      String dbPassword = getProperty(dbProperties, "dbPassword");
      batchSize = Integer.decode(dbProperties.getProperty("batchSize", "1"));
      conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
      insertSt = conn.prepareStatement(insertSql);
    } catch (SQLException | IOException e) {
      throw new RuntimeException(e);
    }
    contextTools = new ContextTools();
    contextTools.setContextSize(MAX_CONTEXT_LENGTH);
    contextTools.setErrorMarkerStart(MARKER_START);
    contextTools.setErrorMarkerEnd(MARKER_END);
    contextTools.setEscapeHtml(false);
    smallContextTools = new ContextTools();
    smallContextTools.setContextSize(SMALL_CONTEXT_LENGTH);
    smallContextTools.setErrorMarkerStart(MARKER_START);
    smallContextTools.setErrorMarkerEnd(MARKER_END);
    smallContextTools.setEscapeHtml(false);
  }

  private String getProperty(Properties prop, String key) {
    String value = prop.getProperty(key);
    if (value == null) {
      throw new RuntimeException("Required key '" + key + "' not found in properties");
    }
    return value;
  }

  @Override
  protected void handleResult(Sentence sentence, List<RuleMatch> ruleMatches, Language language) {
    try {
      java.sql.Date nowDate = new java.sql.Date(new Date().getTime());
      for (RuleMatch match : ruleMatches) {
        String smallContext = smallContextTools.getContext(match.getFromPos(), match.getToPos(), sentence.getText());
        insertSt.setString(1, language.getShortCode());
        Rule rule = match.getRule();
        insertSt.setString(2, rule.getId());
        insertSt.setString(3, rule.getCategory().getName());
        if (rule instanceof AbstractPatternRule) {
          AbstractPatternRule patternRule = (AbstractPatternRule) rule;
          insertSt.setString(4, patternRule.getSubId());
        } else {
          insertSt.setNull(4, Types.VARCHAR);
        }
        insertSt.setString(5, rule.getDescription());
        insertSt.setString(6, StringUtils.abbreviate(match.getMessage(), 255));

        String context = contextTools.getContext(match.getFromPos(), match.getToPos(), sentence.getText());
        if (context.length() > MAX_CONTEXT_LENGTH) {
          // let's skip these strange cases, as shortening the text might leave us behind with invalid markup etc
          continue;
        }
        insertSt.setString(7, context);
        insertSt.setString(8, StringUtils.abbreviate(smallContext, 255));
        
        insertSt.setDate(9, nowDate);  // should actually be the dump's date, but isn't really used anyway...
        insertSt.setDate(10, nowDate);
        insertSt.setString(11, sentence.getUrl());
        insertSt.setString(12, sentence.getSource());
        insertSt.addBatch();
        if (++batchCount >= batchSize){
          executeBatch();
          batchCount = 0;
        }

        checkMaxErrors(++errorCount);
        if (errorCount % 100 == 0) {
          System.out.println("Storing error #" + errorCount + " for text:");
          System.out.println("  " + sentence.getText());
        }
      }
      checkMaxSentences(++sentenceCount);
    } catch (DocumentLimitReachedException | ErrorLimitReachedException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Error storing matches for '" + sentence.getTitle() + "'", e);
    }
  }

  private void executeBatch() throws SQLException {
    boolean autoCommit = conn.getAutoCommit();
    conn.setAutoCommit(false);
    try {
      insertSt.executeBatch();
      if (autoCommit) {
        conn.commit();
      }
    } finally {
      conn.setAutoCommit(autoCommit);
    }
  }

  @Override
  public void close() throws Exception {
    if (insertSt != null) {
      if (batchCount > 0) {
        executeBatch();
      }
      insertSt.close();
    }
    if (conn != null) {
      conn.close();
    }
  }

}
