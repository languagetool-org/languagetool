#!/usr/bin/perl
# Filter An Gramadóir's equivalent of replacements for items that have been categorised
# Copyright 2018 Jim O'Regan (MIT/LGPL)
use warnings;
use strict;
use utf8;
use FindBin qw($RealBin);

open(PLACES, '<', "$RealBin/../../rules/ga/placenames.txt") or die "placenames.txt: $!\n";
open(PEOPLE, '<', "$RealBin/../../rules/ga/names.txt") or die "names.txt: $!\n";
open(REPL, '<', "$RealBin/../../rules/ga/replace.txt") or die "replace.txt: $!\n";
open(EARR, '<', "$RealBin/earraidi-ga.bs") or die "earraidi-ga.bs: $!\n";
open(EILE, '<', "$RealBin/eile-ga.bs") or die "eile-ga.bs: $!\n";
open(OEILE, '>', "$RealBin/eile.txt") or die "eile.txt: $!\n";
open(OEARR, '>', "$RealBin/earraidi.txt") or die "earraidi.txt: $!\n";
binmode(STDOUT, ":utf8");
binmode(PLACES, ":utf8");
binmode(PEOPLE, ":utf8");
binmode(REPL, ":utf8");
binmode(EARR, ":utf8");
binmode(EILE, ":utf8");
binmode(OEARR, ":utf8");
binmode(OEILE, ":utf8");

my %filt = ();
my %earr = ();
my %eile = ();

while(<PLACES>) {
    chomp;
    my ($l, $r) = split/=/;
    for my $in (split/\|/, $l) {
        for my $out (split/\|/, $r) {
            my $arr = [];
            push(@$arr, $out);
            $filt{$in} = $arr;
        }
    }
}

while(<PEOPLE>) {
    chomp;
    my ($l, $r) = split/=/;
    for my $in (split/\|/, $l) {
        for my $out (split/\|/, $r) {
            my $arr = [];
            push(@$arr, $out);
            $filt{$in} = $arr;
        }
    }
}

while(<REPL>) {
    chomp;
    my ($l, $r) = split/=/;
    for my $in (split/\|/, $l) {
        for my $out (split/\|/, $r) {
            my $arr = [];
            push(@$arr, $out);
            $filt{$in} = $arr;
        }
    }
}

while(<EARR>) {
    chomp;
    my @arr = split/ /;
    my $first = shift @arr;
    my $rest = join(' ', @arr);
    if(!exists $filt{$first}) {
        if (!exists $earr{$first}) {
            my $tmpa = [];
            $earr{$first} = $tmpa;
        }
        push(@{$earr{$first}}, $rest);
    }
}

while(<EILE>) {
    chomp;
    my @arr = split/ /;
    my $first = shift @arr;
    my $rest = join(' ', @arr);
    if(!exists $filt{$first}) {
        if (!exists $earr{$first}) {
            my $tmpa = [];
            $eile{$first} = $tmpa;
        }
        push(@{$eile{$first}}, $rest);
    }
}

for my $kear (keys %earr) {
    print OEARR "$kear=" . join('|', @{$earr{$kear}}) . "\n";
}

for my $keil (keys %eile) {
    print OEILE "$keil=" . join('|', @{$eile{$keil}}) . "\n";
}
