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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

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

  private Deque<QueueEntry> textRuleQueue = new ArrayDeque<QueueEntry>();  //  Queue to check text rules in a separate thread
  private Object queueWakeup = new Object();
  private MultiDocumentsHandler multiDocHandler;
  private SwJLanguageTool langTool;
  private QueueIterator queueIterator;
  private int lastStart = -1;
  private int lastCache = 0;
  private String lastDocId = null;
  private boolean interruptCheck = false;
  private boolean queueRuns = false;

  private static final boolean debugMode = false;   //  should be false except for testing
  
  TextLevelCheckQueue(MultiDocumentsHandler multiDocumentsHandler) {
    multiDocHandler = multiDocumentsHandler;
    langTool = multiDocHandler.initLanguageTool();
    multiDocHandler.initCheck(langTool);
    queueIterator = new QueueIterator();
    queueIterator.start();
  }
 
 /**
  * Add a new entry to queue
  * add it only if the new entry is not identical with the last entry or the running
  * @param nStart
  * @param nEnd
  * @param cacheNum
  * @param nCheck
  * @param docId
  * @param overrideRunning
  */
  
  public void addQueueEntry(int nStart, int nEnd, int cacheNum, int nCheck, String docId, boolean overrideRunning) {
    if(nStart < 0 || nEnd <= nStart || cacheNum < 0 || docId == null) {
      return;
    }
    if(!textRuleQueue.isEmpty()) {
      if(!overrideRunning && nStart == lastStart && cacheNum == lastCache && docId.equals(lastDocId)) {
        return;
      }
      QueueEntry queueEntry = textRuleQueue.peekLast();
      if(nStart == queueEntry.nStart && cacheNum == queueEntry.nCache && docId.equals(queueEntry.docId)) {
        if(overrideRunning && !queueEntry.overrideRunning) {
          textRuleQueue.pollLast();
        } else {
          return;
        }
      }
    }
    if(debugMode) {
      MessageHandler.printToLogFile("add queue entry: docId = " + docId + ", nStart = " + nStart + ", nEnd = " + nEnd 
          + ", nCache = " + cacheNum + ", nCheck = " + nCheck + ", overrideRunning = " + overrideRunning);
    }
    interruptCheck = false;
    textRuleQueue.addLast(new QueueEntry(nStart, nEnd, cacheNum, nCheck, docId, overrideRunning));
    wakeupQueue();
  }
  
  /**
   * Create and give back a new queue entry
   * @param nStart
   * @param nEnd
   * @param nCache
   * @param nCheck
   * @param docId
   * @param overrideRunning
   * @return
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
      if(debugMode) {
        MessageHandler.printToLogFile("wake queue");
      }
      queueWakeup.notify();
    }
  }
  
  /**
   * Set a stop flag to get a definite ending of the iteration
   */
  public void setStop() {
    textRuleQueue.clear();
    interruptCheck = true;
    QueueEntry queueEntry = new QueueEntry();
    queueEntry.setStop();
    if(debugMode) {
      MessageHandler.printToLogFile("stop queue");
    }
    textRuleQueue.addLast(queueEntry);
    wakeupQueue();
  }
  
  /**
   * Reset the queue
   * all entries are removed; LanguageTool is new initialized
   */
  public void setReset() {
    textRuleQueue.clear();
    interruptCheck = true;
    if(debugMode) {
      MessageHandler.printToLogFile("reset queue");
    }
    doReset();
    wakeupQueue();
  }
  
  /**
   * remove all entries for the disposed docId (gone document)
   * @param docId
   */
  public void setDispose(String docId) {
    if(debugMode) {
      MessageHandler.printToLogFile("dispose queue");
    }
    if(!textRuleQueue.isEmpty()) {
      for (QueueEntry queueEntry : textRuleQueue) {
        if(docId.equals(queueEntry.docId)) {
          textRuleQueue.remove(queueEntry);
        }
      }
    }
    if(lastStart >= 0 && lastDocId.equals(docId)) {
      interruptCheck = true;
    }
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
   * reset LanguageToo; do an new initialization
   */
  private void doReset() {
    langTool = multiDocHandler.initLanguageTool();
    multiDocHandler.initCheck(langTool);
    textRuleQueue.clear();
  }
  
  /**
   *  get an entry for the next unchecked paragraphs
   */
  QueueEntry getNextQueueEntry(int nPara, int nCache, String docId) {
    List<SingleDocument> documents = multiDocHandler.getDocuments();
    int nDoc = 0;
    for(int n = 0; n < documents.size(); n++) {
      if(docId.equals(documents.get(n).getDocID())) {
        QueueEntry queueEntry = documents.get(n).getNextQueueEntry(nPara, nCache);
        if(queueEntry != null) {
          return queueEntry;
        }
        nDoc = n;
        break;
      }
    }
    for(int i = nDoc + 1; i < documents.size(); i++) {
      QueueEntry queueEntry = documents.get(i).getNextQueueEntry(-1, nCache);
      if(queueEntry != null) {
        return queueEntry;
      }
    }
    for(int i = 0; i < nDoc; i++) {
      QueueEntry queueEntry = documents.get(i).getNextQueueEntry(-1, nCache);
      if(queueEntry != null) {
        return queueEntry;
      }
    }
    return null;
  }
  
  /**
   *  run a queue entry for the specific document
   */
  void runQueueEntry(int nStart, int nEnd, int cacheNum, int nCheck, String docId, boolean doReset, SwJLanguageTool langTool) {
    for (SingleDocument document : multiDocHandler.getDocuments()) {
      if(docId.equals(document.getDocID())) {
        document.runQueueEntry(nStart, nEnd, cacheNum, nCheck, doReset, langTool);
        return;
      }
    }
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
      this.docId = new String(docId);
      this.overrideRunning = overrideRunning;
    }
    
    QueueEntry(int nStart, int nEnd, int nCache, int nCheck, String docId) {
      this(nStart, nEnd, nCache, nCheck, docId, false);
    }
    
    QueueEntry() {
    }

    void setReset() {
      special = TextLevelCheckQueue.RESET_FLAG;
    }
    
    void setStop() {
      special = TextLevelCheckQueue.STOP_FLAG;
    }
    
    void setDispose(String docId) {
      special = TextLevelCheckQueue.DISPOSE_FLAG;
    }

  }

  /**
   * class for automatic iteration of the queue
   */
  class QueueIterator extends Thread {
      
    public QueueIterator() {
    }
    
    @Override
    public void run() {
      queueRuns = true;
      if(debugMode) {
        MessageHandler.printToLogFile("queue started");
      }
      for(;;) {
        if(textRuleQueue.isEmpty()) {
          if(lastDocId != null) {
            QueueEntry queueEntry = getNextQueueEntry(lastStart, lastCache, lastDocId);
            if(queueEntry != null) {
              interruptCheck = false;
              textRuleQueue.addLast(queueEntry);
              continue;
            }
          }
          synchronized(queueWakeup) {
            try {
              if(debugMode) {
                MessageHandler.printToLogFile("queue waits");
              }
              lastStart = -1;
              queueWakeup.wait();;
            } catch (Throwable e) {
              MessageHandler.showError(e);
              return;
            }
          }
        } else {
          QueueEntry queueEntry = textRuleQueue.pollLast();
          if(queueEntry.special == STOP_FLAG) {
            if(debugMode) {
              MessageHandler.printToLogFile("queue ended");
            }
            queueRuns = false;
            return;
          } else {
            if(debugMode) {
              MessageHandler.printToLogFile("run queue entry: docId = " + queueEntry.docId + ", nStart = " 
                  + queueEntry.nStart + ", nEnd = " + queueEntry.nEnd + ", nCheck = " + queueEntry.nCheck + ", overrideRunning = " + queueEntry.overrideRunning);
            }
            lastStart = queueEntry.nStart;
            lastCache = queueEntry.nCache;
            lastDocId = new String(queueEntry.docId);
            runQueueEntry(queueEntry.nStart, queueEntry.nEnd, queueEntry.nCache, queueEntry.nCheck, 
                queueEntry.docId, queueEntry.overrideRunning, langTool);
            interruptCheck = false;
          }
        }
      }
    }
    
  }
    
}
