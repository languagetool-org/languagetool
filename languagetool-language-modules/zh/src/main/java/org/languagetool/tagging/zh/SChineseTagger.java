package org.languagetool.tagging.zh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.Tagger;

public class SChineseTagger implements Tagger{

    @Override
    public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens)  {
        List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
        int pos = 0;
        for (String word : sentenceTokens) {
            List<AnalyzedToken> l = new ArrayList<>();
            AnalyzedToken at = createToken(word);
            l.add(at);
            tokenReadings.add(new AnalyzedTokenReadings(l, pos));
            pos += word.length();
        }
        return tokenReadings;
    }

    @Override
    public final AnalyzedTokenReadings createNullToken(String token, int startPos) {
        return new AnalyzedTokenReadings(new AnalyzedToken(token, null, null), startPos);
    }

    @Override
    public AnalyzedToken createToken(String token, String posTag) {
        return new AnalyzedToken(token, posTag, null);
    }

    private AnalyzedToken createToken(String word) {
        if(!word.contains("/")) {
            return createToken(word, null);
            // return new Analyzed(word, null, null);
        }
        String[] parts = word.split("/");
        // return new Analyzed(parts[0], parts[1], null);
        return createToken(parts[0], parts[1]);
    }

}
