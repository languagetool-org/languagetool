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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.UserConfig;
import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.gui.Configuration;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.remote.CheckConfiguration;
import org.languagetool.remote.CheckConfigurationBuilder;
import org.languagetool.remote.RemoteLanguageTool;
import org.languagetool.remote.RemoteResult;
import org.languagetool.remote.RemoteRuleMatch;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;

/**
 * Class to switch between running LanguageTool in multi or single thread mode
 * @since 4.6
 * @author Fred Kruse
 */
public class SwJLanguageTool {
  
  boolean isMultiThread = false;
  boolean isRemote = false;
  boolean useServerConfig = false;
  String serverUrl = null;
  JLanguageTool lt = null;
  MultiThreadedJLanguageTool mlt = null;
  LORemoteLanguageTool rlt = null;

  public SwJLanguageTool(Language language, Language motherTongue, UserConfig userConfig, 
      Configuration config) throws MalformedURLException {
    isMultiThread = config.isMultiThread();
    isRemote = config.doRemoteCheck();
    useServerConfig = config.useServerConfiguration();
    serverUrl = config.getServerUrl();
    if(isRemote) {
      rlt = new LORemoteLanguageTool(language, motherTongue, userConfig);
    } else if(isMultiThread) {
      mlt = new MultiThreadedJLanguageTool(language, motherTongue, userConfig); 
    } else {
      lt = new JLanguageTool(language, motherTongue, null, userConfig); 
    }
  }

  public List<Rule> getAllRules() {
    if(isMultiThread && !isRemote) {
      return mlt.getAllRules(); 
    } else {
      return lt.getAllRules(); 
    }
  }

