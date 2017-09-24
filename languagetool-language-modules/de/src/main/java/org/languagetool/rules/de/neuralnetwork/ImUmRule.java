package org.languagetool.rules.de.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class ImUmRule extends GermanNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("im", "um");

    private double minScore = 2.50; // p=0.992, r=0.533, tp=1065, tn=1991, fp=9, fn=935, 1000+1000, 2017-09-24

    public ImUmRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("Wir sitzen <marker>um</marker> Bus."),
                Example.fixed("Wir sitzen <marker>im</marker> Bus."));
        addExamplePair(Example.wrong("Wir kommen <marker>im</marker> 5 Uhr."),
                Example.fixed("Wir kommen <marker>um</marker> 5 Uhr."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/im_um/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/im_um/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_IM_VS_UM";
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
