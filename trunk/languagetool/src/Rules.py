# -*- coding: iso-8859-1 -*-
# Class for Grammar and Style Rules
#$rcs = ' $Id: Rules.py,v 1.12 2004-10-31 22:55:23 dnaber Exp $ ' ;
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

import Tools
import codecs # tktk

import copy
import os
import re
import string
import sys
import xml.dom.minidom
from string import *

# FIXME:
grammarFile = 'engrammar.xml'
wordFile = 'enwords.xml'
falsefriendsFile = 'enfalse_friends.xml'

class Rule:
	"""Style or grammar rule -- quasi virtual class."""

	def __init__(self, rule_id, message, false_positives, language):
		self.rule_id = rule_id
		self.message = message
		# errors per 100 sentences in the BNC, i.e. mostly false positives:
		self.false_positives = false_positives
		self.language = language	# two letter code like "en" or None (= relevant for alle languages)
		return

	# match() is not defined here, but in the sub classes

class Rules:
	"""All known style and grammar error rules (from XML and the built-in ones)."""

	python_rules_dir = "python_rules"

	def __init__(self, max_sentence_length, grammar_rules, word_rules, \
		builtin_rules, false_friend_rules, textlanguage, mothertongue):
		"""Parse all rules and put them in the self.rules list, together
		with built-in rules like the SentenceLengthRule."""
		self.textlanguage = textlanguage
		if textlanguage == 'en':
			self.rule_files = [os.path.join(sys.path[0], "rules", grammarFile),
						os.path.join(sys.path[0], "rules", wordFile),
						os.path.join(sys.path[0], "rules", falsefriendsFile)]
		else:
			self.rule_files = [os.path.join(sys.path[0], "rules", grammarFile)]
		self.rules = []

		# dynamically load rule files from the "python_rules" dir:
		sys.path.append(self.python_rules_dir)
		dyn_files = os.listdir(self.python_rules_dir)
		for filename in dyn_files:
			if textlanguage == 'en':
				if filename[0:2] != 'en' and filename[0:3] != 'all':
					continue
			elif textlanguage == 'de':
				if filename[0:2] != 'de' and filename[0:3] != 'all':
					continue
			elif textlanguage == 'hu':
				if filename[0:2] != 'hu' and filename[0:3] != 'all':
					continue
			if not filename.endswith(".py") or filename.endswith("Test.py"):
				continue
			filename = filename[:-3]		# cut off ".py"
			exec("import %s" % filename)
			try:
				exec("dynamic_rule = %s.%s()" % (filename, filename))
			except AttributeError:
				print filename
				raise InvalidFilename(filename)
			if not hasattr(dynamic_rule, "match"):
				raise MissingMethod("match", "%s.py" % filename)
			if dynamic_rule.rule_id == "SENTENCE_LENGTH" and \
				max_sentence_length != None:
				dynamic_rule.setMaxLength(max_sentence_length)
			# do not use the rule if it wasn't activated
			# (builtin_rules == None will use all rules):
			if not builtin_rules or dynamic_rule.rule_id in builtin_rules:
				self.rules.append(dynamic_rule)

		for filename in self.rule_files:
			# minidom expects the DTD in the current directory, not in the
			# documents directory, so we have to chdir to 'rules':
			dir_temp = os.getcwd()
			os.chdir(os.path.dirname(filename))
			doc = xml.dom.minidom.parse(os.path.basename(filename))
			os.chdir(dir_temp)
			if filename.endswith(grammarFile):
				rule_nodes = doc.getElementsByTagName("rule")
				for rule_node in rule_nodes:
					rule = PatternRule(rule_node)
					lang_ok = 0
					if self.textlanguage == None or self.textlanguage == rule.language:
						lang_ok = 1
					if lang_ok and (grammar_rules == None or rule.rule_id in grammar_rules):
						self.rules.append(rule)
			elif filename.endswith("words.xml"):
				rule_nodes = doc.getElementsByTagName("rule")
				for rule_node in rule_nodes:
					rule = PatternRule(rule_node)
					lang_ok = 0
					if self.textlanguage == None or self.textlanguage == rule.language:
						lang_ok = 1
					if lang_ok and (word_rules == None or rule.rule_id in word_rules):
						self.rules.append(rule)
			elif filename.endswith("false_friends.xml"):
				pattern_nodes = doc.getElementsByTagName("pattern")
				for pattern_node in pattern_nodes:
					lang = pattern_node.getAttribute("lang")
					if self.textlanguage == None or lang == self.textlanguage:
						rule = PatternRule(pattern_node.parentNode, 1, mothertongue, textlanguage)
						if rule.valid and (false_friend_rules == None or \
							rule.rule_id in false_friend_rules):
							self.rules.append(rule)
		return

