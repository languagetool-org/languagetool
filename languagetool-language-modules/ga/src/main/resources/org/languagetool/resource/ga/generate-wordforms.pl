#!/usr/bin/perl

use warnings;
use strict;
use utf8;

my @models = (
    qw/iC Ca iCí iCí/,
    qw/C iCe Ca C/,
);
# blainsneog[blainsneog/Noun:Fem:Com:Sg, blainsneog/Noun:Fem:Gen:Weak:Pl,
# mblainsneog[blainsneog/Noun:Fem:Com:Sg:Ecl,blainsneog/Noun:Fem:Gen:Weak:Pl:DefArt,blainsneog/Noun:Fem:Gen:Weak:Pl:Ecl
# bhlainsneog[bhlainsneog/Noun:Fem:Com:Sg:DefArt,blainsneog/Noun:Fem:Com:Sg:Len,blainsneog/Noun:Fem:Gen:Weak:Pl:Len,blainsneog/Noun:Fem:Voc:Sg:Len
# blainsneoige[blainsneog/Noun:Fem:Gen:Sg,blainsneog/Noun:Fem:Gen:Sg:DefArt
# bhlainsneoige[blainsneog/Noun:Fem:Gen:Sg:Len
# mblainsneoige[blainsneog/Noun:Fem:Gen:Sg:Ecl, </S>]
# blainsneoga[blainsneog/Noun:Fem:Com:Pl,blainsneog/Noun:Fem:Com:Pl:DefArt,blainsneog/Noun:Fem:Voc:Pl,blainsneog/Noun:Fem:Voc:Pl:DefArt
# mblainsneoga[blainsneog/Noun:Fem:Com:Pl:Ecl

# Sheáin[Seáin/Prop:Noun:Fem:Gen:Sg:Len*,Seán/Noun:Masc:Gen:Sg:Len*,Seán/Noun:Masc:Voc:Sg:Len*,Seán/Prop:Noun:Masc:Gen:Len*,Seán/Prop:Noun:Masc:Voc:Len

# dorcán[dorcán/Noun:Masc:Com:Sg,dorcán/Noun:Masc:Com:Sg:DefArt,dorcán/Noun:Masc:Gen:Weak:Pl
# dhorcán[dorcán/Noun:Masc:Com:Sg:Len, dorcán/Noun:Masc:Gen:Weak:Pl:Len, </S>]
# ndorcán[dorcán/Noun:Masc:Com:Sg:Ecl*,dorcán/Noun:Masc:Gen:Weak:Pl:DefArt,dorcán/Noun:Masc:Gen:Weak:Pl:Ecl
# dorcáin[dorcán/Noun:Masc:Com:Pl*,dorcán/Noun:Masc:Com:Pl:DefArt*,dorcán/Noun:Masc:Gen:Sg
# dhorcáin[dorcán/Noun:Masc:Com:Pl:Len,dorcán/Noun:Masc:Gen:Sg:DefArt,dorcán/Noun:Masc:Gen:Sg:Len,dorcán/Noun:Masc:Voc:Sg:Len
# ndorcáin[dorcán/Noun:Masc:Com:Pl:Ecl, dorcán/Noun:Masc:Gen:Sg:Ecl

my %reliables = (
  'dóir' => ['dóir', 'dóra', 'dóirí', 'dóirí'],
);


