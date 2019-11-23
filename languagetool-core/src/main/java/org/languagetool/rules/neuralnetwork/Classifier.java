package org.languagetool.rules.neuralnetwork;

public interface Classifier {

  float[] getScores(String[] context);

}
