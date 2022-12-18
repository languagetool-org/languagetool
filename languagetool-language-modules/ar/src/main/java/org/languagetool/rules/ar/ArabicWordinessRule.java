/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2019 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.rules.ar;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.Arabic;
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ar.ArabicSynthesizer;
import org.languagetool.tagging.ar.ArabicTagManager;
import org.languagetool.tagging.ar.ArabicTagger;
import org.languagetool.tools.ArabicNumbersWords;
import org.languagetool.tools.ArabicWordMaps;
import org.languagetool.tools.StringTools;

import java.util.*;

import static java.util.Arrays.asList;
import static org.languagetool.tools.ArabicConstants.TEH_MARBUTA;

/**
 * A rule that matches wordy expressions.
 * Arabic implementation. Loads the list of words from
 * <code>/ar/wordiness.txt</code>.
 *
 * @author Sohaib Afifi
 * @author Taha Zerrouki
 * @since 5.0
 */
public class ArabicWordinessRule extends AbstractSimpleReplaceRule2 {

  public static final String AR_WORDINESS_REPLACE = "AR_WORDINESS_REPLACE";

  private static final String FILE_NAME = "/ar/wordiness.txt";
  private static final Locale AR_LOCALE = new Locale("ar");  // locale used on case-conversion

  public ArabicWordinessRule(ResourceBundle messages) {
    super(messages, new Arabic());
    super.setCategory(Categories.REDUNDANCY.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    addExamplePair(Example.wrong("<marker>هناك خطأ في العبارة</marker>"),
                   Example.fixed("<marker>في العبارة خطأ</marker>"));
  }

  @Override
  public final List<String> getFileNames() {
    return Collections.singletonList(FILE_NAME);
  }

  @Override
  public final String getId() {
    return AR_WORDINESS_REPLACE;
  }

  @Override
  public String getDescription() {
    return "2. حشو(تعبير فيه تكرار)";
  }

  @Override
  public String getShort() {
    return "حشو (تعبير فيه تكرار)";
  }

  @Override
  public String getMessage() {
    return "'$match' تعبير فيه حشو يفضل أن يقال $suggestions";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " أو ";
  }

  @Override
  public Locale getLocale() {
    return AR_LOCALE;
  }

  /**
   * Filter that maps suggestion from adverb to adjective.
   *
   * @since 5.8
   */
  public static class AdjectiveToExclamationFilter extends RuleFilter {

    private final ArabicTagger tagger = new ArabicTagger();
    private static final String FILE_NAME = "/ar/arabic_adjective_exclamation.txt";
    private final Map<String, List<String>> adj2compList = loadFromPath(FILE_NAME);

    private final Map<String, String> adj2comp = new HashMap<String, String>() {{
      // tri letters verb:
      put("رشيد", "أرشد");
      put("طويل", "أطول");
      put("بديع", "أبدع");
    }};


    @Nullable
    @Override
    public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {

      //  This rule return only the comparative according to given adjective
      String noun = arguments.get("noun"); // the second argument
      int adjTokenIndex;
      try {
        adjTokenIndex = Integer.valueOf(arguments.get("adj_pos")) - 1;
      } catch (NumberFormatException e) {
        throw new RuntimeException("Error parsing adj_pos from : " + arguments.get("adj_pos"));
      }

      // filter tokens which have a lemma of adjective

      // some cases can have multiple lemmas, but only adjective lemma are used
      List<String> adjLemmas = tagger.getLemmas(patternTokens[adjTokenIndex], "adj");

      // get comparative from Adj/comp list
      List<String> compList = new ArrayList<>();
      for (String adjLemma : adjLemmas) {
        // get comparative suitable to adjective
        List<String> comparativeList = adj2compList.get(adjLemma);
        if (comparativeList != null) {
          compList.addAll(comparativeList);
        }
      }

      //

      // remove duplicates
      compList = new ArrayList<>(new HashSet<>(compList));
      RuleMatch newMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), match.getMessage(), match.getShortMessage());

      // generate suggestion
      List<String> suggestionList = prepareSuggestions(compList, noun);
      for (String sug : suggestionList) {
        newMatch.addSuggestedReplacement(sug);
      }
      return newMatch;
    }

    /* prepare suggesiyton for a list of comparative */
    protected static List<String> prepareSuggestions(List<String> compList, String noun) {
      List<String> sugList = new ArrayList<>();
      for (String comp : compList) {
        sugList.addAll(prepareSuggestions(comp, noun));
      }
      return sugList;
    }

