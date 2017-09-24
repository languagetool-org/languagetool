package org.languagetool.rules.neuralnetwork;

public interface IClassifier {

    double[] getScores(String[] context);

}
