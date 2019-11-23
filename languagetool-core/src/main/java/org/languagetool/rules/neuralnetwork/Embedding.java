package org.languagetool.rules.neuralnetwork;

import java.util.Arrays;
import java.util.Objects;

class Embedding {
  
  private final Dictionary dictionary;
  private final Matrix embedding;

  Embedding(Dictionary dictionary, Matrix embedding) {
    this.dictionary = Objects.requireNonNull(dictionary);
    this.embedding = Objects.requireNonNull(embedding);
  }

  public Matrix lookup(String[] words) {
    return new Matrix(Arrays.stream(words)
            .map(dictionary::safeGet)
            .map(embedding::row)
            .reduce(Embedding::concat)
            .get());
  }

  private static float[] concat(float[] a, float[] b) {
    int aLen = a.length;
    int bLen = b.length;
    float[] c = new float[aLen + bLen];
    System.arraycopy(a, 0, c, 0, aLen);
    System.arraycopy(b, 0, c, aLen, bLen);
    return c;
  }
}
