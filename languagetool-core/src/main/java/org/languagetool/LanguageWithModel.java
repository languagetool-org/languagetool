package org.languagetool;

import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class LanguageWithModel extends Language implements AutoCloseable {
  private final AtomicBoolean noLmWarningPrinted = new AtomicBoolean();
  private LanguageModel languageModel;

  @Override
  public LanguageModel getLanguageModel(File indexDir) throws IOException {
    if (languageModel != null) {
      return languageModel;
    }
    synchronized (this) {
      if (languageModel != null) {
        return languageModel;
      }
      languageModel = initLanguageModel(indexDir, languageModel);
      return languageModel;
    }
  }

  @Override
  public synchronized void close() throws Exception {
    if (languageModel != null) {
      languageModel.close();
      languageModel = null;
    }
  }

  protected LanguageModel initLanguageModel(File indexDir, LanguageModel languageModel) {
    if (languageModel == null) {
      File topIndexDir = new File(indexDir, getShortCode());
      if (topIndexDir.exists()) {
        //noinspection resource
        languageModel = new LuceneLanguageModel(topIndexDir);
      } else if (noLmWarningPrinted.compareAndSet(false, true)) {
        System.err.println("WARN: ngram index dir " + topIndexDir + " not found for " + getName());
      }
    }
    return languageModel;
  }
}
