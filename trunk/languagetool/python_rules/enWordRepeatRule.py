# Rule that checks for repeated words
# (c) 2003,2004 Daniel Naber <daniel.naber@t-online.de>
#
#$rcs = ' $Id: enWordRepeatRule.py,v 1.3 2004-08-30 20:15:03 tyuk Exp $ ' ;
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

import Rules
import Tools

class enWordRepeatRule(Rules.Rule):
	"""Rule that checks if a word is repeated, even if one
	word is uppercase and the other one is lowercase."""

	punct = re.compile("([,:;]+)")		# copied from Tagger.Text()
	
	def __init__(self):
		Rules.Rule.__init__(self, "WORD_REPEAT", "A word was repeated", 0, None)
		return

	def match(self, tagged_words, chunks, position_fix=0, line_fix=0, column_fix=0):
		matches = []
		text_length = 0
		i = 0
		line_breaks = 0
		column = 0
		while 1:
			org_word = tagged_words[i][0]
			line_breaks_cur = Tools.Tools.countLinebreaks(org_word) 
			if line_breaks_cur > 0:
				column = 0
			line_breaks = line_breaks + line_breaks_cur
			if i >= len(tagged_words)-2:
				break
			org_tag = tagged_words[i][2]
			if org_tag == "CRD":
				# ignore numbers like "5,000,000"
				i = i + 1
				continue
			if org_word and self.punct.match(org_word):
				# ignore punctuation
				i = i + 1
				continue
			org_word_next = tagged_words[i+2][0]
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
			if org_word.lower() == org_word_next.lower():
				matches.append(Rules.RuleMatch(self.rule_id,
					text_length+position_fix, 
					text_length+whitespace_length+len(org_word_next)+position_fix,
					line_breaks+line_fix, column+column_fix,
					"<message>You repeated a word. Maybe you should <em>remove</em> one "+
					"of the words?</message>", org_word))
			i = i + 1
		return matches
