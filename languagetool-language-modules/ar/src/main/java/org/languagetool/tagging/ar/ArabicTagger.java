/* LanguageTool, a natural language style checker
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
package org.languagetool.tagging.ar;

import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tools.ArabicStringTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @since 4.9
 */
public class ArabicTagger extends BaseTagger {

  private final ArabicTagManager tagmanager = new ArabicTagManager();
  private boolean newStylePronounTag = false;

  public ArabicTagger() {
    super("/ar/arabic.dict", new Locale("ar"));
  }


  @Override
  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens) {
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    IStemmer dictLookup = new DictionaryLookup(getDictionary());
    int pos = 0;
    for (String word : sentenceTokens) {
      List<AnalyzedToken> l = new ArrayList<>();
      String striped = ArabicStringTools.removeTashkeel(word);
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(striped));
      addTokens(taggerTokens, l);
      // additional tagging with prefixes
      // if not a stop word add more stemming
      if (!isStopWord(taggerTokens)) {
        // test all possible tags
        addTokens(additionalTags(word, dictLookup), l);
      }
      if (l.isEmpty()) {
        l.add(new AnalyzedToken(word, null, null));
      }
      tokenReadings.add(new AnalyzedTokenReadings(l, pos));
      pos += word.length();
    }
    return tokenReadings;
  }

  @Nullable
  protected List<AnalyzedToken> additionalTags(String word, IStemmer stemmer) {
    String striped = ArabicStringTools.removeTashkeel(word);
    List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
    List<Integer> prefixIndexList = getPrefixIndexList(striped);
    List<Integer> suffixIndexList = getSuffixIndexList(striped);

    for (int i : prefixIndexList) {
      for (int j : suffixIndexList) {
        // avoid default case of returned word as it
        if ((i == 0) && (j == striped.length())) {
          continue;
        }
        // get stem return a list, to generate some variants for stems.
        List<String> stemsList = getStem(striped, i, j);
        List<String> tags = getTags(striped, i, j);

        for (String stem : stemsList) {
          List<AnalyzedToken> taggerTokens;
          taggerTokens = asAnalyzedTokenList(stem, stemmer.lookup(stem));

          for (AnalyzedToken taggerToken : taggerTokens) {
            String posTag = taggerToken.getPOSTag();
            // modify tags in postag, return null if not compatible
            posTag = tagmanager.modifyPosTag(posTag, tags);

            if (posTag != null) {
              additionalTaggedTokens.add(new AnalyzedToken(word, posTag, taggerToken.getLemma()));
            }
          } // for taggerToken
        } // for stem in stems
      } // for j
    } // for i
    return additionalTaggedTokens;
  }

  private void addTokens(List<AnalyzedToken> taggedTokens, List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      l.addAll(taggedTokens);
    }
  }


  private List<Integer> getSuffixIndexList(String possibleWord) {
    List<Integer> suffixIndexes = new ArrayList<>();
    suffixIndexes.add(possibleWord.length());
    int suffixPos = possibleWord.length();
    if (possibleWord.endsWith("ك")
      || possibleWord.endsWith("ها")
      || possibleWord.endsWith("هما")
      || possibleWord.endsWith("كما")
      || possibleWord.endsWith("هم")
      || possibleWord.endsWith("هن")
      || possibleWord.endsWith("كم")
      || possibleWord.endsWith("كن")
      || possibleWord.endsWith("نا")
    ) {
      if (possibleWord.endsWith("ك")) {
        suffixPos -= 1;
      } else if (possibleWord.endsWith("هما") || possibleWord.endsWith("كما")) {
        suffixPos -= 3;
      } else {
        suffixPos -= 2;
      }
      suffixIndexes.add(suffixPos);
    }
    return suffixIndexes;
  }

  private List<Integer> getPrefixIndexList(String possibleWord) {
    List<Integer> prefixIndexes = new ArrayList<>();
    prefixIndexes.add(0);
    int prefixPos;

    // four letters
    if (possibleWord.startsWith("وكال")
      || possibleWord.startsWith("وبال")
      || possibleWord.startsWith("فكال")
      || possibleWord.startsWith("فبال")
    ) {
      prefixPos = 4;
      prefixIndexes.add(prefixPos);
    }

    // three letters
    if (possibleWord.startsWith("ولل") // هذه حالة ول+ال
      || possibleWord.startsWith("فلل")
      || possibleWord.startsWith("فال")
      || possibleWord.startsWith("وال")
      || possibleWord.startsWith("بال")
      || possibleWord.startsWith("كال")
    ) {
      prefixPos = 3;
      prefixIndexes.add(prefixPos);
    }

    // two letters
    if (possibleWord.startsWith("لل")  // حالة ل+ال أي حرفان زائدان
      || possibleWord.startsWith("وك")
      || possibleWord.startsWith("ول")
      || possibleWord.startsWith("وب")
      || possibleWord.startsWith("فك")
      || possibleWord.startsWith("فل")
      || possibleWord.startsWith("فب")
      || possibleWord.startsWith("ال")
      //  حالة الفعل المضارع، السين فقط ما يؤخذ
      || possibleWord.startsWith("فسأ")
      || possibleWord.startsWith("فسن")
      || possibleWord.startsWith("فسي")
      || possibleWord.startsWith("فست")
      || possibleWord.startsWith("وسأ")
      || possibleWord.startsWith("وسن")
      || possibleWord.startsWith("وسي")
      || possibleWord.startsWith("وست")

    ) {
      prefixPos = 2;
      prefixIndexes.add(prefixPos);
    }

    // one letter
    if (possibleWord.startsWith("ك")
      || possibleWord.startsWith("ل")
      || possibleWord.startsWith("ب")
      || possibleWord.startsWith("و")
      || possibleWord.startsWith("ف")
      || possibleWord.startsWith("سأ")  //  حالة الفعل المضارع، السين فقط ما يؤخذ
      || possibleWord.startsWith("سن")
      || possibleWord.startsWith("سي")
      || possibleWord.startsWith("ست")
    ) {
      prefixPos = 1;
      prefixIndexes.add(prefixPos);
    }

    // get prefixe
    return prefixIndexes;
  }


  private List<String> getTags(String word, int posStart, int posEnd) {
    List<String> tags = new ArrayList<>();
    // extract tags from word
    String prefix = getPrefix(word, posStart);
    String suffix = getSuffix(word, posEnd);
    // prefixes
    // first place
    if (prefix.startsWith("و") || prefix.startsWith("ف")) {
      tags.add("CONJ;W");
      prefix = prefix.replaceAll("^[وف]", "");

    }
    // second place
    if (prefix.startsWith("ك")) {
      tags.add("JAR;K");
    } else if (prefix.startsWith("ل")) {
      tags.add("JAR;L");
    } else if (prefix.startsWith("ب")) {
      tags.add("JAR;B");
    } else if (prefix.startsWith("س")) {
      tags.add("ISTIQBAL;S");
    }
    // last place
    if (prefix.endsWith("ال")
      || prefix.endsWith("لل")
    ) {
      tags.add("PRONOUN;D");
    }
    // suffixes
    if (!newStylePronounTag) {
      switch (suffix) {
        case "ني":
        case "نا":
        case "ك":
        case "كما":
        case "كم":
        case "كن":
        case "ه":
        case "ها":
        case "هما":
        case "هم":
        case "هن":
          tags.add("PRONOUN;H");
          break;
      }
    } else // if newStyle
    {
      switch (suffix) {
        case "ني":
          tags.add("PRONOUN;b");
          break;
        case "نا":
          tags.add("PRONOUN;c");
          break;
        case "ك":
          tags.add("PRONOUN;d");
          break;
        case "كما":
          tags.add("PRONOUN;e");
          break;
        case "كم":
          tags.add("PRONOUN;f");
          break;
        case "كن":
          tags.add("PRONOUN;g");
          break;
        case "ه":
          tags.add("PRONOUN;H");
          break;
        case "ها":
          tags.add("PRONOUN;i");
          break;
        case "هما":
          tags.add("PRONOUN;j");
          break;
        case "هم":
          tags.add("PRONOUN;k");
          break;
        case "هن":
          tags.add("PRONOUN;n");
      }
    }


    return tags;
  }

  /**
   * @return test if word has stopword tagging
   */
  private boolean isStopWord(List<AnalyzedToken> taggerTokens) {
    // if one token is stop word
    for (AnalyzedToken tok : taggerTokens) {
      if (tok != null && tagmanager.isStopWord(tok.getPOSTag())) {
        return true;
      }
    }
    return false;
  }

  private String getPrefix(String word, int pos) {
    return word.substring(0, pos);
  }

  private String getSuffix(String word, int pos) {
    return word.substring(pos);
  }

  private List<String> getStem(String word, int posStart, int posEnd) {
    // get prefix
    // extract only stem+suffix, the suffix ill be replaced by pronoun model
    List<String> stemList = new ArrayList<>();
    String stem = word.substring(posStart);
    if (posEnd != word.length()) { // convert attached pronouns to one model form
      stem = stem.replaceAll("(ك|ها|هما|هم|هن|كما|كم|كن|نا|ي)$", "ه");
    }
    // correct some stems
    // correct case of للاسم
    String prefix = getPrefix(word, posStart);
    if (prefix.endsWith("لل")) {
      stemList.add("ل" + stem); // للاعب => لل- لاعب
    }

    stemList.add(stem);  // لاسم أو ل+لاعب
    return stemList;
  }


  /*
   * a temporary attribute to be compatible with existing pronoun tag style
   */
  public void enableNewStylePronounTag() {
    newStylePronounTag = true;
  }


  /**
   * @return if have a flag which is a noun/verb and has proclitics, return the first prefix named procletic letters for this case
   */
  public String getProclitic(AnalyzedToken token) {
    String postag = token.getPOSTag();
    String word = token.getToken();
    if (postag.isEmpty())
      return "";
    // if the word is Verb
    // extract conjuction and IStiqbal procletic
    String prefix = "";
    if (tagmanager.isVerb(postag)) {
      char conjflag = tagmanager.getFlag(postag, "CONJ");
      char istqbalflag = tagmanager.getFlag(postag, "ISTIQBAL");
      // if the two flags are set, return 2 letters prefix
      int prefixLength = 0;
      if (conjflag == 'W')
        prefixLength += 1;
      if (istqbalflag == 'S')
        prefixLength += 1;
      prefix = getPrefix(word, prefixLength);
    } else if (tagmanager.isNoun(postag)) {
      char conjflag = tagmanager.getFlag(postag, "CONJ");
      char jarflag = tagmanager.getFlag(postag, "JAR");
      // if the two flags are set, return 2 letters prefix
      int prefixLength = 0;
      if (conjflag != '-')
        prefixLength += 1;
      if (jarflag != '-')
        prefixLength += 1;
      //
      if (tagmanager.isDefinite(postag)) {
        if (jarflag == 'L') {
          // case of لل+بيت
          prefixLength += 1;
        } else {
          // case of ال+بيت
          prefixLength += 2;
        }
      }
      prefix = getPrefix(word, prefixLength);
    }
    return prefix;
  }

  /**
   * @return if have a flag which is a noun and has pronoun, return the suffix letters for this case
   */
  public String getEnclitic(AnalyzedToken token) {
    String postag = token.getPOSTag();
    String word = token.getToken();
    if (postag.isEmpty()) {
      return "";
    }
    char flag = tagmanager.getFlag(postag, "PRONOUN");
    String suffix = "";
    if (flag != '-') {
      if (word.endsWith("ه")) {
        suffix = "ه";
      } else if (word.endsWith("ها")) {
        suffix = "ها";
      } else if (word.endsWith("هما")) {
        suffix = "هما";
      } else if (word.endsWith("هم")) {
        suffix = "هم";
      } else if (word.endsWith("هن")) {
        suffix = "هن";
      } else if (word.endsWith("ك")) {
        suffix = "ك";
      } else if (word.endsWith("كما")) {
        suffix = "كما";
      } else if (word.endsWith("كم")) {
        suffix = "كم";
      } else if (word.endsWith("كن")) {
        suffix = "كن";
      } else if (word.endsWith("ني")) {
        suffix = "ني";
      } else if (word.endsWith("نا")) {
        suffix = "نا";
      }
      // case of some prepositon like مني منا، عني عنّا
      else if ((word.equals("عني") || word.equals("مني")) && word.endsWith("ني")) {
        suffix = "ني";
      } else if ((word.equals("عنا") || word.equals("منا")) && word.endsWith("نا")) {
        suffix = "نا";
      } else {
        suffix = "";
      }
    } else {
      return tagmanager.getPronounSuffix(postag);
    }

    return suffix;
  }

  /* tag a single word */
  public AnalyzedTokenReadings tag(String word) {
    List<String> wordlist = new ArrayList<>();
    wordlist.add(word);

    List<AnalyzedTokenReadings> ATR = tag(wordlist);
    return ATR.get(0);
  }

}
