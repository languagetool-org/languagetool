/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.bigdata;

import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.English;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Prepare Weka training input from example sentences.
 */
class TextToArff {

  private final InMemoryLookupTable lookupTable;
  private final ContextBuilder contextBuilder;

  TextToArff(InMemoryLookupTable lookupTable) {
    this.lookupTable = lookupTable;
    contextBuilder = new ContextBuilder();
  }

  private void writeHeader() {
    System.out.println("@relation grammarDataset");
    for (int i = 0; i < 5; i++) {  //TODO
      System.out.println("@attribute dim" + i + " numeric");
    }
    System.out.println("@attribute homophone { there, their }");
    System.out.println();
    System.out.println("@data");
  }

  private void iterateTokens(String homophone1, String homophone2, AnalyzedTokenReadings[] tokens) {
    int i = 0;
    for (AnalyzedTokenReadings token : tokens) {
      boolean equals1 = token.getToken().equals(homophone1);
      boolean equals2 = token.getToken().equals(homophone2);
      String homophone = equals1 ? homophone1 : homophone2;
      if (equals1 || equals2) {
        List<String> context = contextBuilder.getContext(tokens, i, 2);
        //System.out.println(line);
        boolean allLong = context.stream().allMatch(l -> l.length() > 1);
        if (context.size() == 5 && allLong) {
          //System.out.println(String.join(",", context).replace("'", "''") + "," + homophone);
          // average the context vectors:
          INDArray avgVector = Nd4j.create(100);
          //System.out.println(avgVector);
          for (String contextTerm : context) {
            // TODO: homophone selber (in der mitte) rauslassen?
            INDArray vector = lookupTable.vector(contextTerm);
            //System.out.println(vector);
            if (vector != null) {
              avgVector = avgVector.add(vector);
            }
          }
          avgVector.divi(context.size());
          //System.out.println(avgVector + " " + homophone);
          for (int j = 0; j < 100; j++) {
            System.out.print(avgVector.getDouble(j) + " ");
          }
          System.out.println();
          //System.out.println("--------------------");
        }
      }
      i++;
    }
  }

  public static void main(String[] args) throws IOException {
    Pair<InMemoryLookupTable, VocabCache> pair = WordVectorSerializer.loadTxt(new File("/media/Data/word-embeddings/glove/glove.6B.100d-top50K.txt"));
    System.out.println("Done.");
    InMemoryLookupTable lookupTable = pair.getFirst();
    //INDArray vector = lookupTable.vector("house");
    //System.out.println("->" +vector);
    TextToArff prg = new TextToArff(lookupTable);

    //String filename = "/media/Data/mix-corpus/sentences-there-100.txt";
    //String homophone = "there";
    String filename = "/media/Data/mix-corpus/sentences-their-100.txt";
    String homophone1 = "their";
    String homophone2 = "there";
    prg.writeHeader();
    JLanguageTool lt = new JLanguageTool(new English());
    try (Scanner s = new Scanner(new File(filename))) {
      while (s.hasNextLine()) {
        String line = s.nextLine();
        AnalyzedSentence sentence = lt.getAnalyzedSentence(line);
        AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
        prg.iterateTokens(homophone1, homophone2, tokens);
      }
    }
  }
}
