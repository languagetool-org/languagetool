package org.languagetool.rules.de.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class IhmImRule extends GermanNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("ihm", "im");

    private double minScore = 1.25; // p=0.992, r=0.828, tp=1657, tn=1986, fp=14, fn=343, 1000+1000, 2017-09-24

    public IhmImRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("Sollen wir <marker>im</marker> ein Geschenk machen?"),
                Example.fixed("Sollen wir <marker>ihm</marker> ein Geschenk machen?"));
        addExamplePair(Example.wrong("Ich bin <marker>ihm</marker> Hauptbahnhof."),
                Example.fixed("Ich bin im Hauptbahnhof."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/ihm_im/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/ihm_im/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_IHM_VS_IM";
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
