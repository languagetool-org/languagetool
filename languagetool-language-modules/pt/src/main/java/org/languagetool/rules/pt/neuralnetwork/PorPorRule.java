package org.languagetool.rules.pt.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.neuralnetwork.Classifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class PorPorRule extends PortugueseNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("por", "pôr");

    private double minScore = 0.5; // TODO calibration not done

    public PorPorRule(ResourceBundle messages) {
        super(messages);

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/pt/neuralnetwork/por_por/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/pt/neuralnetwork/por_por/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_POR_VS_POR";
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
