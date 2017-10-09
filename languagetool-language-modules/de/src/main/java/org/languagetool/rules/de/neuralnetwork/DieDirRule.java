package org.languagetool.rules.de.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class DieDirRule extends GermanNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("die", "dir");

    private double minScore = 1.50; // p=1.000, r=0.607, tp=623, tn=1027, fp=0, fn=404, 1000+27, 2017-10-08

    public DieDirRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("Wann kann ich zu <marker>die</marker> kommen?"),
                Example.fixed("Wann kann ich zu <marker>dir</marker> kommen?"));
        addExamplePair(Example.wrong("Für <marker>dir</marker> ersten 100 Anrufer gibt es noch einen Rechenschieber, mit dem es möglich ist, durch null zu teilen."),
                Example.fixed("Für <marker>die</marker> ersten 100 Anrufer gibt es noch einen Rechenschieber, mit dem es möglich ist, durch null zu teilen."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/die_dir/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/die_dir/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_DIE_VS_DIR";
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
