#!/usr/bin/python
# -*- coding: iso-8859-1 -*-
#
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

import re

class Entities:
	"""Some(!) BNC SGML entities."""

	def cleanEntities(s):
		"""Replace only the most common BNC entities with their
		ASCII respresentation."""
		entities = { 	"amp" : "&",
						"pound": "P",		# fixme: use "£"
						"eacute": "e",
						"aacute": "a",
						"bquo": "\"",
						"equo": "\"",
						"ecirc": "e",
						"quot": "'",
						#"deg": u"°",
						"dollar": "$",
						"egrave": "e",
						"percnt": "&",
						"ndash": "-",
						"mdash": "--",
						"hellip": "...",
						"lsqb": "[",
						"rsqb": "]",
						"uuml": "u",	#fixme: use ü
						"auml": "a",	# see above!
						"ouml": "o",
						"Uuml": "U",
						"Auml": "A",
						"Ouml": "O"
					}
		try:
			for key in entities:
				#s = re.compile("&%s;?" % key).sub("%s" % entities[key].encode('latin1'), s)
				s = s.replace("&%s;" % key, entities[key])
				s = s.replace("&%s" % key, entities[key])
		except TypeError:
			# FIXME: what to do here?!
			print >> sys.stderr, "TypeError: '%s'" % s
		return s

	cleanEntities = staticmethod(cleanEntities)

if __name__ == "__main__":
	main()
