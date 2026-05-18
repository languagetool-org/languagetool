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
package org.languagetool.tagging.ca;

import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.rules.SimpleReplaceDataLoader;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.TaggedWord;
import org.languagetool.tools.StringTools;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Catalan Tagger
 *
 * @author Jaume Ortolà
 */
public class CatalanTagger extends BaseTagger {

  public static final CatalanTagger INSTANCE_VAL = new CatalanTagger(Languages.getLanguageForShortCode("ca-ES-valencia"));
  public static final CatalanTagger INSTANCE_CAT = new CatalanTagger(Languages.getLanguageForShortCode("ca-ES"));

  private static final Pattern ADJ_PART_FS = Pattern.compile("VMP00SF.|A[QO].[FC]S.");
  private static final Pattern VERB = Pattern.compile("V.+");
  private static final Pattern PREFIXES_FOR_VERBS = Pattern.compile("(auto)(.*[aeiouàéèíòóïü].+[aeiouàéèíòóïü].*)",
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern ADJECTIU_COMPOST = Pattern.compile("(.*)o-(.*.*)",
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern TRES_ADJECTIUS = Pattern.compile("(.*)o-(.*)o-(.*.*)",
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private final List<String> ALTRES_PREFIXOS = Arrays.asList("greco", "sino", "italo", "franco", "gal·lo", "luso",
    "germano", "hispano", "anglo", "àrabo", "austro", "belgo");
  private final List<String> NO_ALTRES_PREFIXOS = Arrays.asList("grego", "xineso", "italiano", "franceso",
    "portugueso", "angleso", "espanyolo", "alemanyo", "arabo", "austríaco", "bèlgico");
  private static final List<String> ALLUPPERCASE_EXCEPTIONS = Arrays.asList("ARNAU", "CRISTIAN", "TOMÀS");
  private boolean isValencian;

  private static final String endings = "a|ada|ades|am|ant|ar|ara|aran|arem|aren|ares|areu|aria|arien|aries|arà|aràs|aré|aríem|aríeu|assen|asses|assin|assis|at|ats|au|ava|aven|aves|e|ec|ega|eguda|egudes|eguem|eguen|eguera|egueren|egueres|egues|eguessen|eguesses|eguessin|eguessis|egueu|egui|eguin|eguis|egut|eguts|egué|eguérem|eguéreu|egués|eguéssem|eguésseu|eguéssim|eguéssiu|eguí|eix|eixem|eixen|eixent|eixeran|eixerem|eixeren|eixeres|eixereu|eixeria|eixerien|eixeries|eixerà|eixeràs|eixeré|eixeríem|eixeríeu|eixes|eixessen|eixesses|eixessin|eixessis|eixeu|eixi|eixia|eixien|eixies|eixin|eixis|eixo|eixé|eixérem|eixéreu|eixés|eixéssem|eixésseu|eixéssim|eixéssiu|eixí|eixíem|eixíeu|em|en|es|esc|esca|escuda|escudes|escut|escuts|esquem|esquen|esquera|esqueren|esqueres|esques|esquessen|esquesses|esquessin|esquessis|esqueu|esqui|esquin|esquis|esqué|esquérem|esquéreu|esqués|esquéssem|esquésseu|esquéssim|esquéssiu|esquí|essen|esses|essin|essis|eu|i|ia|ida|ides|ien|ies|iguem|igueu|im|in|int|ir|ira|iran|irem|iren|ires|ireu|iria|irien|iries|irà|iràs|iré|iríem|iríeu|is|isc|isca|isquen|isques|issen|isses|issin|issis|it|its|iu|ix|ixen|ixes|o|à|àrem|àreu|às|àssem|àsseu|àssim|àssiu|àvem|àveu|èixer|éixer|és|éssem|ésseu|éssim|éssiu|í|íem|íeu|írem|íreu|ís|íssem|ísseu|íssim|íssiu|ïs";
  private static final Pattern desinencies_1conj_0 = Pattern.compile("(.+?)(" + endings + ")", Pattern.CASE_INSENSITIVE);
  private static final Pattern desinencies_1conj_1 = Pattern.compile("(.+)(" + endings + ")", Pattern.CASE_INSENSITIVE);
  private static final Map<String, List<String>> wrongVerbs = new SimpleReplaceDataLoader().loadWords("/ca/replace_verbs.txt");

  public CatalanTagger(Language language) {
    super("/ca/ca-ES" + JLanguageTool.DICTIONARY_FILENAME_EXTENSION,
      new Locale("ca"), false);
    isValencian = "valencia".equals(language.getVariant());
  }

  @Override
  public boolean overwriteWithManualTagger() {
    return false;
  }

  @Override
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) {
    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;
    for (String originalWord : sentenceTokens) {
      // This hack allows all rules and dictionary entries to work with
      // typewriter apostrophe
      boolean containsTypographicApostrophe = false;
      if (originalWord.length() > 1) {
        if (originalWord.contains("’")) {
          containsTypographicApostrophe = true;
          originalWord = originalWord.replace("’", "'");
        }
      }
      String normalizedWord = StringTools.normalizeNFC(originalWord);
      final List<AnalyzedToken> analyzedTokenList = new ArrayList<>();
      final String lowerWord = normalizedWord.toLowerCase(locale);
      final boolean isLowercase = normalizedWord.equals(lowerWord);
      final boolean isMixedCase = StringTools.isMixedCase(normalizedWord);
      final boolean isAllUpper = StringTools.isAllUppercase(normalizedWord);
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(originalWord,
        getWordTagger().tag(normalizedWord));

      // normal case:
      addTokens(taggerTokens, analyzedTokenList);
      // tag non-lowercase (alluppercase or startuppercase), but not mixedcase
      // word with lowercase word tags:
      if (!isLowercase && !isMixedCase) {
        List<AnalyzedToken> lowerTaggerTokens = asAnalyzedTokenListForTaggedWords(originalWord,
          getWordTagger().tag(lowerWord));
        addTokens(lowerTaggerTokens, analyzedTokenList);
      }

      //tag all-uppercase proper nouns (ex. FRANÇA)
      if ((analyzedTokenList.isEmpty() || ALLUPPERCASE_EXCEPTIONS.contains(normalizedWord)) && isAllUpper) {
        final String firstUpper = StringTools.uppercaseFirstChar(lowerWord);
        List<AnalyzedToken> firstupperTaggerTokens = asAnalyzedTokenListForTaggedWords(originalWord,
          getWordTagger().tag(firstUpper));
        addTokens(firstupperTaggerTokens, analyzedTokenList);
      }
      // additional tagging with prefixes
      if (analyzedTokenList.isEmpty() && !isMixedCase) {
        addTokens(additionalTags(originalWord), analyzedTokenList);
      }
      // emoji
      if (analyzedTokenList.isEmpty() && StringTools.isEmoji(originalWord)) {
        analyzedTokenList.add(new AnalyzedToken(originalWord, "_emoji_", "_emoji_"));
      }
      // filter for Valencian POS tags
      filterAnalyzedTokensInPlace(analyzedTokenList);
      // incorrect verbs
      boolean isIncorrectVerb = false;
      if (analyzedTokenList.isEmpty()) {
        List<AnalyzedToken> tagsForIncorrectVerbs = additionalTagsForIncorrectVerbs(originalWord, lowerWord);
        if (!tagsForIncorrectVerbs.isEmpty()) {
          addTokens(tagsForIncorrectVerbs, analyzedTokenList);
          isIncorrectVerb = true;
        }
      }
      // if empty, add an analyzed token with no lemma and no postag
      if (analyzedTokenList.isEmpty()) {
        analyzedTokenList.add(new AnalyzedToken(originalWord, null, null));
      }
      AnalyzedTokenReadings atr = new AnalyzedTokenReadings(analyzedTokenList, pos);
      if (containsTypographicApostrophe) {
        atr.setTypographicApostrophe();
      }
      if (isIncorrectVerb) {
        atr.setChunkTags(Collections.singletonList(new ChunkTag("_incorrect_verb_")));
      }
      tokenReadings.add(atr);
      pos += originalWord.length();
    }
    return tokenReadings;
  }

  private void filterAnalyzedTokensInPlace(final List<AnalyzedToken> analyzedTokenList) {
    // Si som en valencià eliminem el "0" al principi de l'etiqueta
    // Si no som en valencià, ignorem les etiquetes que comencen per "0"
    if (analyzedTokenList.isEmpty()) return;
    final int size = analyzedTokenList.size();
    if (isValencian) {
      for (int i = 0; i < size; i++) {
        AnalyzedToken token = analyzedTokenList.get(i);
        String posTag = token.getPOSTag();
        if (posTag != null && posTag.length() > 0 && posTag.charAt(0) == '0') {
          analyzedTokenList.set(i, new AnalyzedToken(
            token.getToken(),
            posTag.substring(1),
            token.getLemma()
          ));
        }
      }
    } else {
      for (int i = size - 1; i >= 0; i--) {
        String posTag = analyzedTokenList.get(i).getPOSTag();
        if (posTag != null && posTag.length() > 0 && posTag.charAt(0) == '0') {
          analyzedTokenList.remove(i);
        }
      }
    }
  }

  @Nullable
  protected List<AnalyzedToken> additionalTags(String word) {
    List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
    //Any well-formed adverb with suffix -ment is tagged as an adverb (RG)
    //Adjectiu femení singular o participi femení singular + -ment
    final String lowerWord = StringTools.normalizeNFC(word.toLowerCase(locale));
    if (lowerWord.endsWith("ment")) {
      final String possibleAdj = lowerWord.replaceAll("^(.+)ment$", "$1");
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(possibleAdj,
        getWordTagger().tag(possibleAdj));
      for (AnalyzedToken taggerToken : taggerTokens) {
        final String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          final Matcher m = ADJ_PART_FS.matcher(posTag);
          if (m.matches()) {
            additionalTaggedTokens.add(new AnalyzedToken(word, "RG", lowerWord));
            return additionalTaggedTokens;
          }
        }
      }
    }
    //Any well-formed verb with prefixes is tagged as a verb copying the original tags
    Matcher matcher = PREFIXES_FOR_VERBS.matcher(word);
    if (matcher.matches()) {
      final String possibleVerb = StringTools.normalizeNFC(matcher.group(2).toLowerCase());
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(possibleVerb,
        getWordTagger().tag(possibleVerb));
      for (AnalyzedToken taggerToken : taggerTokens) {
        if (!taggerToken.getLemma().equals("nòmer")) {
          final String posTag = taggerToken.getPOSTag();
          if (posTag != null) {
            final Matcher m = VERB.matcher(posTag);
            if (m.matches()) {
              String lemma = matcher.group(1).toLowerCase().concat(taggerToken.getLemma());
              additionalTaggedTokens.add(new AnalyzedToken(word, posTag, lemma));
            }
          }
        }
      }
      return additionalTaggedTokens;
    }
    // folklòrico-popular
    matcher = ADJECTIU_COMPOST.matcher(word);
    if (matcher.matches()) {
      final String adj1 = matcher.group(1).toLowerCase();
      if (isValidAdjectiveForm(adj1)) {
        final String adj2 = matcher.group(2).toLowerCase();
        List<AnalyzedToken> atl2 = asAnalyzedTokenListForTaggedWords(adj2, getWordTagger().tag(adj2));
        for (AnalyzedToken at : atl2) {
          if (at.getPOSTag() != null && at.getPOSTag().startsWith("A")) {
            additionalTaggedTokens.add(new AnalyzedToken(word, at.getPOSTag(), adj1 + "o-" + at.getLemma()));
            return additionalTaggedTokens;
          }
        }
      }
    }
    // franco-americano-alemany
    matcher = TRES_ADJECTIUS.matcher(word);
    if (matcher.matches()) {
      final String adj1 = matcher.group(1).toLowerCase();
      final String adj2 = matcher.group(2).toLowerCase();
      if (isValidAdjectiveForm(adj1) && isValidAdjectiveForm(adj2)) {
        final String adj3 = matcher.group(3).toLowerCase();
        List<AnalyzedToken> atl3 = asAnalyzedTokenListForTaggedWords(adj3, getWordTagger().tag(adj3));
        for (AnalyzedToken at : atl3) {
          if (at.getPOSTag() != null && at.getPOSTag().startsWith("A")) {
            additionalTaggedTokens.add(new AnalyzedToken(word, at.getPOSTag(),
              adj1 + "o-" + adj2 + "o-" + at.getLemma()));
            return additionalTaggedTokens;
          }
        }
      }
    }

    // Any well-formed noun with prefix ex- is tagged as a noun copying the original tags
    /*if (word.startsWith("ex")) {
      final String lowerWord = word.toLowerCase(conversionLocale);
      final String possibleNoun = lowerWord.replaceAll("^ex(.+)$", "$1");
      List<AnalyzedToken> taggerTokens;
      taggerTokens = asAnalyzedTokenList(possibleNoun,dictLookup.lookup(possibleNoun));
      for (AnalyzedToken taggerToken : taggerTokens) {
        final String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          final Matcher m = NOUN.matcher(posTag);
          if (m.matches()) {
            String lemma = "ex".concat(taggerToken.getLemma());
            additionalTaggedTokens.add(new AnalyzedToken(word, posTag, lemma));
          }
        }
      }
      return additionalTaggedTokens;
    }*/
    // Interpret deprecated characters of "ela geminada"
    // U+013F LATIN CAPITAL LETTER L WITH MIDDLE DOT
    // U+0140 LATIN SMALL LETTER L WITH MIDDLE DOT
    if (word.contains("\u0140") || word.contains("\u013f")) {
      final String possibleWord = lowerWord.replace("\u0140", "l·");
      return asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(possibleWord));
    }

    // adjectives -iste in Valencian variant
    if (isValencian && lowerWord.endsWith("iste")) {
      final String possibleAdjNoun = lowerWord.replaceAll("^(.+)iste$", "$1ista");
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(possibleAdjNoun,
        getWordTagger().tag(possibleAdjNoun));
      for (AnalyzedToken taggerToken : taggerTokens) {
        final String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          if (posTag.equals("NCCS000")) {
            additionalTaggedTokens.add(new AnalyzedToken(word, "NCMS000", possibleAdjNoun));
          }
          if (posTag.equals("AQ0CS0")) {
            additionalTaggedTokens.add(new AnalyzedToken(word, "AQ0MS0", possibleAdjNoun));
          }
          if (!additionalTaggedTokens.isEmpty()) {
            return additionalTaggedTokens;
          }
        }
      }
    }
    return null;
  }

  private void addTokens(final List<AnalyzedToken> taggedTokens, final List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      l.addAll(taggedTokens);
    }
  }

  private boolean wordformHasPostag(String wordform, String postag) {
    List<AnalyzedToken> atl1 = asAnalyzedTokenListForTaggedWords(wordform, getWordTagger().tag(wordform));
    if (atl1 == null) {
      return false;
    }
    for (AnalyzedToken at : atl1) {
      if (at.getPOSTag() != null && at.getPOSTag().equals(postag)) {
        return true;
      }
    }
    return false;
  }

  // check: grec(o)-, russ(o)-...
  private boolean isValidAdjectiveForm(String wordStem) {
    return !NO_ALTRES_PREFIXOS.contains(wordStem + "o")
      && (wordformHasPostag(wordStem + "a", "AQ0FS0")
      || ALTRES_PREFIXOS.contains(wordStem + "o"));
  }


  /*
   * Get POS tags for incorrect verbs including inflected forms
   */
  private List<AnalyzedToken> additionalTagsForIncorrectVerbs(String originalWord, String lowerWord) {
    List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
    //enriure, enfotre...
    if (lowerWord.startsWith("en")) {
      List<TaggedWord> taggedWords = getWordTagger().tag(lowerWord.substring(2));
      List<TaggedWord> selectedTaggedWords = new ArrayList<>();
      String lemma = "";
      for (TaggedWord taggedWord : taggedWords) {
        if (taggedWord.getPosTag().startsWith("V")) {
          selectedTaggedWords.add(new TaggedWord("en" + taggedWord.getLemma(), taggedWord.getPosTag()));
          lemma = "en" + taggedWord.getLemma();
        }
      }
      if (!selectedTaggedWords.isEmpty() && List.of("enfotre", "enriure").contains(lemma)) {
        additionalTaggedTokens = asAnalyzedTokenListWithLemma(originalWord, lemma, selectedTaggedWords);
        return additionalTaggedTokens;
      }
    }
    for (Pattern pattern : List.of(desinencies_1conj_0, desinencies_1conj_1)) {
      Matcher m = pattern.matcher(lowerWord);
      if (!m.matches()) {
        continue;
      }
      String baseLexeme = m.group(1);
      String desinence = m.group(2);
      String adjustedLexeme = baseLexeme;
      List<String> lexemes = new ArrayList<>(List.of(baseLexeme));
      if (desinence.startsWith("e") || desinence.startsWith("é")
        || desinence.startsWith("i") || desinence.startsWith("ï")) {
        adjustedLexeme = adjustLexemeForSoftVowel(baseLexeme);
        if (!lexemes.contains(adjustedLexeme)) {
          lexemes.add(adjustedLexeme);
        }
      }
      if (desinence.startsWith("ï")) {
        desinence = "i" + desinence.substring(1);
      }
      additionalTaggedTokens = tryTag(originalWord, adjustedLexeme + "ar", "cant" + desinence);
      for (String lex : lexemes) {
        if (additionalTaggedTokens.isEmpty()) {
          additionalTaggedTokens = tryTag(originalWord, lex + "ir", "serv" + desinence);
        }
        if (additionalTaggedTokens.isEmpty() && lex.endsWith("g")) {
          additionalTaggedTokens = tryTag(originalWord, lex + "uir", "serv" + desinence);
        }
      }
      if (additionalTaggedTokens.isEmpty()) {
        String eixer = baseLexeme + "èixer";
        additionalTaggedTokens = tryTag(originalWord, eixer, "con" + desinence);
        if (additionalTaggedTokens.isEmpty()) {
          additionalTaggedTokens = tryTag(originalWord, eixer, "desmer" + desinence);
        }
      }
      if (!additionalTaggedTokens.isEmpty()) {
        break;
      }
    }
    return additionalTaggedTokens;
  }

  private List<AnalyzedToken> tryTag(String originalWord, String infinitive, String conjugated) {
    if (!wrongVerbs.containsKey(infinitive)) {
      return Collections.emptyList();
    }
    return asAnalyzedTokenListWithLemma(originalWord, infinitive, getWordTagger().tag(conjugated));
  }

  private String adjustLexemeForSoftVowel(String lexeme) {
    if (lexeme.endsWith("c"))   {
      return lexeme.substring(0, lexeme.length() - 1) + "ç";
    }
    if (lexeme.endsWith("qu"))  {
      return lexeme.substring(0, lexeme.length() - 2) + "c";
    }
    if (lexeme.endsWith("g"))   {
      return lexeme.substring(0, lexeme.length() - 1) + "j";
    }
    if (lexeme.endsWith("gü"))  {
      return lexeme.substring(0, lexeme.length() - 2) + "gu";
    }
    if (lexeme.endsWith("gu"))  {
      return lexeme.substring(0, lexeme.length() - 2) + "g";
    }
    return lexeme;
  }

  /*
   * Get analyzed token list with a new lemma
   */
  private List<AnalyzedToken> asAnalyzedTokenListWithLemma(String word, String lemma, List<TaggedWord> taggedWords) {
    List<AnalyzedToken> aTokenList = new ArrayList<>();
    if (taggedWords == null) {
      return aTokenList;
    }
    List<AnalyzedToken> atl = asAnalyzedTokenListForTaggedWords(word, taggedWords);
    for (AnalyzedToken at : atl) {
      if (at.getPOSTag().startsWith("V")) {
        // només verbs, no "cantada/NCFS000"
        aTokenList.add(new AnalyzedToken(word, at.getPOSTag(), lemma));
      }
    }
    return aTokenList;
  }

}


