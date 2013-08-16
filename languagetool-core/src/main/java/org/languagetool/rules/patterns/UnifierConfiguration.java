package org.languagetool.rules.patterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnifierConfiguration {
	/**
	 * A Map for storing the equivalence types for features. Features are
	 * specified as Strings, and map into types defined as maps from Strings to
	 * Elements.
	 */
	private final Map<EquivalenceTypeLocator, Element> equivalenceTypes;

	/**
	 * A Map that stores all possible equivalence types listed for features.
	 */
	private final Map<String, List<String>> equivalenceFeatures;

	public UnifierConfiguration() {
		this.equivalenceTypes = new HashMap<EquivalenceTypeLocator, Element>();
		this.equivalenceFeatures = new HashMap<String, List<String>>();
	}

	/**
	 * Prepares equivalence types for features to be tested. All equivalence
	 * types are given as {@link Element}s. They create an equivalence set (with
	 * abstraction).
	 * 
	 * @param feature
	 *            Feature to be tested, like gender, grammatical case or number.
	 * @param type
	 *            Type of equivalence for the feature, for example plural, first
	 *            person, genitive.
	 * @param elem
	 *            Element specifying the equivalence.
	 */
	public final void setEquivalence(final String feature, final String type,
			final Element elem) {
		if (equivalenceTypes.containsKey(new EquivalenceTypeLocator(feature,
				type))) {
			return;
		}
		equivalenceTypes.put(new EquivalenceTypeLocator(feature, type), elem);
		final List<String> lTypes;
		if (equivalenceFeatures.containsKey(feature)) {
			lTypes = equivalenceFeatures.get(feature);
		} else {
			lTypes = new ArrayList<String>();
		}
		lTypes.add(type);
		equivalenceFeatures.put(feature, lTypes);
	}

	public Map<String, List<String>> getEquivalenceFeatures() {
		return equivalenceFeatures;
	}

	public Map<EquivalenceTypeLocator, Element> getEquivalenceTypes() {
		return equivalenceTypes;
	}
	
	public Unifier createUnifier() {
		return new Unifier(this.equivalenceTypes, this.equivalenceFeatures);
	}
}
