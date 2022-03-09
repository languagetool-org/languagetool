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

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @since 4.3
 */
class DatabaseLogger {

  // package private for mocking in tests
  static DatabaseLogger instance = null;

  // smaller numbers used for tests
  static final int POLLING_TIME = 1000;
  static int SQL_BATCH_SIZE = 1000;
  static int SQL_BATCH_WAITING_TIME = 10000; // milliseconds to wait until batch gets committed anyway
  
  private static final int MAX_QUEUE_SIZE = 50000; // drop entries after limit is reached, to avoid running out of memory

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
      try (SqlSession session = sessionFactory.openSession(ExecutorType.BATCH, false)) {
        while (!Thread.currentThread().isInterrupted()) {
          int batchSize = 0;
          long batchTime = System.currentTimeMillis();
          // commit when batch size is reached or after waiting period elapsed
          while(!Thread.currentThread().isInterrupted()
            && batchSize < SQL_BATCH_SIZE
            && System.currentTimeMillis() - batchTime < SQL_BATCH_WAITING_TIME)  {
            if (messages.size() > SQL_BATCH_SIZE) {
              ServerTools.print(String.format("Logging queue filling up: %d entries", messages.size()));
            }
            // polling to be able to react when waiting time has elapsed
            DatabaseLogEntry entry = messages.poll(POLLING_TIME, TimeUnit.MILLISECONDS);
            if (entry == null) {
              continue;
            }
            batchSize++;
            session.insert(entry.getMappingIdentifier(), entry.getMapping());
            DatabaseLogEntry followup = entry.followup();
            if (followup != null) { // followup statements need to be inserted directly afterwards, dependant on e.g. generated primary keys
              session.insert(followup.getMappingIdentifier(), followup.getMapping());
              batchSize++;
            }
          }
          session.commit();
        }
      } catch (Exception e) {
        e.printStackTrace();
        if (!Thread.currentThread().isInterrupted()) {
          new WorkerThread().start();
        }
      }
    }
  }

  private final BlockingQueue<DatabaseLogEntry> messages = new LinkedBlockingQueue<>();
  private SqlSessionFactory sessionFactory = null;
  private WorkerThread worker = null;
  private boolean disabled = true;

  private DatabaseLogger() {
  }

  private void start(SqlSessionFactory factory) {
    sessionFactory = factory;
    disabled = false;
    worker = new WorkerThread();
    worker.start();
  }

  public void disableLogging() {
    this.disabled = true;
    if (worker != null) {
      worker.interrupt();
    }
  }

  public boolean isLogging() {
    return !this.disabled;
  }

  public void log(DatabaseLogEntry entry) {
    try {
      if (!disabled) {
        if (messages.size() < MAX_QUEUE_SIZE) {
          messages.put(entry);
        } else {
          ServerTools.print("Logging queue has reached size limit; discarding new messages.");
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * For testing; wait until queue is empty.
   */
  public void flush() {
    try {
      while (messages.peek() != null) {
          Thread.sleep(100);
      }
      Thread.sleep(SQL_BATCH_WAITING_TIME + 5000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  void createTestTables() {
    createTestTables(false);
  }

  void createTestTables(boolean mysql) {
    try (SqlSession session = sessionFactory.openSession(true)) {
      String[] statements = {"org.languagetool.server.LogMapper.createRuleMatches",
        "org.languagetool.server.LogMapper.createCheckLog",
        "org.languagetool.server.LogMapper.createMiscLog",
        "org.languagetool.server.LogMapper.createPings",
        "org.languagetool.server.LogMapper.createAccessLimits",
        "org.languagetool.server.LogMapper.createCheckError",
        "org.languagetool.server.LogMapper.createCacheStats",
        "org.languagetool.server.LogMapper.createServers",
        "org.languagetool.server.LogMapper.createClients"};
      for (String statement : statements) {
        if (mysql) {
          session.insert(statement + "MySQL");
        } else {
          session.insert(statement);
        }
      }
    }
  }

  void dropTestTables() {
    try (SqlSession session = sessionFactory.openSession(true)) {
      session.delete("org.languagetool.server.LogMapper.dropRuleMatches");
      session.delete("org.languagetool.server.LogMapper.dropCheckLog");
      session.delete("org.languagetool.server.LogMapper.dropMiscLog");
      session.delete("org.languagetool.server.LogMapper.dropPings");
      session.delete("org.languagetool.server.LogMapper.dropAccessLimits");
      session.delete("org.languagetool.server.LogMapper.dropCheckError");
      session.delete("org.languagetool.server.LogMapper.dropCacheStats");
      session.delete("org.languagetool.server.LogMapper.dropServers");
      session.delete("org.languagetool.server.LogMapper.dropClients");
    }
  }

}
