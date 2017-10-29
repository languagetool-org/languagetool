package org.languagetool.rules.neuralnetwork;

public interface IClassifier {

    float[] getScores(String[] context);

}
