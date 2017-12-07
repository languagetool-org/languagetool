package org.languagetool.rules.neuralnetwork;

import java.io.InputStream;

public class SingleLayerClassifier implements Classifier {

  private final Embedding embedding;
  private final Matrix W_fc1;
  private final Matrix b_fc1;

  public SingleLayerClassifier(Dictionary dictionary, Matrix embedding, InputStream WPath, InputStream bPath) {
    this.embedding = new Embedding(dictionary, embedding);
    W_fc1 = new Matrix(WPath);
    b_fc1 = new Matrix(bPath).transpose();
  }

  @Override
  public float[] getScores(String[] context) {
    return embedding.lookup(context).mul(W_fc1).add(b_fc1).row(0);
  }

}
