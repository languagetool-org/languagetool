package org.languagetool.rules.en.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class ToTooRule extends EnglishNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("to", "too");

    private double minScore = 1.00; // p=0.992, r=0.844, tp=1240, tn=1459, fp=10, fn=229, 1000+469, 2017-09-24

    public ToTooRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("I am going <marker>too</marker> England."),
                Example.fixed("I am going <marker>too</marker> England."));
        addExamplePair(Example.wrong("It’s <marker>to</marker> hot."),
                Example.fixed("It’s <marker>to</marker> hot."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/to_too/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/to_too/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "EN_TO_VS_TOO";
    }

    @Override
    public List<String> getSubjects() {
        return subjects;
    }

    @Override
    protected double getMinScore() {
        return minScore;
    }

    public void setMinScore(double minScore) {
        this.minScore = minScore;
    }

}
