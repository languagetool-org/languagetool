package org.languagetool.rules.de.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class WirWirdRule extends GermanNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("wir", "wird");

    private double minScore = 1.25; // p=0.991, r=0.718, tp=976, tn=1351, fp=9, fn=384, 360+1000, 2017-10-08

    public WirWirdRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("Wann <marker>wir</marker> nächstes Jahr das Theater stattfinden?"),
                Example.fixed("Wann <marker>wird</marker> nächstes Jahr das Theater stattfinden?"));
        addExamplePair(Example.wrong("Für Donnerstag suchen <marker>wird</marker> noch Helfer für die ESAG."),
                Example.fixed("Für Donnerstag suchen <marker>wir</marker> noch Helfer für die ESAG."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/wir_wird/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/wir_wird/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_WIR_VS_WIRD";
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
