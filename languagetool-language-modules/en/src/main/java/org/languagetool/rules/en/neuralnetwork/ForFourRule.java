package org.languagetool.rules.en.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class ForFourRule extends EnglishNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("for", "four");

    private double minScore = 1.50; // p=0.990, r=0.685, tp=1370, tn=1986, fp=14, fn=630, 1000+1000, 2017-10-14

    public ForFourRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("I need it <marker>four</marker> my program."),
                Example.fixed("I need it <marker>for</marker> my program."));
        addExamplePair(Example.wrong("I have published <marker>for</marker> papers."),
                Example.fixed("I have published <marker>four</marker> papers."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/for_four/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/for_four/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "EN_FOR_VS_FOUR";
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
