package org.languagetool.rules.de.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class WieWirRule extends GermanNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("wie", "wir");

    private double minScore = 1.50; // p=0.992, r=0.707, tp=1118, tn=1573, fp=9, fn=464, 1000+582, 2017-09-24

    public WieWirRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("Außerdem suchen <marker>wir</marker> Helfer für unsere Weihnachtsfeier."),
                Example.fixed("Außerdem suchen <marker>wie</marker> Helfer für unsere Weihnachtsfeier."));
        addExamplePair(Example.wrong("Um <marker>wir</marker> viel Uhr treffen wir uns?"),
                Example.fixed("Um <marker>wie</marker> viel Uhr treffen wir uns?"));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/wie_wir/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/wie_wir/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_WIE_VS_WIR";
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
