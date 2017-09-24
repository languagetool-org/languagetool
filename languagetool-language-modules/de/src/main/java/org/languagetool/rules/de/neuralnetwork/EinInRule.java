package org.languagetool.rules.de.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class EinInRule extends GermanNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("ein", "in");

    private double minScore = 3.50; // p=0.990, r=0.236, tp=472, tn=1995, fp=5, fn=1528, 1000+1000, 2017-09-24

    public EinInRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("Ein Fehler ist <marker>ein</marker> diesem Satz."),
                Example.fixed("Ein Fehler ist <marker>in</marker> diesem Satz."));
        addExamplePair(Example.wrong("Es wird eine Einführung <marker>ein</marker> die Sprache Frege gegeben."),
                Example.fixed("Es wird eine Einführung <marker>in</marker> die Sprache Frege gegeben."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/ein_in/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/ein_in/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_EIN_VS_IN";
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
