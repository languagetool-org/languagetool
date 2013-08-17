package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Esperanto;

public class EsperantoConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Esperanto();
	}

	@Override
	protected String createSampleText() {
		return "Kreu la artikolon Enriched text aŭ aldonu peton pri ĝi.";
	}
}
