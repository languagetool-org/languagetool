package org.languagetool.rules.de.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class DichDochRule extends GermanNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("dich", "doch");

    private double minScore = 1.00; // p=0.991, r=0.563, tp=449, tn=794, fp=4, fn=349, 48+750, 2017-09-24

    public DichDochRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("Der ist <marker>dich</marker> als Kind in den Kaffeepott gefallen."),
                Example.fixed("Der ist <marker>doch</marker> als Kind in den Kaffeepott gefallen."));
        addExamplePair(Example.wrong("Falls du <marker>doch</marker> dafür interessierst, kannst du dich bewerben."),
                Example.fixed("Falls du <marker>dich</marker> dafür interessierst, kannst du dich bewerben."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/dich_doch/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/dich_doch/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_DICH_VS_DOCH";
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
