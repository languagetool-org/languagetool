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
import java.util.Map;
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
import org.languagetool.remote.CheckConfiguration;
import org.languagetool.remote.CheckConfigurationBuilder;
import org.languagetool.remote.RemoteLanguageTool;
import org.languagetool.remote.RemoteResult;
import org.languagetool.remote.RemoteRuleMatch;
import org.languagetool.rules.Category;
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
  private final boolean useServerConfig;
  private final String serverUrl;
  private final Map<String, Integer> configurableValues;
  private final MultiThreadedJLanguageTool mlt;
  private final LORemoteLanguageTool rlt;

  private JLanguageTool lt;

  public SwJLanguageTool(Language language, Language motherTongue, UserConfig userConfig, 
      Configuration config, List<Rule> extraRemoteRules, boolean testMode) throws MalformedURLException {
    isMultiThread = config.isMultiThread();
    isRemote = config.doRemoteCheck() && !testMode;
    useServerConfig = config.useServerConfiguration();
    serverUrl = config.getServerUrl();
    configurableValues = config.getConfigurableValues();
    if(isRemote) {
      lt = null;
      mlt = null;
      rlt = new LORemoteLanguageTool(language, motherTongue, userConfig, extraRemoteRules);
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

  public Set<String> getDisabledRules() {
    if(isMultiThread && !isRemote) {
      return mlt.getDisabledRules(); 
    } else {
      return lt.getDisabledRules(); 
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

  public void activateWord2VecModelRules(File indexDir) throws IOException {
    if(isMultiThread && !isRemote) {
      mlt.activateWord2VecModelRules(indexDir); 
    } else {
      lt.activateWord2VecModelRules(indexDir); 
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
    private static final String BLANK = " ";
    private static final String SERVER_URL = "https://languagetool.org/api";
    private static final int SERVER_LIMIT = 20000;
    private boolean initDone = false;
    private URL serverBaseUrl;
    private Language language;
    private Language motherTongue;
    private RemoteLanguageTool remoteLanguageTool;
    private List<String> enabledRules = new ArrayList<>();
    private List<String> disabledRules = new ArrayList<>();
    private List<Rule> allRules = new ArrayList<>();
    private CheckConfiguration remoteConfig;
    private CheckConfigurationBuilder configBuilder;
    private List<Rule> extraRemoteRules;
    private int maxTextLength;
    
    LORemoteLanguageTool(Language language, Language motherTongue, UserConfig userConfig,
                         List<Rule> extraRemoteRules) throws MalformedURLException {
      this.language = language;
      this.motherTongue = motherTongue;
      serverBaseUrl = new URL(serverUrl == null ? SERVER_URL : serverUrl);
      remoteLanguageTool = new RemoteLanguageTool(serverBaseUrl);
      lt = new JLanguageTool(language, motherTongue, null, userConfig);
      this.extraRemoteRules = extraRemoteRules; 
      allRules.addAll(lt.getAllRules());
      allRules.addAll(extraRemoteRules);
    }
    
    List<RuleMatch> check(String text, ParagraphHandling paraMode) throws IOException {
      if(!initDone) {
        try {
          maxTextLength = remoteLanguageTool.getMaxTextLength();
          MessageHandler.printToLogFile("Server Limit text length: " + maxTextLength);
        } catch (Throwable t) {
          MessageHandler.printException(t);
          maxTextLength = SERVER_LIMIT;
          MessageHandler.printToLogFile("Server doesn't support maxTextLength, Limit text length set to: " + maxTextLength);
        }
        initDone = true;
      }
      configBuilder = new CheckConfigurationBuilder(language.getShortCodeWithCountryAndVariant());
      if(motherTongue != null) {
        configBuilder.setMotherTongueLangCode(motherTongue.getShortCodeWithCountryAndVariant());
      }
      if(paraMode == ParagraphHandling.ONLYPARA) {
        if(!useServerConfig) {
          configBuilder.enabledRuleIds(enabledRules);
          configBuilder.ruleValues(getRuleValues());
          configBuilder.enabledOnly();
        }
        configBuilder.mode("textLevelOnly");
      } else {
        if(!useServerConfig) {
          configBuilder.enabledRuleIds(enabledRules);
          configBuilder.disabledRuleIds(disabledRules);
          configBuilder.ruleValues(getRuleValues());
        }
        configBuilder.mode("allButTextLevelOnly");
      }
      remoteConfig = configBuilder.build();
      List<RuleMatch> ruleMatches = new ArrayList<>();
      int limit;
      for (int nStart = 0; text.length() > nStart; nStart += limit) {
        String subText;
        if(text.length() <= nStart + maxTextLength) {
          subText = text.substring(nStart);
          limit = maxTextLength;
        } else {
          int nEnd = text.lastIndexOf(SingleDocument.END_OF_PARAGRAPH, nStart + SERVER_LIMIT) + SingleDocument.NUMBER_PARAGRAPH_CHARS;
          if(nEnd <= nStart) {
            nEnd = text.lastIndexOf(BLANK, nStart + SERVER_LIMIT) + 1;
            if(nEnd <= nStart) {
              nEnd = nStart + SERVER_LIMIT;
            }
          }
          subText = text.substring(nStart, nEnd);
          limit = nEnd;
        }
        RemoteResult remoteResult = null;
        try {
          remoteResult = remoteLanguageTool.check(subText, remoteConfig);
        } catch (Throwable t) {
          MessageHandler.printException(t);
          MessageHandler.showMessage(MESSAGES.getString("loRemoteSwitchToLocal"));
          isRemote = false;
          isMultiThread = false;
          return lt.check(text, true, paraMode);
        }
        ruleMatches.addAll(toRuleMatches(remoteResult.getMatches(), nStart));
      }
      return ruleMatches;
    }
    
    Language getLanguage() {
      return language;
    }
    
    List<Rule> getAllRules() {
      return allRules;
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
    List<String> getRuleValues() {
      List<String> ruleValues = new ArrayList<>();
      Set<String> rules = configurableValues.keySet();
      for (String rule : rules) {
        ruleValues.add(rule + ":" + configurableValues.get(rule));
      }
      return ruleValues;
    }
    
    private RuleMatch toRuleMatch(RemoteRuleMatch remoteMatch, int nOffset) throws MalformedURLException {
      Rule matchRule = null;
      for (Rule rule : allRules) {
        if(remoteMatch.getRuleId().equals(rule.getId())) {
          matchRule = rule;
        }
      }
      if(matchRule == null) {
        matchRule = new DummyRule(remoteMatch.getRuleId(), remoteMatch.getRuleDescription(),
            remoteMatch.getCategoryId().isPresent() ? remoteMatch.getCategoryId().get() : null,
            remoteMatch.getCategory().isPresent() ? remoteMatch.getCategory().get() : null);
        allRules.add(matchRule);
        extraRemoteRules.add(matchRule);
      }
      RuleMatch ruleMatch = new RuleMatch(matchRule, null, remoteMatch.getErrorOffset() + nOffset, 
          remoteMatch.getErrorOffset() + remoteMatch.getErrorLength() + nOffset, remoteMatch.getMessage(), 
          remoteMatch.getShortMessage().isPresent() ? remoteMatch.getShortMessage().get() : null);
      if(remoteMatch.getUrl().isPresent()) {
        ruleMatch.setUrl(new URL(remoteMatch.getUrl().get()));
      }
      if(remoteMatch.getReplacements().isPresent()) {
        ruleMatch.setSuggestedReplacements(remoteMatch.getReplacements().get());
      }
      return ruleMatch;
    }
    
    private List<RuleMatch> toRuleMatches(List<RemoteRuleMatch> remoteRulematches, int nOffset) throws MalformedURLException {
      List<RuleMatch> ruleMatches = new ArrayList<>();
      if(remoteRulematches == null || remoteRulematches.isEmpty()) {
        return ruleMatches;
      }
      for(RemoteRuleMatch remoteMatch : remoteRulematches) {
        RuleMatch ruleMatch = toRuleMatch(remoteMatch, nOffset);
        if(ruleMatch != null) {
          ruleMatches.add(ruleMatch);
        }
      }
      return ruleMatches;
    }
    
    class DummyRule extends Rule {
      
      private final String ruleId;
      private final String description;
      
      DummyRule(String ruleId, String description, String categoryId, String categoryName) {
        this.ruleId = ruleId;
        this.description = description != null ? description : "unknown rule name";
        if (categoryId != null && categoryName != null) {
          setCategory(new Category(new CategoryId(categoryId), categoryName));
        }
      }

      @Override
      public String getId() {
        return ruleId;
      }

      @Override
      public String getDescription() {
        return description;
      }

      @Override
      public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
        return null;
      }
      
    }
    
  }

}
