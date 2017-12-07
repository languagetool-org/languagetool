package org.languagetool.rules.neuralnetwork;

import java.io.InputStream;

public class TwoLayerClassifier implements Classifier {

  private final Embedding embedding;
  private final Matrix W_fc1;
  private final Matrix b_fc1;
  private final Matrix W_fc2;
  private final Matrix b_fc2;

  public TwoLayerClassifier(Embedding embedding, InputStream W1, InputStream b1, InputStream W2, InputStream b2) {
    this.embedding = embedding;
    W_fc1 = new Matrix(W1);
    b_fc1 = new Matrix(b1).transpose();
    W_fc2 = new Matrix(W2);
    b_fc2 = new Matrix(b2).transpose();
  }

  public float[] getScores(String[] context) {
    return embedding.lookup(context).mul(W_fc1).add(b_fc1).relu().mul(W_fc2).add(b_fc2).row(0);
  }

}
