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
for my $word (qw/a de mar ó roimh trí/) {
    my $upper = uc(unfada($word));
my $out=<<__END__;
        <rule id="${upper}_NOUN" name="$word NOUN">
            <pattern>
                <token>$word</token>
                <marker>
                    <token postag=".*Noun.*" postag_regexp="yes"><exception postag=".*:Len" postag_regexp="yes"/></token>
                </marker>
            </pattern>
            <message>Séimhiú ar iarraidh: <suggestion><match no="2" postag="([^:]*):(.*)" postag_regexp="yes" postag_replace="\$1:\$2:Len"/></suggestion></message>
            <example correction='bhaile'>$word <marker>baile</marker></example>
        </rule>
__END__
    print $out;
}