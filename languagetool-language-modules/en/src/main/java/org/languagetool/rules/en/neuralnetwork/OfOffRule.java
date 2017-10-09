package org.languagetool.rules.en.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class OfOffRule extends EnglishNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("of", "off");

    private double minScore = 2.00; // p=0.990, r=0.433, tp=795, tn=1826, fp=8, fn=1039, 1000+834, 2017-10-08

    public OfOffRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("I have to cut <marker>of</marker> the branch."),
                Example.fixed("I have to cut <marker>off</marker> the branch."));
        addExamplePair(Example.wrong("You can extend the range <marker>off</marker> the search."),
                Example.fixed("You can extend the range <marker>of</marker> the search."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/of_off/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/of_off/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "EN_OF_VS_OFF";
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
