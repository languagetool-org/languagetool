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

  // get from http://nlp.stanford.edu/projects/glove/:
  private static final String WORD_EMBEDDINGS = "/media/Data/word-embeddings/glove/glove.6B.100d-top50K.txt";

  private static final InMemoryLookupTable lookupTable = getWordEmbeddings();
  
  NeuralNetTools() {
  }
  
  private static InMemoryLookupTable getWordEmbeddings() {
    System.out.println("Loading embeddings...");
    try {
      long startTime = System.currentTimeMillis();
      Pair<InMemoryLookupTable, VocabCache> pair = WordVectorSerializer.loadTxt(new File(WORD_EMBEDDINGS));
      long time = System.currentTimeMillis() - startTime;
      System.out.println("Loaded embeddings (" + time + "ms)");
      return pair.getFirst();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  INDArray getContextVector(int contextSize, AnalyzedTokenReadings[] tokens, int wordPos) {
    INDArray in = Nd4j.createComplex(1, contextSize*2+1);  // TODO: does createComplex() make sense here?
    int pos = 0;
    for (int j = wordPos - contextSize; j <= wordPos + contextSize; j++) {
      INDArray vector = getTokenVector(tokens, j);
      in.put(pos, vector);  // TODO: this seems wrong, only the first value of 100 dimensions is used
      pos++;
    }
    //System.out.println("----------");
    //System.out.println(in);
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
    INDArray vector = lookupTable.vector(token);
    //System.out.println("Vector for " + token + ": " + vector);
    return vector;
  }

}
