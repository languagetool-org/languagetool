package org.languagetool.rules.de.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class DaDasRule extends GermanNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("da", "das");

    private double minScore =  1.50; // p=0.992, r=0.251, tp=502, tn=1996, fp=4, fn=1498, 1000+1000, 2017-09-24

    public DaDasRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("Danke für <marker>da</marker> Angebot."),
                Example.fixed("Danke für <marker>das</marker> Angebot."));
        addExamplePair(Example.wrong("Ja doch, meine beiden Kommilitonen <marker>das</marker> hinten, aber die wollten selber mitmachen."),
                Example.fixed("Ja doch, meine beiden Kommilitonen <marker>da</marker> hinten, aber die wollten selber mitmachen."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/da_das/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/da_das/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_DA_VS_DAS";
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
