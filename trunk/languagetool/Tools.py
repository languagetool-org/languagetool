# Tools class
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

import sys
import re

class Tools:

	def __init__(self):
		return
	
	def getXML(node, xmlstr=""):
		"""Get the XML content of a node, but only elements and text."""
		if node and node.nodeType == node.ELEMENT_NODE:
			#print "*%s" % node.tagName
			#xmlstr = xmlstr + "<>"
			l = []
			for child in node.childNodes:
				l.append(Tools.getXML(child, xmlstr))
			xmlstr = "<%s>%s</%s>" % (node.tagName, str.join('', l), node.tagName)
		elif node and node.nodeType == node.TEXT_NODE:
			xmlstr = "%s%s" % (xmlstr, node.data)
		return xmlstr

	getXML = staticmethod(getXML)
