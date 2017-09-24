package org.languagetool.rules.en.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class FormFromRule extends EnglishNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("form", "from");

    private double minScore = 1.50; // p=0.995, r=0.313, tp=627, tn=1997, fp=3, fn=1373, 1000+1000, 2017-09-24

    public FormFromRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("I come <marker>form</marker> England."),
                Example.fixed("I come <marker>from</marker> England."));
        addExamplePair(Example.wrong("It’s a republican <marker>from</marker> of government."),
                Example.fixed("It’s a republican <marker>form<m/arker> of government."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/form_from/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/form_from/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "EN_FORM_VS_FROM";
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
