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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.gui.Configuration;
import org.languagetool.remote.CheckConfiguration;
import org.languagetool.remote.CheckConfigurationBuilder;
import org.languagetool.remote.RemoteConfigurationInfo;
import org.languagetool.remote.RemoteLanguageTool;
import org.languagetool.remote.RemoteResult;
import org.languagetool.remote.RemoteRuleMatch;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;

/**
 * Class to run LanguageTool in LO to use a remote server
 * @since 4.8
 * @author Fred Kruse
 */
class LORemoteLanguageTool {

  private static final String BLANK = " ";
  private static final String SERVER_URL = "https://languagetool.org/api";
  private static final int SERVER_LIMIT = 20000;

  private final Set<String> enabledRules = new HashSet<>();
  private final Set<String> disabledRules = new HashSet<>();
  private final Set<CategoryId> disabledRuleCategories = new HashSet<>();
  private final Set<CategoryId> enabledRuleCategories = new HashSet<>();
  private final List<Rule> allRules = new ArrayList<>();
  private final List<String> ruleValues = new ArrayList<>();

  private Language language;
  private Language motherTongue;
  private RemoteLanguageTool remoteLanguageTool;
  private int maxTextLength;
  private boolean remoteRun;
  
  LORemoteLanguageTool(Language language, Language motherTongue, Configuration config,
                       List<Rule> extraRemoteRules) throws MalformedURLException {
    this.language = language;
    this.motherTongue = motherTongue;
    String serverUrl = config.getServerUrl();
    setRuleValues(config.getConfigurableValues());
    URL serverBaseUrl = new URL(serverUrl == null ? SERVER_URL : serverUrl);
    remoteLanguageTool = new RemoteLanguageTool(serverBaseUrl);
    try {
      String urlParameters = "language=" + language.getShortCodeWithCountryAndVariant();
      RemoteConfigurationInfo configInfo = remoteLanguageTool.getConfigurationInfo(urlParameters);
      storeAllRules(configInfo.getRemoteRules());
      maxTextLength = configInfo.getMaxTextLength();
      MessageHandler.printToLogFile("Server Limit text length: " + maxTextLength);
      remoteRun = true;
    } catch (Throwable t) {
      MessageHandler.printException(t);
      maxTextLength = SERVER_LIMIT;
      MessageHandler.printToLogFile("Server doesn't support maxTextLength, Limit text length set to: " + maxTextLength);
      remoteRun = false;
    }
  }
  
