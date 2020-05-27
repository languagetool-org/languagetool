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
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.en.translation.BeoLingusTranslator;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.rules.translation.Translator;
import org.languagetool.synthesis.en.EnglishSynthesizer;
import org.languagetool.tools.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public abstract class AbstractEnglishSpellerRule extends MorfologikSpellerRule {

  private static Logger logger = LoggerFactory.getLogger(AbstractEnglishSpellerRule.class);
  private static final EnglishSynthesizer synthesizer = (EnglishSynthesizer) Languages.getLanguageForShortCode("en").getSynthesizer();

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
    topSuggestions = getTopSuggestions();
    topSuggestionsIgnoreCase = getTopSuggestionsIgnoreCase();
  }

  @Override
  protected List<SuggestedReplacement> filterSuggestions(List<SuggestedReplacement> suggestions, AnalyzedSentence sentence, int i) {
    List<SuggestedReplacement> result = super.filterSuggestions(suggestions, sentence, i);
    List<SuggestedReplacement> clean = new ArrayList<>();
    for (SuggestedReplacement suggestion : result) {
      if (!suggestion.getReplacement().matches(".* (s|t|d|ll|ve)")) {  // e.g. 'timezones' suggests 'timezone s'
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
        .filter(k -> !k.getReplacement().startsWith("re ") &&
                     !k.getReplacement().startsWith("en ") &&
                     !k.getReplacement().startsWith("inter ") &&
                     !k.getReplacement().endsWith(" able") &&
                     !k.getReplacement().endsWith(" ed"))
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
          String[] forms = Objects.requireNonNull(language.getSynthesizer()).synthesize(new AnalyzedToken(word, null, baseForm), posTag);
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

  protected final Map<String, List<String>> topSuggestions;
  protected final Map<String, List<String>> topSuggestionsIgnoreCase;

  protected Map<String, List<String>> getTopSuggestionsIgnoreCase() {
    Map<String, List<String>> s = new HashMap<>();
    s.put("gif", Arrays.asList("GIF"));
    s.put("gifs", Arrays.asList("GIFs"));
    s.put("atm", Arrays.asList("ATM"));
    s.put("atms", Arrays.asList("ATMs"));
    s.put("png", Arrays.asList("PNG"));
    s.put("pngs", Arrays.asList("PNGs"));
    s.put("csv", Arrays.asList("CSV"));
    s.put("csvs", Arrays.asList("CSVs"));
    s.put("pdf", Arrays.asList("PDF"));
    s.put("pdfs", Arrays.asList("PDFs"));
    s.put("jpeg", Arrays.asList("JPEG"));
    s.put("jpegs", Arrays.asList("JPEGs"));
    s.put("jpg", Arrays.asList("JPG"));
    s.put("jpgs", Arrays.asList("JPGs"));
    s.put("bmp", Arrays.asList("BMP"));
    s.put("bmps", Arrays.asList("BMPs"));
    s.put("docx", Arrays.asList("DOCX"));
    s.put("xlsx", Arrays.asList("XLSX"));
    s.put("btw", Arrays.asList("BTW"));
    s.put("idk", Arrays.asList("IDK"));
    s.put("ai", Arrays.asList("AI"));
    s.put("ip", Arrays.asList("IP"));
    s.put("rfc", Arrays.asList("RFC"));
    s.put("ppt", Arrays.asList("PPT"));
    s.put("ppts", Arrays.asList("PPTs"));
    s.put("pptx", Arrays.asList("PPTX"));
    s.put("vpn", Arrays.asList("VPN"));
    s.put("psn", Arrays.asList("PSN"));
    s.put("usd", Arrays.asList("USD"));
    s.put("tv", Arrays.asList("TV"));
    s.put("eur", Arrays.asList("EUR"));
    s.put("tbh", Arrays.asList("TBH"));
    s.put("tbd", Arrays.asList("TBD"));
    s.put("tba", Arrays.asList("TBA"));
    s.put("omg", Arrays.asList("OMG"));
    s.put("lol", Arrays.asList("LOL"));
    s.put("lmao", Arrays.asList("LMAO"));
    s.put("wtf", Arrays.asList("WTF"));
    s.put("fyi", Arrays.asList("FYI"));
    s.put("url", Arrays.asList("URL"));
    s.put("urls", Arrays.asList("URLs"));
    s.put("usb", Arrays.asList("USB"));
    s.put("bbq", Arrays.asList("BBQ"));
    s.put("bbqs", Arrays.asList("BBQs"));
    s.put("ngo", Arrays.asList("NGO"));
    s.put("ngos", Arrays.asList("NGOs"));
    s.put("js", Arrays.asList("JS"));
    s.put("css", Arrays.asList("CSS"));
    s.put("roi", Arrays.asList("ROI"));
    s.put("pov", Arrays.asList("POV"));
    s.put("ctrl", Arrays.asList("Ctrl"));

    s.put("italia", Arrays.asList("Italy"));
    s.put("macboook", Arrays.asList("MacBook"));
    s.put("macboooks", Arrays.asList("MacBooks"));
    s.put("paypal", Arrays.asList("PayPal"));
    s.put("youtube", Arrays.asList("YouTube"));
    s.put("whatsapp", Arrays.asList("WhatsApp"));
    s.put("webex", Arrays.asList("WebEx"));
    s.put("jira", Arrays.asList("Jira"));
    s.put("applepay", Arrays.asList("Apple Pay"));
    s.put("&&", Arrays.asList("&"));
    s.put("wensday", Arrays.asList("Wednesday"));
    s.put("linkedin", Arrays.asList("LinkedIn"));
    s.put("ebay", Arrays.asList("eBay"));
    s.put("interweb", Arrays.asList("internet"));
    s.put("interwebs", Arrays.asList("internet"));

    s.put("afro-american", Arrays.asList("Afro-American"));
    s.put("oconnor", Arrays.asList("O'Connor"));
    s.put("oconor", Arrays.asList("O'Conor"));
    s.put("obrien", Arrays.asList("O'Brien"));
    s.put("odonnell", Arrays.asList("O'Donnell"));
    s.put("oneill", Arrays.asList("O'Neill"));
    s.put("oneil", Arrays.asList("O'Neil"));
    s.put("oconnell", Arrays.asList("O'Connell"));
    s.put("todo", Arrays.asList("To-do", "To do"));
    s.put("todos", Arrays.asList("To-dos"));
    s.put("ecommerce", Arrays.asList("e-commerce"));
    s.put("elearning", Arrays.asList("e-learning"));
    s.put("esport", Arrays.asList("e-sport"));
    s.put("esports", Arrays.asList("e-sports"));
    s.put("g-mail", Arrays.asList("Gmail"));
    s.put("playstation", Arrays.asList("PlayStation"));
    return s;
  }

  protected Map<String, List<String>> getTopSuggestions() {
    Map<String, List<String>> s = new HashMap<>();
    s.put("sin-off", Arrays.asList("sign-off"));
    s.put("sin-offs", Arrays.asList("sign-offs"));
    s.put("Sin-off", Arrays.asList("Sign-off"));
    s.put("Sin-offs", Arrays.asList("Sign-offs"));
    s.put("Alot", Arrays.asList("A lot"));
    s.put("alot", Arrays.asList("a lot"));
    s.put("DDOS", Arrays.asList("DDoS"));
    s.put("async", Arrays.asList("asynchronous", "asynchronously"));
    s.put("Async", Arrays.asList("Asynchronous", "Asynchronously"));
    s.put("endevours", Arrays.asList("endeavours"));
    s.put("endevors", Arrays.asList("endeavors"));
    s.put("endevour", Arrays.asList("endeavour"));
    s.put("endevor", Arrays.asList("endeavor"));
    s.put("countrys", Arrays.asList("countries", "country's", "country"));
    s.put("ad-hoc", Arrays.asList("ad hoc"));
    s.put("adhoc", Arrays.asList("ad hoc"));
    s.put("Ad-hoc", Arrays.asList("Ad hoc"));
    s.put("Adhoc", Arrays.asList("Ad hoc"));
    s.put("ad-on", Arrays.asList("add-on"));
    s.put("add-o", Arrays.asList("add-on"));
    s.put("acc", Arrays.asList("account", "accusative"));
    s.put("Acc", Arrays.asList("Account", "Accusative"));
    s.put("ºC", Arrays.asList("°C"));
    s.put("jus", Arrays.asList("just", "juice"));
    s.put("Jus", Arrays.asList("Just", "Juice"));
    s.put("sayed", Arrays.asList("said"));
    s.put("sess", Arrays.asList("says", "session", "cess"));
    s.put("Addon", Arrays.asList("Add-on"));
    s.put("Addons", Arrays.asList("Add-ons"));
    s.put("ios", Arrays.asList("iOS"));
    s.put("yrs", Arrays.asList("years"));
    s.put("standup", Arrays.asList("stand-up"));
    s.put("standups", Arrays.asList("stand-ups"));
    s.put("Standup", Arrays.asList("Stand-up"));
    s.put("Standups", Arrays.asList("Stand-ups"));
    s.put("Playdough", Arrays.asList("Play-Doh"));
    s.put("playdough", Arrays.asList("Play-Doh"));
    s.put("biggy", Arrays.asList("biggie"));
    s.put("lieing", Arrays.asList("lying"));
    s.put("preffered", Arrays.asList("preferred"));
    s.put("preffering", Arrays.asList("preferring"));
    s.put("reffered", Arrays.asList("referred"));
    s.put("reffering", Arrays.asList("referring"));
    s.put("passthrough", Arrays.asList("pass-through"));
    s.put("&&", Arrays.asList("&"));
    s.put("cmon", Arrays.asList("c'mon"));
    s.put("Cmon", Arrays.asList("C'mon"));
    s.put("da", Arrays.asList("the"));
    s.put("Da", Arrays.asList("The"));
    s.put("Vue", Arrays.asList("Vue.JS"));
    s.put("errornous", Arrays.asList("erroneous"));
    s.put("brang", Arrays.asList("brought"));
    s.put("brung", Arrays.asList("brought"));
    s.put("thru", Arrays.asList("through"));
    s.put("pitty", Arrays.asList("pity"));
    s.put("barbwire", Arrays.asList("barbed wire"));
    s.put("barbwires", Arrays.asList("barbed wires"));
    // the replacement pairs would prefer "speak"
    s.put("speach", Arrays.asList("speech"));
    s.put("icecreem", Arrays.asList("ice cream"));
    // in en-gb it's 'maths'
    s.put("math", Arrays.asList("maths"));
    s.put("fora", Arrays.asList("for a"));
    s.put("lotsa", Arrays.asList("lots of"));
    s.put("tryna", Arrays.asList("trying to"));
    s.put("coulda", Arrays.asList("could have"));
    s.put("shoulda", Arrays.asList("should have"));
    s.put("woulda", Arrays.asList("would have"));
    s.put("tellem", Arrays.asList("tell them"));
    s.put("Tellem", Arrays.asList("Tell them"));
    s.put("Webex", Arrays.asList("WebEx"));
    s.put("didint", Arrays.asList("didn't"));
    s.put("Didint", Arrays.asList("Didn't"));
    s.put("wasint", Arrays.asList("wasn't"));
    s.put("hasint", Arrays.asList("hasn't"));
    s.put("doesint", Arrays.asList("doesn't"));
    s.put("ist", Arrays.asList("is"));
    s.put("Boing", Arrays.asList("Boeing"));
    s.put("te", Arrays.asList("the"));
    s.put("todays", Arrays.asList("today's"));
    s.put("Todays", Arrays.asList("Today's"));
    s.put("todo", Arrays.asList("to-do", "to do"));
    s.put("todos", Arrays.asList("to-dos", "to do"));
    s.put("heres", Arrays.asList("here's"));
    s.put("Heres", Arrays.asList("Here's"));
    s.put("aways", Arrays.asList("always"));
    s.put("McDonalds", Arrays.asList("McDonald's"));
    s.put("ux", Arrays.asList("UX"));
    s.put("ive", Arrays.asList("I've"));
    s.put("infos", Arrays.asList("informations"));
    s.put("Infos", Arrays.asList("Informations"));
    s.put("prios", Arrays.asList("priorities"));
    s.put("Prio", Arrays.asList("Priority"));
    s.put("prio", Arrays.asList("priority"));
    s.put("Ecommerce", Arrays.asList("E-Commerce"));
    s.put("ebook", Arrays.asList("e-book"));
    s.put("ebooks", Arrays.asList("e-books"));
    s.put("eBook", Arrays.asList("e-book"));
    s.put("eBooks", Arrays.asList("e-books"));
    s.put("Ebook", Arrays.asList("E-Book"));
    s.put("Ebooks", Arrays.asList("E-Books"));
    s.put("Esport", Arrays.asList("E-Sport"));
    s.put("Esports", Arrays.asList("E-Sports"));
    s.put("R&B", Arrays.asList("R & B", "R 'n' B"));
    s.put("ie", Arrays.asList("i.e."));
    s.put("eg", Arrays.asList("e.g."));
    s.put("ppl", Arrays.asList("people"));
    s.put("kiddin", Arrays.asList("kidding"));
    s.put("doin", Arrays.asList("doing"));
    s.put("nothin", Arrays.asList("nothing"));
    s.put("SPOC", Arrays.asList("SpOC"));
    s.put("Thx", Arrays.asList("Thanks"));
    s.put("thx", Arrays.asList("thanks"));
    s.put("ty", Arrays.asList("thank you", "thanks"));
    s.put("Sry", Arrays.asList("Sorry"));
    s.put("sry", Arrays.asList("sorry"));
    s.put("im", Arrays.asList("I'm"));
    s.put("spoilt", Arrays.asList("spoiled"));
    s.put("Lil", Arrays.asList("Little"));
    s.put("lil", Arrays.asList("little"));
    s.put("gmail", Arrays.asList("Gmail"));
    s.put("Sucka", Arrays.asList("Sucker"));
    s.put("sucka", Arrays.asList("sucker"));
    s.put("whaddya", Arrays.asList("what are you", "what do you"));
    s.put("Whaddya", Arrays.asList("What are you", "What do you"));
    s.put("sinc", Arrays.asList("sync"));
    s.put("sweety", Arrays.asList("sweetie"));
    s.put("sweetys", Arrays.asList("sweeties"));
    s.put("sowwy", Arrays.asList("sorry"));
    s.put("Sowwy", Arrays.asList("Sorry"));
    s.put("grandmum", Arrays.asList("grandma", "grandmother"));
    s.put("Grandmum", Arrays.asList("Grandma", "Grandmother"));
    s.put("Hongkong", Arrays.asList("Hong Kong"));
    // For non-US English
    s.put("center", Arrays.asList("centre"));
    s.put("ur", Arrays.asList("your", "you are"));
    s.put("Ur", Arrays.asList("Your", "You are"));
    s.put("ure", Arrays.asList("your", "you are"));
    s.put("Ure", Arrays.asList("Your", "You are"));
    s.put("mins", Arrays.asList("minutes", "min"));
    s.put("addon", Arrays.asList("add-on"));
    s.put("addons", Arrays.asList("add-ons"));
    s.put("afterparty", Arrays.asList("after-party"));
    s.put("Afterparty", Arrays.asList("After-party"));
    s.put("wellbeing", Arrays.asList("well-being"));
    s.put("cuz", Arrays.asList("because"));
    s.put("coz", Arrays.asList("because"));
    s.put("pls", Arrays.asList("please"));
    s.put("Pls", Arrays.asList("Please"));
    s.put("plz", Arrays.asList("please"));
    s.put("Plz", Arrays.asList("Please"));
    // AtD irregular plurals - START
    s.put("addendums", Arrays.asList("addenda"));
    s.put("algas", Arrays.asList("algae"));
    s.put("alumnas", Arrays.asList("alumnae"));
    s.put("alumnuses", Arrays.asList("alumni"));
    s.put("analysises", Arrays.asList("analyses"));
    s.put("appendixs", Arrays.asList("appendices"));
    s.put("axises", Arrays.asList("axes"));
    s.put("bacilluses", Arrays.asList("bacilli"));
    s.put("bacteriums", Arrays.asList("bacteria"));
    s.put("basises", Arrays.asList("bases"));
    s.put("beaus", Arrays.asList("beaux"));
    s.put("bisons", Arrays.asList("bison"));
    s.put("buffalos", Arrays.asList("buffaloes"));
    s.put("calfs", Arrays.asList("calves"));
    s.put("Childs", Arrays.asList("Children"));
    s.put("childs", Arrays.asList("children"));
    s.put("crisises", Arrays.asList("crises"));
    s.put("criterions", Arrays.asList("criteria"));
    s.put("curriculums", Arrays.asList("curricula"));
    s.put("datums", Arrays.asList("data"));
    s.put("deers", Arrays.asList("deer"));
    s.put("diagnosises", Arrays.asList("diagnoses"));
    s.put("echos", Arrays.asList("echoes"));
    s.put("elfs", Arrays.asList("elves"));
    s.put("ellipsises", Arrays.asList("ellipses"));
    s.put("embargos", Arrays.asList("embargoes"));
    s.put("erratums", Arrays.asList("errata"));
    s.put("firemans", Arrays.asList("firemen"));
    s.put("fishs", Arrays.asList("fishes", "fish"));
    s.put("genuses", Arrays.asList("genera"));
    s.put("gooses", Arrays.asList("geese"));
    s.put("halfs", Arrays.asList("halves"));
    s.put("heros", Arrays.asList("heroes"));
    s.put("indexs", Arrays.asList("indices", "indexes"));
    s.put("lifes", Arrays.asList("lives"));
    s.put("mans", Arrays.asList("men"));
    s.put("matrixs", Arrays.asList("matrices"));
    s.put("meanses", Arrays.asList("means"));
    s.put("mediums", Arrays.asList("media"));
    s.put("memorandums", Arrays.asList("memoranda"));
    s.put("mooses", Arrays.asList("moose"));
    s.put("mosquitos", Arrays.asList("mosquitoes"));
    s.put("neurosises", Arrays.asList("neuroses"));
    s.put("nucleuses", Arrays.asList("nuclei"));
    s.put("oasises", Arrays.asList("oases"));
    s.put("ovums", Arrays.asList("ova"));
    s.put("oxs", Arrays.asList("oxen"));
    s.put("oxes", Arrays.asList("oxen"));
    s.put("paralysises", Arrays.asList("paralyses"));
    s.put("potatos", Arrays.asList("potatoes"));
    s.put("radiuses", Arrays.asList("radii"));
    s.put("selfs", Arrays.asList("selves"));
    s.put("serieses", Arrays.asList("series"));
    s.put("sheeps", Arrays.asList("sheep"));
    s.put("shelfs", Arrays.asList("shelves"));
    s.put("scissorses", Arrays.asList("scissors"));
    s.put("specieses", Arrays.asList("species"));
    s.put("stimuluses", Arrays.asList("stimuli"));
    s.put("stratums", Arrays.asList("strata"));
    s.put("tableaus", Arrays.asList("tableaux"));
    s.put("thats", Arrays.asList("those"));
    s.put("thesises", Arrays.asList("theses"));
    s.put("thiefs", Arrays.asList("thieves"));
    s.put("thises", Arrays.asList("these"));
    s.put("tomatos", Arrays.asList("tomatoes"));
    s.put("tooths", Arrays.asList("teeth"));
    s.put("torpedos", Arrays.asList("torpedoes"));
    s.put("vertebras", Arrays.asList("vertebrae"));
    s.put("vetos", Arrays.asList("vetoes"));
    s.put("vitas", Arrays.asList("vitae"));
    s.put("watchs", Arrays.asList("watches"));
    s.put("wifes", Arrays.asList("wives", "wife's"));
    s.put("womans", Arrays.asList("women", "woman's"));
    s.put("womens", Arrays.asList("women's"));
    // AtD irregular plurals - END
    // "tippy-top" is an often used word by Donald Trump
    s.put("tippy-top", Arrays.asList("tip-top", "top most"));
    s.put("tippytop", Arrays.asList("tip-top", "top most"));
    s.put("imma", Arrays.asList("I'm going to", "I'm a"));
    s.put("Imma", Arrays.asList("I'm going to", "I'm a"));
    s.put("dontcha", Arrays.asList("don't you"));
    s.put("tobe", Arrays.asList("to be"));
    s.put("Gi", Arrays.asList("Hi"));
    s.put("Ji", Arrays.asList("Hi"));
    s.put("Dontcha", Arrays.asList("don't you"));
    s.put("greatfruit", Arrays.asList("grapefruit", "great fruit"));
    s.put("ur", Arrays.asList("your", "you are"));
    s.put("Insta", Arrays.asList("Instagram"));
    s.put("IO", Arrays.asList("I/O"));
    s.put("wierd", Arrays.asList("weird"));
    s.put("Wierd", Arrays.asList("Weird"));

    return s;
  }

  /**
   * @since 2.7
   */
  @Override
  protected List<SuggestedReplacement> getAdditionalTopSuggestions(List<SuggestedReplacement> suggestions, String word) throws IOException {

    if (word.length() < 20 && word.matches("[a-zA-Z-]+.?")) {
      List<String> prefixes = Arrays.asList("inter", "pre");
      for (String prefix : prefixes) {
        if (word.startsWith(prefix)) {
          String lastPart = word.substring(prefix.length());
          if (!isMisspelled(lastPart)) {
            // as these are only single words and both the first part and the last part are spelled correctly
            // (but the combination is not), it's okay to log the words from a privacy perspective:
            logger.info("UNKNOWN-EN: " + word);
          }
        }
      }
    }

    List<String> curatedSuggestions = new ArrayList<>();
    curatedSuggestions.addAll(topSuggestions.getOrDefault(word, Collections.emptyList()));
    curatedSuggestions.addAll(topSuggestionsIgnoreCase.getOrDefault(word.toLowerCase(), Collections.emptyList()));
    if (!curatedSuggestions.isEmpty()) {
      return SuggestedReplacement.convert(curatedSuggestions);
    } else if (word.endsWith("ys")) {
      String suggestion = word.replaceFirst("ys$", "ies");
      if (!speller1.isMisspelled(suggestion)) {
        return SuggestedReplacement.convert(Arrays.asList(suggestion));
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