class InvalidFilename(Exception):

	def __init__(self, value):
		self.value = value
		return

	def __str__(self):
		s = "Constructor must be named as the file, i.e. '%s'" % self.value
		return s

class MissingMethod(Exception):

	def __init__(self, value, filename):
		self.value = value
		self.filename = filename
		return

	def __str__(self):
		s = "The '%s' method needs to be implemented in %s" % (self.value, self.filename)
		return s

class WhitespaceRule(Rule):
	"""A rule that matches punctuation not followed by a whitespace
	and whitespace preceding punctuation. This rule does not work
	on sentence level, it works on complete tagged texts or paragraphs."""

	punct = "[.,?!:;]"
	punct_regex = re.compile("^%s+$" % punct)
	whitespace_regex = re.compile("^\s+$")
	after_punct_regex = re.compile("^[\"]+$")
	number_regex = re.compile("^\d+$")
	whitespace_before_punct = re.compile("^\s+%s" % punct)

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
		
	def match(self, tagged_words, chunks=None, position_fix=0, line_fix=0, column_fix=0):
		"""Check if a sentence contains whitespace/token sequences
		that are against the 'use a space after, but not before, a token'
		rule."""
		matches = []
		text_length = 0
		line_breaks = 1
		column = 0
		i = 0
		while 1:
			if i >= len(tagged_words)-1:
				break
			org_word = tagged_words[i][0]
			line_breaks_cur = Tools.Tools.countLinebreaks(org_word) 
			if line_breaks_cur > 0:
				column = 0
			line_breaks = line_breaks + line_breaks_cur
			org_word_next = self.getNextTriple(tagged_words, i+1)
			if org_word_next:
				org_word_next = org_word_next[0]
			text_length = text_length + len(org_word)
			if tagged_words[i][1] == None:
				# ignore whitespace
				if line_breaks_cur == 0:
					column = column + len(org_word)
				i = i + 1
				continue
			whitespace_length = len(tagged_words[i+1][0])
			if line_breaks_cur == 0:
				column = column + len(org_word)
			if self.punct_regex.match(org_word) and not (org_word.endswith("\n") or org_word.endswith("\r")):
				word_next = tagged_words[i+1][1]
				word_next = self.getNextTriple(tagged_words, i+1)
				if word_next:
					word_next = word_next[1]
					if word_next and self.number_regex.match(word_next):
						# don't complain about "24,000" etc.
						i = i + 1
						continue
				if word_next and (not self.after_punct_regex.match(org_word_next)) and \
					(not self.whitespace_regex.match(org_word_next)):
					matches.append(RuleMatch(self.rule_id, text_length, text_length + len(org_word), 
						line_breaks+line_fix,
						column+column_fix,
						"Usually a space character is inserted after punctuation."))
			elif self.whitespace_before_punct.match(org_word):
				if not self.punct_regex.match(org_word_next):
					matches.append(RuleMatch(self.rule_id, text_length, text_length + len(org_word),
						line_breaks+line_fix, column+column_fix,
						"Usually no space character is inserted before punctuation."))
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
		if not self.rule_id:
			# FIXME? rule_id is not unique...
			self.rule_id = rule_node.parentNode.getAttribute("id")
		self.pattern = rule_node.getElementsByTagName("pattern")[0].childNodes[0].data.strip()
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
		self.false_positives = None		# None = unknown
		if rule_node.getElementsByTagName("error_rate"):
			error_rate_node = rule_node.getElementsByTagName("error_rate")[0]
			warnings = error_rate_node.getAttribute("warnings")
			sentences = error_rate_node.getAttribute("sentences")
			try:
				if int(sentences) != 0:
					error_rate = float(warnings) / float(sentences) * 100
					self.false_positives = error_rate
			except ValueError:
				pass
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
			self.pattern = rule_node.getElementsByTagName("pattern")[0].childNodes[0].data.strip()
			repl_word, repl_trans = self.getOtherMeaning(rule_node.parentNode, mothertongue, textlang)
			l = []
			for elem in repl_trans:
				l.append("<em>%s</em>" % elem)
			repl_trans_str = str.join(', ', l)
			self.message = "'%s' means %s. " % (self.pattern, str.join(', ', translations))
			if repl_word:
				self.message = self.message + " Did you maybe mean '%s', which is %s?" % \
					(repl_word, repl_trans_str)
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

	def match(self, tagged_words, chunks=None, position_fix=0, line_fix=0, column_fix=0):
		"""Check if there are rules that match the tagged_words. Returns a list
		of RuleMatch objects."""
		matches = []
		ct = 0
		tagged_words_copy = tagged_words		# no copy, just a refernce
		last_match = None

		#print self.rule_id
		#print tagged_words_copy
		for word_tag_tuple in tagged_words_copy:
			i = ct
			p = 0		# matched position in the pattern so far
			expected_token = None		# expected token if the pattern matches
			found = None
			match = 1
			first_match = None
			chunk_corr = 0
			chunk_len = 0

			while match:
				try:
					if not tagged_words_copy[i][1] and tagged_words_copy[i][2] != 'SENT_START' and tagged_words_copy[i][2] != 'SENT_END':
						# here's just whitespace or other un-taggable stuff:
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

				#print "expected_token_str=%s" % expected_token_str
				if tagged_words_copy[i][2] == 'SENT_START':
					found = 'SENT_START'
				elif tagged_words_copy[i][2] == 'SENT_END':
					found = 'SENT_END'
				elif expected_token.is_word:
					# TODO: some cases need to be escaped, e.g. "?", but
					# this breaks the pipe etc.
					#expected_token_str = re.escape(expected_token_str)
					# look at the real word:
					try:
						found = tagged_words_copy[i][1].strip()
					except:		# text isn't that long
						break
				elif expected_token.is_chunk:
					#print "chunk %s@%d?" % (expected_token.token, i)
					found = None
					for from_pos, to_pos, chunk_name in chunks:
						if i >= from_pos and i <= to_pos:
							found = chunk_name
							#print "CHUNK %d-%d: %s" % (from_pos, to_pos, chunk_name)
							i = i + (to_pos - from_pos)
							chunk_corr = chunk_corr + (to_pos - from_pos)
							chunk_len = chunk_len + 1
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
				case_sensitive = re.IGNORECASE
				if self.case_sensitive:
					case_sensitive = 0
				if expected_token.simple_token:
					# speed up for e.g. simple false friends rules that don't
					# require regex matching:
					if case_sensitive:
						#print "exp:%s" %expected_token
						match = (expected_token_str.lower() == found.lower())
					else:
						match = (expected_token_str == found)
				else:
					match = re.compile("%s$" % expected_token_str, case_sensitive).match(found)
				#print "%s: %s/%s -> %s" % (self.rule_id, found, expected_token_str, match)
				if expected_token.negation:
					if not match:
						match = 1
					else:
						match = None
				#print "F=%s, m=%s, '%s'" % (found, match, re.escape(expected_token.token))
				i = i + 1
				p = p + 1

			#print "p=%d, len(self.tokens)=%d" % (p, len(self.tokens))
			if match and p == len(self.tokens):

				#print "##MATCH "+found+" " +expected_token_str
				#FIXME: does this always mark the correct position?
				(first_match, from_pos, to_pos, line, column) = self.listPosToAbsPos(tagged_words_copy, \
					first_match, 0)
				to_pos = to_pos + chunk_corr

				# Let \n in a rule refer to the n'th matched word:
				l = first_match
				lcount = 1
				msg = self.message
				while lcount <= len(self.tokens) and l < len(tagged_words_copy):
					if not tagged_words_copy[l][1] and tagged_words_copy[l][2] != 'SENT_START' and tagged_words_copy[l][2] != 'SENT_END':
						pass
					else:
						msg = msg.replace("\\%d" % lcount, tagged_words_copy[l][0])
						lcount = lcount + 1
					l = l + 1

				first_match_word = tagged_words_copy[first_match][0]
				match = RuleMatch(self.rule_id, from_pos+position_fix, to_pos+position_fix, \
					line+line_fix, column+column_fix, msg, first_match_word)
				matches.append(match)

			ct = ct + 1
		return matches

	def listPosToAbsPos(self, l, first_match, chunk_corr=0):
		#print "*%d (%d)" % (first_match, chunk_corr)
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
		match_len = len(self.tokens)-self.marker_from+self.marker_to+chunk_corr
		for el in l[first_match:]:
			if match_len == 0:
				break
			if el[1]:
				match_len = match_len - 1
			last_match = last_match + 1

		from_pos = 0
		line = 0
		column = 0			# FIXME!
		for el in l[:first_match]:
			#print "** '%s' (%d)" % (el[0], first_match)
			matches = re.findall("[\n\r]", el[0])
			line = line + len(matches)
			if len(matches) > 0:
				column = 0
			else:
				column = column + len(el[0])
			from_pos = from_pos + len(el[0])
		#print "** L=%s" % line
		to_pos = 0
		for el in l[:last_match]:
			to_pos = to_pos + len(el[0])

		return (first_match, from_pos, to_pos, line, column)

