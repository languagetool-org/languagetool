#!/usr/bin/python
# -*- coding: iso-8859-1 -*-
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

import codecs
import getopt
import locale
import os
import re
import socket
import string
import sys
import time
import xml.dom.minidom

import profile

sys.path.append(os.path.join(sys.path[0], "src"))
import Entities
import Tagger
import Chunker
import Rules
import SentenceSplitter
import Tools
import ConfigParser

class TextChecker:
	"""A rule-based style and grammar checker."""
	
	context = 15			# display this many character to the right and left for error context

	def __init__(self, grammar, falsefriends, words, \
		builtin, textlanguage, mothertongue, max_sentence_length, debug_mode):
		# Which rules are activated (a list of IDs):
		self.grammar = grammar
		self.falsefriends = falsefriends
		self.words = words
		self.builtin = builtin
		self.textlanguage = textlanguage
		self.mothertongue = mothertongue
		self.max_sentence_length = max_sentence_length
		self.debug_mode = debug_mode
		config = ConfigParser.ConfigParser()
		config.readfp(open('TextChecker.ini'))
		Tagger.dicFile = config.get(textlanguage, 'dicFile');
		Tagger.affFile = config.get(textlanguage, 'affFile');
		if self.max_sentence_length == None:
			self.max_sentence_length = config.get(textlanguage, 'maxSentenceLength');
		Rules.grammarFile = config.get(textlanguage, 'grammarFile');
		self.tagger = Tagger.Tagger(textlanguage)
		self.chunker = Chunker.Chunker()
		rules = Chunker.Rules()
		self.chunker.setRules(rules)
		self.tagger.bindData()
		self.rules = Rules.Rules(self.max_sentence_length, self.grammar,\
			self.words, self.builtin, self.falsefriends, \
			textlanguage, mothertongue)
		self.bnc_paras = 0
		self.bnc_sentences = 0
		self.xml_output = 0		# default to non-XML output
		# anything but 'C' seems to be okay to make the sentence splitter work
		# for languages with special characters:
		locale.setlocale(locale.LC_CTYPE, 'en_US.iso-8859-1')
		return

	def setXMLOutput(self, xml_output):
		self.xml_output = xml_output
		return

	def setInputEncoding(self, input_encoding):
		self.input_encoding = input_encoding
		return

	def checkFile(self, filename):
		"""Check a text file and return the results as an XML formatted list
		of possible errors."""
		text = ""
		f = codecs.open(filename, "r", self.input_encoding)
		text = f.read()
		f.close()
		(rule_matches, result, tagged_words) = self.check(text)
		return (rule_matches, result, tagged_words)

	def check(self, text):
		"""Check a text string and return the results as an XML formatted list
		of possible errors."""
		splitter = SentenceSplitter.SentenceSplitter()
		sentences = splitter.split(text)
		rule_matches = []
		char_counter = 0
		all_tagged_words = []
		line_counter = 1
		column_counter = 0
		prev_sentence = ""
		for sentence in sentences:
			#print "S='%s'" % (sentence)
			tagged_words = self.tagger.tagText(sentence)
			if self.debug_mode:
				print "Tw:",
				for tagged_word in tagged_words:
					if tagged_word[2]:
						print "%s/%s" % (tagged_word[0], tagged_word[2]),
			chunks = self.chunker.chunk(tagged_words)
			tagged_words.insert(0, ('', None, 'SENT_START'))
			tagged_words.append(('', None, 'SENT_END'))
			all_tagged_words.extend(tagged_words)
			if prev_sentence.endswith("\n") or sentence.startswith("\n"):
				column_counter = 0
			for rule in self.rules.rules:
				matches = rule.match(tagged_words, chunks, char_counter, line_counter, column_counter)
				rule_matches.extend(matches)
			for triple in sentence:
				char_counter = char_counter + len(triple[0])
			line_counter = line_counter + Tools.Tools.countLinebreaks(sentence)
			if Tools.Tools.countLinebreaks(sentence):
				column_counter = 0
			column_counter = column_counter + len(sentence)
			prev_sentence = sentence

		if not self.builtin or "WHITESPACE" in self.builtin:
			whitespace_rule = Rules.WhitespaceRule()
			rule_matches.extend(whitespace_rule.match(all_tagged_words))

		rule_match_list = []
		for rule_match in rule_matches:
			if self.xml_output:
				rule_match_list.append(rule_match.toXML())
				rule_match_list.append("\n")
			else:
				rule_match_list.append(rule_match.__str__())
				from_pos = max(rule_match.from_pos-self.context, 0)
				to_pos = min(rule_match.to_pos+self.context, len(text))
				summary = text[from_pos:to_pos]
				summary = re.compile("[\n\r]").sub(" ", summary)
				rule_match_list.append("\n\t...%s..." % summary)
				rule_match_list.append("\n")
				# TODO: use "^" to mark the *exact* position of the error:
				#rule_match_list.append("\n\t   %s^\n" % (" " * (context-1)))
		result = string.join(rule_match_list, "")
		if self.xml_output:
			result = "<errors>\n%s</errors>" % result
		# TODO: optionally return tagged text?
		return (rule_matches, result, all_tagged_words)

	def checkBNCFiles(self, directory, checker):
		"""Recursively load all files from a directory, extract
		all paragraphs and feed them to the style and grammar checker
		one by one."""
		para_regex = re.compile("<p>(.*?)</p>", re.DOTALL)
		sentence_regex = re.compile("<s n=\d+>", re.DOTALL)
		xml_regex = re.compile("<.*?>", re.DOTALL)
		whitespace_regex = re.compile("\s+", re.DOTALL)
		files = []
		filemode = 0
		if os.path.isfile(directory):		# call with a filename is okay
			files = [directory]
			filemode = 1
		else:
			files = os.listdir(directory)
		for file in files:
			filename = None
			if filemode:
				filename = file
			else:
				filename = os.path.join(directory, file)
			if os.path.isdir(filename):
				self.checkBNCFiles(filename, checker)
			elif os.path.isfile(filename) and filename.find(".") != -1:
				print >> sys.stderr, "Ignoring %s" % filename
			elif os.path.isfile(filename):
				print >> sys.stderr, "FILE=%s" % filename
				f = open(filename, 'r')
				s = f.read()
				f.close()
				s = unicode(s, 'iso-8859-1')
				s_matches = sentence_regex.findall(s)
				self.bnc_sentences = self.bnc_sentences + len(s_matches)
				matches = para_regex.findall(s)
				for match in matches:
					self.bnc_paras = self.bnc_paras + 1
					s = xml_regex.sub("", match)
					s = whitespace_regex.sub(" ", s)
					s = Entities.Entities.cleanEntities(s)
					s = s.strip()
					(rule_matches, result, tagged_words) = checker.check(s)
					if len(rule_matches) == 0:
						pass
					else:
						for rule_match in rule_matches:
							s_mark = "%s***%s" % (s[:rule_match.from_pos], s[rule_match.from_pos:])
							print "%s:\n<!--%s: %s -->\n%s" % (rule_match.id, filename, s_mark.encode('utf8'), result.encode('utf8'))
		return

