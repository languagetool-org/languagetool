#
#  Makefile für deutsche Wortsammlung in Oo
#
#  make :      erzeuge deutsch.dic und deutschneu.dic in ergebnis
#  make all:   erzeuge deutsch.dic und deutschneu.dic in ergebnis
#  make neu:   erzeuge deutschneu.dic in ergebnis
#  make comb:  erzeuge deutsch.dic  in ergebnis
#  make clean: lösche alles in pfad ergebnis
#  make ordne: sortiere und ordne worte in pfad worte
#

default: all

./ergebnis/deutschneu.dic:
	./bin/build_neu.sh

./ergebnis/deutsch.dic:
	./bin/build_comb.sh
	
./worte/geordnet:
	./bin/clean_worte.sh

# neu machen
neu: ./ergebnis/deutschneu.dic

# comb machen
comb: ./ergebnis/deutsch.dic

# worte ordnen
ordne: ./worte/geordnet

#putzen
clean:
	rm -rf ./ergebnis/*

# alles machen 
all: neu comb
