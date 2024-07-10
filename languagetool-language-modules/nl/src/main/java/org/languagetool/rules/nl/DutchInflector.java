package org.languagetool.rules.nl;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.languagetool.rules.spelling.CachingWordListLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DutchInflector {
    public static final DutchInflector INSTANCE = new DutchInflector();
    protected final CachingWordListLoader wordListLoader = new CachingWordListLoader();
    //NOUNS
    protected final Set<String> nouns_de = new ObjectOpenHashSet<>();
    protected final Set<String> nouns_het = new ObjectOpenHashSet<>();
    protected final Set<String> nouns_de_sf = new ObjectOpenHashSet<>();
    protected final Set<String> nouns_het_sf = new ObjectOpenHashSet<>();
    private static final String NOUNS_DE = "nl/inflector/nouns_de.txt";
    private static final String NOUNS_HET = "nl/inflector/nouns_het.txt";
    private static final String NOUNS_DE_SF = "nl/inflector/nouns_de_sf.txt";
    private static final String NOUNS_HET_SF = "nl/inflector/nouns_het_sf.txt";
    //VERBS
    protected final Set<String> verbs_xde = new ObjectOpenHashSet<>();
    protected final Set<String> verbs_xte = new ObjectOpenHashSet<>();
    private static final String VERBS_XDE = "nl/inflector/verbs_xde.txt";
    private static final String VERBS_XTE = "nl/inflector/verbs_xte.txt";

    public DutchInflector() {
        nouns_de.addAll(wordListLoader.loadWords(NOUNS_DE));
        nouns_het.addAll(wordListLoader.loadWords(NOUNS_HET));
        // for nouns where the consonant changes when plural
        nouns_de_sf.addAll(wordListLoader.loadWords(NOUNS_DE_SF));
        nouns_het_sf.addAll(wordListLoader.loadWords(NOUNS_HET_SF));
        // for verbs
        verbs_xde.addAll(wordListLoader.loadWords(VERBS_XDE));
        verbs_xte.addAll(wordListLoader.loadWords(VERBS_XTE));
    }

    public List<String> getPOSTag(String word) {
        List<String> result = new ArrayList<>(2);
        result.add(null);
        result.add(null);

        // Check all noun sets
        String[] tagAndLemma = checkAllLemmas(word);
        if (tagAndLemma != null) {
            result.set(0, tagAndLemma[0]);
            result.set(1, tagAndLemma[1]);
        }
        return result;
    }

    private String[] checkAllLemmas(String word) {
        String[] tagAndLemma;

        // check nouns
        tagAndLemma = checkLemmas(word, nouns_de, this::nounsDe);
        if (tagAndLemma != null) return tagAndLemma;

        tagAndLemma = checkLemmas(word, nouns_het, this::nounsHet);
        if (tagAndLemma != null) return tagAndLemma;

        tagAndLemma = checkLemmas(word, nouns_de_sf, this::nounsDe);
        if (tagAndLemma != null) return tagAndLemma;

        tagAndLemma = checkLemmas(word, nouns_het_sf, this::nounsHet);
        if (tagAndLemma != null) return tagAndLemma;

        // check verbs
        // to do: add verb check even if noun was found
        tagAndLemma = checkLemmas(word, verbs_xde, this::verbsXde);
        if (tagAndLemma != null) return tagAndLemma;

        tagAndLemma = checkLemmas(word, verbs_xte, this::verbsXte);
        if (tagAndLemma != null) return tagAndLemma;

        return null;
    }

    private String[] checkLemmas(String word, Set<String> lemmas, inflectionLogic logic) {
        for (String lemma : lemmas) {
            String foundTag = logic.apply(word, lemma);
            if (foundTag != null) {
                //System.out.println(word + " gets tag " + foundTag);
                return new String[]{foundTag, lemma};
            }
        }
        return null;
    }

    private String nounsDe(String word, String lemma) {
        if (word.equals(lemma)) return "ZNW:EKV:DE_";
        String commonTag = nounsCommonInflection(word, lemma);
        if (commonTag != null) return commonTag;
        return null;
    }

    private String nounsHet(String word, String lemma) {
        if (word.equals(lemma)) return "ZNW:EKV:HET";
        String commonTag = nounsCommonInflection(word, lemma);
        if (commonTag != null) return commonTag;
        return null;
    }

    private String nounsCommonInflection(String word, String lemma) {
        if ( word.equals(lemma + "je")){
            return "ZNW:EKV:VRK:HET";
        } else if ( word.equals(lemma + "jes")){
            return "ZNW:MRV:VRK:DE_";
        } else if (lemma.endsWith("f") && (word.equals(lemma.substring(0, lemma.length() - 1) + "ven"))){
            return "ZNW:MRV:DE_";
        } else if (lemma.endsWith("s") && (word.equals(lemma.substring(0, lemma.length() - 1) + "zen"))){
            return "ZNW:MRV:DE_";
        } else if ( word.equals(lemma + "en")){
            return "ZNW:MRV:DE_";
        }
        return null;
    }

    private String verbsCommonInflection(String word, String lemma, String fpp) {
        if (word.equals(lemma)) {
            return "WKW:TGW:INF";
        } else if (word.equals(lemma + "de")) {
            return "WKW:ODW:VRB";
        } else if (word.equals(lemma + "den")) {
            return "WKW:ODW:MRV:DE_";
        } else if (word.equals(fpp + "t")) {
            return "WKW:TGW:3EP";
        } else if (word.equals(fpp + "d")) {
            return "WKW:ODW:ONV";
        } else if (word.equals(fpp)) {
            return "WKW:TGW:1EP";
        }
        return null;
    }

    private String verbsXde(String word, String lemma) {
        // spartelen > spartelde
        String fpp = lemma.substring(0, lemma.length() - 2);
        String commonTag = verbsCommonInflection(word, lemma, fpp);
        if (commonTag != null) return commonTag;
        if (word.equals(fpp + "de")){
            return "WKW:VLT:1EP";
        } else if (word.equals(fpp + "den")){
            return "WKW:VLT:INF";
        } else if (word.equals("ge" + fpp + "d")){
            return "WKW:VTD:ONV";
        } else if (word.equals("ge" + fpp + "de")){
            return "WKW:VTD:VRB";
        } else if (word.equals("ge" + fpp + "den")){
            return "WKW:VTD:ZNW:MRV:DE_";
        } else if (word.equals(fpp + "det")){
            return "WKW:VLT:GIJ";
        }
        return null;
    }

    private String verbsXte(String word, String lemma) {
        // janken > jankte
        String fpp = lemma.substring(0, lemma.length() - 2);
        String commonTag = verbsCommonInflection(word, lemma, fpp);
        if (commonTag != null) return commonTag;
        if (word.equals(fpp + "te")){
            return "WKW:VLT:1EP";
        } else if (word.equals(fpp + "ten")){
            return "WKW:VLT:INF";
        } else if (word.equals("ge" + fpp + "t")){
            return "WKW:VTD:ONV";
        } else if (word.equals("ge" + fpp + "te")){
            return "WKW:VTD:VRB";
        } else if (word.equals("ge" + fpp + "ten")){
            return "WKW:VTD:ZNW:MRV:DE_";
        } else if (word.equals(fpp + "tet")){
            return "WKW:VLT:GIJ";
        }
        return null;
    }

    private String verbsDoubleCons(String word, String lemma) {
        // stoffen > stofte
        return null;

    }

    private interface inflectionLogic {
        String apply(String word, String lemma);
    }

    public static void main(String[] args) {
        DutchInflector inflector = new DutchInflector();
        List<String> result = inflector.getPOSTag("kaasje");
        if (result.get(0) != null) {
            System.out.println("POS: " + result.get(0) + ", Lemma: " + result.get(1));
        } else {
            System.out.println("Word not found in inflector.");
        }
    }
}