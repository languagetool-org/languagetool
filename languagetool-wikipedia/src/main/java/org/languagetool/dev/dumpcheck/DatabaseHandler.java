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

  private final Connection conn;
  private final ContextTools contextTools;

  DatabaseHandler(File propertiesFile, int maxSentences, int maxErrors) {
    super(maxSentences, maxErrors);
    final Properties dbProperties = new Properties();
    try (FileInputStream inStream = new FileInputStream(propertiesFile)) {
      dbProperties.load(inStream);
      final String dbUrl = getProperty(dbProperties, "dbUrl");
      final String dbUser = getProperty(dbProperties, "dbUser");
      final String dbPassword = getProperty(dbProperties, "dbPassword");
      conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    } catch (SQLException | IOException e) {
      throw new RuntimeException(e);
    }
    contextTools = new ContextTools();
    contextTools.setContextSize(CONTEXT_SIZE);
    contextTools.setErrorMarkerStart(MARKER_START);
    contextTools.setErrorMarkerEnd(MARKER_END);
    contextTools.setEscapeHtml(false);
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
    final String sql = "INSERT INTO corpus_match " +
            "(version, language_code, ruleid, rule_category, rule_subid, rule_description, message, error_context, corpus_date, " +
            "check_date, sourceuri, source_type, is_visible) "+
            "VALUES (0, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";
    try (PreparedStatement prepSt = conn.prepareStatement(sql)) {
      final java.sql.Date nowDate = new java.sql.Date(new Date().getTime());
      for (RuleMatch match : ruleMatches) {
        prepSt.setString(1, language.getShortName());
        final Rule rule = match.getRule();
        prepSt.setString(2, rule.getId());
        prepSt.setString(3, rule.getCategory().getName());
        if (rule instanceof PatternRule) {
          final PatternRule patternRule = (PatternRule) rule;
          prepSt.setString(4, patternRule.getSubId());
        } else {
          prepSt.setNull(4, Types.VARCHAR);
        }
        prepSt.setString(5, rule.getDescription());
        prepSt.setString(6, StringUtils.abbreviate(match.getMessage(), 255));
        final String context = contextTools.getContext(match.getFromPos(), match.getToPos(), sentence.getText());
        if (context.length() > 255) {
          // let's skip these strange cases, as shortening the text might leave us behind with invalid markup etc
          continue;
        }
        prepSt.setString(7, context);
        prepSt.setDate(8, nowDate);  // should actually be the dump's date, but isn't really used anyway...
        prepSt.setDate(9, nowDate);
        prepSt.setString(10, sentence.getUrl());
        prepSt.setString(11, sentence.getSource());
        prepSt.executeUpdate();
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

  @Override
  public void close() throws Exception {
    if (conn != null) {
      conn.close();
    }
  }

}
