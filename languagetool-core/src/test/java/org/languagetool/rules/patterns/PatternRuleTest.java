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
package org.languagetool.rules.patterns;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.languagetool.*;
import org.languagetool.rules.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * @author Daniel Naber
 */
public class PatternRuleTest extends AbstractPatternRuleTest {

  // A test sentence should only be a single sentence - if that's not the case it can
  // happen that rules are checked as being correct that in reality will never match.
  // This check prints a warning for affected rules, but it's disabled by default because
  // it makes the tests very slow:
  private static final boolean CHECK_WITH_SENTENCE_SPLITTING = false;
  private static final Comparator<Match> MATCH_COMPARATOR = Comparator.comparingInt(Match::getTokenRef);

  static class PatternRuleTestFailure extends Exception {
    private final AbstractPatternRule rule;
    private final String message;

    public PatternRuleTestFailure(AbstractPatternRule rule, String message) {
      this.rule = rule;
      this.message = message;
    }

    @Override
    public String getMessage() {
      return String.format("Test failure for rule %s in file %s: %s",
        rule.getFullId(), rule.getSourceFile(), message);
    }

  }

  public void testFake() {
    // there's no test here - the languages are supposed to extend this class and call runGrammarRulesFromXmlTest() 
  }

  // for calling PatternRuleTest.main(), e.g. from scripts; allow to check and fail at the end
  static class PatternRuleErrorCollector extends ErrorCollector {
    public void check() throws Throwable {
      verify();
    }
  }

  @org.junit.Rule
  public final PatternRuleErrorCollector ruleErrors = new PatternRuleErrorCollector();

  @Test
  public void testSupportsLanguage() {
    FakeLanguage fakeLanguage1 = new FakeLanguage("yy");
    FakeLanguage fakeLanguage2 = new FakeLanguage("zz");
    PatternRule patternRule1 = new PatternRule("ID", fakeLanguage1, Collections.emptyList(), "", "", "");
    assertTrue(patternRule1.supportsLanguage(fakeLanguage1)); 
    assertFalse(patternRule1.supportsLanguage(fakeLanguage2));
    FakeLanguage fakeLanguage1WithVariant1 = new FakeLanguage("zz", "VAR1");
    FakeLanguage fakeLanguage1WithVariant2 = new FakeLanguage("zz", "VAR2");
    PatternRule patternRuleVariant1 = new PatternRule("ID", fakeLanguage1WithVariant1, Collections.emptyList(), "", "", "");
    assertTrue(patternRuleVariant1.supportsLanguage(fakeLanguage1WithVariant1));    
    assertFalse(patternRuleVariant1.supportsLanguage(fakeLanguage1));
    assertFalse(patternRuleVariant1.supportsLanguage(fakeLanguage2));
    assertFalse(patternRuleVariant1.supportsLanguage(fakeLanguage1WithVariant2));
  }

  /**
   * To be called from language modules. Languages.get() knows only the languages that's in the classpath.
   * @param ignoredLanguage ignore this language - useful to speed up tests from languages that 
   *                        have another language as a dependency
   */
  protected void runGrammarRulesFromXmlTest(Language ignoredLanguage) throws IOException {
    int count = 0;
    for (Language lang : Languages.get()) {
      if (ignoredLanguage.getShortCodeWithCountryAndVariant().equals(lang.getShortCodeWithCountryAndVariant())) {
        continue;
      }
      runGrammarRuleForLanguage(lang);
      count++;
    }
    if (count == 0) {
      System.err.println("Warning: no languages found in classpath - cannot run any grammar rule tests");
    }
  }
  
  /**
   * To be called from language modules. Languages.get() only knows the languages that are in the classpath,
   * and that's only the demo language for languagetool-core.
   */
  protected void runGrammarRulesFromXmlTest() throws IOException {
    for (Language lang : Languages.get()) {
        runGrammarRuleForLanguage(lang);
    }
    if (Languages.get().isEmpty()) {
      System.err.println("Warning: no languages found in classpath - cannot run any grammar rule tests");
    }
  }

