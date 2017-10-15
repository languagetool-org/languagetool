package org.languagetool.rules.pt.neuralnetwork;

import org.jetbrains.annotations.NotNull;
import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.neuralnetwork.AbstractNeuralNetworkRule;
import org.languagetool.rules.neuralnetwork.Matrix;
import org.languagetool.rules.neuralnetwork.Suggestion;

import java.io.InputStream;
import java.util.ResourceBundle;

public abstract class PortugueseNeuralNetworkRule extends AbstractNeuralNetworkRule {
    protected static org.languagetool.rules.neuralnetwork.Dictionary dictionary;
    protected static Matrix embedding;

    static {
        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream dictionaryPath = dataBroker.getFromResourceDirAsStream("/pt/neuralnetwork/dictionary.txt");
        dictionary = new org.languagetool.rules.neuralnetwork.Dictionary(dictionaryPath);
        final InputStream embeddingsPath = dataBroker.getFromResourceDirAsStream("/pt/neuralnetwork/final_embeddings.txt");
        embedding = new Matrix(embeddingsPath);
    }

    protected PortugueseNeuralNetworkRule(ResourceBundle messages) {
        super(messages);
    }

    @Override
    public String getDescription() {
        return "Possible Typo '" + getSubjects().get(0) + "'/'" + getSubjects().get(1) + "'";
    }

    @NotNull @Override
    protected String getMessage(Suggestion suggestion, double[] y) {
        String msg = "Possible confusion of '" + getSubjects().get(0) + "' and '" + getSubjects().get(1) + "'. " + certaintiesToString(y);
        if(suggestion.isUnsure()) {
            msg = "(low certainty) " + msg;
        }
        return msg;
    }
}
