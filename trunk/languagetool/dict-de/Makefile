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
	./bin/build_neu.sh

./ergebnis/deutsch.dic:
	./bin/build_comb.sh
	
./worte/geordnet:
	./bin/clean_worte.sh

./woerter/auscvs:
	./bin/from_cvs.sh

# neu machen
neu: ./ergebnis/deutschneu.dic

# comb machen
comb: ./ergebnis/deutsch.dic

# worte ordnen
ordne: ./worte/geordnet

# worte cvs
cvs: ./woerter/auscvs

#putzen
clean:
	rm -rf ./ergebnis/*

# alles machen 
all: cvs ordne neu comb
