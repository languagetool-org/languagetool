# Class for Grammar and Style Rules
# (c) 2002,2003 Daniel Naber <daniel.naber@t-online.de>
#$rcs = ' $Id: Rules.py,v 1.12 2003-07-06 21:17:38 dnaber Exp $ ' ;
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

import Tools

import copy
import os
import re
import string
import sys
import xml.dom.minidom

class Rules:
	"""All known style and grammar error rules (from XML and the built-in ones)."""

	rule_files = [os.path.join("rules", "grammar.xml"),
						os.path.join("rules", "words.xml"),
						os.path.join("rules", "false_friends.xml")]
	
	def __init__(self, max_sentence_length, grammar_rules, word_rules, \
		false_friend_rules, textlang, mothertongue):
		"""Parse all rules and put them in the self.rules list, together
		with built-in rules like the SentenceLengthRule."""
		self.rules = []

		# built-in rules:
		length_rule = SentenceLengthRule()
		if max_sentence_length != None:
			length_rule.setMaxLength(max_sentence_length)
		self.rules.append(length_rule)
		a_an_rule = AvsAnRule()
		self.rules.append(a_an_rule)

		for filename in self.rule_files:
			# minidom expects the DTD in the current directory, not in the
			# documents directory, so we have to chdir to 'rules':
			dir_temp = os.getcwd()
			os.chdir(os.path.dirname(filename))
			doc = xml.dom.minidom.parse(os.path.basename(filename))
			os.chdir(dir_temp)
			if filename.endswith("grammar.xml"):
				rule_nodes = doc.getElementsByTagName("rule")
				for rule_node in rule_nodes:
					rule = PatternRule(rule_node)
					lang_ok = 0
					if textlang == None or textlang == rule.language:
						lang_ok = 1
					if lang_ok and (grammar_rules == None or rule.rule_id in grammar_rules):
						self.rules.append(rule)
			elif filename.endswith("words.xml"):
				rule_nodes = doc.getElementsByTagName("rule")
				for rule_node in rule_nodes:
					rule = PatternRule(rule_node)
					lang_ok = 0
					if textlang == None or textlang == rule.language:
						lang_ok = 1
					if lang_ok and (word_rules == None or rule.rule_id in word_rules):
						self.rules.append(rule)
			elif filename.endswith("false_friends.xml"):
				pattern_nodes = doc.getElementsByTagName("pattern")
				for pattern_node in pattern_nodes:
					lang = pattern_node.getAttribute("lang")
					if textlang == None or lang == textlang:
						rule = PatternRule(pattern_node.parentNode, 1, mothertongue, textlang)
						if rule.valid and (false_friend_rules == None or \
							rule.rule_id in false_friend_rules):
							self.rules.append(rule)
		return

class Rule:
	"""Style or grammar rule -- quasi virtual class."""
	
	def __init__(self, rule_id, message, false_positives, language):
		self.rule_id = rule_id
		self.message = message
		self.false_positives = false_positives	# percent of sentences that are wrongly tagged as wrong
		self.language = language	# two letter code like "en" or None (= relevant for alle languages)
		return

	def match(self):
		"""Do nothing (quasi virtual method)."""
		return

class SentenceLengthRule(Rule):
	"""Check if a sentence is 'too long'."""

	max_length = 30
	
	def __init__(self):
		Rule.__init__(self, "SENTENCE_LENGTH", "This sentence is too long.", 0, None)
		return

	def setMaxLength(self, max_length):
		"""Set the maximum length that's still okay. Limit 0 means no limit."""
		self.max_length = int(max_length)
		return
		
	def match(self, tagged_words, position_fix=0):
		"""Check if a sentence is too long, according to the limit set
		by setMaxLength(). Put the warning on the first word
		above the limit. Assumes that tagged_words is exactly one sentence."""
		if self.max_length == 0:		# 0 = no limit
			return []
		matches = []
		text_length = 0
		count = 0
		too_long = 0
		too_long_start = 0
		too_long_end = 0
		for (org_word, tagged_word, tagged_tag) in tagged_words:
			text_length = text_length + len(org_word)
			if not tagged_tag or not tagged_word:
				# don't count whitespace etc
				continue
			count = count + 1
			if count > self.max_length and not too_long:
				too_long = 1
				too_long_start = text_length-len(org_word)
				too_long_end = text_length
		if too_long:
			matches.append(RuleMatch(self.rule_id, too_long_start,
				too_long_end, 
				"This sentence is %d words long, which exceeds the "
				"configured limit of %d words." % (count, self.max_length)))
		return matches


