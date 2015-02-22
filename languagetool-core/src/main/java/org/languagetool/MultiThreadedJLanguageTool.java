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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

/**
 * A variant of {@link JLanguageTool} that uses several threads for rule matching.
 * Use this if you want text checking to be fast and do not care about the 
 * high load that this might cause.
 * 
 * <p><b>Thread-safety:</b> See the remarks at {@link JLanguageTool}.
 */
public class MultiThreadedJLanguageTool extends JLanguageTool {
  
  private final int threadPoolSize;
  private final ExecutorService threadPool;

  public MultiThreadedJLanguageTool(Language language) {
    this(language, null);
  }

  public MultiThreadedJLanguageTool(Language language, Language motherTongue) {
    super(language, motherTongue);
    
    threadPoolSize = getDefaultThreadCount();
    threadPool = Executors.newFixedThreadPool(getThreadPoolSize(), new DaemonThreadFactory());
  }

  private static int getDefaultThreadCount() {
    String threadCountStr = System.getProperty("org.languagetool.thread_count_internal", "-1");
    int threadPoolSize = Integer.parseInt(threadCountStr);
    if (threadPoolSize == -1) {
      threadPoolSize = Runtime.getRuntime().availableProcessors();
    }
    return threadPoolSize;
  }

  /**
   * When no thread pool size is {@link #setThreadPoolSize(int) configured}, the number of available processors is returned. 
   * 
   * @see #setThreadPoolSize(int)
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
    final List<AnalyzedSentence> analyzedSentences = new ArrayList<>();
    
    final ExecutorService executorService = getExecutorService();

    int j = 0;
    
    List<Callable<AnalyzedSentence>> callables = new ArrayList<>();
    for (final String sentence : sentences) {
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
  protected List<RuleMatch> performCheck(final List<AnalyzedSentence> analyzedSentences, final List<String> sentences,
       final List<Rule> allRules, final ParagraphHandling paraMode, 
       final AnnotatedText annotatedText) throws IOException {
    int charCount = 0;
    int lineCount = 0;
    int columnCount = 1;

    final List<RuleMatch> ruleMatches = new ArrayList<>();
    
    final ExecutorService executorService = getExecutorService();
    try {
      final List<Callable<List<RuleMatch>>> callables =
              createTextCheckCallables(paraMode, annotatedText, analyzedSentences, sentences, allRules, charCount, lineCount, columnCount);
      final List<Future<List<RuleMatch>>> futures = executorService.invokeAll(callables);
      for (Future<List<RuleMatch>> future : futures) {
        ruleMatches.addAll(future.get());
      }
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    
    return ruleMatches;
  }

  private List<Callable<List<RuleMatch>>> createTextCheckCallables(ParagraphHandling paraMode,
       AnnotatedText annotatedText, List<AnalyzedSentence> analyzedSentences, List<String> sentences, 
       List<Rule> allRules, int charCount, int lineCount, int columnCount) {
    final int threads = getThreadPoolSize();
    final int totalRules = allRules.size();
    final int chunkSize = totalRules / threads;
    int firstItem = 0;
    final List<Callable<List<RuleMatch>>> callables = new ArrayList<>();
    
    // split the rules - all rules are independent, so it makes more sense to split
    // the rules than to split the text:
    for (int i = 0; i < threads; i++) {
      final List<Rule> subRules;
      //TODO: make sure we don't split rules with same id so RuleGroupFilter still works
      if (i == threads - 1) {
        // make sure the last rules are not lost due to rounding issues:
        subRules = allRules.subList(firstItem, totalRules);
      } else {
        subRules = allRules.subList(firstItem, firstItem + chunkSize);
      }
      callables.add(new TextCheckCallable(subRules, sentences, analyzedSentences, paraMode, annotatedText, charCount, lineCount, columnCount));
      firstItem = firstItem + chunkSize;
    }
    return callables;
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
      AnalyzedSentence analyzedSentence = super.call();
      AnalyzedTokenReadings[] anTokens = analyzedSentence.getTokens();
      anTokens[anTokens.length - 1].setParagraphEnd();
      analyzedSentence = new AnalyzedSentence(anTokens);  ///TODO: why???
      return analyzedSentence;
    }
  }

  private static final class DaemonThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(Runnable r) {
      Thread thread = new Thread(r);
      thread.setDaemon(true); // so we don't have to shut down executor explicitly
      return thread;
    }
  }
}
