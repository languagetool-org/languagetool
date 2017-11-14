package org.languagetool.rules.neuralnetwork;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Stream;

public class TwoLayerClassifier implements IClassifier {

  private final Dictionary dictionary;
  private final Matrix embedding;
  private final Matrix W_fc1;
  private final Matrix b_fc1;
  private final Matrix W_fc2;
  private final Matrix b_fc2;

  public TwoLayerClassifier(Dictionary dictionary, Matrix embedding, InputStream W1, InputStream b1, InputStream W2, InputStream b2) {
    this.dictionary = dictionary;
    this.embedding = embedding;
    W_fc1 = new Matrix(W1);
    b_fc1 = new Matrix(b1).transpose();
    W_fc2 = new Matrix(W2);
    b_fc2 = new Matrix(b2).transpose();
  }

  public float[] getScores(String[] context) {
    final Matrix x = new Matrix(contextStream(context)
            .map(dictionary::safeGet)
            .map(embedding::row)
            .reduce(TwoLayerClassifier::concat)
            .get()); // TODO -> static
    return x.mul(W_fc1).add(b_fc1).relu().mul(W_fc2).add(b_fc2).row(0);
  }

  @NotNull
  private Stream<String> contextStream(String[] context) {
    final int inputs = W_fc1.rows() / embedding.columns();
    if (inputs == 4) {
      return Arrays.stream(context);
    } else if (inputs == 2) {
      return Stream.of(context[1], context[2]);
    }
    throw new IllegalStateException("W_fc1 with " + inputs + " inputs not supported.");
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
