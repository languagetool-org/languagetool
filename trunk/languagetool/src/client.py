#!/usr/bin/python
# daniel.naber@t-online.de, 2003-05-02
# This is just a test to show how a TextChecker server can be called

import socket

sentence = "A sentence bigger then a short one."

server_name = "127.0.0.1"
server_port = 50100

print "Test client for socket_server.py"
print "Connecting %s, port %d..." % (server_name, server_port)
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect(("127.0.0.1", 50100))
print "Connected."
cfg = '<config textlanguage="en" mothertongue="de" grammar="COMP_THAN" />\n'
s.sendall("%s<text>%s</text>" % (cfg, sentence))
print "Data sent, waiting for reply..."
data = ""
while 1:
	received = s.recv(1024)
	data = "%s%s" % (data, received)
	if not received:
		break
s.close()
print "Received reply:"
print data
