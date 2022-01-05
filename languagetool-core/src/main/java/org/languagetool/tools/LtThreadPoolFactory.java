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
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public final class LtThreadPoolFactory {
  public static final String SERVER_POOL = "lt-server-thread";
  public static final String TEXT_CHECKER_POOL = "lt-text-checker-thread";
  public static final String REMOTE_RULE_EXECUTING_POOL = "remote-rule-executing-thread";
  public static final int REMOTE_RULE_POOL_SIZE_FACTOR = 4;
  // we need more maximum threads for timed out requests that haven't been interrupted/cancelled (or reacted to that) yet

  private static final ConcurrentMap<String, ThreadPoolExecutor> executorServices = new ConcurrentHashMap<>();

  private static final Counter rejectedTasks = Counter.build("languagetool_threadpool_rejected_tasks",
    "Rejected tasks by threadpool").labelNames("pool").register();
  private static final Gauge threadGauge = Gauge.build("languagetool_threadpool_thread_states", "Threads by states and threadpool")
    .labelNames("pool", "state").register();
//  private static final Gauge waitingThreads = Gauge.build("languagetool_threadpool_waiting_threads", "Waiting threads by threadpool")
//    .labelNames("pool", "state").register();
//  private static final Gauge timedWaitingThreads = Gauge.build("languagetool_threadpool_timed_waiting_threads", "Timed_Waiting threads by threadpool")
//    .labelNames("pool", "state").register();
//  private static final Gauge blockingThreads = Gauge.build("languagetool_threadpool_blocking_threads", "Blocking threads by threadpool")
//    .labelNames("pool", "state").register();
//  private static final Gauge runningThreads = Gauge.build("languagetool_threadpool_running_threads", "Running threads by threadpool")
//    .labelNames("pool", "state").register();
  
  private LtThreadPoolFactory() {
  }

  static {
    Timer timer = new Timer("LtThreadPoolMonitor", true);
    TimerTask timedAction = new TimerTask() {
      final String[] poolNames = new String[]{SERVER_POOL, TEXT_CHECKER_POOL, REMOTE_RULE_EXECUTING_POOL};

      @Override
      public void run() {
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        Arrays.stream(poolNames).forEach(name -> {
          Stream<Thread> blocked = threads.stream().filter(thread -> thread.getName().startsWith(name) && thread.getState() == Thread.State.BLOCKED);
          Stream<Thread> waiting = threads.stream().filter(thread -> thread.getName().startsWith(name) && thread.getState() == Thread.State.WAITING);
          Stream<Thread> waiting_timed = threads.stream().filter(thread -> thread.getName().startsWith(name) && thread.getState() == Thread.State.TIMED_WAITING);
          Stream<Thread> running = threads.stream().filter(thread -> thread.getName().startsWith(name) && thread.getState() == Thread.State.RUNNABLE);
          threadGauge.labels(name, "blocking").set(blocked.count());
          threadGauge.labels(name, "waiting").set(waiting.count());
          threadGauge.labels(name, "timed-waiting").set(waiting_timed.count());
          threadGauge.labels(name, "running").set(running.count());
          log.trace(LoggingTools.SYSTEM, "{} blockingThreads: {}", name, threadGauge.labels(name, "blocking").get());
          log.trace(LoggingTools.SYSTEM, "{} waitingThreads: {}", name, threadGauge.labels(name, "waiting").get());
          log.trace(LoggingTools.SYSTEM, "{} timedWaitingThreads: {}", name, threadGauge.labels(name, "timed-waiting").get());
          log.trace(LoggingTools.SYSTEM, "{} runningThreads: {}", name, threadGauge.labels(name, "running").get());
        });
      }
    };
    timer.scheduleAtFixedRate(timedAction, 0, 1000);
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
   * @param identifier           Name of the thread-pool, will be used as name of the threads in the threadPool
   * @param corePool             Number of core pool threads
   * @param maxThreads           Maximum number of parallel threads running in this pool
   * @param maxTaskInQueue       Number of maximum Task in the pool queue
   * @param keepAliveTimeSeconds keep-alive time for idle threads
   * @param isDaemon             Run the threads as daemon threads
   * @param exceptionHandler     Handler for exceptions in Thread
   * @param reuse                True if thread-pool should be reused
   * @return a Fixed ThreadPoolExecutor
   */
  public static ThreadPoolExecutor createFixedThreadPoolExecutor(@NotNull String identifier, int corePool, int maxThreads, int maxTaskInQueue, long keepAliveTimeSeconds, boolean isDaemon, @NotNull Thread.UncaughtExceptionHandler exceptionHandler, boolean reuse) {
    if (reuse) {
      return executorServices.computeIfAbsent(identifier, id -> getNewThreadPoolExecutor(identifier, corePool, maxThreads, maxTaskInQueue, keepAliveTimeSeconds, isDaemon, exceptionHandler));
    } else {
      return getNewThreadPoolExecutor(identifier, corePool, maxThreads, maxTaskInQueue, keepAliveTimeSeconds, isDaemon, exceptionHandler);
    }
  }

  private static class LtRejectedExecutionHandler extends ThreadPoolExecutor.AbortPolicy {

    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
      String pool = ((LtThreadPoolExecutor) threadPoolExecutor).getName();
      rejectedTasks.labels(pool).inc();
      log.warn(LoggingTools.SYSTEM, "Task rejected from pool '{}' (queue full, all threads exhausted)", pool);
      super.rejectedExecution(runnable, threadPoolExecutor);
    }
  }

  private static final LtRejectedExecutionHandler handler = new LtRejectedExecutionHandler();

  @NotNull
  private static ThreadPoolExecutor getNewThreadPoolExecutor(@NotNull String identifier, int corePool, int maxThreads, int maxTaskInQueue, long keepAliveTimeSeconds, boolean isDaemon, @NotNull Thread.UncaughtExceptionHandler exceptionHandler) {
    log.debug(LoggingTools.SYSTEM, String.format("Create new threadPool with corePool: %d maxThreads: %d maxTaskInQueue: %d identifier: %s daemon: %s exceptionHandler: %s", corePool, maxThreads, maxTaskInQueue, identifier, isDaemon, exceptionHandler));
    BlockingQueue<Runnable> queue;
    if (maxTaskInQueue == 0) {
      queue = new LinkedBlockingQueue<>();
    } else if (maxTaskInQueue < 0) {
      queue = new SynchronousQueue<>();
    } else {
      // fair = true helps with respecting keep-alive time
      queue = new ArrayBlockingQueue<>(maxTaskInQueue, true);
    }
    ThreadFactory threadFactory = new ThreadFactoryBuilder()
      .setNameFormat(identifier + "-%d")
      .setDaemon(isDaemon)
      .setUncaughtExceptionHandler(exceptionHandler)
      .build();
    ThreadPoolExecutor newThreadPoolExecutor = new LtThreadPoolExecutor(identifier, corePool, maxThreads, keepAliveTimeSeconds, SECONDS, queue, threadFactory, handler);
    return newThreadPoolExecutor;
  }

  /**
   * @param identifier Name of an already created tread-pool
   * @return An optional of ThreadPoolExecutor (Null or Object)
   */
  public static Optional<ThreadPoolExecutor> getFixedThreadPoolExecutor(@NotNull String identifier) {
    ThreadPoolExecutor value = executorServices.get(identifier);
    if (value == null) {
      log.debug(LoggingTools.SYSTEM, "Request: " + identifier + " not found, returning default pool");
      return Optional.of(defaultPool);
    } else {
      return Optional.of(value);
    }
  }

  static final ThreadPoolExecutor defaultPool = new ThreadPoolExecutor(12, 64, 60, SECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setNameFormat("default-lt-pool-%d").build(), new ThreadPoolExecutor.AbortPolicy());
}
