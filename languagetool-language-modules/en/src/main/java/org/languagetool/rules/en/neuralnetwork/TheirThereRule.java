package org.languagetool.rules.en.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class TheirThereRule extends EnglishNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("their", "there");

    private double minScore = 0.50; // p=0.994, r=0.940, tp=1880, tn=1989, fp=11, fn=120, 1000+1000, 2017-10-14

    public TheirThereRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("Is <marker>their</marker> an advantage?"),
                Example.fixed("Is <marker>there</marker> an advantage?"));
        addExamplePair(Example.wrong("You can buy <marker>there</marker> new album tomorrow."),
                Example.fixed("You can buy <marker>their</marker> new album tomorrow."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/their_there/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/their_there/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "EN_THEIR_VS_THERE";
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
