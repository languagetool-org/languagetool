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

my %POS = (
    'A' => 'Adj:.*';
    'N' => '.*Noun.*';
    'NG' => '.*Noun.*:Gen.*';
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

	if(/([^ ]+) <N[^>]+>([^<]+)<\/[^>]*>:BACHOIR\{([^\}]+)\}/) {
		my $num = $1;
		my $word = $2;
		my $repl = $3;

		my $titlenum = uc($num);
		$titlenum =~ s/.\?//g;
		my $titleword = uc($word);
		my $title = $titlenum . '_' . $titleword;

		my $egnum = $num;
		$egnum =~ s/.\?//g;

my $out=<<__END__;
        <rule id="$title" name="$egnum $word">
            <pattern>
                <token regexp="yes">$num</token>
                <marker>
                    <token>$word</token>
                </marker>
            </pattern>
            <message>Ba chóir duit <suggestion>$repl</suggestion> a scríobh.</message>
            <example correction='$repl'>$egnum <marker>$word</marker></example>
        </rule>
__END__

	print $out;
	}
}
