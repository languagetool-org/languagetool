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

import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.bitext.BitextRule;
import org.languagetool.rules.patterns.bitext.BitextPatternRule;
import org.languagetool.rules.patterns.bitext.BitextPatternRuleLoader;
import org.languagetool.rules.patterns.bitext.FalseFriendsAsBitextLoader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.*;

public final class Tools {

  private Tools() {
    // cannot construct, static methods only
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
  public static List<RuleMatch> checkBitext(final String src, final String trg,
                                            final JLanguageTool srcLt, final JLanguageTool trgLt,
                                            final List<BitextRule> bRules) throws IOException {
    final AnalyzedSentence srcText = srcLt.getAnalyzedSentence(src);
    final AnalyzedSentence trgText = trgLt.getAnalyzedSentence(trg);
    final List<RuleMatch> ruleMatches = trgLt.checkAnalyzedSentence(JLanguageTool.ParagraphHandling.NORMAL,
            trgLt.getAllRules(), 0, 0, 1, trg, trgText, null);
    for (BitextRule bRule : bRules) {
      final RuleMatch[] curMatch = bRule.match(srcText, trgText);
      if (curMatch != null) {
        ruleMatches.addAll(Arrays.asList(curMatch));
      }
    }
    return ruleMatches;
  }

  /** 
   * Gets default bitext rules for a given pair of languages
   *
   * @param source  Source language.
   * @param target  Target language.
   * @return  List of Bitext rules
   */
  public static List<BitextRule> getBitextRules(final Language source, 
      final Language target) throws IOException, ParserConfigurationException, SAXException {
    final List<BitextRule> bRules = new ArrayList<>();
    //try to load the bitext pattern rules for the language...
    final BitextPatternRuleLoader ruleLoader = new BitextPatternRuleLoader();          
    final String name = "/" + target.getShortName() + "/bitext.xml";
    final InputStream is = JLanguageTool.getDataBroker().getFromRulesDirAsStream(name);
    if (is != null) {
      bRules.addAll(ruleLoader.getRules(is, name));
    }
    
    //load the false friend rules in the bitext mode:
    final FalseFriendsAsBitextLoader fRuleLoader = new FalseFriendsAsBitextLoader();
    final String falseFriendsFile = "/false-friends.xml";
    final List<BitextPatternRule> rules = fRuleLoader.getFalseFriendsAsBitext(falseFriendsFile, source, target);
    bRules.addAll(rules);

    //load Java bitext rules:
    bRules.addAll(getAllBuiltinBitextRules(source, null));
    return bRules;
  }

  /**
   * Use reflection to add bitext rules.
   */
  private static List<BitextRule> getAllBuiltinBitextRules(final Language language,
      final ResourceBundle messages) {
    final List<BitextRule> rules = new ArrayList<>();
    try {
      final List<Class<? extends BitextRule>> classes = BitextRule.getRelevantRules();
      for (final Class class1 : classes) {
        final Constructor[] constructors = class1.getConstructors();
        boolean foundConstructor = false;
        for (final Constructor constructor : constructors) {
          final Class[] paramTypes = constructor.getParameterTypes();
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
    } catch (final Exception e) {
      throw new RuntimeException("Failed to load bitext rules", e);
    }
    return rules;
  }

  /**
   * @return the number of rule matches
   */
  public static int profileRulesOnLine(final String contents,
      final JLanguageTool lt, final Rule rule) throws IOException {
    int count = 0;
    for (final String sentence : lt.sentenceTokenize(contents)) {
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
  public static String correctText(final String contents, final JLanguageTool lt) throws IOException {
    final List<RuleMatch> ruleMatches = lt.check(contents);
    if (ruleMatches.isEmpty()) {
      return contents;  
    }    
    return correctTextFromMatches(contents, ruleMatches);    
  }

  /**
   * @since 2.3
   */
  public static String correctTextFromMatches(
      final String contents, final List<RuleMatch> matches) {
    final StringBuilder sb = new StringBuilder(contents);
    final List<String> errors = new ArrayList<>();
    for (RuleMatch rm : matches) {
      final List<String> replacements = rm.getSuggestedReplacements();
      if (!replacements.isEmpty()) {
        errors.add(sb.substring(rm.getFromPos(), rm.getToPos()));
      }
    }
    int offset = 0;
    int counter = 0;
    for (RuleMatch rm : matches) {
      final List<String> replacements = rm.getSuggestedReplacements();
      if (!replacements.isEmpty()) {
        //make sure the error hasn't been already corrected:
        if (errors.get(counter).equals(sb.substring(rm.getFromPos() - offset, rm.getToPos() - offset))) {
          sb.replace(rm.getFromPos() - offset, rm.getToPos() - offset, replacements.get(0));
          offset += (rm.getToPos() - rm.getFromPos()) - replacements.get(0).length();
        }
        counter++;
      }
    }
    return sb.toString();  
  }
  
  /**
   * Get a stacktrace as a string.
   */
  public static String getFullStackTrace(final Throwable e) {
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    return sw.toString();
  }

  /**
   * Load a file from the classpath using {@link Class#getResourceAsStream(String)}.
   * 
   * @return the stream of the file
   */
  public static InputStream getStream(final String filename) throws IOException {
    // the other ways to load the stream like
    // "Tools.class.getClass().getResourceAsStream(filename)"
    // don't work in a web context (using Grails):
    final InputStream is = Tools.class.getResourceAsStream(filename);
    if (is == null) {
      throw new IOException("Could not load file from classpath : " + filename);
    }
    return is;
  }

  /**
   * Enable and disable rules of the given LanguageTool instance.
   *
   * @param lt LanguageTool object
   * @param disabledRules ids of the rules to be disabled
   * @param enabledRules ids of the rules to be enabled
   */
  public static void selectRules(final JLanguageTool lt, final String[] disabledRules, final String[] enabledRules) {
    selectRules(lt, disabledRules, enabledRules, true);
  }

  /**
   * Enable and disable rules of the given LanguageTool instance.
   *
   * @param lt LanguageTool object
   * @param disabledRuleIds ids of the rules to be disabled
   * @param enabledRuleIds ids of the rules to be enabled
   * @param useEnabledOnly if set to {@code true}, disable all rules except those enabled explicitly
   */
  public static void selectRules(final JLanguageTool lt, final List<String> disabledRuleIds, final List<String> enabledRuleIds, boolean useEnabledOnly) {
    selectRules(lt, disabledRuleIds.toArray(new String[disabledRuleIds.size()]), enabledRuleIds.toArray(new String[enabledRuleIds.size()]), useEnabledOnly);
  }

  /**
   * Enable and disable rules of the given LanguageTool instance.
   *
   * @param lt LanguageTool object
   * @param disabledRules ids of the rules to be disabled
   * @param enabledRules ids of the rules to be enabled
   * @param useEnabledOnly if set to {@code true}, disable all rules except those enabled explicitly
   */
  public static void selectRules(final JLanguageTool lt, final String[] disabledRules, final String[] enabledRules, boolean useEnabledOnly) {
    // disable rules that are disabled explicitly:
    for (final String disabledRule : disabledRules) {
      lt.disableRule(disabledRule);
    }
    // enable rules
    if (enabledRules.length > 0) {
      final Set<String> enabledRuleIDs = new HashSet<>(Arrays.asList(enabledRules));
      for (String ruleName : enabledRuleIDs) {
        lt.enableDefaultOffRule(ruleName);
        lt.enableRule(ruleName);
      }
      // disable all rules except those enabled explicitly, if any:
      if (useEnabledOnly) {
        List<String> rulesToBeDisabled = new ArrayList<>();
        for (Rule rule : lt.getAllRules()) {
          if (!enabledRuleIDs.contains(rule.getId())) {
            rulesToBeDisabled.add(rule.getId());
          }
        }
        lt.disableRules(rulesToBeDisabled);
      }
    }
  }

}
