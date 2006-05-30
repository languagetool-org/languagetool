/*
 * Created on 15.05.2006
 */
package de.danielnaber.languagetool.tagging.pl;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;

import com.dawidweiss.stemmers.Lametyzator;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.JLanguageTool;

/**
 * Polish POS tagger based on FSA morphological dictionaries.
 * 
 * @author Marcin Milkowski
 */
public class PolishTagger implements Tagger {

  private static final String RESOURCE_FILENAME = "resource" + File.separator + "pl"
      + File.separator + "polish.dict";

  private Lametyzator morfologik = null;

  public List tag(List sentenceTokens) throws IOException {
    List l = new ArrayList();
    String[] taggerTokens;
    boolean firstWord = true;
    String annotations;
    int pos = 0;
    // caching Lametyzator instance - lazy init
    if (morfologik == null) {
      File resourceFile = JLanguageTool.getAbsoluteFile(RESOURCE_FILENAME);
      System
          .setProperty(Lametyzator.PROPERTY_NAME_LAMETYZATOR_DICT, resourceFile.getAbsolutePath());
      morfologik = new Lametyzator();
    }

    for (Iterator iter = sentenceTokens.iterator(); iter.hasNext();) {
      String word = (String) iter.next();

      taggerTokens = morfologik.stemAndForm(word);
      if (firstWord && taggerTokens == null) { // e.g. "Das" -> "das" at start of sentence
        taggerTokens = morfologik.stemAndForm(word.toLowerCase());
        firstWord = false;
      }
      annotations = "";
      if (taggerTokens != null) {
        for (int i = 0; i < taggerTokens.length; i++) {
          // Lametyzator returns data as String[]
          // first lemma, then annotations
          // skipping lemma here
          // TODO: add lemma to the analyzedtoken structure
          if (i % 2 != 0) {
            annotations = annotations.concat(taggerTokens[i].toUpperCase());
            if (i + 1 < taggerTokens.length)
              annotations = annotations.concat("|");
          }
        }
        l.add(new AnalyzedToken(word, annotations, pos));
        // System.err.println(annotations);
      } else
        l.add(new AnalyzedToken(word, null, pos));
      pos += word.length();
    }

    return l;
  }

  public Object createNullToken(String token, int startPos) {
    return new AnalyzedToken(token, null, startPos);
  }

}