  protected void runGrammarRuleForLanguage(Language lang) throws IOException {
    if (lang.getShortCode().equals("de")) {
      if (lang.getShortCodeWithCountryAndVariant().equals("de-DE")) {
        runTestForLanguage(lang);  // tests de-DE-AT/grammar.xml
        runTestForLanguage(Languages.getLanguageForShortCode("de"));  // tests de/grammar.xml
      } else if (lang.getShortCodeWithCountryAndVariant().equals("de-CH")) {
        runTestForLanguage(lang);
      } else{
        System.out.println("Skipping " + lang + " because only de-DE and de-CH gets tested for German (assuming there are no de-AT specific rules)");
      }
    } else {
      if (skipCountryVariant(lang)) {
        System.out.println("Skipping " + lang + " because there are no specific rules for that variant");
        return;
      }
      runTestForLanguage(lang);
    }
  }

  private void runGrammarRulesFromXmlTestIgnoringLanguages(Set<Language> ignoredLanguages) throws IOException {
    System.out.println("Known languages: " + Languages.getWithDemoLanguage());
    for (Language lang : Languages.getWithDemoLanguage()) {
      if (ignoredLanguages != null && ignoredLanguages.contains(lang)) {
        continue;
      }
      runTestForLanguage(lang);
    }
  }

  public void runTestForLanguage(Language lang) throws IOException {
    validatePatternFile(lang);
    System.out.println("Running pattern rule tests for " + lang.getName() + " (" + lang.getClass().getName() + ")... ");
    MultiThreadedJLanguageTool lt = createToolForTesting(lang);
    MultiThreadedJLanguageTool allRulesLt = new MultiThreadedJLanguageTool(lang);
    validateRuleIds(lang, allRulesLt);
    validateSentenceStartNotInMarker(allRulesLt);
    validateUnifyIgnoreAtTheStartOfUnify(allRulesLt);
    List<AbstractPatternRule> rules = getAllPatternRules(lang, lt);
    testRegexSyntax(lang, rules);
    testGrammarRulesFromXML(rules, allRulesLt, lang);
    System.out.println(rules.size() + " rules tested.");
    allRulesLt.shutdown();
    lt.shutdown();
  }

  @NotNull
  protected static MultiThreadedJLanguageTool createToolForTesting(Language lang) {
    MultiThreadedJLanguageTool lt = new MultiThreadedJLanguageTool(lang);
    if (CHECK_WITH_SENTENCE_SPLITTING) {
      disableSpellingRules(lt);
    }
    return lt;
  }

  protected void validatePatternFile(Language lang) throws IOException {
    validatePatternFile(getGrammarFileNames(lang));
  }
  
  protected void validatePatternFile(List<String> grammarFiles) throws IOException {
    XMLValidator validator = new XMLValidator();
    for (String grammarFile : grammarFiles) {
      System.out.println("Running XML validation for " + grammarFile + "...");
      String rulesDir = JLanguageTool.getDataBroker().getRulesDir();
      String ruleFilePath = rulesDir + "/" + grammarFile;
      try (InputStream xmlStream = this.getClass().getResourceAsStream(ruleFilePath)) {
        if (xmlStream == null) {
          if (!ruleFilePath.equals("/org/languagetool/rules/en/en-US/grammar-l2-de.xml") &&
              !ruleFilePath.equals("/org/languagetool/rules/en/en-US/grammar-l2-fr.xml") &&
              !ruleFilePath.equals("/org/languagetool/rules/de/de-DE/grammar.xml")
            ) {
            System.out.println("No rule file found at " + ruleFilePath + " in classpath. THIS SHOULD BE FIXED!");
          }
          continue;
        }
        // if there are multiple xml grammar files we'll prepend all unification elements 
        // from the first file to the rest of them 
        if (grammarFiles.size() > 1 && !grammarFiles.get(0).equals(grammarFile)) {
          validator.validateWithXmlSchema(rulesDir + "/" + grammarFiles.get(0), ruleFilePath, rulesDir + "/rules.xsd");
        } else {
          validator.validateWithXmlSchema(ruleFilePath, rulesDir + "/rules.xsd");
        }
      }
    }
  }

