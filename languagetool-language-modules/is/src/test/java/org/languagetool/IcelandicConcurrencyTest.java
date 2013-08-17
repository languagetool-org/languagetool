package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Icelandic;

public class IcelandicConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Icelandic();
	}

	@Override
	protected String createSampleText() {
		return "Enginn texti er á þessari síðu enn sem komið er. Þú getur leitað í öðrum síðum, leitað í tengdum skrám, eða breytt henni sjálfur.";
	}
}
