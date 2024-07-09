package org.languagetool.rules.nl;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.languagetool.rules.spelling.CachingWordListLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DutchInflector {
    public static final DutchInflector INSTANCE = new DutchInflector();
    protected final CachingWordListLoader wordListLoader = new CachingWordListLoader();
    protected final Set<String> nouns_de = new ObjectOpenHashSet<>();
    protected final Set<String> nouns_het = new ObjectOpenHashSet<>();
    private static final String NOUNS_DE = "nl/inflector/nouns_de.txt";
    private static final String NOUNS_HET = "nl/inflector/nouns_het.txt";

    public DutchInflector() {
        nouns_de.addAll(wordListLoader.loadWords(NOUNS_DE));
        nouns_het.addAll(wordListLoader.loadWords(NOUNS_HET));
    }

    public List<String> getPOSTag(String word) {
        List<String> result = new ArrayList<>(2);
        result.add(null);
        result.add(null);
        if (nouns_de.contains(word)) {
            //add logic to check for DE nouns
            result.set(0, "ZNW:EKV:DE_");
            result.set(1, word);
        } else if (nouns_het.contains(word)) {
            //add logic to check for HET nouns
            result.set(0, "ZNW:EKV:HET");
            result.set(1, word);
        }
        return result;
    }
}
