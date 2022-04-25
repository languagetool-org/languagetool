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
import org.apache.commons.lang3.NotImplementedException;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.languagetool.Language;
import org.languagetool.Premium;
import org.languagetool.rules.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Encapsulate database access. Will do nothing if database access is not configured.
 * @since 4.2
 */
class DatabaseAccessOpenSource extends DatabaseAccess {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseAccessOpenSource.class);
  private static final String NON_PREMIUM_MSG = "This server does not support username/password";

  private final Cache<String, Long> dbLoggingCache = CacheBuilder.newBuilder()
    .expireAfterAccess(1, TimeUnit.HOURS)
    .maximumSize(5000)
    .build();

  public DatabaseAccessOpenSource(HTTPServerConfig config) {
    super(config);
    if (config.getDatabaseDriver() != null) {
      try {
        logger.info("Setting up database access, URL " + config.getDatabaseUrl() + ", driver: " + config.getDatabaseDriver() + ", user: " + config.getDatabaseUsername());
        InputStream inputStream = Resources.getResourceAsStream("org/languagetool/server/mybatis-config.xml");
        Properties properties = new Properties();
        properties.setProperty("driver", config.getDatabaseDriver());
        properties.setProperty("url", config.getDatabaseUrl());
        properties.setProperty("username", config.getDatabaseUsername());
        properties.setProperty("password", config.getDatabasePassword());
        properties.setProperty("premium", Premium.isPremiumVersion() ? "Premium" : "OpenSource");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, properties);

        // try to close connections even on hard restart
        // workaround as described in https://github.com/mybatis/mybatis-3/issues/821
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
          if (sqlSessionFactory != null) {
            ((PooledDataSource) sqlSessionFactory
              .getConfiguration().getEnvironment().getDataSource()).forceCloseAll();
          }
        }));

        DatabaseLogger.init(sqlSessionFactory);
        if (!config.getDatabaseLogging()) {
          logger.info("dbLogging not set to true, turning off logging");
          DatabaseLogger.getInstance().disableLogging();
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      logger.info("Not setting up database access, dbDriver is not configured");
    }
  }

  @Override
  void invalidateCaches() {

  }

  @Override
  boolean addWord(String word, Long userId, String groupName) {
    return addWord(word, userId);
  }

  @Override
  boolean deleteWord(String word, Long userId, String groupName) {
    return deleteWord(word, userId);
  }

  @Override
  boolean deleteWordBatch(List<String> words, Long userId, String groupName) {
    boolean deleted = false;
    for (String word : words) {
      if (deleteWord(word, userId)) {
        deleted = true;
      }
    }
    return deleted;
  }

  @Override
  void addWordBatch(List<String> words, Long userId, String groupName) {
    words.forEach(w -> addWord(w, userId));
  }

  @Override
  UserInfoEntry getUserInfoWithPassword(String username, String password) {
    throw new NotImplementedException(NON_PREMIUM_MSG);
  }

  @Override
  ExtendedUserInfo getExtendedUserInfo(String user) {
    throw new NotImplementedException(NON_PREMIUM_MSG);
  }

  @Override
  ExtendedUserInfo getExtendedUserInfo(long userId) {
    throw new NotImplementedException(NON_PREMIUM_MSG);
  }

  @Override
  UserInfoEntry getUserInfoWithApiKey(String username, String apiKey) {
    Long userId = getUserId(username, apiKey);
    UserInfoEntry user = new UserInfoEntry(userId, username, null, null, null, null, null, null, null, null,  apiKey, null);
    return user;
  }

  @Override
  UserInfoEntry getUserInfoWithAddonToken(String username, String apiKey) {
    throw new NotImplementedException(NON_PREMIUM_MSG);
  }

  @Override
  void invalidateUserInfoCache(String user) {
    throw new NotImplementedException(NON_PREMIUM_MSG);
  }

  @Override
  Long getUserRequestCount(Long userId) {
    return null;
  }

  List<String> getWords(Long userId, int offset, int limit) {
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
      List<String> existingWords = session.selectList("org.languagetool.server.UserDictMapper.selectWord", map);
      if (existingWords.size() >= 1) {
        logger.info("Did not add '" + word + "' for user " + userId + " to list of ignored words, already exists");
        return false;
      } else {
        Date now = new Date();
        map.put("created_at", now);
        map.put("updated_at", now);
        int affectedRows = session.insert("org.languagetool.server.UserDictMapper.addWord", map);
        logger.info("Added '" + word + "' for user " + userId + " to list of ignored words, affectedRows: " + affectedRows);
        return affectedRows == 1;
      }
    }
  }

  Long getUserId(String username, String apiKey) {
    if (username == null || username.trim().isEmpty()) {
      throw new IllegalArgumentException("username must be set");
    }
    if (apiKey == null || apiKey.trim().isEmpty()) {
      throw new IllegalArgumentException("apiKey must be set");
    }
    if (sqlSessionFactory ==  null) {
      throw new IllegalStateException("sqlSessionFactory not initialized - has the database been configured?");
    }
    try {
      Long value = dbLoggingCache.get(String.format("user_%s_%s", username, apiKey), () -> {
        try (SqlSession session = sqlSessionFactory.openSession()) {
          Map<Object, Object> map = new HashMap<>();
          map.put("username", username);
          map.put("apiKey", apiKey);
          Long id = session.selectOne("org.languagetool.server.UserDictMapper.getUserIdByApiKey", map);
          if (id == null) {
            return -1L;
          }
          return id;
        }
      });
      if (value == -1) {
        throw new IllegalArgumentException("No user found for given username '" + username + "' and given api key");
      } else {
        return value;
      }
    } catch (ExecutionException e) {
      throw new IllegalStateException("Could not fetch given user '" + username + "' from cache", e);
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
        logger.info("Did not delete '" + word + "' for user " + userId + " from list of ignored words, does not exist");
        return false;
      } else {
        int affectedRows = session.delete("org.languagetool.server.UserDictMapper.deleteWord", map);
        logger.info("Deleted '" + word + "' for user " + userId + " from list of ignored words, affectedRows: " + affectedRows);
        return affectedRows >= 1;
      }
    }
  }

  /**
   * @since 4.3
   */
  @Override
  Long getOrCreateServerId() {
    // original code not working anymore
    // database logging is going to be deprecated
    // this is not supported anymore, just silently fail
    return null;
  }

  /**
   * @since 4.3
   */
  @Override
  Long getOrCreateClientId(String client) {
    if (sqlSessionFactory == null || client == null) {
      return null;
    }
    try {
      Long id = dbLoggingCache.get("client_" + client, () -> {
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
              return -1L;
            } else {
              return (Long) value;
            }
          }
        } catch (PersistenceException e) {
          logger.warn("Error: Could not get/register id for this client: " + client, e);
          return -1L;
        }
      });
      if (id == -1L) { // loaders can't return null, so using -1 instead
        return null;
      } else {
        return id;
      }
    } catch (ExecutionException e) {
      logger.warn("Failure in getOrCreateClientId with client '" + client + "': ", e);
      return null;
    }
  }

  @Override
  List<DictGroupEntry> getDictGroups(Long userId) {
    return Collections.emptyList();
  }

  @Override
  Long getOrCreateDictGroup(Long userId, String groupName) {
    throw new NotImplementedException(NON_PREMIUM_MSG);
  }

  private void validateWord(String word) {
    if (word == null || word.trim().isEmpty()) {
      throw new BadRequestException("Invalid word, cannot be empty or whitespace only");
    }
    if (word.matches(".*\\s.*")) {
      throw new BadRequestException("Invalid word, you can only words that don't contain spaces: '" + word + "'");
    }
  }

  /** For unit tests only! */
  @Override
  public void createAndFillTestTables(boolean mysql, List<String> skipStatements) {
    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      System.out.println("Setting up tables and adding test user...");
      String[] statements = { "org.languagetool.server.UserDictMapper.createUserTable",
        "org.languagetool.server.UserDictMapper.createIgnoreWordTable" };
      for (String statement : statements) {
        if (skipStatements.contains(statement)) {
          continue;
        }
        if (mysql) {
          session.insert(statement + "MySQL");
        } else {
          session.insert(statement);
        }
      }
      session.insert("org.languagetool.server.UserDictMapper.createTestUser1");
      session.insert("org.languagetool.server.UserDictMapper.createTestUser2");
    }
  }

  /** For unit tests only! */
  @Override
  public void deleteTestTables() {
    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      System.out.println("Deleting tables...");
      session.delete("org.languagetool.server.UserDictMapper.deleteUsersTable");
      session.delete("org.languagetool.server.UserDictMapper.deleteIgnoreWordsTable");
    }
  }

  @Override
  public List<String> getWords(UserLimits limits, List<String> groups, int offset, int limit) {
    return getWords(limits.getPremiumUid(), offset, limit);
  }

  @Override
  public List<Rule> getRules(UserLimits limits, Language lang, List<String> groups) {
    // not implemented in open source
    return Collections.emptyList();
  }

}
