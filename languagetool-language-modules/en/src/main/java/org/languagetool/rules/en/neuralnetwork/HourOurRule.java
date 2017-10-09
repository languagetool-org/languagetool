package org.languagetool.rules.en.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class HourOurRule extends EnglishNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("hour", "our");

    private double minScore = 0.50; // p=1.000, r=0.789, tp=397, tn=503, fp=0, fn=106, 170+333, 2017-10-08

    public HourOurRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("Is this <marker>hour</marker> car?"),
                Example.fixed("Is this <marker>our</marker> car?"));
        addExamplePair(Example.wrong("This place is one <marker>our</marker> away from where he lives."),
                Example.fixed("This place is one <marker>hour</marker> away from where he lives."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/hour_our/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/hour_our/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "EN_HOUR_VS_OUR";
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
