#!/usr/bin/python
# Copyright (C) 2003 Daniel Naber <daniel.naber@t-online.de>
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

import TextChecker
import Tools

import re
import sys
import time
import unittest
import xml.dom.ext.reader.Sax2

class Error:

	def __init__(self, sentence, correctable, span_list):
		self.sentence = self.cleanup(sentence)
		self.correctable = correctable
		self.span_list = span_list
		return

	def cleanup(self, xml_str):
		xml_str = re.compile("\s+", re.DOTALL).sub(" ", xml_str)
		return xml_str

	def __str__(self):
		return "%s (%s)" % (self.sentence, self.span_list)
		
class Sentence:
	
	def __init__(self, sentence, expected_error_id):
		self.sentence = sentence
		self.expected_error_id = expected_error_id
		return
	
class TextCheckerTest(unittest.TestCase):

	RULE_FILE = "rules/grammar.xml"
	ERROR_FILE = "../errors.xml"

	def loadRuleExamples(self):
		reader = xml.dom.ext.reader.Sax2.Reader()
		doc = reader.fromStream(self.RULE_FILE)
		rule_nodes = doc.getElementsByTagName("rule")
		sentences = []
		for rule_node in rule_nodes:
			expected_error_id = rule_node.getAttribute("id")
			if not expected_error_id:
				parent_node = rule_node.parentNode	# 'rulegroup'
				expected_error_id = parent_node.getAttribute("id")
			self.assert_(expected_error_id != '', "No 'id' attribute found")
			example_nodes = rule_node.getElementsByTagName("example")
			for example_node in example_nodes:
				example_type = example_node.getAttribute("type")
				xml_sentence = Tools.Tools.getXML(example_node)
				xml_sentence = re.compile("\s+").sub(" ", xml_sentence)
				s = None
				if example_type == 'correct':
					s = Sentence(xml_sentence, None)
				else:
					s = Sentence(xml_sentence, expected_error_id)
				sentences.append(s)
		return sentences

	def loadExampleSentences(self):
		reader = xml.dom.ext.reader.Sax2.Reader()
		doc = reader.fromStream(self.ERROR_FILE)
		s_nodes = doc.getElementsByTagName("s")
		errors = []
		for s_node in s_nodes:
			spans = []
			corr = s_node.getAttribute("correctable")
			#xml_str = self.getXML(s_node)
			xml_str = ""
			for child in s_node.childNodes:
				if child.nodeType == child.TEXT_NODE:
					xml_str = xml_str + child.data
				elif child.nodeType == child.ELEMENT_NODE:
					# <replace>, <remove> or <insert> may appear here,
					# more than once:
					from_pos = len(xml_str)
					xml_str = xml_str + child.childNodes[0].data
					to_pos = len(xml_str)
					spans.append((from_pos, to_pos))
			#print "***%s" % xml_str
			err = Error(xml_str, corr, spans)
			print err	###########
			errors.append(err)
		return errors

	def addMarker(self, s, from_pos, to_pos, marker):
		s = "%s%s%s" % (s[:from_pos], marker, s[from_pos:])
		s = "%s%s%s" % (s[:to_pos], marker, s[to_pos:])
		return s
		
	def display(self, tagged_text):
		l = []
		for (org, cleaned, tag) in tagged_text:
			if cleaned:
				l.append("%s/%s" % (cleaned, tag))
		return str.join(' ', l)
	
	def testRules(self):
		"""Test if the style and grammar checker can find the errors in the
		rules' examples and if it doesn't find errors in the examples which
		are correct. If something goes wrong, this only throws an assert()
		at the very end."""
		examples = self.loadRuleExamples()
		checker = TextChecker.TextChecker(grammar=None, \
			falsefriends=None, words=None, \
			textlanguage=None, mothertongue=None,
			max_sentence_length=0)
		err_count = 0
		for sentence_obj in examples:
			(rule_matches, xml_err, tagged_text) = checker.check(sentence_obj.sentence)
			tagged_clean = self.display(tagged_text)
			i = 1
			if len(rule_matches) > 1:
				errs = ""
				for rule_match in rule_matches:
					errs = errs + rule_match.id + ", "
				msg = "Error '%s' not found, found '%s' instead. Sentence:\n%s\nTagged sentence:\n%s" % \
					(sentence_obj.expected_error_id, errs, sentence_obj.sentence, tagged_clean)
				print "*** Error: %s" % msg
			if len(rule_matches) == 1:
				found_error_id = rule_matches[0].id
				msg = "Error '%s' not found, found '%s' instead. Sentence:\n%s\nTagged sentence:\n%s" % \
					(sentence_obj.expected_error_id, found_error_id, sentence_obj.sentence, tagged_clean)
				if sentence_obj.expected_error_id != found_error_id:
					print "*** Error: %s" % msg
					print
					err_count = err_count + 1
			else:
				# No error was found:
				msg = "Error '%s' not found. Sentence:\n%s\nTagged sentence:\n%s" % \
					(sentence_obj.expected_error_id, sentence_obj.sentence, tagged_clean)
				if sentence_obj.expected_error_id != None:
					print "*** Error: %s" % msg
					print
					err_count = err_count + 1
		print >> sys.stderr, "%d problems (errors not detected resp. wrong error detected)" % err_count
		####### FIXME:
		#self.assertEqual(err_count, 0)
		return

	def testExampleSentences(self):
		checker = TextChecker.TextChecker(grammar=None, \
			falsefriends=None, words=None, \
			textlanguage=None, mothertongue=None,
			max_sentence_length=0)
		errors = self.loadExampleSentences()
		stat_corr_found = 0
		stat_found = 0
		stat_not_found = 0
		for err in errors:
			(rule_match, xml_err, tagged_text) = checker.check(err.sentence)
			#print err
			#print "##"+xml_err
			# fixme? more than one error per rule?
			#print rule_match[0]
			#if rule_match:
			#	print "#%d,%d" % (rule_match[0].list_pos_from, rule_match[0].list_pos_to)
			found_err_from = re.compile('from="(\d+)"', re.DOTALL).search(xml_err)
			found_err_to = re.compile('to="(\d+)"', re.DOTALL).search(xml_err)
			error_found = None
			correctly_marked = None
			if found_err_from and found_err_to:
				found_err_from = int(found_err_from.group(1))
				found_err_to = int(found_err_to.group(1))
				error_found = 1
				#print "found error at: %d, %d" % (found_err_from, found_err_to)
				for (from_pos, to_pos) in err.span_list:
					#print "%d <= %d and %d >= %d" % (found_err_from, from_pos, found_err_to, to_pos)
					if found_err_from <= from_pos and found_err_to >= to_pos:
						# the checker marked the error's position or more,
						# that's okay:
						correctly_marked = 1
						break
			## TODO: check if a good replacement is suggested!
			if error_found and correctly_marked:
				s_disp = self.addMarker(err.sentence, found_err_from, found_err_to, '*') 
				print "Found error at right position in '%s'" % (s_disp)
				stat_corr_found = stat_corr_found + 1
			elif error_found:
				s_disp = self.addMarker(err.sentence, found_err_from, found_err_to, '*') 
				print "*** Error found at wrong position in '%s' (pos: %d-%d, expected: %s)" % \
					(s_disp, found_err_from, found_err_to, str(err.span_list))
				stat_found = stat_found + 1
			else:
				print "*** Error NOT found in '%s'" % (err.sentence)
				stat_not_found = stat_not_found + 1
		print "Correctly found: %d" % stat_corr_found
		print "Found at wrong position: %d" % stat_found
		print "Not found at all: %d" % stat_not_found
		return

if __name__ == "__main__":
    unittest.main()