class AvsAnRule(Rule):
	"""Check if the determiner (if any) before a word is:
	-'an' if the next word starts with a vowel
	-'a' if the next word does not start with a vowel
	This rule knows about some exceptions (e.g. 'an hour')."""

	requires_a_file = os.path.join("data", "det_a.txt")
	requires_an_file = os.path.join("data", "det_an.txt")
	
	def __init__(self):
		Rule.__init__(self, "WHITESPACE", "Insert a space character before punctuation.", 0, None)
		self.requires_a = self.loadWords(self.requires_a_file) 
		self.requires_an = self.loadWords(self.requires_an_file)
		return

	def loadWords(self, filename):
		f = open(filename)
		contents = f.read()
		f.close()
		l = re.split("\n", contents)
		i = 0
		for el in l:
			if el.startswith("#") or el == '':
				del l[i]
			else:
				l[i] = l[i].strip().lower()
			i = i + 1
		return l
		
	def match(self, tagged_words, position_fix=0):
		matches = []
		text_length = 0
		i = 0
		while 1:
			if i >= len(tagged_words)-2:
				break
			org_word = tagged_words[i][0]
			org_word_next = tagged_words[i+2][0]	# jump over whitespace
			#print "<tt>'%s' -- '%s'</tt><br>" % (org_word, org_word_next)
			if org_word.lower() == 'a':
				err = 0
				if org_word_next in self.requires_an:
					err = 1
				elif org_word_next[0].lower() in ('a', 'e', 'i', 'o', 'u') and \
					not org_word_next in self.requires_a:
					err = 1
				if err:
					matches.append(RuleMatch(self.rule_id,
						text_length++position_fix, text_length+len(org_word)+position_fix, 
						"Use <em>an</em> instead of <em>a</em> if the following "+
						"word starts with a vowel sound, e.g. 'an article', "+
						"'an hour'", org_word))
			elif org_word.lower() == 'an':
				err = 0
				if org_word_next in self.requires_a:
					err = 1
				elif not org_word_next[0].lower() in ('a', 'e', 'i', 'o', 'u') and \
					not org_word_next in self.requires_an:
					err = 1
				if err:
					matches.append(RuleMatch(self.rule_id,
						text_length++position_fix, text_length+len(org_word)+position_fix, 
						"Use <em>a</em> instead of <em>an</em> if the following "+
						"word doesn't start with a vowel sound, e.g. 'a test', "+
						"'a university'", org_word))
				pass
			text_length = text_length + len(org_word)
			i = i + 1
		return matches

class WhitespaceRule(Rule):
	"""A rule that matches punctuation not followed by a whitespace
	and whitespace preceding punctuation. This rule does not work
	on sentence level, it works on complete tagged texts or paragraphs."""

	punct_regex = re.compile("^[.,?!:;]+$")
	whitespace_regex = re.compile("^\s+$")
	after_punct_regex = re.compile("^[\"]+$")
	
	def __init__(self):
		Rule.__init__(self, "WHITESPACE", "Insert a space character before punctuation.", 0, None)
		return

	def getNextTriple(self, tagged_words, pos):
		"""Get the next triple form the tagged_words list, starting at
		pos but ignoring all SENT_START and SENT_END tags."""
		tag = tagged_words[pos][2]
		while tag == 'SENT_START' or tag == 'SENT_END':
			pos = pos + 1
			if pos >= len(tagged_words):
				return None
			tag = tagged_words[pos][2]
		return tagged_words[pos]
		
	def match(self, tagged_words):
		"""Check if a sentence contains whitespace/token sequences
		that are against the 'use a space after, but not before, a token'
		rule."""
		matches = []
		text_length = 0
		i = 0
		while 1:
			if i >= len(tagged_words)-1:
				break
			org_word = tagged_words[i][0]
			org_word_next = self.getNextTriple(tagged_words, i+1)
			if org_word_next:
				org_word_next = org_word_next[0]
			#print "<tt>'%s' -- '%s'</tt><br>" % (org_word, org_word_next)
			if self.punct_regex.match(org_word):
				word_next = tagged_words[i+1][1]
				word_next = self.getNextTriple(tagged_words, i+1)
				if word_next:
					word_next = word_next[1]
				if word_next and (not self.after_punct_regex.match(org_word_next)) and \
					(not self.whitespace_regex.match(org_word_next)):
					matches.append(RuleMatch(self.rule_id, text_length, text_length + len(org_word), 
						"Usually a space character is inserted after punctuation."))
			elif self.whitespace_regex.match(org_word):
				if self.punct_regex.match(org_word_next):
					matches.append(RuleMatch(self.rule_id, text_length, text_length + len(org_word), 
						"Usually no space character is inserted before punctuation."))
			text_length = text_length + len(org_word)
			i = i + 1
		return matches

