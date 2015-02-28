#!/usr/bin/perl -w
#
# This script transforms the lexicon from Dicollecte.org into 
# a suitable tagged dictionary for LanguageTool.
#
# For example, running 'dicollecte-to-lt.pl lexique-dicollecte-fr-v5.0.2.txt'
# creates lexique-dicollecte-fr-v5.0.2.txt.LT.txt, where
# lexique-dicollecte-fr-v5.0.2.txt can be found in 
# http://www.dicollecte.org/download/fr/lexique-dicollecte-fr-v5.0.2.zip
#
# Author: Dominique Pellé <dominique.pelle@gmail.com>
use strict;

my $in  = $ARGV[0];
my $out = "$in.LT.txt";
my $err = "$in.LT.err";

open(IN,  "< $in")  or die "can't open $in: $!\n";
open(OUT, "> $out") or die "can't open $out: $!\n";
open(ERR, "> $err") or die "can't open $err$!\n";

my %N_J_Z  = ('nom' => 'N', 'adj' => 'J', 'prn' => 'Z', 'geo' => 'Z', 'patr' => 'Z', 'npr' => 'Z');
my %m_f_e  = ('mas' => 'm', 'fem' => 'f', 'epi' => 'e');
my %s_p_sp = ('sg'  => 's', 'pl'  => 'p', 'inv' => 'sp');
my %tense  = ('infi' => 'inf',
              'ppre' => 'ppr',
              'ppas' => 'ppa',
              'ipre' => 'ind pres',
              'ipre' => 'ind pres',
              'spre' => 'sub pres',
              'iimp' => 'ind impa',
              'simp' => 'sub impa',
              'simp' => 'sub impa',
              'ifut' => 'ind futu',
              'cond' => 'con pres',
              'impe' => 'imp pres',
              'ipsi' => 'ind psim');
my %pers   = ('1sg'  => '1 s', '2sg' => '2 s', '3sg' => '3 s', '1isg' => '1 s',
              '1pl'  => '1 p', '2pl' => '2 p', '3pl' => '3 p', '3pl!' => '3 p');

