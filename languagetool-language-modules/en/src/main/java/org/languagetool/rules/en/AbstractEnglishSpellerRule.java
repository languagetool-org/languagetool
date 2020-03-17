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
import org.languagetool.rules.en.translation.BeoLingusTranslator;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.rules.translation.Translator;
import org.languagetool.synthesis.en.EnglishSynthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractEnglishSpellerRule extends MorfologikSpellerRule {

  private static final EnglishSynthesizer synthesizer = new EnglishSynthesizer(new English());

  private final BeoLingusTranslator translator;

  public AbstractEnglishSpellerRule(ResourceBundle messages, Language language) throws IOException {
    this(messages, language, null, Collections.emptyList());
  }

  /**
   * @since 4.4
   */
  public AbstractEnglishSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    this(messages, language, null, userConfig, altLanguages, null, null);
  }

  protected static Map<String,String> loadWordlist(String path, int column) {
    if (column != 0 && column != 1) {
      throw new IllegalArgumentException("Only column 0 and 1 are supported: " + column);
    }
    Map<String,String> words = new HashMap<>();
    List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(path);
    for (String line : lines) {
      line = line.trim();
      if (line.isEmpty() ||  line.startsWith("#")) {
        continue;
      }
      String[] parts = line.split(";");
      if (parts.length != 2) {
        throw new RuntimeException("Unexpected format in " + path + ": " + line + " - expected two parts delimited by ';'");
      }
      words.put(parts[column].toLowerCase(), parts[column == 1 ? 0 : 1]);
    }
    return words;
  }


  /**
   * @since 4.5
   * optional: language model for better suggestions
   */
  public AbstractEnglishSpellerRule(ResourceBundle messages, Language language, GlobalConfig globalConfig, UserConfig userConfig,
                                    List<Language> altLanguages, LanguageModel languageModel, Language motherTongue) throws IOException {
    super(messages, language, globalConfig, userConfig, altLanguages, languageModel, motherTongue);
    super.ignoreWordsWithLength = 1;
    setCheckCompound(true);
    addExamplePair(Example.wrong("This <marker>sentenc</marker> contains a spelling mistake."),
                   Example.fixed("This <marker>sentence</marker> contains a spelling mistake."));
    String languageSpecificIgnoreFile = getSpellingFileName().replace(".txt", "_"+language.getShortCodeWithCountryAndVariant()+".txt");
    for (String ignoreWord : wordListLoader.loadWords(languageSpecificIgnoreFile)) {
      addIgnoreWords(ignoreWord);
    }
    translator = BeoLingusTranslator.getInstance(globalConfig);
  }

  @Override
  protected List<String> filterSuggestions(List<String> suggestions, AnalyzedSentence sentence, int i) {
    List<String> result = super.filterSuggestions(suggestions, sentence, i);
    List<String> clean = new ArrayList<>();
    for (String suggestion : result) {
      if (!suggestion.matches(".* (s|t|d|ll|ve)")) {  // e.g. 'timezones' suggests 'timezone s'
        clean.add(suggestion);
      }
    }
    return clean;
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
          String suggestion = StringTools.startsWithUppercase(word) ?
              StringTools.uppercaseFirstChar(variantInfo.otherVariant()) : variantInfo.otherVariant();
          replaceFormsOfFirstMatch(message, sentence, ruleMatches, suggestion);
        }
      }
    }
    // filter "re ..." (#2562):
    for (RuleMatch ruleMatch : ruleMatches) {
      List<SuggestedReplacement> cleaned = ruleMatch.getSuggestedReplacementObjects().stream()
        .filter(k -> !k.getReplacement().startsWith("re ") && !k.getReplacement().endsWith(" ed"))
        .collect(Collectors.toList());
      ruleMatch.setSuggestedReplacementObjects(cleaned);
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
    } else if ("alot".equals(word)) {
      return Arrays.asList("a lot");
    } else if ("css".equals(word)) {
      return Arrays.asList("CSS");
    } else if ("ad-hoc".equals(word) || "adhoc".equals(word)) {
      return Arrays.asList("ad hoc");
    } else if ("Ad-hoc".equals(word) || "Adhoc".equals(word)) {
      return Arrays.asList("Ad hoc");
    } else if ("ad-on".equals(word) || "add-o".equals(word)) {
      return Arrays.asList("add-on");
    } else if ("acc".equals(word)) {
      return Arrays.asList("account", "accusative");
    } else if ("Acc".equals(word)) {
      return Arrays.asList("Account", "Accusative");
    } else if ("jus".equals(word)) {
      return Arrays.asList("just", "juice");
    } else if ("Jus".equals(word)) {
      return Arrays.asList("Just", "Juice");
    } else if ("sayed".equals(word)) {
      return Arrays.asList("said");
    } else if ("sess".equals(word)) {
      return Arrays.asList("says", "session", "cess");
    } else if ("Addon".equals(word)) {
      return Arrays.asList("Add-on");
    } else if ("Addons".equals(word)) {
      return Arrays.asList("Add-ons");
    } else if ("ios".equals(word)) {
      return Arrays.asList("iOS");
    } else if ("yrs".equals(word)) {
      return Arrays.asList("years");
    } else if ("standup".equals(word)) {
      return Arrays.asList("stand-up");
    } else if ("standups".equals(word)) {
      return Arrays.asList("stand-ups");
    } else if ("Standup".equals(word)) {
      return Arrays.asList("Stand-up");
    } else if ("Standups".equals(word)) {
      return Arrays.asList("Stand-ups");
    } else if ("Playdough".equals(word)) {
      return Arrays.asList("Play-Doh");
    } else if ("playdough".equals(word)) {
      return Arrays.asList("Play-Doh");
    } else if ("biggy".equals(word)) {
      return Arrays.asList("biggie");
    } else if ("lieing".equals(word)) {
      return Arrays.asList("lying");
    } else if ("preffered".equals(word)) {
      return Arrays.asList("preferred");
    } else if ("preffering".equals(word)) {
      return Arrays.asList("preferring");
    } else if ("reffered".equals(word)) {
      return Arrays.asList("referred");
    } else if ("reffering".equals(word)) {
      return Arrays.asList("referring");
    } else if ("passthrough".equals(word)) {
      return Arrays.asList("pass-through");
    } else if ("&&".equals(word)) {
      return Arrays.asList("&");
    } else if ("cmon".equals(word)) {
      return Arrays.asList("c'mon");
    } else if ("Cmon".equals(word)) {
      return Arrays.asList("C'mon");
    } else if ("da".equals(word)) {
      return Arrays.asList("the");
    } else if ("Da".equals(word)) {
      return Arrays.asList("The");
    } else if ("Vue".equals(word)) {
      return Arrays.asList("Vue.JS");
    } else if ("errornous".equals(word)) {
      return Arrays.asList("erroneous");
    } else if ("brang".equals(word) || "brung".equals(word)) {
      return Arrays.asList("brought");
    } else if ("thru".equals(word)) {
      return Arrays.asList("through");
    } else if ("pitty".equals(word)) {
      return Arrays.asList("pity");
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
    } else if ("tryna".equals(word)) {
      return Arrays.asList("trying to");
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
    } else if ("Oconnor".equalsIgnoreCase(word)) {
      return Arrays.asList("O'Connor");
    } else if ("Oconor".equalsIgnoreCase(word)) {
      return Arrays.asList("O'Conor");
    } else if ("Obrien".equalsIgnoreCase(word)) {
      return Arrays.asList("O'Brien");
    } else if ("Odonnell".equalsIgnoreCase(word)) {
      return Arrays.asList("O'Donnell");
    } else if ("Oneill".equalsIgnoreCase(word)) {
      return Arrays.asList("O'Neill");
    } else if ("Oneil".equalsIgnoreCase(word)) {
      return Arrays.asList("O'Neil");
    } else if ("Oconnell".equalsIgnoreCase(word)) {
      return Arrays.asList("O'Connell");
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
    } else if ("todos".equals(word)) {
      return Arrays.asList("to-dos", "to do");
    } else if ("Todo".equalsIgnoreCase(word)) {
      return Arrays.asList("To-do", "To do");
    } else if ("Todos".equalsIgnoreCase(word)) {
      return Arrays.asList("To-dos");
    } else if ("heres".equals(word)) {
      return Arrays.asList("here's");
    } else if ("Heres".equals(word)) {
      return Arrays.asList("Here's");
    } else if ("aways".equals(word)) {
      return Arrays.asList("always");
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
    } else if ("Ecommerce".equals(word)) {
      return Arrays.asList("E-Commerce");
    } else if ("ecommerce".equalsIgnoreCase(word)) {
      return Arrays.asList("e-commerce");
    } else if ("elearning".equalsIgnoreCase(word)) {
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
    } else if ("Esport".equals(word)) {
      return Arrays.asList("E-Sport");
    } else if ("Esports".equals(word)) {
      return Arrays.asList("E-Sports");
    } else if ("esport".equalsIgnoreCase(word)) {
      return Arrays.asList("e-sport");
    } else if ("esports".equalsIgnoreCase(word)) {
      return Arrays.asList("e-sports");
    } else if ("R&B".equals(word)) {
      return Arrays.asList("R & B", "R 'n' B");
    } else if ("ie".equals(word)) {
      return Arrays.asList("i.e.");
    } else if ("eg".equals(word)) {
      return Arrays.asList("e.g.");
    } else if ("ppl".equals(word)) {
      return Arrays.asList("people");
    } else if ("kiddin".equals(word)) {
      return Arrays.asList("kidding");
    } else if ("doin".equals(word)) {
      return Arrays.asList("doing");
    } else if ("nothin".equals(word)) {
      return Arrays.asList("nothing");
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
    } else if ("gmail".equals(word) || "g-mail".equalsIgnoreCase(word)) {
      return Arrays.asList("Gmail");
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
    } else if ("sweety".equals(word)) {
      return Arrays.asList("sweetie");
    } else if ("sweetys".equals(word)) {
      return Arrays.asList("sweeties");
    } else if ("Hongkong".equals(word)) {
      return Arrays.asList("Hong Kong");
    } else if ("Playstation".equalsIgnoreCase(word)) {
      return Arrays.asList("PlayStation");
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
    } else if ("cuz".equals(word) || "coz".equals(word)) {
      return Arrays.asList("because");
    } else if ("pls".equals(word)) {
      return Arrays.asList("please");
    } else if ("Pls".equals(word)) {
      return Arrays.asList("Please");
    } else if ("plz".equals(word)) {
      return Arrays.asList("please");
    } else if ("Plz".equals(word)) {
      return Arrays.asList("Please");
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
    } else if ("tobe".equals(word)) {
      return Arrays.asList("to be");
    } else if ("Gi".equals(word) || "Ji".equals(word)) {
      return Arrays.asList("Hi");
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

  @Override
  protected Translator getTranslator(GlobalConfig globalConfig) {
    return translator;
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
