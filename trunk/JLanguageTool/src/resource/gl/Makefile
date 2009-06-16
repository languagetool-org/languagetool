export LOCALE=POSIX
all: compile
compile:
	perl /usr/local/bin/morph_data.pl < lexicon.txt | sort -u | fsa_build -O -o galician.dict
test: compile
	@echo "Introduza palabras que desexe testar: "
	@echo -n ">> "
	@fsa_morph -d galician.dict