class PatternRule(Rule):
	"""A rule that can be formalised in the XML configuration file."""
	
	def __init__(self, node, is_false_friend_node=None, mothertongue=None, textlang=None):
		"""Build an object by parsing an XML rule node."""
		if node == None:
			# for the test cases. They use setVars().
			return
		if is_false_friend_node:
			self.parseFalseFriendsRuleNode(node, mothertongue, textlang)
		else:
			self.parseRuleNode(node)
		return

	def parseRuleNode(self, rule_node):
		self.rule_id = rule_node.getAttribute("id")
		self.pattern = rule_node.getElementsByTagName("pattern")[0].childNodes[0].data
		token_strings = re.split("\s+", self.pattern)
		self.tokens = []
		for token_string in token_strings:
			token = Token(token_string)
			self.tokens.append(token)
		pattern_node = rule_node.getElementsByTagName("pattern")[0]
		self.language = pattern_node.getAttribute("lang")
		marker_from_att = pattern_node.getAttribute("mark_from")
		if marker_from_att:
			self.marker_from = int(marker_from_att)
		else:
			self.marker_from = 0
		marker_to_att = pattern_node.getAttribute("mark_to")
		if marker_to_att:
			self.marker_to = int(marker_to_att)
		else:
			self.marker_to = 0
		self.case_sensitive = 0
		if rule_node.getElementsByTagName("pattern")[0].getAttribute("case_sensitive") == 'yes':
			#print "*** %s" % rule_node.getElementsByTagName("pattern")[0].getAttribute("case_sensitive")
			self.case_sensitive = 1
		if rule_node.getElementsByTagName("message"):
			self.message = Tools.Tools.getXML(rule_node.getElementsByTagName("message")[0])
		else:
			self.message = Tools.Tools.getXML(rule_node.parentNode.getElementsByTagName("message")[0])
		example_nodes = rule_node.getElementsByTagName("example")
		self.example_good = ""
		self.example_bad = ""
		for example_node in example_nodes:
			# TODO?: only one good and one bad example currently supported:
			if example_node.getAttribute("type") == 'correct':
				self.example_good = Tools.Tools.getXML(example_node.childNodes[0])
			else:
				self.example_bad = Tools.Tools.getXML(example_node.childNodes[0])
		if rule_node.getElementsByTagName("error_rate"):
			self.false_positives = rule_node.getElementsByTagName("error_rate")[0].childNodes[0].data
		else:
			self.false_positives = 0
		return

	def parseFalseFriendsRuleNode(self, rule_node, mothertongue, textlang):
		# This is only called for rule nodes that have a pattern
		# element with the relevant language. 
		self.rule_id = rule_node.parentNode.getAttribute("id")
		pattern_node = rule_node.getElementsByTagName("pattern")[0]
		self.language = rule_node.getAttribute("lang")
		# Now look for the correct translation:
		trans_nodes = rule_node.getElementsByTagName("translation")
		self.valid = 0		# useless object because no translation was found
		translations = []
		for trans_node in trans_nodes:
			trans_lang = trans_node.getAttribute("lang")
			if trans_lang == mothertongue:
				self.valid = 1
				trans_str = trans_node.childNodes[0].data
				translations.append(trans_str)
		if self.valid:
			self.case_sensitive = 0
			self.pattern = rule_node.getElementsByTagName("pattern")[0].childNodes[0].data
			repl_word, repl_trans = self.getOtherMeaning(rule_node.parentNode, mothertongue, textlang)
			self.message = "'%s' means %s. Did you maybe mean '%s' (%s)?" % (self.pattern, \
				str.join(', ', translations), repl_word, str.join(', ', repl_trans))
			#print "#%s" % self.message.encode('latin1')
			token_strings = re.split("\s+", self.pattern)
			self.tokens = []
			for token_string in token_strings:
				token = Token('"%s"' % token_string) # quotes = it's a word (not a POS tag)
				self.tokens.append(token)
				#print "#%s" % token
			self.marker_from = 0
			self.marker_to = 0
		return

	def getOtherMeaning(self, rulegroup_node, mothertongue, textlang):
		"""Get the word (and its correct translations) that the user
		maybe meant when he used a false friend. Returns a tuple
		(word, [translations])."""
		replace_nodes = rulegroup_node.getElementsByTagName("pattern")
		word = None
		translations = []
		for replace_node in replace_nodes:
			repl_lang = replace_node.getAttribute("lang")
			if repl_lang == mothertongue:
				word = replace_node.childNodes[0].data
			trans_nodes = replace_node.parentNode.getElementsByTagName("translation")
			for trans_node in trans_nodes:
				trans_lang = trans_node.getAttribute("lang")
				#print "#%s, %s" % (trans_lang, textlang)
				if trans_lang == textlang:
					self.valid = 1
					trans_str = trans_node.childNodes[0].data
					translations.append(trans_str)
		return (word, translations)
	
	def setVars(self, rule_id, pattern, message, marker_from, marker_to, \
			example_good, example_bad, case_sensitive, false_positives, language):
		"""Manually initialize the pattern rule -- for test cases only."""
		self.rule_id = rule_id
		self.message = message
		self.false_positives = false_positives
		self.language = language
		self.marker_from = marker_from
		self.marker_to = marker_to
		self.example_good = example_good
		self.example_bad = example_bad
		self.case_sensitive = case_sensitive
		self.tokens = []
		token_strings = re.split("\s+", pattern)
		for token_string in token_strings:
			token = Token(token_string)
			self.tokens.append(token)
		return
		
	def isRealWord(self, tagged_words, i):
		if not tagged_words[i][1] and tagged_words[i][2] != 'SENT_START' and \
			tagged_words[i][2] != 'SENT_END':
			return 0
		return 1
	
	def match(self, tagged_words, position_fix):
		"""Check if there are rules that match the tagged_words. Returns a list
		of RuleMatch objects."""
		matches = []
		ct = 0
		tagged_words_copy = tagged_words		# no copy, just a refernce
		last_match = None
		
		#print tagged_words_copy
		for word_tag_tuple in tagged_words_copy:
			i = ct
			p = 0		# matched position in the pattern so far
			expected_token = None		# expected token if the pattern matches
			found = None
			match = 1
			first_match = None

			while match:
				try:
					if not self.isRealWord(tagged_words_copy, i):
						# here's just whitespace or other un-taggable crap:
						i = i + 1
						ct = ct + 1
						continue
					elif not first_match:
						first_match = ct
				except IndexError:		# end of tagged words
					break
				try:
					expected_token = self.tokens[p]
				except IndexError:
					# pattern isn't that long
					break
				expected_token_str = expected_token.token
				if tagged_words_copy[i][2] == 'SENT_START':
					found = 'SENT_START'
				elif tagged_words_copy[i][2] == 'SENT_END':
					found = 'SENT_END'
				elif expected_token.is_word:
					# TODO: some cases need to be esacped, e.g. "?", but
					# this breaks the pipe etc.
					#expected_token_str = re.escape(expected_token_str)
					# look at the real word:
					try:
						found = tagged_words_copy[i][1].strip()
					except:		# text isn't that long
						break
				else:
					# look at the word's POS tag:
					try:
						found = tagged_words_copy[i][2]
					except:		# text ends here
						break
				if not found:
					#print >> sys.stderr, "*** 'found' undefined (i=%d, %s/%s)" % (i, tagged_words_copy[i][1], tagged_words_copy[i][2])
					break
				case_switch = re.IGNORECASE
				if self.case_sensitive:
					case_switch = 0
				match = re.compile("%s$" % expected_token_str, case_switch).match(found)
				#print "%s: %s -- %s -%s- -> %s" % (self.rule_id, self.tokens[p], found, expected_token_str, match)
				if expected_token.negation:
					if not match:
						match = 1
					else:
						match = None
				#print "F=%s, m=%s, '%s'" % (found, match, re.escape(expected_token.token))
				i = i + 1
				p = p + 1

			if match and p == len(self.tokens):

				(first_match, from_pos, to_pos) = self.listPosToAbsPos(tagged_words_copy, first_match)
					
				# Let \n in a rule refer to the n'th matched word:
				l = first_match
				lcount = 1
				msg = self.message
				while lcount <= len(self.tokens) and l < len(tagged_words_copy):
					if self.isRealWord(tagged_words_copy, l):
						msg = msg.replace("\\%d" % lcount, tagged_words_copy[l][0])
						lcount = lcount + 1
					l = l + 1
				
				first_match_word = tagged_words_copy[first_match][0]
				match = RuleMatch(self.rule_id, \
					from_pos+position_fix, to_pos+position_fix, \
					msg, first_match_word)
				matches.append(match)

			ct = ct + 1
		return matches

	def listPosToAbsPos(self, l, first_match):

		j = first_match + 1
		i = 0
		mark_from_tmp = self.marker_from
		while mark_from_tmp > 0 and j < len(l):
			if l[j][1]:
				mark_from_tmp = mark_from_tmp - 1
			i = i + 1
			j = j + 1
		first_match = first_match + i

		last_match = first_match
		match_len = len(self.tokens)-self.marker_from+self.marker_to
		for el in l[first_match:]:
			if match_len == 0:
				break
			if el[1]:
				match_len = match_len - 1
			last_match = last_match + 1

		from_pos = 0
		for el in l[:first_match]:
			from_pos = from_pos + len(el[0])
		to_pos = 0
		for el in l[:last_match]:
			to_pos = to_pos + len(el[0])

		return (first_match, from_pos, to_pos)