  protected void validateRuleIds(Language lang, JLanguageTool lt) {
    List<Rule> allRules = lt.getAllRules();
    Set<String> categoryIds = new HashSet<>();
    new RuleIdValidator(lang).validateUniqueness();
    for (Rule rule : allRules) {
      if (rule.getId().equalsIgnoreCase("ID")) {
        System.err.println("WARNING: " + lang.getShortCodeWithCountryAndVariant() + " has a rule with id 'ID', this should probably be changed");
      }
      if (rule.getId().startsWith("DB_")) {
        fail("Rule ID must not start with 'DB_', this prefix is reserved for internal use: " + rule.getId());
      }
      if (rule.getId().contains("[") || rule.getId().contains("]")) {
        fail("Rule ID must not contain '[...]': " + rule.getId());
      }
      if (rule.getId().length() > 79) {  // limit needed so the Grafana import script works
        fail("Rule ID too long, keep it <= 79 chars: " + rule.getId());
      }
      Category category = rule.getCategory();
      String catId = category.getId().toString();
      if (!catId.matches("[A-Z0-9_-]+") && !categoryIds.contains(catId)) {
        System.err.println("WARNING: category id '" + catId + "' doesn't match expected regexp [A-Z0-9_-]+");
        categoryIds.add(catId);
      }
    }
  }

  /*
   * A <marker> that covers the SENT_START can lead to obscure offset issues, so warn about that. 
   */
  protected void validateSentenceStartNotInMarker(JLanguageTool lt) {
    System.out.println("Check that sentence start tag is not included in <marker>....");
    List<Rule> rules = lt.getAllRules();
    for (Rule rule : rules) {
      if (rule instanceof AbstractPatternRule) {
        List<PatternToken> patternTokens = ((AbstractPatternRule) rule).getPatternTokens();
        if (patternTokens != null) {
          boolean hasExplicitMarker = patternTokens.stream().anyMatch(PatternToken::isInsideMarker);
          for (PatternToken patternToken : patternTokens) {
            if ((patternToken.isInsideMarker() || !hasExplicitMarker) && patternToken.isSentenceStart()) {
              System.err.println("WARNING: Sentence start in <marker>: " + rule.getFullId() +
                      " (hasExplicitMarker: " + hasExplicitMarker + ") - please move the <marker> so the SENT_START is not covered");
            }
          }
        }
      }
    }
  }

  /*
   * Check <unify-ignore> at the start of <unify>
   */
  protected void validateUnifyIgnoreAtTheStartOfUnify(JLanguageTool lt) {
    System.out.println("Check that <unify-ignore> is not at the start of <unify>....");
    List<Rule> rules = lt.getAllRules();
    for (Rule rule : rules) {
      if (rule instanceof AbstractPatternRule) {
        List<PatternToken> patternTokens = ((AbstractPatternRule) rule).getPatternTokens();
        if (patternTokens != null) {
          boolean hasUnify = patternTokens.stream().anyMatch(PatternToken::isUnified);
          if (hasUnify) {
            for (PatternToken patternToken : patternTokens) {
              if (patternToken.isUnified()) {
                if (patternToken.isUnificationNeutral()) {
                  String failure = "<ignore-unify> at the start of <unify> - please move the token outside of <unify>";
                  addError((AbstractPatternRule) rule, failure);
                }
                break;
              }
            }
          }
        }
      }
    }
  }
  
  private static void disableSpellingRules(JLanguageTool lt) {
    List<Rule> allRules = lt.getAllRules();
    for (Rule rule : allRules) {
      if (rule instanceof SpellingCheckRule) {
        lt.disableRule(rule.getId());
      }
    }
  }

