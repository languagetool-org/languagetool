/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tools;

import org.languagetool.*;
import org.languagetool.rules.*;
import org.languagetool.rules.bitext.BitextRule;
import org.languagetool.rules.patterns.PasswordAuthenticator;
import org.languagetool.rules.patterns.bitext.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Constructor;
import java.net.*;
import java.text.MessageFormat;
import java.util.*;

public final class Tools {

  private static LinguServices linguServices = null;
  
  private Tools() {
    // cannot construct, static methods only
  }

  /**
   * Translate a text string based on our i18n files.
   * @since 3.1
   */
  public static String i18n(ResourceBundle messages, String key, Object... messageArguments) {
    MessageFormat formatter = new MessageFormat("");
    formatter.applyPattern(messages.getString(key));
    return formatter.format(messageArguments);
  }

  /**
   * Checks the bilingual input (bitext).
   *
   * @param src   Source text.
   * @param trg   Target text.
   * @param srcLt Source JLanguageTool (used to analyze the text).
   * @param trgLt Target JLanguageTool (used to analyze the text).
   * @param bRules  Bilingual rules used in addition to target standard rules.  
   * @return  The list of rule matches on the bitext.
   * @since 1.0.1
   */
  public static List<RuleMatch> checkBitext(String src, String trg,
                                            JLanguageTool srcLt, JLanguageTool trgLt,
                                            List<BitextRule> bRules) throws IOException {
    AnalyzedSentence srcText = srcLt.getAnalyzedSentence(src);
    AnalyzedSentence trgText = trgLt.getAnalyzedSentence(trg);
    List<Rule> nonBitextRules = trgLt.getAllActiveRules();
    List<RuleMatch> ruleMatches = trgLt.checkAnalyzedSentence(JLanguageTool.ParagraphHandling.NORMAL, nonBitextRules, trgText, true);
    for (BitextRule bRule : bRules) {
      RuleMatch[] curMatch = bRule.match(srcText, trgText);
      if (curMatch != null && curMatch.length > 0) {
        // adjust positions for bitext rules
        for (RuleMatch match : curMatch) {
          if (match.getColumn() < 0) {
            match.setColumn(1);
          }
          if (match.getEndColumn() < 0) {
            match.setEndColumn(trg.length() + 1); // we count from 0
          }
          if (match.getLine() < 0) {
            match.setLine(1);
          }
          if (match.getEndLine() < 0) {
            match.setEndLine(1);
          }
          ruleMatches.add(match);
        }
      }
    }
    return ruleMatches;
  }

  /** 
   * Gets default bitext rules for a given pair of languages
   * @param source  Source language.
   * @param target  Target language.
   * @return  List of Bitext rules
   */
  public static List<BitextRule> getBitextRules(Language source,
      Language target) throws IOException, ParserConfigurationException, SAXException {
    return getBitextRules(source, target, null);
  }

  /**
   * Gets default bitext rules for a given pair of languages
   * @param source  Source language.
   * @param target  Target language.
   * @param externalBitextRuleFile external file with bitext rules
   * @return  List of Bitext rules
   * @since 2.9
   */
  public static List<BitextRule> getBitextRules(Language source,
      Language target, File externalBitextRuleFile) throws IOException, ParserConfigurationException, SAXException {
    List<BitextRule> bRules = new ArrayList<>();
    //try to load the bitext pattern rules for the language...
    BitextPatternRuleLoader ruleLoader = new BitextPatternRuleLoader();          
    String name = "/" + target.getShortCode() + "/bitext.xml";
    if (JLanguageTool.getDataBroker().ruleFileExists(name)) {
      InputStream is = JLanguageTool.getDataBroker().getFromRulesDirAsStream(name);
      if (is != null) {
        bRules.addAll(ruleLoader.getRules(is, name));
      }
    }
    if (externalBitextRuleFile != null) {
      bRules.addAll(ruleLoader.getRules(new FileInputStream(externalBitextRuleFile), externalBitextRuleFile.getAbsolutePath()));
    }
    
    //load the false friend rules in the bitext mode:
    FalseFriendsAsBitextLoader fRuleLoader = new FalseFriendsAsBitextLoader();
    String falseFriendsFile = "/false-friends.xml";
    List<BitextPatternRule> rules = fRuleLoader.getFalseFriendsAsBitext(falseFriendsFile, source, target);
    bRules.addAll(rules);

    //load Java bitext rules:
    bRules.addAll(getAllBuiltinBitextRules(source, null));
    return bRules;
  }

