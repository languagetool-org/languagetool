#!/usr/bin/python
# daniel.naber@t-online.de, 2003-05-02
# This is just a test how a TextChecker server can be called

import socket

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect(("127.0.0.1", 50100))

print "Connected"
cfg = '<config textlanguage="en" mothertongue="de" grammar="COMP_THAN" />\n'
s.sendall("%s<text>A sentence bigger then a short one.</text>" % cfg)
print "Data sent"
data = ""
while 1:
	received = s.recv(1024)
	data = "%s%s" % (data, received)
	if not received:
		break
s.close()
print "Received:"
print data
