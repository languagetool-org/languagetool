#!/bin/sh
# extract language-specific file from Tatoeba 'sentences.csv' (http://tatoeba.org/deu/downloads)
# dnaber, 2013-10-22

grep "	eng" sentences.csv >tatoeba-en.txt
grep "	deu" sentences.csv >tatoeba-de.txt
grep "	lit" sentences.csv >tatoeba-lt.txt
grep "	mal" sentences.csv >tatoeba-ml.txt
grep "	pol" sentences.csv >tatoeba-pl.txt
grep "	por" sentences.csv >tatoeba-pt.txt
grep "	ron" sentences.csv >tatoeba-ro.txt
grep "	rus" sentences.csv >tatoeba-ru.txt
grep "	slk" sentences.csv >tatoeba-sk.txt
grep "	slv" sentences.csv >tatoeba-sl.txt 
grep "	spa" sentences.csv >tatoeba-es.txt
grep "	swe" sentences.csv >tatoeba-sv.txt
grep "	tgl" sentences.csv >tatoeba-tl.txt
grep "	ukr" sentences.csv >tatoeba-uk.txt
grep "	khm" sentences.csv >tatoeba-km.txt
grep "	jpn" sentences.csv >tatoeba-ja.txt
grep "	ita" sentences.csv >tatoeba-it.txt
grep "	ell" sentences.csv >tatoeba-el.txt
grep "	isl" sentences.csv >tatoeba-is.txt
grep "	glg" sentences.csv >tatoeba-gl.txt
grep "	fra" sentences.csv >tatoeba-fr.txt
grep "	epo" sentences.csv >tatoeba-eo.txt
grep "	nld" sentences.csv >tatoeba-nl.txt
grep "	dan" sentences.csv >tatoeba-da.txt
grep "	cat" sentences.csv >tatoeba-ca.txt
grep "	bre" sentences.csv >tatoeba-br.txt
grep "	bel" sentences.csv >tatoeba-be.txt
grep "	ast" sentences.csv >tatoeba-ast.txt
grep "	cmn" sentences.csv >tatoeba-zh.txt
