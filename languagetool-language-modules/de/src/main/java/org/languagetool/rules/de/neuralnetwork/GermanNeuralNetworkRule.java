package org.languagetool.rules.de.neuralnetwork;

import org.jetbrains.annotations.NotNull;
import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.neuralnetwork.AbstractNeuralNetworkRule;
import org.languagetool.rules.neuralnetwork.Matrix;
import org.languagetool.rules.neuralnetwork.Suggestion;

import java.io.InputStream;
import java.util.ResourceBundle;

public abstract class GermanNeuralNetworkRule extends AbstractNeuralNetworkRule {
    protected static org.languagetool.rules.neuralnetwork.Dictionary dictionary;
    protected static Matrix embedding;

    static {
        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream dictionaryPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/dictionary.txt");
        dictionary = new org.languagetool.rules.neuralnetwork.Dictionary(dictionaryPath);
        final InputStream embeddingsPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/final_embeddings.txt");
        embedding = new Matrix(embeddingsPath);
    }

    protected GermanNeuralNetworkRule(ResourceBundle messages) {
        super(messages);
    }

    @Override
    public String getDescription() {
        return "Möglicher Tippfehler '" + getSubjects().get(0) + "'/'" + getSubjects().get(1) + "'";
    }

    @NotNull @Override
    protected String getMessage(Suggestion suggestion, double[] y) {
        String msg = "Mögliche Verwechslung von '" + getSubjects().get(0) + "' und '" + getSubjects().get(1) + "'. " + certaintiesToString(y);
        if(suggestion.isUnsure()) {
            msg = "(Geringe Sicherheit) " + msg;
        }
        return msg;
    }
}
