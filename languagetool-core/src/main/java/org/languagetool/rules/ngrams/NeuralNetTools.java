package org.languagetool.rules.ngrams;

import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Experimental;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.FileNotFoundException;

@Experimental
class NeuralNetTools {

  private static final String WORD_EMBEDDINGS = "/media/Data/word-embeddings/glove/glove.6B.100d-top50K.txt";

  private final InMemoryLookupTable lookupTable;

  NeuralNetTools() {
    this.lookupTable = getWordEmbeddings();
  }
  
  InMemoryLookupTable getWordEmbeddings() {
    System.out.println("Loading embeddings...");
    try {
      Pair<InMemoryLookupTable, VocabCache> pair = WordVectorSerializer.loadTxt(new File(WORD_EMBEDDINGS));
      System.out.println("Loaded embeddings...");
      return pair.getFirst();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  INDArray getSentenceVector(int contextSize, AnalyzedTokenReadings[] tokens, int wordPos) {
    INDArray in = Nd4j.createComplex(1, contextSize*2+1);
    int pos = 0;
    for (int j = wordPos - contextSize; j <= wordPos + contextSize; j++) {
      INDArray vector = getTokenVector(tokens, j);
      in.put(pos, vector);
      pos++;
    }
    return in;
  }

  // gets the word2vec representation of a word
  private INDArray getTokenVector(AnalyzedTokenReadings[] tokens, int j) {
    String token;
    if (j < 0) {
      token = "_START_";
    } else if (j >= tokens.length) {
      token = "_END_";
    } else {
      token = tokens[j].getToken();
    }
    return lookupTable.vector(token);
  }

}
