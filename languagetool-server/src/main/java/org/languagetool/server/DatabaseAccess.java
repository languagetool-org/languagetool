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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.SQL;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.languagetool.Language;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.languagetool.server.ServerTools.print;

/**
 * Encapsulate database access. Will do nothing if database access is not configured.
 * @since 4.2
 */
class DatabaseAccess {

  private static DatabaseAccess instance;
  private static SqlSessionFactory sqlSessionFactory;

  private final Cache<Long, List<UserDictEntry>> userDictCache = CacheBuilder.newBuilder()
          .maximumSize(1000)
          .expireAfterWrite(24, TimeUnit.HOURS)
          .build();

  private DatabaseAccess(HTTPServerConfig config) {
    if (config.getDatabaseDriver() != null) {
      try {
        print("Setting up database access, URL " + config.getDatabaseUrl() + ", driver: " + config.getDatabaseDriver() + ", user: " + config.getDatabaseUsername());
        InputStream inputStream = Resources.getResourceAsStream("org/languagetool/server/mybatis-config.xml");
        Properties properties = new Properties();
        properties.setProperty("driver", config.getDatabaseDriver());
        properties.setProperty("url", config.getDatabaseUrl());
        properties.setProperty("username", config.getDatabaseUsername());
        properties.setProperty("password", config.getDatabasePassword());
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, properties);
        DatabaseLogger.init(sqlSessionFactory);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      print("Not setting up database access, dbDriver is not configured");
    }
  }
  
  static synchronized void init(HTTPServerConfig config) {
    if (instance == null) {
      instance = new DatabaseAccess(config);
    }
  }

  static synchronized DatabaseAccess getInstance() {
    if (instance == null) {
      throw new IllegalStateException("DatabaseAccess.init() has not been called yet");
    }
    return instance;
  }

  List<String> getUserDictWords(Long userId) {
    List<String> dictEntries = new ArrayList<>();
    if (sqlSessionFactory == null) {
      return dictEntries;
    }
    try (SqlSession session = sqlSessionFactory.openSession()) {
      try {
        List<UserDictEntry> dict = session.selectList("org.languagetool.server.UserDictMapper.selectWordList", userId);
        for (UserDictEntry userDictEntry : dict) {
          dictEntries.add(userDictEntry.getWord());
        }
        if (dict.size() <= 1000) {  // make sure users with huge dict don't blow up the cache
          userDictCache.put(userId, dict);
        } else {
          print("WARN: Large dict size " + dict.size() + " for user " + userId + " - will not put user's dict in cache");
        }
      } catch (Exception e) {
        // try to be more robust when database is down, i.e. don't just crash but try to use cache:
        List<UserDictEntry> cachedDictOrNull = userDictCache.getIfPresent(userId);
        if (cachedDictOrNull != null) {
          print("ERROR: Could not get words from database for user " + userId + ": " + e.getMessage() + ", will use cached version (" + cachedDictOrNull.size() + " items). Full stack trace follows:", System.err);
          for (UserDictEntry userDictEntry : cachedDictOrNull) {
            dictEntries.add(userDictEntry.getWord());
          }
        } else {
          print("ERROR: Could not get words from database for user " + userId + ": " + e.getMessage() + " - also, could not use version from cache, user id not found in cache, will use empty dict. Full stack trace follows:", System.err);
        }
        e.printStackTrace();
      }
    }
    return dictEntries;
  }

