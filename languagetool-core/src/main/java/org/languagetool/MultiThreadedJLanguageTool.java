/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleSet;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A variant of {@link JLanguageTool} that uses several threads for rule matching.
 * Use this if you want text checking to be fast and do not care about the 
 * high load that this might cause. Call {@link #shutdown()} when you don't need
 * the object anymore.
 * 
 * <p>Also see the javadoc of {@link JLanguageTool}.</p>
 * 
 * <p><b>Thread-safety:</b> this class is <b>not</b> thread-safe, see the remarks at {@link JLanguageTool}.
 */
public class MultiThreadedJLanguageTool extends JLanguageTool {
  
  private final int threadPoolSize;
  private final ExecutorService threadPool;

  public MultiThreadedJLanguageTool(Language language) {
    this(language, null);
  }

  /**
   * @see #shutdown()
   * @param threadPoolSize the number of concurrent threads (use 0 or negative value for a default)
   * @since 2.9
   */
  public MultiThreadedJLanguageTool(Language language, int threadPoolSize) {
    this(language, null, threadPoolSize, null);
  }

  /**
   * @see #shutdown()
   */
  public MultiThreadedJLanguageTool(Language language, Language motherTongue) {
    this(language, motherTongue, getDefaultThreadCount(), null);
  }

  /**
   * @since 4.2
   */
  public MultiThreadedJLanguageTool(Language language, Language motherTongue, UserConfig userConfig) {
    this(language, motherTongue, getDefaultThreadCount(), userConfig);
  }

  /**
   * @see #shutdown()
   * @param threadPoolSize the number of concurrent threads
   * @since 4.2
   */
  public MultiThreadedJLanguageTool(Language language, Language motherTongue, int threadPoolSize,
      UserConfig userConfig) {
    this(language, motherTongue, -1,null, userConfig);
  }

  /**
   * @see #shutdown()
   * @param threadPoolSize the number of concurrent threads
   * @since 4.2
   */
  public MultiThreadedJLanguageTool(Language language, Language motherTongue, int threadPoolSize,
                                    GlobalConfig globalConfig, UserConfig userConfig) {
    super(language, Collections.emptyList(), motherTongue, null, globalConfig, userConfig);
    this.threadPoolSize = threadPoolSize <= 0 ? getDefaultThreadCount() : threadPoolSize;
    threadPool = new ForkJoinPool(this.threadPoolSize, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, false);
  }

  /**
   * Call this to shut down the internally used thread pool.
   * @since 3.0
   */
  public void shutdown() {
    threadPool.shutdownNow();
  }

  /**
   * Call this to shut down the internally used thread pool after all running tasks are finished.
   * @since 3.1
   */
  public void shutdownWhenDone() {
    threadPool.shutdown();
  }

  private static int getDefaultThreadCount() {
    return Runtime.getRuntime().availableProcessors();
  }

  /**
   * When no thread pool size is configured, the number of available processors is returned.
   */
  protected int getThreadPoolSize() {
    return threadPoolSize;
  }
  
  /**
   * @return a fixed size executor with the given number of threads
   */
  protected ExecutorService getExecutorService() {
    return threadPool;
  }
  
  @Override
  protected List<AnalyzedSentence> analyzeSentences(List<String> sentences) throws IOException {
    if (sentences.size() < 2) {
      return super.analyzeSentences(sentences);
    }

    List<AnalyzedSentence> analyzedSentences = new ArrayList<>();
    
    ExecutorService executorService = getExecutorService();

    int j = 0;
    
    List<Callable<AnalyzedSentence>> callables = new ArrayList<>();
    for (String sentence : sentences) {
      AnalyzeSentenceCallable analyzeSentenceCallable = 
          ++j < sentences.size() 
            ? new AnalyzeSentenceCallable(sentence)
            : new ParagraphEndAnalyzeSentenceCallable(sentence);
      callables.add(analyzeSentenceCallable);
    }
    
    try {
      List<Future<AnalyzedSentence>> futures = executorService.invokeAll(callables);
      for (Future<AnalyzedSentence> future : futures) {
        AnalyzedSentence analyzedSentence = future.get();
        rememberUnknownWords(analyzedSentence);
        printSentenceInfo(analyzedSentence);
        analyzedSentences.add(analyzedSentence);
      }
      
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    
    return analyzedSentences;
  }
  
  
  @Override
  protected CheckResults performCheck(List<AnalyzedSentence> analyzedSentences, List<String> sentenceTexts,
                                         RuleSet ruleSet, ParagraphHandling paraMode,
                                         AnnotatedText annotatedText, RuleMatchListener listener, Mode mode, Level level, boolean checkRemoteRules) {
    List<Rule> allRules = ruleSet.allRules();
    List<SentenceData> sentences = computeSentenceData(analyzedSentences, sentenceTexts);

    Map<Rule, BitSet> map = new HashMap<>();
    for (int i = 0; i < sentences.size(); i++) {
      for (Rule rule : ruleSet.rulesForSentence(sentences.get(i).analyzed)) {
        map.computeIfAbsent(rule, __ -> new BitSet()).set(i);
      }
    }

    AtomicInteger ruleIndex = new AtomicInteger();
    Map<Integer, List<RuleMatch>> ruleMatches = new TreeMap<>();
    List<Range> ignoreRanges = new ArrayList<>();
    List<Future<?>> futures = IntStream.range(0, getThreadPoolSize()).mapToObj(__ -> getExecutorService().submit(() -> {
      while (true) {
        int index = ruleIndex.getAndIncrement();
        if (index >= allRules.size()) return null;

        Rule rule = allRules.get(index);
        BitSet applicable = map.get(rule);
        if (applicable == null) continue;

        // less need for special treatment of remote rules when execution is already parallel
        CheckResults res = new TextCheckCallable(RuleSet.plain(Collections.singletonList(rule)),
          RuleSet.filterList(applicable, sentences),
          paraMode, annotatedText, listener, mode, level, true).call();
        if (!res.getRuleMatches().isEmpty()) {
          synchronized (ruleMatches) {
            ruleMatches.put(index, res.getRuleMatches());
          }
          synchronized (ignoreRanges) {
            ignoreRanges.addAll(res.getIgnoredRanges());
          }
        }
      }
    })).collect(Collectors.toList());

    try {
      for (Future<?> future : futures) {
        future.get();
      }
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    List<RuleMatch> rm = applyCustomFilters(Lists.newArrayList(Iterables.concat(ruleMatches.values())), annotatedText);
    return new CheckResults(rm, ignoreRanges);
  }

  private class AnalyzeSentenceCallable implements Callable<AnalyzedSentence> {
    private final String sentence;

    private AnalyzeSentenceCallable(String sentence) {
      this.sentence = sentence;
    }

    @Override
    public AnalyzedSentence call() throws Exception {
      return getAnalyzedSentence(sentence);
    }
  }
  
  private final class ParagraphEndAnalyzeSentenceCallable extends AnalyzeSentenceCallable {
    private ParagraphEndAnalyzeSentenceCallable(String sentence) {
      super(sentence);
    }

    @Override
    public AnalyzedSentence call() throws Exception {
      return markAsParagraphEnd(super.call());
    }
  }
}
