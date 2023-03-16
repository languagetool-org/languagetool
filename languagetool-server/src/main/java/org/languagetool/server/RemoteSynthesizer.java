package org.languagetool.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.synthesis.Synthesizer;

public class RemoteSynthesizer {

  protected List<String> synthesize(String languageCode, String lemma, String postag, boolean postagRegexp)
      throws IOException {
    Language lang = Languages.getLanguageForShortCode(languageCode);
    Synthesizer synth = lang.getSynthesizer();
    AnalyzedToken at = new AnalyzedToken(lemma, postag, lemma);
    String[] synthesizedForms = synth.synthesize(at, postag, postagRegexp);
    // removing duplicates. TODO: de-duplicate in the original synthesizer (?)
    return removeDuplicates(synthesizedForms);
  }

  protected List<String> synthesize(String languageCode, AnalyzedTokenReadings atrs, boolean postagRegexp,
      String postagSelect, String postagReplace, String lemmaReplace) throws IOException {
    if (!postagRegexp) {
      return synthesize(languageCode, lemmaReplace, postagReplace, false);
    }
    AnalyzedToken atr = atrs.readingWithTagRegex(postagSelect);
    if (atr == null) {
      // TODO: log error
      return null;
    }
    if (lemmaReplace != null & !lemmaReplace.isEmpty()) {
      atr = new AnalyzedToken(atr.getToken(), atr.getPOSTag(), lemmaReplace);
    }
    String postagReplaceFinal = null;
    try {
      Pattern p = Pattern.compile(postagSelect);
      Matcher m = p.matcher(atr.getPOSTag());
      postagReplaceFinal = m.replaceAll(postagReplace);
    } catch (IndexOutOfBoundsException | PatternSyntaxException e) {
      // TODO: log error
      return null;
    }
    Language lang = Languages.getLanguageForShortCode(languageCode);
    Synthesizer synth = lang.getSynthesizer();
    String[] synthesizedForms = synth.synthesize(atr, postagReplaceFinal, true);
    return removeDuplicates(synthesizedForms);
  }

  private List<String> removeDuplicates(String[] forms) {
    List<String> results = new ArrayList<>();
    for (String s : forms) {
      if (!results.contains(s)) {
        results.add(s);
      }
    }
    return results;
  }

}
