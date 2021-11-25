/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2020 Fabian Richter
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

package org.languagetool.tools;

import io.prometheus.client.Gauge;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * ThreadPoolExecutor with some stopping logic for OOM and metrics tracking
 */
class LtThreadPoolExecutor extends ThreadPoolExecutor {

//  private static final Gauge activeThreads = Gauge.build("languagetool_threadpool_active_threads", "Running threads by threadpool")
//    .labelNames("pool").register();
  private static final Gauge queueSize = Gauge.build("languagetool_threadpool_queue_size", "Queue size by threadpool")
    .labelNames("pool").register();
  private static final Gauge maxQueueSize = Gauge.build("languagetool_threadpool_max_queue_size", "Queue capacity by threadpool")
    .labelNames("pool").register();
  private static final Gauge largestPoolSize = Gauge.build("languagetool_threadpool_largest_queue_size", "The largest number of threads that have ever simultaneously been in the pool")
    .labelNames("pool").register();
  private static final Gauge waitingThreads = Gauge.build("languagetool_threadpool_waiting_threads", "Waiting threads by threadpool")
    .labelNames("pool").register();
  private static final Gauge timedWaitingThreads = Gauge.build("languagetool_threadpool_timed_waiting_threads", "Timed_Waiting threads by threadpool")
    .labelNames("pool").register();
  private static final Gauge blockingThreads = Gauge.build("languagetool_threadpool_blocking_threads", "Blocking threads by threadpool")
    .labelNames("pool").register();
  private static final Gauge runningThreads = Gauge.build("languagetool_threadpool_running_threads", "Running threads by threadpool")
    .labelNames("pool").register();

  @Getter
  private final String name;
  private final Queue<Runnable> queue;

  LtThreadPoolExecutor(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull BlockingQueue<Runnable> workQueue, @NotNull ThreadFactory threadFactory, @NotNull RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    this.name = name;
    this.queue = getQueue();
    maxQueueSize.labels(name).set(workQueue.remainingCapacity());
  }

  @Override
  public void execute(@NotNull Runnable command) {
    super.execute(command);
    queueSize.labels(name).set(queue.size());
  }

  @Override
  public boolean remove(Runnable task) {
    boolean status = super.remove(task);
    queueSize.labels(name).set(queue.size());
    return status;
  }

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    super.afterExecute(r, t);
    updateThreadGauges();
//    activeThreads.labels(name).dec();
//    waitingThreads.labels(name).inc();
    queueSize.labels(name).set(queue.size());
    // inherited from removed StoppingThreadPoolExecutor in org.languagetool.server.Server
    if (t != null && t instanceof OutOfMemoryError) {
      // we prefer to stop instead of being in an unstable state:
      //noinspection CallToPrintStackTrace
      t.printStackTrace();
      System.exit(1);
    }
  }

  @Override
  protected void beforeExecute(Thread t, Runnable r) {
    super.beforeExecute(t, r);
    updateThreadGauges();
//    activeThreads.labels(name).inc();
//    waitingThreads.labels(name).dec();
  }

  @NotNull
  @Override
  public Future<?> submit(@NotNull Runnable runnable) {
    largestPoolSize.labels(name).set(getLargestPoolSize());
    return super.submit(runnable);
  }

  @NotNull
  @Override
  public <T> Future<T> submit(@NotNull Runnable runnable, T t) {
    largestPoolSize.labels(name).set(getLargestPoolSize());
    return super.submit(runnable, t);
  }

  @NotNull
  @Override
  public <T> Future<T> submit(@NotNull Callable<T> callable) {
    largestPoolSize.labels(name).set(getLargestPoolSize());
    return super.submit(callable);
  }

  private void updateThreadGauges() {
    Set<Thread> threads = Thread.getAllStackTraces().keySet();
    Stream<Thread> filtered = threads.stream().filter(thread -> thread.getName().startsWith(name));
    blockingThreads.labels(name).set(filtered.filter(thread -> thread.getState() == Thread.State.BLOCKED).count());
    waitingThreads.labels(name).set(filtered.filter(thread -> thread.getState() == Thread.State.WAITING).count());
    timedWaitingThreads.labels(name).set(filtered.filter(thread -> thread.getState() == Thread.State.TIMED_WAITING).count());
    runningThreads.labels(name).set(filtered.filter(thread -> thread.getState() == Thread.State.RUNNABLE).count());
  }
}
