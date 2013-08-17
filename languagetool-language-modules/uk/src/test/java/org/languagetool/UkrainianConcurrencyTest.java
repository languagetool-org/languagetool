package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Ukrainian;

public class UkrainianConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Ukrainian();
	}

	@Override
	protected String createSampleText() {
		return "Матеріал з Вікіпедії — вільної енциклопедії.";
	}

}
