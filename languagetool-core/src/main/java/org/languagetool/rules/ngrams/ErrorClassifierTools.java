package org.languagetool.rules.ngrams;

import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ErrorClassifierTools {

  private static final String WORD_EMBEDDINGS = "/media/Data/word-embeddings/glove/glove.6B.100d-top50K.txt";

  private final InMemoryLookupTable lookupTable;

  ErrorClassifierTools() throws FileNotFoundException {
    this.lookupTable = getWordEmbeddings();
  }
  
  InMemoryLookupTable getWordEmbeddings() throws FileNotFoundException {
    System.out.println("Loading embeddings...");
    Pair<InMemoryLookupTable, VocabCache> pair = WordVectorSerializer.loadTxt(new File(WORD_EMBEDDINGS));
    System.out.println("Done.");
    return pair.getFirst();
  }

  INDArray getSentenceVector(int contextSize, String sentence, String word) {
    List<String> origTokens = Arrays.asList(sentence.split("[-?!/<>\\[\\]=()—'’\",.;:\\s]"));
    List<String> tokens = origTokens.stream().filter(t -> !t.trim().isEmpty()).map(String::toLowerCase).collect(Collectors.toList());
    INDArray in = Nd4j.createComplex(1, contextSize*2+1);
    int wordPos = tokens.indexOf(word.toLowerCase());
    if (wordPos == -1) {
      throw new RuntimeException("'" + word + "' not found: " + sentence);
    } else {
      int pos = 0;
      for (int j = wordPos - contextSize; j <= wordPos + contextSize; j++) {
        INDArray vector = getTokenVector(tokens, j);
        in.put(pos, vector);
        pos++;
      }
      return in;
    }
  }

  // gets the word2vec representation of a word
  private INDArray getTokenVector(List<String> tokens, int j) {
    String token;
    if (j < 0) {
      token = "_START_";
    } else if (j >= tokens.size()) {
      token = "_END_";
    } else {
      token = tokens.get(j);
    }
    return lookupTable.vector(token);
  }

}
