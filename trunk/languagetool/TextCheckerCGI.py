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

import cgi
import string
import sys
import os

os.chdir(sys.path[0])
sys.path.append(sys.path[0])
sys.path.append(os.path.join(sys.path[0], "src"))

import TagInfo
import TextChecker

# FIXME: for debugging only
import cgitb
cgitb.enable()

def main():
	form = cgi.FieldStorage()
	if form.getvalue("text"):
		check(form)
	elif form.getvalue("explain"):
		displayExplanation(form)
	else:
		displayForm(form)
	return	

def displayExplanation(form):
	print "Content-Type: text/html\n\n"
	tag = cgi.escape(form.getvalue("explain"))	# security: anti XSS
	taginfo = TagInfo.TagInfo(form.getvalue("lang"))
	print """<html><head>
		<title>LanguageTool: Tag explanation for %s</title>
		</head>
		<body>
		<h1>Tag explanation for %s</h1>

		<p><strong>%s:</strong> %s</p>
		
		</body></html>""" % (tag, tag, tag, taginfo.getExp(form.getvalue("explain")))
	return

def displayForm(form):
	print "Content-Type: text/html\n\n"
	print """<html><head>
		<title>LanguageTool Web Interface</title>
		<style rel='stylesheet'>
			<!--
			.error { font-weight: bold; color: red; }
			-->
		</style>
		</head>
		<body>
		<h1>LanguageTool Web Interface</h1>
		Enter text here:<br />
		<form action="/cgi-bin/languagetool/TextCheckerCGI.py" method="post">
			<textarea name="text" rows="8" cols="80"></textarea><br />
			<select name="lang">
				<option value="en">English</option>
				<option value="de">German</option>
				<option value="hu">Hungarian (experimental)</option>
			</select><br />
			<input type="checkbox" name="tags" checked="checked"> Show part-of-speech tags
			<input type="checkbox" name="german_ff" checked="checked"> Check for false friends (for German native speakers)<br />
			<input type="checkbox" name="style"> Check for some style issues (e.g. <em>don't</em> instead of <em>do not</em>)<br />
			<input type="checkbox" name="sentencelength"> Complain about long sentences (more than 30 words)
			<br />
			<input type="submit" value="Check Text">
		</form>
		<p><strong>Suggested test text</strong><br>
		The incorrect words are <span class="error">red</span> (copy and paste 
		into text field to try it):</p>

		<p>Then he <span class="error">look</span> at the building.<br>
		I definitely think <span class="error">is</span> should be less than four years.<br>
		This allows to provide a powerful <span class="error">a</span> help system.<br>
		His house is as big <span class="error">like</span> mine.<br>
		His car is larger <span class="error">then</span> mine.
		</p>
		<hr>
		<a href="http://www.danielnaber.de/languagetool/">LanguageTool Homepage</a>
		</body></html>"""
	return
	
def check(form):
	print "Content-Type: text/html\n\n"
	text = form.getvalue("text").decode('latin1')
	if not text:
		text = ""
	# TODO: put options for alle these in the web page? too confusing...
	grammar = None
	falsefriends = None
	words = None
	builtin = None
	textlanguage = "en"
	if not form.getvalue("style"):
		words = ["__NONE"]
	mothertongue = "de"
	if not form.getvalue("german_ff"):
		mothertongue = None
	if form.getvalue("lang"):
		textlanguage = form.getvalue("lang")
	max_sentence_length = 0
	if form.getvalue("sentencelength"):
		max_sentence_length = None
	checker = TextChecker.TextChecker(grammar, falsefriends, words, builtin, \
		textlanguage, mothertongue, max_sentence_length)

	print """<html><head>
		<title>Check result</title>
		<style rel='stylesheet'>
			<!--
			.tag { color: grey; }
			.error { font-weight: bold; color: red; }
			.expl { color: blue; }
			.repl { font-weight: bold; }
			-->
			</style>
		<script language="javascript">
			<!--
			var data = new Array();
			"""
	taginfo = TagInfo.TagInfo(textlanguage)
	print taginfo.getJavascriptCode()
	print """
			function info(s) {	
				alert(s + ": " + data[s]);
			}
			//-->
		</script>
		</head>
		<body>
		<h1>Result</h1>"""

	(rule_matches, res, tags) = checker.check(text)
	# TODO: add an option to print the complete checker XML response:
	#print "XML reply:<br><pre>%s</pre>" % cgi.escape(res)
	#print "<pre>%s</pre>" % tags
	taglist = []
	char_count = 0
	list_count = 0
	text_list = []
	for tag_triple in tags:
		tag_str = ""
		if form.getvalue("tags") and tag_triple[2]:
			w = tag_triple[2]
			tag_str = '<span class="tag"><a href="TextCheckerCGI.py?explain=%s&amp;lang=%s" \
				onclick="info(\'%s\');return false;">[%s]</a></span>' \
				% (w, textlanguage, w, w)
			if tag_triple[2] == 'SENT_END':
				tag_str = '%s<br>\n' % tag_str
		word = cgi.escape(tag_triple[0])
		text_list.append(word)
		text_list.append(tag_str)
		char_count = char_count + len(word)
	# guarantee that the rule_matches are ordered by their position:
	rule_matches.sort()
	rule_matches.reverse()	# add messages from end of list to avoid count confusion
	for rule_match in rule_matches:
		# TODO: this produces invalid code if the rule ranges are nested!
		ct = 0
		i = 0
		start_found = 0
		end_found = 0
		for el in text_list:
			if not el.startswith("<span"):
				from_pos = ct
				to_pos = ct + len(el)
				if rule_match.to_pos <= to_pos and rule_match.to_pos >= from_pos and not end_found:
					text_list[i] = '%s</span><span class="expl">[%s (%s)]</span>' % (text_list[i], \
						rule_match.message, rule_match.id)
					end_found = 1
				elif rule_match.from_pos <= to_pos and rule_match.from_pos >= from_pos and not start_found:
					text_list[i] = '<span class="error">%s' % text_list[i]
					start_found = 1
				ct = ct + len(el)
			if end_found and start_found:
				break
			i = i + 1
	text = string.join(text_list, '')
	print text.encode('latin1')

	if len(rule_matches) == 1:
		print "<p>%d possible error found.</p>" % len(rule_matches)
	else:
		print "<p>%d possible errors found.</p>" % len(rule_matches)
	
	#print "<p>" + cgi.escape(res)
	print "</body></html>"

main()
