#
#  Makefile für deutsche Wortsammlung in Oo
#
#  make :      erzeuge deutsch.dic und deutschneu.dic in ergebnis
#  make all:   erzeuge deutsch.dic und deutschneu.dic in ergebnis
#  make neu:   erzeuge deutschneu.dic in ergebnis
#  make comb:  erzeuge deutsch.dic  in ergebnis
#  make clean: lösche alles in pfad ergebnis
#  make ordne: sortiere und ordne worte in pfad worte
#  make cvs:   kopiere worte von cvs vom woerter nach worte
#

default: all

./ergebnis/deutschneu.dic:
	sh ./bin/build_neu.sh

./ergebnis/deutsch.dic:
	sh ./bin/build_comb.sh
	
./worte/geordnet:
	sh ./bin/clean_worte.sh

./woerter/auscvs:
	sh ./bin/from_cvs.sh

./woerter/auscvsdaniel:
	sh ./bin/from_cvs_daniel.sh
	
./affix/affchk:
	awk -f ./awk/checkaffix.awk ./affix/deutsch.aff	

# neu machen
neu: ./ergebnis/deutschneu.dic

# comb machen
comb: ./ergebnis/deutsch.dic

# worte ordnen
ordne: ./worte/geordnet

# worte cvs
cvs: ./woerter/auscvs

# worte cvs ohne selten
cvsdaniel: ./woerter/auscvsdaniel

# affix check
affchk: ./affix/affchk

#putzen
clean:
	rm -rf ./ergebnis/*

# alles machen 
all: cvs ordne neu comb affchk

daniel: cvsdaniel ordne neu comb affchk
