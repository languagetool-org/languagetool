#!/usr/bin/perl -w
#
#   Should be called from within CreateDictFromLexiqueWithLT-4.5.sh shell script
#
# This is a data formatting script preparatory to dictionnary compilation
#
#
# Author: Dominique Pellé <dominique.pelle@gmail.com>
use strict;

#my $FileName = "lexique-dicollecte-fr-v6.4";


my $FileName  = $ARGV[0];
my $in = "$FileName.maigre.txt";
my $out = "$FileName.maigre.LT.txt";
my $err = "$FileName.maigre.LT.err";

open(IN,  "< $in")  or die "can't open $in: $!\n";
open(OUT, "> $out") or die "can't open $out: $!\n";
open(ERR, "> $err") or die "can't open $err$!\n";

my %N_J_Z  = ('nom' => 'N', 'adj' => 'J', 'prn' => 'Z', 'geo' => 'Z', 'patr' => 'Z', 'npr' => 'Z', 'det' => 'D', 'detpos' => 'D', 'detdem' => 'D', 'detneg' => 'D', 'detind' => 'D', 'titr' => 'T', 'cjsub' => 'C sub', 'cjco' => 'C coor', 'cj' => 'C', 'prorel' => 'R rel', 'proint' => 'R inte', 'adv' => 'A', 'prep' => 'P', 'properobj' => 'R pers obj', 'propersuj' => 'R pers suj', 'prodem' => 'R dem', 'proneg' => 'R', 'proind' => 'R', 'advint' => 'A inte' );
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
              '1pl'  => '1 p', '2pl' => '2 p', '3pl' => '3 p', '3pl!' => '3 p',
              '1pe' => '1', '2pe' => '2', '3pe' => '3'  );

while (<DATA>) { print OUT }
while (<IN>) {
  my @col = split "\t";
next unless ($#col >= 1);
  my ($flex, $lemma, $tag) = @col[0, 1, 2];

  my $aux = '';
     $aux = ' avoir' if ($lemma eq 'avoir');
     $aux = ' etre'  if ($lemma eq 'être');

  if ($tag =~ /^((?:(?:nom|adj|prn|geo|patr|npr|det|detdem|detpos|detneg|detind|titr|properobj|prodem|proneg|proind|proint|prorel|prep) )+)(mas|fem|epi) (sg|pl|inv)$/)
    { 
my $tmp02=$2;
my $tmp03=$3;
    for my $a (split(' ', $1)) {
        if( $a =~ /^prep$/ )
            {
              printf OUT "$flex\t$lemma\t%s\n", $N_J_Z{$a};
            }
      elsif( $a =~ /^properobj$/ )
            {
              printf OUT "$flex\t$lemma\t%s 3 %s %s\n", $N_J_Z{$a}, $m_f_e{$tmp02}, $s_p_sp{$tmp03};
            }
      else
            {
              printf OUT "$flex\t$lemma\t%s %s %s\n", $N_J_Z{$a}, $m_f_e{$tmp02}, $s_p_sp{$tmp03};
            }
        }  
    }
  elsif ($tag =~ /^((?:(?:propersuj|properobj) )+)(1pe|2pe|3pe) (mas|fem|epi) (sg|pl|inv)$/)
    { 
    for my $a (split(' ', $1))
            {
              printf OUT "$flex\t$lemma\t%s %s %s %s\n", $N_J_Z{$a}, $pers{$2}, $m_f_e{$3}, $s_p_sp{$4};
            }
   }
  elsif ($tag =~ /^((?:(?:cjsub|proint|prorel|adv|prep|cjco|properobj|proind|prodem|cj|advint) ?)+)$/)
    { 
    for my $b (split(' ', $1))
        {
        if( $b =~ /^prorel$/ )
            {
              printf OUT "$flex\t$lemma\t%s e sp\n", $N_J_Z{$b};
            }
      else
            {
              printf OUT "$flex\t$lemma\t%s\n", $N_J_Z{$b};
            }  
        }
   }elsif ($tag =~ /^((?:(?:adv|loc\.(?:adv|adj|verb|nom|cj)) ?)+)$/) {
    printf OUT "$flex\t$lemma\tA\n";
   }elsif ($tag =~ /^((?:(?:loc\.(?:adv|adj|verb|nom|cj|prep)) ?)+)$/) {
    printf OUT "$flex\t$lemma\tP\n";
  } elsif ($tag eq 'nom mas') {
    print OUT "$flex\t$lemma\tN m s\n";
  } elsif ($tag eq 'nom fem') {
    print OUT "$flex\t$lemma\tN f s\n";
#  } elsif ($tag eq 'cjco') {
#    print OUT "$flex\t$lemma\tC coor\n";
#  } elsif ($tag eq 'cjsub') {
#    print OUT "$flex\t$lemma\tC sub\n";
  } elsif ($tag eq 'prep') {
    print OUT "$flex\t$lemma\tP\n";
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
l	le	D e s
j	je	R pers suj 1 e s
tout	tout	R m s
toutes	tout	R f p
tous	tout	R m p
