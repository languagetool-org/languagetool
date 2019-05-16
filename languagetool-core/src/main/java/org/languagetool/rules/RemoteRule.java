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

package org.languagetool.rules;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.languagetool.AnalyzedSentence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public abstract class RemoteRule extends Rule {
  private static final Logger logger = LoggerFactory.getLogger(RemoteRule.class);

  /* needs to be shared between rule instances because new instances may be created and discarded often
     needs to be a map because 'static' and inheritance don't play nice in Java */
  private static final ConcurrentMap<String, Long> lastFailure = new ConcurrentHashMap<>();
  private static final ConcurrentMap<String, AtomicInteger> consecutiveFailures = new ConcurrentHashMap<>();
  private static final ThreadFactory threadFactory = new ThreadFactoryBuilder()
    .setNameFormat("remote-rule-pool-{}").setDaemon(true).build();
  // needed to run callables with timeout
  private static final ConcurrentMap<String, ExecutorService> executors = new ConcurrentHashMap<>();
  protected final RemoteRuleConfig serviceConfiguration;

  public RemoteRule(ResourceBundle messages, List<RemoteRuleConfig> configuredServices) {
    super(messages);
    serviceConfiguration = RemoteRuleConfig.getRelevantConfig(this, configuredServices);
    String rule = getId();
    lastFailure.putIfAbsent(rule, 0L);
    consecutiveFailures.putIfAbsent(rule, new AtomicInteger());
    // TODO maybe use fixed pool, take number of concurrent requests from configuration?
    executors.putIfAbsent(rule, Executors.newCachedThreadPool(threadFactory));
  }

  protected abstract Callable<List<RuleMatch>> fetchMatches(List<AnalyzedSentence> sentences);

  public FutureTask<List<RuleMatch>> run(List<AnalyzedSentence> sentences) {
    return new FutureTask<>(() -> {
      String rule = getId();
      if (consecutiveFailures.get(rule).get() >= serviceConfiguration.getFall()) {
        long failureInterval = System.currentTimeMillis() - lastFailure.get(rule);
        if (failureInterval < serviceConfiguration.getDownMilliseconds()) {
          return Collections.emptyList();
        }
      }

      for (int i = 0; i <= serviceConfiguration.getMaxRetries(); i++) {
        Callable<List<RuleMatch>> task = fetchMatches(sentences);
        try {
          long timeout = serviceConfiguration.getTimeoutMilliseconds();
          Future<List<RuleMatch>> future = executors.get(rule).submit(task);
          List<RuleMatch> result;
          if (timeout <= 0)  { // for debugging, disable timeout
            result = future.get();
          } else {
            result = future.get(serviceConfiguration.getTimeoutMilliseconds(), TimeUnit.MILLISECONDS);
          }
          future.cancel(true);
          consecutiveFailures.get(rule).set(0);
          return result;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
          logger.warn("Error while fetching results for remote rule " + rule + ", tried " + (i + 1) + " times" , e);
        }
      }
      consecutiveFailures.get(rule).incrementAndGet();
      logger.warn("Fetching results for remote rule " + rule + " failed " + "");
      if (consecutiveFailures.get(rule).get() >= serviceConfiguration.getFall()) {
        lastFailure.put(rule, System.currentTimeMillis());
        logger.warn("Remote rule " + rule + " marked as DOWN.");
      }
      return Collections.emptyList();
    });
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    FutureTask<List<RuleMatch>> task = run(Collections.singletonList(sentence));
    task.run();
    try {
      return task.get().toArray(new RuleMatch[0]);
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

}