  public List<Rule> getAllActiveOfficeRules() {
    if(isMultiThread && !isRemote) {
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

  public void disableCategory(CategoryId id) {
    if(isMultiThread && !isRemote) {
      mlt.disableCategory(id); 
    } else {
      lt.disableCategory(id); 
    }
  }

  public void activateLanguageModelRules(File indexDir) throws IOException {
    if(isMultiThread && !isRemote) {
      mlt.activateLanguageModelRules(indexDir); 
    } else {
      lt.activateLanguageModelRules(indexDir); 
    }
  }

  public List<RuleMatch> check(String text, boolean tokenizeText, ParagraphHandling paraMode) throws IOException {
    if(isRemote) {
      return rlt.check(text, paraMode); 
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
    if(isMultiThread && !isRemote) {
      return mlt.sentenceTokenize(text); 
    } else {
      return lt.sentenceTokenize(text); 
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

  private class LORemoteLanguageTool {
    private static final String SERVER_URL = "https://languagetool.org/api";
    private static final int SERVER_LIMIT = 20000;
    private static final String END_OF_PARAGRAPH = "\n";  //  Paragraph Separator like in standalone GUI
    private boolean initDone = false;
    private URL serverBaseUrl;
    private Language language;
    private RemoteLanguageTool remoteLanguageTool;
    private List<String> enabledRules = new ArrayList<String>();
    private List<String> disabledRules = new ArrayList<String>();
    private List<String> textRules;
    private List<String> nonTextRules;
    private List<Rule> allRules;
    private CheckConfiguration remoteConfig;
    private CheckConfigurationBuilder configBuilder;
    
    public LORemoteLanguageTool(Language language, Language motherTongue, UserConfig userConfig) throws MalformedURLException {
      this.language = language;
      configBuilder = new CheckConfigurationBuilder(language.getShortCodeWithCountryAndVariant());
      configBuilder.setMotherTongueLangCode(motherTongue.getShortCodeWithCountryAndVariant());
      serverBaseUrl = new URL(serverUrl == null ? SERVER_URL : serverUrl);
      remoteLanguageTool = new RemoteLanguageTool(serverBaseUrl) ;
      lt = new JLanguageTool(language, motherTongue, null, userConfig);
    }
    
    List<RuleMatch> check(String text, ParagraphHandling paraMode) throws MalformedURLException {
      if(!initDone) {
        allRules = lt.getAllActiveOfficeRules();
        textRules = getAllTextRules();
        nonTextRules = getAllNonTextRules();
        configBuilder.enabledOnly();
        if(paraMode == ParagraphHandling.ONLYPARA) {
          configBuilder.enabledRuleIds(textRules);
        } else {
          configBuilder.enabledRuleIds(nonTextRules);
        }
        remoteConfig = configBuilder.build();
      }
      List<RemoteRuleMatch> remoteRulematches = new ArrayList<RemoteRuleMatch>();
      int limit = SERVER_LIMIT;
      for (int nStart = 0; text.length() > nStart; nStart += limit) {
        String subText;
        if(text.length() <= nStart + SERVER_LIMIT) {
          subText = text.substring(nStart);
          limit = SERVER_LIMIT;
        } else {
          int nEnd = text.lastIndexOf(END_OF_PARAGRAPH, nStart + SERVER_LIMIT);
          if(nEnd <= nStart) {
            nEnd = nStart + SERVER_LIMIT;
          }
          subText = text.substring(nStart, nEnd);
          limit = nEnd;
        }
        RemoteResult remoteResult = remoteLanguageTool.check(subText, remoteConfig);
        remoteRulematches.addAll(remoteResult.getMatches()); 
      }
      return toRuleMatches(remoteRulematches);
    }
    
    Language getLanguage() {
      return language;
    }
    
    void enableRule (String ruleId) {
      disabledRules.remove(ruleId);
      enabledRules.add(ruleId);
      lt.enableRule(ruleId);
      initDone = false;
    }
    
    void disableRule (String ruleId) {
      disabledRules.add(ruleId);
      enabledRules.remove(ruleId);
      lt.disableRule(ruleId);
      initDone = false;
    }
    
    private List<String> getAllTextRules() {
      textRules = new ArrayList<String>();
      Set<String> disRules = lt.getDisabledRules();
      for(Rule rule : allRules) {
        if(rule instanceof TextLevelRule && !disRules.contains(rule.getId()) && !disabledRules.contains(rule.getId())) {
          textRules.add(rule.getId());
        }
      }
      return textRules;
    }
    
    private List<String> getAllNonTextRules() {
      nonTextRules = new ArrayList<String>();
      Set<String> disRules = lt.getDisabledRules();
      for(Rule rule : allRules) {
        if(!(rule instanceof TextLevelRule) && !disRules.contains(rule.getId()) && !disabledRules.contains(rule.getId())) {
          nonTextRules.add(rule.getId());
        }
      }
      return nonTextRules;
    }
    
    private RuleMatch toRuleMatch(RemoteRuleMatch remoteMatch) throws MalformedURLException {
      Rule matchRule = null;
      for (Rule rule : allRules) {
        if(remoteMatch.getRuleId().equals(rule.getId())) {
          matchRule = rule;
        }
      }
      if(matchRule == null) {
        return null;
      }
      RuleMatch ruleMatch = new RuleMatch(matchRule, null, remoteMatch.getErrorOffset(), 
          remoteMatch.getErrorOffset() + remoteMatch.getErrorLength(), remoteMatch.getMessage(), 
          remoteMatch.getShortMessage().isPresent() ? remoteMatch.getShortMessage().get() : null);
      if(remoteMatch.getUrl().isPresent()) {
        ruleMatch.setUrl(new URL(remoteMatch.getUrl().get()));
      }
      if(remoteMatch.getReplacements().isPresent()) {
        ruleMatch.setSuggestedReplacements(remoteMatch.getReplacements().get());
      }
      return ruleMatch;
    }
    
    private List<RuleMatch> toRuleMatches(List<RemoteRuleMatch> remoteRulematches) throws MalformedURLException {
      List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
      if(remoteRulematches == null || remoteRulematches.isEmpty()) {
        return ruleMatches;
      }
      for(RemoteRuleMatch remoteMatch : remoteRulematches) {
        RuleMatch ruleMatch = toRuleMatch(remoteMatch);
        if(ruleMatch != null) {
          ruleMatches.add(ruleMatch);
        }
      }
      return ruleMatches;
    }
    
  }
  

}
