package org.languagetool.rules.ngrams;

import org.languagetool.Language;

import java.util.*;

public class GoogleTokenUtil {


    public static List<String> getGoogleTokensForString(String sentence, boolean addStartToken, Language language) {
        List<String> tokens = new LinkedList<>();
        for (GoogleToken token : GoogleToken.getGoogleTokens(sentence, addStartToken, language.getWordTokenizer())) {
            tokens.add(token.token);
        }
        return tokens;
    }
}
