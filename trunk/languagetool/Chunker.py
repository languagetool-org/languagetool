# Assign chunks to a tagged text
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

import os
import re

class Chunker:
	"""fixme-doc"""

	def __init__(self):
		"""fixme-doc"""
		return

	def __getPattern(self, pattern):
		repeat = 0
		if pattern.endswith("+"):
			# cut off '+':
			pattern = pattern[0:len(pattern)-1]
			repeat = 1
		return (repeat, pattern)

	def chunk(self, tagged_text):
		"""fixme-doc"""
		l = []
		
		#example:
		# tagged_text:   XX AT NN1 NN1 VB
		# rule.pattern:     AT NN1+
		rules = Rules()
		for rule in rules.rules:		# TODO:other way round is more efficient
										# as only one tag is possible for each range?
			match_start = None
			match_end = None
			i = 0
			
			#FIXME: ignore whitespace!
			
			for word, norm_word, tag in tagged_text:
				pattern_pos = 0
				print "%s, %s ?= %s" % (word, tag, rule.pattern[pattern_pos])
				while tag == rule.pattern[pattern_pos]:
					if not match_start:
						match_start = i

					(pattern_repeat, pattern) = self.__getPattern(rule.pattern[pattern_pos])

					#FIXME: support regex like repeat operator '+':
					if pattern_repeat:					
						print "Repeat '%s -- %s'!++++" % (tag, pattern)
						while tag == pattern:
							print "~"
							try:
								tag = rule.pattern[pattern_pos]
							except IndexError:
								print "end pattern"

					pattern_pos = pattern_pos + 1
					#try:
					#	tag = tagged_text[i]
					#except IndexError:
					if pattern_pos == len(rule.pattern)-1:
						print "match!"
						match_end = i+1
						l.append((match_start+1, match_end+1, rule.name))
						break
					i = i + 1
		print l	####
		return l

class Rules:

	chunk_rules = os.path.join("data", "chunks.txt")
	
	def __init__(self):
		"""fixme-doc"""
		self.rules = []
		f = open(self.chunk_rules)
		lines = f.readlines()
		f.close()
		for line in lines:
			rule = Rule(line)
			self.rules.append(rule)
		return

class Rule:

	def __init__(self, line):
		"""Parse a chunk rule in this format:
		name: tag1 tag2..."""
		parts = re.split("\s+", line.strip())
		name = parts[0]
		self.name = name[0:len(name)-1]	# cut off colon
		self.pattern = parts[1:]
		return
