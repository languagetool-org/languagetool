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
import re
import socket
import string
import sys
import time

import Tagger
import Rules
import SentenceSplitter

# TODO:
#  --server mode that listens to a local unix sockets and expects:
#	<text grammar="id1,id2,..."
#		falsefriends="id1,..." words="..." (...)>Bla &lt;foo&gt; some text...</text>

class TextChecker:
	"A rule-based style and grammar checker."

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
		self.rules = Rules.Rules()
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
		tx = time.time()
		rule_matches = []
		all_tagged_words = []
		char_counter = 0
		for sentence in sentences:
			tagged_words = self.tagger.tagText(sentence)
			all_tagged_words.extend(tagged_words)
			#print "****"
			#print tagged_words
			#print "time1: %.2fsec" % (time.time()-tx)
			#tx = time.time()
			for rule in self.rules.rules:
				#print rule
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

def usage():
	print "Usage: TextChecker.py <filename>"
	return

def main():
	# todo: max sent. length
	(options, rest) = getopt.getopt(sys.argv[1:], 'hsgfw', \
		['help', 'server', 'grammar=', 'falsefriends=', 'words=', \
		'mothertongue=', 'textlanguage='])
	#print options
	#print "rest=%s"%rest

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

	for o, a in options:
		if o in ("-h", "--help"):
			usage()
			sys.exit()
		elif o in ("-s", "--server"):
			checker = TextChecker(grammar, falsefriends, words, builtin, \
				textlanguage, mothertongue, max_sentence_length)
			checker.server()
			sys.exit()

	if len(rest) == 1:
		checker = TextChecker(grammar, falsefriends, words, builtin, \
			textlanguage, mothertongue, max_sentence_length)
		(rule_matches, result, tagged_words) = checker.checkFile(rest[0])
		if not result:
			print "No errors found."
		else:
			print result
	else:
		usage()

if __name__ == "__main__":
    main()
