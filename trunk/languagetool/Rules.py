# Class for Grammar and Style Rules
# (c) 2002,2003 Daniel Naber <daniel.naber@t-online.de>
#$rcs = ' $Id: Rules.py,v 1.7 2003-06-21 19:49:04 dnaber Exp $ ' ;
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
import sys
import xml.dom.minidom

class Rules:
	"""All known style and grammar error rules (from XML and from python code)."""

	rules_grammar_file = "rules/grammar.xml"
	
	def __init__(self, max_sentence_length, grammar_rules):
		"""Parse all rules and put them in the self.rules list, together
		with built-in rules like the SentenceLengthRule."""
		self.rules = []
		length_rule = SentenceLengthRule()
		if max_sentence_length != None:
			length_rule.setMaxLength(max_sentence_length)
		self.rules.append(length_rule)
		# minidom expects the DTD in the current directory, not in the
		# documents directory, so we have to chdir to 'rules':
		dir_temp = os.getcwd()
		os.chdir(os.path.dirname(self.rules_grammar_file))
		doc = xml.dom.minidom.parse(os.path.basename(self.rules_grammar_file))
		os.chdir(dir_temp)
		rule_nodes = doc.getElementsByTagName("rule")
		for rule_node in rule_nodes:
			rule = PatternRule(rule_node)
			if grammar_rules == None or rule.rule_id in grammar_rules:
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
			matches.append(RuleMatch("MAX_LEN", too_long_start,
				too_long_end, self.max_length, self.max_length+1,
				"This sentence is %d words long, which exceeds the "
				"configured limit of %d words." % (count, self.max_length)))
		return matches

class PatternRule(Rule):
	"""A rule that can be formalised in the XML configuration file."""
	
	def __init__(self, rule_node):
		"""Build an object by parsing an XML rule node."""
		if rule_node == None:
			# for the test cases. They use setVars().
			return
		self.rule_id = rule_node.getAttribute("id")
		self.pattern = rule_node.getElementsByTagName("pattern")[0].childNodes[0].data
		token_strings = re.split("\s+", self.pattern)
		self.tokens = []
		for token_string in token_strings:
			token = Token(token_string)
			self.tokens.append(token)
		self.language = rule_node.getElementsByTagName("pattern")[0].getAttribute("lang")
		self.case_sensitive = 0
		if rule_node.getElementsByTagName("pattern")[0].getAttribute("case_sensitive") == 'yes':
			#print "*** %s" % rule_node.getElementsByTagName("pattern")[0].getAttribute("case_sensitive")
			self.case_sensitive = 1
		if rule_node.getElementsByTagName("message"):
			self.message = Tools.Tools.getXML(rule_node.getElementsByTagName("message")[0])
		else:
			self.message = Tools.Tools.getXML(rule_node.parentNode.getElementsByTagName("message")[0])
		self.marker_position = int(rule_node.getElementsByTagName("marker")[0].childNodes[0].data)
		example_nodes = rule_node.getElementsByTagName("example")
		self.example_good = ""
		self.example_bad = ""
		for example_node in example_nodes:
			# TODO?: only one good and one bad example currently supported:
			if example_node.getAttribute("type") == 'correct':
				self.example_good = Tools.Tools.getXML(example_node.childNodes[0])
			else:
				self.example_bad = Tools.Tools.getXML(example_node.childNodes[0])
		self.false_positives = rule_node.getElementsByTagName("error_rate")[0].childNodes[0].data
		return

	def setVars(self, rule_id, pattern, message, marker_position, \
			example_good, example_bad, case_sensitive, false_positives, language):
		"""Manually initialize the pattern rule -- for test cases only."""
		self.rule_id = rule_id
		self.message = message
		self.false_positives = false_positives
		self.language = language
		self.marker_position = marker_position
		self.example_good = example_good
		self.example_bad = example_bad
		self.case_sensitive = case_sensitive
		self.tokens = []
		token_strings = re.split("\s+", pattern)
		for token_string in token_strings:
			token = Token(token_string)
			self.tokens.append(token)
		return
		
	def match(self, tagged_words, position_fix):
		"""Check if there are rules that match the tagged_words. Returns a list
		of RuleMatch objects."""
		matches = []
		ct = 0
		tagged_words_copy = tagged_words		# no copy, just a refernce
		for word_tag_tuple in tagged_words_copy:
			i = ct
			p = 0
			expected_token = None		# expected token if the pattern matches
			found = None
			match = 1
			first_match = ct	

			while match:
				try:
					if not tagged_words_copy[i][1] and tagged_words_copy[i][2] != 'SENT_START' and \
						tagged_words_copy[i][2] != 'SENT_END':
						# here's just whitespace or other un-taggable crap:
						i = i + 1
						ct = ct + 1
						continue
				except IndexError:		# end of tagged words
					break
				try:
					expected_token = self.tokens[p]
				except IndexError:
					# pattern isn't that long
					break
				if tagged_words_copy[i][2] == 'SENT_START':
					found = 'SENT_START'
				elif tagged_words_copy[i][2] == 'SENT_END':
					found = 'SENT_END'
				elif expected_token.is_word:
					# look at the real word:
					try:
						found = tagged_words_copy[i][1]
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
				match = re.compile(expected_token.token+"$", case_switch).match(found)
				if expected_token.negation:
					if not match:
						match = 1
					else:
						match = None
				i = i + 1
				p = p + 1

			if match and p == len(self.tokens):
				ct_tmp = 0
				list_match_from = 0
				list_match_to = 0
				from_pos = 0
				to_pos = 0
				for tagged_word in tagged_words_copy:
					#print "%s [fm=%d, marker=%d, ct=%d]<br>" % (str(tagged_word), first_match, self.marker_position, ct_tmp)
					# TODO: break
					# fixme: not correct at end of sentence (e.g. "...don't.") etc.??
					if ct_tmp < first_match+self.marker_position:
						from_pos = from_pos + len(tagged_word[0])
						list_match_from = ct_tmp+1
					if ct_tmp < ct+self.marker_position:
						##fixme: problem at end...??
						to_pos = to_pos + len(tagged_word[0])
						list_match_to = ct_tmp+1
					ct_tmp = ct_tmp + 1
				match = RuleMatch(self.rule_id, \
					from_pos+position_fix, \
					to_pos+position_fix, \
					list_match_from, list_match_to, self.message)
				matches.append(match)

			ct = ct + 1		
		return matches
		
class RuleMatch:
	"""A matching rule, i.e. an error or a warning and from/to positions."""
	
	def __init__(self, rule_id, from_pos, to_pos, list_pos_from, list_pos_to, message):
		self.id = rule_id
		self.from_pos = from_pos
		self.to_pos = to_pos
		self.list_pos_from = list_pos_from
		self.list_pos_to = list_pos_to
		self.message = message
		return

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
				print >> sys.stderr, "*** Warning: token '%s' starts with quote but doesn't\
					end with quote!" % token
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
