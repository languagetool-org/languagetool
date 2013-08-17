package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.PortugueseBrazil;

public class PortugueseBrazilConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new PortugueseBrazil();
	}

	@Override
	protected String createSampleText() {
		return "Outras razões pelas quais esta mensagem pode aparecer";
	}

}
