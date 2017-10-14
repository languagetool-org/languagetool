package org.languagetool.rules.en.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class KnowNowRule extends EnglishNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("know", "now");

    private double minScore = 0.75; // p=0.991, r=0.919, tp=1140, tn=1231, fp=10, fn=101, 241+1000, 2017-10-14

    public KnowNowRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("I don’t <marker>now</marker> the answer."),
                Example.fixed("I don’t <marker>know</marker> the answer."));
        addExamplePair(Example.wrong("We are <marker>know</marker> working on better rules."),
                Example.fixed("We are <marker>now</marker> working on better rules."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/know_now/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/know_now/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "EN_KNOW_VS_NOW";
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