  List<RuleMatch> check(String text, ParagraphHandling paraMode) throws IOException {
    if (!remoteRun) {
      return null;
    }
    List<RuleMatch> ruleMatches = new ArrayList<>();
    if (text == null || text.trim().isEmpty()) {
      return ruleMatches;
    }
    CheckConfigurationBuilder configBuilder = new CheckConfigurationBuilder(language.getShortCodeWithCountryAndVariant());
    if (motherTongue != null) {
      configBuilder.setMotherTongueLangCode(motherTongue.getShortCodeWithCountryAndVariant());
    }
    if (paraMode == ParagraphHandling.ONLYPARA) {
      configBuilder.ruleValues(ruleValues);
      if (enabledRules.size() > 0) {
        configBuilder.enabledRuleIds(enabledRules.toArray(new String[0]));
        configBuilder.enabledOnly();
      }
      configBuilder.mode("textLevelOnly");
    } else {
      configBuilder.enabledRuleIds(enabledRules.toArray(new String[0]));
      configBuilder.disabledRuleIds(disabledRules.toArray(new String[0]));
      configBuilder.ruleValues(ruleValues);
      configBuilder.mode("allButTextLevelOnly");
    }
    CheckConfiguration remoteConfig = configBuilder.build();
    int limit;
    for (int nStart = 0; text.length() > nStart; nStart += limit) {
      String subText;
      if (text.length() <= nStart + maxTextLength) {
        subText = text.substring(nStart);
        limit = maxTextLength;
      } else {
        int nEnd = text.lastIndexOf(SingleDocument.END_OF_PARAGRAPH, nStart + SERVER_LIMIT) + SingleDocument.NUMBER_PARAGRAPH_CHARS;
        if (nEnd <= nStart) {
          nEnd = text.lastIndexOf(BLANK, nStart + SERVER_LIMIT) + 1;
          if (nEnd <= nStart) {
            nEnd = nStart + SERVER_LIMIT;
          }
        }
        subText = text.substring(nStart, nEnd);
        limit = nEnd;
      }
      RemoteResult remoteResult;
      try {
        remoteResult = remoteLanguageTool.check(subText, remoteConfig);
      } catch (Throwable t) {
        MessageHandler.printException(t);
        remoteRun = false;
        return null;
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
  
  boolean remoteRun() {
    return remoteRun;
  }
  
  private boolean ignoreRule(Rule rule) {
    Category ruleCategory = rule.getCategory();
    boolean isCategoryDisabled = (disabledRuleCategories.contains(ruleCategory.getId()) || rule.getCategory().isDefaultOff()) 
            && !enabledRuleCategories.contains(ruleCategory.getId());
    boolean isRuleDisabled = disabledRules.contains(rule.getId()) 
            || (rule.isDefaultOff() && !enabledRules.contains(rule.getId()));
    boolean isDisabled;
    if (isCategoryDisabled) {
      isDisabled = !enabledRules.contains(rule.getId());
    } else {
      isDisabled = isRuleDisabled;
    }
    return isDisabled;
  }

  public List<Rule> getAllActiveOfficeRules() {
    List<Rule> rulesActive = new ArrayList<>();
    for (Rule rule : allRules) {
      if (!ignoreRule(rule) && !rule.isOfficeDefaultOff()) {
        rulesActive.add(rule);
      } else if (rule.isOfficeDefaultOn() && !disabledRules.contains(rule.getId())) {
        rulesActive.add(rule);
        enableRule(rule.getId());
      } else if (!ignoreRule(rule) && rule.isOfficeDefaultOff() && !enabledRules.contains(rule.getId())) {
        disableRule(rule.getId());
      }
    }    
    return rulesActive;
  }
  
 public Set<String> getDisabledRules() {
   return disabledRules;
 }
  
  void enableRule (String ruleId) {
    disabledRules.remove(ruleId);
    enabledRules.add(ruleId);
  }
  
  void disableRule (String ruleId) {
    disabledRules.add(ruleId);
    enabledRules.remove(ruleId);
  }
  
  public void disableCategory(CategoryId id) {
    disabledRuleCategories.add(id);
    enabledRuleCategories.remove(id);
  }
  
  private void setRuleValues(Map<String, Integer> configurableValues) {
    ruleValues.clear();
    Set<String> rules = configurableValues.keySet();
    for (String rule : rules) {
      String ruleValueString = new String(rule + ":" + configurableValues.get(rule));
      ruleValues.add(ruleValueString);
    }
  }
  
  private RuleMatch toRuleMatch(RemoteRuleMatch remoteMatch, int nOffset) throws MalformedURLException {
    Rule matchRule = null;
    for (Rule rule : allRules) {
      if (remoteMatch.getRuleId().equals(rule.getId())) {
        matchRule = rule;
      }
    }
    RuleMatch ruleMatch = new RuleMatch(matchRule, null, remoteMatch.getErrorOffset() + nOffset, 
        remoteMatch.getErrorOffset() + remoteMatch.getErrorLength() + nOffset, remoteMatch.getMessage(), 
        remoteMatch.getShortMessage().isPresent() ? remoteMatch.getShortMessage().get() : null);
    if (remoteMatch.getUrl().isPresent()) {
      ruleMatch.setUrl(new URL(remoteMatch.getUrl().get()));
    }
    if (remoteMatch.getReplacements().isPresent()) {
      ruleMatch.setSuggestedReplacements(remoteMatch.getReplacements().get());
    }
    return ruleMatch;
  }
  
  private List<RuleMatch> toRuleMatches(List<RemoteRuleMatch> remoteRulematches, int nOffset) throws MalformedURLException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    if (remoteRulematches == null || remoteRulematches.isEmpty()) {
      return ruleMatches;
    }
    for(RemoteRuleMatch remoteMatch : remoteRulematches) {
      RuleMatch ruleMatch = toRuleMatch(remoteMatch, nOffset);
      ruleMatches.add(ruleMatch);
    }
    return ruleMatches;
  }
  
  private void storeAllRules(List<Map<String,String>> listRuleMaps) {
    allRules.clear();
    for(Map<String,String> ruleMap : listRuleMaps) {
      Rule rule;
      if (ruleMap.containsKey("isTextLevelRule")) {
        rule = new RemoteTextLevelRule(ruleMap);
      } else {
        rule = new RemoteRule(ruleMap);
      }
      allRules.add(rule);
    }
  }

  class RemoteRule extends Rule {
    
    private final String ruleId;
    private final String description;
    private final boolean hasConfigurableValue;
    private final boolean isDictionaryBasedSpellingRule;
    private final int defaultValue;
    private final int minConfigurableValue;
    private final int maxConfigurableValue;
    private final String configureText;
    
    RemoteRule(Map<String,String> ruleMap) {
      ruleId = ruleMap.get("ruleId");
      description = ruleMap.get("description");
      if (ruleMap.containsKey("isDefaultOff")) {
        setDefaultOff();
      }
      if (ruleMap.containsKey("isOfficeDefaultOn")) {
        setOfficeDefaultOn();
      }
      if (ruleMap.containsKey("isOfficeDefaultOff")) {
        setOfficeDefaultOff();
      }
      if (ruleMap.containsKey("isDictionaryBasedSpellingRule")) {
        isDictionaryBasedSpellingRule = true;
      } else {
        isDictionaryBasedSpellingRule = false;
      }
      if (ruleMap.containsKey("hasConfigurableValue")) {
        hasConfigurableValue = true;
        defaultValue = Integer.parseInt(ruleMap.get("defaultValue"));
        minConfigurableValue = Integer.parseInt(ruleMap.get("minConfigurableValue"));
        maxConfigurableValue = Integer.parseInt(ruleMap.get("maxConfigurableValue"));
        configureText = ruleMap.get("configureText");
      } else {
        hasConfigurableValue = false;
        defaultValue = 0;
        minConfigurableValue = 0;
        maxConfigurableValue = 100;
        configureText = "";
      }
      setCategory(new Category(new CategoryId(ruleMap.get("categoryId")), ruleMap.get("categoryName")));
      setLocQualityIssueType(ITSIssueType.getIssueType(ruleMap.get("locQualityIssueType")));
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
    public boolean isDictionaryBasedSpellingRule() {
      return isDictionaryBasedSpellingRule;
    }

    @Override
    public boolean hasConfigurableValue() {
      return hasConfigurableValue;
    }

    @Override
    public int getDefaultValue() {
      return defaultValue;
    }

    @Override
    public int getMinConfigurableValue() {
      return minConfigurableValue;
    }

    @Override
    public int getMaxConfigurableValue() {
      return maxConfigurableValue;
    }

    @Override
    public String getConfigureText() {
      return configureText;
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
      return null;
    }
    
  }
  
  class RemoteTextLevelRule extends TextLevelRule {
    
    private final String ruleId;
    private final String description;
    private final boolean hasConfigurableValue;
    private final boolean isDictionaryBasedSpellingRule;
    private final int defaultValue;
    private final int minConfigurableValue;
    private final int maxConfigurableValue;
    private final int minToCheckParagraph;
    private final String configureText;
    
    RemoteTextLevelRule(Map<String,String> ruleMap) {
      ruleId = ruleMap.get("ruleId");
      description = ruleMap.get("description");
      if (ruleMap.containsKey("isDefaultOff")) {
        setDefaultOff();
      }
      if (ruleMap.containsKey("isOfficeDefaultOn")) {
        setOfficeDefaultOn();
      }
      if (ruleMap.containsKey("isOfficeDefaultOff")) {
        setOfficeDefaultOff();
      }
      if (ruleMap.containsKey("isDictionaryBasedSpellingRule")) {
        isDictionaryBasedSpellingRule = true;
      } else {
        isDictionaryBasedSpellingRule = false;
      }
      if (ruleMap.containsKey("hasConfigurableValue")) {
        hasConfigurableValue = true;
        defaultValue = Integer.parseInt(ruleMap.get("defaultValue"));
        minConfigurableValue = Integer.parseInt(ruleMap.get("minConfigurableValue"));
        maxConfigurableValue = Integer.parseInt(ruleMap.get("maxConfigurableValue"));
        configureText = ruleMap.get("configureText");
      } else {
        hasConfigurableValue = false;
        defaultValue = 0;
        minConfigurableValue = 0;
        maxConfigurableValue = 100;
        configureText = "";
      }
      minToCheckParagraph = Integer.parseInt(ruleMap.get("minToCheckParagraph"));
      setCategory(new Category(new CategoryId(ruleMap.get("categoryId")), ruleMap.get("categoryName")));
      setLocQualityIssueType(ITSIssueType.getIssueType(ruleMap.get("locQualityIssueType")));
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
    public boolean isDictionaryBasedSpellingRule() {
      return isDictionaryBasedSpellingRule;
    }

    @Override
    public boolean hasConfigurableValue() {
      return hasConfigurableValue;
    }

    @Override
    public int getDefaultValue() {
      return defaultValue;
    }

    @Override
    public int getMinConfigurableValue() {
      return minConfigurableValue;
    }

    @Override
    public int getMaxConfigurableValue() {
      return maxConfigurableValue;
    }

    @Override
    public String getConfigureText() {
      return configureText;
    }

    @Override
    public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
      return null;
    }

    @Override
    public int minToCheckParagraph() {
      return minToCheckParagraph;
    }
    
  }
  
}
