package org.languagetool.tokenizers.zh;

import com.hankcs.hanlp.tokenizer.TraditionalChineseTokenizer;
import com.hankcs.hanlp.seg.common.Term;
import org.languagetool.tokenizers.Tokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TChinsesWordTokenizer implements Tokenizer {



    @Override
    public List<String> tokenize(String text) {
        List<Term> termList = TraditionalChineseTokenizer.segment(text);
        List<String> termStringList = new ArrayList<>();
        for (Term term: termList) {
            termStringList.add(term.toString());
        }
        return termStringList;
    }
}
