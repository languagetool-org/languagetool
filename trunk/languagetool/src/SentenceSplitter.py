#!/usr/bin/python
# -*- coding: iso-8859-1 -*-
# Copyright (C) 2003 Daniel Naber <daniel.naber@t-online.de>
# Based on Shlomo Yona's Perl module Lingua::EN::Sentence 0.25

import os
import string
import re
import sys

class SentenceSplitter:

	ABBR_FILE = os.path.join(sys.path[0], "data", "abbr.txt")
	
	EOS = "\001"
	#EOS = "<>"		# for testing only
	P = """[\.!?]"""				## PUNCTUATION
	AP = """(?:'|"|�|\)|\]|\})?"""	## AFTER PUNCTUATION
	PAP = "%s%s" % (P, AP)
	
	reFlags = re.DOTALL|re.LOCALE
	
	def __init__(self):
		"""Init the object by loading the abbreviation list."""
		self.abbr = self.loadAbbreviations()
		return

	def loadAbbreviations(self):
		"""Load the abbreviation list and return all words in a list."""
		abbr = []
		f = open(self.ABBR_FILE, "r")
		while 1:
			l = f.readline()
			if not l:
				break
			l = l.strip()
			if l:
				abbr.append(l)
		f.close()
		return abbr
		
	def split(self, text):
		"""Take a text and split it into sentences. Return the list
		of sentences. Adapted from Perl's Lingua-EN-Sentence-0.25 module."""
		if text == None:
			return []
		#print "text=%s" % text
		marked_text = self.first_sentence_breaking(text)
		#print "marked_text=%s" % marked_text
		fixed_marked_text = self.remove_false_end_of_sentence(marked_text)
		#print "fixed_marked_text=%s" % fixed_marked_text
		fixed_marked_text = self.split_unsplit_stuff(fixed_marked_text)
		#print "fixed_marked_text=%s" % fixed_marked_text
		sentences = re.split(self.EOS, fixed_marked_text)
		return sentences

	def first_sentence_breaking(self, text):
		"""Add a special break character at all places with typical sentence
		delimiters."""
		# Double new-line means a new sentence:
		text = re.compile("(\n\s*\n)", self.reFlags).sub("\\1%s" % self.EOS, text)
		# Punctuation followed by whitespace means a new sentence:
		text = re.compile("(%s\s)" % self.PAP, self.reFlags).sub("\\1%s" % self.EOS, text)
		# New (compared to the perl module): Punctuation followed by uppercase followed
		# by non-uppercase character (except dot) means a new sentence:
		text = re.compile("(%s)([%s][^%s.])" % (self.PAP, string.uppercase, string.uppercase), \
			self.reFlags).sub("\\1%s\\2" % self.EOS, text)
		# Break also when single letter comes before punctuation:
		text = re.compile("(\s\w%s)" % self.P, self.reFlags).sub("\\1%s" % self.EOS, text)
		return text
		
	def remove_false_end_of_sentence(self, text):
		"""Repair some positions that don't require a split, i.e. remove the
		special break character."""
		
		# Don't split at e.g. "U. S. A.":
		text = re.compile("([^-\w]\w%s\s)%s" % (self.PAP, self.EOS), self.reFlags).sub("\\1", text)
		# Don't split at e.g. "U.S.A.":
		text = re.compile("([^-\w]\w%s)%s" % (self.P, self.EOS), self.reFlags).sub("\\1", text)

		# Don't split after a white-space followed by a single letter followed
		# by a dot followed by another whitespace.
		# e.g. " p. "
		text = re.compile("(\s\w\.\s+)%s" % self.EOS, self.reFlags).sub("\\1", text)

		# Don't split at "bla bla... yada yada" (TODO: use \.\.\.\s+ instead?)
		text = re.compile("(\.\.\. )%s([%s])" % (self.EOS, string.lowercase), self.reFlags).sub("\\1\\2", text)
		# Don't split [.?!] when the're quoted:
		text = re.compile("(['\"]%s['\"]\s+)%s" % (self.P, self.EOS)).sub("\\1", text)

		# Don't split at abbreviations:
		for abbr in self.abbr:
			# TODO: really ignore case?
			s = "(\\b%s%s\s)%s" % (abbr, self.PAP, self.EOS)
			text = re.compile(s, self.reFlags|re.IGNORECASE).sub("\\1", text)
		
		# Don't break after quote unless there's a capital letter:
		# e.g.: "That's right!" he said.
		text = re.compile('(["\']\s*)%s(\s*[%s])' % (self.EOS, string.lowercase), self.reFlags).sub("\\1\\2", text)

		# fixme? not sure where this should occur, leaving it commented out:
		# don't break: text . . some more text.
		#text=~s/(\s\.\s)$EOS(\s*)/$1$2/sg;

		text = re.compile("(\s%s\s)%s" % (self.PAP, self.EOS), self.reFlags).sub("\\1", text)

		# extension by dnaber --commented out, doesn't help:
		#text = re.compile("(:\s+)%s(\s*[%s])" % (self.EOS, string.lowercase), self.reFlags).sub("\\1\\2", text)
		return text

	def split_unsplit_stuff(self, text):
		"""Treat some more special cases that make up a sentence boundary. Insert
		the special break character at these positions."""
		# Split at e.g. "no. 5 ":
		text = re.compile("(\D\d+)(%s)(\s+)" % self.P, self.reFlags).sub("\\1\\2%s\\3" % self.EOS, text)
		# TODO: Not sure about this one, leaving out foir now:
		#text = re.compile("(%s\s)(\s*\()" % self.PAP, self.reFlags).sub("\\1%s\\2" % self.EOS, text)
		# Split e.g.: He won't. #Really.
		text = re.compile("('\w%s)(\s)" % self.P, self.reFlags).sub("\\1%s\\2" % self.EOS, text)
		# Split e.g.: He won't say no. Not really.
		text = re.compile("(\sno\.)(\s+)(?!\d)", self.reFlags|re.IGNORECASE).sub("\\1%s\\2" % self.EOS, text)
		# Split at "a.m." or "p.m." followed by a capital letter.
		text = re.compile("([ap]\.m\.\s+)([%s])" % string.uppercase, self.reFlags).sub("\\1%s\\2" % self.EOS, text)
		return text

if __name__ == "__main__":
	#t = '"Do split me." Will you?'
	#print t
	#s = SentenceSplitter()
	#l = s.split(t)
	#print l
	print "Please use ./SentenceSplitterTest.py for testing."
