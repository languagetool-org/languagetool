package org.languagetool.rules.neuralnetwork;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Word2VecModel {

  private final Dictionary dictionary;
  private final Matrix embedding;

  public Word2VecModel(String path) throws FileNotFoundException {
    dictionary = new org.languagetool.rules.neuralnetwork.Dictionary(new FileInputStream(path + File.separator + "dictionary.txt"));
    embedding = new Matrix(new FileInputStream(path + File.separator + "final_embeddings.txt"));
  }

  public Dictionary getDictionary() {
    return dictionary;
  }

  public Matrix getEmbedding() {
    return embedding;
  }

}
