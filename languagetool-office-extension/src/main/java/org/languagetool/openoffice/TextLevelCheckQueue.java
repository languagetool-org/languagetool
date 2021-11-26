/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Fred Kruse
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
package org.languagetool.openoffice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.languagetool.Language;

import com.sun.star.lang.Locale;

/**
 * Class of a queue to handle parallel check of text level rules
 * @since 4.9
 * @author Fred Kruse
 */
public class TextLevelCheckQueue {
  
  public static final int NO_FLAG = 0;
  public static final int RESET_FLAG = 1;
  public static final int STOP_FLAG = 2;
  public static final int DISPOSE_FLAG = 3;

  private static final int MAX_WAIT = 5000;
  
  private static final int HEAP_CHECK_INTERVAL = 50;

  private List<QueueEntry> textRuleQueue = Collections.synchronizedList(new ArrayList<QueueEntry>());  //  Queue to check text rules in a separate thread
  private Object queueWakeup = new Object();
  private MultiDocumentsHandler multiDocHandler;
  private SortedTextRules sortedTextRules = null;

  private QueueIterator queueIterator;
  private int lastStart = -1;
  private int lastEnd = -1;
  private int lastCache = -1;
  private String lastDocId = null;
  private Language lastLanguage = null;
  private boolean interruptCheck = false;
  private boolean queueRuns = false;
  private boolean queueWaits = false;
  
  private int numSinceHeapTest = 0;

  private static boolean debugMode = false;   //  should be false except for testing
  
  TextLevelCheckQueue(MultiDocumentsHandler multiDocumentsHandler) {
    multiDocHandler = multiDocumentsHandler;
    queueIterator = new QueueIterator();
    queueIterator.start();
    debugMode = OfficeTools.DEBUG_MODE_TQ;
  }
 
 /**
  * Add a new entry to queue
  * add it only if the new entry is not identical with the last entry or the running
  */
  public void addQueueEntry(int nStart, int nEnd, int nCache, int nCheck, String docId, boolean overrideRunning) {
    if (nStart < 0 || nEnd <= nStart || nCache < 0 || docId == null) {
      if (debugMode) {
        MessageHandler.printToLogFile("Return without add to queue: nCache = " + nCache
            + ", nStart = " + nStart + ", nEnd = " + nEnd 
            + ", nCheck = " + nCheck + ", docId = " + docId + ", overrideRunning = " + overrideRunning);
      }
      return;
    }
    QueueEntry queueEntry = new QueueEntry(nStart, nEnd, nCache, nCheck, docId, overrideRunning);
    int setIn = -1;
    if (!textRuleQueue.isEmpty()) {
      if (!overrideRunning && nStart >= lastStart  && nEnd <= lastEnd && nCache == lastCache && docId.equals(lastDocId)) {
        return;
      }
      synchronized(textRuleQueue) {
        for (int i = textRuleQueue.size() - 1; i >= 0; i--) {
          QueueEntry entry = textRuleQueue.get(i);
          if (entry.isObsolete(queueEntry)) {
            if (!overrideRunning && entry.overrideRunning) {
              queueEntry.overrideRunning = true;
            }
            textRuleQueue.remove(i);
            if (setIn > i) {
              setIn = i;
            }
            if (debugMode) {
              MessageHandler.printToLogFile("remove queue entry: docId = " + entry.docId + ", nStart = " + entry.nStart + ", nEnd = " + entry.nEnd 
                  + ", nCache = " + entry.nCache + ", nCheck = " + entry.nCheck + ", overrideRunning = " + entry.overrideRunning);
            }
          } else if (entry.isEqualButSmallerCacheNumber(queueEntry)) {
            setIn = i;
            if (debugMode) {
              MessageHandler.printToLogFile("smaler nCache (old): docId = " + entry.docId + ", nStart = " + entry.nStart + ", nEnd = " + entry.nEnd 
                  + ", nCache = " + entry.nCache + ", nCheck = " + entry.nCheck + ", overrideRunning = " + entry.overrideRunning);
              MessageHandler.printToLogFile("smaler nCache (new): docId = " + docId + ", nStart = " + nStart + ", nEnd = " + nEnd 
                  + ", nCache = " + nCache + ", nCheck = " + nCheck + ", overrideRunning = " + overrideRunning);
            }
          }
        }
      }
    }
    if (debugMode) {
      MessageHandler.printToLogFile("add queue entry: docId = " + docId + ", nStart = " + nStart + ", nEnd = " + nEnd 
          + ", nCache = " + nCache + ", nCheck = " + nCheck + ", overrideRunning = " + overrideRunning);
    }
    if (setIn >= 0) {
      textRuleQueue.add(setIn, queueEntry);
    } else {
      textRuleQueue.add(queueEntry);
    }
    interruptCheck = false;
    wakeupQueue();
  }
  
