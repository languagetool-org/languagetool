/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.ner;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EnglishNamedEntityDetector {

  private static final String TOKENIZER_MODEL = "/en-token.bin";
  private static final String POS_TAGGER_MODEL = "/en-pos-maxent.bin";
  private static final String NER_MODEL = "/eu/crydee/uima/opennlp/resources/en-ner-person.bin";

  /**
   * This needs to be static to save memory: as Language.LANGUAGES is static, any language
   * that is once created there will never be released. As English has several variants,
   * we'd have as many posModels etc. as we have variants -> huge waste of memory:
   */
  private static volatile TokenizerModel tokenModel;
  private static volatile POSModel posModel;
  private static volatile TokenNameFinderModel nerModel;
  private static volatile NameFinderME ner = null;

  public EnglishNamedEntityDetector() {
    try {
      if (tokenModel == null) {
        tokenModel = new TokenizerModel(Tools.getStream(TOKENIZER_MODEL));
      }
      if (posModel == null) {
        posModel = new POSModel(Tools.getStream(POS_TAGGER_MODEL));
      }
      if (nerModel == null) {
        nerModel = new TokenNameFinderModel(Tools.getStream(NER_MODEL));
        ner = new NameFinderME(nerModel);
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not initialize English NER model", e);
    }
  }

  // non-private for test cases
  String[] tokenize(String sentence) {
    TokenizerME tokenizer = new TokenizerME(tokenModel);
    String cleanString = sentence.replace('’', '\'');  // this is the type of apostrophe that OpenNLP expects
    //System.out.println("##" + Arrays.toString(tokenizer.tokenizePos(cleanString)));
    //System.out.println("## '" + tokenizer.tokenizePos(cleanString)[0].getCoveredText(cleanString) + "'");
    return tokenizer.tokenize(cleanString);
  }

  Span[] findNamedEntities(String[] tokens) {
    return ner.find(tokens);
  }

  // returned Spans refer to character offsets
  public List<Span> findNamedEntities(String sentence) {
    String[] tokens = tokenize(sentence);
    //System.out.println(Arrays.toString(tokens));
    Span[] spans = ner.find(tokens);
    //System.out.println(Arrays.toString(spans));
    List<Span> spanList = new ArrayList<>();
    for (Span span : spans) {
      int start = findCharPos(span.getStart(), tokens, sentence);
      //System.out.println("char start: " + start + " for token start " + span.getStart());
      int end = findCharPos(span.getEnd(), tokens, sentence);
      if (start > -1 && end > -1) {  // could be -1 for non-space whitespace chars (nbsp etc)
        spanList.add(new Span(start, end, span.getType()));
      }
    }
    return spanList;
  }

  int findCharPos(int tokenStart, String[] tokens, String sentence) {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for (String token : tokens) {
      String variant1 = sb.toString() + token;
      String variant2 = sb.toString() + " " + token;
      //System.out.println("i == tokenStart ? " + i + " == " + tokenStart);
      //System.out.println(" startWith1 " + variant1 + " -> " + sentence.startsWith(variant1));
      //System.out.println(" startWith2 " + variant2 + " -> " + sentence.startsWith(variant2));
      if (sentence.startsWith(variant1)) {
        if (i == tokenStart) {
          return sb.length();
        }
        sb.append(token);
      } if (sentence.startsWith(variant2)) {
        if (i == tokenStart) {
          return sb.length() + 1;
        }
        sb.append(" ");
        sb.append(token);
      }
      i++;
    }
    return -1;
  }

}
