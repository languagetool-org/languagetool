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

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.languagetool.server.ServerTools.print;

/**
 * Encapsulate database access. Will do nothing if database access is not configured.
 * @since 4.2
 */
class DatabaseAccess {

  private static DatabaseAccess instance;
  private static SqlSessionFactory sqlSessionFactory;
  
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
      List<UserDictEntry> dict = session.selectList("org.languagetool.server.UserDictMapper.selectWordList", userId);
      for (UserDictEntry userDictEntry : dict) {
        dictEntries.add(userDictEntry.getWord());
      }
    }
    return dictEntries;
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
      HashMap<Object, Object> map = new HashMap<>();
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
}
