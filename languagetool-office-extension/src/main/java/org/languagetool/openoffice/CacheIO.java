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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.languagetool.JLanguageTool;
import org.languagetool.gui.Configuration;
import org.languagetool.openoffice.SingleDocument.IgnoredMatches;

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
  private static final boolean DEBUG_MODE = OfficeTools.DEBUG_MODE_IO;

  private static final long MAX_CACHE_TIME = 365 * 24 * 3600000;      //  Save cache files maximal one year
  private static final String CACHEFILE_MAP = "LtCacheMap";           //  Name of cache map file
  private static final String CACHEFILE_PREFIX = "LtCache";           //  Prefix for cache files (simply a number is added for file name)
  private static final String CACHEFILE_EXTENSION = "lcz";            //  extension of the files name (Note: cache files are in zip format)
  private static final int MIN_CHARACTERS_TO_SAVE_CACHE = 25000;      //  Minimum characters of document for saving cache 
  
  private String documentPath = null;
  private AllCaches allCaches;
  
  CacheIO(XComponent xComponent) {
    setDocumentPath(xComponent);
  }
  
  void setDocumentPath(XComponent xComponent) {
    if (xComponent != null) {
      documentPath = getDocumentPath(xComponent);
    }
  }
  
  /** 
   * returns the text cursor (if any)
   * returns null if it fails
   */
  private static String getDocumentPath(XComponent xComponent) {
    try {
      XTextDocument curDoc = UnoRuntime.queryInterface(XTextDocument.class, xComponent);
      if (curDoc == null) {
        MessageHandler.printToLogFile("CacheIO: getDocumentPath: XTextDocument not found!");
        return null;
      }
      XController xController = curDoc.getCurrentController();
      if (xController == null) {
        MessageHandler.printToLogFile("CacheIO: getDocumentPath: XController not found!");
        return null;
      }
      XModel xModel = xController.getModel();
      if (xModel == null) {
        MessageHandler.printToLogFile("CacheIO: getDocumentPath: XModel not found!");
        return null;
      }
      String url = xModel.getURL();
      if (url == null || !url.startsWith("file://")) {
        if (url != null && !url.isEmpty()) {
          MessageHandler.printToLogFile("Not a file URL: " + (url == null ? "null" : url));
        }
        return null;
      }
      if (DEBUG_MODE) {
        MessageHandler.printToLogFile("CacheIO: getDocumentPath: file URL: " + url);
      }
      URI uri = new URI(url);
      return uri.getPath();
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }

  /**
   * get the path to the cache file
   * if create == true: a new file is created if the file does not exist
   * if create == false: null is returned if the file does not exist
   */
  private String getCachePath(boolean create) {
    if (documentPath == null) {
      MessageHandler.printToLogFile("CacheIO: getCachePath: documentPath == null!");
      return null;
    }
    File cacheDir = OfficeTools.getCacheDir();
    if (DEBUG_MODE) {
      MessageHandler.printToLogFile("CacheIO: getCachePath: cacheDir: " + cacheDir.getAbsolutePath());
    }
    CacheFile cacheFile = new CacheFile(cacheDir);
    String cacheFileName = cacheFile.getCacheFileName(documentPath, create);
    if (cacheFileName == null) {
      MessageHandler.printToLogFile("CacheIO: getCachePath: cacheFileName == null!");
      return null;
    }
    File cacheFilePath = new File(cacheDir, cacheFileName);
    if (!create) {
      cacheFile.cleanUp(cacheFileName);
    }
    if (DEBUG_MODE) {
      MessageHandler.printToLogFile("CacheIO: getCachePath: cacheFilePath: " + cacheFilePath.getAbsolutePath());
    }
    return cacheFilePath.getAbsolutePath();
  }
  
  /**
   * save all caches (document cache, all result caches) to cache file
   */
  private void saveAllCaches(String cachePath) {
    try {
      GZIPOutputStream fileOut = new GZIPOutputStream(new FileOutputStream(cachePath));
      ObjectOutputStream out = new ObjectOutputStream(fileOut);
      out.writeObject(allCaches);
      out.close();
      fileOut.close();
      MessageHandler.printToLogFile("Caches saved to: " + cachePath);
      if (DEBUG_MODE) {
        printCacheInfo();
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
    }
  }
  
  /**
   * returns true if the number of characters of a document exceeds 
   * the minimal number of characters to save the cache
   */
  private boolean exceedsSaveSize(DocumentCache docCache) {
    int nChars = 0;
    for (int i = 0; i < docCache.size(); i++) {
      nChars += docCache.getFlatParagraph(i).length();
      if (nChars > MIN_CHARACTERS_TO_SAVE_CACHE) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * save all caches if the document exceeds the defined minimum of paragraphs
   */
  public void saveCaches(DocumentCache docCache, List<ResultCache> paragraphsCache,
      IgnoredMatches ignoredMatches, Configuration config, MultiDocumentsHandler mDocHandler) {
    String cachePath = getCachePath(true);
    if (cachePath != null) {
      try {
        if (exceedsSaveSize(docCache)) {
          allCaches = new AllCaches(docCache, paragraphsCache, mDocHandler.getAllDisabledRules(), config.getDisabledRuleIds(), config.getDisabledCategoryNames(), 
              config.getEnabledRuleIds(), ignoredMatches, JLanguageTool.VERSION);
          saveAllCaches(cachePath);
        } else {
          File file = new File( cachePath );
          if (file.exists() && !file.isDirectory()) {
            file.delete();
          }
        }
      } catch (Throwable t) {
        MessageHandler.printToLogFile("CacheIO: saveCaches: " + t.getMessage());
        if (DEBUG_MODE) {
          MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
        }
      }
    }
  }
  
  /**
   * read all caches (document cache, all result caches) from cache file if it exists
   */
  public boolean readAllCaches(Configuration config, MultiDocumentsHandler mDocHandler) {
    String cachePath = getCachePath(false);
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
          printCacheInfo();
        }
        if (runSameRules(config, mDocHandler)) {
          return true;
        } else {
          MessageHandler.printToLogFile("Version or active rules have changed: Cache rejected (Cache Version: " 
                + allCaches.ltVersion + ", actual LT Version: " + JLanguageTool.VERSION + ")");
          return false;
        }
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
    }
    return false;
  }
  
  /**
   * Test if cache was created with same rules
   */
  private boolean runSameRules(Configuration config, MultiDocumentsHandler mDocHandler) {
    if (!allCaches.ltVersion.equals(JLanguageTool.VERSION)) {
      return false;
    }
    if (config.getEnabledRuleIds().size() != allCaches.enabledRuleIds.size() || config.getDisabledRuleIds().size() != allCaches.disabledRuleIds.size() 
          || config.getDisabledCategoryNames().size() != allCaches.disabledCategories.size()) {
      return false;
    }
    for (String ruleId : config.getEnabledRuleIds()) {
      if (!allCaches.enabledRuleIds.contains(ruleId)) {
        return false;
      }
    }
    for (String category : config.getDisabledCategoryNames()) {
      if (!allCaches.disabledCategories.contains(category)) {
        return false;
      }
    }
    Set<String> disabledRuleIds = new HashSet<String>(config.getDisabledRuleIds());
    String langCode = OfficeTools.localeToString(mDocHandler.getLocale());
    for (String ruleId : mDocHandler.getDisabledRules(langCode)) {
      disabledRuleIds.add(ruleId);
    }
    if (disabledRuleIds.size() != allCaches.disabledRuleIds.size()) {
      return false;
    }
    for (String ruleId : disabledRuleIds) {
      if (!allCaches.disabledRuleIds.contains(ruleId)) {
        return false;
      }
    }
    Map<String, Set<String>> disabledRulesUI = new HashMap<String, Set<String>>();
    for (String lang : allCaches.disabledRulesUI.keySet()) {
      Set<String > ruleIDs = new HashSet<String>();
      for (String ruleID : allCaches.disabledRulesUI.get(lang)) {
        ruleIDs.add(ruleID);
      }
      disabledRulesUI.put(langCode, ruleIDs);
    }
    mDocHandler.setAllDisabledRules(disabledRulesUI);
    return true;
  }
  
  /**
   * get document cache
   */
  public DocumentCache getDocumentCache() {
    return allCaches.docCache;
  }
  
  /**
   * get paragraph caches (results for check of paragraphes)
   */
  public List<ResultCache> getParagraphsCache() {
    return allCaches.paragraphsCache;
  }
  
  /**
   * get ignored matches
   */
  public Map<Integer, Map<String, Set<Integer>>> getIgnoredMatches() {
    Map<Integer, Map<String, Set<Integer>>> ignoredMatches = new HashMap<>();
    for (int y : allCaches.ignoredMatches.keySet()) {
      Map<String, Set<Integer>> newIdMap = new HashMap<>();
      Map<String, Set<Integer>> idMap = new HashMap<>(allCaches.ignoredMatches.get(y));
      for (String id : idMap.keySet()) {
        Set<Integer> xSet = new HashSet<>(idMap.get(id));
        newIdMap.put(id, xSet);
      }
      ignoredMatches.put(y, newIdMap);
    }
    return ignoredMatches;
  }
  
  /**
   * set all caches to null
   */
  public void resetAllCache() {
    allCaches = null;
  }
  
  /**
   * print debug information of caches to log file
   */
  private void printCacheInfo() {
    MessageHandler.printToLogFile("CacheIO: saveCaches:");
    MessageHandler.printToLogFile("Document Cache: Number of paragraphs: " + allCaches.docCache.size());
    MessageHandler.printToLogFile("Paragraph Cache(0): Number of paragraphs: " + allCaches.paragraphsCache.get(0).getNumberOfParas() 
        + ", Number of matches: " + allCaches.paragraphsCache.get(0).getNumberOfMatches());
    MessageHandler.printToLogFile("Paragraph Cache(1): Number of paragraphs: " + allCaches.paragraphsCache.get(1).getNumberOfParas() 
        + ", Number of matches: " + allCaches.paragraphsCache.get(1).getNumberOfMatches());
    for (int n = 0; n < allCaches.docCache.size(); n++) {
      MessageHandler.printToLogFile("allCaches.docCache.getFlatParagraphLocale(" + n + "): " 
            + (allCaches.docCache.getFlatParagraphLocale(n) == null ? "null" : OfficeTools.localeToString(allCaches.docCache.getFlatParagraphLocale(n))));
    }
    if (allCaches.paragraphsCache.get(0) == null) {
      MessageHandler.printToLogFile("paragraphsCache(0) == null");
    } else {
      if (allCaches.paragraphsCache.get(0).getNumberOfMatches() > 0) {
        for (int n = 0; n < allCaches.paragraphsCache.get(0).getNumberOfParas(); n++) {
          if (allCaches.paragraphsCache.get(0).getMatches(n) == null) {
            MessageHandler.printToLogFile("allCaches.sentencesCache.getMatches(" + n + ") == null");
          } else {
            if (allCaches.paragraphsCache.get(0).getMatches(n).length > 0) {
              MessageHandler.printToLogFile("Paragraph " + n + " sentence match[0]: " 
                  + "nStart = " + allCaches.paragraphsCache.get(0).getMatches(n)[0].nErrorStart 
                  + ", nLength = " + allCaches.paragraphsCache.get(0).getMatches(n)[0].nErrorLength
                  + ", errorID = " 
                  + (allCaches.paragraphsCache.get(0).getMatches(n)[0].aRuleIdentifier == null ? "null" : allCaches.paragraphsCache.get(0).getMatches(n)[0].aRuleIdentifier));
            }
          }
        }
      }
    }
  }

  class AllCaches implements Serializable {

    private static final long serialVersionUID = 5L;

    DocumentCache docCache;                 //  cache of paragraphs
    List<ResultCache> paragraphsCache;      //  Cache for matches of text rules
    Map<String, List<String>> disabledRulesUI;
    List<String> disabledRuleIds;
    List<String> disabledCategories;
    List<String> enabledRuleIds;
    Map<Integer, Map<String, Set<Integer>>> ignoredMatches;          //  Map of matches (number of paragraph, number of character) that should be ignored after ignoreOnce was called
    String ltVersion;
    
    AllCaches(DocumentCache docCache, List<ResultCache> paragraphsCache, Map<String, Set<String>> disabledRulesUI, Set<String> disabledRuleIds, 
        Set<String> disabledCategories, Set<String> enabledRuleIds, IgnoredMatches ignoredMatches, String ltVersion) {
      this.docCache = docCache;
      this.paragraphsCache = paragraphsCache;
      this.disabledRulesUI = new HashMap<String, List<String>>();
      for (String langCode : disabledRulesUI.keySet()) {
        List <String >ruleIDs = new ArrayList<String>();
        for (String ruleID : disabledRulesUI.get(langCode)) {
          ruleIDs.add(ruleID);
        }
        this.disabledRulesUI.put(langCode, ruleIDs);
      }
      this.disabledRuleIds = new ArrayList<String>();
      for (String ruleID : disabledRuleIds) {
        this.disabledRuleIds.add(ruleID);
      }
      this.disabledCategories = new ArrayList<String>();
      for (String category : disabledCategories) {
        this.disabledCategories.add(category);
      }
      this.enabledRuleIds = new ArrayList<String>();
      for (String ruleID : enabledRuleIds) {
        this.enabledRuleIds.add(ruleID);
      }
      this.ltVersion = ltVersion;
      Map<Integer, Map<String, Set<Integer>>> clone = new HashMap<>();
      for (int y : ignoredMatches.getFullMap().keySet()) {
        Map<String, Set<Integer>> newIdMap = new HashMap<>();
        Map<String, Set<Integer>> idMap = new HashMap<>(ignoredMatches.get(y));
        for (String id : idMap.keySet()) {
          Set<Integer> xSet = new HashSet<>(idMap.get(id));
          newIdMap.put(id, xSet);
        }
        clone.put(y, newIdMap);
      }
      this.ignoredMatches = clone;
    }
    
  }

  /**
   * Class to to handle the cache files
   * cache files are stored in the LT configuration directory subdirectory 'cache'
   * the paths of documents are mapped to cache files and stored in the cache map
   */
  class CacheFile implements Serializable {

    private static final long serialVersionUID = 1L;
    private CacheMap cacheMap;
    private File cacheMapFile;

    CacheFile() {
      this(OfficeTools.getCacheDir());
    }

    CacheFile(File cacheDir) {
      cacheMapFile = new File(cacheDir, CACHEFILE_MAP);
      if (cacheMapFile != null) {
        if (cacheMapFile.exists() && !cacheMapFile.isDirectory()) {
          if (read()) {
            return;
          }
        }
        cacheMap = new CacheMap();
        if (DEBUG_MODE) {
          MessageHandler.printToLogFile("CacheIO: CacheFile: create cacheMap file");
        }
        write(cacheMap);
      }
    }

    /**
     * read the cache map from file
     */
    public boolean read() {
      try {
        FileInputStream fileIn = new FileInputStream(cacheMapFile);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        cacheMap = (CacheMap) in.readObject();
        if (DEBUG_MODE) {
          MessageHandler.printToLogFile("CacheIO: CacheFile: read cacheMap file: size=" + cacheMap.size());
        }
        in.close();
        fileIn.close();
        return true;
      } catch (Throwable t) {
        MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
        return false;
      }
    }

    /**
     * write the cache map to file
     */
    public void write(CacheMap cacheMap) {
      try {
        FileOutputStream fileOut = new FileOutputStream(cacheMapFile);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        if (DEBUG_MODE) {
          MessageHandler.printToLogFile("CacheIO: CacheFile: write cacheMap file: size=" + cacheMap.size());
        }
        out.writeObject(cacheMap);
        out.close();
        fileOut.close();
      } catch (Throwable t) {
        MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      }
    }
    
    /**
     * get the cache file name for a given document path
     * if create == true: create a cache file if it not exists 
     */
    public String getCacheFileName(String docPath, boolean create) {
      if (cacheMap == null) {
        return null;
      }
      int orgSize = cacheMap.size();
      String cacheFileName = cacheMap.getOrCreateCacheFile(docPath, create);
      if (cacheMap.size() != orgSize) {
        write(cacheMap);
      }
      return cacheFileName;
    }
    
   /**
    * remove unused files from cache directory
    */
    public void cleanUp(String curCacheFile) {
      CacheCleanUp cacheCleanUp = new CacheCleanUp(cacheMap, curCacheFile);
      cacheCleanUp.start();
    }
    
    /**
     * Class to create and handle the cache map
     * the clean up process is run in a separate parallel thread
     */
    class CacheMap implements Serializable {
      private static final long serialVersionUID = 1L;
      private Map<String, String> cacheNames;     //  contains the mapping from document paths to cache file names
      
      CacheMap() {
        cacheNames = new HashMap<>();
      }
      
      CacheMap(CacheMap in) {
        cacheNames = new HashMap<>();
        cacheNames.putAll(in.getCacheNames());
      }

      /**
       * get the cache map that contains the mapping from document paths to cache file names 
       */
      private Map<String, String> getCacheNames() {
        return cacheNames;
      }

      /**
       * get the size of the cache map
       */
      public int size() {
        return cacheNames.keySet().size();
      }
      
      /**
       * return true if the map contains the cache file name
       */
      public boolean containsValue(String value) {
        return cacheNames.containsValue(value);
      }
      
      /**
       * get all document paths contained in the map
       */
      public Set<String> keySet() {
        return cacheNames.keySet();
      }
      
      /**
       * get the cache file name from a document path
       */
      public String get(String key) {
        return cacheNames.get(key);
      }
      
      /**
       * remove a document paths from the map (inclusive the mapped cache file name)
       */
      public String remove(String key) {
        return cacheNames.remove(key);
      }
      
      /**
       * get the cache file name for a document paths
       * if create == true:  create the file if not exist
       * if create == false: return null if not exist
       */
      public String getOrCreateCacheFile(String docPath, boolean create) {
        if (DEBUG_MODE) {
          MessageHandler.printToLogFile("CacheIO: getOrCreateCacheFile: docPath=" + docPath);
          for (String file : cacheNames.keySet()) {
            MessageHandler.printToLogFile("cacheNames: docPath=" + file + ", cache=" + cacheNames.get(file));
          }
        }
        if (cacheNames.containsKey(docPath)) {
          return cacheNames.get(docPath);
        }
        if (!create) {
          return null;
        }
        int i = 1;
        String cacheName = CACHEFILE_PREFIX + i + "." + CACHEFILE_EXTENSION;
        while (cacheNames.containsValue(cacheName)) {
          i++;
          cacheName = CACHEFILE_PREFIX + i + "." + CACHEFILE_EXTENSION;
        }
        cacheNames.put(docPath, cacheName);
        return cacheName;
      }
    }
    
    /**
     * class to clean up cache
     * delete cache files for not existent document paths
     * remove not existent document paths and cache files from map
     * the clean up process is ran in a separate thread 
     */
    class CacheCleanUp extends Thread implements Serializable {
      private static final long serialVersionUID = 1L;
      private CacheMap cacheMap;
      private String currentFile;
      
      CacheCleanUp(CacheMap in, String curFile) {
        cacheMap = new CacheMap(in);
        currentFile = curFile;
      }
      
      /**
       * run clean up process
       */
      @Override
      public void run() {
        try {
          long systemTime = System.currentTimeMillis();
          boolean mapChanged = false;
          File cacheDir = OfficeTools.getCacheDir();
          List<String> mapedDocs = new ArrayList<String>();
          for (String doc : cacheMap.keySet()) {
            mapedDocs.add(doc);
          }
          for (String doc : mapedDocs) {
            File docFile = new File(doc);
            String cacheFileName = cacheMap.get(doc);
            File cacheFile = new File(cacheDir, cacheFileName);
            if (DEBUG_MODE) {
              MessageHandler.printToLogFile("CacheIO: CacheCleanUp: CacheMap: docPath=" + doc + ", docFile exist: " + (docFile == null ? "null" : docFile.exists()) + 
                  ", cacheFile exist: " + (cacheFile == null ? "null" : cacheFile.exists()));
            }
            if (docFile == null || !docFile.exists() || cacheFile == null || !cacheFile.exists() 
                || (systemTime - cacheFile.lastModified() > MAX_CACHE_TIME && !cacheFileName.equals(currentFile))) {
              cacheMap.remove(doc);
              mapChanged = true;
              MessageHandler.printToLogFile("CacheIO: CacheCleanUp: Remove Path from CacheMap: " + doc);
              if (cacheFile != null && cacheFile.exists()) {
                cacheFile.delete();
                MessageHandler.printToLogFile("CacheIO: CacheCleanUp: Delete cache file: " + cacheFile.getAbsolutePath());
              }
            }
          }
          if (mapChanged) {
            if (DEBUG_MODE) {
              MessageHandler.printToLogFile("CacheIO: CacheCleanUp: Write CacheMap");
            }
            write(cacheMap);
          }
          File[] cacheFiles = cacheDir.listFiles();
          if (cacheFiles != null) {
            for (File cacheFile : cacheFiles) {
              if (!cacheMap.containsValue(cacheFile.getName()) && !cacheFile.getName().equals(CACHEFILE_MAP)) {
                cacheFile.delete();
                MessageHandler.printToLogFile("Delete cache file: " + cacheFile.getAbsolutePath());
              }
            }
          }
        } catch (Throwable t) {
          MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
        }
      }
    }
  }
  
}
