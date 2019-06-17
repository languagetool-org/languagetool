/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.server;

import org.languagetool.*;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper for JLanguageTool instances that can be made immutable
 * Use case: Setup instances once (ahead of time or on demand), cache and use when matching queries come in; work around thread safety issues by only giving out one reference at a time
 * @see PipelinePool
 */
class Pipeline extends JLanguageTool {

  static class IllegalPipelineMutationException extends RuntimeException {
    IllegalPipelineMutationException() {
      super("Pipeline is frozen; mutating shared JLanguageTool instance is forbidden.");
    }
  }

  private boolean setup = false;
  private long lastUsedTimestamp;

  /**
   * Prevents any further changes after this method was called.
   */
  void setupFinished() {
   this.setup = true;
  }

  /**
   * Refresh expire timer of pipeline
   */
  void refreshExpireTimer() {
    lastUsedTimestamp = System.currentTimeMillis();
  }

  /**
   * Test if expire time has elapsed since last use.
   * @return is pipeline expired?
   */
  boolean isExpired() {
    long delta = System.currentTimeMillis() - lastUsedTimestamp;
    return delta > PipelinePool.PIPELINE_EXPIRE_TIME;
  }

  Pipeline(Language language, List<Language> altLanguages, Language motherTongue, ResultCache cache, GlobalConfig globalConfig, UserConfig userConfig) {
    super(language, altLanguages, motherTongue, cache, globalConfig, userConfig);
    lastUsedTimestamp = System.currentTimeMillis();
  }

  @Override
  public void setCleanOverlappingMatches(boolean cleanOverlappingMatches) {
    if (setup) {
      throw new IllegalPipelineMutationException();
    }
    super.setCleanOverlappingMatches(cleanOverlappingMatches);
  }

  @Override
  public void setMaxErrorsPerWordRate(float maxErrorsPerWordRate) {
    if (setup) {
      throw new IllegalPipelineMutationException();
    }
    super.setMaxErrorsPerWordRate(maxErrorsPerWordRate);
  }

  @Override
  public void setOutput(PrintStream printStream) {
    if (setup) {
      throw new IllegalPipelineMutationException();
    }
    super.setOutput(printStream);
  }

  @Override
  public List<AbstractPatternRule> loadPatternRules(String filename) throws IOException {
    if (setup) {
      throw new IllegalPipelineMutationException();
    }
    return super.loadPatternRules(filename);
  }

  @Override
  public List<AbstractPatternRule> loadFalseFriendRules(String filename) throws ParserConfigurationException, SAXException, IOException {
    if (setup) {
      throw new IllegalPipelineMutationException();
    }
    return super.loadFalseFriendRules(filename);
  }

  @Override
  public void activateLanguageModelRules(File indexDir) throws IOException {
    if (setup) {
      throw new IllegalPipelineMutationException();
    }
    super.activateLanguageModelRules(indexDir);
  }

  @Override
  public void activateWord2VecModelRules(File indexDir) throws IOException {
    if (setup) {
      throw new IllegalPipelineMutationException();
    }
    super.activateWord2VecModelRules(indexDir);
  }

  @Override
  public void addRule(Rule rule) {
    if (setup) {
      throw new IllegalPipelineMutationException();
    }
    super.addRule(rule);
  }

  @Override
  public void disableRule(String ruleId) {
    if (setup) {
      throw new IllegalPipelineMutationException();
    }
    super.disableRule(ruleId);
  }

  @Override
  public void disableRules(List<String> ruleIds) {
    if (setup) {
      throw new IllegalPipelineMutationException();
    }
    super.disableRules(ruleIds);
  }

  @Override
  public void disableCategory(CategoryId id) {
    if (setup) {
      throw new IllegalPipelineMutationException();
    }
    super.disableCategory(id);
  }

  @Override
  public Set<String> getDisabledRules() {
    return Collections.unmodifiableSet(super.getDisabledRules());
  }

  @Override
  public void enableRule(String ruleId) {
    if (setup) {
      throw new IllegalPipelineMutationException();
    }
    super.enableRule(ruleId);
  }

  @Override
  public void enableRuleCategory(CategoryId id) {
    if (setup) {
      throw new IllegalPipelineMutationException();
    }
    super.enableRuleCategory(id);
  }

  @Override
  public List<String> getUnknownWords() {
    return Collections.unmodifiableList(super.getUnknownWords());
  }

  @Override
  public Map<CategoryId, Category> getCategories() {
    return Collections.unmodifiableMap(super.getCategories());
  }

  @Override
  public List<Rule> getAllRules() {
    return Collections.unmodifiableList(super.getAllRules());
  }

  @Override
  public List<Rule> getAllActiveRules() {
    return Collections.unmodifiableList(super.getAllActiveRules());
  }

  @Override
  public List<Rule> getAllActiveOfficeRules() {
    return Collections.unmodifiableList(super.getAllActiveOfficeRules());
  }

  @Override
  public List<AbstractPatternRule> getPatternRulesByIdAndSubId(String Id, String subId) {
    return Collections.unmodifiableList(super.getPatternRulesByIdAndSubId(Id, subId));
  }

  @Override
  public void setConfigValues(Map<String, Integer> v) {
    if (setup) {
      throw new IllegalPipelineMutationException();
    }
    super.setConfigValues(v);
  }

}
