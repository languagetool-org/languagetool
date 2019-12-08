#!/usr/bin/perl
use warnings;
use strict;
use utf8;

binmode(STDIN, ":utf8");
binmode(STDOUT, ":utf8");

sub unfada {
    my $word = shift;
    $word =~ s/á/a/g;
    $word =~ s/é/e/g;
    $word =~ s/í/i/g;
    $word =~ s/ó/o/g;
    $word =~ s/ú/u/g;
    $word;
}

while(<>) {
    chomp;
    my ($in, $out) = split/=/;
    next if($in !~ /'/);
    write_rule($in, $out);
}

sub write_rule {
    my $in = shift;
    my $out = shift;
    my ($tok1, $tok2) = split/'/, $in;
    my $utok1 = uc(unfada($tok1));
    my $utok2 = uc(unfada($tok2));
    print<<__END__;
        <rule id="${utok1}_APOS_$utok2" name="$in">
            <pattern>
                <token>$tok1</token>
                <token spacebefore="no" regexp="yes">&apost;</token>
                <token spacebefore="no">$tok2</token>
            </pattern>
            <message>An <suggestion>$out</suggestion> a bhí i gceist agat?</message>
            <example correction="$out"><marker>$in</marker></example>
        </rule>
__END__
}