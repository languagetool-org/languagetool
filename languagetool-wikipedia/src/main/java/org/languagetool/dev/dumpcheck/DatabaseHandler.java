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

import org.apache.commons.lang.StringUtils;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRule;
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

  private final PreparedStatement lookupSt;
  private final PreparedStatement insertSt;

  private int batchSize;
  private int batchCount=0;

  DatabaseHandler(File propertiesFile, int maxSentences, int maxErrors) {
    super(maxSentences, maxErrors);

    final String lookupSql = "SELECT id FROM corpus_match_hidden WHERE " +
            "language_code = ? AND sourceuri = ? AND ruleid = ? AND small_error_context = ?";
    final String insertSql = "INSERT INTO corpus_match " +
            "(version, language_code, ruleid, rule_category, rule_subid, rule_description, message, error_context, small_error_context, corpus_date, " +
            "check_date, sourceuri, source_type, is_visible) "+
            "VALUES (0, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";

    final Properties dbProperties = new Properties();
    try (FileInputStream inStream = new FileInputStream(propertiesFile)) {
      dbProperties.load(inStream);
      final String dbUrl = getProperty(dbProperties, "dbUrl");
      final String dbUser = getProperty(dbProperties, "dbUser");
      final String dbPassword = getProperty(dbProperties, "dbPassword");
      try {
        batchSize = Integer.decode(dbProperties.getProperty("batchSize", "1"));
       }
      catch(NumberFormatException e){
        batchSize = 1;
      }
      conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
      lookupSt = conn.prepareStatement(lookupSql);
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
    final String value = prop.getProperty(key);
    if (value == null) {
      throw new RuntimeException("Required key '" + key + "' not found in properties");
    }
    return value;
  }

  @Override
  protected void handleResult(Sentence sentence, List<RuleMatch> ruleMatches, Language language) {
    try {
      final java.sql.Date nowDate = new java.sql.Date(new Date().getTime());
      for (RuleMatch match : ruleMatches) {
        final String smallContext = smallContextTools.getContext(match.getFromPos(), match.getToPos(), sentence.getText());
        if (ruleIsMarkedHidden(language, sentence.getUrl(), match, smallContext, lookupSt)) {
          System.out.println("Skipping match " + match.getRule().getId() + " for " + sentence.getTitle() + " as it is hidden");
          continue;
        }
        
        insertSt.setString(1, language.getShortName());
        final Rule rule = match.getRule();
        insertSt.setString(2, rule.getId());
        insertSt.setString(3, rule.getCategory().getName());
        if (rule instanceof PatternRule) {
          final PatternRule patternRule = (PatternRule) rule;
          insertSt.setString(4, patternRule.getSubId());
        } else {
          insertSt.setNull(4, Types.VARCHAR);
        }
        insertSt.setString(5, rule.getDescription());
        insertSt.setString(6, StringUtils.abbreviate(match.getMessage(), 255));

        final String context = contextTools.getContext(match.getFromPos(), match.getToPos(), sentence.getText());
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
        if (++batchCount>=batchSize){
            insertSt.executeBatch();
            batchCount=0;
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

  // Whether a match has been marked as 'false alarm' or 'already fixed' by a user - in that
  // case, we don't want to re-insert it into the list of matches.
  private boolean ruleIsMarkedHidden(Language language, String url, RuleMatch match, String smallContext, PreparedStatement lookupSt) throws SQLException {
      boolean ret=false;
    // TODO: should we consider the subid?
    lookupSt.setString(1, language.getShortName());
    lookupSt.setString(2, url);
    lookupSt.setString(3, match.getRule().getId());
    lookupSt.setString(4, smallContext);

    try (ResultSet resultSet = lookupSt.executeQuery()) {
      try {
        if (resultSet.isBeforeFirst()) {
          ret=true;
        }
      } catch (SQLFeatureNotSupportedException e){
        ret=resultSet.next();
      }
    }

    return ret;
  }

  @Override
  public void close() throws Exception {
    if (insertSt != null) {
        if (batchCount>0) {
            insertSt.executeBatch();
        }
        insertSt.close();
    }
    if (lookupSt != null) {
        lookupSt.close();
    }
    if (conn != null) {
      conn.close();
    }
  }

}
