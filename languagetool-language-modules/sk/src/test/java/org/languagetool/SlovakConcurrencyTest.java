package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Slovak;

public class SlovakConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Slovak();
	}

	@Override
	protected String createSampleText() {
		return "Wikipédia neobsahuje článok s takýmto názvom.";
	}

}
