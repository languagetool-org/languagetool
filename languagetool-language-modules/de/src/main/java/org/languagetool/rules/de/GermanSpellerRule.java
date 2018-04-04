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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.German;
import org.languagetool.rules.Example;
import org.languagetool.rules.spelling.hunspell.CompoundAwareHunspellRule;
import org.languagetool.rules.spelling.morfologik.MorfologikMultiSpeller;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.de.GermanCompoundTokenizer;
import org.languagetool.tools.StringTools;

import de.danielnaber.jwordsplitter.GermanWordSplitter;

public class GermanSpellerRule extends CompoundAwareHunspellRule {

  public static final String RULE_ID = "GERMAN_SPELLER_RULE";

  private static final int MAX_EDIT_DISTANCE = 2;
  
  // some exceptions for changes to the spelling in 2017 - just a workaround so we don't have to touch the binary dict:
  private static final Pattern PREVENT_SUGGESTION = Pattern.compile(
          ".*(?i:Majonäse|Bravur|Anschovis|Belkanto|Campagne|Frotté|Grisli|Jockei|Joga|Kalvinismus|Kanossa|Kargo|Ketschup|" +
          "Kollier|Kommunikee|Masurka|Negligee|Nessessär|Poulard|Varietee|Wandalismus|kalvinist).*");

