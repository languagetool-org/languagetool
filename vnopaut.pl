#!/usr/bin/perl

use warnings;
use strict;
use utf8;

binmode(STDOUT, ":utf8");

sub unfada {
    my $in = shift;
    $in =~ s/á/a/g;
    $in =~ s/é/e/g;
    $in =~ s/í/i/g;
    $in =~ s/ó/o/g;
    $in =~ s/ú/u/g;
    return $in;
}
for my $word (qw/ár bhur dár faoinár inár lenár ónár trínár/) {
    my $upper = uc(unfada($word));
my $out=<<__END__;
        <rule id="${upper}_DHA_VERB" name="$word dhá VERB">
            <pattern>
                <token>$word</token>
                <token>dhá</token>
                <marker>
                    <token postag=".*NOUN.*" postag_regexp="yes"><exception postag=".*:Ecl" postag_regexp="yes"/></token>
                </marker>
            </pattern>
            <message>Urú ar iarraidh: <suggestion><match no="3" postag="([^:]*):(.*)" postag_regexp="yes" postag_replace="\$1:\$2:Ecl"/></suggestion></message>
            <example correction='dteach'>$word dhá <marker>teach</marker></example>
        </rule>
__END__
    print $out;
}