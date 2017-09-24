package org.languagetool.rules.en.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class ThanThenRule extends EnglishNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("than", "then");

    private double minScore = 1.25; // p=0.992, r=0.802, tp=1603, tn=1987, fp=13, fn=397, 1000+1000, 2017-09-24

    public ThanThenRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("I am better <marker>then</marker> you."),
                Example.fixed("I am better <marker>than</marker> you."));
        addExamplePair(Example.wrong("I met him and <marker>than</marker> I met her."),
                Example.fixed("I met him and <marker>then</marker> I met her."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/than_then/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/than_then/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "EN_THAN_VS_THEN";
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