  private final Set<String> wordsToBeIgnoredInCompounds = new HashSet<>();
  private static final Map<Pattern, Function<String,List<String>>> ADDITIONAL_SUGGESTIONS = new HashMap<>();
  static{
    put("[aA]wa", w -> Arrays.asList("AWA", "ach was", "aber"));
    put("[aA]lsallerersten?s", w -> Arrays.asList(w.replaceFirst("lsallerersten?s", "ls allererstes"), w.replaceFirst("lsallerersten?s", "ls Allererstes")));
    putRepl("(an|auf|ein|zu)gehangen(e[mnrs]?)?$", "hangen", "hängt");
    putRepl("[oO]key", "ey$", "ay");
    put("geupdate[dt]$", "upgedatet");
    put("[pP]roblemhaft(e[nmrs]?)?", w -> Arrays.asList(w.replaceFirst("haft", "behaftet"), w.replaceFirst("haft", "atisch")));
    put("rosane[mnrs]?$", w -> Arrays.asList("rosa", w.replaceFirst("^rosan", "rosafarben")));
    put("Erbung", w -> Arrays.asList("Vererbung", "Erbschaft"));
    put("Energiesparung", w -> Arrays.asList("Energieeinsparung", "Energieersparnis"));
    putRepl("for?melar(en?)?", "for?me", "Formu");
    putRepl("näste[mnrs]?$", "^näs", "nächs");
    putRepl("Erdogans?$", "^Erdogan", "Erdoğan");
    put("Germanistiker[ns]", "Germanisten");
    putRepl("Germanistikerin(nen)?", "Germanistiker", "Germanist");
    putRepl("[eE]rhöherung(en)?", "[eE]rhöherung", "Erhöhung");
    putRepl("[aA]ufjedenfall", "jedenfall$", " jeden Fall");
    putRepl("^funk?z[ou]nier.+", "funk?z[ou]nier", "funktionier");
    putRepl("[wW]öruber", "öru", "orü");
    put("Bequemheit", "Bequemlichkeit");
    put("[mM]issionarie?sie?rung", "Missionierung");
    put("[sS]chee?selonge?", "Chaiselongue");
    put("Re[kc]amiere", "Récamière");
    put("legen[td]lich", "lediglich");
    put("[mM]illion(en)?mal", w -> Collections.singletonList(StringTools.uppercaseFirstChar(w.replaceFirst("mal", " Mal"))));
    put("desweitere[nm]", "des Weiteren");
    putRepl("einzigste[mnrs]?", "einzigst", "einzig");
    putRepl("[iI]nterkurell(e[nmrs]?)?", "ku", "kultu");
    putRepl("ubera(g|sch)end(e[nmrs]?)?", "uber", "überr");
    putRepl("[wW]olt$", "lt", "llt");
    putRepl("[zZ]uende", "ue", "u E");
    putRepl("[iI]nbälde", "nb", "n B");
    putRepl("[lL]etztenendes", "ene", "en E");
    putRepl("[nN]achwievor", "wievor", " wie vor");
    putRepl("[uU]nswar", "swar", "d zwar");
    putRepl("[wW]aschte(s?t)?", "aschte", "usch");
    putRepl("[wW]aschten", "ascht", "usch");
    putRepl("Probiren?", "ir", "ier");
    putRepl("[gG]esetztreu(e[nmrs]?)?", "tz", "tzes");
    putRepl("[wW]ikich(e[nmrs]?)?", "k", "rkl");
    putRepl("[uU]naufbesichtigt(e[nmrs]?)?", "aufbe", "beauf");
    putRepl("[nN]utzvoll(e[nmrs]?)?", "utzvoll", "ützlich");
    putRepl("Lezte[mnrs]?", "Lez", "Letz");
    putRepl("Makeups?", "up", "-up");
    putRepl("Add-?Ons?", "Add-?On", "Add-on");
    putRepl("Internetkaffees?", "kaffee", "café");
    putRepl("[gG]ehorsamkeitsverweigerung(en)?", "[gG]ehorsamkeit", "Gehorsam");
    putRepl("[wW]ochende[ns]?", "[wW]ochend", "Wochenend");
    putRepl("[kK]ongratulier(en?|t(en?)?|st)", "[kK]on", "");
    putRepl("[wWkKdD]an$", "n$", "nn");
    putRepl("geh?neh?m[ie]gung(en)?", "geh?neh?m[ie]gung", "Genehmigung");
    putRepl("Korrigierung(en)?", "igierung", "ektur");
    putRepl("[nN]ocheimal", "eimal", " einmal");
    putRepl("[aA]benzu", "enzu", " und zu");
    putRepl("[kK]onflikation(en)?", "[kK]onfli", "Kompli");
    putRepl("[mM]itanader", "ana", "einan");
    putRepl("[qQ]ualitäts?bewußt(e[mnrs]?)?", "ts?bewußt", "tsbewusst");
    putRepl("[vV]oraussichtig(e[nmrs]?)?", "sichtig", "sichtlich");
    putRepl("[gG]leichrechtig(e[nmrs]?)?", "rechtig", "berechtigt");
    putRepl("[uU]nnützlich(e[nmrs]?)?", "nützlich", "nütz");
    putRepl("[rR]eligiösisch(e[nmrs]?)?", "isch", "");
    putRepl("[fF]olklorisch(e[nmrs]?)?", "isch", "istisch");
    putRepl("todesbedroh(end|lich)(e[nmrs]?)?", "todes", "lebens");
    putRepl("^[uU]nabsichtig(e[nmrs]?)?", "ig", "lich");
    putRepl("([eE]r|[bB]e|unter)?hälst", "hälst", "hältst");
    put("[wW]ohlfühlseins?", w -> Arrays.asList("Wellness", w.replaceFirst("[wW]ohlfühlsein", "Wohlbefinden"), w.replaceFirst("[wW]ohlfühlsein", "Wohlfühlen")));
    putRepl("[sS]chmett?e?rling(s|en?)?", "[sS]chmett?e?rling", "Schmetterling");
    putRepl("^[eE]inlamie?nie?r(st|en?|(t(e[nmrs]?)?))?", "^einlamie?nie?r", "laminier");
    putRepl("[bB]ravurös(e[nrms]?)?", "vur", "vour");
    putRepl("[aA]ss?ecoires?", "[aA]ss?ec", "Access");
    putRepl("[aA]ufwechse?lungsreich(er|st)?(e[nmrs]?)?", "ufwechse?lung", "bwechslung");
    putRepl("[iI]nordnung", "ordnung", " Ordnung");
    putRepl("[wW]ienerschnitzel[ns]?", "[wW]ieners", "Wiener S");
    putRepl("[kK]oxial(e[nmrs]?)?", "x", "ax");
    putRepl("[dD]urs?chnitt?lich(e[nmrs]?)?", "s?chnitt?", "chschnitt");
    putRepl("[oO]rganisativ(e[nmrs]?)?", "tiv", "torisch");
    putRepl("[kK]ontaktfreundlich(e[nmrs]?)?", "ndlich", "dig");
    put("[oO]r?ganisazion", "Organisation");
    put("[oO]rganisative", "Organisation");
    putRepl("[hH]eilei[td]s?", "[hH]eilei[td]", "Highlight");
    putRepl("[mM]atschscheiben?", "[mM]atschsch", "Mattsch");
    put("schafen?", w -> Arrays.asList(w.replaceFirst("sch", "schl"), w.replaceFirst("af", "arf"), w.replaceFirst("af", "aff")));
    putRepl("[hH]ofen?", "of", "off");
    putRepl("[sS]ommerverien?", "[sS]ommerverien?", "Sommerferien");
    putRepl("[rR]ecourcen?", "[rR]ec", "Ress");
    putRepl("[fF]amm?ill?i?[aä]risch(e[mnrs]?)?", "amm?ill?i?[aä]risch", "amiliär");
    putRepl("Sim-Karten?", "^Sim", "SIM");
    putRepl("[aA]leine", "l", "ll");
    putRepl("Kaput", "t", "tt");
    putRepl("[fF]estell(s?t|en?)", "est", "estst");
    put("[kK]onn?ekt?schen", "Connection");
    put("Naricht", "Nachricht");
    put("Getrixe", "Getrickse");
    put("Ausage", "Aussage");
    put("gelessen", "gelesen");
    put("Kanst", "Kannst");
    put("Unwohlbefinden", "Unwohlsein");
    put("leiwagen", "Leihwagen");
    put("krahn", "Kran");
    put("[hH]ifi", "Hi-Fi");
    put("chouch", "Couch");
    put("eh?rgeit?z", "Ehrgeiz");
    put("solltes", "solltest");
    put("geklabt", "geklappt");
    put("tuhen", "tun");
    put("ccm", "cm³");
    put("Kilimand?jaro", "Kilimandscharo");
    put("berücksichtung", "Berücksichtigung");
    put("artzt?", "Arzt");
    put("[tT]h?elepath?ie", "Telepathie");
    put("Wi-?Fi-Dire[ck]t", "Wi-Fi Direct");
    put("gans", "ganz");
    put("Pearl-Harbou?r", "Pearl Harbor");
    put("[aA]utonomität", "Autonomie");
    put("[fF]r[uü]h?st[uü]c?k", "Frühstück");
    put("zucc?h?inis?", "Zucchini");
    put("[mM]itag", "Mittag");
    put("Lexion", "Lexikon");
    put("[mM]otorisation", "Motorisierung");
    put("[mM]enegment", "Management");
    put("abgeschaffen", "abgeschafft");
    put("Verschiden", "Verschieden");
    put("Anschovis", "Anchovis");
    put("Bravur", "Bravour");
    put("Grisli", "Grizzly");
    put("Grislibär", "Grizzlybär");
    put("Grislibären", "Grizzlybären");
    put("Frotté", "Frottee");
    put("Joga", "Yoga");
    put("Kalvinismus", "Calvinismus");
    put("Kollier", "Collier");
    put("Kolliers", "Colliers");
    put("Ketschup", "Ketchup");
    put("Kommunikee", "Kommuniqué");
    put("Negligee", "Negligé");
    put("Nessessär", "Necessaire");
    put("passee", "passé");
    put("Varietee", "Varieté");
    put("Varietees", "Varietés");
    put("Wandalismus", "Vandalismus");
    put("Campagne", "Kampagne");
    put("Campagnen", "Kampagnen");
    put("Jockei", "Jockey");
    put("Roulett", "Roulette");
  }

