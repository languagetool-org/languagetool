package org.languagetool.rules.en.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class SideSiteRule extends EnglishNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("side", "site");

    private double minScore = 0.5; // bad: p=1.000, r=0.080, tp=107, tn=1333, fp=0, fn=1226, 726+607, 2017-10-08

    public SideSiteRule(ResourceBundle messages) {
        super(messages);

//        addExamplePair(Example.wrong("You can meet me on the north site of the monument."),
//                Example.fixed("You can meet me on the north side of the monument."));
//        addExamplePair(Example.wrong("I met him and <marker>than</marker> I met her."),
//                Example.fixed("I met him and <marker>then</marker> I met her."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/side_site/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/side_site/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "EN_SIDE_VS_SITE";
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
