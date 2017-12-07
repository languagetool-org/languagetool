package org.languagetool.rules.neuralnetwork;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Word2VecModel {

  private final Embedding embedding;

  private final File path;

  public Word2VecModel(String path) throws FileNotFoundException {
    Dictionary dictionary = new org.languagetool.rules.neuralnetwork.Dictionary(new FileInputStream(path + File.separator + "dictionary.txt"));
    Matrix embedding = new Matrix(new FileInputStream(path + File.separator + "final_embeddings.txt"));
    this.embedding = new Embedding(dictionary, embedding);
    this.path = new File(path);
  }

  public Embedding getEmbedding() {
    return embedding;
  }

  public File getPath() {
    return path;
  }

}