class RuleMatch:
	"""A matching rule, i.e. an error or a warning and from/to positions."""
	
	def __init__(self, rule_id, from_pos, to_pos, message, first_match_word=None):
		self.id = rule_id
		self.from_pos = from_pos
		self.to_pos = to_pos
		self.message = message
		if first_match_word and first_match_word[0] in string.uppercase:
			# Replace the first char in <em>...</em> with its uppercase
			# variant. Useful for replacements at the beginning of the
			# sentence
			self.message = re.compile("<em>(.)").sub(self.upper, self.message)
		return

	def upper(self, match):
		return "<em>%s" % match.group(1)[0].upper()
	
	def __str__(self):
		"""String representation of this object, equals XML representation
		(except the stripped whit space)."""
		strng = self.toXML().strip() 
		return strng

	def toXML(self):
		"""XML representation of this object."""
		strng = '<error from="%d" to="%d">%s</error>' % (self.from_pos, self.to_pos, self.message)
		return strng

	def __cmp__(self, b):
		"""Compare by 'from' position."""
		if self.from_pos > b.from_pos:
			return 1
		elif self.from_pos < b.from_pos:
			return -1
		else:
			return 0

class Token:
	"""A word or tag token, negated or not. Examples:
	"^(has|will)",
	"he",
	(VB|VBP)
	"""
	
	def __init__(self, token):
		self.token = token
		self.negation = 0
		self.is_word = 0
		self.is_tag = 0
		if self.token.startswith('^'):
			self.token = token[1:len(token)]	# remove '^'
			self.negation = 1
		if self.token.startswith('"'):
			self.is_word = 1
			if not self.token.endswith('"'):
				print >> sys.stderr, "*** Warning: token '%s' starts with quote but doesn't end with quote!" % self.token
			self.token = self.token[1:(len(self.token)-1)]	# remove quotes
		else:
			self.is_tag = 1
		return

	def __str__(self):
		"""For debugging only"""
		strng = self.token 
		if self.negation:
			strng = "^%s" % strng
		if self.is_word:
			strng = '"%s"' % strng
		return strng
