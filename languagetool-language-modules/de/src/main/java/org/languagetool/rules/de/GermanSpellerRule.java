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
import java.util.stream.Collectors;

public class GermanSpellerRule extends CompoundAwareHunspellRule {

  public static final String RULE_ID = "GERMAN_SPELLER_RULE";

  // according to http://www.spiegel.de/kultur/zwiebelfisch/zwiebelfisch-der-gebrauch-des-fugen-s-im-ueberblick-a-293195.html
  private static final Pattern ENDINGS_NEEDING_FUGENS = Pattern.compile(".*(tum|ling|ion|tät|keit|schaft|sicht|ung|en)");
  private static final int MAX_EDIT_DISTANCE = 2;
  
  // some exceptions for changes to the spelling in 2017 - just a workaround so we don't have to touch the binary dict:
  private static final Pattern PREVENT_SUGGESTION = Pattern.compile(
          ".*(?i:Majonäse|Bravur|Anschovis|Belkanto|Campagne|Frotté|Grisli|Jokei|Joga|Kalvinismus|Kanossa|Kargo|Ketschup|" +
          "Kollier|Kommunikee|Masurka|Negligee|Nessessär|Poulard|Varietee|Wandalismus|kalvinist).*");

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
    String pattern = "(" + nonWordPattern.pattern() + "|(?<=\\d)-|-(?=\\d+))";
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
  protected void addIgnoreWords(String origLine) {
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
      super.addIgnoreWords(word);
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
    suggestions = suggestions.stream().filter(k -> !PREVENT_SUGGESTION.matcher(k).matches() && !k.endsWith("roulett")).collect(Collectors.toList());
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
    // Remove suggestions like "Mafiosi s" and "Mafiosi s.":
    suggestions.removeIf(s -> Arrays.stream(s.split(" ")).anyMatch(k -> k.matches("\\w\\p{Punct}?")));
    // This is not quite correct as it might remove valid suggestions that start with "-",
    // but without this we get too many strange suggestions that start with "-" for no apparent reason
    // (e.g. for "Gratifikationskrisem" -> "-Gratifikationskrisen"):
    suggestions.removeIf(s -> s.length() > 1 && s.startsWith("-"));
  }

