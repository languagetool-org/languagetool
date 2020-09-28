/* LanguageTool, a natural language style checker 
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;


/**
 * Class to read and write LT-Office-Extension-Cache
 * @since 5.2
 * @author Fred Kruse
 */
public class CacheIO implements Serializable {

  private static final long serialVersionUID = 1L;
  private static final String CACHEFILE_EXTENSION = "lcz";
  private static final int MIN_PARAGRAPHS_TO_SAVE_CACHE = 20;
  private static final boolean DEBUG_MODE = OfficeTools.DEBUG_MODE_IO;
  
  private String cachePath;
  private AllCaches allCaches;
  
  CacheIO(XComponent xComponent) {
    cachePath = getCachePath(xComponent);
  }
  
  /** 
   * Returns the text cursor (if any)
   * Returns null if it fails
   */
  private static String getDocumentPath(XComponent xComponent) {
    try {
      XTextDocument curDoc = UnoRuntime.queryInterface(XTextDocument.class, xComponent);
      if (curDoc == null) {
        MessageHandler.printToLogFile("XTextDocument not found!");
        return null;
      }
      XController xController = curDoc.getCurrentController();
      if (xController == null) {
        MessageHandler.printToLogFile("XController not found!");
        return null;
      }
      XModel xModel = xController.getModel();
      if (xModel == null) {
        MessageHandler.printToLogFile("XModel not found!");
        return null;
      }
      String url = xModel.getURL();
      if (url == null || !url.startsWith("file://")) {
        MessageHandler.printToLogFile("Not a file URL: " + (url == null ? "null" : url));
        return null;
      }
      URI uri = new URI(url);
      return uri.getPath();
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }

  private String getCachePath(XComponent xComponent) {
    String path = getDocumentPath(xComponent);
    if (path == null) {
      return null;
    }
    int nDot = path.lastIndexOf(".");
    path = path.substring(0, nDot + 1);
    path = path + CACHEFILE_EXTENSION;
    return path;
  }
  
  private void saveAllCaches() {
    try {
      GZIPOutputStream fileOut = new GZIPOutputStream(new FileOutputStream(cachePath));
      ObjectOutputStream out = new ObjectOutputStream(fileOut);
      out.writeObject(allCaches);
      out.close();
      fileOut.close();
      MessageHandler.printToLogFile("Caches saved to: " + cachePath);
      if (DEBUG_MODE) {
        MessageHandler.printToLogFile("Document Cache: Number of paragraphs: " + allCaches.docCache.size());
        MessageHandler.printToLogFile("Sentences Cache: Number of paragraphs: " + allCaches.sentencesCache.getNumberOfParas() 
            + ", Number of matches: " + allCaches.sentencesCache.getNumberOfMatches());
        MessageHandler.printToLogFile("Paragraph Cache(0): Number of paragraphs: " + allCaches.paragraphsCache.get(0).getNumberOfParas() 
            + ", Number of matches: " + allCaches.paragraphsCache.get(0).getNumberOfMatches());
        MessageHandler.printToLogFile("Paragraph Cache(1): Number of paragraphs: " + allCaches.paragraphsCache.get(1).getNumberOfParas() 
            + ", Number of matches: " + allCaches.paragraphsCache.get(1).getNumberOfMatches());
        printCacheInfo();
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
    }
  }
  
  public void saveCaches(DocumentCache docCache, ResultCache sentencesCache, List<ResultCache> paragraphsCache) {
    if (cachePath != null) {
      try {
        if (docCache.size() >= MIN_PARAGRAPHS_TO_SAVE_CACHE) {
          allCaches = new AllCaches(docCache, sentencesCache, paragraphsCache);
          saveAllCaches();
        } else {
          File file = new File( cachePath );
          if (file.exists() && !file.isDirectory()) {
            file.delete();
          }
        }
      } catch (Throwable t) {
        MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      }
    }
  }
  
  public boolean readAllCaches() {
    if (cachePath == null) {
      return false;
    }
    try {
      File file = new File( cachePath );
      if (file.exists() && !file.isDirectory()) {
        GZIPInputStream fileIn = new GZIPInputStream(new FileInputStream(file));
        ObjectInputStream in = new ObjectInputStream(fileIn);
        allCaches = (AllCaches) in.readObject();
        in.close();
        fileIn.close();
        MessageHandler.printToLogFile("Caches read from: " + cachePath);
        if (DEBUG_MODE) {
          MessageHandler.printToLogFile("Document Cache: Number of paragraphs: " + allCaches.docCache.size());
          MessageHandler.printToLogFile("Sentences Cache: Number of paragraphs: " + allCaches.sentencesCache.getNumberOfParas() 
              + ", Number of matches: " + allCaches.sentencesCache.getNumberOfMatches());
          MessageHandler.printToLogFile("Paragraph Cache(0): Number of paragraphs: " + allCaches.paragraphsCache.get(0).getNumberOfParas() 
              + ", Number of matches: " + allCaches.paragraphsCache.get(0).getNumberOfMatches());
          MessageHandler.printToLogFile("Paragraph Cache(1): Number of paragraphs: " + allCaches.paragraphsCache.get(1).getNumberOfParas() 
              + ", Number of matches: " + allCaches.paragraphsCache.get(1).getNumberOfMatches());
          printCacheInfo();
        }
        return true;
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
    }
    return false;
  }
  
  public DocumentCache getDocumentCache() {
    return allCaches.docCache;
  }
  
  public ResultCache getSentencesCache() {
    return allCaches.sentencesCache;
  }
  
  public List<ResultCache> getParagraphsCache() {
    return allCaches.paragraphsCache;
  }
  
  public void resetAllCache() {
    allCaches = null;
  }
  
  private void printCacheInfo() {
    if (allCaches.sentencesCache == null) {
      MessageHandler.printToLogFile("sentencesCache == null");
    } else {
      if (allCaches.sentencesCache.getNumberOfMatches() > 0) {
        for (int n = 0; n < allCaches.sentencesCache.getNumberOfParas(); n++) {
          if (allCaches.sentencesCache.getMatches(n) == null) {
            MessageHandler.printToLogFile("allCaches.sentencesCache.getMatches(" + n + ") == null");
          } else {
            if (allCaches.sentencesCache.getMatches(n).length > 0) {
              MessageHandler.printToLogFile("Paragraph " + n + " sentence match[0]: " 
                  + "nStart = " + allCaches.sentencesCache.getMatches(n)[0].nErrorStart 
                  + ", nLength = " + allCaches.sentencesCache.getMatches(n)[0].nErrorLength
                  + ", errorID = " 
                  + (allCaches.sentencesCache.getMatches(n)[0].aRuleIdentifier == null ? "null" : allCaches.sentencesCache.getMatches(n)[0].aRuleIdentifier));
            }
          }
        }
      }
    }
  }
  
  class AllCaches implements Serializable {

    private static final long serialVersionUID = 1L;

    DocumentCache docCache;                 //  cache of paragraphs
    ResultCache sentencesCache;             //  Cache for matches of sentences rules
    List<ResultCache> paragraphsCache;      //  Cache for matches of text rules
    
    AllCaches(DocumentCache docCache, ResultCache sentencesCache, List<ResultCache> paragraphsCache) {
      this.docCache = docCache;
      this.sentencesCache = sentencesCache;
      this.paragraphsCache = paragraphsCache;
    }
    
  }

  
  
}
