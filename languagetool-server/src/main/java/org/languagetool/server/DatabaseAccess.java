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
import org.apache.ibatis.jdbc.SQL;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Premium;
import org.languagetool.rules.Rule;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulate database access. Will do nothing if database access is not configured.
 *
 * @since 4.2
 */
abstract class DatabaseAccess {
  private static DatabaseAccess instance;
  protected SqlSessionFactory sqlSessionFactory;

  /**
   * Implementations required to provide a constructor with the same signature
   */
  protected DatabaseAccess(HTTPServerConfig config) {
  }
  
  static synchronized void init(HTTPServerConfig config) {
    if (instance == null) {
      String className = "org.languagetool.server.DatabaseAccess";
      if (Premium.isPremiumVersion()) {
        className += "Premium";
      } else {
        className += "OpenSource";
      }
      try {
        Class<DatabaseAccess> clazz = (Class<DatabaseAccess>) JLanguageTool.getClassBroker().forName(className);
        instance = clazz.getConstructor(HTTPServerConfig.class).newInstance(config);
      } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
  }

  static synchronized void reset() {
    if (instance != null) {
      instance.sqlSessionFactory = null;
    }
    instance = null;
  }

  static synchronized DatabaseAccess getInstance() {
    if (instance == null) {
      throw new IllegalStateException("DatabaseAccess.init() has not been called yet or failed");
    }
    return instance;
  }

  /**
   * @since 5.7
   * Test if instance is configured and can be used
   */
  static synchronized boolean isReady() {
    return instance != null;
  }

  /**
   * For tests, to avoid waiting for the invalidation period.
   */
  abstract void invalidateCaches();

  abstract boolean addWord(String word, Long userId, String groupName);

  abstract boolean deleteWord(String word, Long userId, String groupName);

  /**
   * remove words in sql batch mode, no auto commit for better performance with large lists
   * also suppresses uniqueness checks
   */
  abstract boolean deleteWordBatch(List<String> words, Long userId, String groupName);

  /**
   * add words in sql batch mode, no auto commit for better performance with large lists
   * also suppresses uniqueness checks
   */
  abstract void addWordBatch(List<String> words, Long userId, String groupName);

  abstract UserInfoEntry getUserInfoWithPassword(String username, String password);

  /**
   * Get more general information on a user
   * Expects access to already be authorized
   *
   * @param user email address of user
   * @return POJO with user information
   */
  abstract ExtendedUserInfo getExtendedUserInfo(String user);

  /**
   * Get more general information on a user.
   * Expects access to already be authorized.
   *
   * @param userId user id
   * @return POJO with more user information
   */
  abstract ExtendedUserInfo getExtendedUserInfo(long userId);

  abstract UserInfoEntry getUserInfoWithApiKey(String username, String apiKey);

  abstract UserInfoEntry getUserInfoWithAddonToken(String username, String apiKey);

  abstract void invalidateUserInfoCache(String user);

  abstract Long getUserRequestCount(Long userId);

  abstract Long getOrCreateServerId();

  abstract Long getOrCreateClientId(String client);


  /**
   * get all dictionary groups belonging to a user
   */
  abstract List<DictGroupEntry> getDictGroups(Long userId);

  /**
   * get or create a group with this name if it doesn't exist
   *
   * @return id of the created/existing group
   */
  abstract Long getOrCreateDictGroup(Long userId, String groupName);

  /**
   * For unit tests only!
   */
  public void createAndFillTestTables() {
    createAndFillTestTables(false);
  }

  /**
   * For unit tests only!
   */
  public void createAndFillTestTables(boolean mysql) {
    createAndFillTestTables(false, Collections.emptyList());
  }

  /**
   * For unit tests only!
   */
  public abstract void createAndFillTestTables(boolean mysql, List<String> skipStatements);

  /**
   * For unit tests only!
   */
  public void shutdownCompact() {
    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      System.out.println("Running shutdownCompact...");
      session.update("shutdownCompact");
    }
  }

  /**
   * For unit tests only!
   */
  abstract void deleteTestTables();

  /** For unit tests only! */
  ResultSet executeStatement(SQL sql) throws SQLException {
    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      try (Connection conn = session.getConnection()) {
        try (Statement stmt = conn.createStatement()) {
          return stmt.executeQuery(sql.toString());
        }
      }
    }
  }

  /** For unit tests only! */
  void execute(SQL sql) throws SQLException {
    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      try (Connection conn = session.getConnection()) {
        try (Statement stmt = conn.createStatement()) {
          stmt.execute(sql.toString());
        }
      }
    }
  }

  /**
   * @param limits user account and settings for e.g. caching
   * @param groups names of dictionaries to be fetched, or null for default dictionary
   * @param offset use offset with limit for an ordered list of words in the dictionary, or RowBounds.NO_ROW_OFFSET
   * @param limit use limit with offset for an ordered list of words in the dictionary, or use RowBounds.NO_ROW_LIMIT
   * @return a list of words from the user's dictionary (complete, or from the given range)
   */
  public abstract List<String> getWords(UserLimits limits, List<String> groups, int offset, int limit);

  /**
   * @param limits user account and settings for e.g. caching
   * @param lang language of rules to fetch; fetches global rules and language-specific rules for that language
   * @param groups names of groups of rules to be fetched, or null for default set of rules
   * @return a list of user rules (complete, or from the given range)
   */
  public abstract List<Rule> getRules(UserLimits limits, Language lang, @Nullable List<String> groups);
}