class RuleMatch:
	"""A matching rule, i.e. an error or a warning and from/to positions."""

	def __init__(self, rule_id, from_pos, to_pos, line, column, message, first_match_word=None):
		self.id = rule_id
		self.from_pos = from_pos
		self.to_pos = to_pos
		self.line = line
		self.column = column
		self.message = message
		# TOOD: is it okay to use 'latin1' here?:
		if first_match_word and first_match_word[0] in unicode(string.uppercase, 'latin1'):
			# Replace the first char in <em>...</em> with its uppercase
			# variant. Useful for replacements at the beginning of the
			# sentence
			self.message = re.compile("<em>(.)").sub(self.upper, self.message)
		return

	def upper(self, match):
		return "<em>%s" % match.group(1)[0].upper()

	def __str__(self):
		"""String representation of this object, i.e. human readable output."""
		msg = self.message
		msg = re.compile("</?message>").sub("", msg)
		msg = re.compile("</?em>").sub("'", msg)
		strng = 'Line %d, Column %d: %s' % (self.line, self.column, msg)
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
	"""A word, tag or chunk token, negated or not. Examples:
	"^(has|will)",
	"he",
	(VB|VBP),
	_NP
	"""
	
	def __init__(self, token):
		self.token = token
		self.negation = 0
		self.is_word = 0
		self.is_tag = 0
		self.is_chunk = 0
		if self.token.find("|") != -1 or self.token.find("(") != -1 \
			or self.token.find("[") != -1 or self.token.find(".") != -1:
			self.simple_token = 0
		else:
			self.simple_token = 1		# no regex required
		if self.token.startswith('^'):
			self.token = token[1:]	# remove '^'
			self.negation = 1
		if self.token.startswith('"'):
			self.is_word = 1
			if not self.token.endswith('"'):
				print >> sys.stderr, "*** Warning: token '%s' starts with quote but doesn't end with quote!" % self.token
			self.token = self.token[1:(len(self.token)-1)]	# remove quotes
		elif self.token.startswith('_'):
			self.token = token[1:]	# remove '_'
			self.is_chunk = 1
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