  /**
   * Create and give back a new queue entry
   */
  public QueueEntry createQueueEntry(int nStart, int nEnd, int nCache, int nCheck, String docId, boolean overrideRunning) {
    return (new QueueEntry(nStart, nEnd, nCache, nCheck, docId, overrideRunning));
  }
  
  QueueEntry createQueueEntry(int nStart, int nEnd, int cacheNum, int nCheck, String docId) {
    return createQueueEntry(nStart, nEnd, cacheNum, nCheck, docId, false);
  }
  
  /**
   * wake up the waiting iteration of the queue
   */
  private void wakeupQueue() {
    synchronized(queueWakeup) {
      if (debugMode) {
        MessageHandler.printToLogFile("wake queue");
      }
      queueWakeup.notify();
    }
  }

  /**
   * Set a stop flag to get a definite ending of the iteration
   */
  public void setStop() {
    if (queueRuns) {
      synchronized(textRuleQueue) {
        textRuleQueue.clear();
      }
      interruptCheck = true;
      QueueEntry queueEntry = new QueueEntry();
      queueEntry.setStop();
      if (debugMode) {
        MessageHandler.printToLogFile("stop queue");
      }
      textRuleQueue.add(queueEntry);
    }
    wakeupQueue();
  }
  
  /**
   * Reset the queue
   * all entries are removed; LanguageTool is new initialized
   */
  public void setReset() {
    synchronized(textRuleQueue) {
      textRuleQueue.clear();
    }
    if (!queueWaits && lastStart >= 0) {
      waitForInterrupt();
    }
    if (debugMode) {
      MessageHandler.printToLogFile("reset queue");
    }
    doReset();
    wakeupQueue();
  }
  
  /**
   * remove all entries for the disposed docId (gone document)
   * @param docId
   */
  public void interruptCheck(String docId) {
    if (debugMode) {
      MessageHandler.printToLogFile("interrupt queue");
    }
    if (!textRuleQueue.isEmpty()) {
      synchronized(textRuleQueue) {
        for (int i = textRuleQueue.size() - 1; i >= 0; i--) {
          QueueEntry queueEntry = textRuleQueue.get(i);
          if (docId.equals(queueEntry.docId)) {
            textRuleQueue.remove(queueEntry);
          }
        }
      }
    }
    if (!queueWaits && lastStart >= 0 && lastDocId != null && lastDocId.equals(docId)) {
      waitForInterrupt();
      lastDocId = null;
    }
  }
  
  /**
   * Set interrupt and wait till finish last check
   */
  private void waitForInterrupt() {
    interruptCheck = true;
    MessageHandler.printToLogFile("Interrupt initiated");
    wakeupQueue();
    int n = 0;
    while (interruptCheck && n < MAX_WAIT) {
      try {
        Thread.sleep(1);
        n++;
      } catch (InterruptedException e) {
        MessageHandler.showError(e);
      }
    }
  }

  /**
   *  get the document by ID
   */
  SingleDocument getSingleDocument(String docId) {
    for (SingleDocument document : multiDocHandler.getDocuments()) {
      if (docId.equals(document.getDocID())) {
        return document;
      }
    }
    return null;
  }
  
  /**
   *  get language of document by ID
   */
  Language getLanguage(String docId, int nStart) {
    SingleDocument document = getSingleDocument(docId);
    if (document != null) {
      DocumentCache docCache = document.getDocumentCache();
      if (docCache != null && nStart < docCache.textSize()) {
        Locale locale = docCache.getTextParagraphLocale(nStart);
        if (multiDocHandler.hasLocale(locale)) {
          return multiDocHandler.getLanguage(locale);
        }
      }
    }
    return null;
  }
  
  /**
   * gives back information if queue is interrupted
   */
  public boolean isInterrupted() {
    return interruptCheck;
  }
  
  /**
   * gives back information if queue is running
   */
  public boolean isRunning() {
    return queueRuns;
  }
  
  /**
   * gives back information if queue is waiting
   */
  public boolean isWaiting() {
    return queueWaits;
  }
  
