package org.languagetool.rules.de.neuralnetwork;

public interface IClassifier {

    double[] getScores(String[] context);

}
