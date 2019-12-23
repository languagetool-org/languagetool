/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.en;

import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.language.English;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Example;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.SuggestedReplacement;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.synthesis.en.EnglishSynthesizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class AbstractEnglishSpellerRule extends MorfologikSpellerRule {

  private static final EnglishSynthesizer synthesizer = new EnglishSynthesizer(new English());

  public AbstractEnglishSpellerRule(ResourceBundle messages, Language language) throws IOException {
    this(messages, language, null, Collections.emptyList());
  }

  /**
   * @since 4.4
   */
  public AbstractEnglishSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    this(messages, language, userConfig, altLanguages, null);
  }

  protected static Map<String,String> loadWordlist(String path, int column) {
    if (column != 0 && column != 1) {
      throw new IllegalArgumentException("Only column 0 and 1 are supported: " + column);
    }
    Map<String,String> words = new HashMap<>();
    try (
      InputStreamReader isr = new InputStreamReader(JLanguageTool.getDataBroker().getFromResourceDirAsStream(path), StandardCharsets.UTF_8);
      BufferedReader br = new BufferedReader(isr);
    ) {
      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() ||  line.startsWith("#")) {
          continue;
        }
        String[] parts = line.split(";");
        if (parts.length != 2) {
          throw new IOException("Unexpected format in " + path + ": " + line + " - expected two parts delimited by ';'");
        }
        words.put(parts[column], parts[column == 1 ? 0 : 1]);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return words;
  }


  /**
   * @since 4.5
   * optional: language model for better suggestions
   */
  @Experimental
  public AbstractEnglishSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig,
                                    List<Language> altLanguages, LanguageModel languageModel) throws IOException {
    super(messages, language, userConfig, altLanguages, languageModel);
    super.ignoreWordsWithLength = 1;
    setCheckCompound(true);
    addExamplePair(Example.wrong("This <marker>sentenc</marker> contains a spelling mistake."),
                   Example.fixed("This <marker>sentence</marker> contains a spelling mistake."));
    String languageSpecificIgnoreFile = getSpellingFileName().replace(".txt", "_"+language.getShortCodeWithCountryAndVariant()+".txt");
    for (String ignoreWord : wordListLoader.loadWords(languageSpecificIgnoreFile)) {
      addIgnoreWords(ignoreWord);
    }
  }

  @Override
  protected List<RuleMatch> getRuleMatches(String word, int startPos, AnalyzedSentence sentence, List<RuleMatch> ruleMatchesSoFar, int idx, AnalyzedTokenReadings[] tokens) throws IOException {
    List<RuleMatch> ruleMatches = super.getRuleMatches(word, startPos, sentence, ruleMatchesSoFar, idx, tokens);
    if (ruleMatches.size() > 0) {
      // so 'word' is misspelled:
      IrregularForms forms = getIrregularFormsOrNull(word);
      if (forms != null) {
        String message = "Possible spelling mistake. Did you mean <suggestion>" + forms.forms.get(0) +
                "</suggestion>, the " + forms.formName + " form of the " + forms.posName +
                " '" + forms.baseform + "'?";
        addFormsToFirstMatch(message, sentence, ruleMatches, forms.forms);
      } else {
        VariantInfo variantInfo = isValidInOtherVariant(word);
        if (variantInfo != null) {
          String message = "Possible spelling mistake. '" + word + "' is " + variantInfo.getVariantName() + ".";
          replaceFormsOfFirstMatch(message, sentence, ruleMatches, variantInfo.otherVariant());
        }
      }
    }
    return ruleMatches;
  }

  /**
   * @since 4.5
   */
  @Nullable
  protected VariantInfo isValidInOtherVariant(String word) {
    return null;
  }

  private void addFormsToFirstMatch(String message, AnalyzedSentence sentence, List<RuleMatch> ruleMatches, List<String> forms) {
    // recreating match, might overwrite information by SuggestionsRanker;
    // this has precedence
    RuleMatch oldMatch = ruleMatches.get(0);
    RuleMatch newMatch = new RuleMatch(this, sentence, oldMatch.getFromPos(), oldMatch.getToPos(), message);
    List<String> allSuggestions = new ArrayList<>(forms);
    for (String repl : oldMatch.getSuggestedReplacements()) {
      if (!allSuggestions.contains(repl)) {
        allSuggestions.add(repl);
      }
    }
    newMatch.setSuggestedReplacements(allSuggestions);
    ruleMatches.set(0, newMatch);
  }

  private void replaceFormsOfFirstMatch(String message, AnalyzedSentence sentence, List<RuleMatch> ruleMatches, String suggestion) {
    // recreating match, might overwrite information by SuggestionsRanker;
    // this has precedence
    RuleMatch oldMatch = ruleMatches.get(0);
    RuleMatch newMatch = new RuleMatch(this, sentence, oldMatch.getFromPos(), oldMatch.getToPos(), message);
    SuggestedReplacement sugg = new SuggestedReplacement(suggestion);
    sugg.setShortDescription(language.getName());
    newMatch.setSuggestedReplacementObjects(Collections.singletonList(sugg));
    ruleMatches.set(0, newMatch);
  }

  @SuppressWarnings({"ReuseOfLocalVariable", "ControlFlowStatementWithoutBraces"})
  @Nullable
  private IrregularForms getIrregularFormsOrNull(String word) {
    IrregularForms irregularFormsOrNull = getIrregularFormsOrNull(word, "ed", Arrays.asList("ed"), "VBD", "verb", "past tense");
    if (irregularFormsOrNull != null) return irregularFormsOrNull;
    irregularFormsOrNull = getIrregularFormsOrNull(word, "ed", Arrays.asList("d" /* e.g. awaked */), "VBD", "verb", "past tense");
    if (irregularFormsOrNull != null) return irregularFormsOrNull;
    irregularFormsOrNull = getIrregularFormsOrNull(word, "s", Arrays.asList("s"), "NNS", "noun", "plural");
    if (irregularFormsOrNull != null) return irregularFormsOrNull;
    irregularFormsOrNull = getIrregularFormsOrNull(word, "es", Arrays.asList("es"/* e.g. 'analysises' */), "NNS", "noun", "plural");
    if (irregularFormsOrNull != null) return irregularFormsOrNull;
    irregularFormsOrNull = getIrregularFormsOrNull(word, "er", Arrays.asList("er"/* e.g. 'farer' */), "JJR", "adjective", "comparative");
    if (irregularFormsOrNull != null) return irregularFormsOrNull;
    irregularFormsOrNull = getIrregularFormsOrNull(word, "est", Arrays.asList("est"/* e.g. 'farest' */), "JJS", "adjective", "superlative");
    return irregularFormsOrNull;
  }

  @Nullable
  private IrregularForms getIrregularFormsOrNull(String word, String wordSuffix, List<String> suffixes, String posTag, String posName, String formName) {
    try {
      for (String suffix : suffixes) {
        if (word.endsWith(wordSuffix)) {
          String baseForm = word.substring(0, word.length() - suffix.length());
          String[] forms = synthesizer.synthesize(new AnalyzedToken(word, null, baseForm), posTag);
          List<String> result = new ArrayList<>();
          for (String form : forms) {
            if (!speller1.isMisspelled(form)) {
              // only accept suggestions that the spellchecker will accept
              result.add(form);
            }
          }
          // the internal dict might contain forms that the spell checker doesn't accept (e.g. 'criterions'),
          // but we trust the spell checker in this case:
          result.remove(word);
          result.remove("badder");  // non-standard usage
          result.remove("baddest");  // non-standard usage
          result.remove("spake");  // can be removed after dict update
          if (result.size() > 0) {
            return new IrregularForms(baseForm, posName, formName, result);
          }
        }
      }
      return null;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @since 2.7
   */
  @Override
  protected List<String> getAdditionalTopSuggestions(List<String> suggestions, String word) throws IOException {
    if ("Alot".equals(word)) {
      return Arrays.asList("A lot");
    } else if ("acc".equals(word)) {
      return Arrays.asList("account", "accusative");
    } else if ("Acc".equals(word)) {
      return Arrays.asList("Account", "Accusative");
    } else if ("cmon".equals(word)) {
      return Arrays.asList("c'mon");
    } else if ("Cmon".equals(word)) {
      return Arrays.asList("C'mon");
    } else if ("alot".equals(word)) {
      return Arrays.asList("a lot");
    } else if ("da".equals(word)) {
      return Arrays.asList("the");
    } else if ("Da".equals(word)) {
      return Arrays.asList("The");
    } else if ("errornous".equals(word)) {
      return Arrays.asList("erroneous");
    } else if ("brang".equals(word) || "brung".equals(word)) {
      return Arrays.asList("brought");
    } else if ("thru".equals(word)) {
      return Arrays.asList("through");
    } else if ("speach".equals(word)) {  // the replacement pairs would prefer "speak"
      return Arrays.asList("speech");
    } else if ("icecreem".equals(word)) {
      return Arrays.asList("ice cream");
    } else if ("math".equals(word)) { // in en-gb it's 'maths'
      return Arrays.asList("maths");
    } else if ("fora".equals(word)) {
      return Arrays.asList("for a");
    } else if ("lotsa".equals(word)) {
      return Arrays.asList("lots of");
    } else if ("coulda".equals(word)) {
      return Arrays.asList("could have");
    } else if ("shoulda".equals(word)) {
      return Arrays.asList("should have");
    } else if ("woulda".equals(word)) {
      return Arrays.asList("would have");
    } else if ("tellem".equals(word)) {
      return Arrays.asList("tell them");
    } else if ("Tellem".equals(word)) {
      return Arrays.asList("Tell them");
    } else if ("afro-american".equalsIgnoreCase(word)) {
      return Arrays.asList("Afro-American");
    } else if ("Webex".equals(word)) {
      return Arrays.asList("WebEx");
    } else if ("didint".equals(word)) {
      return Arrays.asList("didn't");
    } else if ("Didint".equals(word)) {
      return Arrays.asList("Didn't");
    } else if ("wasint".equals(word)) {
      return Arrays.asList("wasn't");
    } else if ("hasint".equals(word)) {
      return Arrays.asList("hasn't");
    } else if ("doesint".equals(word)) {
      return Arrays.asList("doesn't");
    } else if ("ist".equals(word)) {
      return Arrays.asList("is");
    } else if ("Boing".equals(word)) {
      return Arrays.asList("Boeing");
    } else if ("te".equals(word)) {
      return Arrays.asList("the");
    } else if ("todays".equals(word)) {
      return Arrays.asList("today's");
    } else if ("Todays".equals(word)) {
      return Arrays.asList("Today's");
    } else if ("todo".equals(word)) {
      return Arrays.asList("to-do", "to do");
    } else if ("Todo".equals(word)) {
      return Arrays.asList("To-do", "To do");
    } else if ("heres".equals(word)) {
      return Arrays.asList("here's");
    } else if ("Heres".equals(word)) {
      return Arrays.asList("Here's");
    } else if ("McDonalds".equals(word)) {
      return Arrays.asList("McDonald's");
    } else if ("ux".equals(word)) {
      return Arrays.asList("UX");
    } else if ("ive".equals(word)) {
      return Arrays.asList("I've");
    } else if ("infos".equals(word)) {
      return Arrays.asList("informations");
    } else if ("Infos".equals(word)) {
      return Arrays.asList("Informations");
    } else if ("prios".equals(word)) {
      return Arrays.asList("priorities");
    } else if ("Prio".equals(word)) {
      return Arrays.asList("Priority");
    } else if ("prio".equals(word)) {
      return Arrays.asList("Priority");
    } else if ("esport".equals(word)) {
      return Arrays.asList("e-sport");
    } else if ("Esport".equals(word)) {
      return Arrays.asList("E-Sport");
    } else if ("eSport".equals(word)) {
      return Arrays.asList("e-sport");
    } else if ("esports".equals(word)) {
      return Arrays.asList("e-sports");
    } else if ("Esports".equals(word)) {
      return Arrays.asList("E-Sports");
    } else if ("eSports".equals(word)) {
      return Arrays.asList("e-sports");
    } else if ("ecommerce".equals(word)) {
      return Arrays.asList("e-commerce");
    } else if ("Ecommerce".equals(word)) {
      return Arrays.asList("E-Commerce");
    } else if ("eCommerce".equals(word)) {
      return Arrays.asList("e-commerce");
    } else if ("elearning".equals(word)) {
      return Arrays.asList("e-learning");
    } else if ("eLearning".equals(word)) {
      return Arrays.asList("e-learning");
    } else if ("ebook".equals(word)) {
      return Arrays.asList("e-book");
    } else if ("ebooks".equals(word)) {
      return Arrays.asList("e-books");
    } else if ("eBook".equals(word)) {
      return Arrays.asList("e-book");
    } else if ("eBooks".equals(word)) {
      return Arrays.asList("e-books");
    } else if ("Ebook".equals(word)) {
      return Arrays.asList("E-Book");
    } else if ("Ebooks".equals(word)) {
      return Arrays.asList("E-Books");
    } else if ("R&B".equals(word)) {
      return Arrays.asList("R & B", "R 'n' B");
    } else if ("ie".equals(word)) {
      return Arrays.asList("i.e.");
    } else if ("eg".equals(word)) {
      return Arrays.asList("e.g.");
    } else if ("Thx".equals(word)) {
      return Arrays.asList("Thanks");
    } else if ("thx".equals(word)) {
      return Arrays.asList("thanks");
    } else if ("ty".equals(word)) {
      return Arrays.asList("thank you", "thanks");
    } else if ("Sry".equals(word)) {
      return Arrays.asList("Sorry");
    } else if ("sry".equals(word)) {
      return Arrays.asList("sorry");
    } else if ("im".equals(word)) {
      return Arrays.asList("I'm");
    } else if ("spoilt".equals(word)) {
      return Arrays.asList("spoiled");
    } else if ("Lil".equals(word)) {
      return Arrays.asList("Little");
    } else if ("lil".equals(word)) {
      return Arrays.asList("little");
    } else if ("Sucka".equals(word)) {
      return Arrays.asList("Sucker");
    } else if ("sucka".equals(word)) {
      return Arrays.asList("sucker");
    } else if ("whaddya".equals(word)) {
      return Arrays.asList("what are you", "what do you");
    } else if ("Whaddya".equals(word)) {
      return Arrays.asList("What are you", "What do you");
    } else if ("sinc".equals(word)) {
      return Arrays.asList("sync");
    } else if ("Hongkong".equals(word)) {
      return Arrays.asList("Hong Kong");
    } else if ("center".equals(word)) {
      // For non-US English
      return Arrays.asList("centre");
    } else if ("ur".equals(word)) {
      return Arrays.asList("your", "you are");
    } else if ("Ur".equals(word)) {
      return Arrays.asList("Your", "You are");
    } else if ("ure".equals(word)) {
      return Arrays.asList("your", "you are");
    } else if ("Ure".equals(word)) {
      return Arrays.asList("Your", "You are");
    } else if ("mins".equals(word)) {
      return Arrays.asList("minutes", "min");
    } else if ("addon".equals(word)) {
      return Arrays.asList("add-on");
    } else if ("addons".equals(word)) {
      return Arrays.asList("add-ons");
    } else if ("afterparty".equals(word)) {
      return Arrays.asList("after-party");
    } else if ("Afterparty".equals(word)) {
      return Arrays.asList("After-party");
    } else if ("wellbeing".equals(word)) {
      return Arrays.asList("well-being");
    } else if ("cuz".equals(word) || "coz".equals(word) ) {
      return Arrays.asList("because");
    } else if ("pls".equals(word)) {
      return Arrays.asList("please");
    } else if ("Pls".equals(word)) {
      return Arrays.asList("Please");
    } else if ("prio".equals(word)) {
      return Arrays.asList("priority");
    } else if ("prios".equals(word)) {
      return Arrays.asList("priorities");
    } else if ("gmail".equals(word)) {
      return Arrays.asList("Gmail");
      // AtD irregular plurals - START
    } else if ("addendums".equals(word)) {
      return Arrays.asList("addenda");
    } else if ("algas".equals(word)) {
      return Arrays.asList("algae");
    } else if ("alumnas".equals(word)) {
      return Arrays.asList("alumnae");
    } else if ("alumnuses".equals(word)) {
      return Arrays.asList("alumni");
    } else if ("analysises".equals(word)) {
      return Arrays.asList("analyses");
    } else if ("appendixs".equals(word)) {
      return Arrays.asList("appendices");
    } else if ("axises".equals(word)) {
      return Arrays.asList("axes");
    } else if ("bacilluses".equals(word)) {
      return Arrays.asList("bacilli");
    } else if ("bacteriums".equals(word)) {
      return Arrays.asList("bacteria");
    } else if ("basises".equals(word)) {
      return Arrays.asList("bases");
    } else if ("beaus".equals(word)) {
      return Arrays.asList("beaux");
    } else if ("bisons".equals(word)) {
      return Arrays.asList("bison");
    } else if ("buffalos".equals(word)) {
      return Arrays.asList("buffaloes");
    } else if ("calfs".equals(word)) {
      return Arrays.asList("calves");
    } else if ("childs".equals(word)) {
      return Arrays.asList("children");
    } else if ("crisises".equals(word)) {
      return Arrays.asList("crises");
    } else if ("criterions".equals(word)) {
      return Arrays.asList("criteria");
    } else if ("curriculums".equals(word)) {
      return Arrays.asList("curricula");
    } else if ("datums".equals(word)) {
      return Arrays.asList("data");
    } else if ("deers".equals(word)) {
      return Arrays.asList("deer");
    } else if ("diagnosises".equals(word)) {
      return Arrays.asList("diagnoses");
    } else if ("echos".equals(word)) {
      return Arrays.asList("echoes");
    } else if ("elfs".equals(word)) {
      return Arrays.asList("elves");
    } else if ("ellipsises".equals(word)) {
      return Arrays.asList("ellipses");
    } else if ("embargos".equals(word)) {
      return Arrays.asList("embargoes");
    } else if ("erratums".equals(word)) {
      return Arrays.asList("errata");
    } else if ("firemans".equals(word)) {
      return Arrays.asList("firemen");
    } else if ("fishs".equals(word)) {
      return Arrays.asList("fishes", "fish");
    } else if ("genuses".equals(word)) {
      return Arrays.asList("genera");
    } else if ("gooses".equals(word)) {
      return Arrays.asList("geese");
    } else if ("halfs".equals(word)) {
      return Arrays.asList("halves");
    } else if ("heros".equals(word)) {
      return Arrays.asList("heroes");
    } else if ("indexs".equals(word)) {
      return Arrays.asList("indices", "indexes");
    } else if ("lifes".equals(word)) {
      return Arrays.asList("lives");
    } else if ("mans".equals(word)) {
      return Arrays.asList("men");
    } else if ("matrixs".equals(word)) {
      return Arrays.asList("matrices");
    } else if ("meanses".equals(word)) {
      return Arrays.asList("means");
    } else if ("mediums".equals(word)) {
      return Arrays.asList("media");
    } else if ("memorandums".equals(word)) {
      return Arrays.asList("memoranda");
    } else if ("mooses".equals(word)) {
      return Arrays.asList("moose");
    } else if ("mosquitos".equals(word)) {
      return Arrays.asList("mosquitoes");
    } else if ("neurosises".equals(word)) {
      return Arrays.asList("neuroses");
    } else if ("nucleuses".equals(word)) {
      return Arrays.asList("nuclei");
    } else if ("oasises".equals(word)) {
      return Arrays.asList("oases");
    } else if ("ovums".equals(word)) {
      return Arrays.asList("ova");
    } else if ("oxs".equals(word)) {
      return Arrays.asList("oxen");
    } else if ("oxes".equals(word)) {
      return Arrays.asList("oxen");
    } else if ("paralysises".equals(word)) {
      return Arrays.asList("paralyses");
    } else if ("potatos".equals(word)) {
      return Arrays.asList("potatoes");
    } else if ("radiuses".equals(word)) {
      return Arrays.asList("radii");
    } else if ("selfs".equals(word)) {
      return Arrays.asList("selves");
    } else if ("serieses".equals(word)) {
      return Arrays.asList("series");
    } else if ("sheeps".equals(word)) {
      return Arrays.asList("sheep");
    } else if ("shelfs".equals(word)) {
      return Arrays.asList("shelves");
    } else if ("scissorses".equals(word)) {
      return Arrays.asList("scissors");
    } else if ("specieses".equals(word)) {
      return Arrays.asList("species");
    } else if ("stimuluses".equals(word)) {
      return Arrays.asList("stimuli");
    } else if ("stratums".equals(word)) {
      return Arrays.asList("strata");
    } else if ("tableaus".equals(word)) {
      return Arrays.asList("tableaux");
    } else if ("thats".equals(word)) {
      return Arrays.asList("those");
    } else if ("thesises".equals(word)) {
      return Arrays.asList("theses");
    } else if ("thiefs".equals(word)) {
      return Arrays.asList("thieves");
    } else if ("thises".equals(word)) {
      return Arrays.asList("these");
    } else if ("tomatos".equals(word)) {
      return Arrays.asList("tomatoes");
    } else if ("tooths".equals(word)) {
      return Arrays.asList("teeth");
    } else if ("torpedos".equals(word)) {
      return Arrays.asList("torpedoes");
    } else if ("vertebras".equals(word)) {
      return Arrays.asList("vertebrae");
    } else if ("vetos".equals(word)) {
      return Arrays.asList("vetoes");
    } else if ("vitas".equals(word)) {
      return Arrays.asList("vitae");
    } else if ("watchs".equals(word)) {
      return Arrays.asList("watches");
    } else if ("wifes".equals(word)) {
      return Arrays.asList("wives");
    } else if ("womans".equals(word)) {
      return Arrays.asList("women");
      // AtD irregular plurals - END
    } else if ("tippy-top".equals(word) || "tippytop".equals(word)) {
      // "tippy-top" is an often used word by Donald Trump
      return Arrays.asList("tip-top", "top most");
    } else if ("imma".equals(word)) {
      return Arrays.asList("I'm going to", "I'm a");
    } else if ("Imma".equals(word)) {
      return Arrays.asList("I'm going to", "I'm a");
    } else if ("dontcha".equals(word)) {
      return Arrays.asList("don't you");
    } else if ("Dontcha".equals(word)) {
      return Arrays.asList("don't you");
    } else if ("greatfruit".equals(word)) {
      return Arrays.asList("grapefruit", "great fruit");
    } else if (word.endsWith("ys")) {
      String suggestion = word.replaceFirst("ys$", "ies");
      if (!speller1.isMisspelled(suggestion)) {
        return Arrays.asList(suggestion);
      }
    }

    return super.getAdditionalTopSuggestions(suggestions, word);
  }

  private static class IrregularForms {
    final String baseform;
    final String posName;
    final String formName;
    final List<String> forms;
    private IrregularForms(String baseform, String posName, String formName, List<String> forms) {
      this.baseform = baseform;
      this.posName = posName;
      this.formName = formName;
      this.forms = forms;
    }
  }
}
