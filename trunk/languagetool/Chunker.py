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

class Chunker:
	"""fixme-doc"""

	def __init__(self):
		"""fixme-doc"""
		return

	def chunk(self, tagged_text):
		"""fixme-doc"""
		l = []
		i = 0
		# XX AT NN1 NN1 VB
		#    AT NN1+
		rules = Rules()
		for rule in rules.rules:		# TODO:other way round is more efficient
										# as only one tag is possible for each range?
			match_start = None
			match_end = None
			for word, norm_word, tag in tagged_text:
				print "%s, %s" % (word, tag)
				pattern_pos = 0
				while tag == rule.pattern[pattern_pos]:
					if not match_start:
						match_start = i
					#if rules[pattern_pos].endswith("+"):
					#	while...:
					pattern_pos = pattern_pos + 1
					try:
						tag = rule.pattern[pattern_pos]
					except IndexError:
					#if pattern_pos == len(self.rules):
						print "match!"
						match_end = i+1
						l.append((match_start, match_end, rule.name))
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
			self.rules.append(r)
		return

class Rule:

	def __init__(self, line):
		"""Parse a chunk rule in this format:
		name: tag1 tag2..."""
		parts = re.split("\s+", line.strip())
		self.name = parts[0]
		self.pattern = parts[1:]
		return