  /**
   * reset LanguageToo; do an new initialization
   */
  private void doReset() {
    textRuleQueue.clear();
    queueIterator.initLangtool(null);
  }
  
  /**
   *  get an entry for the next unchecked paragraphs
   */
  QueueEntry getNextQueueEntry(int nPara, String docId) {
    List<SingleDocument> documents = multiDocHandler.getDocuments();
    int nDoc = 0;
    for (int n = 0; n < documents.size(); n++) {
      if ((docId == null || docId.equals(documents.get(n).getDocID())) && !documents.get(n).isDisposed() && !documents.get(n).isImpress()) {
        QueueEntry queueEntry = documents.get(n).getNextQueueEntry(nPara);
        if (queueEntry != null) {
          return queueEntry;
        }
        nDoc = n;
        break;
      }
    }
    for (int n = 0; n < documents.size(); n++) {
      if (docId != null && docId.equals(documents.get(n).getDocID()) && !documents.get(n).isDisposed() && !documents.get(n).isImpress()) {
        QueueEntry queueEntry = documents.get(n).getQueueEntryForChangedParagraph();
        if (queueEntry != null) {
          return queueEntry;
        }
        nDoc = n;
        break;
      }
    }
    for (int i = nDoc + 1; i < documents.size(); i++) {
      if (!documents.get(i).isDisposed() && !documents.get(i).isImpress()) {
        QueueEntry queueEntry = documents.get(i).getNextQueueEntry(-1);
        if (queueEntry != null) {
          return queueEntry;
        }
      }
    }
    for (int i = 0; i < nDoc; i++) {
      if (!documents.get(i).isDisposed() && !documents.get(i).isImpress()) {
        QueueEntry queueEntry = documents.get(i).getNextQueueEntry(-1);
        if (queueEntry != null) {
          return queueEntry;
        }
      }
    }
    return null;
  }
  
  /**
   * run heap space test, in intervals
   */
  private boolean testHeapSpace() {
    if (numSinceHeapTest > HEAP_CHECK_INTERVAL) {
      numSinceHeapTest = 0;
      if (!multiDocHandler.isEnoughHeapSpace()) {
        return false;
      }
    } else {
      numSinceHeapTest++;
    }
    return true;
  }

  
  /**
   * Internal class to store queue entries
   */
  class QueueEntry {
    int nStart;
    int nEnd;
    int nCache;
    int nCheck;
    String docId;
    boolean overrideRunning;
    int special = TextLevelCheckQueue.NO_FLAG;
    
    QueueEntry(int nStart, int nEnd, int nCache, int nCheck, String docId, boolean overrideRunning) {
      this.nStart = nStart;
      this.nEnd = nEnd;
      this.nCache = nCache;
      this.nCheck = nCheck;
      this.docId = docId;
      this.overrideRunning = overrideRunning;
    }
    
    QueueEntry(int nStart, int nEnd, int nCache, int nCheck, String docId) {
      this(nStart, nEnd, nCache, nCheck, docId, false);
    }
    
    QueueEntry() {
    }

    /**
     * Set reset flag
     */
    void setReset() {
      special = TextLevelCheckQueue.RESET_FLAG;
    }
    
    /**
     * Set stop flag
     */
    void setStop() {
      special = TextLevelCheckQueue.STOP_FLAG;
    }
    
    /**
     * Set dispose flag
     */
    void setDispose(String docId) {
      special = TextLevelCheckQueue.DISPOSE_FLAG;
    }
    
    /**
     * Define equal queue entries
     */
    @Override
    public boolean equals(Object o) {
      if (o == null || !(o instanceof QueueEntry)) {
        return false;
      }
      QueueEntry e = (QueueEntry) o;
      if (nStart == e.nStart && nEnd == e.nEnd && nCache == e.nCache && nCheck == e.nCheck && docId.equals(e.docId)) {
        return true;
      }
      return false;
    }

    /**
     * entry is equal but number of cache is smaller then new entry e
     */
    public boolean isEqualButSmallerCacheNumber(QueueEntry e) {
      if (e == null) {
        return false;
      }
      if (nStart >= e.nStart && nEnd <= e.nEnd && nCache < e.nCache && docId.equals(e.docId)) {
        return true;
      }
      return false;
    }

