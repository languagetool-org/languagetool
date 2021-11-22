/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2021 Stefan Viol (https://stevio.de)
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

package org.languagetool.tools;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public final class LtThreadPoolFactory {
  public static final String SERVER_POOL                = "lt-server-thread";
  public static final String TEXT_CHECKER_POOL          = "lt-text-checker-thread";
  public static final String REMOTE_RULE_WAITING_POOL   = "remote-rule-waiting-thread";
  public static final String REMOTE_RULE_EXECUTING_POOL = "remote-rule-executing-thread";

  private static final ConcurrentMap<String, ThreadPoolExecutor> executorServices = new ConcurrentHashMap<>();

  private LtThreadPoolFactory() {
  }

  /**
   * @param identifier       Name of the thread-pool, will be used as name of the threads in the threadPool
   * @param maxThreads       Number of parallel threads running in this pool
   * @param maxTaskInQueue   Number of maximum Task in the pool queue
   * @param isDaemon         Run the threads as daemon threads
   * @param exceptionHandler Handler for exceptions in Thread
   * @param reuse            True if thread-pool should be reused
   * @return a Fixed ThreadPoolExecutor
   */
  public static ThreadPoolExecutor createFixedThreadPoolExecutor(@NotNull String identifier, int maxThreads, int maxTaskInQueue, boolean isDaemon, @NotNull Thread.UncaughtExceptionHandler exceptionHandler, boolean reuse) {
    return createFixedThreadPoolExecutor(identifier, maxThreads / 2, maxThreads, maxTaskInQueue, 60, isDaemon, exceptionHandler, reuse);
  }


  /**
   * @param identifier       Name of the thread-pool, will be used as name of the threads in the threadPool
   * @param corePool          Number of core pool threads
   * @param maxThreads       Maximum number of parallel threads running in this pool
   * @param maxTaskInQueue   Number of maximum Task in the pool queue
   * @param keepAliveTimeSeconds keep-alive time for idle threads
   * @param isDaemon         Run the threads as daemon threads
   * @param exceptionHandler Handler for exceptions in Thread
   * @param reuse            True if thread-pool should be reused
   * @return a Fixed ThreadPoolExecutor
   */
  public static ThreadPoolExecutor createFixedThreadPoolExecutor(@NotNull String identifier, int corePool, int maxThreads, int maxTaskInQueue, long keepAliveTimeSeconds, boolean isDaemon, @NotNull Thread.UncaughtExceptionHandler exceptionHandler, boolean reuse) {
    if (reuse) {
      return executorServices.computeIfAbsent(identifier, id -> getNewThreadPoolExecutor(identifier, corePool, maxThreads, maxTaskInQueue, keepAliveTimeSeconds, isDaemon, exceptionHandler));
    } else {
      return getNewThreadPoolExecutor(identifier, corePool, maxThreads, maxTaskInQueue, keepAliveTimeSeconds, isDaemon, exceptionHandler);
    }
  }

  @NotNull
  private static ThreadPoolExecutor getNewThreadPoolExecutor(@NotNull String identifier, int corePool, int maxThreads, int maxTaskInQueue, long keepAliveTimeSeconds, boolean isDaemon, @NotNull Thread.UncaughtExceptionHandler exceptionHandler) {
    log.debug(String.format("Create new threadPool with maxThreads: %d maxTaskInQueue: %d identifier: %s daemon: %s exceptionHandler: %s", maxThreads, maxTaskInQueue, identifier, isDaemon, exceptionHandler));
    BlockingQueue<Runnable> boundedQueue;
    if (maxTaskInQueue <= 0) {
      boundedQueue = new LinkedBlockingQueue<>();
    } else {
      boundedQueue = new ArrayBlockingQueue<>(maxTaskInQueue);
    }
    ThreadFactory threadFactory = new ThreadFactoryBuilder()
      .setNameFormat(identifier + "-%d")
      .setDaemon(isDaemon)
      .setUncaughtExceptionHandler(exceptionHandler)
      .build();
    ThreadPoolExecutor newThreadPoolExecutor = new LtThreadPoolExecutor(identifier, corePool, maxThreads, keepAliveTimeSeconds, SECONDS, boundedQueue, threadFactory, new ThreadPoolExecutor.AbortPolicy());
    return newThreadPoolExecutor;
  }

  /**
   * @param identifier Name of an already created tread-pool
   * @return An optional of ThreadPoolExecutor (Null or Object)
   */
  public static Optional<ThreadPoolExecutor> getFixedThreadPoolExecutor(@NotNull String identifier) {
    ThreadPoolExecutor value = executorServices.get(identifier);
    if (value == null) {
      log.debug("Request: " + identifier + " not found, returning default pool");
      return Optional.of(defaultPool);
    } else {
      return Optional.of(value);
    }
  }

  static final ThreadPoolExecutor defaultPool = new ThreadPoolExecutor(12, 64, 60, SECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setNameFormat("default-lt-pool-%d").build(), new ThreadPoolExecutor.AbortPolicy());
}
