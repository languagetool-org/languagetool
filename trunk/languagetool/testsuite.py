#!/usr/bin/python
# Copyright (C) 2003,2004 Daniel Naber <daniel.naber@t-online.de>

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
import unittest

sys.path.append(os.path.join(sys.path[0], "src"))
import SentenceSplitterTest
import ChunkerTest
import RulesTest
import TaggerTest
import EnglishTest
import GermanTest
import HungarianTest

suite = unittest.TestSuite()
did_test = 0
if len(sys.argv) == 3 and (sys.argv[1] == "rules" or sys.argv[1] == "all") and (sys.argv[2] == "en" or sys.argv[2] == "all"):
	suite.addTest(unittest.makeSuite(EnglishTest.EnglishTestCase))
	did_test = 1
if len(sys.argv) == 3 and (sys.argv[1] == "rules" or sys.argv[1] == "all") and (sys.argv[2] == "de" or sys.argv[2] == "all"):
 	suite.addTest(unittest.makeSuite(GermanTest.GermanTestCase))
	did_test = 1
if len(sys.argv) == 3 and (sys.argv[1] == "rules" or sys.argv[1] == "all") and (sys.argv[2] == "hu" or sys.argv[2] == "all"):
	suite.addTest(unittest.makeSuite(HungarianTest.HungarianTestCase))
	did_test = 1
if (len(sys.argv) == 2 and sys.argv[1] == "programs")  or (len(sys.argv) == 3 and sys.argv[1] == "rules" and sys.argv[2] == "all"):
	suite.addTest(unittest.makeSuite(ChunkerTest.ChunkerTestCase))
	suite.addTest(unittest.makeSuite(RulesTest.RuleTestCase))
	suite.addTest(unittest.makeSuite(RulesTest.RuleMatchTestCase))
	suite.addTest(unittest.makeSuite(RulesTest.TokenTestCase))
	suite.addTest(unittest.makeSuite(SentenceSplitterTest.SentenceSplitterTestCase))
	# this one takes most time:
	suite.addTest(unittest.makeSuite(TaggerTest.TaggerTestCase))
	suite.addTest(unittest.makeSuite(EnglishTest.EnglishTestCase))
	did_test = 1
if did_test == 0:
	print "Usage: testsuite.py <programs|rules en, de, hu, or all>"
	print "example: testsuite.py programs   - do program tests"
	print "example: testsuite.py rules all  - do all tests"
	print "example: testsuite.py rules hu  -  do Hungarian rule tests"
	sys.exit()
unittest.TextTestRunner().run(suite)
