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

  static DatabaseLogger getInstance(SqlSessionFactory sessionFactory) {
    if (instance == null) {
      instance = new DatabaseLogger(sessionFactory);
    }
    return instance;
  }

  private class WorkerThread extends Thread {
    @Override
    public void run() {
      try (SqlSession session = sessionFactory.openSession(true)) {
        while (!Thread.currentThread().isInterrupted()) {
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

  private final BlockingQueue<DatabaseLogEntry> messages;
  private final SqlSessionFactory sessionFactory;

  private DatabaseLogger(SqlSessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
    if (sessionFactory != null) {
       messages = new LinkedBlockingQueue<>();
      WorkerThread worker = new WorkerThread();
      worker.start();
    } else {
      messages = null;
    }
  }

  public void log(DatabaseLogEntry entry) {
    try {
      if (messages != null) {
        messages.put(entry);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
