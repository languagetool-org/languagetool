package org.languagetool.rules.de.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Example;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class WieWirRule extends GermanNeuralNetworkRule {
    private final List<String> subjects = Arrays.asList("wie", "wir");

    public WieWirRule(ResourceBundle messages) {
        super(messages);

        addExamplePair(Example.wrong("Außerdem suchen <marker>wir</marker> Helfer für unsere Weihnachtsfeier."),
                Example.fixed("Außerdem suchen <marker>wie</marker> Helfer für unsere Weihnachtsfeier."));
        addExamplePair(Example.wrong("Um <marker>wir</marker> viel Uhr treffen wir uns?"),
                Example.fixed("Um <marker>wie</marker> viel Uhr treffen wir uns?"));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/wie_wir/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/wie_wir/b_fc1.txt");
        classifier = new Classifier(dictionary, embedding, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_WIE_VS_WIR";
    }

    @Override
    protected List<String> getSubjects() {
        return subjects;
    }
}
