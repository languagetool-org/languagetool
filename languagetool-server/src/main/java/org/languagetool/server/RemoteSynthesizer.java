package org.languagetool.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.languagetool.AnalyzedToken;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.synthesis.Synthesizer;

public class RemoteSynthesizer {

  protected List<String> synthesize(String languageCode, String lemma, String postagRegexp) throws IOException {
    Language lang = null; 
    lang = Languages.getLanguageForShortCode(languageCode);
    Synthesizer synth = lang.getSynthesizer();
    AnalyzedToken at = new AnalyzedToken(lemma, postagRegexp, lemma);
    String[] synthesizedForms = synth.synthesize(at, postagRegexp, true);
    // removing duplicates. TODO: de-duplicate in the original synthesizer (?)
    List<String> results = new ArrayList<>(); 
    for (String s : synthesizedForms) {
      if (!results.contains(s)) {
        results.add(s);
      }
    }
    return results;

  }

}
