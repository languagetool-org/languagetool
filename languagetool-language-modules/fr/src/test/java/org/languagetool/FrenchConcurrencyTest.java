package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.French;

public class FrenchConcurrencyTest extends AbstractLanguageConcurrencyTest {

	@Override
	protected Language createLanguage() {
		return new French();
	}

	@Override
	protected String createSampleText() {
		return "Cherchez d'autres pages de Wikipédia pointant vers ce titre.";
	}

}
