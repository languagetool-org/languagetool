package org.languagetool.tokenizers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.ManualTagger;
import org.languagetool.tagging.Tagger;

/**
 * Adapter from {@link ManualTagger} to {@link Tagger}. <br/>
 * Note: It resides in "test" package because for now it is only used on unit
 * testing.
 */
public class ManualTaggerAdapter implements Tagger {

	private ManualTagger manualTagger;

	public ManualTaggerAdapter(ManualTagger manualTagger) {
		this.manualTagger = manualTagger;
	}

	@Override
	public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens)
			throws IOException {
		final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
		int pos = 0;
		for (final String word : sentenceTokens) {
			final List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
			final String[] manualTags = manualTagger.lookup(word.toLowerCase());
			if (manualTags != null) {
				for (int i = 0; i < manualTags.length; i = i + 2) {
					l.add(new AnalyzedToken(word, manualTags[i + 1],
							manualTags[i]));
				}
			}
			if (l.isEmpty()) {
				l.add(new AnalyzedToken(word, null, null));
			}
			tokenReadings.add(new AnalyzedTokenReadings(l
					.toArray(new AnalyzedToken[l.size()]), pos));
			pos += word.length();
		}

		return tokenReadings;
	}

	@Override
	public AnalyzedTokenReadings createNullToken(String token, int startPos) {
		return new AnalyzedTokenReadings(new AnalyzedToken(token, null, null),
				startPos);
	}

	@Override
	public AnalyzedToken createToken(String token, String posTag) {
		return new AnalyzedToken(token, posTag, null);
	}

}