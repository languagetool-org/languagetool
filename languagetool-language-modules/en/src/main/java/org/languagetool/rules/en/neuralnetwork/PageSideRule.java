package org.languagetool.rules.en.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class PageSideRule extends EnglishNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("page", "side");

    private double minScore = 2.75; // bad: p=0.990, r=0.117, tp=103, tn=877, fp=1, fn=775, 152+726, 2017-10-08

    public PageSideRule(ResourceBundle messages) {
        super(messages);

//        addExamplePair(Example.wrong("Open your books on side 42."),
//                Example.fixed("I am better <marker>than</marker> you."));
//        addExamplePair(Example.wrong("I met him and <marker>than</marker> I met her."),
//                Example.fixed("I met him and <marker>then</marker> I met her."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/page_side/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/en/neuralnetwork/page_side/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "EN_PAGE_VS_SIDE";
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
