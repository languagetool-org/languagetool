package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Russian;

public class RussianConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Russian();
	}

	@Override
	protected String createSampleText() {
		return "Материал из Википедии — свободной энциклопедии";
	}

}
