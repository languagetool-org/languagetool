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

import unittest
import Chunker

class ChunkerTest(unittest.TestCase):

	def testChunk(self):
		c = Chunker.Chunker()

		tagged_text = [('Blah','Blah','XX'),
			('the', 'the', 'AT0'),
			('house', 'house', 'NN1'),
			('foo', 'foo', 'YY')]
		chunks = c.chunk(tagged_text)
		self.assertEqual(chunks, [(1, 2, 'NP')])

		tagged_text = [('Blah','Blah','XX'),
			('house', 'house', 'NN1'),
			('foo', 'foo', 'YY')]
		chunks = c.chunk(tagged_text)
		self.assertEqual(chunks, [])

		tagged_text = [('Blah','Blah','XX'),
			('the', 'the', 'AT0'),
			('summer', 'summer', 'NN1'),
			('house', 'house', 'NN1'),
			('foo', 'foo', 'YY')]
		chunks = c.chunk(tagged_text)
		self.assertEqual(chunks, [(1, 3, 'NP')])

		return
			
if __name__ == "__main__":
    unittest.main()
