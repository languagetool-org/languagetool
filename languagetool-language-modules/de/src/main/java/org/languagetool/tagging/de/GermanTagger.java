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
package org.languagetool.tagging.de;

import com.google.common.base.Suppliers;
import gnu.trove.THashMap;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.spelling.CachingWordListLoader;
import org.languagetool.synthesis.GermanSynthesizer;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.CombiningTagger;
import org.languagetool.tagging.ManualTagger;
import org.languagetool.tagging.TaggedWord;
import org.languagetool.tokenizers.de.GermanCompoundTokenizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

import static org.languagetool.tools.StringTools.uppercaseFirstChar;

/**
 * German part-of-speech tagger, requires data file in <code>de/german.dict</code> in the classpath.
 * The POS tagset is described in
 * <a href="https://github.com/languagetool-org/languagetool/blob/master/languagetool-language-modules/de/src/main/resources/org/languagetool/resource/de/tagset.txt">tagset.txt</a>
 *
 * @author Marcin Milkowski, Daniel Naber
 */
public class GermanTagger extends BaseTagger {

  private static final List<String> allAdjGruTags = new ArrayList<>();
  static {
    for (String nomAkkGenDat : Arrays.asList("NOM", "AKK", "GEN", "DAT")) {
      for (String pluSin : Arrays.asList("PLU", "SIN")) {
        for (String masFemNeu : Arrays.asList("MAS", "FEM", "NEU")) {
          for (String defIndSol : Arrays.asList("DEF", "IND", "SOL")) {
            allAdjGruTags.add("ADJ:" + nomAkkGenDat + ":" + pluSin + ":" + masFemNeu + ":GRU:" + defIndSol);
          }
        }
      }
    }
  }

  // do not add noun tags to these words, e.g. don't add noun tags to "Wegstrecken" for weg_strecken from spelling.txt:
  private static final List<String> nounTagExpansionExceptions = Arrays.asList("Wegstrecken");

  private static final List<String> tagsForWeise = new ArrayList<>();
  static {
    // "kofferweise", "idealerweise" etc.
    tagsForWeise.add("ADJ:AKK:PLU:FEM:GRU:SOL");
    tagsForWeise.add("ADJ:AKK:PLU:MAS:GRU:SOL");
    tagsForWeise.add("ADJ:AKK:PLU:NEU:GRU:SOL");
    tagsForWeise.add("ADJ:AKK:SIN:FEM:GRU:DEF");
    tagsForWeise.add("ADJ:AKK:SIN:FEM:GRU:IND");
    tagsForWeise.add("ADJ:AKK:SIN:FEM:GRU:SOL");
    tagsForWeise.add("ADJ:AKK:SIN:NEU:GRU:DEF");
    tagsForWeise.add("ADJ:NOM:PLU:FEM:GRU:SOL");
    tagsForWeise.add("ADJ:NOM:PLU:MAS:GRU:SOL");
    tagsForWeise.add("ADJ:NOM:PLU:NEU:GRU:SOL");
    tagsForWeise.add("ADJ:NOM:SIN:FEM:GRU:DEF");
    tagsForWeise.add("ADJ:NOM:SIN:FEM:GRU:IND");
    tagsForWeise.add("ADJ:NOM:SIN:FEM:GRU:SOL");
    tagsForWeise.add("ADJ:NOM:SIN:MAS:GRU:DEF");
    tagsForWeise.add("ADJ:NOM:SIN:NEU:GRU:DEF");
    tagsForWeise.add("ADJ:PRD:GRU");
  }

  private final ManualTagger removalTagger;
  private static final Supplier<ExpansionInfos> expansionInfos = Suppliers.memoize(GermanTagger::initExpansionInfos);

  public static final GermanTagger INSTANCE = new GermanTagger();

  public GermanTagger() {
    super("/de/german.dict", Locale.GERMAN);
    removalTagger = (ManualTagger) ((CombiningTagger) getWordTagger()).getRemovalTagger();
  }

