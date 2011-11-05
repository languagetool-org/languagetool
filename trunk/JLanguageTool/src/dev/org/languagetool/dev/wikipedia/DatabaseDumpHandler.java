/*
 * Created on 04.04.2010
 */
package de.danielnaber.languagetool.dev.wikipedia;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.Tools;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * Writes result of LanguageTool check to database. Used for community.languagetool.org.
 *  
 * @author Daniel Naber
 */
class DatabaseDumpHandler extends BaseWikipediaDumpHandler {

    private final Connection conn;

    DatabaseDumpHandler(JLanguageTool lt, int maxArticles, Date dumpDate, String langCode,
            File propertiesFile, Language lang) throws IOException {
    super(lt, maxArticles, dumpDate, langCode, lang);
    final Properties dbProperties = new Properties();
    final FileInputStream inStream = new FileInputStream(propertiesFile);
    try {
        dbProperties.load(inStream);
        final String dbDriver = getProperty(dbProperties, "dbDriver");
        final String dbUrl = getProperty(dbProperties, "dbUrl");
        final String dbUser = getProperty(dbProperties, "dbUser");
        final String dbPassword = getProperty(dbProperties, "dbPassword");
        Class.forName(dbDriver);
        conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      } finally {
        inStream.close();
      }
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
        throw new RuntimeException("required key '" +key+ "' not found in properties");
      }
      return value;
    }

    @Override
    protected void handleResult(String title, List<RuleMatch> ruleMatches,
            String text, Language language) throws SQLException {
      final String sql = "INSERT INTO corpus_match " +
              "(version, language_code, ruleid, message, error_context, corpus_date, " +
              "check_date, sourceuri, is_visible) "+
              "VALUES (0, ?, ?, ?, ?, ?, ?, ?, 1)";
      final PreparedStatement prepSt = conn.prepareStatement(sql);
      try {
        for (RuleMatch match : ruleMatches) {
          prepSt.setString(1, language.getShortName());
          prepSt.setString(2, match.getRule().getId());
          prepSt.setString(3, match.getMessage());
          prepSt.setString(4, Tools.getContext(match.getFromPos(),
                match.getToPos(), text, CONTEXT_SIZE, MARKER_START, MARKER_END));
          prepSt.setDate(5, new java.sql.Date(dumpDate.getTime()));
          prepSt.setDate(6, new java.sql.Date(new Date().getTime()));
          prepSt.setString(7, URL_PREFIX.replaceAll(LANG_MARKER, langCode) + title);
          prepSt.executeUpdate();
        }
      } finally {
        prepSt.close();
      }
    }

}
