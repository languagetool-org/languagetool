# A to-be-probabilistc part-of-speech tagger (see the QTag paper), 2002-06-15
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

#from mx.BeeBase.BeeDict import BeeDict

import os
import re
import string
import sys
import cPickle		# see tag.py for speed comparison

class Tagger:
	"POS-Tag any text. The result in XML can be used to re-build the original "
	"text by concatenating all contents of the <w> tags. Whitespace and non-word "
	"characters have term=None and type=None, i.e. they are inside their own <w> "
	"elements. Words that could not be tagged have type=unknown. A heuristics "
	"tags sentence ends with type=SENT_END (fixme, out of date!)."

	db_name = "data/words_bee"
	data_table = None
	
	def __init__(self):
		return

	def bindData(self):
		### pickle:
		try:
			self.data_table = cPickle.load(open(self.db_name))
		except IOError:
			print >> sys.stderr, "No date file '%s' yet, starting with empty table." % self.db_name
			self.data_table = {}
		### beedict:
		#self.data_table = BeeDict(self.db_name, min_recordsize=0, readonly=0, \
		#	recover=0, autocommit=1, validate=0)
		return

	def commitData(self):
		print >> sys.stderr, "Known words = %d" % len(self.data_table.keys())
		print >> sys.stderr, "Commiting results..."
		### pickle:
		cPickle.dump(self.data_table, open(self.db_name, 'w'), 1)
		### beedict:
		#self.data_table.commit()
		#print >> sys.stderr, "Closing results..."
		#self.data_table.close()
		return
	
	def deleteData(self):
		### beedict:
		#print >> sys.stderr, "Deleting old data files..."
		#os.remove(self.db_name + ".dat")
		#os.remove(self.db_name + ".idx")
		return

	def buildData(self, filename):
		print >> sys.stderr, "Loading %s..." % filename
		text = PreTaggedText(filename)
		tagged_words = text.getTaggedWords()
		text.addToData(tagged_words, self.data_table)

	def tagFile(self, filename):
		"Tag the contents of a file and return XML."
		text = TextToTag()
		text.setFilename(filename)
		tagged_words = text.tag(self.data_table)
		#print tagged_words
		xml = text.toXML(tagged_words)
		return xml

	def tagText(self, strng):
		"Tag a string and return tagged_words list of tuples."
		text = TextToTag()
		text.setText(strng)
		tagged_words = text.tag(self.data_table)
		return tagged_words


class Text:

	## fixme:  move to __init__!?
	bnc_word_regexp = None
	count_unambiguous = 0
	count_ambiguous = 0
	count_unknown = 0
	
	nonword = re.compile("([\s,.]+)")

	def __init__(self):
		self.bnc_word_regexp = re.compile("<W\s+TYPE=\"(.*?)\".*?>(.*?)</W>", \
			re.DOTALL|re.IGNORECASE)
		return
		
	def expandEntities(self, text):
		text = re.compile("&amp;", re.IGNORECASE).sub("&", text)
		return text

	def normalise(self, text):
		# sometime there's <PB...>...</PB> *inside* <W...>...</W>!
		text = re.compile("<.*?>", re.DOTALL|re.IGNORECASE).sub("", text)
		text = text.strip()
		return text

	def splitBNCTag(self, tag):
		"Turn a string with BNC tags like 'NN1-NP0' into a list, i.e. ['NN1', 'NP0']."
		"Single tags like 'NN0' become ['NN0']."
		tags = re.split("-", tag)
		return tags

	def tagWord(self, word, data_table):
		"Return a tuple (normalised_word, tag)."
		orig_word = word
		word = self.normalise(word)
		word = re.compile("[^\w' ]", re.IGNORECASE).sub("", word)
		short_form_pos = word.find("n't")
		if (not word) or self.nonword.match(word):
			# word is just white space
			return [(orig_word, None, None)]
		# sanity check:
		if word.count("'") > 1:
			print >> sys.stderr, "*** What's this, more than one apostroph: '%s'!?" % word
		if short_form_pos != -1 and short_form_pos != 0:
			# sanity check:
			if not word.endswith("n't"):
				print >> sys.stderr, "*** What's this, negation not at the end: '%s'!?" % word
			# Special case: BNC tags "wasn't" like this: "<w VBD>was<w XX0>n't"
			# Call yourself, but don't indefinitely recurse.
			# Also ignore cases where the apostroph occurs twice in a word
			first_part = self.tagWord(word[0:short_form_pos], data_table)[0]
			second_part = self.tagWord("n't", data_table)[0]
			tag_results = []
			tag_results.append((word[0:short_form_pos], first_part[1], first_part[2]))
			tag_results.append(("n't", second_part[1], second_part[2]))
			return tag_results
		possesive_pos = word.find("'s")
		if possesive_pos != -1 and possesive_pos != 0:
			# sanity check:
			if not word.endswith("'s"):
				print >> sys.stderr, "*** What's this, possesive not at the end: '%s'!?" % word
			first_part = self.tagWord(word[0:possesive_pos], data_table)[0]
			second_part = self.tagWord("'s", data_table)[0]
			tag_results = []
			tag_results.append((word[0:possesive_pos], first_part[1], first_part[2]))
			tag_results.append(("'s", second_part[1], second_part[2]))
			return tag_results
		#### fixme: ignore upper/lower case?!?!?!
		if not data_table.has_key(word):
			# word is unknown
			self.count_unknown = self.count_unknown + 1
			return [(orig_word, word, "unknown")]
		else:
			pos_table = data_table[word].table
			if len(pos_table) == 1:
				# word is unambiguous
				self.count_unambiguous = self.count_unambiguous + 1
				return [(orig_word, word, pos_table.keys()[0])]
			else:
				# word is ambiguous
				# simple algorithm: select to most probable POS tag:
				max_occurences = 0
				max_tag = "error"
				for pos_tag in pos_table.keys():
					if pos_table[pos_tag] > max_occurences:
						max_occurences = pos_table[pos_tag]
						max_tag = pos_tag
				# TODO: *** add "better" algorithm here ***
				# 	i.e. check two preceding tags and the two following tags (->QTag)
				self.count_ambiguous = self.count_ambiguous + 1
				return [(orig_word, word, max_tag)]


