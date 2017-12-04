package org.languagetool.rules.neuralnetwork;

import java.io.InputStream;
import java.util.Arrays;

public class SingleLayerClassifier implements Classifier {

    private final Dictionary dictionary;
    private final Matrix embedding;
    private final Matrix W_fc1;
    private final Matrix b_fc1;

    public SingleLayerClassifier(Dictionary dictionary, Matrix embedding, InputStream WPath, InputStream bPath) {
        this.dictionary = dictionary;
        this.embedding = embedding;
        W_fc1 = new Matrix(WPath);
        b_fc1 = new Matrix(bPath).transpose();
    }

    public float[] getScores(String[] context) {
        final Matrix x = new Matrix(Arrays.stream(context)
                .map(dictionary::safeGet)
                .map(embedding::row)
                .reduce(SingleLayerClassifier::concat)
                .get());
        return x.mul(W_fc1).add(b_fc1).row(0);
    }

    private static float[] concat(float[] a, float[] b) {
        int aLen = a.length;
        int bLen = b.length;
        float[] c= new float[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

}
