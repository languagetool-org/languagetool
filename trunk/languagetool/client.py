#!/usr/bin/python
# daniel.naber@t-online.de, 2003-05-02
# This is just a test how a TextChecker server can be called

# TODO?: add options as XML attributes to <text>

import socket

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect(("127.0.0.1", 50100))

print "Connected"
s.sendall("<text>A sentence bigger then a short one.</text>")
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
