package org.languagetool.rules.de.neuralnetwork;

import java.io.InputStream;
import java.util.Arrays;

class Classifier implements IClassifier {

    private final Dictionary dictionary;
    private final Matrix embedding;
    private final Matrix W_fc1;
    private final Matrix b_fc1;

    Classifier(InputStream dictionaryPath, InputStream embeddingPath, InputStream WPath, InputStream bPath) {
        dictionary = new Dictionary(dictionaryPath);
        embedding = new Matrix(embeddingPath);
        W_fc1 = new Matrix(WPath);
        b_fc1 = new Matrix(bPath).transpose();
    }

    public double[] getScores(String[] context) {
        final Matrix x = new Matrix(Arrays.stream(context)
                .map(dictionary::safeGet)
                .map(embedding::row)
                .reduce(Classifier::concat)
                .get());
        return x.mul(W_fc1).add(b_fc1).row(0);
    }

    private static double[] concat(double[] a, double[] b) {
        int aLen = a.length;
        int bLen = b.length;
        double[] c= new double[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

}