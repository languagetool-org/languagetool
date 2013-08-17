package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Khmer;

public class KhmerConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Khmer();
	}

	@Override
	protected String createSampleText() {
		return "បច្ចុប្បន្នគ្មានអត្ថបទក្នុងទំព័រនេះទេ។";
	}
}
