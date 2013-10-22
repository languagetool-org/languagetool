/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wikipedia;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.dev.dumpcheck.ErrorLimitReachedException;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.tools.ContextTools;

/**
 * Writes result of LanguageTool check to database. Used for community.languagetool.org.
 *  
 * @author Daniel Naber
 * @deprecated use {@link org.languagetool.dev.dumpcheck.DatabaseHandler} instead (deprecated since 2.4)
 */
@Deprecated
class DatabaseDumpHandler extends BaseWikipediaDumpHandler {

    private final Connection conn;
    private final ContextTools contextTools;

    DatabaseDumpHandler(JLanguageTool lt, Date dumpDate, String langCode,
            File propertiesFile, Language lang) throws IOException {
    super(lt, dumpDate, langCode, lang);
    final Properties dbProperties = new Properties();
      try (FileInputStream inStream = new FileInputStream(propertiesFile)) {
        dbProperties.load(inStream);
        final String dbDriver = getProperty(dbProperties, "dbDriver");
        final String dbUrl = getProperty(dbProperties, "dbUrl");
        final String dbUser = getProperty(dbProperties, "dbUser");
        final String dbPassword = getProperty(dbProperties, "dbPassword");
        Class.forName(dbDriver);
        conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
      } catch (ClassNotFoundException | SQLException e) {
        throw new RuntimeException(e);
      }
      contextTools = new ContextTools();
      contextTools.setContextSize(CONTEXT_SIZE);
      contextTools.setErrorMarkerStart(MARKER_START);
      contextTools.setErrorMarkerEnd(MARKER_END);
      contextTools.setEscapeHtml(false);
    }
    
    @Override
    protected void close() {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
    }

    private String getProperty(Properties prop, String key) {
      final String value = prop.getProperty(key);
      if (value == null) {
        throw new RuntimeException("required key '" + key + "' not found in properties");
      }
      return value;
    }

    @Override
    protected void handleResult(String title, List<RuleMatch> ruleMatches,
            String text, Language language) throws SQLException {
      final String sql = "INSERT INTO corpus_match " +
              "(version, language_code, ruleid, rule_subid, rule_description, message, error_context, corpus_date, " +
              "check_date, sourceuri, is_visible) "+
              "VALUES (0, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";
      try (PreparedStatement prepSt = conn.prepareStatement(sql)) {
        final java.sql.Date dumpSqlDate = new java.sql.Date(dumpDate.getTime());
        final java.sql.Date nowDate = new java.sql.Date(new Date().getTime());
        for (RuleMatch match : ruleMatches) {
          prepSt.setString(1, language.getShortName());
          final Rule rule = match.getRule();
          prepSt.setString(2, rule.getId());
          if (rule instanceof PatternRule) {
            final PatternRule patternRule = (PatternRule) rule;
            prepSt.setString(3, patternRule.getSubId());
          } else {
            prepSt.setNull(3, Types.VARCHAR);
          }
          prepSt.setString(4, rule.getDescription());
          prepSt.setString(5, StringUtils.abbreviate(match.getMessage(), 255));
          final String context = contextTools.getContext(match.getFromPos(), match.getToPos(), text);
          if (context.length() > 255) {
            // let's skip these strange cases, as shortening the text might leave us behind with invalid markup etc
            continue;
          }
          prepSt.setString(6, context);
          prepSt.setDate(7, dumpSqlDate);
          prepSt.setDate(8, nowDate);
          prepSt.setString(9, URL_PREFIX.replaceAll(LANG_MARKER, langCode) + title);
          prepSt.executeUpdate();
          errorCount++;
          if (maxErrors > 0 && errorCount >= maxErrors) {
            throw new ErrorLimitReachedException(maxErrors);
          }
        }
      }
    }

}
