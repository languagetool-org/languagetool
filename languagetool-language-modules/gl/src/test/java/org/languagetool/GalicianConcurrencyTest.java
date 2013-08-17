package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Galician;

public class GalicianConcurrencyTest extends AbstractLanguageConcurrencyTest {

	@Override
	protected Language createLanguage() {
		return new Galician();
	}

	@Override
	protected String createSampleText() {
		return "Olle as páxinas da Galipedia que ligan con este título.";
	}

}
