/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import com.google.common.base.Suppliers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;

/**
 * Abstract rule class. A Rule describes a language error and can test whether a
 * given pre-analyzed text contains that error using the {@link Rule#match(AnalyzedSentence)}
 * method.
 *
 * <p>Rules are created whenever a {@link JLanguageTool} or
 * a {@link org.languagetool.MultiThreadedJLanguageTool} object is created.
 * As these objects are not thread-safe, this can happen often. Rules should thus
 * make sure that their initialization works fast. For example, if a rule needs
 * to load data from disk, it should store it in a static variable to make sure
 * the loading happens only once.
 *
 * Rules also need to make sure their {@code match()} code is stateless, i.e. that
 * its results are not influenced by previous calls to {@code match()} (this is relevant
 * if pipeline caching is used).
 *
 * @author Daniel Naber
 */
public abstract class Rule {

  private static final Category MISC = new Category(CategoryIds.MISC, "Miscellaneous");

  protected final ResourceBundle messages;

  @Nullable
  private List<Tag> tags;

  private List<CorrectExample> correctExamples;
  private List<IncorrectExample> incorrectExamples;
  private List<ErrorTriggeringExample> errorTriggeringExamples;
  private ITSIssueType locQualityIssueType = ITSIssueType.Uncategorized;
  private Category category;
  private URL url;
  private boolean isPremium;
  private boolean defaultOff;
  private boolean defaultTempOff;
  private boolean officeDefaultOn = false;
  private boolean officeDefaultOff = false;
  private int minPrevMatches = 0; // minimum number of previous matches to show the rule
  private int distanceTokens = -1; // distance (number of tokens) between matches to consider a repetition

  public Rule() {
    this(null);
  }

  /**
   * Called by rules that require a translation of their messages.
   */
  public Rule(ResourceBundle messages) {
    this.messages = messages;
    if (messages != null) {
      setCategory(Categories.MISC.getCategory(messages));  // the default, sub classes may overwrite this
    } else {
      setCategory(MISC);
    }
  }

  /**
   * A string used to identify the rule in e.g. configuration files.
   * This string is supposed to be unique and to stay the same in all upcoming
   * versions of LanguageTool. It's supposed to contain only the characters {@code A-Z} 
   * and the underscore.
   */
  public abstract String getId();

  /**
   * Same as {@link #getId()} for Java rules. For XML rules, this can contain a numbers
   * that identifies the subrule of a rule group.
   * @since 4.9
   */
  public String getFullId() {
    return getId();
  }
  
  /**
   * A short description of the error this rule can detect, usually in the language of the text
   * that is checked.
   */
  public abstract String getDescription();

  /**
   * Check whether the given sentence matches this error rule, i.e. whether it
   * contains the error detected by this rule. Note that the order in which
   * this method is called is not always guaranteed, i.e. the sentence order in the
   * text may be different from the order in which you get the sentences (this may be the
   * case when LanguageTool is used as a LibreOffice/OpenOffice add-on, for example).
   * In other words, implementations must be stateless, so that a previous call to
   * this method has no influence on later calls.
   *
   * @param sentence a pre-analyzed sentence
   * @return an array of {@link RuleMatch} objects
   */
  public abstract RuleMatch[] match(AnalyzedSentence sentence) throws IOException;

  /**
   * A number that estimates how many words there must be after a match before we
   * can be (relatively) sure the match is valid. This is useful for check-as-you-type,
   * where a match might occur and the word that gets typed next makes the match
   * disappear (something one would obviously like to avoid).
   * Note: this may over-estimate the real context size.
   * Returns {@code -1} when the sentence needs to end to be sure there's a match.
   * @since 4.5
   */
  public int estimateContextForSureMatch() {
    return 0;
  }
    
