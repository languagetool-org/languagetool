#!/usr/bin/python
# A rule-based style and grammar checker
# Copyright (C) 2002,2003 Daniel Naber <daniel.naber@t-online.de>
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

import getopt
import os
import re
import socket
import string
import sys
import time

import Tagger
import Rules
import SentenceSplitter

class TextChecker:
	"""A rule-based style and grammar checker."""

	def __init__(self, grammar, falsefriends, words, builtin, \
		textlanguage, mothertongue, max_sentence_length):
		# which rules are activated (lists)?
		#fixme: not used???
		self.grammar = grammar
		self.falsefriends = falsefriends
		self.words = words
		self.builtin = builtin
		self.textlanguage = textlanguage
		self.mothertongue = mothertongue
		self.max_sentence_length = max_sentence_length
		self.tagger = Tagger.Tagger()
		self.tagger.bindData()
		self.rules = Rules.Rules(self.max_sentence_length)
		return
		
	def checkFile(self, filename):
		"""Check a text file and return the results as an XML formatted list 
		of possible errors."""
		f = open(filename)
		text = f.read()
		f.close()
		(rule_matches, result, tagged_words) = self.check(text)
		return (rule_matches, result, tagged_words)

	def check(self, text):
		"""Check a text string and return the results as an XML formatted list 
		of possible errors."""
		splitter = SentenceSplitter.SentenceSplitter()
		sentences = splitter.split(text)
		#print sentences
		#tx = time.time()
		rule_matches = []
		all_tagged_words = []
		char_counter = 0
		for sentence in sentences:
			tagged_words = self.tagger.tagText(sentence)
			tagged_words.insert(0, ('', None, 'SENT_START'))
			tagged_words.append(('', None, 'SENT_END'))
			#print tagged_words
			all_tagged_words.extend(tagged_words)
			#print "time1: %.2fsec" % (time.time()-tx)
			#tx = time.time()
			for rule in self.rules.rules:
				matches = rule.match(tagged_words, char_counter)
				rule_matches.extend(matches)
				#print "time2: %.2fsec" % (time.time()-tx)
				#tx = time.time()
			for triple in sentence:
				char_counter = char_counter + len(triple[0])
		rule_match_list = []
		for rule_match in rule_matches:
			rule_match_list.append(rule_match.toXML())
		xml_part = "<errors>\n%s\n</errors>\n" % string.join(rule_match_list, "\n")
		#print "time3: %.2fsec" % (time.time()-tx)
		# TODO: optionally return tagged text
		#print "2=>%.2f" % (time.time()-tx)
		return (rule_matches, xml_part, all_tagged_words)

def cleanEntities(s):
	"""Replace only the most common BNC entities with their
	ASCII respresentation."""
	s = re.compile("&amp;?").sub("&", s)
	s = re.compile("&pound;?").sub("£", s)
	s = s.replace("&ast", "*")
	s = s.replace("&aacute", "a")
	s = s.replace("&agr", "a")
	s = s.replace("&bquo", "\"")
	s = s.replace("&equo", "\"")
	s = s.replace("&quot", "'")
	s = s.replace("&deg", "°")
	s = s.replace("&dollar", "$")
	s = s.replace("&eacute", "e")
	s = s.replace("&egrave", "e")
	s = s.replace("&percnt", "&")
	s = s.replace("&ndash", "-")
	s = s.replace("&mdash", "--")
	s = s.replace("&hellip", "...")
	s = s.replace("&lsqb", "[")
	s = s.replace("&rsqb", "]")
	s = s.replace("&uuml", "ü")
	s = s.replace("&auml", "ä")
	s = s.replace("&öuml", "ö")
	s = s.replace("&Uuml", "Ü")
	s = s.replace("&Auml", "Ä")
	s = s.replace("&Ouml", "Ö")
	return s

def checkBNCFiles(directory, checker):
	"""Recursively load all files from a directory, extract
	all paragraphs and feed them to the style and grammar checker
	one by one."""
	para_regex = re.compile("<p>(.*?)</p>", re.DOTALL)
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
			#print filename
			checkBNCFiles(filename, checker)
		elif os.path.isfile(filename):
			print "FILE=%s" % file
			f = open(filename)
			s = f.read()
			f.close()
			matches = para_regex.findall(s)
			for match in matches:
				s = xml_regex.sub("", match)
				s = whitespace_regex.sub(" ", s)
				s = cleanEntities(s)
				s = s.strip()
				#print s
				(rule_matches, result, tagged_words) = checker.check(s)
				if len(rule_matches) == 0:
					pass
					#print >> sys.stderr, "No errors found."
				else:
					print result
	return

def usage():
	print "Usage: TextChecker.py [OPTION] <filename>"
	print "  -h, --help               Show this help"
	print "  -c, --check              Check directory with BNC files in SGML format"
	print "  -g, --grammar=...        Use only these grammar rules"
	print "  -f, --falsefriends=...   Use only these false friend rules"
	print "  -m, --mothertongue=...   Your native language"
	print "  -l, --sentencelength=... Maximum sentence length"
	return

def main():
	options = None
	rest = None
	try:
		(options, rest) = getopt.getopt(sys.argv[1:], 'hcg:f:w:l:', \
			['help', 'check', 'grammar=', 'falsefriends=', 'words=', \
			'mothertongue=', 'textlanguage=', 'sentencelength='])
	except getopt.GetoptError,e :
		print >> sys.stderr, "Error: ", e
		usage()
		sys.exit(2)
	grammar = falsefriends = words = []
	# todo: use?
	builtin = []
	textlanguage = mothertongue = None
	max_sentence_length = None

	for o, a in options:
		if o in ("-g", "--grammar"):
			grammar = a.split(",")
		elif o in ("-f", "--falsefriends"):
			falsefriends = a.split(",")
		elif o in ("-w", "--words"):
			words = a.split(",")
		elif o in ("-m", "--mothertongue"):
			mothertongue = a
		elif o in ("-t", "--textlanguage"):
			textlanguage = a
		elif o in ("-l", "--sentencelength"):
			max_sentence_length = a

	for o, a in options:
		if o in ("-h", "--help"):
			usage()
			sys.exit(0)
		elif o in ("-c", "--check"):
			checker = TextChecker(grammar, falsefriends, words, builtin, \
				textlanguage, mothertongue, max_sentence_length)
			for filename in rest:
				checkBNCFiles(filename, checker)
			sys.exit(0)

	if len(rest) == 1:
		checker = TextChecker(grammar, falsefriends, words, builtin, \
			textlanguage, mothertongue, max_sentence_length)
		(rule_matches, result, tagged_words) = checker.checkFile(rest[0])
		if not result:
			print >> sys.stderr, "No errors found."
		else:
			print result
	else:
		usage()
	return

if __name__ == "__main__":
    main()
