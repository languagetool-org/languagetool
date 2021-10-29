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

  private static final ConcurrentMap<String, ThreadPoolExecutor> executorServices = new ConcurrentHashMap<>();

  private LtThreadPoolFactory() {
  }

  /**
   * @param identifier       Name of the thread-pool, will be used as name of the threads in the threadPool
   * @param maxThreads       Number of parallel threads running in this pool
   * @param maxTaskInQueue   Number of maximum Task in the pool queue
   * @param isDaemon         Run the threads as daemon threads
   * @param exceptionHandler Handler for exceptions in Thread
   * @return a Fixed ThreadPoolExecutor
   */
  private static ThreadPoolExecutor getOrCreateFixedThreadPoolExecutor(@NotNull String identifier, @NotNull int maxThreads, @NotNull int maxTaskInQueue, @NotNull boolean isDaemon, @NotNull Thread.UncaughtExceptionHandler exceptionHandler) {
    if (executorServices.containsKey(identifier)) {
      log.info("ThreadPool with identifier: " + identifier + " already exists. Return this one");
      System.out.println("ThreadPool with identifier: " + identifier + " already exists. Return this one");
      return executorServices.get(identifier);
    }
    log.info(String.format("Create new threadPool with maxThreads: %d maxTaskInQueue: %d identifier: %s daemon: %s exceptionHandler: %s", maxThreads, maxTaskInQueue, identifier, isDaemon, exceptionHandler));
    System.out.println(String.format("Create new threadPool with maxThreads: %d maxTaskInQueue: %d identifier: %s daemon: %s exceptionHandler: %s", maxThreads, maxTaskInQueue, identifier, isDaemon, exceptionHandler));
    ArrayBlockingQueue<Runnable> boundedQueue = new ArrayBlockingQueue<>(maxTaskInQueue);
    ThreadFactory threadFactory = new ThreadFactoryBuilder()
      .setNameFormat(identifier + "-%d")
      .setDaemon(isDaemon)
      .setUncaughtExceptionHandler(exceptionHandler)
      .build();
    ThreadPoolExecutor newThreadPoolExecutor = new ThreadPoolExecutor(maxThreads / 2, maxThreads, 60, SECONDS, boundedQueue, threadFactory, new ThreadPoolExecutor.AbortPolicy());
    executorServices.put(identifier, newThreadPoolExecutor);
    return newThreadPoolExecutor;
  }

  public static ThreadPoolExecutor createFixedThreadPoolExecutor(@NotNull String identifier, @NotNull int maxThreads, @NotNull int maxTaskInQueue, @NotNull boolean isDaemon, @NotNull Thread.UncaughtExceptionHandler exceptionHandler, boolean reuse) {
    if (reuse) {
      return getOrCreateFixedThreadPoolExecutor(identifier, maxThreads, maxTaskInQueue, isDaemon, exceptionHandler);
    }
    log.info(String.format("Create new threadPool with maxThreads: %d maxTaskInQueue: %d identifier: %s daemon: %s exceptionHandler: %s", maxThreads, maxTaskInQueue, identifier, isDaemon, exceptionHandler));
    System.out.println(String.format("Create new threadPool with maxThreads: %d maxTaskInQueue: %d identifier: %s daemon: %s exceptionHandler: %s", maxThreads, maxTaskInQueue, identifier, isDaemon, exceptionHandler));
    ArrayBlockingQueue<Runnable> boundedQueue = new ArrayBlockingQueue<>(maxTaskInQueue);
    ThreadFactory threadFactory = new ThreadFactoryBuilder()
      .setNameFormat(identifier + "-%d")
      .setDaemon(isDaemon)
      .setUncaughtExceptionHandler(exceptionHandler)
      .build();
    ThreadPoolExecutor newThreadPoolExecutor = new ThreadPoolExecutor(maxThreads / 2, maxThreads, 60, SECONDS, boundedQueue, threadFactory, new ThreadPoolExecutor.AbortPolicy());
    return newThreadPoolExecutor;
  }

  /**
   * @param identifier Name of an already created tread-pool
   * @return An optional of ThreadPoolExecutor (Null or Object)
   */
  public static Optional<ThreadPoolExecutor> getFixedThreadPoolExecutor(@NotNull String identifier) {
    log.info("Request: " + identifier + " ThreadPoolExecutor");
    System.out.println("Request: " + identifier + " ThreadPoolExecutor");
    return Optional.ofNullable(executorServices.get(identifier));
  }
}
