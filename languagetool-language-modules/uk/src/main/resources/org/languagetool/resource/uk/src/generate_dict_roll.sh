#!/bin/sh

[ -s tagged.main.txt ] && cp -f tagged.main.txt tagged.main.txt.bak
[ -s tagged.main.dups.txt ] && mv -f tagged.main.dups.txt tagged.main.dups.txt.bak
[ -s tagged.main.uniq.txt ] && mv -f tagged.main.uniq.txt tagged.main.uniq.txt.bak
