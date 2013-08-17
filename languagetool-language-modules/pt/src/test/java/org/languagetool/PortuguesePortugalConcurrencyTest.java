package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.PortuguesePortugal;

public class PortuguesePortugalConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new PortuguesePortugal();
	}

	@Override
	protected String createSampleText() {
		return "Outras razões pelas quais esta mensagem pode aparecer";
	}
}
