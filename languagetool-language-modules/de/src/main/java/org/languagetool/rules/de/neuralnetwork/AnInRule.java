package org.languagetool.rules.de.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class AnInRule extends GermanNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("an", "in");

    private double minScore = 1.50; // bad: p=0.955, r=0.128, tp=256, tn=1988, fp=12, fn=1744, 1000+1000, 2017-10-08

    public AnInRule(ResourceBundle messages) {
        super(messages);

//        addExamplePair(Example.wrong("Danke für <marker>da</marker> Angebot."),
//                Example.fixed("Danke für <marker>das</marker> Angebot."));
//        addExamplePair(Example.wrong("Ja doch, meine beiden Kommilitonen <marker>das</marker> hinten, aber die wollten selber mitmachen."),
//                Example.fixed("Ja doch, meine beiden Kommilitonen <marker>da</marker> hinten, aber die wollten selber mitmachen."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/an_in/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/an_in/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_AN_VS_IN";
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
