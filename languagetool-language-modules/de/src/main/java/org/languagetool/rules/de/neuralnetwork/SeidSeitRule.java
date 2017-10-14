package org.languagetool.rules.de.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class SeidSeitRule extends GermanNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("seid", "seit");

    private double minScore = 0.50; // p=1.000, r=0.842, tp=845, tn=1003, fp=0, fn=158, 3+1000, 2017-10-14

    public SeidSeitRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("Der Stammtisch findet <marker>seid</marker> 2017 am ersten Dienstag im Monat statt."),
                Example.fixed("Der Stammtisch findet <marker>seit</marker> 2017 am ersten Dienstag im Monat statt."));
        addExamplePair(Example.wrong("Ihr <marker>seit</marker> beim Stammtisch herzlich willkommen."),
                Example.fixed("Ihr <marker>seid</marker> beim Stammtisch herzlich willkommen."));


        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/seid_seit/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/seid_seit/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_SEID_VS_SEIT";
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
