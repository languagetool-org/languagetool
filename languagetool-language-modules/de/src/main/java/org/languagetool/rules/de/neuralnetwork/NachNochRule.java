package org.languagetool.rules.de.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class NachNochRule extends GermanNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("nach", "noch");

    private double minScore = 1.75; // p=0.990, r=0.504, tp=1009, tn=1990, fp=10, fn=991, 1000+1000, 2017-09-24

    public NachNochRule(ResourceBundle messages) {
        super(messages);
        
        addExamplePair(Example.wrong("Ich bin <marker>nach</marker> nicht da."),
                Example.fixed("Ich bin <marker>noch</marker> nicht da."));
        addExamplePair(Example.wrong("Wir fahren <marker>noch</marker> Düsseldorf."),
                Example.fixed("Wir fahren <marker>noch</marker> Düsseldorf."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/nach_noch/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/nach_noch/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_NACH_VS_NOCH";
    }

    @Override
    protected List<String> getSubjects() {
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