  /**
   * Use reflection to add bitext rules.
   */
  private static List<BitextRule> getAllBuiltinBitextRules(Language language,
      ResourceBundle messages) {
    List<BitextRule> rules = new ArrayList<>();
    try {
      List<Class<? extends BitextRule>> classes = BitextRule.getRelevantRules();
      for (Class class1 : classes) {
        Constructor[] constructors = class1.getConstructors();
        boolean foundConstructor = false;
        for (Constructor constructor : constructors) {
          Class[] paramTypes = constructor.getParameterTypes();
          if (paramTypes.length == 0) {
            rules.add((BitextRule) constructor.newInstance());
            foundConstructor = true;
            break;
          }
          if (paramTypes.length == 1
              && paramTypes[0].equals(ResourceBundle.class)) {
            rules.add((BitextRule) constructor.newInstance(messages));
            foundConstructor = true;
            break;
          }
          if (paramTypes.length == 2
              && paramTypes[0].equals(ResourceBundle.class)
              && paramTypes[1].equals(Language.class)) {
            rules.add((BitextRule) constructor.newInstance(messages, language));
            foundConstructor = true;
            break;
          }
        }
        if (!foundConstructor) {
          throw new RuntimeException("Unknown constructor type for rule class " + class1.getName()
                  + ", it supports only these constructors: " + Arrays.toString(constructors));
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to load bitext rules", e);
    }
    return rules;
  }

  /**
   * @return the number of rule matches
   */
  public static int profileRulesOnLine(String contents,
      JLanguageTool lt, Rule rule) throws IOException {
    int count = 0;
    for (String sentence : lt.sentenceTokenize(contents)) {
      count += rule.match(lt.getAnalyzedSentence(sentence)).length ;
    }
    return count;
  }

  /**
   * Automatically applies suggestions to the text, as suggested
   * by the rules that match.
   * Note: if there is more than one suggestion, always the first
   * one is applied, and others are ignored silently.
   *
   * @param contents String to be corrected
   * @param lt Initialized LanguageTool object
   * @return Corrected text as String.
   */
  public static String correctText(String contents, JLanguageTool lt) throws IOException {
    List<RuleMatch> ruleMatches = lt.check(contents);
    if (ruleMatches.isEmpty()) {
      return contents;  
    }    
    return correctTextFromMatches(contents, ruleMatches);    
  }

  /**
   * @since 2.3
   */
  public static String correctTextFromMatches(
      String contents, List<RuleMatch> matches) {
    StringBuilder sb = new StringBuilder(contents);
    List<String> errors = new ArrayList<>();
    for (RuleMatch rm : matches) {
      List<String> replacements = rm.getSuggestedReplacements();
      if (!replacements.isEmpty()) {
        errors.add(sb.substring(rm.getFromPos(), rm.getToPos()));
      }
    }
    int offset = 0;
    int counter = 0;
    for (RuleMatch rm : matches) {
      List<String> replacements = rm.getSuggestedReplacements();
      if (!replacements.isEmpty()) {
        //make sure the error hasn't been already corrected:
        if (rm.getFromPos()-offset >= 0 &&
            rm.getToPos()-offset >= rm.getFromPos()-offset &&
            errors.get(counter).equals(sb.substring(rm.getFromPos() - offset, rm.getToPos() - offset))) {
          sb.replace(rm.getFromPos() - offset, rm.getToPos() - offset, replacements.get(0));
          offset += rm.getToPos() - rm.getFromPos() - replacements.get(0).length();
        }
        counter++;
      }
    }
    return sb.toString();  
  }
  
  /**
   * Get a stacktrace as a string.
   */
  public static String getFullStackTrace(Throwable e) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    return sw.toString();
  }

  /**
   * Load a file from the classpath using {@link Class#getResourceAsStream(String)}.
   * Please load files in the {@code rules} and {@code resource} directories with
   * {@link org.languagetool.broker.ResourceDataBroker} instead.
   */
  public static InputStream getStream(String path) throws IOException {
    // the other ways to load the stream like
    // "Tools.class.getClass().getResourceAsStream(filename)"
    // don't work in a web context (using Grails):
    InputStream is = JLanguageTool.getDataBroker().getAsStream(path);
    if (is == null) {
      throw new IOException("Could not load file from classpath: '" + path + "'");
    }
    return is;
  }

  /**
   * Enable and disable rules of the given LanguageTool instance.
   * @param lt LanguageTool object
   * @param disabledRuleIds ids of the rules to be disabled
   * @param enabledRuleIds ids of the rules to be enabled
   * @param useEnabledOnly if set to {@code true}, disable all rules except those enabled explicitly
   */
  public static void selectRules(JLanguageTool lt, List<String> disabledRuleIds, List<String> enabledRuleIds, boolean useEnabledOnly) {
    Set<String> disabledRuleIdsSet = new HashSet<>();
    disabledRuleIdsSet.addAll(disabledRuleIds);
    Set<String> enabledRuleIdsSet = new HashSet<>();
    enabledRuleIdsSet.addAll(enabledRuleIds);
    selectRules(lt, Collections.emptySet(), Collections.emptySet(), disabledRuleIdsSet, enabledRuleIdsSet, useEnabledOnly, false);
  }

  /**
   * @since 3.3
   */
  public static void selectRules(JLanguageTool lt, Set<CategoryId> disabledCategories, Set<CategoryId> enabledCategories,
                                 Set<String> disabledRules, Set<String> enabledRules, boolean useEnabledOnly, boolean enableTempOff) {
    if (enableTempOff) {
      for (Rule rule : lt.getAllRules()) {
        if (rule.isDefaultTempOff()) {
          System.out.println("Activating " + rule.getFullId() + ", which is default='temp_off'");
          lt.enableRule(rule.getFullId());
        }
      }
    }
    for (CategoryId id : disabledCategories) {
      lt.disableCategory(id);
    }
    if (enabledCategories.size() > 0) {
      for (CategoryId id : enabledCategories) {
        lt.enableRuleCategory(id);
      }
      if (useEnabledOnly) {
        // disable all rules except those in explicitly enabled categories, if any:
        for (Rule rule : lt.getAllRules()) {
          Category category = rule.getCategory();
          if (!enabledCategories.contains(category.getId())) {
            lt.disableRule(rule.getFullId());
          }
        }
      }
    }
    // disable rules that are disabled explicitly:
    for (String disabledRule : disabledRules) {
      lt.disableRule(disabledRule);
    }
    // enable rules
    if (enabledRules.size() > 0) {
      for (String ruleName : enabledRules) {
        lt.enableRule(ruleName);
      }
      if (useEnabledOnly) {
        // disable all rules except those enabled explicitly, if any:
        for (Rule rule : lt.getAllRules()) {
          if (!(enabledRules.contains(rule.getFullId()) || enabledRules.contains(rule.getId()))) {
            lt.disableRule(rule.getFullId());
          }
        }
      }
    }
  }

  /**
   * Enable and disable bitext rules.
   * @param bRules List of all bitext rules
   * @param disabledRules ids of rules to be disabled
   * @param enabledRules ids of rules to be enabled (by default all are enabled)
   * @param useEnabledOnly if set to {@code true}, if set to {@code true}, disable all rules except those enabled explicitly.
   * @return the list of rules to be used.
   * @since 2.8
   */
  public static List<BitextRule> selectBitextRules(List<BitextRule> bRules, List<String> disabledRules, List<String> enabledRules, boolean useEnabledOnly) {
    List<BitextRule> newBRules = new ArrayList<>(bRules.size());
    newBRules.addAll(bRules);
    List<BitextRule> rulesToDisable = new ArrayList<>();
    if (useEnabledOnly) {
      for (String enabledRule : enabledRules) {
        for (BitextRule b : bRules) {
          if (!b.getId().equals(enabledRule)) {
            rulesToDisable.add(b);
          }
        }
      }
    } else {
      for (String disabledRule : disabledRules) {
        for (BitextRule b : newBRules) {
          if (b.getId().equals(disabledRule)) {
            rulesToDisable.add(b);
          }
        }
      }
    }
    newBRules.removeAll(rulesToDisable);
    return newBRules;
  }

  /**
   * Calls {@code Authenticator.setDefault()} with a password
   * authenticator so that it's possible to use URLs of the
   * format {@code http://username:password@server} when loading XML files.
   * If the password manager doesn't allow calling {@code Authenticator.setDefault()},
   * this will be silently ignored and the feature of using these URLs
   * will not be available.
   * @since 3.0
   */
  public static void setPasswordAuthenticator() {
    SecurityManager security = System.getSecurityManager();
    if (security != null) {
      try {
        security.checkPermission(new NetPermission("setDefaultAuthenticator"));
        Authenticator.setDefault(new PasswordAuthenticator());
      } catch (SecurityException e) {
        // ignore, but the feature to use user:password in the URL cannot be used now,
        // see https://github.com/languagetool-org/languagetool/issues/255
      }
    } else {
      Authenticator.setDefault(new PasswordAuthenticator());
    }
  }

  /**
   * Create a URL object from a string. Helper method that turns
   * the {@code MalformedURLException} into a {@code RuntimeException}.
   * @since 4.0
   */
  public static URL getUrl(String url) {
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @since 4.9
   */
  public static boolean isParagraphEnd(List<AnalyzedSentence> sentences, int nTest, Language lang) {
    if (nTest >= sentences.size() - 1) {
      return true;
    }
    if (lang.getSentenceTokenizer().singleLineBreaksMarksPara()) {
      if (sentences.get(nTest).getText().endsWith("\n") || sentences.get(nTest).getText().endsWith("\n\r")) {
        return true;
      }
    } else {
      if (sentences.get(nTest).getText().endsWith("\n\n") || sentences.get(nTest).getText().endsWith("\n\r\n\r") || sentences.get(nTest).getText().endsWith("\r\n\r\n")) {
        return true;
      }
    }
    if (sentences.get(nTest + 1).getText().startsWith("\n") || sentences.get(nTest + 1).getText().startsWith("\r\n")) {
      return true;
    }
    return false;
  }

  /**
   * set linguistic services (only to introduce external speller for LT)
   * since 5.7
   */
  public static void setLinguisticServices(LinguServices ls) {
    linguServices = ls;
  }
  
  /**
   * since 5.7
   */
  public static boolean isExternSpeller() {
    return linguServices != null;
  }
  
  /**
   * since 5.7
   */
  public static LinguServices getLinguisticServices() {
    return linguServices;
  }
  
}
