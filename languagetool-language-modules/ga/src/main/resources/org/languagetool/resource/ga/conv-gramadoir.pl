#!/usr/bin/perl

use warnings;
use strict;
use utf8;

binmode(STDIN, ":utf8");
binmode(STDOUT, ":utf8");

my %TOKEN = (
    '<A>ANYTHING</A>' => '<token postag="Adj:.*" postag_regexp="yes"></token>',
    '<N>ANYTHING</N>' => '<token postag=".*Noun.*" postag_regexp="yes"></token>',
    '<N>UNLENITED</N>' => '<token postag=".*Noun.*" postag_regexp="yes"><exception postag="*:Len" postag_regexp="yes"/></token>',
    '<A>UNLENITED</A>' => '<token postag="Adj:.*" postag_regexp="yes"><exception postag="*:Len" postag_regexp="yes"/></token>',
    '<N pl="n" gnt="n" gnd="f">ECLIPSED</N>' => '<token postag="(?:C[UMC]:)?Noun:Fem:Com:Sg:Ecl" postag_regexp="yes"></token>',
);

my %PARTTOKEN = (
    '<V cop="y">' => 'Cop:.*',
    '<N pl="n" gnt="n" gnd="f">' => '(?:C[UMC]:)?Noun:Fem:Com:Sg',
    '<N pl="n" gnt="n" gnd="m">' => '(?:C[UMC]:)?Noun:Masc:Com:Sg',
    '<N pl="n" gnt="y" gnd="f">' => '(?:C[UMC]:)?Noun:Fem:Gen:Sg',
    '<N pl="n" gnt="y" gnd="m">' => '(?:C[UMC]:)?Noun:Masc:Gen:Sg',
    '<N pl="y" gnt="n" gnd="f">' => '(?:C[UMC]:)?Noun:Fem:Com:Pl',
    '<N pl="y" gnt="n" gnd="m">' => '(?:C[UMC]:)?Noun:Masc:Com:Pl',
    '<N pl="y" gnt="y" gnd="f">' => '(?:C[UMC]:)?Noun:Fem:Gen:Pl',
    '<N pl="y" gnt="y" gnd="m">' => '(?:C[UMC]:)?Noun:Masc:Gen:Pl',
);

my %POS = (
    'A' => 'Adj:.*',
    'N' => '.*Noun.*',
    'NG' => '.*Noun.*:Gen.*',
);

while(<>) {
	chomp;
	s/\[Aa\]/a/g;
	s/\[Áá\]/á/g;
	s/\[Bb\]/b/g;
	s/\[Cc\]/c/g;
	s/\[Dd\]/d/g;
	s/\[Ee\]/e/g;
	s/\[Ff\]/f/g;
	s/\[Gg\]/g/g;
	s/\[Hh\]/h/g;
	s/\[Ii\]/i/g;
	s/\[Ll\]/l/g;
	s/\[Mm\]/m/g;
	s/\[Nn\]/n/g;
	s/\[Oo\]/o/g;
	s/\[Óó\]/ó/g;
	s/\[Pp\]/p/g;
	s/\[Rr\]/r/g;
	s/\[Ss\]/s/g;
	s/\[Tt\]/t/g;
	s/\[Uu\]/u/g;
	s/\[Úú\]/ú/g;

    next if(/^#/);
    if(/^s\/([^\/]*)[\/](.*)\/g;$/) {
        my $name = $1;
        my $regex = $2;
        $name = lc($name);
        $regex =~ s/\[\^<\]\+/.+/g;
        $regex =~ s/\[\^<\]\*/.*/g;
        print "        <!ENTITY $name \"$regex\">\n";
    } else {
        print "Missed: $_\n";
    }
}
