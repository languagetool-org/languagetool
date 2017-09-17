package org.languagetool.rules.de.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class EinInRule extends GermanNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("ein", "in");

    public EinInRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("Ein Fehler ist <marker>ein</marker> diesem Satz."),
                Example.fixed("Ein Fehler ist <marker>in</marker> diesem Satz."));
        addExamplePair(Example.wrong("Ich bin <marker>ein</marker> dem Haus."),
                Example.fixed("Ich bin <marker>in</marker> dem Haus."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/ein_in/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/ein_in/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_EIN_VS_IN";
    }

    @Override
    protected List<String> getSubjects() {
        return subjects;
    }
}
