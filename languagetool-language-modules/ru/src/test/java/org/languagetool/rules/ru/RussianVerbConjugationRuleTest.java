/* LanguageTool, a natural language style checker
 * Copyright (C) 2010 Daniel Naber (http://www.languagetool.org)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
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

public class RussianVerbConjugationRuleTest {

    private Set<String> rightSentences = ImmutableSet.of("Я иду", "Она сидит", "Оно думает",
            "Они пишут", "Мы думаем", "Ты читаешь", "Он творит", "Вы идёте",
            "Я ходил", "Они ходили", "Мы ходили", "Она ходила", "Оно ходило", "Я ходила",
            "Я пойду", "Она пойдёт", "Оно пойдёт", "Мы пойдём", "Ты пойдёшь", "Я согласился на предложение.", "Джек и я согласились" );

    private Set<String> wrongSentences = ImmutableSet.of("Я идёт", "Она сидят",
            "Оно думаешь","Они идёте","Мы думаю","Ты читает", "Он творю",
            "Я ходили", "Они ходил", "Мы ходила", "Она ходил", "Оно ходила", "Я ходило",
            "Я пойдёт", "Она пойдут", "Оно пойдёте", "Мы пойдёшь", "Ты пойду");

    @Test
    public void testRussianVerbConjugationRule() throws IOException {
        RussianVerbConjugationRule rule = new RussianVerbConjugationRule(TestTools.getEnglishMessages());
        JLanguageTool lt = new JLanguageTool(new Russian());

        for (String sentence : wrongSentences) {
            AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(sentence);
            assertEquals("Expected error in sentence: " + sentence, 1, rule.match(analyzedSentence).length);
        }

        for(String sentence : rightSentences) {
            AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(sentence);
            assertEquals("Did not expect error in sentence: " + sentence, 0, rule.match(analyzedSentence).length);
        }
    }
}