def usage():
	print "Usage: TextChecker.py [OPTION] <filename>"
	print "  -h, --help               Show this help"
	print "  -l, --lang=...           The text's language (de, en, or hu)"
	print "  -g, --grammar=...        Use only these grammar rules"
	print "  -f, --falsefriends=...   Use only these false friend rules"
	print "  -w, --words=...          Use only these style/word rules"
	print "  -b, --builtin=...        Use only these builtin rules (currently only WHITESPACE)"
	print "  -m, --mothertongue=...   Your native language, used with false friend checking"
	print "  -s, --sentencelength=... Warn if a sentence is longer than this (default: never warn)"
	#print "  -c, --check              Check directory with BNC files in SGML format"
	print "  -e, --encoding           Input file's encoding/charset (e.g. latin1 or utf8)"
	print "  -x, --xml                Print out result as XML"
	print "  -d, --debug              Print out tagged words"
	return

def main():
	options = None
	rest = None
	try:
		(options, rest) = getopt.getopt(sys.argv[1:], 'hcxdg:f:w:b:m:l:s:e:', \
			['help', 'check', 'xml', 'debug', 'grammar=', 'falsefriends=', 'words=', \
			'builtin=', 'mothertongue=', 'lang=', 'sentencelength=', 'encoding='])
	except getopt.GetoptError,e :
		print >> sys.stderr, "Error: ", e
		usage()
		sys.exit(2)
		
	# Define the variables with the default values:
	grammar = None
	falsefriends = None
	words = None
	builtin = None
	textlanguage = mothertongue = None
	max_sentence_length = None
	textlanguage = 'en'
	xml_output = 0
	debug_mode = 0
	input_encoding = 'latin1'

	for o, a in options:
		if o in ("-g", "--grammar"):
			grammar = a.split(",")
		elif o in ("-f", "--falsefriends"):
			falsefriends = a.split(",")
		elif o in ("-w", "--words"):
			words = a.split(",")
		elif o in ("-b", "--builtin"):
			builtin = a.split(",")
		elif o in ("-m", "--mothertongue"):
			mothertongue = a
		elif o in ("-l", "--lang"):
			textlanguage = a
		elif o in ("-s", "--sentencelength"):
			max_sentence_length = a
		elif o in ("-e", "--encoding"):
			input_encoding = a
		elif o in ("-x", "--xml"):
			xml_output = 1
		elif o in ("-d", "--debug"):
			debug_mode = 1

	for o, a in options:
		if o in ("-h", "--help"):
			usage()
			sys.exit(0)
		elif o in ("-c", "--check"):
			checker = TextChecker(grammar, falsefriends, words, \
				builtin, textlanguage, mothertongue, max_sentence_length, debug_mode)
			for filename in rest:
				checker.checkBNCFiles(filename, checker)
			print >> sys.stderr, "Checked %d sentences in %d paragraphs." % \
				(checker.bnc_sentences, checker.bnc_paras)
			sys.exit(0)

	if len(rest) == 1:
		filename = rest[0]
		if not xml_output:
			display_name = Tools.Tools.getLanguageName(textlanguage)
			if not display_name:
				print >> sys.stderr, "Unknown language code '%s'" % textlanguage
				print >> sys.stderr, "Supported languages are en, de, and hu"
				sys.exit(2)
			print "Checking '%s', file encoding %s, language %s:" % (filename, \
				input_encoding, display_name)
		checker = TextChecker(grammar, falsefriends, words, builtin, \
			textlanguage, mothertongue, max_sentence_length, debug_mode)
		checker.setXMLOutput(xml_output)
		checker.setInputEncoding(input_encoding)
		(rule_matches, result, tagged_words) = checker.checkFile(filename)
		if not result:
			print >> sys.stderr, "No errors found."
		else:
			print result.encode('latin1')
	else:
		usage()
		sys.exit(1)
	return

if __name__ == "__main__":
	main()
	#profile.run('main()', 'prof')
