# -*- coding: iso-8859-1 -*-
# Rule that checks the use of 'a' vs. 'an'
# (c) 2003 Daniel Naber <daniel.naber@t-online.de>
#
#$rcs = ' $Id: huAvsAnRule.py,v 1.7 2004-08-30 20:15:03 tyuk Exp $ ' ;
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
import sys

sys.path.append("..")
import Rules
import Tools

class huAvsAnRule(Rules.Rule):
	"""Check if the determiner (if any) before a word is:
	-'an' if the next word starts with a vowel
	-'a' if the next word does not start with a vowel
	This rule knows about some exceptions (e.g. 'an hour')."""

	#requires_a_file = os.path.join("data", "det_a.txt")
	#requires_an_file = os.path.join("data", "det_an.txt")

	def __init__(self):
		Rules.Rule.__init__(self, "DET", "'a' es 'az' hasznalata.", 0, None)
		self.requires_a = {}
		self.requires_an = {}
		#self.requires_a = self.loadWords(self.requires_a_file)
		#self.requires_an = self.loadWords(self.requires_an_file)
		return

#	def loadWords(self, filename):
#		f = open(filename)
#		l = []
#		while 1:
#			line = f.readline()
#			if not line:
#				break
#			if line.startswith("#") or line.strip() == '':
#				continue
#			l.append(line.strip().lower())
#		f.close()
#		return l

	def IsRoman(self, word):
		i = 0
		while i < len(word):
			if not word[i] in ('I','V','X','L','C','D','M'):
				return 0
			i = i + 1
		return 1 

	def match(self, tagged_words, chunks, position_fix=0, line_fix=0, column_fix=0):
		# fixme: use line_fix and column_fix
		matches = []
		text_length = 0
		line_breaks = 0
		column = 0
		i = 0
		#print tagged_words
		while 1:
			if i >= len(tagged_words)-2:
				break
			org_word = tagged_words[i][0]
			org_word_next = tagged_words[i+2][0]
			line_breaks_cur = Tools.Tools.countLinebreaks(org_word)
			if line_breaks_cur > 0:
				column = 0
			line_breaks = line_breaks + line_breaks_cur
			#print "<tt>'%s' -- '%s'</tt><br>" % (org_word, org_word_next)
			if org_word.lower() == 'a':
				err = 0
				if org_word_next.lower() in self.requires_an:
					err = 1
#				org_word_next[0].lower() in ('a', 'e', 'i', 'o', 'u','á','é','ö','ü','í','û','õ') and \
				elif len(org_word_next) > 0 and \
				org_word_next[0].lower() in ('a', 'e', 'i', 'o', 'u',u'á',u'é',u'ö',u'ü',u'í',u'û',u'õ',u'ú') and \
					not org_word_next.lower() in self.requires_a and \
					 not self.IsRoman(org_word_next.upper()):
					err = 1
				if err:
					matches.append(Rules.RuleMatch(self.rule_id,
						text_length+position_fix, text_length+len(org_word)+position_fix,
						line_breaks+line_fix, column,
						"<message>Hasznaljon <em>az-t a</em> helyett "+
						"ha a kovetkezo szo maganhangzoval kezdodik</message>", org_word))
#			elif org_word.lower() == 'az':
#				err = 0
#				if org_word_next.lower() in self.requires_a:
#					err = 1
#				elif len(org_word_next) > 0 and \
#					(not org_word_next[0].lower() in ('a', 'e', 'i', 'o', 'u',u'á',u'é',u'ö',u'ü',u'í',u'û',u'õ',u'ú','1')) and \
#					not org_word_next.lower() in self.requires_an:
#					err = 1
#				if err:
#					matches.append(Rules.RuleMatch(self.rule_id,
#						text_length++position_fix, text_length+len(org_word)+position_fix,
#						"Hasznaljon <em>a-t az</em> helyett "+
#						"ha a kovetkezo szo massalhangzoval kezdodik", org_word))
#				pass
			text_length = text_length + len(org_word)
			i = i + 1
		return matches
