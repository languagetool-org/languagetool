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
import ConfigParser

class TextChecker:
	"""A rule-based style and grammar checker."""
	
	encoding = "latin1"

	def __init__(self, grammar, falsefriends, words, \
		builtin, textlanguage, mothertongue, max_sentence_length):
		# Which rules are activated (a list of IDs):
		self.grammar = grammar
		self.falsefriends = falsefriends
		self.words = words
		self.builtin = builtin
		self.textlanguage = textlanguage
		self.mothertongue = mothertongue
		self.max_sentence_length = max_sentence_length
		config = ConfigParser.ConfigParser()
		config.readfp(open('TextChecker.ini'))
		Tagger.dicFile = config.get(textlanguage, 'dicFile');
		Tagger.affFile = config.get(textlanguage, 'affFile');
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
		return

	def checkFile(self, filename):
		"""Check a text file and return the results as an XML formatted list
		of possible errors."""
		text = ""
		#print "###%s" % filename.lower()
		#if filename.lower().endswith("xml"):
		#	doc = xml.dom.minidom.parse(filename)
		#	text_nodes = doc.getElementsByTagName("text:p")	# ???fixme?
		#	for text_node in text_nodes:
		#		#node_value = text_node.nodeValue
		#		if text_node.firstChild:
		#			#print "#"+str(text_node.firstChild.nodeValue)
		#			# fixme: make faster (append):
		#			text = "%s%s" % (text, text_node.firstChild.nodeValue)
		#	
		#else:
		f = codecs.open(filename, "r", self.encoding)
		text = f.read()
		f.close()
		#print "--->%s" % text
		(rule_matches, result, tagged_words) = self.check(text)
		return (rule_matches, result, tagged_words)

	def check(self, text):
		"""Check a text string and return the results as an XML formatted list
		of possible errors."""
		splitter = SentenceSplitter.SentenceSplitter()
		sentences = splitter.split(text)
		#tx = time.time()
		rule_matches = []
		char_counter = 0
		all_tagged_words = []
		for sentence in sentences:
			tagged_words = self.tagger.tagText(sentence)
#			print tagged_words
			chunks = self.chunker.chunk(tagged_words)
			#print "CHUNKS: %s" % chunks
			#print "CHUNKS: %s" % tagged_words[chunks[0][0]:chunks[0][1]]
			tagged_words.insert(0, ('', None, 'SENT_START'))
			tagged_words.append(('', None, 'SENT_END'))
			all_tagged_words.extend(tagged_words)
			#print "time1: %.2fsec" % (time.time()-tx)
			#tx = time.time()
			for rule in self.rules.rules:
#				print rule.rule_id
				matches = rule.match(tagged_words, chunks, char_counter)
				rule_matches.extend(matches)
				#print "time2: %.2fsec" % (time.time()-tx)
				#tx = time.time()
			for triple in sentence:
				char_counter = char_counter + len(triple[0])

		if not self.builtin or "WHITESPACE" in self.builtin:
			whitespace_rule = Rules.WhitespaceRule()
			rule_matches.extend(whitespace_rule.match(all_tagged_words))

		#print "###%s<p>" % str(all_tagged_words)
		rule_match_list = []
		for rule_match in rule_matches:
			rule_match_list.append(rule_match.toXML())
		xml_part = "<errors>\n%s\n</errors>\n" % string.join(rule_match_list, "\n")
		#print "time3: %.2fsec" % (time.time()-tx)
		# TODO: optionally return tagged text
		#print "2=>%.2f" % (time.time()-tx)
		return (rule_matches, xml_part, all_tagged_words)

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
				#print filename
				self.checkBNCFiles(filename, checker)
			elif os.path.isfile(filename) and filename.find(".") != -1:
				print >> sys.stderr, "Ignoring %s" % filename
			elif os.path.isfile(filename):
				print >> sys.stderr, "FILE=%s" % filename
				f = open(filename, 'r')
				s = f.read()
				f.close()
				s = unicode(s, 'iso-8859-1')
#				print s
				s_matches = sentence_regex.findall(s)
				self.bnc_sentences = self.bnc_sentences + len(s_matches)
				matches = para_regex.findall(s)
				for match in matches:
					#print len(match)
					self.bnc_paras = self.bnc_paras + 1
					s = xml_regex.sub("", match)
					s = whitespace_regex.sub(" ", s)
					s = Entities.Entities.cleanEntities(s)
					s = s.strip()
					#continue
					(rule_matches, result, tagged_words) = checker.check(s)
					if len(rule_matches) == 0:
						pass
						#print >> sys.stderr, "No errors found."
					else:
						for rule_match in rule_matches:
							s_mark = "%s***%s" % (s[:rule_match.from_pos], s[rule_match.from_pos:])
							print "%s:\n<!--%s: %s -->\n%s" % (rule_match.id, filename, s_mark.encode('utf8'), result.encode('utf8'))
							#print "%s:\n<!--  -->\n%s" % (filename, result.encode('utf8'))
		return

def usage():
	print "Usage: TextChecker.py [OPTION] <filename>"
	print "  -h, --help               Show this help"
	print "  -c, --check              Check directory with BNC files in SGML format"
	print "  -g, --grammar=...        Use only these grammar rules"
	print "  -f, --falsefriends=...   Use only these false friend rules"
	print "  -w, --words=...          Use only these style/word rules"
	print "  -b, --builtin=...        Use only these builtin rules"
	print "  -m, --mothertongue=...   Your native language"
	print "  -t, --textlanguage=...   The text's language"
	print "  -l, --sentencelength=... Maximum sentence length"
	return

def main():
	options = None
	rest = None
	try:
		(options, rest) = getopt.getopt(sys.argv[1:], 'hcg:f:w:b:m:t:l:', \
			['help', 'check', 'grammar=', 'falsefriends=', 'words=', \
			'builtin=', 'mothertongue=', 'textlanguage=', 'sentencelength='])
	except getopt.GetoptError,e :
		print >> sys.stderr, "Error: ", e
		usage()
		sys.exit(2)
	grammar = None
	falsefriends = None
	words = None
	builtin = None
	textlanguage = mothertongue = None
	max_sentence_length = None
	#	print "%s %s %s" %(Tagger.dicFile, Tagger.affFile,Tagger.grammarFile)
	#	sys.exit(0)
	textlanguage = 'de'		# default

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
		elif o in ("-t", "--textlanguage"):
			textlanguage = a
		elif o in ("-l", "--sentencelength"):
			max_sentence_length = a

	for o, a in options:
		if o in ("-h", "--help"):
			usage()
			sys.exit(0)
		elif o in ("-c", "--check"):
			checker = TextChecker(grammar, falsefriends, words, \
				builtin, textlanguage, mothertongue, max_sentence_length)
			for filename in rest:
				checker.checkBNCFiles(filename, checker)
			print >> sys.stderr, "Checked %d sentences in %d paragraphs." % \
				(checker.bnc_sentences, checker.bnc_paras)
			sys.exit(0)

	if len(rest) == 1:
		checker = TextChecker(grammar, falsefriends, words, builtin, \
			textlanguage, mothertongue, max_sentence_length)
		(rule_matches, result, tagged_words) = checker.checkFile(rest[0])
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
