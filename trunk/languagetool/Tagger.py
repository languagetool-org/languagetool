# -*- coding: iso-8859-1 -*-
# A probabilistic part-of-speech tagger (see the QTag paper) with
# a rule-based extension.
# (c) 2003 Daniel Naber <daniel.naber@t-online.de>
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or (at
# your option) any later version.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
# USA

import os
import re
import string
import sys
import cPickle
import htmlentitydefs

class Tagger:
	"""POS-tag any text. The result in XML can be used to re-build the original
	text by concatenating all contents of the <w> tags. Whitespace characters 
	have term=None and type=None, i.e. they are inside their own <w>
	elements. Words that could not be tagged have type=unknown."""

	db_word_name = os.path.join("data", "words")
	db_seq_name = os.path.join("data", "seqs")
	#uncountable_name = os.path.join("data", "uncountable.txt")
	
	def __init__(self, db_word_name=None, db_seq_name=None):
		"""Initialize the tagger, optionally using the given
		file names that will be used to load and save data later."""
		self.data_table = None
		self.seqs_table = None		# tag sequences: seqs_table[tag1,tag2,tag3] = value
		if db_word_name:
			self.db_word_name = db_word_name
		if db_seq_name:
			self.db_seq_name = db_seq_name
		#uncountable_nouns = self.loadUncountables()
		self.word_count = 0
		return

	def loadUncountables(self):
		"""TODO: not used yet."""
		l = []
		f = open(self.uncountable_name)
		while 1:
			line = f.readline()
			if not line:
				break
			line = line.strip()
			if not line.startswith("#") and line != '':
				l.append(line)
		f.close()
		return l
		
	def bindData(self):
		"""Load the word/POS tag and POS tag sequence data from disk."""
		try:
			self.data_table = cPickle.load(open(self.db_word_name))
		except IOError:
			print >> sys.stderr, "No date file '%s' yet, starting with empty table." % self.db_word_name
			self.data_table = {}
		try:
			self.seqs_table = cPickle.load(open(self.db_seq_name))
		except IOError:
			print >> sys.stderr, "No date file '%s' yet, starting with empty table." % self.db_seq_name
			self.seqs_table = {}
		return

	def commitData(self):
		"""Save the word/POS tag and POS tag sequence data to disk."""
		print >> sys.stderr, "Words = %d" % self.word_count
		print >> sys.stderr, "Known words = %d" % len(self.data_table.keys())
		print >> sys.stderr, "Known sequences = %d" % len(self.seqs_table.keys())
		print >> sys.stderr, "Commiting results..."
		cPickle.dump(self.data_table, open(self.db_word_name, 'w'), 1)
		cPickle.dump(self.seqs_table, open(self.db_seq_name, 'w'), 1)
		return
	
	def deleteData(self):
		"""Remove the word/POS tag and POS tag sequence data files from disk."""
		print >> sys.stderr, "Deleting old data files..."
		try:
			os.remove(self.db_word_name)
		except OSError, e:
			print >> sys.stderr, "Note: Could not delete file: %s" % e
		try:
			os.remove(self.db_seq_name)
		except OSError, e:
			print >> sys.stderr, "Note: Could not delete file: %s" % e
		return

	def buildData(self, filename):
		"""Load a BNC file in XML format and count the word/POS occurences
		and the POS tag sequences."""
		print >> sys.stderr, "Loading %s..." % filename
		text = PreTaggedText(filename)
		tagged_words = text.getTaggedWords()
		self.word_count = self.word_count + len(tagged_words)
		text.addToData(tagged_words, self.data_table, self.seqs_table)
		return

	def buildDataFromString(self, s):
		"""Take a string with format "word1/tag1 word2/tag2 ..." and
		count the word/POS occurences and the POS tag sequences.
		Only useful for the test cases."""
		pairs = re.compile("\s+").split(s)
		tagged_words = []
		split_regex = re.compile("/")
		for pair in pairs:
			pair = split_regex.split(pair)
			if len(pair) != 2:
				# e.g. punctuation
				continue
			word = pair[0]
			tag = pair[1]
			tagged_words.append((word, tag))
		text = TextToTag()
		text.addToData(tagged_words, self.data_table, self.seqs_table)
		return

	def tagFile(self, filename):
		"""POS-tag the contents of a text file and return XML that contains
		the original text with each word's POS tag in the "type"
		attribute."""
		text = TextToTag()
		text.setFilename(filename)
		tagged_words = text.tag(self.data_table, self.seqs_table)
		#print tagged_words
		xml = text.toXML(tagged_words)
		return xml

	def tagText(self, strng):
		"""POS-tag a string and return a list of (word, normalized word, tag)
		triples."""
		text = TextToTag()
		text.setText(strng)
		tagged_words = text.tag(self.data_table, self.seqs_table)
		return tagged_words

	def tagSeq(self, triple):
		"""Return the probability of a 3-POS-tag sequence."""
		if len(triple) != 3:
			#TODO?: throw exception
			print >> sys.stderr, "Sequence does not consist of 3 tokens: '%s'" % str(seq)
			return None
		try:
			probability = self.seqs_table[triple]
		except KeyError:
			probability = 0
		return probability

	def tagWord(self, word):
		"""See Text.tagWord()"""
		text = TextToTag()
		text.setText("")
		tag = text.tagWord(word, self.data_table)
		return tag

	def guessTagTest(self, word):
		"""See Text.guessTag(). For test cases only."""
		text = TextToTag()
		text.setText("")
		tag = text.guessTag(word)
		return tag

class Text:

	DUMMY = None
	number_regex = re.compile("^[0-9\W]+$")
	time_regex = re.compile("\d(am|pm)$")
	bnc_regex = re.compile("<w (.*?)>(.*?)<", re.DOTALL)

	mapping_file = os.path.join("data", "c7toc5.txt")

	def __init__(self):
		self.count_unambiguous = 0
		self.count_ambiguous = 0
		self.count_unknown = 0
		self.nonword = re.compile("([\s,:;]+)")
		self.nonword_punct = re.compile("([,:;]+)")
		self.sentence_end = re.compile("([.!?]+)$")
		self.bnc_word_regexp = re.compile("<W\s+TYPE=\"(.*?)\".*?>(.*?)</W>", \
			re.DOTALL|re.IGNORECASE)
		self.mapping = self.loadMapping()
		return
		
	def loadMapping (self):
		f = open(self.mapping_file)
		line_count = 1
		mapping = {}
		while 1:
			line = f.readline().strip()
			if not line:
				break
			l = re.split("\s+", line)
			if not len(l) == 2:
				print >> sys.stderr, "No valid mapping in line %d: '%s'" % (line_count, line)
			(c7, c5) = l[0], l[1]
			if mapping.has_key(c7):
				print >> sys.stderr, "No valid mapping in line %d: '%s', duplicate key '%s'" % (line_count, line, c7)
				continue
			mapping[c7] = c5
			#print "%s -> %s" % (c7, c5)
			line_count = line_count + 1
		f.close()
		return mapping
		
	def expandEntities(self, text):
		"""Take a text and expand a few selected entities. Return the same
		text with entities expanded. (We cannot simply parse the file with 
		DOM, as we don't have an XML DTD -- the original files were SGML.)"""
		text = re.compile("&amp;", re.IGNORECASE).sub("&", text)
		# TODO: several entities are missing here:
		#text = re.compile("&#(x..);", re.IGNORECASE).sub(self.expandHexEntities, text)
		text = re.compile("&#xA3;", re.IGNORECASE).sub("£", text)
		return text

	#def expandHexEntities(self, matchobj):
	#	htmlentitydefs.entitydefs[]
	#	s = u'\%s' % matchobj.group(1)
	#	#s = "Y"
	#	return s

	def getBNCTuples(self, text):
		"""Return a list of (tag, word) tuples from text if
		text is a BNC Sampler text in XML format. Otherwise
		return an empty list. The tags are mapped from the C7 tag set
		to the much smaller C5 tag set."""
		l = []
		pos = 0
		while 1:
			m = self.bnc_regex.search(text, pos)
			if not m:	
				break
			tag = m.group(1)
			if self.mapping.has_key(tag):
				tag = self.mapping[tag]
			else:
				#print "no mapping: %s" % tag
				pass
			l.append((tag, m.group(2).strip()))
			pos = m.start()+1
		return l
		
	def normalise(self, text):
		"""Take a string and remove XML markup and whitespace at the beginning 
		and the end. Return the modified string."""
		# sometimes there's <PB...>...</PB> *inside* <W...>...</W>!
		text = re.compile("<.*?>", re.DOTALL|re.IGNORECASE).sub("", text)
		text = text.strip()
		return text

	def splitBNCTag(self, tag):
		"""Take a string with BNC tags like 'NN1-NP0' and return a list,
		e.g. ['NN1', 'NP0']. For single tags like 'NN0' this will
		be returned: ['NN0']."""
		tags = re.split("-", tag)
		return tags

	def guessTag(self, word):
		"""Take a word and guess which POS tag it might	have and return
		that POS tag. This considers e.g. word prefixes, suffixes and 
		capitalization. If no guess can be made, None is returned."""

		# numbers:
		if self.number_regex.match(word):
			return 'CRD'
			
		# e.g. HIV
		if len(word) >= 2 and word == word.upper():
			return 'NN0'

		# this >=3 limit also prevents to assign 'A' (i.e. determiner
		# at sentence start) NP0, of course that's only relevant
		# for the test cases:
		if len(word) >= 3 and word[0] in string.uppercase:	# e.g. "Jefferson"
			return 'NP0'

		# e.g. freedom, contentment, celebration, assistance, fighter,
		# violinist, capacity
		noun = ['dom', 'ment', 'tion', 'sion', 'ance', 'ence', 'er', 'or', 
			'ist', 'ness', 'icity']
		for suffix in noun:
			if word.endswith(suffix):
				return 'NN1'

		# e.g. quickly
		if word.endswith("ly"):
			return 'AV0'

		# e.g. 8.55am
		if self.time_regex.search(word):
			return 'AV0'

		# e.g. extensive, heroic, financial, portable, hairy
		# mysterious, hopeful, powerless
		# 'en' was left out, could also be a verb
		adj = ['ive', 'ic', 'al', 'able', 'y', 'ous', 'ful', 'less']
		for suffix in adj:
			if word.endswith(suffix):
				return 'AJ0'

		# e.g. publicize, publicise, activate, simplify
		# 'en' was left out, could also be a adjective
		verb = ['ize', 'ise', 'ate', 'ify', 'fy']
		for suffix in verb:
			if word.endswith(suffix):
				# fixme: could also be VVB
				return 'VVI'

		return None
	
	def tagWord(self, word, data_table):
		"""Find all possible tags for a word and return a list of tuples:
		[(orig_word, normalised_word, [(tag, probability])]"""
		orig_word = word
		word = self.normalise(word)
		#print "#%s<br>" % word
		#word = re.compile("[^\w' ]", re.IGNORECASE).sub("", word)
		if word and self.nonword_punct.match(word):
			# punctuation
			return [(orig_word, orig_word, [])]
		if (not word) or self.nonword.match(word):
			# word is just white space
			return [(orig_word, None, [])]
		# sanity check:
		if word.count("'") > 1:
			print >> sys.stderr, "*** What's this, more than one apostroph: '%s'?" % word

		# Special cases: BNC tags "wasn't" like this: "<w VBD>was<w XX0>n't"
		# Call yourself, but don't indefinitely recurse.
		special_cases = ("n't", "'s", "'re", "'ll", "'ve")
		for special_case in special_cases:
			special_case_pos = word.find(special_case)
			if special_case_pos != -1 and special_case_pos != 0:
				first_part = self.tagWord(word[0:special_case_pos], data_table)[0]
				second_part = self.tagWord(special_case, data_table)[0]
				tag_results = []
				#FIXME: return prob?:
				#print second_part
				tag_results.append((word[0:special_case_pos], first_part[1], first_part[2]))
				tag_results.append((special_case, second_part[1], second_part[2]))
				return tag_results

		# TODO?: ignore upper/lower case?, no -- seems to decrease precision
		#word = word.lower()
		if not data_table.has_key(word):
			# word is unknown
			self.count_unknown = self.count_unknown + 1
			guess_tag = self.guessTag(word)
			if guess_tag:
				return [(orig_word, word, [(guess_tag, 1)])]
			else:
				return [(orig_word, word, [("unknown", 1)])]
		else:
			pos_table = data_table[word].table
			if len(pos_table) == 1:
				# word is unambiguous
				self.count_unambiguous = self.count_unambiguous + 1
				return [(orig_word, word, [(pos_table.keys()[0], 1)])]
			else:
				# word is ambiguous
				tag_tuples = []
				for pos_tag in pos_table.keys():
					#print "pos_tag=%s -> %.2f" % (pos_tag, pos_table[pos_tag])
					tag_tuples.append((pos_tag, pos_table[pos_tag]))
				self.count_ambiguous = self.count_ambiguous + 1
				return [(orig_word, word, tag_tuples)]

	def addToData(self, tagged_words, data_table, seqs_table):
		"""Count words and POS tags so they can later be added
		to the persistent storage."""
		tag_list = self.addWords(tagged_words, data_table)
		# Normalize data_table values so they are probabilities (0 to 1):
		for e in data_table.keys():
			t = data_table[e].table
			occ_all = 0
			for occ in t.values():
				occ_all = occ_all + occ
			for key in t.keys():
				t[key] = t[key] / occ_all
		self.addTagList(tag_list, seqs_table)
		# Normalize seqs_table values so they are probabilities (0 to 1):
		# We normalize so that we don't divide by the number of all
		# tag-triples, but only ny the count of sequences (X, seqs_table[key], Y)
		keys = {}
		tags_done = {}	#don't count a tag more than once
		for tag in tag_list:
			tags = self.splitBNCTag(tag)
			for tag in tags:
				if tags_done.has_key(tag):
					continue
				for key in seqs_table.keys():
					if tag == key[1]:
						#print "%s<->%s (%.2f)" % (tag, key, seqs_table[key])
						try:
							keys[tag] = keys[tag] + seqs_table[key]
						except KeyError:
							keys[tag] = seqs_table[key]
						tags_done[tag] = 1	# 1 = fake value

		# TODO: do these numbers become too small, as the Qtag paper states?		
		for key in seqs_table.keys():
			occ = 1
			try:
				middle_tag = key[1]
				occ = keys[middle_tag]
			except KeyError:
				if middle_tag:
					print >> sys.stderr, "Warning: number of occurences of '%s' not found" % middle_tag
			seqs_table[key] = float(seqs_table[key]) / occ
			#print "## seqs_table[%s] = %s / %s -> %s" % (key, float(seqs_table[key]), occ, seqs_table[key])
		return
	
	def addWords(self, tagged_words, data_table):
		"""For each word, save the tag frequency to data_table so
		it can later be added to the persistent storage. Return
		a list of all tags."""
		all_tags_list = []
		for (word, tag) in tagged_words:
			#onlex for testing if case-insensitivity is better:
			#word = word.lower()
			all_tags_list.append(tag)
			tag_list = self.splitBNCTag(tag)
			assert(len(tag_list) == 1 or len(tag_list) == 2)
			#print "word/pos_list: %s/%s" % (word, tag_list)
			if data_table.has_key(word):
				# word is already known
				word_table = data_table[word].table
				for tag in tag_list:
					if word_table.has_key(tag):
						word_table[tag] = word_table[tag] + 1.0/len(tag_list)
						#print "word_table[%s] += %f" % (tag, 1.0/len(tag_list))
					else:
						word_table[tag] = 1.0/len(tag_list)
						#print "word_table[%s] = %f" % (tag, word_table[tag])
			else:
				word_table = {}
				for tag in tag_list:
					word_table[tag] = 1.0/len(tag_list)
					#print "word_table[%s] = %f" % (tag, word_table[tag])
				data_table[word] = WordData(word, word_table)
		return all_tags_list
		
	def addTagList(self, tag_list, seqs_table):
		"""Save information about POS tag triplets to seqs_table."""
		#TODO?: add two dummy entries *for each sentence*?!?! -> no
		i = 0
		if len(tag_list) == 0:
			return
		while 1:
			#print "%d, %d" % (i, len(tag_list))
			if i >= len(tag_list) + 2:
				break
			# Special cases to emulate dummy entries. We don't want
			# to modify the list, and working on a copy might be slow,
			# so handle 'start of list' and 'end of list' as special cases:
			tag0 = None
			tag1 = None
			tag2 = None
			if i == 0:
				tag0 = [self.DUMMY]
				tag1 = [self.DUMMY]
				tag2 = self.splitBNCTag(tag_list[i])
			elif i == 1:
				tag0 = [self.DUMMY]
				tag1 = self.splitBNCTag(tag_list[i-1])
				try:
					tag2 = self.splitBNCTag(tag_list[i])
				except IndexError:	
					tag2 = [self.DUMMY]
			elif i == len(tag_list):
				tag0 = self.splitBNCTag(tag_list[i-2])
				tag1 = self.splitBNCTag(tag_list[i-1])
				tag2 = [self.DUMMY]
			elif i == len(tag_list) + 1:
				tag0 = self.splitBNCTag(tag_list[i-2])
				tag1 = [self.DUMMY]
				tag2 = [self.DUMMY]
			else:
				tag0 = self.splitBNCTag(tag_list[i-2])
				tag1 = self.splitBNCTag(tag_list[i-1])
				tag2 = self.splitBNCTag(tag_list[i])
			for t0 in tag0:
				for t1 in tag1:
					for t2 in tag2:
						final_key = (t0, t1, t2)
						val = 1.0 / (len(tag0) * len(tag1) * len(tag2))
						#print "fK=%s -> %.2f " % (str(final_key), val)
						if seqs_table.has_key(final_key):
							seqs_table[final_key] = seqs_table[final_key] + val
						else:
							seqs_table[final_key] = val
			i = i + 1
		#debug:
		#for k in seqs_table.keys():
		#	print "%s -> %s" % (k, seqs_table[k])
		return


class TextToTag(Text):
	"""Any text (also pre-tagged texts from the BNC -- for 
	testing the tagger)."""

	DUMMY = None
	
	def __init__(self):
		self.text = None
		Text.__init__(self)
		return

	def setText(self, text):
		self.text = text
		return

	def setFilename(self, filename):
		f = open(filename)
		self.text = f.read()
		f.close()
		return
	
	def getPrevToken(self, i, tagged_list):
		"""Find the token previous to the token at position i from tagged_list,
		ignoring whitespace tokens. Return a tuple (word, tuple_list),
		whereas tuple_list is a list of (tag, tag_probability) tuples."""
		j = i-1
		while j >= 0:
			(orig_word_tmp, tagged_word_tmp, tag_tuples_tmp) = self.getTuple(tagged_list[j])
			j = j - 1
			if not tagged_word_tmp:
				continue
			else:
				prev = tag_tuples_tmp
				return (orig_word_tmp, prev)
		return (None, None)

	def getNextToken(self, i, tagged_list):
		"""Find the token next to the token at position i from tagged_list,
		ignoring whitespace tokens. See self.getPrevToken()"""
		j = i + 1
		while j < len(tagged_list):
			(orig_word_tmp, tagged_word_tmp, tag_tuples_tmp) = self.getTuple(tagged_list[j])
			j = j + 1
			if not tagged_word_tmp:
				continue
			else:
				next = tag_tuples_tmp
				return (orig_word_tmp, next)
		return (None, None)

	def getBestTag(self, prev, tag_tuples, next, seqs_table, i, tag_table):
		"""Check the probability of all 3-tag sequences and choose that with
		the highest combined probability."""
		max_prob = 0
		max_prob_no_context = 0		# special case, mostly for test cases
		best_tag = None
		for tag_tuples_prev in prev:
			for tag_tuples_here in tag_tuples:
				#print tag_tuples_here
				for tag_tuples_next in next:
					seq = (tag_tuples_prev[0], tag_tuples_here[0], tag_tuples_next[0])
					seq_prob = 0	# sequence probability
					try:
						seq_prob = seqs_table[seq]
					except KeyError:
						pass
					prob_combined = seq_prob * tag_tuples_here[1]
					k = (i, tag_tuples_here[0])
					try:
						tag_table[k] = tag_table[k] + prob_combined
					except KeyError:
						tag_table[k] = prob_combined
					# also work if all contexts have probability == 0,
					# use the pos tag probability without context then:
					if tag_tuples_here[1] >= max_prob_no_context:
						max_prob_no_context = tag_tuples_here[1]
						best_tag_no_context = tag_tuples_here[0]
					if prob_combined >= max_prob:
						max_prob = prob_combined
						best_tag = tag_tuples_here[0]
					#print "##seq=%s, %.7f*%.2f=%f" % (str(seq), seq_prob, tag_tuples_here[1], prob_combined)
		if max_prob == 0:
			best_tag = best_tag_no_context
		return best_tag

	def checkBNCMatch(self, i, tagged_list_bnc, word, best_tag):
		"""Check for mismatches, i.e. POS tags that differ from the original
		tag in BNC. Print out a warning for all those differences and return
		1, otherwise return 0. Note that the BNC's tags are only correct 
		in 97-98%. If the original tag is 'UNC' and this tagger's tag is
		not 'unknown', this is still considered a mismatch."""
		if i >= len(tagged_list_bnc)-1:
			print >> sys.stderr, "Index out of range..."
			return 0
		if not tagged_list_bnc[i]:
			return 0
		word_from_bnc, tags_from_bnc = tagged_list_bnc[i]
		#print "%s ?= %s" % (word_from_bnc, word)
		if best_tag == 'unknown':
			# 'UNC' means unclassified in BNC, assume that this corresponds
			# to out 'unknown':
			best_tag = 'UNC'
		if not word == word_from_bnc:
			print >> sys.stderr, "*** word mismatch: '%s'/'%s'" % (word, word_from_bnc)
			#sys.exit()
		elif not best_tag in tags_from_bnc:
			print >> sys.stderr, "*** tag mismatch: got %s/%s, expected %s/%s" % \
				(word, best_tag, word_from_bnc, tags_from_bnc)
			return 1
		return 0

	def getStats(self, count_wrong_tags):
		"""Get some human-readable statistics about tagging success,
		e.g. number and percentage of correctly tagged tokens."""
		res = "<!-- Statistics:\n"
		sum = self.count_unknown + self.count_unambiguous + self.count_ambiguous
		if sum > 0:
			res = res + "count_unknown = %d (%.2f%%)\n" % (self.count_unknown, float(self.count_unknown)/float(sum)*100)
			res = res + "count_unambiguous = %d (%.2f%%)\n" % (self.count_unambiguous, float(self.count_unambiguous)/float(sum)*100)
			res = res + "count_ambiguous = %d (%.2f%%)\n" % (self.count_ambiguous, float(self.count_ambiguous)/float(sum)*100)
			#res = res + "sum = %d\n" % sum
			if not count_wrong_tags == "n/a":
				res = res + "correct tags = %d (%.2f%%)\n" % (sum-count_wrong_tags, float(sum-count_wrong_tags)/float(sum)*100)
				res = res + "count_wrong_tags = %d (%.2f%%)\n" % (count_wrong_tags, float(count_wrong_tags)/float(sum)*100)
		res = res + "-->"
		return res

	def applyConstraints(self, prev_word, curr_word, next_word, tagged_tuples):
		"""Some hard-coded and manually written rules that prevent mistaggings by 
		the probabilistic tagger. Removes incorrect POS tags from tagged_tuples.
		Returns nothing, as it works directly on tagged_tuples."""
		# demo rule just for the test cases:
		if curr_word and curr_word.lower() == 'demodemo':
			self.constrain(tagged_tuples, 'AA')
		# ...
		return

	def constrain(self, tagged_tuples, pos_tag):
		"""Remove the pos_tag reading from tagged_tuples. Returns nothing,
		works directly on tagged_tuples."""
		i = 0
		for t in tagged_tuples:
			if t[0] == pos_tag:
				del tagged_tuples[i]
			i = i + 1
		return
	
	def applyTagRules(self, curr_word, tagged_word, curr_tag):
		"""Some hard-coded and manually written rules that extent the
		tagging. Returns a (word, normalized_word, tag) triple."""
		# ...
		return None

	def tag(self, data_table, seqs_table):
		"""Tag self.text and return list of tuples
		(word, normalized word, most probable tag)"""
		self.text = self.expandEntities(self.text)
		result_tuple_list = []
		count_wrong_tags = "n/a"
		is_bnc = 0
		word_matches = self.getBNCTuples(self.text)
		if len(word_matches) > 0:
			# seems like this is a BNC text used for testing
			is_bnc = 1
			print >> sys.stderr, "BNC text detected."
			count_wrong_tags = 0
		else:
			word_matches = self.nonword.split(self.text)
			
		# Put sentence end periods etc into an extra element.
		# We cannot just split on periods etc. because that would
		# break inner-sentence tokens like "... No. 5 ...":
		# fixme: only work on the last element (not counting white space)
		# FIXME: doesn't work here: "I cannot , she said."
		if not is_bnc:
			j = len(word_matches)-1
			while j >= 0:
				w = word_matches[j]
				s_end_match = self.sentence_end.search(w)
				if s_end_match:
					word_matches[j] = w[:len(w)-len(s_end_match.group(1))]
					word_matches.insert(j+1, s_end_match.group(1))
					break
				j = j - 1
			
		#print "word_matches=%s" % word_matches
		i = 0
		tagged_list = [self.DUMMY, self.DUMMY]
		tagged_list_bnc = [self.DUMMY, self.DUMMY]

		while i < len(word_matches):
			next_token = None
			tags = None
			if is_bnc:
				# word_matches[i] is a (tag,word) tuple
				(tag, word) = word_matches[i]
				if i+1 < len(word_matches):
					(next_token, foo) = word_matches[i+1]
				word = self.normalise(word)
				tags = self.splitBNCTag(tag)
			else:
				word = word_matches[i]
				if i+1 < len(word_matches):
					next_token = word_matches[i+1]
			if i + 2 < len(word_matches):
				# BNC special case: "of course" and some others are tagged as one word!
				tuple_word = "%s %s" % (word, word_matches[i+2])		# +2 = jump over whitespace
				if data_table.has_key(tuple_word):
					#print >> sys.stderr, "*** SPECIAL CASE %d '%s' ..." % (i, tuple_word)
					word = tuple_word
					i = i + 2
			r = Text.tagWord(self, word, data_table)
			tagged_list.extend(r)

			if is_bnc:
				for el in r:
					# happens e.g. with this (wrong?) markup in BNC:
					#<W TYPE="CRD" TEIFORM="w">4's</W>
					# My tagger tags <4> and <'s>, so there's an offset
					# which makes futher comparisons BNC <-> tagger impossible,
					# so use this pseudo-workaround and just re-use the tags
					# for the <'s>, too:
					#print "%s -> %s" % (el[0], tags)
					tagged_list_bnc.append((el[0], tags))
			i = i + 1
		#print tagged_list[:15]
		#print tagged_list_bnc[:15]
		#return
		
		tagged_list.append(self.DUMMY)
		tagged_list.append(self.DUMMY)
		i = 0

		tag_table = {}
		for tagged in tagged_list:
			### ********************************
			### ***** QTag-like algorithm: *****
			### ********************************
			# The algorithm works on a window of 3 tokens.
			# Always look at the middle tag, so find the 
			# surrounding tokens (ignoring whitespace).
			# Always look at the tag sequence of the
			# whole 3-tag window.
			if i == 0:			# jump over first dummy entry
				i = i + 1
				continue
			next = [(None, 1)]
			(orig_word, tagged_word, tag_tuples) = self.getTuple(tagged)
			if not tagged_word and not tagged == None:	# tagges == None -> dummy entry
				# ignore white space
				i = i + 1
				result_tuple_list.append((orig_word, tagged_word, None))
				continue
			if not tag_tuples:
				tag_tuples = [(None, 1)]
			#print "tagged_word=%s, tag_tuples=%s" % (tagged_word, str(tag_tuples))
			prev_word, prev = self.getPrevToken(i, tagged_list)
			if not prev:
				prev = [(None, 1)]
			next_word, next = self.getNextToken(i, tagged_list)
			if not next:
				next = [(None, 1)]

			### Constraint-based part:
			self.applyConstraints(prev_word, orig_word, next_word, tag_tuples)
			
			#print "##'%s', tag_tuples=%s" % (orig_word, tag_tuples)
			best_tag = self.getBestTag(prev, tag_tuples, next, seqs_table, i, tag_table)

			#print "%s\nTT=%s" % (tag_tuples, tag_table)
			#print
			if tagged:
				#print "BEST: %s -> %s" % (tagged_word, best_tag)
				result_tuple_list.append((orig_word, tagged_word, best_tag))
			if is_bnc:
				wrong_tags = self.checkBNCMatch(i, tagged_list_bnc, orig_word, best_tag)
				count_wrong_tags = count_wrong_tags + wrong_tags
			i = i + 1

		i = 0
		for tag_triple in result_tuple_list:
			triple = self.applyTagRules(tag_triple[0], tag_triple[1], tag_triple[2])
			if triple:
				result_tuple_list[i] = triple
			if self.sentence_end.search(tag_triple[0]):
				# make sure punctuation doesn't have tags:
				result_tuple_list[i] = (tag_triple[0], None, None)
			i = i + 1
		
		#debug:
		#for tag_triple in result_tuple_list:
		#	print tag_triple

		#stat = self.getStats(count_wrong_tags)
		#print >> sys.stderr, stat
		return result_tuple_list

	def getTuple(self, tagged_list_elem):
		if not tagged_list_elem:
			orig_word = None
			tagged_word = None
			tag_tuples = None
		else:
			(orig_word, tagged_word, tag_tuples) = tagged_list_elem
		return (orig_word, tagged_word, tag_tuples)
	
	def toXML(self, tagged_words):
		"Show result as XML."
		xml_list = []
		for (orig_word, word, tag) in tagged_words:
			# fast appending:
			if not word and not tag:
				xml_list.append(' <w>%s</w>\n' % orig_word)
			else:
				xml_list.append(' <w term="%s" type="%s">%s</w>\n' % (word, tag, orig_word))
		xml = "<taggedWords>\n" + string.join(xml_list, "") + "</taggedWords>\n"
		return xml
	
	
class PreTaggedText(Text):
	"Text from the BNC Sampler in XML format."
	
	def __init__(self, filename):
		self.content = None
		Text.__init__(self)
		f = open(filename)
		self.content = f.read()
		f.close()
		return

	def getTaggedWords(self):
		"Returns list of tuples (word, tag)"
		text = self.expandEntities(self.content)
		word_matches = self.getBNCTuples(text)
		tagged_words = []
		for (tag, word) in word_matches:
			tagged_words.append((word, tag))
		return tagged_words


class WordData:
	"A term and the frequency of its tags."

	def __init__(self, word, table):
		self.word = word
		# table = tag / number of occurences
		# deep copy the hash table (TODO: use deep copy functions):
		self.table = {}
		for el in table:
			self.table[el] = table[el]
		return

	def __str__(self):
		"Show word data (debugging only!)"
		string = self.word + ":\n"
		for el in self.table:
			string = string + "\t" + el + ": " + str(self.table[el]) + "\n"
		return string
