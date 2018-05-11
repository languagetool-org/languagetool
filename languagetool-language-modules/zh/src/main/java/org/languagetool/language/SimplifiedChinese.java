package org.languagetool.language;

import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.zh.SChineseTagger;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.zh.SChineseSentenceTokenizer;
import org.languagetool.tokenizers.zh.SChineseWordTokenizer;


public class SimplifiedChinese extends Chinese {

    private Tokenizer wordTokenizer;

    @Override
    public String getName() {
        return "SimplifiedChinese";
    }

    @Override
    public Tagger getTagger() {
        return new SChineseTagger();
    }

    @Override
    public Tokenizer getWordTokenizer() {
        if (wordTokenizer == null) {
            wordTokenizer = new SChineseWordTokenizer();
        }
        return wordTokenizer;
    }

    @Override
    public SentenceTokenizer getSentenceTokenizer() {
        return new SChineseSentenceTokenizer();
    }

}
