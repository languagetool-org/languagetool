/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.UserConfig;
import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.gui.Configuration;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

/**
 * Class to switch between running LanguageTool in multi or single thread mode
 * @since 4.6
 * @author Fred Kruse
 */
public class SwJLanguageTool {
  
  private static final ResourceBundle MESSAGES = JLanguageTool.getMessageBundle();
  private boolean isMultiThread;
  private boolean isRemote;
  private final MultiThreadedJLanguageTool mlt;
  private final LORemoteLanguageTool rlt;
  private JLanguageTool lt;
  private boolean doReset;

  public SwJLanguageTool(Language language, Language motherTongue, UserConfig userConfig, 
      Configuration config, List<Rule> extraRemoteRules, boolean testMode) throws MalformedURLException {
    isMultiThread = config.isMultiThread();
    isRemote = config.doRemoteCheck() && !testMode;
    doReset = false;
    if(isRemote) {
      lt = null;
      mlt = null;
      rlt = new LORemoteLanguageTool(language, motherTongue, config, extraRemoteRules);
      if(!rlt.remoteRun()) {
        MessageHandler.showMessage(MESSAGES.getString("loRemoteSwitchToLocal"));
        isRemote = false;
        isMultiThread = false;
        lt = new JLanguageTool(language, motherTongue, null, userConfig);
      }
    } else if(isMultiThread) {
      lt = null;
      mlt = new MultiThreadedJLanguageTool(language, motherTongue, userConfig);
      rlt = null;
    } else {
      lt = new JLanguageTool(language, motherTongue, null, userConfig);
      mlt = null;
      rlt = null;
    }
  }
  
  public boolean isRemote() {
    return isRemote;
  }

  public List<Rule> getAllRules() {
    if(isRemote) {
      return rlt.getAllRules();
    } else if(isMultiThread) {
      return mlt.getAllRules(); 
    } else {
      return lt.getAllRules(); 
    }
  }

  public List<Rule> getAllActiveOfficeRules() {
    if(isRemote) {
      return rlt.getAllActiveOfficeRules();
    } else if(isMultiThread) {
        return mlt.getAllActiveOfficeRules(); 
    } else {
      return lt.getAllActiveOfficeRules(); 
    }
  }

  public void enableRule(String ruleId) {
    if(isRemote) {
      rlt.enableRule(ruleId);
    } else if(isMultiThread) {
      mlt.enableRule(ruleId); 
    } else {
      lt.enableRule(ruleId); 
    }
  }

  public void disableRule(String ruleId) {
    if(isRemote) {
      rlt.disableRule(ruleId);
    } else if(isMultiThread) {
      mlt.disableRule(ruleId); 
    } else {
      lt.disableRule(ruleId); 
    }
  }

  public Set<String> getDisabledRules() {
    if(isRemote) {
      return rlt.getDisabledRules();
    } else if(isMultiThread) {
        return mlt.getDisabledRules(); 
    } else {
      return lt.getDisabledRules(); 
    }
  }

  public void disableCategory(CategoryId id) {
    if(isRemote) {
      rlt.disableCategory(id);
    } else if(isMultiThread) {
        mlt.disableCategory(id); 
    } else {
      lt.disableCategory(id); 
    }
  }

  public void activateLanguageModelRules(File indexDir) throws IOException {
    if(!isRemote) {
      if(isMultiThread) {
        mlt.activateLanguageModelRules(indexDir); 
      } else {
        lt.activateLanguageModelRules(indexDir); 
      }
    }
  }

  public void activateWord2VecModelRules(File indexDir) throws IOException {
    if(!isRemote) {
      if(isMultiThread) {
        mlt.activateWord2VecModelRules(indexDir); 
      } else {
        lt.activateWord2VecModelRules(indexDir); 
      }
    }
  }

  public List<RuleMatch> check(String text, boolean tokenizeText, ParagraphHandling paraMode) throws IOException {
    if(isRemote) {
      List<RuleMatch> ruleMatches = rlt.check(text, paraMode);
      if(ruleMatches == null) {
        doReset = true;
        ruleMatches = new ArrayList<RuleMatch>();
      }
      return ruleMatches;
    } else if(isMultiThread) {
      return mlt.check(text, tokenizeText, paraMode); 
    } else {
      return lt.check(text, tokenizeText, paraMode); 
    }
  }

  public List<RuleMatch> check(AnnotatedText annotatedText, boolean tokenizeText, ParagraphHandling paraMode) throws IOException {
    if(isRemote) {
      return rlt.check(annotatedText.getOriginalText(), paraMode); 
    } else if(isMultiThread) {
      synchronized(mlt) {
        return mlt.check(annotatedText, tokenizeText, paraMode);
      }
    } else {
      return lt.check(annotatedText, tokenizeText, paraMode); 
    }
  }

  public List<String> sentenceTokenize(String text) {
    if(isRemote) {
      return lt.sentenceTokenize(text);   // This is only a dummy; don't use it for call of remote server
    } else if(isMultiThread) {
        return mlt.sentenceTokenize(text); 
    } else {
      return lt.sentenceTokenize(text); 
    }
  }

  public AnalyzedSentence getAnalyzedSentence(String sentence) throws IOException {
    if(isRemote) {
      return lt.getAnalyzedSentence(sentence);   // This is only a dummy; don't use it for call of remote server
    } else if(isMultiThread) {
        return mlt.getAnalyzedSentence(sentence); 
    } else {
      return lt.getAnalyzedSentence(sentence); 
    }
  }

  public Language getLanguage() {
    if(isRemote) {
      return rlt.getLanguage();
    } else if(isMultiThread) {
      return mlt.getLanguage(); 
    } else {
      return lt.getLanguage(); 
    }
  }
  
  public boolean doReset() {
    return doReset;
  }

}
