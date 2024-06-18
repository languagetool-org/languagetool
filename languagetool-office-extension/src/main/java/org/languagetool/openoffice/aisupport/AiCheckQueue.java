/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.openoffice.aisupport;

import java.util.List;

import org.languagetool.Language;
import org.languagetool.openoffice.MessageHandler;
import org.languagetool.openoffice.MultiDocumentsHandler;
import org.languagetool.openoffice.OfficeTools;
import org.languagetool.openoffice.SingleDocument;
import org.languagetool.openoffice.SwJLanguageTool;
import org.languagetool.openoffice.TextLevelCheckQueue;
import org.languagetool.openoffice.DocumentCache;
import org.languagetool.openoffice.DocumentCache.TextParagraph;
import org.languagetool.openoffice.OfficeTools.DocumentType;

/**
 * Class of a queue to handle check of AI error detection
 * @since 6.5
 * @author Fred Kruse
 */
public class AiCheckQueue extends TextLevelCheckQueue {

  private static boolean debugMode = false;   //  should be false except for testing

  public AiCheckQueue(MultiDocumentsHandler multiDocumentsHandler) {
    super(multiDocumentsHandler);
    MessageHandler.printToLogFile("AI Queue started");
  }
  
  /**
   * Add a new entry to queue
   * add it only if the new entry is not identical with the last entry or the running
   */
   public void addQueueEntry(TextParagraph nTPara, String docId, boolean next) {
     if (nTPara == null || nTPara.number < 0 || docId == null || interruptCheck) {
       if (debugMode) {
         MessageHandler.printToLogFile("AiCheckQueue: addQueueEntry: Return without add to queue: nCache = " + OfficeTools.CACHE_AI
             + ", nTPara = " + (nTPara == null ? "null" : ("(" + nTPara.number + "/" + nTPara.type + ")")) + ", docId = " + docId);
       }
       return;
     }
     QueueEntry queueEntry = new QueueEntry(nTPara, nTPara, OfficeTools.CACHE_AI, 0, docId, false);
     if (!textRuleQueue.isEmpty()) {
       synchronized(textRuleQueue) {
         for (int i = 0; i < textRuleQueue.size(); i++) {
           QueueEntry entry = textRuleQueue.get(i);
           if (entry.equals(queueEntry)) {
             if (debugMode) {
               MessageHandler.printToLogFile("AiCheckQueue: addQueueEntry: Entry removed: nCache = " + OfficeTools.CACHE_AI
                   + ", nTPara = (" + nTPara.number + "/" + nTPara.type + "), docId = " + docId);
             }
             textRuleQueue.remove(i);
             break;
           }
         }
       }
     }
     synchronized(textRuleQueue) {
       if (next) {
         textRuleQueue.add(0, queueEntry);
       } else {
         textRuleQueue.add(queueEntry);
       }
       if (debugMode) {
         if (debugMode) {
           MessageHandler.printToLogFile("AiCheckQueue: addQueueEntry: Entry removed: nCache = " + OfficeTools.CACHE_AI
               + ", nTPara = (" + nTPara.number + "/" + nTPara.type + "), docId = " + docId);
         }
       }
     }
     interruptCheck = false;
     wakeupQueue();
   }
  
   /**
    *  get an entry for the next unchecked paragraphs
    */
  @Override
  protected QueueEntry getNextQueueEntry(TextParagraph nPara, String docId) {
    List<SingleDocument> documents = multiDocHandler.getDocuments();
    int nDoc = 0;
    for (int n = 0; n < documents.size(); n++) {
      if ((docId == null || docId.equals(documents.get(n).getDocID())) && !documents.get(n).isDisposed() && documents.get(n).getDocumentType() == DocumentType.WRITER) {
        QueueEntry queueEntry = documents.get(n).getNextAiQueueEntry(nPara);
        if (queueEntry != null) {
          return queueEntry;
        }
        nDoc = n;
        break;
      }
    }
    for (int i = nDoc + 1; i < documents.size(); i++) {
      if (!documents.get(i).isDisposed() && documents.get(i).getDocumentType() == DocumentType.WRITER) {
        QueueEntry queueEntry = documents.get(i).getNextAiQueueEntry(null);
        if (queueEntry != null) {
          return queueEntry;
        }
      }
    }
    for (int i = 0; i < nDoc; i++) {
      if (!documents.get(i).isDisposed() && documents.get(i).getDocumentType() == DocumentType.WRITER) {
        QueueEntry queueEntry = documents.get(i).getNextAiQueueEntry(null);
        if (queueEntry != null) {
          return queueEntry;
        }
      }
    }
    return null;
  }
   
   /**
    *  get language of document by ID
    *  Set to null for AI
    */
  @Override
  protected Language getLanguage(String docId, TextParagraph nStart) {
    return null;
  }

  /**
   * initialize languagetool (set to default for AI)
   */
  @Override
  public void initLangtool(Language language) throws Throwable {
    lt = multiDocHandler.getLanguageTool();
  }
   
  /**
   *  run a queue entry for the specific document
   */
  @Override
  protected void runQueueEntry(QueueEntry qEntry, MultiDocumentsHandler multiDocHandler, SwJLanguageTool lt) throws Throwable {
    SingleDocument document = getSingleDocument(qEntry.docId);
    TextParagraph nTPara = qEntry.nStart;
    if (document != null && !document.isDisposed() && nTPara != null) {
      DocumentCache docCache = document.getDocumentCache();
      if (docCache != null) {
        int nFPara = docCache.getFlatParagraphNumber(nTPara);
        AiErrorDetection aiError = new AiErrorDetection(document, multiDocHandler.getConfiguration());
        aiError.addAiRuleMatchesForParagraph(nFPara);
      }
    }
  }

}