    protected static List<String> prepareSuggestions(String comp, String noun) {
        /*
        الحالات:
        الاسم ليس ضميرا


        ال   كم الولد جميل==> ما أجمل الولد
        أجمل بالولد

        حالة الضمير

        كم هو جميل==> ما أجمله
          أجمل به

        حالة الضفة غير الثلاثية
        اسم:
        كم الطالب شديد الاستيعاب
        ما أشد استيعاب الطالب
        أشدد باستيعابه

        ضمير
        كم هو شديد الاستيعاب
        ما أشد استيعابه
        أشد باستيعابه
         */

      List<String> sugList = new ArrayList<>();
      StringBuilder suggestion = new StringBuilder();
      // first form of exclamation ما أجمل
      suggestion.append(comp);
      if (noun != null && !noun.isEmpty() && isPronoun(noun)) {
        // no space adding
        suggestion.append(ArabicWordMaps.getAttachedPronoun(noun));
      } else {
        //if comparative is of second form don't add a space
        if (!comp.endsWith(" ب")) {
          suggestion.append(" ");
        }
        suggestion.append(noun);
      }


      // second form of exclamation أجمل ب
      // add suggestions
      sugList.add(suggestion.toString());
      return sugList;
    }

    /* test if the word is an isolated pronoun */
    private static boolean isPronoun(String word) {
      if (word == null) {
        return false;
      }
      return word.equals("هو")
        || word.equals("هي")
        || word.equals("هم")
        || word.equals("هما")
        || word.equals("أنا");
    }

    /* get correspondant attched to unattached pronoun */
    private static String getAttachedPronoun(String word) {
      if (word == null) {
        return "";
      }
      Map<String, String> isolatedToAttachedPronoun = new HashMap<>();
      isolatedToAttachedPronoun.put("هو", "ه");
      isolatedToAttachedPronoun.put("هي", "ها");
      isolatedToAttachedPronoun.put("هم", "هم");
      isolatedToAttachedPronoun.put("هن", "هن");
      isolatedToAttachedPronoun.put("نحن", "نا");
      return isolatedToAttachedPronoun.getOrDefault(word, "");
    }


    protected static Map<String, List<String>> loadFromPath(String path) {
      return new SimpleReplaceDataLoader().loadWords(path);
    }