  private static ExpansionInfos initExpansionInfos() {
    Map<String, PrefixInfixVerb> verbInfos = new THashMap<>();
    Map<String, NominalizedVerb> nominalizedVerbInfos = new THashMap<>();
    Map<String, NominalizedGenitiveVerb> nominalizedGenVerbInfos = new THashMap<>();
    List<String> spellingWords = new CachingWordListLoader().loadWords("de/hunspell/spelling.txt");
    for (String line : spellingWords) {
      if (!line.contains("_") || line.endsWith("_in")) {
        continue;
      }
      String[] parts = line.replace("#.*", "").trim().split("_");
      String prefix = parts[0];
      String verbBaseform = parts[1];
      try {
        String[] forms = GermanSynthesizer.INSTANCE.synthesizeForPosTags(verbBaseform, s -> s.startsWith("VER:"));
        for (String form : forms) {
          if (!form.contains("ß")) {  // skip these, it's too risky to introduce old spellings like "gewußt" from the synthesizer
            verbInfos.put(prefix + form, new PrefixInfixVerb(prefix, "", verbBaseform));
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      verbInfos.put(prefix + "zu" + verbBaseform, new PrefixInfixVerb(prefix, "zu", verbBaseform));  //  "zu<verb>" is not part of forms from synthesizer
      nominalizedVerbInfos.put(uppercaseFirstChar(prefix) + verbBaseform,
        new NominalizedVerb(uppercaseFirstChar(prefix), verbBaseform));
      nominalizedGenVerbInfos.put(uppercaseFirstChar(prefix) + verbBaseform + "s",
        new NominalizedGenitiveVerb(uppercaseFirstChar(prefix), verbBaseform));
    }
    return new ExpansionInfos(verbInfos, nominalizedVerbInfos, nominalizedGenVerbInfos);
  }

  private List<TaggedWord> addStem(List<TaggedWord> analyzedWordResults, String stem) {
    List<TaggedWord> result = new ArrayList<>();
    for (TaggedWord tw : analyzedWordResults) {
      String lemma = tw.getLemma();
      if (stem.length() > 0 && stem.charAt(stem.length() - 1) != '-' && tw.getPosTag().startsWith("SUB")) {
        lemma = lemma.toLowerCase();
      }
      result.add(new TaggedWord(stem + lemma, tw.getPosTag()));
    }
    return result;
  }
  
  //Removes the irrelevant part of dash-linked words (SSL-Zertifikat -> Zertifikat)
  private String sanitizeWord(String word) {
    String result = word;

    //Find the last part of the word that is not nothing
    //Skip words ending in a dash as they'll be misrecognized
    if (!word.endsWith("-")) {
      String[] splitWord = word.split("-");
      String lastPart = splitWord.length > 1 && !splitWord[splitWord.length - 1].trim().equals("") ? splitWord[splitWord.length - 1] : word;

      //Find only the actual important part of the word
      List<String> compoundedWord = GermanCompoundTokenizer.getStrictInstance().tokenize(lastPart);
      if (compoundedWord.size() > 1 && StringTools.startsWithUppercase(word)) {  // don't uppercase last part of e.g. "vanillig-karamelligen"
        lastPart = uppercaseFirstChar(compoundedWord.get(compoundedWord.size() - 1));
      } else {
        lastPart = compoundedWord.get(compoundedWord.size() - 1);
      }

      //Only give result if the last part is either a noun or an adjective (or adjective written in Uppercase)
      List<TaggedWord> tagged = tag(lastPart);
      if (tagged.size() > 0 && (StringUtils.startsWithAny(tagged.get(0).getPosTag(), "SUB", "ADJ") || matchesUppercaseAdjective(lastPart))) {
        result = lastPart;
      }
    }
    return result;
  }

  /**
   * Return only the first reading of the given word or {@code null}.
   */
  @Nullable
  public AnalyzedTokenReadings lookup(String word) throws IOException {
    List<AnalyzedTokenReadings> result = tag(Collections.singletonList(word), false);
    AnalyzedTokenReadings atr = result.get(0);
    if (atr.getAnalyzedToken(0).getPOSTag() == null) {
      return null;
    }
    return atr;
  }

  public List<TaggedWord> tag(String word) {
    return getWordTagger().tag(word);
  }

  private boolean matchesUppercaseAdjective(String unknownUppercaseToken) {
    List<TaggedWord> temp = getWordTagger().tag(StringTools.lowercaseFirstChar(unknownUppercaseToken));
    return temp.size() > 0 && temp.get(0).getPosTag().startsWith("ADJ");
  }

  @Override
  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens) throws IOException {
    return tag(sentenceTokens, true);
  }

  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens, boolean ignoreCase) throws IOException {
    boolean firstWord = true;
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;
    int idxPos = 0;

    String prevWord = null;
    for (String word : sentenceTokens) {
      List<AnalyzedToken> readings = new ArrayList<>();
      List<TaggedWord> taggerTokens = null;
      // Gender star etc:
      String genderGap = "[*:_/]";
      if (idxPos+2 < sentenceTokens.size() && sentenceTokens.get(idxPos+1).matches(genderGap)) {
        if (sentenceTokens.get(idxPos+2).matches("in(nen)?|r|e")) {  // "jede*r", "sein*e"
          taggerTokens = new ArrayList<>();
          taggerTokens.addAll(getWordTagger().tag(word));
          taggerTokens.addAll(getWordTagger().tag(word + sentenceTokens.get(idxPos+2)));
        }
      }
      if (taggerTokens == null) {
        taggerTokens = getWordTagger().tag(word);
      }

      //Only first iteration. Consider ":" as a potential sentence start marker
      if ((firstWord || ":".equals(prevWord)) && taggerTokens.isEmpty() && ignoreCase) { // e.g. "Das" -> "das" at start of sentence
        taggerTokens = getWordTagger().tag(word.toLowerCase());
        firstWord = !StringUtils.isAlphanumeric(word);
      } else if (pos == 0 && ignoreCase) {   // "Haben", "Sollen", "Können", "Gerade" etc. at start of sentence
        taggerTokens.addAll(getWordTagger().tag(word.toLowerCase()));
      } else if (pos > 1 && taggerTokens.isEmpty() && ignoreCase) {
        int idx = sentenceTokens.indexOf(word);
        // add lowercase token readings to words at start of direct speech
        if (idx > 2 && sentenceTokens.get(idx-1).contentEquals("„") && sentenceTokens.get(idx-3).contentEquals(":")) {
          taggerTokens.addAll(getWordTagger().tag(word.toLowerCase()));
        }
      }

      if (taggerTokens.size() > 0) { //Word known, just add analyzed token to readings
        readings.addAll(getAnalyzedTokens(taggerTokens, word));
      } else { // Word not known, try to decompose it and use the last part for POS tagging:
        PrefixInfixVerb verbInfo = expansionInfos.get().verbInfos.get(word);
        NominalizedVerb nomVerbInfo = expansionInfos.get().nominalizedVerbInfos.get(word);
        NominalizedGenitiveVerb nomGenVerbInfo = expansionInfos.get().nominalizedGenVerbInfos.get(word);
        boolean addNounTags = !nounTagExpansionExceptions.contains(word);
        //String prefixVerbLastPart = prefixedVerbLastPart(word);   // see https://github.com/languagetool-org/languagetool/issues/2740
        if (verbInfo != null) {   // e.g. "herumgeben" with "herum_geben" in spelling.txt
          if (StringTools.startsWithLowercase(verbInfo.prefix)) {
            String noPrefixForm = word.substring(verbInfo.prefix.length() + verbInfo.infix.length());   // infix can be "zu"
            List<TaggedWord> tags = tag(noPrefixForm);
            for (TaggedWord tag : tags) {
              if (tag.getPosTag() != null && (tag.getPosTag().startsWith("VER:") || tag.getPosTag().startsWith("PA2:"))) {  // e.g. "schicke" is verb and adjective
                readings.add(new AnalyzedToken(word, tag.getPosTag(), verbInfo.prefix + tag.getLemma()));
              }
            }
          }
        } else if (nomVerbInfo != null && addNounTags) {
          // e.g. "herum_geben" in spelling.txt -> "(das) Herumgeben"
          readings.add(new AnalyzedToken(word, "SUB:NOM:SIN:NEU:INF", nomVerbInfo.prefix + nomVerbInfo.verbBaseform));
          readings.add(new AnalyzedToken(word, "SUB:AKK:SIN:NEU:INF", nomVerbInfo.prefix + nomVerbInfo.verbBaseform));
          readings.add(new AnalyzedToken(word, "SUB:DAT:SIN:NEU:INF", nomVerbInfo.prefix + nomVerbInfo.verbBaseform));
        } else if (nomGenVerbInfo != null && addNounTags) {
          // e.g. "herum_geben" in spelling.txt -> "(des) Herumgebens"
          readings.add(new AnalyzedToken(word, "SUB:GEN:SIN:NEU:INF", nomGenVerbInfo.prefix + nomGenVerbInfo.verbBaseform));
        /*} else if (prefixVerbLastPart != null) {   // "aufstöhnen" etc.
          List<TaggedWord> taggedWords = getWordTagger().tag(prefixVerbLastPart);
          String firstPart = word.replaceFirst(prefixVerbLastPart + "$", "");
          for (TaggedWord taggedWord : taggedWords) {
            readings.add(new AnalyzedToken(word, taggedWord.getPosTag(), firstPart+taggedWord.getLemma()));
          }*/
        } else if (isWeiseException(word)) {   // "idealerweise" etc. but not "überweise", "eimerweise"
          for (String tag : tagsForWeise) {
            readings.add(new AnalyzedToken(word, tag, word));
          }
        } else if (!StringUtils.isAllBlank(word)) {
          List<String> compoundParts = GermanCompoundTokenizer.getStrictInstance().tokenize(word);
          if (compoundParts.size() <= 1) {//Could not find simple compound parts
            // Recognize alternative imperative forms (e.g., "Geh bitte!" in addition to "Gehe bitte!")
            List<AnalyzedToken> imperativeFormList = getImperativeForm(word, sentenceTokens, pos);
            List<AnalyzedToken> substantivatedFormsList = getSubstantivatedForms(word, sentenceTokens);
            if (imperativeFormList.size() > 0) {
              readings.addAll(imperativeFormList);
            } else if (substantivatedFormsList.size() > 0) {
              readings.addAll(substantivatedFormsList);
            } else {
              if (StringUtils.startsWithAny(word, "bitter", "dunkel", "erz", "extra", "früh",
                "gemein", "hyper", "lau", "mega", "minder", "stock", "super", "tod", "ultra", "un", "ur")) {
                String lastPart = RegExUtils.removePattern(word, "^(bitter|dunkel|erz|extra|früh|gemein|grund|hyper|lau|mega|minder|stock|super|tod|ultra|u[nr]|voll)");
                if (lastPart.length() > 3) {
                  String firstPart = StringUtils.removeEnd(word, lastPart);
                  List<TaggedWord> taggedWords = getWordTagger().tag(lastPart);
                  for (TaggedWord taggedWord : taggedWords) {
                    if (!(firstPart.length() == 2 && taggedWord.getPosTag().startsWith("VER"))) {
                      readings.add(new AnalyzedToken(word, taggedWord.getPosTag(), firstPart+taggedWord.getLemma()));
                    }
                  }
                }
              }
              //Separate dash-linked words
              //Only check single word tokens and skip words containing numbers because it's unpredictable
              if (StringUtils.split(word, ' ').length == 1 && !Character.isDigit(word.charAt(0))) {
                String wordOrig = word;
                word = sanitizeWord(word);
                String wordStem = wordOrig.substring(0, wordOrig.length() - word.length());

                //Tokenize, start word uppercase if it's a result of splitting
                List<String> compoundedWord = GermanCompoundTokenizer.getStrictInstance().tokenize(word);
                if (compoundedWord.size() > 1) {
                  word = uppercaseFirstChar(compoundedWord.get(compoundedWord.size() - 1));
                } else {
                  word = compoundedWord.get(compoundedWord.size() - 1);
                }
                
                List<TaggedWord> linkedTaggerTokens = addStem(getWordTagger().tag(word), wordStem); //Try to analyze the last part found

                //Some words that are linked with a dash ('-') will be written in uppercase, even adjectives
                if (wordOrig.contains("-") && linkedTaggerTokens.isEmpty() && matchesUppercaseAdjective(word)) {
                  word = StringTools.lowercaseFirstChar(word);
                  linkedTaggerTokens = getWordTagger().tag(word);
                }

                word = wordOrig;
                
                boolean wordStartsUppercase = StringTools.startsWithUppercase(word);
                if (linkedTaggerTokens.isEmpty()) {
                  readings.add(getNoInfoToken(word));
                } else {
                  if (wordStartsUppercase) { //Choose between uppercase/lowercase Lemma
                    readings.addAll(getAnalyzedTokens(linkedTaggerTokens, word));
                  } else {
                    readings.addAll(getAnalyzedTokens(linkedTaggerTokens, word, compoundedWord));
                  }
                }
              } else {
                readings.add(getNoInfoToken(word));
              }
            }
          } else if (!(idxPos+2 < sentenceTokens.size() && sentenceTokens.get(idxPos+1).equals(".") && sentenceTokens.get(idxPos+2).matches("com|net|org|de|at|ch|fr|uk|gov"))) {  // TODO: find better way to ignore domains
            // last part governs a word's POS:
            String lastPart = compoundParts.get(compoundParts.size() - 1);
            if (StringTools.startsWithUppercase(word)) {
              lastPart = uppercaseFirstChar(lastPart);
            }
            List<TaggedWord> partTaggerTokens = getWordTagger().tag(lastPart);
            if (partTaggerTokens.isEmpty()) {
              readings.add(getNoInfoToken(word));
            } else {
              readings.addAll(getAnalyzedTokens(partTaggerTokens, word, compoundParts));
            }
          }
        }
        if (readings.isEmpty()) {
          readings.add(getNoInfoToken(word));
        }
      }
      tokenReadings.add(new AnalyzedTokenReadings(readings.toArray(new AnalyzedToken[0]), pos));
      pos += word.length();
      prevWord = word;
      idxPos++;
    }
    return tokenReadings;
  }

  @Nullable
  String prefixedVerbLastPart(String word) {
    // "aufstöhnen" (auf+stöhnen) etc.
    for (String prefix : VerbPrefixes.get()) {
      if (word.startsWith(prefix)) {
        List<TaggedWord> tags = tag(word.replaceFirst("^" + prefix, ""));
        if (tags.stream().anyMatch(k -> k.getPosTag() != null && k.getPosTag().startsWith("VER"))) {
          return word.substring(prefix.length());
        }
      }
    }
    return null;
  }

  boolean isWeiseException(String word) {
    if (word.endsWith("erweise")) {  // "idealerweise" etc.
      List<TaggedWord> tags = tag(StringUtils.removeEnd(word, "erweise"));
      return tags.stream().anyMatch(k -> k.getPosTag() != null && k.getPosTag().startsWith("ADJ"));
    }
    return false;
  }

  /*
   * Tag alternative imperative forms (e.g., "Geh bitte!" in addition to "Gehe bitte!")
   * To avoid false positives and conflicts with DE_CASE the tagging is restricted to
   * [a] words at the start of a sentence ("Geh bitte!") if the sentence counts more than one word
   * [b1] words preceded by ich/ihr/er/es/sie to catch some real errors ("Er geh jetzt.") by the new rule in rulegroup SUBJECT_VERB_AGREEMENT
   * [b2] words preceded by aber/nun/jetzt (e.g., "Bitte geh!", "Jetzt sag schon!" etc.)
   * @param word to be checked
   */
  private List<AnalyzedToken> getImperativeForm(String word, List<String> sentenceTokens, int pos) {
    int idx = sentenceTokens.indexOf(word);
    String previousWord = "";
    while (--idx > -1) {
      previousWord = sentenceTokens.get(idx);
      if (!StringUtils.isWhitespace(previousWord)) {
        break;
      }
    }
    if (!(pos == 0 && sentenceTokens.size() > 1)
        && !StringUtils.equalsAnyIgnoreCase(previousWord, "ich", "er", "es", "sie", "bitte", "aber", "nun", "jetzt", "„")) {
      return Collections.emptyList();
    }
    String w = pos == 0 || "„".equals(previousWord) ? word.toLowerCase() : word;
    List<TaggedWord> taggedWithE = getWordTagger().tag(w.concat("e"));
    for (TaggedWord tagged : taggedWithE) {
      if (tagged.getPosTag().startsWith("VER:IMP:SIN")) {
        // do not overwrite manually removed tags
        if (removalTagger == null || !removalTagger.tag(w).contains(tagged)) {
          return getAnalyzedTokens(Arrays.asList(tagged), word);
        }
        break;
      }
    }
    return Collections.emptyList();
  }

  /*
   * Tag substantivated adjectives and participles, which are currently tagged not tagged correctly
   * (e.g., "Verletzter" in "Ein Verletzter kam ins Krankenhaus" needs to be tagged as "SUB:NOM:SIN:MAS")
   * @param word to be checked
   */
  private List<AnalyzedToken> getSubstantivatedForms(String word, List<String> sentenceTokens) {
    if (word.endsWith("er")) {
      if (word.matches("\\d{4}+er")) {
        // e.g. "Den 2019er Wert hatten sie geschätzt"
        List<AnalyzedToken> list = new ArrayList<>();
        for (String tag : allAdjGruTags) {
          list.add(new AnalyzedToken(word, tag, word));
        }
        return list;
      }
      List<TaggedWord> lowerCaseTags = getWordTagger().tag(word.toLowerCase());
      // do not add tag words whose lower case variant is an adverb (e.g, "Früher") to avoid false negatives for DE_CASE
      if (lowerCaseTags.stream().anyMatch(t -> t.getPosTag().startsWith("ADV"))) {
        return Collections.emptyList();
      }
      int idx = sentenceTokens.indexOf(word);
      // is followed by an uppercase word? If 'yes', the word is probably not substantivated
      while (++idx < sentenceTokens.size()) {
        String nextWord = sentenceTokens.get(idx);
        if (StringUtils.isWhitespace(nextWord)) {
          continue;
        }
        if (nextWord.length() > 0 && (Character.isUpperCase(nextWord.charAt(0)) || "als".equals(nextWord))) {
          return Collections.emptyList();
        }
        break;
      }
      String femaleForm = word.substring(0, word.length()-1);
      List<TaggedWord> taggedFemaleForm = getWordTagger().tag(femaleForm);
      boolean isSubstantivatedForm = taggedFemaleForm.stream().anyMatch(t -> t.getPosTag().equals("SUB:NOM:SIN:FEM:ADJ"));
      if (isSubstantivatedForm) {
        List<AnalyzedToken> list = new ArrayList<>();
        list.add(new AnalyzedToken(word, "SUB:NOM:SIN:MAS:ADJ", word));
        list.add(new AnalyzedToken(word, "SUB:GEN:PLU:MAS:ADJ", word));
        return list;
      }
    }
    return Collections.emptyList();
  }

  private AnalyzedToken getNoInfoToken(String word) {
    return new AnalyzedToken(word, null, null);
  }

  private List<AnalyzedToken> getAnalyzedTokens(List<TaggedWord> taggedWords, String word) {
    List<AnalyzedToken> result = new ArrayList<>();
    for (TaggedWord taggedWord : taggedWords) {
      result.add(new AnalyzedToken(word, taggedWord.getPosTag(), taggedWord.getLemma()));
    }
    return result;
  }

  private List<AnalyzedToken> getAnalyzedTokens(List<TaggedWord> taggedWords, String word, List<String> compoundParts) {
    List<AnalyzedToken> result = new ArrayList<>();
    for (TaggedWord taggedWord : taggedWords) {
      if (taggedWord.getPosTag() != null && taggedWord.getPosTag().startsWith("VER:IMP")) {
        // ignore imperative, as otherwise e.g. "zehnfach" will be interpreted as a verb (zehn + fach)
        continue;
      }
      List<String> allButLastPart = compoundParts.subList(0, compoundParts.size() - 1);
      StringBuilder lemma = new StringBuilder();
      int i = 0;
      for (String s : allButLastPart) {
        lemma.append(i == 0 ? s : StringTools.lowercaseFirstChar(s));
        i++;
      }
      lemma.append(StringTools.lowercaseFirstChar(taggedWord.getLemma()));
      result.add(new AnalyzedToken(word, taggedWord.getPosTag(), lemma.toString()));
    }
    return result;
  }

  static class PrefixInfixVerb {
    String prefix;
    String infix;
    String verbBaseform;
    PrefixInfixVerb(String prefix, String infix, String verbBaseform) {
      this.prefix = prefix;
      this.infix = infix;
      this.verbBaseform = verbBaseform;
    }
  }

  static class NominalizedVerb {
    String prefix;
    String verbBaseform;
    NominalizedVerb(String prefix, String verbBaseform) {
      this.prefix = prefix;
      this.verbBaseform = verbBaseform;
    }
  }

  static class NominalizedGenitiveVerb {
    String prefix;
    String verbBaseform;
    NominalizedGenitiveVerb(String prefix, String verbBaseform) {
      this.prefix = prefix;
      this.verbBaseform = verbBaseform;
    }
  }

  static class ExpansionInfos {
    Map<String, PrefixInfixVerb> verbInfos;
    Map<String, NominalizedVerb> nominalizedVerbInfos;
    Map<String, NominalizedGenitiveVerb> nominalizedGenVerbInfos;
    ExpansionInfos(Map<String, PrefixInfixVerb> verbInfos, Map<String, NominalizedVerb> nominalizedVerbInfos,
                   Map<String, NominalizedGenitiveVerb> nominalizedGenVerbInfos) {
      this.verbInfos = verbInfos;
      this.nominalizedVerbInfos = nominalizedVerbInfos;
      this.nominalizedGenVerbInfos = nominalizedGenVerbInfos;
    }
  }

}