  @Override
  protected List<String> sortSuggestionByQuality(String misspelling, List<String> suggestions) {
    List<String> result = new ArrayList<>();
    for (String suggestion : suggestions) {
      if (misspelling.equalsIgnoreCase(suggestion)) {
        // this should be preferred - only case differs:
        result.add(0, suggestion);
      } else if (suggestion.contains(" ")) {
        // prefer e.g. "vor allem":
        result.add(0, suggestion);
      } else {
        result.add(suggestion);
      }
    }
    return result;
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
    String suggestion;
    if ("WIFI".equals(w) || "wifi".equals(w)) {
      return Collections.singletonList("Wi-Fi");
    } else if ("ausversehen".equals(w)) {
      return Collections.singletonList("aus Versehen");
    } else if ("getz".equals(w)) {
      return Arrays.asList("jetzt", "geht's");
    } else if ("Trons".equals(w)) {
      return Collections.singletonList("Trance");
    } else if (w.matches("desweitere[nm]")) {
      return Collections.singletonList("des Weiteren");
    } else if (word.matches("einzigste[mnrs]?")) {
      return Collections.singletonList(word.replaceFirst("^einzigst", "einzig"));
    } else if (word.endsWith("standart")) {
      suggestion = word.replaceFirst("standart$", "standard");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.endsWith("standarts")) {
      suggestion = word.replaceFirst("standarts$", "standards");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.endsWith("parties")) {
      suggestion = word.replaceFirst("parties$", "partys");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.endsWith("derbies")) {
      suggestion = word.replaceFirst("derbies$", "derbys");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.endsWith("stories")) {
      suggestion = word.replaceFirst("stories$", "storys");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.endsWith("tip")) {
      suggestion = word.replaceFirst("tip$", "tipp");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.endsWith("tips")) {
      suggestion = word.replaceFirst("tips$", "tipps");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.endsWith("oullie")) {
      suggestion = word.replaceFirst("oullie$", "ouille");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.startsWith("Bundstift")) {
      suggestion = word.replaceFirst("^Bundstift", "Buntstift");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.matches(".*[mM]ajonäse.*")) {
      suggestion = word.replaceFirst("ajonäse", "ayonnaise");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.matches(".*[lL]aborants$")) {
      suggestion = word.replaceFirst("ts$", "ten");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.matches("interkurell(e[nmrs]?)?")) {
      suggestion = word.replaceFirst("ku", "kultu");
      return Collections.singletonList(suggestion);
    } else if (word.equals("wolt")) {
      return Collections.singletonList("wollt");
    } else if (word.equals("angepreist")) {
      return Collections.singletonList("angepriesen");
    } else if (word.equals("halo")) {
      return Collections.singletonList("hallo");
    } else if (word.equals("zuende")) {
      return Collections.singletonList("zu Ende");
    } else if (word.equals("zumindestens")) {
      return Collections.singletonList("zumindest");
    } else if (word.equals("ca")) {
      return Collections.singletonList("ca.");
    } else if (word.equals("Jezt")) {
      return Collections.singletonList("Jetzt");
    } else if (word.equals("Rolladen")) {
      return Collections.singletonList("Rollladen");
    } else if (word.equals("Maßname")) {
      return Collections.singletonList("Maßnahme");
    } else if (word.equals("Maßnamen")) {
      return Collections.singletonList("Maßnahmen");
    } else if (word.equals("nanten")) {
      return Collections.singletonList("nannten");
    } else if (word.equals("Stories")) {
      return Collections.singletonList("Storys");
    } else if (word.equals("Lobbies")) {
      return Collections.singletonList("Lobbys");
    } else if (word.equals("Hobbies")) {
      return Collections.singletonList("Hobbys");
    } else if (word.equals("Parties")) {
      return Collections.singletonList("Partys");
    } else if (word.equals("Babies")) {
      return Collections.singletonList("Babys");
    } else if (word.equals("Ladies")) {
      return Collections.singletonList("Ladys");
    } else if (word.equals("Hallochen")) {
      return Arrays.asList("Hallöchen", "hallöchen");
    } else if (word.equals("hallochen")) {
      return Collections.singletonList("hallöchen");
    } else if (word.matches("[mM]issionarie?sie?rung")) {
      return Collections.singletonList("Missionierung");
    } else if (word.matches("[sS]chee?selonge?")) {
      return Collections.singletonList("Chaiselongue");
    } else if (word.matches("Re[kc]amiere")) {
      return Collections.singletonList("Récamière");
    } else if (word.matches("legen[td]lich")) {
      return Collections.singletonList("lediglich");
    } else if (word.matches("[mM]illion(en)?mal")) {
      suggestion = word.replaceFirst("^million", "Million");
      suggestion = suggestion.replaceFirst("mal$", "");
      return Collections.singletonList(suggestion + " Mal");
    } else if (word.equals("ok")) {
      return Arrays.asList("okay", "O.\u202fK."); // Duden-like suggestion with no-break space
    } else if (word.equals("Germanistiker")) {
      return Arrays.asList("Germanist", "Germanisten");
    } else if (word.matches("Germanistiker[ns]")) {
      return Collections.singletonList("Germanisten");
    } else if (word.matches("Germanistikerin(nen)?")) {
      return Collections.singletonList(word.replaceFirst("Germanistiker", "Germanist"));
    } else if (word.matches("[eE]rhöherung")) {
      return Collections.singletonList("Erhöhung");
    } else if (word.matches("[eE]rhöherungen")) {
      return Collections.singletonList("Erhöhungen");
    } else if (word.matches("[aA]ufjedenfall")) {
      suggestion = word.replaceFirst("jedenfall$", "");
      return Collections.singletonList(suggestion + " jeden Fall");
    } else if (word.matches("^funk?z[ou]nier.+")) {
      suggestion = word.replaceFirst("funk?z[ou]nier", "funktionier");
      return Collections.singletonList(suggestion);
    } else if (word.equals("Wöruber")) {
      return Collections.singletonList("Worüber");
    } else if (word.equals("par")) {
      return Collections.singletonList("paar");
    } else if (word.equals("vllt")) {
      return Collections.singletonList("vielleicht");
    } else if (word.equals("iwie")) {
      return Collections.singletonList("irgendwie");
    } else if (word.equals("sry")) {
      return Collections.singletonList("sorry");
    } else if (word.equals("Zynik")) {
      return Collections.singletonList("Zynismus");
    } else if (word.matches("[aA]wa")) {
      return Arrays.asList("AWA", "ach was", "aber");
    } else if (word.equals("ch")) {
      return Collections.singletonList("ich");
    } else if (word.matches("aufgehangen(e[mnrs]?)?$")) {
      return Collections.singletonList(word.replaceFirst("hangen", "hängt"));
    } else if (word.matches("rosane[mnrs]?$")) {
      return Arrays.asList("rosa", word.replaceFirst("^rosan", "rosafarben"));
    } else if (word.matches("geupdate[dt]$")) {
      return Collections.singletonList("upgedatet");
    } else if (word.matches("näste[mnrs]?$")) {
      return Collections.singletonList(word.replaceFirst("^näs", "nächs"));
    } else if (word.matches("Erdogans?$")) {
      return Collections.singletonList(word.replaceFirst("^Erdogan", "Erdoğan"));
    } else if (word.matches("Email[a-zäöü]{5,}")) {
      String suffix = word.substring(5);
      if (hunspellDict.misspelled(suffix)) {
        List<String> suffixSuggestions = hunspellDict.suggest(suffix);
        suffix = suffixSuggestions.isEmpty() ? suffix : suffixSuggestions.get(0);
      }
      return Collections.singletonList("E-Mail-"+Character.toUpperCase(suffix.charAt(0))+suffix.substring(1));
    } else if (word.equals("wiederspiegeln")) {
      return Collections.singletonList("widerspiegeln");
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
    if (!StringTools.startsWithUppercase(word) && !word.matches("(nord|ost|süd|west).*")) {
      // otherwise stuff like "rumfangreichen" gets accepted
      return false;
    }
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