class TextToTag(Text):
	"Any text (also pre tagged texts from the BNC - for testing the tagger)."

	text = None
	
	def __init__(self):
		Text.__init__(self)

	def setText(self, text):
		self.text = text
		return

	def setFilename(self, filename):
		f = open(filename)
		self.text = f.read()
		f.close()
		return
	
	def tag(self, data_table):
		"Returns list of tuples (word, tag)"
		self.text = self.expandEntities(self.text)
		word_matches = self.bnc_word_regexp.findall(self.text)
		result_tuple_list = []
		count_wrong_tags = "n/a"
		is_bnc = 0
		if len(word_matches) > 0:
			# seems like this is a BNC text used for testing
			is_bnc = 1
			print >> sys.stderr, "BNC text detected."
			count_wrong_tags = 0
		else:
			word_matches = self.nonword.split(self.text)
		i = 0
		#for tmp_word in word_matches:
		while i < len(word_matches):
			next_token = None
			if is_bnc:
				# tmp_word is a tuple
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
			tagged_list = Text.tagWord(self, word, data_table)
			i = i + 1
			for tagged in tagged_list:
				(orig_word, tagged_word, tagged_tag) = tagged
				result_tuple_list.append(tagged)
				### check for mismatches:
				if is_bnc:
					if not orig_word == tagged_word:
						print >> sys.stderr, "*** word mismatch: '%s'/'%s'" % (word, orig_word)
						continue
					if (not tagged_tag in tags) or (not word == orig_word):
						count_wrong_tags = count_wrong_tags + 1
						print >> sys.stderr, "*** tag mismatch: got %s/%s, expected %s/%s" % \
							(tagged_word, tagged_tag, word, tag)
			### TODO: add sentence border tags:
			# fixme: add all special cases:
			# no sentence end:
			# 3.14
			# U.S.A.
			# valid sentence ends:
			# end[?!]
			# end...
			next_char_end = 0		# whitespace or end follows
			if i+1 >= len(word_matches):
				# not defined -> text ends here
				next_char_end = 1
			elif re.compile("\s+").match(next_token):
				# whitespace follows -> this character ends a sentence
				next_char_end = 1
			if word.strip() in ('.', '!', '?') and next_char_end:
				result_tuple_list.append(('', '', 'SENT_END'))
			###/
		### Some statistics:
		res = "--- STATISTICS: ---\n"
		sum = self.count_unknown + self.count_unambiguous + self.count_ambiguous
		if sum > 0:
			res = res + "count_unknown = %d (%.2f%%)\n" % (self.count_unknown, float(self.count_unknown)/float(sum)*100)
			res = res + "count_unambiguous = %d (%.2f%%)\n" % (self.count_unambiguous, float(self.count_unambiguous)/float(sum)*100)
			res = res + "count_ambiguous = %d (%.2f%%)\n" % (self.count_ambiguous, float(self.count_ambiguous)/float(sum)*100)
			res = res + "sum = %d\n" % sum
			if not count_wrong_tags == "n/a":
				res = res + "correct tags = %d (%.2f%%)\n" % (sum-count_wrong_tags, float(sum-count_wrong_tags)/float(sum)*100)
				res = res + "count_wrong_tags = %d (%.2f%%)\n" % (count_wrong_tags, float(count_wrong_tags)/float(sum)*100)
		#print >> sys.stderr, res
		#print >> sys.stderr, "**"+str(result_tuple_list)
		return result_tuple_list

	def toXML(self, tagged_words):
		"Show result as XML."
		xml_list = []
		for (orig_word, word, tag) in tagged_words:
			# fast appending:
			xml_list.append(' <w term="%s" type="%s">%s</w>\n' % (word, tag, orig_word))
		xml = "<taggedWords>\n" + string.join(xml_list, "") + "</taggedWords>\n"
		return xml
	
	
class PreTaggedText(Text):
	"Text from the BNC in XML format."
	
	content = None
	
	def __init__(self, filename):
		Text.__init__(self)
		f = open(filename)
		self.content = f.read()
		f.close()
		return

	def getTaggedWords(self):
		"Returns list of tuples (word, tag)"
		text = self.expandEntities(self.content)
		word_matches = self.bnc_word_regexp.findall(text)
		tagged_words = []
		for (tag, word) in word_matches:
			word = word.strip()
			tagged_words.append((word, tag))
		return tagged_words

	def addToData(self, tagged_words, data_table):
		"Add data about words and their tags to the persistent storage."
		"TODO: Also save information about POS tag triplets."
		tag_list = self.addWords(tagged_words, data_table)
		self.addTagList(tag_list, data_table)
		return
	
	def addWords(self, tagged_words, data_table):
		all_tags_list = []
		for (word, tag) in tagged_words:
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
			all_tags_list.append(tag)
		return all_tags_list
		
	def addTagList(self, tag_list, data_table):
		# todo
		return


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
