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

    public MirMitRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("Ich mache <marker>mit</marker> Sorgen."),
                Example.fixed("Ich mache <marker>mir</marker> Sorgen."));
        addExamplePair(Example.wrong("Ich komme <marker>mir</marker> dem Auto."),
                Example.fixed("Ich komme <marker>mit</marker> dem Auto."));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream dictionaryPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/mir_mit/dictionary.txt");
        final InputStream embeddingsPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/mir_mit/final_embeddings.txt");
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/mir_mit/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/mir_mit/b_fc1.txt");
        classifier = new Classifier(dictionaryPath, embeddingsPath, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_MIR_VS_MIT";
    }

    @Override
    List<String> getSubjects() {
        return subjects;
    }
}