# -*- coding: iso-8859-1 -*-
# Tools class
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

import sys
import re

class Tools:

	def __init__(self):
		return
	
	def getXML(node, xmlstr=""):
		"""Get the XML content of a node, but only elements and text."""
		if node and node.nodeType == node.ELEMENT_NODE:
			l = []
			for child in node.childNodes:
				l.append(Tools.getXML(child, xmlstr))
			xmlstr = "<%s>%s</%s>" % (node.tagName, str.join('', l), node.tagName)
		elif node and node.nodeType == node.TEXT_NODE:
			xmlstr = "%s%s" % (xmlstr, node.data)
		return xmlstr

	getXML = staticmethod(getXML)

	def countLinebreaks(s):
		matches = re.findall("[\n\r]", s)
		#print "#%s -> %s" % (s, len(matches))
		return len(matches)

	countLinebreaks = staticmethod(countLinebreaks)

	def getLanguageName(shortName):
		if shortName == 'en':
			return 'English'
		elif shortName == 'de':
			return 'German'
		elif shortName == 'hu':
			return 'Hungarian'
		return None
		
	getLanguageName = staticmethod(getLanguageName)
