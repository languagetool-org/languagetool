# -*- coding: iso-8859-1 -*-
# Assign chunks to a tagged text
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

import os
import re
import sys

class Chunker:
	"""Assign chunks (like "noun phrase") to a tagged text."""

	def __init__(self):
		return

	def setRules(self, rules):
		"""Use the rules from this Rules object for the chunk() method."""
		self.rules = rules
		return

	def chunk(self, tagged_text):
		"""Take a POS tagged text and find all its chunks. Returns
		a list of (from, to, chunk_name) tuples where the from/to positions
		refer to the list position. Only parts of the list may be
		covered by chunks. There are no overlappings."""
		l = []
		
		tagged_text_pos = 0
		while 1:
			if tagged_text_pos >= len(tagged_text):
				break
			word, norm_word, tag = tagged_text[tagged_text_pos]

			for rule in self.rules.rules:
				#print "### %s" % rule.name
				match_start = None
				match_end = None
				pattern_pos = 0
				pos_corr = 0

				rule_match = 1
				cont = 1

				while 1:
					#print " %d,%d,%d" % (tagged_text_pos,pattern_pos,pos_corr)
					try:
						tag = tagged_text[tagged_text_pos+pattern_pos+pos_corr][2]
					except IndexError:
						#print "index error"
						break
					#print "%s ?= %s (pp=%d, ttp=%d)" % (tag, rule.pattern[pattern_pos], pattern_pos, tagged_text_pos)
					if pattern_pos == 0 and tag == None:
						cont = 0
						break
					if tag == None:
						# ignore whitespace
						pos_corr = pos_corr + 1
						continue
					if tag != rule.pattern[pattern_pos]:
						rule_match = 0
						break
					if match_start == None:
						match_start = tagged_text_pos

					pattern_pos = pattern_pos + 1
					if pattern_pos == len(rule.pattern):
						#print "match (%s)! tagged_text_pos=%d" % (rule.name, tagged_text_pos)
						match_end = match_start + pattern_pos + pos_corr - 1
						l.append((match_start, match_end, rule.name))
						tagged_text_pos = tagged_text_pos + (match_end - match_start)
						cont = 0
						break
				if not rule_match:
					continue		# next rule
				if not cont:
					break			# next word
			tagged_text_pos = tagged_text_pos + 1
				
		#print l
		return l

class Rules:
	"""A container for chunking rules."""

	chunk_rules = os.path.join(sys.path[0], "data", "chunks.txt")
	
	def __init__(self):
		"""Read the chunking rules from data/chunks.txt. The rules
		can then be access via Rules.rules."""
		self.rules = []
		f = open(self.chunk_rules)
		lines = f.readlines()
		f.close()
		for line in lines:
			if line.startswith("#"):	# ignore comments
				continue
			rule = Rule(line.strip())
			self.rules.append(rule)
		return

class Rule:
	"""A chunking rule, consisting of a name and a pattern. The
	pattern is a list of POS tags."""

	def __init__(self, line):
		"""Parse a chunk rule in this format:
		name: tag1 tag2..."""
		parts = re.split("\s+", line.strip())
		name = parts[0]
		self.name = name[0:len(name)-1]		# cut off colon
		self.pattern = parts[1:]
		return