  private static void putRepl(String wordPattern, String pattern, String replacement) {
    ADDITIONAL_SUGGESTIONS.put(Pattern.compile(wordPattern), w -> Collections.singletonList(w.replaceFirst(pattern, replacement)));
  }

  private static void put(String pattern, String replacement) {
    ADDITIONAL_SUGGESTIONS.put(Pattern.compile(pattern), w -> Collections.singletonList(replacement));
  }

  private static void put(String pattern, Function<String, List<String>> f) {
    ADDITIONAL_SUGGESTIONS.put(Pattern.compile(pattern), f);
  }

  private static final GermanWordSplitter splitter = getSplitter();
  private static GermanWordSplitter getSplitter() {
    try {
      return new GermanWordSplitter(false);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private final LineExpander lineExpander = new LineExpander();
  private final GermanCompoundTokenizer compoundTokenizer;
  private final Synthesizer synthesizer;
  private final Tagger tagger;

  public GermanSpellerRule(ResourceBundle messages, German language) {
    super(messages, language, language.getNonStrictCompoundSplitter(), getSpeller(language));
    addExamplePair(Example.wrong("LanguageTool kann mehr als eine <marker>nromale</marker> Rechtschreibprüfung."),
                   Example.fixed("LanguageTool kann mehr als eine <marker>normale</marker> Rechtschreibprüfung."));
    compoundTokenizer = language.getStrictCompoundTokenizer();
    tagger = language.getTagger();
    synthesizer = language.getSynthesizer();
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
    } else if (origLine.endsWith("-*")) {
      // words whose line ends with "-*" are only allowed in hyphenated compounds
      wordsToBeIgnoredInCompounds.add(origLine.substring(0, origLine.length() - 2));
      return;
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
        String path = "/de/hunspell/spelling.txt";
        try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path);
             BufferedReader br = new BufferedReader(new InputStreamReader(stream, "utf-8"))) {
          return new MorfologikMultiSpeller(morfoFile, new ExpandingReader(br), path, MAX_EDIT_DISTANCE);
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
      if (misspelling.equalsIgnoreCase(suggestion) || suggestion.contains(" ")) {
        // this should be preferred - only case differs || prefer e.g. "vor allem":
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
    boolean ignoreByHyphen = false;
    boolean ignoreHyphenatedCompound = false;
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
    String suggestion;
    if ("WIFI".equalsIgnoreCase(word)) {
      return Collections.singletonList("Wi-Fi");
    } else if ("genomen".equals(word)) {
      return Collections.singletonList("genommen");
    } else if ("Preis-Leistungsverhältnis".equals(word)) {
      return Collections.singletonList("Preis-Leistungs-Verhältnis");
    } else if ("ausversehen".equals(word)) {
      return Collections.singletonList("aus Versehen");
    } else if ("getz".equals(word)) {
      return Arrays.asList("jetzt", "geht's");
    } else if ("Trons".equals(word)) {
      return Collections.singletonList("Trance");
    } else if (word.endsWith("ibelkeit")) {
      suggestion = word.replaceFirst("elkeit$", "ilität");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.endsWith("aquise")) {
      suggestion = word.replaceFirst("aquise$", "akquise");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
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
    } else if (word.endsWith("tips")) {
      suggestion = word.replaceFirst("tips$", "tipps");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.endsWith("tip")) {
      suggestion = word + "p";
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.endsWith("oullie")) {
      suggestion = word.replaceFirst("oullie$", "ouille");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.startsWith("[dD]urschnitt")) {
      suggestion = word.replaceFirst("^urschnitt", "urchschnitt");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.startsWith("Bundstift")) {
      suggestion = word.replaceFirst("^Bundstift", "Buntstift");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.matches("[aA]llmähll?i(g|ch)(e[mnrs]?)?")) {
      suggestion = word.replaceFirst("llmähll?i(g|ch)", "llmählich");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.matches(".*[mM]ajonäse.*")) {
      suggestion = word.replaceFirst("ajonäse", "ayonnaise");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.matches(".*[rR]es(a|er)[vw]i[he]?rung(en)?")) {
      suggestion = word.replaceFirst("es(a|er)[vw]i[he]?rung", "eservierung");
      if (!hunspellDict.misspelled(suggestion)) { // suggest e.g. 'Ticketreservierung', but not 'Blödsinnsquatschreservierung'
        return Collections.singletonList(suggestion);
      }
    } else if (word.matches("[rR]eschaschier.+")) {
      suggestion = word.replaceFirst("schaschier", "cherchier");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.matches(".*[lL]aborants$")) {
      suggestion = word.replaceFirst("ts$", "ten");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.matches("[pP]roff?ess?ion([äe])h?ll?(e[mnrs]?)?")) {
      suggestion = word.replaceFirst("roff?ess?ion([äe])h?l{1,2}", "rofessionell");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.matches("[vV]erstehendniss?(es?)?")) {
      suggestion = word.replaceFirst("[vV]erstehendnis", "Verständnis");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.matches("diagno[sz]ier.*")) {
      suggestion = word.replaceAll("gno[sz]ier", "gnostizier");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.matches(".*eiss.*")) {
      suggestion = word.replaceAll("eiss", "eiß");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.matches(".*uess.*")) {
      suggestion = word.replaceAll("uess", "üß");
      if (!hunspellDict.misspelled(suggestion)) {
        return Collections.singletonList(suggestion);
      }
    } else if (word.matches("bi[sß][ij]en")) {
      return Collections.singletonList("bisschen");
    } else if (word.equals("gin")) {
      return Collections.singletonList("ging");
    } else if (word.equals("dh") || word.equals("dh.")) {
      return Collections.singletonList("d.\u202fh.");
    } else if (word.equals("ua") || word.equals("ua.")) {
      return Collections.singletonList("u.\u202fa.");
    } else if (word.equals("zb") || word.equals("zb.")) {
      return Collections.singletonList("z.\u202fB.");
    } else if (word.equals("uvm") || word.equals("uvm.")) {
      return Collections.singletonList("u.\u202fv.\u202fm.");
    } else if (word.equals("udgl") || word.equals("udgl.")) {
      return Collections.singletonList("u.\u202fdgl.");
    } else if (word.equals("Ruhigkeit")) {
      return Collections.singletonList("Ruhe");
    } else if (word.equals("angepreist")) {
      return Collections.singletonList("angepriesen");
    } else if (word.equals("halo")) {
      return Collections.singletonList("hallo");
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
    } else if (word.endsWith("ies")) {
      if (word.equals("Stories")) {
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
      } else if (word.endsWith("parties")) {
        suggestion = word.replaceFirst("parties$", "partys");
        if (!hunspellDict.misspelled(suggestion)) {
          return Collections.singletonList(suggestion);
        }
      }
    } else if (word.equals("Hallochen")) {
      return Arrays.asList("Hallöchen", "hallöchen");
    } else if (word.equals("hallochen")) {
      return Collections.singletonList("hallöchen");
    } else if (word.equals("ok")) {
      return Arrays.asList("okay", "O.\u202fK."); // Duden-like suggestion with no-break space
    } else if (word.equals("gesuchen")) {
      return Arrays.asList("gesuchten", "gesucht");
    } else if (word.equals("Germanistiker")) {
      return Arrays.asList("Germanist", "Germanisten");
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
    } else if (word.matches("Email[a-zäöü]{5,}")) {
      String suffix = word.substring(5);
      if (hunspellDict.misspelled(suffix)) {
        List<String> suffixSuggestions = hunspellDict.suggest(suffix);
        suffix = suffixSuggestions.isEmpty() ? suffix : suffixSuggestions.get(0);
      }
      return Collections.singletonList("E-Mail-"+Character.toUpperCase(suffix.charAt(0))+suffix.substring(1));
    } else if (word.equals("wiederspiegeln")) {
      return Collections.singletonList("widerspiegeln");
    } else if (word.equals("ch")) {
        return Collections.singletonList("ich");
    } else {
      for (Pattern p : ADDITIONAL_SUGGESTIONS.keySet()) {
        if (p.matcher(word).matches()) {
          return ADDITIONAL_SUGGESTIONS.get(p).apply(word);
        }
      }
    }
    if (!StringTools.startsWithUppercase(word)) {
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
        String partialWord = words[0] + "-" + words[1];
        if (super.ignoreWord(partialWord) || wordsToBeIgnoredInCompounds.contains(partialWord)) { // "Au-pair-Agentr"
          startAt = 2;
          suggestionLists.add(Collections.singletonList(words[0] + "-" + words[1]));
        }
        partialWord = words[words.length-2] + "-" + words[words.length-1];
        if (super.ignoreWord(partialWord) || wordsToBeIgnoredInCompounds.contains(partialWord)) { // "Seniren-Au-pair"
          stopAt = words.length-2;
        }
        for (int idx = startAt; idx < stopAt; idx++) {
          if (hunspellDict.misspelled(words[idx])) {
            List<String> list = sortSuggestionByQuality(words[idx], super.getSuggestions(words[idx]));
            suggestionLists.add(list);
          } else {
            suggestionLists.add(Collections.singletonList(words[idx]));
          }
        }
        if (stopAt < words.length-1) {
          suggestionLists.add(Collections.singletonList(partialWord));
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
          // avoid overly long lists of suggestions (we just take the first results, although we don't know whether they are better):
          return additionalSuggestions.subList(0, Math.min(5, additionalSuggestions.size()));
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

  private boolean ignoreByHangingHyphen(List<String> words, int idx) throws IOException {
    String word = words.get(idx);
    String nextWord = getWordAfterEnumerationOrNull(words, idx+1);
    nextWord = StringUtils.removeEnd(nextWord, ".");

    boolean isCompound = nextWord != null && (compoundTokenizer.tokenize(nextWord).size() > 1 || nextWord.indexOf("-") > 0);
    if (isCompound) {
      word = StringUtils.removeEnd(word, "-");
      boolean isMisspelled = hunspellDict.misspelled(word);  // "Stil- und Grammatikprüfung" or "Stil-, Text- und Grammatikprüfung"
      if (isMisspelled && (super.ignoreWord(word) || wordsToBeIgnoredInCompounds.contains(word))) {
        isMisspelled = false;
      } else if (isMisspelled && word.endsWith("s") && isNeedingFugenS(StringUtils.removeEnd(word, "s"))) {
        // Vertuschungs- und Bespitzelungsmaßnahmen: remove trailing "s" before checking "Vertuschungs" so that the spell checker finds it
        isMisspelled = hunspellDict.misspelled(StringUtils.removeEnd(word, "s"));
      }
      return !isMisspelled;
    }
    return false;
  }

  private boolean isNeedingFugenS (String word) {
	// according to http://www.spiegel.de/kultur/zwiebelfisch/zwiebelfisch-der-gebrauch-des-fugen-s-im-ueberblick-a-293195.html
    return StringUtils.endsWithAny(word, "tum", "ling", "ion", "tät", "keit", "schaft", "sicht", "ung", "en");
  }

  // for "Stil- und Grammatikprüfung", get "Grammatikprüfung" when at position of "Stil-"
  @Nullable
  private String getWordAfterEnumerationOrNull(List<String> words, int idx) {
    for (int i = idx; i < words.size(); i++) {
      String word = words.get(i);
      if (!(word.endsWith("-") || StringUtils.equalsAny(word, ",", "und", "oder") || word.trim().isEmpty())) {
        return word;
      }
    }
    return null;
  }

  // check whether a <code>word<code> is a valid compound (e.g., "Feynmandiagramm" or "Feynman-Diagramm")
  // that contains an ignored word from spelling.txt (e.g., "Feynman")
  private boolean ignoreCompoundWithIgnoredWord(String word) throws IOException {
    if (!StringTools.startsWithUppercase(word) && !StringUtils.startsWithAny(word, "nord", "west", "ost", "süd", "α-", "β-", "ɣ-")) {
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
      boolean isCandidateForNonHyphenatedCompound = !StringUtils.isAllUpperCase(ignoredWord) && (StringUtils.isAllLowerCase(partialWord) || ignoredWord.endsWith("-"));
      boolean needFugenS = isNeedingFugenS(ignoredWord);
      if (isCandidateForNonHyphenatedCompound && !needFugenS && partialWord.length() > 1) {
        return !hunspellDict.misspelled(partialWord) || !hunspellDict.misspelled(StringUtils.capitalize(partialWord));
      } else if (isCandidateForNonHyphenatedCompound && needFugenS && partialWord.length() > 2) {
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

    if (super.ignoreWord(stripFirst) || wordsToBeIgnoredInCompounds.contains(stripFirst)) { // e.g., "Senioren-Au-pair"
      hasIgnoredWord = true;
      if (!super.ignoreWord(words[0])) {
        toSpellCheck.add(words[0]);
      }
    } else if (super.ignoreWord(stripLast) || wordsToBeIgnoredInCompounds.contains(stripLast)) { // e.g., "Au-pair-Agentur"
      hasIgnoredWord = true;
      if (!super.ignoreWord(words[words.length-1])){
        toSpellCheck.add(words[words.length-1]);
      }
    } else {
      for (String word1 : words) {
        if (super.ignoreWord(word1) || wordsToBeIgnoredInCompounds.contains(word1)) {
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
      if (buffer.isEmpty()) {
        String line = super.readLine();
        if (line == null) {
          return null;
        }
        buffer.addAll(lineExpander.expandLine(line));
      }
      return buffer.remove(0);
    }
  }

  @Override
  protected boolean isQuotedCompound (AnalyzedSentence analyzedSentence, int idx, String token) {
    if (idx > 3 && token.startsWith("-")) {
      return "“".equals(analyzedSentence.getTokens()[idx-1].getToken()) &&
          "„".equals(analyzedSentence.getTokens()[idx-3].getToken());
    }
    return false;
  }
}
