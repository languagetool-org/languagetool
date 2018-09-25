/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.server;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @since 4.3
 */
class DatabaseLogger {

  private static DatabaseLogger instance = null;

  /**
   * @return an instance that will be disabled until initialized by DatabaseAccess
   */

  public static DatabaseLogger getInstance() {
    if (instance == null) {
      instance = new DatabaseLogger();
    }
    return instance;
  }

  /**
   * called by DatabaseAccess.init
   * @param factory shared factory from DatabaseAccess
   */
  static void init(SqlSessionFactory factory) {
   getInstance().start(factory);
  }

  private class WorkerThread extends Thread {
    @Override
    public void run() {
      try (SqlSession session = sessionFactory.openSession(true)) {
        while (!Thread.currentThread().isInterrupted()) {
          if (messages.size() > 10) {
            ServerTools.print(String.format("Logging queue filling up: %d entries", messages.size()));
          }
          DatabaseLogEntry entry = messages.take();
          Map<Object, Object> parameters = entry.getMapping();
          session.insert(entry.getMappingIdentifier(), parameters);
          entry.followup(parameters);
        }
      } catch (Exception e) {
        e.printStackTrace();
        if (!Thread.currentThread().isInterrupted()) {
          new WorkerThread().start();
        }
      }
    }
  }

  private final BlockingQueue<DatabaseLogEntry> messages = new LinkedBlockingQueue<>();;
  private SqlSessionFactory sessionFactory = null;
  private WorkerThread worker = null;
  private boolean disabled = true;

  private void start(SqlSessionFactory factory) {
    sessionFactory = factory;
    disabled = false;
    worker = new WorkerThread();
    worker.start();
  }

  private DatabaseLogger() {

  }

  public void disableLogging() {
    this.disabled = true;
    if (worker != null) {
      worker.interrupt();
    }
  }

  /**
   * For use in unit tests, because logging may require information from the database
   * which might not be setup there
   */
  public boolean isLogging() {
    return !this.disabled;
  }

  public void log(DatabaseLogEntry entry) {
    try {
      if (!disabled) {
        messages.put(entry);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  void createTestTables() {
    try (SqlSession session = sessionFactory.openSession(true)) {
      session.insert("org.languagetool.server.LogMapper.createRuleMatches");
      session.insert("org.languagetool.server.LogMapper.createCheckLog");
      session.insert("org.languagetool.server.LogMapper.createMiscLog");
      session.insert("org.languagetool.server.LogMapper.createAccessLimits");
      session.insert("org.languagetool.server.LogMapper.createCheckError");
      session.insert("org.languagetool.server.LogMapper.createCacheStats");
      session.insert("org.languagetool.server.LogMapper.createServers");
      session.insert("org.languagetool.server.LogMapper.createClients");
    }
  }

  void dropTestTables() {
    try (SqlSession session = sessionFactory.openSession(true)) {
      session.delete("org.languagetool.server.LogMapper.dropRuleMatches");
      session.delete("org.languagetool.server.LogMapper.dropCheckLog");
      session.delete("org.languagetool.server.LogMapper.dropMiscLog");
      session.delete("org.languagetool.server.LogMapper.dropAccessLimits");
      session.delete("org.languagetool.server.LogMapper.dropCheckError");
      session.delete("org.languagetool.server.LogMapper.dropCacheStats");
      session.delete("org.languagetool.server.LogMapper.dropServers");
      session.delete("org.languagetool.server.LogMapper.dropClients");
    }
  }

}
