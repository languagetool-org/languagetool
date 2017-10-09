package org.languagetool.rules.de.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class WarWahrRule extends GermanNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("war", "wahr");

    private double minScore = 1.75; // bad: p=1.000, r=0.005, tp=6, tn=1194, fp=0, fn=1188, 1000+194, 2017-10-08

    public WarWahrRule(ResourceBundle messages) {
        super(messages);

//        addExamplePair(Example.wrong("Danke für <marker>da</marker> Angebot."),
//                Example.fixed("Danke für <marker>das</marker> Angebot."));
//        addExamplePair(Example.wrong("Ja doch, meine beiden Kommilitonen <marker>das</marker> hinten, aber die wollten selber mitmachen."),
//                Example.fixed("Ja doch, meine beiden Kommilitonen <marker>da</marker> hinten, aber die wollten selber mitmachen."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/war_wahr/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/war_wahr/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_WAR_VS_WAHR";
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
