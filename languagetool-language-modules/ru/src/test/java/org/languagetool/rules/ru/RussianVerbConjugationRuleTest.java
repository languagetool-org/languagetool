package org.languagetool.rules.ru;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Russian;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by zaets on 30.05.2017.
 */
public class RussianVerbConjugationRuleTest {

    private Set<String> rightSentences = ImmutableSet.of("Я иду", "Она сидит", "Оно думает",
            "Они пишут", "Мы думаем", "Ты читаешь", "Он творит", "Вы идёте",
            "Я ходил", "Они ходили", "Мы ходили", "Она ходила", "Оно ходило", "Я ходила",
            "Я пойду", "Она пойдёт", "Оно пойдёт", "Мы пойдём", "Ты пойдёшь");

    private Set<String> wrongSentences = ImmutableSet.of("Я идёт", "Она сидят",
            "Оно думаешь","Они идёте","Мы думаю","Ты читает", "Он творю",
            "Я ходили", "Они ходил", "Мы ходила", "Она ходил", "Оно ходила", "Я ходило",
            "Я пойдёт", "Она пойдут", "Оно пойдёте", "Мы пойдёшь", "Ты пойду");

    @Test
    public void testRussianVerbConjugationRule() throws IOException {
        RussianVerbConjugationRule rule = new RussianVerbConjugationRule(TestTools.getEnglishMessages());
        JLanguageTool lt = new JLanguageTool(new Russian());

        for (String sentence : wrongSentences) {
            System.out.println(sentence);
            AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(sentence);
            assertEquals(1, rule.match(analyzedSentence).length);
        }

        for(String sentence : rightSentences) {
            System.out.println(sentence);
            AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(sentence);
            assertEquals(0, rule.match(analyzedSentence).length);
        }
    }
}
