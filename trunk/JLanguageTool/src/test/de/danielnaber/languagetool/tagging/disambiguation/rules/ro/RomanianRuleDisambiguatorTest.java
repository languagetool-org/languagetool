package de.danielnaber.languagetool.tagging.disambiguation.rules.ro;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.tagging.disambiguation.xx.DemoDisambiguator;
import de.danielnaber.languagetool.tagging.ro.RomanianTagger;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.ro.RomanianWordTokenizer;

public class RomanianRuleDisambiguatorTest extends TestCase {

	private RomanianTagger tagger;
	private RomanianWordTokenizer tokenizer;
	private SentenceTokenizer sentenceTokenizer;
	private RomanianRuleDisambiguator disambiguator;
	private DemoDisambiguator disamb2;

	public void setUp() {
		tagger = new RomanianTagger();
		tokenizer = new RomanianWordTokenizer();
		sentenceTokenizer = new SentenceTokenizer();
		disambiguator = new RomanianRuleDisambiguator();
		disamb2 = new DemoDisambiguator();
	}

	public void testCare1() throws IOException {
		TestTools
				.myAssert(
						"Persoana care face treabă.",
						"/[null]SENT_START Persoana/[persoană]Sfs3aac000  /[null]null care/[car]Snp3anc000|care/[care]0000000000|care/[care]N000a0l000|care/[căra]V0p3000cz0|care/[căra]V0s3000cz0  /[null]null face/[face]V000000f00|face/[face]V0s3000iz0  /[null]null treabă/[treabă]Sfs3anc000 ./[null]null",
						tokenizer, sentenceTokenizer, tagger, disamb2);
		TestTools
				.myAssert(
						"Persoana care face treabă.",
						"/[null]SENT_START Persoana/[persoană]Sfs3aac000  /[null]null care/[care]N000a0l000  /[null]null face/[face]V000000f00|face/[face]V0s3000iz0  /[null]null treabă/[treabă]Sfs3anc000 ./[null]null",
						tokenizer, sentenceTokenizer, tagger, disambiguator);

	}

	public void testEsteO() throws IOException {
		TestTools
				.myAssert(
						"este o masă.",
						"/[null]SENT_START este/[fi]V0s3000izb  /[null]null o/[o]Dfs3a0t000|o/[o]I00000o000|o/[o]Nfs3a0p00c|o/[o]Sms3anc000|o/[vrea]V0s3000iov  /[null]null masă/[masa]V0s3000is0|masă/[masă]Sfs3anc000 ./[null]null",
						tokenizer, sentenceTokenizer, tagger, disamb2);
		TestTools
				.myAssert(
						"este o masă.",
						"/[null]SENT_START este/[fi]V0s3000izb  /[null]null o/[o]Dfs3a0t000|o/[o]I00000o000|o/[o]Nfs3a0p00c|o/[o]Sms3anc000|o/[vrea]V0s3000iov  /[null]null masă/[masă]Sfs3anc000 ./[null]null",
						tokenizer, sentenceTokenizer, tagger, disambiguator);
		TestTools
				.myAssert(
						"este o masă",
						"/[null]SENT_START este/[fi]V0s3000izb  /[null]null o/[o]Dfs3a0t000|o/[o]I00000o000|o/[o]Nfs3a0p00c|o/[o]Sms3anc000|o/[vrea]V0s3000iov  /[null]null masă/[masă]Sfs3anc000",
						tokenizer, sentenceTokenizer, tagger, disambiguator);

	}

	public void testDezambiguizareVerb() throws IOException {
		TestTools
				.myAssert(
						"vom participa la",
						"/[null]SENT_START vom/[vrea]V0p1000ivv  /[null]null participa/[participa]V000000f00|participa/[participa]V0s3000ii0  /[null]null la/[la]P000000000|la/[la]Sms3anc000",
						tokenizer, sentenceTokenizer, tagger, disamb2);
		TestTools
				.myAssert(
						"vom participa la",
						"/[null]SENT_START vom/[vrea]V0p1000ivv  /[null]null participa/[participa]V000000f00  /[null]null la/[la]P000000000|la/[la]Sms3anc000",
						tokenizer, sentenceTokenizer, tagger, disambiguator);

		TestTools
				.myAssert(
						"vom culege",
						"/[null]SENT_START vom/[vrea]V0p1000ivv  /[null]null culege/[culege]V000000f00|culege/[culege]V0s2000m00|culege/[culege]V0s3000iz0",
						tokenizer, sentenceTokenizer, tagger, disamb2);
		TestTools
				.myAssert(
						"vom culege",
						"/[null]SENT_START vom/[vrea]V0p1000ivv  /[null]null culege/[culege]V000000f00",
						tokenizer, sentenceTokenizer, tagger, disambiguator);
		TestTools
				.myAssert(
						"veți culege",
						"/[null]SENT_START veți/[vrea]V0p2000ivv  /[null]null culege/[culege]V000000f00",
						tokenizer, sentenceTokenizer, tagger, disambiguator);
	}
}