while (<DATA>) { print OUT }
while (<IN>) {
  my @col = split "\t";
  next unless ($#col >= 2);
  my ($flex, $lemma, $tag) = @col[1, 2, 3];

  my $aux = '';
     $aux = ' avoir' if ($lemma eq 'avoir');
     $aux = ' etre'  if ($lemma eq 'être');

  if ($tag =~ /^((?:(?:nom|adj|prn|geo|patr|npr) )+)(mas|fem|epi) (sg|pl|inv)$/) { 
    for my $a (split(' ', $1)) {
      printf OUT "$flex\t$lemma\t%s %s %s\n", $N_J_Z{$a}, $m_f_e{$2}, $s_p_sp{$3};
    }
  } elsif ($tag =~ /^(?:adv|loc\.(?:adv|adj|verb|nom))$/) {
    printf OUT "$flex\t$lemma\tA\n";
  } elsif ($tag eq 'nom mas') {
    print OUT "$flex\t$lemma\tN m s\n";
  } elsif ($tag eq 'nom fem') {
    print OUT "$flex\t$lemma\tN f s\n";
  } elsif ($tag eq 'interj') {
    printf OUT "$flex\t$lemma\tI\n";
  } elsif ($tag eq 'nb epi sg') {
    printf OUT "$flex\t$lemma\tK\n";
    printf OUT "$flex\t$lemma\tD e s\n";
  } elsif ($tag eq 'nb epi pl') {
    printf OUT "$flex\t$lemma\tK\n";
    printf OUT "$flex\t$lemma\tD e p\n";
  } elsif ($tag =~ 'v\S* ppas (?:1jsg )?(mas|fem|epi) (sg|pl|inv)$') {
    printf OUT "$flex\t$lemma\tV%s ppa %s %s\n", $aux, $m_f_e{$1}, $s_p_sp{$2};
  } elsif ($tag =~ 'v\S* ppas adj (?:1jsg )?(mas|fem|epi) (sg|pl|inv)$') {
    printf OUT "$flex\t$lemma\tV%s ppa %s %s\n", $aux, $m_f_e{$1}, $s_p_sp{$2};
    printf OUT "$flex\t$lemma\tJ %s %s\n",             $m_f_e{$1}, $s_p_sp{$2};
  } elsif ($tag =~ /^v\S* (infi|ppre)$/) {
    printf OUT "$flex\t$lemma\tV%s %s\n", $aux, $tense{$1};
  } elsif ($tag =~ /^v\S*((?: (?:[is]pre|[is]imp|impe|ifut|cond|ipsi|ppas|ppre|infi))+)((?: [123](?:sg|pl!?|isg))+)$/) {
    for my $a (split(' ', $1)) { for my $b (split(' ', $2)) {
      printf OUT "$flex\t$lemma\tV%s %s %s\n", $aux, $tense{$a}, $pers{$b};
    } }
  } else {
    print ERR "unhandled tag [$tag] flexion=[$flex]\n";
  }
}
__DATA__
.	.	M fin
!	!	M fin excl
?	?	M fin inte
,	,	M nonfin
;	;	M nonfin
:	:	M nonfin
aussi	aussi	C
cependant	cependant	C
comment	comment	C
jusqu	jusqu	C
jusque	jusque	C
partant	partant	C
pourquoi	pourquoi	C
s	s	C
sinon	sinon	C
soirs	soirs	C
versus	versus	C
car	car	C coor
donc	donc	C coor
et	et	C coor
mais	mais	C coor
ni	ni	C coor
or	or	C coor
ou	ou	C coor
comme	comme	C sub
lorsqu	lorsque	C sub
lorsque	lorsque	C sub
puisqu	puisque	C sub
puisque	puisque	C sub
qu	qu	C sub
quand	quand	C sub
que	que	C sub
quoique	quoique	C sub
si	si	C sub
soit	soit	C sub
au	au	D m s
aucun	aucun	D m s
aucune	aucun	D f s
aucunes	aucuns	D f p
aucuns	aucuns	D m p
autre	autre	D e s
autres	autre	D e p
aux	au	D e p
ce	ce	D m s
certain	certain	D m s
certaine	certain	D f s
certaines	certains	D f p
certains	certains	D m p
ces	ce	D e p
cet	ce	D m s
cette	ce	D f s
chacun	chacun	D m s
chacune	chacun	D f s
chaque	chaque	D e s
des	du	D e p
différentes	différents	D f p
différents	différents	D m p
divers	divers	D m p
diverses	divers	D f p
du	du	D m s
force	force	D e p
force	force	D e s
l	le	D e s
la	le	D f s
laquelle	lequel	D f s
le	le	D m s
lequel	lequel	D m s
les	le	D e p
lesquelles	lequel	D f p
lesquels	lequel	D m p
leur	leur	D e s
leurs	leur	D e p
même	même	D e s
mêmes	même	D e p
ma	mon	D f s
maint	maint	D m s
mainte	maint	D f s
maintes	maint	D f p
maints	maint	D m p
mes	mon	D e p
mieux	mieux	D e p
mieux	mieux	D e s
moins	moins	D e p
moins	moins	D e s
mon	mon	D m s
nos	notre	D e p
notre	notre	D e s
nul	nul	D m s
nulle	nul	D f s
nulles	nul	D f p
nuls	nul	D m p
plus	plus	D e p
plus	plus	D e s
plusieurs	plusieurs	D e p
quel	quel	D m s
quelle	quel	D f s
quelles	quel	D f p
quelqu	quelque	D e s
quelque	quelque	D e s
quelques	quelques	D e p
quels	quel	D m p
sa	son	D f s
ses	son	D e p
seul	seul	D m s
seule	seul	D f s
son	son	D m s
ta	ton	D f s
tel	tel	D m s
telle	tel	D f s
telles	tel	D f p
tels	tel	D m p
tes	ton	D e p
ton	ton	D m s
tous	tout	D m p
tout	tout	D m s
toute	tout	D f s
toutes	tout	D f p
un	un	D m s
une	un	D f s
unes	un	D f p
uns	un	D m p
vos	votre	D e p
votre	votre	D e s
à	à	P
après	après	P
attendu	attendu	P
autour	autour	P
avant	avant	P
avec	avec	P
chez	chez	P
concernant	concernant	P
contre	contre	P
côté	côté	P
d	d	P
dans	dans	P
de	de	P
dedans	dedans	P
dehors	dehors	P
depuis	depuis	P
derrière	derrière	P
dès	dès	P
dessous	dessous	P
dessus	dessus	P
devant	devant	P
devers	devers	P
durant	durant	P
en	en	P
endéans	endéans	P
entre	entre	P
envers	envers	P
ès	ès	P
excepté	excepté	P
fors	fors	P
hormis	hormis	P
hors	hors	P
jouxte	jouxte	P
jusqu	jusqu	P
jusque	jusque	P
jusques	jusques	P
lès	lès	P
lez	lez	P
malgré	malgré	P
moyennant	moyennant	P
nonobstant	nonobstant	P
ôté	ôté	P
outre	outre	P
par	par	P
par-devers	par-devers	P
parmi	parmi	P
passé	passé	P
pendant	pendant	P
plein	plein	P
pour	pour	P
près	près	P
proche	proche	P
sans	sans	P
sauf	sauf	P
selon	selon	P
sous	sous	P
suivant	suivant	P
sur	sur	P
touchant	touchant	P
vers	vers	P
versus	versus	P
via	via	P
voici	voici	P
vu	vu	P
ça	ça	R dem m s
aucun	aucun	R m s
aucune	aucun	R f s
auquel	auquel	R rel m s
auxquelles	auquel	R rel f p
auxquels	auquel	R rel m p
c	c	R dem e s
ce	ce	R dem e s
ceci	ceci	R dem e s
cela	cela	R dem m s
celle	celui	R dem f s
celle-ci	celle-ci	R dem f s
celle-là	celle-là	R dem f s
celles	celui	R dem f p
celles-ci	celle-ci	R dem f p
celles-là	celles-là	R dem f p
celui	celui	R dem m s
celui-ci	celui-ci	R dem m s
celui-là	celui-là	R dem m s
certaines	certains	R f p
certains	certains	R m p
ceux	celui	R dem m p
ceux-ci	ceux-ci	R dem m p
ceux-là	ceux-là	R dem m p
chacun	chacun	R m s
chacune	chacun	R f s
desquelles	duquel	R rel f p
desquels	duquel	R rel m p
dont	dont	R rel e sp
duquel	duquel	R rel m s
elle	elle	R pers suj 3 f s
elle-même	elle-même	R refl 3 f s
elles	elles	R pers suj 3 f p
elles-mêmes	elles-mêmes	R refl 3 f p
en	en	R pers obj e sp
eux	eux	R pers obj 3 m p
eux-mêmes	eux-mêmes	R refl 3 m p
icelle	icelui	R f s
icelles	icelui	R f p
icelui	icelui	R m s
iceux	icelui	R m p
il	il	R pers suj 3 m s
ils	ils	R pers suj 3 m p
j	je	R pers suj 1 s
je	je	R pers suj 1 s
l	l	R pers obj 3 s
l'on	l'on	R pers suj 3 e s
la	la	R pers obj 3 f s
laquelle	lequel	R rel f s
le	le	R pers obj 3 m s
lequel	lequel	R rel m s
les	les	R pers obj 3 p
lesquelles	lequel	R rel f p
lesquels	lequel	R rel m p
leur	leur	R e s
leur	leur	R pers obj 3 p
leurs	leur	R e p
lui	lui	R 3 s
lui	lui	R pers obj 3 m s
lui-même	lui-même	R refl 3 m s
mézigue	mézigue	R pers obj 1 s
m	m	R pers obj 1 s
me	me	R pers obj 1 s
mien	mien	R m s
mienne	mien	R f s
miennes	mien	R f p
miens	mien	R m p
moi	moi	R pers obj 1 s
moi-même	moi-même	R refl 1 e s
nôtre	nôtre	R e s
nôtres	nôtre	R e p
nous	nous	R pers obj 1 p
nous	nous	R pers suj 1 p
nous-mêmes	nous-mêmes	R refl 1 e p
nul	nul	R m s
où	où	R inte
où	où	R rel e sp
on	on	R pers suj 3 m s
personne	personne	R m s
plusieurs	plusieurs	R e p
qu	qu	R inte
qu	qu	R rel e sp
que	que	R inte
que	que	R rel e sp
quel	quel	R m s
quelle	quel	R f s
quelles	quel	R f p
quels	quel	R m p
qui	qui	R inte
qui	qui	R rel e sp
quiconque	quiconque	R m s
quoi	quoi	R inte
quoi	quoi	R rel e sp
rien	rien	R m s
sézigue	sézigue	R pers obj 3 s
s	s	R pers obj 3 sp
se	se	R pers obj 3 sp
sien	sien	R m s
sienne	sien	R f s
siennes	sien	R f p
siens	sien	R m p
soi	soi	R pers obj 3 m s
soi-même	soi-même	R refl 3 e s
t	t	R 2 s
t	t	R pers obj 2 s
te	te	R pers obj 2 s
tel	tel	R m s
telle	tel	R f s
telles	tel	R f p
tels	tel	R m p
tien	tien	R m s
tienne	tien	R f s
tiennes	tien	R f p
tiens	tien	R m p
toi	toi	R pers obj 2 s
toi-même	toi-même	R refl 2 e s
tous	tous	R m p
toutes	tous	R f p
tu	tu	R pers suj 2 s
vôtre	vôtre	R e s
vôtres	vôtre	R e p
vous	vous	R pers obj 2 p
vous	vous	R pers suj 2 p
vous-mêmes	vous-mêmes	R refl 2 e p
y	y	R pers obj e sp
av	av	S
bd	bd	S
boul	boul	S
cc	cc	S
cf	cf	S
cg	cg	S
cl	cl	S
cm	cm	S
div	div	S
dl	dl	S
dz	dz	S
éd	éd	S
env	env	S
etc	etc	S
ex	ex	S
fig	fig	S
ibid	ibid	S
id	id	S
janv	janv	S
kg	kg	S
km	km	S
max	max	S
mg	mg	S
ml	ml	S
mm	mm	S
mn	mn	S
nov	nov	S
oct	oct	S
p	p	S
pc	pc	S
préf	préf	S
rd	rd	S
réf	réf	S
suiv	suiv	S
v	v	S
vo	vo	S
vs	vs	S
beaucoup	beaucoup	A
assez	assez	A
bien	bien	A
jamais	jamais	A
très	très	A