    public static String getDataFilePath() {
      return FILE_NAME;
    }
  }

  /*
   * Synthesize suggestions using the lemma from one token (lemma_from)
   * and the POS tag from another one (postag_from).
   *
   * The lemma_select and postag_select attributes are required
   * to choose one among several possible readings.
   * @since 5.8
   */
  public static class AdvancedSynthesizerFilter extends AbstractAdvancedSynthesizerFilter {

    private final ArabicSynthesizer synth = new ArabicSynthesizer(new Arabic());

    @Override
    protected Synthesizer getSynthesizer() {
      return synth;
    }

  }

  /**
   * Arabic localization of {@link AbstractDateCheckFilter}.
   *
   * @since 4.8
   */
  public static class DateCheckFilter extends AbstractDateCheckFilter {

    private final DateFilterHelper dateFilterHelper = new DateFilterHelper();


    @Override
    protected Calendar getCalendar() {
      return Calendar.getInstance(Locale.forLanguageTag("ar"));
    }

    @SuppressWarnings("ControlFlowStatementWithoutBraces")
    @Override
    protected int getDayOfWeek(String dayStr) {
      return dateFilterHelper.getDayOfWeek(dayStr);

    }

    @Override
    protected String getDayOfWeek(Calendar date) {
      return date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.forLanguageTag("ar"));
    }

    protected String getDayOfWeek(int day) {
      return dateFilterHelper.getDayOfWeekName(day);
    }


    @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
    @Override
    protected int getMonth(String monthStr) {
      return dateFilterHelper.getMonth(monthStr);
    }

  }

  /**
   * Date filter that expects a 'date' argument in the format 'dd-mm-yyyy'.
   *
   * @since 5.8
   */
  public static class DMYDateCheckFilter extends DateCheckFilter {

    @Override
    public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> args, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {
      if (args.containsKey("year") || args.containsKey("month") || args.containsKey("day")) {
        throw new RuntimeException("Set only 'weekDay' and 'date' for " + DMYDateCheckFilter.class.getSimpleName());
      }
      String dateString = getRequired("date", args);
      String[] parts = dateString.split("-");
      if (parts.length != 3) {
        throw new RuntimeException("Expected date in format 'dd-mm-yyyy': '" + dateString + "'");
      }
      args.put("day", parts[0]);
      args.put("month", parts[1]);
      args.put("year", parts[2]);
      return super.acceptRuleMatch(match, args, patternTokenPos, patternTokens);
    }

  }

  /**
   * @since 5.8
   */
  static class DateFilterHelper {

    protected Calendar getCalendar() {
      return Calendar.getInstance(Locale.forLanguageTag("ar"));
    }

    @SuppressWarnings("ControlFlowStatementWithoutBraces")
    protected int getDayOfWeek(String dayStr) {

      switch (dayStr) {
        case "السبت":
          return Calendar.SATURDAY;
        case "الأحد":
          return Calendar.SUNDAY;
        case "الإثنين":
          return Calendar.MONDAY;
        case "الاثنين":
          return Calendar.MONDAY;
        case "الثلاثاء":
          return Calendar.TUESDAY;
        case "الأربعاء":
          return Calendar.WEDNESDAY;
        case "الخميس":
          return Calendar.THURSDAY;
        case "الجمعة":
          return Calendar.FRIDAY;
      }
      throw new RuntimeException("لا يمكن إيجاد اسم يوم لـ" + dayStr + "'");
    }


    @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
    protected int getMonth(String monthStr) {
      String mon = StringTools.trimSpecialCharacters(monthStr);
      switch (mon) {
        // الأشهر العربية بالسريانية
        case "كانون الثاني":
          return 1;
        case "كانون ثاني":
          return 1;
        case "شباط":
          return 2;
        case "آذار":
          return 3;
        case "نيسان":
          return 4;
        case "أيار":
          return 5;
        case "حزيران":
          return 6;
        case "تموز":
          return 7;
        case "آب":
          return 8;
        case "أيلول":
          return 9;
        case "تشرين الأول":
          return 10;
        case "تشرين الثاني":
          return 11;
        case "كانون الأول":
          return 12;
        case "تشرين ثاني":
          return 11;
        case "كانون أول":
          return 12;
        // الأشهر المعربة عن الإنجليزية
        case "يناير":
          return 1;
        case "فبراير":
          return 2;
        case "مارس":
          return 3;
        case "أبريل":
          return 4;
        case "مايو":
          return 5;
        case "يونيو":
          return 6;
        case "يوليو":
          return 7;
        case "أغسطس":
          return 8;
        case "سبتمبر":
          return 9;
        case "أكتوبر":
          return 10;
        case "نوفمبر":
          return 11;
        case "ديسمبر":
          return 12;
        // الأشهر المعربة عن الفرنسية
        case "جانفي":
          return 1;
        case "جانفييه":
          return 1;
        case "فيفري":
          return 2;
        case "أفريل":
          return 4;
        case "ماي":
          return 5;
        case "جوان":
          return 6;
        case "جويلية":
          return 7;
        case "أوت":
          return 8;
      }
      throw new RuntimeException("لا اسم شهر لـ '" + monthStr + "'");
    }


    /* get day of week name */
    protected String getDayOfWeekName(int day) {
      switch (day) {
        case Calendar.SATURDAY:
          return "السبت";
        case Calendar.SUNDAY:
          return "الأحد";
        case Calendar.MONDAY:
          return "الإثنين";
        case Calendar.TUESDAY:
          return "الثلاثاء";
        case Calendar.WEDNESDAY:
          return "الأربعاء";
        case Calendar.THURSDAY:
          return "الخميس";
        case Calendar.FRIDAY:
          return "الجمعة";
        default:
          return "غير محدد";
      }

    }
  }

  /**
   * Filter that maps suggestion from adverb to adjective.
   *
   * @since 5.8
   */
  public static class MasdarToVerbFilter extends RuleFilter {

    private final ArabicTagger tagger = new ArabicTagger();
    private static final String FILE_NAME = "/ar/arabic_masdar_verb.txt";
    private final Map<String, List<String>> masdar2verbList = loadFromPath(FILE_NAME);
    private final ArabicSynthesizer synthesizer = new ArabicSynthesizer(new Arabic());

    final List<String> authorizeLemma = new ArrayList() {{
      add("قَامَ");
    }};


    @Nullable
    @Override
    public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {

      //  The pattern is composed from to words
      // قام بالأكل
      // يقوم بالأكل
      // يقومون بالأكل
      // first token: auxialliary  verb Qam
      // second token: Noun as Masdar
      // replace the Masdar by its verb
      // inflect the verb according the auxilaiary verb inflection

      String auxVerb = arguments.get("verb"); // الفعل قام أو ما شابهه
      String masdar = arguments.get("noun");  // masdar

      // filter tokens which have a lemma
      // some cases can have multiple lemmas, but only auxilliry lemma are used
      List<String> auxVerbLemmasAll = tagger.getLemmas(patternTokens[0], "verb");
      List<String> auxVerbLemmas = filterLemmas(auxVerbLemmasAll);

      // get all lemmas of the given masdar
      List<String> masdarLemmas = tagger.getLemmas(patternTokens[1], "masdar");

      // generate multiple verb from masdar lemmas list
      List<String> verbList = new ArrayList<>();

      // if the auxiliary verb has many lemmas, filter authorized lemma only
      // the first token: auxiliary verb
      for (AnalyzedToken auxVerbToken : patternTokens[0]) {
        // if the token has an authorized lemma
        if (auxVerbLemmas.contains(auxVerbToken.getLemma())) {
          // for all masdar lemmas
          for (String lemma : masdarLemmas) {
            // get verb suitable to masdar

            List<String> verbLemmaList = masdar2verbList.get(lemma);
            if (verbLemmaList != null) {
              // if verb, inflect verb according to auxiliary verb inflection
              for (String vrbLem : verbLemmaList) {
                List<String> inflectedVerbList = synthesizer.inflectLemmaLike(vrbLem, auxVerbToken);
                verbList.addAll(inflectedVerbList);
              }
            }
          }

        }
      }
      // remove duplicates
      verbList = new ArrayList<>(new HashSet<>(verbList));

      RuleMatch newMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), match.getMessage(), match.getShortMessage());
      // generate suggestion
      for (String verb : verbList) {
        newMatch.addSuggestedReplacement(verb);
      }
      return newMatch;
    }

    List<String> filterLemmas(List<String> lemmas) {
      List<String> filtered = new ArrayList<>();

      for (String lem : authorizeLemma) {
        if (lemmas.contains(lem)) {
          filtered.add(lem);
        }
      }
      return filtered;
    }

    protected static Map<String, List<String>> loadFromPath(String path) {
      return new SimpleReplaceDataLoader().loadWords(path);
    }
  }

  /**
   * Filter that maps suggestion for numeric phrases.
   */
  public static class NumberPhraseFilter extends RuleFilter {

    private final ArabicTagger tagger = new ArabicTagger();
    private static final ArabicTagManager tagmanager = new ArabicTagManager();
    private final ArabicSynthesizer synthesizer = new ArabicSynthesizer(new Arabic());


    @Nullable
    @Override
    public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {

      // get the previous word
      String previousWord = arguments.getOrDefault("previous", "");
      // previous word index in token list
      //    int previousWordPos =0;
      int previousWordPos = getPreviousPos(arguments);

      // get the inflect mark
      String inflectArg = arguments.getOrDefault("inflect", "");
      // get the next  word as units
      String nextWord = arguments.getOrDefault("next", "");

      int nextWordPos = getNextPos(arguments, patternTokens.length);

      List<String> numWordTokens = new ArrayList<>();
      /// get all numeric tokens
      int startPos = (previousWordPos > 0) ? previousWordPos + 1 : 0;

      int end_pos = (nextWordPos > 0) ? Integer.min(nextWordPos, patternTokens.length) : patternTokens.length + nextWordPos;

      for (int i = startPos; i < end_pos; i++) {
        numWordTokens.add(patternTokens[i].getToken().trim());
      }
      String numPhrase = String.join(" ", numWordTokens);
      /* extract features from previous */
      boolean feminin = false;
      boolean attached = false;
      String inflection = getInflectedCase(patternTokens, previousWordPos, inflectArg);
      List<String> suggestionList;
      if (nextWord.isEmpty()) {
        suggestionList = prepareSuggestion(numPhrase, previousWord, null, feminin, attached, inflection);
      } else {
        AnalyzedTokenReadings nextWordToken = null;
        if (end_pos > 0 && end_pos < patternTokens.length) {
          nextWordToken = patternTokens[end_pos];
        }
        suggestionList = prepareSuggestionWithUnits(numPhrase, previousWord, nextWordToken, feminin, attached, inflection);
      }
      RuleMatch newMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), match.getMessage(), match.getShortMessage());

      if (!suggestionList.isEmpty()) {
        for (String sug : suggestionList) {
          newMatch.addSuggestedReplacement(sug);
        }
      }
      return newMatch;
    }

    // extract inflection case
    private static String getInflectedCase(AnalyzedTokenReadings[] patternTokens, int previousPos, String inflect) {
      if (inflect != "") {
        return inflect;
      }
      // if the previous is Jar

      if (previousPos >= 0 && previousPos < patternTokens.length) {
        AnalyzedTokenReadings previousToken = patternTokens[previousPos];
        for (AnalyzedToken tk : patternTokens[previousPos]) {
          if (tk.getPOSTag() != null && tk.getPOSTag().startsWith("PR")) {
            return "jar";
          }
        }
      }
      String firstWord = patternTokens[previousPos + 1].getToken();
      if (firstWord.startsWith("ب")
        || firstWord.startsWith("ل")
        || firstWord.startsWith("ك")
      ) {
        return "jar";
      }
      return "";
    }

    // extract inflection case
    private static boolean getFemininCase(AnalyzedTokenReadings[] patternTokens, int nextPos) {
      // if the previous is Jar
      for (AnalyzedToken tk : patternTokens[nextPos]) {
        if (tagmanager.isFeminin(tk.getPOSTag())) {
          return true;
        }
      }
      return false;
    }

    /* prepare suggestion for given phrases */
    public static List<String> prepareSuggestion(String numPhrase, String previousWord, AnalyzedTokenReadings nextWord, boolean feminin, boolean attached, String inflection) {

      List<String> tmpsuggestionList = ArabicNumbersWords.getSuggestionsNumericPhrase(numPhrase, feminin, attached, inflection);
      List<String> suggestionList = new ArrayList<>();
      if (!tmpsuggestionList.isEmpty()) {
        for (String sug : tmpsuggestionList)
          if (!previousWord.isEmpty()) {
            suggestionList.add(previousWord + " " + sug);
          }
      }
      return suggestionList;
    }

    /* prepare suggestion for given phrases */
    public List<String> prepareSuggestionWithUnits(String numPhrase, String previousWord, AnalyzedTokenReadings nextWord, boolean feminin, boolean attached, String inflection) {

      String defaultUnit = "دينار";

      List<Map<String, String>> tmpsuggestionList = ArabicNumbersWords.getSuggestionsNumericPhraseWithUnits(numPhrase, defaultUnit, feminin, attached, inflection);
      List<String> suggestionList = new ArrayList<>();
      if (!tmpsuggestionList.isEmpty()) {
        for (Map<String, String> sugMap : tmpsuggestionList) {
          String sug = sugMap.get("phrase");
          List<String> inflectedUnitList = inflectUnit(nextWord, sugMap);
          for (String unit : inflectedUnitList) {
            StringBuilder tmp = new StringBuilder();
            if (!previousWord.isEmpty()) {
              tmp.append(previousWord + " ");
            }
            tmp.append(sug);
            if (unit != null && !unit.isEmpty()) {
              tmp.append(" " + unit);
            }
            suggestionList.add(tmp.toString());
          }
        }
      }

      return suggestionList;
    }

    /* get suitable forms for the given unit */
    private List<String> inflectUnit(AnalyzedTokenReadings unit, Map<String, String> sugMap) {
      if (unit == null) {
        return null;
      } else {
        String inflection = sugMap.getOrDefault("unitInflection", "");
        String number = sugMap.getOrDefault("unitNumber", "");
        List<String> tmpList = new ArrayList<>();
        List<String> inflectedList = new ArrayList<>();
        for (AnalyzedToken tk : unit) {
          String postag = tk.getPOSTag();
          //      String lemma = tk.getLemma();
          if (tagmanager.isNoun(postag) && !tagmanager.isDefinite(postag) && !tagmanager.hasPronoun(postag)) {
            // add inflection flag
            if (inflection == "jar") {
              postag = tagmanager.setMajrour(postag);
            } else if (inflection == "raf3") {
              postag = tagmanager.setMarfou3(postag);
            } else if (inflection == "nasb") {
              postag = tagmanager.setMansoub(postag);
            } else {
              postag = tagmanager.setMarfou3(postag);
            }

            // add number flag
            if (number == "one") {
              postag = tagmanager.setSingle(postag);
            } else if (number == "two") {
              postag = tagmanager.setDual(postag);
            } else if (number == "plural") {
              postag = tagmanager.setPlural(postag);

            } else {
              postag = tagmanager.setSingle(postag);

            }
            //  add Tanwin
            if (number == "one" && inflection == "nasb") {
              postag = tagmanager.setTanwin(postag);
            }

            //
            // for each potag generate a new token
            if (!tmpList.contains(postag)) {
              tmpList.add(postag);
              List<String> syhthesizedList = asList(synthesizer.synthesize(tk, postag));
              if (syhthesizedList != null && !syhthesizedList.isEmpty()) {
                inflectedList.addAll(syhthesizedList);
              }
            }
          }


        }
        return inflectedList;
        //    if(inflectedList.isEmpty())
        //    {
        //      return inflected + tmpList.toString();
        //    }
        //    return inflectedList.toString()+tmpList.toString();
        ////    return inflectedList.toString()+tmpList.toString();
        //  }

      }
    }

    private static int getPreviousPos(Map<String, String> args) {
      int previousWordPos = 0;
      if (args.get("previousPos") != null)
        try {
          if (args.get("previousPos") != null) {
            previousWordPos = Integer.valueOf(args.get("previousPos")) - 1;
          }
        } catch (NumberFormatException e) {
          throw new RuntimeException("Error parsing previousPos from : " + args.get("previousPos"));
        }
      return previousWordPos;

    }

    private static int getNextPos(Map<String, String> args, int size) {
      int nextPos = 0;
      try {
        nextPos = Integer.parseInt(args.getOrDefault("nextPos", "0"));
        // the next token is index with a negative offset
        if (nextPos < 0) {
          nextPos = size + nextPos;
        }
      } catch (NumberFormatException e) {
        return 0;
      }
      return nextPos;
    }
  }

  /**
   * Filter that maps suggestion from adverb to adjective.
   *
   * @since 5.8
   */
  public static class VerbToMafoulMutlaqFilter extends RuleFilter {

    private final ArabicTagger tagger = new ArabicTagger();
    private static final String FILE_NAME = "/ar/arabic_verb_masdar.txt";

    private final Map<String, List<String>> verb2masdarList = loadFromPath(FILE_NAME);
    private final ArabicSynthesizer synthesizer = new ArabicSynthesizer(new Arabic());


    @Nullable
    @Override
    public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {

      String verb = arguments.get("verb");
      List<String> verbLemmas = tagger.getLemmas(patternTokens[0], "verb");
      String adj = arguments.get("adj");

      // generate multiple masdar from verb lemmas list */
      List<String> inflectedMasdarList = new ArrayList<>();
      List<String> inflectedAdjList = new ArrayList<>();
      String inflectedAdjMasculine = synthesizer.inflectAdjectiveTanwinNasb(adj, false);
      String inflectedAdjfeminin = synthesizer.inflectAdjectiveTanwinNasb(adj, true);
      for (String lemma : verbLemmas) {
        // get sugegsted masdars lemmas
        List<String> msdrLemmaList = verb2masdarList.get(lemma);
        if (msdrLemmaList != null) {

          for (String msdr : msdrLemmaList) {
            if (msdr != null) {
              String inflectedMasdar = synthesizer.inflectMafoulMutlq(msdr);
              inflectedMasdarList.add(inflectedMasdar);
              String inflectedAdj = (msdr.endsWith(Character.toString(TEH_MARBUTA))) ? inflectedAdjfeminin : inflectedAdjMasculine;
              inflectedAdjList.add(inflectedAdj);
            }
          }
        }
      }
      RuleMatch newMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), match.getMessage(), match.getShortMessage());
      int i = 0;
      List<String> suggestionPhrases = new ArrayList<>();
      for (String msdr : inflectedMasdarList) {
        String sugPhrase = verb + " " + msdr + " " + inflectedAdjList.get(i);
        // Avoid redendency
        if (!suggestionPhrases.contains(sugPhrase)) {
          newMatch.addSuggestedReplacement(sugPhrase);
          suggestionPhrases.add(sugPhrase);
        }
        i++;
      }
      return newMatch;
    }

    protected static Map<String, List<String>> loadFromPath(String path) {
      return new SimpleReplaceDataLoader().loadWords(path);
    }
  }
}