  /**
   * Overwrite this to avoid false alarms by ignoring these patterns -
   * note that your {@link #match(AnalyzedSentence)} method needs to
   * call {@link #getSentenceWithImmunization} for this to be used
   * and you need to check {@link AnalyzedTokenReadings#isImmunized()}
   * @since 3.1
   */
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return Collections.emptyList();
  }

  /**
   * Overwrite this to return true, if a value may be configured by option panel
   * @since 4.2
   */
  public boolean hasConfigurableValue() {
    return false;
  }

  /**
   * Overwrite this to get a default Integer value by option panel
   * @since 4.1
   */
  public int getDefaultValue() {
    return 0;
  }

  /**
   * Overwrite this to define the minimum of a configurable value
   * @since 4.2
   */
  public int getMinConfigurableValue() {
    return 0;
  }

  /**
   * Overwrite this to define the maximum of a configurable value
   * @since 4.2
   */
  public int getMaxConfigurableValue() {
    return 100;
  }

  /**
   * Overwrite this to define the Text in the option panel for the configurable value
   * @since 4.2
   */
  public String getConfigureText() {
    return "";
  }

  /**
   * To be called from {@link #match(AnalyzedSentence)} for rules that want
   * {@link #getAntiPatterns()} to be considered.
   * @since 3.1
   */
  protected AnalyzedSentence getSentenceWithImmunization(AnalyzedSentence sentence) {
    if (!getAntiPatterns().isEmpty()) {
      //we need a copy of the sentence, not reference to the old one
      AnalyzedSentence immunizedSentence = sentence.copy(sentence);
      for (DisambiguationPatternRule patternRule : getAntiPatterns()) {
        try {
          immunizedSentence = patternRule.replace(immunizedSentence);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      return immunizedSentence;
    }
    return sentence;
  }

  /**
   * Helper for implementing {@link #getAntiPatterns()}. The result of this method should better be cached, please see
   * {@link #cacheAntiPatterns} which does that.
   * @since 3.1
   */
  protected static List<DisambiguationPatternRule> makeAntiPatterns(List<List<PatternToken>> patternList, Language language) {
    List<DisambiguationPatternRule> rules = new ArrayList<>();
    for (List<PatternToken> patternTokens : patternList) {
      rules.add(new DisambiguationPatternRule("INTERNAL_ANTIPATTERN", "(no description)", language,
              patternTokens, null, null, DisambiguationPatternRule.DisambiguatorAction.IMMUNIZE));
    }
    return rules;
  }

  /**
   * @return a memoizing supplier that caches the result of {@link #makeAntiPatterns}. It makes sense
   * to store the returned value, e.g. in a field.
   * @since 5.2
   */
  protected static Supplier<List<DisambiguationPatternRule>> cacheAntiPatterns(Language language, List<List<PatternToken>> antiPatterns) {
    return Suppliers.memoize(() -> makeAntiPatterns(antiPatterns, language));
  }

  /**
   * Whether this rule can be used for text in the given language.
   * Since LanguageTool 2.6, this also works {@link org.languagetool.rules.patterns.PatternRule}s
   * (before, it used to always return {@code false} for those).
   */
  public boolean supportsLanguage(Language language) {
    try {
      List<Class<? extends Rule>> relevantRuleClasses = new ArrayList<>();
      UserConfig config = new UserConfig();
      List<Rule> relevantRules = new ArrayList<>(language.getRelevantRules(JLanguageTool.getMessageBundle(),
          config, null, Collections.emptyList()));  //  empty UserConfig has to be added to prevent null pointer exception
      relevantRules.addAll(language.getRelevantLanguageModelCapableRules(JLanguageTool.getMessageBundle(), null,
        null, config, null, Collections.emptyList()));
      for (Rule relevantRule : relevantRules) {
        relevantRuleClasses.add(relevantRule.getClass());
      }
      return relevantRuleClasses.contains(this.getClass());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Whether this is a spelling rule that uses a dictionary.
   * Rules that return {@code true} here are basically rules that work like
   * a simple hunspell-like spellchecker: they check words without considering
   * the words' context.
   * @since 2.5
   */
  public boolean isDictionaryBasedSpellingRule() {
    return false;
  }
  
  /**
   * Whether this rule should be forced to be used in LO/OO extension.
   * Rules that return {@code true} will be enabled always in LO/OO extension
   * regardless of other options like isDictionaryBasedSpellingRule().
   * @since 2.6
   */
  public boolean useInOffice() {
    return false;
  }

  /**
   * Set the examples that are correct and thus do not trigger the rule.
   */
  public final void setCorrectExamples(List<CorrectExample> correctExamples) {
    this.correctExamples = correctExamples.isEmpty() ? null : correctExamples;
  }

  /**
   * Get example sentences that are correct and thus will not match this rule.
   */
  public final List<CorrectExample> getCorrectExamples() {
    return correctExamples == null ? Collections.emptyList() : Collections.unmodifiableList(correctExamples);
  }

  /**
   * Set the examples that are incorrect and thus do trigger the rule.
   */
  public final void setIncorrectExamples(List<IncorrectExample> incorrectExamples) {
    this.incorrectExamples = incorrectExamples.isEmpty() ? null : incorrectExamples;
  }

  /**
   * Get example sentences that are incorrect and thus will match this rule.
   */
  public final List<IncorrectExample> getIncorrectExamples() {
    return incorrectExamples == null ? Collections.emptyList() : Collections.unmodifiableList(incorrectExamples);
  }

  /**
   * Set the examples that are correct but still trigger the rule due to an issue with the rule.
   * @since 3.5
   */
  public final void setErrorTriggeringExamples(List<ErrorTriggeringExample> examples) {
    this.errorTriggeringExamples = examples.isEmpty() ? null : examples;
  }

  /**
   * Get the examples that are correct but still trigger the rule due to an issue with the rule.
   * @since 3.5
   */
  public final List<ErrorTriggeringExample> getErrorTriggeringExamples() {
    return errorTriggeringExamples == null ? Collections.emptyList() : Collections.unmodifiableList(this.errorTriggeringExamples);
  }

  /**
   * @return a category (never null since LT 3.4)
   */
  @NotNull
  public Category getCategory() {
    return category;
  }

  public final void setCategory(Category category) {
    this.category = Objects.requireNonNull(category, "category cannot be null");
  }

  protected final RuleMatch[] toRuleMatchArray(List<RuleMatch> ruleMatches) {
    return ruleMatches.toArray(new RuleMatch[0]);
  }

  /**
   * Checks whether the rule has been turned off by default by the rule author.
   * @return True if the rule is turned off by default.
   */
  public final boolean isDefaultOff() {
    return defaultOff;
  }

  /**
   * Checks whether the rule has been turned off using "default='temp_off'" by default by the rule author.
   * This is a special case where the rule is off for users but active for nightly regression checks.
   */
  public final boolean isDefaultTempOff() {
    return defaultTempOff;
  }

  /**
   * Turns the rule off by default.
   */
  public final void setDefaultOff() {
    defaultOff = true;
  }

  /**
   * Turns the pattern rule off by default, expect for internal regression tests.
   */
  public final void setDefaultTempOff() {
    defaultOff = true;
    defaultTempOff = true;
  }

  /**
   * Turns the rule on by default.
   */
  public final void setDefaultOn() {
    defaultOff = false;
  }
  
  /**
   * Checks whether the rule has been turned off by default for Office Extension by the rule author.
   * @return True if the rule is turned off. Overrides the default for LO/OO.
   * @since 4.0
  */
  public final boolean isOfficeDefaultOff() {
    return officeDefaultOff;
  }

  /**
   * Checks whether the rule has been turned on by default for Office Extension by the rule author.
   * @return True if the rule is turned on. Overrides the default for LO/OO.
   * @since 4.0
   */
  public final boolean isOfficeDefaultOn() {
    return officeDefaultOn;
  }

  /**
   * Turns the rule off for Office Extension by default.
   * @since 4.0
   */
  public final void setOfficeDefaultOff() {
    officeDefaultOff = true;
  }

  /**
   * Turns the rule on for Office Extension by default.
   * @since 4.0
   */
  public final void setOfficeDefaultOn() {
    officeDefaultOn = true;
  }
  
  /**
   * An optional URL describing the rule match in more detail. Typically points to a dictionary or grammar website
   * with explanations and examples. Will return {@code null} for rules that have no URL.
   * @since 1.8
   */
  @Nullable
  public URL getUrl() {
    return url;
  }

  /**
   * @since 1.8
   * @see #getUrl()
   */
  public void setUrl(URL url) {
    this.url = url;
  }

  /**
   * Returns the Localization Quality Issue Type, as defined
   * at <a href="http://www.w3.org/International/multilingualweb/lt/drafts/its20/its20.html#lqissue-typevalues"
   * >http://www.w3.org/International/multilingualweb/lt/drafts/its20/its20.html#lqissue-typevalues</a>.
   *
   * <p>Note that not all languages nor all rules actually map yet to a type yet. In those
   * cases, <tt>uncategorized</tt> is returned.
   *
   * @return the Localization Quality Issue Type - <tt>uncategorized</tt> if no type has been assigned
   * @since 2.5
   */
  public ITSIssueType getLocQualityIssueType() {
    return locQualityIssueType;
  }

  /**
   * Set the Localization Quality Issue Type.
   * @see #getLocQualityIssueType()
   * @since 2.5
   */
  public void setLocQualityIssueType(ITSIssueType locQualityIssueType) {
    this.locQualityIssueType = Objects.requireNonNull(locQualityIssueType);
  }

  /**
   * Convenience method to add a pair of sentences: an incorrect sentence and the same sentence
   * with the error corrected.
   * @since 2.5
   */
  protected void addExamplePair(IncorrectExample incorrectSentence, CorrectExample correctSentence) {
    if (correctExamples == null) {
      correctExamples = new ArrayList<>(0);
    }
    if (incorrectExamples == null) {
      incorrectExamples = new ArrayList<>(0);
    }
    String correctExample = correctSentence.getExample();
    int markerStart= correctExample.indexOf("<marker>");
    int markerEnd = correctExample.indexOf("</marker>");
    if (markerStart != -1 && markerEnd != -1) {
      List<String> correction = Collections.singletonList(correctExample.substring(markerStart + "<marker>".length(), markerEnd));
      incorrectExamples.add(new IncorrectExample(incorrectSentence.getExample(), correction));
    } else {
      incorrectExamples.add(incorrectSentence);
    }
    correctExamples.add(correctSentence);
  }

  /**
   * Convenience method to set a pair of sentences: an incorrect sentence and the same sentence
   * with the error corrected.
   * @since 4.9
   */
  protected void setExamplePair(IncorrectExample incorrectSentence, CorrectExample correctSentence) {
    if (incorrectExamples != null) {
      incorrectExamples.clear();
    }
    if (correctSentence != null) {
      correctExamples.clear();
    }
    addExamplePair(incorrectSentence, correctSentence);
  }

  /**
   * @since 5.1
   */
  public void addTags(List<String> tags) {
    if (tags.isEmpty()) return;

    List<Tag> myTags = this.tags;
    if (myTags == null) {
      this.tags = myTags = new ArrayList<>();
    }
    for (String tag : tags) {
      if (myTags.stream().noneMatch(k -> k.name().equals(tag))) {
        myTags.add(Tag.valueOf(tag));
      }
    }
  }

  /**
   * @since 5.1
   */
  public void setTags(List<Tag> tags) {
    this.tags = tags.isEmpty() ? null : Objects.requireNonNull(tags);
  }

  /** @since 5.1 */
  @NotNull
  public List<Tag> getTags() {
    return tags == null ? Collections.emptyList() : tags;
  }

  /** @since 5.1 */
  public boolean hasTag(Tag tag) {
    return tags != null && tags.contains(tag);
  }

  public boolean isPremium() {
    return isPremium;
  }

  public void setPremium(boolean premium) {
    isPremium = premium;
  }
  
  public void setMinPrevMatches(int i) {
    minPrevMatches = i;
  }
  
  public int getMinPrevMatches() {
    return minPrevMatches;
  }
  
  public void setDistanceTokens(int i) {
    distanceTokens = i;
  }
  
  public int getDistanceTokens() {
    return distanceTokens;
  }
}
