package org.languagetool.rules.de.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class FielenVielenRule extends GermanNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("fielen", "vielen");

    private double minScore = 0.50; // p=0.994, r=0.715, tp=805, tn=1121, fp=5, fn=321, 126+1000, 2017-09-24

    public FielenVielenRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("Bei <marker>fielen</marker> Antworten bin ich mir nicht sicher."),
                Example.fixed("Bei <marker>vielen</marker> Antworten bin ich mir nicht sicher."));
        addExamplePair(Example.wrong("Wenn sie fragen würde, <marker>vielen</marker> dann die Antworten anders aus?"),
                Example.fixed("Wenn sie fragen würde, <marker>fielen</marker> dann die Antworten anders aus?"));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/fielen_vielen/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/fielen_vielen/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_FIELEN_VS_VIELEN";
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
