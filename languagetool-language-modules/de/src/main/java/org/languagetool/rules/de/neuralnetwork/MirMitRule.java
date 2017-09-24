package org.languagetool.rules.de.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class MirMitRule extends GermanNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("mir", "mit");

    private double minScore = 1.50; // p=0.994, r=0.658, tp=775, tn=1173, fp=5, fn=403, 178+1000, 2017-09-24

    public MirMitRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("Ich mache <marker>mit</marker> Sorgen."),
                Example.fixed("Ich mache <marker>mir</marker> Sorgen."));
        addExamplePair(Example.wrong("Ich fahre <marker>mir</marker> dem Auto."),
                Example.fixed("Ich fahre <marker>mit</marker> dem Auto."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/mir_mit/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/mir_mit/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_MIR_VS_MIT";
    }

    @Override
    protected List<String> getSubjects() {
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