  protected void testRegexSyntax(Language lang, List<AbstractPatternRule> rules) {
    System.out.println("Checking regexp syntax of " + rules.size() + " rules for " + lang + "...");
    for (AbstractPatternRule rule : rules) {
      // Test the rule pattern.
      /* check for useless 'marker' elements commented out - too slow to always run:
      PatternRuleXmlCreator creator = new PatternRuleXmlCreator();
      String xml = creator.toXML(rule.getPatternRuleId(), lang);
      if (PATTERN_MARKER_START.matcher(xml).matches() && PATTERN_MARKER_END.matcher(xml).matches()) {
        System.err.println("WARNING " + lang + ": useless <marker>: " + rule.getFullId());
      }*/

      // too aggressive for now:
      //PatternTestTools.failIfWhitespaceInToken(rule.getPatternTokens(), rule, lang);

      PatternTestTools.warnIfRegexpSyntaxNotKosher(rule.getPatternTokens(),
              rule.getId(), rule.getSubId(), lang);

      // Test the rule antipatterns.
      List<DisambiguationPatternRule> antiPatterns = rule.getAntiPatterns();
      for (DisambiguationPatternRule antiPattern : antiPatterns) {
        PatternTestTools.warnIfRegexpSyntaxNotKosher(antiPattern.getPatternTokens(),
                antiPattern.getId(), antiPattern.getSubId(), lang);
      }
    }
  }

  private void addError(AbstractPatternRule rule, String failure) {
    synchronized (ruleErrors) {
      ruleErrors.addError(new PatternRuleTestFailure(rule, failure));
    }
  }