    /**
     * entry is obsolete and should be replaced by new entry e
     */
    public boolean isObsolete(QueueEntry e) {
      if (e == null || nCheck != e.nCheck || nCache != e.nCache || !docId.equals(e.docId)) {
        return false;
      }
      if (nCheck < 1 || (nCheck == -1 && e.nStart >= nStart && e.nStart <= nEnd) 
          || (nCheck >= 0 && nStart == e.nStart && nEnd == e.nEnd)) {
        return true;
      }
      return false;
    }

    /**
     *  run a queue entry for the specific document
     */
    void runQueueEntry(MultiDocumentsHandler multiDocHandler, SwJLanguageTool lt) {
      if (testHeapSpace()) {
        SingleDocument document = getSingleDocument(docId);
        if (document != null) {
          document.runQueueEntry(nStart, nEnd, nCache, nCheck, overrideRunning, lt);
        }
      }
    }
    
  }

  /**
   * class for automatic iteration of the queue
   */
  class QueueIterator extends Thread {
    
    private SwJLanguageTool lt;

      
    public QueueIterator() {
    }
    
    /**
     * initialize languagetool for text level iteration
     */
    public void initLangtool(Language language) {
      if (debugMode) {
        MessageHandler.printToLogFile("queue: InitLangtool: language = " + (language == null ? "null" : language.getShortCodeWithCountryAndVariant()));
      }
      lt = multiDocHandler.initLanguageTool(language, false);
      multiDocHandler.initCheck(lt, multiDocHandler.getLocale());
      sortedTextRules = new SortedTextRules(lt, multiDocHandler.getConfiguration(), multiDocHandler.getDisabledRules());
    }
    
    /**
     * Run queue for check with text
     */
    @Override
    public void run() {
      try {
        queueRuns = true;
        if (debugMode) {
          MessageHandler.printToLogFile("queue started");
        }
        for (;;) {
          queueWaits = false;
          if (interruptCheck) {
            MessageHandler.printToLogFile("Interrupt ended");
          }
          interruptCheck = false;
          if (textRuleQueue.isEmpty()) {
            synchronized(textRuleQueue) {
              if (lastDocId != null) {
                QueueEntry queueEntry = getNextQueueEntry(lastStart, lastDocId);
                if (queueEntry != null) {
                  textRuleQueue.add(queueEntry);
                  queueEntry = null;
                  continue;
                }
              }
            }
            synchronized(queueWakeup) {
              try {
                if (debugMode) {
                  MessageHandler.printToLogFile("queue waits");
                }
                lastStart = -1;
                lastEnd = -1;
                queueWaits = true;
                queueWakeup.wait();
              } catch (Throwable e) {
                MessageHandler.showError(e);
                return;
              }
            }
          } else {
            QueueEntry queueEntry;
            synchronized(textRuleQueue) {
              if (!textRuleQueue.isEmpty()) {
                queueEntry = textRuleQueue.get(textRuleQueue.size() - 1);
                textRuleQueue.remove(textRuleQueue.size() - 1);
              } else {
                continue;
              }
            }
            if (queueEntry.special == STOP_FLAG) {
              if (debugMode) {
                MessageHandler.printToLogFile("queue ended");
              }
              queueRuns = false;
              return;
            } else {
              if (debugMode) {
                MessageHandler.printToLogFile("run queue entry: docId = " + queueEntry.docId + ", nStart = " 
                    + queueEntry.nStart + ", nEnd = " + queueEntry.nEnd + ", nCheck = " + queueEntry.nCheck + ", overrideRunning = " + queueEntry.overrideRunning);
              }
              Language entryLanguage = getLanguage(queueEntry.docId, queueEntry.nStart);
              if (entryLanguage != null) {
                if (lastLanguage == null || !lastLanguage.equals(entryLanguage)) {
                  lastLanguage = entryLanguage;
                  initLangtool(lastLanguage);
                  sortedTextRules.activateTextRulesByIndex(queueEntry.nCache, lt);
                } else if (lastCache != queueEntry.nCache) {
                  sortedTextRules.activateTextRulesByIndex(queueEntry.nCache, lt);
                }
              }
              lastDocId = queueEntry.docId;
	            lastStart = queueEntry.nStart;
	            lastEnd = queueEntry.nEnd;
	            lastCache = queueEntry.nCache;
	            queueEntry.runQueueEntry(multiDocHandler, entryLanguage == null ? null : lt);
              queueEntry = null;
            }
          }
        }
      } catch (Throwable e) {
        MessageHandler.showError(e);
      }
    }
    
  }
    
}
