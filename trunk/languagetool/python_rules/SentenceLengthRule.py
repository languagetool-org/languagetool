# Rule that checks for longs entences
# (c) 2003 Daniel Naber <daniel.naber@t-online.de>
#
#$rcs = ' $Id: SentenceLengthRule.py,v 1.2 2004-03-14 15:34:08 dnaber Exp $ ' ;
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

import sys

sys.path.append("..")
import Rules

class SentenceLengthRule(Rules.Rule):
	"""Check if a sentence is 'too long'."""

	max_length = 0
	
	def __init__(self):
		Rules.Rule.__init__(self, "SENTENCE_LENGTH", "This sentence is too long.", 0, None)
		return

	def setMaxLength(self, max_length):
		"""Set the maximum length that's still okay. Limit 0 means no limit."""
		self.max_length = int(max_length)
		return
		
	def match(self, tagged_words, chunks=None, position_fix=0):
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
			matches.append(Rules.RuleMatch(self.rule_id, too_long_start,
				too_long_end, 
				"This sentence is %d words long, which exceeds the "
				"configured limit of %d words." % (count, self.max_length)))
		return matches

