/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import de.danielnaber.jwordsplitter.GermanWordSplitter;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.language.German;
import org.languagetool.rules.Example;
import org.languagetool.rules.spelling.hunspell.CompoundAwareHunspellRule;
import org.languagetool.rules.spelling.morfologik.MorfologikMultiSpeller;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.de.GermanCompoundTokenizer;
import org.languagetool.tools.StringTools;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class GermanSpellerRule extends CompoundAwareHunspellRule {

  public static final String RULE_ID = "GERMAN_SPELLER_RULE";

  // according to http://www.spiegel.de/kultur/zwiebelfisch/zwiebelfisch-der-gebrauch-des-fugen-s-im-ueberblick-a-293195.html
  private static final Pattern ENDINGS_NEEDING_FUGENS = Pattern.compile(".*(tum|ling|ion|tät|keit|schaft|sicht|ung|en)");
  private static final int MAX_EDIT_DISTANCE = 2;
  private static final int SUGGESTION_MIN_LENGTH = 2;
  private static final List<Replacement> REPL = Arrays.asList(
      // see de_DE.aff:
      new Replacement("f", "ph"),
      new Replacement("ph", "f"),
      new Replacement("ß", "ss"),
      new Replacement("ss", "ß"),
      new Replacement("s", "ss"),
      new Replacement("ss", "s"),
      new Replacement("i", "ie"),
      new Replacement("ie", "i"),
      new Replacement("ee", "e"),
      new Replacement("o", "oh"),
      new Replacement("oh", "o"),
      new Replacement("a", "ah"),
      new Replacement("ah", "a"),
      new Replacement("e", "eh"),
      new Replacement("eh", "e"),
      new Replacement("ae", "ä"),
      new Replacement("oe", "ö"),
      new Replacement("ue", "ü"),
      new Replacement("Ae", "Ä"),
      new Replacement("Oe", "Ö"),
      new Replacement("Ue", "Ü"),
      new Replacement("d", "t"),
      new Replacement("t", "d"),
      new Replacement("th", "t"),
      new Replacement("t", "th"),
      new Replacement("r", "rh"),
      new Replacement("ch", "k"),
      new Replacement("k", "ch"),
      // not in de_DE.aff (not clear what uppercase replacement we need...):
      new Replacement("F", "Ph"),
      new Replacement("Ph", "F")
  );

  private final LineExpander lineExpander = new LineExpander();
  private final GermanCompoundTokenizer compoundTokenizer;
  private final GermanWordSplitter splitter;
  private final Synthesizer synthesizer;
  private final Tagger tagger;

  public GermanSpellerRule(ResourceBundle messages, German language) {
    super(messages, language, language.getNonStrictCompoundSplitter(), getSpeller(language));
    addExamplePair(Example.wrong("LanguageTool kann mehr als eine <marker>nromale</marker> Rechtschreibprüfung."),
                   Example.fixed("LanguageTool kann mehr als eine <marker>normale</marker> Rechtschreibprüfung."));
    compoundTokenizer = language.getStrictCompoundTokenizer();
    tagger = language.getTagger();
    synthesizer = language.getSynthesizer();
    try {
      splitter = new GermanWordSplitter(false);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void init() throws IOException {
    super.init();
    String pattern = "(" + nonWordPattern.pattern() + "|(?<=\\d)\\-|\\-(?=\\d+))";
    nonWordPattern = Pattern.compile(pattern);
    needsInit = false;
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Override
  public List<String> getCandidates(String word) {
    List<List<String>> partList = splitter.getAllSplits(word);
    List<String> candidates = new ArrayList<>();
    for (List<String> parts : partList) {
      candidates.addAll(super.getCandidates(parts));
      if (parts.size() == 2 && !parts.get(0).endsWith("s")) {
        // so we get e.g. Einzahlungschein -> Einzahlungsschein
        candidates.add(parts.get(0) + "s" + parts.get(1));
      }
      if (parts.size() == 2 && parts.get(1).startsWith("s")) {
        // so we get e.g. Ordnungshütter -> Ordnungshüter (Ordnungshütter is split as Ordnung + shütter)
        String firstPart = parts.get(0);
        String secondPart = parts.get(1);
        candidates.addAll(super.getCandidates(Arrays.asList(firstPart + "s", secondPart.substring(1))));
      }
    }
    return candidates;
  }

  @Override
  protected boolean isProhibited(String word) {
    return word.startsWith("Standart-") || super.isProhibited(word);
  }

  @Override
  protected void addIgnoreWords(String origLine, Set<String> wordsToBeIgnored) {
    String line;
    if (language.getShortCodeWithCountryAndVariant().equals("de-CH")) {
      // hack: Swiss German doesn't use "ß" but always "ss" - replace this, otherwise
      // misspellings (from Swiss point-of-view) like "äußere" wouldn't be found:
      line = origLine.replace("ß", "ss");
    } else {
      line = origLine;
    }
    List<String> words = expandLine(line);
    for (String word : words) {
      super.addIgnoreWords(word, wordsToBeIgnored);
    }
  }

  @Override
  protected List<String> expandLine(String line) {
    return lineExpander.expandLine(line);
  }

  /*
   * @since 3.6
   */
  @Override
  public List<String> getSuggestions(String word) throws IOException {
    List<String> suggestions = super.getSuggestions(word);
    if (word.endsWith(".")) {
      // To avoid losing the "." of "word" if it is at the end of a sentence.
      suggestions.replaceAll(s -> s.endsWith(".") ? s : s + ".");
    }
    return suggestions;
  }

  @Nullable
  private static MorfologikMultiSpeller getSpeller(Language language) {
    if (!language.getShortCode().equals(Locale.GERMAN.getLanguage())) {
      throw new RuntimeException("Language is not a variant of German: " + language);
    }
    try {
      String morfoFile = "/de/hunspell/de_" + language.getCountries()[0] + ".dict";
      if (JLanguageTool.getDataBroker().resourceExists(morfoFile)) {
        // spell data will not exist in LibreOffice/OpenOffice context
        try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream("/de/hunspell/spelling.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(stream, "utf-8"))) {
          return new MorfologikMultiSpeller(morfoFile, new ExpandingReader(br), MAX_EDIT_DISTANCE);
        }
      } else {
        return null;
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not set up morfologik spell checker", e);
    }
  }

  @Override
  protected void filterForLanguage(List<String> suggestions) {
    if (language.getShortCodeWithCountryAndVariant().equals("de-CH")) {
      for (int i = 0; i < suggestions.size(); i++) {
        String s = suggestions.get(i);
        suggestions.set(i, s.replace("ß", "ss"));
      }
    }
    // This is not quite correct as it might remove valid suggestions that start with "-",
    // but without this we get too many strange suggestions that start with "-" for no apparent reason
    // (e.g. for "Gratifikationskrisem" -> "-Gratifikationskrisen"):
    suggestions.removeIf(s -> s.length() > 1 && s.startsWith("-"));
  }

  // Use hunspell-style replacements to get good suggestions for "heisse", namely "heiße" etc
  // TODO: remove this when the Morfologik speller can do this directly during tree iteration:
  @Override
  protected List<String> sortSuggestionByQuality(String misspelling, List<String> suggestions) {
    List<String> sorted1 = sortByReplacements(misspelling, suggestions);
    return sortByCase(misspelling, sorted1);
  }

  @Override
  protected boolean ignoreWord(List<String> words, int idx) throws IOException {
    boolean ignore = super.ignoreWord(words, idx);
    boolean ignoreUncapitalizedWord = !ignore && idx == 0 && super.ignoreWord(StringUtils.uncapitalize(words.get(0)));
    boolean ignoreByHyphen = false, ignoreHyphenatedCompound = false;
    if (!ignore && !ignoreUncapitalizedWord) {
      if (words.get(idx).contains("-")) {
        ignoreByHyphen = words.get(idx).endsWith("-") && ignoreByHangingHyphen(words, idx);
      }
      ignoreHyphenatedCompound = !ignoreByHyphen && ignoreCompoundWithIgnoredWord(words.get(idx));
    }
    return ignore || ignoreUncapitalizedWord || ignoreByHyphen || ignoreHyphenatedCompound;
  }

  @Override
  protected List<String> getAdditionalTopSuggestions(List<String> suggestions, String word) throws IOException {
    String w = StringUtils.removeEnd(word, ".");
    if ("unzwar".equals(w)) {
      return Collections.singletonList("und zwar");
    } else if ("desweiteren".equals(w)) {
      return Collections.singletonList("des Weiteren");
    } else if ("wieviel".equals(w)) {
      return Collections.singletonList("wie viel");
    } else if ("wieviele".equals(w)) {
      return Collections.singletonList("wie viele");
    } else if ("wievielen".equals(w)) {
      return Collections.singletonList("wie vielen");
    } else if ("Trons".equals(w)) {
      return Collections.singletonList("Trance");
    } else if ("einzigste".equals(w)) {
      return Collections.singletonList("einzige");
    } else if (word.endsWith("standart")) {
      return Collections.singletonList(word.replaceFirst("standart$", "standard"));
    } else if (word.endsWith("standarts")) {
      return Collections.singletonList(word.replaceFirst("standarts$", "standards"));
    } else if (word.equals("Rolladen")) {
      return Collections.singletonList("Rollladen");
    } else if (word.equals("Maßname")) {
      return Collections.singletonList("Maßnahme");
    } else if (word.equals("Maßnamen")) {
      return Collections.singletonList("Maßnahmen");
    } else if (word.equals("nanten")) {
      return Collections.singletonList("nannten");
    } else if (word.equals("Hobbies")) {
      return Collections.singletonList("Hobbys");
    } else if (!StringTools.startsWithUppercase(word)) {
      String ucWord = StringTools.uppercaseFirstChar(word);
      if (!suggestions.contains(ucWord) && !hunspellDict.misspelled(ucWord)) {
        // Hunspell doesn't always automatically offer the most obvious suggestion for compounds:
        return Collections.singletonList(ucWord);
      }
    }
    String verbSuggestion = getPastTenseVerbSuggestion(word);
    if (verbSuggestion != null) {
      return Collections.singletonList(verbSuggestion);
    }
    String participleSuggestion = getParticipleSuggestion(word);
    if (participleSuggestion != null) {
      return Collections.singletonList(participleSuggestion);
    }
    // hyphenated compounds words (e.g., "Netflix-Flm")
    if (suggestions.isEmpty() && word.contains("-")) {
      String[] words = word.split("-");
      if (words.length > 1) {
        List<List<String>> suggestionLists = new ArrayList<>(words.length);
        int startAt = 0, stopAt = words.length;
        if (super.ignoreWord(words[0] + "-" + words[1])) { // "Au-pair-Agentr"
          startAt = 2;
          suggestionLists.add(Collections.singletonList(words[0] + "-" + words[1]));
        }
        if (super.ignoreWord(words[words.length-2] + "-" + words[words.length-1])) { // "Seniren-Au-pair"
          stopAt = words.length-2;
        }
        for (int idx = startAt; idx < stopAt; idx++) {
          if (super.ignoreWord(words[idx])) {
            suggestionLists.add(Collections.singletonList(words[idx]));
          } else if (hunspellDict.misspelled(words[idx])) {
            List<String> list = sortSuggestionByQuality(words[idx], super.getSuggestions(words[idx]));
            suggestionLists.add(list);
          } else {
            suggestionLists.add(Collections.singletonList(words[idx]));
          }
        }
        if (stopAt < words.length-1) {
          suggestionLists.add(Collections.singletonList(words[words.length-2] + "-" + words[words.length-1]));
        }
        if (suggestionLists.size() <= 3) {  // avoid OOM on words like "free-and-open-source-and-cross-platform"
          List<String> additionalSuggestions = suggestionLists.get(0);
          for (int idx = 1; idx < suggestionLists.size(); idx++) {
            List<String> suggestionList = suggestionLists.get(idx);
            List<String> newList = new ArrayList<>(additionalSuggestions.size() * suggestionList.size());
            for (String additionalSuggestion : additionalSuggestions) {
              for (String aSuggestionList : suggestionList) {
                newList.add(additionalSuggestion + "-" + aSuggestionList);
              }
            }
            additionalSuggestions = newList;
          }
          return additionalSuggestions;
        }
      }
    }
    return Collections.emptyList();
  }

  // Get a correct suggestion for invalid words like greifte, denkte, gehte: useful for
  // non-native speakers and cannot be found by just looking for similar words.
  @Nullable
  private String getPastTenseVerbSuggestion(String word) {
    if (word.endsWith("e")) {
      // strip trailing "e"
      String wordStem = word.substring(0, word.length()-1);
      try {
        String lemma = baseForThirdPersonSingularVerb(wordStem);
        if (lemma != null) {
          AnalyzedToken token = new AnalyzedToken(lemma, null, lemma);
          String[] forms = synthesizer.synthesize(token, "VER:3:SIN:PRT:.*", true);
          if (forms.length > 0) {
            return forms[0];
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }

  @Nullable
  private String baseForThirdPersonSingularVerb(String word) throws IOException {
    List<AnalyzedTokenReadings> readings = tagger.tag(Collections.singletonList(word));
    for (AnalyzedTokenReadings reading : readings) {
      if (reading.hasPartialPosTag("VER:3:SIN:")) {
        return reading.getReadings().get(0).getLemma();
      }
    }
    return null;
  }

  // Get a correct suggestion for invalid words like geschwimmt, geruft: useful for
  // non-native speakers and cannot be found by just looking for similar words.
  @Nullable
  private String getParticipleSuggestion(String word) {
    if (word.startsWith("ge") && word.endsWith("t")) {
      // strip leading "ge" and replace trailing "t" with "en":
      String baseform = word.substring(2, word.length()-1) + "en";
      try {
        String participle = getParticipleForBaseform(baseform);
        if (participle != null) {
          return participle;
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }

  @Nullable
  private String getParticipleForBaseform(String baseform) throws IOException {
    AnalyzedToken token = new AnalyzedToken(baseform, null, baseform);
    String[] forms = synthesizer.synthesize(token, "VER:PA2:.*", true);
    if (forms.length > 0 && !hunspellDict.misspelled(forms[0])) {
      return forms[0];
    }
    return null;
  }

  private boolean ignoreByHangingHyphen(List<String> words, int idx) {
    String word = words.get(idx);
    String nextWord = getWordAfterEnumerationOrNull(words, idx+1);
    if (nextWord != null) {
      nextWord = StringUtils.removeEnd(nextWord, ".");
    }
    boolean isCompound = nextWord != null && compoundTokenizer.tokenize(nextWord).size() > 1;
    if (isCompound) {
      return !hunspellDict.misspelled(StringUtils.removeEnd(word, "-"));  // "Stil- und Grammatikprüfung" or "Stil-, Text- und Grammatikprüfung"
    }
    return false;
  }

  // for "Stil- und Grammatikprüfung", get "Grammatikprüfung" when at position of "Stil-"
  @Nullable
  private String getWordAfterEnumerationOrNull(List<String> words, int idx) {
    for (int i = idx; i < words.size(); i++) {
      String word = words.get(i);
      boolean inEnumeration = ",".equals(word) || "und".equals(word) || "oder".equals(word) || word.trim().isEmpty() || word.endsWith("-");
      if (!inEnumeration) {
        return word;
      }
    }
    return null;
  }

  // check whether a <code>word<code> is a valid compound (e.g., "Feynmandiagramm" or "Feynman-Diagramm")
  // that contains an ignored word from spelling.txt (e.g., "Feynman")
  private boolean ignoreCompoundWithIgnoredWord(String word) throws IOException{
    String[] words = word.split("-");
    if (words.length < 2) {
      // non-hyphenated compound (e.g., "Feynmandiagramm"):
      // only search for compounds that start(!) with a word from spelling.txt
      int end = super.startsWithIgnoredWord(word, true);
      if (end < 3) {
    	// support for geographical adjectives - although "süd/ost/west/nord" are not in spelling.txt 
    	// to accept sentences such as
    	// "Der westperuanische Ferienort, das ostargentinische Städtchen, das südukrainische Brauchtum, der nordägyptische Staudamm."
    	if (word.startsWith("ost") || word.startsWith("süd")) {
          end = 3;
    	} else if (word.startsWith("west") || word.startsWith("nord")) {
    	  end = 4;
    	} else {
    	  return false;
    	}
      }
      String ignoredWord = word.substring(0, end);
      String partialWord = word.substring(end);
      boolean needFugenS = ENDINGS_NEEDING_FUGENS.matcher(ignoredWord).matches();
      if (!needFugenS && partialWord.length() > 1) {
        return !hunspellDict.misspelled(partialWord) || !hunspellDict.misspelled(StringUtils.capitalize(partialWord));
      } else if (needFugenS && partialWord.length() > 2) {
        partialWord = partialWord.startsWith("s") ? partialWord.substring(1) : partialWord;
        return !hunspellDict.misspelled(partialWord) || !hunspellDict.misspelled(StringUtils.capitalize(partialWord));
      }
      return false;
    }
    // hyphenated compound (e.g., "Feynman-Diagramm"):
    boolean hasIgnoredWord = false;
    List<String> toSpellCheck = new ArrayList<>(3);
    String stripFirst = word.substring(words[0].length()+1); // everything after the first "-"
    String stripLast  = word.substring(0, word.length()-words[words.length-1].length()-1); // everything up to the last "-"

    if (super.ignoreWord(stripFirst)) { // e.g., "Senioren-Au-pair"
      hasIgnoredWord = true;
      if (!super.ignoreWord(words[0])) {
        toSpellCheck.add(words[0]);
      }
    } else if (super.ignoreWord(stripLast)) { // e.g., "Au-pair-Agentur"
      hasIgnoredWord = true;
      if (!super.ignoreWord(words[words.length-1])){
        toSpellCheck.add(words[words.length-1]);
      }
    } else {
      for (String word1 : words) {
        if (super.ignoreWord(word1)) {
          hasIgnoredWord = true;
        } else {
          toSpellCheck.add(word1);
        }
      }
    }

    if (hasIgnoredWord) {
      for (String w : toSpellCheck) {
        if (hunspellDict.misspelled(w)) {
          return false;
        }
      }
    }
    return hasIgnoredWord;
  }

  private List<String> sortByReplacements(String misspelling, List<String> suggestions) {
    List<String> result = new ArrayList<>();
    for (String suggestion : suggestions) {
      boolean moveSuggestionToTop = false;
      for (Replacement replacement : REPL) {
        String modifiedMisspelling = misspelling.replace(replacement.key, replacement.value);
        boolean equalsAfterReplacement = modifiedMisspelling.equals(suggestion);
        if (equalsAfterReplacement) {
          moveSuggestionToTop = true;
          break;
        }
      }
      if (!ignoreSuggestion(suggestion)) {
        if (moveSuggestionToTop) {
          // this should be preferred, as the replacements make it equal to the suggestion:
          result.add(0, suggestion);
        } else {
          result.add(suggestion);
        }
      }
    }
    return result;
  }

  private List<String> sortByCase(String misspelling, List<String> suggestions) {
    List<String> result = new ArrayList<>();
    for (String suggestion : suggestions) {
      if (misspelling.equalsIgnoreCase(suggestion)) {
        // this should be preferred - only case differs:
        result.add(0, suggestion);
      } else {
        result.add(suggestion);
      }
    }
    return result;
  }

  private boolean ignoreSuggestion(String suggestion) {
    String[] parts = suggestion.split(" ");
    if (parts.length > 1) {
      for (String part : parts) {
        if (part.length() < SUGGESTION_MIN_LENGTH) {
          return true;
        }
      }
    }
    return false;
  }

  private static class Replacement {
    final String key;
    final String value;
    private Replacement(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }

  static class ExpandingReader extends BufferedReader {

    private final List<String> buffer = new ArrayList<>();
    private final LineExpander lineExpander = new LineExpander();

    ExpandingReader(Reader in) {
      super(in);
    }

    @Override
    public String readLine() throws IOException {
      if (buffer.size() > 0) {
        return buffer.remove(0);
      } else {
        String line = super.readLine();
        if (line == null) {
          return null;
        }
        buffer.addAll(lineExpander.expandLine(line));
        return buffer.remove(0);
      }
    }
  }

}