  protected void testGrammarRulesFromXML(List<AbstractPatternRule> rules, JLanguageTool allRulesLt, Language lang) {
    if (System.getProperty("skipRules") != null) {
      System.out.println("SKIPPING: Checking example sentences of " + rules.size() + " rules for " + lang + "...");
      return;
    } else {
      System.out.println("Checking example sentences of " + rules.size() + " rules for " + lang + "...");
    }

    int threadCount = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    try {

      List<Future<?>> futures = new ArrayList<>();
      ThreadLocal<MultiThreadedJLanguageTool> lt = ThreadLocal.withInitial(() -> createToolForTesting(lang));

      Map<String, AbstractPatternRule> complexRules = new HashMap<>();
      int skipCount = 0;
      AtomicInteger i = new AtomicInteger();
      for (AbstractPatternRule rule : rules) {
        String sourceFile = rule.getSourceFile();
        if (lang.isVariant() && sourceFile != null &&
                sourceFile.matches("/org/languagetool/rules/" + lang.getShortCode() + "/grammar.*\\.xml") &&
                !sourceFile.contains("-l2-") && !lang.getShortCodeWithCountryAndVariant().equals("de-DE-x-simple-language") &&
                !lang.getClass().getName().contains("PremiumOnly")) {
          //System.out.println("Skipping " + rule.getFullId() + " in " + sourceFile + " because we're checking a variant");
          skipCount++;
          continue;
        }

        futures.add(executor.submit(() -> {
          testCorrectSentences(lt.get(), allRulesLt, rule);
          testBadSentences(lt.get(), allRulesLt, lang, complexRules, rule);
          testErrorTriggeringSentences(lt.get(), rule);
          if (i.incrementAndGet() % 100 == 0) {
            System.out.println("Testing rule " + i + "...");
          }
          return null;
        }));
      }
      for (Future<?> task : futures) {
        try {
          task.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e);
        }
      }
      System.out.println("Skipped " + skipCount + " rules for variant language to avoid checking rules more than once");

      if (!complexRules.isEmpty()) {
        Set<String> set = complexRules.keySet();
        List<AbstractPatternRule> badRules = new ArrayList<>();
        for (String aSet : set) {
          AbstractPatternRule badRule = complexRules.get(aSet);
          if (badRule instanceof PatternRule) {
            ((PatternRule)badRule).notComplexPhrase();
            badRule.setMessage("The rule contains a phrase that never matched any incorrect example.\n" + ((PatternRule) badRule).toPatternString());
            badRules.add(badRule);
          }
        }
        if (!badRules.isEmpty()) {
          testGrammarRulesFromXML(badRules, allRulesLt, lang);
        }
      }

    } finally {
      executor.shutdown();
    }

  }

  private void testBadSentences(JLanguageTool lt, JLanguageTool allRulesLt, Language lang,
                                Map<String, AbstractPatternRule> complexRules, AbstractPatternRule rule) throws IOException {
    List<IncorrectExample> badSentences = rule.getIncorrectExamples();
    if (badSentences.isEmpty()) {
      addError(rule, "No incorrect examples found.");
      return;
    }
    // necessary for XML Pattern rules containing <or>
    List<AbstractPatternRule> rules = allRulesLt.getPatternRulesByIdAndSubId(rule.getId(), rule.getSubId());
    for (IncorrectExample origBadExample : badSentences) {
      // enable indentation use
      String origBadSentence = origBadExample.getExample().replaceAll("[\\n\\t]+", "");
      List<String> expectedCorrections = origBadExample.getCorrections();
      int expectedMatchStart = origBadSentence.indexOf("<marker>");
      int expectedMatchEnd = origBadSentence.indexOf("</marker>") - "<marker>".length();
      if (expectedMatchStart == -1 || expectedMatchEnd == -1) {
        addError(rule, "No error position markup ('<marker>...</marker>') in bad example.");
        continue;
      }
      String badSentence = ExampleSentence.cleanMarkersInExample(origBadSentence);
      if (!(badSentence.trim().length() > 0)) {
          addError(rule, "Empty incorrect example sentence after cleaning/trimming.");
          continue;
      }
      
      String marker = origBadSentence.substring(expectedMatchStart+"<marker>".length(), origBadSentence.indexOf("</marker>"));
      if (marker.startsWith(", ") && origBadExample.getCorrections().stream().anyMatch(k -> !k.startsWith(" ") && !k.startsWith(",") && !k.startsWith("?") && !k.startsWith(".") && !k.startsWith(";") && !k.startsWith("…"))) {
        System.err.println("*** WARNING: " + lang.getName() + " rule " + rule.getFullId() + " removes ', ' but " +
          "doesn't have a space, comma, semicolon, or dot at the start of the suggestion: " + origBadSentence + " => " + origBadExample.getCorrections());
      }

      // necessary for XML Pattern rules containing <or>
      List<RuleMatch> matches = new ArrayList<>();
      for (Rule auxRule : rules) {
        if (lang.getShortCode().matches("gl|eo|br|ca|zh")) {
          // this is less strict, getMatchesForText() should be used. Language maintainers
          // should make sure their tests work even when in the strict mode:
          matches.addAll(getMatchesForSingleSentence(auxRule, badSentence, lt));
        } else {
          matches.addAll(getMatchesForText(auxRule, badSentence, lt));
        }
      }

      if (rule instanceof RegexPatternRule || rule instanceof PatternRule && !((PatternRule)rule).isWithComplexPhrase()) {
        if (matches.size() != 1) {
          AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(badSentence);
          StringBuilder sb = new StringBuilder("Analyzed token readings:");
          for (AnalyzedTokenReadings atr : analyzedSentence.getTokens()) {
            sb.append(' ').append(atr);
          }
          String info = "";
          if (rule instanceof RegexPatternRule) {
            info = "\nRegexp: " + ((RegexPatternRule) rule).getPattern();
          }
          String failure = "\"" + badSentence + "\"\n"
                  + "Errors expected: 1\n"
                  + "Errors found   : " + matches.size() + "\n"
                  + "Message: " + rule.getMessage() + "\n" + sb + "\nMatches: " + matches + info;
          addError(rule, failure);
          continue;
        }

        int maxReference = 0;
        if (rule.getSuggestionMatches() != null) {
          Optional<Match> opt = rule.getSuggestionMatches().stream().max(MATCH_COMPARATOR);
          maxReference = opt.isPresent() ? opt.get().getTokenRef() : 0;
        }
        maxReference = Math.max( rule.getMessage() != null ? findLargestReference(rule.getMessage()) : 0, maxReference);
        if (rule.getPatternTokens() != null && maxReference > rule.getPatternTokens().size()) {
          System.err.println("Warning: Rule "+rule.getFullId()+" refers to token \\"+(maxReference)+" but has only "+rule.getPatternTokens().size()+" tokens.");
        }

        if (expectedMatchStart != matches.get(0).getFromPos() || expectedMatchEnd != matches.get(0).getToPos()) {
          String matchPositions = String.format("(expected match position: %d - %d, actual: %d - %d)",
            expectedMatchStart, expectedMatchEnd, matches.get(0).getFromPos(), matches.get(0).getToPos());
          addError(rule, "Incorrect match position markup " + matchPositions + " in sentence: " + badSentence);
        } else {
          // make sure suggestion is what we expect it to be
          assertSuggestions(badSentence, expectedCorrections, rule, matches);
          // make sure the suggested correction doesn't produce an error:
          if (matches.get(0).getSuggestedReplacements().size() > 0) {
            int fromPos = matches.get(0).getFromPos();
            int toPos = matches.get(0).getToPos();
            for (String replacement : matches.get(0).getSuggestedReplacements()) {
              String fixedSentence = badSentence.substring(0, fromPos)
                  + replacement + badSentence.substring(toPos);
              matches = getMatchesForText(rule, fixedSentence, lt);
              if (matches.size() > 0) {
                  addError(rule, "Incorrect input:\n"
                                 + "  " + badSentence
                                 + "\nCorrected sentence:\n"
                                 + "  " + fixedSentence
                                 + "\nThe correction triggered an error itself:\n"
                                 + "  " + matches.get(0) + "\n");
              }
            }
          }
        }
      } else { // for multiple rules created with complex phrases
        matches = getMatchesForText(rule, badSentence, lt);
        if (matches.isEmpty()
            && !complexRules.containsKey(rule.getId() + badSentence)) {
          complexRules.put(rule.getId() + badSentence, rule);
        }

        if (matches.size() != 0) {
          complexRules.put(rule.getId() + badSentence, null);
          if (matches.size() != 1) {
            addError(rule, "Did expect one error in: \"" + badSentence
                           + "\" , got " + matches.size());
          } else if (expectedMatchStart != matches.get(0).getFromPos() || expectedMatchEnd != matches.get(0).getToPos()) {
            String matchPositions = String.format("(expected match position: %d - %d, actual: %d - %d)",
              expectedMatchStart, expectedMatchEnd, matches.get(0).getFromPos(), matches.get(0).getToPos());
            addError(rule, "Incorrect match position markup " + matchPositions + "in sentence: " + badSentence);
          } else {
            assertSuggestions(badSentence, expectedCorrections, rule, matches);
            assertSuggestionsDoNotCreateErrors(badSentence, lt, rule, matches);
          }
        }
      }

      // check for overlapping rules
      /*matches = getMatches(rule, badSentence, lt);
      List<RuleMatch> matchesAllRules = allRulesLt.check(badSentence);
      for (RuleMatch match : matchesAllRules) {
        if (!match.getRule().getId().equals(rule.getId()) && !matches.isEmpty()
            && rangeIsOverlapping(matches.get(0).getFromPos(), matches.get(0).getToPos(), match.getFromPos(), match.getToPos()))
          System.err.println("WARNING: " + lang.getShortCode() + ": '" + badSentence + "' in "
                  + rule.getId() + " also matched " + match.getRule().getId());
      }*/
    }
  }

  private int findLargestReference (String message) {
    Pattern pattern = Pattern.compile("\\\\[0-9]+");
    Matcher matcher = pattern.matcher(message);
    int max = 0;
    while (matcher.find()) {
      max = Math.max(max, Integer.parseInt(matcher.group().replace("\\", "")));
    }
    return max;
  }

  private void testErrorTriggeringSentences(JLanguageTool lt, AbstractPatternRule rule) throws IOException {
    for (ErrorTriggeringExample example : rule.getErrorTriggeringExamples()) {
      String sentence = cleanXML(example.getExample());
      List<RuleMatch> matches = getMatchesForText(rule, sentence, lt);
      if (matches.isEmpty()) {
        addError(rule,
          "Example sentence marked with 'triggers_error' didn't actually trigger an error: '" + sentence + "'");
      }
    }
  }

  /**
   * returns true if [a, b] has at least one number in common with [x, y]
   */
  private boolean rangeIsOverlapping(int a, int b, int x, int y) {
    if (a < x) {
      return x <= b;
    } else {
      return a <= y;
    }
  }

  private void assertSuggestions(String sentence, List<String> expectedCorrections, AbstractPatternRule rule, List<RuleMatch> matches) {
    if (!expectedCorrections.isEmpty()) {
      boolean expectedNonEmptyCorrection = expectedCorrections.get(0).length() > 0;
      if (expectedNonEmptyCorrection) {
        if (!(rule.getMessage().contains("<suggestion>") || rule.getSuggestionsOutMsg().contains("<suggestion>"))
            && rule.getFilter() == null) {
          addError(rule,
              "You specified a correction, but your message has no suggestions. rule.getMessage(): " + rule.getMessage()
                  + ", rule.getSuggestionsOutMsg(): " + rule.getSuggestionsOutMsg() + ", rule.getFullId():"
                  + rule.getFullId());
        }
      }
      List<String> realSuggestions = matches.get(0).getSuggestedReplacements();
      if (realSuggestions.isEmpty()) {
        boolean expectedEmptyCorrection = expectedCorrections.size() == 1 && expectedCorrections.get(0).length() == 0;
        if (!expectedEmptyCorrection) {
          addError(rule, "Incorrect suggestions: Expected '"
                         + expectedCorrections + "', got  " + " <no suggestion> on input: '" + sentence + "'");
        }
      } else {
        if (!expectedCorrections.equals(realSuggestions)) {
          addError(rule, "Incorrect suggestions: Expected '" + String.join("|", expectedCorrections) + "', got: '"
                       + String.join("|", realSuggestions) + "' on input: '" + sentence + "'");
        }
      }
    }
  }

  private void assertSuggestionsDoNotCreateErrors(String badSentence, JLanguageTool lt, AbstractPatternRule rule, List<RuleMatch> matches) throws IOException {
    if (matches.get(0).getSuggestedReplacements().size() > 0) {
      int fromPos = matches.get(0).getFromPos();
      int toPos = matches.get(0).getToPos();
      for (String replacement : matches.get(0).getSuggestedReplacements()) {
        String fixedSentence = badSentence.substring(0, fromPos)
            + replacement + badSentence.substring(toPos);
        List<RuleMatch> tempMatches = getMatchesForText(rule, fixedSentence, lt);
        if (0 != tempMatches.size()) {
          addError(rule,
            "Corrected sentence for rule " + rule.getFullId() + " triggered error: " + fixedSentence);
        }
      }
    }
  }

  private void testCorrectSentences(JLanguageTool lt, JLanguageTool allRulesLt,
                                    AbstractPatternRule rule) throws IOException {
    List<CorrectExample> goodSentences = rule.getCorrectExamples();
    // necessary for XML Pattern rules containing <or>
    List<AbstractPatternRule> rules = allRulesLt.getPatternRulesByIdAndSubId(rule.getId(), rule.getSubId());
    for (CorrectExample goodSentenceObj : goodSentences) {
      // enable indentation use
      String goodSentence = goodSentenceObj.getExample().replaceAll("[\\n\\t]+", "");
      goodSentence = cleanXML(goodSentence);
      if (!(goodSentence.trim().length() > 0)) {
        addError(rule, "Empty correct example.");
        continue;
      }
      boolean isMatched = false;
      // necessary for XML Pattern rules containing <or>
      for (Rule auxRule : rules) {
        isMatched = isMatched || match(auxRule, goodSentence, lt);
      }
      if (isMatched) {
        AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(goodSentence);
        StringBuilder sb = new StringBuilder("Analyzed token readings:");
        for (AnalyzedTokenReadings atr : analyzedSentence.getTokens()) {
          sb.append(' ').append(atr);
        }
        String failure = "Did not expect error in:\n" +
          "  " + goodSentence + "\n" +
          "  " + sb + "\n";
        addError(rule, failure);
      }
      // avoid matches with all the *other* rules:
      /*
      List<RuleMatch> matches = allRulesLt.check(goodSentence);
      for (RuleMatch match : matches) {
        System.err.println("WARNING: " + lang.getShortCode() + ": '" + goodSentence + "' did not match "
                + rule.getId() + " but matched " + match.getRule().getId());
      }
      */
    }
  }

  protected String cleanXML(String str) {
    return str.replaceAll("<([^<].*?)>", "");
  }

  private boolean match(Rule rule, String sentence, JLanguageTool lt) throws IOException {
    List<AnalyzedSentence> analyzedSentences = lt.analyzeText(sentence);
    int matchCount = 0;
    for (AnalyzedSentence analyzedSentence : analyzedSentences) {
    RuleMatch[] matches = rule.match(analyzedSentence);
      matchCount += matches.length;
    }
    return matchCount > 0;
  }

  // Unlike getMatchesForSingleSentence() this splits the text at sentence boundaries
  private List<RuleMatch> getMatchesForText(Rule rule, String sentence, JLanguageTool lt) throws IOException {
    List<AnalyzedSentence> analyzedSentences = lt.analyzeText(sentence);
    List<RuleMatch> matches = new ArrayList<>();
    int matchOffset = 0;
    // fix offset calculation for testCorrectSentences / testBadSentences (e.g. position of marker)
    for (AnalyzedSentence analyzedSentence : analyzedSentences) {
      List<RuleMatch> sentenceMatches = Arrays.asList(rule.match(analyzedSentence));
      for (RuleMatch match : sentenceMatches) {
        match.setOffsetPosition(match.getFromPos() + matchOffset, match.getToPos() + matchOffset);
      }
      matches.addAll(sentenceMatches);
      matchOffset += analyzedSentence.getText().length();
    }
    return matches;
  }

  private List<RuleMatch> getMatchesForSingleSentence(Rule rule, String sentence, JLanguageTool lt) throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(sentence);
    RuleMatch[] matches = rule.match(analyzedSentence);
    if (CHECK_WITH_SENTENCE_SPLITTING) {
      // "real check" with sentence splitting:
      for (Rule r : lt.getAllActiveRules()) {
        lt.disableRule(r.getId());
      }
      lt.enableRule(rule.getId());
      List<RuleMatch> realMatches = lt.check(sentence);
      List<String> realMatchRuleIds = new ArrayList<>();
      for (RuleMatch realMatch : realMatches) {
        realMatchRuleIds.add(realMatch.getRule().getId());
      }
      for (RuleMatch match : matches) {
        String ruleId = match.getRule().getId();
        if (!match.getRule().isDefaultOff() && !realMatchRuleIds.contains(ruleId)) {
          System.err.println("WARNING: " + lt.getLanguage().getName()
                  + ": missing rule match " + ruleId + " when splitting sentences for test sentence '" + sentence + "'");
        }
      }
    }
    return Arrays.asList(matches);
  }

  protected PatternRule makePatternRule(String s, boolean caseSensitive, boolean regex) {
    List<PatternToken> patternTokens = new ArrayList<>();
    String[] parts = s.split(" ");
    boolean pos = false;
    PatternToken pToken;
    for (String element : parts) {
      if (element.equals(JLanguageTool.SENTENCE_START_TAGNAME)) {
        pos = true;
      }
      if (!pos) {
        pToken = new PatternToken(element, caseSensitive, regex, false);
      } else {
        pToken = new PatternToken("", caseSensitive, regex, false);
      }
      if (pos) {
        pToken.setPosToken(new PatternToken.PosToken(element, false, false));
      }
      patternTokens.add(pToken);
      pos = false;
    }
    return new PatternRule("ID1", TestTools.getDemoLanguage(), patternTokens,
        "test rule", "user visible message", "short comment");
  }

  /**
   * Test XML patterns, as a help for people developing rules that are not
   * programmers.
   */
  public static void main(String[] args) throws Throwable {
    PatternRuleTest test = new PatternRuleTest();
    System.out.println("Running XML pattern tests...");
    System.out.println("LanguageTool version " + JLanguageTool.VERSION + " (" + JLanguageTool.BUILD_DATE + ", " + JLanguageTool.GIT_SHORT_ID + ")");
    if (args.length == 0) {
      test.runGrammarRulesFromXmlTestIgnoringLanguages(null);
    } else {
      Set<Language> ignoredLanguages = TestTools.getLanguagesExcept(args);
      test.runGrammarRulesFromXmlTestIgnoringLanguages(ignoredLanguages);
    }
    test.ruleErrors.check();
    System.out.println("Tests finished!");
  }

}