  List<UserDictEntry> getWords(Long userId, int offset, int limit) {
    if (sqlSessionFactory == null) {
      return new ArrayList<>();
    }
    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      Map<Object, Object> map = new HashMap<>();
      map.put("userId", userId);
      return session.selectList("org.languagetool.server.UserDictMapper.selectWordList", map, new RowBounds(offset, limit));
    }
  }
  
  boolean addWord(String word, Long userId) {
    validateWord(word);
    if (sqlSessionFactory == null) {
      return false;
    }
    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      Map<Object, Object> map = new HashMap<>();
      map.put("word", word);
      map.put("userId", userId);
      List<UserDictEntry> existingWords = session.selectList("org.languagetool.server.UserDictMapper.selectWord", map);
      if (existingWords.size() >= 1) {
        print("Did not add '" + word + "' for user " + userId + " to list of ignored words, already exists");
        return false;
      } else {
        Date now = new Date();
        map.put("created_at", now);
        map.put("updated_at", now);
        int affectedRows = session.insert("org.languagetool.server.UserDictMapper.addWord", map);
        print("Added '" + word + "' for user " + userId + " to list of ignored words, affectedRows: " + affectedRows);
        return affectedRows == 1;
      }
    }
  }

  Long getUserId(String username, String apiKey) {
    if (username == null || username.trim().isEmpty()) {
      throw new IllegalArgumentException("username must be set");
    }
    if (apiKey == null || apiKey.trim().isEmpty()) {
      throw new IllegalArgumentException("apikey must be set");
    }
    if (sqlSessionFactory ==  null) {
      throw new IllegalStateException("sqlSessionFactory not initialized - has the database been configured?");
    }
    try (SqlSession session = sqlSessionFactory.openSession()) {
      Map<Object, Object> map = new HashMap<>();
      map.put("username", username);
      map.put("apiKey", apiKey);
      Long id = session.selectOne("org.languagetool.server.UserDictMapper.getUserIdByApiKey", map);
      if (id == null) {
        throw new IllegalArgumentException("No user found for given username '" + username + "' and given api key");
      }
      return id;
    }
  }

  boolean deleteWord(String word, Long userId) {
    if (sqlSessionFactory == null) {
      return false;
    }
    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      Map<Object, Object> map = new HashMap<>();
      map.put("word", word);
      map.put("userId", userId);
      int count = session.delete("org.languagetool.server.UserDictMapper.selectWord", map);
      if (count == 0) {
        print("Did not delete '" + word + "' for user " + userId + " from list of ignored words, does not exist");
        return false;
      } else {
        int affectedRows = session.delete("org.languagetool.server.UserDictMapper.deleteWord", map);
        print("Deleted '" + word + "' for user " + userId + " from list of ignored words, affectedRows: " + affectedRows);
        return affectedRows >= 1;
      }
    }
  }

  /**
   * @since 4.3
   */
  Long getOrCreateServerId() {
    if (sqlSessionFactory == null) {
      return null;
    }
    try {
      String hostname = InetAddress.getLocalHost().getHostName();
      try (SqlSession session = sqlSessionFactory.openSession(true)) {
        Map<Object, Object> parameters = new HashMap<>();
        parameters.put("hostname", hostname);
        List<Long> result = session.selectList("org.languagetool.server.LogMapper.findServer", parameters);
        if (result.size() > 0) {
          return result.get(0);
        } else {
          session.insert("org.languagetool.server.LogMapper.newServer", parameters);
          Object value = parameters.get("id");
          if (value == null) {
            //System.err.println("Could not get new server id for this host.");
            return null;
          } else {
            return (Long) value;
          }
        }
      }
    } catch (UnknownHostException e) {
      //System.err.println("Could not get hostname to fetch/register server id: " + e);
      return null;
    } catch (PersistenceException e) {
      //System.err.println("Could not get fetch/register server id from database: " + e);
      return null;
    }
  }

  /**
   * @since 4.3
   */
  Long getOrCreateClientId(String client) {
    if (sqlSessionFactory == null || client == null) {
      return null;
    }

    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      Map<Object, Object> parameters = new HashMap<>();
      parameters.put("name", client);
      List<Long> result = session.selectList("org.languagetool.server.LogMapper.findClient", parameters);
      if (result.size() > 0) {
        return result.get(0);
      } else {
        session.insert("org.languagetool.server.LogMapper.newClient", parameters);
        Object value = parameters.get("id");
        if (value == null) {
          //System.err.println("Could not get/register id for this client.");
          return null;
        } else {
          return (Long) value;
        }
      }
    } catch(PersistenceException e) {
      //System.err.println("Could not get/register id for this client: " + e);
      return null;
    }
  }

  @Deprecated
  void logAccess(Language lang, int textSize, int matches, Long userId) {
    if (sqlSessionFactory == null) {
      return;
    }
    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      Map<Object, Object> map = new HashMap<>();
      Calendar date = Calendar.getInstance();
      SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      map.put("day", dayFormat.format(date.getTime()));
      map.put("date", dateFormat.format(date.getTime()));
      map.put("user_id", userId);
      map.put("textsize", textSize);
      map.put("matches", matches);
      map.put("language", lang.getShortCodeWithCountryAndVariant());
      session.insert("org.languagetool.server.LogMapper.logCheck", map);
    } catch (Exception e) {
      print("Could not log check for " + userId + ": " + e.getMessage());
    }
  }
  
  private void validateWord(String word) {
    if (word == null || word.trim().isEmpty()) {
      throw new IllegalArgumentException("Invalid word, cannot be empty or whitespace only");
    }
    if (word.matches(".*\\s.*")) {
      throw new IllegalArgumentException("Invalid word, you can only words that don't contain spaces: '" + word + "'");
    }
  }

  /** For unit tests only! */
  public static void createAndFillTestTables() {
    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      System.out.println("Setting up tables and adding test user...");
      session.insert("org.languagetool.server.UserDictMapper.createUserTable");
      session.insert("org.languagetool.server.UserDictMapper.createIgnoreWordTable");
      session.insert("org.languagetool.server.UserDictMapper.createTestUser1");
      session.insert("org.languagetool.server.UserDictMapper.createTestUser2");
    }
  }
  
  /** For unit tests only! */
  public static void deleteTestTables() {
    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      System.out.println("Deleting tables...");
      session.delete("org.languagetool.server.UserDictMapper.deleteUsersTable");
      session.delete("org.languagetool.server.UserDictMapper.deleteIgnoreWordsTable");
    }
  }

  /** For unit tests only */
  static ResultSet executeStatement(SQL sql) throws SQLException {
    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      try (Connection conn = session.getConnection()) {
        try (Statement stmt = conn.createStatement()) {
          return stmt.executeQuery(sql.toString());
        }
      }
    }
  }

}
