#!/usr/bin/python
# -*- coding: iso-8859-1 -*-
# Provide user information about BNC tags
#
# LanguageTool -- A Rule-Based Style and Grammar Checker
# Copyright (C) 2002,2003,2004 Daniel Naber <daniel.naber@t-online.de>
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

import re
import sys

class TagInfo:

	TAG_STRING = {}
	TAG_STRING['en'] = """AJ0 Adjective (general or positive) (e.g. good, old, beautiful)
		AJC Comparative adjective (e.g. better, older)
		AJS Superlative adjective (e.g. best, oldest)
		AT0 Article (e.g. the, a, an, no) [N.B. no is included among articles, which are defined here as determiner words which typically begin a noun phrase, but which cannot occur as the head of a noun phrase.]
		AV0 General adverb: an adverb not subclassified as AVP or AVQ (see below) (e.g. often, well, longer (adv.), furthest. [Note that adverbs, unlike adjectives, are not tagged as positive, comparative, or superlative. This is because of the relative rarity of comparative and superlative adverbs.]
		AVP Adverb particle (e.g. up, off, out) [N.B. AVP is used for such "prepositional adverbs", whether or not they are used idiomatically in a phrasal verb: e.g. in 'Come out here' and 'I can't hold out any longer', the same AVP tag is used for out.
		AVQ Wh-adverb (e.g. when, where, how, why, wherever) [The same tag is used, whether the word occurs in interrogative or relative use.]
		CJC Coordinating conjunction (e.g. and, or, but)
		CJS Subordinating conjunction (e.g. although, when)
		CJT The subordinating conjunction that [N.B. that is tagged CJT when it introduces not only a nominal clause, but also a relative clause, as in 'the day that follows Christmas'. Some theories treat that here as a relative pronoun, whereas others treat it as a conjunction.We have adopted the latter analysis.]
		CRD Cardinal number (e.g. one, 3, fifty-five, 3609)
		DPS Possessive determiner (e.g. your, their, his)
		DT0 General determiner: i.e. a determiner which is not a DTQ. [Here a determiner is defined as a word which typically occurs either as the first word in a noun phrase, or as the head of a noun phrase. E.g. This is tagged DT0 both in 'This is my house' and in 'This house is mine'.]
		DTQ Wh-determiner (e.g. which, what, whose, whichever) [The category of determiner here is defined as for DT0 above. These words are tagged as wh-determiners whether they occur in interrogative use or in relative use.]
		EX0 Existential there, i.e. there occurring in the there is ... or there are ... construction
		ITJ Interjection or other isolate (e.g. oh, yes, mhm, wow)

		NN0 Common noun, neutral for number (e.g. aircraft, data, committee) [N.B. Singular collective nouns such as committee and team are tagged NN0, on the grounds that they are capable of taking singular or plural agreement with the following verb: e.g. 'The committee disagrees/disagree'.]
		NN1 Singular common noun (e.g. pencil, goose, time, revelation)
		NN2 Plural common noun (e.g. pencils, geese, times, revelations)
		NP0 Proper noun (e.g. London, Michael, Mars, IBM) [N.B. the distinction between singular and plural proper nouns is not indicated in the tagset, plural proper nouns being a comparative rarity.]
		ORD Ordinal numeral (e.g. first, sixth, 77th, last) . [N.B. The ORD tag is used whether these words are used in a nominal or in an adverbial role. Next and last, as "general ordinals", are also assigned to this category.]
		PNI Indefinite pronoun (e.g. none, everything, one [as pronoun], nobody) [N.B. This tag applies to words which always function as [heads of] noun phrases. Words like some and these, which can also occur before a noun head in an article-like function, are tagged as determiners (see DT0 and AT0 above).]
		PNP Personal pronoun (e.g. I, you, them, ours) [Note that possessive pronouns like ours and theirs are tagged as personal pronouns.]
		PNQ Wh-pronoun (e.g. who, whoever, whom) [N.B. These words are tagged as wh-pronouns whether they occur in interrogative or in relative use.]
		PNX Reflexive pronoun (e.g. myself, yourself, itself, ourselves)

		POS The possessive or genitive marker 's or ' (e.g. for 'Peter's or somebody else's', the sequence of tags is: NP0 POS CJC PNI AV0 POS)
		PRF The preposition of. Because of its frequency and its almost exclusively postnominal function, of is assigned a special tag of its own.
		PRP Preposition (except for of) (e.g. about, at, in, on, on behalf of, with)
		PUL Punctuation: left bracket - i.e. ( or [
		PUN Punctuation: general separating mark - i.e. . , ! , : ; - or ?
		PUQ Punctuation: quotation mark - i.e. ' or "
		PUR Punctuation: right bracket - i.e. ) or ]
		TO0 Infinitive marker to 
		UNC Unclassified items which are not appropriately classified as items of the English lexicon. [Items tagged UNC include foreign (non-English) words, special typographical symbols, formulae, and (in spoken language) hesitation fillers such as er and erm.]

		VBB The present tense forms of the verb BE, except for is, 's: i.e. am, are, 'm, 're and be [subjunctive or imperative]
		VBD The past tense forms of the verb BE: was and were
		VBG The -ing form of the verb BE: being
		VBI The infinitive form of the verb BE: be
		VBN The past participle form of the verb BE: been
		VBZ The -s form of the verb BE: is, 's

		VDB The finite base form of the verb DO: do
		VDD The past tense form of the verb DO: did
		VDG The -ing form of the verb DO: doing
		VDI The infinitive form of the verb DO: do
		VDN The past participle form of the verb DO: done
		VDZ The -s form of the verb DO: does, 's

		VHB The finite base form of the verb HAVE: have, 've
		VHD The past tense form of the verb HAVE: had, 'd
		VHG The -ing form of the verb HAVE: having
		VHI The infinitive form of the verb HAVE: have
		VHN The past participle form of the verb HAVE: had
		VHZ The -s form of the verb HAVE: has, 's

		VM0 Modal auxiliary verb (e.g. will, would, can, could, 'll, 'd)

		VVB The finite base form of lexical verbs (e.g. forget, send, live, return) [Including the imperative and present subjunctive]
		VVD The past tense form of lexical verbs (e.g. forgot, sent, lived, returned)
		VVG The -ing form of lexical verbs (e.g. forgetting, sending, living, returning)
		VVI The infinitive form of lexical verbs (e.g. forget, send, live, return)
		VVN The past participle form of lexical verbs (e.g. forgotten, sent, lived, returned)
		VVZ The -s form of lexical verbs (e.g. forgets, sends, lives, returns)

		XX0 The negative particle not or n't 
		ZZ0 Alphabetical symbols (e.g. A, a, B, b, c, d)"""

	TAG_STRING['de'] = """ADJ Adjective (general) (e.g. gut, alt)
		ADJE Comparative adjective (e.g. alte)
		ADJER  adjective with er Ending (e.g. alter)
		ADJES  adjective with es Ending (e.g. altes)
		ADJEM  adjective with em Ending (e.g. altem)
		ADJEN  adjective with en Ending (e.g. alten)
		*ADV  Adverb like abends, morgen
		
		PRA  Pronoun with accusativ  wider, gegen
		PRD  Pronoun with dativ  ab, aus
		PRD  Pronoun with accusativ or dativ  in, über
		
		PP1  Personal pronoun ich, mich, mir
		PP2  Personal pronoun du
		PP3  Personal pronoun er, sie, es
		PP4  Personal pronoun wir
		PP5  Personal pronoun ihr
		
		*IND  oh, ah, heisa
		*INT  Interrogating word like Wer, wo, etc...
		
		CNT  Number
		CJC  Conjunctive word like und, oder, ...
		
		V    verb, e.g. gehen
		V11  verb, e.g. gehe
		V12  verb, e.g. gehst
		V13  verb, e.g. geht
		V14  verb, e.g. gehen
		V15  verb, e.g. gehet
		
		HV   auxiliary verb, e.g. moegen
		HV11 auxiliary verb, e.g. mag
		HV12 auxiliary verb, e.g. magst
		HV13 auxiliary verb, e.g. mag
		HV14 auxiliary verb, e.g. moegen
		HV15 auxiliary verb, e.g. moeget
		
		N    Noun
		NMS  Noun male no ending, e.g. Garten
		NFS  Noun female no ending, e.g. Frau
		NNS  Noun neutrum no ending
		NFNS Noun female or neutrum no ending
		NFMS Noun female or male no ending
		NMNS Noun male or neutrum no ending
		NFMNS Noun male female or neutrum no ending
		NM  Noun male with ending like Gartens
		NF  Noun female with ending  like Frauen
		NN  Noun neutrum with ending
		NFN Noun female or neutrum with ending
		NFM Noun female or male with ending
		NMN Noun male or neutrum with ending
		NFMN Noun male female or neutrum with ending
		
		UA1   indefinite article ein
		UAE   indefinite article eine
		UAR   indefinite article einer
		UAN   indefinite article einen
		UAM   indefinite article einem
		UAS   indefinite article eines
		* INT,IND,ADV sometimes mixed up in the word collection - to be corrected"""

	TAG_STRING['hu'] = """ADJS Singular adjective (e.g.  szep)
		ADJP Plural Adjective (e.g. szepek)
		ADJN Numeric Adjective (e.g. tizedik)
		ADV  Adverb like szepen, jol
		NS   Noun, singular  asztalnak
		NSN  Noun, singular, nominativ asztal
		NSR  Noun, singular, not nominativ asztalt
		NP   Noun, plural asztalokat
		NPN  Noun, plural, nominativ asztalok
		NPR  Noun, plural, not nominativ asztalokra
		V1   Verb, Singular, 1-st person  irok
		V2   Verb, Singular, 2-nd person
		V3   Verb, Singular, 3-rd person
		V4   Verb, Plural, 1-st person
		V5   Verb, Plural, 2-nd person
		V6   Verb, Plural, 3-rd person
		VINF Verb infinitiv
		IKV1  Prefixed Verb, Singular, 1-st person megirok
		IKV2  Prefixed Verb, Singular, 2-nd person
		IKV3  Prefixed Verb, Singular, 3-rd person
		IKV4  Prefixed Verb, Plural, 1-st person
		IKV5  Prefixed Verb, Plural, 2-nd person
		IKV6  Prefixed Verb, Plural, 3-rd person
		VINF  Prefixed Verb infinitiv
		SI1   Help Verb, Singular, 1-st person akarok
		SI2   Help Verb, Singular, 2-nd person
		SI3   Help Verb, Singular, 3-rd person
		SI4   Help Verb, Plural, 1-st person
		SI5   Help Verb, Plural, 2-nd person
		SI6   Help Verb, Plural, 3-rd person
		SIINF Help Verb infinitiv
		IKSI1 Prefixed Help Verb, Singular, 1-st person  megvagyok
		IKSI2 Prefixed Help Verb, Singular, 2-nd person
		IKSI3 Prefixed Help Verb, Singular, 3-rd person
		IKSI4 Prefixed Help Verb, Plural, 1-st person
		IKSI5 Prefixed Help Verb, Plural, 2-nd person
		IKSI6 Prefixed Help Verb, Plural, 3-rd person
		IKSIINF Prefixed Help Verb infinitiv
		NEIK  Non detachable verb prefix be, ki, le, fel, etc...
		PP1  Personal pronom en
		PP2  Personal pronom te
		PP3  Personal pronom o
		PP4  Personal pronom mi
		PP5  Personal pronom ti
		PP6  Personal pronom ok
		RPP1 Owning Personal Pronom enyem
		RPP2 Owning Personal Pronom tied
		RPP3 Owning Personal Pronom ove
		RPP4 Owning Personal Pronom mienk
		RPP5 Owning Personal Pronom tietek
		RPP6 Owning Personal Pronom ovek
		IND  uhum
		INT  Interrogating word like nemde etc...
		CRD  Number tizenot
		INTRN Numerical interrogation mennyi, etc...
		INTR Interrogation miert, etc...
		CJC  Conjunctive word like es vagy, ...
		DNV  Double role, Noun and verb var
		DAV  Double role, Adj and Verb irt
		DNA  Double role, Noun and ADJ or ADV iro ...	
		RART Conjunction word like de, hogy
		"""

	def __init__(self, lang):
		if not self.TAG_STRING.has_key(lang):
			raise KeyError, "no information found for language '%s'" % lang
		tag_lines = re.split("\n", self.TAG_STRING[lang])
		self.tags = []		# [(short, explanation)]
		for tag_line in tag_lines:
			tag_line = tag_line.strip()
			parts = re.split("\s+", tag_line)
			short_tag = parts[0]
			tag_exp = str.join(' ', parts[1:])
			self.tags.append((short_tag, tag_exp))
		return

	def getExp(self, short_tag_search):
		for (tag_short, tag_exp) in self.tags:
			if short_tag_search == tag_short:
				return tag_exp
		return None

	def getJavascriptCode(self):
		l = []
		for (tag_short, tag_exp) in self.tags:
			tag_exp = tag_exp.replace("\"", "\\\"")
			l.append('data["%s"] = "%s";' % (tag_short, tag_exp))
		return str.join('\n', l)
		
	def getHTMLCode(self):
		l = []
		l.append('<table border="0" cellpadding="0" cellspacing="2">')
		for (tag_short, tag_exp) in self.tags:
			tag_exp = tag_exp.replace("\"", "\\\"")
			if tag_short:
				l.append('<tr bgcolor="#dddddd"><td valign="top"><strong>%s</strong></td><td>%s</td></tr>' % (tag_short, tag_exp))
			else:
				l.append('<tr><td>&nbsp;</td></tr>')
		l.append('</table>')
		return str.join('\n', l)

	def printAll(self):
		for (tag_short, tag_exp) in self.tags:
			if tag_short:
				print "%s: %s" % (tag_short, tag_exp)
			else:
				print
		return

if __name__ == "__main__":
	# TODO: take language as parameter
	if len(sys.argv) < 2:
		print "Usage: TagInfo.py <language>"
		print "	where <language> is a language code like en, de, ..."
		sys.exit(1)
	taginfo = TagInfo(sys.argv[1])
	taginfo.printAll()
